package com.guardanis.imageloader.transitions.modules;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

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
    }

    @Override
    public void onStart(@Nullable Drawable old, Drawable target) {

    }

    @Override
    public void onPredrawOld(Canvas canvas, @Nullable Drawable old, long startTime) {

    }

    @Override
    public void revertPostDrawOld(TransitionDrawable transitionDrawable, @Nullable Drawable old) {

    }

    @Override
    public void onPredrawTarget(TransitionDrawable transitionDrawable, Canvas canvas, Drawable target, long startTime) {
        if(target instanceof StubDrawable)
            return;

        float percentCompleted = calculatePercentCompleted(startTime);

        canvas.rotate(rotateFrom + (percentCompleted * difference),
                target.getIntrinsicWidth() / 2,
                target.getIntrinsicHeight() / 2);
    }

    @Override
    public void revertPostDrawTarget(TransitionDrawable transitionDrawable, Drawable target) {

    }

}
