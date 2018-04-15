package com.abe.jason.rateme.kairos.delete_face;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.abe.jason.rateme.activity.AsyncCallback;
import com.abe.jason.rateme.activity.MainActivity;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Abe on 3/22/2018.
 */

public class DeleteFaceProfileRequest extends AsyncTask<String, String, String> {
    private static final String TAG = "DeleteFaceProfileRequest.java";
    private static final String KAIROS_DELETE_FACE_PROFILE_URL = "https://api.kairos.com/gallery/remove_subject";
    private static final Moshi moshi = new Moshi.Builder().build();
    private static final JsonAdapter<DeleteFaceProfileResponse> deleteFaceProfileResponseAdapter = moshi.adapter(DeleteFaceProfileResponse.class);

    private Context mContext;
    private boolean success = false;
    private String responseString;
    private AsyncCallback asyncCallback;


    public DeleteFaceProfileRequest(Context mContext, AsyncCallback asyncCallback) {
        this.mContext = mContext;
        this.asyncCallback = asyncCallback;
    }

    @Override
    protected String doInBackground(String... strings) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("gallery_name", "test")
                .addFormDataPart("subject_id", MainActivity.mFireBaseUserId)
                .build();
        Request request = new Request.Builder()
                .url(KAIROS_DELETE_FACE_PROFILE_URL)
                .addHeader("app_id", MainActivity.KAIROS_FACE_API_ID)
                .addHeader("app_key", MainActivity.KAIROS_FACE_API_KEY)
                .post(requestBody)
                .build();

        try {
            Response response = MainActivity.client.newCall(request).execute();
            if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            try {
                DeleteFaceProfileResponse responseData = deleteFaceProfileResponseAdapter.fromJson(response.body().source());
                responseString = "Successfully deleted profile for user " + MainActivity.mFireBaseUserId;
                success = true;
            } catch (RuntimeException e) {
                responseString = "Error deleting profile";
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
        asyncCallback.deleteProfileResponse(result);
    }
}
