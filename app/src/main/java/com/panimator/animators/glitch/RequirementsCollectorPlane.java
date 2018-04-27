package com.panimator.animators.glitch;

import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.panimator.R;
import com.panimator.animation.AndroidAnimatorManager;
import com.panimator.animation.AndroidPlane;
import com.panimator.codeBlue.BlueKey;
import com.panimator.codeBlue.rendering.RenderingSession;

import java.io.File;

/**
 * Created by ASANDA on 2018/01/18.
 * for Pandaphic
 */

public class RequirementsCollectorPlane extends AndroidPlane implements RenderingSession.SessionCallback, View.OnClickListener {
    public static final BlueKey<String> SESSION_KEY_IMAGE_PATH = new BlueKey<>("SK_IMAGE_PATH");
    public static final BlueKey<String> SESSION_KEY_OVERLAY_TEXT = new BlueKey<>("SK_OVERLAY_TEXT");
    public static final BlueKey<Typeface> SESSION_KEY_OVERLAY_FONT = new BlueKey<>("SK_OVERLAY_FONT");
    private static final int REQ_GLITCH_IMAGE = 251;
    private EditText txtInput;
    private String[] fonts;
    private int counter = 0;

    public RequirementsCollectorPlane(AndroidPlaneListener pPlaneListener, RenderingSession pRenderingSession) {
        super(pPlaneListener, pRenderingSession);
    }

    @Override
    protected View onRequestGui() {
        return this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_MANAGER).inflate(R.layout.glitch_plane_layout);
    }

    @Override
    protected void onCreate() {
        this.getRenderingSession().registerSessionCallback(this);
        this.getGui().findViewById(R.id.btn_glitch_next).setOnClickListener(this);
        this.getGui().findViewById(R.id.btn_glitch_fonts).setOnClickListener(this);
        txtInput = this.getGui().findViewById(R.id.txt_glitch_overlay);
        this.fonts = this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_MANAGER).getLocalFonts();
    }

    @Override
    protected void onStart() {
        this.requestExtraMedia(REQ_GLITCH_IMAGE, AndroidPlane.MEDIA_TYPE_IMAGE);
    }

    @Override
    protected void onReturnMediaRequest(int requestCode, int resultCode, String mediaPath) {
        super.onReturnMediaRequest(requestCode, resultCode, mediaPath);
        Log.i("EGGS", mediaPath);
        if(true){
            getRenderingSession().getBundle().push(SESSION_KEY_IMAGE_PATH, mediaPath);
            BitmapDrawable drawable = (BitmapDrawable)BitmapDrawable.createFromPath(mediaPath);
            this.getGui().setBackground(drawable);
        }
    }


    @Override
    public void onReturnFrame(Object frame) {
        this.updateProgress(this.getRenderingSession().getFramesRendered());
    }

    @Override
    public void onRenderingFinished(String outputFile) {
        this.showAnimationPreview(outputFile);
        this.dismissProgress();
    }

    @Override
    public void onMaxFramesReached() {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_glitch_next){
            this.getRenderingSession().getBundle().push(SESSION_KEY_OVERLAY_TEXT, txtInput.getText().toString())
                    .push(SESSION_KEY_OVERLAY_FONT, this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_MANAGER).getLocalFont(this.fonts[counter]));

            Thread backgroundRunner = new Thread(new Runnable() {
                @Override
                public void run() {
                    getRenderingSession().pushRender();
                    getRenderingSession().encode(8);
                }
            });
            backgroundRunner.start();
            this.showProgress(12 * 19 + 960 * 6);
        }else if(v.getId() == R.id.btn_glitch_fonts){
            counter++;
            if(counter >= this.fonts.length){
                counter = 0;
            }
            String selectedFont = this.fonts[counter];
            this.txtInput.setTypeface(this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_MANAGER).getLocalFont(selectedFont));
        }else{

        }

    }
}
