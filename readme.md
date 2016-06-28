# imageloader

Another lazy image-loading library with AndroidSVG and Bitmap filtering support built-in

![imageloader Sample](https://github.com/mattsilber/imageloader/raw/master/imageloader.gif)

# Installation

```
    repositories {
        jcenter()
    }

    dependencies {
        compile('com.guardanis:imageloader:1.2.9')
    }
```

If you want to use a custom version of *com.caverock.androidsvg*, ensure that you've set **transitive=false** as a property on the imageloader dependency you define in your *build.gradle* file.


# Usage

This was originally written several years ago as an included lazy loader for downloading/caching images, but then evolved to allow the chaining of adjustments on the underlying Bitmap (what I call an ImageFilter). Let's say, for instance, I want to download your Facebook profile picture, throw a blur on top, overlay a dark-transparent color, and then throw it into an ImageView, but fade in for a duration of 150ms. Well, you could actually do all of that pretty easily:

```
    new ImageRequest<ImageView>(context)
        .setTargetUrl("https://d3819ii77zvwic.cloudfront.net/wp-content/uploads/2013/07/awkward_photos.jpg")
        .setTargetView(myImageView)
        .addBlurFilter()
        .addColorOverlayFilter(activity.getResources().getColor(R.color.menu_header_user_image_parent_blurred_overlay))
        .setFadeTransition(150)
        .execute();
```

### Custom Filters

As mentioned above, this library comes with a few stock Bitmap filters (such as blurring, overlaying colors, rotating, color replacement, etc.). But, it also let's you add your own filters by working with the abstract ImageFilter class. Using the above example, we can easily add a custom filter via *addImageFilter(ImageFilter<Bitmap> filter)*:

```
    new ImageRequest<ImageView>(context, "https://d3819ii77zvwic.cloudfront.net/wp-content/uploads/2013/07/awkward_photos.jpg")
        .setTargetView(myImageView)
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
* BitmapColorReplacementFilter : addColorReplacementFilter(int, int), addColorReplacementFilter(Map<Int, Int>) : Replace any occurance of a color with it's mapped value
* BitmapRotationFilter : addRotationFilter(int) : Rotate the Bitmap (degrees) (may change Bitmap size)
* BitmapColorFilter : addColorFilter(ColorFilter) : Apply a ColorFilter to the Bitmap
* BitmapCenterCropFilter : addCenterCropFilter(int, int) : Perform a centerCrop-type filter on an image (useful for backgrounds)

### AndroidSVG Assets

For more information about AndroidSVG, click [here](https://github.com/BigBadaboom/androidsvg).

If you want to load SVG Images and perform adjustments on the underlying Bitmap, you can create and execute an SVGAssetRequest, where the targetUrl is simply the file name of the SVG in your *assets* folder. e.g.

```
    new SVGAssetRequest<ImageView>(context)
        .setTargetUrl("my_svg_file.svg")
        .setTargetView(myImageView)
        .execute();
```

SVGs are also fully supported over the internet via an ImageRequest, as long as the SVG has an intrinsice width and height. See below for more details on that.

##### SVG Requirements

In order to load an SVG efficiently (as in, downsample it appropriately), we need to know it's intrinsic width and height in pixels. That means, if a width/height or at least a viewBox (the fallback) attribute is missing from the SVG's declaration, it won't be able to render it. e.g.

    x="0px" y="0px" **width="150px" height="150px"**

or

    x="0px" y="0px" **viewBox="0 0 150 150"**

As long as the width/height or viewBox are correctly set, everything should be good to go, and all SVGs should scale up/down correctly (all of which the ImageRequest can handle by itself).

Support for the viewBox fallback was added in the release of v1.2.2.

### Stubs

The ImageRequest system allows you to specify both a loading and an error stub to an individual request by attaching a StubHandler:

```
    new ImageRequest<ImageView>(context, "https://d3819ii77zvwic.cloudfront.net/wp-content/uploads/2013/07/awkward_photos.jpg")
        .setTargetView(myImageView)
        .setShowStubOnExecute(true)
        .setShowStubOnError(true)
        .overrideStubs(new SubHolder(){
            @Override
            public Drawable getLoadingDrawable(Context context){
                return ContextCompat.getDrawable(context, R.drawable.some_drawable);
            }
            @Override
            public Drawable getErrorDrawable(Context context){
                return ContextCompat.getDrawable(context, R.drawable.some_drawable);
            }
        })
        .execute();
```

As of version 1.0.8, the ImageRequest will use the DefaultLoadingDrawable for loading stubs, as opposed to the static resources image. If you'd like to configure the default tint for the loading drawable, override the color resource *R.color.ail__default_stub_loading_tint*. The error stub is still the same *R.drawable.ail__image_loader_stub_error*. 

If you want to revert back to the previous behavior of resources-only, you can override *R.bool.ail__use_old_resource_stubs* or call *ImageRequest.setUseOldResourceStubs(true)* and it will use the old default stub container (unless you manually override the stubs). If you choose to do this, just override the drawables:

    ail__image_loader_stub
    ail__image_loader_stub_error

If you want to create your own animation drawables in code, you can simply supply an AnimatedStubDrawable (simple Drawable extention) with your StubHolder.

By default, the ImageRequest will show the loading stub, but not the error stub, which needs to be manually enabled. I don't remember for the life of me why I did that, but I may consider changing that in the future...

### Transitions

As of version 1.1.0, a single TransitionController is now the interface between a single TransitionDrawable and any TransitionModules it contains. The TransitionModules are the modular components used to actually perform adjustments to the underlying Canvas/Bitmap/Drawable at the appropriate times, and can be easily added to any request. The idea here is to (hopefully) allow multiple types of transitions to be run at the same time (e.g. scale and fade).

The currently included TransitionModules are the FadingTransitionModule and the ScalingTransitionModule.

### Caching

By default, the system will try to save things in {External_Storage_Dir}/{some.package.name}, and fallback to the context.getCacheDir() when the media isn't mounted or WRITE_EXTERNAL_STORAGE permissions has been revoked. 

If you would like to override that bahavior, setting **R.bool.ail__external_storage_enabled** to false will cause it to use the Cache directory by default. 

If you would like to use **context.getFilesDir()** instead of the Cache, simply set **R.bool.ail__use_cache_dir** to false.

##### Notes
* Prefetching images can be achieved by simply not setting a target View (e.g. don't call ImageRequest.setTargetView(myImageView). 
* ImageRequest's for the same URL are safe to call at the same time. The ImageLoader will delay subsequent requests for the same URL until the download has finished.
* Adjustments via ImageFilter are only saved, and success callbacks are only triggered, when a target View is present.
* Setting a maximum cachable duration for a target URL can be achieved by calling ImageRequest.setMaxCacheDurationMs(long); e.g. setMaxCacheDurationMs(TimeUnit.DAYS.toMillis(5));
* For lists, you probably don't want exit transitions as it can appear weird when scrolling quickly. To disable them, call ImageRequest.setExitTransitionsEnabled(false)

##### Things I plan on adding when I get the chance
* Custom SVG Image Filters (e.g. replacing colors)
* Cache extra image adjustments by file size
* Restart image downloads for certain failed images download errors

##### Known Issues
* If trying to load a super large image (like a picture from the Camera) into an ImageView with layout_width as either fill_parent or wrap_cotent, it may run into an OutOfMemoryError with filters due to the barely-downsampled size of the image. To avoid this, either set the LayoutParams's width manually, or use the helper method: **ImageRequest.setRequiredImageWidth(int)**
* Loading many versions of the same SVG file too quickly can cause the SVGParser to throw an "Invalid Colour Keyword" error due to the version we're using not supporting asynchronous SVG parsing.
* Using the DefaultLoadingStub when the request is for View's background may cause a slight vertical rendering shift I don't yet understand.

####### RotationTransitionModule issues:
* It doesn't work for none-square backgrounds (i.e. non-ImageView requests or ones with setAsbackground(true)). It's all weird and not pretty. Don't use it.

