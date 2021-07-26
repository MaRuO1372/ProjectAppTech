package com.titaniu.projectapptech;

import android.util.Log;

import java.util.ArrayList;

public class GameField {
    public ArrayList<Tile> Tiles;
    private int[][] tileIDs;
    private int[][] tileTypes;
    public int cols;
    private int[][] removeTile;
    private GameType gType;
    public int availableMoves;
    public int[] resCount;
    public long score;
    public int scoreTopups;
    public int scoreSeqModifier = 1;
    public ArrayList<Topup> TopUps;

    GameField(GameType gType){
        cols = Constants.Columns(gType);
        this.gType = gType;
        resCount = new int[Constants.BallTypes(gType)];
        tileIDs = new int[cols][cols];
        tileTypes = new int[cols][cols];
    }

    public void init(){
        Tiles = new ArrayList<>();
        TopUps = new ArrayList<>();
        for(int i = 0; i < cols; i++){
            for(int j = 0; j < cols; j++){
                Tile t = new Tile();
                t.Column = j;
                t.Row = i;
                t.Type = (int)(Math.random() * Constants.BallTypes(gType));
                t.dX = 0;
                t.dY = 0;
                Tiles.add(t);
                tileIDs[i][j] = Tiles.indexOf(t);
                tileTypes[i][j] = t.Type;
            }
        }
        availableMoves = movesCount();
    }

    public void init(int[][] tiles){
        Tiles = new ArrayList<>();
        TopUps = new ArrayList<>();
        for(int i = 0; i < cols; i++){
            for(int j = 0; j < cols; j++){
                Tile t = new Tile();
                t.Column = j;
                t.Row = i;
                t.Type = tiles[i][j];
                t.dX = 0;
                t.dY = 0;
                Tiles.add(t);
                tileIDs[i][j] = Tiles.indexOf(t);
                tileTypes[i][j] = t.Type;
            }
        }
        availableMoves = movesCount();
    }

    public int getTileId(int row, int col){
        if(fits(row, col)){
            return tileIDs[row][col];
        }
        return -1;
    }

    public Tile getTile(int row, int col){
        int tileId = getTileId(row, col);
        if(tileId >= 0){
            return Tiles.get(tileId);
        }
        return null;
    }

    public int getTileType(int row, int col){
        if(fits(row, col)){
            return tileTypes[row][col];
        }
        return -1;
    }

    public boolean getRemoveState(int row, int col) {
        return fits(row, col) && removeTile[row][col] == 1;
    }

    public Tile getNeighbour(int x, int y, int dx, int dy, int threshold){
        //определяет, есть ли соседняя клетка в направлении dx, dy
        //возвращает ID соседа или -1, если нет соседа
        if(Math.abs(dx) <= threshold)
            dx = 0;
        if(Math.abs(dy) <= threshold)
            dy = 0;
        if(Math.abs(dx) > Math.abs(dy)){
            dx = dx / Math.abs(dx);
            dy = 0;
        } else {
            if(dy != 0){
                dy = dy / Math.abs(dy);
            }
            dx = 0;
        }
        int newX = x + dx;
        int newY = y + dy;

        if (fits(newY, newX) && (dx != 0 || dy != 0)){
            return Tiles.get(tileIDs[newY][newX]);
        }
        return null;
    }

    public boolean isNeighbour(Tile t, int row, int col){
        return Math.abs(t.Column - col) + Math.abs(t.Row - row) == 1;
    }

    private boolean fits(int row, int col){
        return row >= 0 && row < cols && col >= 0 && col < cols;
    }

