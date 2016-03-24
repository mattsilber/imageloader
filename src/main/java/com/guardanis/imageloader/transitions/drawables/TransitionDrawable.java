package com.guardanis.imageloader.transitions.drawables;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.guardanis.imageloader.stubs.AnimatedStubDrawable;
import com.guardanis.imageloader.stubs.StubDrawable;

public abstract class TransitionDrawable extends BitmapDrawable {

    protected enum TransitionStage {
        AWAITING_START, TRANSITIONING, FINISHED;
    }

    protected int duration;
    protected Drawable stubDrawable;
    protected Drawable postTransitionDrawable;

    protected TransitionStage transitionStage = TransitionStage.AWAITING_START;
    protected long animationStart;

    public TransitionDrawable(Context context, Drawable from, Bitmap to, int duration) {
        this(context, from, to, null, duration);
    }

    public TransitionDrawable(Context context, Drawable from, Bitmap to, Drawable postTransitionDrawable, int duration) {
        super(context.getResources(), to);

        this.stubDrawable = from;
        this.postTransitionDrawable = postTransitionDrawable;
        this.duration = duration;
    }

    public void start(){
        this.transitionStage = TransitionStage.TRANSITIONING;
        this.animationStart = System.currentTimeMillis();

        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if(transitionStage == TransitionStage.TRANSITIONING){
            float normalized = (System.currentTimeMillis() - animationStart) / (float)duration;

            if (1f <= normalized) {
                transitionStage = TransitionStage.FINISHED;
                stubDrawable = null;

                handlePostTransitionDrawing(canvas);
            }
            else {
                if (stubDrawable != null)
                    drawStub(canvas, normalized);

                drawForegroundTransition(canvas, normalized);
            }
        }
        else if(transitionStage == TransitionStage.AWAITING_START){
            if(stubDrawable != null)
                drawStub(canvas, 0);
        }
        else handlePostTransitionDrawing(canvas);
    }

    protected void drawStub(Canvas canvas, float normalizedPercentCompleted){
        canvas.save();

        int[] translation = calculateStubTranslation(stubDrawable);

        canvas.translate(translation[0], translation[1]);

        drawBackgroundTransition(canvas, normalizedPercentCompleted);

        canvas.restore();
    }

    protected void handlePostTransitionDrawing(Canvas canvas){
        if(isTargetStubDrawable()){
            postTransitionDrawable.draw(canvas);

            if(postTransitionDrawable instanceof AnimatedStubDrawable)
                invalidateSelf();
        }
        else super.draw(canvas);
    }

    protected int[] calculateStubTranslation(Drawable drawable){
        int halfXDistance = (Math.max(getBounds().right, drawable.getBounds().right) - Math.min(getBounds().right, drawable.getBounds().right)) / 2;
        if(getBounds().right < drawable.getBounds().right)
            halfXDistance *= -1;

        int halfYDistance = (Math.max(getBounds().bottom, drawable.getBounds().bottom) - Math.min(getBounds().bottom, drawable.getBounds().bottom)) / 2;
        if(getBounds().bottom < drawable.getBounds().bottom)
            halfYDistance *= -1;

        return new int[]{ halfXDistance, halfYDistance };
    }

    protected abstract void drawBackgroundTransition(Canvas canvas, float normalizedPercentCompleted);

    protected abstract void drawForegroundTransition(Canvas canvas, float normalizedPercentCompleted);

    protected void drawSuper(Canvas canvas){
        if(isTargetStubDrawable())
            postTransitionDrawable.draw(canvas);
        else super.draw(canvas);
    }

    @Override
    public void setAlpha(int alpha){
        super.setAlpha(alpha);

        if(isTargetStubDrawable())
            postTransitionDrawable.setAlpha(alpha);
    }

    protected boolean isTargetStubDrawable(){
        return postTransitionDrawable != null
                && (postTransitionDrawable instanceof AnimatedStubDrawable
                    || postTransitionDrawable instanceof StubDrawable);
    }
}
