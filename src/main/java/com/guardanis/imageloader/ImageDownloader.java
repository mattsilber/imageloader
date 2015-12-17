package com.guardanis.imageloader;

import android.os.Handler;
import android.util.Log;

import java.io.File;

public class ImageDownloader implements Runnable {

    public interface DownloadEventListener {
        public void onDownloadCompleted(ImageDownloader request, String targetUrl);
        public void onDownloadFailed(ImageDownloader request, String targetUrl);
    }

    private static final String TAG = "AIL";

    protected ImageRequest imageRequest;
    protected DownloadEventListener downloadEventListener;
    protected Handler handler;

    public ImageDownloader(Handler handler, ImageRequest imageRequest, DownloadEventListener downloadEventListener){
        this.handler = handler;
        this.imageRequest = imageRequest;
        this.downloadEventListener = downloadEventListener;
    }

    @Override
    public void run() {
        try{
            File downloadedFile = ImageLoader.getInstance(imageRequest.getContext())
                    .download(imageRequest.getTargetUrl(), imageRequest.getOriginalRequestFile());

            if(downloadedFile != null){
                handler.post(new Runnable(){
                    public void run() {
                        Log.d(TAG, "Download success: " + imageRequest.getTargetUrl());
                        downloadEventListener.onDownloadCompleted(ImageDownloader.this, imageRequest.getTargetUrl());
                    }
                });
                return;
            }
        }
        catch(Throwable e){ e.printStackTrace(); }

        handler.post(new Runnable(){
            public void run() {
                Log.d(TAG, "Download failed: " + imageRequest.getTargetUrl());
                downloadEventListener.onDownloadFailed(ImageDownloader.this, imageRequest.getTargetUrl());
            }
        });
    }

}
