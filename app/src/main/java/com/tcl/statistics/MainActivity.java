package com.tcl.statistics;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tcl.statistics.agent.StatisticsAgent;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EVENT_NAME_1 = "MAINACTIVITY_CLICK_BTN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }


    private void init() {
        StatisticsAgent.setDebugMode(true);

        findViewById(R.id.btn_click_event).setOnClickListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //页面onResume
        StatisticsAgent.onResume(getApplicationContext());
    }


    @Override
    protected void onPause() {
        super.onPause();
        //页面onPause
        StatisticsAgent.onPause(getApplicationContext());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_click_event) {
            StatisticsAgent.onEvent(getApplicationContext(), EVENT_NAME_1);
        }

    }
}
