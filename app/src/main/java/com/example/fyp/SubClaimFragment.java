package com.example.fyp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.fyp.utils.Claim;
import com.example.fyp.utils.ClaimAdapter;
import com.example.fyp.utils.SubClaimAdapter;
import com.example.fyp.utils.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
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

        firestore = FirebaseFirestore.getInstance();

        Gson gson = new Gson();
        sharedPreferences = this.getActivity().getSharedPreferences("sharePreferences",MODE_PRIVATE);
        String json = sharedPreferences.getString("CurrentUser", null);
        u = gson.fromJson(json, User.class);

        loadClaim(u.getId());
        initScrollListener();

        return view;
    }

    public void loadClaim(String uID){
        progress.setVisibility(View.VISIBLE);
        Log.d("uid",uID);
        Query query = firestore.collection("claims").whereEqualTo("managerID", uID).orderBy("date", Query.Direction.DESCENDING).limit(10);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
            }
        });

    }

    private void loadMore(){
        claimList.add(null);


        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                subClaimAdapter.notifyItemInserted(claimList.size() - 1);
                Query query = firestore.collection("claims").whereEqualTo("managerID", u.getId()).orderBy("date", Query.Direction.DESCENDING).startAfter(lastResult).limit(5);
                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
