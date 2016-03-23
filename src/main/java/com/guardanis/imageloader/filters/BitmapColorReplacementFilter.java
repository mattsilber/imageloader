package com.guardanis.imageloader.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class BitmapColorReplacementFilter extends ImageFilter<Bitmap> {

    private Map<Integer, Integer> replacements;

    /**
     * @param context
     * @param replace The singular color value to be replaced
     * @param with The value to replace with
     */
    public BitmapColorReplacementFilter(Context context, int replace, int with) {
        super(context);
        this.replacements = new HashMap<Integer, Integer>();
        this.replacements.put(replace, with);
    }

    /**
     * @param context
     * @param replacements A Map of integers where the key is the value to be replaced and the value is what is should be replaced with
     */
    public BitmapColorReplacementFilter(Context context, Map<Integer, Integer> replacements) {
        super(context);
        this.replacements = replacements;
    }

    @Override
    public Bitmap filter(Bitmap unedited) {
        if(unedited != null){
            Bitmap copy = unedited.copy(unedited.getConfig(), true);

            copy.setPixels(getReplacementPixels(copy), 0, copy.getWidth(),
                    0, 0, copy.getWidth(), copy.getHeight());

            return copy;
        }

        return unedited;
    }

    private int[] getReplacementPixels(Bitmap copy){
        int[] pixels = new int [copy.getHeight() * copy.getWidth()];
        Log.d("imageloader", "searching replacements");
        copy.getPixels(pixels, 0, copy.getWidth(),
                0, 0, copy.getWidth(), copy.getHeight());

        for(int i = 0; i < pixels.length; i++)
            for(Integer key : replacements.keySet())
                if(pixels[i] == key){
                    pixels[i] = replacements.get(key);
                    Log.d("imageloader", "Found " + key + " replacing with " + replacements.get(key));
                }

        return pixels;
    }

    @Override
    public String getAdjustmentInfo(){
        String values = "";

        for(Integer key : replacements.keySet())
            values += key + "-" + replacements.get(key);

        return getClass().getSimpleName() + "_" + values;
    }
}
