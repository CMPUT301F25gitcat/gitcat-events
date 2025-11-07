package com.example.gitcat_events.core.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * This class represents an event that users can register for and attend
 */
public class Event implements Serializable {
    //TODO: javadocs, waitlist functions, accepted list functions, tests
    //based off CRC card in wiki: https://github.com/CMPUT301F25gitcat/gitcat-events/wiki/Part-3:-CRC-Cards#event
    private String name;
    private String description;
    private int capacity;   //refers to the size of the final list
    @Nullable private Integer maxWaitListSize;
    private Calendar eventDate;
    private Calendar raffleDate;
    private Boolean geoLocationRequired;

    //current plan is to store images as strings
    private String poster;
    //private QRCode qrCode; --QRCode class is not done yet
    private String organizerDeviceId; // Device ID of the organizer (permanent identifier)
    private String documentId; // Firestore document ID (not stored in DB, set when loaded)
    @Nullable private String selectionCriteria; // Guidelines/criteria for lottery selection

    // No-arg constructor for Firebase
    public Event() {
    }

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


    // Getters and Setters (organized by field)
    
    // Name
    /**
     * This gets the name of the event
     * @return
     * returns the event name
     */
    public String getName() {
        return this.name;
    }
    /**
     * This sets the name of the event
     * @param name
     * the new name of the event
     */
    public void setName(String name) {
        this.name = name;
    }

    // Description
    /**
     * This gets the description of the event
     * @return
     * returns the event description
     */
    public String getDescription() {
        return description;
    }
    /**
     * This sets the description of the event
     * @param description
     * the new description of the event
     */
    public void setDescription(String description) {
        this.description = description;
    }

    // Capacity
    /**
     * This gets the capacity of the event
     * @return
     * returns the event capacity (size of final list)
     */
    public int getCapacity() {
        return capacity;
    }
    /**
     * This sets the capacity of the event
     * @param capacity
     * the new capacity of the event
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // Max Waitlist Size
    @Nullable
    /**
     * This gets the maximum waitlist size
     * @return
     * returns the maximum waitlist size, can be null
     */
    public Integer getMaxWaitListSize() {
        return maxWaitListSize;
    }

    /**
     * This sets the maximum waitlist size
     * @param maxWaitListSize
     * the new maximum waitlist size, can be null
     */
    public void setMaxWaitListSize(@Nullable Integer maxWaitListSize) {
        this.maxWaitListSize = maxWaitListSize;
    }

    // Event Date
    /**
     * This gets the event date
     * @return
     * returns the date when the event occurs
     */
    public Calendar getEventDate() {
        return eventDate;
    }

    /**
     * This sets the event date
     * @param eventDate
     * the new event date
     */
    public void setEventDate(Calendar eventDate) {
        this.eventDate = eventDate;
    }

    // Raffle Date (Final Registration Date)
    /**
     * This gets the raffle date (final registration date)
     * @return
     * returns the raffle date
     */
    public Calendar getRaffleDate() {
        return raffleDate;
    }
    /**
     * This sets the raffle date (final registration date)
     * @param raffleDate
     * the new raffle date
     */
    public void setRaffleDate(Calendar raffleDate) {
        this.raffleDate = raffleDate;
    }

    // Geolocation Required
    /**
     * This gets whether geolocation is required
     * @return
     * returns true if geolocation is required, false otherwise
     */
    public Boolean getGeoLocationRequired() {
        return geoLocationRequired;
    }
    /**
     * This sets whether geolocation is required
     * @param geoLocationRequired
     * the new geolocation requirement setting
     */
    public void setGeoLocationRequired(Boolean geoLocationRequired) {
        this.geoLocationRequired = geoLocationRequired;
    }

    // Poster
    /**
     * This gets the poster image
     * @return
     * returns the poster image string
     */
    public String getPoster() {
        return poster;
    }

    /**
     * This sets the poster image
     * @param poster
     * the new poster image string
     */
    public void setPoster(String poster) {
        this.poster = poster;
    }

    // Organizer Device ID
    /**
     * This gets the organizer device ID
     * @return
     * returns the organizer device ID
     */
    public String getOrganizerDeviceId() {
        return organizerDeviceId;
    }

    /**
     * This sets the organizer device ID
     * @param organizerDeviceId
     * the new organizer device ID
     */
    public void setOrganizerDeviceId(String organizerDeviceId) {
        this.organizerDeviceId = organizerDeviceId;
    }

    // Document ID (Firestore)
    /**
     * This gets the Firestore document ID
     * @return
     * returns the Firestore document ID
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * This sets the Firestore document ID
     * @param documentId
     * the new Firestore document ID
     */
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // Selection Criteria
    @Nullable
    /**
     * This gets the selection criteria
     * @return
     * returns the selection criteria, can be null
     */
    public String getSelectionCriteria() {
        return selectionCriteria;
    }

    /**
     * This sets the selection criteria
     * @param selectionCriteria
     * the new selection criteria, can be null
     */
    public void setSelectionCriteria(@Nullable String selectionCriteria) {
        this.selectionCriteria = selectionCriteria;
    }

    /*public void setQRCode(QRCode qrCode) {  -- QR code class is not ready yet
        this.qrCode = qrCode;
    }*/
}
