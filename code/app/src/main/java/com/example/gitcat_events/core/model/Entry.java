package com.example.gitcat_events.core.model;

/**
 * Represents an entry in the waitlist or accepted list for an event. 
 * 
 * @author Momoore Oshinaike 
 * 
 * @see AcceptedListEntry
 * @see WaitListEntry
 * 
 */

public abstract class Entry {
    private String eventId;
    private String userDeviceId;
    
    /**
     * No-arg constructor for Firebase
     */
    public Entry() {
    }
    
    /**
     * constructs a new Entry with specific event ID and user device ID. 
     * @param eventId identifier for every single event in the database uniquely 
     * @param userDeviceId identifier for every single user in the database uniquely 
     */
    public Entry(String eventId, String userDeviceId){
        this.eventId = eventId;
        this.userDeviceId = userDeviceId;
    }

    //getters and setters:

    /**
     * sets the event ID for this entry. 
     * @param eventId the event ID for this event. 
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * gets the event ID for this entry. 
     * @return the event ID for this event. 
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * gets the user device ID for this entry. 
     * @return the user device ID for this entry. 
     */
    public String getUserDeviceId() {
        return userDeviceId;
    }

    /**
     * sets the user device ID for this entry. 
     * @param userDeviceId the user device ID for this user. 
     */
    public void setUserDeviceId(String userDeviceId) {
        this.userDeviceId = userDeviceId;
    }
}
