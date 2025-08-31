package com.example.e2taskly.presentation.activity;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e2taskly.R;
import com.example.e2taskly.adapters.CategoryListAdapter;
import com.example.e2taskly.model.TaskCategory;
import com.example.e2taskly.service.TaskCategoryService;

import java.util.ArrayList;
import java.util.List;

public class ShowCategoriesActivity extends AppCompatActivity {

    private TaskCategoryService taskCategoryService;

    private List<TaskCategory> categories;

    private TaskCategory selectedCategory;

    private ListView  categoryListView;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        taskCategoryService = new TaskCategoryService(this);
        setContentView(R.layout.activity_category_show);

        categoryListView = findViewById(R.id.categoryListView);
        categories = new ArrayList<>();
        categories = taskCategoryService.getAllCategories();

        CategoryListAdapter adapter = new CategoryListAdapter(this, new ArrayList<>(categories));
        categoryListView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        categories = taskCategoryService.getAllCategories();
        CategoryListAdapter adapter = new CategoryListAdapter(this, new ArrayList<>(categories));
        categoryListView.setAdapter(adapter);
    }

}
