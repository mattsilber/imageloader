package com.guardanis.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import java.io.File;

public abstract class LocalImageRequest<V extends View>  extends ImageRequest<V> {

    public LocalImageRequest(Context context) {
        this(context, "");
    }

    public LocalImageRequest(Context context, String targetUrl) {
        super(context, targetUrl);
        setShowStubOnExecute(false);
    }

    @Override
    protected void performFullImageRequest() {
        int requiredImageWidth = getRequiredImageWidth();

        File imageFile = getEditedRequestFile();
        if(imageFile.exists())
            onRequestCompleted(ImageUtils.decodeFile(imageFile, requiredImageWidth));
        else processImage(getEditedRequestFile(), decodeImageFile(requiredImageWidth));
    }

    protected abstract Bitmap decodeImageFile(int requiredImageWidth);

}
