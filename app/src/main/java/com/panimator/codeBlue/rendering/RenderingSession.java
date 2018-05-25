package com.panimator.codeBlue.rendering;

import com.panimator.codeBlue.display.Bundle;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by ASANDA on 2018/01/28.
 * for Pandaphic
 */

public abstract class RenderingSession<F> {
    public static final char SUID_SEPARATOR = ':';
    public static final short MAX_FRAMES = 10000;
    private final Renderer renderer;
    private final Bundle bundle;
    private final ArrayList<SessionCallback<F>> sessionCallbacks;
    private int framesRendered;


    public RenderingSession(Renderer pRenderer){
        this(pRenderer, new Bundle());
    }

    public RenderingSession(Renderer pRenderer, Bundle pBundle){
        this.renderer = pRenderer;
        this.bundle = pBundle;
        this.framesRendered = 0;
        this.sessionCallbacks = new ArrayList<>();
    }

    public final void start(){
        this.onStart();
    }

    public final void registerSessionCallback(SessionCallback<F> sessionCallback){
        this.sessionCallbacks.add(sessionCallback);
    }

    public final void pushRender(){
        if(this.getFramesRendered() == 0){
            this.getRenderer().prepare(this);
        }

        this.getRenderer().render(this);
    }


    public final boolean returnFrame(F frame){
        this.framesRendered++;
        for(SessionCallback sessionCallback : this.sessionCallbacks){
            if(sessionCallback != null){
                if(getFramesRendered() < MAX_FRAMES){
                    sessionCallback.onReturnFrame(frame);
                }else {
                     sessionCallback.onMaxFramesReached();
                }
            }
        }
        return (getFramesRendered() < MAX_FRAMES)? onReturnFrame(frame) : false;
    }

    public final boolean saveFrame(F frame, String outputPath){
        return this.onSaveFrame(frame, outputPath);
        
    }

    public final void encode(double frameRate){
        this.encode(frameRate, null);
    }

    public final void encode(double frameRate, String audioPath){
        this.onRequestEncode(this.getBundle().pull(RendererManager.SESSION_KEY_WORKSPACE), audioPath, this.getBundle().pull(RendererManager.SESSION_KEY_OUTPUT_DIRECTORY), frameRate);
    }

    protected final void onRenderingFinished(String outputFile){
        for(SessionCallback sessionCallback : this.sessionCallbacks){
            if(sessionCallback != null){
                sessionCallback.onRenderingFinished(outputFile);
            }
        }
    }

    public final void reset(){
        this.framesRendered = 0;
        this.onRequestReset();
    }

    public final void release(){
        this.onRequestRelease();
    }

    protected final void broadcastError(String message){
        for(SessionCallback<F> sessionCallback : this.getSessionCallbacks()){
            if(sessionCallback != null){
                sessionCallback.onError(message);
            }
        }
    }

    protected abstract void onStart();
    protected abstract void onRequestReset();
    protected abstract void onRequestRelease();
    protected abstract void onRequestEncode(String workspace, String audioPath, String outputDirectory, double frameRate);
    protected abstract boolean onReturnFrame(F frame);
    protected abstract boolean onSaveFrame(F frame, String outputPath);

    public final Renderer getRenderer(){
        return this.renderer;
    }

    public final Bundle getBundle(){
        return this.bundle;
    }

    public final int getFramesRendered(){
        return this.framesRendered;
    }

    public final String getSuid(int index){
        return this.getRenderer().getTitle() +  + index;
    }

    protected final ArrayList<SessionCallback<F>> getSessionCallbacks(){
        return this.sessionCallbacks;
    }

    public interface SessionCallback<F>{
        void onReturnFrame(F frame);
        void onRenderingFinished(String outputFile);
        void onMaxFramesReached();
        void onError(String message);
    }
}
