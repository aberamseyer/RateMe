package com.abe.jason.rateme.activity;

import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abe.jason.rateme.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment.java";

    public static MediaPlayer mp1;

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
           // Toast.makeText(getActivity(), "ACTIVITY RESULT", Toast.LENGTH_SHORT).show();
           //updateState();
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

        //Toast.makeText(getActivity(), "VIEW CREATED", Toast.LENGTH_SHORT).show();


        for(View currView : profileViews)
            currView.setVisibility(View.INVISIBLE);

        if(MainActivity.mFirebaseDatabase != null) {
            updateState();

        }


        view.findViewById(R.id.btn_face).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FaceTrackerActivity.class);
                intent.putExtra("method", "recognize");
                startActivityForResult(intent, 1);
            }
        });
        view.findViewById(R.id.btn_nearby).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), FindNearbyDevices.class));
            }
        });
        /*view.findViewById(R.id.btn_recognize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FaceTrackerActivity.class);
                intent.putExtra("method", "recognize");
                startActivityForResult(intent, 1);
            }
        });*/
    }

    public void updateState() {
        // will update any time the user's rating is changed
        MainActivity.mFirebaseDatabase.child("users").child(MainActivity.mFireBaseUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String name = "" + dataSnapshot.child("name").getValue();
                    nameTextView.setText(name);
                    float rating;
                    try {
                        rating = Float.parseFloat("" + dataSnapshot.child("rating").getValue());

                        //Toast.makeText(getActivity(), Float.toString(rating), Toast.LENGTH_SHORT).show();
                        try {
                            if(mp1 != null) mp1.release();
                            switch ((int) rating) {
                                case 5:
                                    mp1 = MediaPlayer.create(getActivity(), R.raw.star5);
                                    break;
                                case 4:
                                    mp1 = MediaPlayer.create(getActivity(), R.raw.star4);
                                    break;
                                case 3:
                                    mp1 = MediaPlayer.create(getActivity(), R.raw.star3);
                                    break;
                                case 2:
                                    mp1 = MediaPlayer.create(getActivity(), R.raw.star2);
                                    break;
                                default:
                                    mp1 = MediaPlayer.create(getActivity(), R.raw.star1);
                                    break;
                            }
                            mp1.start();
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }

                        String ratingAsString = String.format(Locale.getDefault(), "%.3f", rating);
                        int decimal = ratingAsString.indexOf(".");
                        startCountAnimation(firstTwo, 0, Float.parseFloat(ratingAsString.substring(0, decimal + 2)), 400, 1);
//                        firstTwo.setText(ratingAsString.substring(0, decimal + 2));
//                        lastTwo.setText(ratingAsString.substring(decimal + 2));
                        if(Integer.parseInt(ratingAsString.substring(decimal + 2)) == 0) {
                            lastTwo.setText("00");
                        }
                        else {
                            startCountAnimation(lastTwo, 0, Integer.parseInt(ratingAsString.substring(decimal + 2)), 700, 0);
                        }
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

    private void startCountAnimation(final TextView textView, float start, float end, int duration, final int precision) {
        ValueAnimator animator = ValueAnimator.ofFloat(start, end);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                textView.setText(String.format("%." + precision + "f", animation.getAnimatedValue()));
            }
        });
        animator.start();
    }
}
