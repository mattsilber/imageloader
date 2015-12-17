package com.guardanis.imageloader.transitions.drawables;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

public class FadingTransitionDrawable extends TransitionDrawable {

    protected final int MAX_ALPHA = 255;
    protected final int TRANSITION_OUT_SPEED_MULTIPLIER = 2;

    protected int alpha = 0xFF;
    protected int stubStartingAlpha = 0xFF;

    @SuppressLint("NewApi")
    public FadingTransitionDrawable(Context context, Drawable from, Bitmap to, int fadeDuration) {
        super(context, from, to, fadeDuration);

        if(Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT && from != null)
            stubStartingAlpha = from.getAlpha();
    }

    @Override
    protected void drawBackgroundTransition(Canvas canvas, float normalizedPercentCompleted) {
        stubDrawable.setAlpha((int)Math.max(MAX_ALPHA - (stubStartingAlpha * normalizedPercentCompleted * TRANSITION_OUT_SPEED_MULTIPLIER), 0));
        stubDrawable.draw(canvas);
        stubDrawable.setAlpha(stubStartingAlpha);
    }

    @Override
    protected void drawForegroundTransition(Canvas canvas, float normalizedPercentCompleted) {
        super.setAlpha((int) (alpha * normalizedPercentCompleted));
        drawSuper(canvas);
        super.setAlpha(alpha);
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
        return PixelFormat.RGBA_8888;
    }

}
