package com.example.whatsapp;

import java.util.Calendar;

public class Contacts {

    public Contacts(){

    }

    public String name,status,image;

    public Contacts(String name, String ststus, String image) {
        this.name = name;
        this.status = ststus;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStstus() {
        return status;
    }

    public void setStstus(String ststus) {
        this.status = ststus;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
