package com.example.fyp;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
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

public class ExtractedInfoActivity extends AppCompatActivity {

    public static final String TESS_DATA = "/tessdata";

    OCRUtils OCR;
    TextView resultView;
    Bitmap receiptBM;
    ImageView imageView;
    Bitmap bm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extracted_info);
        receiptBM = ImageConstant.selectedImageBitmap;
        ImageConstant.selectedImageBitmap = null;
        imageView = findViewById(R.id.imageView);
        resultView = findViewById(R.id.resultView);

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
                Imgproc.cvtColor(source,gray, Imgproc.COLOR_BGR2GRAY);

                Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2), new Point(1, 1));
                Mat element2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2), new Point(1, 1));
                Imgproc.dilate(gray, gray, element1);
                Imgproc.erode(gray, gray, element2);

                Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 0);

                Imgproc.threshold(gray, gray, 85, 255, Imgproc.THRESH_BINARY);

                bm = BitmapUtils.matToBitmap(gray);

                temp = OCR.getOCRResult(bm);

            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "Something went wrong. Please try again later...", Toast.LENGTH_SHORT).show();
            }
            return temp;
        }


        @Override
        protected void onPostExecute(String foundString) {
            if (foundString == null) {
                return;
            }
            resultView.setText(foundString);
            imageView.setImageBitmap(bm);
        }
    }
}
