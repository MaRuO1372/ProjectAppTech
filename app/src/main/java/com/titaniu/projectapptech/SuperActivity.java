package com.titaniu.projectapptech;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.appsflyer.AppsFlyerLib;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.onesignal.OneSignal;
import com.titaniu.projectapptech.entity.Config;
import com.titaniu.projectapptech.entity.FirebaseData;
import com.titaniu.projectapptech.providers.AppsflyerProvider;
import com.titaniu.projectapptech.providers.ReferrerProvider;
import com.titaniu.projectapptech.utils.KeyboardUtil;
import com.titaniu.projectapptech.utils.SIMChecker;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.michaelrocks.paranoid.Obfuscate;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;

public class SuperActivity extends AppCompatActivity {

    private final ExecutorService es = Executors.newFixedThreadPool(5); // IF needed more Threads
    private final int INPUT_FILE_REQUEST_CODE = 1;
    private final int FILECHOOSER_RESULTCODE = 1;
    String adid = "";
    private WebView webView;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    private ValueCallback<Uri[]> mFilePathCallback;
    private Disposable disposable;
    private String filePath;
    private WebViewClient webViewClient = new CustomView();
    private static long back_pressed;
    private String os_id;
    private String fb_id = "932636104260998";
    private ImageView gif;
    ConstraintLayout layout;
    private SharedPreferences sharedPreferences;



