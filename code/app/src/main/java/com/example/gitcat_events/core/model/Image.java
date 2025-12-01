package com.example.gitcat_events.core.model;

public class Image {
    private String url;
    private String origin;
    private String originId; // either device id or user id

    private String ownerName;
    public Image(String url, String origin, String originId, String ownerName) {
        this.url = url;
        this.origin = origin;
        this.originId = originId;
        this.ownerName = ownerName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }
}
