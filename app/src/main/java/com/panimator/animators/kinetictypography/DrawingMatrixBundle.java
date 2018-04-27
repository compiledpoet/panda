package com.panimator.animators.kinetictypography;

import android.graphics.Rect;

/**
 * Created by ASANDA on 2018/01/23.
 * for Pandaphic
 */

public class DrawingMatrixBundle {
    private final DrawingMatrix[] drawingMatrices;
    private final Rect bounds;

    public DrawingMatrixBundle(DrawingMatrix[] pDrawingMatrices, Rect pBounds){
        this.drawingMatrices = pDrawingMatrices;
        this.bounds = pBounds;
    }

    public DrawingMatrix[] getDrawingMatrices(){
        return this.drawingMatrices;
    }

    public Rect getBounds(){
        return this.bounds;
    }
}
