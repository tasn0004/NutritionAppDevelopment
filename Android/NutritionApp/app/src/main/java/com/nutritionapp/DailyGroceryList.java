package com.nutritionapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
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
 * Represents a Fragment that displays a daily grocery list.
 * The list is retrieved from a backend and presents a list of grocery items for each day.
 * You can toggle and delete out each ingredient from the list. Also can add ingredient to the list too
 */
public class DailyGroceryList extends Fragment implements IngredientAdapter.OnIngredientToggledListener {
    private ArrayList<GroceryItem> dailyIngredients;
    private RecyclerView dailyRecyclerView;
    private IngredientAdapter dailyIngredientAdapter;
    private String selectedDate;
    private TabLayout tabLayout;
    private TextView dateText;
    private static final String PREFS_NAME = "UserSettings";
    private static final String WEEK_START_DAY = "WeekStartDay";
    private static final String SELECTED_DATE_KEY = "selectedDate";
    /**
     * Callback when an ingredient's toggle status is changed.
     *
     * @param formattedDate The formatted date string (e.g., "MMMM d, yyyy").
     * @param item          The grocery item whose toggle status has been changed.
     */
    @Override
    public void onIngredientToggled(String formattedDate, GroceryItem item) {
        updateToggledStatusOnBackend(formattedDate, item);
    }
    /**
     * Updates the toggle status of a given ingredient for the specified date in the backend.
     *
     * @param formattedDate The formatted date string (e.g., "MMMM d, yyyy").
     * @param item          The grocery item whose toggle status needs to be updated.
     */
    private void updateToggledStatusOnBackend(String formattedDate, GroceryItem item) {
        formattedDate = dateText.getText().toString();// Get the selected date
        ParseUser currentUser = ParseUser.getCurrentUser();// Get current user
        // Check if user exists
        if (currentUser != null) {
            try {
                JSONObject groceryList = currentUser.getJSONObject("groceryList");// Get the user's grocery list
                // Check if the list has items for the given date
                if (groceryList != null && groceryList.has(formattedDate)) {
                    JSONArray dateArray = groceryList.getJSONArray(formattedDate);
                    // Search for the item to update its toggle status
                    for (int i = 0; i < dateArray.length(); i++) {
                        JSONObject ingredientObject = dateArray.getJSONObject(i);
                        String name = ingredientObject.getString("ingredientName");
                        if (name.equals(item.getIngredientName())) {
                            ingredientObject.put("isToggled", item.isToggled());
                            break; // exit the loop once we find the matching ingredient
                        }
                    }
                    // Update the groceryList with modified dateArray and save the user
                    groceryList.put(formattedDate, dateArray);
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
                }
            } catch (JSONException e) {
                Log.e("Error", "Error processing JSON during toggle update: " + e.getMessage());
            }
        }
    }
    /**
     * Inflate the layout for this fragment and set up its UI components.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.daily_grocery, container, false);

        return view;
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    dailyIngredients = new ArrayList<>();
    // ingredients for daily
    tabLayout = view.findViewById(R.id.tabLayout);
    dateText = view.findViewById(R.id.dateText);
    // Setup daily RecyclerView
    dailyRecyclerView = view.findViewById(R.id.groceryListRecyclerView);
    dailyIngredientAdapter = new IngredientAdapter(dailyIngredients, this, true );
    setupRecyclerView(dailyRecyclerView, dailyIngredientAdapter, dailyIngredients);
    // Set the button click for adding items
    SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
    String currentDate = sdf.format(new Date());
        dateText.setText(currentDate);
    setupTabLayout();
    loadIngredientsForDate(dateText.getText().toString()); // Fetch ingredients for the current date
    setupAddItemButton(view);
    //clearGroceryList();

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
    }
    private void loadIngredientsForDate(String formattedDate) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            parseUserGroceryList(currentUser, formattedDate);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        loadIngredientsForDate(dateText.getText().toString());
    }
    private void parseUserGroceryList(ParseUser currentUser, String formattedDate) {
        JSONObject groceryList = currentUser.getJSONObject("groceryList");
        ArrayList<GroceryItem> ingredients = new ArrayList<>();
        if (groceryList != null && groceryList.has(formattedDate)) {
            parseDateIngredients(formattedDate, groceryList, ingredients);
        }
        updateAdapter(ingredients);
        updateListVisibility(); // Add this line

    }
    private void updateListVisibility() {
        if (dailyIngredients.isEmpty()) {
            dailyRecyclerView.setVisibility(View.GONE);
            TextView emptyListTextView = getView().findViewById(R.id.emptyListTextView);
            emptyListTextView.setText("No ingredients for today");
            emptyListTextView.setVisibility(View.VISIBLE);
        } else {
            dailyRecyclerView.setVisibility(View.VISIBLE);
            TextView emptyListTextView = getView().findViewById(R.id.emptyListTextView);
            emptyListTextView.setVisibility(View.GONE);
        }
    }

    private void parseDateIngredients(String formattedDate, JSONObject groceryList, ArrayList<GroceryItem> ingredients) {
        try {
            JSONArray dateArray = groceryList.getJSONArray(formattedDate);

            //Maintain a Map for unique ingredient names and their respective GroceryItem
            Map<String, GroceryItem> ingredientMap = new HashMap<>();

            // Iterating through the array and aggregating quantities of identical items
            for (int i = 0; i < dateArray.length(); i++) {
                JSONObject ingredientObject = dateArray.getJSONObject(i);
                String name = ingredientObject.getString("ingredientName");
                double amount = ingredientObject.getDouble("quantity");
                String unit = ingredientObject.getString("quantityUnit");
                boolean isToggled = ingredientObject.optBoolean("isToggled");
                boolean mealPlanAdded = ingredientObject.optBoolean("mealPlanAdded");

                // If the ingredient is already in the map, add to its quantity
                if(ingredientMap.containsKey(name)){
                    GroceryItem existingItem = ingredientMap.get(name);
                    if (existingItem != null) {
                        existingItem.setIngredientAmount(existingItem.getIngredientAmount() + amount);
                        // Consider the toggle status for aggregated ingredients (you may want to modify this logic if needed)
                        existingItem.setToggled(existingItem.isToggled() || isToggled);
                    }
                }
                //If it's not in the map, add it
                else {
                    GroceryItem newItem = new GroceryItem(name, amount, unit, isToggled, mealPlanAdded);
                    newItem.setToggled(isToggled);
                    ingredientMap.put(name, newItem);
                }
            }

            //Populate the List
            ingredients.addAll(ingredientMap.values());
        } catch (JSONException e) {
            Log.e("Error", "Error processing JSON: " + e.getMessage());
        }
    }


    private void updateAdapter(ArrayList<GroceryItem> ingredients) {
        dailyIngredients.clear();
        dailyIngredients.addAll(ingredients);
        dailyIngredientAdapter.notifyDataSetChanged();
    }

    private void setupTabLayout() {
        tabLayout.removeAllTabs();

        SharedPreferences settings = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String weekStartDay = settings.getString(WEEK_START_DAY, "Sun");

        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(getDayOfWeekIndex(weekStartDay) + 1);
        calendar.set(Calendar.DAY_OF_WEEK, getDayOfWeekIndex(weekStartDay) + 1);

        for (int i = 0; i < 7; i++) {
            String dayOfWeek = new SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.getTime());
            tabLayout.addTab(tabLayout.newTab().setText(dayOfWeek));

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


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.setFirstDayOfWeek(getDayOfWeekIndex(weekStartDay) + 1);
                selectedDate.set(Calendar.DAY_OF_WEEK, getDayOfWeekIndex(weekStartDay) + 1);
                selectedDate.add(Calendar.DATE, tab.getPosition());
                updateDateText(selectedDate);

                // Load the ingredients for the selected date
                loadIngredientsForDate(dateText.getText().toString());
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
     * Updates the displayed date text based on the provided Calendar object.
     *
     * @param selectedDate The selected date as a Calendar object.
     */
    private void updateDateText(Calendar selectedDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String formattedDate = sdf.format(selectedDate.getTime());
        dateText.setText(formattedDate);
    }
    /**
     * Converts a day of the week string into its corresponding index.
     *
     * @param day A string representing a day of the week (e.g., "Sun", "Mon").
     * @return An integer index for the day of the week, starting with Sunday as 0.
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
     * Sets up the provided RecyclerView with the specified adapter and ingredient list.
     * Adds dividers between items and enables swipe-to-delete functionality.
     *
     * @param recyclerView   The RecyclerView to set up.
     * @param adapter        The adapter for the RecyclerView.
     * @param ingredientsList The list of ingredients to display.
     */
    private void setupRecyclerView(RecyclerView recyclerView, IngredientAdapter adapter, ArrayList<GroceryItem> ingredientsList) {
        // Setting up a linear layout manager for the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        // Adding dividers between each item in the RecyclerView
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        // ItemTouchHelper allows for item to swap to left
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            // This method is called when an item is dragged and moved within the RecyclerView
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            // This method is called when an item is swiped ( left to delete )
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < adapter.getItemCount()) {
                    GroceryItem removedItem = adapter.getGroceryItemAt(position); // assuming you have a getter in your adapter

                    if (removedItem.isMealPlanAdded()) {
                        // If the item is added from the meal plan, show a dialog
                        new AlertDialog.Builder(getContext())
                                .setTitle("Meal Plan Item")
                                .setMessage("This ingredient has been added from the meal plan. Do you want to toggle it off?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    // Update the isToggled state
                                    if (!removedItem.isToggled()) {
                                        removedItem.setToggled(true);
                                    }
                                    // Notify the dataset change
                                    adapter.notifyDataSetChanged();
                                })
                                .setNegativeButton("No", (dialog, which) -> {
                                    // Restore the item
                                    adapter.notifyItemChanged(position);
                                })
                                .show();
                    } else {
                        // If the item is not added from the meal plan, just remove it
                        adapter.removeItem(position);
                        deleteIngredientFromUser(selectedDate, removedItem);
                    }
                } else {
                    Log.e("onSwiped", "Attempt to delete item at invalid position: " + position);
                    adapter.notifyDataSetChanged();
                }
            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Sets up the "Add Item" button's functionality.
     *
     * @param view The root view containing the "Add Item" button.
     */
    private void setupAddItemButton(View view) {
        TextView dailyAddItemButton = view.findViewById(R.id.addItemButton);
        dailyAddItemButton.setOnClickListener(v -> showAddItemDialog());

    }
    /**
     * Displays a dialog that allows users to add a new grocery item.
     */
    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add item");

        // Inflate a layout for capturing all the fields of a GroceryItem
        View inputView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_item, null);
        final EditText ingredientName = inputView.findViewById(R.id.ingredientName);
        final EditText ingredientAmount = inputView.findViewById(R.id.ingredientAmount);
        final EditText ingredientUnit = inputView.findViewById(R.id.ingredientUnit);

        builder.setView(inputView);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String description = ingredientName.getText().toString();
            double quantity;
            try {
                quantity = Double.parseDouble(ingredientAmount.getText().toString());
            } catch (NumberFormatException e) {
                quantity = 0;
            }
            String unit = ingredientUnit.getText().toString();
            if (!description.isEmpty() && quantity != 0 && !unit.isEmpty()) {
                dailyIngredients.add(new GroceryItem(description, quantity, unit, false, false));
                dailyIngredientAdapter.notifyItemInserted(dailyIngredients.size() - 1);
                // Using the selected date from the TabLayout:
                addIngredientToUser(dateText.getText().toString(), description, quantity, unit);

            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    /**
     * Adds a new ingredient to the current user's grocery list in the backend.
     *
     * @param formattedDate The date for which to add the ingredient.
     * @param name          Name of the ingredient.
     * @param amount        Quantity/amount of the ingredient.
     * @param unit          The unit of measurement for the ingredient.
     */
    private void addIngredientToUser(String formattedDate, String name, double amount, String unit) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            JSONObject groceryList = currentUser.getJSONObject("groceryList");
            if (groceryList == null) {
                groceryList = new JSONObject();
            }

            try {
                JSONArray dateArray;
                if (groceryList.has(formattedDate)) {
                    dateArray = groceryList.getJSONArray(formattedDate);
                } else {
                    dateArray = new JSONArray();
                }

                JSONObject ingredientObject = new JSONObject();
                ingredientObject.put("ingredientName", name);
                ingredientObject.put("quantity", amount);
                ingredientObject.put("quantityUnit", unit);
                ingredientObject.put("isToggled", false);
                ingredientObject.put("mealPlanAdded", false);
                dateArray.put(ingredientObject);

                groceryList.put(formattedDate, dateArray);

                currentUser.put("groceryList", groceryList);
                currentUser.saveInBackground(e -> {
                    if (e != null) {
                        Log.e("Error", "Error updating groceryList: " + e.getMessage());
                    } else {
                        Log.d("Success", "Ingredient added to groceryList successfully!");
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("Error", "Error processing JSON: " + e.getMessage());
            }
        }
        loadIngredientsForDate(dateText.getText().toString());
    }
    /**
     * Removes an ingredient from the current user's grocery list in the backend.
     *
     * @param formattedDate The date from which to remove the ingredient.
     * @param removedItem   The grocery item to be removed.
     */
    private void deleteIngredientFromUser(String formattedDate, GroceryItem removedItem) {
        formattedDate = dateText.getText().toString();
        Log.d("Debug", "Formatted date: " + formattedDate);

        Log.d("Debug", "deleteIngredientFromUser is called");
        ParseUser currentUser = ParseUser.getCurrentUser();
        Log.d("Debug", "Formatted date: " + formattedDate);
        if (currentUser != null) {
            JSONObject groceryList = currentUser.getJSONObject("groceryList");
            Log.d("Debug", "Grocery List: " + groceryList.toString());

            if (groceryList != null && groceryList.has(formattedDate)) {
                try {
                    // Get the ingredients array for the specified date
                    JSONArray dateArray = groceryList.getJSONArray(formattedDate);

                    JSONArray newDateArray = new JSONArray();

                    // Find the ingredient to delete
                    for (int i = 0; i < dateArray.length(); i++) {
                        JSONObject ingredientObject = dateArray.getJSONObject(i);
                        if (ingredientObject.getString("ingredientName").equals(removedItem.getIngredientName())) {
                            // Skip adding this item to the new array to effectively delete it
                            continue; // Go to the next iteration, effectively "deleting" this item
                        }
                        // Keep the item by adding it to the new array
                        newDateArray.put(ingredientObject);
                    }

                    // If the array is empty, remove the date entry, otherwise update it
                    if (newDateArray.length() == 0) {
                        groceryList.remove(formattedDate);
                    } else {
                        groceryList.put(formattedDate, newDateArray);
                    }

                    // Update the user object in Parse
                    currentUser.put("groceryList", groceryList);
                    currentUser.saveInBackground(e -> {
                        if (e != null) {
                            Log.e("Error", "Error updating groceryList: " + e.getMessage());
                        } else {
                            Log.d("Success", "Ingredient removed from groceryList successfully!");
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("Error", "Error processing JSON: " + e.getMessage());
                }
            }
        }
    }
    /**
     * Clears the entire grocery list for the current user in the backend.
     */
    private void clearGroceryList() {   
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            currentUser.remove("groceryList"); // remove the groceryList column
            currentUser.saveInBackground(e -> {
                if (e != null) {
                    // Handle the error.
                    Log.e("Error", "Error clearing groceryList: " + e.getMessage());
                } else {
                    // Successfully cleared the groceryList.
                    Log.d("Success", "groceryList cleared successfully!");
                }
            });
        }
    }
}
