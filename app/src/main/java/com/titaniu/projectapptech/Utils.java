package com.titaniu.projectapptech;

import android.content.Context;
import android.util.DisplayMetrics;

import java.util.Locale;

public class Utils {

    public static int DpToPx(Context ctx, int dp){
        DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static String formatCounter(long counter){
        // if counter < 10 000 - show as is
        // 10000 > 10.0K
        // 10250 > 10.2K
        // 100 000 > 100K
        // 103 500 > 103K
        // 1 000 000 > 1.00M
        // 1 015 000 > 1.01M
        // 10 000 000 > 10.0M
        // 100 000 000 > 100M
        // 1 000 000 000 > 1.00B
        // 1 000 000 000 000 > 1.00T
        // 1 000 000 000 000 000 > 1.00q
        // 1 000 000 000 000 000 000 > 1.00Q
        // 9 223 372 036 854 775 808 > 9.22Q
        // next > return "Error"
        if (counter < 0){
            return "Error";
        } else if (counter < 10000){
            return String.valueOf(counter);
        } else if (counter < 100000){
            return String.valueOf(Math.floor(counter/100)/10) + "K";
        } else if (counter < 1000000){
            return String.valueOf((int)Math.floor(counter/1000))+ "K";
        } else if (counter < 10000000){
            return String.format(Locale.ENGLISH, "%.2f", Math.floor(counter/10000)/100)+ "M";
        } else if (counter < 100000000){
            return String.valueOf(Math.floor(counter/100000)/10)+ "M";
        } else if (counter < 1000000000) {
            return String.valueOf((int)Math.floor(counter/1000000))+ "M";
        } else if (counter < 10000000000L){
            return String.format(Locale.ENGLISH, "%.2f", Math.floor(counter/10000000)/100)+ "B";
        } else if (counter < 100000000000L){
            return String.valueOf(Math.floor(counter/100000000)/10)+ "B";
        } else if (counter < 1000000000000L) {
            return String.valueOf((int)Math.floor(counter/1000000000))+ "B";
        } else if (counter < 10000000000000L){
            return String.format(Locale.ENGLISH, "%.2f", Math.floor(counter/10000000000L)/100)+ "T";
        } else if (counter < 100000000000000L){
            return String.valueOf(Math.floor(counter/100000000000L)/10)+ "T";
        } else if (counter < 1000000000000000L) {
            return String.valueOf((int)Math.floor(counter/1000000000000L))+ "T";
        } else if (counter < 10000000000000000L){
            return String.format(Locale.ENGLISH, "%.2f", Math.floor(counter/10000000000000L)/100)+ "q";
        } else if (counter < 100000000000000000L){
            return String.valueOf(Math.floor(counter/100000000000000L)/10)+ "q";
        } else if (counter < 1000000000000000000L) {
            return String.valueOf((int)Math.floor(counter/1000000000000000L))+ "q";
        } else {
            return String.format(Locale.ENGLISH, "%.2f", Math.floor(counter/10000000000000000L)/100)+ "Q";
        }

    }
}