package com.guardanis.imageloader.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class BitmapColorOverlayFilter extends ImageFilter<Bitmap> {

    private int bitmapColorOverlay;

    public BitmapColorOverlayFilter(Context context, int bitmapColorOverlay) {
        super(context);
        this.bitmapColorOverlay = bitmapColorOverlay;
    }

    @Override
    public Bitmap filter(Bitmap unedited) {
        if(unedited != null){
            if(!unedited.isMutable())
                unedited = mutate(unedited);

            Paint paint = new Paint();
            paint.setColor(bitmapColorOverlay);

            Canvas canvas = new Canvas(unedited);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
        }

        return unedited;
    }

    @Override
    public String getAdjustmentInfo(){
        return getClass().getSimpleName() + "_" + bitmapColorOverlay;
    }
}
