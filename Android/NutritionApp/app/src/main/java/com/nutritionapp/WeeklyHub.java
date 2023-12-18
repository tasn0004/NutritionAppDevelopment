package com.nutritionapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class WeeklyHub extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private WeeklyHubAdapter wAdapter; // Declare wAdapter as a member variable
    private ImageView blurOverlayImageView;
    private TextView blurOverlayText;

    Boolean isPaidAccount ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weekly_hub);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isPaidAccount = preferences.getBoolean("isPaidAccount", false);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(null);
        // set the default navigation bar to checklist
        bottomNavigationView.setSelectedItemId(R.id.navigation_checklist);
        // Initialize wAdapter
        tabLayout = findViewById(R.id.tab_layout_grocery);
        blurOverlayImageView = findViewById(R.id.blurOverlayImageViewWeeklyHub);

        wAdapter = new WeeklyHubAdapter(this);
        viewPager = findViewById(R.id.view_pager_grocery);
        viewPager.setAdapter(wAdapter); // Set the adapter
        setupTabLayout();
        blurOverlayText = findViewById(R.id.blurOverlayTextWeeklyHub); // Initialize the TextView

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Disable swiping in WeeklyHub's ViewPager2 when on GroceryList tab
                setPagingEnabled(position != 2);
            }
        });

        WeeklyHubAdapter wAdapter = new WeeklyHubAdapter(this);
        viewPager.setAdapter(wAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("   Meal Plan   ");
                            break;
                        case 1:
                            tab.setText("   Targets   ");
                            break;
                        case 2:
                            tab.setText("   Grocery List   ");
                            break;
                    }
                }
        ).attach();
        tabLayout.setTabTextColors(getResources().getColor(R.color.TextColorWhiteVsBlack),
                getResources().getColor(R.color.button_selected));

        /****************************************************************************/
        //navigation bar [ HOME - SEARCH - CHECKLIST - USER ]
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    Intent searchIntent = new Intent(WeeklyHub.this, HomePage.class);
                    startActivity(searchIntent);
                } else if (itemId == R.id.navigation_checklist) {

                    Intent searchIntent = new Intent(WeeklyHub.this, WeeklyHub.class);
                    startActivity(searchIntent);
                } else if (itemId == R.id.navigation_account) {
                    Intent searchIntent = new Intent(WeeklyHub.this, Settings.class);
                    startActivity(searchIntent);
                }
                return true;
            }
        });
        /****************************************************************************/
        View root = tabLayout.getChildAt(0);
        if (root instanceof LinearLayout) {
            ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(getResources().getColor(R.color.DarkGreyForTabs));
            drawable.setSize(3, 3);
            ((LinearLayout) root).setDividerPadding(20);
            ((LinearLayout) root).setDividerDrawable(drawable);
        }
        if (!isPaidAccount) {
            blurOverlayImageView.setOnClickListener(v -> {
            });
            blurOverlayImageView.setVisibility(View.VISIBLE);
            blurOverlayText.setVisibility(View.VISIBLE);
            applyBlurToWholePage();

        }
    }
    public void setPagingEnabled(boolean enabled) {
        viewPager.setUserInputEnabled(enabled);
    }

    private void applyBlurToWholePage() {
        viewPager.post(() -> {
            viewPager.setDrawingCacheEnabled(true);

            // Create the bitmap and draw the viewPager onto it
            Bitmap bitmap = Bitmap.createBitmap(viewPager.getWidth(), viewPager.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            viewPager.draw(canvas);
            viewPager.setDrawingCacheEnabled(false);

            if (bitmap != null) {
                Bitmap blurredBitmap = BlurBuilder.blur(this, bitmap);
                blurOverlayImageView.setImageBitmap(blurredBitmap);
                blurOverlayImageView.setVisibility(View.VISIBLE);
            }
        });
    }
    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Meal Plan");
                    break;
                case 1:
                    tab.setText("Targets");
                    break;
                case 2:
                    tab.setText("Grocery List");
                    break;
            }
        }).attach();

        if (!isPaidAccount) {
            LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
            for (int i = 0; i < tabStrip.getChildCount(); i++) {
                tabStrip.getChildAt(i).setOnTouchListener((v, event) -> true);
                tabStrip.getChildAt(i).setClickable(false);
            }
        }
    }
}


    class WeeklyHubAdapter extends FragmentStateAdapter {
    private SparseArray<Fragment> fragmentMap = new SparseArray<>();

    public WeeklyHubAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new MealPlan();
                break;
            case 1:
                fragment = new NutritionGoals();
                break;
            case 2:
                fragment = new GroceryList();
                break;
            default:
                fragment = new MealPlan();
        }
        fragmentMap.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public Fragment getFragment(int position) {
        return fragmentMap.get(position);
    }
}
