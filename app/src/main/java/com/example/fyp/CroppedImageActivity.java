package com.example.fyp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.fyp.utils.ImageConstant;

import java.io.File;
import java.io.IOException;

public class CroppedImageActivity extends AppCompatActivity {

    ImageView imageView;
    Bitmap selectedImageBM;
    Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropped_image);

        btnContinue = findViewById(R.id.btnContinue);
        imageView = findViewById(R.id.imageView);
        selectedImageBM = ImageConstant.selectedImageBitmap;
        ImageConstant.selectedImageBitmap = null;
        imageView.setImageBitmap(selectedImageBM);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedImageBM != null) {

//                    File photoFile = null;
//                    try {
//                        photoFile = ImageConstant.createImageFile();
//
//                    } catch (IOException ex) {
//                        Toast.makeText(getApplicationContext(), "Error.", Toast.LENGTH_LONG).show();
//                    }
//                    ImageConstant.addPic(selectedImageBM, photoFile);
//                    ImageConstant.galleryAddPic(photoFile.getAbsolutePath(), getApplicationContext());
//
//                    Toast.makeText(getApplicationContext(), "Image saved to device.", Toast.LENGTH_LONG).show();

                    ImageConstant.selectedImageBitmap = selectedImageBM;
                    Intent intent = new Intent(getApplicationContext(), ExtractedInfoActivity.class);
                    startActivity(intent);
                }

            }
        });
    }

}
