package com.panimator.animation;

import android.util.Size;

/**
 * Created by ASANDA on 2018/01/16.
 */

public class ObjectSize {
    private final Size size;
    private double score;

    public ObjectSize(Size pSize){
        this.size = pSize;
    }

    public void setScore(double pScore){
        this.score = pScore;
    }

    public double getScore(){
        return this.score;
    }

    public Size getSize(){
        return this.size;
    }
}
