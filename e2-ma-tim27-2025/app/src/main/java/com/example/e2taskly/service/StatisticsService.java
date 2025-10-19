package com.example.e2taskly.service;

import android.content.Context;

import com.example.e2taskly.data.repository.TaskRepository;

import java.util.Map;

public class StatisticsService {
    TaskRepository taskRepository;
    public StatisticsService(Context context){
        this.taskRepository = new TaskRepository(context);
    }
    public Map<String, Integer> getTaskStatusCounts(String userId) {
        return taskRepository.getTaskStatusCounts(userId);
    }
    public int getLongestTaskStreak(String userId) {
        return taskRepository.getLongestTaskStreak(userId);
    }
    public Map<String, Integer> getCompletedTasksCountByCategory(String userId) {
        return taskRepository.getCompletedTasksCountByCategory(userId);
    }
    public Map<String, Double> getAverageDifficultyXpOverTime(String userId) {
        return taskRepository.getAverageDifficultyXpOverTime(userId);
    }
    public Map<String, Integer> getXpEarnedLast7Days(String userId) {
        return taskRepository.getXpEarnedLast7Days(userId);
    }
}
