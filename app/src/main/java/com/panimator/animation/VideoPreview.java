package com.panimator.animation;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by ASANDA on 2018/04/05.
 * for Pandaphic
 */
public class VideoPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera cameraDevice;
    boolean created = false;

    public VideoPreview(Context context) {
        super(context);
        init();
    }

    public VideoPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public VideoPreview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(this.cameraDevice != null){
            try {
                this.cameraDevice.setPreviewDisplay(mHolder);
                this.cameraDevice.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        created = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.refresh(this.cameraDevice);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void init() {
        this.mHolder = this.getHolder();
        this.mHolder.addCallback(this);
    }

    public void refresh(Camera PcameraDevice) {
        if(this.mHolder.getSurface() == null)
            return;

       try {
           cameraDevice.stopPreview();
       } catch(Exception e){
           e.printStackTrace();
       }

       this.setCameraDevice(PcameraDevice);
    }

    private void setCameraDevice(Camera pCameraDevice){
        this.cameraDevice = pCameraDevice;
        if(this.created){
            try {
                this.cameraDevice.setPreviewDisplay(mHolder);
                this.cameraDevice.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
