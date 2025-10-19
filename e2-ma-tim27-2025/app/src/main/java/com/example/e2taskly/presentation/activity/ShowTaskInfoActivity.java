package com.example.e2taskly.presentation.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e2taskly.R;
import com.example.e2taskly.model.enums.Difficulty;
import com.example.e2taskly.model.enums.Importance;
import com.example.e2taskly.model.enums.ProgressType;
import com.example.e2taskly.presentation.adapter.OccurrenceListAdapter;
import com.example.e2taskly.presentation.adapter.OnOccurrenceUpdateListener;
import com.example.e2taskly.model.RepeatingTask;
import com.example.e2taskly.model.RepeatingTaskOccurrence;
import com.example.e2taskly.model.SingleTask;
import com.example.e2taskly.model.Task;
import com.example.e2taskly.model.enums.RepeatingType;
import com.example.e2taskly.model.enums.TaskStatus;
import com.example.e2taskly.model.enums.TaskType;
import com.example.e2taskly.service.MissionProgressService;
import com.example.e2taskly.service.TaskService;
import com.example.e2taskly.service.UserService;
import com.example.e2taskly.util.SharedPreferencesUtil;
import com.example.e2taskly.util.XpCounterManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShowTaskInfoActivity extends AppCompatActivity implements OnOccurrenceUpdateListener {

    // UI Komponente
    private TextView tvTaskName, tvCategory, tvDifficulty, tvImportance,tvTotalXP, tvDescription, tvStatus;
    private TextView tvSingleDate, tvStartDate, tvEndDate, tvRepeatInfo;
    private ImageButton btnEditTask, btnDeleteTask;
    private Button btnComplete, btnPause, btnCancel;
    private LinearLayout layoutSingleInfo, layoutRepeatingInfo;
    private TaskService taskService;
    private MissionProgressService progressService;
    private UserService userService;
    private Task currentTask;
    private ListView rTaskOccurrencesListView;
    private OccurrenceListAdapter occurrencesListAdapter;
    private SharedPreferencesUtil sharedPreferencesUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_info);

        taskService = new TaskService(this);
        userService = new UserService(this);
        progressService = new MissionProgressService(this);

        sharedPreferencesUtil = new SharedPreferencesUtil(this);

        initViews();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("TASK_ID")) {
            int taskId = intent.getIntExtra("TASK_ID", -1);

            if (taskId != -1) {
                currentTask = taskService.getTaskById(taskId);

                if (currentTask != null) {
                    populateUI();
                    setupListeners();
                } else {
                    Toast.makeText(this, "Task not found!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @Override
    protected  void onResume(){
        super.onResume();

        if (currentTask != null) {
            Task updatedTask = taskService.getTaskById(currentTask.getId());
            if (updatedTask != null) {
                currentTask = updatedTask;
                populateUI();

            } else {
                Toast.makeText(this, "Task is no longer available.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    private void initViews() {

        tvTaskName = findViewById(R.id.tvTaskName);
        tvCategory = findViewById(R.id.tvCategory);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvImportance = findViewById(R.id.tvImportance);
        tvTotalXP = findViewById(R.id.tvTotalXP);
        tvDescription = findViewById(R.id.tvDescription);
        tvStatus = findViewById(R.id.tvStatus);


        layoutSingleInfo = findViewById(R.id.layoutSingleInfo);
        tvSingleDate = findViewById(R.id.tvSingleDate);
        layoutRepeatingInfo = findViewById(R.id.layoutRepeatingInfo);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvRepeatInfo = findViewById(R.id.tvRepeatInfo);
        rTaskOccurrencesListView = findViewById(R.id.occurrencesListView);



        btnEditTask = findViewById(R.id.btnEditTask);
        btnDeleteTask = findViewById(R.id.btnDeleteTask);
        btnComplete = findViewById(R.id.btnComplete);
        btnPause = findViewById(R.id.btnPause);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void populateUI() {

        tvTaskName.setText(currentTask.getName());
        tvDescription.setText(currentTask.getDescription());

        if (currentTask.getCategory() != null) {
            tvCategory.setText(currentTask.getCategory().getName());
        }
        tvDifficulty.setText(currentTask.getDifficulty().toString());
        tvImportance.setText(currentTask.getImportance().toString());
        tvTotalXP.setText(currentTask.getValueXP() + "XP");

        // Podesavanje boje statusa
        updateStatusUI(currentTask.getStatus());


        // Provera tipa taska i prikazivanje odgovarajuceg layouta
        if (currentTask instanceof SingleTask) {

            layoutSingleInfo.setVisibility(View.VISIBLE);
            layoutRepeatingInfo.setVisibility(View.GONE);

            SingleTask singleTask = (SingleTask) currentTask;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
            tvSingleDate.setText(singleTask.getTaskDate().format(formatter));

        } else if (currentTask instanceof RepeatingTask) {

            layoutSingleInfo.setVisibility(View.GONE);
            layoutRepeatingInfo.setVisibility(View.VISIBLE);

            RepeatingTask repeatingTask = (RepeatingTask) currentTask;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
            tvStartDate.setText(repeatingTask.getStartingDate().format(formatter));
            tvEndDate.setText(repeatingTask.getFinishingDate().format(formatter));

            // Kreiranje lepog ispisa za ponavljanje
            String type = repeatingTask.getRepeatingType() == RepeatingType.DAILY ? "day" : "week";
            if (repeatingTask.getInterval() > 1) {
                type += "s"; // Mnozina (days, weeks)
            }
            String repeatText = String.format("Every %d %s", repeatingTask.getInterval(), type);
            tvRepeatInfo.setText(repeatText);

            List<RepeatingTaskOccurrence> occurrences = taskService.getAllTaskOccurrences(repeatingTask.getId());

            occurrencesListAdapter = new OccurrenceListAdapter(this,new ArrayList<>(occurrences),this);
            rTaskOccurrencesListView.setAdapter(occurrencesListAdapter);
        }

        setupButtonsVisibility();

    }

    private void setupListeners() {

        btnEditTask.setOnClickListener(v -> {
            Toast.makeText(this, "Edit clicked", Toast.LENGTH_SHORT).show();

             Intent editIntent = new Intent(this, ManageTaskActivity.class);
             editIntent.putExtra("TASK_ID", currentTask.getId());
             startActivity(editIntent);
        });

        btnDeleteTask.setOnClickListener(v -> {
            Toast.makeText(this, "Delete clicked", Toast.LENGTH_SHORT).show();
            if(currentTask.getStatus().equals(TaskStatus.ACTIVE) || currentTask.getStatus().equals(TaskStatus.PAUSED)) {

                try {
                    if (currentTask.getType().equals(TaskType.SINGLE)) {
                        taskService.deleteById(currentTask.getId());
                        finish();
                    } else {
                        RepeatingTask rTask = (RepeatingTask) currentTask;

                        if (rTask.getStartingDate().isAfter(LocalDate.now())) {
                            taskService.deleteById(currentTask.getId());
                            finish();
                        } else {
                            List<RepeatingTaskOccurrence> occurrences = taskService.getAllTaskOccurrences(currentTask.getId());
                            occurrences.removeIf(o -> o.getOccurrenceDate().isAfter(LocalDate.now()));
                            //occurrences.removeIf(o -> o.getOccurrenceStatus().equals(TaskStatus.ACTIVE));
                            rTask.setOccurrences(occurrences);
                            rTask.setStatus(TaskStatus.DELETED);
                            rTask.setDeleted(true);
                            taskService.deleteFutureOccurrences(currentTask.getId());
                            taskService.updateTask(rTask);
                            finish();
                        }
                    }
                } catch (Exception e){
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace(); // Za debagovanje
                }

            }

        });

        btnComplete.setOnClickListener(v -> {
            Toast.makeText(this, "Complete clicked", Toast.LENGTH_SHORT).show();
            if(currentTask.getType().equals(TaskType.SINGLE)) {

                String currentUserId = sharedPreferencesUtil.getActiveUserUid();
                XpCounterManager xpManager = new XpCounterManager(this, currentUserId);

                int xpToAward = xpManager.calculateXpToAward(currentTask);

                if (xpToAward > 0) {
                    userService.setTaskService(taskService);
                    userService.addXpToUser(currentTask.getCreatorId(), xpToAward);
                    xpManager.recordXpAward(currentTask); // Zabeleži da je XP dodeljen
                    Toast.makeText(this, "Dodeljeno " + xpToAward + " XP poena!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Dostignut je limit za XP za ovu vrstu zadatka.", Toast.LENGTH_LONG).show();
                }
                currentTask.setStatus(TaskStatus.COMPLETED);
                taskService.updateTask(currentTask);

                updateProgress(currentTask,currentUserId);

                updateStatusUI(TaskStatus.COMPLETED);
                setupButtonsVisibility();
            }
        });

        btnPause.setOnClickListener(v -> {
            if (currentTask.getStatus().equals(TaskStatus.PAUSED)) {
                currentTask.setStatus(TaskStatus.ACTIVE);
                taskService.updateTask(currentTask);

                if(currentTask.getType().equals(TaskType.REPEATING)) {
                    taskService.unpauseAllOccurrences(currentTask.getId());
                }

                updateStatusUI(TaskStatus.ACTIVE);
                setupButtonsVisibility();
                populateUI();
                Toast.makeText(this, "Task Unpaused", Toast.LENGTH_SHORT).show();

            } else if (currentTask.getStatus().equals(TaskStatus.ACTIVE)) {
                currentTask.setStatus(TaskStatus.PAUSED);
                taskService.updateTask(currentTask);
                if(currentTask.getType().equals(TaskType.REPEATING)) {
                    taskService.pauseAllOccurrences(currentTask.getId());
                }
                updateStatusUI(TaskStatus.PAUSED);
                setupButtonsVisibility();
                populateUI();
                Toast.makeText(this, "Task Paused", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> {
            Toast.makeText(this, "Cancel clicked", Toast.LENGTH_SHORT).show();
            if(currentTask.getType().equals(TaskType.SINGLE)) {
                currentTask.setStatus(TaskStatus.CANCELED);
                taskService.updateTask(currentTask);
                updateStatusUI(TaskStatus.CANCELED);
                setupButtonsVisibility();
            }
        });
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
                        Toast.makeText(this, "Greška prilikom ažuriranja progresa misije.", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    /**
     * Helper metoda za azuriranje izgleda statusa (tekst i boja pozadine).
     * Boja se postavlja direktno preko heksadecimalnog koda.
     * @param status Trenutni status taska
     */
    private void updateStatusUI(TaskStatus status) {
        tvStatus.setText(status.toString());
        int backgroundColor;

        switch (status) {
            case COMPLETED:
                // Zelena boja
                backgroundColor = Color.parseColor("#4CAF50");
                break;
            case FAILED:
                // Crvena boja
                backgroundColor = Color.parseColor("#F44336");
                break;
            case PAUSED:
                // Zuta boja
                backgroundColor = Color.parseColor("#FFEB3B");
                break;
            case CANCELED:
                // Narandzasta boja
                backgroundColor = Color.parseColor("#FF9800");
                break;
            case DELETED:
                backgroundColor = Color.parseColor("#FF0000");
                break;
            case ACTIVE:
            default:
                // Plava boja
                backgroundColor = Color.parseColor("#2196F3");
                break;
        }

        tvStatus.setBackgroundColor(backgroundColor);
    }

    public void setupButtonsVisibility() {
        if (currentTask == null || currentTask.getStatus() == null) {
            return;
        }

        switch (currentTask.getStatus()) {
            case FAILED:
            case CANCELED:
            case DELETED:
            case COMPLETED:
                btnEditTask.setVisibility(View.GONE);
                btnDeleteTask.setVisibility(View.GONE);
                btnComplete.setVisibility(View.GONE);
                btnPause.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                break;

            case ACTIVE:
                if(currentTask.getType().equals(TaskType.REPEATING)){
                    btnEditTask.setVisibility(View.VISIBLE);
                    btnDeleteTask.setVisibility(View.VISIBLE);
                    btnComplete.setVisibility(View.GONE);
                    btnPause.setVisibility(View.VISIBLE);
                    btnCancel.setVisibility(View.GONE);
                }else{
                btnEditTask.setVisibility(View.VISIBLE);
                btnDeleteTask.setVisibility(View.VISIBLE);
                btnComplete.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                }

                btnPause.setText("Pause");
                break;

            case PAUSED:
                btnEditTask.setVisibility(View.VISIBLE);
                btnDeleteTask.setVisibility(View.VISIBLE);

                btnComplete.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);

                btnPause.setVisibility(View.VISIBLE);
                btnPause.setText("Unpause");
                break;

            default:
                btnEditTask.setVisibility(View.GONE);
                btnDeleteTask.setVisibility(View.GONE);
                btnComplete.setVisibility(View.GONE);
                btnPause.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onOccurrenceUpdated() {

        int taskId = currentTask.getId();
        currentTask = taskService.getTaskById(taskId);

        populateUI();

        Log.d("TaskUpdate", "UI je osvežen nakon promene occurrence-a.");
    }
}