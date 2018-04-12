package com.abe.jason.rateme.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.abe.jason.rateme.R;

import org.w3c.dom.Text;

public class ProfileActivity extends AppCompatActivity {
    private String name;
    private float rating;
    private TextView nameLabel;
    private TextView ratingLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_view);

        Intent intent = getIntent();
        this.name = intent.getStringExtra("passedName");
        this.rating = intent.getFloatExtra("passedRating", 0.0f);

        this.nameLabel = findViewById(R.id.nameLabel);
        this.ratingLabel = findViewById(R.id.ratingLabel);

        nameLabel.setText(this.name);
        ratingLabel.setText(Float.toString(this.rating));
    }
}
