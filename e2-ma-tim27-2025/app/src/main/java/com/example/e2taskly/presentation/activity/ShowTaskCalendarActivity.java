package com.example.e2taskly.presentation.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.e2taskly.R;
import com.example.e2taskly.adapters.TaskListAdapter;
import com.example.e2taskly.decorator.EventDecorator;
import com.example.e2taskly.decorator.MultiLineSpan;
import com.example.e2taskly.model.RepeatingTask;
import com.example.e2taskly.model.RepeatingTaskOccurrence;
import com.example.e2taskly.model.SingleTask;
import com.example.e2taskly.model.Task;
import com.example.e2taskly.service.TaskService;
import com.example.e2taskly.service.UserService;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ShowTaskCalendarActivity extends AppCompatActivity {

    private TaskService taskService;
    private ArrayList<Task> currentlyDisplayedTasks;
    private TaskListAdapter taskListAdapter;

    private Map<LocalDate, List<Task>> tasksByDateMap;
    private ListView tasksCalendarListView;
    private MaterialCalendarView taskCalendarView;



    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        taskService = new TaskService(this);

        setContentView(R.layout.activity_task_calendar_show);

        Toolbar toolbar =findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);

        initViews();
        loadAndProcessTasks();
        setupCalendarListener();

        updateTaskListForDate(LocalDate.now());
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadAndProcessTasks();

        CalendarDay selectedDay = taskCalendarView.getSelectedDate();
        LocalDate dateToDisplay = (selectedDay != null) ?
                LocalDate.of(selectedDay.getYear(), selectedDay.getMonth() + 1, selectedDay.getDay()) :
                LocalDate.now(); // Ako nista nije selektovano, prikazi za danas

        updateTaskListForDate(dateToDisplay);
    }

    private void initViews(){

        tasksCalendarListView = findViewById(R.id.tasksCalendarListView);
        taskCalendarView = findViewById(R.id.taskCalendarView);

        currentlyDisplayedTasks = new ArrayList<>();

        taskListAdapter = new TaskListAdapter(this, new ArrayList<>(currentlyDisplayedTasks),false);
        tasksCalendarListView.setAdapter(taskListAdapter);
    }

    private void loadAndProcessTasks(){

        taskCalendarView.removeDecorators();

        tasksByDateMap = new HashMap<>();

        List<SingleTask> sTasks = taskService.getAllSingleTasks();

        for (SingleTask sTask : sTasks) {
            LocalDate date = sTask.getTaskDate();

            tasksByDateMap.computeIfAbsent(date, k -> new ArrayList<>()).add(sTask);
        }

        List<RepeatingTask> rTasks = taskService.getAllRepeatingTasks();
        for (RepeatingTask rTask : rTasks) {
                rTask.setOccurrences(taskService.getAllTaskOccurrences(rTask.getId()));
            for (RepeatingTaskOccurrence occurrence : rTask.getOccurrences()) {
                LocalDate date = occurrence.getOccurrenceDate();

                tasksByDateMap.computeIfAbsent(date, k -> new ArrayList<>()).add(rTask);
            }
        }

        //Crtanmje boja u kalendaru
        Map<CalendarDay, HashSet<String>> colorsByDate = new HashMap<>();

        // Prolazimo kroz našu postojeću mapu zadataka po datumu
        for (Map.Entry<LocalDate, List<Task>> entry : tasksByDateMap.entrySet()) {
            LocalDate localDate = entry.getKey();
            List<Task> tasksOnDay = entry.getValue();

            // Konvertujemo LocalDate u CalendarDay koji biblioteka koristi
            CalendarDay calendarDay = CalendarDay.from(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());

            // Za svaki zadatak na taj dan, uzimamo boju i dodajemo je u set za taj CalendarDay
            for (Task task : tasksOnDay) {
                colorsByDate.computeIfAbsent(calendarDay, k -> new HashSet<>()).add(task.getCategory().getColorHex());
            }
        }

        // Prolazimo kroz mapu koja sadrži boje za svaki dan
        for (Map.Entry<CalendarDay, HashSet<String>> entry : colorsByDate.entrySet()) {
            CalendarDay day = entry.getKey();
            HashSet<String> colorHexSet = entry.getValue();

            // 1. Konvertujemo set Hex stringova u listu integera
            List<Integer> colorInts = new ArrayList<>();
            for (String hex : colorHexSet) {
                colorInts.add(Color.parseColor(hex));
            }

            // Log provera
            Log.d("DECORATOR_DEBUG", "Za dan " + day.toString() + " kreiram MultiLineSpan sa " + colorInts.size() + " boja.");

            // 2. Kreiramo JEDAN MultiLineSpan koji sadrži SVE boje za taj dan // MultiLineSpan(listaBoja, padding, DEBLJINA_LINIJE, razmakIzmeđuLinija)
            MultiLineSpan multiLineSpan = new MultiLineSpan(colorInts, 0.2f, 5f, 2f);

            // 3. Kreiramo set datuma koji sadrži samo OVAJ JEDAN dan
            HashSet<CalendarDay> singleDaySet = new HashSet<>();
            singleDaySet.add(day);

            // 4. Kreiramo JEDAN dekorater za taj dan i dodajemo ga na kalendar
            taskCalendarView.addDecorator(new EventDecorator(singleDaySet, multiLineSpan));
        }


    }

    private void setupCalendarListener() {
        taskCalendarView.setOnDateChangedListener((widget, date, selected) -> {
            // 'date' je tipa CalendarDay, konvertujemo ga u LocalDate
            LocalDate selectedDate = LocalDate.of(date.getYear(), date.getMonth() + 1, date.getDay());
            updateTaskListForDate(selectedDate);
        });
    }

    private void updateTaskListForDate(LocalDate date) {
        List<Task> tasksForSelectedDate = tasksByDateMap.get(date);

        taskListAdapter.clear();

        if (tasksForSelectedDate != null) {
            taskListAdapter.addAll(tasksForSelectedDate);
        }

        taskListAdapter.notifyDataSetChanged();
    }



}
