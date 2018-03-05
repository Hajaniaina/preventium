package com.preventium.boxpreventium.utils;

/**
 * Created by Diane on 3/1/2018.
 */

public class ColorCEP {

    private static int colorA;
    private static int colorV;
    private static int colorF;
    private static int colorM;

    public static void addColors(int a, int v, int f, int m){
        if(a!=0 && v != 0 && f != 0 && m != 0){
            colorA = a;
            colorV = v;
            colorF = f;
            colorM = m;
        }
    }

    public static int getA(){
        return colorA;
    }

    public static int getV(){
        return colorV;
    }

    public static int getF(){
        return colorF;
    }

    public static int getM(){
        return colorM;
    }

}
