package com.app.adam.simplemusicplayer;

/**
 * Created by Adam on 3/13/2015.
 */
public class Converter {


    public Converter(){}


    public String milliSecondsToTimer(long currentDuration) {


        String finalTimerString = "";
        String secondsString = "";

        int hours = (int)( currentDuration / (1000*60*60));
        int minutes = (int)(currentDuration % (1000*60*60)) / (1000*60);
        int seconds = (int) ((currentDuration % (1000*60*60)) % (1000*60) / 1000);
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;
    }

    public int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        percentage =(((double)currentSeconds)/totalSeconds)*100;

        return percentage.intValue();
    }

    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        return currentDuration * 1000;
    }
}
