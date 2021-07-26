package com.titaniu.projectapptech;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ImageView movingBall = findViewById(R.id.ball3);

        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -Utils.DpToPx(this,48));
        animation.setDuration(700);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setStartOffset(1000);

        movingBall.startAnimation(animation);
    }

    public void back(View view){
        finish();
    }
}
