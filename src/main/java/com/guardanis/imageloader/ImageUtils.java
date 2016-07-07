package com.guardanis.imageloader;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.caverock.androidsvg.SVG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Locale;

public class ImageUtils {

    public enum ImageType {
        BITMAP, SVG, GIF;
    }

    private static final int MAX_REDUCTION_FACTOR = 16;

    private static final String TAG = "AIL";

    protected ImageUtils() { }

    public static void saveStream(File file, InputStream is) throws Exception {
        OutputStream os = new FileOutputStream(file);
        copyStream(is, os);
        os.close();
    }

    @Deprecated
    public static void saveBitmap(Context context, String filePath, Bitmap bitmap) {
        saveBitmapAsync(context, new File(filePath), bitmap);
    }

    @Deprecated
    public static boolean saveBitmap(Context context, File imageFile, Bitmap bitmap) {
        saveBitmapAsync(context, imageFile, bitmap);

        return true;
    }

    public static void saveBitmapAsync(final Context context, final File imageFile, final Bitmap bitmap) {
        try{
            if(bitmap == null || !(checkInternalStorageAvailability(context) || checkBuildImageFileDirectory(context, imageFile)))
                return;

            new Thread(new Runnable(){
                public void run(){
                    try{
                        File temp = new FileCache(context)
                                .getFile(System.currentTimeMillis() + "_temp_" + imageFile.getAbsolutePath());

                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(temp));

                        temp.renameTo(imageFile);
                    }
                    catch(Exception e){ e.printStackTrace(); }
                }
            }).start();
        }
        catch(Exception e){ e.printStackTrace(); }
    }

    private static boolean checkInternalStorageAvailability(Context context) {
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY) || Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return true;
        else{
            Toast.makeText(context, "Can't save image while media is mounted!", Toast.LENGTH_LONG)
                    .show();

            return false;
        }
    }

    private static boolean checkBuildImageFileDirectory(Context context, File imageFile) throws Exception {
        if(imageFile.exists() || imageFile.getParentFile().mkdirs() || imageFile.createNewFile())
            return true;
        else{
            Toast.makeText(context, "Couldn't build image directory!", Toast.LENGTH_LONG)
                    .show();

            return false;
        }
    }

    public static void copyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;

        try{
            byte[] bytes = new byte[buffer_size];
            for(; ;){
                int count = is.read(bytes, 0, buffer_size);

                if(count == -1)
                    break;

                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){ }
    }

    public static Bitmap decodeFile(File file, int requiredWidth) {
        if(file.getName().toLowerCase(Locale.US).endsWith(".svg"))
            return decodeSvgFile(file, requiredWidth);
        else return decodeBitmapFile(file, requiredWidth, 1);
    }

    public static Bitmap decodeSVGAsset(Context context, String assetFileName) {
        return decodeSVGAsset(context, assetFileName, -1);
    }

    public static Bitmap decodeSVGAsset(Context context, String assetFileName, int requiredWidth) {
        try{
            SVG svg = SVG.getFromAsset(context.getAssets(), assetFileName);
            return decodeBitmap(svg, requiredWidth);
        }
        catch(OutOfMemoryError e){ e.printStackTrace(); }
        catch(Exception e){ e.printStackTrace(); }

        return null;
    }

    private static Bitmap decodeSvgFile(File file, int requiredWidth) {
        try{
            return decodeSvg(new FileInputStream(file),
                    requiredWidth);
        }
        catch(Throwable e){ e.printStackTrace(); }

        return null;
    }

    public static Bitmap decodeSvgResource(Context context, int resourceId, int requiredWidth) {
        try{
            return decodeSvg(context.getResources().openRawResource(resourceId),
                    requiredWidth);
        }
        catch(Throwable e){ e.printStackTrace(); }

        return null;
    }

    private static Bitmap decodeSvg(InputStream stream, int requiredWidth) {
        try{
            Bitmap bitmap = decodeBitmap(SVG.getFromInputStream(stream),
                    requiredWidth);

            stream.close();
            return bitmap;
        }
        catch(Throwable e){ e.printStackTrace(); }

        return null;
    }

    public static Bitmap decodeBitmap(SVG svg, int requiredWidth) {
        float documentWidth = svg.getDocumentWidth();
        float documentHeight = svg.getDocumentHeight();

        if((documentWidth == -1 || documentHeight == -1) && (svg.getDocumentViewBox() != null && 0 < svg.getDocumentViewBox().width())){
            documentWidth = svg.getDocumentViewBox().width();
            documentHeight = svg.getDocumentViewBox().height();
        }

        if(documentWidth != -1){
            if(requiredWidth < 1)
                requiredWidth = (int) documentWidth;

            float scaleFactor = (float) requiredWidth / documentWidth;
            int adjustedHeight = (int) (scaleFactor * documentHeight);

            Bitmap newBitmap = Bitmap.createBitmap(requiredWidth,
                    adjustedHeight,
                    Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(newBitmap);

            canvas.drawPicture(svg.renderToPicture(),
                    new Rect(0, 0, requiredWidth, adjustedHeight));

            return newBitmap;
        }

        return null;
    }

    private static Bitmap decodeBitmapFile(File file, int requiredWidth, int reductionFactor) {
        try{
            BitmapFactory.Options options = decodeOptions(new FileInputStream(file));

            return decodeBitmap(new FileInputStream(file),
                    options,
                    requiredWidth,
                    reductionFactor);
        }
        catch(OutOfMemoryError e){
            e.printStackTrace();

            System.gc();

            return reductionFactor < MAX_REDUCTION_FACTOR
                    ? decodeBitmapFile(file, requiredWidth, reductionFactor < 1 ? 2 : reductionFactor * 2)
                    : null;
        }
        catch(Exception e){ e.printStackTrace(); }

        return null;
    }

    public static Bitmap decodeBitmapAsset(Context context, String asset, int requiredWidth) {
        return decodeBitmapAsset(context, asset, requiredWidth, 1);
    }

    private static Bitmap decodeBitmapAsset(Context context, String asset, int requiredWidth, int reductionFactor) {
        try{
            BitmapFactory.Options options = decodeOptions(context.getAssets().open(asset));

            return decodeBitmap(context.getAssets().open(asset),
                    options,
                    requiredWidth,
                    reductionFactor);
        }
        catch(OutOfMemoryError e){
            e.printStackTrace();

            System.gc();

            return reductionFactor < MAX_REDUCTION_FACTOR
                    ? decodeBitmapAsset(context, asset, requiredWidth, reductionFactor < 1 ? 2 : reductionFactor * 2)
                    : null;
        }
        catch(Exception e){ e.printStackTrace(); }

        return null;
    }

    private static BitmapFactory.Options decodeOptions(InputStream stream) throws Exception {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(stream, null, options);

        stream.close();

        return options;
    }

    private static Bitmap decodeBitmap(InputStream stream, BitmapFactory.Options options, int requiredWidth, int reductionFactor) throws Exception {
        int inSampleSize = calculateInSampleSize(options, requiredWidth, reductionFactor);

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);

        stream.close();

        return bitmap;
    }

    public static Bitmap decodeBitmapResource(Resources resources, int resId, int requiredWidth){
        return decodeBitmapResource(resources, resId, requiredWidth, 1);
    }

    public static Bitmap decodeBitmapResource(Resources resources, int resId, int requiredWidth, int reductionFactor){
        try{
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(resources, resId, options);

            options.inSampleSize = calculateInSampleSize(options,
                    requiredWidth,
                    reductionFactor);

            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeResource(resources, resId, options);
        }
        catch(OutOfMemoryError e){
            e.printStackTrace();

            System.gc();

            return reductionFactor < MAX_REDUCTION_FACTOR
                    ? decodeBitmapResource(resources, resId, requiredWidth, reductionFactor < 1 ? 2 : reductionFactor * 2)
                    : null;
        }
        catch(Exception e){ e.printStackTrace(); }

        return null;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int requiredWidth, int reductionFactor) {
        int inSampleSize = 1;

        if(1 < requiredWidth && requiredWidth < options.outWidth)
            while((options.outWidth / inSampleSize) > requiredWidth)
                inSampleSize *= 2;

        return inSampleSize * reductionFactor;
    }

    public static StateListDrawable buildStateListDrawable(Context context, SVG svgAssetNormal, SVG svgAssetPressed) {
        return buildStateListDrawable(buildBitmapDrawable(context, svgAssetNormal),
                buildBitmapDrawable(context, svgAssetPressed));
    }

    public static StateListDrawable buildStateListDrawable(Drawable dNormal, Drawable dPressed) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{ android.R.attr.state_pressed }, dPressed);
        states.addState(new int[]{ }, dNormal);

        return states;
    }

    public static BitmapDrawable buildBitmapDrawable(Context context, SVG svg) {
        return new BitmapDrawable(context.getResources(),
                decodeBitmap(svg, (int) svg.getDocumentWidth()));
    }

    public static void log(Context context, String message){
        if(context.getResources().getBoolean(R.bool.ail__debug_log_enabled))
            Log.d(TAG, message);
    }

    public static void log(Context context, Throwable e){
        if(context.getResources().getBoolean(R.bool.ail__debug_log_enabled))
            e.printStackTrace();
    }

    public static ImageType getImageType(Context context, String url) {
        url = url.toLowerCase(Locale.US);

        if(url.endsWith(".svg"))
            return ImageType.SVG;
        else if(url.endsWith(".gif"))
            return ImageType.GIF;

        try{
            Uri uri = Uri.parse(URLEncoder.encode(url, "UTF-8"));

            if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT) || uri.getScheme().equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                String mimeType = context.getContentResolver()
                        .getType(uri);

                return ImageType.valueOf(mimeType.toUpperCase(Locale.US));
            }
        }
        catch(Exception e){ }

        log(context, String.format("Mime type not found for: %1$s, Treating as a Bitmap", url));

        return ImageType.BITMAP;
    }

}