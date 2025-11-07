package com.example.gitcat_events.core.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
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
    public String getName() {
        return name;
    }
    public void setName(@Nullable String name){
        this.name = name;
    }
    
    @Nullable
    public String getEmail() {
        return email;
    }
    public void setEmail(@Nullable String email){
        this.email = email;
    }
    
    @Nullable
    public String getPhone(){
        return  phone;
    }
    public void setPhone(@Nullable String phone){
        this.phone = phone;
    }
    
    public String getDeviceId(){
        return deviceId;
    }
    public void setDeviceId(String deviceId){
        this.deviceId = deviceId;
    }
    @Nullable public String getProfilePictureUrl(){
        return profilePictureUrl;
    }
    public void setProfilePictureUrl(@Nullable String profilePictureUrl){
        this.profilePictureUrl = profilePictureUrl;
    }
}
