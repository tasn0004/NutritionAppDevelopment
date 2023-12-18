package com.nutritionapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class SignUpPage4 extends AppCompatActivity {
    private UserAccount userData;
    private ArrayList<String> healthConditionsList = new ArrayList<>();
    private LinearLayout checkBoxLayout;
    ImageButton finishButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up4);

        // Hide the top bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        userData = (UserAccount) getIntent().getSerializableExtra("userData");
        if (userData == null) {
            userData = new UserAccount();
        }
        // Initialize UI components
        finishButton = findViewById(R.id.nextButton4);
        TextView backText = findViewById(R.id.backText4);

        // Configure listeners
        configureBackTextListener(backText);
        configureFinishButtonListener();


        checkBoxLayout = findViewById(R.id.healthCheckboxLayout);

        loadData();

    }

    private void configureBackTextListener(TextView backText) {
        backText.setOnClickListener(v -> onBackPressed());
    }

    private void configureFinishButtonListener() {
        finishButton.setOnClickListener(v -> {
            if (healthConditionsList.isEmpty() && !isNoneSelected()) {
                Toast.makeText(SignUpPage4.this, "Please select at least one health condition or 'None'.", Toast.LENGTH_SHORT).show();
                return; // Prevent proceeding to the next page
            }

            if (isNoneSelected()) {
                userData.setHealthConcerns(new String[]{"None"});
            } else {
                String[] conditions = healthConditionsList.toArray(new String[0]);
                userData.setHealthConcerns(conditions);
            }

            Intent intent = new Intent(SignUpPage4.this, SignUpPage5.class);
            intent.putExtra("userData", userData);  // Pass the updated UserAccount object to next activity
            startActivity(intent);
        });
    }

    private boolean isNoneSelected() {
        View noneCheckboxView = checkBoxLayout.findViewWithTag("None");
        if (noneCheckboxView instanceof CheckBox) {
            return ((CheckBox) noneCheckboxView).isChecked();
        }
        return false;
    }
    private void configureNoHealthCheckBoxListener(CheckBox noneCheckBox, List<CheckBox> allCheckBoxes) {
        noneCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // If "None" is selected, uncheck all other checkboxes and grey them out
                for (CheckBox checkBox : allCheckBoxes) {
                    if (checkBox != noneCheckBox) {
                        checkBox.setChecked(false);
                    }
                }
            } else {
                // If "None" is unselected, enable all checkboxes and set their text color back
                for (CheckBox checkBox : allCheckBoxes) {
                    checkBox.setEnabled(true);
                    checkBox.setTextColor(getResources().getColor(R.color.AlwaysBlack));
                }
            }
        });
    }
    private void configureCheckBoxListener(CheckBox checkBox, String condition, CheckBox noneCheckBox) {
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // If current checkbox is checked, deselect the "None" checkbox
                noneCheckBox.setChecked(false);
                healthConditionsList.add(condition);
            } else {
                healthConditionsList.remove(condition);
            }
        });
    }

    public void loadData() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("NutritionProfiles");
        query.whereEqualTo("type", "healthConcern");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    // Data retrieval successful
                    populateCheckboxes(objects);
                } else {
                    // Handle error
                    Toast.makeText(SignUpPage4.this, "Error loading data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void populateCheckboxes(List<ParseObject> objects) {
        checkBoxLayout.removeAllViews();
        CheckBox noneCheckBox = new CheckBox(this);
        noneCheckBox.setText("None");
        noneCheckBox.setTag("None");
        noneCheckBox.setTextSize(18);
        noneCheckBox.setTextColor(getResources().getColor(R.color.AlwaysBlack));
        noneCheckBox.setButtonTintList(getResources().getColorStateList(R.color.checkbox_color));
        noneCheckBox.setPadding(0, 30, 0, 30);
        checkBoxLayout.addView(noneCheckBox);

        List<CheckBox> allCheckBoxes = new ArrayList<>();

        for (ParseObject object : objects) {
            CheckBox checkBox = new CheckBox(this);
            String healthConcern = object.getString("name");
            checkBox.setText(healthConcern);
            checkBox.setTextSize(18);
            checkBox.setTextColor(getResources().getColor(R.color.AlwaysBlack));
            checkBox.setButtonTintList(getResources().getColorStateList(R.color.checkbox_color));
            checkBox.setPadding(0, 30, 0, 30);
            checkBoxLayout.addView(checkBox);

            configureCheckBoxListener(checkBox, object.getString("name"), noneCheckBox);
            allCheckBoxes.add(checkBox);
        }

        configureNoHealthCheckBoxListener(noneCheckBox, allCheckBoxes);
    }
}
