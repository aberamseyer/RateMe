package com.abe.jason.rateme.kairos.recognize;

import android.os.AsyncTask;
import android.util.Log;

import com.abe.jason.rateme.activity.AsyncCallback;
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

public class RecognizeRequest extends AsyncTask<String, String, String> {
    private static final String TAG = "RecognizeRequest.java";
    private static final String KAIROS_RECOGNIZE_URL = " https://api.kairos.com/recognize";
    private static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    private static final Moshi moshi = new Moshi.Builder().build();
    private static final JsonAdapter<RecognizeResponse> recognizeResponseAdapter = moshi.adapter(RecognizeResponse.class);

    private AsyncCallback asyncCallback;
    private byte[] file;

    public RecognizeRequest(byte[] file, AsyncCallback asyncCallback) {
        this.file = new byte[file.length];
        this.file = file;
        this.asyncCallback = asyncCallback;
    }

    @Override
    protected void onPreExecute() {  }

    @Override
    protected String doInBackground(String... strings) {
        String responseString = "";
        Log.d(TAG, "Took picture, trying request...");
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("gallery_name", "test")
                .addFormDataPart("image", "upload.jpeg",
                        RequestBody.create(MEDIA_TYPE_JPEG, file))
                .build();
        Request request = new Request.Builder()
                .url(KAIROS_RECOGNIZE_URL)
                .addHeader("app_id", MainActivity.KAIROS_FACE_API_ID)
                .addHeader("app_key", MainActivity.KAIROS_FACE_API_KEY)
                .post(requestBody)
                .build();

        String id = "";
        try {
            Response response = MainActivity.client.newCall(request).execute();
            if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            try {
                RecognizeResponse responseData = recognizeResponseAdapter.fromJson(response.body().source());
                id = responseData.getImages().get(0).getTransaction().getSubjectId();
                responseString = "I see firebase user id#: " + response;
            } catch (RuntimeException e) {
                responseString = "Couldn't recognize anyone";
            }
        } catch (IOException e) {
            Log.d(TAG, "Error while making API request");
        }
        Log.d(TAG, responseString);
        return id;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        asyncCallback.recognizeResponse(result);
    }
}
