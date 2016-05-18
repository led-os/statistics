package com.tcl.statisticsdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tcl.statisticsdk.agent.StatisticsAgent;
import com.tcl.statisticsdk.util.LogUtils;

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
        findViewById(R.id.btn_test_error).setOnClickListener(this);
        findViewById(R.id.btn_switch_page).setOnClickListener(this);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                LogUtils.D("uncatch Exception");

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        // 页面onResume
        StatisticsAgent.onResume(getApplicationContext());

        System.currentTimeMillis();
    }


    @Override
    protected void onPause() {
        super.onPause();
        // 页面onPause
        StatisticsAgent.onPause(getApplicationContext());
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btn_click_event:

                StatisticsAgent.onEvent(getApplicationContext(), EVENT_NAME_1);

                break;
            case R.id.btn_switch_page_two:

                Intent intent = new Intent();
                intent.setClass(this, ActivityTwo.class);
                startActivity(intent);

                break;
            case R.id.btn_test_error:

                errorOccurs();

                break;
        }
    }

    private void errorOccurs() {

        int numberone = 1;
        int numbertwo = 0;

        int result = numberone / numbertwo;
    }

}
