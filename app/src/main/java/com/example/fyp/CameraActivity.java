package com.example.fyp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.fyp.utils.ImageConstant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


import static android.os.Environment.getExternalStoragePublicDirectory;

public class CameraActivity extends AppCompatActivity {

    int ImageMethod;
    String currentPhotoPath;
    Uri galleryImage;
    ImageView photo = null;
    Bitmap imageBitmap = null;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("opencv","OpenCV loaded successfully");

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageMethod = getIntent().getIntExtra("ImageMethod",0); //get the request code from previous activity
        // check if ImageMethod, if is 0, go back
        if(ImageMethod != 0) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1);
            }
            setContentView(R.layout.activity_camera);
            photo = (ImageView) findViewById(R.id.bitmap_photo);

            Button cancelButton = (Button) findViewById(R.id.Cancel_button);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    back();
                }
            });

            Button contButton = (Button) findViewById(R.id.Continue_button);
            contButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (imageBitmap != null) {
                        ImageConstant.selectedImageBitmap = imageBitmap;
                        Intent intent = new Intent(getApplicationContext(), CropImageActivity.class);
                        startActivity(intent);
                    }else{
                        Toast.makeText(getApplicationContext(),"No photo detected, please try again.",Toast.LENGTH_LONG).show();
                        back();
                    }
                }
            });

            //check the request code, 1 for image capture, 2 for choose from gallery
            if (ImageMethod == ImageConstant.REQUEST_IMAGE_CAPTURE) {
                openCamera();
            } else if (ImageMethod == ImageConstant.REQUEST_IMAGE_GALLERY) {
                loadGallery();
            }else{
                back();
            }

        }else{
            back();
        }
    }

    public void back(){
        onBackPressed();
    }


    public void loadGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, ImageMethod);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ImageConstant.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if(currentPhotoPath != null) {
                ImageConstant.galleryAddPic(currentPhotoPath, this);
                try{
                    imageBitmap =  MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(currentPhotoPath)));
                    photo.setImageBitmap(imageBitmap);
                }catch(Exception ex){
                    Toast.makeText(getApplicationContext(),"Opps, something went wrong. Picture is unable to be displayed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        }else if (requestCode == ImageConstant.REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            galleryImage = data.getData();
            try{
                InputStream inputStream = getContentResolver().openInputStream(this.galleryImage);
                imageBitmap = BitmapFactory.decodeStream(inputStream);
                photo.setImageBitmap(imageBitmap);
            }catch(Exception ex){
                Toast.makeText(getApplicationContext(),"Opps, something went wrong. Picture is unable to be displayed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void openCamera(){
        Toast.makeText(this,"Starting camera....", Toast.LENGTH_LONG).show();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = ImageConstant.createImageFile();
                currentPhotoPath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                Toast.makeText(this,ex.toString(), Toast.LENGTH_LONG).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, ImageMethod);
            }else{
                Toast.makeText(this,"photoFile is null.",Toast.LENGTH_SHORT).show();
            }
        }
    }

}
