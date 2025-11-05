package com.example.gitcat_events.core.model;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class Event {
    //based off CRC card in wiki: https://github.com/CMPUT301F25gitcat/gitcat-events/wiki/Part-3:-CRC-Cards#event
    private String name;
    private String description;
    private int capacity;   //refers to the size of the final list
    @Nullable
    private Integer maxWaitListSize;

    //current plan is to store images as strings
    private String poster;
    private QRCode qrCode;

    //is using UIDs here a bad idea?
    private ArrayList<Integer> waitList;
    private ArrayList<Integer> acceptedEntries;
    private int organizer;

    public Event(String name, String description, int capacity, @Nullable Integer maxWaitListSize, String poster) {
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.maxWaitListSize = maxWaitListSize;
        this.poster = poster;
        this.qrCode = new QRCode;
    }

    public int getWaitListCount() {
        return this.waitList.size();
    }

    public Boolean addToWaitList(int uid) {
        if ((this.maxWaitListSize != null) && (this.maxWaitListSize > this.getWaitListCount())) {
            this.waitList.add(uid);
            return Boolean.TRUE;
        } else if (this.maxWaitListSize == null) {
            this.waitList.add(uid);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public void removeFromWaitList(int uid) {
        this.waitList.remove(uid);  //currently unsure if this removes the element at index uid or the uid itself
    }

    public Boolean addToAcceptedEntries(int uid) {
        if (this.capacity > this.acceptedEntries.size()) {
            this.acceptedEntries.add(uid);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public void removeFromAcceptedEntries(int uid) {
        this.acceptedEntries.remove(uid);   //currently unsure if this removes the element at index uid or the uid itself
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

    public ArrayList<Integer> getAcceptedEntries() {
        return acceptedEntries;
    }

    public int getCapacity() {
        return capacity;
    }

    public ArrayList<Integer> getWaitList() {
        return waitList;
    }

    public int getOrganizer() {
        return organizer;
    }

    @Nullable
    public Integer getMaxWaitListSize() {
        return maxWaitListSize;
    }

    public QRCode getQrCode() {
        return this.qrCode;
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

    public void setQRCode(QRCode qrCode) {
        this.qrCode = qrCode;
    }
}
