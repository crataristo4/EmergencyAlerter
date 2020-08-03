package com.dalilu.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class ShareLocation extends BaseObservable {
    public String location;
    public String photoUrl;

    public ShareLocation() {
    }

    @Bindable
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
