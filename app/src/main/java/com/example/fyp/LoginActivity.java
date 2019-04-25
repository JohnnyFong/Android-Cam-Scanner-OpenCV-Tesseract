package com.example.fyp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.fyp.utils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

public class LoginActivity extends AppCompatActivity  {

    // UI references.
    private AutoCompleteTextView inputEmail;
    private EditText inputPassword;
    private TextView registerText;
    private FirebaseAuth fireAuth;
    private SharedPreferences sharedPreferences;

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

        sharedPreferences = getSharedPreferences("sharePreferences",MODE_PRIVATE);

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

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.Theme_AppCompat_DayNight_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        try {

            fireAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.collection("users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User u = documentSnapshot.toObject(User.class);

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                Gson gson = new Gson();
                                String json = gson.toJson(u);
                                editor.putString("CurrentUser", json);
                                editor.apply();

                                progressDialog.dismiss();
                                Intent MainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(MainIntent);
                                finish();
                                Toast.makeText(LoginActivity.this, "Signed in", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Invalid Email or Password. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        catch(Exception ex){
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(),"Please make sure the Email and Password are keyed in.",Toast.LENGTH_LONG).show();
        }

    }


}

