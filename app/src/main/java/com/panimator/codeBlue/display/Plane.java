package com.panimator.codeBlue.display;

import android.graphics.Camera;
import android.hardware.camera2.CameraDevice;

import com.panimator.codeBlue.rendering.RenderingSession;


/**
 * Created by ASANDA on 2018/01/26.
 * for Pandaphic
 */

public abstract class Plane<T> {
    public static final int REQ_CODE_NONE = -1;
    private final PlaneListener planeListener;
    private final RenderingSession renderingSession;
    private final T Gui;
    private int requestCode;
    private double progressMax;
    private boolean isClosing;

    public Plane(PlaneListener pPlaneListener, RenderingSession pRenderingSession){
        this.isClosing = false;
        this.planeListener = pPlaneListener;
        this.renderingSession = pRenderingSession;
        this.setRequestCode(REQ_CODE_NONE);
        this.Gui = this.onRequestGui();
        if(!this.isClosing())
            this.onCreate();
    }


    public final void start(){
        this.isClosing = false;
        this.onStart();
    }

    public final void resume(){
        this.resume(this.getRequestCode());
    }

    public final void resume(int pResumeCode){
        this.onResume(pResumeCode);
    }

    public final void pause() {
        this.onPause();
    }

    public final void stop(){
        this.onStop();
    }

    public final void requestClose(String reason){
        if(this.isClosing)
            return;

        this.isClosing = true;
        if(this.planeListener != null){
            this.planeListener.onRequestClose(this, reason);
        }
    }

    public final T getGui(){
        return this.Gui;
    }

    public final int getRequestCode(){
        return this.requestCode;
    }

    public final boolean isClosing(){
        return this.isClosing;
    }

    public final void showProgress(double max){
        this.progressMax = max;
        if(this.planeListener != null){
            this.planeListener.showProgress();
        }
    }

    public final void updateProgress(double progress){
        if(this.planeListener != null){
            this.planeListener.updateProgress(this.progressMax, progress);
        }
    }

    public final void dismissProgress(){
        this.progressMax = 0;
        if(this.planeListener != null){
            this.planeListener.dismissProgress();
        }
    }

    public void onGUIAttached(T gui){

    }



    protected abstract T onRequestGui();
    protected abstract void onCreate();

    protected void onStart(){}
    protected void onResume(int requestCode){}
    protected void onPause(){}
    protected void onStop(){}
    protected void setRequestCode(int pRequestCode){
        this.requestCode = pRequestCode;
    }
    protected void startPlane(Class<? extends Plane> plane, RenderingSession renderingSession){
        if(this.getPlaneListener() != null){
            boolean started = this.getPlaneListener().onRequestShowPlane(plane, renderingSession);
            if(started){
                this.pause();
            }
        }
    }

    protected PlaneListener getPlaneListener(){
        return this.planeListener;
    }
    protected RenderingSession getRenderingSession(){
        return this.renderingSession;
    }
    protected String getTitle(){
        return this.getRenderingSession().getRenderer().getTitle();
    }



    public interface PlaneListener{
        void onRequestClose(Plane plane, String reason);
        boolean onRequestShowPlane(Class<? extends Plane> plane, RenderingSession renderingSession);
        void showProgress();
        void updateProgress(double max, double progress);
        void dismissProgress();
    }
}
