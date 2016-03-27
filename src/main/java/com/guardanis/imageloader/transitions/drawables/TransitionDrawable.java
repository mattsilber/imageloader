package com.guardanis.imageloader.transitions.drawables;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.guardanis.imageloader.stubs.AnimatedStubDrawable;
import com.guardanis.imageloader.stubs.StubDrawable;
import com.guardanis.imageloader.transitions.modules.TransitionModule;

import java.util.HashMap;
import java.util.Map;

public class TransitionDrawable extends BitmapDrawable {

    protected enum TransitionStage {
        AWAITING_START, TRANSITIONING, FINISHED;
    }

    protected Drawable oldDrawable;
    protected Drawable targetDrawable;


    protected Map<Class, TransitionModule> modules = new HashMap<Class, TransitionModule>();

    protected TransitionStage transitionStage = TransitionStage.AWAITING_START;
    protected long animationStart;

    public TransitionDrawable(Context context, Drawable from, Drawable to, Bitmap canvas) {
        super(context.getResources(), canvas);

        this.oldDrawable = from;
        this.targetDrawable = to;
    }

    public TransitionDrawable registerModule(TransitionModule module){
        if(module != null)
            modules.put(module.getClass(), module);

        return this;
    }

    public void start(){
        this.transitionStage = TransitionStage.TRANSITIONING;
        this.animationStart = System.currentTimeMillis();

        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if(transitionStage == TransitionStage.TRANSITIONING){
            boolean unfinishedExists = false;

            for(TransitionModule module : modules.values())
                if(System.currentTimeMillis() < animationStart + module.getDuration())
                    unfinishedExists = true;

            if(unfinishedExists){
                updateModulesAndDraw(canvas);

                invalidateSelf();
            }
            else{
                transitionStage = TransitionStage.FINISHED;
                oldDrawable = null;

                handlePostTransitionDrawing(canvas);
            }
        }
        else if(transitionStage == TransitionStage.AWAITING_START){
            if(oldDrawable != null)
                drawOldDrawable(canvas);
        }
        else handlePostTransitionDrawing(canvas);
    }

    protected void updateModulesAndDraw(Canvas canvas){
        canvas.save();

        for(TransitionModule module : modules.values())
            module.onPredrawOld(canvas, oldDrawable, animationStart);

        if (oldDrawable != null)
            drawOldDrawable(canvas);

        canvas.restore();
        safelyRevertOldDrawables();

        canvas.save();

        for(TransitionModule module : modules.values())
            module.onPredrawTarget(this, canvas, targetDrawable, animationStart);

        drawTarget(canvas);

        canvas.restore();
        safelyRevertTargetDrawables();
    }

    protected void drawOldDrawable(Canvas canvas){
        canvas.save();

        int[] translation = calculateStubTranslation(oldDrawable);

        canvas.translate(translation[0], translation[1]);

        oldDrawable.draw(canvas);

        canvas.restore();
    }

    protected void drawTarget(Canvas canvas){
        if(targetDrawable instanceof StubDrawable)
            targetDrawable.draw(canvas);
        else super.draw(canvas);
    }

    protected void handlePostTransitionDrawing(Canvas canvas){
        updateModulesAndDraw(canvas);

        if(targetDrawable instanceof AnimatedStubDrawable)
            invalidateSelf();
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

    protected void safelyRevertOldDrawables(){
        for(TransitionModule module : modules.values()){
            try{
                module.revertPostDrawOld(this, oldDrawable);
            }
            catch(NullPointerException e){ } // Likely old drawable is just null
            catch(Throwable e){ e.printStackTrace(); }
        }
    }

    protected void safelyRevertTargetDrawables(){
        for(TransitionModule module : modules.values()){
            try{
                module.revertPostDrawTarget(this, oldDrawable);
            }
            catch(NullPointerException e){ } // Likely old drawable is just null
            catch(Throwable e){ e.printStackTrace(); }
        }
    }

    public Drawable getTargetDrawable(){
        return targetDrawable;
    }

}
