package com.guardanis.imageloader.stubs.builders;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.guardanis.imageloader.stubs.DefaultLoadingDrawable;

public class DefaultLoadingStubBuilder implements StubBuilder {

    @Override
    public Drawable build(Context context) {
        return new DefaultLoadingDrawable(context.getResources());
    }

}
