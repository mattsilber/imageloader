package com.guardanis.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

public class SVGAssetRequest<V extends View>  extends LocalImageRequest<V> {

    public SVGAssetRequest(Context context) {
        this(context, "");
    }

    public SVGAssetRequest(Context context, String targetUrl) {
        super(context, targetUrl);
    }

    @Override
    protected Bitmap decodeImageFile(int requiredImageWidth) {
        return ImageUtils.decodeSVGAsset(context, targetUrl, requiredImageWidth);
    }

}
