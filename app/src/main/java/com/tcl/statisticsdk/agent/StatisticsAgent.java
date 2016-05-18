package com.tcl.statisticsdk.agent;

import android.content.Context;
import android.text.TextUtils;

import com.orhanobut.logger.Logger;
import com.tcl.statisticsdk.bean.EventItem;
import com.tcl.statisticsdk.util.CheckUtils;
import com.tcl.statisticsdk.util.LogUtils;

import java.util.HashMap;
import java.util.Map;

public class StatisticsAgent {
    private static boolean mInitCatchException = false;

    static {
        mInitCatchException = false;
        Logger.init("tyler.tang");
    }

    public static synchronized void onResume(Context context) {
        if (context == null) {
            LogUtils.D("onResume-- context is null");
            return;
        }
        //init(context);
        //StatisticsHandler.getInstance().sendMessage(StatisticsHandler.WHAT_ON_RESUME, context);
    }

    public static synchronized void onPause(Context context) {
        if (context == null) {
            LogUtils.D("onPause-- context is null");
            return;
        }
        StatisticsHandler.getInstance().sendMessage(StatisticsHandler.WHAT_ON_PAUSE, context);
    }

    public static synchronized void onPageStart(String pageName) {
        if (TextUtils.isEmpty(pageName)) {
            LogUtils.D("onPageStart-- pageName is null");
            return;
        }
        StatisticsHandler.getInstance().sendMessage(StatisticsHandler.WHAT_ON_PAGE_START, pageName);
    }

    public static synchronized void onPageEnd(String pageName) {
        if (TextUtils.isEmpty(pageName)) {
            LogUtils.D("onPageEnd-- pageName is null");
            return;
        }
        StatisticsHandler.getInstance().sendMessage(StatisticsHandler.WHAT_ON_PAGE_END, pageName);
    }

    public static synchronized void onEvent(Context context, String eventName) {
        if (!(CheckUtils.isLegalEventName(eventName)))
            throw new RuntimeException("error!eventName:" + eventName
                    + " is not legal,only letter,number and underline is valid");

        EventItem event = new EventItem(System.currentTimeMillis(), eventName);
        StatisticsHandler.getInstance().sendMessage(StatisticsHandler.WHAT_ON_EVENT, event);
    }

    public static synchronized void onEvent(Context context, String eventName, HashMap<String, String> map) {
        if (!(CheckUtils.isLegalEventName(eventName)))
            throw new RuntimeException("error!eventName:" + eventName
                    + " is not legal,only letter,number and underline is valid");

        if (!(CheckUtils.isLegalParamKeyAndValue(map))) throw new RuntimeException("error!map is not legal");

        EventItem event = new EventItem(System.currentTimeMillis(), eventName, map);
        StatisticsHandler.getInstance().sendMessage(StatisticsHandler.WHAT_ON_EVENT, event);
    }

    public static synchronized void onEvent(Context context, String eventName, Map<String, String> map, int value) {
        if (!(CheckUtils.isLegalEventName(eventName)))
            throw new RuntimeException("error!eventName:" + eventName
                    + " is not legal,only letter,number and underline is valid");

        if (!(CheckUtils.isLegalParamKeyAndValue(map))) throw new RuntimeException("error!map is not legal");

        EventItem event = new EventItem(System.currentTimeMillis(), eventName, map, value);
        StatisticsHandler.getInstance().sendMessage(StatisticsHandler.WHAT_ON_EVENT, event);
    }

    public static void onExit(Context context) {
        StatisticsHandler.getInstance().sendMessage(StatisticsHandler.WHAT_ON_EXIT);
    }

    protected static void onErrorExit(Context context) {
        if (StatisticsConfig.isCatchExceptionEnable(context))
            StatisticsHandler.getInstance().sendMessage(StatisticsHandler.WHAT_ON_EXIT);
    }

    public static void init(Context context) {
//        initExceptionCatcher(context);
    }

    private static void initExceptionCatcher(Context context) {
        synchronized (StatisticsAgent.class) {
            if (!(mInitCatchException)) {
                Logger.d("发送始化异常捕获器的");
//                StatisticsHandler.getInstance().sendMessage(StatisticsHandler.WHAT_ON_CATCH_EXCEPTION, context);
                mInitCatchException = true;
            }
        }
    }

    public static void setCatchException(Context context, boolean catchException) {
        StatisticsConfig.setCatchExceptionEnable(context, catchException);
    }

    public static void setSessionTimeOut(Context context, long value) {
        StatisticsConfig.setSessionTimeOut(context, value);
    }

    public static void setDebugMode(boolean debug) {
        LogUtils.mDebug = debug;
    }

    public static void onKillProcess(Context context) {
        StatisticsHandler.getInstance().onKillProcess();
    }

    public static synchronized void sendLog() {
        StatisticsHandler.getInstance().sendMessage(StatisticsHandler.WHAT_SEND_LOG);
    }
}
