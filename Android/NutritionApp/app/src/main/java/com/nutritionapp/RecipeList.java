package com.nutritionapp;

import java.util.List;
import java.util.Map;

public class RecipeList {
    // this all where see on recipe list where small picture/ time takes/ name of recipe / like and favorite button
    private String name;
    private int likeCount;
    private int timeInMinutes;
    private String  imageUrl;
    private String youtubeVideoId;
    private String description;
    private List<String>  nutrition;
    private int sodiumGrams  ;
    private int proteinGrams;
    private int CarbohydratesMilligrams;
    private int commentsCount;
    private List<List<Object>> ingredients;
    private String id;
    private Map<String, List<Object>> nutritionInformation;
    private String backgroundContent;


    // Constructor for Recipe list
    public RecipeList(String id, String name, int likeCount, int timeInMinutes, String imageUrl, String youtubeVideoId, String description,int commentsCount, List<List<Object>> ingredients, int proteinGrams , int CarbohydratesMilligrams, int sodiumGrams, Map<String, List<Object>> nutritionInformation, String backgroundContent ) {
        this.name = name;
        this.likeCount = likeCount;
        this.timeInMinutes = timeInMinutes;
        this.imageUrl = imageUrl;
        this.youtubeVideoId = youtubeVideoId;
        this.description = description;
        this.commentsCount = commentsCount;
        this.ingredients = ingredients;
        this.proteinGrams = proteinGrams;
        this.CarbohydratesMilligrams = CarbohydratesMilligrams;
        this.sodiumGrams = sodiumGrams;
        this.id = id;
        this.nutritionInformation = nutritionInformation;
        this.backgroundContent = backgroundContent;

    }
    //getters
    public String  getId() {
        return id;
    }
    public String  getImageUrl() {
        return imageUrl;
    }
    public String getBackgroundContent() {
        return backgroundContent;
    }

    public String getName() {
        return name;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getTimeInMinutes() {
        return timeInMinutes;
    }
    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }
    public String getDescription() { return description; }
    public int getCommentsCount() {
        return commentsCount;
    }
    public List<List<Object>>getIngredients() {
        return ingredients;
    }
    public int getProteinGrams() {
        return proteinGrams;
    }
    public int getSodiumGrams() {
        return sodiumGrams;
    }
    public int getCarbohydratesMilligrams() {
        return CarbohydratesMilligrams;
    }
    public Map<String, List<Object>> getNutritionInformation() {
        return nutritionInformation;
    }
}