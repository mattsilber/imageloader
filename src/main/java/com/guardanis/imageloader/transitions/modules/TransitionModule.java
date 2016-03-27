package com.guardanis.imageloader.transitions.modules;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.guardanis.imageloader.transitions.drawables.TransitionDrawable;

public abstract class TransitionModule {

    protected long duration;

    public TransitionModule(long duration){
        this.duration = duration;
    }

    public abstract void onStart(@Nullable Drawable old, Drawable target);

    public abstract void onPredrawOld(Canvas canvas, @Nullable Drawable old, long startTime);

    public abstract void revertPostDrawOld(TransitionDrawable transitionDrawable, @Nullable Drawable old);

    public abstract void onPredrawTarget(TransitionDrawable transitionDrawable, Canvas canvas, Drawable target, long startTime);

    public abstract void revertPostDrawTarget(TransitionDrawable transitionDrawable, Drawable target);

    protected float calculatePercentCompleted(long startTimeMs){
        return Math.min(1f, (System.currentTimeMillis() - startTimeMs) / (float) duration);
    }

    public long getDuration(){
        return duration;
    }

}
