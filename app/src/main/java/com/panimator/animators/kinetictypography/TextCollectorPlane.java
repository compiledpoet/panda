package com.panimator.animators.kinetictypography;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.panimator.R;
import com.panimator.animation.AndroidAnimatorManager;
import com.panimator.animation.AndroidPlane;
import com.panimator.codeBlue.BlueKey;
import com.panimator.codeBlue.display.Bundle;
import com.panimator.codeBlue.rendering.RenderingSession;


/**
 * Created by ASANDA on 2018/01/17.
 */

public class TextCollectorPlane extends AndroidPlane implements View.OnClickListener, View.OnLayoutChangeListener, RenderingSession.SessionCallback<Bitmap>{
    public static final BlueKey<String[]> SESSION_KEY_TEXT = new BlueKey<>("SK_TEXT");
    public static final BlueKey<Typeface> SESSION_KEY_FONT = new BlueKey<>("SK_FONT");
    public static final BlueKey<int[]> SESSION_KEY_COLORS = new BlueKey<>("SK_COLORS");
    private static final double FRAME_RATE = 2;
    private EditText edtInput;
    private String[] fonts;
    private int[][] colors;
    private short fontsCounter, colorsCounter;
    private double progress = 0;


    public TextCollectorPlane(AndroidPlaneListener pPlaneListener, RenderingSession pRenderingSession) {
        super(pPlaneListener, pRenderingSession);
        this.getRenderingSession().registerSessionCallback(this);
    }

    @Override
    protected void onCreate() {
        this.edtInput = (EditText) getGui().findViewById(R.id.edt_kinetic_input);
        TextView txtFonts = (TextView) getGui().findViewById(R.id.txt_fonts);
        ImageView imgColor = (ImageView) getGui().findViewById(R.id.img_color);
        Button btnDone = (Button) getGui().findViewById(R.id.btn_done);

        txtFonts.setOnClickListener(this);
        imgColor.setOnClickListener(this);
        btnDone.setOnClickListener(this);

        this.edtInput.addOnLayoutChangeListener(this);
    }

    @Override
    protected View onRequestGui() {
        return this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_MANAGER).inflate(R.layout.kinetic_input_layout);
    }

    @Override
    protected void onStart() {
        this.fonts = this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_MANAGER).getLocalFonts();
        this.colors = new int[][]{new int[]{Color.parseColor("#d3cce3"), Color.parseColor("#e9e4f0")}, new int[]{Color.parseColor("#Fc4a1a"), Color.parseColor("#f7b733")}, new int[]{Color.parseColor("#30e8bf"), Color.parseColor("#ff8235")}, new int[]{Color.parseColor("#2196f3"), Color.parseColor("#f44336")}, new int[]{Color.parseColor("#da4453"), Color.parseColor("#89216b")}, new int[]{Color.parseColor("#11998e"), Color.parseColor("#38ef7d")}, new int[]{Color.parseColor("#fc5c7d"), Color.parseColor("#6a82fb")}, new int[]{Color.parseColor("#ff5f6d"), Color.parseColor("#ffc371")}};
        this.fontsCounter = 0;
        this.colorsCounter = 0;
        setNextFont();
        setNextColor();
    }



    private void setNextFont() {
        if(fontsCounter >= fonts.length || fontsCounter < 0){
            fontsCounter = 0;
        }
        Typeface font = this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_MANAGER).getLocalFont(fonts[fontsCounter]);
        edtInput.setTypeface(font);
        fontsCounter++;
    }

    private void setNextColor() {
        colorsCounter++;
        if(colorsCounter >= colors.length || colorsCounter < 0){
            colorsCounter = 0;
        }
        int[] color = colors[colorsCounter];
        Shader gradient = new LinearGradient(0, 0, edtInput.getWidth(), edtInput.getHeight(), color, null, Shader.TileMode.REPEAT);
        edtInput.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        edtInput.getPaint().setShader(gradient);
        edtInput.invalidate();
    }




    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.txt_fonts:
                setNextFont();
                break;
            case R.id.img_color:
                setNextColor();
                break;
            case R.id.btn_done:
                Animate();
                break;
        }

    }

    private void Animate() {
        String[] words = edtInput.getText().toString().split(" ");
        Bundle bundle = this.getRenderingSession().getBundle();
        bundle.push(SESSION_KEY_TEXT, words)
                .push(SESSION_KEY_FONT, edtInput.getTypeface())
                .push(SESSION_KEY_COLORS, colors[colorsCounter]);
        Thread renderingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                TextCollectorPlane.this.getRenderingSession().pushRender();
                TextCollectorPlane.this.getRenderingSession().encode(FRAME_RATE);

            }
        });
        renderingThread.start();
        hideSoftKeyboard();
        this.showProgress(words.length + KineticTypography.END_PADDING_FRAMES + 1);
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        colorsCounter--;
        setNextColor();
    }

    @Override
    public void onReturnFrame(Bitmap frame) {
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
}
