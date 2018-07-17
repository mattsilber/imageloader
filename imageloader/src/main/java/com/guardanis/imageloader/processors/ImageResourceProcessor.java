package com.guardanis.imageloader.processors;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.guardanis.imageloader.ImageRequest;
import com.guardanis.imageloader.ImageUtils;
import com.guardanis.imageloader.filters.ImageFilter;

import java.io.File;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;

public class ImageResourceProcessor extends ImageFileProcessor {

    @Override
    public Drawable process(ImageRequest request, List<ImageFilter<Bitmap>> bitmapImageFilters) throws Exception {
        switch(request.getTargetImageType()){
            case GIF:
                return new GifDrawable(request.getContext().getResources(), request.getTargetResourceId());
            case SVG:
                return process(request.getContext(),
                        ImageUtils.decodeSvgResource(request.getContext(),
                                request.getTargetResourceId(),
                                request.getTargetImageWidth()),
                        request.getEditedRequestFile(),
                        bitmapImageFilters);
            case BITMAP:
            default:
                return processBitmapResource(request, bitmapImageFilters);
        }
    }

    protected Drawable processBitmapResource(ImageRequest request, List<ImageFilter<Bitmap>> bitmapImageFilters) throws Exception {
        File editedImageFile = request.getEditedRequestFile();
        int requiredImageWidth = request.getTargetImageWidth();

        if(editedImageFile.exists())
            return decodeBitmapDrawable(request.getContext(), editedImageFile, requiredImageWidth);

        return process(request.getContext(),
                ImageUtils.decodeBitmapResource(request.getContext().getResources(),
                        request.getTargetResourceId(),
                        request.getTargetImageWidth()),
                editedImageFile,
                bitmapImageFilters);
    }
}
