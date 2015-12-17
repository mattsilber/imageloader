package com.guardanis.imageloader.transitions;

import android.graphics.drawable.Drawable;

import com.guardanis.imageloader.ImageRequest;
import com.guardanis.imageloader.transitions.drawables.FadingTransitionDrawable;

public class FadeTransitionController extends TransitionController {

    private int fadeDuration;

    public FadeTransitionController(ImageRequest request, int fadeDuration) {
        super(request);
        this.fadeDuration = fadeDuration;
    }

    @Override
    protected void performTransition(final Drawable to) {
        request.getTargetView().post(new Runnable() {
            public void run() {
                final FadingTransitionDrawable transition = new FadingTransitionDrawable(request.getContext(),
                        getCurrentTargetDrawableMutable(),
                        getTargetBitmap(to),
                        fadeDuration);

                setTransitionDrawable(transition);
            }
        });
    }

}
