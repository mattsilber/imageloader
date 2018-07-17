package com.guardanis.imageloader.processors;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.guardanis.imageloader.ImageRequest;
import com.guardanis.imageloader.ImageUtils;
import com.guardanis.imageloader.filters.ImageFilter;

import java.io.File;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;

public class ImageAssetProcessor extends ImageFileProcessor {

    @Override
    public Drawable process(ImageRequest request, List<ImageFilter<Bitmap>> bitmapImageFilters) throws Exception {
        switch(request.getTargetImageType()){
            case GIF:
                return new GifDrawable(request.getContext()
                        .getAssets()
                        .openFd(request.getTargetUrl()));
            case SVG:
            case BITMAP:
            default:
                return processAsset(request, bitmapImageFilters);
        }
    }

    protected Drawable processAsset(ImageRequest request, List<ImageFilter<Bitmap>> bitmapImageFilters) throws Exception {
        File editedImageFile = request.getEditedRequestFile();
        int requiredImageWidth = request.getTargetImageWidth();

        if(editedImageFile.exists())
            return decodeBitmapDrawable(request.getContext(),
                    editedImageFile,
                    requiredImageWidth);

        Bitmap asset = request.getTargetImageType() == ImageUtils.ImageType.SVG
                ? ImageUtils.decodeSVGAsset(request.getContext(), request.getTargetUrl(), requiredImageWidth)
                : ImageUtils.decodeBitmapAsset(request.getContext(), request.getTargetUrl(), requiredImageWidth);

        return process(request.getContext(), asset, editedImageFile, bitmapImageFilters);
    }
}
