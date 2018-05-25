package com.panimator.animators.flipmotion;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.media.MediaRecorder;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.panimator.R;
import com.panimator.animation.AndroidAnimatorManager;
import com.panimator.animation.AndroidCameraExtender;
import com.panimator.codeBlue.BlueKey;
import com.panimator.codeBlue.Func;
import com.panimator.codeBlue.rendering.RenderingSession;

import java.io.File;

/**
 * Created by ASANDA on 2018/05/03.
 * for Pandaphic
 */
public class VideoCollectorPlane extends AndroidCameraExtender implements View.OnClickListener, MediaRecorder.OnInfoListener, RenderingSession.SessionCallback {
    public final static BlueKey<String> SESSION_KEY_OUTPUT_PATH = new BlueKey<>("SK_OUTPUT_PATH");
    private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if(!VideoCollectorPlane.this.isCameraOpened()){
                try {
                    VideoCollectorPlane.this.openCamera(width, height);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    private MediaRecorder mediaRecorder;


    public VideoCollectorPlane(AndroidPlaneListener pPlaneListener, RenderingSession pRenderingSession) {
        super(pPlaneListener, pRenderingSession);
        this.mediaRecorder = new MediaRecorder();
        pRenderingSession.registerSessionCallback(this);
        this.initMediaRecorder();
    }

    private void initMediaRecorder() {
        this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        this.mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        String animationName = "Pandaphic_" + Func.generateRandomHex(this.getRenderingSession().getRenderer().getTitle());
        String outputFilePath = this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_OUTPUT_DIRECTORY) + File.separator + animationName + ".mp4";
        this.getRenderingSession().getBundle().push(SESSION_KEY_OUTPUT_PATH, outputFilePath);
        this.mediaRecorder.setOutputFile(outputFilePath);
        this.mediaRecorder.setAudioEncodingBitRate(10000000);
        this.mediaRecorder.setVideoFrameRate(30);
        this.mediaRecorder.setMaxDuration(7000);
        this.mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        this.mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        this.mediaRecorder.setOnInfoListener(this);
    }

    @Override
    protected void onCameraOpened() {
        if(!this.isPreviewing()){
            this.startPreview();
        }
    }

    @Override
    protected void onPreviewStarted() {

    }

    @Override
    protected void onRecordingStarted() {

    }

    @Override
    protected void onImageCaptured(byte[] rawImage) {

    }

    @Override
    protected TextureView onRequestSurfaceView() {
        TextureView ttv = this.getGui().findViewById(R.id.ttv_video_preview);
        ttv.setSurfaceTextureListener(this.surfaceTextureListener);
        return ttv;
    }

    @Override
    public short getDisplayType() {
        return DISPLAY_TYPE_IMMERSION;
    }

    @Override
    protected View onRequestGui() {
        View parentView = this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_MANAGER).inflate(R.layout.flipper_layout);
        Button btn_record = parentView.findViewById(R.id.btn_record_video);
        btn_record.setOnClickListener(this);
        return parentView;
    }

    @Override
    protected void onCreate() {

    }

    @Override
    protected void onResume(int requestCode) {
        super.onResume(requestCode);
        if(!this.isCameraOpened()){
            if(this.getTextureView().isAvailable()){
                try {
                    this.openCamera(this.getTextureView().getWidth(), this.getTextureView().getHeight());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    this.requestClose("Couldn't Open Camera. Error:" + e.getMessage());
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }else{
                this.getTextureView().setSurfaceTextureListener(this.surfaceTextureListener);
            }
        }else{
            if(!this.isPreviewing()){
                this.startPreview();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(this.isRecording()){
            this.stopRecording();
        }

        if(this.isCameraOpened()){
            this.closeCamera();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_record_video:
                btnRecordClick();
                break;
        }
    }

    private void btnRecordClick() {
        if(this.isRecording()){
            this.stopRecording();
        }else{
            this.startRecording(this.mediaRecorder);
        }
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        switch (what){
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
            {
                this.stopRecording();
                this.showProgress(100);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        VideoCollectorPlane.this.getRenderingSession().pushRender();
                        //VideoCollectorPlane.this.getRenderingSession().encode(1);
                }
                }).start();
            }break;
        }
    }

    @Override
    public void onReturnFrame(Object frame) {

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
