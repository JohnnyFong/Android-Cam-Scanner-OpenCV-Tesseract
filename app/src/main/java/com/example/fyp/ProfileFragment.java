package com.example.fyp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp.utils.Department;
import com.example.fyp.utils.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.fyp.utils.SpinnerAdp;

public class ProfileFragment extends Fragment {

    private FirebaseAuth fireAuth;
    private FirebaseFirestore fireStore;

    private ScrollView registerScroll;
    private EditText inputEmail, inputPassword, inputName, inputPhnum;
    private Button btnRegister;
    private TextView loginText, managerText;
    private Spinner departmentSpiner, managerSpinner;
    private User user;

    private String email;
    private String password;
    private String name;
    private String phnum;
    private String department;
    private String lineManager;

    private SpinnerAdp spinnerAdp;

    ArrayAdapter<Department> departmentAdapter;
    ArrayAdapter<User> userAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container,false);
        Button updateButton = (Button) view.findViewById(R.id.update_button);
        departmentSpiner = view.findViewById(R.id.department_spinner);
        managerSpinner = view.findViewById(R.id.manager_spinner);

        fireStore = FirebaseFirestore.getInstance();

        spinnerAdp = new SpinnerAdp(getContext(),fireStore);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Do update profile here
            }
        });
        loadDepartment();

        return view;




    }

    private void loadDepartment(){
//        departmentAdapter = new ArrayAdapter<Department>(this, android.R.layout.simple_spinner_item, departments);
//        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departmentAdapter = spinnerAdp.departmentArrayAdapter();
        departmentAdapter.notifyDataSetChanged();
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

    private void loadManager(Department d) {
//        userAdapter = new ArrayAdapter<User>(this, android.R.layout.simple_spinner_item, lineManagers);
//        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userAdapter = spinnerAdp.userArrayAdapter(d);
        if (userAdapter != null) {
            userAdapter.notifyDataSetChanged();
            managerSpinner.setAdapter(userAdapter);
        } else {
            managerSpinner.setAdapter(null);
            Toast.makeText(getContext(), "This department has no line manager yet.", Toast.LENGTH_SHORT).show();
        }
    }

}
