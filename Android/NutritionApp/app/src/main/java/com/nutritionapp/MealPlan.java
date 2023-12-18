package com.nutritionapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MealPlan extends Fragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TextView dateText;
    private static final String PREFS_NAME = "UserSettings";
    private static final String WEEK_START_DAY = "WeekStartDay";
    private TabLayoutMediator tabLayoutMediator;
    private RecyclerView breakfastRecyclerView;
    private RecyclerView lunchRecyclerView;
    private RecyclerView dinnerRecyclerView;
    private RecyclerView dessertRecyclerView;
    private RecyclerView snackRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.meal_plan, container, false);

        dateText = view.findViewById(R.id.dateText);
        tabLayout = view.findViewById(R.id.tabLayout);
        breakfastRecyclerView = view.findViewById(R.id.breakfastRecyclerView);
        lunchRecyclerView = view.findViewById(R.id.lunchRecyclerView);
        dinnerRecyclerView = view.findViewById(R.id.dinnerRecyclerView);
        dessertRecyclerView = view.findViewById(R.id.DessertRecyclerView);
        snackRecyclerView = view.findViewById(R.id.snackRecyclerView);
        breakfastRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lunchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dinnerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dessertRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        snackRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setButtonListener(view, R.id.addBreakfastRecipeButton, "breakfast");
        setButtonListener(view, R.id.addLunchRecipeButton, "lunch");
        setButtonListener(view, R.id.addDinnerRecipeButton, "dinner");
        setButtonListener(view, R.id.addDessertsRecipeButton, "desserts");
        setButtonListener(view, R.id.addSnacksRecipeButton, "snacks");
        //here will formate and set the current day on text view
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        dateText.setText(currentDate);
        // setting up the week and loading meals for the selected day
        setupTabLayout();
        deletePastWeekMealPlans();
        String initialDate = getFormattedDateBasedOnTab();
        loadMealsForDate(initialDate);

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

        return view;
    }
    /**
     * Return the index of a given day of the week.
     *
     * @param day - The abbreviated name of the day.
     * @return index of the day of the week.
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
    /**
     * Setup tab layout with days of the week based on user's preferences.
     */
    private void setupTabLayout() {
        //clear any tab exist
        tabLayout.removeAllTabs();
        // fetching the week start day from shared preferences
        SharedPreferences settings = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String weekStartDay = settings.getString(WEEK_START_DAY, "Sun");
        // calendar instance for date manipulations
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(getDayOfWeekIndex(weekStartDay) + 1);
        calendar.set(Calendar.DAY_OF_WEEK, getDayOfWeekIndex(weekStartDay) + 1);
        // looping through the days of the week to add as tabs
        for (int i = 0; i < 7; i++) {
            String dayOfWeek = new SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.getTime());
            tabLayout.addTab(tabLayout.newTab().setText(dayOfWeek));
            // Creating calendar instances to check if a tab date is before the current date
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

            if (tabDate.before(today)) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null) {
                    tab.view.setAlpha(0.4f);  // This will reduce the brightness for past days
                }
            }

            calendar.add(Calendar.DATE, 1);
        }

        // Set the current day tab to be selected by default
        int todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1 - getDayOfWeekIndex(weekStartDay);
        if (todayIndex < 0) todayIndex += 7;
        tabLayout.selectTab(tabLayout.getTabAt(todayIndex));
        // Adding a listener to handle tab selection changes
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.setFirstDayOfWeek(getDayOfWeekIndex(weekStartDay) + 1);
                selectedDate.set(Calendar.DAY_OF_WEEK, getDayOfWeekIndex(weekStartDay) + 1);
                selectedDate.add(Calendar.DATE, tab.getPosition());

                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                String formattedDate = sdf.format(selectedDate.getTime());

                updateDateText(selectedDate);
                loadMealsForDate(formattedDate);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
    /**
     * Retrieve the meal plan associated with the current user.
     *
     * @return a JSONObject representing the user's meal plan, or null if none exists.
     */
    private void updateDateText(Calendar selectedDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        String formattedDate = sdf.format(selectedDate.getTime());
        dateText.setText(formattedDate);
    }
    /**
     * Retrieve the meal plan associated with the current user.
     *
     * @return a JSONObject representing the user's meal plan, or null if none exists.
     */
    private void loadMealsForDate(String formattedDate) {
        // Fetch the meal plan for the current user
        JSONObject mealPlan = getMealPlanForUser();
        if (mealPlan != null) {// Check if the meal plan has an entry for the selected date
            try {
                if (mealPlan.has(formattedDate)) {  // Check if the mealPlan contains the date
                    JSONObject datePlan = mealPlan.getJSONObject(formattedDate);
                    // Load meals into respective RecyclerViews based on the meal type
                    loadMealsInRecyclerView(datePlan.getJSONArray("breakfast"), breakfastRecyclerView);
                    loadMealsInRecyclerView(datePlan.getJSONArray("lunch"), lunchRecyclerView);
                    loadMealsInRecyclerView(datePlan.getJSONArray("dinner"), dinnerRecyclerView);
                    loadMealsInRecyclerView(datePlan.getJSONArray("desserts"), dessertRecyclerView);
                    loadMealsInRecyclerView(datePlan.getJSONArray("snacks"), snackRecyclerView);
                    // Set up swipe to delete functionality for each meal type's RecyclerView
                    setupSwipeToDelete(breakfastRecyclerView, "breakfast");
                    setupSwipeToDelete(lunchRecyclerView, "lunch");
                    setupSwipeToDelete(dinnerRecyclerView, "dinner");
                    setupSwipeToDelete(dessertRecyclerView, "desserts");
                    setupSwipeToDelete(snackRecyclerView, "snacks");

                } else {
                    // Clear the RecyclerViews if there's no data for the selected day
                    clearRecyclerView(breakfastRecyclerView);
                    clearRecyclerView(lunchRecyclerView);
                    clearRecyclerView(dinnerRecyclerView);
                    clearRecyclerView(dessertRecyclerView);
                    clearRecyclerView(snackRecyclerView);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            // Clear the RecyclerViews if the mealPlan is null
            clearRecyclerView(breakfastRecyclerView);
            clearRecyclerView(lunchRecyclerView);
            clearRecyclerView(dinnerRecyclerView);
            clearRecyclerView(dessertRecyclerView);
            clearRecyclerView(snackRecyclerView);
        }
    }
    /**
     * Clears all items from a given RecyclerView.
     *
     * @param recyclerView - The RecyclerView to be cleared.
     */
    private void clearRecyclerView(RecyclerView recyclerView) {
        if (recyclerView.getAdapter() != null) {
            recyclerView.setAdapter(null);  // Set the adapter to null
        }
    }

    /**
     * Retrieve the meal plan associated with the current user.
     *
     * @return a JSONObject representing the user's meal plan, or null if none exists.
     */
    private JSONObject getMealPlanForUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            JSONObject mealPlan = currentUser.getJSONObject("mealPlan");
            return mealPlan;
        }
        return null;
    }
    /**
     * Load meals into a RecyclerView given a JSONArray of meal IDs.
     *
     * @param mealIdsArray - The JSONArray containing the IDs of the meals.
     * @param recyclerView - The RecyclerView to populate.
     */
    private void loadMealsInRecyclerView(JSONArray mealIdsArray, RecyclerView recyclerView) {
        List<String> mealIds = new ArrayList<>();
        for (int i = 0; i < mealIdsArray.length(); i++) {
            try {
                mealIds.add(mealIdsArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // using the Parse query to fetch recipes based on the ObjectIDs
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Recipes");
        query.whereContainedIn("objectId", mealIds);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> recipeObjects, ParseException e) {
                if (e == null) {
                    Map<String, RecipeList> recipesMap = new HashMap<>();
                    for (ParseObject recipeObject : recipeObjects) {
                        String id = recipeObject.getObjectId();
                        RecipeList recipe = UtilityRecipe.convertParseObjectToRecipeList(recipeObject);
                        recipesMap.put(id, recipe);
                    }
                    // Create the final list respecting duplicates
                    List<RecipeList> recipesToShow = new ArrayList<>();
                    for (String mealId : mealIds) {
                        recipesToShow.add(recipesMap.get(mealId));
                    }
                    MealPlanAdapter  adapter = new MealPlanAdapter(recipesToShow);
                    recyclerView.setAdapter(adapter);
                } else {
                    Log.e("back4app", "Error: " + e.getMessage());
                }
            }
        });
    }
    /**
     * Returns a formatted date string based on the currently selected tab.
     *
     * @return the formatted date string.
     */
    private String getFormattedDateBasedOnTab() {
        SharedPreferences settings = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String weekStartDay = settings.getString(WEEK_START_DAY, "Sun");

        Calendar selectedDate = Calendar.getInstance();
        selectedDate.setFirstDayOfWeek(getDayOfWeekIndex(weekStartDay) + 1);
        selectedDate.set(Calendar.DAY_OF_WEEK, getDayOfWeekIndex(weekStartDay) + 1);
        selectedDate.add(Calendar.DATE, tabLayout.getSelectedTabPosition());

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        return sdf.format(selectedDate.getTime());
    }
    /**
     * Sets a listener on a button to navigate to the home page and save selected meal type and date.
     *
     * @param view       - The parent view containing the button.
     * @param buttonId   - The ID of the button.
     * @param mealType   - The meal type associated with the button.
     */
    private void setButtonListener(View view, int buttonId, String mealType) {

        Button button = view.findViewById(buttonId);
        button.setOnClickListener(v -> {
            // Convert the date from EEEE, MMMM d format to MMMM d, yyyy format
            SimpleDateFormat sdfCurrent = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
            Date currentDate = null;
            try {
                currentDate = sdfCurrent.parse(dateText.getText().toString());
            } catch (java.text.ParseException e) {
                throw new RuntimeException(e);
            }

            SimpleDateFormat sdfTarget = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            String targetDateString = getFormattedDateBasedOnTab();

            // Save to SharedPreferences
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("RecipeData", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("selectedDate", targetDateString);
            editor.putString("selectedMealType", mealType);
            editor.apply();

            // Navigate to HomePage
            Intent homePage = new Intent(getActivity(), HomePage.class);
            startActivity(homePage);
        });
    }
    /**
     * Deletes a given recipe from the meal plan for a specific date and meal type.
     *
     * @param formattedDate - The date from which to remove the recipe.
     * @param mealType      - The type of meal (e.g., "breakfast", "lunch", etc.).
     * @param recipeId      - The ID of the recipe to remove.
     */
    private void deleteRecipeFromMealPlan(String formattedDate, String mealType, String recipeId) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            try {
                JSONObject mealPlan = currentUser.getJSONObject("mealPlan");
                if (mealPlan != null && mealPlan.has(formattedDate)) {
                    JSONObject datePlan = mealPlan.getJSONObject(formattedDate);
                    JSONArray selectedMealArray = datePlan.getJSONArray(mealType);

                    boolean isRecipePresent = false;
                    for (int i = 0; i < selectedMealArray.length(); i++) {
                        if (selectedMealArray.getString(i).equals(recipeId)) {
                            selectedMealArray.remove(i);
                            isRecipePresent = true;
                            break;
                        }
                    }

                    if (isRecipePresent) {
                        // Check if all meal types are empty
                        boolean allEmpty = true;
                        String[] mealTypes = {"breakfast", "lunch", "dinner", "desserts", "snacks"};
                        for (String type : mealTypes) {
                            JSONArray meals = datePlan.optJSONArray(type);
                            if (meals != null && meals.length() > 0) {
                                allEmpty = false;
                                break;
                            }
                        }

                        // If all meal types are empty, remove the date entry
                        if (allEmpty) {
                            mealPlan.remove(formattedDate);
                        }

                        currentUser.put("mealPlan", mealPlan);
                        currentUser.saveEventually();

                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Recipes");  // Assuming your Recipe table is named "Recipe"
                        query.getInBackground(recipeId, (recipe, e) -> {
                            if (e == null) {
                                JSONArray ingredientsArray = recipe.getJSONArray("ingredients");
                                updateGroceryList(formattedDate, ingredientsArray);
                            } else {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Enables swipe-to-delete functionality for a given RecyclerView.
     *
     * @param recyclerView - The RecyclerView to enable swipe-to-delete on.
     * @param mealType     - The type of meal (e.g., "breakfast", "lunch", etc.).
     */
    private void setupSwipeToDelete(RecyclerView recyclerView, String mealType) {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                MealPlanAdapter adapter = (MealPlanAdapter) recyclerView.getAdapter();

                if (adapter != null) {
                    RecipeList recipeToRemove = adapter.getItemAt(position);

                    // Remove from the backend
                    deleteRecipeFromMealPlan(getFormattedDateBasedOnTab(), mealType, recipeToRemove.getId());

                    // Remove from UI
                    adapter.removeRecipeAt(position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;

                if (dX < 0) { // Left swipe
                    Drawable shadowDrawable = getResources().getDrawable(R.drawable.shadow_gradient);

                    // Adjust the shadow bounds based on the swiped item's actual position on the screen
                    int top = itemView.getTop() + 40;
                    int bottom = itemView.getBottom() - 40;
                    int right = itemView.getRight() - 40;
                    int left = itemView.getLeft()+120;
                    shadowDrawable.setBounds(left, top, right, bottom);
                    shadowDrawable.draw(c);

                    // Draw delete icon only when the item is being swiped
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && isCurrentlyActive) {
                        Drawable deleteIcon = getResources().getDrawable(R.drawable.delete_icon);
                        int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
                        int iconRight = itemView.getRight() - iconMargin ;
                        int iconLeft = iconRight - deleteIcon.getIntrinsicWidth() ;

                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        deleteIcon.draw(c);
                    }
                }
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                if (viewHolder != null && actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                    viewHolder.itemView.setBackgroundColor(0);  // reset to the default background color
                }
                super.onSelectedChanged(viewHolder, actionState);
            }

        }).attachToRecyclerView(recyclerView);
    }
    /**
     * Converts a drawable into a bitmap.
     *
     * @param drawable - The drawable to convert.
     * @return the resulting bitmap.
     */
    public Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void updateGroceryList(String formattedDate, JSONArray ingredientsArray) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        JSONObject groceryList = currentUser.getJSONObject("groceryList");
        JSONArray dateGroceryList = groceryList.optJSONArray(formattedDate);

        if (dateGroceryList == null) return;

        for (int i = 0; i < ingredientsArray.length(); i++) {
            try {
                JSONArray ingredient = ingredientsArray.getJSONArray(i);
                String ingredientName = ingredient.getString(0);
                double ingredientQuantity = ingredient.getDouble(1);
                String ingredientUnit = ingredient.getString(2);

                boolean ingredientFound = false;  // New variable to track if the ingredient is found

                for (int j = 0; j < dateGroceryList.length(); j++) {
                    JSONObject groceryItem = dateGroceryList.getJSONObject(j);
                    if (groceryItem.getBoolean("mealPlanAdded")
                            && ingredientName.equals(groceryItem.getString("ingredientName"))
                            && ingredientUnit.equals(groceryItem.getString("quantityUnit"))) {

                        ingredientFound = true;  // Set ingredientFound to true if the ingredient is found

                        if (ingredientQuantity == groceryItem.getDouble("quantity")) {
                            dateGroceryList.remove(j);
                        } else {
                            double newQuantity = groceryItem.getDouble("quantity") - ingredientQuantity;
                            groceryItem.put("quantity", newQuantity);
                        }
                        break;
                    }
                }

                // If ingredient is not found, just skip and continue to the next ingredient
                if (!ingredientFound) continue;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        currentUser.put("groceryList", groceryList);
        currentUser.saveEventually();
    }

    private void deletePastWeekMealPlans() {
        SharedPreferences settings = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String weekStartDay = settings.getString(WEEK_START_DAY, "Sun");

        Calendar today = Calendar.getInstance();
        Calendar startOfWeek = (Calendar) today.clone();
        startOfWeek.setFirstDayOfWeek(getDayOfWeekIndex(weekStartDay) + 1);
        startOfWeek.set(Calendar.DAY_OF_WEEK, getDayOfWeekIndex(weekStartDay) + 1);
        startOfWeek.add(Calendar.WEEK_OF_YEAR, -1);  // Move to the start of the previous week

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String startOfPreviousWeek = sdf.format(startOfWeek.getTime());

        // Now delete all meal plans before this date
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            JSONObject mealPlan = currentUser.getJSONObject("mealPlan");
            if (mealPlan == null) {
                return; // exit the method to avoid further processing
            }
            Iterator<String> dates = mealPlan.keys();
            while (dates.hasNext()) {
                String date = dates.next();
                try {
                    Date mealPlanDate = sdf.parse(date);
                    if (mealPlanDate != null && mealPlanDate.before(startOfWeek.getTime())) {
                        dates.remove();  // This will remove the date from the JSONObject
                    }
                } catch (java.text.ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            currentUser.put("mealPlan", mealPlan);
            currentUser.saveEventually();
        }
    }
    public void addToMealPlan(ParseUser user, String date, String mealType, String recipeId) {
        JSONObject mealPlan = user.getJSONObject("mealPlan");
        if (mealPlan == null) {
            mealPlan = new JSONObject();
        }

        try {
            JSONObject dayMeals;
            if (mealPlan.has(date)) {
                dayMeals = mealPlan.getJSONObject(date);
            } else {
                dayMeals = new JSONObject();
            }

            JSONArray mealArray;
            if (dayMeals.has(mealType)) {
                mealArray = dayMeals.getJSONArray(mealType);
            } else {
                mealArray = new JSONArray();
            }
            mealArray.put(recipeId);

            dayMeals.put(mealType, mealArray);
            mealPlan.put(date, dayMeals);

            user.put("mealPlan", mealPlan);
            user.saveInBackground();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}

