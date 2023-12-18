package com.nutritionapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.parse.ParseUser;

public class ForgetPassword extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_password);

        emailEditText = findViewById(R.id.emailEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                if (!email.isEmpty()) {
                    resetPassword(email);
                } else {
                    Toast.makeText(ForgetPassword.this, "Please enter your email address.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void resetPassword(String email) {
        ParseUser.requestPasswordResetInBackground(email, e -> {
            if (e == null) {
                Toast.makeText(ForgetPassword.this, "An email was successfully sent.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ForgetPassword.this, "Failed to send reset email: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
