package com.coding.pixel.labboapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class UniInfoActivity extends AppCompatActivity {

    private WebView UniWebsite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uni_info);

        Toolbar unitoolbar = findViewById(R.id.uniInfoBar);
        setSupportActionBar(unitoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("University Information");

        UniWebsite = findViewById(R.id.uniWebsite);
        WebSettings webSettings = UniWebsite.getSettings();
        webSettings.setJavaScriptEnabled(true);
        UniWebsite.loadUrl("http://vehari.comsats.edu.pk/");
        UniWebsite.setWebViewClient(new WebViewClient());

    }
    @Override
    public void onBackPressed() {
        if(UniWebsite.canGoBack()){
            UniWebsite.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
