package com.panimator.animators.voicevisualizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.panimator.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * Created by ASANDA on 2018/01/12.
 */

public class VisualizerView extends View {
    private static final double NOISE_THRESH_HOLD = 0.2;
    private static final  int padding = 30;
    private static final int TEXT_SIZE = 50;
    private Paint drawingPaint, textPaint, blurPaint;
    private double[] dataSource;

    public VisualizerView(Context context) {
        super(context);
        initializeResources();
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeResources();
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeResources();
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeResources();
    }

    private void initializeResources(){
        drawingPaint = new Paint();
        drawingPaint.setStrokeWidth(4f);
        drawingPaint.setAntiAlias(true);
        drawingPaint.setColor(Color.parseColor("#FFFFFF"));

        blurPaint = new Paint();
        blurPaint.setColor(Color.BLUE);

        textPaint = new Paint();
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);

    }

    public void update(double[] pDataSource){
        dataSource = pDataSource;
        this.invalidate();
    }

    private double sigmoid(double x){
        return ( 1 / (1 + Math.exp(x)));
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int halfHeight = (int)((double)canvas.getHeight() / 2.00);
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm");
   //     canvas.drawText("Log: " + d.format(new Date()), padding, halfHeight + 10 + TEXT_SIZE, textPaint);

        if(dataSource == null){
            return;
        }
        long startTime = System.currentTimeMillis();
        float width = canvas.getWidth(),
                height = canvas.getHeight();
        Shader sweeper = new SweepGradient(width / 2f, (float)halfHeight - 160, new int[]{ Color.parseColor("#FF000C"), Color.parseColor("#ff00e4"), Color.parseColor("#2400ff"), Color.parseColor("#00eaff"), Color.parseColor("#00ff00"), Color.parseColor("#FF6C00"), Color.parseColor("#FF000C")}, null);
        Shader gradient = new LinearGradient((float)padding, 0f, (width - (2 * padding)), height, new int[]{ Color.parseColor("#FF000C"), Color.parseColor("#FF000C"), Color.parseColor("#FF000C"), Color.parseColor("#ff00e4"), Color.parseColor("#2400ff"), Color.parseColor("#00eaff"), Color.parseColor("#00ff00"), Color.parseColor("#FF6C00"), Color.parseColor("#FF000C")}, new float[]{0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f}, Shader.TileMode.MIRROR);

        blurPaint.setShader(sweeper);
        drawingPaint.setShader(sweeper);
        double MAX = getMax(dataSource);

        double barWidth = (((double)canvas.getWidth() - (double)(2 * padding)) / (double)dataSource.length);

        blurPaint.setMaskFilter(new BlurMaskFilter((float)barWidth + 4f, BlurMaskFilter.Blur.NORMAL));

        if(MAX < NOISE_THRESH_HOLD){
            int radius = 150;
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

                canvas.drawLine(x1 + (width / 2), y1 + (halfHeight), x2 + (width / 2) , y2 + halfHeight, drawingPaint);
            }
        }else{
            int radius = 150;
            double conveter = Math.PI / 127;
            for(int pos = 0; pos < 255; pos++) {
                int scaledHeight = (int) (((dataSource[pos] / MAX) * halfHeight) / 4.00);

                double tetha = conveter * pos;
                double cT = Math.cos(tetha);
                double sT = Math.sin(tetha);
                float x1 = (float)(radius * cT);
                float y1 = (float)(radius * sT);
                float x2 = (float)((radius + scaledHeight) * cT);
                float y2 = (float)((radius + scaledHeight) * sT);

                canvas.drawLine(x1 + (width / 2), y1 + halfHeight, x2 + (width / 2) , y2 + halfHeight, drawingPaint);
            }
        }
        Log.i("ContentValues", "onRead_: " + (System.currentTimeMillis() - startTime));
    }



    private double getMax(double[] dataSource) {
        double highest = 0;
        for(int pos = 0; pos < dataSource.length; pos++){
            double num = dataSource[pos];
            if(num > highest){
                highest = num;
            }
        }
        return highest;
    }
}
