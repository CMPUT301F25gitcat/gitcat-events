package com.example.gitcat_events.core.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
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
public class Event implements Serializable {
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
    private String organizerDeviceId; // Device ID of the organizer (permanent identifier)
    private String documentId; // Firestore document ID (not stored in DB, set when loaded)
    @Nullable private String selectionCriteria; // Guidelines/criteria for lottery selection

    /**
     * No-arg constructor for Firebase
     */
    public Event() {
    }

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
     * sets the name of an event 
     * @param name the name of the event which cant be null 
     */
    public void setName(String name) {
        this.name = name;
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
     * sets the capacity of an event 
     * @param capacity the capacity that an event can hold 
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
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
     * sets the maximum wait list size of an event.
     * @param maxWaitListSize the maxmimum size of waitlist for an event. 
     */
    public void setMaxWaitListSize(@Nullable Integer maxWaitListSize) {
        this.maxWaitListSize = maxWaitListSize;
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
     * sets the date of the event taking place.
     * @param eventDate the data of the event taking place 
     */
    public void setEventDate(Calendar eventDate) {
        this.eventDate = eventDate;
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
     * sets the date of the raffle for the event 
     * @param raffleDate the date of the raffle for the event 
     */
    public void setRaffleDate(Calendar raffleDate) {
        this.raffleDate = raffleDate;
    }

    /**
     * 
     * gets whether geolocation is required for the event
     * @return true if geolocation is required, false otherwise
     */
    public Boolean getGeoLocationRequired() {
        return geoLocationRequired;
    }

    /**
     * 
     * sets whether geolocation is required for the event
     * @param geoLocationRequired true if geolocation is required, false otherwise
     */
    public void setGeoLocationRequired(Boolean geoLocationRequired) {
        this.geoLocationRequired = geoLocationRequired;
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
     * sets the poster of an event 
     * @param poster the poster of an event which cant be null
     */
    public void setPoster(String poster) {
        this.poster = poster;
    }

    /**
     * 
     * gets the organizer device ID of an event 
     * 
     * @return the organizer device ID of the event 
     */
    public String getOrganizerDeviceId() {
        return organizerDeviceId;
    }

    /**
     * 
     * sets the organizer device ID of an event 
     * @param organizerDeviceId the organizer device ID who hosts the event 
     */
    public void setOrganizerDeviceId(String organizerDeviceId) {
        this.organizerDeviceId = organizerDeviceId;
    }

    /**
     * 
     * gets the document ID of the event in Firestore
     * 
     * @return the document ID of the event
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * 
     * sets the document ID of the event in Firestore
     * @param documentId the document ID of the event
     */
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // this is the selection criteria 
    /**
     * this gets the selction criteraia for an event 
     * @return the selection criteria for an event which can be null. 
     */
    @Nullable
    public String getSelectionCriteria() {
        return selectionCriteria;
    }

    /**
     * 
     * @param selectionCriteria set the selection criteria for an event.
     */

    public void setSelectionCriteria(@Nullable String selectionCriteria) {
        this.selectionCriteria = selectionCriteria;
    }

    /*public void setQRCode(QRCode qrCode) {  -- QR code class is not ready yet
        this.qrCode = qrCode;
    }*/
}