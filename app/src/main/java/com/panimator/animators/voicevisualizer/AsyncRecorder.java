package com.panimator.animators.voicevisualizer;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Process;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by ASANDA on 2018/01/13.
 */

public class AsyncRecorder extends AsyncTask<String, double[], Integer> {
    public static final int SAMPLING_RATE = 16000;
    public static final byte CHANNELS = 1;
    public static final short BIT_RATE = 16;
    private final RecordListener RecodingCallback;
    private boolean ContinueRecording;
    private double AudioLength;
    private int bufferSize;


    public AsyncRecorder(RecordListener pRecordingCallback) {
        this.ContinueRecording = true;
        this.RecodingCallback = pRecordingCallback;
        this.AudioLength = 0;
        this.bufferSize = 0;
    }

    @Override
    protected Integer doInBackground(String... params) {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
        this.checkBufferSize();
        bufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLING_RATE * 2;
        }

        byte[] AudioBuffer = new byte[bufferSize];
        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            return -1;
        }
        recorder.startRecording();
        if (this.RecodingCallback != null) {
            this.RecodingCallback.onStartRecording(bufferSize);
        }

        int totalDataRead = 0;
        while (ContinueRecording) {
            int readLength = recorder.read(AudioBuffer, 0, AudioBuffer.length);
            totalDataRead += readLength;
            if (this.RecodingCallback != null) {
                this.RecodingCallback.onReceiveRawAudioData(Arrays.copyOf(AudioBuffer,  readLength));
            }
        }

        recorder.stop();
        recorder.release();
        this.AudioLength = CalculateAudioLength(totalDataRead);

        if (this.RecodingCallback != null) {
            this.RecodingCallback.onRecordingFinished(this.AudioLength);
        }
        return 1;
    }

    private void checkBufferSize() {
        int[] sampleRates = new int[]{ 8000, 11025, 16000, 22050, 44100 };
        for(int rate : sampleRates){
            int _bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            Log.i("_buff_", "Buffer Size." + _bufferSize);
        }
    }

    private double CalculateAudioLength(int audioSize) {
        return (audioSize / (((double) SAMPLING_RATE * (double)CHANNELS * (double)BIT_RATE) / 8.00));
    }

    public double getAudioLength(){
        return this.AudioLength;
    }

    public int getBufferSize(){
        return this.bufferSize;
    }

    public void Record(){
        this.execute("C:BLUE");
    }

    public void Stop(){
        this.ContinueRecording = false;
    }
}