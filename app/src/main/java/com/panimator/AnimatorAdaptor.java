package com.panimator;

import android.animation.Animator;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.panimator.animation.AndroidAnimator;

import java.io.File;

/**
 * Created by ASANDA on 2018/04/22.
 * for Pandaphic
 */
public class AnimatorAdaptor extends ArrayAdapter<AnimatorAdaptor.AnimatorObject> implements MediaPlayer.OnPreparedListener {
    private final View.OnClickListener onClickListener;
    public AnimatorAdaptor(@NonNull Context context, int resource, @NonNull AnimatorAdaptor.AnimatorObject[] objects, View.OnClickListener listener) {
        super(context, resource, objects);
        this.onClickListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.animator_item_layout, parent, false);
        }

        AnimatorAdaptor.AnimatorObject animatorObject = this.getItem(position);

            TextView txtTitle = convertView.findViewById(R.id.txt_animator_title);
            txtTitle.setOnClickListener(this.onClickListener);
            txtTitle.setText(animatorObject.androidAnimator.getTitle());

            VideoView previewPlayer = convertView.findViewById(R.id.vid_demo);

            File demoPath = new File(MainActivity.DEMO_DIRS, animatorObject.androidAnimator.getPreviewName());
            Log.i("GSH", demoPath.getAbsolutePath());

            if(!demoPath.exists())
                return convertView;

        Log.i("GSH", demoPath.getAbsolutePath());

        Uri uri = Uri.fromFile(demoPath);//Environment.getExternalStorageDirectory() + File.separator + "v.mp4"); //callerIntent.getStringExtra(EXTRA_ANIMATION_PATH));

        //Setting MediaController and URI, then starting the videoView
        previewPlayer.setOnPreparedListener(this);
        previewPlayer.setMediaController(null);
        previewPlayer.setVideoURI(uri);
        previewPlayer.setZOrderOnTop(true);
        previewPlayer.requestFocus();
        previewPlayer.start();
            return convertView;


    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.setLooping(true);
    }

    public static class AnimatorObject{
        private boolean isSet;
        private final AndroidAnimator androidAnimator;
        private View view;

        public AnimatorObject(AndroidAnimator pAndroidAnimator){
            this.androidAnimator = pAndroidAnimator;
        }

        public AndroidAnimator getAndroidAnimator() {
            return androidAnimator;
        }

        public static AnimatorObject[] fromAnimator(AndroidAnimator[] androidAnimators){
            AnimatorObject[] animatorObjects = new AnimatorObject[androidAnimators.length];
            for(int pos = 0; pos < animatorObjects.length; pos++){
                animatorObjects[pos] = new AnimatorObject(androidAnimators[pos]);
            }
            return  animatorObjects;
        }

    }
}
