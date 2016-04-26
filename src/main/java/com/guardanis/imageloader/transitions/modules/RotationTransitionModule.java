package com.guardanis.imageloader.transitions.modules;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.guardanis.imageloader.stubs.StubDrawable;
import com.guardanis.imageloader.transitions.drawables.TransitionDrawable;

public class RotationTransitionModule extends TransitionModule {

    protected float rotateFrom;
    protected float rotateTo;
    protected float difference;

    public RotationTransitionModule(int from, int to, long duration) {
        super(duration);
        this.rotateFrom = from;
        this.rotateTo = to;
        this.difference = to - from;

        registerInterpolator(TransitionModule.INTERPOLATOR_IN, new DecelerateInterpolator());
    }

    @Override
    public void onStart(@Nullable Drawable old, Drawable target) {

    }

    @Override
    public void onPredrawOld(TransitionDrawable transitionDrawable, Canvas canvas, @Nullable Drawable old, long startTime) {

    }

    @Override
    public void revertPostDrawOld(TransitionDrawable transitionDrawable, @Nullable Drawable old) {

    }

    @Override
    public void onPredrawTarget(TransitionDrawable transitionDrawable, Canvas canvas, Drawable target, long startTime) {
        if(target instanceof StubDrawable)
            return;

        float percentCompleted = interpolate(TransitionModule.INTERPOLATOR_IN, startTime);

        canvas.rotate(rotateFrom + (percentCompleted * difference),
                transitionDrawable.getBitmap().getWidth() / 2,
                transitionDrawable.getBitmap().getHeight() / 2);
    }

    @Override
    public void revertPostDrawTarget(TransitionDrawable transitionDrawable, Drawable target) {

    }

}
