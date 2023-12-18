package com.nutritionapp;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Utility class providing helper methods related to the RecipeList operations.
 * This class contains static utility methods that aid in converting a RecipeList object to an Intent
 * for detail viewing and converting a ParseObject to a RecipeList object.
 * Note: This utility class is not intended for instantiation.
 */
public class UtilityRecipe {

    private UtilityRecipe() {
    }
    /**
     * Creates and returns an intent for the RecipeDetail activity interact with recipe details.
     *
     * @param context Context from which the method is called, typically an Activity.
     * @param recipe  RecipeList object containing recipe details.
     * @return Intent to be used to start the RecipeDetail activity.
     */
    public static Intent getRecipeDetailIntent(Context context, RecipeList recipe) {
        Intent intent = new Intent(context, RecipeDetail.class);
        intent.putExtra("RECIPE_NAME", recipe.getName());
        intent.putExtra("LIKE_COUNT", recipe.getLikeCount());
        intent.putExtra("IMAGE_URL", recipe.getImageUrl());
        intent.putExtra("YOUTUBE_VIDEO_ID", recipe.getYoutubeVideoId());
        intent.putExtra("DESCRIPTION", recipe.getDescription());
        intent.putExtra("INGREDIENTS", (Serializable) recipe.getIngredients());
        intent.putExtra("PROTEIN_AMOUNT", recipe.getProteinGrams());
        intent.putExtra("CARBS_AMOUNT", recipe.getCarbohydratesMilligrams());
        intent.putExtra("SODIUM_AMOUNT", recipe.getSodiumGrams());
        intent.putExtra("RECIPE_ID", recipe.getId());
        intent.putExtra("COMMENT_COUNT", recipe.getCommentsCount());
        intent.putExtra("TIME_IN_MINUTES", recipe.getTimeInMinutes());
        Gson gson = new Gson();
        String nutritionInfoJson = gson.toJson(recipe.getNutritionInformation());
        intent.putExtra("NUTRITION_INFORMATION", nutritionInfoJson);
        intent.putExtra("BACKGROUND", recipe.getBackgroundContent());

        return intent;
    }
    /**
     * Converts a ParseObject to a RecipeList object.
     *
     * @param recipeObject ParseObject containing recipe data.
     * @return RecipeList object populated with the data from the ParseObject.
     */
    public static RecipeList convertParseObjectToRecipeList(ParseObject recipeObject) {
        String id = recipeObject.getObjectId();
        String name = recipeObject.getString("name");
        int likeCount = recipeObject.getInt("likeCount");
        int timeInMinutes = recipeObject.getInt("timeInMinutes");
        ParseFile imageFile = recipeObject.getParseFile("image");
        String imageUrl = imageFile != null ? imageFile.getUrl() : "";
        String youtubeVideoId = recipeObject.getString("videoUrl");
        String description = recipeObject.getString("description");
        int commentsCount = recipeObject.getInt("numberOfComments");
        int proteinGrams = recipeObject.getInt("proteinGrams");
        int sodiumGrams = recipeObject.getInt("sodiumMilligrams");
        int CarbohydratesMilligrams = recipeObject.getInt("carbsGrams");
        List<List<Object>> ingredients = recipeObject.getList("ingredients");
        Map<String, List<Object>> nutritionInformation = recipeObject.getMap("nutritionInformation");
        String backgroundContent = recipeObject.getString("background");

        return new RecipeList(id, name, likeCount, timeInMinutes, imageUrl, youtubeVideoId, description, commentsCount, ingredients, proteinGrams,sodiumGrams,CarbohydratesMilligrams, nutritionInformation, backgroundContent);
    }

}