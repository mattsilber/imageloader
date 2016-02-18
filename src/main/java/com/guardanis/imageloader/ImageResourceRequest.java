package com.guardanis.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

public class ImageResourceRequest<V extends View> extends LocalImageRequest<V> {

    protected int imageResourceId;

    public ImageResourceRequest(Context context, int imageResourceId) {
        super(context, "");

        this.imageResourceId = imageResourceId;
    }

    @Override
    public ImageResourceRequest<V> setTargetUrl(String targetUrl) {
        throw new UnsupportedOperationException(context.getString(R.string.ail__image_resource_request_error_constructor));
    }

    @Override
    protected Bitmap decodeImageFile(int requiredImageWidth) {
        return ImageUtils.decodeBitmapResource(context.getResources(), imageResourceId, requiredImageWidth);
    }

}
