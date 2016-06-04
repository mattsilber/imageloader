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

    protected static final int MIN_ARC = 5;
    protected static final int MAX_ARC = 360;
    protected static final float ARC_SPEED = 7.25f;

    protected static final float ROTATION_SPEED = 2.85f;

    protected LoadingDrawableState state;

    protected Paint loadingPaint = new Paint();
    protected float rotationDegrees = 0;
    protected float arcAngle = 270;
    protected RectF drawableBounds = new RectF();
    protected boolean arcModeLower = true;

    private int[] lastBounds = new int[]{ -1, -1 };

    protected int[] sizeRange;

    public DefaultLoadingDrawable(Resources resources){
        this(resources, resources.getColor(R.color.ail__default_stub_loading_tint));
    }

    public DefaultLoadingDrawable(Resources resources, int color){
        this.state = new LoadingDrawableState(resources);

        this.sizeRange = new int[]{ (int) resources.getDimension(R.dimen.ail__default_stub_loading_arc_min_size),
                (int) resources.getDimension(R.dimen.ail__default_stub_loading_arc_max_size)};

        setBounds(0, 0,
                (int) resources.getDimension(R.dimen.ail__default_stub_loading_bounds),
                (int) resources.getDimension(R.dimen.ail__default_stub_loading_bounds));

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
        if(!(lastBounds[0] == canvas.getWidth() && lastBounds[1] == canvas.getHeight()))
            setupRect(canvas);

        if(canvasMatrixOverride != null)
            canvas.setMatrix(canvasMatrixOverride);

        canvas.save();

        canvas.rotate(rotationDegrees, canvas.getWidth() / 2, canvas.getHeight() / 2);

        canvas.drawArc(drawableBounds, 0, arcAngle, false, loadingPaint);

        canvas.restore();

        canvas.getMatrix(baseCanvasMatrix);

        updatePositions();
        invalidateSelf();
    }

    protected void setupRect(Canvas canvas){
        int desiredSize = Math.min(canvas.getWidth() / 8, canvas.getHeight() / 5) / 2;
        desiredSize = Math.min(desiredSize, sizeRange[1]);
        desiredSize = Math.max(desiredSize, sizeRange[0]);

        int[] center = new int[]{ canvas.getWidth() / 2,
                canvas.getHeight() / 2 };

        drawableBounds.left = center[0] - desiredSize;
        drawableBounds.right = center[0] + desiredSize;
        drawableBounds.top = center[1] - desiredSize;
        drawableBounds.bottom = center[1] + desiredSize;

        lastBounds = new int[]{ canvas.getWidth(),
                canvas.getHeight() };
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
