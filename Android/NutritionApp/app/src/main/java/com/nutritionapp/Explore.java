package com.nutritionapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Explore extends Fragment {

    private RecyclerView categoryRecyclerView;
    private RecyclerView recipeRecyclerView;
    private CategoryAdapter cAdapter;
    private ExploreRecipeAdapter rAdapter;
    private EditText searchBox;
    private ProgressBar loadingIndicator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.explore, container, false);
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);
        recipeRecyclerView = view.findViewById(R.id.recipeRecyclerView);
        recipeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        searchBox = view.findViewById((R.id.search_box));
        searchBox.setInputType(InputType.TYPE_CLASS_TEXT);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);

        loadCategories();

        // Only for Making adapter null on text change
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    // If searchBox is empty, show categories and hide recipes
                    categoryRecyclerView.setVisibility(View.VISIBLE);
                    recipeRecyclerView.setVisibility(View.GONE);
                } else {
                    // If searchBox is not empty, hide categories and prepare to show recipes
                    categoryRecyclerView.setVisibility(View.GONE);
                    recipeRecyclerView.setVisibility(View.GONE);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Perform the action you want when the "Done" button is pressed.
                    // You can put your code here.
                    String s = searchBox.getText().toString();
                    if (s.toString().isEmpty()) {
                    recipeRecyclerView.setVisibility(View.GONE);
                    categoryRecyclerView.setVisibility(View.VISIBLE);
                    loadCategories();
                } else {
                    categoryRecyclerView.setVisibility(View.GONE);
                    recipeRecyclerView.setVisibility(View.VISIBLE);
                    rAdapter = null;
                    recipeRecyclerView.setAdapter(rAdapter);
                    PerformNameQuery(s.toString());
                }
                    return true; // Return true to indicate that you've handled the event.
                }
                return false; // Return false to let the system handle other events.
            }
        });
        return view;
    }


    private void loadCategories() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Category");
        loadingIndicator.setVisibility(View.VISIBLE);

        query.findInBackground((categories, e) -> {
            if (e == null) {
                loadingIndicator.setVisibility(View.GONE);

                List<Category> categoryList = new ArrayList<>();
                for (ParseObject obj : categories) {
                    String name = obj.getString("name");
                    ParseFile imageFile = obj.getParseFile("categoryImage");
                    if (imageFile != null) {
                        String imageUrl = imageFile.getUrl();
                        categoryList.add(new Category(name, imageUrl));
                    } else {
                        break;
                    }
                }
                cAdapter = new CategoryAdapter(categoryList, category -> {
                    Intent intent = new Intent(getContext(), RecipeModel.class);
                    intent.putExtra("CATEGORY", category);
                    startActivity(intent);
                });
                categoryRecyclerView.setAdapter(cAdapter);
            }
        });
    }

    private void PerformNameQuery(String queryBeforeUpper) {
        String query = queryBeforeUpper.substring(0, 1).toUpperCase() + queryBeforeUpper.substring(1);
        ParseQuery<ParseObject> nameQuery = ParseQuery.getQuery("Recipes");
        nameQuery.whereMatches("name", query, "i"); // 'i' flag for case-insensitive
        loadingIndicator.setVisibility(View.VISIBLE);

        nameQuery.findInBackground((nameRecipes, nameError) -> {
            loadingIndicator.setVisibility(View.GONE);

            if (nameError == null && nameRecipes.size() > 0) {
                List<RecipeList> recipeList = new ArrayList<>();
                // Handle found recipes by name
                for (ParseObject recipe : nameRecipes) {

                    RecipeList r = UtilityRecipe.convertParseObjectToRecipeList(recipe);
                    RecipeList recipePopulate = new RecipeList(r.getId(), r.getName(), r.getLikeCount(), r.getTimeInMinutes(), r.getImageUrl(), r.getYoutubeVideoId(), r.getDescription(), r.getCommentsCount(), r.getIngredients(), r.getProteinGrams(),r.getSodiumGrams(),r.getCarbohydratesMilligrams(), r.getNutritionInformation(), r.getBackgroundContent());
                    recipeList.add(recipePopulate);
                }
                rAdapter = new ExploreRecipeAdapter(recipeList);  // Use the ExploreRecipeAdapter
                recipeRecyclerView.setAdapter(rAdapter);
            } else {
                // No recipes found by name, so execute the ingredients query
                fetchMaxIngredientLength(query);
            }
        });
    }

    private void PerformIngredientQuery(String query, int index, int maxIngredientLength) {
        if (index < maxIngredientLength) {
            ParseQuery<ParseObject> ingredientsQuery = ParseQuery.getQuery("Recipes");
            ingredientsQuery.whereMatches("ingredients." + index + ".0", query, "i"); // 'i' flag for case-insensitive
            loadingIndicator.setVisibility(View.VISIBLE);

            ingredientsQuery.findInBackground((ingredientsRecipes, ingredientsError) -> {
                loadingIndicator.setVisibility(View.GONE);

                if (ingredientsError == null && ingredientsRecipes.size() > 0) {

                    List<RecipeList> recipeList = new ArrayList<>();
                    // Handle found recipes by name
                    for (ParseObject recipe : ingredientsRecipes) {
                        RecipeList r = UtilityRecipe.convertParseObjectToRecipeList(recipe);
                        RecipeList recipePopulate = new RecipeList(r.getId(), r.getName(), r.getLikeCount(), r.getTimeInMinutes(), r.getImageUrl(), r.getYoutubeVideoId(), r.getDescription(), r.getCommentsCount(), r.getIngredients(), r.getProteinGrams(),r.getSodiumGrams(),r.getCarbohydratesMilligrams(), r.getNutritionInformation(), r.getBackgroundContent());
                        recipeList.add(recipePopulate);
                    }
                    rAdapter = new ExploreRecipeAdapter(recipeList);  // Use the ExploreRecipeAdapter
                    recipeRecyclerView.setAdapter(rAdapter);
                }
                // Continue to the next index
                PerformIngredientQuery(query, index + 1, maxIngredientLength);
            });
        } else {
            // All possible indexes have been checked, and no more values were found
        }
    }

    private void fetchMaxIngredientLength(String query) {
        ParseQuery<ParseObject> maxIngredientLengthQuery = ParseQuery.getQuery("Recipes");
        maxIngredientLengthQuery.orderByDescending("ingredients.length");
        maxIngredientLengthQuery.getFirstInBackground((maxIngredientRecipe, error) -> {
            if (error == null) {
                List<Object> ingredientsList = maxIngredientRecipe.getList("ingredients");
                if (ingredientsList != null) {
                    int maxIngredientLength = ingredientsList.size();
                    PerformIngredientQuery(query, 0, maxIngredientLength);
                }
            }
        });
    }
}
