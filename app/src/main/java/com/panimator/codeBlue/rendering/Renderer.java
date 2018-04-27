package com.panimator.codeBlue.rendering;

import com.panimator.codeBlue.display.Plane;

/**
 * Created by ASANDA on 2018/01/27.
 * for Pandaphic
 */

public abstract class Renderer<F> {
    public final void prepare(RenderingSession<F> renderingSession){
        this.onPrepare(renderingSession);
    }
    public final void render(RenderingSession<F> renderingSession){
        this.onRender(renderingSession);
    }

    protected abstract void onPrepare(RenderingSession<F> renderingSession);
    protected abstract void onRender(RenderingSession<F> renderingSession);

    public abstract Class<? extends Plane> getMainInputCollector();
    public abstract String getTitle();
    public String getPreviewName(){return  this.getTitle().toLowerCase().replace(' ', '_') + "_demo.mp4";}
    @Override
    public String toString() {
        return this.getTitle();
    }
}
