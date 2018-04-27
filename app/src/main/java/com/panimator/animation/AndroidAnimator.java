package com.panimator.animation;

import android.graphics.Canvas;

import com.panimator.codeBlue.rendering.Renderer;

/**
 * Created by ASANDA on 2018/01/30.
 * for Pandaphic
 */

public abstract class AndroidAnimator<F> extends Renderer<F> {
    protected abstract String[] getRequiredPermissions();

    protected void refreshCanvas(Canvas canvas, int color){
        canvas.drawColor(color);
    }
}
