package com.panimator.animators.kinetictypography;

import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by ASANDA on 2017/12/25.
 */

public class DrawingMatrix {
    public static final short MATRIX_COUNT = 12;
    private final Matrix[] Matrices;
    private short firstRowHeight , secondRowHeight;


    public DrawingMatrix(short matrixSize){
        this.firstRowHeight = this.secondRowHeight = matrixSize;
        this.Matrices = new Matrix[MATRIX_COUNT];
        for(int pos = 0; pos < MATRIX_COUNT; pos++){
            Matrices[pos] = new Matrix(matrixSize);
        }
    }

    public Matrix get(int pos){
        Log.i(TAG, pos + "POSITION");
        return (pos < Matrices.length)? Matrices[pos] : null;
    }
    public void setFirstRowHeight(short pFirstRowHeight){ this.firstRowHeight = pFirstRowHeight; }
    public void setSecondRowHeight(short pSecondRowHeight){ this.secondRowHeight = pSecondRowHeight; }

    public short getFirstRowHeight(){ return this.firstRowHeight; }
    public short getSecondRowHeight(){ return this.secondRowHeight; }

    public int getMatrixStart(short pos){
        if(pos < 0){ return -1; }
        short halfMatrixCount = (short)(MATRIX_COUNT / 2.00);
        if(pos == 0 || pos == halfMatrixCount){
            return 0;
        }

        short prevPos = (short)(pos - 1);
        return getMatrixStart(prevPos) + get(prevPos).getWidth();
    }

    public  int getMatrixTop(short pos){
        if(pos < 0){ return -1; }
        short halfMatrixCount = (short)(MATRIX_COUNT / 2.00);
        if(pos < halfMatrixCount){
            return 0;
        }else{
            int aboveMatrix = pos -  halfMatrixCount;
            return get(aboveMatrix).getHeight();
        }
    }

    public int getWidth() {
        return getMatrixStart((short)5) + get(5).getWidth();
    }

    public int getHeight() {
        return firstRowHeight + secondRowHeight;
    }
}
