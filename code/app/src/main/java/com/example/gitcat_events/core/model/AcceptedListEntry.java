package com.example.gitcat_events.core.model;

public class AcceptedListEntry extends Entry {
    //TODO: javadocs, tests
    
    // No-arg constructor for Firebase
    public AcceptedListEntry() {
        super();
    }
    
    public AcceptedListEntry(String eventId, String userDeviceId){
        super(eventId, userDeviceId);
    }
}
