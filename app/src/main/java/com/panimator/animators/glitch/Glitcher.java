package com.panimator.animators.glitch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.panimator.animation.AndroidAnimationSession;
import com.panimator.animation.AndroidAnimator;
import com.panimator.animation.AndroidAnimatorManager;
import com.panimator.animation.Interpol;
import com.panimator.animation.TextLine;
import com.panimator.codeBlue.Func;
import com.panimator.codeBlue.display.Plane;
import com.panimator.codeBlue.rendering.RenderingSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Random;

/**
 * Created by ASANDA on 2018/01/18.
 * for Pandaphic
 */

public class Glitcher extends AndroidAnimator<Bitmap> {
    private static final double FRAME_RATE = 12.00;
    private static final int ANIMATION_DURATION = 9;
    private static final double GLITCH_INTENSITY =  0.0479;
    private static final int[][] SWITCHERS = new int[][] { new int[] { 0, 1, 1 }, new int[] { 1, 0, 1 }, new int[] { 1, 1, 0 }, new int[] { 0, 0, 1 }, new int[] { 1, 0, 0 }, new int[] { 0, 1, 0} };
    private final Paint drawingPaint = new Paint();
    private final int TEXT_PADDING = 50;

    @Override
    protected void onPrepare(RenderingSession<Bitmap> renderingSession) {
        Typeface font = renderingSession.getBundle().pull(RequirementsCollectorPlane.SESSION_KEY_OVERLAY_FONT);
        this.drawingPaint.setTypeface(font);
    }

    @Override
    protected void onRender(RenderingSession<Bitmap> renderingSession) {
        String workspace = renderingSession.getBundle().pull(AndroidAnimatorManager.SESSION_KEY_WORKSPACE);
        String glitchSampleDirectory = createSamplesDirectory(workspace);
        String imagePath = renderingSession.getBundle().pull(RequirementsCollectorPlane.SESSION_KEY_IMAGE_PATH),
                overlayText = renderingSession.getBundle().pull(RequirementsCollectorPlane.SESSION_KEY_OVERLAY_TEXT);
        Bitmap canvas = renderingSession.getBundle().pull(AndroidAnimationSession.SESSION_KEY_CANVAS);
        Canvas drawingCanvas = new Canvas(canvas);
        String g = "onRead";
        Bitmap originalImage = BitmapFactory.decodeFile(imagePath);
        originalImage = Bitmap.createScaledBitmap(originalImage, drawingCanvas.getWidth(), drawingCanvas.getHeight(), false);
        int offset = (int)((double)originalImage.getWidth() * GLITCH_INTENSITY);
        drawingPaint.setTextSize(drawingCanvas.getHeight() * 0.05f);

        TextLine[] lines = TextLine.getLines(overlayText, drawingPaint, drawingCanvas.getWidth(), TEXT_PADDING);
        Rect shadow = new Rect(0, 0, originalImage.getWidth(), originalImage.getHeight());
        for(int pos = 0; pos < SWITCHERS.length; pos++)
        {
            int[] switcher = SWITCHERS[pos];
            int hasRed = switcher[0], hasGreen = switcher[1], hasBlue = switcher[2];
            Bitmap channelMap = Bitmap.createBitmap(originalImage.getWidth(), originalImage.getHeight(), Bitmap.Config.ARGB_8888);
            for(int y = 0; y < channelMap.getHeight(); y++)
            { renderingSession.returnFrame(null);
                Log.i("EGGS", pos + "" + originalImage.getWidth() + "|" + originalImage.getHeight());
                for(int x = 0; x < channelMap.getWidth(); x++)
                {
                    Pixel pixelColor = new Pixel(originalImage.getPixel(x, y));
                    Pixel  glitchColor = new Pixel(Color.rgb(pixelColor.Red() * hasRed, pixelColor.Green() * hasGreen, pixelColor.Blue() * hasBlue));
                    int originalX = x + offset;
                    Pixel originalColor;
                    if(originalX >= originalImage.getWidth()){
                        originalColor = new Pixel(Color.TRANSPARENT);
                    }else{
                        originalColor = new Pixel(originalImage.getPixel(x + offset, y));
                    }

                    int red = (originalColor.Red() * Math.abs(hasRed - 1)) + (glitchColor.Red() * hasRed);
                    int green = (originalColor.Green() * Math.abs(hasGreen - 1)) + (glitchColor.Green() * hasGreen);
                    int blue = (originalColor.Blue() * Math.abs(hasBlue - 1)) + (glitchColor.Blue() * hasBlue);
                    channelMap.setPixel(x, y, Color.rgb(red, green, blue));
                }
            }
            drawText(channelMap, shadow, lines);

            renderingSession.saveFrame(channelMap, glitchSampleDirectory + File.separator + "glitch" + pos + ".png");
            //channelMap.Save(@"C:\Users\ASANDA\Desktop\ge\channel" + hasRed + "." + hasGreen + "." + hasBlue + ".png");
        }

        drawText(originalImage, shadow, lines);
        String staticGlitchPath = Environment.getExternalStorageDirectory() + File.separator + "static.png";
        renderingSession.saveFrame(originalImage, staticGlitchPath);

        int numFrames = (int)(FRAME_RATE * ANIMATION_DURATION);
        String[] glitches = new File(glitchSampleDirectory).list();
        Random randomGenerator = new Random();
        int counter = 0;
        for(int pos = 0; pos < numFrames; pos++){
            String glitchPath = glitchSampleDirectory + File.separator  + glitches[randomGenerator.nextInt(glitches.length)],
                    animationPath = workspace + File.separator + AndroidAnimationSession.FRAME_NAME_PREFIX + Func.padInt(pos, '0', String.valueOf(RenderingSession.MAX_FRAMES).length()) + AndroidAnimationSession.FRAME_COMPRESSION_FORMAT;

            renderingSession.returnFrame(null);
            Log.i("EGGS", pos + ":F+");
            if(counter >= 8){
                CopyFile(glitchPath, animationPath);
            }else{
                CopyFile(staticGlitchPath, animationPath);
            }
            counter = (counter >= 11)? 0 : counter + 1;
        }
    }

