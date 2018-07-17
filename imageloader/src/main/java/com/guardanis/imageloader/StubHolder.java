package com.guardanis.imageloader;

import android.content.Context;
import android.graphics.drawable.Drawable;

@Deprecated
public interface StubHolder {

    public Drawable getLoadingDrawable(Context context);

    public Drawable getErrorDrawable(Context context);

}
