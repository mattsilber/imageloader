package com.guardanis.imageloader.processors;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.guardanis.imageloader.ImageRequest;
import com.guardanis.imageloader.ImageUtils;
import com.guardanis.imageloader.filters.ImageFilter;

import java.io.File;
import java.util.List;

public class ImageDrawableResourceProcessor extends ImageFileProcessor {

    @Override
    public Drawable process(ImageRequest request, List<ImageFilter<Bitmap>> bitmapImageFilters) throws Exception {
        File editedImageFile = request.getEditedRequestFile();
        int requiredImageWidth = request.getTargetImageWidth();

        if(editedImageFile.exists())
            return decodeBitmapDrawable(request.getContext(), editedImageFile, requiredImageWidth);

        Bitmap uneditedBitmap = ImageUtils.decodeDrawableResource(request.getContext(), request.getTargetResourceId(), requiredImageWidth);

        return process(request.getContext(), uneditedBitmap, editedImageFile, bitmapImageFilters);
    }
}
