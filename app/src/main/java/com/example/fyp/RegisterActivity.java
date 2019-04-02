package com.example.fyp;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth fireAuth;
    private FirebaseAuth.AuthStateListener fireAuthLis;
    private DatabaseReference fireDatabase;

    private EditText inputEmail, inputPassword, inputName;
    private Button btnRegister;
    private TextView loginText, managerText;
    private Spinner departmentSpiner, managerSpinner;

    String[] departments = {"Finance","Human Resources","IT"};
    String[] managers= {"John","Alex","Mary"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnRegister = findViewById(R.id.btn_register);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        inputName = findViewById(R.id.input_name);
        loginText = findViewById(R.id.link_login);
        departmentSpiner = findViewById(R.id.department_spinner);
        managerText = findViewById(R.id.managerText);
        managerSpinner = findViewById(R.id.manager_spinner);

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, departments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departmentSpiner.setAdapter(adapter);
        departmentSpiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                loadManager();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        loadDepartment();

        //get firebase instance
        fireAuth = FirebaseAuth.getInstance();
        //get database instance
        fireDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        fireAuthLis = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };

    }

    private void loadDepartment(){

    }

    private void loadManager(){

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, managers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        managerSpinner.setAdapter(adapter);
    }

    private void registerUser(){
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        fireAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "User Created" , Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "Authentication failed." + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
