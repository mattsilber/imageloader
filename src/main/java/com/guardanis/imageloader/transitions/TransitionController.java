package com.guardanis.imageloader.transitions;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.guardanis.imageloader.ImageRequest;
import com.guardanis.imageloader.R;
import com.guardanis.imageloader.stubs.StubDrawable;
import com.guardanis.imageloader.transitions.drawables.TransitionDrawable;
import com.guardanis.imageloader.transitions.modules.TransitionModule;

import java.util.HashMap;
import java.util.Map;

public class TransitionController {

    protected ImageRequest request;

    protected Map<Class, TransitionModule> modules = new HashMap<Class, TransitionModule>();

    public TransitionController(ImageRequest request){
        this.request = request;
    }

    public void registerModule(TransitionModule module){
        if(module != null)
            modules.put(module.getClass(), module);
    }

    /**
     * @param c the class of the module (e.g. FadingTransitionModule)
     * @param interpolatorId the int ID from TransitionModule keys or custom entries (e.g. TransitionModule.INTERPOLATOR_IN)
     * @param interpolator the interpolator to substitute with
     */
    public void registerModuleInterpolator(Class c, int interpolatorId, Interpolator interpolator){
        if(modules != null)
            modules.get(c).registerInterpolator(interpolatorId, interpolator);
    }

    public void unregisterModules(){
        modules = new HashMap<Class, TransitionModule>();
    }

    public void transitionTo(Drawable to){
        if(modules.size() < 1)
            setTargetViewDrawable(to);
        else{
            Bitmap targetBitmap = getTargetBitmap(to, request.getTargetView());

            Drawable currentlyInView = request.isExitTransitionEnabled()
                    ? getCurrentTargetDrawable(targetBitmap)
                    : null;

            TransitionDrawable drawable = new TransitionDrawable(request.getContext(),
                    currentlyInView,
                    to,
                    targetBitmap);

            if(currentlyInView != null){
                if(!request.isRequestForBackgroundImage() && currentlyInView instanceof TransitionDrawable)
                    ((TransitionDrawable) currentlyInView).overrideCanvasMatrix(((TransitionDrawable) currentlyInView).getBaseCanvasMatrix());
                else if(currentlyInView instanceof StubDrawable)
                    ((StubDrawable) currentlyInView).overrideCanvasMatrix(((StubDrawable) currentlyInView).getBaseCanvasMatrix());
            }

            for(TransitionModule module : modules.values())
                drawable.registerModule(module);

            setTransitionDrawable(drawable);
        }
    }

    protected Drawable getCurrentTargetDrawable(Bitmap targetBitmap){
        Drawable current = request.isRequestForBackgroundImage()
                ? request.getTargetView().getBackground()
                : ((ImageView)request.getTargetView()).getDrawable();

        if(current == null)
            return ContextCompat.getDrawable(request.getContext(), R.drawable.ail__default_fade_placeholder);
        else if(current instanceof TransitionDrawable && ((TransitionDrawable) current).getTargetDrawable() instanceof StubDrawable)
            return ((TransitionDrawable) current).getTargetDrawable();
        else return current;
    }

    protected Bitmap getTargetBitmap(Drawable drawable, @Nullable View targetView){
        if(drawable instanceof BitmapDrawable)
            return ((BitmapDrawable) drawable).getBitmap();
        else {
            Bitmap bitmap = null;

            if(drawable.getIntrinsicWidth() < 1 || drawable.getIntrinsicHeight() < 1){
                if(!(targetView == null || targetView.getLayoutParams() == null || targetView.getLayoutParams().width < 1 || targetView.getLayoutParams().height < 1))
                    bitmap = Bitmap.createBitmap(targetView.getLayoutParams().width,
                            targetView.getLayoutParams().height,
                            Bitmap.Config.ARGB_8888);
                else if(drawable.getBounds().right - drawable.getBounds().left < 1)
                    bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                else bitmap = Bitmap.createBitmap(drawable.getBounds().right - drawable.getBounds().left,
                        drawable.getBounds().bottom - drawable.getBounds().top,
                        Bitmap.Config.ARGB_8888);
            }
            else bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);

            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        }
    }

    protected void setTransitionDrawable(final TransitionDrawable drawable){
        request.getTargetView().post(new Runnable(){
            public void run(){
                if (request.isRequestForBackgroundImage())
                    setBackgroundDrawable(drawable);
                else ((ImageView) request.getTargetView())
                        .setImageDrawable(drawable);

                drawable.start(request.getContext());
            }
        });
    }

    protected void setTargetViewDrawable(final Drawable drawable){
        request.getTargetView().post(new Runnable(){
            public void run(){
                if(request.isRequestForBackgroundImage())
                    setBackgroundDrawable(drawable);
                else ((ImageView) request.getTargetView())
                        .setImageDrawable(drawable);
            }
        });
    }

    @SuppressLint("NewApi")
    protected void setBackgroundDrawable(Drawable drawable) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
            request.getTargetView().setBackgroundDrawable(drawable);
        else request.getTargetView().setBackground(drawable);
    }
}
