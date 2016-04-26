package com.guardanis.imageloader.transitions.modules;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.guardanis.imageloader.stubs.StubDrawable;
import com.guardanis.imageloader.transitions.drawables.TransitionDrawable;

public class FadingTransitionModule extends TransitionModule {

    protected final int MAX_ALPHA = 0xFF;
    protected final int TRANSITION_OUT_SPEED_MULTIPLIER = 2;

    protected int oldSDrawableStartingAlpha = 0xFF;

    public FadingTransitionModule(long duration) {
        super(duration);

        registerInterpolator(TransitionModule.INTERPOLATOR_OUT, new DecelerateInterpolator());
        registerInterpolator(TransitionModule.INTERPOLATOR_IN, new AccelerateInterpolator());
    }

    @Override
    public void onStart(@Nullable Drawable old, Drawable target) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            oldSDrawableStartingAlpha = old.getAlpha();
    }

    @Override
    public void onPredrawOld(TransitionDrawable transitionDrawable, Canvas canvas, @Nullable Drawable old, long startTime) {
        if(old != null){
            int alpha = (int) Math.max(MAX_ALPHA - (oldSDrawableStartingAlpha * interpolate(TransitionModule.INTERPOLATOR_OUT, startTime) * TRANSITION_OUT_SPEED_MULTIPLIER), 0);
            int correctedAlpha = Math.min(alpha, transitionDrawable.getOverriddenMaxAlpha());

            if(old instanceof TransitionDrawable)
                ((TransitionDrawable) old).overrideMaxAlphaOut(correctedAlpha);

            old.setAlpha(correctedAlpha);
        }
    }

    @Override
    public void revertPostDrawOld(TransitionDrawable transitionDrawable, @Nullable Drawable old) {
        if(old != null)
            old.setAlpha(MAX_ALPHA);
    }

    @Override
    public void onPredrawTarget(TransitionDrawable transitionDrawable, Canvas canvas, Drawable target, long startTime) {
        if(target instanceof StubDrawable)
            target.setAlpha((int) (MAX_ALPHA * interpolate(TransitionModule.INTERPOLATOR_IN, startTime)));
        else transitionDrawable.setAlpha((int) (MAX_ALPHA * interpolate(TransitionModule.INTERPOLATOR_IN, startTime)));
    }

    @Override
    public void revertPostDrawTarget(TransitionDrawable transitionDrawable, Drawable target) {
        if(target instanceof StubDrawable)
            target.setAlpha(MAX_ALPHA);
        else transitionDrawable.setAlpha(MAX_ALPHA);
    }

}
