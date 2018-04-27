package com.panimator.codeBlue.display;

import com.panimator.codeBlue.BlueKey;

import java.util.HashMap;

/**
 * Created by ASANDA on 2018/01/26.
 * for Pandaphic
 */

public class Bundle {
    private final  HashMap<BlueKey, Object> bundleData;

    public Bundle(){
        this.bundleData = new HashMap<>();
    }

    public <T> Bundle push(BlueKey<T> key, T value){
        this.bundleData.put(key, value);
        return this;
    }

    public <T> T pull(BlueKey<T> key){
        return (this.pushed((key)))? (T)bundleData.get(key) : null;
    }

    public <T> boolean pushed(BlueKey<T> key){
        return this.bundleData.containsKey(key);
    }

    public <T> void remove(BlueKey<T> Key){
        bundleData.remove(Key);
    }
    public void clear(){
        this.bundleData.clear();
    }

    public int size(){
        return this.bundleData.size();

    }

}
