package com.guardanis.imageloader.transitions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

// Idea stub: https://github.com/bumptech/glide/issues/363#issuecomment-139541960
public class FadingTransitionDrawable extends BitmapDrawable {

    protected final int TRANSITION_OUT_SPEED_MULTIPLIER = 2;

    protected int fadeDuration;
    protected Drawable stub;

    protected long animationStart;
    protected boolean animating = false;

    protected int alpha = 0xFF;

    public FadingTransitionDrawable(Context context, Drawable from, Bitmap to, int fadeDuration) {
        super(context.getResources(), to);

        this.stub = from;
        this.fadeDuration = fadeDuration;
        this.animating = true;
        this.animationStart = System.currentTimeMillis();
    }

    @Override
    public void draw(Canvas canvas) {
        if(animating){
            float normalized = (System.currentTimeMillis() - animationStart) / (float)fadeDuration;

            if (1f <= normalized) {
                animating = false;
                stub = null;
                super.draw(canvas);
            }
            else {
                int partialAlpha = (int) (alpha * normalized);

                if (stub != null)
                    drawStub(canvas, partialAlpha);

                super.setAlpha(partialAlpha);
                super.draw(canvas);
                super.setAlpha(alpha);

                this.invalidateSelf();
            }
        }
        else super.draw(canvas);
    }

    private void drawStub(Canvas canvas, int partialAlpha){
        canvas.save();

        int halfXDistance = (Math.max(getBounds().right, stub.getBounds().right) - Math.min(getBounds().right, stub.getBounds().right)) / 2;
        if(getBounds().right < stub.getBounds().right)
            halfXDistance *= -1;

        int halfYDistance = (Math.max(getBounds().bottom, stub.getBounds().bottom) - Math.min(getBounds().bottom, stub.getBounds().bottom)) / 2;
        if(getBounds().bottom < stub.getBounds().bottom)
            halfYDistance *= -1;

        canvas.translate(halfXDistance, halfYDistance);

        stub.setAlpha(Math.max(255 - (partialAlpha * TRANSITION_OUT_SPEED_MULTIPLIER), 0));
        stub.draw(canvas);
        stub.setAlpha(alpha);

        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    @Override
    public void setColorFilter(ColorFilter cf)  {
        // Nothing
    }

    @Override
    public int getOpacity(){
        return PixelFormat.OPAQUE;
    }

}
