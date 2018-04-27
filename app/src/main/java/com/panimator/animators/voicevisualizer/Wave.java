package com.panimator.animators.voicevisualizer;

import android.preference.PreferenceActivity;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by ASANDA on 2018/01/13.
 */

public class Wave {
    private static final int HEADER_SIZE = 44;
    public static double fromPCM(String PCM_Path, String Output_Path, int SampleRate, byte channels, int bufferSize){
        try {
            FileInputStream FIS = new FileInputStream(PCM_Path);
            FileOutputStream FOS = new FileOutputStream(Output_Path);
            long audioLength = FIS.getChannel().size(),
                    dataLength = audioLength + 36;
            int byteRate = (16 * SampleRate * channels / 8);
            byte[] data = new byte[bufferSize];
            writeHeader(FOS, dataLength, audioLength, SampleRate, byteRate, channels);
            while (FIS.read(data) != -1){
                FOS.write(data);
            }
            FIS.close();
            FOS.close();
            return (audioLength /((double)SampleRate * channels * 2));
        } catch (FileNotFoundException e) {
            Log.i(TAG, e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
            e.printStackTrace();
        }
        return -12;
    }

    private static void writeHeader(FileOutputStream FOS, long dataLength, long audioLength, long sampleRate, int byteRate, byte channels) throws IOException {
        byte[] Header = new byte[44];
        Header[0] = 'R';
        Header[1]= 'I';
        Header[2]= 'F';
        Header[3]= 'F';
        Header[4]= (byte) (dataLength & 0xff);
        Header[5]= (byte) ((dataLength >> 8) & 0xff);
        Header[6]= (byte) ((dataLength >> 16) & 0xff);
        Header[7]= (byte) ((dataLength >> 24) & 0xff);
        Header[8]= 'W';
        Header[9]= 'A';
        Header[10]= 'V';
        Header[11]= 'E';
        Header[12]= 'f';
        Header[13]= 'm';
        Header[14]= 't';
        Header[15]= ' ';
        Header[16]= 16;
        Header[17]= 0;
        Header[18]= 0;
        Header[19]= 0;
        Header[20]= 1;//PCM
        Header[21]= 0;
        Header[22]= channels;
        Header[23]= 0;
        Header[24]= (byte)(sampleRate & 0xff);
        Header[25]= (byte)((sampleRate >> 8) & 0xff);
        Header[26]= (byte)((sampleRate >> 16) & 0xff);
        Header[27]= (byte)((sampleRate >> 24) & 0xff);
        Header[28]= (byte)(byteRate & 0xff);
        Header[29]= (byte)((byteRate >> 8) & 0xff);
        Header[30]= (byte)((byteRate >> 16) & 0xff);
        Header[31]= (byte)((byteRate >> 24) & 0xff);
        Header[32]= (2 * 16 / 8);
        Header[33]= 0;
        Header[34]= 16;//16 bits/sample
        Header[35]= 0;
        Header[36]= 'd';
        Header[37]= 'a';
        Header[38]= 't';
        Header[39]= 'a';
        Header[40]= (byte)(audioLength & 0xff);
        Header[41]= (byte)((audioLength >> 8) & 0xff);
        Header[42]= (byte)((audioLength >> 16) & 0xff);
        Header[43]= (byte)((audioLength >> 24) & 0xff);
        FOS.write(Header);
    }
}
