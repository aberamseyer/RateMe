package com.abe.jason.rateme.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.abe.jason.rateme.R;
import com.abe.jason.rateme.kairos.delete_face.DeleteFaceProfileRequest;

public class AccountFragment extends Fragment implements AsyncCallback {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // logout button
        view.findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).signOut();
            }
        });

        // camera buttons
        view.findViewById(R.id.btn_train).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FaceTrackerActivity.class);
                intent.putExtra("method", "enroll");
                startActivity(intent);
            }
        });
        view.findViewById(R.id.btn_delete_face_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DeleteFaceProfileRequest(getContext(), AccountFragment.this).execute();
            }
        });
        view.findViewById(R.id.btn_change_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), RegistrationActivity.class).putExtra("method", "change"));
            }
        });
    }



    @Override
    public void recognizeResponse(String id) { }

    @Override
    public void deleteProfileResponse(String id) {
        Toast.makeText(getContext(), "Face profile for user id " + MainActivity.mFireBaseUserId + " deleted", Toast.LENGTH_SHORT).show();
    }
}
