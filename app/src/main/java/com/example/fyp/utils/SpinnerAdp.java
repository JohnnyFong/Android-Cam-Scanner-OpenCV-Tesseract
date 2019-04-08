package com.example.fyp.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SpinnerAdp {

    ArrayAdapter<Department> departmentAdapter;
    ArrayAdapter<User> userAdapter;

    Context context;
    FirebaseFirestore fs;

    Boolean flag;

    public SpinnerAdp(Context context, FirebaseFirestore fs){
        this.context = context;
        this.fs = fs;
    }

    public ArrayAdapter<Department> departmentArrayAdapter(){

        List<Department> departments = new ArrayList<>();
        departmentAdapter= new ArrayAdapter<Department>(this.context, android.R.layout.simple_spinner_item, departments);
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //FirebaseFirestore fs = FirebaseFirestore.getInstance();
        fs.collection("department").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //check if there are any department
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //for each department found add it to the adapter for department spinner
                        Department d = document.toObject(Department.class);
                        d.setId(document.getId());
                        departmentAdapter.add(d);
                    }
                }
            }
        });
        return departmentAdapter;
    }

    public ArrayAdapter<User> userArrayAdapter(Department d){
        flag = true;
        List<User> user = new ArrayList<>();
        userAdapter= new ArrayAdapter<User>(this.context, android.R.layout.simple_spinner_item, user);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayList<String> managers = d.getLineManager();
        try {
            for (String manager : managers) {
                DocumentReference userRef = fs.collection("users").document(manager);
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User u = document.toObject(User.class);
                                u.setId(document.getId());
                                userAdapter.add(u);
                            } else {
                               flag = false;
                            }
                        } else {
                            flag = false;
                        }
                    }
                });
            }
        }catch(IllegalArgumentException ex){
            flag = false;
        }

        if(flag) {
            return userAdapter;
        }else{
            return null;
        }
    }
}
