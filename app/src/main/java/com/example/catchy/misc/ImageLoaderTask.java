package com.example.catchy.misc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.example.catchy.misc.BitmapCache;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class ImageLoaderTask extends AsyncTask<Integer, String, Bitmap> {
    int m_position;
    String m_photoPath;
    boolean song;

    public ImageLoaderTask(int position, String photoPath, boolean song) {
        m_position = position;
        m_photoPath = photoPath;
        this.song = song;
    }

    @Override
    protected Bitmap doInBackground(Integer... integers) {
        Bitmap bitmap = null;
        try {
            bitmap = decodeSampledBitmapFromString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    //set photoView and holder
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null) {
            BitmapCache.addBitmapToMemoryCache(m_position, bitmap, song);

            // to do: set imageView
        }
    }

    //resample Bitmap to prevent out-of-memory crashes
    private Bitmap decodeSampledBitmapFromString() throws IOException {
        Bitmap bitmap = Picasso.get().load(m_photoPath).get();
        return bitmap;
    }

    //calculate bitmap sample sizes
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

}