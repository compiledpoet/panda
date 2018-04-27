package com.panimator.animators.voicevisualizer;

/**
 * Created by ASANDA on 2018/01/14.
 */

public interface RecordListener {
    void onStartRecording(int BufferSize);
    public void onReceiveRawAudioData(byte[] rawAudioData);
    public void onRecordingFinished(double recordingLength);
}
