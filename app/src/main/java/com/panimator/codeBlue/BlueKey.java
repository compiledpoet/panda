package com.panimator.codeBlue;

/**
 * Created by ASANDA on 2018/01/28.
 * for Pandaphic
 */

public class BlueKey<T> {
    private final String value;

    public BlueKey(String pValue){
        this.value = pValue;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null){
            if(obj.getClass() == this.getClass()){
                BlueKey blueKey = (BlueKey)obj;
                return (this.value.equals(blueKey.value));
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public String toString() {
        return this.value;
    }
}
