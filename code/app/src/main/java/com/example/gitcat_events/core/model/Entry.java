package com.example.gitcat_events.core.model;

/**
 * Represetns an entry in the waitlist or accepted list for an event. 
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
    public Entry(int eventID, int userID){
        this.eventID=eventID;
        this.userID=userID;
    }

    //getters and setters:

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public int getEventID() {
        return eventID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
