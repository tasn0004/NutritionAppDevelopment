package com.nutritionapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class NutrientViewLines extends LinearLayout {
    private TextView nutrientNameText;
    private TextView nutrientDetailsText;
    private ProgressBar nutrientProgressBar;


    public NutrientViewLines(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.item_nutrient_lines, this, true);
        nutrientNameText = findViewById(R.id.nutrientNameText);
        nutrientDetailsText = findViewById(R.id.nutrientDetailsText);
        nutrientProgressBar = findViewById(R.id.nutrientProgressBar);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.NutrientViewLines,
                0, 0);

        try {
            if (a.hasValue(R.styleable.NutrientViewLines_progressColor)) {
                int progressColor = a.getColor(R.styleable.NutrientViewLines_progressColor, 0);
                setProgressColor(progressColor);
            }
        } finally {
            a.recycle();
        }
    }
    public void setNutrientName(String name) {
        nutrientNameText.setText(name);
    }

    public void setNutrientDetails(String details) {
        nutrientDetailsText.setText(details);
    }

    public void setProgress(int progress) {
        nutrientProgressBar.setProgress(progress);
    }
    public void setProgressColor(int color) {
        nutrientProgressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
}
