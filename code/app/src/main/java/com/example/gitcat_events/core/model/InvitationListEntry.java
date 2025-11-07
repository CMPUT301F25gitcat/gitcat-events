package com.example.gitcat_events.core.model;
/**
 * This class models a user's invitation to an event with status tracking
 */
public class InvitationListEntry extends Entry {
    //TODO: tests
    
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
    /**
     * This gets the status of the invitation
     * @return
     * returns "pending", "accepted", or "declined"
     */
    public String getStatus() {
        return status;
    }
    /**
     * This sets the status of the invitation
     * @param status
     * the new status of the invitation
     */
    
    public void setStatus(String status) {
        this.status = status;
    }
    /**
     * This gets the draw round of the invitation
     * @return
     * returns the draw round
     */
    public int getDrawRound() {
        return drawRound;
    }
    /**
     * This sets the draw round of the invitation
     * @param drawRound
     * the new draw round of the invitation
     */
    
    public void setDrawRound(int drawRound) {
        this.drawRound = drawRound;
    }
    /**
     * This gets the timestamp of the invitation
     * @return
     * returns the timestamp when the invitation was created
     */
    public long getTimestamp() {
        return timestamp;
    }
    /**
     * This sets the timestamp of the invitation
     * @param timestamp
     * the new timestamp of the invitation
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    /**
     * This gets the expiration time of the invitation
     * @return
     * returns the expiration time in milliseconds
     */
    public long getExpiresAt() {
        return expiresAt;
    }
    /**
     * This sets the expiration time of the invitation
     * @param expiresAt
     * the new expiration time in milliseconds
     */
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}

