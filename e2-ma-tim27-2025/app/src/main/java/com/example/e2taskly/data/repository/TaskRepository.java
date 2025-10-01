package com.example.e2taskly.data.repository;

import android.content.Context;
import android.util.Pair;

import com.example.e2taskly.data.database.TaskLocalDataSource;
import com.example.e2taskly.model.RepeatingTask;
import com.example.e2taskly.model.RepeatingTaskOccurrence;
import com.example.e2taskly.model.SingleTask;
import com.example.e2taskly.model.Task;
import com.example.e2taskly.model.enums.TaskStatus;

import java.util.List;
import java.util.Map;

public class TaskRepository {

    private TaskLocalDataSource localDataSource;

    public TaskRepository(Context context){
        localDataSource = new TaskLocalDataSource(context);
    }

    public long createTask(Task task){
        return localDataSource.addTask(task);
    }

    public List<RepeatingTask> getAllRepeatingTasks(){
        return localDataSource.getAllRepeatingTasks();
    }

    public List<SingleTask> getAllSingleTasks(){
        return localDataSource.getAllSingleTasks();
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

//    Lazar Statistika
    public Map<String, Integer> getTaskStatusCounts(String userId) {
        return localDataSource.getTaskStatusCounts(userId);
    }
    public int getLongestTaskStreak(String userId) {
        List<Pair<String, String>> taskEvents = localDataSource.getAllTaskEventsForStreak(userId);

        int longestStreak = 0;
        int currentStreak = 0;
        String completedStatus = TaskStatus.COMPLETED.name();
        String failedStatus = TaskStatus.FAILED.name();

        for (Pair<String, String> event : taskEvents) {
            if (event.second.equals(completedStatus)) {
                currentStreak++;
            } else if (event.second.equals(failedStatus)) {
                longestStreak = Math.max(longestStreak, currentStreak);
                currentStreak = 0;
            }
        }

        longestStreak = Math.max(longestStreak, currentStreak);

        return longestStreak;
    }
    public Map<String, Integer> getCompletedTasksCountByCategory(String userId) {
        return localDataSource.getCompletedTasksCountByCategory(userId);
    }
    public Map<String, Double> getAverageDifficultyXpOverTime(String userId) {
        return localDataSource.getAverageDifficultyXpOverTime(userId);
    }
    public Map<String, Integer> getXpEarnedLast7Days(String userId) {
        return localDataSource.getXpEarnedLast7Days(userId);
    }

}
