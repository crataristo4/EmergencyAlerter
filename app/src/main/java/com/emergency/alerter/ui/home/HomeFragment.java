package com.emergency.alerter.ui.home;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.camerakit.CameraKitView;
import com.emergency.alerter.MainActivity;
import com.emergency.alerter.R;
import com.emergency.alerter.databinding.FragmentHomeBinding;
import com.emergency.alerter.utils.AppConstants;
import com.emergency.alerter.utils.DisplayViewUI;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static android.os.Environment.*;

public class HomeFragment extends Fragment {
    String currentPhotoPath;
    private FragmentHomeBinding fragmentHomeBinding;
    private Bitmap bitmap ;
    private StorageReference imageStorageRef;
    private DatabaseReference dbRef;
    static final int REQUEST_TAKE_PHOTO = 1;
    ImageView imageView;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fragmentHomeBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_home, container, false);
        return fragmentHomeBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageStorageRef = FirebaseStorage.getInstance().getReference().child("alerts");
        dbRef = FirebaseDatabase.getInstance().getReference().child("alerts");
        imageView = fragmentHomeBinding.imageView;



        fragmentHomeBinding.textHome.setOnClickListener(v -> dispatchTakePictureIntent());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppConstants.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){

            if (data != null) {
                bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data") ;
                imageView.setImageBitmap(bitmap);
               // uploadData();
                setPic();




        }

        }
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);


        imageView.setImageBitmap(bitmap);

        ProgressDialog pd =  DisplayViewUI.displayProgress(requireActivity(),"uploading");
        pd.show();

        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        byte [] bytes = boas.toByteArray();
       bitmap.compress(Bitmap.CompressFormat.JPEG,100,boas);

        Log.i( "width and height : ", photoW + "... " + photoH);


        StorageReference filePath = imageStorageRef.child("test");

        filePath.putBytes(bytes).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                pd.dismiss();

            }
            return filePath.getDownloadUrl();

        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                pd.dismiss();

                Uri downLoadUri = task.getResult();
                assert downLoadUri != null;
                String     getImageUploadUri = downLoadUri.toString();
                Log.i( "Url: ", getImageUploadUri );

                //fire store cloud store
                   /* dbReference.add(itemsMap).addOnCompleteListener(task2 -> {

                        if (task2.isSuccessful()) {
                            serviceTypeDbRef.child(uid).child(randomUID).setValue(itemsMap);

                            progressDialog.dismiss();
                            DisplayViewUI.displayToast(this, "Successful");

                            startActivity(new Intent(AddDesignOrStyleActivity.this, MainActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                            finish();


                        } else {
                            pd.dismiss();
                            DisplayViewUI.displayToast(this, Objects.requireNonNull(task2.getException()).getMessage());

                        }

                    });
*/
            } else {
                pd.dismiss();
                DisplayViewUI.displayToast(requireContext(), Objects.requireNonNull(task.getException()).getMessage());

            }

        });

    }



    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
                        "com.emergency.alerter.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void uploadData() {


        ProgressDialog pd =  DisplayViewUI.displayProgress(requireActivity(),"uploading");
        pd.show();

        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        byte [] bytes = boas.toByteArray();
        //bitmap.compress(Bitmap.CompressFormat.JPEG,100,boas);

        Log.i( "width and height : ", width + "... " + height);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;



        BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        Log.i( "Data: ", "width" +  options.outWidth + " height " + options.outHeight);


        StorageReference filePath = imageStorageRef.child("test");

        filePath.putBytes(bytes).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                pd.dismiss();

            }
            return filePath.getDownloadUrl();

        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                pd.dismiss();

                Uri downLoadUri = task.getResult();
                assert downLoadUri != null;
                String     getImageUploadUri = downLoadUri.toString();
                Log.i( "Url: ", getImageUploadUri );

                //fire store cloud store
                   /* dbReference.add(itemsMap).addOnCompleteListener(task2 -> {

                        if (task2.isSuccessful()) {
                            serviceTypeDbRef.child(uid).child(randomUID).setValue(itemsMap);

                            progressDialog.dismiss();
                            DisplayViewUI.displayToast(this, "Successful");

                            startActivity(new Intent(AddDesignOrStyleActivity.this, MainActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                            finish();


                        } else {
                            pd.dismiss();
                            DisplayViewUI.displayToast(this, Objects.requireNonNull(task2.getException()).getMessage());

                        }

                    });
*/
            } else {
                pd.dismiss();
                DisplayViewUI.displayToast(requireContext(), Objects.requireNonNull(task.getException()).getMessage());

            }

        });

    }


}