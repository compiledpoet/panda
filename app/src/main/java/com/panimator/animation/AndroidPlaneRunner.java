package com.panimator.animation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.panimator.R;
import com.panimator.codeBlue.BlueKey;
import com.panimator.codeBlue.Func;
import com.panimator.codeBlue.display.Plane;
import com.panimator.codeBlue.rendering.RenderingSession;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class AndroidPlaneRunner extends AppCompatActivity implements AndroidPlane.AndroidPlaneListener, View.OnClickListener {
    private final static int REQ_PERMISSION_CODE = 18;
    private static final String NULL_MSG = null;
    private int planeRequestCode_IMAGE;
    private RelativeLayout parentView;
    private View progressDialog;
    private TextView txtFeedback;
    private boolean isProgressShown;
    private Stack<AndroidPlane> planeStack;
    private AndroidAnimationSession animationSession;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent callerIntent = this.getIntent();
        if(callerIntent.hasExtra(AndroidAnimatorManager.EXTRA_SESSION_KEY)){
            this.parentView = new RelativeLayout(this);
            this.setContentView(parentView);
            this.mHandler = new Handler();
            this.progressDialog = this.getLayoutInflater().inflate(R.layout.progress_dialog_layout, null, false);
            this.txtFeedback = this.progressDialog.findViewById(R.id.txt_feedback);
            this.progressDialog.setOnClickListener(this);

            this.planeStack = new Stack<>();

            BlueKey<AndroidAnimationSession> sessionKey = new BlueKey(callerIntent.getStringExtra(AndroidAnimatorManager.EXTRA_SESSION_KEY));
            this.animationSession = Func.pullStatic(sessionKey);
            String title = this.animationSession.getRenderer().getTitle();
            if(title != null)
                this.setTitle(title);

            String[] deniedPermission = this.getDeniedPermission(((AndroidAnimator)this.animationSession.getRenderer()).getRequiredPermissions());
            if(deniedPermission.length > 0){
                ActivityCompat.requestPermissions(this, deniedPermission, REQ_PERMISSION_CODE);
            }else{
                if(this.addPlane(this.animationSession.getRenderer().getMainInputCollector(), this.animationSession)){
                    //should call pausePreviousPlane(); but only one plane vele so...;
                    if(!this.resumeTopPlane())
                        this.selfClose(NULL_MSG);
                }else
                    this.selfClose(NULL_MSG);
            }
        }else{
            Toast.makeText(this, "No Session Was Specified.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private final boolean addPlane(Class<? extends Plane> planeClass, RenderingSession pAnimationSession){
        try {
            AndroidPlane plane = (AndroidPlane)planeClass.getConstructor(AndroidPlane.AndroidPlaneListener.class, RenderingSession.class).newInstance(this, pAnimationSession);
            if(plane.isClosing()){
                return false;
            }else{
                this.planeStack.push(plane);
                plane.start();
                return !plane.isClosing();
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return false;
    }

    private final boolean resumeTopPlane(){
        if(this.planeStack.size() > 0){
            AndroidPlane plane = this.planeStack.peek();
            View gui = plane.getGui();
            if(!plane.isClosing() && gui != null){
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

                this.parentView.removeAllViews();
                this.parentView.addView(gui, params);
                this.setupDisplay(plane.getDisplayType());
                plane.onGUIAttached(gui);
                plane.resume();
                return !plane.isClosing();
            }
        }

        return false;
    }

    private final void pausePreviousPlane(){
        if(this.planeStack.size()  > 1){
            AndroidPlane previousPlane = this.planeStack.get(1);
            previousPlane.pause();
        }
    }

    private final void stopTopPlane(){

        if(this.planeStack.size() > 1){
            this.planeStack.pop().stop();
            this.parentView.removeAllViews();
            this.resumeTopPlane();
        }else if(this.planeStack.size() == 1){
            AndroidPlane plane = this.planeStack.get(0);
            plane.stop();
            Log.i("SGB",  this.planeStack.size() + "--|");

            planeStack.clear();
            this.selfClose(NULL_MSG);
        }

    }

    private void selfClose(String msg){
        if(msg != null)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        this.planeStack.clear();
        finish();
    }



    private void setupDisplay(int displayType){
        switch (displayType){
            case AndroidPlane.DISPLAY_TYPE_IMMERSION:
                this.setupWindowForImmersion();
                break;
        }
    }

    private void setupWindowForImmersion() {
        View decorView = this.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    public String[] getDeniedPermission(String[] permissionsNeeded) {
        ArrayList<String> deniedPermission = new ArrayList<>();
        for(String permission : permissionsNeeded){
            if(ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED){
                deniedPermission.add(permission);
            }
        }
        return deniedPermission.toArray(new String[deniedPermission.size()]);
    }

    @Override
    public void onBackPressed() {
        stopTopPlane();
    }

    @Override
    public void onRequestClose(Plane plane, String reason) {
        int planeIndex = planeStack.indexOf(plane);
        if(planeIndex < 0)
            return;

        this.planeStack.pop();
        plane.stop();
        if(planeIndex == 0){
            //Plane is visible to user
            this.parentView.removeAllViews();
            if(this.planeStack.size() > 0){
                this.resumeTopPlane();
            }
        }
    }

    @Override
    public boolean onRequestShowPlane(Class<? extends Plane> plane, RenderingSession renderingSession) {
        addPlane(plane, renderingSession);
        pausePreviousPlane();
        resumeTopPlane();
        return true;
    }

    @Override
    public void showProgress() {
        if(!isProgressShown){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            this.parentView.addView(progressDialog, params);
            this.progressDialog.requestFocus();
            isProgressShown = true;
        }
    }

    @Override
    public void updateProgress(double max, double progress) {
        if(isProgressShown){
            final int percentage = (int)((progress / max) * 100.00);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    txtFeedback.setText("Rendering ( " + percentage + "% )");
                }
            });
        }
    }

    @Override
    public void dismissProgress() {
        if(isProgressShown){
            this.parentView.removeView(this.progressDialog);
            isProgressShown = false;
        }
    }


    @Override
    public void onRequestExternalMedia(int requestCode, int mediaType) {
        switch (mediaType){
            case AndroidPlane.MEDIA_TYPE_IMAGE:
                this.planeRequestCode_IMAGE = requestCode;
                this.getExternalMediaImage();
                break;
            case AndroidPlane.MEDIA_TYPE_AUDIO:
                break;
            case AndroidPlane.MEDIA_TYPE_VIDEO:
                break;
        }
    }

    @Override
    public void onRequestShowAnimationPreview(String animationPath) {
        Intent previewIntent = new Intent(this, AnimationPreview.class);
        previewIntent.putExtra(AnimationPreview.EXTRA_ANIMATION_PATH, animationPath);
        this.startActivity(previewIntent);
    }

    @SuppressLint("ServiceCast")
    @Override
    public <T> T onRequestSealedObject(BlueKey<T> key) {
        if(key.equals(SESSION_KEY_SEALED_CAMERA_MANAGER)){
            return (T)getSystemService(CAMERA_SERVICE);
        }else if(key.equals(SESSION_KEY_SEALED_ROTATION))
            return (T)new Integer(this.getWindowManager().getDefaultDisplay().getRotation());
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri path = result.getUri();
                this.planeStack.get(0).returnMediaRequest(requestCode, resultCode, path.getPath());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQ_PERMISSION_CODE){
            boolean allGranted = true;
            for(int grantResult : grantResults){
                if(grantResult == PackageManager.PERMISSION_DENIED){
                    allGranted = false;
                    break;
                }
            }

            if(allGranted){

            }else{
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setMessage("Unfortunately You Can\'t Use This Animator Since Animators Need All Permissions To Be Granted To Work Properly.")
                        .setCancelable(false)
                        .setTitle("Can\'t Continue")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AndroidPlaneRunner.this.finish();
                            }
                        }).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(this.planeStack.size() > 0){
            this.planeStack.peek().resume();
        }else{
            selfClose(NULL_MSG);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(this.planeStack.size() > 0){
            this.planeStack.peek().pause();
        }else{
            selfClose(NULL_MSG);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.animationSession.release();
        this.planeStack.clear();
    }

    @Override
    public void onClick(View v) {

    }

    private void getExternalMediaImage(){
        Size canvasSize = this.animationSession.getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_SIZE);
        CropImage.activity()
                .setAspectRatio(canvasSize.getWidth(), canvasSize.getHeight())
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }


}
