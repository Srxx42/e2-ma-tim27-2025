package com.example.e2taskly.service;

import android.content.Context;
import android.util.Log;

import com.example.e2taskly.data.repository.AllianceRepository;
import com.example.e2taskly.data.repository.InviteRepository;
import com.example.e2taskly.data.repository.UserRepository;
import com.example.e2taskly.model.Alliance;
import com.example.e2taskly.model.User;
import com.example.e2taskly.model.enums.MissionStatus;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AllianceService {
    private final AllianceRepository allianceRepository;
    private final UserRepository userRepository;
    private final InviteService inviteService;
    public AllianceService(Context context){
        this.allianceRepository = new AllianceRepository(context);
        this.userRepository = new UserRepository(context);
        this.inviteService = new InviteService(context);
    }
    public Task<Void> createAlliance(String leaderId, String allianceName, List<User> friendsToInvite, String leaderUsername) {
        return userRepository.getUserProfile(leaderId).continueWithTask(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                throw new Exception("Could not retrieve user profile to create alliance.");
            }
            User leader = task.getResult();

            if (leader.getAllianceId() != null && !leader.getAllianceId().isEmpty()) {
                throw new Exception("You are already in an alliance. Leave your current one to create a new one.");
            }

            Alliance newAlliance = new Alliance();
            newAlliance.setName(allianceName);
            newAlliance.setLeaderId(leaderId);
            List<String> members = new ArrayList<>();
            members.add(leaderId);
            newAlliance.setMemberIds(members);
            newAlliance.setMissionStatus(MissionStatus.NOT_STARTED);
            newAlliance.setCurrentMissionId(null);

            return allianceRepository.createAlliance(newAlliance)
                    .onSuccessTask(aVoid -> userRepository.updateUserAllianceId(leaderId, newAlliance.getAllianceId()))
                    .onSuccessTask(aVoid -> {
                        if (friendsToInvite != null && !friendsToInvite.isEmpty()) {
                            return inviteService.sendInvites(leaderId, leaderUsername, newAlliance, friendsToInvite);
                        }
                        return Tasks.forResult(null);
                    });
        });
    }
    public Task<Alliance> getAlliance(String allianceId) {
        return allianceRepository.getAlliance(allianceId);
    }

    public Task<Void> acceptInvite(String userId, String newAllianceId, String inviteId) {
        return userRepository.getUserProfile(userId).continueWithTask(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                throw new Exception("Could not retrieve user profile.");
            }
            User user = task.getResult();
            String oldAllianceId = user.getAllianceId();


            if (oldAllianceId != null && !oldAllianceId.isEmpty()) {
                throw new Exception("ALREADY_IN_ALLIANCE");
            }

            // Ako nije, nastavi sa normalnim pridruživanjem
            return joinNewAlliance(userId, newAllianceId, inviteId);
        });
    }

    public Task<Void> forceAcceptInvitationAndLeaveOld(String inviteId, String userId) {
        return userRepository.getUserProfile(userId).continueWithTask(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                throw new Exception("Could not find user profile.");
            }
            User user = task.getResult();
            String oldAllianceId = user.getAllianceId();

            if (oldAllianceId == null || oldAllianceId.isEmpty()) {

                return inviteService.getInvitationById(inviteId).onSuccessTask(invite ->
                        joinNewAlliance(userId, invite.getAllianceId(), inviteId)
                );
            }

            return leaveAlliance(userId, oldAllianceId).onSuccessTask(aVoid ->
                    inviteService.getInvitationById(inviteId).onSuccessTask(invite ->
                            joinNewAlliance(userId, invite.getAllianceId(), inviteId)
                    )
            );
        });
    }
    private Task<Void> joinNewAlliance(String userId, String newAllianceId, String inviteId) {
        Task<Void> addMemberTask = allianceRepository.addMember(newAllianceId, userId);
        Task<Void> updateUserTask = userRepository.updateUserAllianceId(userId, newAllianceId);
        Task<Void> updateInviteTask = inviteService.acceptInvite(inviteId);

        return Tasks.whenAll(addMemberTask, updateUserTask, updateInviteTask)
                .onSuccessTask(aVoid -> sendAcceptanceNotification(newAllianceId, userId));
    }
    private Task<Void> sendAcceptanceNotification(String allianceId, String newMemberId) {
        Task<Alliance> allianceTask = allianceRepository.getAlliance(allianceId);
        Task<User> newMemberTask = userRepository.getUserProfile(newMemberId);

        return Tasks.whenAll(allianceTask, newMemberTask).onSuccessTask(aVoid -> {
            Alliance alliance = allianceTask.getResult();
            User newMember = newMemberTask.getResult();

            if (alliance == null || newMember == null) {
                Log.e("AllianceService", "Alliance or new member not found, cannot send notification.");
                return Tasks.forResult(null);
            }

            return userRepository.getUserProfile(alliance.getLeaderId()).onSuccessTask(leader -> {
                if (leader == null || leader.getFcmToken() == null || leader.getFcmToken().isEmpty()) {
                    Log.e("AllianceService", "Leader not found or has no FCM token.");
                    return Tasks.forResult(null);
                }

                inviteService.sendAcceptanceNotification(leader.getFcmToken(), newMember.getUsername(), alliance.getName());
                return Tasks.forResult(null);
            });
        });
    }



    public Task<Void> leaveAlliance(String userId, String allianceId) {
        return allianceRepository.getAlliance(allianceId).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            Alliance alliance = task.getResult();

            if (alliance.getLeaderId().equals(userId)) {
                throw new Exception("The leader cannot leave the alliance, only disband it.");
            }

            if (alliance.getCurrentMissionId() != null) {
                throw new Exception("You cannot leave the alliance during an active mission.");
            }

            Task<Void> removeMemberTask = allianceRepository.removeMember(allianceId, userId);
            Task<Void> updateUserTask = userRepository.updateUserAllianceId(userId, null);

            return Tasks.whenAll(removeMemberTask, updateUserTask);
        });
    }

    public Task<Void> disbandAlliance(String leaderId, String allianceId) {
        return allianceRepository.getAlliance(allianceId).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            Alliance alliance = task.getResult();

            if (!alliance.getLeaderId().equals(leaderId)) {
                throw new Exception("Only the leader can disband the alliance.");
            }
            if (alliance.getCurrentMissionId() != null) {
                throw new Exception("Cannot disband during an active mission.");
            }

            List<Task<Void>> tasks = new ArrayList<>();
            for (String memberId : alliance.getMemberIds()) {
                tasks.add(userRepository.updateUserAllianceId(memberId, null));
            }

            return Tasks.whenAll(tasks).onSuccessTask(aVoid -> {
                return allianceRepository.deleteAlliance(allianceId);
            });
        });
    }
}
