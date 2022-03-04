package com.hiopengl.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

public class BitmapUtil {
    public static Bitmap getBitmapFromAssets(Context context, String fileName) {
        Bitmap bitmap = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap getBitmapFromText(Context context, String text, int fontSize) {
        return null;
    }

    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }
}
