package com.panimator.animators.motionsnap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.panimator.R;
import com.panimator.animation.AndroidAnimationSession;
import com.panimator.animation.AndroidAnimatorManager;
import com.panimator.animation.AndroidPlane;
import com.panimator.animation.AndroidCameraExtender;
import com.panimator.codeBlue.Func;
import com.panimator.codeBlue.rendering.RenderingSession;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Handler;


/**
 * Created by ASANDA on 2018/01/17.
 */

public class VideoCollectorPlane extends AndroidCameraExtender {
    private static int REQ_VIDEO = 122;
    private final MediaRecorder mediaRecorder;
    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if(!VideoCollectorPlane.this.isCameraOpened()){
                try {
                    VideoCollectorPlane.this.openCamera(width, height);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    VideoCollectorPlane.this.requestClose("Couldn't Open Camera. Error:" + e.getMessage());
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

    public VideoCollectorPlane(AndroidPlaneListener pPlaneListener, RenderingSession pRenderingSession) {
        super(pPlaneListener, pRenderingSession);
        this.mediaRecorder = new MediaRecorder();
        this.initMediaRecorder();
    }
    private void initMediaRecorder() {
        this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        this.mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        String animationName = "Pandaphic_" + Func.generateRandomHex(this.getRenderingSession().getRenderer().getTitle());
        final String outputFilePath = this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_OUTPUT_DIRECTORY) + File.separator + animationName + ".mp4";
        this.mediaRecorder.setOutputFile(outputFilePath);
        this.mediaRecorder.setAudioEncodingBitRate(10000000);
        this.mediaRecorder.setVideoFrameRate(30);
        this.mediaRecorder.setMaxDuration(7000);
        this.mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        this.mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

    }



    @Override
    protected TextureView onRequestSurfaceView() {
        TextureView textureView = this.getGui().findViewById(R.id.ttv_video_display);
        textureView.setSurfaceTextureListener(this.textureListener);
        return textureView;
    }

    @Override
    protected View onRequestGui() {
        RelativeLayout parent =  (RelativeLayout)this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_MANAGER).inflate(R.layout.motion_snap_layout);
        final Button btn = parent.findViewById(R.id.btn_record_video);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(VideoCollectorPlane.this.isRecording()){
                    VideoCollectorPlane.this.stopRecording();
                    btn.setText("Start Record");
                }else{
                    VideoCollectorPlane.this.startRecording(VideoCollectorPlane.this.mediaRecorder);
                    btn.setText("Stop Recording");
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            VideoCollectorPlane.this.captureMotionImage();
                        }
                    }, 5000);
                }
            }
        });
        return  parent;
    }

    @Override
    protected void onCreate() {

    }

    @Override
    public short getDisplayType() {
        return DISPLAY_TYPE_IMMERSION;
    }

    @Override
    protected void onStart() {
        super.onStart();
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
                this.getTextureView().setSurfaceTextureListener(this.textureListener);
            }
        }else{
            if(!this.isPreviewing()){
                this.startPreview();;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        try{
            String outputPath = Environment.getExternalStorageDirectory() + File.separator + "Snap.jpg";
            FileOutputStream FOS = new FileOutputStream(outputPath);
            FOS.write(rawImage);
            FOS.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
