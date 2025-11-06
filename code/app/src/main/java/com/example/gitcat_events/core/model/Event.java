package com.example.gitcat_events.core.model;

public class Event {
    private String name;
    private String desc;
    private String date;

    public Event(String name, String desc, String date) {
        this.name = name;
        this.desc = desc;
        this.date = date;
        // add more
    }

    public String getName(){
        return name;
    }

    public String getDesc(){
        return desc;
    }

    public String getDate(){
        return date;
    }
}
