package com.nutritionapp;

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
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;

    public CategoryAdapter(List<Category> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.name.setText(category.getName());
        Glide.with(holder.image.getContext()).load(category.getImageUrl()).into(holder.image);

        holder.image.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.recipeOverlay), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView name;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.categoryImage);
            name = itemView.findViewById(R.id.categoryName);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onCategoryClick(categories.get(position));
                    }
                }
            });
        }

    }
    public interface CategoryClickListener {
        void onCategoryClick(Category category);
    }

    private CategoryClickListener listener;

    public CategoryAdapter(List<Category> categories, CategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

}
