package com.abe.jason.rateme.activity;

/**
 * Created by Abe on 3/25/2018.
 *
 * implemented by FaceTrackerActivity to allow execution of code in the activity after an api call
 */

public interface AsyncCallback {
    void recognizeResponse(String id);
}
