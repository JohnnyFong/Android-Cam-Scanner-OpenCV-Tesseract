package com.example.fyp;

import android.app.Application;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OnStart extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser != null){
            startActivity(new Intent(OnStart.this, MainActivity.class));
        }else{
            startActivity(new Intent(OnStart.this, LoginActivity.class));
        }
    }
}
