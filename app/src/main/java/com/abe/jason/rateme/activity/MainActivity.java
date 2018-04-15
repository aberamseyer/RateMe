package com.abe.jason.rateme.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

        setupBottomNavigation();
    }

    @Override
    public void onBackPressed() {
        signOut();
    }

    public void signOut() {
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
                                Toast.makeText(MainActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();
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

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {

            // Select first menu item by default and show Fragment accordingly.
            Menu menu = bottomNavigationView.getMenu();
            selectFragment(menu.getItem(0));

            // Set action to perform when any menu-item is selected.
            bottomNavigationView.setOnNavigationItemSelectedListener(
                    new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            selectFragment(item);
                            return false;
                        }
                    });
        }
    }

    protected void selectFragment(MenuItem item) {

        item.setChecked(true);

        switch (item.getItemId()) {
            case R.id.action_home:
                // Action to perform when Home Menu item is selected.
                pushFragment(new HomeFragment());
                break;
            /*case R.id.action_camera:
                // Action to perform when Camera Menu item is selected.
                Intent intent = new Intent(MainActivity.this, FaceTrackerActivity.class);
                intent.putExtra("method", "recognize");
                startActivity(intent);
                break;*/
            case R.id.action_account:
                // Action to perform when Account Menu item is selected.
                pushFragment(new ProfileFragment());
                break;
        }
    }

    protected void pushFragment(Fragment fragment) {
        if (fragment == null)
            return;

        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            if (ft != null) {
                ft.replace(R.id.rootLayout, fragment);
                ft.commit();
            }
        }
    }
}
