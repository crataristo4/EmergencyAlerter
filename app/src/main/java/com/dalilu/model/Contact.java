package com.dalilu.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;


public class Contact extends BaseObservable implements Parcelable {
    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };


    public String userName;
    public String phoneNumber;
    public String userPhotoUrl;
    public String userId;

    public Contact(String userName, String phoneNumber, String userPhotoUrl) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.userPhotoUrl = userPhotoUrl;
    }

    public Contact() {
    }

    public Contact(String userName, String phoneNumber) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;

    }

    protected Contact(Parcel in) {
        userName = in.readString();
        phoneNumber = in.readString();
        userPhotoUrl = in.readString();
        userId = in.readString();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Bindable
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Bindable
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public void setUserPhotoUrl(String userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userName);
        dest.writeString(phoneNumber);
        dest.writeString(userPhotoUrl);
        dest.writeString(userId);
    }
}
