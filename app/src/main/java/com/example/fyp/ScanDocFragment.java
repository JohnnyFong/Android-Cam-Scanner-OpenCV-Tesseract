package com.example.fyp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.fyp.utils.ImageConstant;

public class ScanDocFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_document, container,false);
        Button camButton = (Button) view.findViewById(R.id.show_camera_button);
        Button galButton = (Button) view.findViewById(R.id.gallery_button);

        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage(view, ImageConstant.REQUEST_IMAGE_CAPTURE);
            }
        });

        galButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage(view, ImageConstant.REQUEST_IMAGE_GALLERY);
            }
        });

        return view;


    }

    private void getImage(View view, int REQUEST_CODE){
        Intent MainIntent = new Intent(view.getContext(), CameraActivity.class);
        MainIntent.putExtra("ImageMethod", REQUEST_CODE);
        startActivity(MainIntent);
    }



}
