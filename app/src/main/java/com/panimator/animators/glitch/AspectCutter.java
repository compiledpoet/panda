package com.panimator.animators.glitch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ASANDA on 2018/02/24.
 * for Pandaphic
 */

public class AspectCutter extends View {
    public static final int GRABBER_RADIUS = 20;
    private static final int GRID_BAR_THICKNESS =  7;
    private static final int DOWN_CORNER_TOP_LEFT = 0, DOWN_CORNER_TOP_RIGHT = 1, DOWN_CORNER_BOTTOM_LEFT = 2, DOWN_CORNER_BOTTOM_RIGHT = 3;
    private Paint drawingPaint;
    private float aspectRatio = 0.75f;
    private PointF downLocation;


    public AspectCutter(Context context) {
        super(context);
        initialize();
    }

    public AspectCutter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public AspectCutter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public AspectCutter(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private final void initialize(){
        this.drawingPaint = new Paint();
        drawingPaint.setColor(Color.WHITE);
        drawingPaint.setStrokeWidth(GRID_BAR_THICKNESS);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(0, 0, GRABBER_RADIUS, drawingPaint);
        canvas.drawCircle(canvas.getWidth(), 0, GRABBER_RADIUS, drawingPaint);
        canvas.drawCircle(canvas.getWidth(), canvas.getHeight(), GRABBER_RADIUS, drawingPaint);
        canvas.drawCircle(0, canvas.getHeight(), GRABBER_RADIUS, drawingPaint);

        float quarterHeight = (canvas.getHeight() /  3.0f),
                quarterWidth = (canvas.getWidth() / 3.0f);

        for(int pos = 0; pos < 4; pos++){
            float currentHeight = quarterHeight * pos;
            canvas.drawLine(0 , currentHeight, canvas.getWidth(), currentHeight, drawingPaint);
        }

        for(int pos = 0; pos < 4; pos++){
            float currentWidth = quarterWidth * pos;
            canvas.drawLine(currentWidth , 0, currentWidth, canvas.getHeight(), drawingPaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downLocation = new PointF(event.getX(), event.getY());
                return true;
            case MotionEvent.ACTION_MOVE:
                if(downLocation != null){
                    int corner = getDownCorner();
                }
                return true;
            case MotionEvent.ACTION_UP:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec),
                measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec),
                heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int scaledWidth = (int)(measuredHeight * aspectRatio);
        setMeasuredDimension(scaledWidth, measuredHeight);
    }

    private final int getDownCorner(){
        if(downLocation != null){

        }else{
            return -1;
        }
        return -1;
    }
}
