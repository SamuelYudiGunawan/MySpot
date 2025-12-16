package com.example.myspot.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {
    
    private static final int MAX_IMAGE_SIZE = 1024; // Max width/height in pixels
    private static final int COMPRESSION_QUALITY = 80; // JPEG quality (0-100)
    
    /**
     * Compress and save image from URI
     * @param context The application context
     * @param imageUri The URI of the original image
     * @return The file path of the compressed image, or null if failed
     */
    public static String compressAndSaveImage(Context context, Uri imageUri) {
        try {
            // Read the original image
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }
            
            if (originalBitmap == null) {
                return null;
            }
            
            // Calculate new dimensions
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            
            if (width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE) {
                float scale = Math.min((float) MAX_IMAGE_SIZE / width, (float) MAX_IMAGE_SIZE / height);
                width = Math.round(width * scale);
                height = Math.round(height * scale);
            }
            
            // Resize bitmap
            Bitmap compressedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true);
            originalBitmap.recycle();
            
            // Save compressed image
            File imageFile = createImageFile(context);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream);
            outputStream.flush();
            outputStream.close();
            compressedBitmap.recycle();
            
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Create a file for storing the compressed image
     */
    private static File createImageFile(Context context) throws IOException {
        String imageFileName = "SPOT_" + System.currentTimeMillis();
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            storageDir = context.getFilesDir();
        }
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
    
    /**
     * Load bitmap from file path
     */
    public static Bitmap loadBitmapFromPath(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                return BitmapFactory.decodeFile(imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Load bitmap from URI
     */
    public static Bitmap loadBitmapFromUri(Context context, Uri imageUri) {
        if (imageUri == null) {
            return null;
        }
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

