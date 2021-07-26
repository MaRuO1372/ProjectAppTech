package com.titaniu.projectapptech.Data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface hsDao {
    //
    // High Scores
    //
    @Insert
    void insert(HighScore hs);

    @Query("SELECT sum(score) AS total FROM high_scores WHERE gameType = :type AND day = :day AND month = :month AND year = :year")
    TotalScore score(int type, int day, int month, int year);

    @Query("SELECT sum(score) AS total FROM high_scores WHERE gameType = :type AND month = :month AND year = :year")
    TotalScore score(int type, int month, int year);

    @Query("SELECT sum(score) AS total FROM high_scores WHERE gameType = :type AND year = :year")
    TotalScore score(int type, int year);

    @Query("DELETE FROM high_scores WHERE gameType = :type AND day = :day AND month = :month AND year = :year" )
    void delete(int type, int day, int month, int year);

    @Query("DELETE FROM high_scores")
    void deleteAllScores();

    @Query("SELECT * FROM high_scores WHERE gameType = :type AND day = :day AND month = :month AND year = :year")
    List<HighScore> list(int type, int day, int month, int year);

    //
    // Resources
    //
    @Insert
    void insertRes(Resource... res);
    @Query("SELECT ballType AS BallType, SUM(qty) AS Qty FROM resources " +
            "WHERE gameType = :gameType AND day = :day AND month = :month AND year = :year " +
            "GROUP BY ballType, gameType, day, month, year")
    List<TotalRes> res(int gameType, int day, int month, int year);

    @Query("DELETE FROM resources")
    void deleteAllRes();
}
