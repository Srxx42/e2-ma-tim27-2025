package com.example.e2taskly.model;

import com.example.e2taskly.model.enums.MissionStatus;

import java.util.List;

public class Alliance {
    private String allianceId;
    private String name;
    private String leaderId;
    private List<String> memberIds;
    private MissionStatus missionStatus;
    private String currentMissionId;

    public Alliance() {
    }

    public Alliance(String allianceId, String name, String leaderId) {
        this.allianceId = allianceId;
        this.name = name;
        this.leaderId = leaderId;
        this.missionStatus = MissionStatus.NOT_STARTED;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public MissionStatus getMissionStatus() {
        return missionStatus;
    }

    public void setMissionStatus(MissionStatus missionStatus) {
        this.missionStatus = missionStatus;
    }

    public String getCurrentMissionId() {
        return currentMissionId;
    }

    public void setCurrentMissionId(String currentMissionId) {
        this.currentMissionId = currentMissionId;
    }
}
