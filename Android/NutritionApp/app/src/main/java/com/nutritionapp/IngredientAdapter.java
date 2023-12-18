package com.nutritionapp;

import android.graphics.Paint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
/**
 * IngredientAdapter provides a binding from an ArrayList of GroceryItem data to views displayed
 * within a RecyclerView. This adapter also supports toggling of ingredients and updates the UI
 * based on the toggled state.
 *
 */
public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {
    // Listener for the toggle event
    private OnIngredientToggledListener mListener;
    // Boolean to decide if a ToggleButton should be shown
    private boolean showToggleButton;
    // List of grocery items to be displayed
    private ArrayList<GroceryItem> ingredients;

    /**
     * Constructor for IngredientAdapter.
     *
     * @param ingredients List of ingredients (grocery items) to display.
     * @param listener Callback for when an ingredient is toggled.
     * @param showToggleButton Flag to determine if toggle buttons should be displayed (this for later, not sure yet).
     */
    public IngredientAdapter(ArrayList<GroceryItem> ingredients, OnIngredientToggledListener listener, boolean showToggleButton) {
        this.ingredients = ingredients;
        this.mListener = listener;
        this.showToggleButton = showToggleButton;
    }
    /**
     * Sets or updates the ingredients list.
     *
     * @param ingredients Updated list of ingredients.
     */
    public void setIngredients(ArrayList<GroceryItem> ingredients) {
        this.ingredients = ingredients;
    }
    /**
     * Listener interface to notify when an ingredient is toggled.
     */
    public interface OnIngredientToggledListener {
        void onIngredientToggled(String formattedDate, GroceryItem item);
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);
        return new IngredientViewHolder(view);
    }

    /**
     * Removes an item from the ingredients list at the given position.
     *
     * @param position Index of the item to remove.
     * @return The removed GroceryItem, or null if the position was invalid.
     */
    public GroceryItem removeItem(int position) {
        GroceryItem removedItem = null;
        if (position >= 0 && position < ingredients.size()) {
            removedItem = ingredients.remove(position);
            notifyItemRemoved(position);
        }
        return removedItem;
    }
    /**
     * get the GroceryItem at the given position.
     *
     * @param position Index of the item to retrieve.
     * @return The GroceryItem at the specified position.
     */
    public GroceryItem getGroceryItemAt(int position) {
        return ingredients.get(position);
    }
    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        GroceryItem currentItem = ingredients.get(position);
        // Bind data to UI elements
        holder.ingredientNameText.setText(currentItem.getIngredientName());
        holder.ingredientQuantityText.setText(currentItem.getIngredientAmount() + " " + currentItem.getIngredientUnit());
        holder.toggleButton.setChecked(currentItem.isToggled());
        // Apply check mark effect based on the toggled state
        if (currentItem.isToggled()) {
            holder.ingredientNameText.setPaintFlags(holder.ingredientNameText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.ingredientQuantityText.setPaintFlags(holder.ingredientQuantityText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.ingredientNameText.setPaintFlags(holder.ingredientNameText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.ingredientQuantityText.setPaintFlags(holder.ingredientQuantityText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

            holder.rowContainer.setBackgroundColor(Color.TRANSPARENT);
        }
        // Set the listener to detect when an ingredient is toggled
        holder.toggleButton.setOnCheckedChangeListener(null); // Remove any previous listener
        holder.toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (showToggleButton) {
                GroceryItem itemToToggle = ingredients.get(holder.getAdapterPosition());
                itemToToggle.setToggled(isChecked);
                // Notify the listener about the toggled ingredient
                if (mListener != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
                    String formattedDate = sdf.format(Calendar.getInstance().getTime());
                    mListener.onIngredientToggled(formattedDate, itemToToggle);
                }

                holder.itemView.post(() -> notifyItemChanged(holder.getAdapterPosition()));
            }
        });

        if (showToggleButton) {
            holder.toggleButton.setVisibility(View.VISIBLE);
        } else {
            holder.toggleButton.setVisibility(View.GONE);
        }
    }
    @Override
    public int getItemCount() {
        return ingredients.size();
    }
    /**
     * ViewHolder class to represent the UI elements for each ingredient item.
     */
    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        public TextView ingredientText;
        public ToggleButton toggleButton;
        public LinearLayout rowContainer;
        public TextView ingredientNameText;
        public TextView ingredientQuantityText;
        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientNameText = itemView.findViewById(R.id.ingredientNameText);
            ingredientQuantityText = itemView.findViewById(R.id.ingredientQuantityText);
            toggleButton = itemView.findViewById(R.id.toggleButton);
            rowContainer = itemView.findViewById(R.id.rowContainer);
        }
    }
}
