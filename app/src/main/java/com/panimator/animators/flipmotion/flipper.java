package com.panimator.animators.flipmotion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;

import com.panimator.animation.AndroidAnimationSession;
import com.panimator.animation.AndroidAnimator;
import com.panimator.animation.AndroidAnimatorManager;
import com.panimator.animation.FFMpegHelper;
import com.panimator.codeBlue.display.Plane;
import com.panimator.codeBlue.rendering.RenderingSession;

import org.opencv.core.Mat;

import java.io.File;

/**
 * Created by ASANDA on 2018/05/03.
 * for Pandaphic
 */
public class flipper extends AndroidAnimator {
    boolean framesExtracted;
    File framesPath;
    @Override
    protected String[] getRequiredPermissions() {
        return new String[0];
    }

    @Override
    protected void onPrepare(RenderingSession renderingSession) {
        String outpuPath = renderingSession.getBundle().pull(VideoCollectorPlane.SESSION_KEY_OUTPUT_PATH);
        FFMpegHelper ffMpegHelper = renderingSession.getBundle().pull(AndroidAnimatorManager.SESSION_KEY_FFMPEG);
        String workstation = renderingSession.getBundle().pull(AndroidAnimatorManager.SESSION_KEY_WORKSPACE);
        this.framesPath = new File(workstation, "frames");
        if(!framesPath.exists())
            framesPath.mkdir();

        String command = "-i " + outpuPath + " -vf fps=1 " + framesPath.getAbsolutePath() + File.separator + "frame_%04d.jpg";
        this.framesExtracted = false;
        ffMpegHelper.execute(command, new FFMpegHelper.FFMpegListener() {
            @Override
            public void onCommandFinished() {
                flipper.this.framesExtracted = true;
            }
        });
    }

    @Override
    protected void onRender(RenderingSession renderingSession) {
        while(!framesExtracted);

        Paint p = new Paint();
        p.setColor(Color.parseColor("#212121"));
        String workstation = renderingSession.getBundle().pull(AndroidAnimatorManager.SESSION_KEY_WORKSPACE);
        String[] frames = framesPath.list();
        Bitmap map = renderingSession.getBundle().pull(AndroidAnimationSession.SESSION_KEY_CANVAS);
        Canvas canvas = new Canvas(map);

        double aspectRatio = map.getWidth() /  (double)map.getHeight();

        if(frames.length > 1){
            Bitmap previousFlip = map.copy(Bitmap.Config.ARGB_8888, true);
            Bitmap firstFrame = BitmapFactory.decodeFile(framesPath.getAbsolutePath() + File.separator + frames[0]);
            int width, height;
            if(firstFrame.getHeight() * aspectRatio > firstFrame.getWidth()){
                width = firstFrame.getWidth();
                height = (int)(width / aspectRatio);
            }else{
                height = firstFrame.getHeight();
                width = (int)(height * aspectRatio);
            }

            int cWidth = map.getWidth() / 2;
            int cHeight = map.getHeight() / 2;

            Bitmap lastPad = Bitmap.createBitmap(map.getWidth(), map.getHeight(), Bitmap.Config.ARGB_8888),
                    nextPad = Bitmap.createBitmap(map.getWidth(), map.getHeight(), Bitmap.Config.ARGB_8888);

            Canvas lCanvas = new Canvas(lastPad),
                    nCanvas = new Canvas(nextPad);



            Rect topHalf = new Rect(0,0, map.getWidth(), cHeight),
                    bottomHalf = new Rect(0, cHeight, map.getWidth(), map.getHeight());

            Bitmap nextFlip = Bitmap.createScaledBitmap(Bitmap.createBitmap(firstFrame, (firstFrame.getWidth() - width) / 2, (firstFrame.getHeight() - height) / 2, width,height), map.getWidth(), map.getHeight(), false);
            lCanvas.drawBitmap(nextFlip, topHalf, topHalf, null);
            lCanvas.drawRect(bottomHalf, p);

            nCanvas.drawRect(topHalf, p);
            nCanvas.drawBitmap(nextFlip, bottomHalf, bottomHalf, null);
            nextFlip.recycle();


            double r = map.getHeight() / 2.0, MS = 80;
            double W = (map.getWidth() - MS);

                int tetha = 0, velocity = 0, accelaration = -5;
                while(true){
                    canvas.drawColor(Color.parseColor("#000000"));

                    Camera camera = new Camera();
                    camera.save();
                    camera.translate(0, 0, (float)r);
                    if(tetha >= -90 && tetha <= 0){
                        camera.rotateX(tetha);
                    }else if(tetha < 90 && tetha > 0){

                    }else{
                        break;
                    }

                    Matrix matrix = new Matrix();
                    matrix.toString();
                    camera.getMatrix(matrix);
                    camera.restore();


                    matrix.preTranslate(-cWidth, -cHeight);
                    matrix.postTranslate(cWidth, cHeight);

                    canvas.drawBitmap(lastPad, topHalf, topHalf, null);
                    canvas.drawBitmap(nextPad, matrix, null);
                    canvas.drawBitmap(lastPad, bottomHalf, bottomHalf, null);
                    renderingSession.returnFrame(map);

                    velocity += accelaration;
                    tetha += velocity;

                }

        }

    }

    @Override
    public Class<? extends Plane> getMainInputCollector() {
        return VideoCollectorPlane.class;
    }

    @Override
    public String getTitle() {
        return "Flip Motion";
    }
}
