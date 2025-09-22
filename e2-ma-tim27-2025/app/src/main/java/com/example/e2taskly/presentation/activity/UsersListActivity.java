package com.example.e2taskly.presentation.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e2taskly.R;
import com.example.e2taskly.model.User;
import com.example.e2taskly.presentation.adapter.FriendAdapter;
import com.example.e2taskly.presentation.adapter.UserAdapter;
import com.example.e2taskly.service.UserService;
import com.example.e2taskly.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class UsersListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUsers;
    private ProgressBar progressBar;
    private FriendAdapter friendAdapter;
    private SearchView searchView;
    private List<User> displayedUserList = new ArrayList<>();
    private List<User> fullUserList = new ArrayList<>();
    private UserService userService;
    private User currentUser;
    private SharedPreferencesUtil sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPreferences = new SharedPreferencesUtil(this);
        userService = new UserService(this);
        progressBar = findViewById(R.id.progressBar);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        searchView = findViewById(R.id.searchView);

//        setupRecyclerView();
        setupSearch();
        loadUsers();
    }

    private void setupRecyclerView(List<String> friendIds) {
        friendAdapter = new FriendAdapter(this, displayedUserList, friendIds, userService, currentUser.getUid());
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(friendAdapter);
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        String currentUserId = sharedPreferences.getActiveUserUid();
        if (currentUserId == null) {
            Toast.makeText(this, "Error: The user is not logged in.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        // 1. Prvo učitaj profil trenutnog korisnika
        userService.getUserProfile(currentUserId).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                currentUser = task.getResult();
                setupRecyclerView(currentUser.getFriendIds());

                userService.getAllUsers().addOnCompleteListener(allUsersTask -> {
                    progressBar.setVisibility(View.GONE);
                    if (allUsersTask.isSuccessful() && allUsersTask.getResult() != null) {
                        fullUserList.clear();
                        for (User user : allUsersTask.getResult()) {
                            if (!user.getUid().equals(currentUserId)) {
                                fullUserList.add(user);
                            }
                        }
                        filter(""); // Prikaz početne liste
                    } else {
                        Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }
    private void filter(String text) {
        List<User> filteredList = new ArrayList<>();
        for (User user : fullUserList) {
            if (user.getUsername().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(user);
            }
        }

        displayedUserList.clear();
        displayedUserList.addAll(filteredList);
        friendAdapter.notifyDataSetChanged();
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