package com.example.fyp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity  {

    // UI references.
    private AutoCompleteTextView inputEmail;
    private EditText inputPassword;
    private TextView registerText;
    private FirebaseAuth fireAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //check if the user is logged in or not
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            //user is logged in, direct to MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_login);
        // Set up the login form.

        ScrollView sv = (ScrollView) findViewById(R.id.login_scroll);
        sv.setVerticalScrollBarEnabled(false);
        sv.setHorizontalScrollBarEnabled(false);

        fireAuth = FirebaseAuth.getInstance();

        inputEmail = (AutoCompleteTextView) findViewById(R.id.input_email);
        inputPassword = (EditText) findViewById(R.id.input_password);


        Button mEmailSignInButton = (Button) findViewById(R.id.btn_signin);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        registerText = findViewById(R.id.registerText);
        registerText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(registerIntent);
            }
        });


    }

    private void attemptLogin() {

        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        fireAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent MainIntent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(MainIntent);
                    finish();
                    Toast.makeText(LoginActivity.this,"Signed in", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Invalid Email or Password. Please try again later." , Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}

