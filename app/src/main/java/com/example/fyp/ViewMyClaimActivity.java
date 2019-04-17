package com.example.fyp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
    private EditText status, amount, manager, date;
    private RelativeLayout progress;
    private ImageView imageView;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_claim);

        claim = (Claim) getIntent().getSerializableExtra("claimObj");

        status = findViewById(R.id.input_status);
        amount = findViewById(R.id.input_amount);
        manager = findViewById(R.id.input_manager);
        date = findViewById(R.id.input_date);
        imageView = findViewById(R.id.imageView);

        status.setText(claim.getStatus());
        amount.setText("RM" + String.valueOf(claim.getAmount()));

        Calendar cal = Calendar.getInstance();
        cal.setTime(claim.getDate());
        String d = DateFormat.format("dd-MM-yyyy HH:mm", cal).toString();
        date.setText(d);

        status.setFocusable(false);
        amount.setFocusable(false);
        manager.setFocusable(false);
        date.setFocusable(false);
        imageView.requestFocus();

        progress = findViewById(R.id.loadingPanel);

        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        loadClaim();

    }

    private void loadClaim(){
        progress.setVisibility(View.VISIBLE);
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
                Toast.makeText(getApplicationContext(),"Failed to retrieve manager info.", Toast.LENGTH_SHORT).show();
            }
        });

        //get picture
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
