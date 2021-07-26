package com.titaniu.projectapptech;

public enum GameType {
    Easy(1),
    Medium(2),
    Hard(3);

    private final int value;

    GameType(int val){
        value = val;
    }

    public int getInt(){
        return value;
    }
}
