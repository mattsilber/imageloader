package com.guardanis.imageloader.processors;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.guardanis.imageloader.ImageRequest;
import com.guardanis.imageloader.ImageUtils;
import com.guardanis.imageloader.filters.ImageFilter;

import java.io.File;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;

public class ImageFileProcessor extends ImageProcessor {

    @Override
    public Drawable process(ImageRequest request, List<ImageFilter<Bitmap>> bitmapImageFilters) throws Exception {
        File target = new File(request.getTargetUrl());

        switch(request.getTargetImageType()){
            case GIF:
                return new GifDrawable(target);
            case BITMAP:
            case SVG:
            default:
                return processBitmap(request,
                        target,
                        bitmapImageFilters);
        }
    }

    protected Drawable processBitmap(ImageRequest request, File originalFile, List<ImageFilter<Bitmap>> bitmapImageFilters) throws Exception {
        int requiredImageWidth = request.getTargetImageWidth();

        File editedImageFile = request.getEditedRequestFile();
        if(!editedImageFile.exists()){
            if(originalFile.exists())
                return process(request.getContext(),
                        ImageUtils.decodeFile(originalFile, requiredImageWidth),
                        editedImageFile,
                        bitmapImageFilters);
        }
        else
            return decodeBitmapDrawable(request.getContext(),
                editedImageFile,
                requiredImageWidth);

        return null;
    }

}
