package com.panimator.animation;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.panimator.R;

import java.io.File;

public class AnimationPreview extends AppCompatActivity implements MediaPlayer.OnPreparedListener {
    public static final String EXTRA_ANIMATION_PATH = "EA_Path";
    VideoView previewPlayer;
    String animationPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animation_preview_layout);

        Intent callerIntent = this.getIntent();
        if(callerIntent.hasExtra(EXTRA_ANIMATION_PATH)){

            //specify the location of media file
            this.animationPath = callerIntent.getStringExtra(EXTRA_ANIMATION_PATH);
            Uri uri = Uri.parse(animationPath);//Environment.getExternalStorageDirectory() + File.separator + "v.mp4"); //callerIntent.getStringExtra(EXTRA_ANIMATION_PATH));
            this.previewPlayer = this.findViewById(R.id.vid_preview);
            //Setting MediaController and URI, then starting the videoView
            previewPlayer.setOnPreparedListener(this);
            previewPlayer.setMediaController(null);
            previewPlayer.setVideoURI(uri);
            previewPlayer.setZOrderOnTop(true);
            previewPlayer.requestFocus();
            previewPlayer.start();
        }else{
            Toast.makeText(this, "No Animation Path Was Specified.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preview_share_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_share_all:
                shareVideo();
                return true;
            case R.id.action_share_whatsapp:
                return false;
            default:
                return false;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.setLooping(true);
    }

    private void shareVideo(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("video/mp4");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(this.animationPath));
        startActivity(Intent.createChooser(shareIntent, "Share With:"));
    }
}
