package com.example.e2taskly.model;

import com.example.e2taskly.model.enums.TaskStatus;

import java.time.LocalDate;

public class RepeatingTaskOccurrence {

     private int id;
     private int repeatingTaskId;
     private LocalDate occurrenceDate;
     private TaskStatus occurrenceStatus;

     public RepeatingTaskOccurrence(int id, int repeatingTaskId, LocalDate occurrenceDate, TaskStatus occurrenceStatus) {
          this.id = id;
          this.repeatingTaskId = repeatingTaskId;
          this.occurrenceDate = occurrenceDate;
          this.occurrenceStatus = occurrenceStatus;
     }

     public int getId() {
          return id;
     }

     public void setId(int id) {
          this.id = id;
     }

     public int getRepeatingTaskId() {
          return repeatingTaskId;
     }

     public void setRepeatingTaskId(int repeatingTaskId) {
          this.repeatingTaskId = repeatingTaskId;
     }

     public LocalDate getOccurrenceDate() {
          return occurrenceDate;
     }

     public void setOccurrenceDate(LocalDate occurrenceDate) {
          this.occurrenceDate = occurrenceDate;
     }

     public TaskStatus getOccurrenceStatus() {
          return occurrenceStatus;
     }

     public void setOccurrenceStatus(TaskStatus occurrenceStatus) {
          this.occurrenceStatus = occurrenceStatus;
     }
}
