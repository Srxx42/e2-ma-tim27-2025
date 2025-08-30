package com.example.e2taskly.model;

import com.example.e2taskly.model.enums.Difficulty;
import com.example.e2taskly.model.enums.Importance;
import com.example.e2taskly.model.enums.TaskStatus;
import com.example.e2taskly.model.enums.TaskType;

import java.time.LocalDate;

public class SingleTask extends Task {

    LocalDate taskDate;


    protected SingleTask(int id, int creatorId, String name, String description, TaskCategory category,
                         TaskType type, TaskStatus status, Importance importance, Difficulty difficulty, int valueXP,
                         boolean deleted, LocalDate taskDate) {
        super(id, creatorId, name, description, category, type, status, importance, difficulty, valueXP, deleted);
        this.taskDate = taskDate;
    }

    public LocalDate getTaskDate() {
        return taskDate;
    }

    public void setTaskDate(LocalDate taskDate) {
        this.taskDate = taskDate;
    }
}
