package com.example.e2taskly.service;

import android.content.Context;

import com.example.e2taskly.data.repository.TaskCategoryRepository;
import com.example.e2taskly.model.TaskCategory;

import java.util.ArrayList;
import java.util.List;

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

    public List<String> getUsedColors(){
         List<TaskCategory> categories = new ArrayList<>();
         List<String> usedColors = new ArrayList<>();

         categories = taskCategoryRepository.getAllCategories();

         for(TaskCategory tc : categories){
             usedColors.add(tc.getColorHex());
         }

         return usedColors;
    }

    public boolean doesNameExist(String name){
        List<TaskCategory> categories = new ArrayList<>();
        List<String> names = new ArrayList<>();

        categories = taskCategoryRepository.getAllCategories();

        for(TaskCategory tc : categories){
            names.add(tc.getName());
        }

        return names.contains(name);
    }

    public List<TaskCategory> getAllCategories(){
        return taskCategoryRepository.getAllCategories();
    }

    public TaskCategory getCategoryById(int id){
        return taskCategoryRepository.getCategoryById(id);
    }

    public boolean updateCategory(int id, String name, String hexColor){
        return taskCategoryRepository.updateCategory(id,name,hexColor);
    }

    public boolean deleteById(int id){
        return taskCategoryRepository.deleteById(id);
    }



}
