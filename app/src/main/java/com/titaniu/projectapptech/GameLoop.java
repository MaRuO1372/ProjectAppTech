package com.titaniu.projectapptech;

import android.graphics.Canvas;

public class GameLoop extends Thread {
    private GameView view;
    private long oldTime;
    static private long FPS = 25;
    private boolean running = false;
    private boolean isPaused;

    GameLoop(GameView view){
        this.view = view;
    }

    public void SetRunning(boolean run){
        oldTime = System.currentTimeMillis();
        running = run;
    }

    public void SetPause(boolean pause){
        synchronized (view.getHolder()){
            isPaused = pause;
        }
    }

    @Override
    public void run() {
        super.run();
        long frameMs = 1000/FPS;
        long startTime;
        long sleepTime;
        double delta;

        while(running){
            startTime = System.currentTimeMillis();
            if(isPaused){
                try {
                    sleep(50);
                } catch (InterruptedException ex){
                    ex.printStackTrace();
                }
            } else {
                Canvas c = null;
                //startTime = System.currentTimeMillis();
                try {
                    c = view.getHolder().lockCanvas();
                    synchronized (view.getHolder()){
                        //newTime = System.currentTimeMillis();
                        delta = startTime - oldTime;
                        view.update(delta/1000);
                        view.onDraw(c);
                    }
                } finally {
                    if(c != null){
                        view.getHolder().unlockCanvasAndPost(c);
                    }
                }
            }

            sleepTime = frameMs - (System.currentTimeMillis() - startTime);

            try{
                if(sleepTime > 0){
                    sleep(sleepTime);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            oldTime = startTime;
        }

    }
}
