package com.example.fyp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class MyClaimFragment extends Fragment {

    private List<Claim> claimList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ClaimAdapter claimAdapter;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore firestore;
    private DocumentSnapshot lastResult;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable  ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_claim, container, false);

//        initScrollListener();
        recyclerView = view.findViewById(R.id.recycler_view);

        claimAdapter = new ClaimAdapter(claimList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(claimAdapter);

        firestore = FirebaseFirestore.getInstance();

        Gson gson = new Gson();
        sharedPreferences = this.getActivity().getSharedPreferences("sharePreferences",MODE_PRIVATE);
        String json = sharedPreferences.getString("CurrentUser", null);
        User u = gson.fromJson(json, User.class);

        loadClaim(u.getId());

        return view;
    }

    private void loadClaim(String uID){
        Query query;
        if(lastResult == null){
            query = firestore.collection("claims").orderBy("date", Query.Direction.ASCENDING).limit(3);
        }else{
            query = firestore.collection("claims").orderBy("date", Query.Direction.ASCENDING).startAfter(lastResult).limit(3);
        }

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
            }
        });

    }

//    private void initScrollListener(){
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//
//                if(!)
//            }
//        });
//    }

}
