package com.emergency.alerter.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class AlertItems extends BaseObservable {
    public String userName;
    public String imageUrl;
    public String videoUrl;
    public String audioUrl;
    public String userPhotoUrl;
    public Object timeStamp;
    public double latitude;
    public double longitude;
    public String reportDescription;
    public int type, audioLength;

    public AlertItems() {
    }

    //Image type constructor
    public AlertItems(int type, String userName, String imageUrl, String userPhotoUrl, Object timeStamp,
                      double latitude, double longitude, String reportDescription) {

        this.type = type;
        this.userName = userName;
        this.imageUrl = imageUrl;
        this.userPhotoUrl = userPhotoUrl;
        this.timeStamp = timeStamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.reportDescription = reportDescription;

    }

    //Video type constructor
    public AlertItems(int type, String userName, String videoUrl, String userPhotoUrl, Object timeStamp,
                      double latitude, double longitude) {

        this.type = type;
        this.userName = userName;
        this.videoUrl = videoUrl;
        this.userPhotoUrl = userPhotoUrl;
        this.timeStamp = timeStamp;
        this.latitude = latitude;
        this.longitude = longitude;

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


    @Bindable
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Bindable
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Bindable
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

    @Bindable
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

    @Bindable
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Bindable
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Bindable
    public String getReportDescription() {
        return reportDescription;
    }

    public void setReportDescription(String reportDescription) {
        this.reportDescription = reportDescription;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Bindable
    public int getAudioLength() {
        return audioLength;
    }

    public void setAudioLength(int audioLength) {
        this.audioLength = audioLength;
    }
}
