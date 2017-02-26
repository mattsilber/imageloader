package com.guardanis.imageloader.stubs.loaders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.guardanis.imageloader.R;

public class EmptyStubBuilder implements StubBuilder {

    @Override
    public Drawable build(Context context) {
        return ContextCompat.getDrawable(context,
                R.drawable.ail__default_image_placeholder);
    }

}
