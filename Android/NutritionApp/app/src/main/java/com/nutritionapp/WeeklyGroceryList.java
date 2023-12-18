package com.nutritionapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This class represents the weekly grocery list feature in the application.
 * It displays all ingredients from the User table's groceryList column for the current week.
 * Ingredients from past weeks will be automatically deleted.
 */
public class WeeklyGroceryList extends Fragment implements IngredientAdapter.OnIngredientToggledListener {
    private TextView dateText;  // Displays the date for the current week
    private static final String PREFS_NAME = "UserSettings";  // Name of the shared preferences file
    private static final String WEEK_START_DAY = "WeekStartDay";  // Key for retrieving the start day of the week from shared preferences
    private ArrayList<GroceryItem> weeklyIngredients;  // List of ingredients for the current week
    private RecyclerView weeklyRecyclerView;  // RecyclerView to display the ingredients
    private IngredientAdapter weeklyIngredientAdapter;  // Adapter for the RecyclerView
    /**
     * Callback when an ingredient's toggle status is changed.
     *
     * @param formattedDate The formatted date string (e.g., "MMMM d, yyyy")
     * @param item          The grocery item whose toggle status has been changed.
     */
    @Override
    public void onIngredientToggled(String formattedDate, GroceryItem item) {
        updateToggledStatusOnBackend(formattedDate, item);
    }
    /**
     * Updates the toggle status of ingredient for the specified date on the backend.
     * for the given date, updates its toggle status, and saves the updated grocery list back
     * to the backend.
     *
     * @param formattedDate The formatted date string (e.g., "MMMM d, yyyy")
     * @param item          The grocery item whose toggle status needs to be updated.
     */
    private void updateToggledStatusOnBackend(String formattedDate, GroceryItem item) {
        String referenceDate = dateText.getText().toString();
        Calendar startOfWeek = getStartOfWeek(referenceDate);
        Calendar endOfWeek = getEndOfWeek(referenceDate);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            try {
                JSONObject groceryList = currentUser.getJSONObject("groceryList");

                // Iterate through each day of the week
                while (!startOfWeek.after(endOfWeek)) {
                    formattedDate = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(startOfWeek.getTime());

                    if (groceryList != null && groceryList.has(formattedDate)) {
                        JSONArray dateArray = groceryList.getJSONArray(formattedDate);
                        for (int i = 0; i < dateArray.length(); i++) {
                            JSONObject ingredientObject = dateArray.getJSONObject(i);
                            String name = ingredientObject.getString("ingredientName");
                            if (name.equals(item.getIngredientName())) {
                                ingredientObject.put("isToggled", item.isToggled());
                                break; // exit the loop once we find the matching ingredient
                            }
                        }
                        // Update the groceryList with modified dateArray
                        groceryList.put(formattedDate, dateArray);
                    }
                    // Move to the next day
                    startOfWeek.add(Calendar.DATE, 1);
                }

                // Save the user after all the updates
                currentUser.put("groceryList", groceryList);
                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.d("Save Status", "Successfully saved!");
                        } else {
                            Log.e("Save Error", "Error while saving: " + e.getMessage());
                        }
                    }
                });
            } catch (JSONException e) {
                Log.e("Error", "Error processing JSON during toggle update: " + e.getMessage());
            }
        }
    }
    /**
     *  initialize UI components, and load ingredients for the current week.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.weekly_grocery, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Now you can safely access your views
        weeklyRecyclerView = view.findViewById(R.id.weeklyGroceryListRecyclerView);
        dateText = view.findViewById(R.id.weeklyText);
        weeklyIngredients = new ArrayList<>();  // Initialize your list
        weeklyIngredientAdapter = new IngredientAdapter(weeklyIngredients, this, true); // Initialize your adapter with the list
        setupRecyclerView(weeklyRecyclerView, weeklyIngredientAdapter, weeklyIngredients);

        // Load and process data
        SharedPreferences settings = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String weekStartDay = settings.getString(WEEK_START_DAY, "Sun");
        updateWeekDisplay(weekStartDay);
        loadIngredientsForWeek(dateText.getText().toString());
        deletePastWeekIngredients();
    }

    /**
     * Load ingredients from the user table for the specified week.
     *
     * @param referenceDate Date string indicating the current week.
     */
    private void loadIngredientsForWeek(String referenceDate) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            parseUserGroceryListForWeek(currentUser, referenceDate);
        }
    }
    /**
     * Parse and aggregate ingredients from the given user's grocery list for the specified week.
     *
     * @param currentUser   The user whose grocery list will be parsed.
     * @param referenceDate Date string indicating the current week.
     */
    private void parseUserGroceryListForWeek(ParseUser currentUser, String referenceDate) {
        // Extract grocery list from the user object
        JSONObject groceryList = currentUser.getJSONObject("groceryList");
        ArrayList<GroceryItem> weeklyIngredients = new ArrayList<>();
        Map<String, GroceryItem> ingredientMap = new HashMap<>();
        if (groceryList != null) {
            // Calculate the start and end dates for the week
            Calendar startOfWeek = getStartOfWeek(referenceDate);
            Calendar endOfWeek = getEndOfWeek(referenceDate);
            // Iterate through each day of the week
            while (!startOfWeek.after(endOfWeek)) {
                String formattedDate = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(startOfWeek.getTime());
                if (groceryList.has(formattedDate)) {
                    parseDateIngredients(formattedDate, groceryList, ingredientMap);
                }
                startOfWeek.add(Calendar.DATE, 1);
            }
        }
        // Convert the map values to the list and update weekly list
        weeklyIngredients.addAll(ingredientMap.values());
        updateAdapter(weeklyIngredients);
    }
    /**
     * Load ingredients again when the fragment is resumed.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadIngredientsForWeek(dateText.getText().toString());
    }

    /**
     * Parse and aggregate ingredients from the given date's array in the grocery list.
     *
     * @param formattedDate Date in "MMMM d, yyyy" format.
     * @param groceryList   JSON object containing the user's grocery list.
     * @param ingredientMap Map used to aggregate ingredient quantities.
     */
    private void parseDateIngredients(String formattedDate, JSONObject groceryList, Map<String, GroceryItem> ingredientMap) {
        try {
            JSONArray dateArray = groceryList.getJSONArray(formattedDate);

            // loop through the ingredient array for the given date
            for (int i = 0; i < dateArray.length(); i++) {
                JSONObject ingredientObject = dateArray.getJSONObject(i);
                String name = ingredientObject.getString("ingredientName");
                double amount = ingredientObject.getDouble("quantity");
                String unit = ingredientObject.getString("quantityUnit");
                boolean isToggled = ingredientObject.optBoolean("isToggled");
                boolean mealPlanAdded = ingredientObject.optBoolean("mealPlanAdded");
                String mapKey = name + "-" + unit;  // Combine name and unit to create a unique key

                // If the ingredient is already in the map
                if (ingredientMap.containsKey(mapKey)) {
                    GroceryItem existingItem = ingredientMap.get(mapKey);
                    if (existingItem != null) {
                        // If either the existing item or the current item is not toggled
                        if (!existingItem.isToggled() || !isToggled) {
                            // Set the combined item as not toggled
                            existingItem.setToggled(false);
                            // Only combine quantities if current item is not toggled
                            if (!isToggled) {
                                existingItem.setIngredientAmount(existingItem.getIngredientAmount() + amount);
                            }
                        }
                        // If both the existing item and the current item are toggled
                        else {
                            existingItem.setIngredientAmount(existingItem.getIngredientAmount() + amount);
                        }
                    }
                }
                // If it's not in the map and isToggled is false
                else {
                    GroceryItem newItem = new GroceryItem(name, amount, unit, isToggled, mealPlanAdded);
                    ingredientMap.put(mapKey, newItem);
                }
            }
        } catch (JSONException e) {
            Log.e("Error", "Error parsing date ingredients: " + e.getMessage());
        }
    }




    /**
     * Update the RecyclerView adapter with new ingredients.
     *
     * @param newIngredients Updated list of ingredients.
     */
    private void updateAdapter(ArrayList<GroceryItem> newIngredients) {
        weeklyIngredients.clear();
        weeklyIngredients.addAll(newIngredients);
        weeklyIngredientAdapter.notifyDataSetChanged();

        checkAndUpdateListVisibility();

    }
    private void checkAndUpdateListVisibility() {
        if (weeklyIngredients.isEmpty()) {
            // List is empty, hide RecyclerView and show TextView with a message
            weeklyRecyclerView.setVisibility(View.GONE);
            TextView emptyListTextView = getView().findViewById(R.id.emptyListTextView);
            emptyListTextView.setText("No ingredients for this week");
            emptyListTextView.setVisibility(View.VISIBLE);
        } else {
            // List has items, show RecyclerView and hide TextView
            weeklyRecyclerView.setVisibility(View.VISIBLE);
            TextView emptyListTextView = getView().findViewById(R.id.emptyListTextView);
            emptyListTextView.setVisibility(View.GONE);
        }
    }

    /**
     * Sets up the given RecyclerView with provided adapter and ingredients list.
     *
     * @param recyclerView   The RecyclerView to set up.
     * @param adapter        The adapter to use with the RecyclerView.
     * @param ingredientsList The list of ingredients to display.
     */
    private void setupRecyclerView(RecyclerView recyclerView, IngredientAdapter adapter, ArrayList<GroceryItem> ingredientsList) {
        // Setting up a linear layout manager for the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        // Adding dividers between each item in the RecyclerView
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            // This method is called when an item is dragged and moved within the RecyclerView
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int from = (int) viewHolder.itemView.getTag();
                int to = (int) target.itemView.getTag();
                // Swap the items positions
                Collections.swap(ingredientsList, from, to);
                adapter.notifyItemMoved(from, to);
                viewHolder.itemView.setTag(to);
                target.itemView.setTag(from);
                return true;
            }
            // This method is called when an item is swiped (for now we don't allow to swap or move ingredients)
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }

        });
        // Attach the touch helper to the RecyclerView
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    /**
     * Converts the provided day string to its corresponding index.
     *
     * @param day The string representation of the day ("Sun", "Mon", etc.).
     * @return The index of the provided day.
     */
    private int getDayOfWeekIndex(String day) {
        switch (day) {
            // Mapping each day to its respective index
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
                return 0;// Default to Sunday
        }
    }
    /**
     * Calculates the start date of the week based on the provided week starting day.
     *
     * @param weekStartDay The day the week starts on ("Sun", "Mon", etc.).
     * @return A Calendar object set to the start date of the week.
     */
    private Calendar getStartOfWeek(String weekStartDay) {
        Calendar calendar = Calendar.getInstance();
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int startDayOfWeek = getDayOfWeekIndex(weekStartDay) + 1; // Calendar's DAY_OF_WEEK starts from 1 (Day selected)
        // calculate the offset to get the start of the week
        int daysOffset = startDayOfWeek - currentDayOfWeek;
        if (daysOffset > 0) {
            daysOffset -= 7;
        }
        calendar.add(Calendar.DAY_OF_YEAR, daysOffset);
        return calendar;
    }
    /**
     * Calculates the end date of the week based on the provided week starting day.
     *
     * @param weekStartDay The day the week starts on ("Sun", "Mon", etc.).
     * @return A Calendar object set to the end date of the week.
     */
    private Calendar getEndOfWeek(String weekStartDay) {
        Calendar startOfWeek = getStartOfWeek(weekStartDay);
        Calendar endOfWeek = (Calendar) startOfWeek.clone();
        endOfWeek.add(Calendar.DAY_OF_YEAR, 6);
        return endOfWeek;
    }
    /**
     * Updates the dateText view to display the current week's range based on the provided week starting day.
     *
     * @param weekStartDay The day the week starts on ("Sun", "Mon", etc.).
     */
    private void updateWeekDisplay(String weekStartDay) {
        Calendar startOfWeek = getStartOfWeek(weekStartDay);
        Calendar endOfWeek = getEndOfWeek(weekStartDay);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        String weekRange = sdf.format(startOfWeek.getTime()) + " - " + sdf.format(endOfWeek.getTime());
        dateText.setText("Week: " + weekRange);
    }
    /**
     * Deletes ingredients from the previous week from the user's grocery list.
     */
    private void deletePastWeekIngredients() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) return;

        JSONObject groceryList = currentUser.getJSONObject("groceryList");
        if (groceryList == null) return;

        SharedPreferences settings = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String weekStartDay = settings.getString(WEEK_START_DAY, "Sun");

        // Calculate the start and end dates for the past week based on user preference
        Calendar startOfLastWeek = getStartOfWeek(weekStartDay);
        startOfLastWeek.add(Calendar.WEEK_OF_YEAR, -1); // move to last week
        Calendar endOfLastWeek = getEndOfWeek(weekStartDay);
        endOfLastWeek.add(Calendar.WEEK_OF_YEAR, -1); // move to last week

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

        while (!startOfLastWeek.after(endOfLastWeek)) {
            String formattedDate = sdf.format(startOfLastWeek.getTime());
            groceryList.remove(formattedDate);
            startOfLastWeek.add(Calendar.DATE, 1);
        }

        currentUser.put("groceryList", groceryList);
        currentUser.saveInBackground(e -> {
            if (e == null) {
                Log.i("Info", "Past week's ingredients deleted successfully!");
            } else {
                Log.e("Error", "Error deleting past week's ingredients: " + e.getMessage());
            }
        });
    }
}