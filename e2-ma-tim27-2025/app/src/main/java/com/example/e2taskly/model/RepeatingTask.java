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

    List<RepeatingTaskOccurrence> occurrences;




    public RepeatingTask(int id, String creatorId, String name, String description, TaskCategory category,
                         TaskType type, TaskStatus status, Importance importance, Difficulty difficulty,
                         int valueXP, boolean deleted, RepeatingType repeatingType,int interval,
                         LocalDate startingDate, LocalDate finishingDate) {
        super(id, creatorId, name, description, category, type, status, importance, difficulty, valueXP, deleted);
        this.repeatingType = repeatingType;
        this.interval = interval;
        this.startingDate = startingDate;
        this.finishingDate = finishingDate;
        this.occurrences = new ArrayList<>();
    }

    public List<RepeatingTaskOccurrence> getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(List<RepeatingTaskOccurrence> occurrences) {
        this.occurrences = occurrences;
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

}
