package com.panimator.animation;

import android.content.Context;
import android.support.v4.app.INotificationSideChannel;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import static android.content.ContentValues.TAG;

/**
 * Created by ASANDA on 2018/01/13.
 */

public class FFMpegHelper {
    private static FFMpegHelper Instance;
    private final FFmpeg fFmpeg;

    private FFMpegHelper(Context context){
        fFmpeg = FFmpeg.getInstance(context);
    }

    public  void execute(String command, final FFMpegListener callback){
        Log.i(TAG, command);
        String[] commands = command.split(" ");
        try {
            fFmpeg.execute(commands, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    Log.i(TAG, "S:" + message);
                }

                @Override
                public void onProgress(String message) {
                    Log.i(TAG, "P:" + message);
                }

                @Override
                public void onFailure(String message) {
                    Log.i(TAG, "F:" + message);
                }

                @Override
                public void onStart() {
                    Log.i(TAG, "Started:");
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "Finished:");
                    if(callback != null){
                        callback.onCommandFinished();
                    }
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    public static FFMpegHelper getInstance(Context pContext){
        if(Instance == null){
            Instance = new FFMpegHelper(pContext);
        }
        return Instance;
    }

    public static interface FFMpegListener{
        public void onCommandFinished();
    }
}
