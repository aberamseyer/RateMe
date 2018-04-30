/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.abe.jason.rateme.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.abe.jason.rateme.R;
import com.abe.jason.rateme.kairos.enroll.EnrollRequest;
import com.abe.jason.rateme.kairos.recognize.RecognizeRequest;
import com.abe.jason.rateme.ui.camera.CameraSourcePreview;
import com.abe.jason.rateme.ui.camera.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class FaceTrackerActivity extends AppCompatActivity implements ViewSwitcher.ViewFactory {
    private static final String TAG = "FaceTrackerActivity";

    private FloatingActionButton profileViewBtn;

    private CameraSource mCameraSource = null;
    private boolean hasSubmitted = false;   // flag that ensures we only send arrow_first request per unique face
    private long lastSubmittedTime = System.currentTimeMillis()-1000;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static String detectedUserID = "";

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private GestureDetectorCompat mDetector;
    private ImageView rateArrow;

    private boolean notAllowedRating = false;
    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.camera_preview);

        mPreview = findViewById(R.id.preview);
        ImageView switchBtn = findViewById(R.id.btn_switch_camera);
        switchBtn.bringToFront();
        if(getIntent().getStringExtra("method").equals("enroll")) {
            switchBtn.setVisibility(View.GONE);
        }
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCameraSource(true);
            }
        });
        mGraphicOverlay = findViewById(R.id.faceOverlay);
        mDetector = new GestureDetectorCompat(this, new MyGestureListener());

