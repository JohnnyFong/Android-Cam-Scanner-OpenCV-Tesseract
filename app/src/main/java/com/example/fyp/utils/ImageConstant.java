package com.example.fyp.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageConstant {

    public final static int REQUEST_IMAGE_CAPTURE = 1;
    public final static int REQUEST_IMAGE_GALLERY = 2;
    public static Bitmap selectedImageBitmap;

    public static boolean deleteImage(String filePath, Context context){
        boolean flag = false;
        File deletePath = new File(filePath);
        if(deletePath.exists()){
            if(deletePath.delete()){
                //send the boardcast to gallery
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(filePath))));
                flag = true;
                return flag;
            }
        }else{
            //file does not exist
            return flag;
        }
        return flag;
    }

    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Log.d("storage",storageDir.toString());
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        //currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static void galleryAddPic(String filePath, Context context) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public static void addPic(Bitmap bm, File file){
        if(file != null) {
            OutputStream fOutStream = null;
            try {
                fOutStream = new FileOutputStream(file);
                bm.compress(Bitmap.CompressFormat.PNG, 100, fOutStream);
                fOutStream.flush();
                fOutStream.close();

            } catch (FileNotFoundException ex) {

            } catch (IOException ex) {

            }
        }else{

        }
    }
}
