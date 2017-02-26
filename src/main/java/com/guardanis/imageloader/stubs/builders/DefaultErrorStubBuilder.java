package com.guardanis.imageloader.stubs.builders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.guardanis.imageloader.R;

public class DefaultErrorStubBuilder implements StubBuilder {

    @Override
    public Drawable build(Context context) {
        return ContextCompat.getDrawable(context,
                R.drawable.ail__image_loader_stub_error);
    }

}
