package com.example.gitcat_events.core.model;

abstract class Entry {
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
