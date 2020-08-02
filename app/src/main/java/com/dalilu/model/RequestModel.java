package com.dalilu.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class RequestModel extends BaseObservable {
    private String senderId;
    private String receiverId;
    private String response;
    private String phoneNumber;
    private String senderPhoto;
    private String senderName;
    private String receiverName;
    private String receiverPhoto;
    private String dateRequested;

    public RequestModel() {
    }


    public RequestModel(String senderId, String receiverId, String response, String senderPhoto, String senderName, String receiverName, String receiverPhoto) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.response = response;
        this.senderPhoto = senderPhoto;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.receiverPhoto = receiverPhoto;
    }

    @Bindable
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    @Bindable
    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    @Bindable
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @Bindable
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSenderPhoto() {
        return senderPhoto;
    }

    public void setSenderPhoto(String senderPhoto) {
        this.senderPhoto = senderPhoto;
    }

    @Bindable
    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    @Bindable
    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhoto() {
        return receiverPhoto;
    }

    public void setReceiverPhoto(String receiverPhoto) {
        this.receiverPhoto = receiverPhoto;
    }

    @Bindable
    public String getDateRequested() {
        return dateRequested;
    }

    public void setDateRequested(String dateRequested) {
        this.dateRequested = dateRequested;
    }
}
