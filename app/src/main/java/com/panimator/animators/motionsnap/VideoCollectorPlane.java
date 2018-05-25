package com.panimator.animators.motionsnap;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.panimator.R;
import com.panimator.animation.AndroidAnimationSession;
import com.panimator.animation.AndroidAnimatorManager;
import com.panimator.animation.AndroidPlane;
import com.panimator.animation.AndroidCameraExtender;
import com.panimator.codeBlue.Func;
import com.panimator.codeBlue.rendering.RenderingSession;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

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
    private static final int SNAP_TIME = 5000;
    private final android.os.Handler mHandler;

    private final MediaRecorder mediaRecorder;
    private ImageView imgSnap;
    private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if(!isCameraOpened()){
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



    public VideoCollectorPlane(AndroidPlaneListener pPlaneListener, RenderingSession pRenderingSession) {
        super(pPlaneListener, pRenderingSession);
        this.mHandler = new android.os.Handler();
        this.mediaRecorder = new MediaRecorder();
        this.initMediaRecorder();
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
    protected void onImageCaptured(final byte[] rawImage) {
        Log.i("COMP-S", "Cap1");
        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                Bitmap map = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length);
                Log.i("COMP-S", "" + (map == null));

                imgSnap.setImageBitmap(map);

            }
        });


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
    protected View onRequestGui() {
        RelativeLayout parent =  (RelativeLayout)this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_MANAGER).inflate(R.layout.motion_snap_layout);

        this.imgSnap = parent.findViewById(R.id.img_snap);

        Button btn = parent.findViewById(R.id.btn_record_video);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording()){
                    VideoCollectorPlane.this.startRecording(mediaRecorder);
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            VideoCollectorPlane.this.captureMotionImage();
                            Log.i("COMP-S", "Cap");
                        }
                    }, SNAP_TIME);
                }
                else
                    VideoCollectorPlane.this.stopRecording();
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
            if(this.getTextureView().isAvailable()) {
                try {
                    this.openCamera(this.getTextureView().getWidth(), this.getTextureView().getHeight());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }else{
                this.getTextureView().setSurfaceTextureListener(this.surfaceTextureListener);
            }
        }else{
            if(!this.isPreviewing())
                this.startPreview();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected TextureView onRequestSurfaceView() {
        return this.getGui().findViewById(R.id.ttv_video_preview);
    }


//
//    @Override
//    protected void onImageCaptured(byte[] rawImage) {
//        try{
//            String outputPath = Environment.getExternalStorageDirectory() + File.separator + "Snap.jpg";
//            FileOutputStream FOS = new FileOutputStream(outputPath);
//            FOS.write(rawImage);
//            FOS.close();
//        }catch (FileNotFoundException e){
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}
