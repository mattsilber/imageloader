package com.guardanis.imageloader.transitions;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.guardanis.imageloader.ImageRequest;

public class FadeTransitionController extends TransitionController {

    private int crossFadeDuration;

    public FadeTransitionController(ImageRequest request, int crossFadeDuration) {
        super(request);
        this.crossFadeDuration = crossFadeDuration;
    }

    @Override
    protected void performTransition(final Drawable to) {
        request.getTargetView().post(new Runnable(){
            public void run(){
                FadingTransitionDrawable transition = new FadingTransitionDrawable(request.getContext(), getCurrentTargetDrawable(), getTargetBitmap(to), crossFadeDuration);
                setTransitionDrawable(transition);
            }
        });
    }

    private void setTransitionDrawable(Drawable drawable){
        if(request.isRequestForBackgroundImage())
            setBackgroundDrawable(drawable);
        else ((ImageView) request.getTargetView()).setImageDrawable(drawable);
    }

    private Bitmap getTargetBitmap(Drawable drawable){
        if(drawable instanceof BitmapDrawable)
            return ((BitmapDrawable)drawable).getBitmap();
        else {
            Bitmap bitmap = null;
            if(drawable.getIntrinsicWidth() < 1 || drawable.getIntrinsicHeight() < 1)
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            else Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

}
