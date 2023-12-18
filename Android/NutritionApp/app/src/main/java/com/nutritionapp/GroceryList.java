package com.nutritionapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;

/**
 * This class represents a GroceryList fragment which displays daily and weekly grocery items.
 * The fragment uses a TabLayout and a ViewPager2 to separate the daily and weekly views.
 */
public class GroceryList extends Fragment {
    private TabLayout groceryTab;

    private ViewPager2 viewPager;
    private ArrayList<GroceryItem> dailyIngredients;
    private ArrayList<String> weeklyIngredients;
    private DotsIndicator dotsIndicator;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.grocery_list, container, false);

        // Initialize ViewPager2 and DotsIndicator
        viewPager = view.findViewById(R.id.view_pager_grocery);
        dotsIndicator = view.findViewById(R.id.dots_indicator);
        groceryTab = view.findViewById(R.id.tab_layout_grocery);

        GroceryListAdapter groceryAdapter = new GroceryListAdapter(requireActivity());
        viewPager.setAdapter(groceryAdapter);

        // Attach DotsIndicator to ViewPager2
        dotsIndicator.setViewPager2(viewPager);
        viewPager.setUserInputEnabled(true); // Enable swiping within GroceryList
        new TabLayoutMediator(groceryTab, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Daily List");
                            break;
                        case 1:
                            tab.setText("Weekly List");
                            break;
                    }
                }
        ).attach();
        dailyIngredients = new ArrayList<>();
        weeklyIngredients = new ArrayList<>();
        changeSelectedDotColor();

        return view;

    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getActivity() instanceof WeeklyHub) {
            ((WeeklyHub) getActivity()).setPagingEnabled(!isVisibleToUser);
        }
    }
    public void enableSwipingInGroceryList(boolean enabled) {
        if (viewPager != null) {
            viewPager.setUserInputEnabled(enabled);
        }
    }
    private void changeSelectedDotColor() {
        int selectedDotColor = getResources().getColor(R.color.GoldenYellow);
        dotsIndicator.setSelectedDotColor(selectedDotColor);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                dotsIndicator.setSelectedDotColor(selectedDotColor);
            }
        });
    }
}

/**
 * This class represents an adapter for the GroceryList ViewPager2.
 * It returns the correct fragment (Daily or Weekly) based on the selected tab.

 */
class GroceryListAdapter extends FragmentStateAdapter {
    /**
     * Constructor for the GroceryListAdapter.
     *
     * @param fragmentActivity the parent activity which is required for the super class constructor.
     */
    public GroceryListAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Returns the corresponding fragment based on the tab position
        switch (position) {
            case 0:
                return new DailyGroceryList();
            case 1:
                return new WeeklyGroceryList();
            default:
                return new DailyGroceryList();
        }
    }
    @Override
    public int getItemCount() {
        return 2; // The number of tabs
    }
}
