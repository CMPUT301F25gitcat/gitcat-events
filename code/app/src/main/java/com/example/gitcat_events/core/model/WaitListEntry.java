package com.example.gitcat_events.core.model;
/**
 * Represent an entry in the waitlist for an event. 
 * This class extends the entry class to represent users who is on waitlist for the event. 
 * 
 * @author Momoore Oshinaike
 * 
 * @see Entry 
 * 
 */



public class WaitListEntry extends Entry {
    /**
     * Constructs a new waitlist entry with specific event ID and user ID.
     * @param EventID identifier for every single event in the database uniquely  
     * @param UserID identifier for every single user in the database uniquely
     */
    //TODO: tests
   public WaitListEntry(int EventID, int UserID){
       super(EventID, UserID);
   }
}

