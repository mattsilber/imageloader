package com.guardanis.imageloader.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;

public class BitmapCircularCropFilter extends ImageFilter<Bitmap> {

    public BitmapCircularCropFilter(Context context) {
        super(context);
    }

    @Override
    public Bitmap filter(Bitmap unedited) {
        Bitmap cropped = Bitmap.createBitmap(unedited.getWidth(), unedited.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(cropped);
        canvas.clipPath(getCircularClippingPath(unedited));
        canvas.drawBitmap(unedited, new Rect(0, 0, unedited.getWidth(), unedited.getHeight()), new Rect(0, 0, unedited.getWidth(), unedited.getHeight()), null);

        return cropped;
    }

    private Path getCircularClippingPath(Bitmap bitmap) {
        int targetRadius = (int) (Math.min(bitmap.getWidth(), bitmap.getHeight()) / 2f) - 1;

        Path path = new Path();
        path.addCircle((bitmap.getWidth() - 1) / 2f,
                (bitmap.getHeight() - 1) / 2f,
                targetRadius,
                Path.Direction.CCW);

        return path;
    }
}
