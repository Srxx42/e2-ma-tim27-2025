package com.example.e2taskly.data.repository;

import android.content.Context;
import android.util.Pair;

import com.example.e2taskly.data.database.TaskLocalDataSource;
import com.example.e2taskly.model.RepeatingTask;
import com.example.e2taskly.model.RepeatingTaskOccurrence;
import com.example.e2taskly.model.SingleTask;
import com.example.e2taskly.model.Task;
import com.example.e2taskly.model.enums.TaskStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
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
    public boolean deleteFutureOccurrences(int repeatingTaskId){
        return localDataSource.deleteFutureOccurrences(repeatingTaskId);
    }

    public boolean deleteAllOccurrences(int repeatingTaskId) {
     return localDataSource.deleteAllOccurrences(repeatingTaskId);
    }

    public boolean updateOccurrence(RepeatingTaskOccurrence occurrence){
        return localDataSource.updateOccurrence(occurrence);
    }

    public boolean pauseAllOccurrences(int taskId){
        return localDataSource.pauseAllOccurrences(taskId);
    }

    public boolean unpauseAllOccurrences(int taskId){
        return localDataSource.unpauseAllOccurrences(taskId);
    }

    public List<SingleTask> getAllSingleTasksForAllUsers() {
        return localDataSource.getAllSingleTasksForAllUsers();
    }

    public List<RepeatingTask> getAllRepeatingTasksForAllUsers() {
        return localDataSource.getAllRepeatingTasksForAllUsers();
    }

    public int getUserTaskCountByStatus(Date startDate, Date endDate, TaskStatus status, String userUid){
        return localDataSource.getUserTaskCountByStatus(startDate,endDate,status,userUid);
    }

    public boolean isThereTaskWithCategory(int categoryId){
        return localDataSource.isThereTaskWithCategory(categoryId);
    }


    public List<Task> getTasksByCreator(String userId) {
        List<Task> allTasks = new ArrayList<>();

        List<SingleTask> singleTasks = localDataSource.getAllSingleTasks(userId);
        List<RepeatingTask> repeatingTasks = localDataSource.getAllRepeatingTasks(userId);

        allTasks.addAll(singleTasks);
        allTasks.addAll(repeatingTasks);

        return allTasks;
    }

}
