package com.example.gitcat_events.core.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
public class Profile implements Serializable{
    private String name;
    private String email;
    @Nullable private String phone; // nullable/optional

    public Profile() {}                   // required for toObject(...)
    public Profile(String name, String email, @Nullable String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
    public String getName() {
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email){
        this.email = email;
    }
    @Nullable public String getPhone(){
        return  phone;
    }
    public void setPhone(@Nullable String phone){
        this.phone = phone;
    }
}
