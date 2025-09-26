package com.example.e2taskly.presentation.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e2taskly.R;
import com.example.e2taskly.model.Alliance;
import com.example.e2taskly.model.User;
import com.example.e2taskly.presentation.adapter.UserAdapter;
import com.example.e2taskly.service.AllianceService;
import com.example.e2taskly.service.UserService;
import com.example.e2taskly.util.SharedPreferencesUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class AllianceActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private LinearLayout layoutAllianceInfo, layoutNoAlliance;
    private TextView textViewAllianceName, textViewAllianceLeader;
    private RecyclerView recyclerViewMembers;
    private Button buttonCreateAlliance;
    private UserService userService;
    private AllianceService allianceService;
    private UserAdapter memberAdapter;
    private String currentUserId;
    private User currentUser;
    private SharedPreferencesUtil sharedPreferences;
    private Alliance currentAlliance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        userService = new UserService(this);
        allianceService = new AllianceService(this);
        sharedPreferences = new SharedPreferencesUtil(this);
        currentUserId = sharedPreferences.getActiveUserUid();

        progressBar = findViewById(R.id.progressBar);
        layoutAllianceInfo = findViewById(R.id.layoutAllianceInfo);
        layoutNoAlliance = findViewById(R.id.layoutNoAlliance);
        textViewAllianceName = findViewById(R.id.textViewAllianceName);
        textViewAllianceLeader = findViewById(R.id.textViewAllianceLeader);
        recyclerViewMembers = findViewById(R.id.recyclerViewMembers);
        buttonCreateAlliance = findViewById(R.id.buttonCreateAlliance);

        setupRecyclerView();

        buttonCreateAlliance.setOnClickListener(v -> showCreateAllianceDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserAllianceStatus();
    }

    private void setupRecyclerView() {
        memberAdapter = new UserAdapter(this, new ArrayList<>(), new ArrayList<>(), null,false);
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMembers.setAdapter(memberAdapter);
    }

    private void loadUserAllianceStatus() {
        progressBar.setVisibility(View.VISIBLE);
        layoutAllianceInfo.setVisibility(View.GONE);
        layoutNoAlliance.setVisibility(View.GONE);

        userService.getUserProfile(currentUserId).addOnSuccessListener(user -> {
            this.currentUser = user;
            if (user.getAllianceId() != null && !user.getAllianceId().isEmpty()) {
                loadAllianceDetails(user.getAllianceId());
            } else {
                displayNoAllianceView();
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error fetching profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadAllianceDetails(String allianceId) {
        allianceService.getAlliance(allianceId).addOnSuccessListener(alliance -> {
            this.currentAlliance = alliance;
            invalidateOptionsMenu();
            userService.getUsersByIds(alliance.getMemberIds()).addOnSuccessListener(members -> {
                progressBar.setVisibility(View.GONE);
                displayAllianceDetails(alliance, members);
            }).addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error fetching members: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error fetching alliance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void displayNoAllianceView() {
        progressBar.setVisibility(View.GONE);
        layoutAllianceInfo.setVisibility(View.GONE);
        layoutNoAlliance.setVisibility(View.VISIBLE);
    }

    private void displayAllianceDetails(Alliance alliance, List<User> members) {
        layoutAllianceInfo.setVisibility(View.VISIBLE);
        layoutNoAlliance.setVisibility(View.GONE);

        textViewAllianceName.setText(alliance.getName());

        String leaderName = "Unknown";
        for (User member : members) {
            if (member.getUid().equals(alliance.getLeaderId())) {
                leaderName = member.getUsername();
                break;
            }
        }
        textViewAllianceLeader.setText("Leader: " + leaderName);

        memberAdapter.updateUsers(members);
    }

    private void showCreateAllianceDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Create New Alliance");

        final EditText input = new EditText(this);
        input.setHint("Enter alliance name");
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String allianceName = input.getText().toString().trim();
            if (TextUtils.isEmpty(allianceName)) {
                Toast.makeText(this, "Alliance name cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Ovde bi idealno išao korak za izbor prijatelja
            List<String> friendsToInvite = new ArrayList<>();

            progressBar.setVisibility(View.VISIBLE);
            allianceService.createAlliance(currentUserId, allianceName, friendsToInvite)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Alliance '" + allianceName + "' created!", Toast.LENGTH_SHORT).show();
                        loadUserAllianceStatus();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.alliance_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (currentAlliance != null && currentUser != null) {
            boolean isLeader = currentUser.getUid().equals(currentAlliance.getLeaderId());
            menu.findItem(R.id.action_leave_alliance).setVisible(!isLeader);
            menu.findItem(R.id.action_disband_alliance).setVisible(isLeader);
        } else {
            menu.findItem(R.id.action_leave_alliance).setVisible(false);
            menu.findItem(R.id.action_disband_alliance).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_leave_alliance) {
            showLeaveAllianceDialog();
            return true;
        } else if (itemId == R.id.action_disband_alliance) {
            showDisbandAllianceDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showLeaveAllianceDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Leave Alliance")
                .setMessage("Are you sure you want to leave '" + currentAlliance.getName() + "'?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Leave", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    allianceService.leaveAlliance(currentUserId, currentAlliance.getAllianceId())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "You have left the alliance.", Toast.LENGTH_SHORT).show();
                                loadUserAllianceStatus();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .show();
    }

    private void showDisbandAllianceDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Disband Alliance")
                .setMessage("Are you sure you want to permanently disband '" + currentAlliance.getName() + "'? This action cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Disband", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    allianceService.disbandAlliance(currentUserId, currentAlliance.getAllianceId())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Alliance has been disbanded.", Toast.LENGTH_SHORT).show();
                                loadUserAllianceStatus();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .show();
    }
}