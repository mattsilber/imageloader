package com.guardanis.imageloader.transitions.modules;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import android.view.animation.Interpolator;

import com.guardanis.imageloader.transitions.drawables.TransitionDrawable;

import java.util.HashMap;
import java.util.Map;

public abstract class TransitionModule {

    public static final int INTERPOLATOR_IN = 0;
    public static final int INTERPOLATOR_OUT = 1;

    protected long duration;

    protected Map<Integer, Interpolator> interpolators = new HashMap<Integer, Interpolator>();

    public TransitionModule(long duration){
        this.duration = duration;
    }

    public TransitionModule registerInterpolator(int id, Interpolator interpolator){
        interpolators.put(id, interpolator);

        return this;
    }

    public Interpolator getInterpolator(int id){
        return interpolators.get(id);
    }

    public abstract void onStart(@Nullable Drawable old, Drawable target);

    public abstract void onPredrawOld(TransitionDrawable transitionDrawable, Canvas canvas, @Nullable Drawable old, long startTime);

    public abstract void revertPostDrawOld(TransitionDrawable transitionDrawable, @Nullable Drawable old);

    public abstract void onPredrawTarget(TransitionDrawable transitionDrawable, Canvas canvas, Drawable target, long startTime);

    public abstract void revertPostDrawTarget(TransitionDrawable transitionDrawable, Drawable target);

    protected float calculatePercentCompleted(long startTimeMs){
        return Math.min(1f, (System.currentTimeMillis() - startTimeMs) / (float) duration);
    }

    protected float interpolate(int id, long startTimeMs) {
        return interpolate(id, calculatePercentCompleted(startTimeMs));
    }

    protected float interpolate(int id, float percentCompleted){
        return interpolators.get(id) == null
                ? 1f
                : interpolators.get(id).getInterpolation(percentCompleted);
    }

    public long getDuration(){
        return duration;
    }

}
