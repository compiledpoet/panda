package com.panimator.animators.boidcompiler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;
import android.util.Log;

import com.panimator.animation.AndroidAnimationSession;
import com.panimator.animation.AndroidAnimator;
import com.panimator.codeBlue.display.Plane;
import com.panimator.codeBlue.physics.Particle;
import com.panimator.codeBlue.physics.Vector;
import com.panimator.codeBlue.rendering.RenderingSession;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ASANDA on 2018/04/23.
 * for Pandaphic
 */
public class BoidCompiler extends AndroidAnimator<Bitmap> {
    private final int RES_WIDTH = 15, RES_HEIGHT = 15;
    final int PARTICLES_COUNT = 80;
    final Random randomGenerator = new Random();
    private final Paint drawingPaint;
    Bitmap sourceMap;
    Particle[] particles;
    List<Point> availablePoints = new ArrayList<>();

    public BoidCompiler(){
        this.drawingPaint = new Paint();
        drawingPaint.setStrokeWidth(4);
    }

    @Override
    protected String[] getRequiredPermissions() {
        return new String[0];
    }

    @Override
    protected void onPrepare(RenderingSession<Bitmap> renderingSession) {
        String filePath = renderingSession.getBundle().pull(SourceImageCollector.SESSION_KEY_IMAGEPATH);

        Bitmap rawSourceMap = BitmapFactory.decodeFile(filePath);
        Bitmap canvas = renderingSession.getBundle().pull(AndroidAnimationSession.SESSION_KEY_CANVAS);

        sourceMap = Bitmap.createScaledBitmap(rawSourceMap, canvas.getWidth(), canvas.getHeight(), false);


        BitmapShader shader = new BitmapShader(sourceMap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR);
        drawingPaint.setShader(shader);

        int wSize = canvas.getWidth() / RES_WIDTH, hSize = canvas.getHeight() / RES_HEIGHT;
        for(int y = 0; y < RES_HEIGHT; y++){
            for(int x = 0; x < RES_WIDTH; x++){
                int minX = x * wSize, maxX = minX + wSize;
                int minY = y * hSize, maxY = minY + hSize;

                int pX = randomGenerator.nextInt(maxX - minX) + minX;
                int pY = randomGenerator.nextInt(maxY - minY) + minY;
                availablePoints.add(new Point(pX, pY));
            }
        }

        particles = new Particle[PARTICLES_COUNT];
        for(int pos = 0; pos < PARTICLES_COUNT; pos++){
            particles[pos] = new Particle((randomGenerator.nextBoolean())? 0 : canvas.getWidth(), randomGenerator.nextInt(canvas.getHeight()), new Vector(randomGenerator.nextInt(4), randomGenerator.nextInt(4)));
            Point p = availablePoints.get(randomGenerator.nextInt(availablePoints.size()));
            Vector newTarget = new Vector(p.x, p.y);
            particles[pos].setTarget(newTarget);
            availablePoints.remove(p);
        }

    }

    @Override
    protected void onRender(RenderingSession<Bitmap> renderingSession) {

        Bitmap map = renderingSession.getBundle().pull(AndroidAnimationSession.SESSION_KEY_CANVAS);
        Canvas canvas = new Canvas(map);
        canvas.drawColor(Color.parseColor("#2d2d2d"));


        int activeParticles = this.PARTICLES_COUNT;
        while(availablePoints.size() > 0 || activeParticles > 0){
            for(Particle particle : particles){
                if(particle.targetReached()){
                    activeParticles--;
                    if(availablePoints.size() == 0) continue;

                    Point p = availablePoints.get(randomGenerator.nextInt(availablePoints.size()));
                    Vector newTarget = new Vector(p.x, p.y);
                    particle.setTarget(newTarget);
                    availablePoints.remove(p);
                    activeParticles++;
                }
                particle.move();
                canvas.drawLine(particle.getLastX(), particle.getLastY(), particle.getX(), particle.getY(), drawingPaint);
            }

            renderingSession.returnFrame(map);
        }
    }

    @Override
    public Class<? extends Plane> getMainInputCollector() {
        return SourceImageCollector.class;
    }

    @Override
    public String getTitle() {
        return "WireFrame Compiler";
    }
}
