package com.dalilu.model;

public class ContactsModel {
    String phoneNumber;

    public ContactsModel() {
    }

    public ContactsModel(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
