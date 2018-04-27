package com.panimator.animators.voicevisualizer;

import android.view.View;

import com.panimator.animation.AndroidAnimatorManager;
import com.panimator.animation.AndroidPlane;
import com.panimator.codeBlue.rendering.RenderingSession;

/**
 * Created by ASANDA on 2018/02/18.
 * for Pandaphic
 */

public class MusicPickerPlane extends AndroidPlane {

    public MusicPickerPlane(AndroidPlaneListener pPlaneListener, RenderingSession pRenderingSession) {
        super(pPlaneListener, pRenderingSession);
    }

    @Override
    protected View onRequestGui() {
        return this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_MANAGER).inflate();
    }

    @Override
    protected void onCreate() {

    }
}
