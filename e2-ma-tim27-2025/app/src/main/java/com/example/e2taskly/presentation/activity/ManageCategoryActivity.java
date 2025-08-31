package com.example.e2taskly.presentation.activity;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e2taskly.R;
import com.example.e2taskly.model.TaskCategory;
import com.example.e2taskly.service.TaskCategoryService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class ManageCategoryActivity extends AppCompatActivity {

    private TextInputEditText  editCategoryName;
    private TextInputLayout  textInputCategoryName;
    private Button saveCategoryButton;
    private GridLayout colorGrid;
    private String selectedColorHex = null;

    private int editingCategoryId;

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
         setContentView(R.layout.activity_category_manage);

         colorGrid = findViewById(R.id.colorGrid);
         editCategoryName = findViewById(R.id.editCategoryName);
         textInputCategoryName = findViewById(R.id.textInputCategoryName);
         saveCategoryButton = findViewById(R.id.saveCategoryButton);

         editingCategoryId = getIntent().getIntExtra("CATEGORY_ID",-1);
         addColorOptions();

         if(editingCategoryId != -1){

             TaskCategory category = taskCategoryService.getCategoryById(editingCategoryId);
             if(category != null){
                 editCategoryName.setText(category.getName());
                 selectedColorHex = category.getColorHex();
             }
         }
         saveCategoryButton.setOnClickListener(v -> saveCategory());


     }

     private void addColorOptions(){
         List<String> usedColors = taskCategoryService.getUsedColors();
         String currentColor = null;

         if (editingCategoryId != -1) {
             TaskCategory category = taskCategoryService.getCategoryById(editingCategoryId);
             if (category != null) {
                 currentColor = category.getColorHex();
             }
         }


         for (String hex : PREDEFINED_COLORS) {

             if (!usedColors.contains(hex) || (currentColor != null && currentColor.equals(hex))) {

                 View circleView = new View(this);
                 circleView.setTag(hex); // zapamti hex u View

                 GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                 params.width = 110;
                 params.height = 110;
                 params.setMargins(40, 25, 25, 25);
                 circleView.setLayoutParams(params);

                 GradientDrawable drawable = (GradientDrawable) getDrawable(R.drawable.colored_circles);
                 drawable = (GradientDrawable) drawable.mutate();
                 drawable.setColor(Color.parseColor(hex));

                 if (hex.equals(currentColor)) {
                     drawable.setStroke(6, Color.BLUE);
                     selectedColorHex = hex;
                 }

                 circleView.setBackground(drawable);

                 GradientDrawable finalDrawable = drawable;
                 circleView.setOnClickListener(v -> {

                     for (int i = 0; i < colorGrid.getChildCount(); i++) {
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

     private void saveCategory(){
         String name = editCategoryName.getText() != null ? editCategoryName.getText().toString().trim() : "";
         String hexColor = selectedColorHex;

         int categoryId = getIntent().getIntExtra("CATEGORY_ID",-1);
         boolean success;

         if(categoryId == -1) {
             if (taskCategoryService.doesNameExist(name)) {
                 textInputCategoryName.setError("Category name already exists.");
                 return;
             }
             success = taskCategoryService.saveCategory(name, hexColor);

             if (success) {
                 Toast.makeText(this, "Category saved!", Toast.LENGTH_SHORT).show();
                 finish(); // zatvori activity ili idi nazad
             } else {
                 Toast.makeText(this, "Error occured while saving category.", Toast.LENGTH_SHORT).show();
             }
         } else{
             success = taskCategoryService.updateCategory(categoryId,name,hexColor);
             if (success) {
                 Toast.makeText(this, "Category updated!", Toast.LENGTH_SHORT).show();
                 finish(); // zatvori activity ili idi nazad
             } else {
                 Toast.makeText(this, "Error occured while updating category.", Toast.LENGTH_SHORT).show();
             }

         }
     }



}
