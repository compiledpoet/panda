package com.panimator.animators.boidcompiler;

import android.view.View;
import android.widget.RelativeLayout;

import com.panimator.R;
import com.panimator.animation.AndroidAnimatorManager;
import com.panimator.animation.AndroidPlane;
import com.panimator.animation.AndroidPlaneRunner;
import com.panimator.codeBlue.BlueKey;
import com.panimator.codeBlue.rendering.RenderingSession;

/**
 * Created by ASANDA on 2018/04/23.
 * for Pandaphic
 */
public class SourceImageCollector extends AndroidPlane implements RenderingSession.SessionCallback {
    public static final BlueKey<String> SESSION_KEY_IMAGEPATH = new BlueKey<>("SK_IMAGE_PATH");
    private static final int REQ_SOURCEIMAGE = 121;


    public SourceImageCollector(AndroidPlaneListener pPlaneListener, RenderingSession pRenderingSession) {
        super(pPlaneListener, pRenderingSession);
    }

    @Override
    protected View onRequestGui() {
        View parentView = this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_MANAGER).inflate(R.layout.boid_compiler_layout);
        return parentView;
    }

    @Override
    protected void onCreate() {
        this.getRenderingSession().registerSessionCallback(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.requestExtraMedia(REQ_SOURCEIMAGE, MEDIA_TYPE_IMAGE);
    }

    @Override
    protected void onReturnMediaRequest(int requestCode, int resultCode, String mediaPath) {
        this.getRenderingSession().getBundle().push(SESSION_KEY_IMAGEPATH, mediaPath);
        new Thread(new Runnable() {
            @Override
            public void run() {
                getRenderingSession().pushRender();
                getRenderingSession().encode(65);
            }
        }).start();
        this.showProgress(1000);

        super.onReturnMediaRequest(requestCode, resultCode, mediaPath);
    }


    @Override
    public void onReturnFrame(Object frame) {
        this.updateProgress(this.getRenderingSession().getFramesRendered() + 1);
    }

    @Override
    public void onRenderingFinished(String outputFile) {
        this.showAnimationPreview(outputFile);
    }

    @Override
    public void onMaxFramesReached() {

    }

    @Override
    public void onError(String message) {

    }
}
