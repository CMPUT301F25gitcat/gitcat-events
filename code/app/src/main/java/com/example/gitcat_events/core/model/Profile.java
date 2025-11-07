package com.example.gitcat_events.core.model;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Represents a user's profile containing personal information.
 * Implements serializable to allow profile objects to be serialized for storage. 
 * 
 * @author Ryan Chattopadhyay
 * 
 * @see Profile 
 * 
 * 
 */
public class Profile implements Serializable{
    @Nullable private String name; // optional
    @Nullable private String email; // optional
    @Nullable private String phone; // optional
    private String deviceId; // REQUIRED - unique device identifier
    @Nullable private String profilePictureUrl; // optional

    /**
     * default constructor for serializable 
     * required for toObject(...)
     */
    public Profile() {}
    
    /**
     * constructing a profile with minimal info (deviceId only).
     * @param deviceId the unique device identifier which cannot be null 
     */
    public Profile(String deviceId) {
        this.deviceId = deviceId;
    }
    
    /**
     * constructing a profile with all info.
     * @param name the user's full name and it can be null 
     * @param email the users email address and it can be null 
     * @param phone the users phone number and it can be null 
     * @param deviceId the unique device identifier which cannot be null 
     * @param profilePictureUrl the URL to the users profile pic and can be null.
     */
    public Profile(@Nullable String name, @Nullable String email, @Nullable String phone, String deviceId, @Nullable String profilePictureUrl) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.deviceId = deviceId;
        this.profilePictureUrl = profilePictureUrl;
    }

    /**
     * Gets the name of the user 
     * @return the user's full name 
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * sets the name of the user 
     * @param name sets the name of the user and it can be null 
     */
    public void setName(@Nullable String name){
        this.name = name;
    }

    /**
     * gets the email of the user 
     * @return the users email address 
     */
    @Nullable
    public String getEmail() {
        return email;
    }
    
    /**
     * sets the email of the user 
     * @param email the users email address 
     */
    public void setEmail(@Nullable String email){
        this.email = email;
    }

    /**
     * get the phone number of the user which can be null 
     * @return the users phone number 
     */
    @Nullable
    public String getPhone(){
        return  phone;
    }

    /**
     * sets the phone number of the user which can be null 
     * @param phone the users phone number 
     */
    public void setPhone(@Nullable String phone){
        this.phone = phone;
    }
    
    /**
     * gets the unique device ID which cannot be null 
     * @return the unique device Identifier(ID)
     */
    public String getDeviceId(){
        return deviceId;
    }

    /**
     * sets the unique device ID which cannot be null 
     * @param deviceId  which sets the unique device Identifier(ID)  
     */
    public void setDeviceId(String deviceId){
        this.deviceId = deviceId;
    }

    /**
     * gets the profile picture URL of the user profile which can be null 
     * @return the profile picture URL 
     */
    @Nullable public String getProfilePictureUrl(){
        return profilePictureUrl;
    }

    /**
     * sets the profile picture URL which can be null 
     * @param profilePictureUrl the profile pic URL 
     */
    public void setProfilePictureUrl(@Nullable String profilePictureUrl){
        this.profilePictureUrl = profilePictureUrl;
    }
}