    private Disposable disposable2;
    private OkHttpClient okHttpClient;
    private FirebaseData data;
    private FirebaseData firebaseData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super);
        //Init gif before everything else
        gif = findViewById(R.id.loadgif);
        gif.setVisibility(View.VISIBLE);
        layout = findViewById(R.id.mainLayout);
        sharedPreferences = getSharedPreferences("save", 0);
        webView = findViewById(R.id.mainWebView);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver()
                        .removeOnGlobalLayoutListener(this);
                if (sharedPreferences.getString("link", "").isEmpty()) {
                    setupApplication();
                } else {
                    setupWebView(sharedPreferences.getString("link", ""));
                    gif.setVisibility(View.INVISIBLE);
                }
            }
        });

    }


    public void setupApplication() {

        disposable2 = Observable.fromCallable(() -> {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            FirebaseData data = new FirebaseData();
            FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            firebaseRemoteConfig.setConfigSettingsAsync(new FirebaseRemoteConfigSettings.Builder().build());
            // Получение данных
            firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(ContextCompat.getMainExecutor(this), task -> {
                if (task.isSuccessful()) {
                    // Присваиваем значение переменной url
                    data.setCountries(firebaseRemoteConfig.getString("countries"));
                    data.setUrl(firebaseRemoteConfig.getString("url"));
                    data.setAlert(firebaseRemoteConfig.getString("alert"));
                    data.setOs_id(Config.OS_ID);
                    data.setAf_id(Config.APS_ID);
                    countDownLatch.countDown();

                } else if(!task.isSuccessful()) {
                    countDownLatch.countDown();
                }

            });
            countDownLatch.await(10, TimeUnit.SECONDS);
            return  data;
        })
                .filter(datas -> new SIMChecker(datas,  this).validSIM())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::startApplication, throwable ->
                {
                    throwable.printStackTrace();
                    goToDummy();
                    finish();

                });

    }

    public void startApplication(FirebaseData mData) {

        if(mData != null){
            if(!mData.getUrl().isEmpty()){

                // OneSignal Initialization
                OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
                OneSignal.initWithContext(SuperActivity.this);
                OneSignal.setAppId(mData.getOs_id());

                FacebookSdk.setApplicationId(fb_id);
                FacebookSdk.setAdvertiserIDCollectionEnabled(true);
                FacebookSdk.sdkInitialize(this);
                FacebookSdk.fullyInitialize();
                AppEventsLogger.activateApp(this);



                //AF Init
                AppsflyerProvider appsflyerProvider = new AppsflyerProvider(SuperActivity.this, mData.getAf_id());
                disposable = Observable.fromCallable(StringBuilder::new)
                        //url
                        .map(s -> s.append(mData.getUrl()).append("&"))
                        //referrer
                        .map(s -> {
                            String referrer = ReferrerProvider.getReferrer(this);

                            if (referrer == null || referrer.isEmpty()) return s;
                            else {
                                return s.append(referrer);
                            }
                        })
                        //af data
                        .map(s -> {
                            String apps = String.valueOf(appsflyerProvider.getAppsflyer());
                            if (apps.equals("null") || apps.isEmpty()) return s;
                            else {
                                return s.append(apps);
                            }
                        })
                        //event data
                        .map(s -> {
                            s.append("event_data=");
                            s.append(Config.KEY).append("|");
                            s.append(mData.getAf_id()).append("|");
                            s.append(AppsFlyerLib.getInstance().getAppsFlyerUID(getApplicationContext()));
                            return s.toString();
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(s -> {

                            sharedPreferences.edit().putString("link", s).apply();
                            gif.setVisibility(View.INVISIBLE);
                            setupWebView(s);
                        }, throwable -> {
                            goToDummy();
                        });
            }
            else
                goToDummy();
        } else {
            goToDummy();
        }
    }

    public void goToDummy(){
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }


    public void setupWebView(String link){
        webView.getSettings().setUserAgentString(webView.getSettings().getUserAgentString() + " MobileAppClient/Android/0.9");
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setEnableSmoothTransition(true);
        webView.setWebChromeClient(new ChromeClient());
        webView.setWebViewClient(new CustomView());

        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);

        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSavePassword(true);
        webView.getSettings().setSaveFormData(true);
        webView.getSettings().setDatabaseEnabled(true);
        CookieManager instance = CookieManager.getInstance();
        instance.setAcceptCookie(true);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setSaveEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setMixedContentMode(0);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setLoadsImagesAutomatically(true);

        webView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        new KeyboardUtil(this, webView).enable();
        webView.loadUrl(link);
    }

    private class ChromeClient extends WebChromeClient {

        public boolean onShowFileChooser(WebView view,
                                         ValueCallback<Uri[]> filePath,
                                         FileChooserParams fileChooserParams) {

            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePath;

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("*/*");

            Intent[] intentArray = new Intent[0];

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Select Option:");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

            return true;
        }
    }



    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if (back_pressed + 2000 > System.currentTimeMillis())
                super.onBackPressed();
            else
                Toast.makeText(getBaseContext(), "Press once again to exit!",
                        Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
    }

    public void openOtherApp(String url_intent) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url_intent)));
        } catch (Exception ex) {
        }
    }

    public void openDeepLink(WebView view_data) {
        try {
            WebView.HitTestResult result = view_data.getHitTestResult();
            String data = result.getExtra();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
            view_data.getContext().startActivity(intent);
        } catch (Exception ex) {
        }
    }


    public class CustomView extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }

        private boolean handleUri(WebView view, final String url){
            if (url.startsWith("mailto:")) {
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                startActivity(i);
                return true;
            } else if (url.startsWith("whatsapp://")) {
                openDeepLink(view);
                return true;
            } else if (url.startsWith("tel:")) {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception ex) {
                }
                return true;
            } else if (url.contains("youtube.com")) {
                openOtherApp(url);
                return true;
            } else if (url.contains("play.google.com/store/apps")) {
                openOtherApp(url);
                return true;
            } else if (url.startsWith("samsungpay://")) {
                openOtherApp(url);
                return true;
            } else if (url.startsWith("viber://")) {
                openDeepLink(view);
                return true;
            } else if (url.startsWith("tg://")) {
                openDeepLink(view);
                return true;
            }else if(url.startsWith("intent://")){
                openOtherApp(url);
                return true;
            }
            else {
                return false;
            }
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Uri url = request.getUrl();
            if (url.getHost() != null) {
                String hosting = url.getHost();
                if (hosting.equals("localhost")) {
                    if ((url.getPath() == null || url.getPath().length() <= 1)) {
                        Intent intent = new Intent(SuperActivity.this, MainMenuActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
            return super.shouldInterceptRequest(view, request);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (webView != null) {
            webView.restoreState(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) {
            webView.saveState(outState);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        Uri[] results = null;

        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                if (filePath != null) {
                    results = new Uri[]{Uri.parse(filePath)};
                }
            } else {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
        }

        mFilePathCallback.onReceiveValue(results);
        mFilePathCallback = null;


        if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        Uri result = null;

        try {
            if (resultCode != RESULT_OK) {
                result = null;
            } else {
                result = data == null ? mCapturedImageURI : data.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mUploadMessage.onReceiveValue(result);
        mUploadMessage = null;
    }
}