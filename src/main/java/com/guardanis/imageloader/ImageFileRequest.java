package com.guardanis.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import java.io.File;

public class ImageFileRequest<V extends View> extends LocalImageRequest<V> {

    protected File targetImageFile;

    public ImageFileRequest(Context context) {
        this(context, "");
    }

    public ImageFileRequest(Context context, String targetUrl) {
        this(context, new File(targetUrl));
    }

    public ImageFileRequest(Context context, File targetImageFile) {
        super(context, targetImageFile.getAbsolutePath());
        this.targetImageFile = targetImageFile;
    }

    @Override
    public ImageFileRequest<V> setTargetUrl(String targetUrl) {
        super.setTargetUrl(targetUrl);
        this.targetImageFile = new File(targetUrl);
        return this;
    }

    @Override
    protected Bitmap decodeImageFile(int requiredImageWidth) {
        return ImageUtils.decodeFile(targetImageFile, requiredImageWidth);
    }


}
