package com.example.e2taskly.model;

public class TaskCategory {
     int id;
     String creatorId;
     String name;
     String colorHex;


    public TaskCategory() {}

    public TaskCategory(int id,String creatorId ,String colorHex, String name) {
        this.id = id;
        this.creatorId = creatorId;
        this.colorHex = colorHex;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
}