    public void swap(Tile a1, Tile a2){
        int i1 = Tiles.indexOf(a1);
        int i2 = Tiles.indexOf(a2);
        tileTypes[a1.Row][a1.Column] = a2.Type;
        tileTypes[a2.Row][a2.Column] = a1.Type;
        tileIDs[a1.Row][a1.Column] = i2;
        tileIDs[a2.Row][a2.Column] = i1;
        if(removeTile != null) {
            int removeState1 = removeTile[a1.Row][a1.Column];
            removeTile[a1.Row][a1.Column] = removeTile[a2.Row][a2.Column];
            removeTile[a2.Row][a2.Column] = removeState1;
        }
        Tile b1 = Tiles.get(i1);
        Tile b2 = Tiles.get(i2);
        int tCol = b1.Column;
        int tRow = b1.Row;
        b1.Column = b2.Column;
        b1.Row = b2.Row;
        b1.Selected = false;
        b1.dX = 0;
        b1.dY = 0;
        b2.Column = tCol;
        b2.Row = tRow;
        b2.Selected = false;
        b2.dX = 0;
        b2.dY = 0;
        Tiles.set(i1, b1);
        Tiles.set(i2, b2);
    }

    public void moveDown(int rowSize){
        long scoreDelta = 0;
        for(int r = 0; r < cols; r++){
            for(int c = 0; c < cols; c++){
                if(getRemoveState(r,c)){
                    Tile t = getTile(r,c);
                    int s = scoreForType(t.Type) * scoreSeqModifier;
                    Topup topup = new Topup(r, c, t.Type, s);
                    TopUps.add(topup);
                    scoreDelta += s;
                }
            }
        }

        boolean changed;
        int firstRow = 0;
        do {
            changed = false;
            for (int r = cols - 2; r >= 0; r--) {
                for (int c = 0; c < cols; c++) {
                    if (getRemoveState(r + 1, c) && !getRemoveState(r,c)) {
                        Tile t = getTile(r, c);
                        int dY = t.dY;
                        swap(t, getTile(r + 1, c));
                        Tile f = getTile(r + 1, c);
                        f.dY = dY - rowSize;
                        Tiles.set(Tiles.indexOf(f), f);
                        changed = true;
                        firstRow = r;
                    }
                }
            }
        }while(changed);

        //add new balls
        int[] resDelta = new int[resCount.length];

        for(int r = 0; r < cols; r++) {
            for(int c = 0; c < cols; c++){
                if(getRemoveState(r, c)){
                    Tile t = getTile(r,c);
                    resCount[t.Type]++;
                    resDelta[t.Type]++;

                    t.Type = (int)(Math.random() * Constants.BallTypes(gType));
                    t.dX = 0;
                    t.dY = - (firstRow + 1) * rowSize;
                    t.explodePhase = 0;
                    Tiles.set(Tiles.indexOf(t), t);
                    removeTile[r][c] = 0;
                    tileTypes[r][c] = t.Type;
                }
            }
        }
        score += scoreDelta;
        //scoreTopups += scoreDelta;
        ResourceManager.getInstance().hsRepo.insert(gType, System.currentTimeMillis(), resDelta);
        ResourceManager.getInstance().hsRepo.insert(gType, System.currentTimeMillis(), scoreDelta);
        availableMoves = movesCount();
    }

    public boolean fall(double modifier){
        boolean stillFalling = false;
        for(int i = 0; i < Tiles.size();i++){
            Tile t = Tiles.get(i);
            if(t.dY < 0){
                int inc = (int)(Constants.FALL_SPEED * modifier);
                if(t.dY + inc >= 0){
                    t.dY = 0;
                    t.fallenPhase = Math.PI;
                } else {
                    t.dY += Constants.FALL_SPEED * modifier;
                    stillFalling = true;
                }
                Tiles.set(i, t);
            }
        }
        return stillFalling;
    }

    public void updateFallen(double modifier){
        for(int i = 0; i < Tiles.size(); i++){
            Tile t = Tiles.get(i);
            if(t.fallenPhase > 0){
                double delta = Constants.JUMP_SPEED * modifier;
                if(t.fallenPhase - delta < 0){
                    t.fallenPhase = 0;
                } else {
                    t.fallenPhase -= delta;
                }
                Tiles.set(i, t);
            }
        }
    }

