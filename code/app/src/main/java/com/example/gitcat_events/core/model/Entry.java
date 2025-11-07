package com.example.gitcat_events.core.model;

public abstract class Entry {
    //TODO: javadocs, tests
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

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getUserDeviceId() {
        return userDeviceId;
    }

    public void setUserDeviceId(String userDeviceId) {
        this.userDeviceId = userDeviceId;
    }
}
