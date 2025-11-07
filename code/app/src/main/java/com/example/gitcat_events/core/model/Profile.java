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
    private String name;//user full name 
    private String email;//which is the user email address 
    @Nullable private String phone; // nullable/optional
    @Nullable private String deviceId; // unique device identifier
    @Nullable private String profilePictureUrl; // URL to profile picture
    //default constructor for serializable 
    public Profile() {}                   // required for toObject(...)
    /**
     *  constructing a profile with minimal info. 
     * @param name the user's full name can't be null
     * @param email the users email address and it cant be null 
     * @param phone the users phone number and it can't be null
     */
    public Profile(String name, String email, @Nullable String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
    /**
     * constructing a profile with all info.
     * @param name the user's full name and it cant be null 
     * @param email the users email address and it cant be null 
     * @param phone the users phone number and it can be null 
     * @param deviceId the unique device identifier which can be null 
     * @param profilePictureUrl the URL to the users profile pic and can be null.
     */
    public Profile(String name, String email, @Nullable String phone, @Nullable String deviceId, @Nullable String profilePictureUrl) {
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
    
    public String getName() {
        return name;
    }

    /**
     * sets the name of the user 
     * @param name sets the name of the user and it cant be null 
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * gets the email of the user 
     * @return the users email address 
     */
    public String getEmail() {
        return email;
    }
    /**
     * sets the email of the user 
     * @param email the users email address 
     */
    public void setEmail(String email){
        this.email = email;
    }

    /**
     * get the phone number of the user which can be null 
     * @return the users phone number 
     */
    @Nullable public String getPhone(){
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
     * gets the unique device ID which can be null 
     * @return the unique device Identifier(ID)
     */
    @Nullable public String getDeviceId(){
        return deviceId;
    }

    /**
     * sets the unique device ID which can be null 
     * @param deviceId  which sets the unique device Identifier(ID)  
     */
    public void setDeviceId(@Nullable String deviceId){
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
