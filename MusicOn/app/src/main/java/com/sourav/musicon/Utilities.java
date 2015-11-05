package com.sourav.musicon;

import android.util.Log;

/**
 * Created by ASUS on 31/10/2015.
 */
public class Utilities {
    public String millisecondsToTimer(long milliseconds)
    {
        int hour=(int)(milliseconds)/(1000*60*60);
        int mins=(int)((milliseconds%(1000*60*60))/(1000*60));
        int seconds=(int)(milliseconds%(1000*60*60)%(1000*60))/1000;

        String finalTime="";
        String secondString="";
        if(hour>0)
            finalTime=hour+":";

        if(seconds<10)
            secondString="0"+seconds;
        else
            secondString=""+seconds;

        finalTime=finalTime+mins+":"+secondString;
        return finalTime;
    }

    public int getProgressPercentage(long elapsedduration, long totalduration)
    {
        long elapsed=elapsedduration/1000;
        long total=totalduration/1000;
        Double percentage=(double)(((elapsed)*100)/(total));
        Log.d("player",elapsed+" "+total+" "+ percentage.intValue());
        return percentage.intValue();
    }

    public int getTimerFromProgress(int progress, long totalduration)
    {
        Double elapsed=(double)(progress*totalduration)/100;
        return elapsed.intValue();
    }
}
