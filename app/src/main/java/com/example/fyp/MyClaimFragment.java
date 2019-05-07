package com.example.fyp;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.fyp.utils.Claim;
import com.example.fyp.utils.ClaimAdapter;
import com.example.fyp.utils.User;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class MyClaimFragment extends Fragment {

    private List<Claim> claimList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ClaimAdapter claimAdapter;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore firestore;
    private DocumentSnapshot lastResult;
    private boolean isLoading = false;
    private User u;
    private RelativeLayout progress;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayoutCompat bottomsheet;
    private Spinner statusSpinner;
    private Button btnFilter, btnReset;
    private EditText dateFilter;
    private final Calendar myCalendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener date;
    private Query query;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable  ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_claim, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("My Claim");

        recyclerView = view.findViewById(R.id.recycler_view);

        progress = view.findViewById(R.id.loadingPanel);

        claimAdapter = new ClaimAdapter(claimList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(claimAdapter);

        firestore = FirebaseFirestore.getInstance();

        Gson gson = new Gson();
        sharedPreferences = this.getActivity().getSharedPreferences("sharePreferences",MODE_PRIVATE);
        String json = sharedPreferences.getString("CurrentUser", null);
        u = gson.fromJson(json, User.class);

        query = firestore.collection("claims").whereEqualTo("userID", u.getId()).orderBy("date", Query.Direction.DESCENDING);

        loadClaim();
        initScrollListener();
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
                if(!claimList.isEmpty()) {
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
                    claimList.clear();
                    query = firestore.collection("claims").whereEqualTo("userID", u.getId()).orderBy("date", Query.Direction.DESCENDING);
                    loadClaim();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });


        return view;
    }

    public void filterClaim(){
        String status;
        String startingDate;
        Date d = null;
        Boolean flag = true;
        status = (String) statusSpinner.getSelectedItem();
        startingDate = dateFilter.getText().toString();

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
                if(status.equals("All")) {
                    query = firestore.collection("claims")
                            .whereEqualTo("userID", u.getId())
                            .whereGreaterThan("date",d)
                            .orderBy("date", Query.Direction.DESCENDING);
                }else{
                    query = firestore.collection("claims")
                            .whereEqualTo("userID", u.getId())
                            .whereEqualTo("status",status)
                            .whereGreaterThan("date",d)
                            .orderBy("date", Query.Direction.DESCENDING);
                }
            }
        }else{
            //date is empty

            //change the query then call load function
            if(status.equals("All")) {
                query = firestore.collection("claims")
                        .whereEqualTo("userID", u.getId())
                        .orderBy("date", Query.Direction.DESCENDING);
            }else{
                query = firestore.collection("claims")
                        .whereEqualTo("userID", u.getId())
                        .whereEqualTo("status",status)
                        .orderBy("date", Query.Direction.DESCENDING);
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
//        Log.d("uid",uID);
//        Query query = firestore.collection("claims").whereEqualTo("userID", uID).orderBy("date", Query.Direction.DESCENDING).limit(10);

        query.limit(10).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    Claim claim = documentSnapshot.toObject(Claim.class);
                    claimList.add(claim);
                }
                //get the last document of the snapshot
                claimAdapter.notifyDataSetChanged();
                if(queryDocumentSnapshots.size() > 0) {
                    lastResult = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                }
                progress.setVisibility(View.GONE);

                if(claimList.size()<10){
                    isLoading = true;
                }

            }
        });

    }

    private void loadMore(){
        claimList.add(null);


        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                claimAdapter.notifyItemInserted(claimList.size() - 1);
//                Query query = firestore.collection("claims").whereEqualTo("userID", u.getId()).orderBy("date", Query.Direction.DESCENDING).startAfter(lastResult).limit(5);

                query.startAfter(lastResult).limit(5).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        claimList.remove(claimList.size() - 1);
                        int scrollPosition = claimList.size();
                        claimAdapter.notifyItemRemoved(scrollPosition);

                        for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                            Claim claim = documentSnapshot.toObject(Claim.class);
                            claimList.add(claim);
                        }
                        //get the last document of the snapshot
                        claimAdapter.notifyDataSetChanged();
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
