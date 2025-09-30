package com.example.e2taskly.data.repository;

import android.content.Context;

import com.example.e2taskly.data.database.TaskLocalDataSource;
import com.example.e2taskly.model.RepeatingTask;
import com.example.e2taskly.model.RepeatingTaskOccurrence;
import com.example.e2taskly.model.SingleTask;
import com.example.e2taskly.model.Task;

import java.util.List;

public class TaskRepository {

    private TaskLocalDataSource localDataSource;

    public TaskRepository(Context context){
        localDataSource = new TaskLocalDataSource(context);
    }

    public long createTask(Task task){
        return localDataSource.addTask(task);
    }

    public List<RepeatingTask> getAllRepeatingTasks(String creatorID){
        return localDataSource.getAllRepeatingTasks(creatorID);
    }

    public List<SingleTask> getAllSingleTasks(String creatorID){
        return localDataSource.getAllSingleTasks(creatorID);
    }

    public Task getTaskById(int id){
        return localDataSource.getTaskById(id);
    }

    public boolean updateTask(Task task){
        return localDataSource.updateTask(task);
    }

    public boolean deleteById(int id){ return localDataSource.deleteTaskById(id);}

    public boolean saveTaskOccurrence(RepeatingTaskOccurrence occurence){
        return localDataSource.saveTaskOccurrence(occurence);
    }

    public List<RepeatingTaskOccurrence> getAllTaskOccurrences(int taskId){
        return localDataSource.getAllTaskOccurrences(taskId);
    }

    public boolean deleteFutureOccurrences(int repeatingTaskId){
        return localDataSource.deleteFutureOccurrences(repeatingTaskId);
    }

    public boolean deleteAllOccurrences(int repeatingTaskId) {
     return localDataSource.deleteAllOccurrences(repeatingTaskId);
    }


}
