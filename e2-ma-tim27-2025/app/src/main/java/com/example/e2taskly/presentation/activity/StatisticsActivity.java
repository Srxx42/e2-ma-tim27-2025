package com.example.e2taskly.presentation.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.e2taskly.R;
import com.example.e2taskly.model.User;
import com.example.e2taskly.service.StatisticsService;
import com.example.e2taskly.service.UserService;
import com.example.e2taskly.util.SharedPreferencesUtil;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsActivity extends AppCompatActivity {

    private StatisticsService statisticsService;
    private UserService userService;
    private SharedPreferencesUtil sharedPreferences;
    private String currentUserId;

    private TextView textViewActiveDays, textViewLongestStreak;
    private PieChart chartTaskStatus;
    private BarChart chartTasksByCategory;
    private LineChart chartAverageDifficulty, chartXpLast7Days;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPreferences = new SharedPreferencesUtil(this);
        currentUserId = sharedPreferences.getActiveUserUid();
        statisticsService = new StatisticsService(this);
        userService = new UserService(this);

        setupViews();
        loadAllStatistics();
    }

    private void setupViews() {
        textViewActiveDays = findViewById(R.id.textViewActiveDays);
        textViewLongestStreak = findViewById(R.id.textViewLongestStreak);
        chartTaskStatus = findViewById(R.id.chartTaskStatus);
        chartTasksByCategory = findViewById(R.id.chartTasksByCategory);
        chartAverageDifficulty = findViewById(R.id.chartAverageDifficulty);
        chartXpLast7Days = findViewById(R.id.chartXpLast7Days);
    }

    private void loadAllStatistics() {
        executorService.execute(() -> {
            final Map<String, Integer> statusCounts = statisticsService.getTaskStatusCounts(currentUserId);
            final int longestStreak = statisticsService.getLongestTaskStreak(currentUserId);
            final Map<String, Integer> categoryCounts = statisticsService.getCompletedTasksCountByCategory(currentUserId);
            final Map<String, Double> avgDifficulty = statisticsService.getAverageDifficultyXpOverTime(currentUserId);
            final Map<String, Integer> weeklyXp = statisticsService.getXpEarnedLast7Days(currentUserId);

            runOnUiThread(() -> {
                textViewLongestStreak.setText(longestStreak + " days");
                setupDonutChart(statusCounts);
                setupBarChart(categoryCounts);
                setupAvgDifficultyChart(avgDifficulty);
                setupWeeklyXpChart(weeklyXp);
            });

            userService.getUserProfile(currentUserId).addOnSuccessListener(user -> {
                if (user != null) {
                    textViewActiveDays.setText(user.getActiveDaysStreak() + " days");
                }
            });
        });
    }

    private void setupDonutChart(Map<String, Integer> data) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(data.getOrDefault("completed", 0), "Completed"));
        entries.add(new PieEntry(data.getOrDefault("not_completed", 0), "Unfinished"));
        entries.add(new PieEntry(data.getOrDefault("canceled", 0), "Canceled"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        chartTaskStatus.setData(pieData);
        chartTaskStatus.setHoleRadius(58f);
        chartTaskStatus.setTransparentCircleRadius(61f);
        chartTaskStatus.setCenterText("Created:\n" + data.getOrDefault("created", 0));
        chartTaskStatus.setDrawEntryLabels(false);
        chartTaskStatus.getDescription().setEnabled(false);
        chartTaskStatus.getLegend().setEnabled(true);
        chartTaskStatus.getLegend().setTextColor(Color.WHITE);
        chartTaskStatus.invalidate();
    }

    private void setupBarChart(Map<String, Integer> data) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            entries.add(new BarEntry(i, entry.getValue()));
            labels.add(entry.getKey());
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Number of tasks");
        dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        dataSet.setValueTextColor(Color.WHITE);

        BarData barData = new BarData(dataSet);
        chartTasksByCategory.setData(barData);
        chartTasksByCategory.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartTasksByCategory.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chartTasksByCategory.getXAxis().setGranularity(1f);
        chartTasksByCategory.getXAxis().setGranularityEnabled(true);
        chartTasksByCategory.getDescription().setEnabled(false);
        chartTasksByCategory.getXAxis().setTextColor(Color.WHITE);
        chartTasksByCategory.getAxisLeft().setTextColor(Color.WHITE);
        chartTasksByCategory.getAxisRight().setTextColor(Color.WHITE);
        chartTasksByCategory.getLegend().setTextColor(Color.WHITE);
        chartTasksByCategory.invalidate();
    }

    private void setupAvgDifficultyChart(Map<String, Double> data) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(data.entrySet());
        Collections.sort(sortedEntries, Comparator.comparing(Map.Entry::getKey));
        int i = 0;
        for (Map.Entry<String, Double> entry : sortedEntries) {
            entries.add(new Entry(i, entry.getValue().floatValue()));
            labels.add(entry.getKey().substring(5));
            i++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Prosečan XP težine");
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        chartAverageDifficulty.setData(lineData);
        chartAverageDifficulty.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartAverageDifficulty.getDescription().setEnabled(false);
        chartAverageDifficulty.getLegend().setTextColor(Color.WHITE);
        chartAverageDifficulty.getXAxis().setTextColor(Color.WHITE);
        chartAverageDifficulty.getAxisLeft().setTextColor(Color.WHITE);
        chartAverageDifficulty.getAxisRight().setTextColor(Color.WHITE);
        chartAverageDifficulty.invalidate();
    }

    private void setupWeeklyXpChart(Map<String, Integer> data) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            entries.add(new Entry(i, entry.getValue()));
            labels.add(entry.getKey().substring(5));
            i++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Earned XP");
        dataSet.setValueTextColor(Color.WHITE);
//        dataSet.setColor(ContextCompat.getColor(this, R.color.design_default_color_primary));
//        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.design_default_color_secondary));

        LineData lineData = new LineData(dataSet);
        chartXpLast7Days.setData(lineData);
        chartXpLast7Days.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartXpLast7Days.getDescription().setEnabled(false);
        chartXpLast7Days.getLegend().setTextColor(Color.WHITE);
        chartXpLast7Days.getXAxis().setTextColor(Color.WHITE);
        chartXpLast7Days.getAxisLeft().setTextColor(Color.WHITE);
        chartXpLast7Days.getAxisRight().setTextColor(Color.WHITE);
        chartXpLast7Days.invalidate();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}