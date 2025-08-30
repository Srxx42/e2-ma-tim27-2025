package com.example.e2taskly.presentation.activity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e2taskly.R;

public class AddCategoryActivity extends AppCompatActivity {
     private GridLayout colorGrid;
     private String selectedColorHex = null;

     private final String[] PREDEFINED_COLORS = {
             "#F44336", "#E91E63", "#9C27B0", "#2196F3",
             "#4CAF50", "#FFC107", "#FF9800", "#795548",
             "#009688", "#3F51B5", "#673AB7", "#607D8B"
     };

     @Override
    protected  void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_category_create);

         colorGrid = findViewById(R.id.colorGrid);

         addColorOptions();
     }

     private void addColorOptions(){

         // List <String> usedColors = taskRepository.getUsedColors();
         for (String hex : PREDEFINED_COLORS){

            // if (!usedColors.contains(hex)) {}

             View circleView = new View(this);

             GridLayout.LayoutParams params= new GridLayout.LayoutParams();
             params.width = 110;
             params.height = 110;
             params.setMargins(40,25,25,25);
             circleView.setLayoutParams(params);

             GradientDrawable drawable = (GradientDrawable) getDrawable(R.drawable.colored_circles);
             drawable = (GradientDrawable) drawable.mutate();
             drawable.setColor(Color.parseColor(hex));
             circleView.setBackground(drawable);

             GradientDrawable finalDrawable = drawable;
             circleView.setOnClickListener(v ->{

                 for(int i = 0; i < colorGrid.getChildCount(); i++){
                     View child = colorGrid.getChildAt(i);
                     GradientDrawable bg = (GradientDrawable) child.getBackground();
                     bg.setStroke(2, Color.TRANSPARENT);
                 }

                 finalDrawable.setStroke(6, Color.BLUE);

                 selectedColorHex = hex;
             });

             colorGrid.addView(circleView);
         }
     }


}
