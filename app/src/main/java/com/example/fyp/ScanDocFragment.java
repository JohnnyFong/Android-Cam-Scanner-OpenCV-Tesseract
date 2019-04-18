package com.example.fyp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.fyp.utils.ImageConstant;
import com.example.fyp.utils.User;
import com.google.gson.Gson;

import static android.content.Context.MODE_PRIVATE;

public class ScanDocFragment extends Fragment {
    private User u;
    private SharedPreferences sharedPreferences;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_document, container,false);
        Button camButton = (Button) view.findViewById(R.id.show_camera_button);
        Button galButton = (Button) view.findViewById(R.id.gallery_button);

        Gson gson = new Gson();
        sharedPreferences = this.getActivity().getSharedPreferences("sharePreferences",MODE_PRIVATE);
        String json = sharedPreferences.getString("CurrentUser", null);
        u = gson.fromJson(json, User.class);

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
        if(u.getLineManager().equals("")){
            Toast.makeText(this.getContext(),"Please ensure that you have a line manager before submitting a claim.", Toast.LENGTH_LONG).show();
        }else{
            Intent MainIntent = new Intent(view.getContext(), CameraActivity.class);
            MainIntent.putExtra("ImageMethod", REQUEST_CODE);
            startActivity(MainIntent);
        }

    }



}
