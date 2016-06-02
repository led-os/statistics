package com.tcl.statistics.client;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.tcl.statisticsdk.agent.StatisticsAgent;
import com.tcl.statisticsdk.client.R;

public class UploadLogActivity extends AppCompatActivity {

    private TextView mEdit;
    private static final int MSG_SHOW_RS = 999;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upload_log);

        initComponent();

        initLog();

    }

    private void initComponent() {

        mEdit = (TextView) findViewById(R.id.et_showLog);

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                int what = msg.what;

                switch (what) {
                    case MSG_SHOW_RS:
                        if (msg.obj != null) {
                            mEdit.setText(msg.obj.toString());
                        }
                        break;
                }
            }
        };


    }

    private void initLog() {

        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();

                String result = StatisticsAgent.getCurrentUploadLog();

                Message msg = Message.obtain();
                msg.obj = result;
                msg.what = MSG_SHOW_RS;
                mHandler.sendMessage(msg);

            }
        };
        thread.start();

    }
}
