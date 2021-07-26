package com.titaniu.projectapptech;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.titaniu.projectapptech.Data.HighScoresRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GameStats extends Activity implements AsyncStatsResponse {

    private showWeekStats weekAsync = new showWeekStats();
    private showMonthStats monthAsync = new showMonthStats();
    private showYearStats yearAsync = new showYearStats();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_stats);
        Intent intent = getIntent();
        GameType gType = (GameType) intent.getSerializableExtra("GameType");

        TextView statsTitle = findViewById(R.id.statsTitle);
        TextView labelWeek = findViewById(R.id.labelWeek);
        TextView labelMonth = findViewById(R.id.labelMonth);
        TextView labelYear = findViewById(R.id.labelYear);
        Button backBtn = findViewById(R.id.buttonBackFromStats);
        StatisticParams chartParams = new StatisticParams();
        int primaryColor;

        switch(gType){
            case Easy:
                statsTitle.setText(String.format(getString(R.string.stats_title), getString(R.string.easy_label)));
                primaryColor = getResources().getColor(R.color.easy);
                chartParams.color = ContextCompat.getColor(this, R.color.easy);
                backBtn.setBackground(getResources().getDrawable(R.drawable.rounded_back_easy));
                break;
            case Medium:
                statsTitle.setText(String.format(getString(R.string.stats_title), getString(R.string.expert_label)));
                primaryColor = getResources().getColor(R.color.expert);
                chartParams.color = ContextCompat.getColor(this, R.color.expert);
                backBtn.setBackground(getResources().getDrawable(R.drawable.rounded_back_expert));
                break;
            case Hard:
                statsTitle.setText(String.format(getString(R.string.stats_title), getString(R.string.master_label)));
                primaryColor = getResources().getColor(R.color.master);
                chartParams.color = ContextCompat.getColor(this, R.color.master);
                backBtn.setBackground(getResources().getDrawable(R.drawable.rounded_back_master));
                break;

            default:
                primaryColor = getResources().getColor(R.color.white);
                break;

        }

        statsTitle.setTextColor(primaryColor);
        labelWeek.setTextColor(primaryColor);
        labelMonth.setTextColor(primaryColor);
        labelYear.setTextColor(primaryColor);
        backBtn.setTextColor(primaryColor);
        backBtn.setTextScaleX(0.8f);
        backBtn.setHeight(Utils.DpToPx(this,40));

        chartParams.repo = ResourceManager.getInstance().hsRepo;
        chartParams.gameType = gType;
        chartParams.timestamp = System.currentTimeMillis();

        weekAsync.delegate = this;
        weekAsync.execute(chartParams);

        monthAsync.delegate = this;
        monthAsync.execute(chartParams);

        yearAsync.delegate = this;
        yearAsync.execute(chartParams);

    }

    public void back(View view){
        finish();
    }

    @Override
    public void processFinish(BarData data, final ArrayList<String> labels, int color, String chartType) {
        BarChart chart;
        Description desc = new Description();
        desc.setText("");

        switch (chartType){
            case "week":
                chart = findViewById(R.id.weekChart);
                data.setValueFormatter(new ScoreFormatter());
                break;
            case "month":
                chart = findViewById(R.id.monthChart);
                break;
            case "year":
                chart = findViewById(R.id.yearChart);
                break;
            default:
                return;
        }

        chart.setDrawGridBackground(false);
        chart.setDescription(desc);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setDrawLabels(false);
        chart.getXAxis().setTextColor(color);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return labels.get((int)value);
            }
        });
        chart.getLegend().setEnabled(false);
        chart.setData(data);
        chart.animateXY(1000,2000);
        chart.invalidate();
    }

    private static class showYearStats extends AsyncTask<StatisticParams,Void,Void> {
        AsyncStatsResponse delegate = null;
        private BarData _data;
        private Calendar _c;
        private int _color;
        private ArrayList<String> _labels;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            _data = null;
            _c = Calendar.getInstance();
        }

        @Override
        protected Void doInBackground(StatisticParams... params) {
            StatisticParams config = params[0];
            _color = config.color;

            //Log.d("Match3", "Chart data started");

            if(config.repo != null){
                _c.setTimeInMillis(config.timestamp);

                _labels = new ArrayList<>();
                ArrayList<BarEntry> vs = new ArrayList<>();

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM", Locale.US);

                for(int i = 0; i < 12; i++) {
                    Calendar today = Calendar.getInstance();
                    today.set(_c.get(Calendar.YEAR), i+1, 1);
                    long score = config.repo.getScoreMonth(
                            config.gameType,
                            today.getTimeInMillis());
                    //Log.d("Match3", "Score ("+i+") " + score);
                    BarEntry entry = new BarEntry(i, score, dateFormat.format(today.getTime()));

                    _labels.add(dateFormat.format(today.getTime()));
                    vs.add(entry);
                }

                BarDataSet bds = new BarDataSet(vs, "Score");
                bds.setColor(config.color);
                //ArrayList<BarDataSet> datasets = new ArrayList<>();
                //datasets.add(bds);

                _data = new BarData(bds);
                _data.setDrawValues(false);

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(_data != null){
                delegate.processFinish(_data, _labels, _color, "year");
            }
        }
    }

    private static class showMonthStats extends AsyncTask<StatisticParams,Void,Void>{
        AsyncStatsResponse delegate = null;
        private BarData _data;
        private Calendar _c;
        private int _color;
        private ArrayList<String> _labels;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            _data = null;
            _c = Calendar.getInstance();
        }

        @Override
        protected Void doInBackground(StatisticParams... params) {
            StatisticParams config = params[0];
            _color = config.color;

            Log.d("Match3", "Chart data started");

            if(config.repo != null){
                _c.setTimeInMillis(config.timestamp);

                _labels = new ArrayList<>();
                ArrayList<BarEntry> vs = new ArrayList<>();

                SimpleDateFormat dateFormat = new SimpleDateFormat("d", Locale.US);

                for(int i = 0; i < _c.getMaximum(Calendar.DAY_OF_MONTH); i++) {
                    Calendar today = Calendar.getInstance();
                    today.set(_c.get(Calendar.YEAR), _c.get(Calendar.MONTH), i + 1);
                    long score = config.repo.getScoreDay(
                            config.gameType,
                            today.getTimeInMillis());
                    //Log.d("Match3", "Score ("+i+") " + score);
                    BarEntry entry = new BarEntry(i, score, dateFormat.format(today.getTime()));
                    _labels.add(dateFormat.format(today.getTime()));
                    vs.add(entry);
                }

                BarDataSet bds = new BarDataSet(vs, "Score");
                bds.setColor(config.color);
                //ArrayList<BarDataSet> datasets = new ArrayList<>();
                //datasets.add(bds);

                _data = new BarData(bds);
                _data.setDrawValues(false);

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(_data != null){
                delegate.processFinish(_data, _labels, _color,"month");

            }
        }
    }

    private static class showWeekStats extends AsyncTask<StatisticParams,Void,Void> {
        AsyncStatsResponse delegate = null;
        //private BarChart _chart;
        private BarData _data;
        private Calendar _c;
        private int _color;
        private ArrayList<String> _labels;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            _data = null;
            _c = Calendar.getInstance();
        }

        @Override
        protected Void doInBackground(StatisticParams... params) {
            StatisticParams config = params[0];
            //_chart = config.chart;
            _color = config.color;

            Log.d("Match3", "Chart data started");

            if(config.repo != null){
                _c.setTimeInMillis(config.timestamp);

                _labels = new ArrayList<>();
                ArrayList<BarEntry> vs = new ArrayList<>();

                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE", Locale.US);

                for(int i = 0; i < 7; i++) {
                    long score = config.repo.getScoreDay(
                            config.gameType,
                            _c.getTimeInMillis());
                    Log.d("Match3", "Score ("+i+") " + score);
                    BarEntry entry = new BarEntry(6-i, score);
                    _labels.add(0, dateFormat.format(_c.getTime()));
                    vs.add(entry);
                    _c.add(Calendar.DAY_OF_MONTH, -1);
                }


                BarDataSet bds = new BarDataSet(vs, "Score");
                bds.setColor(config.color);
                //ArrayList<BarDataSet> datasets = new ArrayList<>();
                //datasets.add(bds);

                _data = new BarData(bds);
                _data.setValueTextColor(_color);
                _data.setValueTextSize(12);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(_data != null){
                delegate.processFinish(_data, _labels, _color, "week");

            }
        }
    }

    private class StatisticParams {
        GameType gameType;
        HighScoresRepository repo;
        long timestamp;
        int color;
    }

    private static class ScoreFormatter implements IValueFormatter {

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            if(value == 0)
                return "";
            return Utils.formatCounter((long) value);
        }
    }
}
