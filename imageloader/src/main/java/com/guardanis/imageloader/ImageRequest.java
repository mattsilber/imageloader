package com.guardanis.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.guardanis.imageloader.ImageUtils.ImageType;
import com.guardanis.imageloader.filters.BitmapBlurFilter;
import com.guardanis.imageloader.filters.BitmapCenterCropFilter;
import com.guardanis.imageloader.filters.BitmapCircularCropFilter;
import com.guardanis.imageloader.filters.BitmapColorFilter;
import com.guardanis.imageloader.filters.BitmapColorOverlayFilter;
import com.guardanis.imageloader.filters.BitmapColorOverrideFilter;
import com.guardanis.imageloader.filters.BitmapRotationFilter;
import com.guardanis.imageloader.filters.ImageFilter;
import com.guardanis.imageloader.processors.ExternalImageProcessor;
import com.guardanis.imageloader.processors.ImageAssetProcessor;
import com.guardanis.imageloader.processors.ImageDrawableResourceProcessor;
import com.guardanis.imageloader.processors.ImageFileProcessor;
import com.guardanis.imageloader.processors.ImageProcessor;
import com.guardanis.imageloader.processors.ImageResourceProcessor;
import com.guardanis.imageloader.stubs.builders.StubBuilder;
import com.guardanis.imageloader.transitions.TransitionController;
import com.guardanis.imageloader.transitions.modules.FadingTransitionModule;
import com.guardanis.imageloader.transitions.modules.RotationTransitionModule;
import com.guardanis.imageloader.transitions.modules.ScalingTransitionModule;
import com.guardanis.imageloader.transitions.modules.TransitionModule;
import com.guardanis.imageloader.transitions.modules.TranslateTransitionModule;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageRequest<V extends View> implements Runnable {

    public interface RequestStartedCallback {
        public void onRequestStarted(ImageRequest request);
    }

    public interface ImageSuccessCallback {
        public void onImageReady(ImageRequest request, Drawable result);
    }

    public interface ImageErrorCallback {
        public void onImageLoadingFailure(ImageRequest request, @Nullable Throwable e);
    }

    protected static final int DEFAULT_BLUR_RADIUS = 15;
    protected static final int DEFAULT_CROSS_FADE_DURATION = 300;
    protected static final int DEFAULT_SCALE_DURATION = 300;
    protected static final int DEFAULT_ROTATION_DURATION = 300;

    protected static final int WIDTH_UNKNOWN = -1;

    protected Context context;

    protected String targetUrl;
    protected int targetResourceId = -1;

    protected ImageType targetImageType = ImageType.BITMAP;
    protected ImageProcessor imageProcessor;
    protected boolean loadingTargetLocally = false;

    protected V targetView;
    protected boolean setImageAsBackground = false;
    protected int requiredImageWidth = WIDTH_UNKNOWN;

    protected long maxCacheDurationMs = -1;
    private boolean lruCacheEnabled = true;

    protected List<ImageFilter<Bitmap>> bitmapImageFilters = new ArrayList<ImageFilter<Bitmap>>();

    protected boolean showStubOnExecute = true;
    protected boolean showStubOnError = false;
    protected boolean disableExecutionStubIfDownloaded = true;
    protected StubBuilder loadingStubBuilder;
    protected StubBuilder errorStubBuilder;

    protected TransitionController transitionController = new TransitionController(this);
    protected boolean exitTransitionsEnabled = true;
    protected boolean transitionOnSuccessEnabled = true;

    protected Map<String, String> httpRequestParams = new HashMap<String, String>();

    protected RequestStartedCallback requestStartedCallback;

    protected ImageSuccessCallback successCallback;
    protected boolean triggerSuccessForValidViewsOnly = true;

    protected ImageErrorCallback errorCallback;

    protected boolean tagRequestPreventionEnabled = false;

    protected long startedAtMs;

    public ImageRequest(Context context) {
        this.context = context;
        this.showStubOnExecute = context.getResources().getBoolean(R.bool.ail__show_stub_on_execute);
        this.showStubOnError = context.getResources().getBoolean(R.bool.ail__show_stub_on_error);
        this.lruCacheEnabled = context.getResources().getBoolean(R.bool.ail__lru_cache_enabled);
        this.tagRequestPreventionEnabled = context.getResources().getBoolean(R.bool.ail__tag_request_prevention_enabled);
    }

    public ImageRequest(Context context, V targetView) {
        this(context);

        this.targetView = targetView;
    }

    public ImageRequest<V> setTargetUrl(final String targetUrl) {
        this.targetUrl = targetUrl;
        this.targetImageType = ImageUtils.getImageType(context, targetUrl);
        this.loadingTargetLocally = false;

        this.imageProcessor = new ExternalImageProcessor();

        return this;
    }

    public ImageRequest<V> setTargetAsset(String targetAssetUrl){
        this.targetUrl = targetAssetUrl;
        this.targetImageType = ImageUtils.getImageType(context, targetUrl);
        this.loadingTargetLocally = true;
        this.showStubOnExecute = context.getResources()
                .getBoolean(R.bool.ail__local_execution_stubs);

        this.imageProcessor = new ImageAssetProcessor();

        return this;
    }

    public ImageRequest<V> setTargetFile(String file){
        return setTargetFile(new File(file));
    }

    public ImageRequest<V> setTargetFile(File file){
        this.targetUrl = file.getAbsolutePath();
        this.targetImageType = ImageUtils.getImageType(context, targetUrl);
        this.loadingTargetLocally = true;
        this.showStubOnExecute = context.getResources()
                .getBoolean(R.bool.ail__local_execution_stubs);

        this.imageProcessor = new ImageFileProcessor();

        return this;
    }

    public ImageRequest<V> setTargetResource(int targetResourceId){
        // MIME types and extensions are unavailable for resource files
        return setTargetResource(targetResourceId, ImageType.BITMAP);
    }

    public ImageRequest<V> setTargetResource(int targetResourceId, ImageType type){
        this.targetResourceId = targetResourceId;
        this.targetImageType = type;
        this.loadingTargetLocally = true;
        this.showStubOnExecute = context.getResources()
                .getBoolean(R.bool.ail__local_execution_stubs);

        this.imageProcessor = new ImageResourceProcessor();

        return this;
    }

    /**
     * @param drawableResourceId A bitmap or Vector Asset from local drawable resources
     */
    public ImageRequest<V> setTargetDrawable(@DrawableRes int drawableResourceId){
        this.targetResourceId = drawableResourceId;
        this.targetImageType = ImageType.CUSTOM;
        this.loadingTargetLocally = true;
        this.showStubOnExecute = context.getResources()
                .getBoolean(R.bool.ail__local_execution_stubs);

        this.imageProcessor = new ImageDrawableResourceProcessor();

        return this;
    }

    /**
     * Override the ImageProcessor that was set by calling the setTargetUrl, setTargetAsset, setTargetFile, or setTargetResource methods.
     * If those methods are called after this, they will override this call. This is really only useful for implementing third-party image libraries.
     */
    public ImageRequest<V> overrideImageProcessor(@NonNull ImageProcessor imageProcessor){
        this.imageProcessor = imageProcessor;
        return this;
    }

    public ImageRequest<V> setTargetView(V targetView) {
        this.targetView = targetView;
        return this;
    }

    /**
     * Add HttpUrlConnection Request Parameter key/value pair. Adding a key/value pair will override previous values for said key.
     */
    public ImageRequest<V> addHttpRequestParam(String key, String value) {
        httpRequestParams.put(key, value);
        return this;
    }

    /**
     * Force request to load image at specific size
     */
    public ImageRequest<V> setRequiredImageWidth(int requiredImageWidth) {
        this.requiredImageWidth = requiredImageWidth;
        return this;
    }

    /**
     * Force request to load image via setBackground
     */
    public ImageRequest<V> setImageAsBackground() {
        this.setImageAsBackground = true;
        return this;
    }

    public ImageRequest<V> setImageAsBackground(boolean setImageAsBackground) {
        this.setImageAsBackground = setImageAsBackground;
        return this;
    }

    public ImageRequest<V> setShowStubOnExecute(boolean showStubOnExecute) {
        this.showStubOnExecute = showStubOnExecute;
        return this;
    }

    public ImageRequest<V> setShowStubOnError(boolean showStubOnError) {
        this.showStubOnError = showStubOnError;
        return this;
    }

    public ImageRequest<V> setDisableExecutionStubIfDownloaded(boolean disableExecutionStubIfDownloaded) {
        this.disableExecutionStubIfDownloaded = disableExecutionStubIfDownloaded;
        return this;
    }

    /**
     * Set the maximum allowed time a cached file from this request is valid for in milliseconds.
     * Files requested beyond this limit will be deleted and re-downloaded.
     * Anything < 0 will never re-download (default behavior).
     */
    public ImageRequest<V> setMaxCacheDurationMs(long maxCacheDurationMs) {
        this.maxCacheDurationMs = maxCacheDurationMs;
        return this;
    }

    /**
     * Enable/disable use of LruCache for caching images in memory
     */
    public ImageRequest<V> setLruCacheEnabled(boolean lruCacheEnabled) {
        this.lruCacheEnabled = lruCacheEnabled;
        return this;
    }

    /**
     * Set a callback to be triggered on the main thread once downloading and/or processing this
     * ImageRequest has begun. This will not be triggered if tag-request-prevention is enabled
     * and the target view has already been claimed for this target.
     */
    public ImageRequest<V> setRequestStartedCallback(RequestStartedCallback requestStartedCallback) {
        this.requestStartedCallback = requestStartedCallback;
        return this;
    }

    /**
     * Set a callback to be triggered on the main thread once the resulting image has been set
     * into the target View. This will not be triggered if the View is null.
     */
    public ImageRequest<V> setSuccessCallback(ImageSuccessCallback successCallback) {
        this.successCallback = successCallback;
        return this;
    }

    /**
     * Set whether or not to trigger the success callback when the View is no longer valid for the request.
     * The default behavior is true, to prevent invalid requests from triggering. Only valid when a
     * target View is present.
     */
    public ImageRequest<V> setTriggerSuccessForValidViewsOnly(boolean triggerSuccessForValidViewsOnly){
        this.triggerSuccessForValidViewsOnly = triggerSuccessForValidViewsOnly;
        return this;
    }

    /**
     * Set a callback to be triggered in the event of a loading error. This will not
     * be triggered if the target View is null.
     */
    public ImageRequest<V> setErrorCallback(ImageErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
        return this;
    }

    public ImageRequest<V> addBlurFilter(){
        return addBlurFilter(DEFAULT_BLUR_RADIUS);
    }

    public ImageRequest<V> addBlurFilter(int blurRadius){
        return addImageFilter(new BitmapBlurFilter(context,
                blurRadius));
    }

    public ImageRequest<V> addColorOverlayFilter(int colorOverlay) {
       return addImageFilter(new BitmapColorOverlayFilter(context,
                colorOverlay));
    }

    public ImageRequest<V> addRotationFilter(int rotationDegrees) {
        return addImageFilter(new BitmapRotationFilter(context,
                rotationDegrees));
    }

    public ImageRequest<V> addCircularCropFilter() {
        return addImageFilter(new BitmapCircularCropFilter(context));
    }

    /**
     * Replace all color values with the supplied color value while maintaining proper opacity at each pixel.
     * Intended for single color-scaled images.
     * @param replacementColor the 24-bit color value to replace with (0xAARRGGBB)
     */
    public ImageRequest<V> addColorOverrideFilter(int replacementColor){
        return addImageFilter(new BitmapColorOverrideFilter(context, replacementColor));
    }

    public ImageRequest<V> addColorFilter(ColorFilter filter) {
        return addImageFilter(new BitmapColorFilter(context, filter));
    }

    public ImageRequest<V> addCenterCropFilter(int width, int height) {
        return addImageFilter(new BitmapCenterCropFilter(context, new int[]{ width, height }));
    }

    public ImageRequest<V> addImageFilter(ImageFilter<Bitmap> imageFilter){
        bitmapImageFilters.add(imageFilter);
        return this;
    }

    @Deprecated
    public ImageRequest<V> overrideStubs(final StubHolder stubHolder){
        setLoadingStub(new StubBuilder() {
            public Drawable build(Context context) {
                return stubHolder.getLoadingDrawable(context);
            }
        });

        setErrorStub(new StubBuilder() {
            public Drawable build(Context context) {
                return stubHolder.getErrorDrawable(context);
            }
        });

        return this;
    }

    public ImageRequest<V> setLoadingStub(StubBuilder loadingStubBuilder){
        this.loadingStubBuilder = loadingStubBuilder;
        return this;
    }

    public ImageRequest<V> setErrorStub(StubBuilder errorStubBuilder){
        this.errorStubBuilder = errorStubBuilder;
        return this;
    }

    public ImageRequest<V> setDefaultImageTransition(){
        transitionController.unregisterModules();
        return this;
    }

    public ImageRequest<V> setFadeTransition(){
        return setFadeTransition(DEFAULT_CROSS_FADE_DURATION);
    }

    public ImageRequest<V> setFadeTransition(long duration){
        return addImageTransitionModule(new FadingTransitionModule(duration));
    }

    public ImageRequest<V> setScaleTransition(float from, float to){
        return setScaleTransition(from, to, DEFAULT_SCALE_DURATION);
    }

    public ImageRequest<V> setScaleTransition(float from, float to, long duration){
        return addImageTransitionModule(new ScalingTransitionModule(from, to, duration));
    }

    public ImageRequest<V> setRotationTransition(int from, int to){
        return setRotationTransition(from, to, DEFAULT_ROTATION_DURATION);
    }

    public ImageRequest<V> setRotationTransition(int from, int to, long duration){
        return addImageTransitionModule(new RotationTransitionModule(from, to, duration));
    }

    /**
     * Translate to 'normal' position from supplied parameters.
     */
    public ImageRequest<V> setTranslateTransition(float fromX, float fromY, long duration){
        return setTranslateTransition(new float[]{ fromX, fromY },
                new float[]{ 0, 0 },
                duration);
    }

    public ImageRequest<V> setTranslateTransition(float fromX, float fromY, float toX, float toY, long duration){
        return setTranslateTransition(new float[]{ fromX, fromY },
                new float[]{ toX, toY },
                duration);
    }

    public ImageRequest<V> setTranslateTransition(float[] from, float[] to, long duration){
        return addImageTransitionModule(new TranslateTransitionModule(from, to, duration));
    }

    public ImageRequest<V> addImageTransitionModule(TransitionModule module){
        this.transitionController.registerModule(module);
        return this;
    }

    public ImageRequest<V> setExitTransitionsEnabled(boolean exitTransitionsEnabled){
        this.exitTransitionsEnabled = exitTransitionsEnabled;
        return this;
    }

    /**
     * Set whether or not the loader should automatically transition the View to the request's completed image
     * for cases where you want to handle the returned Bitmap before it's actually placed within the View.
     * See setSuccessCallback(ImageSuccessCallback) for more info
     */
    public ImageRequest<V> setTransitionOnSuccessEnabled(boolean transitionOnSuccessEnabled){
        this.transitionOnSuccessEnabled = transitionOnSuccessEnabled;
        return this;
    }

    /**
     * Override an Interpolator for a previously added TransitionModule
     * @param c the class of the module (e.g. FadingTransitionModule)
     * @param interpolatorId the int ID from TransitionModule keys or custom entries (e.g. TransitionModule.INTERPOLATOR_IN)
     * @param interpolator the interpolator to substitute with
     */
    public ImageRequest<V> registerImageTransitionInterpolator(Class c, int interpolatorId, Interpolator interpolator){
        this.transitionController.registerModuleInterpolator(c, interpolatorId, interpolator);
        return this;
    }

    /**
     * Enabling this will prevent subsequent requests for the same URL from running when the target is already set correctly.
     * This uses the target View's tag holder. Don't use this with other requests that don't have this flag enabled.
     */
    public ImageRequest<V> setTagRequestPreventionEnabled(boolean tagRequestPreventionEnabled){
        this.tagRequestPreventionEnabled = tagRequestPreventionEnabled;
        return this;
    }

    @Override
    public void run() {
        boolean processableWithView = targetView != null
                && ImageLoader.getInstance(context).isViewStillUsable(this);

        boolean processableWithoutView = targetView == null
                && 0 < getRequiredImageWidth();

        if (!(processableWithView || processableWithoutView))
            return;

        post(new Runnable() {
            public void run() {
                if (requestStartedCallback != null)
                    requestStartedCallback.onRequestStarted(ImageRequest.this);
            }
        });

        try{
            String cacheKey = getEditedRequestFileCacheName();

            Drawable processed = null;

            if(lruCacheEnabled)
                processed = MemoryCache.getInstance(context)
                        .get(cacheKey);

            if(processed == null){
                processed = imageProcessor.process(this, bitmapImageFilters);

                if(lruCacheEnabled
                        && processed != null
                        && processed instanceof BitmapDrawable)
                    MemoryCache.getInstance(context)
                            .put(cacheKey, (BitmapDrawable) processed);
            }

            onRequestCompleted(processed);
        }
        catch(Throwable e){
            ImageUtils.log(context, e);

            onRequestFailed();
        }
    }

    protected int getRequiredImageWidth(){
        if(0 < requiredImageWidth)
            return requiredImageWidth;

        return !(targetView == null || targetView.getLayoutParams() == null)
                ? targetView.getLayoutParams().width
                : WIDTH_UNKNOWN;
    }

    protected void onRequestCompleted(@Nullable final Drawable targetDrawable){
        if(targetDrawable == null){
            onRequestFailed();
            return;
        }

        if(targetView != null){
            if(ImageLoader.getInstance(context).isViewStillUsable(this)){
                if(transitionOnSuccessEnabled)
                    transitionController.transitionTo(targetDrawable);
            }
            else if(triggerSuccessForValidViewsOnly)
                return;
        }

        post(new Runnable(){
            public void run(){
                if(successCallback != null)
                    successCallback.onImageReady(ImageRequest.this, targetDrawable);
            }
        });
    }

    protected void onRequestFailed() {
        if(targetView != null && ImageLoader.getInstance(context).isViewStillUsable(this))
            handleShowStubOnError();

        if(targetView == null || ImageLoader.getInstance(context).isViewStillUsable(this))
            post(new Runnable(){
                public void run(){
                    if(errorCallback != null)
                        errorCallback.onImageLoadingFailure(ImageRequest.this,
                                new RuntimeException("Image could not be loaded"));
                }
            });
    }

    protected void handleShowStubOnError(){
        final Drawable targetDrawable = showStubOnError
                ? getErrorStubBuilder().build(context)
                : ContextCompat.getDrawable(context, R.drawable.ail__default_image_placeholder);

        transitionController.transitionTo(targetDrawable);
    }

    protected String getEditedRequestFileCacheName() {
        String adjustedName = getOriginalRequestFileCacheName();

        for(ImageFilter<Bitmap> filter : bitmapImageFilters)
            adjustedName += "_" + filter.getAdjustmentInfo();

        return adjustedName + "_" + getRequiredImageWidth() + "px";
    }

    public File getEditedRequestFile() {
        return ImageLoader.getInstance(context)
                .getFileCache()
                .getFile(getEditedRequestFileCacheName());
    }

    protected String getOriginalRequestFileCacheName() {
        return 0 < targetResourceId
                ? String.valueOf(targetResourceId)
                : targetUrl;
    }

    public Context getContext(){
        return context;
    }

    public boolean isSetImageAsBackgroundForced(){
        return setImageAsBackground;
    }

    public boolean isRequestForBackgroundImage(){
        return setImageAsBackground || !(targetView instanceof ImageView);
    }

    public V getTargetView(){
        return targetView;
    }

    public Map<String, String> getHttpRequestParams(){
        return httpRequestParams;
    }

    public long getMaxCacheDurationMs(){
        return maxCacheDurationMs;
    }

    /**
     * @return the File associated with the target URL. File won't be null, but may not exist.
     */
    public File getOriginalRequestFile() {
        return ImageLoader.getInstance(context)
                .getFileCache()
                .getFile(getOriginalRequestFileCacheName());
    }

    protected StubBuilder getLoadingStubBuilder(){
        return loadingStubBuilder == null
                ? ImageLoader.getInstance(context).getLoadingStubBuilder()
                : loadingStubBuilder;
    }

    protected StubBuilder getErrorStubBuilder(){
        return errorStubBuilder == null
                ? ImageLoader.getInstance(context).getErrorStubBuilder()
                : errorStubBuilder;
    }

    public String getTargetUrl(){
        return targetUrl;
    }

    public int getTargetResourceId(){
        return targetResourceId;
    }

    public boolean isExitTransitionEnabled(){
        return exitTransitionsEnabled;
    }

    public long getStartedAtMs(){
        return startedAtMs;
    }

    public boolean isTargetLocal(){
        return loadingTargetLocally;
    }

    public int getTargetImageWidth(){
        return getRequiredImageWidth();
    }

    public ImageType getTargetImageType(){
        return targetImageType;
    }

    protected void post(Runnable runnable){
        if(targetView == null)
            ImageLoader.getInstance(context)
                .getHandler()
                .post(runnable);
        else targetView.post(runnable);
    }

    protected boolean isTargetDefined(){
        return ((targetUrl != null && 0 < targetUrl.length()) || 0 < targetResourceId)
                && imageProcessor != null;
    }

    private void handleShowStubOnExecute(){
        if(disableExecutionStubIfDownloaded
                && ImageLoader.getInstance(context).isImageDownloaded(this)
                && ImageLoader.getInstance(context).getFileCache().isCachedFileValid(targetUrl, maxCacheDurationMs))
            return;

        final Drawable targetDrawable = showStubOnExecute
                ? getLoadingStubBuilder().build(context)
                : ContextCompat.getDrawable(context, R.drawable.ail__default_image_placeholder);

        transitionController.transitionTo(targetDrawable);
    }

    public ImageRequest<V> execute() {
        if(!isTargetDefined())
            throw new RuntimeException("No target URL or resource defined!");

        final String fullRequestFile = getEditedRequestFileCacheName();

        if(isTargetViewClaimedBy(fullRequestFile)){
            ImageUtils.log(context, "View already claimed by duplicate request. Ignoring this one [" + fullRequestFile + "]");

            return this;
        }

        startedAtMs = System.currentTimeMillis();

        if(targetView == null)
            ImageLoader.getInstance(context)
                    .submit(this);
        else
            ImageLoader.getInstance(context)
                    .getHandler()
                    .post(new Runnable(){
                        public void run(){
                            if(tagRequestPreventionEnabled)
                                targetView.setTag(fullRequestFile);

                            ImageLoader.getInstance(context)
                                    .claimViewTarget(ImageRequest.this);

                            handleShowStubOnExecute();

                            targetView.post(new Runnable() {
                                public void run() {
                                    ImageLoader.getInstance(context)
                                            .submit(ImageRequest.this);
                                }
                            });
                        }
                    });

        return this;
    }

    protected boolean isTargetViewClaimedBy(String fullRequestFile){
        return tagRequestPreventionEnabled
                && !(targetView == null || targetView.getTag() == null)
                && targetView.getTag().equals(fullRequestFile);
    }

    /**
     * @param delay amount to delay the execution by
     */
    public ImageRequest<V> execute(long delay){
        ImageLoader.getInstance(context)
                .getHandler()
                .postDelayed(new Runnable(){
                    public void run(){
                        execute();
                    }
                }, delay);

        return this;
    }

    public static <V extends View> ImageRequest<V> create(@NonNull V targetView){
        return new ImageRequest<V>(targetView.getContext(), targetView);
    }

    public static ImageRequest prefetch(Context context, String url){
        return new ImageRequest(context)
                .setTargetUrl(url)
                .execute();
    }

}
