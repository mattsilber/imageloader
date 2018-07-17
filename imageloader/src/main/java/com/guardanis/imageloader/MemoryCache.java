package com.guardanis.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.LruCache;

public class MemoryCache {

    private static MemoryCache instance;
    public static MemoryCache getInstance(Context context){
        if(instance == null)
            instance = new MemoryCache(context);

        return instance;
    }

    protected LruCache<String, BitmapDrawable> cache;

    protected MemoryCache(Context context){
        int maxAvailableMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        int availableMemoryReciprical = context.getResources()
                .getInteger(R.integer.ail__lru_available_memory_reciprical);

        int cacheSize = maxAvailableMemory / availableMemoryReciprical;

        this.cache = new LruCache<String, BitmapDrawable>(cacheSize) {
            protected int sizeOf(String key, BitmapDrawable drawable) {
                return (drawable.getBitmap().getRowBytes() * drawable.getBitmap().getHeight()) / 1024;
            }
        };
    }

    public MemoryCache put(Context context, String key, Bitmap bitmap){
        this.cache.put(key, new BitmapDrawable(context.getResources(), bitmap));
        return this;
    }

    public MemoryCache put(String key, BitmapDrawable drawable){
        this.cache.put(key, drawable);
        return this;
    }

    public BitmapDrawable get(String key){
        return cache.get(key);
    }

}
