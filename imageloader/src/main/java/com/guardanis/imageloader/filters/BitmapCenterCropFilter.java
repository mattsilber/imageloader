package com.guardanis.imageloader.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import androidx.annotation.NonNull;

public class BitmapCenterCropFilter extends ImageFilter<Bitmap> {

    private int[] desiredBounds;

    /**
     * @param context
     * @param desiredBounds non-null width/height, respectively
     */
    public BitmapCenterCropFilter(Context context, @NonNull int[] desiredBounds) {
        super(context);
        this.desiredBounds = desiredBounds;
    }

    @Override
    public Bitmap filter(Bitmap unedited) {
        float scale = Math.max(desiredBounds[0] / (float) unedited.getWidth(),
                desiredBounds[1] / (float) unedited.getHeight());

        int xTranslate = (int) -((scale * unedited.getWidth()) - desiredBounds[0]) / 2;
        int yTranslate = (int) -((scale * unedited.getHeight()) - desiredBounds[1]) / 2;

        Bitmap adjusted = Bitmap.createBitmap(desiredBounds[0],
                desiredBounds[1],
                unedited.getConfig());

        Canvas canvas = new Canvas(adjusted);
        canvas.translate(xTranslate, yTranslate);
        canvas.scale(scale, scale);

        canvas.drawBitmap(unedited, 0, 0, null);

        return adjusted;
    }

    @Override
    public String getAdjustmentInfo(){
        return getClass().getSimpleName() + "_" + desiredBounds[0] + "," + desiredBounds[1];
    }
}