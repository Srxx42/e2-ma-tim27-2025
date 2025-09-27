package com.example.e2taskly.presentation.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.example.e2taskly.service.InviteService;
import com.example.e2taskly.service.UserService;
import com.example.e2taskly.util.SharedPreferencesUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AllianceActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private LinearLayout layoutAllianceInfo, layoutNoAlliance;
    private TextView textViewAllianceName, textViewAllianceLeader;
    private RecyclerView recyclerViewMembers;
    private Button buttonCreateAlliance;
    private FloatingActionButton fabInviteMembers;
    private UserService userService;
    private AllianceService allianceService;
    private UserAdapter memberAdapter;
    private String currentUserId;
    private User currentUser;
    private SharedPreferencesUtil sharedPreferences;
    private Alliance currentAlliance;
    private List<User> friendList = new ArrayList<>();
    private List<User> selectedFriends = new ArrayList<>();


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
        fabInviteMembers = findViewById(R.id.fabInviteMembers);
        fabInviteMembers.setOnClickListener(v -> showInviteMoreFriendsDialog());

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
        setLoadingState(true);
        userService.getUserProfile(currentUserId)
                .addOnSuccessListener(user -> {
                    this.currentUser = user;
                    if (user.getAllianceId() != null && !user.getAllianceId().isEmpty()) {
                        loadAllianceDetails(user.getAllianceId());
                    } else {
                        displayNoAllianceView();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    Toast.makeText(this, "Error fetching profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void loadAllianceDetails(String allianceId) {
        allianceService.getAlliance(allianceId)
                .addOnSuccessListener(alliance -> {
                    this.currentAlliance = alliance;
                    invalidateOptionsMenu();

                    userService.getUsersByIds(alliance.getMemberIds())
                            .addOnSuccessListener(members -> {
                                setLoadingState(false);
                                displayAllianceDetails(alliance, members);
                            })
                            .addOnFailureListener(e -> {
                                setLoadingState(false);
                                Toast.makeText(this, "Error fetching members: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    Toast.makeText(this, "Error fetching alliance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            layoutAllianceInfo.setVisibility(View.GONE);
            layoutNoAlliance.setVisibility(View.GONE);
        }
    }

    private void displayNoAllianceView() {
        setLoadingState(false);
        this.currentAlliance = null;
        invalidateOptionsMenu();
        layoutAllianceInfo.setVisibility(View.GONE);
        layoutNoAlliance.setVisibility(View.VISIBLE);
        fabInviteMembers.setVisibility(View.GONE);
    }

    private void displayAllianceDetails(Alliance alliance, List<User> members) {
        layoutAllianceInfo.setVisibility(View.VISIBLE);
        layoutNoAlliance.setVisibility(View.GONE);
        boolean isLeader = currentUser.getUid().equals(alliance.getLeaderId());
        fabInviteMembers.setVisibility(isLeader ? View.VISIBLE : View.GONE);
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
        progressBar.setVisibility(View.VISIBLE);
        userService.getFriendsForCurrentUser(currentUserId)
                .addOnSuccessListener(friends -> {
                    progressBar.setVisibility(View.GONE);
                    this.friendList = friends;
                    showDialogWithFriendList();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Could not load friends list to invite.", Toast.LENGTH_SHORT).show();
                });
    }
    private void showDialogWithFriendList() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Create New Alliance");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);
        final EditText inputName = new EditText(this);
        inputName.setHint("Enter alliance name");
        layout.addView(inputName);
        builder.setView(layout);

        if (friendList.isEmpty()) {
            builder.setMessage("You have no friends to invite.");
        } else {
            String[] friendNames = friendList.stream().map(User::getUsername).toArray(String[]::new);
            boolean[] checkedItems = new boolean[friendList.size()];
            selectedFriends.clear();

            builder.setMultiChoiceItems(friendNames, checkedItems, (dialog, which, isChecked) -> {
                User selectedFriend = friendList.get(which);
                if (isChecked) {
                    selectedFriends.add(selectedFriend);
                } else {
                    selectedFriends.remove(selectedFriend);
                }
            });
        }

        builder.setPositiveButton("Create & Invite", (dialog, which) -> {
            String allianceName = inputName.getText().toString().trim();
            if (TextUtils.isEmpty(allianceName)) {
                Toast.makeText(this, "Alliance name cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            createAllianceWithInvites(allianceName, selectedFriends);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    private void createAllianceWithInvites(String allianceName, List<User> friendsToInvite) {
        progressBar.setVisibility(View.VISIBLE);
        Log.d("AllianceActivity", "Creating alliance. Inviting the following users:");
        for (User friend : friendsToInvite) {
            Log.d("AllianceActivity", "Inviting -> Username: " + friend.getUsername() + ", UID: " + friend.getUid());
        }
        //
        allianceService.createAlliance(currentUserId, allianceName, friendsToInvite, currentUser.getUsername())
             .addOnSuccessListener(aVoid -> {
                 Toast.makeText(this, "Alliance created and invites sent!", Toast.LENGTH_SHORT).show();
                 loadUserAllianceStatus();
             })
             .addOnFailureListener(e -> {
                 progressBar.setVisibility(View.GONE);
                 Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
             });
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
                    setLoadingState(true);
                    allianceService.leaveAlliance(currentUserId, currentAlliance.getAllianceId())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "You have left the alliance.", Toast.LENGTH_SHORT).show();
                                displayNoAllianceView();
                            })
                            .addOnFailureListener(e -> {
                                setLoadingState(false);
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
                    setLoadingState(true);
                    allianceService.disbandAlliance(currentUserId, currentAlliance.getAllianceId())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Alliance has been disbanded.", Toast.LENGTH_SHORT).show();
                                displayNoAllianceView();
                            })
                            .addOnFailureListener(e -> {
                                setLoadingState(false);
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .show();
    }
    private void showInviteMoreFriendsDialog() {
        progressBar.setVisibility(View.VISIBLE);
        userService.getFriendsForCurrentUser(currentUserId)
                .addOnSuccessListener(allFriends -> {
                    progressBar.setVisibility(View.GONE);

                    List<String> currentMemberIds = currentAlliance.getMemberIds();
                    List<User> friendsToDisplay = allFriends.stream()
                            .filter(friend -> !currentMemberIds.contains(friend.getUid()))
                            .collect(Collectors.toList());

                    if (friendsToDisplay.isEmpty()) {
                        Toast.makeText(this, "All of your friends are already in the alliance.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    displayInviteDialog(friendsToDisplay);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Could not load friends list.", Toast.LENGTH_SHORT).show();
                });
    }

    private void displayInviteDialog(List<User> friendsToInvite) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Invite More Friends");

        String[] friendNames = friendsToInvite.stream().map(User::getUsername).toArray(String[]::new);
        boolean[] checkedItems = new boolean[friendsToInvite.size()];
        List<User> newlySelectedFriends = new ArrayList<>();

        builder.setMultiChoiceItems(friendNames, checkedItems, (dialog, which, isChecked) -> {
            User selected = friendsToInvite.get(which);
            if (isChecked) {
                newlySelectedFriends.add(selected);
            } else {
                newlySelectedFriends.remove(selected);
            }
        });

        builder.setPositiveButton("Send Invites", (dialog, which) -> {
            if (!newlySelectedFriends.isEmpty()) {
                InviteService inviteService = new InviteService(this);
                inviteService.sendInvites(currentUserId, currentUser.getUsername(), currentAlliance, newlySelectedFriends)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Invites sent!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}