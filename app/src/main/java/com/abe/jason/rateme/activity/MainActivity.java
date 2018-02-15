package com.abe.jason.rateme.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.abe.jason.rateme.BuildConfig;
import com.abe.jason.rateme.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "FaceGraphic.java";

    public static final String KAIROS_FACE_API_ID = BuildConfig.KAIROS_FACE_API_ID; // loaded from gradle
    public static final String KAIROS_FACE_API_KEY = BuildConfig.KAIROS_FACE_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Firebase sign out
                FirebaseAuth.getInstance().signOut();
                Log.d(TAG, "Current User: " + FirebaseAuth.getInstance().getCurrentUser());

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
        });
        findViewById(R.id.btn_launch_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FaceTrackerActivity.class));
            }
        });
    }
}
