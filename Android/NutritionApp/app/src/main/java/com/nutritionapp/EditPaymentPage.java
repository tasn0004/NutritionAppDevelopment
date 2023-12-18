package com.nutritionapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class EditPaymentPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.edit_user_payment);
    }
}

