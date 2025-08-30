package com.example.e2taskly.service;

import android.content.Context;

import com.example.e2taskly.data.repository.TaskCategoryRepository;
import com.example.e2taskly.model.TaskCategory;

public class TaskCategoryService {

    private  TaskCategoryRepository taskCategoryRepository;

    public TaskCategoryService(Context context){
        taskCategoryRepository = new TaskCategoryRepository(context);
    }

    public boolean saveCategory(String name, String colorHex){
        if(name == null || name.isEmpty()){
            return false;
        }
        if(colorHex == null || colorHex.isEmpty()){
            return false;
        }
        TaskCategory category = new TaskCategory();
        category.setName(name);
        category.setColorHex(colorHex);

        long idLong = taskCategoryRepository.createCategory(category);
        int idInt = (int) idLong;
        category.setId(idInt);

        return idInt != -1;
    }



}
