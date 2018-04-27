package com.panimator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by ASANDA on 2018/01/15.
 */

public class VView extends View {
    private byte[] FFT = null;
    private static final Paint drawingPaint;
    static {
        drawingPaint = new Paint();
        drawingPaint.setColor(Color.RED);
    }

    public VView(Context context) {
        super(context);
    }

    public VView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void Update(byte[] fft){
        this.FFT = fft;
        Log.i("Dewu", "DRAWING");
        this.invalidate();

    }

    public VView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(FFT == null){
            return;
        }

        int barWidth = (int)((double)canvas.getWidth() / FFT.length);
        for(int pos = 0; pos < (int)((double)FFT.length / 2.00) - 1; pos++){
            int real = (FFT[pos * 2]);
            int imagine = FFT[pos * 2 + 1];
            int mag = (int)Math.sqrt(real * real + imagine * imagine);
            Rect rect = new Rect(pos * barWidth, 0, pos * barWidth + barWidth, mag);
            canvas.drawRect(rect, drawingPaint);
        }
    }
}
