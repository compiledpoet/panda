package com.panimator.animation;

import android.graphics.Color;

/**
 * Created by ASANDA on 2018/01/25.
 * for Pandaphic
 */

public class BlueColor {
    private final int color;

    public BlueColor(int pColor){
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
