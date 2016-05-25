package com.tcl.statistics.client;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tcl.statisticsdk.agent.StatisticsAgent;
import com.tcl.statisticsdk.client.R;

public class ActivityTwo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_two);
        init();
        StatisticsAgent.init(getApplicationContext());
        StatisticsAgent.setDebugMode(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatisticsAgent.onResume(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatisticsAgent.onPause(getApplicationContext());
    }

    private void init() {
        /**/
        findViewById(R.id.btn_click_event_two).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.tcl.statisticsdk.agent.StatisticsAgent.onEvent(ActivityTwo.this.getApplicationContext(),
                        "Activity_two_btn_event");
            }

        });
    }

}
