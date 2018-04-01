package com.abe.jason.rateme.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.abe.jason.rateme.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{

    private static final String TAG = "LoginActivity.java";
    private static final int RC_SIGN_IN = 9001;

    private final String SERVER_CLIENT_ID = "662091126758-hufdf9c7s11bk6vqhlu9dh226kudvh9b.apps.googleusercontent.com";

    private SignInButton mSignInButton;

    public static GoogleApiClient mGoogleApiClient;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Handler mHandler;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        /* create a thread to check if this is the first launch, start the introduction if it is */
        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (/*true*/isFirstStart) { // todo change comment for development purposes

                    //  Launch app intro
                    final Intent i = new Intent(LoginActivity.this, Introduction.class);

                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            startActivity(i);
                        }
                    });

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    //  Apply changes
                    e.apply();
                }
            }
        });

        // Start the thread
        t.start();

        // Assign fields
        mSignInButton = findViewById(R.id.sign_in_button);

        // Set click listeners
        mSignInButton.setOnClickListener(this);

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(SERVER_CLIENT_ID)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    finish();
                }
            }
        };

        // create handler to sign the user in silently after showing the activity
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                OptionalPendingResult<GoogleSignInResult> pendingResult =
                        Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
                if (pendingResult.isDone()) {
                    // There's immediate result available.
                    Log.d(TAG, "Silent Sign In: " + pendingResult.get().getSignInAccount() + "\nFirebase Auth: " + mFirebaseAuth.getCurrentUser());
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } else {
                    // There's no immediate result ready, displays some progress indicator and waits for the
                    // async callback.
                    pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                        @Override
                        public void onResult(@NonNull GoogleSignInResult result) {
                            // wait for user to log in normally
                            Log.d(TAG, "Silent Sign In: no user currently signed in");
                        }
                    });
                }
            }
        }, 250);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(..)
        if(requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()) {
                //Google sign-in was successful, authenticate with firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // sign in failed
                Log.e(TAG, "google sign-in failed, might be SHA1 key if app is unsigned");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // if sign-in fails, display message to user. if sign in successfull
                        // the auth state listener will be notified and logic to handle
                        // the signed in user can be handled in the listener
                        if(!task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Login failed, you might not be connect to the Internet", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "signInSuccess");
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }
                    }
                });
    }
}
