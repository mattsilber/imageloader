package com.guardanis.imageloader.processors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.guardanis.imageloader.ImageRequest;
import com.guardanis.imageloader.ImageUtils;
import com.guardanis.imageloader.filters.ImageFilter;

import java.io.File;
import java.util.List;

public abstract class ImageProcessor {

    public abstract Drawable process(ImageRequest request, List<ImageFilter<Bitmap>> bitmapImageFilters) throws Exception;

    protected Drawable process(Context context, Bitmap bitmap, File targetSaveFile, List<ImageFilter<Bitmap>> bitmapImageFilters){
        Bitmap edited = applyFilters(context, bitmap, bitmapImageFilters);

        if(edited != null && 0 < bitmapImageFilters.size())
            ImageUtils.saveBitmapAsync(context, targetSaveFile, edited);

        return new BitmapDrawable(context.getResources(), edited);
    }

    protected Bitmap decodeBitmap(File file, int requiredImageWidth) {
        return ImageUtils.decodeFile(file, requiredImageWidth);
    }

    protected BitmapDrawable decodeBitmapDrawable(Context context, File file, int requiredImageWidth) {
        return new BitmapDrawable(context.getResources(),
                decodeBitmap(file, requiredImageWidth));
    }

    protected Bitmap applyFilters(Context context, Bitmap bitmap, List<ImageFilter<Bitmap>> bitmapImageFilters) {
        if(0 < bitmapImageFilters.size() && bitmap != null){
            try{
                for(ImageFilter<Bitmap> filter : bitmapImageFilters)
                    bitmap = filter.filter(bitmap);
            }
            catch(Throwable e){
                ImageUtils.log(context, e);

                // Let it fail gracefully if our filters couldn't recover
                bitmap = null;
            }
        }

        return bitmap;
    }

}
