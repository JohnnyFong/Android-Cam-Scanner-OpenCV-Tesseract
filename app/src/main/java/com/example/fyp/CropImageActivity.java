package com.example.fyp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.fyp.utils.ImageConstant;
import com.example.fyp.utils.OpenCVUtils;
import com.example.fyp.utils.PolygonView;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CropImageActivity extends AppCompatActivity {

    FrameLayout ImageCrop;
    ImageView imageView;
    PolygonView polygonView;
    Bitmap selectedImageBitmap;
    Button btnContinue;

    OpenCVUtils opencvUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        opencvUtils = new OpenCVUtils();
        btnContinue = findViewById(R.id.btnContinue);
        ImageCrop = findViewById(R.id.ImageCrop);
        imageView = findViewById(R.id.imageView);
        polygonView = findViewById(R.id.polygonView);

        ImageCrop.post(new Runnable(){
            @Override
            public void run() {
                selectedImageBitmap = ImageConstant.selectedImageBitmap;
                ImageConstant.selectedImageBitmap = null; // free the bitmap from constant
                Bitmap scaledBitmap = scaledBitmap(selectedImageBitmap,ImageCrop.getWidth(),ImageCrop.getHeight());
                Log.d("bitmap",scaledBitmap.toString());
                imageView.setImageBitmap(scaledBitmap);
                Bitmap tempBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                Map<Integer, PointF> edgePoints = getEdgePoints(tempBitmap);

                polygonView.setPoints(edgePoints);
                polygonView.setVisibility(View.VISIBLE);

                int padding = (int) getResources().getDimension(R.dimen.scanPadding);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(tempBitmap.getWidth() + 2 * padding, tempBitmap.getHeight() + 2 * padding);
                layoutParams.gravity = Gravity.CENTER;

                polygonView.setLayoutParams(layoutParams);
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(polygonView.shapeValidate()){
                    ImageConstant.selectedImageBitmap = getCroppedImage();
                    Intent intent = new Intent(getApplicationContext(),CroppedImageActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Unable to process the shape, please try re-adjusting or re-take a clear picture.",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height) {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    private Map<Integer, PointF> getEdgePoints(Bitmap bm){
        //get the contour edge points
        MatOfPoint2f p2f = opencvUtils.getPoint(bm);
//        Log.d("bitmap",bm.toString());
//        Log.d("bitmap",p2f.toString());
        List<Point> points = Arrays.asList(p2f.toArray());
        List<PointF> pointf = new ArrayList<>();
        for(int i=0;i<points.size();i++){
            pointf.add(new PointF(((float) points.get(i).x), ((float) points.get(i).y)));
        }
        //order the outline points
        Map<Integer,PointF> orderedPoints = polygonView.getOrderedPoints(pointf);
        if(!polygonView.isValidShape(orderedPoints)){
            //get the outline if the orderedPoints is not valid (not 4)
            Map<Integer, PointF> outlinePoints = new HashMap<>();
            outlinePoints.put(0, new PointF(0,0));
            outlinePoints.put(1, new PointF(bm.getWidth(),0));
            outlinePoints.put(2, new PointF(0,bm.getHeight()));
            outlinePoints.put(3, new PointF(bm.getWidth(),bm.getHeight()));
            orderedPoints = outlinePoints;
        }
        return orderedPoints;
    }

    protected Bitmap getCroppedImage(){
        Map<Integer, PointF> points = polygonView.getPoints();
        float xRatio = (float) selectedImageBitmap.getWidth()/ imageView.getWidth();
        float yRatio = (float) selectedImageBitmap.getHeight() / imageView.getHeight();

        float x0 = (points.get(0).x) * xRatio;
        float x1 = (points.get(1).x) * xRatio;
        float x2 = (points.get(2).x) * xRatio;
        float x3 = (points.get(3).x) * xRatio;

        float y0 = (points.get(0).y) * yRatio;
        float y1 = (points.get(1).y) * yRatio;
        float y2 = (points.get(2).y) * yRatio;
        float y3 = (points.get(3).y) * yRatio;

        return opencvUtils.getScannedBitmap(selectedImageBitmap,x0,y0,x1,y1,x2,y2,x3,y3);
    }
}
