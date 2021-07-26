package com.titaniu.projectapptech.Data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {HighScore.class, Resource.class}, version = 1, exportSchema = false)
public abstract class Db extends RoomDatabase {
    public abstract hsDao hsDao();

    private static Db INSTANCE;

    static Db getDb(final Context context){
        if(INSTANCE == null){
            synchronized (Db.class){
                INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        Db.class,
                        "hs_database")
                        .allowMainThreadQueries()
                        //.fallbackToDestructiveMigration()
                        .build();
            }
        }
        return INSTANCE;
    }
}