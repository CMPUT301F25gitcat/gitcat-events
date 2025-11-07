package com.example.gitcat_events.core.model;

public class WaitListEntry extends Entry {
    //TODO: javadocs, tests
    
    // No-arg constructor for Firebase
    public WaitListEntry() {
        super();
    }
    
    public WaitListEntry(String eventId, String userDeviceId){
        super(eventId, userDeviceId);
    }
}

