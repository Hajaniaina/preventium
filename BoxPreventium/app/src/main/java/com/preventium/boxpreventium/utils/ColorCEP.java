package com.preventium.boxpreventium.utils;

/**
 * Created by Diane on 3/1/2018.
 * Edited by Arnaud on 9/3/2018
 */

public class ColorCEP {

    private int colorA = -1;
    private int colorV = -1;
    private int colorF = -1;
    private int colorM = -1;

    /* singleton structure */
    private static ColorCEP instance = new ColorCEP();
    public static ColorCEP getInstance() { return instance; }

    public void addColors(int a, int v, int f, int m){
         if(a!=0 && v != 0 && f != 0 && m != 0){
            colorA = (int)a;
            colorV = (int)v;
            colorF = (int)f;
            colorM = (int)m;
         }
    }

    public void unsetColors () {
        this.colorA = -1;
        this.colorF = -1;
        this.colorV = -1;
        this.colorM = -1;
    }

    public int getA(){
        return colorA;
    }

    public int getV(){
        return colorV;
    }

    public int getF(){
        return colorF;
    }

    public int getM() { return colorM; }
}
