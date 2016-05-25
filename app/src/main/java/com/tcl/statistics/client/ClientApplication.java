package com.tcl.statistics.client;

import android.app.Application;

import com.tcl.statisticsdk.agent.StatisticsAgent;

/**
 * Created by lenvo on 2016/5/25.
 */
public class ClientApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StatisticsAgent.init(getApplicationContext());
        StatisticsAgent.setDebugMode(true);
    }
}
