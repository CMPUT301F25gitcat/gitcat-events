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
     * 
     * Constructing a new AcceptedListEntry with a specified event ID and user ID 
     * @param EventID identifier for every single event uniquely in the database 
     * @param UserID identifier for every single user uniquely in the database 
     */
    public AcceptedListEntry(int EventID, int UserID){
        super(EventID, UserID);
    }
}
