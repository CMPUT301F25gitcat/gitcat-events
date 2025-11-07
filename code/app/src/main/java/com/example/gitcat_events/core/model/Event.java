package com.example.gitcat_events.core.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Event implements Serializable {
    //TODO: javadocs, waitlist functions, accepted list functions, tests
    //based off CRC card in wiki: https://github.com/CMPUT301F25gitcat/gitcat-events/wiki/Part-3:-CRC-Cards#event
    private String name;
    private String description;
    private int capacity;   //refers to the size of the final list
    @Nullable private Integer maxWaitListSize;
    private Calendar eventDate;
    private Calendar registrationStartDate; // When registration opens
    private Calendar raffleDate; // When registration closes (renamed for clarity, but keeping field name for compatibility)
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

    public Event(String name, String description, int capacity, @Nullable Integer maxWaitListSize, String poster, Calendar registrationStartDate, Calendar raffleDate, Calendar eventDate, Boolean geoLocationRequired) {
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.maxWaitListSize = maxWaitListSize;
        this.poster = poster;
        this.registrationStartDate = registrationStartDate;
        this.eventDate = eventDate;
        this.raffleDate = raffleDate;
        this.geoLocationRequired = geoLocationRequired;
        //this.qrCode = new QRCode;  --QR code class not ready yet
    }


    // Getters and Setters (organized by field)
    
    // Name
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Capacity
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // Max Waitlist Size
    @Nullable
    public Integer getMaxWaitListSize() {
        return maxWaitListSize;
    }

    public void setMaxWaitListSize(@Nullable Integer maxWaitListSize) {
        this.maxWaitListSize = maxWaitListSize;
    }

    // Event Date
    public Calendar getEventDate() {
        return eventDate;
    }

    public void setEventDate(Calendar eventDate) {
        this.eventDate = eventDate;
    }

    // Registration Start Date
    public Calendar getRegistrationStartDate() {
        return registrationStartDate;
    }

    public void setRegistrationStartDate(Calendar registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    // Raffle Date (Final Registration Date)
    public Calendar getRaffleDate() {
        return raffleDate;
    }

    public void setRaffleDate(Calendar raffleDate) {
        this.raffleDate = raffleDate;
    }

    // Geolocation Required
    public Boolean getGeoLocationRequired() {
        return geoLocationRequired;
    }

    public void setGeoLocationRequired(Boolean geoLocationRequired) {
        this.geoLocationRequired = geoLocationRequired;
    }

    // Poster
    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    // Organizer Device ID
    public String getOrganizerDeviceId() {
        return organizerDeviceId;
    }

    public void setOrganizerDeviceId(String organizerDeviceId) {
        this.organizerDeviceId = organizerDeviceId;
    }

    // Document ID (Firestore)
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // Selection Criteria
    @Nullable
    public String getSelectionCriteria() {
        return selectionCriteria;
    }

    public void setSelectionCriteria(@Nullable String selectionCriteria) {
        this.selectionCriteria = selectionCriteria;
    }

    /*public void setQRCode(QRCode qrCode) {  -- QR code class is not ready yet
        this.qrCode = qrCode;
    }*/
}
