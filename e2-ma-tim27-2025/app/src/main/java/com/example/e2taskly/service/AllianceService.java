package com.example.e2taskly.service;

import android.content.Context;

import com.example.e2taskly.data.repository.AllianceRepository;
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

    public AllianceService(Context context){
        this.allianceRepository = new AllianceRepository(context);
        this.userRepository = new UserRepository(context);
    }
    public Task<Void> createAlliance(String leaderId, String allianceName, List<String> friendIdsToInvite) {
        return userRepository.getUserProfile(leaderId).continueWithTask(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                throw new Exception("Could not retrieve user profile to create alliance.");
            }
            User leader = task.getResult();

            if (leader.getAllianceId() != null && !leader.getAllianceId().isEmpty()) {
                throw new Exception("You are already in an alliance. Leave your current one to create a new one.");
            }

            Alliance newAlliance = new Alliance();
            newAlliance.setAllianceId(UUID.randomUUID().toString());
            newAlliance.setName(allianceName);
            newAlliance.setLeaderId(leaderId);
            List<String> members = new ArrayList<>();
            members.add(leaderId);
            newAlliance.setMemberIds(members);
            newAlliance.setMissionStatus(MissionStatus.NOT_STARTED);
            newAlliance.setCurrentMissionId(null);


            return allianceRepository.createAlliance(newAlliance)
                    .onSuccessTask(aVoid -> userRepository.updateUserAllianceId(leaderId, newAlliance.getAllianceId()));
        });
    }
    public Task<Alliance> getAlliance(String allianceId) {
        return allianceRepository.getAlliance(allianceId);
    }

    public Task<Void> acceptInvite(String userId, String newAllianceId) {
        return userRepository.getUserProfile(userId).continueWithTask(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                throw new Exception("Could not retrieve user profile to accept invite.");
            }
            User user = task.getResult();
            String oldAllianceId = user.getAllianceId();

            Task<Void> leaveTask = Tasks.forResult(null);

            if (oldAllianceId != null && !oldAllianceId.isEmpty()) {
                leaveTask = leaveAlliance(userId, oldAllianceId);
            }

            return leaveTask.onSuccessTask(aVoid -> {
                Task<Void> addMemberTask = allianceRepository.addMember(newAllianceId, userId);
                Task<Void> updateUserTask = userRepository.updateUserAllianceId(userId, newAllianceId);
                return Tasks.whenAll(addMemberTask, updateUserTask);
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
