package com.guardanis.imageloader.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

public class BitmapCircularCropFilter extends ImageFilter<Bitmap> {

    private float scale = 1f;
    private float[] offsets = new float[]{ 0, 0 };

    public BitmapCircularCropFilter(Context context) {
        super(context);
    }

    /**
     * @param scale scale < 1 = larger (e.g. circle), scale > 1 = smaller (e.g. rounded rectangle)
     */
    public BitmapCircularCropFilter setScaledDimensions(float scale){
        this.scale = scale;
        return this;
    }

    public BitmapCircularCropFilter setOffsets(float x, float y){
        this.offsets = new float[]{ x, y };
        return this;
    }

    @Override
    public Bitmap filter(Bitmap unedited) {
        Bitmap cropped = Bitmap.createBitmap(unedited.getWidth(), unedited.getHeight(), Bitmap.Config.ARGB_8888);

        int xScaled = (int)(scale * unedited.getWidth());
        int yScaled = (int)(scale * unedited.getHeight());

        int xTranslate = (int)(offsets[0] * xScaled) + ((unedited.getWidth() - xScaled) / 2);
        int yTranslate = (int)(offsets[1] * yScaled) + ((unedited.getHeight() - yScaled) / 2);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Canvas canvas = new Canvas(cropped);
        canvas.clipPath(getCircularClippingPath(cropped));
        canvas.drawBitmap(unedited, new Rect(xTranslate, yTranslate, xTranslate + xScaled, yTranslate + yScaled),
                new Rect(0, 0, unedited.getWidth(), unedited.getHeight()), paint);

        return cropped;
    }

    private Path getCircularClippingPath(Bitmap bitmap) {
        int targetRadius = Math.min(bitmap.getWidth(), bitmap.getHeight()) / 2;

        Path path = new Path();
        path.addCircle(bitmap.getWidth() / 2f,
                bitmap.getHeight() / 2f,
                targetRadius,
                Path.Direction.CCW);

        return path;
    }

    @Override
    public String getAdjustmentInfo(){
        return super.getAdjustmentInfo() + "-" + scale +"," + offsets[0] + "," + offsets[1] + "-";
    }
}
