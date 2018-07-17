package com.guardanis.imageloader;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import java.io.File;

public class FileCache {

    private Context context;

    private File cacheDir;

    private boolean externalStorageEnabled;
    private boolean targetStorageExternal = false;

    public FileCache(Context context) {
        this.context = context.getApplicationContext();

        this.externalStorageEnabled = context.getResources()
                .getBoolean(R.bool.ail__external_storage_enabled);
    }

    private void ensureCacheDirectoryValid(){
        if(cacheDir == null
                || (targetStorageExternal && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
            updateCacheDirectory();
    }

    private void updateCacheDirectory(){
        if(externalStorageEnabled
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            cacheDir = new File(Environment.getExternalStorageDirectory(), context.getPackageName());

            targetStorageExternal = true;
        }
        else {
            cacheDir = context.getResources().getBoolean(R.bool.ail__use_cache_dir)
                    ? context.getCacheDir()
                    : context.getFilesDir();

            targetStorageExternal = false;
        }

        if(!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public File getFile(String url) {
        ensureCacheDirectoryValid();

        String filename = String.valueOf(url.hashCode())
                + (url.endsWith("svg") ? ".svg" : ""); // Since we're using file extensions and not descriptors for SVGs

        return new File(cacheDir, filename);
    }

    public void clear() {
        ensureCacheDirectoryValid();

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
        getFile(url)
                .delete();
    }
}