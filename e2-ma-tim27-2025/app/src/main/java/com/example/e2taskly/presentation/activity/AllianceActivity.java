package com.example.e2taskly.presentation.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.example.e2taskly.R;
import com.example.e2taskly.model.Alliance;
import com.example.e2taskly.model.User;
import com.example.e2taskly.presentation.adapter.FriendInviteAdapter;
import com.example.e2taskly.presentation.adapter.UserAdapter;
import com.example.e2taskly.service.AllianceService;
import com.example.e2taskly.service.InviteService;
import com.example.e2taskly.service.UserService;
import com.example.e2taskly.util.SharedPreferencesUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

        setupRecyclerView();

        fabInviteMembers.setOnClickListener(v -> showInviteMoreFriendsDialog());
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
                                if (currentAlliance != null) layoutAllianceInfo.setVisibility(View.VISIBLE);
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
        setLoadingState(true);
        userService.getFriendsForCurrentUser(currentUserId)
                .addOnSuccessListener(friends -> {
                    setLoadingState(false);
                    layoutNoAlliance.setVisibility(View.VISIBLE);
                    displayFriendInviteDialog(friends, true);
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    layoutNoAlliance.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Could not load friends list.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showInviteMoreFriendsDialog() {
        setLoadingState(true);
        userService.getFriendsForCurrentUser(currentUserId)
                .addOnSuccessListener(allFriends -> {
                    setLoadingState(false);
                    // FIX: Vratite vidljivost layout-a pre prikaza dijaloga
                    layoutAllianceInfo.setVisibility(View.VISIBLE);

                    List<String> currentMemberIds = currentAlliance.getMemberIds();
                    List<User> friendsToDisplay = allFriends.stream()
                            .filter(friend -> !currentMemberIds.contains(friend.getUid()))
                            .collect(Collectors.toList());

                    if (friendsToDisplay.isEmpty()) {
                        Toast.makeText(this, "All friends are already in the alliance.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    displayFriendInviteDialog(friendsToDisplay, false); // false for inviting more
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    // FIX: Vratite vidljivost layout-a i u slučaju greške
                    layoutAllianceInfo.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Could not load friends list.", Toast.LENGTH_SHORT).show();
                });
    }

    private void displayFriendInviteDialog(List<User> friends, boolean isCreating) {
        LayoutInflater inflater = getLayoutInflater();
        // Pretpostavka je da je ime layouta 'dialog_alliance_invite.xml' kao u prethodnom primeru
        // Ako ste ga nazvali 'dialog_create_alliance.xml', koristite to ime.
        View dialogView = inflater.inflate(R.layout.dialog_create_alliance, null);

        TextInputLayout layoutInputName = dialogView.findViewById(R.id.layoutInputName);
        TextInputEditText inputName = dialogView.findViewById(R.id.inputName);
        RecyclerView recyclerViewFriends = dialogView.findViewById(R.id.recyclerViewFriends);
        TextView textViewNoFriends = dialogView.findViewById(R.id.textViewNoFriends);
        TextView labelInviteFriends = dialogView.findViewById(R.id.labelInviteFriends);

        FriendInviteAdapter adapter = new FriendInviteAdapter(friends);
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFriends.setAdapter(adapter);

        if (friends.isEmpty()) {
            recyclerViewFriends.setVisibility(View.GONE);
            textViewNoFriends.setVisibility(View.VISIBLE);
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(dialogView);

        if (isCreating) {
            builder.setTitle("Create New Alliance");
            layoutInputName.setVisibility(View.VISIBLE);
            builder.setPositiveButton("Create & Invite", null);
        } else {
            builder.setTitle("Invite More Friends");
            labelInviteFriends.setText("Select friends to invite:");
            builder.setPositiveButton("Send Invites", null);
        }

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                List<User> selectedFriends = adapter.getSelectedFriends();

                if (isCreating) {
                    String allianceName = inputName.getText().toString().trim();
                    if (TextUtils.isEmpty(allianceName)) {
                        inputName.setError("Alliance name cannot be empty.");
                        return;
                    }
                    createAllianceWithInvites(allianceName, selectedFriends);
                } else {
                    if (selectedFriends.isEmpty()) {
                        Toast.makeText(this, "Please select at least one friend to invite.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendNewInvites(selectedFriends);
                }
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void createAllianceWithInvites(String allianceName, List<User> friendsToInvite) {
        setLoadingState(true);
        allianceService.createAlliance(currentUserId, allianceName, friendsToInvite, currentUser.getUsername())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Alliance created and invites sent!", Toast.LENGTH_SHORT).show();
                    loadUserAllianceStatus();
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    displayNoAllianceView();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void sendNewInvites(List<User> newlySelectedFriends) {
        InviteService inviteService = new InviteService(this);
        inviteService.sendInvites(currentUserId, currentUser.getUsername(), currentAlliance, newlySelectedFriends)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Invites sent!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
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
                                displayNoAllianceView(); // Correctly refreshes UI
                            })
                            .addOnFailureListener(e -> {
                                setLoadingState(false);
                                layoutAllianceInfo.setVisibility(View.VISIBLE);
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
                                displayNoAllianceView(); // Correctly refreshes UI
                            })
                            .addOnFailureListener(e -> {
                                setLoadingState(false);
                                layoutAllianceInfo.setVisibility(View.VISIBLE);
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .show();
    }

}