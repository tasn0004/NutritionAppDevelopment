package com.nutritionapp;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.Serializable;
import java.util.List;

public class MealPlanAdapter extends RecyclerView.Adapter<MealPlanAdapter.ViewHolder> {

    private List<RecipeList> recipeList;

    public MealPlanAdapter(List<RecipeList> recipeList) {
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meal_plan_recipe_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeList recipe = recipeList.get(position);
        holder.recipeName.setText(recipe.getName());
        Glide.with(holder.itemView)
                .load(recipe.getImageUrl())
                .into(holder.recipeImage);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to start the RecipeDetail class.
                Intent intent = UtilityRecipe.getRecipeDetailIntent(v.getContext(), recipe);
                // Start the RecipeDetail class.
                v.getContext().startActivity(intent);
            }
        });

        holder.recipeImage.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.recipeOverlay), PorterDuff.Mode.SRC_ATOP);

    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public RecipeList getItemAt(int position) {
        return recipeList.get(position);
    }

    public void removeRecipeAt(int position) {
        recipeList.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView recipeName;
        ImageView recipeImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.mealRecipeName);
            recipeImage = itemView.findViewById(R.id.mealRecipeImage);
        }
    }
}
