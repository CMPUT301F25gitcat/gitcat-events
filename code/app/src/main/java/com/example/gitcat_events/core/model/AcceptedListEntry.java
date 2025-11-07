package com.example.gitcat_events.core.model;

/**
This class models the user accepting or declining an event.
 **/
public class AcceptedListEntry extends Entry {
    //TODO: tests
    
    private String status; // "pending", "accepted", "declined"
    private int drawRound;
    private long timestamp;
    
    // No-arg constructor for Firebase
    public AcceptedListEntry() {
        super();
    }
    
    public AcceptedListEntry(String eventId, String userDeviceId){
        super(eventId, userDeviceId);
        this.status = "pending";
        this.drawRound = 1;
        this.timestamp = System.currentTimeMillis();
    }
    
    public AcceptedListEntry(String eventId, String userDeviceId, int drawRound){
        super(eventId, userDeviceId);
        this.status = "pending";
        this.drawRound = drawRound;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    /**
     * This gets the status of the event
     * @return
     * returns "accepted" if the user accepted the event, "declined" if the user declined the event, "pending" otherwise
     */
    public String getStatus() {
        return status;
    }

    /**
     * This sets the status of the event
     * @param status
     * the new status of the event
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * This gets the draw round of the event
     * @return
     * returns the draw round
     */
    public int getDrawRound() {
        return drawRound;
    }
    /**
     * This sets the draw round of the event
     * @param drawRound
     * the new draw round of the event
     */
    public void setDrawRound(int drawRound) {
        this.drawRound = drawRound;
    }

    /**
     * This gets the timestamp of the event
     * @return
     * returns the timestamp of the event
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * This sets the timestamp of the event
     * @param timestamp
     * the new timestamp of the event
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
