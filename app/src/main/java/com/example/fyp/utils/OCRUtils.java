package com.example.fyp.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

import static com.googlecode.tesseract.android.TessBaseAPI.PageSegMode.PSM_AUTO;

public final class OCRUtils {

    private static TessBaseAPI mTess = new TessBaseAPI();
    Context context;
    private String datapath;

    public OCRUtils(Context context) {
        this.context = context;
        datapath = context.getExternalFilesDir("/").getPath() + "/";
        mTess.setDebug(true);

        mTess.init(datapath, "eng");
        mTess.setPageSegMode(PSM_AUTO);
    }

    public void stopRecognition() {
        mTess.stop();
    }

    public String getOCRResult(Bitmap bitmap) {

        String whitelist = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789,.";
        mTess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, whitelist);
        mTess.setImage(bitmap);
        String result = mTess.getUTF8Text();
        return result;
    }

    public void onDestroy() {
        if (mTess != null)
            mTess.end();
    }
}
