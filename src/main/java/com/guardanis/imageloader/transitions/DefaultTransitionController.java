package com.guardanis.imageloader.transitions;

import android.graphics.drawable.Drawable;

import com.guardanis.imageloader.ImageRequest;

public class DefaultTransitionController extends TransitionController {

    public DefaultTransitionController(ImageRequest request) {
        super(request);
    }

    @Override
    protected void performTransition(Drawable to) {
        setTargetViewDrawable(to);
    }

}
