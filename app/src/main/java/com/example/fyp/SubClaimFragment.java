package com.example.fyp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.fyp.utils.Claim;
import com.example.fyp.utils.ClaimAdapter;
import com.example.fyp.utils.SubClaimAdapter;
import com.example.fyp.utils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SubClaimFragment extends Fragment {

    private List<Claim> claimList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SubClaimAdapter subClaimAdapter;
    private RelativeLayout progress;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore firestore;
    private DocumentSnapshot lastResult;
    private boolean isLoading = false;
    private User u;
    private SwipeRefreshLayout swipeContainer;
    private Spinner statusSpinner, employeeSpinner;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayoutCompat bottomsheet;
    private Button btnFilter, btnReset;
    private EditText dateFilter;
    private final Calendar myCalendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener date;
    private Query query;
    private ArrayList<User> employees = new ArrayList<>();
    private ArrayAdapter<User> employeeAdapter;
    private boolean oneTime = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sub_claim, container, false);

        progress = view.findViewById(R.id.loadingPanel);
        recyclerView = view.findViewById(R.id.recycler_view);
        subClaimAdapter = new SubClaimAdapter(claimList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(subClaimAdapter);
        swipeContainer = view.findViewById(R.id.swipeContainer);
        employeeSpinner = view.findViewById(R.id.employee_spinner);

        firestore = FirebaseFirestore.getInstance();

        Gson gson = new Gson();
        sharedPreferences = this.getActivity().getSharedPreferences("sharePreferences",MODE_PRIVATE);
        String json = sharedPreferences.getString("CurrentUser", null);
        u = gson.fromJson(json, User.class);

        query = firestore.collection("claims").whereEqualTo("managerID", u.getId());

        loadClaim();
        initScrollListener();

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                claimList.clear();
                loadClaim();
            }
        });

        populateSpinner(view);

        bottomsheet = view.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomsheet);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }else{
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        btnFilter = view.findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!claimList.isEmpty()){
                    filterClaim();
                }else{
                    Toast.makeText(getContext(), "No claim to filter.", Toast.LENGTH_LONG).show();
                }
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        dateFilter = view.findViewById(R.id.input_date);
        dateFilter.setFocusable(false);
        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };
        dateFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(view.getContext(),date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnReset = view.findViewById(R.id.btn_reset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!claimList.isEmpty()){
                    claimList.clear();
                    query = firestore.collection("claims").whereEqualTo("managerID", u.getId());
                    loadClaim();
                }else{
                    Toast.makeText(getContext(), "No claim to Reset.", Toast.LENGTH_LONG).show();
                }
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });



        return view;
    }

    public void laodEmployeeSpinner(){
        employees.clear();
        User u = new User();
        u.setName("All");
        employees.add(u);
        employeeAdapter = new ArrayAdapter<User>(getContext(), android.R.layout.simple_spinner_item, employees);
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        employeeSpinner.setAdapter(employeeAdapter);
        for(Claim claim:claimList){
            firestore.collection("users").document(claim.getUserID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        Boolean f = true;

                        //check if the list already has the name
                        for (User user:employees){
                            if(user.getName().equals((String) documentSnapshot.get("name"))){
                                //has the name, change the flag to false
                                f = false;
                            }

                        }
                        if(f){
                            //flag is true, means the list does not have the name, so add the name into the list
                            employees.add(documentSnapshot.toObject(User.class));
                            employeeAdapter.notifyDataSetChanged();
                        }
                    }

                }
            });
        }
        oneTime = false;
    }

    public void filterClaim(){
        String status;
        String startingDate;
        User e;
        Date d = null;
        Boolean flag = true;
        status = (String) statusSpinner.getSelectedItem();
        startingDate = dateFilter.getText().toString();
        e = (User) employeeSpinner.getSelectedItem();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy");
        if(startingDate.isEmpty()){
            flag = false;
        }
        if(flag){
            //date is not empty
            try {
                d = sdf.parse(startingDate);
            }catch (Exception ex){
                ex.printStackTrace();
                flag = false;
            }

            if(flag){
                //change the query then call load function

                //if no employee selected
                //no status (All)
                if (status.equals("All") && e.toString().equals("All")) {
                    query = firestore.collection("claims")
                            .whereEqualTo("managerID", u.getId())
                            .whereGreaterThan("date", d);
                } else if (status.equals("All") && !e.toString().equals("All")){
                    //if no status, but got employee
                    query = firestore.collection("claims")
                            .whereEqualTo("managerID", u.getId())
                            .whereEqualTo("userID",e.getId())
                            .whereGreaterThan("date", d);
                } else if (!status.equals("All") && e.toString().equals("All")){
                    //if got status, but no employee
                    query = firestore.collection("claims")
                            .whereEqualTo("managerID", u.getId())
                            .whereEqualTo("status", status)
                            .whereGreaterThan("date", d);
                }else {
                    //if got status, got employee
                    query = firestore.collection("claims")
                            .whereEqualTo("managerID", u.getId())
                            .whereEqualTo("userID", e.getId())
                            .whereEqualTo("status", status)
                            .whereGreaterThan("date", d);
                }
            }else{
                Toast.makeText(getContext(),"Unable to filter, please make sure the connection is stable and try again.",Toast.LENGTH_LONG).show();
            }
        }else{
            //date is empty
            //change the query then call load function

            //no employee
            //no status
            if(status.equals("All") && e.toString().equals("All")) {
                query = firestore.collection("claims")
                        .whereEqualTo("managerID", u.getId());
            }else if (status.equals("All") && !e.toString().equals("All")){
                //if no status but got employee
                query = firestore.collection("claims")
                        .whereEqualTo("managerID", u.getId())
                        .whereEqualTo("userID", e.getId());
            }else if (!status.equals("All") && e.toString().equals("All")){
                //if got status but no employee
                query = firestore.collection("claims")
                        .whereEqualTo("managerID", u.getId())
                        .whereEqualTo("status",status);
            }else{
                //if got status got employee
                query = firestore.collection("claims")
                        .whereEqualTo("managerID", u.getId())
                        .whereEqualTo("userID", e.getId())
                        .whereEqualTo("status",status);
            }
        }
        claimList.clear();
        loadClaim();
    }

    public void updateLabel() {
        String myFormat = "dd-MM-yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        dateFilter.setText(sdf.format(myCalendar.getTime()));
    }

    public void populateSpinner(View view){
        statusSpinner = view.findViewById(R.id.status_spinner);
        List<String> list = new ArrayList<String>();
        list.add("All");
        list.add("Pending");
        list.add("Approved");
        list.add("Rejected");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(dataAdapter);
    }

    public void loadClaim(){
        progress.setVisibility(View.VISIBLE);

        query.orderBy("date", Query.Direction.DESCENDING).limit(10).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    Claim claim = documentSnapshot.toObject(Claim.class);
                    claimList.add(claim);
                }
                //get the last document of the snapshot
                subClaimAdapter.notifyDataSetChanged();
                if(queryDocumentSnapshots.size() > 0) {
                    lastResult = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                }
                progress.setVisibility(View.GONE);

                if(claimList.size()<10){
                    isLoading = true;
                }
                swipeContainer.setRefreshing(false);

                if(oneTime){
                    laodEmployeeSpinner();
                }
            }
        });



    }

    private void loadMore(){
        claimList.add(null);


        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                subClaimAdapter.notifyItemInserted(claimList.size() - 1);
//                Query query = firestore.collection("claims").whereEqualTo("managerID", u.getId()).orderBy("date", Query.Direction.DESCENDING).startAfter(lastResult).limit(5);
                query.orderBy("date", Query.Direction.DESCENDING).startAfter(lastResult).limit(5).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        claimList.remove(claimList.size() - 1);
                        int scrollPosition = claimList.size();
                        subClaimAdapter.notifyItemRemoved(scrollPosition);

                        for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                            Claim claim = documentSnapshot.toObject(Claim.class);
                            claimList.add(claim);
                        }
                        //get the last document of the snapshot
                        subClaimAdapter.notifyDataSetChanged();
                        if(queryDocumentSnapshots.size() > 0) {
                            lastResult = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                        }
                        isLoading = false;
                    }
                });
            }
        });

    }

    public void initScrollListener(){
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if(!isLoading){
                    if(linearLayoutManager !=null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == claimList.size() -1){
                        loadMore();
                        isLoading = true;
                    }
                }
            }
        });
    }

}
