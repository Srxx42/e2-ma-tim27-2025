package com.example.e2taskly.presentation.activity;

import android.os.Bundle;

import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e2taskly.R;
import com.example.e2taskly.data.repository.MessageRepository;
import com.example.e2taskly.model.Message;
import com.example.e2taskly.model.User;
import com.example.e2taskly.presentation.adapter.MessageAdapter;
import com.example.e2taskly.service.MessageService;
import com.example.e2taskly.service.UserService;
import com.example.e2taskly.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class AllianceMessagesActivity extends AppCompatActivity {

    public static final String EXTRA_ALLIANCE_ID = "alliance_id";
    public static final String EXTRA_ALLIANCE_NAME = "alliance_name";

    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonSendMessage;
    private ImageView menuButton;
    private MessageAdapter messageAdapter;
    private MessageService messageService;
    private UserService userService;

    private String allianceId;
    private String allianceName;
    private String currentUserId;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_messages);
        EdgeToEdge.enable(this);
        View mainView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            int systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            int imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;

            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), Math.max(systemBarsInsets, imeInsets));

            return insets;
        });
        allianceId = getIntent().getStringExtra(EXTRA_ALLIANCE_ID);
        allianceName = getIntent().getStringExtra(EXTRA_ALLIANCE_NAME);

        if (allianceId == null || allianceId.isEmpty()) {
            Toast.makeText(this, "Alliance not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        SharedPreferencesUtil prefs = new SharedPreferencesUtil(this);
        currentUserId = prefs.getActiveUserUid();

        messageService = new MessageService(this);
        userService = new UserService(this);

        initViews();
        setupRecyclerView();
        loadCurrentUser();

        buttonSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void initViews() {
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);
        menuButton = findViewById(R.id.menuButton);
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(new ArrayList<>(), currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void loadCurrentUser() {
        userService.getUserProfile(currentUserId).addOnSuccessListener(user -> {
            this.currentUsername = user.getUsername();
            listenForMessages();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void listenForMessages() {
        messageService.getAndListenForMessages(allianceId, new MessageService.MessagesCallback() {
            @Override
            public void onInitialMessagesLoaded(List<Message> messages) {
                runOnUiThread(() -> {
                    messageAdapter.setMessages(messages);
                    if (messageAdapter.getItemCount() > 0) {
                        recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                    }
                });
            }
            @Override
            public void onNewMessages(List<Message> messages) {
                runOnUiThread(() -> {
                    messageAdapter.addMessages(messages);
                    recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(AllianceMessagesActivity.this, "Error fetching messages.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }
        editTextMessage.setText("");
        messageService.sendMessage(messageText, allianceId, currentUserId, currentUsername)
                .addOnSuccessListener(aVoid -> editTextMessage.setText(""))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messageService.stopListening();
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