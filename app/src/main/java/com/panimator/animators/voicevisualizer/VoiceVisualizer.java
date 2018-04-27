package com.panimator.animators.voicevisualizer;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.util.Log;

import com.panimator.R;
import com.panimator.animation.AndroidAnimationSession;
import com.panimator.animation.AndroidAnimator;
import com.panimator.animation.AndroidAnimatorManager;
import com.panimator.codeBlue.display.Plane;
import com.panimator.codeBlue.rendering.RenderingSession;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

/**
 * Created by ASANDA on 2018/01/12.
 for Pandaphic

 */

public class VoiceVisualizer extends AndroidAnimator<Bitmap> {
    private static final double NOISE_THRESH_HOLD = 0.2;
    private static final  int padding = 80;
    private static final int TEXT_SIZE = 30;
    private int titleMargin = 0;
    private String title;
    private Paint drawingPaint, textPaint, blurPaint;
    private Canvas drawingCanvas;
    private Bitmap backgroundPattern;

    private void initializeResources(RenderingSession renderingSession){
        drawingPaint = new Paint();
        drawingPaint.setStrokeWidth(4f);
        drawingPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);

        blurPaint = new Paint();

        Bitmap canvas = renderingSession.getBundle().pull(AndroidAnimationSession.SESSION_KEY_CANVAS);
        this.drawingCanvas = new Canvas(canvas);
        float width = canvas.getWidth(),
                height = canvas.getHeight();
        Shader sweeper = new SweepGradient(width / 2f, height / 2f - 160, new int[]{ Color.parseColor("#FF000C"), Color.parseColor("#ff00e4"), Color.parseColor("#2400ff"), Color.parseColor("#00eaff"), Color.parseColor("#00ff00"), Color.parseColor("#FF6C00"), Color.parseColor("#FF000C")}, null);

        blurPaint.setShader(sweeper);
        drawingPaint.setShader(sweeper);
    }

    @Override
    protected String[] getRequiredPermissions() {
        return new String[]{ Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    }

    @Override
    protected void onPrepare(RenderingSession<Bitmap> renderingSession) {
        initializeResources(renderingSession);
        AndroidAnimatorManager animatorManager = renderingSession.getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_MANAGER);
        backgroundPattern = Bitmap.createScaledBitmap(animatorManager.decodeBitmap(R.drawable.pattern_background_tee), drawingCanvas.getWidth(), drawingCanvas.getHeight(), false);
        title = "● " + renderingSession.getBundle().pull(VoiceCollectorPlane.SESSION_KEY_TITLE);
        Rect bounds = new Rect();
        textPaint.getTextBounds(title,0, title.length(), bounds);
        this.titleMargin = (drawingCanvas.getWidth() / 2) - (bounds.width() / 2);
    }

    @Override
    protected void onRender(RenderingSession<Bitmap> renderingSession) {
        double[] dataSource = renderingSession.getBundle().pull(VoiceCollectorPlane.SESSION_KEY_FFT);


        Bitmap canvas = renderingSession.getBundle().pull(AndroidAnimationSession.SESSION_KEY_CANVAS);
        drawingCanvas = new Canvas(canvas);
        int margin = 70;
        Rect visualBackground = new Rect(margin, margin, 500 + margin, 500 + margin);
        Rect titleBacgronud = new Rect(margin, visualBackground.bottom, 500 + margin, visualBackground.bottom + 60);
        int visualBackgrondColor = Color.parseColor("#212121"),
                titleBackgroundColor = Color.parseColor("#cc7831"),
                titleColor = Color.parseColor("#212121");


        //●
        refreshCanvas(drawingCanvas, Color.WHITE);
        drawingCanvas.drawBitmap(backgroundPattern, 0, 0, null);
        textPaint.setColor(visualBackgrondColor);
        drawingCanvas.drawRect(visualBackground, textPaint);
        textPaint.setColor(titleBackgroundColor);
        drawingCanvas.drawRect(titleBacgronud, textPaint);
        textPaint.setColor(titleColor);
        float titleBottom = visualBackground.bottom + (textPaint.getTextSize() / 2f) + (titleBacgronud.height() / 2f);
        drawingCanvas.drawText(title, titleMargin, titleBottom, textPaint);
        double MAX = getMax(dataSource);
        Log.i(TAG, "onRender: " + MAX);
        int width =  drawingCanvas.getWidth();
        int halfHeight = (int)((double)drawingCanvas.getHeight() / 2.00);

        double barWidth = (((double)drawingCanvas.getWidth() - (double)(2 * padding)) / (double)dataSource.length);

        blurPaint.setMaskFilter(new BlurMaskFilter((float)barWidth + 4f, BlurMaskFilter.Blur.NORMAL));
        int radius = 100;
        if(MAX < NOISE_THRESH_HOLD){

            double conveter = Math.PI / 127;
            for(int pos = 0; pos < dataSource.length; pos += 2) {
                int scaledHeight = 2;

                double tetha = conveter * pos;
                double cT = Math.cos(tetha);
                double sT = Math.sin(tetha);
                float x1 = (float)(radius * cT);
                float y1 = (float)(radius * sT);
                float x2 = (float)((radius + scaledHeight) * cT);
                float y2 = (float)((radius + scaledHeight) * sT);

                drawingCanvas.drawLine(x1 + (width / 2), y1 + (halfHeight - 160), x2 + (width / 2) , y2 + halfHeight - 160, drawingPaint);
            }
            renderingSession.returnFrame(canvas);
        }else{
            double conveter = Math.PI / 127;
            double SCALE_TO = ((visualBackground.width() - radius * 2) / 2) - 10;
            for(int pos = 0; pos < 255; pos++) {
                int scaledHeight = (int) ((dataSource[pos] / MAX) * SCALE_TO);

                double tetha = conveter * pos;
                double cT = Math.cos(tetha);
                double sT = Math.sin(tetha);
                float x1 = (float)(radius * cT);
                float y1 = (float)(radius * sT);
                float x2 = (float)((radius + scaledHeight) * cT);
                float y2 = (float)((radius + scaledHeight) * sT);

                drawingCanvas.drawLine(x1 + (width / 2), y1 + (halfHeight - 160), x2 + (width / 2) , y2 + halfHeight - 160, drawingPaint);
            }
            renderingSession.returnFrame(canvas);
        }


    }

    @Override
    public Class<? extends Plane> getMainInputCollector() {
        return VoiceCollectorPlane.class;
    }

    @Override
    public String getTitle() {
        return "Voice Visualizer";
    }


    private double getMax(double[] dataSource) {
        double highest = 0;
        for (double num : dataSource) {
            if (num > highest) {
                highest = num;
            }
        }
        return highest;
    }

}
