package com.nutritionapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FavoriteList extends Fragment {
    private RecyclerView favoriteRecipesRecyclerView;
    private FavoriteListAdapter adapter;  // Change the type to FavoriteListAdapter
    private ProgressBar loadingIndicator;
    private TextView emptyFavoriteListMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sett_favorite, container, false);
        favoriteRecipesRecyclerView = view.findViewById(R.id.favoriteRecipesRecyclerView);
        favoriteRecipesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));  // Assuming 2 columns for grid view.
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        emptyFavoriteListMessage = view.findViewById(R.id.emptyFavoriteListMessage);


        displayFavoriteRecipes();
        onResume();
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        displayFavoriteRecipes();
    }

    private void fetchFavoriteRecipes(FindCallback<ParseObject> callback) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            List<String> favorites = currentUser.getList("favouritedRecipes");

            if (favorites != null && !favorites.isEmpty()) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Recipes");
                query.whereContainedIn("objectId", favorites);
                query.findInBackground(callback);
                emptyFavoriteListMessage.setVisibility(View.GONE);

            }else{
                emptyFavoriteListMessage.setVisibility(View.VISIBLE);
                loadingIndicator.setVisibility(View.GONE);

            }
        }
    }
    private void displayFavoriteRecipes() {
       loadingIndicator.setVisibility(View.VISIBLE);
        fetchFavoriteRecipes(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> recipeList, ParseException e) {

                if (e == null) {
                    if (recipeList.isEmpty()) {
                        // List is empty, show message
                        favoriteRecipesRecyclerView.setVisibility(View.GONE);
                    } else {
                        loadingIndicator.setVisibility(View.GONE);
                        // List has items, show RecyclerView and hide message
                        favoriteRecipesRecyclerView.setVisibility(View.VISIBLE);

                        List<RecipeList> favoriteRecipes = new ArrayList<>();
                        for (ParseObject recipeObject : recipeList) {
                            RecipeList recipe = UtilityRecipe.convertParseObjectToRecipeList(recipeObject);
                            favoriteRecipes.add(recipe);
                        }
                        adapter = new FavoriteListAdapter(favoriteRecipes);
                        favoriteRecipesRecyclerView.setAdapter(adapter);
                    }
                } else {

                    Log.e("ParseError", "Error fetching favorite recipes: " + e.getMessage());
                }
            }
        });
    }


}
