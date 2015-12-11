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
import com.guardanis.imageloader.filters.BitmapColorOverlayFilter;
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

    protected boolean fromAssets = false;

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

    public ImageRequest<V> setTargetAssetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
        this.fromAssets = true;
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

    public ImageRequest<V> setFromAssets() {
        this.fromAssets = true;
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
        if(targetView == null)
            prefetchOriginalImage();
        else performFullImageRequest();
    }

    private void prefetchOriginalImage() {
        File originalImageFile = getOriginalRequestFile();
        if(!originalImageFile.exists())
            downloadImage(originalImageFile, 10);
    }

    private void performFullImageRequest() {
        int requiredImageWidth = targetView == null ? 10 : targetView.getLayoutParams().width;

        File imageFile = getEditedRequestFile();
        if(!imageFile.exists()){
            File originalImageFile = getOriginalRequestFile();
            if(originalImageFile.exists()){
                Bitmap original = ImageUtils.getInstance().decodeFile(originalImageFile, requiredImageWidth);
                if(original == null)
                    downloadAndProcess(originalImageFile, imageFile, requiredImageWidth);
                else processImage(imageFile, original);
            }
            else downloadAndProcess(originalImageFile, imageFile, requiredImageWidth);
        }
        else
            onRequestSuccessful(ImageUtils.getInstance().decodeFile(imageFile, requiredImageWidth));
    }

    private void downloadAndProcess(File originalImageFile, File imageFile, int requiredImageWidth) {
        Bitmap original = downloadImage(originalImageFile, requiredImageWidth);
        if(original == null)
            onRequestFailed();
        else processImage(imageFile, original);
    }

    private Bitmap downloadImage(File originalImageFile, int requiredImageWidth) {
        if(fromAssets)
            return ImageUtils.getInstance().decodeSVGAsset(context, targetUrl, requiredImageWidth);
        else
            return ImageLoader.getInstance(context).download(targetUrl, requiredImageWidth, originalImageFile);
    }

    private void processImage(File imageFile, Bitmap bitmap) {
        if(0 < bitmapImageFilters.size()){
            for(ImageFilter<Bitmap> filter : bitmapImageFilters)
                    bitmap = filter.filter(bitmap);

            // Don't save it if it's from assets, even if we've edited it
            if(!fromAssets)
                ImageUtils.getInstance().saveBitmap(context, imageFile, bitmap);
        }

        onRequestSuccessful(bitmap);
    }

    private void onRequestSuccessful(final Bitmap bitmap) {
        if(targetView == null || !ImageLoader.getInstance(context).isViewStillUsable(targetView, targetUrl))
            return;

        try{
            new Handler(Looper.getMainLooper()).post(new Runnable() {
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

    private void onRequestFailed() {
        if(targetView == null || !ImageLoader.getInstance(context).isViewStillUsable(targetView, targetUrl))
            return;

        if(showStubOnError)
            setTargetViewDrawable(getStubs().getErrorDrawable(context));
        else setTargetViewDrawable(null);
    }

    private String getFullRequestFileCacheName() {
        String adjustedName = targetUrl;

        for(ImageFilter<Bitmap> filter : bitmapImageFilters)
            adjustedName += "_" + filter.getAdjustmentInfo();

        if(fromAssets || targetUrl.endsWith(".svg"))
            adjustedName += ".svg";

        return adjustedName;
    }

    private File getEditedRequestFile() {
        return ImageLoader.getInstance(context).getFileCache().getFile(getFullRequestFileCacheName());
    }

    private String getOriginalRequestFileCacheName() {
        return targetUrl + ((fromAssets || targetUrl.endsWith(".svg")) ? ".svg" : "");
    }

    private File getOriginalRequestFile() {
        return ImageLoader.getInstance(context).getFileCache().getFile(getOriginalRequestFileCacheName());
    }

    private void setTargetViewDrawable(final Drawable drawable){
        targetView.post(new Runnable(){
            public void run(){
                if(targetView instanceof ImageView && !setImageAsBackground)
                    ((ImageView) targetView).setImageDrawable(drawable);
                else setBackgroundDrawable(targetView, drawable);
            }
        });
    }

    @SuppressLint("NewApi")
    private void setBackgroundDrawable(View v, Drawable drawable) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
            v.setBackgroundDrawable(drawable);
        else v.setBackground(drawable);
    }

    private StubHolder getStubs(){
        if(stubHolder == null)
            return ImageLoader.getInstance(context).getStubs();
        else return stubHolder;
    }

    public void execute() {
        if(targetView != null){
            ImageLoader.getInstance(context)
                    .addViewAndTargetUrl(targetView, targetUrl);

            if(showStubOnExecute)
                setTargetViewDrawable(getStubs().getLoadingDrawable(context));
            else setTargetViewDrawable(null);
        }

        ImageLoader.getInstance(context)
                .getExecutorService()
                .submit(this);
    }

}
