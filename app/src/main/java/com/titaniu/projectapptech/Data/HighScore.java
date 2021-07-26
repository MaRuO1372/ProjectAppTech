package com.titaniu.projectapptech.Data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "high_scores")
public class HighScore {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int gameType;

    public int year;
    public int month;
    public int day;

    public long score;
}
