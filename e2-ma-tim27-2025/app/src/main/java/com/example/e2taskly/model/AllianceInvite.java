package com.example.e2taskly.model;

import com.example.e2taskly.model.enums.Status;

import java.util.Date;
import java.util.UUID;

public class AllianceInvite {
    private String inviteId;
    private String allianceId;
    private String allianceName;
    private String senderId;
    private String inviterUsername;
    private String receiverId;
    private Date timestamp;
    private Status status;

    public AllianceInvite() {
    }

    public AllianceInvite(String inviteId, String allianceId, String allianceName, String senderId, String inviterUsername, String receiverId) {
        this.inviteId = inviteId;
        this.allianceId = allianceId;
        this.allianceName = allianceName;
        this.senderId = senderId;
        this.inviterUsername = inviterUsername;
        this.receiverId = receiverId;
    }
    public AllianceInvite(String allianceId, String allianceName, String senderId, String inviterUsername, String receiverId) {
        this.inviteId = UUID.randomUUID().toString();
        this.allianceId = allianceId;
        this.allianceName = allianceName;
        this.senderId = senderId;
        this.inviterUsername = inviterUsername;
        this.receiverId = receiverId;
        this.status = Status.PENDING;
        this.timestamp = new Date();
    }

    public String getInviteId() {
        return inviteId;
    }

    public void setInviteId(String inviteId) {
        this.inviteId = inviteId;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public String getAllianceName() {
        return allianceName;
    }

    public void setAllianceName(String allianceName) {
        this.allianceName = allianceName;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getInviterUsername() {
        return inviterUsername;
    }

    public void setInviterUsername(String inviterUsername) {
        this.inviterUsername = inviterUsername;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
