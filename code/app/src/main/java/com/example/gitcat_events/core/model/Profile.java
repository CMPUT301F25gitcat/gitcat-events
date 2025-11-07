package com.example.gitcat_events.core.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
/**
 * This class represents the user profile
 */
public class Profile implements Serializable{
    @Nullable private String name; // optional
    @Nullable private String email; // optional
    @Nullable private String phone; // optional
    private String deviceId; // REQUIRED - unique device identifier
    @Nullable private String profilePictureUrl; // optional

    public Profile() {}                   // required for toObject(...)

    // Constructor with deviceId only (minimal profile)
    public Profile(String deviceId) {
        this.deviceId = deviceId;
    }

    // Constructor with all fields
    public Profile(@Nullable String name, @Nullable String email, @Nullable String phone, String deviceId, @Nullable String profilePictureUrl) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.deviceId = deviceId;
        this.profilePictureUrl = profilePictureUrl;
    }

    @Nullable
    /**
     * This gets the user's name
     * @return
     * returns the user's name, can be null
     */
    public String getName() {
        return name;
    }
    /**
     * This sets the user's name
     * @param name
     * the new name for the user, can be null
     */
    public void setName(@Nullable String name){
        this.name = name;
    }

    @Nullable
    /**
     * This gets the user's email
     * @return
     * returns the user's email, can be null
     */
    public String getEmail() {
        return email;
    }
    /**
     * This sets the user's email
     * @param email
     * the new email for the user, can be null
     */
    public void setEmail(@Nullable String email){
        this.email = email;
    }

    @Nullable
    /**
     * This gets the user's phone number
     * @return
     * returns the user's phone number, can be null
     */
    public String getPhone(){
        return  phone;
    }
    /**
     * This sets the user's phone number
     * @param phone
     * the new phone number for the user, can be null
     */
    public void setPhone(@Nullable String phone){
        this.phone = phone;
    }
    /**
     * This gets the device ID
     * @return
     * returns the unique device identifier
     */
    public String getDeviceId(){
        return deviceId;
    }
    /**
     * This sets the device ID
     * @param deviceId
     * the new device identifier (required)
     */
    public void setDeviceId(String deviceId){
        this.deviceId = deviceId;
    }
    /**
     * This gets the profile picture URL
     * @return
     * returns the profile picture URL, can be null
     */
    @Nullable public String getProfilePictureUrl(){
        return profilePictureUrl;
    }
    /**
     * This sets the profile picture URL
     * @param profilePictureUrl
     * the new profile picture URL, can be null
     */
    public void setProfilePictureUrl(@Nullable String profilePictureUrl){
        this.profilePictureUrl = profilePictureUrl;
    }
}
