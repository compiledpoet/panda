package com.panimator.codeBlue;

import com.panimator.codeBlue.display.Bundle;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by ASANDA on 2018/01/26.
 * for Pandaphic
 */

public class Func {
    private static final Bundle pushedObjects = new Bundle();
    private static final Random randomGenerator = new Random();

    public static <T> BlueKey<T> pushStatic(T obj){
        BlueKey<T> Key = new BlueKey<>(generateUniqueKey("push"));
        pushedObjects.push(Key, obj);
        return Key;
    }

    public static <T> T pullStatic(BlueKey<T> Key){
        if(pushedStatic(Key)){
            return pushedObjects.pull(Key);
        }else{
            return null;
        }
    }

    public static <T> void Remove(BlueKey<T> Key){
        pushedObjects.remove(Key);
    }

    public static <T> boolean pushedStatic(BlueKey<T> Key){
        return pushedObjects.pushed(Key);
    }

    public static String generateUniqueKey(String body) {
        short MAX = 10999, MIN = 1000;
        String prefix = Integer.toHexString(MIN + randomGenerator.nextInt(MAX - MIN));
        String suffix  = Long.toHexString(System.currentTimeMillis());
        return prefix + ":" + String.format("%040x", new BigInteger(1, body.getBytes())) + ":" + suffix;
    }

    public static String padInt(int value, char pad, int maxLength){
        String valueString = String.valueOf(value);
        int valueLength = valueString.length();
        if(valueLength >= maxLength){
            return valueString;
        }

        char[] padding = new char[maxLength - valueLength];
        Arrays.fill(padding, pad);
        return new String(padding) + valueString;
    }

    public static String generateRandomHex(String prefix){
        final int MIN = 10, MAX = 99;
        int body = MIN + randomGenerator.nextInt(MAX - MIN);
        return Integer.toHexString(prefix.hashCode()) + "_" +Integer.toHexString(body) + "_" + Long.toHexString(System.currentTimeMillis());
    }
}
