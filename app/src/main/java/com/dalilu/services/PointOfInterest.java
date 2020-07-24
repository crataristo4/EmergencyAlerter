package com.dalilu.services;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class PointOfInterest implements Parcelable {

  //Create object from Parcel
  public static final Creator<PointOfInterest> CREATOR = new Creator<PointOfInterest>() {
    @Override
    public PointOfInterest createFromParcel(Parcel parcel) {
      return new PointOfInterest(parcel);
    }

    @Override
    public PointOfInterest[] newArray(int i) {
      return new PointOfInterest[i];
    }
  };
  //Key in firebase
  private String key;
  //Name of poi
  private String name;
  //Position of poi
  private double latitude;
  private double longitude;
  //Image encoded in Base 64
  private String encodedImage = "";
  //User
  private String user;
  private POIInteface poiInteface;

  //Firebase empty constructor
  public PointOfInterest() {
  }

  //Parcelable constructor
  public PointOfInterest(Parcel in) {
    this.name = in.readString();
    this.user = in.readString();
    this.key = in.readString();
    this.latitude = in.readDouble();
    this.longitude = in.readDouble();
    this.encodedImage = in.readString();
  }

  //Contstructor
  public PointOfInterest(String name, double latitude, double longitude) {
    this.name = name;
    this.latitude = latitude;
    this.longitude = longitude;
    this.user = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getLongitude() {
    return this.longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double getLatitude() {
    return this.latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public LatLng coordinatesToLatLng() {
    return new LatLng(latitude, longitude);
  }

  public String getEncodedImage() {
    return this.encodedImage;
  }

  public void setEncodedImage(String encodedImage) {
    this.encodedImage = encodedImage;
  }

  public String getUser() {
    return this.user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getKey() {
    return this.key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void latLngToPosition(LatLng position) {
    this.longitude = position.longitude;
    this.latitude = position.latitude;
  }

  public void queryImage() {
    //Query the image from firbase
    FirebaseManager.getInstance().getImageRef().child(getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        String image = dataSnapshot.getValue(String.class);
        setEncodedImage(image);
        poiInteface.onImageUpdate();
        //Set the string and call the callback method of the interface
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {
        Log.d("queryImage", "Cancelled");
      }
    });
  }

  //Method to register the callback interface
  public void registerCallbackInterface(POIInteface callback) {
    this.poiInteface = callback;
  }

  //Hashcode for hashmap(not used)
  @Override
  public int hashCode() {
    return Objects.hash(key, latitude, longitude, user);
  }

  @Override
  public boolean equals(Object obj) {
    PointOfInterest temp = (PointOfInterest) obj;
    return temp.getKey().equals(this.getKey());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  //Write data to parcel
  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(name);
    parcel.writeString(user);
    parcel.writeString(key);
    parcel.writeDouble(latitude);
    parcel.writeDouble(longitude);
    parcel.writeString(encodedImage);
  }

  public interface POIInteface {
    void onImageUpdate();
  }
}
