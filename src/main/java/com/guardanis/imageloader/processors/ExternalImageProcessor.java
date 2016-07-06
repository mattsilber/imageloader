package com.guardanis.imageloader.processors;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.guardanis.imageloader.ImageRequest;
import com.guardanis.imageloader.filters.ImageFilter;

import java.util.List;

import pl.droidsonroids.gif.GifDrawable;

public class ExternalImageProcessor extends ImageFileProcessor {

    @Override
    public Drawable process(ImageRequest request, List<ImageFilter<Bitmap>> bitmapImageFilters) throws Exception {
        switch(request.getTargetImageType()){
            case GIF:
                return new GifDrawable(request.getOriginalRequestFile());
            case BITMAP:
            case SVG:
            default:
                return processBitmap(request,
                        request.getOriginalRequestFile(),
                        bitmapImageFilters);
        }
    }

}
