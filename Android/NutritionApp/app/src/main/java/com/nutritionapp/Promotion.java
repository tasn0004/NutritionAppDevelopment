package com.nutritionapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
public class Promotion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.promotion);

        final EditText promoCodeInput = findViewById(R.id.promoCodeInput);
        Button applyButton = findViewById(R.id.applyButton);

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String promoCode = promoCodeInput.getText().toString().trim();

                if (promoCode.isEmpty()) {
                    Toast.makeText(Promotion.this, "Please enter a promotion code", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Promotion.this, "Promotion code applied: " + promoCode, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}