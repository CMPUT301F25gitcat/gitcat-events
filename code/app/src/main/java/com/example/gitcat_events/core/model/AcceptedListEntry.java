package com.example.gitcat_events.core.model;


/**
 * Represents an entry in the accepted list  for an event.
 * This class extends the Entry to represent if anything a user who is accepted to the event.
 * 
 * @author Momoore Oshinaike 
 * @see Entry 
 * 
 */
public class AcceptedListEntry extends Entry {
    private String status; // "pending", "accepted", "declined"
    private int drawRound;
    private long timestamp;
    
    /**
     * No-arg constructor for Firebase
     */
    public AcceptedListEntry() {
        super();
    }
    
    /**
     * Constructing a new AcceptedListEntry with a specified event ID and user device ID 
     * @param eventId identifier for every single event uniquely in the database 
     * @param userDeviceId identifier for every single user uniquely in the database 
     */
    public AcceptedListEntry(String eventId, String userDeviceId){
        super(eventId, userDeviceId);
        this.status = "pending";
        this.drawRound = 1;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructing a new AcceptedListEntry with a specified event ID, user device ID, and draw round
     * @param eventId identifier for every single event uniquely in the database 
     * @param userDeviceId identifier for every single user uniquely in the database 
     * @param drawRound the round number for the draw
     */
    public AcceptedListEntry(String eventId, String userDeviceId, int drawRound){
        super(eventId, userDeviceId);
        this.status = "pending";
        this.drawRound = drawRound;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    /**
     * Gets the status of this accepted list entry
     * @return the status ("pending", "accepted", "declined")
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * Sets the status of this accepted list entry
     * @param status the status ("pending", "accepted", "declined")
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Gets the draw round for this entry
     * @return the draw round number
     */
    public int getDrawRound() {
        return drawRound;
    }
    
    /**
     * Sets the draw round for this entry
     * @param drawRound the draw round number
     */
    public void setDrawRound(int drawRound) {
        this.drawRound = drawRound;
    }
    
    /**
     * Gets the timestamp when this entry was created
     * @return the timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Sets the timestamp for this entry
     * @param timestamp the timestamp in milliseconds
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
