package com.titaniu.projectapptech.Data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "resources")
public class Resource {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int year;
    public int month;
    public int day;

    public int gameType;

    public int ballType;

    public int qty;
}