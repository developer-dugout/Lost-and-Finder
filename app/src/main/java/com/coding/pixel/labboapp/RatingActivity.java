package com.coding.pixel.labboapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class RatingActivity extends AppCompatActivity {

    private TextView RateText;
    private RatingBar RateUs;
    private Button RateBut;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        Toolbar toolbar = findViewById(R.id.RateBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Rate Us");

        RateText = findViewById(R.id.ratingus);
        RateUs = findViewById(R.id.RateStars);
        RateBut = findViewById(R.id.rateBut);
        loadingBar = new ProgressDialog(this);

        RateUs.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                RateText.setText("Rating: "+ rating);
            }
        });
        RateBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMainActivity();
            }
        });
    }

    private void SendUserToMainActivity() {

        loadingBar.setTitle("Your Feedback");
        loadingBar.setMessage("Thanks for rating us and show your interest for giving feedback...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        Intent mainIntent = new Intent(RatingActivity.this, MainActivity.class);
        startActivity(mainIntent);
        Toast.makeText(this, "Thanks for your Feedback", Toast.LENGTH_SHORT).show();
    }
}
