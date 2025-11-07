package com.example.gitcat_events.core.model;

public class InvitationListEntry extends Entry {
    //TODO: javadocs, tests
    
    private String status; // "pending", "accepted", "declined"
    private int drawRound;
    private long timestamp;
    private long expiresAt; // Optional expiration time
    
    // No-arg constructor for Firebase
    public InvitationListEntry() {
        super();
    }
    
    public InvitationListEntry(String eventId, String userDeviceId){
        super(eventId, userDeviceId);
        this.status = "pending";
        this.drawRound = 1;
        this.timestamp = System.currentTimeMillis();
    }
    
    public InvitationListEntry(String eventId, String userDeviceId, int drawRound){
        super(eventId, userDeviceId);
        this.status = "pending";
        this.drawRound = drawRound;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getDrawRound() {
        return drawRound;
    }
    
    public void setDrawRound(int drawRound) {
        this.drawRound = drawRound;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public long getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}

