package com.example.e2taskly.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.e2taskly.data.repository.TaskRepository;
import com.example.e2taskly.model.RepeatingTask;
import com.example.e2taskly.model.RepeatingTaskOccurrence;
import com.example.e2taskly.model.Task;
import com.example.e2taskly.model.enums.RepeatingType;
import com.example.e2taskly.model.enums.TaskStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskService {

    private TaskRepository taskRepository;

    public TaskService(Context context) {
        taskRepository = new TaskRepository(context);
    }

    public boolean saveTask(Task task){
        long idLong = taskRepository.createTask(task);
        int idInt = (int) idLong;
        task.setId(idInt);

        return idInt != -1;
    }

    public void createRepeatingTaskOccurrences(RepeatingTask task){
        LocalDate current = task.getStartingDate();
        LocalDate finish = task.getFinishingDate();

        boolean success = false;

        while(!current.isAfter(finish)){

            RepeatingTaskOccurrence occurrence = new RepeatingTaskOccurrence(-1,task.getId(),current, TaskStatus.ACTIVE);

            if(task.getRepeatingType().equals(RepeatingType.DAILY)){
                current = current.plusDays(task.getInterval());
            } else if (task.getRepeatingType().equals(RepeatingType.WEEKLY)){
                current = current.plusWeeks(task.getInterval());
            } else {
                throw new UnsupportedOperationException("Unsupported repeating type " + task.getType());
            }

            success = taskRepository.saveTaskOccurrence(occurrence);

            if(success)  Log.d("TaskService", "Occurrence successfully saved.");
            else Log.e("TaskService", "Error saving occurrence.");
        }
    }

    public List<RepeatingTaskOccurrence> getAllTaskOccurrences(int taskId){
       return  taskRepository.getAllTaskOccurrences(taskId);
    }

}
