package com.guardanis.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.guardanis.imageloader.filters.ImageFilter;

import java.io.File;

import pl.droidsonroids.gif.GifDrawable;

public class GifRequest<V extends View> extends ImageRequest<V> {

    public interface FileLoader {
        public GifDrawable getGifDrawable() throws Exception;
    }

    protected FileLoader fileLoader;
    protected boolean requestIsLocal;

    public GifRequest(Context context) {
        super(context);
    }

    @Override
    public GifRequest<V> setTargetUrl(String targetUrl){
        throw new UnsupportedOperationException("Gif targets must be exclusively declared.");
    }

    public GifRequest<V> setTargetExternalUrl(String targetUrl){
        super.setTargetUrl(targetUrl);

        fileLoader = new FileLoader() {
            public GifDrawable getGifDrawable() throws Exception {
                return new GifDrawable(getOriginalRequestFile());
            }
        };

        requestIsLocal = false;

        return this;
    }

    public GifRequest<V> setTargetAssetUrl(final String targetUrl){
        super.setTargetUrl(targetUrl);

        fileLoader = new FileLoader() {
            public GifDrawable getGifDrawable() throws Exception {
                return new GifDrawable(context.getAssets().openFd(targetUrl));
            }
        };

        requestIsLocal = true;

        return this;
    }

    public GifRequest<V> setTargetFileUrl(final String targetUrl){
        super.setTargetUrl(targetUrl);

        fileLoader = new FileLoader() {
            public GifDrawable getGifDrawable() throws Exception {
                return new GifDrawable(new File(targetUrl));
            }
        };

        requestIsLocal = true;

        return this;
    }

    @Override
    public ImageRequest<V> addImageFilter(ImageFilter<Bitmap> imageFilter){
        throw new UnsupportedOperationException("ImageFilters of type Bitmap are unsupported for Gifs");
    }

    @Override
    public ImageRequest<V> setSuccessCallback(ImageSuccessCallback successCallback) {
        throw new UnsupportedOperationException("Gif targets must be exclusively declared.");
    }

    @Override
    protected void performFullImageRequest() {
        try{
            onRequestCompleted(fileLoader.getGifDrawable());
        }
        catch(Exception e){
            ImageUtils.log(context, e);

            onRequestFailed();
        }
    }

    protected void onRequestCompleted(GifDrawable gif){
        if(gif == null)
            onRequestFailed();
        else if(targetView == null || !ImageLoader.getInstance(context).isViewStillUsable(this))
            return;

        if(transitionOnSuccessEnabled)
            transitionController.transitionTo(gif);
    }

    @Override
    public boolean isTargetLocal(){
        return requestIsLocal;
    }

}
