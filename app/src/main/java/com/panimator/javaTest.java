package com.panimator;

import com.panimator.animators.voicevisualizer.Complex;
import com.panimator.animators.voicevisualizer.FFT;

import java.util.Random;

/**
 * Created by ASANDA on 2018/04/05.
 * for  =
 */
public class javaTest {

    public static void main(String[] args){
        Random random  = new Random();
        int switchi = 0;
        while (true){
            if(switchi == 120){
                System.out.print("\n");
                switchi = 0;
            }
            if(random.nextBoolean())
                System.out.print("\\");
            else
                System.out.print("/");
            switchi++;
        }


    }
}
