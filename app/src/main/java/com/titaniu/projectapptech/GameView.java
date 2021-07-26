package com.titaniu.projectapptech;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static com.titaniu.projectapptech.GameMode.Explode;
import static com.titaniu.projectapptech.GameMode.FallDown;
import static com.titaniu.projectapptech.GameMode.Selection;
import static com.titaniu.projectapptech.GameMode.Swap;

enum GameMode{Selection, Swap, SwapBack, Explode, FallDown, OnHold}


public class GameView extends SurfaceView {
    boolean firstBack = true;
    GameLoop gameLoop;
    int sw, sh, screenDw, screenDh;
    int tileWidth;
    private GameView thisView;
    GameField gField;
    int moveTileCol, moveTileRow;
    int prevX, prevY;
    int SelectedTileIndex;
    Rect fieldRect = new Rect();
    Rect transformRect = new Rect();
    private GameMode gameMode;
    private GameType gameType;
    private int scoreWidth, scoreHeight, topupHeight, resourceCounterHeight;
    Tile swapFrom, swapTo;
    private Paint resourceCounterPaint, scorePaint, resourceMultiplierPaint, resMultiplierShadow;
    private Paint topupPaint, fieldPaint;
    private DecimalFormat df;

    public GameView(Context context, GameType type) {
        super(context);
        thisView = this;
        gameType = type;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        df = new DecimalFormat("###,###", symbols);

        SurfaceHolder holder = getHolder();

        gField = new GameField(gameType);
        gField.init();

        SelectedTileIndex = -1;

        moveTileCol = gField.cols;
        moveTileRow = gField.cols;

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                gField.loadTiles();

                gameLoop = new GameLoop(thisView);
                gameLoop.SetRunning(true);
                gameLoop.start();
                gameMode = Selection;

                if (gField.availableMoves == 0) {
                    gField.matchAll();
                    gameMode = Explode;
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {
                setScreenDimensions(w, h);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                gameLoop.SetRunning(false);
                gameLoop.getThreadGroup().interrupt();
                gField.saveTiles();
                gameMode = GameMode.OnHold;
            }
        });

        DisplayMetrics dm;
        dm = Resources.getSystem().getDisplayMetrics();
        setScreenDimensions(dm.widthPixels, dm.heightPixels);

        resourceCounterHeight = Utils.DpToPx(this.getContext(), 16);
        resourceCounterPaint = new Paint();
        resourceCounterPaint.setColor(Color.WHITE);
        resourceCounterPaint.setTextSize(resourceCounterHeight);
        resourceCounterPaint.setTextAlign(Paint.Align.CENTER);

        int resMultiplierTextSize = Utils.DpToPx(this.getContext(), 10);

        resourceMultiplierPaint = new Paint();
        resourceMultiplierPaint.setColor(Color.WHITE);
        resourceMultiplierPaint.setTextSize(resMultiplierTextSize);
        resourceMultiplierPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        resourceMultiplierPaint.setTextAlign(Paint.Align.LEFT);

        resMultiplierShadow = new Paint();
        resMultiplierShadow.setColor(Color.DKGRAY);
        resMultiplierShadow.setTextSize(resMultiplierTextSize);
        resMultiplierShadow.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        resMultiplierShadow.setTextAlign(Paint.Align.LEFT);

        scorePaint = new Paint();
        scorePaint.setColor(Color.YELLOW);
        scorePaint.setTextSize(Utils.DpToPx(this.getContext(), 48));
        scorePaint.setTextAlign(Paint.Align.CENTER);

        topupPaint = new Paint();
        topupPaint.setColor(Color.YELLOW);
        topupPaint.setTextSize(Utils.DpToPx(this.getContext(), 16));
        topupPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        topupPaint.setTextAlign(Paint.Align.CENTER);

        scoreWidth = 100;
        scoreHeight = Utils.DpToPx(this.getContext(), 48);
        topupHeight = Utils.DpToPx(this.getContext(), 16);

