package com.panimator.animators.glitch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by ASANDA on 2018/02/07.
 * for Pandaphic
 */

public class Cropper extends RelativeLayout {
    private static final double aspectRatio = 0.75;
    private static final int padding = 20;
    private Bitmap imageToCrop = null;
    private String imagePath;

    public Cropper(Context context) {
        super(context);
        this.initializePanView();
    }

    public Cropper(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializePanView();
    }

    public Cropper(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializePanView();
    }

    public Cropper(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializePanView();
    }

    private void initializePanView(){

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(imageToCrop == null){
            return;
        }


    }

    public void setImagePath(String path){
        this.imagePath = path;
        double width = this.getWidth(),
                height = this.getHeight();
        this.imageToCrop = BitmapFactory.decodeFile(path);
        if(this.imageToCrop.getWidth() > width){
            double scaleRatio = (width / (double)this.imageToCrop.getWidth());
            this.imageToCrop = Bitmap.createScaledBitmap(this.imageToCrop, (int)width, (int)(height * scaleRatio), false);
        }else{
            double scaleRatio = (height / (double)this.imageToCrop.getHeight());
            this.imageToCrop = Bitmap.createScaledBitmap(this.imageToCrop, (int)(width * scaleRatio), (int)height, false);
        }
    }


}
