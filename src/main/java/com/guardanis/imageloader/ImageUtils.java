package com.guardanis.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Environment;
import android.widget.Toast;

import com.caverock.androidsvg.SVG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class ImageUtils {

    public static ImageUtils instance;

    public static ImageUtils getInstance() {
        if(instance == null)
            instance = new ImageUtils();

        return instance;
    }

    private static final int MAX_REDUCTION_FACTOR = 8;

    protected ImageUtils() { }

    public void saveStream(File file, InputStream is) throws Exception {
        OutputStream os = new FileOutputStream(file);
        copyStream(is, os);
        os.close();
    }

    public void saveBitmap(Context context, String filePath, Bitmap bitmap) {
        saveBitmap(context, new File(filePath), bitmap);
    }

    public boolean saveBitmap(Context context, File imageFile, Bitmap bitmap) {
        try{
            if(!checkInternalStorageAvailability(context))
                return false;
            else if(!checkBuildImageFileDirectory(context, imageFile)) return false;

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(imageFile));

            return true;
        }
        catch(Exception e){ e.printStackTrace(); }

        return false;
    }

    private boolean checkInternalStorageAvailability(Context context) {
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY) || Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return true;
        else{
            Toast.makeText(context, "Can't save image while media is mounted!", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean checkBuildImageFileDirectory(Context context, File imageFile) throws Exception {
        if(imageFile.exists() || imageFile.getParentFile().mkdirs() || imageFile.createNewFile())
            return true;
        else{
            Toast.makeText(context, "Couldn't build image directory!", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public void copyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try{
            byte[] bytes = new byte[buffer_size];
            for(; ; ){
                int count = is.read(bytes, 0, buffer_size);

                if(count == -1)
                    break;

                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

    public Bitmap decodeFile(File file, int requiredWidth) {
        if(file.getName().toLowerCase(Locale.US).endsWith(".svg"))
            return decodeSvgFile(file, requiredWidth);
        else return decodeBitmapFile(file, requiredWidth, 1);
    }

    public Bitmap decodeSVGAsset(Context context, String assetFileName) {
        return decodeSVGAsset(context, assetFileName, -1);
    }

    public Bitmap decodeSVGAsset(Context context, String assetFileName, int requiredWidth) {
        try{
            SVG svg = SVG.getFromAsset(context.getAssets(), assetFileName);
            return decodeBitmap(svg, requiredWidth);
        }
        catch(OutOfMemoryError e){ e.printStackTrace(); }
        catch(Exception e){ e.printStackTrace(); }

        return null;
    }

    private Bitmap decodeSvgFile(File file, int requiredWidth) {
        try{
            FileInputStream stream = new FileInputStream(file);
            Bitmap bitmap = decodeBitmap(SVG.getFromInputStream(stream), requiredWidth);

            stream.close();
            return bitmap;
        }
        catch(Throwable e){ e.printStackTrace(); }

        return null;
    }

    public Bitmap decodeBitmap(SVG svg, int requiredWidth) {
        if(svg.getDocumentWidth() != -1){
            if(requiredWidth < 1)
                requiredWidth = (int) svg.getDocumentWidth();

            float scaleFactor = (float) requiredWidth / svg.getDocumentWidth();
            int adjustedHeight = (int) (scaleFactor * svg.getDocumentHeight());

            Bitmap newBitmap = Bitmap.createBitmap(requiredWidth, adjustedHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawPicture(svg.renderToPicture(), new Rect(0, 0, requiredWidth, adjustedHeight));

            return newBitmap;
        }
        return null;
    }

    private Bitmap decodeBitmapFile(File file, int requiredWidth, int reductionFactor) {
        try{
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream = new FileInputStream(file);
            BitmapFactory.decodeStream(stream, null, o);
            stream.close();

            int inSampleSize = calculateInSampleSize(o, requiredWidth, reductionFactor);
            o.inSampleSize = inSampleSize;
            o.inJustDecodeBounds = false;
            stream = new FileInputStream(file);

            Bitmap bitmap = BitmapFactory.decodeStream(stream, null, o);
            stream.close();
            return bitmap;
        }
        catch(OutOfMemoryError e){
            e.printStackTrace();
            System.gc();
            return reductionFactor < MAX_REDUCTION_FACTOR ? decodeBitmapFile(file, requiredWidth, reductionFactor < 1 ? 2 : reductionFactor * 2) : null;
        }
        catch(Exception e){ e.printStackTrace(); }

        return null;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int requiredWidth, int reductionFactor) {
        int inSampleSize = 1;

        if(requiredWidth < 1)
            inSampleSize = options.outWidth / requiredWidth;
        else{
            if(options.outWidth > requiredWidth){
                final int halfWidth = options.outWidth / 2;

                while((halfWidth / inSampleSize) > requiredWidth)
                    inSampleSize *= 2;
            }
        }

        return inSampleSize * reductionFactor;
    }

    public StateListDrawable buildStateListDrawable(Context context, SVG svgAssetNormal, SVG svgAssetPressed) {
        return buildStateListDrawable(buildBitmapDrawable(context, svgAssetNormal), buildBitmapDrawable(context, svgAssetPressed));
    }

    public StateListDrawable buildStateListDrawable(Drawable dNormal, Drawable dPressed) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, dPressed);
        states.addState(new int[]{}, dNormal);
        return states;
    }

    public BitmapDrawable buildBitmapDrawable(Context context, SVG svg) {
        return new BitmapDrawable(context.getResources(), decodeBitmap(svg, (int) svg.getDocumentWidth()));
    }

    public Bitmap drawColorOverlay(Bitmap bitmap, int colorOverlay) {
        if(bitmap != null && colorOverlay > -1){
            Paint paint = new Paint();
            paint.setColor(colorOverlay);

            Canvas canvas = new Canvas(bitmap);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
        }
        return bitmap;
    }

}