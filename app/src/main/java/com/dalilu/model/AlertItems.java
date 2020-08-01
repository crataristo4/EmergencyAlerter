package com.dalilu.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class AlertItems extends BaseObservable {
    public String userName;
    public String alertPhotoUrl;
    public String videoUrl;
    public String audioUrl;
    public String userPhotoUrl;
    public Object timeStamp;
    public double latitude;
    public double longitude;
    public String dateReported;
    public String address;
    public String fullName;
    public String phoneNumber;
    public String status;
    public String id;
    public int type, audioLength;

    public AlertItems() {
    }

    //Image type constructor
    public AlertItems(int type, String fullName, String userPhotoUrl, String alertPhotoUrl, Object timeStamp,
                      String address, String id, String dateReported) {

        this.type = type;
        this.fullName = fullName;
        this.userPhotoUrl = userPhotoUrl;
        this.alertPhotoUrl = alertPhotoUrl;
        this.timeStamp = timeStamp;
        this.address = address;
        this.id = id;
        this.dateReported = dateReported;

    }

    //Video type constructor
    public AlertItems(int type, String userName, String videoUrl, String userPhotoUrl, Object timeStamp) {

        this.type = type;
        this.userName = userName;
        this.videoUrl = videoUrl;
        this.userPhotoUrl = userPhotoUrl;
        this.timeStamp = timeStamp;


    }

    //audio type url
    public AlertItems(int type, String userName, String audioUrl, String userPhotoUrl, Object timeStamp,
                      double latitude, double longitude, int audioLength) {

        this.type = type;
        this.userName = userName;
        this.audioUrl = audioUrl;
        this.userPhotoUrl = userPhotoUrl;
        this.timeStamp = timeStamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.audioLength = audioLength;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Bindable
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAlertPhotoUrl() {
        return alertPhotoUrl;
    }

    public void setAlertPhotoUrl(String alertPhotoUrl) {
        this.alertPhotoUrl = alertPhotoUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public void setUserPhotoUrl(String userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }

    public long getTimeStamp() {
        return (long) timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Bindable
    public String getDateReported() {
        return dateReported;
    }

    public void setDateReported(String dateReported) {
        this.dateReported = dateReported;
    }

    @Bindable
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Bindable
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Bindable
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Bindable
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAudioLength() {
        return audioLength;
    }

    public void setAudioLength(int audioLength) {
        this.audioLength = audioLength;
    }
}
