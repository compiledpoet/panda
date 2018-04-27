package com.panimator.animation;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.util.Size;

import com.panimator.codeBlue.BlueKey;
import com.panimator.codeBlue.Func;
import com.panimator.codeBlue.display.Bundle;
import com.panimator.codeBlue.rendering.Renderer;
import com.panimator.codeBlue.rendering.RenderingSession;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by ASANDA on 2018/01/26.
 * for Pandaphic
 */

public class AndroidAnimationSession extends RenderingSession<Bitmap>{
    public static final String FRAME_NAME_PREFIX = "frame-";
    public static final String FRAME_COMPRESSION_FORMAT = ".jpg";
    public static final BlueKey<Bitmap> SESSION_KEY_CANVAS = new BlueKey<>("SK_CANVAS");

    public AndroidAnimationSession(Renderer<Bitmap> pRenderer) {
       this(pRenderer, new Bundle());
    }

    public AndroidAnimationSession(Renderer<Bitmap> pRenderer, Bundle pBundle) {
        super(pRenderer, pBundle);
    }

    @Override
    protected void onStart() {
        Size animationSize = this.getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_SIZE);
        this.getBundle().push(SESSION_KEY_CANVAS, Bitmap.createBitmap(animationSize.getWidth(), animationSize.getHeight(), Bitmap.Config.ARGB_8888));
    }

    @Override
    protected void onRequestReset() {
        File workspaceFile = new File(this.getBundle().pull(AndroidAnimatorManager.SESSION_KEY_WORKSPACE));
        try {
            this.deleteDirectory(workspaceFile);
            if(!workspaceFile.mkdir()){
                this.broadcastError("Couldn\'t Create Workspace");
            }
        } catch (IOException e) {
            this.broadcastError(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onRequestRelease() {
        this.reset();
        this.getBundle().clear();
    }

    @Override
    protected boolean onReturnFrame(Bitmap frame) {
        if(frame == null)
            return true;

        String frameFileName = generateOutputFileName();
        return this.saveFrame(frame, this.getBundle().pull(AndroidAnimatorManager.SESSION_KEY_WORKSPACE) + File.separator + frameFileName);

    }

    public String generateOutputFileName() {
        return FRAME_NAME_PREFIX + Func.padInt(this.getFramesRendered(), '0', String.valueOf(MAX_FRAMES).length()) + FRAME_COMPRESSION_FORMAT;
    }

    @Override
    protected boolean onSaveFrame(Bitmap frame, String outputPath){
        long startTime = System.currentTimeMillis();
        drawWatermark(frame);
        Log.i(TAG, "onRead: " + (System.currentTimeMillis() - startTime));

        long startTime2 = System.currentTimeMillis();
        FileOutputStream fileOS = null;
        boolean returned = false;
        try {
            fileOS = new FileOutputStream(outputPath);
            frame.compress(Bitmap.CompressFormat.JPEG, 100, fileOS);
            returned = true;
            Log.i(TAG, "onRead2: " + (System.currentTimeMillis() - startTime2));
        } catch (FileNotFoundException e) {
            this.broadcastError(e.getMessage());
            e.printStackTrace();
            returned = false;
        }finally {
            if(fileOS != null){
                try {
                    fileOS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return returned;
    }


    @Override
    protected void onRequestEncode(String workspace, String audioPath, String outputDirectory, double frameRate) {
        String animationName = "Pandaphic_" + Func.generateRandomHex(this.getRenderer().getTitle());
        final String outputFilePath = outputDirectory + File.separator + animationName + ".mp4";
        FFMpegHelper ffMpegHelper = this.getBundle().pull(AndroidAnimatorManager.SESSION_KEY_FFMPEG);

        String audioCommand = " -i " + audioPath;
        if(audioPath == null){
            audioCommand = "";
        }

            String command = "-r " + frameRate + " -i " + workspace + File.separator + FRAME_NAME_PREFIX + "%05d" + FRAME_COMPRESSION_FORMAT + audioCommand + " -c:v libx264 -vf fps=25 -pix_fmt yuv420p " + outputFilePath;
            ffMpegHelper.execute(command, new FFMpegHelper.FFMpegListener() {
                @Override
                public void onCommandFinished() {
                    for(SessionCallback<Bitmap> sessionCallback : AndroidAnimationSession.this.getSessionCallbacks()){
                        if(sessionCallback != null){
                            sessionCallback.onRenderingFinished(outputFilePath);
                        }
                    }
                    AndroidAnimationSession.this.reset();
                }
            });

    }

    private void drawWatermark(Bitmap bitmap){
        Canvas canvas = new Canvas(bitmap);
        Bitmap watermark = this.getBundle().pull(AndroidAnimatorManager.SESSION_KEY_WATER_MARK);
        canvas.drawBitmap(watermark, canvas.getWidth() - watermark.getWidth() - AndroidAnimatorManager.WATERMARK_MARGIN, canvas.getHeight() - watermark.getHeight() - AndroidAnimatorManager.WATERMARK_MARGIN, null);
    }

    private void deleteDirectory(File directoryFile) throws IOException {

        if (directoryFile.isDirectory()) {
            String[] children = directoryFile.list();
            for (String child : children) {
                File childFile = new File(directoryFile, child);
                if(childFile.isDirectory()){
                    deleteDirectory(childFile);
                }else{
                    if(!childFile.delete()){
                        throw  new IOException("Couldn\'t Delete File.");
                    }
                }
            }
        }
        if (!directoryFile.delete()) {
            throw new IOException("Couldn\'t Delete File.");
        }
    }
}
