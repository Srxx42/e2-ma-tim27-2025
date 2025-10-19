package com.example.e2taskly.model;

import com.example.e2taskly.model.enums.Difficulty;
import com.example.e2taskly.model.enums.Importance;
import com.example.e2taskly.model.enums.TaskStatus;
import com.example.e2taskly.model.enums.TaskType;

public abstract class Task {

    int Id;
    String CreatorId;
    String name;
    String description;

    TaskCategory category;
    TaskType type;

    TaskStatus status;

    Importance importance;
    Difficulty difficulty;

    int valueXP;
    boolean deleted;


    protected Task(int id, String creatorId, String name, String description, TaskCategory category, TaskType type,
                TaskStatus status, Importance importance, Difficulty difficulty, int valueXP, boolean deleted) {
        Id = id;
        CreatorId = creatorId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.type = type;
        this.status = status;
        this.importance = importance;
        this.difficulty = difficulty;
        this.valueXP = valueXP;
        this.deleted = deleted;
    }


    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getCreatorId() {
        return CreatorId;
    }

    public void setCreatorId(String creatorId) {
        CreatorId = creatorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskCategory getCategory() {
        return category;
    }

    public void setCategory(TaskCategory category) {
        this.category = category;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Importance getImportance() {
        return importance;
    }

    public void setImportance(Importance importance) {
        this.importance = importance;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public int getValueXP() {
        return valueXP;
    }

    public void setValueXP(int valueXP) {
        this.valueXP = valueXP;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
