package com.example.gitcat_events.core.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Represents an event that users can register for. 
 * Events have a capacity, optional waitlist size, dates via calendar objects, and a boolean for if geolocation is required.
 * 
 * @author Momoore Oshinaike 
 * @see AcceptedListEntry
 * @see WaitListEntry
 * @see Entry
 */
public class Event implements Serializable {
    // Based off CRC card in wiki: https://github.com/CMPUT301F25gitcat/gitcat-events/wiki/Part-3:-CRC-Cards#event
    private String name; // Name of the event
    private String description; // Description of the event
    private int capacity; // Refers to the size of the final list (maximum number of people that can attend)
    @Nullable 
    private Integer maxWaitListSize; // Maximum size of waitlist for the event
    private Calendar eventDate; // Date when the event takes place
    private Calendar registrationStartDate; // When registration opens
    private Calendar raffleDate; // When registration closes (raffle date)
    private Boolean geoLocationRequired; // Whether geolocation is required for the event
    
    // Current plan is to store images as strings
    private String poster; // Base64 encoded poster image
    
    // Legacy field (kept for compatibility with Firestore)
    private int organizer; // Legacy organizer ID (integer)
    
    // Additional fields for Firestore integration and enhanced functionality
    @Nullable 
    private String documentId; // Firestore document ID (not stored in DB, set when loaded)
    @Nullable 
    private String organizerDeviceId; // Device ID of the organizer (permanent identifier)
    @Nullable 
    private String selectionCriteria; // Guidelines/criteria for lottery selection
    
    //private QRCode qrCode; -- QRCode class is not done yet

    /**
     * No-arg constructor for Firebase
     */
    public Event() {
    }

    /**
     * Constructs an event object with the following parameters:
     * 
     * @param name the name of the event (cannot be null)
     * @param description a brief and concise description of the event
     * @param capacity the maximum number of people that can attend the event (must be positive)
     * @param maxWaitListSize the maximum size of waitlist (should be positive, can be null)
     * @param poster the poster of an event (Base64 encoded string, cannot be null)
     * @param registrationStartDate the date when registration opens
     * @param raffleDate the date of the raffle for the event (when registration closes)
     * @param eventDate the date of the event taking place
     * @param geoLocationRequired a boolean value to determine if geolocation is required for the event
     */
    public Event(String name, String description, int capacity, @Nullable Integer maxWaitListSize, 
                 String poster, Calendar registrationStartDate, Calendar raffleDate, 
                 Calendar eventDate, Boolean geoLocationRequired) {
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.maxWaitListSize = maxWaitListSize;
        this.poster = poster;
        this.registrationStartDate = registrationStartDate;
        this.raffleDate = raffleDate;
        this.eventDate = eventDate;
        this.geoLocationRequired = geoLocationRequired;
        //this.qrCode = new QRCode; -- QR code class not ready yet
    }

    // Getters and Setters (organized by field)
    
    /**
     * Gets the name of the event
     * 
     * @return the name of the event
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of an event
     * 
     * @param name the name of the event (cannot be null)
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the event
     * 
     * @return the description of the event
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of an event
     * 
     * @param description the description of the event (cannot be null)
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the capacity of an event
     * 
     * @return the capacity of the event
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Sets the capacity of an event
     * 
     * @param capacity the capacity that an event can hold (must be positive)
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Gets the maximum waitlist size of an event
     * 
     * @return the maximum waitlist size of the event (can be null)
     */
    @Nullable
    public Integer getMaxWaitListSize() {
        return maxWaitListSize;
    }

    /**
     * Sets the maximum waitlist size of an event
     * 
     * @param maxWaitListSize the maximum size of waitlist for an event (should be positive, can be null)
     */
    public void setMaxWaitListSize(@Nullable Integer maxWaitListSize) {
        this.maxWaitListSize = maxWaitListSize;
    }

    /**
     * Gets the date of the event taking place
     * 
     * @return the date of the event taking place
     */
    public Calendar getEventDate() {
        return eventDate;
    }

