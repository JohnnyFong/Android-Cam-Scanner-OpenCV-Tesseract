package com.example.fyp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fyp.utils.Claim;
import com.example.fyp.utils.ImageConstant;

import java.util.Calendar;
import java.util.Date;

public class ClaimResultActivity extends AppCompatActivity {

    private Claim claim;
    private Bitmap receiptBM;
    private ImageView imageView;
    private EditText inputAmount, inputDate;
    private Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_result);
        FrameLayout frameLayout = findViewById(R.id.frameLayout);
        Snackbar snackbar = Snackbar.make(frameLayout, "Claim has been successfully created.", Snackbar.LENGTH_LONG);

        View snackbarView = snackbar.getView();
        TextView snacbarText = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        snacbarText.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_done_black_24dp,0);
        snackbar.show();

        receiptBM = ImageConstant.selectedImageBitmap;
        ImageConstant.selectedImageBitmap = null;

        inputAmount = findViewById(R.id.input_amount);
        inputDate = findViewById(R.id.input_date);
        imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(receiptBM);
        imageView.requestFocus();


        claim = (Claim) getIntent().getSerializableExtra("claimObj");

        Calendar cal = Calendar.getInstance();
        cal.setTime(claim.getDate());
        String date = DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();

        inputAmount.setText(String.valueOf(claim.getAmount()));
        inputAmount.setFocusable(false);
        inputDate.setText(date);
        inputDate.setFocusable(false);

        btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }
}