    public void clearSelected() {
        for(int i = 0; i < Tiles.size(); i++){
            Tile t = Tiles.get(i);
            if(t.Selected){
                t.Selected = false;
                Tiles.set(i, t);
            }
        }
    }

    public void matchAll(){
        for(int i = 0; i < cols; i++)
            for(int j = 0; j < cols; j++)
                removeTile[i][j] = 1;
    }

    public boolean match(){
        //int[][] neighbours = new int[cols][cols];
        removeTile = new int[cols][cols];
        boolean result = false;
        //count number of neighbours with the same type
        for(int i = 0; i < cols; i++){
            for(int j = 0; j < cols; j++){
                if(removeTile[i][j] == 0) {
                    int h = 0, v = 0;
                    if (i > 0) {
                        //check up
                        if (tileTypes[i][j] == tileTypes[i - 1][j])
                            h++;
                    }
                    if (j > 0) {
                        //check left
                        if (tileTypes[i][j] == tileTypes[i][j - 1])
                            v++;
                    }
                    if (i < cols - 1) {
                        // check bottom
                        if (tileTypes[i][j] == tileTypes[i + 1][j])
                            h++;
                    }
                    if (j < cols - 1) {
                        //check right
                        if (tileTypes[i][j] == tileTypes[i][j + 1])
                            v++;
                    }
                    //neighbours[i][j] = n;
                    if (h > 1 || v > 1) {
                        result = true;
                        checkNeighbours(i, j, tileTypes[i][j]);
                    }
                }
            }
        }
        return result;
    }

