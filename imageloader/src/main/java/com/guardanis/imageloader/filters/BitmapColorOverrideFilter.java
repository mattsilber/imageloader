package com.guardanis.imageloader.filters;

import android.content.Context;
import android.graphics.Bitmap;

public class BitmapColorOverrideFilter extends ImageFilter<Bitmap> {

    private static final int BASE_MASK = 0x00FFFFFF;
    private static final int ALPHA_POS = 24;

    private int replacementColor;

    /**
     * Replace all color values with the supplied color value while maintaining proper opacity at each pixel.
     * Intended for single color-scaled images.
     * @param replacementColor the 24-bit color value to replace with (0xAARRGGBB). Note: alpha will be ignored
     */
    public BitmapColorOverrideFilter(Context context, int replacementColor) {
        super(context);
        this.replacementColor = replacementColor;
    }

    @Override
    public Bitmap filter(Bitmap unedited) {
        if(unedited != null){
            if(!unedited.isMutable())
                unedited = mutate(unedited);

            unedited.setPixels(getReplacementPixels(unedited), 0, unedited.getWidth(),
                    0, 0, unedited.getWidth(), unedited.getHeight());
        }

        return unedited;
    }

    private int[] getReplacementPixels(Bitmap copy){
        int[] pixels = new int [copy.getWidth() * copy.getHeight()];

        copy.getPixels(pixels, 0, copy.getWidth(),
                0, 0, copy.getWidth(), copy.getHeight());

        for(int i = 0; i < pixels.length; i++)
            pixels[i] = ((pixels[i] >> ALPHA_POS) << ALPHA_POS) | (replacementColor & BASE_MASK);

        return pixels;
    }

    @Override
    public String getAdjustmentInfo(){
        return getClass().getSimpleName() + "_" + replacementColor;
    }
}
