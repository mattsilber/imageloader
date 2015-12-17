package com.guardanis.imageloader.transitions.drawables;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public abstract class TransitionDrawable extends BitmapDrawable {

    protected enum TransitionStage {
        AWAITING_START, TRANSITIONING, FINISHED;
    }

    protected int duration;
    protected Drawable stubDrawable;

    protected TransitionStage transitionStage = TransitionStage.AWAITING_START;
    protected long animationStart;

    public TransitionDrawable(Context context, Drawable from, Bitmap to, int duration) {
        super(context.getResources(), to);

        this.stubDrawable = from;
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
                super.draw(canvas);
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
        else super.draw(canvas);
    }

    private void drawStub(Canvas canvas, float normalizedPercentCompleted){
        canvas.save();

        int halfXDistance = (Math.max(getBounds().right, stubDrawable.getBounds().right) - Math.min(getBounds().right, stubDrawable.getBounds().right)) / 2;
        if(getBounds().right < stubDrawable.getBounds().right)
            halfXDistance *= -1;

        int halfYDistance = (Math.max(getBounds().bottom, stubDrawable.getBounds().bottom) - Math.min(getBounds().bottom, stubDrawable.getBounds().bottom)) / 2;
        if(getBounds().bottom < stubDrawable.getBounds().bottom)
            halfYDistance *= -1;

        canvas.translate(halfXDistance, halfYDistance);

        drawBackgroundTransition(canvas, normalizedPercentCompleted);

        canvas.restore();
    }

    protected abstract void drawBackgroundTransition(Canvas canvas, float normalizedPercentCompleted);

    protected abstract void drawForegroundTransition(Canvas canvas, float normalizedPercentCompleted);

    protected void drawSuper(Canvas canvas){
        super.draw(canvas);
    }
}
