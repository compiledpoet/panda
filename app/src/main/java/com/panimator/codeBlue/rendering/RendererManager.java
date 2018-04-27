package com.panimator.codeBlue.rendering;

import android.util.Size;

import com.panimator.codeBlue.BlueKey;

import java.util.Arrays;

/**
 * Created by ASANDA on 2018/01/27.
 * for Pandaphic
 */

public abstract class RendererManager<R extends Renderer, S extends RenderingSession> {
    public static final short ANIMATION_SIZE_1080p = 0;
    public static final BlueKey<Size> SESSION_KEY_ANIMATION_SIZE = new BlueKey<>("SK_ANIMATION_SIZE");
    public static final BlueKey<String> SESSION_KEY_WORKSPACE = new BlueKey<>("SK_WORKSPACE");
    public static final BlueKey<String> SESSION_KEY_OUTPUT_DIRECTORY = new BlueKey<>("SK_OUTPUT_DIRECTORY");
    public static final double WATERMARK_SCALE_RATIO = 0.10;
    public static final int WATERMARK_MARGIN = 40;
    private static final Size[] animationSizes = new Size[]{ new Size(640, 960) };
    private final R[] renders;

    protected RendererManager(){
        this.renders = this.loadRenders();
    }

    public final S createRenderingSession(R render, short animationSize){
        S session = this.onRequestNewSession(render);
        String renderWorkspace = this.getRenderWorkspace(render);
        String renderedDirectory = this.getRenderedDirectory();
        if(renderWorkspace == null || renderedDirectory == null){
            return null;
        }

        session.getBundle().push(SESSION_KEY_WORKSPACE, renderWorkspace)
                .push(SESSION_KEY_OUTPUT_DIRECTORY, renderedDirectory)
                .push(SESSION_KEY_ANIMATION_SIZE, animationSizes[animationSize]);

        return session;
    }

    public final void startRenderingSession(S session){
        this.registerSession(session.getSuid(Arrays.asList(this.renders).indexOf(session.getRenderer())));
        this.onRequestStartRenderingSession(session);
    }

    public final void stopRenderingSession(S session){
        this.deregisterSession(session.getSuid(Arrays.asList(this.renders).indexOf(session.getRenderer())));
        this.onRequestStopSession(session);
    }

    public final S getRegisteredSession(){
        return this.onRequestRegisteredSession();
    }

    public final boolean isRunningASession(){
        return (this.getRegisteredSession() != null);
    }

    protected abstract R[] loadRenders();
    protected abstract S onRequestNewSession(R render);
    protected abstract String getRenderWorkspace(R render);
    protected abstract String getRenderedDirectory();
    protected abstract void registerSession(String Suid);
    protected abstract void onRequestStartRenderingSession(S session);
    protected abstract void deregisterSession(String Suid);
    protected abstract void onRequestStopSession(S session);
    protected abstract S onRequestRegisteredSession();


    public R[] getLocalRenders(){
        return this.renders;
    }


}