    @Override
    public Class<? extends Plane> getMainInputCollector() {
        return RequirementsCollectorPlane.class;
    }

    @Override
    public String getTitle() {
        return "Glitch Effect";
    }

    private void drawText(Bitmap map, Rect shadow, TextLine[] lines) {
        Canvas drawingCanvas = new Canvas(map);
        drawingPaint.setColor(Color.parseColor("#000000"));
        drawingPaint.setAlpha(160);
        drawingCanvas.drawRect(shadow, drawingPaint);
        drawingPaint.setColor(Color.WHITE);
        drawingPaint.setAlpha(255);
        int linesHalfHeight = (int)((drawingPaint.getTextSize() * lines.length) / 2.00);
        int mapHalfWidth = (int)((double)map.getWidth() / 2.00);
        int mapHalfHeight = (int)((double)map.getHeight() / 2.00);
        int top = mapHalfHeight - linesHalfHeight, spacing = 3;

        for(TextLine line : lines){
            int halfLineWidth = (int)((double)line.getSize().getWidth() /  2.00);
            top += (int)drawingPaint.getTextSize() + spacing;
            drawingCanvas.drawText(line.getText(), mapHalfWidth - halfLineWidth, top, drawingPaint);
        }
        //drawingPaint.setTextSize(preSize);
    }

    private String createSamplesDirectory(String workspace) {
        File SamplesDirectory = new File(workspace, "Sample");
        if(!SamplesDirectory.exists()){
            SamplesDirectory.mkdir();
        }
        return SamplesDirectory.getAbsolutePath();
    }

    private void CopyFile(String originalFile, String destinationFile){
        FileChannel inChannel = null, outChannel = null;
        try {
            inChannel = new FileInputStream(originalFile).getChannel();
            outChannel = new FileOutputStream(destinationFile).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (inChannel != null) {
                    inChannel.close();
                }
                if(outChannel != null){
                    outChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String[] getRequiredPermissions() {
        return new String[0];
    }
}
