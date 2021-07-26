package com.titaniu.projectapptech.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.titaniu.projectapptech.entity.FirebaseData;

public  class SIMChecker {


    private  String countries;
    private Context context;
    private FirebaseData firebaseData;
    public SIMChecker(FirebaseData firebaseData, Context context) {
        this.context = context;
        this.firebaseData = firebaseData;
    }
    public boolean validSIM(){
        boolean validSIM = false;
        countries = firebaseData.getCountries();
        String country = "";
        try {
            country = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getSimCountryIso().toUpperCase();
            if (country.isEmpty()) {
                country = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkCountryIso().toUpperCase();
            }

            if (countries.isEmpty()) {
                validSIM = true;
            } else {
                if (countries.contains(country) && !country.isEmpty()) {
                    validSIM = true;
                } else {
                    validSIM = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return validSIM;
    }
}
