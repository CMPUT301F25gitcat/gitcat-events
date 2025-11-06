package com.example.gitcat_events.core.model;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class Event {
    //TODO: javadocs, waitlist functions, accepted list functions, tests
    //based off CRC card in wiki: https://github.com/CMPUT301F25gitcat/gitcat-events/wiki/Part-3:-CRC-Cards#event
    private String name;
    private String description;
    private int capacity;   //refers to the size of the final list
    @Nullable
    private Integer maxWaitListSize;

    //current plan is to store images as strings
    private String poster;
    //private QRCode qrCode; --QRCode class is not done yet
    private int organizer;

    public Event(String name, String description, int capacity, @Nullable Integer maxWaitListSize, String poster) {
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.maxWaitListSize = maxWaitListSize;
        this.poster = poster;
        //this.qrCode = new QRCode;  --QR code class not ready yet
    }


    //getters and setters:
    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getOrganizer() {
        return organizer;
    }

    @Nullable
    public Integer getMaxWaitListSize() {
        return maxWaitListSize;
    }

    public String getPoster() {
        return poster;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setMaxWaitListSize(@Nullable Integer maxWaitListSize) {
        this.maxWaitListSize = maxWaitListSize;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrganizer(int organizer) {
        this.organizer = organizer;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    /*public void setQRCode(QRCode qrCode) {  -- QR code class is not ready yet
        this.qrCode = qrCode;
    }*/
}
