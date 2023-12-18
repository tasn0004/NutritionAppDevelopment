package com.nutritionapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseObject;
import com.parse.ParseQuery;

public class PrivacyPolicy extends AppCompatActivity {

    ParseObject privacySection;
    TextView privacyText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.privacy_policy);

        privacyText = findViewById(R.id.privacyText);

        // Obtains the information from the database
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ApplicationDocuments");
        query.whereContains("name", "Privacy");
        query.findInBackground((about, aboutError) -> {
            String documentText = about.get(0).getString("documentText");
            privacyText.setText(documentText);
        });
    }
}