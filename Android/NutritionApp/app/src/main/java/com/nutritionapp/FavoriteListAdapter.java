package com.nutritionapp;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FavoriteListAdapter extends RecyclerView.Adapter<FavoriteListAdapter.ViewHolder> {

    private List<RecipeList> favoriteRecipes;
    public ImageView deleteIcon;

    public FavoriteListAdapter(List<RecipeList> favoriteRecipes) {
        this.favoriteRecipes = favoriteRecipes;
    }

    @NonNull
    @Override
    public ViewHolder   onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_recipe_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeList recipe = favoriteRecipes.get(position);
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
        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recipeId = recipe.getId();
                removeRecipeFromFavorites(recipeId, holder.getAdapterPosition());
            }
        });

        holder.recipeImage.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.recipeOverlay), PorterDuff.Mode.SRC_ATOP);

    }

    @Override
    public int getItemCount() {
        return favoriteRecipes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView recipeName;
        public ImageView recipeImage;
        public ImageView deleteIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.favoriteRecipeName);
            recipeImage = itemView.findViewById(R.id.favoriteRecipeImage);
            deleteIcon = itemView.findViewById(R.id.deleteRecipeFromFavorite);

        }
    }
    public void updateFavoriteRecipes(List<RecipeList> newFavoriteRecipes) {
        this.favoriteRecipes = newFavoriteRecipes;
        this.notifyDataSetChanged();
    }
    private void removeRecipeFromFavorites(String recipeId, int position) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            List<String> favorites = currentUser.getList("favouritedRecipes");
            if (favorites != null) {
                favorites.remove(recipeId);  // Remove the recipe ID from favorites list
                currentUser.put("favorites", favorites);  // Update the user object
                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            favoriteRecipes.remove(position);  // Remove the recipe from local list
                            notifyItemRemoved(position);  // Notify the adapter about the removed item
                        } else {
                            Log.e("ParseError", "Failed to remove recipe from favorites: " + e.getMessage());
                        }
                    }
                });
            }
        }
    }
}
