package com.nutritionapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class SignUpPage2 extends AppCompatActivity {

    private ImageButton nextButton2;
    private CheckBox activityVeryLow,activityNormal, activitySlightlyActive, activityModeratelyActive, activityVeryActive, activityExtremelyActive;
    private List<CheckBox> checkBoxList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up2);
        // hide top barR
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        // Initializing buttons and checkboxes to their respective XML IDs
        nextButton2 = findViewById(R.id.nextButton2);
        activityVeryLow = findViewById(R.id.activityVeryLow);
        activityNormal = findViewById(R.id.activityNormal);
        activitySlightlyActive = findViewById(R.id.activitySlightlyActive);
        activityModeratelyActive = findViewById(R.id.activityModeratelyActive);
        activityVeryActive = findViewById(R.id.activityVeryActive);
        activityExtremelyActive = findViewById(R.id.activityExtremelyActive);
        checkBoxList = Arrays.asList(activityVeryLow, activityNormal, activitySlightlyActive, activityModeratelyActive, activityVeryActive, activityExtremelyActive);

        // Set onCheckChangeListeners to ensure only one checkbox is checked
        setupSingleSelectionCheckBox();
        /* nextButton2 action Listener */
        nextButton2.setOnClickListener(v -> {
            // Retrieve the intent that started from previous page
            UserAccount userData = (UserAccount) getIntent().getSerializableExtra("userData");
            if (!isAnyCheckboxSelected()) {
                Toast.makeText(SignUpPage2.this, "Please select at least one activity level.", Toast.LENGTH_SHORT).show();
                return;
            }
            //if UserAccount is empty creating new object
            if (userData == null) {
                userData = new UserAccount();
            }
            if(activityVeryLow.isChecked()){
                userData.setActivityLevel(1.0);
            } else if(activityNormal.isChecked()){
                userData.setActivityLevel(1.2);
            } else if(activitySlightlyActive.isChecked()){
                userData.setActivityLevel(1.3);
            } else if(activityModeratelyActive.isChecked()){
                userData.setActivityLevel(1.5);
            } else if(activityVeryActive.isChecked()){
                userData.setActivityLevel(1.7);
            } else if(activityExtremelyActive.isChecked()){
                userData.setActivityLevel(1.9);
            }
            Intent intent = new Intent(SignUpPage2.this, SignUpPage3.class);
            intent.putExtra("userData", userData);
            startActivity(intent);
        });
    }
    private void setupSingleSelectionCheckBox() {
        for (CheckBox currentCheckBox : checkBoxList) {
            currentCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    uncheckAllExcept(currentCheckBox);
                }
            });
        }
    }
    private boolean isAnyCheckboxSelected() {
        for (CheckBox cb : checkBoxList) {
            if (cb.isChecked()) {
                return true;
            }
        }
        return false;
    }

    private void uncheckAllExcept(CheckBox except) {
        for (CheckBox cb : checkBoxList) {
            if (cb != except) {
                cb.setChecked(false);
            }
        }
    }
}
