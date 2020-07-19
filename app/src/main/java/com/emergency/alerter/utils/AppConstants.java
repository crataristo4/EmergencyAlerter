package com.emergency.alerter.utils;


import com.emergency.alerter.R;


public final class AppConstants {

    public static final int VIDEO_TYPE = 0;
    public static final int IMAGE_TYPE = 1;
    public static final int VIEW_TYPE_LOADING = 2;
    public static final int AUDIO_TYPE = 3;
    public static final int CAMERA_REQUEST_CODE = 1;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    // key to store image path in savedInstance state
    public static final String KEY_IMAGE_STORAGE_PATH = "image_path";

    // Bitmap sampling size
    public static final int BITMAP_SAMPLE_SIZE = 8;

    // Gallery directory name to store the images or videos
    public static final String GALLERY_DIRECTORY_NAME = String.valueOf(R.string.app_name);

    // Image and Video file extensions
    public static final String IMAGE_EXTENSION = "jpg";
    public static final String VIDEO_EXTENSION = "mp4";

    public static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;

}
