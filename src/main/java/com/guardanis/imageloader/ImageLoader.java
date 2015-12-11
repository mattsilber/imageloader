package com.guardanis.imageloader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {

    private static ImageLoader instance = null;

    public static synchronized ImageLoader getInstance(Context context) {
        if(instance == null || instance.context != context.getApplicationContext())
            instance = new ImageLoader(context);

        return instance;
    }

    protected Context context;
    protected FileCache fileCache;
    protected Map<View, String> views = Collections.synchronizedMap(new WeakHashMap<View, String>());
    protected ExecutorService executorService;

    private StubHolder stubHolder;

    protected ImageLoader(Context context) {
        this.context = context.getApplicationContext();
        this.fileCache = new FileCache(context);
        this.executorService = Executors.newFixedThreadPool(context.getResources().getInteger(R.integer.ail__thread_pool_size));
    }

    public Bitmap download(String url, int requiredWidth, File file) {
        HttpURLConnection conn = null;
        InputStream is = null;

        try{
            conn = openImageDownloadConnection(getCorrectDownloadUrl(url));
            is = conn.getInputStream();
            ImageUtils.saveStream(file, is);

            return ImageUtils.decodeFile(file, requiredWidth);
        }
        catch(Throwable ex){ ex.printStackTrace(); }
        finally{ closeConnection(is); }

        return null;
    }

    protected URL getCorrectDownloadUrl(String url) throws Exception {
        URL imageUrl = new URL(url);

        if(url.contains("graph.facebook.com"))
            imageUrl = getAdjustedFacebookImageUrl(imageUrl);

        return imageUrl;
    }

    protected URL getAdjustedFacebookImageUrl(URL imageUrl) throws Exception {
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
        URL adjustedImageUrl = new URL(conn.getHeaderField("Location"));
        conn.disconnect();
        HttpURLConnection.setFollowRedirects(true);
        return adjustedImageUrl;
    }

    protected HttpURLConnection openImageDownloadConnection(URL url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        conn.setInstanceFollowRedirects(true);
        return conn;
    }

    public static void closeConnection(InputStream is) {
        try{
            if(is != null)
                is.close();
        }
        catch(Exception e){ }
    }

    public void addViewAndTargetUrl(View view, String targetUrl) {
        views.put(view, targetUrl);
    }

    public boolean isViewStillUsable(View target, String expectedTag) {
        return expectedTag.equals(views.get(target));
    }

    public FileCache getFileCache() {
        return fileCache;
    }

    public ExecutorService getExecutorService(){
        return executorService;
    }

    public StubHolder getStubs() {
        if(stubHolder == null)
            return defaultStubHolder;
        else return stubHolder;
    }

    public void registerStubs(StubHolder stubHolder) {
        this.stubHolder = stubHolder;
    }

    private final StubHolder defaultStubHolder = new StubHolder(){

        @Override
        public Drawable getLoadingDrawable(Context context) {
            return ContextCompat.getDrawable(context, R.drawable.ail__image_loader_stub);
        }

        @Override
        public Drawable getErrorDrawable(Context context) {
            return ContextCompat.getDrawable(context, R.drawable.ail__image_loader_stub_error);
        }
    };
}
