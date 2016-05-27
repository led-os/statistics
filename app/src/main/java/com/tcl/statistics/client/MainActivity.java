package com.tcl.statistics.client;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.tcl.statisticsdk.agent.StatisticsAgent;
import com.tcl.statisticsdk.agent.StatisticsHandler;
import com.tcl.statisticsdk.client.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int JSON_INDENT = 4;

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
        findViewById(R.id.btn_click_event_params).setOnClickListener(this);

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
        // 模拟事件
            case R.id.btn_click_event:
                StatisticsAgent.onEvent(getApplicationContext(), EVENT_NAME_1);
                break;
            // 模拟跳转Activity
            case R.id.btn_switch_page:

                Intent intent = new Intent();
                intent.setClass(this, ActivityTwo.class);
                startActivity(intent);
                break;
            //模拟异常
            case R.id.btn_test_error:
                errorOccurs();
                break;
            //模拟带参数的事件
            case R.id.btn_click_event_params:
                eventParams();
                break;
            //模以跳转Fragment
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
                        show(msg.obj.toString());
                    }
                    break;
                default:

            }
            return false;
        };
    }


    private void eventParams() {
        HashMap<String, String> params = new HashMap<>();

        params.put("key_1", "true");
        params.put("key_2", "1024");
        params.put("key_3", "key 3 value");

        String event_name = "BTN_EVENT_PARAMS";
        StatisticsAgent.onEvent(getApplicationContext(), event_name, params);
    }


    private void show(String json) {
        if (TextUtils.isEmpty(json)) {
            return;
        }
        try {
            json = json.trim();
            String contents = getResources().getString(R.string.content);
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                String message = jsonObject.toString(JSON_INDENT);
                mText.setText(contents + "\n" + message);
                return;
            }
            if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                String message = jsonArray.toString(JSON_INDENT);
                mText.setText(contents + "\n" + message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void forwardToFragment(){



    }

}
