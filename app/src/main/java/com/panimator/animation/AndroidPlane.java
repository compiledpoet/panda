package com.panimator.animation;

import android.graphics.Camera;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.panimator.codeBlue.BlueKey;
import com.panimator.codeBlue.display.*;
import com.panimator.codeBlue.rendering.RenderingSession;

/**
 * Created by ASANDA on 2018/01/30.
 * for Pandaphic
 */

public abstract class AndroidPlane extends com.panimator.codeBlue.display.Plane<View> {
    public static final short DISPLAY_TYPE_NORMAL = 0, DISPLAY_TYPE_FULLSCREEN = 1, DISPLAY_TYPE_IMMERSION = 2;
    public static final int MEDIA_TYPE_IMAGE = 0, MEDIA_TYPE_AUDIO = 1, MEDIA_TYPE_VIDEO = 2;
    public static final int REQUEST_RESULT_OK = -1, REQUEST_RESULT_CANCELLED = 0;

    public AndroidPlane(AndroidPlaneListener pPlaneListener, RenderingSession pRenderingSession) {
        super(pPlaneListener, pRenderingSession);
    }

    protected final void requestExtraMedia(int requestCode, int mediaType){
        if(this.getPlaneListener() != null){
            ((AndroidPlaneListener)this.getPlaneListener()).onRequestExternalMedia(requestCode, mediaType);
        }
    }

    protected final void showAnimationPreview(String animationPath){
        if(this.getPlaneListener() != null){
            ((AndroidPlaneListener)this.getPlaneListener()).onRequestShowAnimationPreview(animationPath);
        }
    }

    public final void returnMediaRequest(int requestCode, int resultCode, String mediaPath){
        this.onReturnMediaRequest(requestCode, resultCode, mediaPath);
    }

    public short getDisplayType(){ return DISPLAY_TYPE_NORMAL; }

    protected void onReturnMediaRequest(int requestCode, int resultCode, String mediaPath){}

    protected final void  hideSoftKeyboard(){
        View focusedView = this.getGui().findFocus();
        this.hideSoftKeyboard(focusedView);
    }

    protected final void hideSoftKeyboard(View focusedView){
        if(focusedView != null){
            if(focusedView.hasFocus()){
                InputMethodManager inputMethodManager = this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_MANAGER).getInputMethodManager();
                inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            }
        }
    }


    public interface AndroidPlaneListener extends PlaneListener{
        BlueKey<CameraManager> SESSION_KEY_SEALED_CAMERA_MANAGER = new BlueKey<>("SK_SEALED_CAMERA_MANAGER");
        BlueKey<Integer> SESSION_KEY_SEALED_ROTATION = new BlueKey<>("SK_SEALED_DEVICE_ROTATION");

        void onRequestExternalMedia(int requestCode, int mediaType);
        void onRequestShowAnimationPreview(String animationPath);
        <T> T onRequestSealedObject(BlueKey<T> key);

    }
}
