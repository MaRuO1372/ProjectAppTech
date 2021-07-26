package com.titaniu.projectapptech;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Topup {
    int score;
    private int row, col;
    double x,y;
    int type;
    private double speed;
    int targetX, targetY;

    Topup(int row, int col, int type, int score){
        this.score = score;
        this.type = type;
        this.row = row;
        this.col = col;
        this.speed = 0.88 + Math.random() / 4;
        this.x = 0;
        this.y = 0;
        this.targetX = 0;
        this.targetY = 0;
    }

    public void update(double modifier, int tileSize, int screenDy, int screenW, int scoreWidth, int scoreHeight, int topupHeight){
        if(x == 0 && y == 0){
            x = col * tileSize + tileSize / 2;
            y = screenDy + row * tileSize + tileSize / 2;
            targetX = screenW / 2 + ((int)(Math.random() * scoreWidth) - scoreWidth / 2);
            targetY = screenDy / 2 + scoreHeight / 2 + topupHeight + tileSize / 2;
        }
        double xMultiplier = 1;
        if(y != targetY) {
            xMultiplier = Math.abs(x-targetX)/Math.abs(y - targetY);
        }
        //Log.d("Match3", "x: "+x+" y:"+y);
        x = stepValue(x, targetX, modifier, xMultiplier);
        y = stepValue(y, targetY, modifier, speed);
        //Log.d("Match3", "x2: "+x+" y2:"+y);


    }

    public void draw(Canvas canvas, Paint topupPaint){
        canvas.drawText(String.valueOf(score), (int)x, (int)y, topupPaint);
    }

    private double stepValue(double source, double dest, double modifier, double multiplier){
        int direction;
        if(Math.abs(dest - source)  <= Constants.TOPUP_STEP * modifier * multiplier){
            return dest;
        } else {
            if(source > dest){
                direction = -1;
            } else {
                direction = 1;
            }
            return source + Constants.TOPUP_STEP * direction * modifier * multiplier;
        }
    }
}