//        findViewById(R.id.btn_nearby).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(FaceTrackerActivity.this, FindNearbyDevices.class));
//            }
//        });
        rateArrow = findViewById(R.id.swipe_up_to_rate);
        rateArrow.setVisibility(View.INVISIBLE);
        /*
        imageSwitcher = findViewById(R.id.arrow_switcher);
        imageSwitcher.setFactory(this);
        imageSwitcherHandler = new Handler(Looper.getMainLooper());
        imageSwitcher.setVisibility(View.INVISIBLE);
        imageSwitcherHandler.post(new Runnable() { // switches the arrow between the two versions of it automatically
            @Override
            public void run() {
                int delayed = 0;
                switch (animationCounter++) {
                    case 1:
                        imageSwitcher.setImageResource(R.drawable.arrow_first);
                        delayed = 650;
                        break;
                    case 2:
                        imageSwitcher.setImageResource(R.drawable.arrow_second);
                        delayed = 300;
                        break;
                }
                animationCounter %= 3;
                if(animationCounter == 0 ) animationCounter = 1;

                imageSwitcherHandler.postDelayed(this, delayed);
            }
        }); */
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(false);
        } else {
            requestCameraPermission();
        }

        findViewById(R.id.btn_camera_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FaceGraphic.info = "";
                FaceGraphic.rating = 0.0f;
                FaceGraphic.name = "";
                rateArrow.setVisibility(View.INVISIBLE);
//                try { profileViewBtn.setVisibility(View.INVISIBLE); } catch (Exception e) {}
                finish();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }


    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource(boolean switched) {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setProminentFaceOnly(true)
                .setMinFaceSize(0.6f)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }
        if(switched) {
            int facing = mCameraSource.getCameraFacing();
            mCameraSource.release();

            mCameraSource = new CameraSource.Builder(context, detector)
                    .setRequestedPreviewSize(720, 1280)
                    .setFacing(facing == CameraSource.CAMERA_FACING_BACK ?
                            CameraSource.CAMERA_FACING_FRONT : CameraSource.CAMERA_FACING_BACK)
                    .setRequestedFps(30.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            mPreview.stop();
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
        else {
            mCameraSource = new CameraSource.Builder(context, detector)
                    .setRequestedPreviewSize(720, 1280)
                    .setFacing(getIntent().getStringExtra("method").equals("enroll") ? // use the front camera to enroll faces
                            CameraSource.CAMERA_FACING_FRONT : CameraSource.CAMERA_FACING_BACK) //switching this for testing
                    .setRequestedFps(30.0f)
                    .setAutoFocusEnabled(true)
                    .build();
        }
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource(false);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finishAffinity();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Camera Permission was Denied")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }


    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> implements AsyncCallback {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;
        private String recognizedUserID;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay, getApplicationContext());
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {

        }

        /**
         * Update the position/characteristics of the face within the overlay.
         * Only outline the largest face detected
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
            long now = System.currentTimeMillis();
            if (mFaceGraphic.inBounds && !hasSubmitted && (now - lastSubmittedTime) > 1500) { // face is on the screen, we haven't already tried this face, and some time delay has passed
                lastSubmittedTime = now;
                hasSubmitted = true;
                mCameraSource.takePicture(new CameraSource.ShutterCallback() {
                    @Override
                    public void onShutter() {

                    }
                }, new CameraSource.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes) {
                        bytes = scaleDown(bytes);

                        switch ("" + getIntent().getStringExtra("method")) {
                            case "enroll":
                                new EnrollRequest(bytes, FaceTrackerActivity.this).execute();
                                break;
                            case "recognize":
                                new RecognizeRequest(bytes, GraphicFaceTracker.this).execute();
                                break;
                        }

                    }
                });

            }
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            FaceTrackerActivity.detectedUserID = "";
            rateArrow.setVisibility(View.INVISIBLE);
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
            FaceGraphic.info = "";
            FaceGraphic.rating = 0.0f;
            FaceGraphic.name = "";
            FaceTrackerActivity.detectedUserID = "";
            rateArrow.setVisibility(View.INVISIBLE);
//            try { profileViewBtn.setVisibility(View.INVISIBLE); } catch (Exception e) {}
            hasSubmitted = false;
            Log.d(TAG, "onDone()");
        }

        @Override
        public void recognizeResponse(String id) {
            FaceTrackerActivity.detectedUserID = id;
            recognizedUserID = id;
            switch (id) {
                case "-1":
                    Toast.makeText(FaceTrackerActivity.this, "Couldn't recognize face", Toast.LENGTH_SHORT).show();
                    hasSubmitted = false;
                    FaceGraphic.rating = 0.0f;
                    FaceGraphic.name = "";
//                    try { profileViewBtn.setVisibility(View.INVISIBLE); } catch (Exception e) {}
                    break;
                default:
                    MainActivity.mFirebaseDatabase.child("users").child(id).addListenerForSingleValueEvent(new ValueEventListener() { // reads the data once
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                FaceGraphic.info = "" + dataSnapshot.child("name").getValue();
                                FaceGraphic.name =  FaceGraphic.info;
                                notAllowedRating = false;
                                try {
                                    float rating = Float.parseFloat("" + dataSnapshot.child("rating").getValue());

                                    FaceGraphic.info += ", " + String.format(Locale.getDefault(), "%.1f", rating) + " stars";
                                    FaceGraphic.rating = rating;
                                    if (recognizedUserID.equals(MainActivity.mFireBaseUserId)) {
                                        Toast.makeText(getApplicationContext(), "You can't rate yourself!", Toast.LENGTH_SHORT).show();
                                        notAllowedRating = true;
                                        return;
                                    } else if(!(boolean)dataSnapshot.child("active").getValue()) {
                                        Toast.makeText(getApplicationContext(), "This user is not active.", Toast.LENGTH_SHORT).show();
                                        notAllowedRating = true;
                                        return;
                                    } else {
                                        rateArrow.setVisibility(View.VISIBLE);
                                    }
//                                    imageSwitcher.setVisibility(View.VISIBLE);
//                                    imageSwitcher.bringToFront();
//                                    profileViewBtn = findViewById(R.id.viewProfileBtn);
//                                    profileViewBtn.setVisibility(View.VISIBLE);
//                                    profileViewBtn.setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            Intent myIntent = new Intent(FaceTrackerActivity.this, ProfileActivity.class);
//                                            myIntent.putExtra("passedName", FaceGraphic.name); //Optional parameters
//                                            myIntent.putExtra("passedRating", FaceGraphic.rating); //Optional parameters
//                                            myIntent.putExtra("recognizedID", recognizedUserID);
//                                            FaceTrackerActivity.this.startActivityForResult(myIntent, arrow_first);
//                                        }
//                                    });
                                } catch (NumberFormatException e) { }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) { }
                    });
            }
        }

        @Override
        public void deleteProfileResponse(String id) { }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        finish();
    }

    /*
    scales down the image if it's big
     */
    private byte[] scaleDown(byte[] image) {
        final int NEW_WIDTH = 1500;
        Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
        Log.d(TAG, "before scale: " + bmp.getByteCount());
        Log.d(TAG, bmp.getWidth() + "x" + bmp.getHeight() + " ratio: " + (double) bmp.getHeight()/bmp.getWidth());
        if(bmp.getWidth() > NEW_WIDTH) {
            int newHeight = bmp.getHeight() * NEW_WIDTH / bmp.getWidth();
            Bitmap scaled = Bitmap.createScaledBitmap(bmp, NEW_WIDTH, newHeight, false);
            Log.d(TAG, "after scale: " + scaled.getByteCount());
            Log.d(TAG, scaled.getWidth() + "x" + scaled.getHeight() + " ratio: " + (double) scaled.getHeight()/scaled.getWidth());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
            scaled.recycle();
            bmp.recycle();
            return out.toByteArray();
        }
        bmp.recycle();
        return image;
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Log.d(DEBUG_TAG, "vX: " + velocityX + " vY: " + velocityY);
            if(velocityY < -1000 && !FaceTrackerActivity.detectedUserID.equals("") && !notAllowedRating) {
                Intent myIntent = new Intent(FaceTrackerActivity.this, ProfileActivity.class);
                myIntent.putExtra("passedName", FaceGraphic.name); //Optional parameters
                myIntent.putExtra("passedRating", FaceGraphic.rating); //Optional parameters
                myIntent.putExtra("recognizedID", FaceTrackerActivity.detectedUserID);
                FaceTrackerActivity.this.startActivityForResult(myIntent, 1);
            }
            return true;
        }
    }

    @Override
    public View makeView() {
        ImageView imageView = new ImageView(this);
//        imageView.setBackgroundColor(0xFFFFFFFF); // transparent
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setLayoutParams(
                new ImageSwitcher.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT));
        return imageView;
    }
}
