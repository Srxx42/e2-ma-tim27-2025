package com.example.e2taskly.presentation.activity;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.e2taskly.R;
import com.example.e2taskly.service.AllianceService;
import com.example.e2taskly.service.UserService;

public class AllianceDecisionActivity extends AppCompatActivity {
    private AllianceService allianceService;
    private UserService userService;
    private String inviteId;
    private ProgressBar progressBar;
    private Button buttonConfirm, buttonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_decision);

        allianceService = new AllianceService(this);
        userService = new UserService(this);

        inviteId = getIntent().getStringExtra("invite_id");
        if (inviteId == null) {
            Toast.makeText(this, "Error: Invitation ID is missing.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        progressBar = findViewById(R.id.progressBarDecision);
        buttonConfirm = findViewById(R.id.buttonConfirmJoin);
        buttonCancel = findViewById(R.id.buttonCancelJoin);

        buttonConfirm.setOnClickListener(v -> forceAcceptInvite());
        buttonCancel.setOnClickListener(v -> finish());
    }

    private void forceAcceptInvite() {
        setLoading(true);
        String currentUserId = userService.getCurrentUserId();

        allianceService.forceAcceptInvitationAndLeaveOld(inviteId, currentUserId)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Successfully joined the new alliance!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to join: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setLoading(false);
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonConfirm.setEnabled(!isLoading);
        buttonCancel.setEnabled(!isLoading);
    }
}