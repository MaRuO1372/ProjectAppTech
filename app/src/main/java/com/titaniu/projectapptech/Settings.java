package com.titaniu.projectapptech;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class Settings extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void resetScore(View view){
        ResourceManager.getInstance().hsRepo.deleteAllScores();
    }

    public void resetRes(View view){
        ResourceManager.getInstance().hsRepo.deleteAllRes();
    }
}
