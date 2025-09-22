package com.example.e2taskly.presentation.activity;

import android.content.Intent;
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
import com.example.e2taskly.presentation.adapter.FriendAdapter;
import com.example.e2taskly.presentation.adapter.UserAdapter; // IMPORTANT: Use the UserAdapter
import com.example.e2taskly.service.UserService;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;

public class FriendsListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textViewInfo;
    private SearchView searchView;
    private FriendAdapter friendAdapter;
    private UserService userService;

    private List<User> displayedUserList = new ArrayList<>();
    public static List<User> myFriendsListCache = new ArrayList<>(); // Public for adapter access
    private List<User> allUsersListCache = new ArrayList<>();
    private List<String> myFriendIds = new ArrayList<>();
    private String currentUserId;
    public static boolean isSearchActive = false; // Public for adapter access

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String scannedUid = result.getContents();

                    Toast.makeText(this, "Scanned UID: " + scannedUid, Toast.LENGTH_SHORT).show();

                    // Now, open the profile of the scanned user
                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.EXTRA_USER_ID, scannedUid);
                    startActivity(intent);

                } else {
                    // This happens if the user presses the back button without scanning anything.
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
        }

        userService = new UserService(this);
        currentUserId = userService.getCurrentUserId();

        progressBar = findViewById(R.id.progressBar);
        textViewInfo = findViewById(R.id.textViewInfo);
        recyclerView = findViewById(R.id.recyclerViewFriends);
        searchView = findViewById(R.id.searchView);

        setupRecyclerView();
        setupSearch();
        loadInitialData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userService != null && currentUserId != null) {
            userService.getUserProfile(currentUserId).addOnSuccessListener(user -> {
                myFriendIds.clear();
                myFriendIds.addAll(user.getFriendIds());
                if (!isSearchActive) {
                    loadInitialData();
                } else {
                    friendAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void setupRecyclerView() {
        friendAdapter = new FriendAdapter(this, displayedUserList, myFriendIds, userService,currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(friendAdapter);
    }

    private void loadInitialData() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        textViewInfo.setVisibility(View.GONE);

        Task<User> currentUserProfileTask = userService.getUserProfile(currentUserId);
        Task<List<User>> allUsersTask = userService.getAllUsers();

        Tasks.whenAll(currentUserProfileTask, allUsersTask).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (!task.isSuccessful()) {
                Toast.makeText(this, "Error loading data.", Toast.LENGTH_LONG).show(); return;
            }

            if (currentUserProfileTask.isSuccessful() && currentUserProfileTask.getResult() != null) {
                myFriendIds.clear();
                myFriendIds.addAll(currentUserProfileTask.getResult().getFriendIds());
            }

            if (allUsersTask.isSuccessful() && allUsersTask.getResult() != null) {
                allUsersListCache.clear();
                for (User user : allUsersTask.getResult()) {
                    if (!user.getUid().equals(currentUserId)) {
                        allUsersListCache.add(user);
                    }
                }
            }

            myFriendsListCache.clear();
            for (User user : allUsersListCache) {
                if (myFriendIds.contains(user.getUid())) {
                    myFriendsListCache.add(user);
                }
            }

            // Handle search state after data refresh
            String currentQuery = searchView.getQuery().toString();
            if (currentQuery.isEmpty()) {
                updateDisplayedList(myFriendsListCache);
            } else {
                filterAllUsers(currentQuery);
            }
        });
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                isSearchActive = !newText.isEmpty();
                if (isSearchActive) {
                    filterAllUsers(newText);
                } else {
                    updateDisplayedList(myFriendsListCache);
                }
                return true;
            }
        });
    }

    private void filterAllUsers(String text) {
        List<User> filteredList = new ArrayList<>();
        for (User user : allUsersListCache) {
            if (user.getUsername().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(user);
            }
        }
        updateDisplayedList(filteredList);
    }

    private void updateDisplayedList(List<User> newList) {
        displayedUserList.clear();
        displayedUserList.addAll(newList);
        friendAdapter.notifyDataSetChanged();
        updateUIVisibility();
    }
    public void updateUIVisibility() {
        if (displayedUserList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            textViewInfo.setVisibility(View.VISIBLE);
            if (isSearchActive) {
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