    /**
     * Sets the date of the event taking place
     * 
     * @param eventDate the date of the event taking place
     */
    public void setEventDate(Calendar eventDate) {
        this.eventDate = eventDate;
    }

    /**
     * Gets the date when registration opens
     * 
     * @return the date when registration opens
     */
    public Calendar getRegistrationStartDate() {
        return registrationStartDate;
    }

    /**
     * Sets the date when registration opens
     * 
     * @param registrationStartDate the date when registration opens
     */
    public void setRegistrationStartDate(Calendar registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    /**
     * Gets the date of the raffle for the event (when registration closes)
     * 
     * @return the date of the raffle for the event
     */
    public Calendar getRaffleDate() {
        return raffleDate;
    }

    /**
     * Sets the date of the raffle for the event (when registration closes)
     * 
     * @param raffleDate the date of the raffle for the event
     */
    public void setRaffleDate(Calendar raffleDate) {
        this.raffleDate = raffleDate;
    }

    /**
     * Gets whether geolocation is required for the event
     * 
     * @return true if geolocation is required, false otherwise
     */
    public Boolean getGeoLocationRequired() {
        return geoLocationRequired;
    }

    /**
     * Sets whether geolocation is required for the event
     * 
     * @param geoLocationRequired true if geolocation is required, false otherwise
     */
    public void setGeoLocationRequired(Boolean geoLocationRequired) {
        this.geoLocationRequired = geoLocationRequired;
    }

    /**
     * Gets the poster of an event
     * 
     * @return the poster of an event (Base64 encoded string)
     */
    public String getPoster() {
        return poster;
    }

    /**
     * Sets the poster of an event
     * 
     * @param poster the poster of an event (Base64 encoded string, cannot be null)
     */
    public void setPoster(String poster) {
        this.poster = poster;
    }

    /**
     * Gets the organizer ID (legacy field, kept for compatibility)
     * 
     * @return the organizer ID as an integer
     */
    public int getOrganizer() {
        return organizer;
    }

    /**
     * Sets the organizer ID (legacy field, kept for compatibility)
     * 
     * @param organizer the organizer ID as an integer
     */
    public void setOrganizer(int organizer) {
        this.organizer = organizer;
    }

    /**
     * Gets the organizer device ID of an event
     * 
     * @return the organizer device ID of the event (can be null)
     */
    @Nullable
    public String getOrganizerDeviceId() {
        return organizerDeviceId;
    }

    /**
     * Sets the organizer device ID of an event
     * 
     * @param organizerDeviceId the organizer device ID who hosts the event
     */
    public void setOrganizerDeviceId(@Nullable String organizerDeviceId) {
        this.organizerDeviceId = organizerDeviceId;
    }

    /**
     * Gets the document ID of the event in Firestore
     * 
     * @return the document ID of the event (can be null)
     */
    @Nullable
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Sets the document ID of the event in Firestore
     * 
     * @param documentId the document ID of the event
     */
    public void setDocumentId(@Nullable String documentId) {
        this.documentId = documentId;
    }

    /**
     * Gets the selection criteria for an event
     * 
     * @return the selection criteria for an event (can be null)
     */
    @Nullable
    public String getSelectionCriteria() {
        return selectionCriteria;
    }

    /**
     * Sets the selection criteria for an event
     * 
     * @param selectionCriteria the selection criteria for an event
     */
    public void setSelectionCriteria(@Nullable String selectionCriteria) {
        this.selectionCriteria = selectionCriteria;
    }

    /**
     * Gets the raffle date as a formatted string
     * 
     * @return the raffle date formatted as "yyyy-MM-dd"
     */
    public String getRaffleDateString() {
        if (raffleDate == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(this.raffleDate.getTime());
    }

    /**
     * Gets the event date as a formatted string
     * 
     * @return the event date formatted as "yyyy-MM-dd"
     */
    public String getEventDateString() {
        if (eventDate == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(this.eventDate.getTime());
    }

    /*public void setQRCode(QRCode qrCode) {
        this.qrCode = qrCode;
    }*/
}
