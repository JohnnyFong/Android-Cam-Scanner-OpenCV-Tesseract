package com.example.fyp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.example.fyp.utils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseUser fireUser;
    private FirebaseFirestore fireStore;
    private FirebaseAuth auth;

    private ScrollView profileScroll;
    private EditText inputEmail, inputName, inputPhnum;
    private Button btnUpdate;
    private Spinner departmentSpiner, managerSpinner;
    private TextView btnReset;
    private ImageView profileImage;

    private User user;
    private Department d;
    private User u;

    private String email;
    private String name;
    private String phnum;
    private String department;
    private String lineManager;

    private SharedPreferences sharedPreferences;
    private RelativeLayout progress;
    private User updateUser;

    ArrayAdapter<Department> departmentAdapter;
    ArrayAdapter<User> userAdapter;
    List<Department> departments = new ArrayList<>();
    List<User> lineManagers = new ArrayList<>();
    ArrayList<String> managers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileScroll = findViewById(R.id.profile_scroll);
        profileScroll.setVerticalScrollBarEnabled(false);
        profileScroll.setHorizontalScrollBarEnabled(false);

        btnUpdate = findViewById(R.id.update_button);
        btnReset = findViewById(R.id.reset_button);
        inputEmail = findViewById(R.id.input_email);
        inputPhnum = findViewById(R.id.input_phnum);
        inputName = findViewById(R.id.input_name);
        departmentSpiner = findViewById(R.id.department_spinner);
        managerSpinner = findViewById(R.id.manager_spinner);
        profileImage = findViewById(R.id.profileImage);
        profileImage.requestFocus();

        progress = findViewById(R.id.loadingPanel);

        sharedPreferences = getSharedPreferences("sharePreferences",MODE_PRIVATE);

        fireStore = FirebaseFirestore.getInstance();
        fireUser = FirebaseAuth.getInstance().getCurrentUser();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Do update profile here
                updateProfile();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });

        loadProfile();
    }

    private void resetPassword(){
        auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(user.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "A password reset email has been sent to "+user.getEmail()+ ". Please check the email.",Toast.LENGTH_LONG).show();
                    auth.signOut();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "Unable to reset password. Please make sure the email is valid and the network connection are stable.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateProfile(){
        progress.setVisibility(View.VISIBLE);
        email = inputEmail.getText().toString().trim();
        name = inputName.getText().toString();
        phnum = inputPhnum.getText().toString();
        Department d = (Department) departmentSpiner.getSelectedItem();
        department = d.getId();
        User u = (User) managerSpinner.getSelectedItem();
        lineManager = u.getId();

        updateUser = new User(fireUser.getUid(),name,email,phnum,department,lineManager);

        fireStore.collection("users").document(fireUser.getUid()).set(updateUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Gson gson = new Gson();
                String json = gson.toJson(updateUser);
                editor.putString("CurrentUser", json);
                editor.apply();
                progress.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "User profile has been updated" , Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Something went wrong. User not created. Please try again later." , Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void loadProfile(){
        fireStore.collection("users").document(fireUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
            user = documentSnapshot.toObject(User.class);
            inputEmail.setText(user.getEmail());
            inputName.setText(user.getName());
            inputPhnum.setText(user.getPhnum());
            //after get user details only load department and line manager
            loadDepartment();
            }
        });
    }

    private void loadDepartment(){
        departmentAdapter = new ArrayAdapter<Department>(this, android.R.layout.simple_spinner_item, departments);
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        departmentSpiner.setAdapter(departmentAdapter);

        fireStore.collection("department").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //check if there are any department
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //for each department found add it to the adapter for department spinner
                        Department d = document.toObject(Department.class);
                        d.setId(document.getId());
                        departments.add(d);
                    }
                    // update the spinner
                    departmentAdapter.notifyDataSetChanged();
                    //set user's department
                    for (int i = 0; i < departments.size(); i++) {
                        if (departments.get(i).getId().equals(user.getDepartment())) {
                            departmentSpiner.setSelection(i);
                        }
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Something went wrong. No department available. Please try again later." , Toast.LENGTH_SHORT).show();
                }
            }
        });

        departmentSpiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //load the manager of the department
                Department d = (Department) departmentSpiner.getSelectedItem();
                loadManager(d);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void loadManager(final Department d) {
        progress.setVisibility(View.VISIBLE);
        userAdapter = new ArrayAdapter<User>(this, android.R.layout.simple_spinner_item, lineManagers);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        managerSpinner.setAdapter(userAdapter);
        this.d = d;
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

                if(lineManagers.isEmpty()){
                    Toast.makeText(getApplicationContext(), "This department has no line manager yet.", Toast.LENGTH_SHORT).show();
                }else if(d.getId().equals(user.getDepartment())){
                    //change the manager
                    for (int i = 0; i < lineManagers.size(); i++) {
                        if (lineManagers.get(i).getId().equals(user.getLineManager())) {
                            managerSpinner.setSelection(i);
                        }
                    }
                }
                progress.setVisibility(View.GONE);
            }else{
                Toast.makeText(getApplicationContext(), "Something went wrong. No manager available. Please try again later." , Toast.LENGTH_SHORT).show();
            }
            }
        });
    }
}
