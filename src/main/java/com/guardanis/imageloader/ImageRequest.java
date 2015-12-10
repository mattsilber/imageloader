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

import java.io.File;

public class ImageRequest<V extends View> implements Runnable {

    private Context context;
    private String targetUrl;

    private V targetView;
    private boolean setImageAsBackground = false;

    private boolean blurImage = false;
    private int blurRadius = 15;

    private int colorOverlay = -1;

    private boolean showStubOnExecute = true;
    private boolean showStubOnError = false;

    private boolean fromAssets = false;

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

    public ImageRequest<V> setSetImageAsBackground(boolean setImageAsBackground) {
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

    public ImageRequest<V> setBlurImage() {
        this.blurImage = true;
        return this;
    }

    public ImageRequest<V> setBlurRadius(int blurRadius) {
        this.blurRadius = blurRadius;
        return this;
    }

    public ImageRequest<V> setColorOverlay(int colorOverlay) {
        this.colorOverlay = colorOverlay;
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
        if(isImageEditingRequired()){
            if(blurImage)
                bitmap = BlurHandler.blurImageWithRenderScript(context, bitmap, blurRadius);

            if(colorOverlay > 0)
                bitmap = ImageUtils.getInstance().drawColorOverlay(bitmap, colorOverlay);

            // Don't save it if it's from assets, even if we've edited it
            if(!fromAssets)
                ImageUtils.getInstance().saveBitmap(context, imageFile, bitmap);
        }

        onRequestSuccessful(bitmap);
    }

    private boolean isImageEditingRequired() {
        return blurImage || 0 < colorOverlay;
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
            setTargetViewDrawable(ImageLoader.getInstance(context).getStubs().getErrorDrawable(context));
        else setTargetViewDrawable(null);
    }

    private String getFullRequestFileCacheName() {
        return targetUrl
                + (blurImage ? "_blurred" : "")
                + (colorOverlay < 0 ? "" : "_" + colorOverlay)
                + ((fromAssets || targetUrl.endsWith(".svg")) ? ".svg" : "");
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

    public void execute() {
        if(targetView != null){
            ImageLoader.getInstance(context)
                    .addViewAndTargetUrl(targetView, targetUrl);

            if(showStubOnExecute)
                setTargetViewDrawable(ImageLoader.getInstance(context).getStubs().getLoadingDrawable(context));
            else setTargetViewDrawable(null);
        }

        ImageLoader.getInstance(context)
                .getExecutorService()
                .submit(this);
    }

}
