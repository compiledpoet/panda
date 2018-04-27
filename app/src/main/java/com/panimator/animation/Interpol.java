package com.panimator.animation;

import android.util.Log;
import android.util.Size;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by ASANDA on 2018/01/16.
 */

public class Interpol {
    private static final char[] zeros = new char[]{'0', '0', '0', '0', '0'};
    private static final Random randomGenerator;
    static {
        randomGenerator = new Random();
    }

    public static String generateUniqueKey(String body) {
        short MAX = 10999, MIN = 1000;
        String prefix = Integer.toHexString(MIN + randomGenerator.nextInt(MAX - MIN));
        String suffix  = Long.toHexString(System.currentTimeMillis());
        return prefix + ":" + String.format("%040x", new BigInteger(1, body.getBytes())) + ":" + suffix;
    }

    public static Size getOptimalSize(Size[] sizes, int Width, int Height) {
        ObjectSize closestSize = null;
        for (Size compSize : sizes) {
            double compAspectRatio = ((double) compSize.getHeight() / (double) compSize.getWidth()),
                    myAspectRatio = ((double) Height / (double) Width),
                    AbsAspectDifference = Math.abs(compAspectRatio - myAspectRatio),
                    AbsWidthDifference = Math.abs(compSize.getWidth() - Width),
                    AbsHeightDifference = Math.abs(compSize.getHeight() - Height);

            ObjectSize objectSize = new ObjectSize(compSize);
            objectSize.setScore(AbsAspectDifference + AbsWidthDifference + AbsHeightDifference);
            if(closestSize == null){
                closestSize = objectSize;
            }else{
                if(closestSize.getScore() > objectSize.getScore()){
                    closestSize = objectSize;
                }
            }
        }
        return (closestSize == null)? sizes[0] : closestSize.getSize();
    }

    public static String PadNumber(int num){

        int counterLength = String.valueOf(num).length();
        return new String(zeros, 0, 5 - counterLength) + num;
    }

    public static int getStandOutColor(int color, int stand1, int stand2) {
        double distance1 = getColorDistance(color, stand1);
        double distance2 = getColorDistance(color, stand2);
        Log.i("gej", distance1 + "|" + distance2);
        return (distance1 > distance2)? stand1 : stand2;
    }

    public static double getColorDistance(int color1, int color2){
        BlueColor C1 = new BlueColor(color1),
                C2 = new BlueColor(color2);
        double r2 = Math.pow(C1.Red() - C2.Red(), 2),
                g2 = Math.pow(C1.Green() - C2.Green(), 2),
                b2 = Math.pow(C1.Blue() - C2.Blue(), 2);
        return Math.sqrt(r2 + g2 + b2);
    }
}
