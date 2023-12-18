package com.nutritionapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;

public class SignUpPage5 extends AppCompatActivity {
    private TextView backText;
    private Button finishButton;
    private TabLayout tabLayout;

    private static final String PREFS_NAME = "UserSettings";
    private static final String WEEK_START_DAY = "WeekStartDay";
    private TabLayout goalTabLayout;
    private static final String FITNESS_GOAL = "FitnessGoal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up5);

        // hide top bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        finishButton = findViewById(R.id.nextButton5);
        backText = findViewById(R.id.backText5);
        tabLayout = findViewById(R.id.weekTabLayout);
        goalTabLayout = findViewById(R.id.goalTabLayout);

        // Set up TabLayout
        setupTabLayout();
        setupGoalTabLayout();

        // Back Button Listener
        backText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Finish Button Listener
        finishButton.setOnClickListener(v -> {
            UserAccount userData = (UserAccount) getIntent().getSerializableExtra("userData");
            if (userData != null) {
                TabLayout.Tab selectedWeekDayTab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
                TabLayout.Tab selectedFitnessGoalTab = goalTabLayout.getTabAt(goalTabLayout.getSelectedTabPosition());

                if (selectedWeekDayTab == null || selectedFitnessGoalTab == null) {
                    Toast.makeText(SignUpPage5.this, "Please select both a start day of the week and a fitness goal.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Set the values from the selected tabs into the UserAccount object
                String selectedWeekStartDay = selectedWeekDayTab.getText().toString();
                String selectedFitnessGoal = selectedFitnessGoalTab.getText().toString();
                userData.setPreferredStartDayOfWeek(selectedWeekStartDay);
                userData.setWeightManagementGoal(selectedFitnessGoal);

                userData.saveToBack4App();

                Intent intent = new Intent(SignUpPage5.this, HomePage.class);
                startActivity(intent);
            } else {
                Toast.makeText(SignUpPage5.this, "Error saving user details.", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void setupTabLayout() {
        String[] days = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            tabLayout.addTab(tabLayout.newTab().setText(day));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(WEEK_START_DAY, tab.getText().toString());
                editor.apply();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupGoalTabLayout() {
        String[] goals = new String[]{"Lose", "Maintain", "Gain"};
        for (String goal : goals) {
            goalTabLayout.addTab(goalTabLayout.newTab().setText(goal));
        }

        goalTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(FITNESS_GOAL, tab.getText().toString());
                editor.apply();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
}