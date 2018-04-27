package com.panimator.animation;

import android.os.Handler;

import java.sql.Array;
import java.util.ArrayList;

/**
 * Created by ASANDA on 2018/01/14.
 */

public class BlueStream<T> {
    private boolean isSteaming;
    private ArrayList<T> StreamingData;

    public BlueStream(){
        isSteaming = false;
        this.StreamingData = new ArrayList<>();
        //BlueStream<String[]> n;
    }

    public void Write(T obj){
        this.StreamingData.add(obj);
    }

    public T Read(){
        return Read(0);
    }

    public T Read(int pos){
        if(StreamingData.size() > pos){
            return StreamingData.get(pos);
        }
        return null;
    }

    public void Open(){
        isSteaming = true;
    }

    public void Close(){
        isSteaming = false;
    }

    public void Release(){
        this.StreamingData.clear();
    }

    public void Release(T obj){
        this.StreamingData.remove(obj);
    }

    public boolean isSteaming(){
        return isSteaming;
    }

    public int Size(){
        return this.StreamingData.size();
    }

    public static abstract class StreamReader<T>{
        private final Thread BackgroundWorker;

        public StreamReader(final BlueStream<T> pStream){
            this.BackgroundWorker = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (pStream.isSteaming()){
                        while (pStream.Size() > 0 ) {
                            T obj = pStream.Read();
                            StreamReader.this.onRead(obj);
                            pStream.Release(obj);
                        }
                    }
                    StreamReader.this.onDoneReading(pStream);
                }
            });
        }

        public void StartReading(){
            this.BackgroundWorker.start();
        }



        protected abstract void onRead(T object);
        protected abstract void onDoneReading(BlueStream<T> blueStream);
    }
}
