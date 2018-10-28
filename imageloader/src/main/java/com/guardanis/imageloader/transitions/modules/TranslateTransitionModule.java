package com.guardanis.imageloader.transitions.modules;


import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import android.view.animation.DecelerateInterpolator;

import com.guardanis.imageloader.stubs.StubDrawable;
import com.guardanis.imageloader.transitions.drawables.TransitionDrawable;

public class TranslateTransitionModule extends TransitionModule {

    protected float[] translateFrom;
    protected float[] translateTo;
    protected float[] difference;

    protected float[] currentTranslation = new float[2];

    /**
     * Translate the image
     * @param translateFrom [x, y] modifiers for starting position where { position = from[x, y] * [width, height] }, respectively
     * @param translateTo [x, y] modifiers for ending position where { position = to[x, y] * [width, height] }, respectively.
     * @param duration duration, in milliseconds
     */
    public TranslateTransitionModule(float[] translateFrom, float[] translateTo, long duration) {
        super(duration);
        this.translateFrom = translateFrom;
        this.translateTo = translateTo;

        this.difference = new float[]{
                translateTo[0] - translateFrom[0],
                translateTo[1] - translateFrom[1]
        };

        registerInterpolator(TransitionModule.INTERPOLATOR_IN, new DecelerateInterpolator());
    }

    @Override
    public void onStart(@Nullable Drawable old, Drawable target) { }

    @Override
    public void onPredrawOld(TransitionDrawable transitionDrawable, Canvas canvas, @Nullable Drawable old, long startTime) { }

    @Override
    public void revertPostDrawOld(TransitionDrawable transitionDrawable, @Nullable Drawable old) { }

    @Override
    public void onPredrawTarget(TransitionDrawable transitionDrawable, Canvas canvas, Drawable target, long startTime) {
        if(target instanceof StubDrawable)
            return;

        float percentCompleted = interpolate(TransitionModule.INTERPOLATOR_IN, startTime);

        currentTranslation[0] = translateFrom[0] + (percentCompleted * difference[0]);
        currentTranslation[1] = translateFrom[1] + (percentCompleted * difference[1]);

        canvas.translate(canvas.getWidth() * currentTranslation[0],
                canvas.getHeight() * currentTranslation[1]);
    }

    @Override
    public void revertPostDrawTarget(TransitionDrawable transitionDrawable, Drawable target) { }

}
