package com.guardanis.imageloader.transitions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

// Idea from: https://github.com/bumptech/glide/issues/363#issuecomment-139541960
public class FadingTransitionDrawable extends BitmapDrawable {

    protected int fadeDuration;
    protected Drawable stub;

    protected long animationStart;
    protected boolean animating = false;

    protected int alpha = 0xFF;

    public FadingTransitionDrawable(Context context, Drawable from, Bitmap to, int fadeDuration) {
        super(context.getResources(), to);

        this.stub = stub;
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
                if (stub != null)
                    stub.draw(canvas);

                int partialAlpha = (int) (alpha * normalized);
                super.setAlpha(partialAlpha);
                super.draw(canvas);
                super.setAlpha(alpha);

                this.invalidateSelf();
            }
        }
        else super.draw(canvas);
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;

        if (stub != null)
            stub.setAlpha(alpha);

        super.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf)  {
        if (stub != null)
            stub.setColorFilter(cf);

        super.setColorFilter(cf);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (stub != null)
            stub.setBounds(bounds);

        super.onBoundsChange(bounds);
    }

}
