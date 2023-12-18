package com.nutritionapp;

import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;
import com.parse.Parse;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.parse.ParseUser;


public class Splashscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int nightMode = preferences.getInt("nightMode", AppCompatDelegate.MODE_NIGHT_YES);
        AppCompatDelegate.setDefaultNightMode(nightMode);
        setContentView(R.layout.splashscreen);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        ImageView logoImageView = findViewById(R.id.logoImageView);
        TextView appNameTextView = findViewById(R.id.appNameTextView);
        View container = findViewById(R.id.container);

        container.post(() -> {
            // Calculate end position for container
            float originalXPosition = container.getX();
            float widthOfImage = logoImageView.getWidth();
            float endXPosition = originalXPosition - (widthOfImage / 2) +80; //logo icon push to left and name app pop-up next it

            // Animate the container
            ObjectAnimator containerAnimation = ObjectAnimator.ofFloat(container, "x", endXPosition);
            containerAnimation.setDuration(500); // Duration in ms

            // Animate the app name fade in effect simultaneously
            ObjectAnimator appNameAnimation = ObjectAnimator.ofFloat(appNameTextView, "alpha", 0f, 1f);
            appNameAnimation.setDuration(500);// Duration in ms
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(containerAnimation, appNameAnimation);
            animatorSet.start();
        });
        checkPaidAccountStatus();
    }

    private void checkPaidAccountStatus() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            currentUser.fetchInBackground((object, e) -> {
                boolean isPaidAccount = false;
                if (e == null) {
                    isPaidAccount = object.getBoolean("isPaidAccount");
                } else {
                    // Handle error, maybe default to false
                }

                // Store the value in SharedPreferences
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("isPaidAccount", isPaidAccount);
                editor.apply();

                // Proceed to start the next activity
                new Handler().postDelayed(() -> {
                    startActivity(new Intent(Splashscreen.this, LoginPage.class));
                    finish();
                }, 800); // timer
            });
        } else {
            proceedToNextActivity();
        }
    }

    private void proceedToNextActivity() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(Splashscreen.this, LoginPage.class));
            finish();
        }, 800); // timer
    }
}
