package com.nutritionapp;

public class GroceryItem {
    private String ingredientName;
    private double ingredientAmount;
    private String ingredientUnit;
    private boolean toggled;
    private boolean mealPlanAdded;
    public GroceryItem(String ingredientName, double ingredientAmount, String ingredientUnit, boolean toggled, boolean mealPlanAdded) {
        this.ingredientName = ingredientName;
        this.ingredientAmount = ingredientAmount;
        this.ingredientUnit = ingredientUnit;
        this.toggled = toggled;
        this.mealPlanAdded = mealPlanAdded;

    }
    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }
    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }


    public double getIngredientAmount() {
        return ingredientAmount;
    }

    public void setIngredientAmount(double ingredientAmount) {
        this.ingredientAmount = ingredientAmount;
    }


    public String getIngredientUnit() {
        return ingredientUnit;
    }

    public void setIngredientUnit(String ingredientUnit) {
        this.ingredientUnit = ingredientUnit;
    }
    public boolean isMealPlanAdded() {
        return mealPlanAdded;
    }

    public void setMealPlanAdded(boolean mealPlanAdded) {
        this.mealPlanAdded = mealPlanAdded;
    }
}
