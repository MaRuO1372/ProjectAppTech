package com.titaniu.projectapptech.Data;

import android.app.Application;
import android.os.AsyncTask;

import com.titaniu.projectapptech.Constants;
import com.titaniu.projectapptech.GameType;

import java.util.Calendar;
import java.util.List;

public class HighScoresRepository {
    private hsDao mDao;


    public HighScoresRepository(Application app){
        Db db = Db.getDb(app);
        mDao = db.hsDao();
    }

    public void deleteAllScores(){
        mDao.deleteAllScores();
    }

    public void deleteAllRes(){
        mDao.deleteAllRes();
    }

    public long getScoreDay(GameType type, long timestamp){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        return mDao.score(
                type.getInt(),
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.MONTH),
                c.get(Calendar.YEAR)).total;
    }

    public long getScoreMonth(GameType type, long timestamp){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        return mDao.score(
                type.getInt(),
                c.get(Calendar.MONTH),
                c.get(Calendar.YEAR)).total;
    }

    public int[] getResourcesDay(GameType type, long timestamp){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        List<TotalRes> resources = mDao.res(
                type.getInt(),
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.MONTH),
                c.get(Calendar.YEAR));
        int[] result = new int[Constants.BallTypes(type)];
//        String resString = "";
        for (int i = 0; i < resources.size(); i++) {
            TotalRes r = resources.get(i);
            result[r.BallType] += r.Qty;
        }
//        for (int i = 0; i < result.length; i++){
//            resString += "b"+String.valueOf(i)+">"+String.valueOf(result[i])+"|";
//        }

//        Log.d("Match3","ResLoad "+resString);
        return result;
    }

    public void insert (GameType gameType, long timestamp, long score){
        HighScore hs = new HighScore();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        hs.gameType = gameType.getInt();
        hs.day = c.get(Calendar.DAY_OF_MONTH);
        hs.month = c.get(Calendar.MONTH);
        hs.year = c.get(Calendar.YEAR);
        hs.score = score;
        new insertAsyncTask(mDao).execute(hs);
    }

    public void insert (GameType gameType, long timestamp, int[] res){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        Resource[] resources = new Resource[res.length];
        //      String resString = "";
        for(int i = 0; i < res.length; i++){
            Resource r = new Resource();
            r.ballType = i;
            r.day = c.get(Calendar.DAY_OF_MONTH);
            r.month = c.get(Calendar.MONTH);
            r.year = c.get(Calendar.YEAR);
            r.gameType = gameType.getInt();
            r.qty = res[i];
            resources[i] = r;
            //          resString += String.valueOf(res[i])+"|";
        }
        //      Log.d("Match3", "ResSave:"+resString);
        new insertResAsync(mDao).execute(resources);
    }

    private static class insertResAsync extends AsyncTask<Resource, Void, Void> {
        private hsDao mAsyncTaskDao;

        insertResAsync(hsDao dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Resource... resources){
            mAsyncTaskDao.insertRes(resources);
            return null;
        }
    }

    private static class insertAsyncTask extends AsyncTask<HighScore, Void, Void>{
        private hsDao mAsyncTaskDao;

        insertAsyncTask(hsDao dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(HighScore... highScores) {
            mAsyncTaskDao.insert(highScores[0]);
            return null;
        }
    }
}

