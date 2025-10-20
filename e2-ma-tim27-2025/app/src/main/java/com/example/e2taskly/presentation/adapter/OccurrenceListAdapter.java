package com.example.e2taskly.presentation.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.e2taskly.R;
import com.example.e2taskly.model.RepeatingTaskOccurrence;
import com.example.e2taskly.model.SpecialMissionProgress;
import com.example.e2taskly.model.Task;
import com.example.e2taskly.model.enums.Difficulty;
import com.example.e2taskly.model.enums.Importance;
import com.example.e2taskly.model.enums.ProgressType;
import com.example.e2taskly.model.enums.TaskStatus;
import com.example.e2taskly.service.MissionProgressService;
import com.example.e2taskly.service.TaskService;
import com.example.e2taskly.service.UserService;
import com.example.e2taskly.util.SharedPreferencesUtil;
import com.example.e2taskly.util.XpCounterManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class OccurrenceListAdapter extends ArrayAdapter<RepeatingTaskOccurrence> {

    private ArrayList<RepeatingTaskOccurrence> rOccurrences;

    private TaskService taskService;

    private UserService userService;
    private MissionProgressService progressService;
    private OnOccurrenceUpdateListener updateListener;

    private SharedPreferencesUtil sharedPreferencesUtil;

    public OccurrenceListAdapter(@NonNull Context context, @NonNull ArrayList<RepeatingTaskOccurrence> occurrences, OnOccurrenceUpdateListener listener) {
        super(context, 0, occurrences);
        rOccurrences = occurrences;

        taskService = new TaskService(context);
        userService = new UserService(context);
        progressService = new MissionProgressService(context);

        this.sharedPreferencesUtil = new SharedPreferencesUtil(context);
        this.updateListener = listener;
    }

    @Override
    public int getCount() {
        return rOccurrences.size();
    }

    @Nullable
    @Override
    public RepeatingTaskOccurrence getItem(int position) {
        return rOccurrences.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View converView, @NonNull ViewGroup parent) {
        RepeatingTaskOccurrence ocr = getItem(position);

        if (converView == null) {
            converView = LayoutInflater.from(getContext()).inflate(R.layout.item_occurrence,
                    parent, false);
        }

        TextView occurrenceDate = converView.findViewById(R.id.occurrenceDate);
        TextView occurrenceStatus = converView.findViewById(R.id.occurrenceStatus);
        ImageButton completeButton = converView.findViewById(R.id.completeButton);
        ImageButton cancelButton = converView.findViewById(R.id.cancelButton);


        boolean isStatusChangable = canStatusBeChanged(ocr);
        if(!isStatusChangable){

            completeButton.setEnabled(false);
            cancelButton.setEnabled(false);
            completeButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
        } else {
            completeButton.setEnabled(true);
            cancelButton.setEnabled(true);
            completeButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);

        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");


        if(ocr != null){
            occurrenceDate.setText(ocr.getOccurrenceDate().format(formatter));
            occurrenceStatus.setText(ocr.getOccurrenceStatus().toString());
        }


        completeButton.setOnClickListener( v -> {
            Task task = taskService.getTaskById(ocr.getRepeatingTaskId());
            if (task == null) {
                Toast.makeText(getContext(), "Pripadajući zadatak nije pronađen.", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentUserId = sharedPreferencesUtil.getActiveUserUid();
            XpCounterManager xpManager = new XpCounterManager(getContext(), currentUserId);

            Runnable occurrenceCompletionActions = () -> {
                ocr.setOccurrenceStatus(TaskStatus.COMPLETED);
                taskService.updateOccurrence(ocr);
                updateProgress(task, currentUserId);

                if (updateListener != null) {
                    updateListener.onOccurrenceUpdated();
                }
            };

            xpManager.awardXpForTask(task, occurrenceCompletionActions);
        });

        cancelButton.setOnClickListener( v ->{
            ocr.setOccurrenceStatus(TaskStatus.CANCELED);
            taskService.updateOccurrence(ocr);

            if (updateListener != null) {
                updateListener.onOccurrenceUpdated();
            }
        });
        return converView;
    }

    private boolean canStatusBeChanged(RepeatingTaskOccurrence ocr){
        LocalDate lastThreeDays = LocalDate.now().minusDays(3);

        if(ocr.getOccurrenceDate().isAfter(LocalDate.now())) return false;
        if(ocr.getOccurrenceDate().isBefore(lastThreeDays)) return false;
        if(!ocr.getOccurrenceStatus().equals(TaskStatus.ACTIVE)) return false;

        return true;
    }

    private void updateProgress(Task currentTask, String currentUserId){
        ProgressType importanceProgressType;
        if (currentTask.getImportance().equals(Importance.NORMAL) || currentTask.getImportance().equals(Importance.IMPORTANT)) {
            importanceProgressType = ProgressType.EASY_TASK;
        } else {
            importanceProgressType = ProgressType.HARD_TASK;
        }

        ProgressType difficultyProgressType;
        if (currentTask.getDifficulty().equals(Difficulty.EASY) || currentTask.getDifficulty().equals(Difficulty.NORMAL)) {
            difficultyProgressType = ProgressType.EASY_TASK;
        } else {
            difficultyProgressType = ProgressType.HARD_TASK;
        }

        progressService.updateMissionProgress(currentUserId, importanceProgressType)
                .continueWithTask(firstUpdateTask -> {
                    if (!firstUpdateTask.isSuccessful()) {
                        throw firstUpdateTask.getException();
                    }
                    return progressService.updateMissionProgress(currentUserId, difficultyProgressType);
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("TaskComplete", "Oba progresa za misiju su uspešno ažurirana.");
                    } else {
                        Log.e("TaskComplete", "Greška prilikom ažuriranja progresa misije.", task.getException());
                    }
                });

    }

}
