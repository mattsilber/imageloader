package com.guardanis.imageloader;

import android.content.Context;
import android.view.View;

import java.io.File;

public class SVGAssetRequest<V extends View>  extends ImageRequest<V> {

    public SVGAssetRequest(Context context) {
        this(context, "");
    }

    public SVGAssetRequest(Context context, String targetUrl) {
        super(context, targetUrl);
        setShowStubOnExecute(false);
    }

    @Override
    protected void performFullImageRequest() {
        int requiredImageWidth = targetView.getLayoutParams().width;

        File imageFile = getEditedRequestFile();
        if(imageFile.exists())
            onRequestSuccessful(ImageUtils.decodeFile(imageFile, requiredImageWidth));
        else processImage(getEditedRequestFile(),
                ImageUtils.decodeSVGAsset(context, targetUrl, requiredImageWidth));
    }

}
