package com.example.gitcat_events.core.model;

public class QRCode {
    //based on the CRC card in the wiki: https://github.com/CMPUT301F25gitcat/gitcat-events/wiki/Part-3:-CRC-Cards#qrcode
    //TODO: javadocs
    //purpose of this class is just to hold data about the QR code.
    private String image;
    private int eventID;
    public QRCode(String image, int eventID){
        this.image=image;
        this.eventID=eventID;
    }

    //getters and setters:

    public int getEventID() {
        return eventID;
    }

    public String getImage() {
        return image;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
