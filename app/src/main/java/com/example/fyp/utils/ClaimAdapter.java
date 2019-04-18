package com.example.fyp.utils;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.fyp.R;
import com.example.fyp.ViewMyClaimActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ClaimAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Claim> claimList;

    private final int VIEW_ITEM = 0;
    private final int VIEW_LOADING = 1;
    private final int MY_CLAIM = 0;


    public ClaimAdapter(List<Claim> claimList){
        this.claimList = claimList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == VIEW_ITEM){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.claim_list_row,parent,false);

            return new ItemViewHolder(view);
        }else{
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading,parent,false);

            return new LoadingViewHolder(itemView);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if(viewHolder instanceof ItemViewHolder){
            populateItem((ItemViewHolder) viewHolder, position);
        }else if(viewHolder instanceof LoadingViewHolder){
            showLoadingView((LoadingViewHolder) viewHolder, position);
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
        public TextView date, amount, status;
        public Claim claim;

        public ItemViewHolder(View view){
            super(view);
            date = view.findViewById(R.id.date);
            amount = view.findViewById(R.id.amount);
            status = view.findViewById(R.id.status);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), ViewMyClaimActivity.class);
                    intent.putExtra("claimObj",claim);
                    intent.putExtra("claim",MY_CLAIM);
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

    private void showLoadingView(LoadingViewHolder viewHolder, int position){
//        viewHolder.progressBar.setIndeterminate(true);
    }

    private void populateItem(ItemViewHolder viewHolder, int position){
        Claim claim = claimList.get(position);

        Calendar cal = Calendar.getInstance();
        cal.setTime(claim.getDate());
        String date = DateFormat.format("MMMM, dd, yyyy h:mm a", cal).toString();

        viewHolder.date.setText(date);
        viewHolder.amount.setText("RM" +String.valueOf(claim.getAmount()));
        viewHolder.status.setText(claim.getStatus());
        viewHolder.setClaim(claim);
    }


}
