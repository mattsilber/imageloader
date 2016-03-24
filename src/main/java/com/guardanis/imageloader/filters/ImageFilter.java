package com.guardanis.imageloader.filters;

import android.content.Context;
import android.graphics.Bitmap;

public abstract class ImageFilter<T> {

    protected Context context;
    
    public ImageFilter(Context context){
        this.context = context;
    }

    public abstract T filter(T unedited);

    public String getAdjustmentInfo(){
        return getClass().getSimpleName();
    }

    protected Bitmap mutate(Bitmap bitmap){
        return bitmap.copy(bitmap.getConfig(), true);
    }

}
