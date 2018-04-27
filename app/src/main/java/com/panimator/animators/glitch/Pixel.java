package com.panimator.animators.glitch;

import android.graphics.Color;

/**
 * Created by ASANDA on 2018/01/18.
 * for Pandaphic
 */

public class Pixel {
    private final int color;

    public Pixel(int pColor){
        this.color = pColor;
    }

    public int Red(){
        return Color.red(color);
    }

    public int Green(){
        return Color.green(color);
    }

    public int Blue(){
        return Color.blue(color);
    }
}
