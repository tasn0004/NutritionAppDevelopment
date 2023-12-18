package com.nutritionapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginPage extends AppCompatActivity {

    // Declare UI elements
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    ToggleButton light, dark;
    RadioGroup themeRadioGroup;
    private TextView newUser, forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if the user is already logged in
        if (ParseUser.getCurrentUser() != null) {
            // If the user is already logged in, redirect to HomePage
            navigateToHomePage();
            return;
        }

        // Set the content view to the login page layout
        setContentView(R.layout.loginpage);

        // Hide the action bar (top bar)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Linking the UI elements from XML layout to Java variables
        emailEditText = findViewById(R.id.emailInput);
        passwordEditText = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        newUser = findViewById(R.id.newUser);
        forgotPassword = findViewById(R.id.forgotPassword);



        /**
         * Set OnClickListener for the login button
         */
        loginButton.setOnClickListener(v -> {
            // Get the inputted email and password from the EditTexts
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Check if either email or password fields are empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginPage.this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            /**
             * Attempt to log in the user with the provided email and password using Back4App
             */
            ParseUser.logInInBackground(email, password, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (user != null) {
                        // If user object is returned, login was successful
                        Toast.makeText(LoginPage.this, "Login success", Toast.LENGTH_SHORT).show();
                        navigateToHomePage(); // Redirect to HomePage
                    } else {
                        // Handle login failures
                        if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                            Toast.makeText(LoginPage.this, "User not registered or incorrect input", Toast.LENGTH_SHORT).show();
                        } else {
                            // Display other errors that might occur from Back4app server
                            Toast.makeText(LoginPage.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        });

        //when click I'm new user, will navigate to SignUpPage
        newUser.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPage.this, SignUpPage.class);
            startActivity(intent);
        });
        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPage.this, ForgetPassword.class);
            startActivity(intent);
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Switch themeSwitch = findViewById(R.id.themeSwitch);

        int nightMode = preferences.getInt("nightMode", AppCompatDelegate.MODE_NIGHT_YES);
        boolean isNightMode = (nightMode == AppCompatDelegate.MODE_NIGHT_NO);

        themeSwitch.setChecked(isNightMode);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Switch to dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                // Switch to light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            // Save the preference
            preferences.edit().putInt("nightMode", isChecked ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES).apply();
        });

    }

    /**
     * this method to v to the homePage when user sign in
     */
    private void navigateToHomePage() {
        Intent intent = new Intent(LoginPage.this, HomePage.class);
        startActivity(intent);
        finish(); // make sure user does not go back to login page
    }
}
