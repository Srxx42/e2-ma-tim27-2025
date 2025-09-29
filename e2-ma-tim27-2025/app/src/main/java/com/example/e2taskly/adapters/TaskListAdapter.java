package com.example.e2taskly.adapters;

import static androidx.core.content.ContextCompat.getDrawable;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.e2taskly.R;
import com.example.e2taskly.model.RepeatingTask;
import com.example.e2taskly.model.SingleTask;
import com.example.e2taskly.model.Task;
import com.example.e2taskly.model.TaskCategory;
import com.example.e2taskly.model.enums.TaskType;
import com.example.e2taskly.presentation.activity.ManageCategoryActivity;
import com.example.e2taskly.service.TaskService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TaskListAdapter extends ArrayAdapter<Task> {

    private ArrayList<Task> aTasks;

    private TaskService taskService;

    public TaskListAdapter(@NonNull Context context, @NonNull ArrayList<Task> tasks){
        super(context, R.layout.item_task,tasks);
        aTasks = tasks;

        taskService = new TaskService(context);
    }

    @Override
    public int getCount() {
        return aTasks.size();
    }

    @Nullable
    @Override
    public Task getItem(int position) {
        return aTasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View converView, @NonNull ViewGroup parent) {
        Task task = getItem(position);

        if (converView == null) {
            converView = LayoutInflater.from(getContext()).inflate(R.layout.item_task,
                    parent, false);
        }

        LinearLayout taskCard = converView.findViewById(R.id.taskItem);
        TextView taskName = converView.findViewById(R.id.taskName);
        View taskCategoryColor = converView.findViewById(R.id.taskColorView);
        TextView taskCategoryName = converView.findViewById(R.id.taskCategoryName);
        TextView taskStatus = converView.findViewById(R.id.taskStatus);
        TextView taskDates = converView.findViewById(R.id.taskDate);

        if(task != null){
            taskName.setText(task.getName());

            GradientDrawable drawable = (GradientDrawable) getDrawable(getContext(), R.drawable.colored_circles);
            drawable = (GradientDrawable) drawable.mutate();
            drawable.setColor(Color.parseColor(task.getCategory().getColorHex()));
            taskCategoryColor.setBackground(drawable);

            taskCategoryName.setText(task.getCategory().getName());
            taskStatus.setText(task.getStatus().toString().trim());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            if(task.getType().equals(TaskType.SINGLE)){
                SingleTask sTask = (SingleTask) task;
                taskDates.setText(sTask.getTaskDate().format(formatter));
            } else {
                RepeatingTask rTask = (RepeatingTask) task;
                String rTaskDates = rTask.getStartingDate().format(formatter)+ " - " + rTask.getFinishingDate().format(formatter);
                taskDates.setText(rTaskDates);
            }

        }
       /* KOD ZA PRIKAZ TASKA

       taskCard.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ManageCategoryActivity.class);
            intent.putExtra("CATEGORY_ID",task.getId());
            getContext().startActivity(intent);

        }); */


        return converView;
    }

}
