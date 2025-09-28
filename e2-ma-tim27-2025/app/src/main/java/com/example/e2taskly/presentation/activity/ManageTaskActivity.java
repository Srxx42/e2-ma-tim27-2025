package com.example.e2taskly.presentation.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.e2taskly.R;
import com.example.e2taskly.adapters.CategoryListAdapter;
import com.example.e2taskly.model.RepeatingTask;
import com.example.e2taskly.model.SingleTask;
import com.example.e2taskly.model.Task;
import com.example.e2taskly.model.TaskCategory;
import com.example.e2taskly.model.enums.Difficulty;
import com.example.e2taskly.model.enums.Importance;
import com.example.e2taskly.model.enums.RepeatingType;
import com.example.e2taskly.model.enums.TaskStatus;
import com.example.e2taskly.model.enums.TaskType;
import com.example.e2taskly.service.TaskCategoryService;
import com.example.e2taskly.service.TaskService;
import com.example.e2taskly.service.UserService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.collect.ArrayTable;
import com.google.type.DateTime;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ManageTaskActivity extends AppCompatActivity {

    private TextInputEditText editTaskName,editTaskDescription;
    private Spinner categorySpinner;
    private RadioGroup radioGroupDifficulty, radioGroupImportance;
    private Button btnSingle, btnRepeating;

    //Forma za Single task
    private LinearLayout layoutSingle;
    private DatePicker singleDatePicker;
    private Button saveSingleTask;

    //Forma za Repeating task
    private LinearLayout layoutRepeating;
    private Spinner repeatIntervalSpinner;

    private TextInputEditText editStartDate,editEndDate;
    private RadioGroup radioGroupRepeatInterval;
    private Button saveRepeatingTask;

    //Servic-i i jos neophodnih stvari

    private TaskType currentTaskType = TaskType.SINGLE;

    private UserService userService;
    private TaskCategoryService  categoryService;
    private TaskService taskService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        userService = new UserService(this);
        categoryService = new TaskCategoryService(this);
        taskService = new TaskService(this);
        setContentView(R.layout.activity_task_manage);

        initViews();
        setupSpinners();
        initListeners();

        updateFormVisibility();
    }

    private void initViews(){
        //Inicijalizacija zejdnicih polja za obe vrste taska
        editTaskName = findViewById(R.id.editTaskName);
        editTaskDescription = findViewById(R.id.editTasksDescription);
        categorySpinner = findViewById(R.id.categorySpinner);
        radioGroupDifficulty = findViewById(R.id.radioGroupDifficulty);
        radioGroupImportance = findViewById(R.id.radioGroupImportance);
        btnSingle = findViewById(R.id.btnSingle);
        btnRepeating = findViewById(R.id.btnRepeating);

        //Single Task polja
        layoutSingle = findViewById(R.id.layoutSingle);
        singleDatePicker = findViewById(R.id.singleDatePicker);
        saveSingleTask = findViewById(R.id.saveSingleTask);

        //Repeating Task polja
        layoutRepeating = findViewById(R.id.layoutRepeating);
        editStartDate = findViewById(R.id.editStartDate);
        editEndDate = findViewById(R.id.editEndDate);
        repeatIntervalSpinner = findViewById(R.id.repeatIntervalSpinner);
        radioGroupRepeatInterval = findViewById(R.id.radioGroupRepeatInterval);
        saveRepeatingTask = findViewById(R.id.saveRepeatingTask);

    }

    private void setupSpinners(){
        //Spinner za postojece kategorije
        setupCategorySpinner();

        List<Integer> intervalRange = IntStream.rangeClosed(1,30).boxed().collect(Collectors.toList());
        ArrayAdapter<Integer> intervalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,intervalRange);

        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatIntervalSpinner.setAdapter(intervalAdapter);
    }

    private void setupCategorySpinner(){
        List<TaskCategory> categories = new ArrayList<>();

        categories = categoryService.getAllCategories();

        CategorySpinnerAdapter adapter = new CategorySpinnerAdapter(this, categories);
        categorySpinner.setAdapter(adapter);
    }

    private void initListeners(){
        btnSingle.setOnClickListener(v -> {
            currentTaskType = TaskType.SINGLE;
            updateFormVisibility();
        });

        btnRepeating.setOnClickListener(v ->{
            currentTaskType = TaskType.REPEATING;
            updateFormVisibility();
        });

        saveSingleTask.setOnClickListener(v -> validateAndSaveTask());
        saveRepeatingTask.setOnClickListener(v -> validateAndSaveTask());

        editStartDate.setOnClickListener(v -> showDatePickerDialog(editStartDate));
        editEndDate.setOnClickListener(v -> showDatePickerDialog(editEndDate));
    }

    private void updateFormVisibility(){
        if(currentTaskType == TaskType.SINGLE){
            layoutSingle.setVisibility(View.VISIBLE);
            layoutRepeating.setVisibility(View.GONE);
        } else {
            layoutSingle.setVisibility(View.GONE);
            layoutRepeating.setVisibility(View.VISIBLE);
        }
    }

    private void validateAndSaveTask(){
        //Validacija zajednicih polja
        boolean success = false;
        String name = editTaskName.getText().toString().trim();
        if(TextUtils.isEmpty(name)){
            editTaskName.setError("Naziv zadatka je obavezan.");
            editTaskName.requestFocus();
            return;
        }

        if(categorySpinner.getSelectedItem() == null){
            Toast.makeText(this,"Morate izabrati kategoriju.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(radioGroupDifficulty.getCheckedRadioButtonId() == -1){
            Toast.makeText(this, "Morate izabrati tezinu.", Toast.LENGTH_SHORT).show();
        }

        if(radioGroupImportance.getCheckedRadioButtonId() == -1){
            Toast.makeText(this,"Morate izabrati vaznost.",Toast.LENGTH_SHORT).show();
        }
        //Prikupljanje zajednicih polja
        String description = editTaskDescription.getText().toString().trim();
        TaskCategory category = (TaskCategory) categorySpinner.getSelectedItem();
        Difficulty difficulty = getSelectedDifficulty();
        Importance importance = getSelectedImportance();

        //Podrazumevane vrednosti za novi task
        int id = -1;
        String creatorId = userService.getCurrentUserId();
        TaskStatus status = TaskStatus.ACTIVE;
        int valueXP = caluclateTaskXP(difficulty,importance);
        boolean deleted = false;

        //Krairanje task objekta - SINGLE
        if(currentTaskType == TaskType.SINGLE){
            int day = singleDatePicker.getDayOfMonth();
            int month = singleDatePicker.getMonth() + 1;
            int year = singleDatePicker.getYear();
            LocalDate taskDate = LocalDate.of(year,month,day);

            SingleTask task = new SingleTask(id,creatorId,name,description,category,TaskType.SINGLE,status,importance,difficulty,valueXP,deleted,taskDate);
           success = taskService.saveTask(task);
        }
        //Kreiranje task objekta - REPEATING
        else{
            String startDateString = editStartDate.getText().toString().trim();
            String endDateString = editEndDate.getText().toString().trim();

            if (TextUtils.isEmpty(startDateString)) {
                editStartDate.setError("Datum početka je obavezan.");
                editStartDate.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(endDateString)) {
                editEndDate.setError("Datum završetka je obavezan.");
                editEndDate.requestFocus();
                return;
            }

            LocalDate startDate, endDate;
            try{
                startDate = LocalDate.parse(startDateString, dateFormatter);
                endDate = LocalDate.parse(endDateString, dateFormatter);
            } catch(DateTimeException e){
                Toast.makeText(this, "Neispravan format datuma. Koristite DD/MM/YYYY.", Toast.LENGTH_LONG).show();
                Log.e("ManageTaskActivity", "Greška pri parsiranju datuma: " + e.getMessage());
                return;
            }
            if(endDate.isBefore(startDate)){
                Toast.makeText(this, "Datum završetka ne može biti pre datuma početka.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (radioGroupRepeatInterval.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Morate izabrati tip ponavljanja.", Toast.LENGTH_SHORT).show();
                return;
            }

            RepeatingType repeatingType = getSelectedRepeatingType();

            int interval = (int) repeatIntervalSpinner.getSelectedItem();
            RepeatingTask task = new RepeatingTask(id, creatorId, name, description, category, TaskType.REPEATING,
                    status, importance, difficulty, valueXP, deleted, repeatingType, interval, startDate, endDate);

           success = taskService.saveTask(task);
           if(success) taskService.createRepeatingTaskOccurrences(task);
        }

        if (success) {
            Toast.makeText(this, "Task saved!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error occured while saving task.", Toast.LENGTH_SHORT).show();
        }
    }



    private Difficulty getSelectedDifficulty() {
        int selectedId = radioGroupDifficulty.getCheckedRadioButtonId();
        if (selectedId == R.id.Easy) return Difficulty.EASY;
        if (selectedId == R.id.Normal) return Difficulty.NORMAL;
        if (selectedId == R.id.Hard) return Difficulty.HARD;
        if (selectedId == R.id.VeryHard) return Difficulty.EPIC; // Promenjeno VeryHard u EPIC
        return null;
    }

    private Importance getSelectedImportance() {
        int selectedId = radioGroupImportance.getCheckedRadioButtonId();
        if (selectedId == R.id.ImportanceNormal) return Importance.NORMAL;
        if (selectedId == R.id.ImportanceImportant) return Importance.IMPORTANT;
        if (selectedId == R.id.ImportanceUrgent) return Importance.URGENT;
        if (selectedId == R.id.ImportanceSpecial) return Importance.SPECIAL;
        return null;
    }

    private int caluclateTaskXP(Difficulty difficulty,Importance importance){
        int xp = 0;
        switch (difficulty){
            case EASY:
                xp = 1;
                break;
            case NORMAL:
                xp = 3;
                break;
            case HARD:
                xp = 7;
                break;
            case EPIC:
                xp = 20;
                break;
        }
        switch (importance){
            case NORMAL:
                xp += 1;
                break;
            case IMPORTANT:
                xp += 3;
                break;
            case URGENT:
                xp += 10;
                break;
            case SPECIAL:
                xp += 100;
                break;
        }
        return xp;
    }

    private RepeatingType getSelectedRepeatingType() {
        // Koristimo ispravljen ID radio grupe
        int selectedId = radioGroupRepeatInterval.getCheckedRadioButtonId();
        if (selectedId == R.id.repeatDaily) return RepeatingType.DAILY;
        if (selectedId == R.id.repeatWeekly) return RepeatingType.WEEKLY;
        return null;
    }

    // Helper metoda za prikaz DatePickerDialog-a
    private void showDatePickerDialog(final TextInputEditText dateField) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Ako polje već ima datum, prikaži taj datum u dijalogu
        String existingDate = dateField.getText().toString();
        if (!TextUtils.isEmpty(existingDate)) {
            try {
                LocalDate parsedDate = LocalDate.parse(existingDate, dateFormatter);
                year = parsedDate.getYear();
                month = parsedDate.getMonthValue() - 1; // Meseci u Calendar su 0-indeksirani
                day = parsedDate.getDayOfMonth();
            } catch (DateTimeParseException e) {
                Log.e("ManageTaskActivity", "Ne mogu parsirati postojeći datum iz polja: " + existingDate);
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    // Meseci su 0-indeksirani u DatePickerDialog, pa dodajemo 1
                    LocalDate selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDayOfMonth);
                    dateField.setText(selectedDate.format(dateFormatter));
                },
                year,
                month,
                day
        );
        datePickerDialog.show();
    }

    public class CategorySpinnerAdapter extends ArrayAdapter<TaskCategory> {
        private Context context;
        private List<TaskCategory> categories;

        public CategorySpinnerAdapter(Context context, List<TaskCategory> categories) {
            super(context, R.layout.category_item, categories);
            this.context = context;
            this.categories = categories;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }
        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        private View getCustomView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                row = getLayoutInflater().inflate(R.layout.category_item, parent, false);
            }

            TaskCategory currentItem = categories.get(position);

            View colorView = row.findViewById(R.id.colorView);
            TextView nameView = row.findViewById(R.id.categoryName);

            ImageButton deleteButtonInRow = row.findViewById(R.id.deleteButton);
            if (deleteButtonInRow != null) {
                deleteButtonInRow.setVisibility(View.GONE);
            }

            nameView.setText(currentItem.getName());
            colorView.setBackgroundColor(Color.parseColor(currentItem.getColorHex()));

            return row;
        }
    }
}
