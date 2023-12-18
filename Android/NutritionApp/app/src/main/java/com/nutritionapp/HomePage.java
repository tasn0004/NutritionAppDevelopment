package com.nutritionapp;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.navigation.NavigationBarView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomePage extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(null);
        // set the default navigation bar to home
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        MyPagerAdapter pagerAdapter = new MyPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("   Explore   ");
                            break;
                        case 1:
                            tab.setText("   Favourites   ");
                            break;
                    }
                }
        ).attach();
        tabLayout.setTabTextColors(getResources().getColor(R.color.TextColorWhiteVsBlack),
                getResources().getColor(R.color.button_selected));


        /****************************************************************************/

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    Intent searchIntent = new Intent(HomePage.this, HomePage.class);
                    startActivity(searchIntent);

                }  else if (itemId == R.id.navigation_checklist) {
                    Intent searchIntent = new Intent(HomePage.this, WeeklyHub.class);
                    startActivity(searchIntent);
                } else if (itemId == R.id.navigation_account) {
                    Intent searchIntent = new Intent(HomePage.this, Settings.class);
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
    }
}

class MyPagerAdapter extends FragmentStateAdapter {

    public MyPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
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
