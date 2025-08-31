package com.example.e2taskly.data.repository;

import android.content.Context;

import com.example.e2taskly.data.database.SQLiteHelper;
import com.example.e2taskly.data.database.TaskCategoryLocalDataSource;
import com.example.e2taskly.model.TaskCategory;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;

import java.util.List;

public class TaskCategoryRepository {

    private TaskCategoryLocalDataSource localDataSource;

    public TaskCategoryRepository(Context context) {
        localDataSource = new TaskCategoryLocalDataSource(context);
    }

    public long createCategory (TaskCategory category){
        return localDataSource.addCategory(category);
    }

    public List<TaskCategory> getAllCategories(){
        return localDataSource.getAllCategories();
    }

    public TaskCategory getCategoryById(int id){
        return localDataSource.getCategoryById(id);
    }

    public boolean updateCategory(int id,String name, String hexColor){
        return localDataSource.updateCategory(id,name,hexColor);
    }
}
