package com.example.catchy.misc;

import android.graphics.Bitmap;

import androidx.collection.LruCache;

public class BitmapCache {
    private static LruCache<Integer, Bitmap> MemoryCache = null;

    public static void InitBitmapCache() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor

        if (MemoryCache == null) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            // use 1/4th of the available memory for this memory cache.

            final int cacheSize = maxMemory / 4;
            MemoryCache = new LruCache<Integer, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(Integer key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };
        }
    }

    public static void clear() {
        MemoryCache.evictAll();
    }

    public static void addBitmapToMemoryCache(Integer key, Bitmap bitmap) {
        MemoryCache.put(key, bitmap);
    }

    public static Bitmap getBitmapFromMemCache(Integer key) {
        return MemoryCache.get(key);
    }
}
