package com.panimator.animators.motionsnap;

import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.panimator.animation.AndroidAnimator;
import com.panimator.codeBlue.display.Plane;
import com.panimator.codeBlue.rendering.RenderingSession;

/**
 * Created by ASANDA on 2018/01/06.
 */

public class MotionSnap extends AndroidAnimator<Bitmap> {

    @Override
    protected void onPrepare(RenderingSession<Bitmap> renderingSession) {

    }

    @Override
    protected void onRender(RenderingSession<Bitmap> renderingSession) {

    }

    @Override
    public Class<? extends Plane> getMainInputCollector() {
        return VideoCollectorPlane.class;
    }

    @Override
    public String getTitle() {
        return "Motion Snap";
    }


    @Override
    protected String[] getRequiredPermissions() {
        return new String[0];
    }
}
