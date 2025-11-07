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
    }
}
