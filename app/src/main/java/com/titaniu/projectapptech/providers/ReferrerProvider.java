package com.titaniu.projectapptech.providers;

import android.content.Context;
import android.os.RemoteException;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ReferrerProvider {
    private static String referrer;
    public static String getReferrer(Context context){
        CountDownLatch countDownLatch = new CountDownLatch(1);
        referrer  = context.getSharedPreferences("save", 0).getString("referrer", "");
        if(referrer.isEmpty()) {
            final InstallReferrerClient referrerClient = InstallReferrerClient.newBuilder(context).build();
            referrerClient.startConnection(new InstallReferrerStateListener() {
                @Override
                public void onInstallReferrerSetupFinished(int responseCode) {
                    switch (responseCode) {
                        case InstallReferrerClient.InstallReferrerResponse.OK:
                            try {
                                ReferrerDetails response = referrerClient.getInstallReferrer();
                                context.getSharedPreferences("save", 0).edit().putString("referrer", referrer).apply();
                                referrer = response.getInstallReferrer();
                                countDownLatch.countDown();
                                // Сохраняем referrer
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            break;
                        case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                            countDownLatch.countDown();
                            break;
                        case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                            countDownLatch.countDown();
                            break;
                    }
                    referrerClient.endConnection();
                }

                @Override
                public void onInstallReferrerServiceDisconnected() {
                }
            });
        }
        try {
            countDownLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return  referrer;
    }
}