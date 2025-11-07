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
    //TODO: javadocs, tests
    private int eventID;
    private int userID;

    /**
     * constructs a new Entry with specific event ID and user ID. 
     * @param eventID identifier for every single event in the database uniquely 
     * @param userID identifier for every single user in the database uniquely 
     */
    public Entry(int eventID, int userID){
        this.eventID=eventID;
        this.userID=userID;
    }

    //getters and setters:

    /**
     * sets the event ID for this entry. 
     * @param eventID the event ID for this event. 
     */
    public void setEventID(int eventID) {
        this.eventID = eventID;
    }


    /**
     * gets the event ID for this entry. 
     * @return the event ID for this event. 
     */
    public int getEventID() {
        return eventID;
    }

    /**
     * gets the user ID for this entry. 
     * @return the user ID for this entry. 
     */

    public int getUserID() {
        return userID;
    }

    /**
     * sets the user ID for this entry. 
     * @param userID the user ID for this user. 
     */

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
