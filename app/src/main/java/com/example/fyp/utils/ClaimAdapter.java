package com.example.fyp.utils;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.fyp.R;

import java.util.Calendar;
import java.util.List;

public class ClaimAdapter extends RecyclerView.Adapter<ClaimAdapter.MyViewHolder> {
    private List<Claim> claimList;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView date, amount, status;

        public MyViewHolder(View view){
            super(view);
            date = view.findViewById(R.id.date);
            amount = view.findViewById(R.id.amount);
            status = view.findViewById(R.id.status);
        }
    }

    public ClaimAdapter(List<Claim> claimList){
        this.claimList = claimList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.claim_list_row,parent,false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Claim claim = claimList.get(position);

        Calendar cal = Calendar.getInstance();
        cal.setTime(claim.getDate());
        String date = DateFormat.format("dd-MM-yyyy HH:mm", cal).toString();

        holder.date.setText(date);
        holder.amount.setText("RM" +String.valueOf(claim.getAmount()));
        holder.status.setText(claim.getStatus());

    }

    @Override
    public int getItemCount() {
        return claimList.size();
    }
}
