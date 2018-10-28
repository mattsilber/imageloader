# imageloader

[![Download](https://api.bintray.com/packages/mattsilber/maven/imageloader/images/download.svg) ](https://bintray.com/mattsilber/maven/imageloader/_latestVersion)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-imageloader-green.svg?style=true)](https://android-arsenal.com/details/1/4034)

Another lazy image-loading library with AndroidSVG, Animated GIF, and Bitmap filtering support built-in

![imageloader Sample](https://github.com/mattsilber/imageloader/raw/master/imageloader.gif)

# Installation

```groovy
repositories {
    jcenter()
}

dependencies {
    implementation 'com.guardanis:imageloader:2.0.0'
}
```

# Usage

This was originally written several years ago as an included lazy loader for downloading/caching images, but then evolved to allow the chaining of adjustments on the underlying Bitmap (what I call an ImageFilter). Let's say, for instance, I want to download your Facebook profile picture, throw a blur on top, overlay a dark-transparent color, and then throw it into an ImageView, but fade in for a duration of 150ms and scale in with a bounce. Well, you could actually do all of that pretty easily:

```java
ImageRequest.create(findViewbyId(R.id.some_image_view))
    .setTargetUrl("https://d3819ii77zvwic.cloudfront.net/wp-content/uploads/2013/07/awkward_photos.jpg")
    .addBlurFilter()
    .addColorOverlayFilter(activity.getResources()
            .getColor(R.color.my_awesome_overlay_color))
    .setFadeTransition(150)
    .setScaleTransition(0f, 1f, 750)
    .registerImageTransitionInterpolator(ScalingTransitionModule.class,
            TransitionModule.INTERPOLATOR_IN,
            new BounceInterpolator())
    .execute();
```

You can also create a new ImageRequest the old way using `new ImageRequest(context)` and assign the target View later with `ImageRequest.setTargetView(View)`. e.g.

```java
new ImageRequest(context)
    .setTargetView(findViewById(R.id.some_image_view))
    ...
    .execute();
```

### Custom Filters

As mentioned above, this library comes with a few stock Bitmap filters (such as blurring, overlaying colors, rotating, color replacement, etc.). But, it also let's you add your own filters by working with the abstract ImageFilter class. Using the above example, we can easily add a custom filter via *addImageFilter(ImageFilter<Bitmap> filter)*:

```java
ImageRequest.create(myImageView)
    .setTargetUrl("https://d3819ii77zvwic.cloudfront.net/wp-content/uploads/2013/07/awkward_photos.jpg")
    .addImageFilter(new ImageFilter<Bitmap>(context){
        @Override
        public Bitmap filter(Bitmap unedited){
            return unedited;
        }
    })
    .execute();
```

##### Currently supported ImageFilter<Bitmap>
* BitmapBlurFilter : addBlurFilter(), addBlurFilter(int) : Blur the Bitmap
* BitmapCircularCropFilter : addCircularCropFilter() : Crop the Bitmap to the bounrdaries of a circle
* BitmapColorOverlayFilter : addColorOverlayFilter(int) : Overlay the Bitmap with a color
* BitmapColorOverrideFilter : addColorOverrideFilter(int) : Replace all color values with the supplied color value while maintaining proper opacity at each pixel
* BitmapRotationFilter : addRotationFilter(int) : Rotate the Bitmap (degrees) (may change Bitmap size)
* BitmapColorFilter : addColorFilter(ColorFilter) : Apply a ColorFilter to the Bitmap
* BitmapCenterCropFilter : addCenterCropFilter(int, int) : Perform a centerCrop-type filter on an image (useful for backgrounds)

### AndroidSVG Assets

For more information about AndroidSVG, click [here](https://github.com/BigBadaboom/androidsvg).

If you want to load SVG Images and perform adjustments on the underlying Bitmap, you can create and execute a regular ImageRequest where the target can be an external URL, an asset, a File, or a resource in your /raw/ folder. e.g.

```java
ImageRequest.create(myImageView)
    .setTargetAsset("my_svg_file.svg") // From assets
    .setTargetResource(R.raw.my_svg, ImageType.SVG) // From resources
    .setTargetUrl("http://some.site/my_svg.svg")
    .setTargetFile(new File("/sdcard/0/my_svg.svg"))
    .execute();
```

##### SVG Requirements

In order to load an SVG efficiently (as in, down/up-sample it appropriately), we need to know it's intrinsic width and height in pixels. That means, if the width/height and viewBox (the fallback) attributes are both missing from the SVG's declaration, it won't be able to render it. e.g.

    x="0px" y="0px" **width="150px" height="150px"**

or

    x="0px" y="0px" **viewBox="0 0 150 150"**

As long as the width/height or viewBox are correctly set, everything should be good to go, and all SVGs should scale up/down correctly (all of which the ImageRequest can handle by itself).

Support for the viewBox fallback was added in the release of v1.2.2.

### Vector Asset Support

As of version 1.4.2, this library can now support local vector assets located in the application's drawable resources.

```java
ImageRequest.create(myImageView)
    .setTargetDrawable(R.drawable.ic_android_test) // <-- load drawable resources
    .execute();
```


### Gif Support

As of version 1.2.10, this library can now support animated gifs loaded both locally and over the network.

For more information about the android-gif-drawable library created by koral--, click [here](https://github.com/koral--/android-gif-drawable).

To load a gif, create an ImageRequest the same way you would any other request in this library:

```java
ImageRequest.create(myImageView)
    .setTargetUrl("http://site.com/my_gif.gif") // <-- load from web
    .setTargetAsset("my_gif.gif") // <-- Load from assets
    .setTargetFile("/sdcard/0/my_gif.gif") // <-- Load from storage
    .setTargetResource(R.raw.my_gif, ImageType.GIF) // <-- Load from resources
    .execute();
```

By default, gifs will start running automatically. If you want to override that behavior globally, set *R.bool.ail__gif_auto_start_enabled* to false.

You can also handle that manually on a per-case basis using the ImageSuccessCallback:

```java
new ImageRequest(context)
    .setSuccessCallback((r, d) -> {
         if(d instanceof GifDrawable)
              ((GifDrawable) d).stop();
    })
```

##### Supported features
* Transitions at the canvas level are fully supported even while the gifs are running (minus alpha blending, that is)
* Gifs can currently be pulled via the web, assets, local storage, or raw resources
* All other features (other than those listed in the Unsupported section) should work as well.

##### Unsupported and ToDo
* ImageFilters cannot be applied to gifs

### Stubs

The ImageRequest system allows you to specify both a loading and an error stub to an individual request, or by overriding the global defaults.

```java
ImageRequest.create(myImageView)
    .setTargetUrl("https://d3819ii77zvwic.cloudfront.net/wp-content/uploads/2013/07/awkward_photos.jpg")
    .setShowStubOnExecute(true)
    .setShowStubOnError(true)
    .setLoadingStub(context -> new DefaultLoadingStubDrawable(context))
    .setErrorStub(context -> ContextCompat.getDrawable(context, R.drawable.some_drawable))
    .execute();
```

To override the default error image (which is a transparent rectangle...), just override the drawable resource:

```xml
ail__image_loader_stub_error
```

If you want to override the animations globally, you can set the `StubBuilder` class in your Application's onCreate and it will be saved/reloaded as needed.

```java
ImageLoader.getInstance(context)
    .registerLoadingStub(DefaultLoadingStubBuilder.class)
    .registerErrorStub(DefaultErrorStubBuilder.class);
```

If you want to create your own animation drawables in code, you can simply supply an AnimatedStubDrawable (simple Drawable extention) with your request.

By default, the ImageRequest will show the loading stub, but not the error stub, which needs to be manually enabled. I don't remember for the life of me why I did that, but I may consider changing that in the future...

### Transitions

As of version 1.1.0, a single `TransitionController` is now the interface between a single `TransitionDrawable` and any `TransitionModules` it contains. The TransitionModules are the modular components used to actually perform adjustments to the underlying Canvas/Bitmap/Drawable at the appropriate times, and can be easily added to any request. The idea here is to (hopefully) allow multiple types of transitions to be run at the same time (e.g. scale and fade).

##### TransitionModules

There are several TransitionModules already build in:

* FadingTransitionModule 
* ScalingTransitionModule
* TranslateTransitionModule
* RotationTransitionModule

##### Interpolators

Each TransitionModule contains two Interpolators for animating, denoted by the keys of TransitionModule.INTERPOLATOR_IN and TransitionModule.INTERPOLATOR_OUT, respectively.

To change an Interpolator for a specific TransitionModule, you can use the `ImageRequest.registerImageTransitionInterpolator(Class, int, Interpolator)` helper method or register it directly on the module itself.

The `Class` object here would be the TransitionModule you're using (see TransitionModules section above).

Here's an example changing the ScalingTransitionModule's entrance Interpolator to bounce:

```java
ImageRequest.create(findViewbyId(R.id.some_image_view))
    .setScaleTransition(0f, 1f, 750)
    .registerImageTransitionInterpolator(ScalingTransitionModule.class,
            TransitionModule.INTERPOLATOR_IN,
            new BounceInterpolator())
    .execute();
```

### Caching

##### File Caching

By default, the system will try to save things in {External_Storage_Dir}/{some.package.name}, and fallback to the context.getCacheDir() when the media isn't mounted or WRITE_EXTERNAL_STORAGE permissions is not granted. 

If you would like to override that bahavior, setting **R.bool.ail__external_storage_enabled** to false will cause it to use the Cache directory by default. 

If you would like to use **context.getFilesDir()** instead of the Cache, simply set **R.bool.ail__use_cache_dir** to false.

##### Memory Caching

As of version 1.3.1, the imageloader can now use an instance of android.support.v4.util.LruCache before attempting to read from the File cache. By default, the Lru cache will use up to 1/8 of the available virtual memory (taken from the support docs), but this value can be changed by overriding **R.integer.ail__lru_available_memory_reciprical**. 

You can also enable/disable the cache per-request basis by calling **ImageRequest.setLruCacheEnabled(boolean)** or globally by overriding **R.bool.ail__lru_cache_enabled**.

### Prefetching Images

Prefetching images can be achieved by simply not setting a target View (i.e. don't call ImageRequest.setTargetView(myImageView) or use the static create `prefetch(Context, String)` method. 

```java
ImageRequest.prefetch(context,
    "https://d3819ii77zvwic.cloudfront.net/wp-content/uploads/2013/07/awkward_photos.jpg");
```
or 
```java
new ImageRequest(context)
    .setTargetUrl("https://d3819ii77zvwic.cloudfront.net/wp-content/uploads/2013/07/awkward_photos.jpg")
    .execute();
```

As of v1.3.5, a callback for completion or failure can be triggered for prefetching images, provided that either a target View or a required width are explicitly set (`ImageRequest.setRequiredImageWidth(int)`) for the request.

Without a View or an explicit width, none of the ImageFilters can be applied and no completion callbacks will be triggered.

### Notes
* ImageRequest's for the same URL are safe to call at the same time. The ImageLoader will delay subsequent requests for the same URL until the download has finished.
* Adjustments via ImageFilter are only performed/saved, and success callbacks are only triggered, when a target View is present or an explicit width has been set via `ImageRequest.setRequiredImageWidth(int)`.
* Setting a maximum cachable duration for a target URL can be achieved by calling ImageRequest.setMaxCacheDurationMs(long); e.g. setMaxCacheDurationMs(TimeUnit.DAYS.toMillis(5));
* For lists, you probably don't want exit transitions as it can appear weird when scrolling quickly. To disable them, call ImageRequest.setExitTransitionsEnabled(false)

##### Things I plan on adding when I get the chance
* Custom SVG Image Filters (e.g. replacing colors)
* Restart image downloads for certain failed image download errors

##### Known Issues
* If trying to load a super large image (like a picture from the Camera) into an ImageView with layout_width as either fill_parent or wrap_cotent, it may run into an OutOfMemoryError with filters due to the barely-downsampled size of the image. To avoid this, either set the LayoutParams's width manually, or use the helper method: **ImageRequest.setRequiredImageWidth(int)**
* Loading many versions of the same SVG file too quickly can cause the SVGParser to throw an "Invalid Colour Keyword" error due to the version we're using not supporting asynchronous SVG parsing. Delaying those requests by ~5-10ms is a crappy workaround.
* Using the DefaultLoadingStub when the request is for View's background may cause a slight vertical rendering shift I don't yet understand.

##### RotationTransitionModule issues:
* It doesn't work for none-square backgrounds (i.e. non-ImageView requests or ones with setAsbackground(true)). It's all weird and not pretty. Don't use it.

