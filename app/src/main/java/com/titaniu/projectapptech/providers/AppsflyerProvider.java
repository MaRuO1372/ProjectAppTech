package com.titaniu.projectapptech.providers;

import android.content.Context;
import android.util.Log;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class AppsflyerProvider {
    private String AF_KEY;
    private Context mContext;
    private String af_data;
    private CountDownLatch countDownLatch;

    public AppsflyerProvider(Context context, String AF_KEY) {
        mContext = context;
        this.AF_KEY = AF_KEY;
    }

    public String getAppsflyer() throws TimeoutException, InterruptedException, ExecutionException {
        countDownLatch = new CountDownLatch(1);
        Runnable runnable = () -> {
            AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
                @Override
                public void onConversionDataSuccess(Map<String, Object> conversionData) {
                    StringBuilder params = new StringBuilder("&");
                    for (String attrName : conversionData.keySet()) {
                        params.append(attrName).append("=").append(conversionData.get(attrName)).append("&");
                    }
                    af_data = params.toString().replace(" ", "_");
                    countDownLatch.countDown();
                }


                @Override
                public void onConversionDataFail(String s) {
                    Log.d("url", "error data conversion Appsflyer   \n Error is: " + s ) ;
                    countDownLatch.countDown();
                }

                @Override
                public void onAppOpenAttribution(Map<String, String> map) {

                }

                @Override
                public void onAttributionFailure(String s) {
                    Log.d("url", "attribution failure " + s);
                }
            };
            AppsFlyerLib.getInstance().init(AF_KEY, conversionListener, mContext);
            AppsFlyerLib.getInstance().start(mContext);
        };

        Executors.newFixedThreadPool(1).submit(runnable).get(15, TimeUnit.SECONDS);
        countDownLatch.await(15, TimeUnit.SECONDS);
        return  af_data;
    }
}
