package com.nutritionapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class RecipeDetail extends AppCompatActivity {
    String selectedDate ;
    String selectedMealType ;
    private TextView proteinValue, carbsValue, sodiumValue;
    private TableLayout nutritionTable;
    // UI elements
    private TextView recipeName,backgroundContent, timeDetail, descriptionContent, heartCount, commentsCount, ingredientsContent, minText;
    private ImageView recipeImage,chatIcon, blackOverlay, blackOverlay1, descriptionExpandIcon, ingredientsExpandIcon, nutritionExpandIcon, exitButton, optionsIcon, heartIcon, favoriteIcon, shareIcon, blurOverlay,blurOverlay1,blurOverlay2;
    private WebView youtubeWebView;
    private String recipeID;
    List<String> likedRecipes ;
    int topOffset = 0;

    // Variables to keep track of UI states
    private boolean isDescriptionIconRotated = false;
    private boolean isIngredientsIconRotated = false;
    private boolean isNutritionIconRotated = false;
    private boolean isHeartFilled = false;
    private boolean isFavoriteFilled = false;
    private TableLayout ingredientsTable;
    private LinearLayout backgroundLinearLayout;
    boolean isPaidAccount ;
    TextView overlayTextIngredients, overlayTextNutrition, overlayTextDescription;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_details);
        SharedPreferences sharedPreferences = getSharedPreferences("RecipeData", Context.MODE_PRIVATE);
        selectedDate = sharedPreferences.getString("selectedDate", null);
        selectedMealType = sharedPreferences.getString("selectedMealType", null);

        // Hide the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // Initialize UI elements and views
        initializeUI();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isPaidAccount = preferences.getBoolean("isPaidAccount", false);
        // Get the intent and extract data from it
        Intent intent = getIntent();
        extractIntentData(intent);
        fetchTotalComments();
        fetchTimesLiked();
        // Handle events
        setupEventListeners();
        checkIfRecipeIsFavorited();
        //clearMealPlan();
        checkIfRecipeIsLiked();
        if (!isPaidAccount) {
            applyBlurEffect();
        }
        if (!isPaidAccount) {
            applyBlurEffectIngredientsTable();

        }
    }

    private void initializeUI() {
        // Image and text views
        recipeName = findViewById(R.id.recipeNameDetail);
        backgroundContent = findViewById(R.id.backgroundContent);

        recipeImage = findViewById(R.id.recipeImageDetail);
        timeDetail = findViewById(R.id.timeDetail);
        backgroundLinearLayout = findViewById(R.id.backgroundLinearLayout);
        minText= findViewById(R.id.minText);
        descriptionContent = findViewById(R.id.descriptionContent);
        heartCount = findViewById(R.id.heartCountText);
        commentsCount = findViewById(R.id.commentCount);
        chatIcon = findViewById(R.id.chatIcon);

        //ingredientsContent = findViewById(R.id.ingredientsContent);
        heartIcon = findViewById(R.id.heartIconCount);
        favoriteIcon = findViewById(R.id.favoriteIcon);


        // Table layout initialization.
        nutritionTable = findViewById(R.id.nutritionTable);
        // WebView setup for YouTube video
        youtubeWebView = findViewById(R.id.youtubeWebView);
        WebSettings webSettings = youtubeWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        optionsIcon = findViewById(R.id.optionsIcon);

        optionsIcon.setOnClickListener(view -> {
            showPopupMenu(view);
        });
        // Icons and overlays
        exitButton = findViewById(R.id.exitButton);
        optionsIcon = findViewById(R.id.optionsIcon);
        blackOverlay = findViewById(R.id.blackOverlay);
        blackOverlay1 = findViewById(R.id.blackOverlay1);
        descriptionExpandIcon = findViewById(R.id.descriptionExpandIcon);
        ingredientsExpandIcon = findViewById(R.id.ingredientsExpandIcon);
        nutritionExpandIcon = findViewById(R.id.nutritionExpandIcon);
         blurOverlay = findViewById(R.id.blurOverlayImageView);
        blurOverlay1 = findViewById(R.id.blurOverlayImageView1);
        blurOverlay2 = findViewById(R.id.blurOverlayImageView2);
        overlayTextIngredients = findViewById(R.id.overlayTextIngredients);
        overlayTextDescription = findViewById(R.id.overlayTextDescription);
        overlayTextNutrition = findViewById(R.id.overlayTextNutrition);


    }

    private void extractIntentData(Intent intent) {
        String name = intent.getStringExtra("RECIPE_NAME");
        recipeName.setText(name);
        String backgroundText = intent.getStringExtra("BACKGROUND");
        backgroundContent.setText(backgroundText);
        String imageUrl = intent.getStringExtra("IMAGE_URL");
        Glide.with(this).load(imageUrl).into(recipeImage);

        String description = intent.getStringExtra("DESCRIPTION");
        descriptionContent.setText(description);
        recipeID = intent.getStringExtra("RECIPE_ID");

        ingredientsTable = findViewById(R.id.ingredientsTable);
        List<List<Object>> ingredients = (List<List<Object>>) intent.getSerializableExtra("INGREDIENTS");
        int ingredientsSize = ingredients.size();  // Get the size of the ingredients list

        for (int i = 0; i < ingredientsSize; i++) {
            List<Object> ingredientDetails = ingredients.get(i);

            TableRow row = new TableRow(this);
            TextView ingredientName = new TextView(this);
            TextView ingredientAmountAndColor = new TextView(this);

            ingredientName.setText(ingredientDetails.get(0).toString());
            ingredientName.setTextColor(getResources().getColor(R.color.TextColorWhiteVsBlack));
            ingredientAmountAndColor.setText(ingredientDetails.get(1) + " " + ingredientDetails.get(2));
            ingredientAmountAndColor.setTextColor(getResources().getColor(R.color.TextColorWhiteVsBlack));

            ingredientName.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            ingredientAmountAndColor.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            ingredientAmountAndColor.setGravity(Gravity.END);
            int marginInDp = 5; // 5dp
            final float scale = getResources().getDisplayMetrics().density;
            int marginInPx = (int) (marginInDp * scale + 0.5f);

            int padding_in_dp = 12; // existing padding value
            int padding_in_px = (int) (padding_in_dp * scale + 0.5f);

            row.setPadding(0, padding_in_px, 0, padding_in_px);
            ingredientName.setPadding(padding_in_px, 0, 0, 0);
            ingredientAmountAndColor.setPadding(0, 0, padding_in_px, 0);

            row.addView(ingredientName);
            row.addView(ingredientAmountAndColor);

            ingredientsTable.addView(row);

            // Only add separator if it's not the last iteration
            if (i < ingredientsSize - 1) {
                View separator = new View(this);
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 3);
                layoutParams.leftMargin = marginInPx;
                layoutParams.rightMargin = marginInPx;
                separator.setLayoutParams(layoutParams);
                separator.setBackgroundColor(Color.parseColor("#EEEEEE"));
                ingredientsTable.addView(separator);
            }

        }


        heartCount.setText(String.valueOf(intent.getIntExtra("LIKE_COUNT", 0)));
        timeDetail.setText(String.valueOf(intent.getIntExtra("TIME_IN_MINUTES", 0)));






        TableLayout nutritionTable = findViewById(R.id.nutritionTable);
        Gson gson = new Gson();
        String nutritionInfoJson = getIntent().getStringExtra("NUTRITION_INFORMATION");
        Type type = new TypeToken<Map<String, List<Object>>>(){}.getType();
        Map<String, List<Object>> nutritionInformation = gson.fromJson(nutritionInfoJson, type);

