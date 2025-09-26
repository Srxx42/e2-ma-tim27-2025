package com.example.e2taskly.model;

import com.example.e2taskly.model.enums.Difficulty;
import com.example.e2taskly.model.enums.Importance;
import com.example.e2taskly.model.enums.RepeatingType;
import com.example.e2taskly.model.enums.TaskStatus;
import com.example.e2taskly.model.enums.TaskType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RepeatingTask extends  Task {

    RepeatingType repeatingType;

    int interval;

    LocalDate startingDate;

    LocalDate finishingDate;

    List<LocalDate> taskDates;


    public RepeatingTask(int id, String creatorId, String name, String description, TaskCategory category,
                         TaskType type, TaskStatus status, Importance importance, Difficulty difficulty,
                         int valueXP, boolean deleted, RepeatingType repeatingType,int interval,
                         LocalDate startingDate, LocalDate finishingDate) {
        super(id, creatorId, name, description, category, type, status, importance, difficulty, valueXP, deleted);
        this.repeatingType = repeatingType;
        this.interval = interval;
        this.startingDate = startingDate;
        this.finishingDate = finishingDate;
    }


    public List<LocalDate> calculateTaskDates(RepeatingType type, int interval, LocalDate startDate, LocalDate finishDate){

        if(this.taskDates == null){
            this.taskDates = new ArrayList<>();
        }

        //Zadrzavanje proslih datuma pri azuriranju
        List<LocalDate> preservedDates = this.taskDates.stream()
                .filter(d -> d.isBefore(startDate))
                .collect(Collectors.toList());

        List<LocalDate> newDates = new ArrayList<>();

        LocalDate current = startDate;
        while(!current.isAfter(finishDate)){
            newDates.add(current);

            if(type.equals(RepeatingType.DAILY)) {
                current = current.plusDays(interval);
            } else if (type.equals(RepeatingType.WEEKLY)){
                current = current.plusWeeks(interval);
            } else {
                throw new UnsupportedOperationException("Unsupported repeating type " + type);
            }
        }

        this.taskDates = new ArrayList<>();
        this.taskDates.addAll(preservedDates);
        this.taskDates.addAll(newDates);

        return this.taskDates;
    }

    public RepeatingType getRepeatingType() {
        return repeatingType;
    }

    public void setRepeatingType(RepeatingType repeatingType) {
        this.repeatingType = repeatingType;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public LocalDate getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(LocalDate startingDate) {
        this.startingDate = startingDate;
    }

    public LocalDate getFinishingDate() {
        return finishingDate;
    }

    public void setFinishingDate(LocalDate finishingDate) {
        this.finishingDate = finishingDate;
    }

    public List<LocalDate> getTaskDates() {
        return taskDates;
    }

    public void setTaskDates(List<LocalDate> taskDates) {
        this.taskDates = taskDates;
    }
}
