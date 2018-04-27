package com.panimator.animators.slowmo;

import android.widget.TextView;

/**
 * Created by ASANDA on 2018/01/22.
 * for Pandaphic
 */

public class SlowMoBundle {
    private final String FramesDirectory;
    private final double Rate;
    private final int StartTime;

    public SlowMoBundle(String framesDirectory, double rate, int startTime){
        this.FramesDirectory = framesDirectory;
        this.Rate = rate;
        this.StartTime = startTime;
    }
}
