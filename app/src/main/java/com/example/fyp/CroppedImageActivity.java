package com.example.fyp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.fyp.utils.ImageConstant;

public class CroppedImageActivity extends AppCompatActivity {

    ImageView imageView;
    Bitmap selectedImageBM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropped_image);

        imageView = findViewById(R.id.imageView);
        selectedImageBM = ImageConstant.selectedImageBitmap;
        ImageConstant.selectedImageBitmap = null;
        imageView.setImageBitmap(selectedImageBM);
    }
}
