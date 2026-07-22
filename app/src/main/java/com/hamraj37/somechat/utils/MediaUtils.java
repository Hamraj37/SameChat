package com.hamraj37.somechat.utils;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MediaUtils {

    public static File getMediaDirectory(Context context, String type) {
        File baseDir = context.getExternalFilesDir(null); // App-specific external storage
        if (baseDir == null) baseDir = context.getFilesDir();

        String folderName = "SomeChat " + (type.equals("image") ? "Images" : 
                           (type.equals("video") ? "Videos" : 
                           (type.equals("voice") ? "Voice" : "Documents")));
        File dir = new File(baseDir, folderName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getLocalFileForMedia(Context context, String type, String mediaId) {
        return getLocalFileForMedia(context, type, mediaId, null);
    }

    public static File getLocalFileForMedia(Context context, String type, String mediaId, String fileName) {
        if (type.equals("file") && fileName != null) {
            return new File(getMediaDirectory(context, type), fileName);
        }
        String extension = type.equals("image") ? ".jpg" : (type.equals("video") ? ".mp4" : ".3gp");
        return new File(getMediaDirectory(context, type), mediaId + extension);
    }

    public static void saveMediaLocally(Context context, byte[] data, String type, String mediaId) {
        saveMediaLocally(context, data, type, mediaId, null);
    }

    public static void saveMediaLocally(Context context, byte[] data, String type, String mediaId, String fileName) {
        File file = getLocalFileForMedia(context, type, mediaId, fileName);
        if (file.exists()) return;

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isMediaDownloaded(Context context, String type, String mediaId) {
        return isMediaDownloaded(context, type, mediaId, null);
    }

    public static boolean isMediaDownloaded(Context context, String type, String mediaId, String fileName) {
        return getLocalFileForMedia(context, type, mediaId, fileName).exists();
    }
}
