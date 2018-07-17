package com.guardanis.imageloader.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.guardanis.imageloader.ImageUtils;

public class BitmapRotationFilter extends ImageFilter<Bitmap> {

    private int rotationDegrees;

    public BitmapRotationFilter(Context context, int rotationDegrees) {
        super(context);
        this.rotationDegrees = rotationDegrees;
    }

    @Override
    public Bitmap filter(Bitmap unedited) {
        if(rotationDegrees % 360 == 0)
            return unedited;

        Matrix mtx = new Matrix();

        mtx.setRotate(rotationDegrees,
                unedited.getWidth() / 2,
                unedited.getHeight() / 2);

        try{
            return Bitmap.createBitmap(unedited, 0, 0,
                    unedited.getWidth(), unedited.getHeight(),
                    mtx, true);
        }
        catch(OutOfMemoryError e){ // Need to work on this one with large, non-downsampled, images... Fuck.
            ImageUtils.log(context, e);

            System.gc();
        }

        return unedited;
    }
}
