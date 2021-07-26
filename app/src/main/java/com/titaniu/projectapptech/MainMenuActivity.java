package com.titaniu.projectapptech;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class MainMenuActivity extends Activity {
    private TextView scoreEasy, scoreMedium, scoreHard;
    private SharedPreferences prefs;
    private DecimalFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        ResourceManager.getInstance().init(this);

        prefs = getSharedPreferences("Match3", Context.MODE_PRIVATE);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        df = new DecimalFormat("###,###", symbols);

        scoreMedium = findViewById(R.id.scoreMedium);
        scoreEasy = findViewById(R.id.scoreEasy);
        scoreHard = findViewById(R.id.scoreHard);

        //scoreEasy.setText(df.format(prefs.getLong("scoreEasy", 0)));
        //scoreMedium.setText(df.format(prefs.getLong("scoreMedium", 0)));
        //scoreHard.setText(df.format(prefs.getLong("scoreHard", 0)));

        scoreEasy.setText(df.format(ResourceManager.getInstance().hsRepo.getScoreDay(GameType.Easy, System.currentTimeMillis())));
        scoreMedium.setText(df.format(ResourceManager.getInstance().hsRepo.getScoreDay(GameType.Medium, System.currentTimeMillis())));
        scoreHard.setText(df.format(ResourceManager.getInstance().hsRepo.getScoreDay(GameType.Hard, System.currentTimeMillis())));

    }

    public void startClassicGame(View view){
        Intent intent = new Intent(this, ClassicGame.class);
        intent.putExtra("GameType", GameType.Medium);
        startActivityForResult(intent, 0);
    }

    public void startEasyGame(View view){
        Intent intent = new Intent(this, ClassicGame.class);
        intent.putExtra("GameType", GameType.Easy);
        startActivityForResult(intent, 0);
    }

    public void startHardGame(View view){
        Intent intent = new Intent(this, ClassicGame.class);
        intent.putExtra("GameType", GameType.Hard);
        startActivityForResult(intent, 0);
    }

    public void infoEasyGame(View view){
        Intent intent = new Intent(this, GameStats.class);
        intent.putExtra("GameType", GameType.Easy);
        startActivityForResult(intent, 0);
    }

    public void infoExpertGame(View view){
        Intent intent = new Intent(this, GameStats.class);
        intent.putExtra("GameType", GameType.Medium);
        startActivityForResult(intent, 0);
    }

    public void infoMasterGame(View view){
        Intent intent = new Intent(this, GameStats.class);
        intent.putExtra("GameType", GameType.Hard);
        startActivityForResult(intent, 0);
    }

    public void settings(View view){
        Intent intent = new Intent(this, Settings.class);
        startActivityForResult(intent, 0);
    }

    public void help(View view){
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        scoreEasy.setText(df.format(ResourceManager.getInstance().hsRepo.getScoreDay(GameType.Easy, System.currentTimeMillis())));
        scoreMedium.setText(df.format(ResourceManager.getInstance().hsRepo.getScoreDay(GameType.Medium, System.currentTimeMillis())));
        scoreHard.setText(df.format(ResourceManager.getInstance().hsRepo.getScoreDay(GameType.Hard, System.currentTimeMillis())));
    }
}
