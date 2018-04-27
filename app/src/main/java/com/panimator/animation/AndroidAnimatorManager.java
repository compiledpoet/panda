package com.panimator.animation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.panimator.R;
import com.panimator.animators.boidcompiler.BoidCompiler;
import com.panimator.animators.glitch.Glitcher;
import com.panimator.animators.kinetictypography.KineticTypography;
import com.panimator.animators.motionsnap.MotionSnap;
import com.panimator.animators.slowmo.SlowMo;
import com.panimator.animators.voicevisualizer.VoiceVisualizer;
import com.panimator.codeBlue.BlueKey;
import com.panimator.codeBlue.Func;
import com.panimator.codeBlue.rendering.Renderer;
import com.panimator.codeBlue.rendering.RendererManager;
import com.panimator.codeBlue.rendering.RenderingSession;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by ASANDA on 2018/01/09.
 */

public class AndroidAnimatorManager extends RendererManager<AndroidAnimator<Bitmap>, AndroidAnimationSession> {
    public static final BlueKey<AndroidAnimatorManager> SESSION_KEY_ANIMATION_MANAGER = new BlueKey<>("SK_MANAGER");
    public static final BlueKey<FFMpegHelper> SESSION_KEY_FFMPEG = new BlueKey<>("SK_FFMPEG");
    public static final BlueKey<Bitmap> SESSION_KEY_WATER_MARK = new BlueKey<>("SK_WATERMARK");
    public static final String EXTRA_SESSION_KEY = "ES_Key";
    private static final String RENDERED_DIRECTORY_NAME = "Pandaphic";
    private static final String REGISTERED_SESSION = "regSession";
    private static final String FONTS_PATH = "fonts";
    private static final String FONTS_SYSTEM_DEFAULT = ":/\\";
    private final Context context;


    public AndroidAnimatorManager(Context pContext){
        this.context = pContext;
    }

    @Override
    protected AndroidAnimator<Bitmap>[] loadRenders() {
        return new AndroidAnimator[]{ new KineticTypography(), new VoiceVisualizer(), new Glitcher(), new SlowMo(), new MotionSnap(), new BoidCompiler()};
    }

    @Override
    protected AndroidAnimationSession onRequestNewSession(AndroidAnimator<Bitmap> render) {
        AndroidAnimationSession session = new AndroidAnimationSession(render);
        session.getBundle().push(SESSION_KEY_FFMPEG, FFMpegHelper.getInstance(context))
                .push(SESSION_KEY_ANIMATION_MANAGER, this);
        return session;
    }

    @Override
    protected String getRenderWorkspace(AndroidAnimator<Bitmap> render) {
        File renderWorkspaceFie = new File(Environment.getExternalStorageDirectory(), "Panda_" + render.getTitle().replace(' ', '_') +  "_" + Arrays.asList(this.getLocalRenders()).indexOf(render));
        if(!renderWorkspaceFie.exists()){
            if(!renderWorkspaceFie.mkdir()){
                return null;
            }
        }
        return renderWorkspaceFie.getAbsolutePath();
    }

    @Override
    protected String getRenderedDirectory() {
        File renderedDirectoryFile = new File(Environment.getExternalStorageDirectory(), RENDERED_DIRECTORY_NAME);
        if(!renderedDirectoryFile.exists()){
            if(!renderedDirectoryFile.mkdir()){
                return null;
            }
        }
        return renderedDirectoryFile.getAbsolutePath();
    }

    @Override
    protected void registerSession(String Suid) {
        SharedPreferences sharedPreferences = this.getSharedPreferences();
        sharedPreferences.edit().putString(REGISTERED_SESSION, Suid).apply();
    }

    @Override
    protected void onRequestStartRenderingSession(AndroidAnimationSession session) {
        int watermarkSize = (int)(session.getBundle().pull(SESSION_KEY_ANIMATION_SIZE).getWidth() * WATERMARK_SCALE_RATIO);
        session.getBundle().push(SESSION_KEY_WATER_MARK, Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.context.getResources(), R.mipmap.ic_launcher), watermarkSize, watermarkSize, false));

        BlueKey<AndroidAnimationSession> sessionKey = Func.pushStatic(session);
        Intent inputCollectorIntent = new Intent(this.context, AndroidPlaneRunner.class);
        inputCollectorIntent.putExtra(EXTRA_SESSION_KEY, sessionKey.toString());
        session.start();
        this.context.startActivity(inputCollectorIntent);
    }

    @Override
    protected void deregisterSession(String Suid) {
        SharedPreferences sharedPreferences = this.getSharedPreferences();
        sharedPreferences.edit().remove(REGISTERED_SESSION);
    }

    @Override
    protected void onRequestStopSession(AndroidAnimationSession session) {
        session.release();
    }

    @Override
    protected AndroidAnimationSession onRequestRegisteredSession() {
        SharedPreferences sharedPreferences = this.getSharedPreferences();
        String suid = sharedPreferences.getString(REGISTERED_SESSION, null);
        if(suid == null){
            return null;
        }

        String[] suidParts = suid.split(String.valueOf(RenderingSession.SUID_SEPARATOR));
        try{
            int index = Integer.parseInt(suidParts[1]);
            return this.createRenderingSession(this.getLocalRenders()[index], ANIMATION_SIZE_1080p);
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
        return null;
    }

    public String[] getLocalFonts(){
        try {
            return this.context.getAssets().list(FONTS_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[]{ FONTS_SYSTEM_DEFAULT };
    }

    public Typeface getLocalFont(String name){
        if(name.equals(FONTS_SYSTEM_DEFAULT)){
            return Typeface.DEFAULT;
        }
        return Typeface.createFromAsset(this.context.getAssets(), FONTS_PATH + File.separator + name);
    }

    public View inflate(){
        return new View(context);
    }

    public View inflate(int res){
        return LayoutInflater.from(this.context).inflate(res, null, false);
    }

    public Bitmap decodeBitmap(int res){
        return BitmapFactory.decodeResource(this.context.getResources(), res);
    }

    public InputMethodManager getInputMethodManager(){
        return (InputMethodManager)(this.context.getSystemService(Context.INPUT_METHOD_SERVICE));
    }

    private SharedPreferences getSharedPreferences(){
        return this.context.getSharedPreferences(this.context.getPackageName(), Context.MODE_PRIVATE);
    }
}
