package com.tcl.statistics.client;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.tcl.statisticsdk.agent.StatisticsAgent;
import com.tcl.statisticsdk.agent.StatisticsHandler;
import com.tcl.statisticsdk.client.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EVENT_NAME_1 = "MAINACTIVITY_CLICK_BTN";
    HandlerThread mHandlerThread;
    Handler mHandler;
    public static final int WHAT_LOAD_MESSAGE = 999;
    private EditText mText;
    private Thread mLoadFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {

        findViewById(R.id.btn_click_event).setOnClickListener(this);
        findViewById(R.id.btn_test_error).setOnClickListener(this);
        findViewById(R.id.btn_switch_page).setOnClickListener(this);
        mText = (EditText) findViewById(R.id.editText);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                // com.tcl.statisticsdk.util.LogUtils.D("uncatch Exception");
            }
        });

        // init thread
        // mHandlerThread = new HandlerThread("resolveLongThindThread");
        // mHandlerThread.start();

        mHandler = new Handler(getMainLooper(), new HandlerCallBack());

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 页面onResume
        com.tcl.statisticsdk.agent.StatisticsAgent.onResume(getApplicationContext());

        mLoadFile = new Thread() {
            @Override
            public void run() {
                super.run();

                String rs = StatisticsHandler.getInstance().getFileInstoreMessage();
                Message msg = Message.obtain();
                msg.what = WHAT_LOAD_MESSAGE;
                msg.obj = rs;
                mHandler.sendMessage(msg);

            }
        };
        mLoadFile.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        // 页面onPause
        com.tcl.statisticsdk.agent.StatisticsAgent.onPause(getApplicationContext());

        if (mLoadFile != null && mLoadFile.isAlive()) {
            mLoadFile.stop();
            mLoadFile.destroy();
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadFile != null && mLoadFile.isAlive()) {
            mLoadFile.stop();
            mLoadFile.destroy();
        }
    }

    /**
     * 
     */
    class HandlerCallBack implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {

            if (msg == null) {
                return false;
            }
            int what = msg.what;

            switch (what) {
                case WHAT_LOAD_MESSAGE:
                    if (msg.obj instanceof String) {

                        String result = "current file message is : ";
                        result += msg.obj.toString();
                        mText.setText(result);
                    }
                    break;
                default:

            }
            return false;
        };
    }
}
