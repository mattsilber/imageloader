package com.guardanis.imageloader.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;

public class BitmapColorFilter extends ImageFilter<Bitmap> {

    private ColorFilter filter;

    public BitmapColorFilter(Context context, ColorFilter filter) {
        super(context);
        this.filter = filter;
    }

    @Override
    public Bitmap filter(Bitmap unedited) {
        if(unedited != null){
            Bitmap adjusted = Bitmap.createBitmap(unedited.getWidth(), unedited.getHeight(), Bitmap.Config.ARGB_8888);

            Paint paint = new Paint();
            paint.setColorFilter(filter);

            Canvas canvas = new Canvas(unedited);
            canvas.drawBitmap(adjusted, 0, 0, paint);
        }

        return unedited;
    }

    @Override
    public String getAdjustmentInfo(){
        return getClass().getSimpleName() + "_" + filter.getClass().getSimpleName(); // may not be enough...
    }
}
