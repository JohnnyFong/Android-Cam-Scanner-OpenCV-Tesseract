package com.example.fyp;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp.utils.Claim;
import com.example.fyp.utils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

public class ViewMyClaimActivity extends AppCompatActivity {

    private Claim claim;
    private EditText status, amount, manager, date, name;
    private RelativeLayout progress;
    private ImageView imageView;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private Button approve, reject;
    private int flag;
    private AlertDialog.Builder builder;
    private final int APPROVE_FLAG = 0;
    private final int REJECT_FLAG = 1;
    private String word = "pending";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_claim);

        builder = new AlertDialog.Builder(this);

        claim = (Claim) getIntent().getSerializableExtra("claimObj");
        flag = getIntent().getExtras().getInt("claim");

        status = findViewById(R.id.input_status);
        amount = findViewById(R.id.input_amount);
        manager = findViewById(R.id.input_manager);
        date = findViewById(R.id.input_date);
        name = findViewById(R.id.input_name);
        imageView = findViewById(R.id.imageView);

        approve = findViewById(R.id.btn_approve);
        reject = findViewById(R.id.btn_reject);

        status.setFocusable(false);
        amount.setFocusable(false);
        manager.setFocusable(false);
        date.setFocusable(false);
        name.setFocusable(false);
        imageView.requestFocus();

        status.setText(claim.getStatus());
        amount.setText("RM" + String.valueOf(claim.getAmount()));

        Calendar cal = Calendar.getInstance();
        cal.setTime(claim.getDate());
        String d = DateFormat.format("dd-MM-yyyy HH:mm", cal).toString();
        date.setText(d);

        progress = findViewById(R.id.loadingPanel);

        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(flag == 1){
            //sub claim
            getSupportActionBar().setTitle("Subordinate Claim Details");

            if(!claim.getStatus().equals("Pending")){
                approve.setVisibility(View.GONE);
                reject.setVisibility(View.GONE);
            }
            loadSubClaim();
        }else{
            //own claim
            getSupportActionBar().setTitle("My Claim Details");

            name.setVisibility(View.GONE);
            approve.setVisibility(View.GONE);
            reject.setVisibility(View.GONE);

        }
        loadMyClaim();
        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setMessage("You are about to approve this claim")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                processClaim(APPROVE_FLAG);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                dialog.cancel();
                            }
                        });
                //Creating dialog box
                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.setTitle("Approve Claim");
                alert.show();
            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setMessage("You are about to reject this claim")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                processClaim(REJECT_FLAG);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                dialog.cancel();
                            }
                        });
                //Creating dialog box
                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.setTitle("Reject Claim");
                alert.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                onBackPressed();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    private void processClaim(int flag){

        if(flag == APPROVE_FLAG){
            word = "Approved";
        }else{
            word = "Rejected";
        }
        claim.setStatus(word);

        firestore.collection("claims").document(claim.getId()).set(claim).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);
                Snackbar snackbar = Snackbar.make(constraintLayout, "Claim status has been changed successfully.", Snackbar.LENGTH_LONG);
                View snackbarView = snackbar.getView();
                TextView snacbarText = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                snacbarText.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_done_black_24dp,0);
                snackbar.show();
                status.setText(word);
                approve.setVisibility(View.GONE);
                reject.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to update the status of the claim. Please make sure that the network connection are stable", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void loadMyClaim(){
        //get manager
        firestore.collection("users").document(claim.getManagerID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User u = documentSnapshot.toObject(User.class);
                manager.setText(u.getName());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Some manager info are missing in the database.", Toast.LENGTH_SHORT).show();
            }
        });
        loadPhoto();
    }

    private void loadSubClaim(){
        firestore.collection("users").document(claim.getUserID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User u = documentSnapshot.toObject(User.class);
                name.setText(u.getName());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Some employee info are missing in the database.", Toast.LENGTH_SHORT).show();
            }
        });
        loadPhoto();
    }

    private void loadPhoto(){
        //get receipt
        storageReference.child("receipts/"+claim.getPhotoPath()).getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);
                progress.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progress.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"Failed to retrieve photo", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
