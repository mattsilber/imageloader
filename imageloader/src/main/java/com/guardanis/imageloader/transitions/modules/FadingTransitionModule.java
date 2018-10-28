package com.guardanis.imageloader.transitions.modules;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.Nullable;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.guardanis.imageloader.stubs.StubDrawable;
import com.guardanis.imageloader.transitions.drawables.TransitionDrawable;

public class FadingTransitionModule extends TransitionModule {

    protected static final int TRANSITION_OUT_SPEED_MULTIPLIER = 2;

    protected int startAlpha;
    protected int endAlpha;

    protected int oldSDrawableStartingAlpha = 0xFF;
    protected int targetSDrawableStartingAlpha = 0xFF;

    public FadingTransitionModule(long duration) {
        this(0x00, 0xFF, duration);
    }

    public FadingTransitionModule(int startAlpha, int endAlpha, long duration) {
        super(duration);

        this.startAlpha = startAlpha;
        this.endAlpha = endAlpha;

        registerInterpolator(TransitionModule.INTERPOLATOR_OUT, new AccelerateInterpolator());
        registerInterpolator(TransitionModule.INTERPOLATOR_IN, new DecelerateInterpolator());
    }

    @Override
    public void onStart(@Nullable Drawable old, Drawable target) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            oldSDrawableStartingAlpha = old.getAlpha();
            targetSDrawableStartingAlpha = target.getAlpha();
        }
    }

    @Override
    public void onPredrawOld(TransitionDrawable transitionDrawable, Canvas canvas, @Nullable Drawable old, long startTime) {
        if(old != null){
            int alpha = (int) Math.max(endAlpha - (oldSDrawableStartingAlpha * interpolate(TransitionModule.INTERPOLATOR_OUT, startTime) * TRANSITION_OUT_SPEED_MULTIPLIER), 0);
            int correctedAlpha = Math.min(alpha, transitionDrawable.getOverriddenMaxAlpha());

            if(old instanceof TransitionDrawable)
                ((TransitionDrawable) old).overrideMaxAlphaOut(correctedAlpha);

            old.setAlpha(correctedAlpha);
        }
    }

    @Override
    public void revertPostDrawOld(TransitionDrawable transitionDrawable, @Nullable Drawable old) {
        if(old != null)
            old.setAlpha(oldSDrawableStartingAlpha);
    }

    @Override
    public void onPredrawTarget(TransitionDrawable transitionDrawable, Canvas canvas, Drawable target, long startTime) {
        int alpha = (int) (startAlpha + ((endAlpha - startAlpha) * interpolate(TransitionModule.INTERPOLATOR_IN, startTime)));

        if(target instanceof StubDrawable)
            target.setAlpha(alpha);
        else
            transitionDrawable.setAlpha(alpha);
    }

    @Override
    public void revertPostDrawTarget(TransitionDrawable transitionDrawable, Drawable target) {
        if(target instanceof StubDrawable)
            target.setAlpha(targetSDrawableStartingAlpha);
        else
            transitionDrawable.setAlpha(targetSDrawableStartingAlpha);
    }

}