    private boolean hasMoves(int[][]array){
        for(int r = 0; r < cols; r++){
            for(int c = 0; c < cols; c++){
                int h = 0, v = 0;
                if (r > 0) {
                    //check up
                    if (array[r][c] == array[r - 1][c])
                        h++;
                }
                if (c > 0) {
                    //check left
                    if (array[r][c] == array[r][c - 1])
                        v++;
                }
                if (r < cols - 1) {
                    // check bottom
                    if (array[r][c] == array[r + 1][c])
                        h++;
                }
                if (c < cols - 1) {
                    //check right
                    if (array[r][c] == array[r][c + 1])
                        v++;
                }
                //neighbours[i][j] = n;
                if (h > 1 || v > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private int movesCount(){
        int count = 0;
        int[][] types = new int[cols][cols];
        StringBuilder arr = new StringBuilder();
        for(int r = 0; r < cols; r++){
            for(int c = 0; c < cols; c++){
                types[r][c] = tileTypes[r][c];
                arr.append(types[r][c]);
            }
        }
        Log.i("Match3", "Tile Types: "+ arr.toString());
        StringBuilder matches = new StringBuilder();
        for(int r = 0; r < cols - 1; r++){
            for(int c = 0; c < cols - 1; c++){
                //swap down
                int temp = types[r][c];
                types[r][c] = types[r+1][c];
                types[r+1][c] = temp;
                //check matches
                if(hasMoves(types)) {
                    count++;
                    matches.append("(").append(r).append("+:").append(c).append(")");
                }
                //reverse swap
                types[r+1][c] = types[r][c];
                //types[r][c] = temp;

                //swap right
                types[r][c] = types[r][c+1];
                types[r][c+1] = temp;

                //check matches
                if(hasMoves(types)) {
                    count++;
                    matches.append("(").append(r).append(":").append(c).append("+)");
                }
                //reverse swap
                types[r][c+1] = types[r][c];
                types[r][c] = temp;
            }
        }
        //two last swaps for types[cols-1][cols-1]
        //swap up
        int temp = types[cols-1][cols-1];
        types[cols-1][cols-1] = types[cols-2][cols-1];
        types[cols-2][cols-1] = temp;
        //check matches
        if(hasMoves(types)) {
            count++;
            matches.append("(").append(cols-1).append("-:").append(cols-1).append(")");
        }
        //reverse swap
        types[cols-2][cols-1] = types[cols-1][cols-1];
        //types[cols-1][col1-1] = temp;

        //swap left
        types[cols-1][cols-1] = types[cols-1][cols-2];
        types[cols-1][cols-2] = temp;

        //check matches
        if(hasMoves(types)) {
            matches.append("(").append(cols-1).append(":").append(cols-1).append("-)");
            count++;
        }
        //don't need put it back as array its local
        Log.i("Match3", "Matches: "+matches.toString());
        return count;
    }

    private void checkNeighbours(int i, int j, int type){
        removeTile[i][j] = 1;

//        Tile t = Tiles.get(tileIDs[i][j]);
//        t.Selected = true;
//        Tiles.set(tileIDs[i][j], t);

        if(i > 0){
            if(removeTile[i-1][j] == 0 && tileTypes[i-1][j] == type){
                checkNeighbours(i-1, j, type);
            }
        }
        if(j > 0){
            if(removeTile[i][j-1] == 0 && tileTypes[i][j-1] == type){
                checkNeighbours(i, j-1, type);
            }
        }
        if(i < cols - 1){
            if(removeTile[i+1][j] == 0 && tileTypes[i+1][j] == type){
                checkNeighbours(i+1, j, type);
            }
        }
        if(j < cols - 1){
            if(removeTile[i][j+1] == 0 && tileTypes[i][j+1] == type){
                checkNeighbours(i, j+1, type);
            }
        }
    }

    public void saveTiles(){
        StringBuilder encoded = new StringBuilder();
        for(int r = 0; r < cols; r++){
            for(int c = 0; c < cols; c++)
                encoded.append(getTileType(r, c));
        }
        ResourceManager.getInstance().prefSaveString("tiles" + gType.toString(), encoded.toString());
        //saveResources();
        //saveScore();
    }

//    public void saveResources(){
//        StringBuilder sb = new StringBuilder();
//        for(int i = 0; i < resCount.length; i++){
//            sb.append(resCount[i]);
//            if(i < resCount.length - 1)
//                sb.append("|");
//        }
//        ResourceManager.getInstance().prefSaveString("res"+gType.toString(), sb.toString());

    //ResourceManager.getInstance().hsRepo.insert(gType, System.currentTimeMillis(), resCount);
//    }

//    public void saveScore(){
    //ResourceManager.getInstance().prefSaveLong("score"+gType.toString(), score);
//    }

    public void loadTiles(){
        String encoded;
        encoded = ResourceManager.getInstance().prefGetString("tiles"+gType.toString());
        if(encoded.length() == cols * cols){
            for(int r = 0; r < cols; r++) {
                for (int c = 0; c < cols; c++) {
                    Tile t = getTile(r, c);
                    t.Type = Integer.parseInt(encoded.substring(r * cols + c, r*cols + c + 1));
                    tileTypes[r][c] = t.Type;
                    Tiles.set(Tiles.indexOf(t), t);
                }
            }
        }
        availableMoves = movesCount();
        loadResources();
        loadScore();
    }

    private void loadResources(){
        resCount = ResourceManager.getInstance().hsRepo.getResourcesDay(gType, System.currentTimeMillis());
    }

    private void loadScore(){

        //score = ResourceManager.getInstance().prefGetLong("score"+gType.toString());
        score = ResourceManager.getInstance().hsRepo.getScoreDay(gType, System.currentTimeMillis());
    }

    public boolean saveExists(){
        String save = ResourceManager.getInstance().prefGetString("tiles"+gType.toString());
        return save.length() == cols * cols;
    }

    public int scoreForType(int t){
        int s = resCount[t];
        if( s < 10 ){
            return 1;
        } else if (s < 100) {
            return 5;
        } else if (s < 1000) {
            return 10;
        } else if (s < 10000) {
            return 50;
        } else {
            return 100;
        }
    }
}

