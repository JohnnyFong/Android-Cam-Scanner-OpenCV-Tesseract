package com.example.fyp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp.utils.BitmapUtils;
import com.example.fyp.utils.Claim;
import com.example.fyp.utils.ImageConstant;
import com.example.fyp.utils.OCRUtils;
import com.example.fyp.utils.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ExtractedInfoActivity extends AppCompatActivity {

    public static final String TESS_DATA = "/tessdata";
    final String regExp = "[0-9]+([,.][0-9]{1,2})?";
    OCRUtils OCR;
    Bitmap receiptBM;
    ImageView imageView;
    Bitmap bm;
    RelativeLayout progress;
    String[] lines;
    String[] words;
    EditText inputPrice;
    String price;
    Boolean flag = true;
    Button btnContinue;
    Claim claim;

    private SharedPreferences sharedPreferences;
    private FirebaseFirestore fs;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extracted_info);
        receiptBM = ImageConstant.selectedImageBitmap;
        ImageConstant.selectedImageBitmap = null;
        imageView = findViewById(R.id.imageView);
        imageView.requestFocus();
        progress = findViewById(R.id.loadingPanel);
        inputPrice = findViewById(R.id.input_price);
        inputPrice.setEnabled(false);
        inputPrice.setFocusable(false);
        btnContinue = findViewById(R.id.btn_continue);
        imageView.setImageBitmap(receiptBM);
        sharedPreferences = getSharedPreferences("sharePreferences",MODE_PRIVATE);
        fs = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseStorage.setMaxUploadRetryTimeMillis(10000);
        storageReference = firebaseStorage.getReference();
        btnContinue.setEnabled(false);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createClaim();
            }
        });

        getTessData();

        OCR = new OCRUtils(this);
        //get the bitmap from ImageConstant and do OCR
        AsyncOcrTask task = new AsyncOcrTask();
        task.execute();
    }

    private void createClaim(){
        if(inputPrice.getText().toString().matches(regExp)){
            progress.setVisibility(View.VISIBLE);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            receiptBM.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();

            Date date = Calendar.getInstance().getTime();
            StorageReference ref = storageReference.child("receipts/"+date.toString()+".jpg");

            Gson gson = new Gson();
            String json = sharedPreferences.getString("CurrentUser", null);
            User u = gson.fromJson(json, User.class);
            claim = new Claim(u.getId(),"Pending",Double.valueOf(inputPrice.getText().toString()),u.getLineManager(),u.getDepartment(), date, date.toString()+".jpg");

            UploadTask uploadTask = ref.putBytes(data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    fs.collection("claims").add(claim).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            claim.setId(documentReference.getId());
                            fs.collection("claims").document(documentReference.getId()).set(claim).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    ImageConstant.selectedImageBitmap = receiptBM;
                                    progress.setVisibility(View.GONE);
                                    Intent intent = new Intent(getApplicationContext(),ClaimResultActivity.class);
                                    intent.putExtra("claimObj",claim);
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progress.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "Claim has not been created correctly. Please check your connection and try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progress.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Claim has not been created. Please check your connection and try again.", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),"Image upload failed, please try again with stable network.",Toast.LENGTH_LONG).show();
                }
            });

        }else{
            Toast.makeText(getApplicationContext(),"The price is wrong format, make sure to not include any characters.",Toast.LENGTH_LONG).show();
        }

    }

    private void getTessData() {
        try {
            File dir = getExternalFilesDir(TESS_DATA);
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    Toast.makeText(getApplicationContext(), "The folder " + dir.getPath() + "was not created", Toast.LENGTH_SHORT).show();
                }
            }
            String fileList[] = getAssets().list("tessdata");

            for (String fileName : fileList) {
                String pathToDataFile = dir + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {
                    InputStream in = getAssets().open("tessdata/" + fileName);
                    OutputStream out = new FileOutputStream(pathToDataFile);
                    byte[] buff = new byte[1024];
                    int len;
                    while ((len = in.read(buff)) > 0) {
                        out.write(buff, 0, len);
                    }
                    in.close();
                    out.close();
                }
            }
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
    }

    private class AsyncOcrTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            String temp;
            Boolean cnt;
            String p = null;
            try {
                //change bitmap to map
                Mat gray = new Mat();
                Mat source = BitmapUtils.bitmapToMat(receiptBM);
                //pre-processing
                Imgproc.cvtColor(source,gray, Imgproc.COLOR_BGR2GRAY);

                Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2), new Point(1, 1));
                Mat element2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2), new Point(1, 1));
                Imgproc.dilate(gray, gray, element1);
                Imgproc.erode(gray, gray, element2);

                Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 0);

                Imgproc.threshold(gray, gray, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY_INV);

                bm = BitmapUtils.matToBitmap(gray);

                temp = OCR.getOCRResult(bm);

                //split the paragraph into lines
                lines = temp.split("\n");

                //for each line split the words
                for (int j=0; j<lines.length; j++){
                    words = lines[j].split("\\s+");
                    for(String word:words){
                        Log.d("OCR",word);
                    }
                    cnt = false;
                    //for each word, find the word "total"
                    for (int i=0; i<words.length; i++){

                        if(cnt){
                            //extract the number from the line.
                            if(words[i].matches(regExp)){
                                p=words[i];
                            }
                        }else if(words[i].toLowerCase().equals("total") || words[i].toLowerCase().equals("total:")|| words[i].toLowerCase().equals("totali")){//when total is found j is the line, i is the index of the word total
                            cnt = true;//found the total, put the remainding string into this price variable
                        }

                    }
                }

            } catch (Exception ex) {
                Log.d("OCR", ex.toString());
                flag = false;
            }
            return p;
        }


        @Override
        protected void onPostExecute(String foundString) {
            progress.setVisibility(View.GONE);
            inputPrice.setFocusableInTouchMode(true);
            btnContinue.setEnabled(true);
            inputPrice.setEnabled(true);
            if (foundString == null || !flag) {
                inputPrice.setText(null);
                Toast.makeText(getApplicationContext(),"Unable to identify total, Please ensure that the picture is clear and it is a valid receipt and try again.",Toast.LENGTH_LONG).show();
                return;
            }

            inputPrice.setText(foundString);
            price = foundString;

        }
    }
}
