package com.example.e2taskly.presentation.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e2taskly.R;
import com.example.e2taskly.model.User;
import com.example.e2taskly.presentation.adapter.UserAdapter;
import com.example.e2taskly.service.UserService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FriendsListActivity extends AppCompatActivity implements UserAdapter.OnFriendActionListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textViewInfo;
    private SearchView searchView;
    private UserAdapter userAdapter;
    private UserService userService;
    private String currentUserId;
    private List<String> myFriendIds = new ArrayList<>();
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String scannedUid = result.getContents();

                    if (scannedUid.equals(currentUserId)) {
                        Toast.makeText(this, "You cannot add yourself.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (myFriendIds.contains(scannedUid)) {
                        Toast.makeText(this, "You are already friends.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    progressBar.setVisibility(View.VISIBLE);
                    userService.getUserProfile(scannedUid).addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful() && task.getResult() != null) {
                            User userToAdd = task.getResult();
                            onAddFriend(userToAdd);
                        } else {
                            Toast.makeText(this, "Invalid QR Code: User not found.", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Friends");
        }

        userService = new UserService(this);
        currentUserId = userService.getCurrentUserId();

        progressBar = findViewById(R.id.progressBar);
        textViewInfo = findViewById(R.id.textViewInfo);
        recyclerView = findViewById(R.id.recyclerViewFriends);
        searchView = findViewById(R.id.searchView);

        setupRecyclerView();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentQuery = searchView.getQuery().toString();
        if (currentQuery.isEmpty()) {
            loadFriendsList();
        } else {
            searchUsers(currentQuery);
        }
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(this, new ArrayList<>(), myFriendIds, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);
    }


    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadFriendsList();
                } else {
                    searchUsers(newText);
                }
                return true;
            }
        });
    }
    private void loadFriendsList() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        textViewInfo.setVisibility(View.GONE);

        userService.getFriendsForCurrentUser(currentUserId).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null) {
                List<User> friends = task.getResult();
                myFriendIds = friends.stream().map(User::getUid).collect(Collectors.toList());
                userAdapter.updateFriendIds(myFriendIds);
                updateDisplayedList(friends, false);
            } else {
                Toast.makeText(this, "Error loading friends.", Toast.LENGTH_SHORT).show();
                updateDisplayedList(new ArrayList<>(), false);
            }
        });
    }
    private void searchUsers(String query) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        textViewInfo.setVisibility(View.GONE);

        userService.getUserProfile(currentUserId).addOnSuccessListener(currentUser -> {
            myFriendIds.clear();
            myFriendIds.addAll(currentUser.getFriendIds());
            userAdapter.updateFriendIds(myFriendIds);

            userService.searchUsers(query).addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful() && task.getResult() != null) {
                    List<User> searchResults = task.getResult().stream()
                            .filter(user -> !user.getUid().equals(currentUserId))
                            .collect(Collectors.toList());
                    updateDisplayedList(searchResults, true);
                } else {
                    Toast.makeText(this, "Search failed.", Toast.LENGTH_SHORT).show();
                    updateDisplayedList(new ArrayList<>(), true);
                }
            });
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error fetching user profile.", Toast.LENGTH_SHORT).show();
        });
    }


    @Override
    public void onAddFriend(User userToAdd) {
        progressBar.setVisibility(View.VISIBLE);
        userService.addFriend(currentUserId, userToAdd.getUid()).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Friend '" + userToAdd.getUsername() + "' added!", Toast.LENGTH_SHORT).show();
                onResume();
            } else {
                Toast.makeText(this, "Failed to add friend.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRemoveFriend(User userToRemove) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove Friend")
                .setMessage("Are you sure you want to remove '" + userToRemove.getUsername() + "'?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    userService.removeFriend(currentUserId, userToRemove.getUid()).addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Friend removed.", Toast.LENGTH_SHORT).show();
                            onResume();
                        } else {
                            Toast.makeText(this, "Failed to remove friend.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show();
    }

    private void updateDisplayedList(List<User> newList, boolean isSearch) {
        userAdapter.updateUsers(newList);
        if (newList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            textViewInfo.setVisibility(View.VISIBLE);
            if (isSearch) {
                textViewInfo.setText("No users found");
            } else {
                textViewInfo.setText("You have no friends yet.\nUse the search bar to find some!");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textViewInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.friends_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_scan_qr) {
            launchQrScanner();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan a user's QR code");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(true);

        barcodeLauncher.launch(options);
    }
}