package com.nutritionapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class RecipeModel extends AppCompatActivity {

    private ImageView categoryDetailImage;
    private TextView categoryDetailName;
    private RecyclerView recipeRecyclerView;
    private RecipeListAdapterToView adapter;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_detail);

        categoryDetailImage = findViewById(R.id.categoryDetailImage);
        categoryDetailName = findViewById(R.id.categoryDetailName);
        recipeRecyclerView = findViewById(R.id.recipeRecyclerView);

        BottomNavigationView bottomNavigationView;

        Category category = (Category) getIntent().getSerializableExtra("CATEGORY");
        if (category != null) {
            categoryDetailName.setText(category.getName());
            Glide.with(this).load(category.getImageUrl()).into(categoryDetailImage);

            // Setting layout manager for the RecyclerView
            recipeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            fetchRecipesForCategory(category.getName());
        }
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    Intent searchIntent = new Intent(RecipeModel.this, HomePage.class);
                    startActivity(searchIntent);
                    finish();

                }  else if (itemId == R.id.navigation_checklist) {
                    Intent searchIntent = new Intent(RecipeModel.this, WeeklyHub.class);
                    startActivity(searchIntent);
                    finish();
                } else if (itemId == R.id.navigation_account) {
                    Intent searchIntent = new Intent(RecipeModel.this, Settings.class);
                    startActivity(searchIntent);
                }
                return true;
            }
        });

    }

    private void fetchRecipesForCategory(String categoryName) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Recipes");
        // Use whereContains to check if the tags array contains the categoryName
        query.whereContains("tags", categoryName);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> recipeList, ParseException e) {
                if (e == null) {
                    List<RecipeList> recipes = new ArrayList<>();
                    for (ParseObject recipeObject : recipeList) {
                        RecipeList recipe = UtilityRecipe.convertParseObjectToRecipeList(recipeObject);
                        recipes.add(recipe);
                    }

                    // Setting the adapter for the RecyclerView
                    adapter = new RecipeListAdapterToView(recipes);
                    recipeRecyclerView.setAdapter(adapter);
                } else {
                    // Handle error
                    Log.e("back4app", "Error: " + e.getMessage());
                }
            }
        });
    }
}

class MyPagerAdapterCategory extends FragmentStateAdapter {

    public MyPagerAdapterCategory(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Explore();
            case 1:
                return new FavoriteList();
            default:
                return new Explore();
        }
    }
    @Override
    public int getItemCount() {
        return 2;
    }
}

