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
import java.util.List;

public class ExploreRecipeAdapter extends RecyclerView.Adapter<ExploreRecipeAdapter.ViewHolder>{

    private List<RecipeList> searchedRecipes;

    public ExploreRecipeAdapter(List<RecipeList> favoriteRecipes) {
        this.searchedRecipes = favoriteRecipes;
    }

    @NonNull
    @Override
    public ExploreRecipeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_page_results, parent, false);
        return new ExploreRecipeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExploreRecipeAdapter.ViewHolder holder, int position) {
        RecipeList recipe = searchedRecipes.get(position);
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
        return searchedRecipes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView recipeName;
        public ImageView recipeImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.recipeName);
            recipeImage = itemView.findViewById(R.id.recipeImage);

        }
    }

}
