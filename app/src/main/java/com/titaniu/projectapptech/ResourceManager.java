package com.titaniu.projectapptech;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.titaniu.projectapptech.Data.HighScoresRepository;

import java.util.ArrayList;

public class ResourceManager {
    private ArrayList<Bitmap> tileRawImages;
    public ArrayList<Bitmap> tileImages;
    private boolean rawImagesLoaded = false;
    public boolean scaledImagesLoaded = false;
    private int tileSize = 0;
    private Resources res;
    private SharedPreferences prefs;
    public HighScoresRepository hsRepo;

    private static final ResourceManager INSTANCE = new ResourceManager();

    public void loadImages(Resources resources){
        res = resources;
        rawImagesLoaded = false;
        tileRawImages = new ArrayList<>();
        tileRawImages.add(BitmapFactory.decodeResource(res, R.drawable.a1));
        tileRawImages.add(BitmapFactory.decodeResource(res, R.drawable.a2));
        tileRawImages.add(BitmapFactory.decodeResource(res, R.drawable.a3));
        tileRawImages.add(BitmapFactory.decodeResource(res, R.drawable.a4));
        tileRawImages.add(BitmapFactory.decodeResource(res, R.drawable.a5));
        tileRawImages.add(BitmapFactory.decodeResource(res, R.drawable.a6));
        tileRawImages.add(BitmapFactory.decodeResource(res, R.drawable.a7));
        tileRawImages.add(BitmapFactory.decodeResource(res, R.drawable.a8));
        tileRawImages.add(BitmapFactory.decodeResource(res, R.drawable.a9));
        rawImagesLoaded = true;
    }

    public void init(Activity activity){
        hsRepo = new HighScoresRepository(activity.getApplication());
    }

    public void initPrefs(Activity activity){
        prefs = activity.getSharedPreferences("Match3", Context.MODE_PRIVATE);
        Log.i("Match3","Preferences init");
    }

    public void prefSaveString(String key, String value){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
        Log.i("Match3", "PrefS saved "+value);
    }

    public void prefSaveLong(String key, long value){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, value);
        editor.apply();
        Log.i("Match3", "PrefL saved "+value);
    }

    public String prefGetString(String key){
        if(prefs != null) {
            String result = prefs.getString(key, "");
            Log.i("Match3", "Pref loaded "+result);
            return result;
        }
        Log.i("Match3", "Pref load empty");
        return "";
    }

    public long prefGetLong(String key){
        if(prefs != null){
            long result = prefs.getLong(key, 0);
            Log.i("Match3", "Long loaded: " + result);
            return result;
        }
        Log.i("Match3", "Long load fails - prefs empty");
        return 0;
    }

    public void scaleImages(){
        if(tileSize > 0){
            scaleImages(tileSize);
        }
    }

    public void scaleImages(int size){
        scaledImagesLoaded = false;
        if(!rawImagesLoaded) {
            loadImages(res);
        }
        tileImages = new ArrayList<>();
        for (int i = 0; i < tileRawImages.size(); i++) {
            tileImages.add(Bitmap.createScaledBitmap(tileRawImages.get(i), size, size, true));
        }
        scaledImagesLoaded = true;
    }

    public static ResourceManager getInstance(){
        return INSTANCE;
    }
}
