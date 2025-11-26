package com.example.gitcat_events.core.model;

import androidx.annotation.Nullable;

import java.util.Date;
public class Notif {
    private String title;
    private String description;
    private Date date;
    private String documentId;

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
    public String getDocumentId() {
        return documentId;
    }
    public void setDocumentId(@Nullable String documentId) {
        this.documentId = documentId;
    }
}
