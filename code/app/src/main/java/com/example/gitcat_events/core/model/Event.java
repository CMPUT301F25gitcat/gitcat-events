package com.example.gitcat_events.core.model;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * Represents an event that users can register for. 
 * Events have a capacity , optional waitlist size , dates via calendar objects , and a boolean for if geolocation is required.
 * 
 * @author Momoore Oshinaike 
 * @see AcceptedListEntry
 * @see WaitListEntry
 * @see Entry
 * 
 * 
 */

public class Event {
    
    //based off CRC card in wiki: https://github.com/CMPUT301F25gitcat/gitcat-events/wiki/Part-3:-CRC-Cards#event
    private String name;//name of the event 
    private String description;//descriptions of the event 
    private int capacity;   //refers to the size of the final list
    @Nullable private Integer maxWaitListSize; // basically the maximum number of people that could attend the event. 
    private Calendar eventDate;
    private Calendar raffleDate;
    private Boolean geoLocationRequired;

    //current plan is to store images as strings
    private String poster;
    //private QRCode qrCode; --QRCode class is not done yet
    private int organizer;
    /**
     * here we construct the event object with the following parameters:
     * @param name the name of the event which can not be null
     * @param description a brief and concise description of the event
     * @param capacity the maximum number of people that can attend the event. can only be poisitive 
     * @param maxWaitListSize the maximum size of waitlist and should be positive 
     * 
     */

    public Event(String name, String description, int capacity, @Nullable Integer maxWaitListSize, String poster,  Calendar raffleDate, Calendar eventDate, Boolean geoLocationRequired) {
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.maxWaitListSize = maxWaitListSize;
        this.poster = poster;
        this.eventDate = eventDate;
        this.raffleDate = raffleDate;
        this.geoLocationRequired = geoLocationRequired;
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

    public Calendar getEventDate() {
        return eventDate;
    }

    public Calendar getRaffleDate() {
        return raffleDate;
    }

    public void setEventDate(Calendar eventDate) {
        this.eventDate = eventDate;
    }

    public void setRaffleDate(Calendar raffleDate) {
        this.raffleDate = raffleDate;
    }
    /*public void setQRCode(QRCode qrCode) {  -- QR code class is not ready yet
        this.qrCode = qrCode;
    }*/
}
