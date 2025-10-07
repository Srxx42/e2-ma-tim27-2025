package com.example.e2taskly.presentation.adapter;

import android.content.Context;
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
import com.example.e2taskly.model.Task;
import com.example.e2taskly.model.enums.TaskStatus;
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
    private OnOccurrenceUpdateListener updateListener;

    private SharedPreferencesUtil sharedPreferencesUtil;

    public OccurrenceListAdapter(@NonNull Context context, @NonNull ArrayList<RepeatingTaskOccurrence> occurrences, OnOccurrenceUpdateListener listener) {
        super(context, 0, occurrences);
        rOccurrences = occurrences;

        taskService = new TaskService(context);
        userService = new UserService(context);

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


        completeButton.setOnClickListener( v ->{
            Task task = taskService.getTaskById(ocr.getRepeatingTaskId());
            if (task == null) return; // Zaštita


            String currentUserId = sharedPreferencesUtil.getActiveUserUid();
            XpCounterManager xpManager = new XpCounterManager(getContext(), currentUserId);

            int xpToAward = xpManager.calculateXpToAward(task);

            if (xpToAward > 0) {
                userService.setTaskService(taskService);
                userService.addXpToUser(task.getCreatorId(), xpToAward);
                xpManager.recordXpAward(task);
                Toast.makeText(getContext(), "Dodeljeno " + xpToAward + " XP poena!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Dostignut je limit za XP za ovu vrstu zadatka.", Toast.LENGTH_LONG).show();
            }

            ocr.setOccurrenceStatus(TaskStatus.COMPLETED);
            taskService.updateOccurrence(ocr);

            if (updateListener != null) {
                updateListener.onOccurrenceUpdated();
            }
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

}
