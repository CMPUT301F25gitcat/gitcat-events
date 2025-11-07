package com.example.gitcat_events.core.model;

import java.util.Date;

public class Notif {
    private String title;
    private String description;
    private Date date;
    // TODO: add some other fields in the future such as event details and which users receive, etc.

    public Notif(String title, String description, Date date) {
        this.title = title;
        this.description = description;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
