package com.guardanis.imageloader.transitions;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import com.guardanis.imageloader.ImageRequest;
import com.guardanis.imageloader.R;
import com.guardanis.imageloader.transitions.drawables.TransitionDrawable;

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

        return current == null
                ? ContextCompat.getDrawable(request.getContext(), R.drawable.ail__default_fade_placeholder)
                : current;
    }

    protected Drawable getCurrentTargetDrawableMutable(){
        return getCurrentTargetDrawable()
                .getConstantState()
                .newDrawable()
                .mutate();
    }

    protected Bitmap getTargetBitmap(Drawable drawable){
        return getTargetBitmap(drawable, null);
    }

    protected Bitmap getTargetBitmap(Drawable drawable, @Nullable View targetView){
        if(drawable instanceof BitmapDrawable)
            return ((BitmapDrawable) drawable).getBitmap();
        else {
            Bitmap bitmap = null;
            if(drawable.getIntrinsicWidth() < 1 || drawable.getIntrinsicHeight() < 1){
                if(!(targetView == null || targetView.getLayoutParams() == null || targetView.getLayoutParams().width < 1))
                    bitmap = Bitmap.createBitmap(targetView.getLayoutParams().width, targetView.getLayoutParams().height, Bitmap.Config.ARGB_8888);
                else if(drawable.getBounds().right - drawable.getBounds().left < 1)
                    bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                else bitmap = Bitmap.createBitmap(drawable.getBounds().right - drawable.getBounds().left,
                        drawable.getBounds().bottom - drawable.getBounds().top,
                        Bitmap.Config.ARGB_8888);
            }
            else Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        }
    }

    protected void setTransitionDrawable(final TransitionDrawable drawable){
        if (request.isRequestForBackgroundImage())
            setBackgroundDrawable(drawable);
        else ((ImageView) request.getTargetView()).setImageDrawable(drawable);

        drawable.start();
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
