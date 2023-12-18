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

public class SignUpPage3 extends AppCompatActivity {
    private UserAccount userData;
    private ArrayList<String> dietPreferencesList = new ArrayList<>();

    private LinearLayout dietBoxLayout;
    ImageButton finishButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up3);

        // Hide the top bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        userData = (UserAccount) getIntent().getSerializableExtra("userData");
        if (userData == null) {
            userData = new UserAccount();
        }
        // Initialize UI components
        finishButton = findViewById(R.id.nextButton3);
        TextView backText = findViewById(R.id.backText3);

        configureBackTextListener(backText);

        configureFinishButtonListener();

        dietBoxLayout = findViewById(R.id.dietCheckboxLayout);

        loadData();

    }

    private void configureBackTextListener(TextView backText) {
        backText.setOnClickListener(v -> onBackPressed());
    }

    private void configureFinishButtonListener() {
        finishButton.setOnClickListener(v -> {
            // Check if at least one checkbox is selected or "None" is selected
            if (dietPreferencesList.isEmpty() && !isNoneSelected()) {
                Toast.makeText(SignUpPage3.this, "Please select at least one diet preference or 'None'.", Toast.LENGTH_SHORT).show();
                return; // Prevent proceeding to the next page
            }

            if (isNoneSelected()) {
                userData.setDietPreferences(new String[]{"None"});
            } else {
                String[] preferences = new String[dietPreferencesList.size()];
                preferences = dietPreferencesList.toArray(preferences);
                userData.setDietPreferences(preferences);
            }

            Intent intent = new Intent(SignUpPage3.this, SignUpPage4.class);
            intent.putExtra("userData", userData);  // Pass the updated UserAccount object to next activity
            startActivity(intent);
        });
    }


    private boolean isNoneSelected() {
        for (int i = 0; i < dietBoxLayout.getChildCount(); i++) {
            View child = dietBoxLayout.getChildAt(i);
            if (child instanceof CheckBox && "None".equals(child.getTag())) {
                return ((CheckBox) child).isChecked();
            }
        }
        return false;
    }



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
                    Toast.makeText(SignUpPage3.this, "Error loading data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void populateCheckboxes(List<ParseObject> objects) {
        dietBoxLayout.removeAllViews();
        CheckBox noneCheckBox = new CheckBox(this);
        noneCheckBox.setText("None");
        noneCheckBox.setTag("None");
        noneCheckBox.setTextSize(18);
        noneCheckBox.setTextColor(getResources().getColor(R.color.AlwaysBlack));
        noneCheckBox.setButtonTintList(getResources().getColorStateList(R.color.checkbox_color));
        noneCheckBox.setPadding(0, 30, 0, 30);
        dietBoxLayout.addView(noneCheckBox);

        List<CheckBox> allCheckBoxes = new ArrayList<>();

        for (ParseObject object : objects) {
            CheckBox checkBox = new CheckBox(this);
            String dietPreference = object.getString("name");
            checkBox.setText(dietPreference);
            checkBox.setTextSize(18);
            checkBox.setTextColor(getResources().getColor(R.color.AlwaysBlack));
            checkBox.setButtonTintList(getResources().getColorStateList(R.color.checkbox_color));
            checkBox.setPadding(0, 30, 0, 30);
            dietBoxLayout.addView(checkBox);

            configureCheckBoxListener(checkBox, object.getString("name"), noneCheckBox);
            allCheckBoxes.add(checkBox);
        }

        configureNoneCheckBoxListener(noneCheckBox, allCheckBoxes);
    }

    private void configureNoneCheckBoxListener(CheckBox noneCheckBox, List<CheckBox> allCheckBoxes) {
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
}
