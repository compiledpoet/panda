package com.panimator.animators.glitch;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ASANDA on 2018/02/07.
 * for Pandaphic
 */

public class Panner extends View {
    private Point downPosition = null;

    public Panner(Context context) {
        super(context);
    }

    public Panner(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Panner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Panner(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downPosition = new Point((int)event.getX(), (int)event.getY());
                break;
            case MotionEvent.ACTION_HOVER_MOVE:
                if(downPosition != null){
                    Log.i("mMoving", downPosition.toString());
                }
                break;
            case MotionEvent.ACTION_UP:
                downPosition = null;
                break;
        }
        return false;
    }
}
