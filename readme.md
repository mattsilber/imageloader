# imageloader

Another lazy image loading library with AndroidSVG support built-in

# Installation

```
    repositories {
        jcenter()
    }

    dependencies {
        compile('com.guardanis:imageloader:1.0.8')
    }
```

If you want to use a custom version of *com.caverock.androidsvg*, ensure that you've set **transitive=false** as a property on the imageloader dependency you define in your *build.gradle* file.


# Usage

This was originally written several years ago as an included lazy loader for downloading/caching images, but then evolved to allow the chaining of adjustments on the underlying Bitmap (what I call an ImageFilter). Let's say, for instance, I want to download my Facebook profile picture, throw a blur on top, overlay a dark-transparent color, and then throw it into an ImageView, but fade in for a duration of 150ms. Well, you could actually do all of that pretty easily:

```
    new ImageRequest<ImageView>(context)
        .setTargetUrl("https://d3819ii77zvwic.cloudfront.net/wp-content/uploads/2013/07/awkward_photos.jpg")
        .setTargetView(myImageView)
        .addBlurFilter()
        .addColorOverlayFilter(activity.getResources().getColor(R.color.menu_header_user_image_parent_blurred_overlay))
        .setFadeTransition(150)
        .execute();
```

Disclaimer: That picture ain't me.

### Custom Filters

As mentioned above, this library comes with a few stock filters (such as blurring, overlaying colors, rotating, color replacement, etc.). But, it also let's you add your own filters by working with the abstract ImageFilter class. Using the above example, we can easily add a custom filter via *addImageFilter(ImageFilter<Bitmap> filter)*:

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

### AndroidSVG Assets

For more information about AndroidSVG, click [here](https://github.com/BigBadaboom/androidsvg).

If you want to load SVG Images and perform adjustments on the underlying Bitmap, you can create and execute an SVGAssetRequest, where the targetUrl is simply the file name of the SVG in your *assets* folder. e.g.

```
    new SVGAssetRequest<ImageView>(context)
        .setTargetUrl("my_svg_file.svg")
        .setTargetView(myImageView)
        .execute();
```

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

or you can change it globally by overriding the default drawable files:

    ail__image_loader_stub
    ail__image_loader_stub_error

By default, the ImageRequest will show the loading stub, but not the error stub, which needs to be manually enabled. I don't remember for the life of me why I did that, but I may consider changing that in the future...

##### Notes
* Prefetching images can be achieved by simply not setting a target View (e.g. don't call ImageRequest.setTargetView(myImageView). 
* ImageRequest's for the same URL are safe to call at the same time. The ImageLoader will delay subsequent requests for the same URL until the download has finished.
* Adjustments via ImageFilter are only saved when a target View is present

##### Things I plan on adding when I get the chance
* Custom SVG Image Filters (e.g. replacing colors)
* Cache extra image adjustments by file size
* Restart image downloads for certain failed images download errors

##### Known Issues
* If trying to load a super large image (like a picture from the Camera) into an ImageView with layout_width as either fill_parent or wrap_cotent, it may run into an OutOfMemoryError with filters due to the barely-downsampled size of the image. To avoid this, either set the LayoutParams's width manually, or use the helper method: **ImageRequest.setRequiredImageWidth(int)**

