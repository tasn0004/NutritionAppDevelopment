package com.nutritionapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class AboutApp extends AppCompatActivity {

    ParseObject aboutSection;
    TextView aboutText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.about_app);

        aboutText = findViewById(R.id.aboutText);

        // Obtains the information from the database
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ApplicationDocuments");
        query.whereContains("name", "About");
        query.findInBackground((about, aboutError) -> {
            String documentText = about.get(0).getString("documentText");
            aboutText.setText(documentText);
        });

    }
}
