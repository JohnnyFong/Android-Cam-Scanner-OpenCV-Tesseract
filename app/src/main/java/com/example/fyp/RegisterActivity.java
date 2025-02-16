package com.example.fyp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp.utils.Department;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.fyp.utils.User;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth fireAuth;
    private FirebaseFirestore fireStore;

    private ScrollView registerScroll;
    private EditText inputEmail, inputPassword, inputName, inputPhnum;
    private Button btnRegister;
    private TextView loginText;
    private Spinner departmentSpiner, managerSpinner;
    private User user;
    private ImageView imageView;

    private String email;
    private String password;
    private String name;
    private String phnum;
    private String department;
    private String lineManager;

    private RelativeLayout progress;

    private SharedPreferences sharedPreferences;

    ArrayAdapter<Department> departmentAdapter;
    ArrayAdapter<User> userAdapter;
    List<Department> departments = new ArrayList<>();
    List<User> lineManagers = new ArrayList<>();
    ArrayList<String> managers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerScroll = findViewById(R.id.register_scroll);
        registerScroll.setVerticalScrollBarEnabled(false);
        registerScroll.setHorizontalScrollBarEnabled(false);

        sharedPreferences = getSharedPreferences("sharePreferences",MODE_PRIVATE);

        btnRegister = findViewById(R.id.btn_register);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        inputPhnum = findViewById(R.id.input_phnum);
        inputName = findViewById(R.id.input_name);
        loginText = findViewById(R.id.link_login);
        departmentSpiner = findViewById(R.id.department_spinner);
        managerSpinner = findViewById(R.id.manager_spinner);
        imageView = findViewById(R.id.logo);
        imageView.requestFocus();

        progress = findViewById(R.id.loadingPanel);

        //get firebase instance
        fireAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();

        loadDepartment();

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
    }

    private void loadDepartment(){
        progress.setVisibility(View.VISIBLE);
        departmentAdapter = new ArrayAdapter<Department>(this, android.R.layout.simple_spinner_item, departments);
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        fireStore.collection("department").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //check if there are any department
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //for each department found add it to the adapter for department spinner
                        Department d = document.toObject(Department.class);
//                        Log.d("departmentid", d.getId());
                        d.setId(document.getId());
                        departments.add(d);
                    }
                    // update the spinner
                    departmentAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getApplicationContext(), "Something went wrong. No department available. Please try again later." , Toast.LENGTH_SHORT).show();
                }
            }
        });

        departmentSpiner.setAdapter(departmentAdapter);
        departmentSpiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Department d = (Department) departmentSpiner.getSelectedItem();
                loadManager(d);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void loadManager(Department d){
        progress.setVisibility(View.VISIBLE);
        userAdapter = new ArrayAdapter<User>(this, android.R.layout.simple_spinner_item, lineManagers);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        managerSpinner.setAdapter(userAdapter);

        managers = d.getLineManager();
        userAdapter.clear();
        fireStore.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if(task.isSuccessful()){
                for(QueryDocumentSnapshot document:task.getResult()){
                    User u = document.toObject(User.class);
                    for(String manager:managers){
                        if(u.getId().equals(manager)){
                            lineManagers.add(u);
                            break;
                        }
                    }
                }
                userAdapter.notifyDataSetChanged();
                progress.setVisibility(View.GONE);
                if(lineManagers.isEmpty()){
                    Toast.makeText(getApplicationContext(), "This department has no line manager yet.", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(), "Something went wrong. No manager available. Please try again later." , Toast.LENGTH_SHORT).show();
            }
            }
        });
    }

    private void registerUser(){
        progress.setVisibility(View.VISIBLE);

        boolean flag = true;

        email = inputEmail.getText().toString().trim();
        password = inputPassword.getText().toString().trim();
        name = inputName.getText().toString();
        phnum = inputPhnum.getText().toString();
        Department d = (Department) departmentSpiner.getSelectedItem();
        department = d.getId();
        User u = (User) managerSpinner.getSelectedItem();
        lineManager = u.getId();


        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            inputEmail.setError("Please enter a valid Email.");
            flag = false;
        }else{
            inputEmail.setError(null);
        }

        if(password.isEmpty()){
            inputPassword.setError("Please enter your password.");
            flag = false;
        }else{
            inputPassword.setError(null);
        }

        if(name.isEmpty()){
            inputName.setError("Please enter your name.");
            flag = false;
        }else{
            inputName.setError(null);
        }

        if(phnum.isEmpty() || !Patterns.PHONE.matcher(phnum).matches()){
            inputPhnum.setError("Please enter a valid phone number.");
            flag = false;
        }else{
            inputPhnum.setError(null);
        }

        if(flag){
            fireAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        //login successful add profile logic needed.
                        FirebaseUser fbUser = fireAuth.getCurrentUser();
                        user = new User(fbUser.getUid(),name,email,phnum,department,lineManager);

                        fireStore.collection("users").document(fbUser.getUid()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                Gson gson = new Gson();
                                String json = gson.toJson(user);
                                editor.putString("CurrentUser", json);
                                editor.apply();

                                progress.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "User Created" , Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Something went wrong. User not created. Please try again later." , Toast.LENGTH_SHORT).show();

                            }
                        });
                    }else{
                        Toast.makeText(getApplicationContext(), "Register failed." + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            progress.setVisibility(View.GONE);
        }
    }
}
