package com.titaniu.projectapptech;

public class Constants {
    public static int FALL_SPEED = 2000;
    public static int MOVE_THRESHOLD = 20;
    public static int SWAP_STEP = 700;
    public static int TOPUP_STEP = 1000;
    public static int JUMP_SPEED = 15;
    public static int EXPLODE_SPEED = 400;
    public static int EXPLODE_END = 100;

    public static int Columns(GameType type){
        switch(type){
            case Hard:
                return 8;
            default:
                return 7;
        }
    }

    public static int BallTypes(GameType type){
        switch (type){
            case Easy:
                return 5;
            case Medium:
                return 6;
            case Hard:
                return 7;
            default:
                return 6;
        }
    }
}
