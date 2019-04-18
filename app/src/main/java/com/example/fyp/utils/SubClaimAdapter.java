package com.example.fyp.utils;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.fyp.R;
import com.example.fyp.ViewMyClaimActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;
import java.util.List;

public class SubClaimAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Claim> claimList;

    private final int VIEW_ITEM = 0;
    private final int VIEW_LOADING = 1;
    User u;


    public SubClaimAdapter(List<Claim> claimList){
        this.claimList = claimList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == VIEW_ITEM){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_claim_row,parent,false);

            return new SubClaimAdapter.ItemViewHolder(view);
        }else{
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading,parent,false);

            return new SubClaimAdapter.LoadingViewHolder(itemView);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if(viewHolder instanceof SubClaimAdapter.ItemViewHolder){
            populateItem((SubClaimAdapter.ItemViewHolder) viewHolder, position);
        }else if(viewHolder instanceof SubClaimAdapter.LoadingViewHolder){
            showLoadingView((SubClaimAdapter.LoadingViewHolder) viewHolder, position);
        }
    }

    @Override
    public int getItemCount() {
        return claimList.size();
    }
    @Override
    public int getItemViewType(int position) {
        return claimList.get(position) == null ? VIEW_LOADING : VIEW_ITEM;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        public TextView date, amount, status, name;
        public Claim claim;

        public ItemViewHolder(View view){
            super(view);
            date = view.findViewById(R.id.date);
            amount = view.findViewById(R.id.amount);
            status = view.findViewById(R.id.status);
            name = view.findViewById(R.id.name);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), ViewMyClaimActivity.class);
                    intent.putExtra("claimObj",claim);
                    view.getContext().startActivity(intent);
                }
            });
        }
        public void setClaim(Claim claim){
            this.claim = claim;
        }

    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private void showLoadingView(SubClaimAdapter.LoadingViewHolder viewHolder, int position){
//        viewHolder.progressBar.setIndeterminate(true);
    }

    private void populateItem(final SubClaimAdapter.ItemViewHolder viewHolder, int position){
        Claim claim = claimList.get(position);

        Calendar cal = Calendar.getInstance();
        cal.setTime(claim.getDate());
        String date = DateFormat.format("MMMM, dd, yyyy h:mm a", cal).toString();

        viewHolder.date.setText(date);
        viewHolder.amount.setText("RM" +String.valueOf(claim.getAmount()));
        viewHolder.status.setText(claim.getStatus());

        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        fs.collection("users").document(claim.getUserID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                u = documentSnapshot.toObject(User.class);
                viewHolder.name.setText(u.getName());
            }
        });

        viewHolder.setClaim(claim);
    }
}
