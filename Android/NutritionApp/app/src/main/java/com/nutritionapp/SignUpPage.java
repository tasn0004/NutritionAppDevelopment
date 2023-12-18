package com.nutritionapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.service.autofill.UserData;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpPage extends AppCompatActivity {
    private EditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText, passwordConfirmEditText;
    private ImageButton nextButton;
    private CheckBox agreeToTermsCheckBox;
    private TextView termsAndConditionsText, backText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // hide top bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        int color = ContextCompat.getColor(getApplicationContext(), R.color.customAgreementTittle);

        // Initialize EditText and Button references
        firstNameEditText = findViewById(R.id.firstNameInput);
        lastNameEditText = findViewById(R.id.lastNameInput);
        emailEditText = findViewById(R.id.emailInput);
        passwordEditText = findViewById(R.id.passwordInput);
        passwordConfirmEditText = findViewById(R.id.passwordInputConfirm);
        nextButton = findViewById(R.id.nextButton);
        agreeToTermsCheckBox = findViewById(R.id.agreeToTermsCheckBox);
        termsAndConditionsText = findViewById(R.id.termsAndConditionsText);

        // Setup backText click listener
        backText = findViewById(R.id.backText);
        backText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Styling for the terms text
        String termsText = "I acknowledge and agree to the ";
        SpannableString spannable = new SpannableString(termsText + "Terms of Use & Privacy Policy");
        int termsStart = termsText.length();
        int termsEnd = termsStart + "Terms of Use".length();
        setSpanStyles(spannable, termsStart, termsEnd, color);
        int privacyStart = termsEnd + " & ".length();
        int privacyEnd = privacyStart + "Privacy Policy".length();
        setSpanStyles(spannable, privacyStart, privacyEnd, color);
        termsAndConditionsText.setText(spannable);


        // Set click listener for the next button
        nextButton.setOnClickListener(v -> {
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String passwordConfirm = passwordConfirmEditText.getText().toString().trim();

            if (!isValidCredentials(firstName, lastName, email, password, passwordConfirm)) {
                if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(SignUpPage.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignUpPage.this, "Check your input values.", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (!agreeToTermsCheckBox.isChecked()) {
                Toast.makeText(SignUpPage.this, "Please agree to the terms and policy to continue.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Proceed only if all checks pass
            UserAccount userData = new UserAccount();
            userData.setFirstName(firstName);
            userData.setLastName(lastName);
            userData.setEmailAddress(email);
            userData.setPassword(password);
            Intent nextPageIntent = new Intent(SignUpPage.this, SignUpPage1.class);
            nextPageIntent.putExtra("userData", userData);
            startActivity(nextPageIntent);
        });

    }


    private boolean isValidCredentials(String firstName, String lastName, String email, String password, String passwordConfirm) {
        return !firstName.isEmpty() &&
                !lastName.isEmpty() &&
                !email.isEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                !password.isEmpty() &&
                password.equals(passwordConfirm);
    }


    private void setSpanStyles(SpannableString spannable, int start, int end, int color) {
        spannable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
