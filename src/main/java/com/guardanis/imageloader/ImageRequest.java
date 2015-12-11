package com.guardanis.imageloader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import com.guardanis.imageloader.filters.BitmapBlurFilter;
import com.guardanis.imageloader.filters.BitmapCircularCropFilter;
import com.guardanis.imageloader.filters.BitmapColorOverlayFilter;
import com.guardanis.imageloader.filters.BitmapRotationFilter;
import com.guardanis.imageloader.filters.ImageFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageRequest<V extends View> implements Runnable {

    protected static final int DEFAULT_BLUR_RADIUS = 15;

    protected Context context;
    protected String targetUrl;

    protected V targetView;
    protected boolean setImageAsBackground = false;

    protected List<ImageFilter<Bitmap>> bitmapImageFilters = new ArrayList<ImageFilter<Bitmap>>();

    protected boolean showStubOnExecute = true;
    protected boolean showStubOnError = false;

    protected StubHolder stubHolder;

    public ImageRequest(Context context) {
        this(context, "");
    }

    public ImageRequest(Context context, String targetUrl) {
        this.context = context;
        this.targetUrl = targetUrl;
    }

    public ImageRequest<V> setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
        return this;
    }

    public ImageRequest<V> setTargetView(V targetView) {
        this.targetView = targetView;
        return this;
    }

    public ImageRequest<V> setImageAsBackground() {
        this.setImageAsBackground = true;
        return this;
    }

    public ImageRequest<V> setImageAsBackground(boolean setImageAsBackground) {
        this.setImageAsBackground = setImageAsBackground;
        return this;
    }

    public ImageRequest<V> setShowStubOnExecute(boolean showStubOnExecute) {
        this.showStubOnExecute = showStubOnExecute;
        return this;
    }

    public ImageRequest<V> setShowStubOnError(boolean showStubOnError) {
        this.showStubOnError = showStubOnError;
        return this;
    }

    public ImageRequest<V> addBlurFilter(){
        return addBlurFilter(DEFAULT_BLUR_RADIUS);
    }

    public ImageRequest<V> addBlurFilter(int blurRadius){
        bitmapImageFilters.add(new BitmapBlurFilter(context, blurRadius));
        return this;
    }

    public ImageRequest<V> addColorOverlayFilter(int colorOverlay) {
        bitmapImageFilters.add(new BitmapColorOverlayFilter(context, colorOverlay));
        return this;
    }

    public ImageRequest<V> addRotationFilter(int rotationDegrees) {
        bitmapImageFilters.add(new BitmapRotationFilter(context, rotationDegrees));
        return this;
    }

    public ImageRequest<V> addCircularCropFilter() {
        bitmapImageFilters.add(new BitmapCircularCropFilter(context));
        return this;
    }

    public ImageRequest<V> addImageFilter(ImageFilter<Bitmap> imageFilter){
        bitmapImageFilters.add(imageFilter);
        return this;
    }

    public ImageRequest<V> overrideStubs(StubHolder stubHolder){
        this.stubHolder = stubHolder;
        return this;
    }

    @Override
    public void run() {
        if(targetView != null)
            performFullImageRequest();
    }

    protected void performFullImageRequest() {
        int requiredImageWidth = targetView.getLayoutParams().width;

        File imageFile = getEditedRequestFile();
        if(!imageFile.exists()){
            File originalImageFile = getOriginalRequestFile();
            if(!originalImageFile.exists())
                onRequestFailed();
            else processImage(imageFile, ImageUtils.decodeFile(originalImageFile, requiredImageWidth));
        }
        else
            onRequestSuccessful(ImageUtils.decodeFile(imageFile, requiredImageWidth));
    }

    protected void processImage(File imageFile, Bitmap bitmap) {
        if(0 < bitmapImageFilters.size()){
            applyBitmapFilters(bitmap);
            saveBitmap(imageFile, bitmap);
        }

        onRequestSuccessful(bitmap);
    }

    protected void applyBitmapFilters(Bitmap bitmap){
        for(ImageFilter<Bitmap> filter : bitmapImageFilters)
            bitmap = filter.filter(bitmap);
    }

    protected void saveBitmap(File imageFile, Bitmap bitmap){
        ImageUtils.saveBitmap(context, imageFile, bitmap);
    }

    protected void onRequestSuccessful(final Bitmap bitmap) {
        if(targetView == null || !ImageLoader.getInstance(context).isViewStillUsable(targetView, targetUrl))
            return;

        try{
            targetView.post(new Runnable() {
                public void run() {
                    if(targetView instanceof ImageView && !setImageAsBackground)
                        ((ImageView) targetView).setImageBitmap(bitmap);
                    else
                        setBackgroundDrawable(targetView, new BitmapDrawable(targetView.getContext().getResources(), bitmap));
                }
            });
        }
        catch(Throwable e){ e.printStackTrace(); }
    }

    protected void onRequestFailed() {
        if(targetView == null || !ImageLoader.getInstance(context).isViewStillUsable(targetView, targetUrl))
            return;

        if(showStubOnError)
            setTargetViewDrawable(getStubs().getErrorDrawable(context));
        else setTargetViewDrawable(null);
    }

    protected String getFullRequestFileCacheName() {
        String adjustedName = targetUrl;

        for(ImageFilter<Bitmap> filter : bitmapImageFilters)
            adjustedName += "_" + filter.getAdjustmentInfo();

        return adjustedName;
    }

    protected File getEditedRequestFile() {
        return ImageLoader.getInstance(context).getFileCache().getFile(getFullRequestFileCacheName());
    }

    protected String getOriginalRequestFileCacheName() {
        return targetUrl;
    }

    public Context getContext(){
        return context;
    }

    /**
     * @return the File associated with the target URL. File won't be null, but may not exist.
     */
    public File getOriginalRequestFile() {
        return ImageLoader.getInstance(context)
                .getFileCache()
                .getFile(getOriginalRequestFileCacheName());
    }

    protected void setTargetViewDrawable(final Drawable drawable){
        targetView.post(new Runnable(){
            public void run(){
                if(targetView instanceof ImageView && !setImageAsBackground)
                    ((ImageView) targetView).setImageDrawable(drawable);
                else setBackgroundDrawable(targetView, drawable);
            }
        });
    }

    @SuppressLint("NewApi")
    protected void setBackgroundDrawable(View v, Drawable drawable) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
            v.setBackgroundDrawable(drawable);
        else v.setBackground(drawable);
    }

    protected StubHolder getStubs(){
        if(stubHolder == null)
            return ImageLoader.getInstance(context).getStubs();
        else return stubHolder;
    }

    public String getTargetUrl(){
        return targetUrl;
    }

    public void execute() {
        if(targetView == null)
            ImageLoader.getInstance(context).submit(this);
        else {
            targetView.post(new Runnable(){
                public void run(){
                    ImageLoader.getInstance(context)
                            .addViewAndTargetUrl(targetView, targetUrl);

                    if(showStubOnExecute)
                        setTargetViewDrawable(getStubs().getLoadingDrawable(context));
                    else setTargetViewDrawable(null);

                    ImageLoader.getInstance(context).submit(ImageRequest.this);
                }
            });
        }
    }

}
