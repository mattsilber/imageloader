package com.guardanis.imageloader.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitmapRotationFilter extends ImageFilter<Bitmap> {

    private int rotationDegrees;

    public BitmapRotationFilter(Context context, int rotationDegrees) {
        super(context);
        this.rotationDegrees = rotationDegrees;
    }

    @Override
    public Bitmap filter(Bitmap unedited) {
        Matrix mtx = new Matrix();
        mtx.postRotate(rotationDegrees);

        return Bitmap.createBitmap(unedited, 0, 0, unedited.getWidth(), unedited.getHeight(), mtx, true);
    }
}