// Define the order in which you want the nutrients to be displayed
        List<String> orderedKeys = Arrays.asList(
                "calories",
                "fat",
                "saturatedFat",
                "transFat",
                "cholesterol",
                "sodium",
                "potassium",
                "carbohydrates",
                "fibre",
                "sugar",
                "protein",
                "vitaminA",
                "vitaminC",
                "vitaminD",
                "vitaminB12",
                "calcium",
                "iron",
                "magnesium",
                "zinc",
                "folate"
        );

// Convert the map's entry set into a list and sort it based on the predefined order
        List<Map.Entry<String, List<Object>>> sortedEntries = new ArrayList<>(nutritionInformation.entrySet());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sortedEntries.sort(Comparator.comparingInt(e -> orderedKeys.indexOf(e.getKey())));
        }

// Now iterate over the sorted entry list and add rows to the table
        for (Map.Entry<String, List<Object>> entry : sortedEntries) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

            TextView nutrientName = new TextView(this);
            nutrientName.setText(capitalize(entry.getKey()));
            nutrientName.setTextColor(getResources().getColor(R.color.TextColorWhiteVsBlack));
            nutrientName.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

            TextView nutrientValue = new TextView(this);
            nutrientValue.setText(String.format(Locale.getDefault(), "%s%s", entry.getValue().get(0), entry.getValue().get(1)));
            nutrientValue.setTextColor(getResources().getColor(R.color.TextColorWhiteVsBlack));
            nutrientValue.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            nutrientValue.setGravity(Gravity.END);

            row.addView(nutrientName);
            row.addView(nutrientValue);
            nutritionTable.addView(row);

            // Separator
            View separator = new View(this);
            separator.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
            separator.setBackgroundColor(Color.parseColor("#DDDDDD"));
            nutritionTable.addView(separator);
            View spacer = new View(this);
            int spacerHeightInDp = 5; // Height of the spacer in dp
            final float scale = getResources().getDisplayMetrics().density;
            int spacerHeightInPx = (int) (spacerHeightInDp * scale + 0.5f);
            spacer.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, spacerHeightInPx));
            nutritionTable.addView(spacer);
        }

    }



    private Bitmap captureBitmapFromView(View view, int topOffset) {
        if (view.getWidth() == 0 || view.getHeight() == 0) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void applyBlurEffect() {
        applyBlurToView(descriptionContent, blurOverlay1);
        applyBlurToView(nutritionTable, blurOverlay2);

    }
    private void applyBlurEffectIngredientsTable() {
        applyBlurToViewForIngredientsTable(ingredientsTable, blurOverlay);

    }
    private void applyBlurToViewForIngredientsTable(View viewToBlur, ImageView overlayImageView) {


        viewToBlur.post(() -> {
            if (viewToBlur.getWidth() == 0 || viewToBlur.getHeight() == 0) {
                viewToBlur.post(() -> applyBlurToViewForIngredientsTable(viewToBlur, overlayImageView));
                return;
            }
            for (int i = 0; i < 8 && i < ingredientsTable.getChildCount(); i++) {
                View row = ingredientsTable.getChildAt(i);
                topOffset += row.getHeight();
            }

            Bitmap bitmap = captureBitmapFromView(viewToBlur);
            if (bitmap != null) {
                Bitmap blurredBitmap = BlurBuilder.blur(this, bitmap);
                overlayImageView.setImageBitmap(blurredBitmap);

                // Set the Y position and height of the overlay
                overlayImageView.setY(viewToBlur.getTop()+ topOffset);
                overlayImageView.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, viewToBlur.getHeight()));

                overlayImageView.setVisibility(View.VISIBLE);

                // Add text overlay on the blurred image
                addTextOverlay(overlayImageView, "Subscribe to see more", 8, overlayTextIngredients );
            }
        });
    }


    private void applyBlurToView(View viewToBlur, ImageView overlayImageView) {
        viewToBlur.post(() -> {
            if (viewToBlur.getWidth() == 0 || viewToBlur.getHeight() == 0) {
                viewToBlur.post(() -> applyBlurToView(viewToBlur, overlayImageView));
                return;
            }

            Bitmap bitmap = captureBitmapFromView(viewToBlur);
            if (bitmap != null) {
                Bitmap blurredBitmap = BlurBuilder.blur(this, bitmap);
                overlayImageView.setImageBitmap(blurredBitmap);
                overlayImageView.setY(viewToBlur.getTop());
                overlayImageView.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, viewToBlur.getHeight()));
                overlayImageView.setVisibility(View.VISIBLE);

                // Add text overlay on the blurred image
                addTextOverlay(overlayImageView, "Subscribe to see more", 2, overlayTextNutrition );
                addTextOverlay(overlayImageView, "Subscribe to see more", 2, overlayTextDescription );

            }
        });
    }


    private Bitmap captureBitmapFromView(View view) {
        if (view.getWidth() == 0 || view.getHeight() == 0) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }


    private void addTextOverlay(ImageView overlayImageView, String text, int height, TextView textView) {
        // Find the TextView from the layout

        // Set the text and other properties
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textView.setTextColor(Color.WHITE);

        // Make the TextView visible
        textView.setVisibility(View.VISIBLE);

        // Position the TextView
        textView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Center the TextView horizontally and vertically over the overlay
                textView.setX(overlayImageView.getX() + overlayImageView.getWidth() / 2 - textView.getWidth() / 2);
                textView.setY(overlayImageView.getY() + overlayImageView.getHeight() / height - textView.getHeight() / height);

                // Remove the listener to prevent this code from being called multiple times
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    textView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    textView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }



    private String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private void fetchTimesLiked() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Recipes");
        query.getInBackground(recipeID, (recipe, e) -> {
            if (e == null) {
                int timesLiked = recipe.getInt("timesLiked");
                heartCount.setText(String.valueOf(timesLiked));
            } else {
                Log.e("Recipe Fetch Error", "Error fetching timesLiked: " + e.getMessage());
            }
        });
    }


    private void setupEventListeners() {
        setupYoutubeWebView();
        // Toggle rotation and visibility for Description, Ingredients, and Nutrition
        descriptionExpandIcon.setOnClickListener(this::toggleRotation);
        findViewById(R.id.descriptionLabel).setOnClickListener(this::toggleRotation);

        ingredientsExpandIcon.setOnClickListener(this::toggleRotationIngredients);
        findViewById(R.id.ingredientsLabel).setOnClickListener(this::toggleRotationIngredients);

        nutritionExpandIcon.setOnClickListener(this::toggleRotationNutrition);
        findViewById(R.id.nutritionLabel).setOnClickListener(this::toggleRotationNutrition);

        // Toggle Heart icon state
        heartIcon.setOnClickListener(v -> {
            updateLikedRecipes();
        });


        chatIcon.setOnClickListener(v -> {
            CommentsPage commentsPage = new CommentsPage();
            Bundle bundle = new Bundle();
            bundle.putString("RECIPE_ID", recipeID);
            commentsPage.setArguments(bundle);
            commentsPage.show(getSupportFragmentManager(), "CommentsPage");
        });



        // Toggle Favorite icon state
        favoriteIcon.setOnClickListener(v -> {
            isFavoriteFilled = toggleIconState(isFavoriteFilled, favoriteIcon, R.drawable.favorite_outline, R.drawable.favorite_filled);
            updateFavoriteInDatabase(isFavoriteFilled);
        });

        blackOverlay.setOnClickListener(v -> {
        });
        blackOverlay1.setOnClickListener(v -> {
        });
    }
    private void updateLikedRecipes() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        likedRecipes = currentUser.getList("likedRecipes");

        if (likedRecipes == null) {
            likedRecipes = new ArrayList<>();
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Recipes");
        query.getInBackground(recipeID, (recipe, e) -> {
            if (e == null) {
                int currentLikes = recipe.getInt("timesLiked");
                if (likedRecipes.contains(recipeID)) {
                    // Recipe is already liked, so unlike it
                    likedRecipes.remove(recipeID);
                    heartIcon.setImageResource(R.drawable.heart_empty);
                    isHeartFilled = false;
                    currentLikes--; // Decrease timesLiked by 1
                    recipe.put("timesLiked", currentLikes);
                } else {
                    // Recipe is not liked, so like it
                    likedRecipes.add(recipeID);
                    heartIcon.setImageResource(R.drawable.heart_filled);
                    isHeartFilled = true;
                    currentLikes++; // Increase timesLiked by 1
                    recipe.put("timesLiked", currentLikes);
                }

                // Update the TextView immediately
                heartCount.setText(String.valueOf(currentLikes));

                // Save the updated likedRecipes list to the user
                currentUser.put("likedRecipes", likedRecipes);
                currentUser.saveInBackground();

                // Save the updated recipe
                recipe.saveInBackground();
            } else {
                // Handle error
                Log.e("Recipe Update Error", "Error fetching recipe: " + e.getMessage());
            }
        });
    }


    private void checkIfRecipeIsLiked() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        List<String> likedRecipes = currentUser.getList("likedRecipes");

        if (likedRecipes != null && likedRecipes.contains(recipeID)) {
            // Recipe is liked
            heartIcon.setImageResource(R.drawable.heart_filled);
            isHeartFilled = true;
        } else {
            // Recipe is not liked
            heartIcon.setImageResource(R.drawable.heart_empty);
            isHeartFilled = false;
        }
    }

    private void setupYoutubeWebView() {
        // This setup is for handling YouTube video playback in WebView
        youtubeWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        ImageView playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(view -> {
            recipeImage.setVisibility(View.GONE);
            playButton.setVisibility(View.GONE);
            backgroundLinearLayout.setVisibility(View.GONE);
            optionsIcon.setVisibility(View.GONE);
            blackOverlay.setVisibility(View.VISIBLE);
            blackOverlay1.setVisibility(View.VISIBLE);

            youtubeWebView.setVisibility(View.VISIBLE);
            exitButton.setVisibility(View.VISIBLE);

            Intent intent = getIntent();
            String youtubeVideoId = intent.getStringExtra("YOUTUBE_VIDEO_ID");
            String embedVideo = "<html><body style=\"margin: 0; padding: 0\"><iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/" + youtubeVideoId + "?autoplay=1&controls=0\" frameborder=\"0\" allowfullscreen autoplay></iframe></body></html>";
            youtubeWebView.loadData(embedVideo, "text/html", "utf-8");
        });

        exitButton.setOnClickListener(view -> {
            youtubeWebView.loadUrl("about:blank");
            recipeImage.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.VISIBLE);
            backgroundLinearLayout.setVisibility(View.VISIBLE);
            optionsIcon.setVisibility(View.VISIBLE);
            blackOverlay.setVisibility(View.GONE);
            blackOverlay1.setVisibility(View.GONE);

            youtubeWebView.setVisibility(View.GONE);
            exitButton.setVisibility(View.GONE);
        });
    }

    private void toggleRotation(View view) {
        isDescriptionIconRotated = toggleRotationState(isDescriptionIconRotated, descriptionExpandIcon, descriptionContent, blurOverlay1, overlayTextDescription);
    }

    private void toggleRotationIngredients(View view) {
        isIngredientsIconRotated = toggleRotationState(isIngredientsIconRotated, ingredientsExpandIcon, ingredientsTable,blurOverlay, overlayTextIngredients);
    }


    private void toggleRotationNutrition(View view) {
        isNutritionIconRotated = toggleRotationState(isNutritionIconRotated, nutritionExpandIcon, nutritionTable,blurOverlay2, overlayTextNutrition);
    }

    private boolean toggleRotationState(boolean isRotated, ImageView icon, View content, ImageView blurOverlay, TextView textView) {
        if (textView == null) {
            // TextView has not been initialized yet
            return isRotated; // Return the current state without changing anything
        }

        if (isRotated) {
            icon.setRotation(0);
            content.setVisibility(View.GONE);
            blurOverlay.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);

            return false;
        } else {
            icon.setRotation(90);
            content.setVisibility(View.VISIBLE);
            blurOverlay.setVisibility(View.VISIBLE);
            if (!isPaidAccount) {
                textView.setVisibility(View.VISIBLE);
            }
            return true;
        }
    }



    private boolean toggleIconState(boolean isFilled, ImageView icon, int emptyResId, int filledResId) {
        if (isFilled) {
            icon.setImageResource(emptyResId);
            return false;
        } else {
            icon.setImageResource(filledResId);
            return true;
        }
    }
    private void updateFavoriteInDatabase(boolean isAdded) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            List<String> favorites = currentUser.getList("favouritedRecipes");
            if (favorites == null) {
                favorites = new ArrayList<>();
            }
            if (isAdded) {
                if (!favorites.contains(recipeID)) {
                    favorites.add(recipeID);
                }
            } else {
                favorites.remove(recipeID);
            }
            currentUser.put("favouritedRecipes", favorites);
            currentUser.saveInBackground(e -> {
                if (e != null) {
                    // Handle the error.
                } else {
                    // Success: favorite list updated in database.
                }
            });
        }
    }
    private void checkIfRecipeIsFavorited() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            List<String> favorites = currentUser.getList("favorites");
            if (favorites != null && favorites.contains(recipeID)) {
                favoriteIcon.setImageResource(R.drawable.favorite_filled);
                isFavoriteFilled = true;
            } else {
                favoriteIcon.setImageResource(R.drawable.favorite_outline);
                isFavoriteFilled = false;
            }
        }
    }

    /**
     * save the recipe to the specified date and specified meal type to UserTable
     * @param formattedDate the date to be selected for meal plan to add it
     * @param type the type of meal if its (breakfast, breakfast, lunch, dinner, desserts)
     */
    private void saveToDatabase(String formattedDate, String type) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            JSONObject mealPlan = currentUser.getJSONObject("mealPlan");
            if (mealPlan == null) {
                mealPlan = new JSONObject();
            }

            try {
                // If the selected date doesn't exist in mealPlan, create a new entry.
                if (!mealPlan.has(formattedDate)) {
                    JSONObject newDay = new JSONObject();
                    newDay.put("breakfast", new JSONArray());
                    newDay.put("lunch", new JSONArray());
                    newDay.put("dinner", new JSONArray());
                    newDay.put("desserts", new JSONArray());
                    newDay.put("snacks", new JSONArray());
                    mealPlan.put(formattedDate, newDay);
                }

                JSONObject selectedDateMeal = mealPlan.getJSONObject(formattedDate);
                JSONArray selectedMealArray = selectedDateMeal.getJSONArray(type);

                // Add the recipe to the meal plan
                selectedMealArray.put(recipeID);
                currentUser.put("mealPlan", mealPlan);
                currentUser.saveInBackground(e -> {
                    if (e != null) {
                        // Handle the error
                        Log.e("Error", "Error updating mealPlan: " + e.getMessage());
                    } else {
                        // Successfully updated the meal plan
                        Log.d("Success", "Recipe added to mealPlan successfully!");
                        saveIngredientsToGroceryList(formattedDate);

                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                // Handle the error
                Log.e("Error", "Error processing JSON: " + e.getMessage());
            }
        }
    }




    /**
     * show dialog for the user to add the recipe to their meal plan
     */
    private void showMealPlanDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_to_meal_plan, null);

        builder.setView(dialogView);

        // Initialize the date picker and spinner (dropdown) from the custom layout.
        DatePicker datePicker = dialogView.findViewById(R.id.date_picker);
        Spinner typeSpinner = dialogView.findViewById(R.id.type_spinner);

        // Create an adapter for the spinner using predefined meal types.
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.meal_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter to the spinner.
        typeSpinner.setAdapter(typeAdapter);

        // Set an action for the "Add" button of the dialog.
        builder.setPositiveButton("Add", (dialog, which) -> {
            // Format the selected date from the date picker.
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            String selectedDate = sdf.format(new Date(datePicker.getYear() - 1900, datePicker.getMonth(), datePicker.getDayOfMonth()));

            // Get the selected meal type from the spinner.
            String selectedType = typeSpinner.getSelectedItem().toString();

            // Save the recipe to the database with the selected date and meal type.
            saveToDatabase(selectedDate, selectedType);
            Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show();

        });

        // Set an action for the "Cancel" button to dismiss the dialog.
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Create and show the dialog.
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    /**
     * show a pop up menu with option to add current recipe to meal plan and other options
     *
     * @param view clicked to trigger the popup
     */
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.recipe_option_list, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.add_to_meal_plan) {
                if (selectedDate != null && selectedMealType != null) {
                    saveToDatabase(selectedDate, selectedMealType);
                    resetSelectedDateAndMealType();
                    finish();
                    Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, WeeklyHub.class);
                    startActivity(intent);
                    finish();
                } else {
                    showMealPlanDialog();
                }
                return true;
            }
            if (menuItem.getItemId() == R.id.share_recipe) {
                Intent intent = getIntent();
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                String youtubeVideoId = intent.getStringExtra("YOUTUBE_VIDEO_ID");
                shareIntent.setType("text/plain");
                String shareBody = "Check out this Recipe from APPNAME: https://www.youtube.com/watch?v=" + youtubeVideoId;
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Recipe to Share: ");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                startActivity(Intent.createChooser(shareIntent, "Share Using: "));
            }
            return false;
        });

        popupMenu.show();
    }

    /**
     * this method is to clean meal plan list but not only use if something go wrong or wanna rest the mealPlan column
     * so no usages !!
     */
    private void clearMealPlan() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // Set mealPlan column to null
            currentUser.remove("mealPlan");
            currentUser.put("mealPlan", new JSONObject());

            currentUser.saveInBackground(e -> {
                if (e != null) {
                    // Handle the error.
                    Log.e("Error", "Error updating mealPlan: " + e.getMessage());
                } else {
                    // Successfully cleared the mealPlan.
                    Log.d("Success", "mealPlan cleared successfully!");
                }
            });
        }
    }

    /**
     * after
     */
    private void resetSelectedDateAndMealType() {
        SharedPreferences sharedPreferences = getSharedPreferences("RecipeData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("selectedDate");
        editor.remove("selectedMealType");
        editor.apply();
    }
    private void saveIngredientsToGroceryList(String formattedDate) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Recipes");
        query.whereEqualTo("objectId", recipeID);
        query.getFirstInBackground((recipe, e) -> {
            if (e == null && recipe != null) {
                List<List<Object>> ingredientsList = recipe.getList("ingredients");
                addIngredientsToUser(formattedDate, ingredientsList);
            } else {
                Log.e("Error", "Failed to fetch recipe ingredients: " + e.getMessage());
            }
        });
    }
    private void addIngredientsToUser(String formattedDate, List<List<Object>> ingredientsList) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            JSONObject groceryList = currentUser.getJSONObject("groceryList");
            if (groceryList == null) {
                groceryList = new JSONObject();
            }

            try {
                JSONArray dateArray;  // This will store all the ingredients for the given date
                if (groceryList.has(formattedDate)) {
                    dateArray = groceryList.getJSONArray(formattedDate);
                } else {
                    dateArray = new JSONArray();
                }

                for (List<Object> ingredient : ingredientsList) {
                    String name = ingredient.get(0).toString();
                    double quantity = Double.parseDouble(ingredient.get(1).toString());
                    String unit = ingredient.get(2).toString();

                    boolean found = false;
                    for (int i = 0; i < dateArray.length(); i++) {
                        JSONObject existingIngredient = dateArray.getJSONObject(i);
                        if (existingIngredient.getString("ingredientName").equals(name) &&
                                existingIngredient.getString("quantityUnit").equals(unit)) {
                            double existingQuantity = existingIngredient.getDouble("quantity");
                            existingIngredient.put("quantity", existingQuantity + quantity);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        JSONObject ingredientObject = new JSONObject();
                        ingredientObject.put("ingredientName", name);
                        ingredientObject.put("quantity", quantity);
                        ingredientObject.put("quantityUnit", unit);
                        ingredientObject.put("isToggled", false);
                        ingredientObject.put("mealPlanAdded", true);
                        dateArray.put(ingredientObject);
                    }
                }

                groceryList.put(formattedDate, dateArray);

                currentUser.put("groceryList", groceryList);
                currentUser.saveInBackground(e -> {
                    if (e != null) {
                        // Handle the error.
                        Log.e("Error", "Error updating groceryList: " + e.getMessage());
                    } else {
                        // Successfully updated the groceryList.
                        Log.d("Success", "Ingredients added to groceryList successfully!");
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("Error", "Error processing JSON: " + e.getMessage());
            }
        }
    }

    private void fetchTotalComments() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Recipes");

        // Get the specific recipe by ID
        query.getInBackground(recipeID, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    // Make sure the "numberOfComments" column exists and is not null
                    if (object.getNumber("numberOfComments") != null) {
                        int numberOfComments = object.getNumber("numberOfComments").intValue();
                        commentsCount.setText(String.valueOf(numberOfComments));
                    } else {
                        // If "numberOfComments" is null, default to 0
                        commentsCount.setText("0");
                    }
                } else {
                    Log.e("Parse Error", "Error: " + e.getMessage());
                }
            }
        });
    }




}