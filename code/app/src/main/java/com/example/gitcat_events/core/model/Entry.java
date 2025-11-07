package com.example.gitcat_events.core.model;

/**
 * This class serves links events and user devices
 */
public abstract class Entry {
    //TODO: tests
    private String eventId;
    private String userDeviceId;
    
    // No-arg constructor for Firebase
    public Entry() {
    }
    
    public Entry(String eventId, String userDeviceId){
        this.eventId = eventId;
        this.userDeviceId = userDeviceId;
    }

    //getters and setters:
    /**
     * This sets the event ID
     * @param eventId
     * the new event ID
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    /**
     * This gets the event ID
     * @return
     * returns the event ID
     */

    public String getEventId() {
        return eventId;
    }
    /**
     * This gets the user device ID
     * @return
     * returns the user device ID
     */
    public String getUserDeviceId() {
        return userDeviceId;
    }
    /**
     * This sets the user device ID
     * @param userDeviceId
     * the new user device ID
     */
    public void setUserDeviceId(String userDeviceId) {
        this.userDeviceId = userDeviceId;
    }
}
