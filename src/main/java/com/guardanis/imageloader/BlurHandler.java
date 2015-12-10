package com.guardanis.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

public class BlurHandler {

    private static RenderScript renderScript;

    /**
     * @return blurred Bitmap; can be null
     */
    public static Bitmap blurImageWithRenderScript(Context context, Bitmap bitmap, float radius) {
        try{
            if(renderScript == null)
                renderScript = RenderScript.create(context);

            Bitmap blurred = bitmap.copy(bitmap.getConfig(), true);
            final Allocation input = Allocation.createFromBitmap(renderScript, blurred, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
            final Allocation output = Allocation.createTyped(renderScript, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));

            script.setRadius(radius);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(blurred);

            return blurred;
        }
        catch(OutOfMemoryError e){ e.printStackTrace(); }
        catch(Exception e){ e.printStackTrace(); }

        return null;
    }
}
