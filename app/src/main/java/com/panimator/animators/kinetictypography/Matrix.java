package com.panimator.animators.kinetictypography;

/**
 * Created by ASANDA on 2018/01/01.
 */

public class Matrix {
    private short Width, Height;

    public Matrix(short size){
        this.Width = this.Height = size;
    }

    public void setWidth(short pWidth){ this.Width = pWidth; }
    public void setHeight(short pHeight){ this.Height = pHeight; }

    public short getWidth(){ return this.Width; }
    public short getHeight(){ return this.Height; }
}
