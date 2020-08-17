package com.dalilu.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {

    private static final String POI_PATH = "root/poi";
    private static final String USER_PATH = "root/user";
    private static final String IMAGE_PATH = "root/images";
    private static FirebaseManager instance = new FirebaseManager();
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference root = firebaseDatabase.getReference("root");
    private DatabaseReference users = firebaseDatabase.getReference(USER_PATH);
    private final DatabaseReference pointsOfInterest = firebaseDatabase.getReference(POI_PATH);
    private final DatabaseReference images = firebaseDatabase.getReference(IMAGE_PATH);

    public FirebaseManager() {

    }

    public static FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public DatabaseReference getPOIRef() {
        return pointsOfInterest;
    }

    public DatabaseReference getImageRef() {
        return this.images;
    }

    //Save a POI in firebase
    public void createPOI(PointOfInterest poi) {
        String key = pointsOfInterest.push().getKey();
        Map<String, Object> childrenUpdates = new HashMap<>();
        poi.setKey(key);
        childrenUpdates.put("poi" + "/" + key, poi);

        root.updateChildren(childrenUpdates);
    }

    public void deletePOI(PointOfInterest poi) {
        String key = poi.getKey();
        Map<String, Object> childrenUpdates = new HashMap<>();
        childrenUpdates.put("poi/" + key, null);
        childrenUpdates.put("images/" + key, null);
        root.updateChildren(childrenUpdates);
    }

    //Write the encoded image in firebase
    public void writePoiImage(PointOfInterest poi, String encodedImage) {
        String key = poi.getKey();
        Map<String, Object> childrenUpdates = new HashMap<>();
        childrenUpdates.put("images/" + key, encodedImage);

        root.updateChildren(childrenUpdates);
    }

    //Helper method to decode from Base64 to Bitmap
    public Bitmap decodeFromBase64(String image) throws IOException {
        byte[] decodeByteArray = Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodeByteArray, 0, decodeByteArray.length);
    }


}
