package com.example.catchy.misc;

import android.graphics.Bitmap;

import androidx.collection.LruCache;

public class BitmapCache {
    private static LruCache<Integer, Bitmap> SongMemoryCache = null;
    private static LruCache<Integer, Bitmap> UserMemoryCache = null;

    public static void InitBitmapCache(boolean song) {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor

        if (song) {
            if (SongMemoryCache == null) {
                final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
                // use 1/4th of the available memory for this memory cache.

                final int cacheSize = maxMemory / 4;
                SongMemoryCache = new LruCache<Integer, Bitmap>(cacheSize) {
                    @Override
                    protected int sizeOf(Integer key, Bitmap bitmap) {
                        // The cache size will be measured in kilobytes rather than
                        // number of items.
                        return bitmap.getByteCount() / 1024;
                    }
                };
            }
        }
        else {
            if (UserMemoryCache == null) {
                final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
                // use 1/4th of the available memory for this memory cache.

                final int cacheSize = maxMemory / 4;
                UserMemoryCache = new LruCache<Integer, Bitmap>(cacheSize) {
                    @Override
                    protected int sizeOf(Integer key, Bitmap bitmap) {
                        // The cache size will be measured in kilobytes rather than
                        // number of items.
                        return bitmap.getByteCount() / 1024;
                    }
                };
            }
        }
    }

    public static void clearSongCache() {
        SongMemoryCache.evictAll();
    }

    public static void clearUserCache() {
        UserMemoryCache.evictAll();
    }

    public static void addBitmapToMemoryCache(Integer key, Bitmap bitmap, boolean song) {
        if (song)
            SongMemoryCache.put(key, bitmap);
        else
            UserMemoryCache.put(key, bitmap);
    }

    public static Bitmap getBitmapFromMemCache(Integer key, boolean song) {
        if (song)
            return SongMemoryCache.get(key);
        else
            return UserMemoryCache.get(key);
    }
}
