package com.abe.jason.rateme.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.abe.jason.rateme.BuildConfig;
import com.abe.jason.rateme.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity.java";

    public static final String KAIROS_FACE_API_ID = BuildConfig.KAIROS_FACE_API_ID; // loaded from gradle
    public static final String KAIROS_FACE_API_KEY = BuildConfig.KAIROS_FACE_API_KEY;
    public static final OkHttpClient client = new OkHttpClient();

    // Firebase variables
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    public static DatabaseReference mFirebaseDatabase;
    public static String mFireBaseUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, go back to the sign in activity
            finish();
        } else {
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
            mFireBaseUserId = mFirebaseAuth.getCurrentUser().getUid();

            // check to see if the user has completed registration by reading a flag from the db
            mFirebaseDatabase.child("users").child(mFireBaseUserId).addListenerForSingleValueEvent(new ValueEventListener() { // reads the data once
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) { // the very existence of this snapshot means the user has registered, we're good to go. mark them as active
                        mFirebaseDatabase.child("users").child(mFireBaseUserId).child("active").setValue(true);
                    }
                    else {
                        // TODO launch registration activity
                        //startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, "Couldn't connect to database, try again later or check your internet connection", Toast.LENGTH_LONG).show();
                    signOut();
                }
            });
        }

        // logout button
        findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            signOut();
            }
        });

        // camera buttons
        findViewById(R.id.btn_enroll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FaceTrackerActivity.class);
                intent.putExtra("method", "enroll");
                startActivity(intent);
            }
        });
        findViewById(R.id.btn_recognize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FaceTrackerActivity.class);
                intent.putExtra("method", "recognize");
                startActivity(intent);
            }
        });
    }

    private void signOut() {
        // Firebase sign out
        mFirebaseDatabase.child("users").child(mFireBaseUserId).child("active").setValue(false); // set user to inactive
        mFirebaseAuth.signOut();
        Log.d(TAG, "Current User: " + mFirebaseAuth.getCurrentUser());

        // Google sign out
        LoginActivity.mGoogleApiClient.connect();
        LoginActivity.mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                Auth.GoogleSignInApi.signOut(LoginActivity.mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                finish();
                                // Get sign out result
                            }
                        });
            }

            @Override
            public void onConnectionSuspended(int i) {
            }
        });
    }
}
