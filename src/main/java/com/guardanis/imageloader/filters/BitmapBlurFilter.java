package com.guardanis.imageloader.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

public class BitmapBlurFilter extends ImageFilter<Bitmap> {

    private float blurRadius;
    private static RenderScript renderScript;

    public BitmapBlurFilter(Context context, int blurRadius) {
        super(context);
        this.blurRadius = blurRadius;
    }

    public Bitmap filter(Bitmap unedited) {
        try{
            if(renderScript == null)
                renderScript = RenderScript.create(context);

            Bitmap blurred = unedited.copy(unedited.getConfig(), true);
            final Allocation input = Allocation.createFromBitmap(renderScript, blurred, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
            final Allocation output = Allocation.createTyped(renderScript, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));

            script.setRadius(blurRadius);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(blurred);

            return blurred;
        }
        catch(OutOfMemoryError e){ e.printStackTrace(); }
        catch(Exception e){ e.printStackTrace(); }

        return unedited;
    }

    @Override
    public String getAdjustmentInfo(){
        return getClass().getSimpleName() + "_" + blurRadius;
    }

}
