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
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * this class is display data to user and Adapter for recipes_list.xml using RecyclerView
 */
public class RecipeListAdapterToView extends RecyclerView.Adapter<RecipeListAdapterToView.ViewHolder> {


    /**
     * I'm not sure for later when getting data from database we still going to use list but for now just for testing!!!!
     */
    private List<RecipeList> recipeList;
    // Constructor for the Adapter
    public RecipeListAdapterToView(List<RecipeList> recipeList) {
        this.recipeList = recipeList;
    }

    /**
     *
     * this method  Called when a new view is needed. This creates the view but doesn't set any data on it. !!!
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return it will return new view
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_list_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called to populate data into a view that was created earlier which onCreateViewHolder method!!!!!
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeList recipe = recipeList.get(position);
        holder.recipeName.setText(recipe.getName());
        Glide.with(holder.itemView)
                .load(recipe.getImageUrl())
                .into(holder.recipeImage);
        holder.recipeId = recipe.getId();
        // Check and set the favorite state for the current list item
        holder.checkIfRecipeIsFavorited();

        /**
         * so here where view each recipe details and pass data to RecipeDetail class !!!!
         */
        // Set a click listener for the item view.
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

    /**
     * Return the size of the dataset
     * @return current size
     */
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

    /**
     * this method get reference to view data to page xml by id of each components
     *
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView recipeName;
        public ImageView recipeImage;
        public ImageView heartIcon;
        public ImageView shareIcon;
        public ImageView favoriteIcon;
        public boolean isHeartFilled = false;
        public boolean isFavoriteFilled = false;
        public TextView commentsCountText;
        public TextView shareCount;
        public String recipeId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            recipeName = itemView.findViewById(R.id.recipeName);
            recipeImage =    itemView.findViewById(R.id.recipeImage);
            heartIcon = itemView.findViewById(R.id.heartIconCount);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            commentsCountText = itemView.findViewById(R.id.commentCount);


            // Listener for heart icon
            heartIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isHeartFilled) {
                        // Set to empty heart
                        heartIcon.setImageResource(R.drawable.heart_empty);
                    } else {
                        // Set to filled heart
                        heartIcon.setImageResource(R.drawable.heart_filled);
                    }
                    isHeartFilled = !isHeartFilled; // Toggle state
                }
            });

            // Listener for favorite icon
            favoriteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isFavoriteFilled = !isFavoriteFilled; // Toggle state
                    if (isFavoriteFilled) {
                        // Set to filled favorite
                        favoriteIcon.setImageResource(R.drawable.favorite_filled);
                    } else {
                        // Set to empty favorite
                        favoriteIcon.setImageResource(R.drawable.favorite_outline);
                    }
                    updateFavoriteInDatabase(isFavoriteFilled, recipeId);
                }
            });
        }
        // Adjusted checkIfRecipeIsFavorited
        private void checkIfRecipeIsFavorited() {
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser != null) {
                List<String> favorites = currentUser.getList("favorites");
                if (favorites != null && favorites.contains(recipeId)) {
                    favoriteIcon.setImageResource(R.drawable.favorite_filled);
                    isFavoriteFilled = true;
                } else {
                    favoriteIcon.setImageResource(R.drawable.favorite_outline);
                    isFavoriteFilled = false;
                }
            }
        }

        private void updateFavoriteInDatabase(boolean isAdded, String recipeId) {
            // Check if recipeId is valid
            if (recipeId == null || recipeId.isEmpty()) {
                // Handle the error: Log it or display a message to the user
                return;
            }

            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser != null) {
                List<String> favorites = currentUser.getList("favorites");
                if (favorites == null) {
                    favorites = new ArrayList<>();
                }
                if (isAdded) {
                    if (!favorites.contains(recipeId)) {
                        favorites.add(recipeId);
                    }
                } else {
                    favorites.remove(recipeId);
                }
                currentUser.put("favorites", favorites);
                currentUser.saveInBackground(e -> {
                    if (e != null) {
                        // Handle the error.
                    } else {
                        // Success: favorite list updated in database.
                    }
                });
            }
        }

    }


}