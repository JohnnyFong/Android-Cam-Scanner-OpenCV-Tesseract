package com.example.fyp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
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

import com.example.fyp.utils.ImageConstant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

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
import org.opencv.imgproc.Moments;
import org.opencv.utils.Converters;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class CameraActivity extends AppCompatActivity {

    int ImageMethod;
    String currentPhotoPath;
    Uri galleryImage;
    ImageView photo = null;
    Bitmap imageBitmap = null;
    Mat imageGray = null;
    Mat imageBilateral = null;
    Mat imageThreshold = null;
    Mat imageBlur = null;
    Mat imageBorder = null;
    Mat imageDilate = null;
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
                    //openCV();
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

//    public void openCV(){
//        if( ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1);
//        }
//        Mat imageSource = Imgcodecs.imread(currentPhotoPath);
//        imageGray = new Mat(imageSource.size(),CvType.CV_8UC4);
//        imageBilateral = new Mat(imageSource.size(),CvType.CV_8UC4);
//        imageThreshold = new Mat(imageSource.size(),CvType.CV_8UC4);
//        imageBlur = new Mat(imageSource.size(),CvType.CV_8UC4);
//        imageBorder = new Mat(imageSource.size(),CvType.CV_8UC4);
//        imageDilate = new Mat(imageSource.size(), CvType.CV_8UC4);
//
//        //Toast.makeText(this,"Opencv starting",Toast.LENGTH_LONG).show();
//        Imgproc.cvtColor(imageSource,imageGray, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.GaussianBlur(imageGray,imageBlur,new Size(3,3),0);
//        Imgproc.dilate(imageGray, imageDilate, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10.0, 10.0)));
////        Imgproc.threshold(imageGray,imageThreshold,25,255,Imgproc.THRESH_BINARY);
//        Imgproc.bilateralFilter(imageDilate,imageBilateral,9,75,75);
//        Imgproc.adaptiveThreshold(imageBilateral,imageThreshold,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY,115,4);
////        Imgproc.medianBlur(imageThreshold,imageBlur,11);
//
//        Core.copyMakeBorder(imageThreshold,imageBorder,5,5,5,5,Core.BORDER_CONSTANT);
//
//
//
//        Imgproc.Canny(imageBorder,imageSource,10, 100, 3, true);
//
//
//        //find the contour
//        Vector v = new Vector<>();
//
//        //find the contours
//        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        Imgproc.findContours(imageSource, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        double maxArea = -1;
//        int maxAreaIdx = -1;
//        MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
//        MatOfPoint2f approxCurve = new MatOfPoint2f();
//        Mat largest_contour = contours.get(0);
//        List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
//        for (int idx = 0; idx < contours.size(); idx++) {
//            temp_contour = contours.get(idx);
//            double contourarea = Imgproc.contourArea(temp_contour);
//            //compare this contour to the previous largest contour found
//            if (contourarea > maxArea) {
//                //check if this contour is a square
//                MatOfPoint2f new_mat = new MatOfPoint2f( temp_contour.toArray() );
//                int contourSize = (int)temp_contour.total();
//                Imgproc.approxPolyDP(new_mat, approxCurve, contourSize*0.05, true);
//                if (approxCurve.total() == 4) {
//                    maxArea = contourarea;
//                    maxAreaIdx = idx;
//                    largest_contours.add(temp_contour);
//                    largest_contour = temp_contour;
//                }
//            }
//        }
//        MatOfPoint temp_largest = largest_contours.get(largest_contours.size()-1);
//        largest_contours = new ArrayList<MatOfPoint>();
//        largest_contours.add(temp_largest);
//
////        Imgproc.cvtColor(imageSource, imageSource, Imgproc.COLOR_BayerBG2RGB);
////        Imgproc.drawContours(imageSource, largest_contours, -1, new Scalar(0, 255, 0), 5);
//
//
//        //calculate the center of mass of our contour image using moments
////        Moments moment = Imgproc.moments(largest_contours.get(0));
////        int x = (int) (moment.get_m10() / moment.get_m00());
////        int y = (int) (moment.get_m01() / moment.get_m00());
////
////        //SORT POINTS RELATIVE TO CENTER OF MASS
////        Point[] sortedPoints = new Point[4];
////
////        double[] data;
////        int count = 0;
////        for(int i=0; i<largest_contours.get(0).rows(); i++){
////            data = largest_contours.get(0).get(i, 0);
////            double datax = data[0];
////            double datay = data[1];
////            if(datax < x && datay < y){
////                sortedPoints[0]=new Point(datax,datay);
////                count++;
////            }else if(datax > x && datay < y){
////                sortedPoints[1]=new Point(datax,datay);
////                count++;
////            }else if (datax < x && datay > y){
////                sortedPoints[2]=new Point(datax,datay);
////                count++;
////            }else if (datax > x && datay > y){
////                sortedPoints[3]=new Point(datax,datay);
////                count++;
////            }
////        }
////
////        MatOfPoint2f src = new MatOfPoint2f(
////                sortedPoints[0],
////                sortedPoints[1],
////                sortedPoints[2],
////                sortedPoints[3]
////        );
////
////
////        MatOfPoint2f dst = new MatOfPoint2f(
////                new Point(0, 0),
////                new Point(imageSource.width()-1,0),
////                new Point(imageSource.width()-1,imageSource.height()-1),
////                new Point(0,imageSource.height()-1)
////        );
////
//
////        MatOfPoint2f dst = new MatOfPoint2f(
////                new Point(0, 0),
////                new Point(imageSource.width()-1,0),
////                new Point(0,imageSource.height()-1),
////                new Point(imageSource.width()-1,imageSource.height()-1)
////
////        );
//
////
////        Mat warpMat = Imgproc.getPerspectiveTransform(src,dst);
////        //This is you new image as Mat
////        Mat destImage = new Mat();
////        Imgproc.warpPerspective(imageSource, destImage, warpMat, imageSource.size());
//
//
////        Bitmap bm = Bitmap.createBitmap(destImage.cols(), destImage.rows(),Bitmap.Config.ARGB_8888);
////        Utils.matToBitmap(destImage, bm);
//
//        Bitmap bm = Bitmap.createBitmap(imageSource.cols(), imageSource.rows(),Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(imageSource, bm);
//
//
//        try{
//            photo.setImageBitmap(bm);
//        }catch(Exception ex){
//            //Toast.makeText(this,ex.toString(), Toast.LENGTH_LONG).show();
//            Log.d("bitmap",ex.toString());
//        }
//
//
//    }

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
