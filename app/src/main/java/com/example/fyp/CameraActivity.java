package com.example.fyp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class CameraActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;

    ImageView photo = null;
    Mat imageGray = null;
    Mat imageBilateral = null;
    Mat imageThreshold = null;
    Mat imageBlur = null;
    Mat imageBorder = null;
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
        if( ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1);
        }
        setContentView(R.layout.activity_camera);
        photo = (ImageView) findViewById(R.id.bitmap_photo);
        openCamera();

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
                openCV();

            }
        });
    }

    public void back(){
        onBackPressed();
    }

    public void openCV(){
        if( ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1);
        }
        imageGray = new Mat();
        imageBilateral = new Mat();
        imageThreshold = new Mat();
        imageBlur = new Mat();
        imageBorder = new Mat();
        Mat imageSource = Imgcodecs.imread(currentPhotoPath);

        //Toast.makeText(this,"Opencv starting",Toast.LENGTH_LONG).show();
        Imgproc.cvtColor(imageSource,imageGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.bilateralFilter(imageGray,imageBilateral,9,75,75);
        Imgproc.adaptiveThreshold(imageBilateral,imageThreshold,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY,115,4);
        Imgproc.medianBlur(imageThreshold,imageBlur,11);
        Core.copyMakeBorder(imageBlur,imageBorder,5,5,5,5,Core.BORDER_CONSTANT);
        Imgproc.Canny(imageBorder,imageSource,10, 100, 3, true);
        Imgproc.GaussianBlur(imageSource,imageSource,new Size(5,5),5);

        //find the contours
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(imageSource, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = -1;
        int maxAreaIdx = -1;
        MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Mat largest_contour = contours.get(0);
        List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
        for (int idx = 0; idx < contours.size(); idx++) {
            temp_contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(temp_contour);
            //compare this contour to the previous largest contour found
            if (contourarea > maxArea) {
                //check if this contour is a square
                MatOfPoint2f new_mat = new MatOfPoint2f( temp_contour.toArray() );
                int contourSize = (int)temp_contour.total();
                Imgproc.approxPolyDP(new_mat, approxCurve, contourSize*0.05, true);
                if (approxCurve.total() == 4) {
                    maxArea = contourarea;
                    maxAreaIdx = idx;
                    largest_contours.add(temp_contour);
                    largest_contour = temp_contour;
                }
            }
        }
        MatOfPoint temp_largest = largest_contours.get(largest_contours.size()-1);
        largest_contours = new ArrayList<MatOfPoint>();
        largest_contours.add(temp_largest);

        Imgproc.cvtColor(imageSource, imageSource, Imgproc.COLOR_BayerBG2RGB);
        Imgproc.drawContours(imageSource, largest_contours, -1, new Scalar(0, 255, 0), 1);



//        List<MatOfPoint> contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(imageSource, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//        Mat drawing = Mat.zeros(imageSource.size(), CvType.CV_8UC3);
//
//
//        for (int i = 0; i < contours.size(); i++) {
//            Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
//            Imgproc.drawContours(drawing, contours, i, color, 2, Core.LINE_8, hierarchy, 0, new Point());
//        }

        Bitmap bm = Bitmap.createBitmap(imageSource.cols(), imageSource.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imageSource, bm);


        try{
            photo.setImageBitmap(bm);
        }catch(Exception ex){
            //Toast.makeText(this,ex.toString(), Toast.LENGTH_LONG).show();
            Log.d("bitmap",ex.toString());
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
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this,ex.toString(), Toast.LENGTH_LONG).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
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
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic();
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        try{
            Bitmap imageBitmap =  MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(currentPhotoPath)));

            photo.setImageBitmap(imageBitmap);
        }catch(Exception ex){
            //Toast.makeText(this,ex.toString(), Toast.LENGTH_LONG).show();
            Log.d("bitmap",ex.toString());
        }
    }

}
