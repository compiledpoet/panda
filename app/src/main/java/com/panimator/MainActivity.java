package com.panimator;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.panimator.animation.AndroidAnimator;
import com.panimator.animation.AndroidAnimatorManager;
import com.panimator.codeBlue.rendering.RendererManager;
import com.theartofdev.edmodo.cropper.CropImage;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity{
    public static  File DEMO_DIRS;
    private static final String KEY_IS_FIRST_RUN = "KEY_IFR";
    private static final String TAG = "MAIN_ACTIVITY";
    private boolean isPlaying = false;
    private AndroidAnimatorManager androidAnimatorManager;

    static{
        if(OpenCVLoader.initDebug()){
            Log.i(TAG, "LOADED");
        }else{
            Log.i(TAG, "!LOADED");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DEMO_DIRS =  new File(this.getFilesDir(), "animatorDemos");
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = this.getPreferences(MODE_PRIVATE);
        final boolean isFirstRun = sharedPreferences.getBoolean(KEY_IS_FIRST_RUN, true);
        if(isFirstRun){
            InitializeFFmpeg();
            sharedPreferences.edit().putBoolean(KEY_IS_FIRST_RUN, false).apply();
        }

        Toast.makeText(this, "Hello 2018", Toast.LENGTH_LONG).show();

        this.androidAnimatorManager = new AndroidAnimatorManager(this);

        if(!DEMO_DIRS.exists()){
            unpackDemos(DEMO_DIRS);
        }
        ListView lstDisplay = (ListView)this.findViewById(R.id.lst_display);


        final ArrayAdapter<AndroidAnimator<Bitmap>> adp = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, androidAnimatorManager.getLocalRenders());
        lstDisplay.setAdapter(adp);
        lstDisplay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                androidAnimatorManager.startRenderingSession(androidAnimatorManager.createRenderingSession(adp.getItem(position), RendererManager.ANIMATION_SIZE_1080p));
            }
        });

    }

    private void unpackDemos(File parent) {
        parent.mkdir();
        try {
            String[] demos = this.getAssets().list("animator_demos");
            for(String demo : demos){
                InputStream inputStream = this.getAssets().open("animator_demos" + File.separator + demo);
                File ouputPath = new File(parent, demo);
                FileOutputStream FOS = new FileOutputStream(ouputPath);
                byte[] buffer = new byte[8 * 1024];
                int bytesRead = 0;
                while((bytesRead = inputStream.read(buffer))  >= 0){
                    FOS.write(buffer, 0, bytesRead);
                }
                FOS.close();
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void InitializeFFmpeg() {
        FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
        try {
            ffmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Toast.makeText(MainActivity.this, "Fail", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onStart() {
                    Toast.makeText(MainActivity.this, "Started", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFinish() {
                    Toast.makeText(MainActivity.this, "Finished", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }


}
