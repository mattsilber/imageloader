package com.guardanis.imageloader.transitions.modules;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.guardanis.imageloader.stubs.StubDrawable;
import com.guardanis.imageloader.transitions.drawables.TransitionDrawable;

public class ScalingTransitionModule extends TransitionModule {

    protected float scaleFrom;
    protected float scaleTo;
    protected float difference;

    public ScalingTransitionModule(float from, float to, long duration) {
        super(duration);
        this.scaleFrom = from;
        this.scaleTo = to;
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

        float scale = scaleFrom + (percentCompleted * difference);

        canvas.translate((transitionDrawable.getBitmap().getWidth() - (scale * transitionDrawable.getBitmap().getWidth())) / 2,
                (transitionDrawable.getBitmap().getHeight() - (scale * transitionDrawable.getBitmap().getHeight())) / 2);

        canvas.scale(scale, scale);
    }

    @Override
    public void revertPostDrawTarget(TransitionDrawable transitionDrawable, Drawable target) {

    }

}
