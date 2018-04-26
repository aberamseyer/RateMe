package com.abe.jason.rateme.kairos.enroll;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.abe.jason.rateme.activity.FaceTrackerActivity;
import com.abe.jason.rateme.activity.MainActivity;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Abe on 3/22/2018.
 */

public class EnrollRequest extends AsyncTask<String, String, String> {
    private static final String TAG = "RecognizeRequest.java";
    private static final String KAIROS_ENROLL_URL = "https://api.kairos.com/enroll";
    private static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    private static final Moshi moshi = new Moshi.Builder().build();
    private static final JsonAdapter<EnrollResponse> enrollResponseAdapter = moshi.adapter(EnrollResponse.class);

    private Context mContext;
    private byte[] file;
    private String responseString;
    private boolean success = false;

    public EnrollRequest(byte[] file, Context mContext) {
        this.file = new byte[file.length];
        this.file = file;
        this.mContext = mContext;
    }

    @Override
    protected void onPreExecute() {
        ((Activity) mContext).finish();
    }

    @Override
    protected String doInBackground(String... strings) {
        Log.d(TAG, "Took picture, trying request...");
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("gallery_name", "test")
                .addFormDataPart("subject_id", MainActivity.mFireBaseUserId)
                .addFormDataPart("image", "upload.jpeg",
                        RequestBody.create(MEDIA_TYPE_JPEG, file))
                .build();
        Request request = new Request.Builder()
                .url(KAIROS_ENROLL_URL)
                .addHeader("app_id", MainActivity.KAIROS_FACE_API_ID)
                .addHeader("app_key", MainActivity.KAIROS_FACE_API_KEY)
                .post(requestBody)
                .build();

        try {
            Response response = MainActivity.client.newCall(request).execute();
            if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            try {
                EnrollResponse responseData = enrollResponseAdapter.fromJson(response.body().source());
//                responseString = "Successfully enrolled for user " + MainActivity.mFireBaseUserId + ", face_id #: " + responseData.getFaceId();
                responseString = "Successfully enrolled face!";
                Log.d(TAG, "quality: " + responseData.getImages().get(0).getTransaction().getQuality());
                success = true;
            } catch (RuntimeException e) {
                responseString = "Couldn't recognize anyone";
            }
        } catch (IOException e) {
            Log.d(TAG, "Error while making API request");
        }
        Log.d(TAG, responseString);
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(!success) {
            Context appContext = mContext.getApplicationContext();
            appContext.startActivity(new Intent(appContext, FaceTrackerActivity.class).putExtra("method", "enroll"));
        }
        Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
    }
}
