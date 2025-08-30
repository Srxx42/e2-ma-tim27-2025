package com.example.e2taskly.presentation.activity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e2taskly.R;
import com.example.e2taskly.service.TaskCategoryService;
import com.google.android.material.textfield.TextInputEditText;

public class AddCategoryActivity extends AppCompatActivity {

    private TextInputEditText  editCategoryName;
    private Button saveCategoryButton;
    private GridLayout colorGrid;
    private String selectedColorHex = null;

    private TaskCategoryService taskCategoryService;

     private final String[] PREDEFINED_COLORS = {
             "#F44336", "#E91E63", "#9C27B0", "#2196F3",
             "#4CAF50", "#FFC107", "#FF9800", "#795548",
             "#009688", "#3F51B5", "#673AB7", "#607D8B"
     };

     @Override
    protected  void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         taskCategoryService = new TaskCategoryService(this);
         setContentView(R.layout.activity_category_create);

         colorGrid = findViewById(R.id.colorGrid);

         addColorOptions();
         editCategoryName = findViewById(R.id.editCategoryName);
         saveCategoryButton = findViewById(R.id.saveCategoryButton);
         saveCategoryButton.setOnClickListener(v -> saveCategory());
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

     private void saveCategory(){
         String name = editCategoryName.getText() != null ? editCategoryName.getText().toString().trim() : "";
         String hexColor = selectedColorHex;

        boolean success = taskCategoryService.saveCategory(name,hexColor);

         if(success){
             Toast.makeText(this, "Category saved!", Toast.LENGTH_SHORT).show();
             finish(); // zatvori activity ili idi nazad
         } else {
             Toast.makeText(this, "Name or color invalid / already used!", Toast.LENGTH_SHORT).show();
         }
     }


}
