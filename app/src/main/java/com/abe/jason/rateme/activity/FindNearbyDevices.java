package com.abe.jason.rateme.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.abe.jason.rateme.R;
import com.abe.jason.rateme.ui.ListAdapter;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.ArrayList;

public class FindNearbyDevices extends AppCompatActivity {
    private static final String TAG = "FindNearbyDevices.java";
    private Moshi moshi = new Moshi.Builder().build();
    private JsonAdapter<User> jsonAdapter = moshi.adapter(User.class);

    private MessageListener mMessageListener;
    private Message mMessage;
    private ArrayList<User> usersInList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_users);
        ListView lvFoundUsers = findViewById(R.id.list_nearby);
        final ListAdapter mListAdapter = new ListAdapter(this, usersInList);
        lvFoundUsers.setAdapter(mListAdapter);

        lvFoundUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(FindNearbyDevices.this, ProfileActivity.class);
                intent.putExtra("passedName", usersInList.get(position).getName());
                intent.putExtra("passedRating", usersInList.get(position).getRating());
                intent.putExtra("recognizedID", usersInList.get(position).getId());
                startActivityForResult(intent, 1);
            }
        });

        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                User decodedUser = new User("", "", 0);
                try {
                    decodedUser = jsonAdapter.fromJson(new String(message.getContent()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "Found message: " + decodedUser);
                boolean flag = false;
                for(User user : usersInList)
                    if(user.getId().equals(decodedUser.getId()))
                        flag = true;
                if(!flag) {
                    usersInList.add(decodedUser);
                    mListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onLost(Message message) {
                User decodedUser = new User("", "", 0);
                try {
                    decodedUser = jsonAdapter.fromJson(new String(message.getContent()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "Lost sight of message: " + new String(message.getContent()));
                // remove the user from the list
                for(int i = 0; i < usersInList.size(); i++)
                    if(usersInList.get(i).getId().equals(decodedUser.getId())) {
                        usersInList.remove(i);
                        mListAdapter.notifyDataSetChanged();
                    }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();

        MainActivity.mFirebaseDatabase.child("users").child(MainActivity.mFireBaseUserId).addListenerForSingleValueEvent(new ValueEventListener() { // reads the data once
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) { // the very existence of this snapshot means the user has registered, we're good to go. mark them as active
                    String name = "" + dataSnapshot.child("name").getValue();
                    double rating = 0;
                    try {
                        rating = Double.parseDouble("" + dataSnapshot.child("rating").getValue());
                    } catch (NumberFormatException e) { /* leave it at 0 on error */ }
                    mMessage = new Message(jsonAdapter.toJson(new User(MainActivity.mFireBaseUserId, name, rating)).getBytes());

                    Nearby.getMessagesClient(FindNearbyDevices.this).publish(mMessage,
                            new PublishOptions.Builder()
                                    .setStrategy(new Strategy.Builder()
                                            .setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT)
                                            .build())
                                    .setCallback(new PublishCallback() {
                                        @Override
                                        public void onExpired() {
                                            messageError("Action cancelled by user");
                                            finish();
                                        }
                                    })
                                    .build()
                    );
                    Log.d(TAG, "published: " + new String(mMessage.getContent()));
                    Nearby.getMessagesClient(FindNearbyDevices.this).subscribe(mMessageListener);
                }
                else {
                    messageError("You don't have a name");
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    @Override
    public void onStop() {
        Nearby.getMessagesClient(this).unpublish(mMessage);
        Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
        Log.d(TAG, "un-published: " + new String(mMessage.getContent()));
        super.onStop();
    }

    protected void messageError(String error) {
        Toast.makeText(FindNearbyDevices.this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        finish();
    }


    public static final class User {
        private String name = "";
        private String id = "";
        private double rating = 0;

        User(String id, String name, double rating) {
            this.name = name;
            this.id = id;
            this.rating = rating;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public double getRating() {
            return rating;
        }

        public void setRating(double rating) {
            this.rating = rating;
        }

        @Override
        public String toString() {
            return "User{" +
                    "name='" + name + '\'' +
                    ", id='" + id + '\'' +
                    ", rating=" + rating +
                    '}';
        }
    }
}
