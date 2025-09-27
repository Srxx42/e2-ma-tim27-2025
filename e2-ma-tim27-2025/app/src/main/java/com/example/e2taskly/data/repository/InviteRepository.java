package com.example.e2taskly.data.repository;

import android.content.Context;

import com.example.e2taskly.data.database.InviteLocalDataSource;
import com.example.e2taskly.data.remote.InviteRemoteDataSource;
import com.example.e2taskly.model.Alliance;
import com.example.e2taskly.model.enums.Status;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class InviteRepository {
    private InviteLocalDataSource localDataSource;
    private InviteRemoteDataSource remoteDataSource;

    public InviteRepository(Context context) {
        this.localDataSource = new InviteLocalDataSource(context);
        this.remoteDataSource = new InviteRemoteDataSource();
    }

    public Task<Void> sendInvites(String inviterId, String inviterUsername, Alliance alliance, List<String> friendIds) {
        return remoteDataSource.sendInvites(inviterId, inviterUsername, alliance, friendIds);
    }

    public Task<Void> acceptInvite(String inviteId) {
        return remoteDataSource.updateInviteStatus(inviteId, Status.ACCEPTED).addOnSuccessListener(aVoid -> {
            localDataSource.deleteInvite(inviteId);
        });
    }

    public Task<Void> declineInvite(String inviteId) {
        return remoteDataSource.updateInviteStatus(inviteId, Status.DECLINED).addOnSuccessListener(aVoid -> {
            localDataSource.deleteInvite(inviteId);
        });
    }
}
