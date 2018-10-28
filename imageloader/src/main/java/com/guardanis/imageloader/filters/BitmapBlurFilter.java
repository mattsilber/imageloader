package com.guardanis.imageloader.filters;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicBlur;

import com.guardanis.imageloader.ImageUtils;

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

            if(!unedited.isMutable())
                unedited = mutate(unedited);

            final Allocation input = Allocation.createFromBitmap(renderScript,
                    unedited,
                    Allocation.MipmapControl.MIPMAP_NONE,
                    Allocation.USAGE_SCRIPT);

            final Allocation output = Allocation.createTyped(renderScript,
                    input.getType());

            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(renderScript,
                    Element.U8_4(renderScript));

            script.setRadius(blurRadius);
            script.setInput(input);
            script.forEach(output);

            output.copyTo(unedited);
        }
        catch(OutOfMemoryError e){ ImageUtils.log(context, e); }
        catch(Exception e){ ImageUtils.log(context, e); }

        return unedited;
    }

    @Override
    public String getAdjustmentInfo(){
        return getClass().getSimpleName() + "_" + blurRadius;
    }

}
