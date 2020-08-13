package com.dalilu.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class Message extends BaseObservable {
    public String userName, receiverName;
    public String messageDateTime;
    private String message;
    public String id;
    public int type;
    public Object timeStamp;
    private String url;


    public Message() {
    }


    //TEXT type
    public Message(int type, String userName, Object timeStamp, String id, String message) {
        this.type = type;
        this.userName = userName;
        this.timeStamp = timeStamp;
        this.id = id;
        this.message = message;

    }

    //audio type
    public Message(int type, String userName, Object timeStamp, String url) {
        this.type = type;
        this.userName = userName;
        this.timeStamp = timeStamp;
        this.url = url;


    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTimeStamp() {
        return (long) timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }


    @Bindable
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Bindable
    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }


    @Bindable
    public String getMessageDateTime() {
        return messageDateTime;
    }

    public void setMessageDateTime(String messageDateTime) {
        this.messageDateTime = messageDateTime;
    }

    @Bindable
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
