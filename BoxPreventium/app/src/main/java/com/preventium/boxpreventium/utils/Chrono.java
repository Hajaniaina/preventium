package com.preventium.boxpreventium.utils;

/**
 * Created by Franck on 11/08/2016.
 */

public final class Chrono{

    private long begin          =0;
    private long end            =0;
    private long beginPause     =0;
    private long endPause       =0;
    private long duration       =0;

    public static Chrono newInstance() {
        Chrono ret = new Chrono();
        return ret;
    }
    public void start(){
        begin = System.currentTimeMillis();
        end =0;
        beginPause=0;
        endPause=0;
        duration=0;
    }

    public void pause() {
        if(begin==0) {return;}
        beginPause=System.currentTimeMillis();
    }

    public void resume() {
        if(begin==0) {return;}
        if(beginPause==0) {return;}
        endPause=System.currentTimeMillis();
        begin=begin+endPause-beginPause;
        end=0;
        beginPause=0;
        endPause=0;
        duration=0;
    }

    public void stop(){
        if(begin==0) {return;}
        end = System.currentTimeMillis();
        duration=(end-begin) - (endPause-beginPause);
        begin=0;
        end=0;
        beginPause=0;
        endPause=0;
    }

    public boolean isStarted() { return !isStopped(); }

    public boolean isPaused() { return (beginPause > 0); }

    public boolean isStopped() { return (begin == 0); }

    public long getMilliseconds() {
        if( begin == 0 ) return duration;
        if( beginPause > 0 ) return  beginPause - begin;
        return System.currentTimeMillis() - begin;
    }

    public long getTime() {
        return getMilliseconds();
    }

    public double getSeconds() {
        return getMilliseconds() / 1000.0;
    }

    public double getMinutes() { return getMilliseconds() / 60000.0; }

    public static double getMinutes(float time1, float time2) {
        float time = time2 - time1;
        return time / 60000.0;
    }

    public double getHours() { return getMilliseconds() / 3600000.0; }

    public String getDurationTxt() { return timeToHMS( (long)getSeconds() ); }

    public static String timeToHMS(long tempsS) {

        // IN : (long) temps en secondes
        // OUT : (String) temps au format texte : "1 h 26 min 3 s"

        int h = (int) (tempsS / 3600);
        int m = (int) ((tempsS % 3600) / 60);
        int s = (int) (tempsS % 60);

//        String r="";
//        if(h>0) {r+=h+" h ";}
//        if(m>0) {r+=m+" min ";}
//        if(s>0) {r+=s+" s";}
//        if(h<=0 && m<=0 && s<=0) {r="0 s";}
//        return r;

        return String.format("%02d:%02d\'%02d\"",h,m,s);
    }
}
