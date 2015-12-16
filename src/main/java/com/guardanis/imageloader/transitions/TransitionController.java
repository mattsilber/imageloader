package com.guardanis.imageloader.transitions;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.guardanis.imageloader.ImageRequest;
import com.guardanis.imageloader.R;

public abstract class TransitionController {

    protected ImageRequest request;

    public TransitionController(ImageRequest request){
        this.request = request;
    }

    public void transitionTo(Drawable to){
        performTransition(to);
    }

    protected abstract void performTransition(Drawable to);

    protected Drawable getCurrentTargetDrawable(){
        Drawable current = request.isRequestForBackgroundImage()
                ? request.getTargetView().getBackground()
                : ((ImageView)request.getTargetView()).getDrawable();

        if(current == null)
            return ContextCompat.getDrawable(request.getContext(), R.drawable.ail__default_fade_placeholder);
        else return current instanceof TransitionDrawable
                ? ((TransitionDrawable)current).getDrawable(1)
                : current;
    }

    protected void setTargetViewDrawable(final Drawable drawable){
        request.getTargetView().post(new Runnable(){
            public void run(){
                if(request.isRequestForBackgroundImage())
                    setBackgroundDrawable(drawable);
                else ((ImageView) request.getTargetView()).setImageDrawable(drawable);
            }
        });
    }

    @SuppressLint("NewApi")
    protected void setBackgroundDrawable(Drawable drawable) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
            request.getTargetView().setBackgroundDrawable(drawable);
        else request.getTargetView().setBackground(drawable);
    }
}
