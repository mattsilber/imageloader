package com.guardanis.imageloader.stubs;

import android.graphics.Matrix;
import android.graphics.drawable.Drawable;

public abstract class StubDrawable extends Drawable {

    // Base class for determining if a TransitionDrawable should call
    // super.onDraw(Canvas) or draw from the target Drawable

    protected Matrix baseCanvasMatrix = new Matrix();
    protected Matrix canvasMatrixOverride;

    public void overrideCanvasMatrix(Matrix canvasMatrixOverride){
        this.canvasMatrixOverride = canvasMatrixOverride;
    }

    public Matrix getBaseCanvasMatrix(){
        return baseCanvasMatrix;
    }

}
