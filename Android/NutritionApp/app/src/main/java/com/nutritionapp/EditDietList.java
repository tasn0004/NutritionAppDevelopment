package com.nutritionapp;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditDietList extends AppCompatActivity {

    private UserAccount userData;
    private Button saveButton;
    ParseUser currentUser = ParseUser.getCurrentUser();
    private ArrayList<String> dietPreferencesList = new ArrayList<>();
    private LinearLayout dietBoxLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_diet_list);

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

        dietBoxLayout = findViewById(R.id.dietCheckboxLayout);

        loadData();
    }

    /**
     * This method grabs the information from back4app
     */
    private List fetchUserDietaryPreferences() {

        Object dietPreferences = currentUser.get("dietPreferences");

        if (dietPreferences != null && dietPreferences instanceof List) {
            List<String> preferences = (List<String>) dietPreferences;

            return preferences;
        }
        return null;
    }

    /**
     * This saves the new information to the back4app
     */
    private void configureSaveButtonListener() {
        saveButton.setOnClickListener(v -> {

            if (dietPreferencesList.isEmpty()) {
                // If no checkboxes are selected, add "None"
                dietPreferencesList.add("None");
            }

            currentUser.saveInBackground( updateUser -> {

                currentUser.put("dietPreferences", dietPreferencesList);
                    });
            Intent intent = new Intent(EditDietList.this, Settings.class);
            startActivity(intent);
        });
    }

    /**
     * Adjusts the diet preference list
     * @param checkBox
     * @param preference
     */
    private void configureCheckBoxListener(CheckBox checkBox, String preference, CheckBox noneCheckBox) {
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                noneCheckBox.setChecked(false);
                dietPreferencesList.add(preference);
            } else {
                dietPreferencesList.remove(preference);
            }
        });
    }

    public void loadData() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("NutritionProfiles");
        query.whereEqualTo("type", "dietPreference");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    // Data retrieval successful
                    populateCheckboxes(objects);
                } else {
                    // Handle error
                    Toast.makeText(EditDietList.this, "Error loading data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void populateCheckboxes(List<ParseObject> objects) {
        dietBoxLayout.removeAllViews();
        CheckBox noneCheckBox = new CheckBox(this);
        noneCheckBox.setText("None");
        noneCheckBox.setTextSize(18);
        noneCheckBox.setTextColor(getResources().getColor(R.color.TextColorWhiteVsBlack));
        noneCheckBox.setButtonTintList(getResources().getColorStateList(R.color.checkbox_color));
        noneCheckBox.setPadding(0, 30, 0, 30);
        dietBoxLayout.addView(noneCheckBox);

        List<String> userDietPreferences = fetchUserDietaryPreferences();

        List<CheckBox> allCheckBoxes = new ArrayList<>();

        for (ParseObject object : objects) {
            CheckBox checkBox = new CheckBox(this);
            String dietPreference = object.getString("name");
            checkBox.setText(dietPreference);
            checkBox.setTextSize(18);
            checkBox.setTextColor(getResources().getColor(R.color.TextColorWhiteVsBlack));
            checkBox.setButtonTintList(getResources().getColorStateList(R.color.checkbox_color));
            checkBox.setPadding(0, 30, 0, 30);
            dietBoxLayout.addView(checkBox);

            if (userDietPreferences != null && userDietPreferences.contains(dietPreference)) {
                checkBox.setChecked(true);
                dietPreferencesList.add(dietPreference);
            }

            configureCheckBoxListener(checkBox, object.getString("name"), noneCheckBox);
            allCheckBoxes.add(checkBox);
        }

        if (userDietPreferences != null && userDietPreferences.contains("None")) {
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
