package com.nutritionapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.Nullable;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class SearchPageView extends AppCompatActivity {
    String query;
    TextView searchString;
    Button cancelSearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_pageview);

        searchString = findViewById(R.id.searchString);
        cancelSearchButton = findViewById(R.id.cancelSearchButton);

        Intent intent = getIntent();
        query = intent.getStringExtra("query");
        searchString.setText(query);

        querySearch();

        cancelSearchButton.setOnClickListener(v -> {
            finish();
        });

    }

    public void querySearch() {
        // Query to search by name
        ParseQuery<ParseObject> nameQuery = ParseQuery.getQuery("Recipes");
        nameQuery.whereContains("name", query);

        nameQuery.findInBackground((nameRecipes, nameError) -> {
            if (nameError == null && nameRecipes.size() > 0) {
                // Handle found recipes by name
                System.out.println("Found Something!");
                Toast.makeText(SearchPageView.this, "Found Something.", Toast.LENGTH_SHORT).show();
            } else {
                // No recipes found by name, so execute the ingredients query
                queryIngredients();
            }
        });
    }

    public void queryIngredients() {
        // Query to search by ingredients
        ParseQuery<ParseObject> ingredientsQuery = ParseQuery.getQuery("Recipes");

        ingredientsQuery.whereEqualTo("ingredients.0.0", query);

        ingredientsQuery.findInBackground((ingredientsRecipes, ingredientsError) -> {
            if (ingredientsError == null && ingredientsRecipes.size() > 0) {
                // Handle found recipes by ingredients
                System.out.println("Found Something based on ingredients!");
                Toast.makeText(SearchPageView.this, "Found Something.", Toast.LENGTH_SHORT).show();
            } else {
                // No recipes found by ingredients
                Toast.makeText(SearchPageView.this, "No recipes found.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
