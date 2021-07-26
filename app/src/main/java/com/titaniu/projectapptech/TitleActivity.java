package com.titaniu.projectapptech;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class TitleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Activity current = this;
        ResourceManager.getInstance().init(this);
        ResourceManager.getInstance().loadImages(current.getResources());

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(current, MainMenuActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, 0);
        }, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }
}
