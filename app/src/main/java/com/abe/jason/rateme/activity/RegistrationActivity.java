package com.abe.jason.rateme.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.abe.jason.rateme.R;

public class RegistrationActivity extends AppCompatActivity {
    private static final String TAG = "RegistrationActivity.java";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration);

        final EditText firstName = findViewById(R.id.editText);
        Button submit = findViewById(R.id.btn_submit);
        Button cancel = findViewById(R.id.btn_cancel);
        if(getIntent().getStringExtra("method").equals("change")) {
            findViewById(R.id.helpText).setVisibility(View.INVISIBLE);
        }
        else {
            cancel.setVisibility(View.GONE);
        }
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstName.getText().toString().equals("")) {
                    Toast.makeText(RegistrationActivity.this, "You have a name, don't you?", Toast.LENGTH_SHORT).show();
                }
                else if(firstName.getText().toString().matches("[a-zA-z]+")) {
                    MainActivity.mFirebaseDatabase.child("users").child(MainActivity.mFireBaseUserId).child("name").setValue(firstName.getText().toString());
                    Toast.makeText(RegistrationActivity.this, "Thanks " + firstName.getText().toString(), Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    Toast.makeText(RegistrationActivity.this, "Only letters allowed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(getIntent().getStringExtra("method").equals("new")) {
            // prevent cancelling it
        }
        else {
            super.onBackPressed();
        }
    }
}
