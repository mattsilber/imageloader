package com.guardanis.imageloader.stubs;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.guardanis.imageloader.R;

public class DefaultLoadingDrawable extends AnimatedStubDrawable {

    private static final int MIN_ARC = 5;
    private static final int MAX_ARC = 360;
    private static final float ARC_SPEED = 7.25f;

    private static final float ROTATION_SPEED = 2.85f;

    private LoadingDrawableState state;

    private Paint loadingPaint = new Paint();
    private float rotationDegrees = 0;
    private float arcAngle = 270;
    private RectF drawableBounds = new RectF();
    private boolean arcModeLower = true;

    private int lastBoundedWidth = -1;

    public DefaultLoadingDrawable(Resources resources){
        this(resources, resources.getColor(R.color.ail__default_stub_loading_tint));
    }

    public DefaultLoadingDrawable(Resources resources, int color){
        this.state = new LoadingDrawableState(resources);

        setBounds(0, 0,
                (int) resources.getDimension(R.dimen.ail__default_stub_loading_size),
                (int) resources.getDimension(R.dimen.ail__default_stub_loading_size));

        setupPaints(resources, color);
    }

    protected void setupPaints(Resources resources, int color){
        loadingPaint.setAntiAlias(true);
        loadingPaint.setColor(color);
        loadingPaint.setStyle(Paint.Style.STROKE);
        loadingPaint.setStrokeWidth(resources.getDimension(R.dimen.ail__default_stub_loading_stroke_width));
    }

    @Override
    public void draw(Canvas canvas) {
        if(lastBoundedWidth != canvas.getWidth())
            setupRect(canvas);

        canvas.save();

        canvas.rotate(rotationDegrees, canvas.getWidth() / 2, canvas.getHeight() / 2);

        canvas.drawArc(drawableBounds, 0, arcAngle, false, loadingPaint);

        canvas.restore();

        updatePositions();
        invalidateSelf();
    }

    protected void setupRect(Canvas canvas){
        int desiredSize = Math.min(canvas.getClipBounds().width() / 8, canvas.getClipBounds().height() / 5) / 2;

        int[] center = new int[]{ canvas.getClipBounds().width() / 2,
                canvas.getClipBounds().height() / 2 };

        drawableBounds.left = center[0] - desiredSize;
        drawableBounds.right = center[0] + desiredSize;
        drawableBounds.top = center[1] - desiredSize;
        drawableBounds.bottom = center[1] + desiredSize;

        lastBoundedWidth = canvas.getWidth();
    }

    protected void updatePositions(){
        rotationDegrees = (rotationDegrees + ROTATION_SPEED) % 360;

        if(arcModeLower){
            arcAngle -= ARC_SPEED;

            if(arcAngle <= MIN_ARC)
                arcModeLower = false;
        }
        else {
            arcAngle += ARC_SPEED;

            if(MAX_ARC <= arcAngle)
                arcModeLower = true;
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.RGBA_8888;
    }

    @Override
    public void setAlpha(int alpha) {
        loadingPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter filter) {
        loadingPaint.setColorFilter(filter);
    }

    @Override
    public ConstantState getConstantState() {
        state.changingConfigurations = super.getChangingConfigurations();
        return state;
    }

    final static class LoadingDrawableState extends Drawable.ConstantState{

        private Resources resources;
        private int changingConfigurations;

        public LoadingDrawableState(Resources resources){
            this.resources = resources;
        }

        @Override
        public Drawable newDrawable() {
            return new DefaultLoadingDrawable(resources);
        }

        @Override
        public Drawable newDrawable(Resources resources) {
            return new DefaultLoadingDrawable(resources);
        }

        @Override
        public int getChangingConfigurations() {
            return changingConfigurations;
        }
    }

}
