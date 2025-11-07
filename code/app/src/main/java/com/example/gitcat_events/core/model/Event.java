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
     * @param poster the poster of an event which can't be null 
     * @param raffleDate the date of the raffle for the event
     * @param eventDate the date of the event taking place 
     * @param geoLocationRequired a boolean value to determine if geolocation is required for the event or not.
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
    /**
     * 
     * Gets the name of the event 
     * @return the name of the event
     */
    public String getName() {
        return this.name;
    }

    /**
     * 
     * Get the descriptipn of the event 
     * 
     * @return the description of the event
     */
    public String getDescription() {
        return description;
    }

    /**
     * 
     * set the description of an event 
     * 
     * @param description the description of the event which cant be null
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 
     * Gets the capacity of an event 
     * 
     * @return the capcity of the event 
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * 
     * gets the organizer of an event 
     * 
     * @return the organizer of the event 
     */

    public int getOrganizer() {
        return organizer;
    }

    /**
     * 
     * gets the maximum wait list size of an event.
     * 
     * @return the maximum wait list size of the event
     */
    @Nullable
    public Integer getMaxWaitListSize() {
        return maxWaitListSize;
    }

    /**
     * 
     * gets the poster of an event 
     * @return the poster of an event 
     */
    public String getPoster() {
        return poster;
    }

    /**
     * 
     * sets the capacity of an event 
     * @param capacity the capacity that an event can hold 
     */

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * 
     * sets the maximum wait list size of an event.
     * @param maxWaitListSize the maxmimum size of waitlist for an event. 
     */
    public void setMaxWaitListSize(@Nullable Integer maxWaitListSize) {
        this.maxWaitListSize = maxWaitListSize;
    }

    /**
     * 
     * sets the name of an event 
     * @param name the name of the event which cant be null 
     */

    public void setName(String name) {
        this.name = name;
    }


    /**
     * 
     * sets the organizer of an event 
     * @param organizer the organizer who hosts the event 
     */

    public void setOrganizer(int organizer) {
        this.organizer = organizer;
    }

    /**
     * 
     * sets the poster of an event 
     * @param poster the poster of an event which cant be null
     */

    public void setPoster(String poster) {
        this.poster = poster;
    }

    /**
     * 
     * gets the date of the event taking place. 
     * @return the date of the event taking place 
     */

    public Calendar getEventDate() {
        return eventDate;
    }

    /**
     * 
     * gets the date of the raffle for the event 
     * @return the date of the raffle for the event 
     */
    public Calendar getRaffleDate() {
        return raffleDate;
    }

    /**
     * 
     * sets the date of the event taking place.
     * @param eventDate the data of the event taking place 
     */
    public void setEventDate(Calendar eventDate) {
        this.eventDate = eventDate;
    }


    /**
     * 
     * sets the date of the raffle for the event 
     * @param raffleDate the date of the raffle for the event 
     */
    public void setRaffleDate(Calendar raffleDate) {
        this.raffleDate = raffleDate;
    }
    /*public void setQRCode(QRCode qrCode) {  -- QR code class is not ready yet
        this.qrCode = qrCode;
    }*/
}
