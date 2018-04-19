package com.abe.jason.rateme.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.abe.jason.rateme.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment.java";

    private Set<View> profileViews;
    private RatingBar mRatingBar;
    private TextView firstTwo;
    private TextView lastTwo;
    private TextView nameTextView;
    private ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.frament_home, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            updateState();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        profileViews = new HashSet();
        mRatingBar = view.findViewById(R.id.myRatingBar);
        firstTwo = view.findViewById(R.id.myRatingFirst2);
        lastTwo = view.findViewById(R.id.myRatingLast2);
        nameTextView = view.findViewById(R.id.myName);
        mProgressBar = view.findViewById(R.id.progress_loader);
        profileViews.add(mRatingBar); profileViews.add(firstTwo); profileViews.add(lastTwo); profileViews.add(nameTextView);

        for(View currView : profileViews)
            currView.setVisibility(View.INVISIBLE);

        if(MainActivity.mFirebaseDatabase != null)
            updateState();

        view.findViewById(R.id.btn_recognize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FaceTrackerActivity.class);
                intent.putExtra("method", "recognize");
                startActivityForResult(intent, 1);
            }
        });
    }

    public void updateState() {
        // will update any time the user's rating is changed
        MainActivity.mFirebaseDatabase.child("users").child(MainActivity.mFireBaseUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String name = "" + dataSnapshot.child("name").getValue();
                    nameTextView.setText(name);
                    try {
                        float rating = Float.parseFloat("" + dataSnapshot.child("rating").getValue());
                        String ratingAsString = String.format(Locale.getDefault(), "%.3f", rating);
                        int decimal = ratingAsString.indexOf(".");
                        firstTwo.setText(ratingAsString.substring(0, decimal + 2));
                        lastTwo.setText(ratingAsString.substring(decimal + 2));
                        mRatingBar.setRating(rating);
                        for(View currView : profileViews)
                            currView.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                    } catch (NumberFormatException e) { }
                }
                else {
                    Log.d(TAG, "doesn't exist");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }
}
