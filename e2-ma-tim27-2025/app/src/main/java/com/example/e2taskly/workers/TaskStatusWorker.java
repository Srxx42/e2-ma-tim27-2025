package com.example.e2taskly.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.e2taskly.model.RepeatingTask;
import com.example.e2taskly.model.RepeatingTaskOccurrence;
import com.example.e2taskly.model.SingleTask;
import com.example.e2taskly.model.enums.TaskStatus;
import com.example.e2taskly.service.TaskService;

import java.time.LocalDate;
import java.util.List;

public class TaskStatusWorker extends Worker {

    private final TaskService taskService;
    private static final String TAG = "TaskStatusWorker";

    public TaskStatusWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        taskService = new TaskService(getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Worker je pokrenut. Provera statusa taskova...");

        try {
            LocalDate threeDaysAgo = LocalDate.now().minusDays(3);

            List<SingleTask> allSingleTasks = taskService.getAllSingleTasksForAllUsers();
            for (SingleTask task : allSingleTasks) {

                if (task.getStatus() == TaskStatus.ACTIVE && task.getTaskDate().isBefore(threeDaysAgo)) {
                    task.setStatus(TaskStatus.FAILED);
                    taskService.updateTask(task);
                    Log.d(TAG, "SingleTask ID: " + task.getId() + " je postavljen na FAILED.");
                }
            }


            List<RepeatingTask> allRepeatingTasks = taskService.getAllRepeatingTasksForAllUsers();
            for (RepeatingTask rTask : allRepeatingTasks) {
                List<RepeatingTaskOccurrence> occurrences = taskService.getAllTaskOccurrences(rTask.getId());
                for (RepeatingTaskOccurrence occurrence : occurrences) {

                    if (occurrence.getOccurrenceStatus() == TaskStatus.ACTIVE && occurrence.getOccurrenceDate().isBefore(threeDaysAgo)) {
                        occurrence.setOccurrenceStatus(TaskStatus.FAILED);
                        taskService.updateOccurrence(occurrence);
                        Log.d(TAG, "Occurrence ID: " + occurrence.getId() + " za Task ID: " + rTask.getId() + " je postavljen na FAILED.");
                    }
                }
            }

            Log.d(TAG, "Worker je uspešno završio posao.");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Greška prilikom izvršavanja workera.", e);
            return Result.failure();
        }
    }
}