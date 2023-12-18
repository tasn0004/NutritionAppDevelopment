package com.nutritionapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.widget.Switch;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Theme extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.theme);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Switch themeSwitch = findViewById(R.id.themeSwitch);

        // Set the switch state based on saved preference
        int nightMode = preferences.getInt("nightMode", AppCompatDelegate.MODE_NIGHT_NO);
        themeSwitch.setChecked(nightMode == AppCompatDelegate.MODE_NIGHT_YES);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                preferences.edit().putInt("nightMode", AppCompatDelegate.MODE_NIGHT_YES).apply();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                preferences.edit().putInt("nightMode", AppCompatDelegate.MODE_NIGHT_NO).apply();
            }
        });
    }
}
