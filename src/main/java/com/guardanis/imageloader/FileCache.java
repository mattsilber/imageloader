package com.guardanis.imageloader;

import android.content.Context;

import java.io.File;

public class FileCache {

    private File cacheDir;

    public FileCache(Context context) {
        if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), context.getPackageName());
        else cacheDir = context.getCacheDir();

        if(!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public File getFile(String url) {
        String filename = String.valueOf(url.hashCode()) + (url.endsWith("svg") ? ".svg" : "");
        return new File(cacheDir, filename);
    }

    public void clear() {
        File[] files = cacheDir.listFiles();

        if(files != null)
            for(File file : files)
                file.delete();
    }

    public static void clear(Context context){
        new FileCache(context)
                .clear();
    }

    public boolean isCachedFileValid(String url, long maxCacheDurationMs){
        return maxCacheDurationMs < 0
                || System.currentTimeMillis() - getLastModifiedAt(url) < maxCacheDurationMs;
    }

    public long getLastModifiedAt(String url){
        return getFile(url)
                .lastModified();
    }

    public void delete(String url){
        getFile(url).delete();
    }
}