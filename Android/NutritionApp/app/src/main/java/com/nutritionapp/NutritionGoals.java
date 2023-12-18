package com.nutritionapp;

// Android imports

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;


public class NutritionGoals extends Fragment {

    // declare UI components
    private TabLayout tabLayout;
    private TextView dateText;
    // constants for shared preferences
    private static final String PREFS_NAME = "UserSettings";
    private static final String WEEK_START_DAY = "WeekStartDay";
    // declare ProgressBars and TextViews
    private ProgressBar proteinProgressBar, carbsProgressBar, fatsProgressBar;
    private TextView proteinText, carbsText, fatsText, fatsDetailsText, carbsDetailsText, proteinDetailsText;
    private ParseUser currentUser = ParseUser.getCurrentUser();

    private HashMap<String, Float> aggregatedNutrition = new HashMap<>();
    private JSONObject nutritionProfile;
    private int recipesToFetchCount;
    private View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.nutrition_goals, container, false);

        // Initialize the dateText and tabLayout views
        dateText = view.findViewById(R.id.dateText);
        tabLayout = view.findViewById(R.id.tabLayout);

        // Set up date format and display current date
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        SimpleDateFormat sdfN = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

        String currentDate = sdf.format(new Date());
        String currentDateN = sdfN.format(new Date());

        dateText.setText(currentDate);

        // Setup the tabs for the week
        setupTabLayout();

        // Initialize progress bars and their respective texts
        proteinProgressBar = view.findViewById(R.id.proteinProgressBar);
        carbsProgressBar = view.findViewById(R.id.CarbsProgressBar);
        fatsProgressBar = view.findViewById(R.id.FatsProgressBar);
        proteinDetailsText = view.findViewById(R.id.proteinDetailsText);
        carbsDetailsText = view.findViewById(R.id.CarbsDetailsText);
        fatsDetailsText = view.findViewById(R.id.FatsDetailsText);
        proteinText = view.findViewById(R.id.proteinText);
        carbsText = view.findViewById(R.id.carbsText);
        fatsText = view.findViewById(R.id.fatText);

        prepareAndDisplayNutritionForDate(currentDateN);

        // Dividers for Tabs
        View root = tabLayout.getChildAt(0);
        if (root instanceof LinearLayout) {
            ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(getResources().getColor(R.color.LightGreyForTabs));
            drawable.setSize(4, 3);
            ((LinearLayout) root).setDividerPadding(20);
            ((LinearLayout) root).setDividerDrawable(drawable);
        }

                resetNutritionUINoConnection(view);



        return view;
    }


    /**
     * this function to set up the tab layout
     */
    private void setupTabLayout() {
        // Clear any existing tabs
        tabLayout.removeAllTabs();

        // retrieve the week start day from shared preferences
        SharedPreferences settings = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String weekStartDay = settings.getString(WEEK_START_DAY, "Sun");

        Calendar calendar = Calendar.getInstance();
        // set the first day of the week
        calendar.setFirstDayOfWeek(getDayOfWeekIndex(weekStartDay) + 1);
        calendar.set(Calendar.DAY_OF_WEEK, getDayOfWeekIndex(weekStartDay) + 1);

        /**
         * loop through the 7 days of the week
         */
        for (int i = 0; i < 7; i++) {
            // get the day of week string (e.g., "Mon")
            String dayOfWeek = new SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.getTime());
            // qdd a tab for each day
            tabLayout.addTab(tabLayout.newTab().setText(dayOfWeek));

            /**
             * get the current date and the date of the tab
             */
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar tabDate = (Calendar) calendar.clone();
            tabDate.set(Calendar.HOUR_OF_DAY, 0);
            tabDate.set(Calendar.MINUTE, 0);
            tabDate.set(Calendar.SECOND, 0);
            tabDate.set(Calendar.MILLISECOND, 0);

            /**
             * make brightness low for pass day
             */
            if (tabDate.before(today)) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null) {
                    tab.view.setAlpha(0.4f);  // reduce brightness for past days
                }
            }

            // move to the next day
            calendar.add(Calendar.DATE, 1);
        }

        /**
         * set the tab for the current day to be selected
         */
        int todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1 - getDayOfWeekIndex(weekStartDay);
        if (todayIndex < 0) todayIndex += 7;
        tabLayout.selectTab(tabLayout.getTabAt(todayIndex));
        /**
         * here where updated today date text based on tab week selected !!!
         */
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.setFirstDayOfWeek(getDayOfWeekIndex(weekStartDay) + 1);
                selectedDate.set(Calendar.DAY_OF_WEEK, getDayOfWeekIndex(weekStartDay) + 1);
                selectedDate.add(Calendar.DATE, tab.getPosition());
                updateDateText(selectedDate);
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                String formattedDate = sdf.format(selectedDate.getTime());


                aggregatedNutrition.clear();

                prepareAndDisplayNutritionForDate(formattedDate);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
    private void resetNutritionInformation() {
        // Define the default nutrition keys
        String[] nutritionKeys = new String[] {
                "vitaminD", "potassium", "sodium", "saturatedFat", "fibre",
                "zinc", "transFat", "vitaminA", "calories", "carbohydrates",
                "vitaminB12", "sugar", "fat", "calcium", "folate", "magnesium",
                "protein", "vitaminC", "iron", "cholesterol"
        };

        // Clear the existing aggregated nutrition data
        aggregatedNutrition.clear();

        // Set each nutrient key with a default value of 0 in the map
        for (String key : nutritionKeys) {
            aggregatedNutrition.put(key, 0f);
        }

        // Reset the UI components for nutrition information to zero or initial state
        resetNutritionUINoConnection(view);
        resetMacroUI();

    }
    private void resetNutritionUINoConnection(View view) {
        JSONObject nutrientUnitsMapping = getNutrientUnitsMapping(); // Get the JSON object with units

        // Loop over all the keys in the nutrientUnitsMapping
        Iterator<String> keys = nutrientUnitsMapping.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            NutrientViewLines nutrientView = view.findViewById(getResourceIdForKey(key));
            if (nutrientView != null) {
                // Set the nutrient name using the key
                nutrientView.setNutrientName(convertKeyToDisplayableName(key));

                // Retrieve the unit for this nutrient from the mapping
                String unit = nutrientUnitsMapping.optString(key, "g"); // Default to "g" if not found

                // Reset the nutrient details with the correct unit and zero quantity
                nutrientView.setNutrientDetails(String.format("0 %s / 0 %s", unit, unit));
                nutrientView.setProgress(0);
            } else {
                // Log an error if the view could not be found
                Log.e("NutritionReset", "NutrientView not found for key: " + key);
            }
        }
    }

    private JSONObject getNutrientUnitsMapping() {
        String unitsJsonString = "{"
                + "\"vitaminD\": \"iu\","
                + "\"potassium\": \"mg\","
                + "\"sodium\": \"mg\","
                + "\"saturatedFat\": \"g\","
                + "\"fibre\": \"g\","
                + "\"zinc\": \"mg\","
                + "\"transFat\": \"g\","
                + "\"vitaminA\": \"mcg\","
                + "\"calories\": \"cals\","
                + "\"carbohydrates\": \"g\","
                + "\"vitaminB12\": \"mcg\","
                + "\"sugar\": \"g\","
                + "\"fat\": \"g\","
                + "\"calcium\": \"mg\","
                + "\"folate\": \"mcg\","
                + "\"magnesium\": \"mg\","
                + "\"protein\": \"g\","
                + "\"vitaminC\": \"mg\","
                + "\"iron\": \"mg\","
                + "\"cholesterol\": \"mg\""
                + "}";

        try {
            return new JSONObject(unitsJsonString);
        } catch (JSONException e) {
            // Handle the JSON parsing error
            e.printStackTrace();
            return new JSONObject(); // Return an empty JSONObject in case of error
        }
    }


    private String convertKeyToDisplayableName(String key) {
        // Replace this with actual logic to convert keys to displayable names
        String name = key.substring(0, 1).toUpperCase() + key.substring(1);
        return name.replaceAll("([A-Z])", " $1").trim();
    }



    /**
     * this funcction to update the date text view based on the selected tab
     *
     * @param selectedDate calendar object representing the date to be formatted and set to the textView.
     */
    private void updateDateText(Calendar selectedDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        String formattedDate = sdf.format(selectedDate.getTime());
        dateText.setText(formattedDate);
    }

    /**
     * getDayOfWeekIndex function help to return day index
     *
     * @param day this represent the day of the week from sun to sat
     * @return this reutrn 0 by default which Sunday, and return index of day of week based on selected
     */
    private int getDayOfWeekIndex(String day) {
        switch (day) {
            case "Sun":
                return 0;
            case "Mon":
                return 1;
            case "Tue":
                return 2;
            case "Wed":
                return 3;
            case "Thu":
                return 4;
            case "Fri":
                return 5;
            case "Sat":
                return 6;
            default:
                return 0;
        }
    }

    public void prepareAndDisplayNutritionForDate(String date) {
        fetchUserData(date);
    }

    private void fetchUserData(String date) {
        currentUser.fetchInBackground((user, e) -> {
            if (e == null && user != null) {
                nutritionProfile = user.getJSONObject("nutritionProfile");
                JSONObject mealPlan = user.getJSONObject("mealPlan");
                if (mealPlan != null) {

                    fetchMealPlanAndNutritionInfo(mealPlan, date);
                } else{
                    Log.e("NutritionManager", "Meal plan not found for user");
                    resetNutritionInformation();
                }
            } else {
                Log.e("NutritionManager", "Error fetching user data", e);
            }
        });
    }

    private void fetchMealPlanAndNutritionInfo(JSONObject mealPlan, String date) {
        JSONObject dailyMealPlan = mealPlan.optJSONObject(date);
        if (dailyMealPlan != null) {
            ArrayList<String> recipeIds = extractRecipeIds(dailyMealPlan);
            recipesToFetchCount = recipeIds.size();
            if (recipesToFetchCount == 0) {
                // If there are no recipes to fetch, we should reset the nutrition information
                resetNutritionInformation();
            }
            for (String recipeId : recipeIds) {
                Log.d("NutritionManager", "Fetching info for recipeId: " + recipeId);
                fetchRecipeNutritionInfo(recipeId);
            }
        } else {
            Log.e("NutritionManager", "No meal plan for date: " + date);
            // Since there is no meal plan for the selected date, reset the nutrition information.
            resetNutritionInformation();
        }
    }


    private ArrayList<String> extractRecipeIds(JSONObject dailyMealPlan) {
        ArrayList<String> recipeIds = new ArrayList<>();
        Iterator<String> it = dailyMealPlan.keys();
        while (it.hasNext()) {
            String mealType = it.next();
            JSONArray meal = dailyMealPlan.optJSONArray(mealType);
            for (int i = 0; i < meal.length(); i++) {
                recipeIds.add(meal.optString(i));
            }
        }
        return recipeIds;
    }


    private void fetchRecipeNutritionInfo(String recipeId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Recipes");
        query.getInBackground(recipeId, (recipe, e) -> {
            if (e == null) {
                Log.d("NutritionManager", "Recipe retrieved successfully for ID: " + recipeId);

                JSONObject recipeNutritionInfo = recipe.getJSONObject("nutritionInformation");
                Log.d("NutritionManager", "Recipe Nutrition Info: " + recipeNutritionInfo);


                if (recipeNutritionInfo != null) {
                    aggregateNutritionInformation(recipeNutritionInfo);
                } else {
                    Log.e("NutritionManager", "Nutrition info missing for recipe: " + recipeId);
                }

                if (--recipesToFetchCount == 0) {
                    Log.d("NutritionManager", "All recipes fetched, updating UI");

                    updateUIWithAggregatedNutrition();
                }
            } else {
                Log.e("NutritionManager", "Error fetching recipe nutrition info", e);
                Log.e("NutritionManager", "Error fetching recipe: " + recipeId + " Error: " + e.getMessage(), e);
                Log.d("NutritionManager", "Recipes left to fetch: " + recipesToFetchCount);

            }
        });
    }
    private void aggregateNutritionInformation(JSONObject nutritionInfo) {
        Iterator<String> keys = nutritionInfo.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONArray nutrientArray = nutritionInfo.optJSONArray(key);
            if (nutrientArray != null && nutrientArray.length() > 0) {
                float value = (float) nutrientArray.optDouble(0, 0); // Default to 0 if key not found
                Log.d("Aggregating", "Nutrient: " + key + ", Value: " + value);
                if (aggregatedNutrition.containsKey(key)) {
                    aggregatedNutrition.put(key, aggregatedNutrition.get(key) + value);
                } else {
                    aggregatedNutrition.put(key, value);
                }
            }
        }
    }

    private void updateUIWithAggregatedNutrition() {
        // Update macros separately
        updateMacros();

        // Update other nutrition values
        for (String key : aggregatedNutrition.keySet()) {
            if (!isMacroNutrient(key)) {
                updateNutrientViewLine(key, aggregatedNutrition.get(key));
            }
        }
    }


    private void updateMacros() {
        // Update protein, carbs, and fat
        updateNutritionView("protein", proteinProgressBar, proteinText, proteinDetailsText);
        updateNutritionView("carbohydrates", carbsProgressBar, carbsText, carbsDetailsText);
        updateNutritionView("fat", fatsProgressBar, fatsText, fatsDetailsText);
    }

    private String capitalizeFirstLetter(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
    private void resetMacroUI() {
        // Reset macro nutrients progress bars and texts to initial state
        proteinProgressBar.setProgress(0);
        proteinText.setText("0%");
        proteinDetailsText.setText("0g / 0g");

        carbsProgressBar.setProgress(0);
        carbsText.setText("0%");
        carbsDetailsText.setText("0g / 0g");

        fatsProgressBar.setProgress(0);
        fatsText.setText("0%");
        fatsDetailsText.setText("0g / 0g");
    }
    private void updateNutrientViewLine(String key, float consumedValue) {
        NutrientViewLines nutrientView = getView().findViewById(getResourceIdForKey(key));
        if (nutrientView != null) {
            float targetValue = nutritionProfile.optJSONArray(key).optLong(0);
            String unit = nutritionProfile.optJSONArray(key).optString(1);

            // Calculate the progress percentage
            int progressPercentage = 0;
            if (targetValue != 0) { // to avoid division by zero
                progressPercentage = (int) ((consumedValue / targetValue) * 100);
            }

            // Set the nutrient name and details
            nutrientView.setNutrientName(capitalizeFirstLetter(key.replace('_', ' '))); // Replacing underscores with spaces if any
            nutrientView.setNutrientDetails(String.format("%s %s / %s %s", consumedValue, unit, targetValue, unit));

            // Set the progress on the ProgressBar
            nutrientView.setProgress(progressPercentage);
        } else {
            Log.e("NutritionManager", "NutrientView not found for key: " + key);
        }
    }

    private void updateNutritionView(String nutrient, ProgressBar progressBar, TextView nutrientText, TextView detailsText) {
        if (nutritionProfile.has(nutrient)) {
            JSONArray nutrientValues = nutritionProfile.optJSONArray(nutrient);
            float goalValue = (float) nutrientValues.optDouble(0, 0); // Default to 0 if the first value is not a double
            float consumedValue = aggregatedNutrition.containsKey(nutrient) ? aggregatedNutrition.get(nutrient) : 0f;
            Log.d("UpdateView", "Nutrient: " + nutrient + ", Consumed: " + consumedValue + ", Goal: " + goalValue);

            updateProgress(progressBar, consumedValue, goalValue);

            // Change the updateTexts call to reflect the percentage of the goal completed
            float percentage = (consumedValue / goalValue) * 100;
            detailsText.setText(String.format(Locale.getDefault(), "%.1f/%.1f", consumedValue, goalValue));
            nutrientText.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));
        } else {
            Log.e("NutritionManager", "Nutrition profile does not contain: " + nutrient);
        }
    }

    private boolean isMacroNutrient(String nutrient) {
        return nutrient.equals("protein") || nutrient.equals("carbohydrates") || nutrient.equals("fat");
    }

    private void updateProgress(ProgressBar progressBar, float consumed, float goal) {
        progressBar.setMax((int) goal);
        progressBar.setProgress((int) consumed);
    }


    private int getResourceIdForKey(String key) {
        switch (key) {
            case "potassium":
                return R.id.potassium;
            case "fibre":
                return R.id.fibre;
            case "sodium":
                return R.id.sodium;
            case "vitaminD":
                return R.id.vitaminD;
            case "saturatedFat":
                return R.id.saturatedFat;
            case "zinc":
                return R.id.zinc;
            case "transFat":
                return R.id.transFat;
            case "vitaminA":
                return R.id.vitaminA;
            case "calories":
                return R.id.calories;
            case "vitaminB12":
                return R.id.vitaminB12;
            case "sugar":
                return R.id.sugar;
            case "calcium":
                return R.id.calcium;
            case "folate":
                return R.id.folate;
            case "magnesium":
                return R.id.magnesium;
            case "iron":
                return R.id.iron;
            case "cholesterol":
                return R.id.cholesterol;
            case "vitaminC":
                return R.id.vitaminC;
            default:
                return 0;
        }
    }

}