package com.titaniu.projectapptech;

import com.github.mikephil.charting.data.BarData;

import java.util.ArrayList;

public interface AsyncStatsResponse{
    void processFinish(BarData data, ArrayList<String> labels, int color, String chartType);
}
