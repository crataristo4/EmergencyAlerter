package com.dalilu.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

public class FileUtils {

    public static File createFileWithExtension(String extension) {
        File path = Environment.getExternalStoragePublicDirectory(
                "Dalilu/Audio");
        File file = new File(path, UUID.randomUUID() + "-" + Calendar.getInstance()
                .getTimeInMillis() + "." + extension);

        if (!path.exists()) {
            path.mkdirs();
        }
        try {


            file.createNewFile();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
