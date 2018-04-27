package com.panimator.animators.slowmo;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.panimator.animation.AndroidAnimatorManager;
import com.panimator.animation.AndroidPlane;
import com.panimator.animation.FFMpegHelper;
import com.panimator.codeBlue.rendering.RenderingSession;

import java.io.File;
import java.io.IOException;

/**
 * Created by ASANDA on 2018/01/22.
 * for Pandaphic
 */

public class VideoInfoCollectorPlane extends AndroidPlane{


    public VideoInfoCollectorPlane(AndroidPlaneListener pPlaneListener, RenderingSession pRenderingSession) {
        super(pPlaneListener, pRenderingSession);
    }

    private double getFrameRate(String videoPath) {
        MediaExtractor extractor = new MediaExtractor();
        double frameRate = 24;
        try {
            extractor.setDataSource(videoPath);
            for(int pos = 0; pos < extractor.getTrackCount(); pos++){
                MediaFormat format = extractor.getTrackFormat(pos);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if(mime.startsWith("video/")){
                    if(format.containsKey(MediaFormat.KEY_FRAME_RATE)){
                        frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return frameRate;
    }

    @Override
    protected View onRequestGui() {
        return this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_MANAGER).inflate();
    }

    @Override
    protected void onCreate() {

    }
}
