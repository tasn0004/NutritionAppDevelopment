package com.nutritionapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditHealthList extends AppCompatActivity {
    private UserAccount userData;
    private Button saveButton;
    ParseUser currentUser = ParseUser.getCurrentUser();
    private ArrayList<String> healthConditionsList = new ArrayList<>();
    private LinearLayout checkBoxLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_health_list);

        // Hide the top bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        userData = (UserAccount) getIntent().getSerializableExtra("userData");
        if (userData == null) {
            userData = new UserAccount();
        }

        saveButton = findViewById(R.id.saveButton);
        configureSaveButtonListener();

        checkBoxLayout = findViewById(R.id.healthCheckboxLayout);

        loadData();

    }

    /**
     * Grab all user healthconcerns form the back4app database
     */
    private List fetchUserHealthConcerns() {

            Object healthConcerns = currentUser.get("healthConcerns");

            if (healthConcerns != null && healthConcerns instanceof List) {
                List<String> concerns = (List<String>) healthConcerns;

                return concerns;
            }
            return null;

    }

    private void configureBackTextListener(TextView backText) {
        backText.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Saves the health concerns to the user
     */
    private void configureSaveButtonListener() {
        saveButton.setOnClickListener(v -> {

            if (healthConditionsList.isEmpty()) {
                // If no checkboxes are selected, add "None"
                healthConditionsList.add("None");
            }

            currentUser.saveInBackground( updateUser -> {

                currentUser.put("healthConcerns", healthConditionsList);
                    });
            Intent intent = new Intent(EditHealthList.this, Settings.class);
            startActivity(intent);
        });
    }

    /**
     * This function makes the other checkboxes deselect if None is selected
     */
    private void configureNoHealthCheckBoxListener() {
    }

    /**
     * Adjusts the health concern list
     * @param checkBox
     * @param condition
     */
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
                    Toast.makeText(EditHealthList.this, "Error loading data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void populateCheckboxes(List<ParseObject> objects) {
        checkBoxLayout.removeAllViews();
        CheckBox noneCheckBox = new CheckBox(this);
        noneCheckBox.setText("None");
        noneCheckBox.setTextSize(18);
        noneCheckBox.setTextColor(getResources().getColor(R.color.TextColorWhiteVsBlack));
        noneCheckBox.setButtonTintList(getResources().getColorStateList(R.color.checkbox_color));
        noneCheckBox.setPadding(0, 30, 0, 30);
        checkBoxLayout.addView(noneCheckBox);

        List<String> userHealthConcerns = fetchUserHealthConcerns();

        List<CheckBox> allCheckBoxes = new ArrayList<>();

        for (ParseObject object : objects) {
            CheckBox checkBox = new CheckBox(this);
            String healthConcern = object.getString("name");
            checkBox.setText(healthConcern);
            checkBox.setTextSize(18);
            checkBox.setTextColor(getResources().getColor(R.color.TextColorWhiteVsBlack));
            checkBox.setButtonTintList(getResources().getColorStateList(R.color.checkbox_color));
            checkBox.setPadding(0, 30, 0, 30);
            checkBoxLayout.addView(checkBox);

            if (userHealthConcerns != null && userHealthConcerns.contains(healthConcern)) {
                checkBox.setChecked(true);
                healthConditionsList.add(healthConcern);
            }

            configureCheckBoxListener(checkBox, object.getString("name"), noneCheckBox);
            allCheckBoxes.add(checkBox);
        }

        if (userHealthConcerns != null && userHealthConcerns.contains("None")) {
            noneCheckBox.setChecked(true);
        }

        configureNoHealthCheckBoxListener(noneCheckBox, allCheckBoxes);
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
                    checkBox.setTextColor(getResources().getColor(R.color.TextColorWhiteVsBlack));
                }
            }
        });
    }
}