        fieldPaint = new Paint();
        fieldPaint.setColor(getResources().getColor(R.color.fieldSquare));
    }

    protected void update(double modifier) {
        int x1, y1, x2, y2;
        int x1d, y1d, x2d, y2d;
        int x1n, y1n, x2n, y2n;

        gField.updateFallen(modifier);

        //gField.scoreTopups = 0;
        for (int i = 0; i < gField.TopUps.size(); i++) {
            Topup t = gField.TopUps.get(i);
            t.update(modifier, tileWidth, screenDh, sw, scoreWidth, scoreHeight, topupHeight);
            if (t.x == t.targetX && t.y == t.targetY) {
                gField.TopUps.remove(t);
            } else {
                gField.TopUps.set(i, t);
                //gField.scoreTopups += t.score;
            }
        }

        switch (gameMode) {
            case Selection:
                for (int i = 0; i < gField.Tiles.size(); i++) {
                    Tile t = gField.Tiles.get(i);
                    if (t.Selected) {
                        t.jumpPhase += modifier * Constants.JUMP_SPEED;
                        gField.Tiles.set(i, t);
                    }
                }
                break;
            case Swap:
                //current tile positions
                x1 = screenDw + swapFrom.Column * tileWidth + swapFrom.dX;
                y1 = screenDh + swapFrom.Row * tileWidth + swapFrom.dY;
                x2 = screenDw + swapTo.Column * tileWidth + swapTo.dX;
                y2 = screenDh + swapTo.Row * tileWidth + swapTo.dY;

                //tile destinations
                x1d = screenDw + swapTo.Column * tileWidth;
                y1d = screenDh + swapTo.Row * tileWidth;
                x2d = screenDw + swapFrom.Column * tileWidth;
                y2d = screenDh + swapFrom.Row * tileWidth;

                //next step positions
                x1n = stepValue(x1, x1d, modifier);
                y1n = stepValue(y1, y1d, modifier);
                x2n = stepValue(x2, x2d, modifier);
                y2n = stepValue(y2, y2d, modifier);

                swapFrom.dX += x1n - x1;
                swapFrom.dY += y1n - y1;
                swapTo.dX += x2n - x2;
                swapTo.dY += y2n - y2;
                gField.Tiles.set(gField.Tiles.indexOf(swapFrom), swapFrom);
                gField.Tiles.set(gField.Tiles.indexOf(swapTo), swapTo);

                if (x1d == x1n && y1d == y1n && x2d == x2n && y2d == y2n) {
                    int fromDx = swapFrom.dX;
                    int fromDy = swapFrom.dY;
                    int toDx = swapTo.dX;
                    int toDy = swapTo.dY;
                    gField.swap(swapFrom, swapTo);
                    if (gField.match()) {
                        //убрать лишние
                        gameMode = Explode;
                        SelectedTileIndex = -1;
                    } else {
                        gField.swap(swapFrom, swapTo);
                        swapFrom.dX = fromDx;
                        swapFrom.dY = fromDy;
                        swapTo.dX = toDx;
                        swapTo.dY = toDy;
                        gField.Tiles.set(gField.Tiles.indexOf(swapFrom), swapFrom);
                        gField.Tiles.set(gField.Tiles.indexOf(swapTo), swapTo);
                        gameMode = GameMode.SwapBack;
                    }
                }
                break;
            case SwapBack:
                //current tile positions
                x1 = screenDw + swapFrom.Column * tileWidth + swapFrom.dX;
                y1 = screenDh + swapFrom.Row * tileWidth + swapFrom.dY;
                x2 = screenDw + swapTo.Column * tileWidth + swapTo.dX;
                y2 = screenDh + swapTo.Row * tileWidth + swapTo.dY;

                //tile destinations
                x1d = screenDw + swapFrom.Column * tileWidth;
                y1d = screenDh + swapFrom.Row * tileWidth;
                x2d = screenDw + swapTo.Column * tileWidth;
                y2d = screenDh + swapTo.Row * tileWidth;

                //next step positions
                x1n = stepValue(x1, x1d, modifier);
                y1n = stepValue(y1, y1d, modifier);
                x2n = stepValue(x2, x2d, modifier);
                y2n = stepValue(y2, y2d, modifier);

                swapFrom.dX += x1n - x1;
                swapFrom.dY += y1n - y1;
                swapTo.dX += x2n - x2;
                swapTo.dY += y2n - y2;
                gField.Tiles.set(gField.Tiles.indexOf(swapFrom), swapFrom);
                gField.Tiles.set(gField.Tiles.indexOf(swapTo), swapTo);

                if (x1d == x1n && y1d == y1n && x2d == x2n && y2d == y2n) {
                    gField.clearSelected();

                    gameMode = Selection;
                }
                break;
            case Explode:
                boolean finishExplosions = true;
                for (int i = 0; i < gField.cols; i++) {
                    for (int j = 0; j < gField.cols; j++) {
                        if (gField.getRemoveState(i, j)) {
                            Tile t = gField.getTile(i, j);
                            t.explodePhase += Constants.EXPLODE_SPEED * modifier;
                            if (t.explodePhase >= Constants.EXPLODE_END) {
                                t.explodePhase = Constants.EXPLODE_END;
                            } else {
                                finishExplosions = false;
                            }
                            gField.Tiles.set(gField.Tiles.indexOf(t), t);

                        }
                    }
                }
                if (finishExplosions) {
                    gField.moveDown(tileWidth);
                    gameMode = FallDown;
                }
                break;
            case FallDown:
                if (!gField.fall(modifier)) {
                    if (gField.match()) {
                        gField.scoreSeqModifier++;
                        gameMode = Explode;
                    } else {
                        gField.saveTiles();
                        if (gField.availableMoves > 0) {
                            gameMode = Selection;
                        } else {
                            gField.matchAll();
                            gameMode = Explode;
                        }
                        gField.scoreSeqModifier = 1;
                    }
                }
                break;
        }
        gField.scoreTopups = 0;
        for(int i = 0; i < gField.TopUps.size(); i++){
            gField.scoreTopups += gField.TopUps.get(i).score;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas != null && ResourceManager.getInstance().scaledImagesLoaded) {
            canvas.drawColor(Color.BLACK);
            if(firstBack){

                firstBack = false;
            }
            drawBack(canvas);
            drawResources(canvas);
            drawScore(canvas);
            drawFieldSquares(canvas);
            drawBalls(canvas);

            drawTopUps(canvas);
//            drawBalls(canvas);
            //drawMovesLeft(canvas);

            //drawDebugArrays(canvas);
        }
    }

    private void drawBack(Canvas canvas){
        Rect back = new Rect();
        back.set(0,0,sw,sh);
        Bitmap back1;
//        back1 = ResourceManager.getInstance().
//        back1 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.rectangle71),100,100, true);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.back3),null, back,null);
    }

    private void drawFieldSquares(Canvas canvas){
        for (int col = 0; col < gField.cols; col++){
            for (int row = 0; row < gField.cols; row++){
                if((col + row) % 2 == 0 ){
                    fieldRect.set(
                            screenDw + col * tileWidth,
                            screenDh + row * tileWidth,
                            screenDw + col * tileWidth + tileWidth,
                            screenDh + row * tileWidth + tileWidth
                    );
                    canvas.drawRect(fieldRect, fieldPaint);
                }
            }
        }
    }

    private void drawTopUps(Canvas canvas) {
        for (int i = 0; i < gField.TopUps.size(); i++)
            gField.TopUps.get(i).draw(canvas, topupPaint);
    }

    private void drawBalls(Canvas canvas){
        for(int i = 0; i < gField.Tiles.size(); i++){
            Bitmap b;
            Tile t = gField.Tiles.get(i);
            //if tile higher than board - skip it
            if(t.Row * tileWidth + t.dY < - tileWidth)
                continue;

            double jumpModifier = 0;

            if(t.Selected){
                jumpModifier = Math.sin(t.jumpPhase) * tileWidth / 6;
            }
            if (t.fallenPhase > 0){
                jumpModifier = Math.sin(t.fallenPhase) * tileWidth / 6;
            }
            if(t.Type >= ResourceManager.getInstance().tileImages.size())
                continue;
            b = ResourceManager.getInstance().tileImages.get(t.Type);

            if(t.explodePhase > 0){
                transformRect.set(
                        screenDw + t.Column * tileWidth + t.dX + (int)(tileWidth / 2 * t.explodePhase / Constants.EXPLODE_END),
                        screenDh + t.Row * tileWidth + (int)(tileWidth / 2 * t.explodePhase / Constants.EXPLODE_END) + t.dY,
                        screenDw + t.Column * tileWidth + tileWidth + t.dX - (int)(tileWidth / 2 * t.explodePhase / Constants.EXPLODE_END),
                        screenDh + t.Row * tileWidth + tileWidth - (int)(tileWidth / 2 * t.explodePhase / Constants.EXPLODE_END) + t.dY);
                canvas.drawBitmap(b, null, transformRect, null);
            } else if(jumpModifier > 0) {
                transformRect.set(
                        screenDw + t.Column * tileWidth + t.dX - (int)(jumpModifier / 4),
                        screenDh + t.Row * tileWidth + (int)(jumpModifier / 2) + t.dY,
                        screenDw + t.Column * tileWidth + tileWidth + t.dX + (int)(jumpModifier / 4),
                        screenDh + t.Row * tileWidth + tileWidth + t.dY);
                canvas.drawBitmap(b, null, transformRect, null);
            } else {
                canvas.drawBitmap(b,
                        screenDw + t.Column * tileWidth + t.dX,
                        screenDh + t.Row * tileWidth + t.dY + (int) jumpModifier, null);

            }

        }
    }

    private void drawMovesLeft(Canvas canvas){
        Paint movesPaint = new Paint();
        movesPaint.setTextSize(50);
        movesPaint.setColor(Color.WHITE);
        canvas.drawText(String.valueOf(gField.availableMoves),20, sh - 100, movesPaint);
    }

    private void drawDebugArrays(Canvas canvas){
        int size = 20;
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(size);
        for(int i = 0; i < gField.cols; i++){
            for(int j = 0; j < gField.cols; j++){
                canvas.drawText(String.valueOf(gField.getTileId(j, i)), i * size * 2, 3* size +j * size * 2, textPaint);

                canvas.drawText(String.valueOf(gField.getTileType(j,i)), size * 2 * (gField.cols + 2)+ i * size * 2, 3*size + j * size * 2, textPaint);

                for(int k = 0; k < gField.Tiles.size(); k++){
                    Tile t = gField.Tiles.get(k);
                    if(t.Column == i && t.Row == j){
                        canvas.drawText(String.valueOf(k), size * 2 * (2*gField.cols + 4)+ i * size * 2, 3*size + j * size * 2, textPaint);
                        canvas.drawText(String.valueOf(t.Type), size * 2 * (3*gField.cols + 6)+ i * size * 2, 3*size + j * size * 2, textPaint);

                    }
                }
            }
        }
    }

    private void drawResources(Canvas canvas){
        int colSize = Math.min(sw, sh) / Constants.BallTypes(gameType);
        for(int i = 0; i < gField.resCount.length; i++){
            //resource image
            transformRect.set(
                    i * colSize + colSize / 2 - tileWidth / 4,
                    tileWidth  / 8,
                    i * colSize + colSize / 2 + tileWidth / 4,
                    tileWidth / 8 * 5
            );

            if(i < ResourceManager.getInstance().tileImages.size())
                canvas.drawBitmap(ResourceManager.getInstance().tileImages.get(i), null, transformRect, null);

            //resource counter
            canvas.drawText(
                    Utils.formatCounter(gField.resCount[i]),
                    i * colSize + colSize / 2,
                    tileWidth * 6 / 8 + resourceCounterHeight,
                    resourceCounterPaint);

            //resource multiplier with shadow
            canvas.drawText(
                    String.format(Locale.ENGLISH,"x%d", gField.scoreForType(i)),
                    i*colSize + colSize/2 + tileWidth / 7 - 2,
                    tileWidth * 3 / 8 - tileWidth / 20 + 2,
                    resMultiplierShadow);
            canvas.drawText(
                    String.format(Locale.ENGLISH,"x%d", gField.scoreForType(i)),
                    i*colSize + colSize/2 + tileWidth / 7,
                    tileWidth * 3 / 8 - tileWidth / 20,
                    resourceMultiplierPaint);
        }
    }

    private void drawScore(Canvas canvas){
        String scoreText = df.format(gField.score - gField.scoreTopups);
        canvas.drawText(scoreText, sw/2, screenDh/2 + scorePaint.getTextSize() / 2 + tileWidth / 2, scorePaint);
        scoreWidth = (int)scorePaint.measureText(scoreText);
    }

    private int stepValue(int source, int dest, double modifier){
        int direction;
        if(Math.abs(dest - source) <= Constants.SWAP_STEP * modifier){
            return dest;
        } else {
            if(source > dest){
                direction = -1;
            } else {
                direction = 1;
            }
            return source + (int)(Constants.SWAP_STEP * direction * modifier);
        }
    }

    private void swapMode(Tile from, Tile to){
        if(from == null || to == null)
            return;
        gameMode = Swap;
        swapFrom = from;
        swapTo = to;
        swapFrom.jumpPhase = 0;
        swapTo.jumpPhase = 0;
        gField.Tiles.set(gField.Tiles.indexOf(swapFrom), swapFrom);
        gField.Tiles.set(gField.Tiles.indexOf(swapTo), swapTo);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();

        switch(event.getAction()){
            case MotionEvent.ACTION_MOVE:
                if(SelectedTileIndex >= 0 && gameMode == Selection){
                    Tile t = gField.Tiles.get(SelectedTileIndex);
                    t.dX = t.dX + (x - prevX);
                    t.dY = t.dY + (y - prevY);
                    Tile moveTo = gField.getNeighbour(t.Column, t.Row, t.dX, t.dY, Constants.MOVE_THRESHOLD);
                    if(Math.abs(t.dX) > Constants.MOVE_THRESHOLD || Math.abs(t.dY) > Constants.MOVE_THRESHOLD) {
                        t.Selected = false;
                        if(moveTo != null){
                            swapMode(t, moveTo);
                        }
                    }
                    gField.Tiles.set(SelectedTileIndex, t);
                }
                break;
            case MotionEvent.ACTION_DOWN:
                if(gameMode == Selection) {
                    moveTileCol = (int) Math.floor((x - screenDw) / tileWidth);
                    moveTileRow = (int) Math.floor((y - screenDh) / tileWidth);

                    for (int i = 0; i < gField.Tiles.size(); i++) {
                        Tile t = gField.Tiles.get(i);
                        if (t.Column == moveTileCol && t.Row == moveTileRow) {
                            t.Selected = !t.Selected;
                            t.jumpPhase = 0;
                            gField.Tiles.set(i, t);
                        } else {
                            if (t.Selected) {
                                if(gField.isNeighbour(t, moveTileRow, moveTileCol)) {
                                    swapMode(t, gField.getTile(moveTileRow, moveTileCol));
                                } else {
                                    t.Selected = false;
                                    t.jumpPhase = 0;
                                    gField.Tiles.set(i, t);
                                }
                            }
                        }

                    }
                    SelectedTileIndex = gField.getTileId(moveTileRow, moveTileCol);
                }
                break;
            case MotionEvent.ACTION_UP:
                if(SelectedTileIndex >= 0 && gameMode == Selection) {
                    Tile t = gField.Tiles.get(SelectedTileIndex);

                    t.dX = 0;
                    t.dY = 0;
                    gField.Tiles.set(SelectedTileIndex, t);

                    SelectedTileIndex = -1;
                }
                break;
        }

        prevX = x;
        prevY = y;

        return true;//super.onTouchEvent(event);
    }

    private void setScreenDimensions(int width, int height){
        sw = width;
        sh = height;
        tileWidth = Math.min(sw, sh) / Constants.Columns(gameType);
        ResourceManager.getInstance().scaleImages(tileWidth);
        if (sw > sh){
            screenDh = 0;
            screenDw = (sw - sh)/2;
        } else {
            screenDw = 0;
            screenDh = (sh - sw)/2;
        }
    }
}
