package com.panimator.animators.voicevisualizer;

import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.panimator.R;
import com.panimator.animation.AndroidAnimatorManager;
import com.panimator.animation.AndroidPlane;
import com.panimator.animation.BlueStream;
import com.panimator.codeBlue.BlueKey;
import com.panimator.codeBlue.Func;
import com.panimator.codeBlue.rendering.RenderingSession;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static android.content.ContentValues.TAG;

/**
 * Created by ASANDA on 2018/01/17.
 */

public class VoiceCollectorPlane extends AndroidPlane implements View.OnClickListener, RecordListener, RenderingSession.SessionCallback {
    public static final BlueKey<double[]> SESSION_KEY_FFT = new BlueKey<>("SK_FFT");
    public static final BlueKey<String> SESSION_KEY_TITLE = new BlueKey<>("SK_BACKGROUND");


    private String pcmPath;
    private BlueStream<byte[]> audioStream;
    private BlueStream<double[]> fftStream;
    private AsyncRecorder audioRecorder;
    private FileOutputStream FOS;
    private boolean isRecording;
    private double audioLength = 0.1;
    private Handler mHandler;
    private VisualizerView visualizerView;
    private EditText inputTitle;
    private int colorsCounter;

    public VoiceCollectorPlane(AndroidPlaneListener pPlaneListener, RenderingSession pRenderingSession) {
        super(pPlaneListener, pRenderingSession);
        this.getRenderingSession().registerSessionCallback(this);
    }




    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_record_voice:
                if(isRecording){
                    stopRecording();
                }else{
                    startRecording();
                    isRecording = true;
                }
                break;
            case R.id.img_import_audio:
                this.startPlane(MusicPickerPlane.class, this.getRenderingSession());
                break;
        }
    }

    private void startRecording() {
        FOS = null;
        this.audioStream = new BlueStream<>();
        this.fftStream = new BlueStream<>();
        this.audioRecorder = new AsyncRecorder(this);
        this.getRenderingSession().getBundle().push(SESSION_KEY_TITLE, this.inputTitle.getText().toString());
        this.inputTitle.setEnabled(false);
        BlueStream.StreamReader<double[]> fftStreamReader = new BlueStream.StreamReader<double[]>(fftStream) {
            private int progress = 0;
            @Override
            protected void onRead(double[] audioFFT) {

                VoiceCollectorPlane.this.getRenderingSession().getBundle().push(SESSION_KEY_FFT, audioFFT);
                VoiceCollectorPlane.this.getRenderingSession().pushRender();

                if(!isRecording){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            VoiceCollectorPlane.this.updateProgress(progress);
                            progress++;
                        }
                    });
                }
            }

            @Override
            protected void onDoneReading(BlueStream<double[]> blueStream) {
                String outputWavName = Func.generateUniqueKey("Wav") + ".wav";
                String workspace = VoiceCollectorPlane.this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_WORKSPACE);
                String wavPath = workspace + File.separator + outputWavName;

                createWaveFile(pcmPath, wavPath);
                double frameRate = ((double)VoiceCollectorPlane.this.getRenderingSession().getFramesRendered() / audioLength);
                VoiceCollectorPlane.this.getRenderingSession().encode(frameRate, wavPath);
            }
        };

        BlueStream.StreamReader<byte[]> audioStreamReader = new BlueStream.StreamReader<byte[]>(this.audioStream) {
            @Override
            protected void onRead(byte[] rawAudioData) {
                VoiceCollectorPlane.this.WritePCM(rawAudioData);
                final double[] audioFFT = CalculateFFT(rawAudioData);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        visualizerView.update(audioFFT);
                    }
                });
                fftStream.Write(audioFFT);
            }

            @Override
            protected void onDoneReading(BlueStream<byte[]> blueStream) {
                fftStream.Close();
            }
        };

        this.audioStream.Open();
        this.fftStream.Open();
        this.audioRecorder.Record();
        fftStreamReader.StartReading();
        audioStreamReader.StartReading();
    }


    private void stopRecording() {
        this.audioRecorder.Stop();
    }

    @Override
    public void onStartRecording(int BufferSize) {

    }

    @Override
    public void onReceiveRawAudioData(byte[] rawAudioData) {
        this.audioStream.Write(rawAudioData);
    }

    @Override
    public void onRecordingFinished(double recordingLength) {
        this.audioLength = recordingLength;
        this.audioStream.Close();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int max = audioStream.Size() + fftStream.Size() + 1;
                int _bufferLength = AudioRecord.getMinBufferSize(AsyncRecorder.SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                visualizerView.update(getEmptyFFT(_bufferLength / 2));
                VoiceCollectorPlane.this.showProgress(max);
            }
        });
        isRecording = false;
    }



    @Override
    protected void onStop() {
        this.audioRecorder.Stop();
        this.audioStream.Close();
        this.audioStream.Release();
        FileOutputStream FOS = getFOS();
        if(FOS != null){
            try {
                FOS.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onStop();
    }

    private void WritePCM(byte[] rawAudioData) {
        FileOutputStream FOS = getFOS();
        if(FOS == null){
            return;
        }

        try {
            getFOS().write(rawAudioData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double[] CalculateFFT(byte[] rawAudioData) {
        double mMaxFFTSample;
        int pow = (int)(Math.log((rawAudioData.length / 2.0) - 1) / Math.log(2));
        int FFT_POINTS = (int)Math.pow(2, pow);

        double temp;
        Complex[] y;
        Complex[] complexSignal = new Complex[FFT_POINTS];
        double[] absSignal = new double[FFT_POINTS / 2];

        for(int i = 0; i < FFT_POINTS; i++){
            temp = (double)((rawAudioData[2*i] & 0xFF) | (rawAudioData[2*i+1] << 8));
            complexSignal[i] = new Complex(temp, 0.0);
        }
        y = FFT.fft(complexSignal); // --> Here I use FFT class

        mMaxFFTSample = 0.0;
        for(int i = 0; i < (FFT_POINTS / 2); i++)
        {
            absSignal[i] = (Math.sqrt(Math.pow(y[i].re(), 2) + Math.pow(y[i].im(), 2)) * 2) / y.length;

            if(absSignal[i] > mMaxFFTSample)
            {
                mMaxFFTSample = absSignal[i];
            }
        }
        //Log.i("EGGSO",  FFT_POINTS + "|" + mMaxFFTSample + "|" + rawAudioData.length);
        return absSignal;
    }

    private void createWaveFile(String pcmPath, String outputPath) {
        Log.i(TAG, "wav:" + Wave.fromPCM(pcmPath, outputPath, AsyncRecorder.SAMPLING_RATE, AsyncRecorder.CHANNELS, audioRecorder.getBufferSize()));
    }

    private FileOutputStream getFOS(){
        if(this.FOS == null){
            try {
                this.FOS = new FileOutputStream(this.pcmPath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return FOS;
    }

    private double[] getEmptyFFT(int length){
        int pow = (int)(Math.log((length) - 1) / Math.log(2));
        int FFT_POINTS = (int)Math.pow(2, pow);

        double[] fft = new double[FFT_POINTS / 2];
        Arrays.fill(fft, 0);
        return fft;
    }


    @Override
    protected View onRequestGui() {
        AndroidAnimatorManager androidAnimatorManager = this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_ANIMATION_MANAGER);
        View parentView = androidAnimatorManager.inflate(R.layout.activity_visualizer_voice_data_collector);

        this.visualizerView = (VisualizerView)parentView.findViewById(R.id.visualView);
        int _bufferLength = AudioRecord.getMinBufferSize(AsyncRecorder.SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        double[] fft = getEmptyFFT(_bufferLength / 2);
        this.visualizerView.update(fft);
        ImageView imgRecord = parentView.findViewById(R.id.btn_record_voice),
                imgImport = parentView.findViewById(R.id.img_import_audio);

        this.inputTitle = parentView.findViewById(R.id.edt_vvl_title);


        imgRecord.setOnClickListener(this);
        imgImport.setOnClickListener(this);
        return parentView;
    }

    @Override
    protected void onCreate() {
        this.pcmPath = this.getRenderingSession().getBundle().pull(AndroidAnimatorManager.SESSION_KEY_WORKSPACE) + File.separator + Func.generateUniqueKey("Pcm") + ".pcm";
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.mHandler = new Handler();
        colorsCounter = -1;
    }

    @Override
    public void onReturnFrame(Object frame) {

    }

    @Override
    public void onRenderingFinished(final String outputFile) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showAnimationPreview(outputFile);
                VoiceCollectorPlane.this.dismissProgress();
            }
        });
    }

    @Override
    public void onMaxFramesReached() {

    }

    @Override
    public void onError(String message) {

    }
}
