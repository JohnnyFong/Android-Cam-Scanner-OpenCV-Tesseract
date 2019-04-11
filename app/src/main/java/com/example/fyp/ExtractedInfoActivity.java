package com.example.fyp;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp.utils.BitmapUtils;
import com.example.fyp.utils.ImageConstant;
import com.example.fyp.utils.OCRUtils;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ExtractedInfoActivity extends AppCompatActivity {

    public static final String TESS_DATA = "/tessdata";

    OCRUtils OCR;
    TextView resultView, price;
    Bitmap receiptBM;
    ImageView imageView;
    Bitmap bm;
    RelativeLayout progress;
    String[] lines;
    String[] words;
    String P;
    int index1, index2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extracted_info);
        receiptBM = ImageConstant.selectedImageBitmap;
        ImageConstant.selectedImageBitmap = null;
        imageView = findViewById(R.id.imageView);
        resultView = findViewById(R.id.resultView);
        progress = findViewById(R.id.loadingPanel);
        price = findViewById(R.id.price);

        getTessData();

        OCR = new OCRUtils(this);

        AsyncOcrTask task = new AsyncOcrTask();
        task.execute();

        //get the bitmap from ImageConstant and do OCR

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
            String temp = "";

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

                    //for each word, find the word "total"
                    for (int i=0; i<words.length; i++){

                        //when total is found j is the line, i is the index of the word total
                        if(words[i].toLowerCase().equals("total")){
                            index1 = j;
                            index2 = i;

                            P = lines[j];// <--------- the total amount is in this line
                        }
                    }
                }

            } catch (Exception ex) {
                return ex.toString();
            }
            return temp;
        }


        @Override
        protected void onPostExecute(String foundString) {
            if (foundString == null) {
                return;
            }
            progress.setVisibility(View.GONE);
            resultView.setText(foundString);
            imageView.setImageBitmap(bm);
            price.setText(index1 + " " + index2 + " " +P);
        }
    }
}
