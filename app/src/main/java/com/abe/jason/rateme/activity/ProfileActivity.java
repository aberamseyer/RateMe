package com.abe.jason.rateme.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abe.jason.rateme.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private String name;
    private Double rating;
    private String userID;

    private TextView nameLabel;
    private TextView ratingLabel;
    private Button submitButton;
    private RatingBar ratingBar;
    private TextView instructionText;

    // Firebase variables
    public static DatabaseReference mFirebaseDatabase;
    public static String mFireBaseUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ArrayList<Double> defaultList = new ArrayList<Double>(1);
        setContentView(R.layout.profile_view);

        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Intent intent = getIntent();
        this.name = intent.getStringExtra("passedName");
        this.rating = intent.getDoubleExtra("passedRating", 0);
        this.userID = intent.getStringExtra("recognizedID");

        this.nameLabel = findViewById(R.id.nameLabel);
        this.ratingLabel = findViewById(R.id.ratingLabel);
        this.submitButton = findViewById(R.id.submitRatingButton);
        this.ratingBar = findViewById(R.id.giveRatingBar);
        this.instructionText = findViewById(R.id.instructionTextView);

        nameLabel.setText(this.name);
        ratingLabel.setText(Double.toString(this.rating));

        mFirebaseDatabase = MainActivity.mFirebaseDatabase;
        mFireBaseUserId = this.userID;

        mFirebaseDatabase
        .child("users")
        .child(mFireBaseUserId)
        .addListenerForSingleValueEvent(new ValueEventListener() { // reads the data once
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) { // the very existence of this snapshot means the user has registered, we're good to go. mark them as active
                    ArrayList<Double> thelist = (ArrayList<Double>) dataSnapshot.child("ratings").getValue();
                    if(thelist == null)
                        thelist = defaultList;
                    /*Toast.makeText(
                        ProfileActivity.this,
                        thelist.toString(),
                        Toast.LENGTH_LONG)
                    .show();*/
                    updateRatingAndCount(thelist);
                } else {
                    // ???
                    // pretty much
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Couldn't connect to database, try again later or check your internet connection", Toast.LENGTH_LONG).show();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final double newRating;

                DecimalFormat df = new DecimalFormat("#.##");
                df.setRoundingMode(RoundingMode.CEILING);

                //only 2 decimal places
                newRating = Double.parseDouble(df.format(ratingBar.getRating()));

                //to update face tracker and home activity
                rating = Double.parseDouble(df.format(ratingBar.getRating()));

                mFirebaseDatabase
                .child("users")
                .child(mFireBaseUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() { // reads the data once
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) { // the very existence of this snapshot means the user has registered, we're good to go. mark them as active
                            ArrayList<Double> thelist = (ArrayList<Double>) dataSnapshot.child("ratings").getValue();
//                            Toast.makeText(
//                                    ProfileActivity.this,
//                                    thelist.toString(),
//                                    Toast.LENGTH_LONG)
//                                    .show();
                            if(thelist == null) {
                                thelist = defaultList;
                            }
                            thelist.add(newRating);

                            updateRatings(thelist);
                            updateRatingAndCount(thelist);
                        } else {
                            //???
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ProfileActivity.this, "Couldn't connect to database, try again later or check your internet connection", Toast.LENGTH_LONG).show();
                    }
                });

                thankForRating();
            }
        });
    }

    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();

        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", rating);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    public double calculateAverage(ArrayList<Double> values) {
        if(values.size() < 12) { // small number of rating will be a simple average
            double total = 0;
            for (int i = 0; i < values.size(); i++) {
                //Firebase returns doubles and longs, can't just cast
                String s = values.get(i) + "";
                double d = Double.parseDouble(s);

                total += d;
            }
            return values.size() != 0 ? total / values.size() : 0;
        } else { //moving average, not exponential bc lazy
            Double d = new Double (values.size() * 0.50);
            int range = d.intValue(); //average the last half of the values
            double total = 0;
            for(int i = range; i < values.size(); i++) {
                String s = values.get(i) + "";
                double d2 = Double.parseDouble(s);

                total += d2;
            }
            return values.size() != 0 ? total / range : 0;
        }
    }

    public void updateRatingAndCount(ArrayList<Double> list) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        //only 2 decimal places
        Double rounded = Double.parseDouble(df.format(calculateAverage(list)));
        this.ratingLabel.setText(rounded.toString());

        mFirebaseDatabase
                .child("users")
                .child(mFireBaseUserId)
                .child("rating")
                .setValue(rounded);
        mFirebaseDatabase
                .child("users")
                .child(mFireBaseUserId)
                .child("numberOfRatings")
                .setValue(list.size());
    }

    public void updateRatings(ArrayList<Double> list) {
        mFirebaseDatabase
                .child("users")
                .child(mFireBaseUserId)
                .child("ratings")
                .setValue(list);
        /*Toast.makeText(
            ProfileActivity.this,
            "Ratings are now: " + list.toString(),
            Toast.LENGTH_LONG)
        .show();*/
    }

    public void thankForRating() {
//        this.instructionText.setText("Thanks for the rating!");
//        this.ratingBar.setVisibility(View.INVISIBLE);
//        this.submitButton.setEnabled(false);
        Toast.makeText(ProfileActivity.this, "Thanks for the rating!", Toast.LENGTH_SHORT).show();
        finish();
    }
}