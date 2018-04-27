package com.panimator.animation;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;

import com.panimator.R;
import com.panimator.codeBlue.BlueKey;
import com.panimator.codeBlue.rendering.RenderingSession;

import org.jcodec.common.DictionaryCompressor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by ASANDA on 2018/04/05.
 * for Pandaphic
 */
public abstract class AndroidCameraExtender extends AndroidPlane{
    private static final int OPEN_RESULT_CLOSING = 0, OPEN_RESULT_TIMEOUT = 1, OPEN_RESULT_OPENING = 2;
    private static final int TIMEOUT = 2500;
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseArray<Integer> DEFAULT_ORIENTATIONS;
    private static final SparseArray<Integer> INVERSE_ORIENTATIONS;
    static {
        DEFAULT_ORIENTATIONS = new SparseArray<>();
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);

        INVERSE_ORIENTATIONS = new SparseArray<>();
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }
    private final Semaphore threadLocker;
    private final CameraManager cameraManager;
    private final TextureView textureView;
    private Handler mHandler;
    private HandlerThread backgroundWorker;
    private String cameraFrontID,
                        cameraBackID,
                            selectedCameraID;
    private boolean isRecording;
    private boolean isCameraOpened;
    private boolean isPreviewing;
    private boolean isMotionCaptureEnabled;
    private CameraDevice openedCameraDevice;
    private int cameraOrientation;
    private Size sizeVideo,
                    sizePreview,
                        sizeImage;
    private CameraDevice.StateCallback cameraCallback;
    private CameraCaptureSession previewCaptureSession, motionCaptureSession;
    private CaptureRequest.Builder previewCaptureBuilder, motionCaptureBuilder;

    private MediaRecorder activeMediaRecorder;
    private ImageReader activeImageReader;

    public AndroidCameraExtender(AndroidPlaneListener pPlaneListener, RenderingSession pRenderingSession) {
        super(pPlaneListener, pRenderingSession);
        this.isRecording = this.isCameraOpened = this.isPreviewing = this.isMotionCaptureEnabled = false;
        this.threadLocker = new Semaphore(1);
        this.cameraManager = pPlaneListener.onRequestSealedObject(AndroidPlaneListener.SESSION_KEY_SEALED_CAMERA_MANAGER);
        this.textureView = this.onRequestSurfaceView();

        try {
            this.initCameraDevices();
            if(this.selectedCameraID != null){
                cameraCallback = new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice camera) {
                        AndroidCameraExtender.this.openedCameraDevice = camera;
                        AndroidCameraExtender.this.isCameraOpened = true;
                        AndroidCameraExtender.this.threadLocker.release();
                        AndroidCameraExtender.this.onCameraOpened();
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {
                        close(camera, "Camera Disconnected.");
                    }

                    @Override
                    public void onError(@NonNull CameraDevice camera, int error) {
                        close(camera, "Couldn't Open Camera: errorCode= " + error);
                    }

                    private void close(CameraDevice cameraDevice, String reason){
                        if(cameraDevice == null)
                            return;
                        AndroidCameraExtender.this.threadLocker.release();
                        cameraDevice.close();
                        AndroidCameraExtender.this.openedCameraDevice = null;
                        AndroidCameraExtender.this.isCameraOpened = false;
                        AndroidCameraExtender.this.requestClose(reason);
                    }
                };
            }else
                this.requestClose("No Front/Back Camera Was Found On This Device.");
        } catch (CameraAccessException e) {
            e.printStackTrace();
            this.requestClose("Couldn't Access The Camera.");
        }
    }

    @SuppressLint("MissingPermission")
    protected final int openCamera(int width, int height) throws InterruptedException, CameraAccessException {
        if(this.isClosing())
            return OPEN_RESULT_CLOSING;

        if(this.isCameraOpened())
            return OPEN_RESULT_OPENING;

        if(!threadLocker.tryAcquire(TIMEOUT, TimeUnit.MILLISECONDS))
            return OPEN_RESULT_TIMEOUT;

        CameraCharacteristics cameraCharacteristics = this.cameraManager.getCameraCharacteristics(this.selectedCameraID);
        StreamConfigurationMap configurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        this.cameraOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

        this.sizeVideo = this.getVideoSize(configurationMap.getOutputSizes(MediaRecorder.class));
        this.sizePreview = this.getOptimalPreviewSize(configurationMap.getOutputSizes(SurfaceTexture.class), width, height, sizeVideo.getWidth() / sizeVideo.getHeight());
        this.activeImageReader = ImageReader.newInstance(sizeVideo.getWidth(), sizeVideo.getHeight(), ImageFormat.JPEG, 1);
        this.activeImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image img = reader.acquireLatestImage();
                if(img != null){
                    ByteBuffer buffer = img.getPlanes()[0].getBuffer();
                    byte[] rawImage = new byte[buffer.capacity()];
                    buffer.get(rawImage);
                    AndroidCameraExtender.this.onImageCaptured(rawImage);
                }
            }
        }, mHandler);

        this.cameraManager.openCamera(selectedCameraID, this.cameraCallback, this.mHandler);
        return OPEN_RESULT_OPENING;
    }
    protected abstract void onCameraOpened();

    protected final void startPreview(){
        if(!this.isCameraOpened() || !this.textureView.isAvailable() || this.isPreviewing())
            return;

        SurfaceTexture surfaceTexture = this.textureView.getSurfaceTexture();

        try {
            surfaceTexture.setDefaultBufferSize(this.sizePreview.getWidth(), this.sizePreview.getHeight());
            this.previewCaptureBuilder = this.openedCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(surfaceTexture);
            this.previewCaptureBuilder.addTarget(previewSurface);

            this.openedCameraDevice.createCaptureSession(Collections.singletonList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    previewCaptureSession = session;
                    AndroidCameraExtender.this.updatePreview();
                    AndroidCameraExtender.this.onPreviewStarted();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    AndroidCameraExtender.this.isPreviewing = false;
                    requestClose("Couldn't Start Preview.");
                }
            }, this.mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        AndroidCameraExtender.this.isPreviewing = true;
    }
    protected abstract void onPreviewStarted();

    protected final void startRecording(final MediaRecorder mediaRecorder){
        if(this.isRecording() || !this.getTextureView().isAvailable())
            return;

        this.stopPreview();
        SurfaceTexture surfaceTexture = this.getTextureView().getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(this.sizePreview.getWidth(), this.sizePreview.getHeight());

        try {
            this.previewCaptureBuilder = this.openedCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            Surface previewSurface = new Surface(surfaceTexture);
            surfaces.add(previewSurface);
            this.previewCaptureBuilder.addTarget(previewSurface);


            surfaces.add(this.activeImageReader.getSurface());
            isMotionCaptureEnabled = true;


            this.activeMediaRecorder = mediaRecorder;
            this.activeMediaRecorder.setVideoSize(this.sizeVideo.getWidth(), this.sizeVideo.getHeight());
            int rotation = ((AndroidPlaneListener)this.getPlaneListener()).onRequestSealedObject(AndroidPlaneListener.SESSION_KEY_SEALED_ROTATION);
            switch(this.cameraOrientation){
                case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                    this.activeMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                    break;
                case SENSOR_ORIENTATION_INVERSE_DEGREES:
                    this.activeMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                    break;
            }

            try {
                this.activeMediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Surface recordSurface = this.activeMediaRecorder.getSurface();
            surfaces.add(recordSurface);
            this.previewCaptureBuilder.addTarget(recordSurface);

            Log.i("GJYGU", "1");
            this.openedCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    Log.i("GJYGU", "2");
                    AndroidCameraExtender.this.previewCaptureSession = session;
                    isPreviewing = true;
                    AndroidCameraExtender.this.updatePreview();
                    activeMediaRecorder.start();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            AndroidCameraExtender.this.onRecordingStarted();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    AndroidCameraExtender.this.isRecording = false;
                    requestClose("Couldn't Configure Camera For Recording.");
                }
            }, this.mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        AndroidCameraExtender.this.isRecording = true;
    }
    protected abstract void onRecordingStarted();

    protected final void captureMotionImage(){
        if(!this.isMotionCaptureEnabled)
            return;

        try {
            this.motionCaptureBuilder = this.openedCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_VIDEO_SNAPSHOT);
            this.motionCaptureBuilder.addTarget(this.activeImageReader.getSurface());
            this.motionCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, this.cameraOrientation);
            this.previewCaptureSession.capture(this.motionCaptureBuilder.build(), null, this.mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    protected final void captureStillImage(){}
    protected abstract void onImageCaptured(byte[] rawImage);

    protected final void stopRecording(){
        if(!this.isRecording())
            return;

        this.activeMediaRecorder.stop();
        this.activeMediaRecorder.reset();
        this.previewCaptureSession.close();
        this.previewCaptureSession = null;
        this.isRecording = false;
        this.isMotionCaptureEnabled = false;
        this.isPreviewing = false;

    }

    protected final void stopPreview(){
        if(this.previewCaptureSession == null)
            return;

        this.previewCaptureSession.close();
        this.previewCaptureSession = null;
        this.isPreviewing = false;
    }

    protected final void closeCamera(){
        try {
            this.threadLocker.acquire();
            if(this.isRecording()){
                this.stopRecording();
            }else if(this.isPreviewing){
                this.stopPreview();
            }

            if(this.openedCameraDevice != null){
                this.openedCameraDevice.close();
                this.openedCameraDevice = null;
            }
            this.isCameraOpened = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    private void updatePreview(){
        if(!this.isPreviewing())
            return;

        try{
            this.previewCaptureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            this.previewCaptureSession.setRepeatingRequest(this.previewCaptureBuilder.build(), null, this.mHandler);
        }catch(CameraAccessException e){
            e.printStackTrace();
        }
    }




    private Size getOptimalPreviewSize(Size[] options, int width, int height, double aspectRatio){
        List<Size> possibleSize = new ArrayList<>();
        for(int pos = 0; pos < options.length; pos++){
            Size option  = options[pos];
            if(option.getHeight() * aspectRatio == option.getWidth() && option.getHeight() >= height && option.getWidth() >= width)
                possibleSize.add(option);
        }

        if(possibleSize.size() > 0){
            return Collections.min(possibleSize, new Comparator<Size>() {
                @Override
                public int compare(Size o1, Size o2) {
                    return Long.signum((long) o1.getWidth() * o1.getHeight() -
                            (long) o2.getWidth() * o2.getHeight());
                }
            });
        }else
            return options[0];
    }
    private Size getVideoSize(Size[] options){
        final double aspectRatio = 0.75;
        for(int pos = 0; pos < options.length;pos++){
            Size size = options[pos];
            if(size.getHeight() * aspectRatio == size.getWidth() && size.getWidth() <= 1080)
                return size;
        }
        return options[options.length - 1];
    }

    public boolean isRecording(){
        return this.isRecording;
    }
    public boolean isCameraOpened(){ return this.isCameraOpened; }
    public boolean isPreviewing(){ return  this.isPreviewing; }
    public boolean isMotionCaptureEnabled(){ return this.isMotionCaptureEnabled; }

    private void stopBackgroundWorker(){
        if(this.backgroundWorker == null)
            return;

        this.backgroundWorker.quitSafely();
        try{
            this.backgroundWorker.join();
            this.backgroundWorker = null;
            this.mHandler = null;
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
    private void startBackgroundWorker(){
        this.backgroundWorker = new HandlerThread("AndroidCameraExtender");
        this.backgroundWorker.start();
        this.mHandler = new Handler(backgroundWorker.getLooper());
    }

    private final void initCameraDevices() throws CameraAccessException {
        String[] cameraDevices = this.cameraManager.getCameraIdList();
        for(int pos = 0; pos < cameraDevices.length; pos++){
            String cameraID = cameraDevices[pos];
            CameraCharacteristics cameraCharacteristics = this.cameraManager.getCameraCharacteristics(cameraID);
            int lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
            if(lensFacing == CameraCharacteristics.LENS_FACING_FRONT){
                this.selectedCameraID = this.cameraFrontID = cameraID;
            }else if(lensFacing == CameraCharacteristics.LENS_FACING_BACK){
                this.cameraBackID = cameraID;
                if(this.selectedCameraID == null)
                    this.selectedCameraID = cameraID;
            }
        }
    }

    @Override
    protected void onResume(int requestCode) {
        super.onResume(requestCode);
        this.startBackgroundWorker();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.closeCamera();
        this.stopBackgroundWorker();
    }


    protected abstract TextureView onRequestSurfaceView();
    public final  TextureView getTextureView(){
        return this.textureView;
    }
}
