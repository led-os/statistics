package com.tcl.statisticsdk.agent;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;

import com.orhanobut.logger.Logger;
import com.tcl.statisticsdk.bean.EventItem;
import com.tcl.statisticsdk.bean.ExceptionInfo;
import com.tcl.statisticsdk.bean.PageInfo;
import com.tcl.statisticsdk.bean.StatisticsItem;
import com.tcl.statisticsdk.bean.StatisticsResult;
import com.tcl.statisticsdk.net.StatisticsApi;
import com.tcl.statisticsdk.systeminfo.DeviceBasicInfo;
import com.tcl.statisticsdk.util.CrashHandler;
import com.tcl.statisticsdk.util.DeviceUtils;
import com.tcl.statisticsdk.util.FileSerializableUtils;
import com.tcl.statisticsdk.util.LogUtils;
import com.tcl.statisticsdk.util.NetUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 *
 * note:
 *
 * --TODO 1:序列化文件的时间偏长 --TODO 2: pageInfo的设计方案现在是onResume -onPause 就产生了一个pageInfo，对象可能会过多。 --TODO 3:
 * 数据量对序列化数据时间长短的影响。
 *
 * EventInfo 的生命周期和发送成功是一致的，只能发送成功后才会Clean, EventINfo和Result的关联在初始化的时候就构建好了，但这种关系有暴露的太厉害了一点。内聚性不好。
 * PageInfo 生命周期onResume--> onPause StatisticItemInfo 封装了很多个pageInfo，生命周期 StatisticsResult
 * 封装了EventInfo ，和StatisticsItem 只有当成功上传后statistisResult才会生成新的对象。周期可认为是一次有效的数据包。 可以认为是一次上传
 * StatisticsItem一次统计周期超时后都会生成一个新的对象不管是上传成功或是失败。可以认为是一次Seesion。当上传不成功后，有多个。
 *
 */
public class StatisticsHandler {

    private static StatisticsHandler mInstance = new StatisticsHandler();
    private static Context mContext;
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private String mCurrentPageName = "";
    private String mCurrentClassName = "";

    public final static int WHAT_ON_RESUME = 0;
    public final static int WHAT_ON_PAUSE = 1;
    public final static int WHAT_ON_EXIT = 2;
    public final static int WHAT_ON_ERROR_EXIT = 3;
    public final static int WHAT_ON_EVENT = 4;
    public final static int WHAT_ON_PAGE_START = 5;
    public final static int WHAT_ON_PAGE_END = 6;
    public final static int WHAT_ON_CATCH_EXCEPTION = 7;
    public final static int WHAT_SEND_LOG = 999;

    private long mStartTime = 0;
    private long mExitTime;

    private static StatisticsResult mStatisticsResult;
    private static StatisticsItem mStatisticsItem;

    // 当前页面生命周期是 onResume --> onPause
    private PageInfo mCurrentPageInfo;
    // 统计页面信息
    private static List<PageInfo> mPageInfos = new ArrayList<PageInfo>();
    // 不带参数的事件统计
    private static List<EventItem> mNoParamEvents = new ArrayList<EventItem>();
    // 带参数的事件统计
    private static List<EventItem> mHasParamEvents = new ArrayList<EventItem>();
    public static String mExceptionMessage = null;
    public static String mExcetpionCause = null;
    private static final int DATA_MAX_SIZE = 300 * 1024;
    private boolean isSendLog = false;
    private boolean isResume = false;

    public static StatisticsHandler getInstance() {
        return mInstance;
    }

    private StatisticsHandler() {
        startThread();
    }

    private void startThread() {
        mHandlerThread = new HandlerThread("statisticHandler", Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), new HandlerCallBack());
    }


    class HandlerCallBack implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            Object obj = msg.obj;
            switch (msg.what) {
                case WHAT_ON_RESUME:
                    if (obj != null) {
                        onResume((Context) obj);
                    }
                    break;
                case WHAT_ON_PAUSE:
                    if (obj != null) {
                        onPause((Context) obj);
                    }
                    break;
                case WHAT_ON_EXIT:
                    onExit();
                    break;
                case WHAT_ON_ERROR_EXIT:
                    onErrorExit();
                    break;

                case WHAT_ON_EVENT:
                    if (obj != null) {
                        onEvent(obj);
                    }
                    break;
                case WHAT_ON_PAGE_START:
                    if (obj != null) {
                        onPageStart((String) obj);
                    }
                    break;
                case WHAT_ON_PAGE_END:
                    if (obj != null) {
                        onPageEnd((String) obj);
                    }
                    break;
                case WHAT_ON_CATCH_EXCEPTION:
                    if (obj != null) {
                        catchException((Context) obj);
                    }
                    break;
                case WHAT_SEND_LOG:
                    sendLog();
                    break;
            }
            return true;
        }

    }

    // 判断子线程是否死掉
    private void checkThreadAlive() {
        if (mHandlerThread == null || !mHandlerThread.isAlive()) {
            startThread();
        }
    }

    public void sendMessage(int what, Object obj) {
        checkThreadAlive();
        Message msg = mHandler.obtainMessage(what, obj);
        mHandler.sendMessage(msg);
    }


    public void sendMessage(int what) {
        checkThreadAlive();
        mHandler.sendEmptyMessage(what);
    }

    /**
     * 设置异常捕获器
     *
     * @param context
     */
    private static void catchException(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
            // 初始化异常记录器
            CrashHandler crashHandler = CrashHandler.getInstance();
            crashHandler.init(context);
        }
    }

    /**
     * 自定义页面开始
     *
     * @param pageName
     */
    private void onPageStart(String pageName) {
        LogUtils.W("进入自定义页面:" + pageName);
        if (mStatisticsResult == null) {
            LogUtils.W("未执行onResume而执行onPageStart错误");
            return;
        }

        if (mContext == null) {
            LogUtils.W("进入onPageStart，context 为null:");
            return;
        }

        if (convertStatisticResultToJson(mStatisticsResult).length() > DATA_MAX_SIZE) {
            // 大于数据最大值，则将数据存储为昨天文件，并创建新文件存储之后的数据
            LogUtils.E("data is full");
            // 执行当前Activity的onResume操作，保存浏览时间
            for (int i = mPageInfos.size() - 1; i >= 0; i--) {
                PageInfo pageTimeCalculateTool = mPageInfos.get(i);
                if (mCurrentClassName.equals(pageTimeCalculateTool.getPageName())) {
                    if (pageTimeCalculateTool != null) {
                        pageTimeCalculateTool.onPause();
                    }
                    break;
                }
            }
            saveStatisticsResultToHistoryFile();
            deleteTodayCacheFile();
            startNewStatistics();
        }

        if (mStatisticsResult == null) {
            mStatisticsResult = new StatisticsResult(mNoParamEvents, mHasParamEvents, System.currentTimeMillis());
            initNewStatisticsItem();
            // 增加新的统计
            mStatisticsResult.getStatisticItems().add(mStatisticsItem);
        }
        if (!pageName.equals(mCurrentClassName)) {
            mPageInfos.add(new PageInfo(pageName));
            mCurrentClassName = pageName;
        } else {
            for (int i = mPageInfos.size() - 1; i >= 0; i--) {
                PageInfo pageTimeCalculateTool = mPageInfos.get(i);
                if (mCurrentClassName.equals(pageTimeCalculateTool.getPageName())) {
                    if (pageTimeCalculateTool != null) {
                        pageTimeCalculateTool.onResume();
                    }
                    break;
                }
            }
        }
    }

    /**
     * 自定义页面结束
     *
     * @param pageName
     */
    private void onPageEnd(String pageName) {
        LogUtils.W("离开自定义页面:" + pageName);
        if (mStatisticsResult == null) {
            LogUtils.E("执行onPageEnd时，未创建统计信息出错");
            return;
        }

        if (TextUtils.isEmpty(mCurrentClassName)) {
            LogUtils.D("当前页面未先执行onPageStart()，却执行了onPageEnd()");
            return;
        }

        if (mPageInfos == null || mPageInfos.size() == 0) {
            LogUtils.D("mPageInfos is null | mPageInfos.size == 0");
            return;
        }
        for (int i = mPageInfos.size() - 1; i >= 0; i--) {
            PageInfo pageInfo = mPageInfos.get(i);
            if (pageName.equals(pageInfo.getPageName())) {
                if (pageInfo != null) {
                    pageInfo.onPause();
                }
                break;
            }
        }
        mExitTime = System.currentTimeMillis();
        mStatisticsResult.setEndTime(System.currentTimeMillis());
    }

    /**
     * 增加事件统计
     *
     * @param obj
     */
    private void onEvent(Object obj) {
        EventItem event = (EventItem) obj;

        if (event.getEventParamMap() != null) {
            mHasParamEvents.add(event);
        } else {
            mNoParamEvents.add(event);
        }
    }

    /**
     * 进入Activity页面
     *
     * @param context
     */
    private void onResume(Context context) {

        mContext = context.getApplicationContext();
        String className = getShortClassName(context);
        LogUtils.D("进入页面:" + className);
        long currentTime = System.currentTimeMillis();

        if (isResume) {
            LogUtils.E(className + ":未执行onPause而直接执行onResume出错");
            return;
        }

        isResume = true;

        if (System.currentTimeMillis() - mExitTime > StatisticsConfig.getSessionTimeOut(mContext)) {

            // 距离之前离开页面超过Session时间
            mStartTime = System.currentTimeMillis();
            // 从文件中取出统计结果
            mStatisticsResult = fetchStatisticsResultFromFile();
            // 历史日志
            HashMap<Serializable, String> historyLogs = FileSerializableUtils.getInstence().getHistoryLogs(context);

            if (mStatisticsResult != null && mStatisticsResult.getStatisticItems().size() != 0) {

                mNoParamEvents = mStatisticsResult.getNoParamEvents();
                mHasParamEvents = mStatisticsResult.getHasParamEvents();

                LogUtils.D("统计结果不为空，并且有统计对象，准备发送今日数据");

                // 上报结果，后把数据清空结算
                if (reportResult(mStatisticsResult)) {
                    deleteTodayCacheFile();
                    mNoParamEvents.clear();
                    mHasParamEvents.clear();
                    mStatisticsResult = new StatisticsResult(mNoParamEvents, mHasParamEvents, mStartTime);
                }
            } else if (historyLogs == null || historyLogs.size() == 0) {
                // 无历史日志，并且当天日志内容为空，代表首次安装,则发送一个空数据日志
                LogUtils.D("进入页面:" + "无历史日志，并且当天日志内容为空，代表首次安装,则发送一个空数据日志");
                mStatisticsResult = new StatisticsResult(mNoParamEvents, mHasParamEvents, mStartTime);
                reportResult(mStatisticsResult);
            }
            if (historyLogs != null && historyLogs.size() > 0) {
                // 发送历史日志
                SendHistoryLogHandler.getInstance().sendHistoryLogs(context);
            }

            // 清空临时缓存
            clearCache();

            if (mStatisticsResult == null) {
                mStatisticsResult = new StatisticsResult(mNoParamEvents, mHasParamEvents, mStartTime);
            }
            initNewStatisticsItem();
            // 增加新的统计
            mStatisticsResult.getStatisticItems().add(mStatisticsItem);

        } else if (convertStatisticResultToJson(mStatisticsResult).length() > DATA_MAX_SIZE) {
            // 大于数据最大值，则将数据存储为昨天文件，并创建新文件存储之后的数据 ，形成了一个历史文件 --TODO 如果一直往昨天的日志里面去加那么昨天的也会超过这个值

            LogUtils.E("data is full");
            saveStatisticsResultToHistoryFile();
            deleteTodayCacheFile();
            startNewStatistics();
        }
        // 处理页面信息
        resolvePageInfo(className, currentTime);
    }

    private void resolvePageInfo(String className, Long StarTime) {

        mCurrentPageInfo = new PageInfo(className);
        mCurrentPageInfo.setStartTime(StarTime);
        mPageInfos.add(mCurrentPageInfo);
        mCurrentClassName = className;
        Logger.d(" 添加页面信息 class Name %s %d  当前内存中PageInfo数量: %d" , className,mStartTime,mPageInfos.size());
    }


    private void startNewStatistics() {
        mNoParamEvents.clear();
        mHasParamEvents.clear();
        mStatisticsResult = new StatisticsResult(mNoParamEvents, mHasParamEvents, System.currentTimeMillis());
        clearCache();
        initNewStatisticsItem();
        mStatisticsResult.getStatisticItems().add(mStatisticsItem);
    }


    private void deleteTodayCacheFile() {
        if (mContext != null) FileSerializableUtils.getInstence().deleteTodayLogFile(mContext);
    }

    private void initNewStatisticsItem() {
        mStatisticsItem = new StatisticsItem();
        mStatisticsItem.setPageInfos(mPageInfos);
    }

    /**
     * 离开页面
     *
     * @param context
     */
    private void onPause(Context context) {

        mContext = context.getApplicationContext();
        String className = getShortClassName(context);
        long currentTime = System.currentTimeMillis();

        if (mStatisticsResult == null) {
            LogUtils.D("执行onPause方法之前，没有创建日志信息");
            return;
        }

        if (mPageInfos == null || mPageInfos.size() == 0) {
            LogUtils.D("mPageInfos is null | mPageInfos.size == 0");
            return;
        }
        if (!isResume) {
            LogUtils.E(className + ":未执行onResume而直接执行onPause出错");
            return;
        }
        if (TextUtils.isEmpty(mCurrentClassName)) {
            LogUtils.D("当前页面" + mCurrentClassName + ":未先执行onResume，却执行了onPause()");
            return;
        }

        isResume = false;
        LogUtils.W("离开页面:" + className);
        resolvePageInfoOnPause(currentTime);

        mExitTime = System.currentTimeMillis();
        mStatisticsResult.setEndTime(System.currentTimeMillis());
        saveStatisticsResult();
    }


    private void resolvePageInfoOnPause(long currentTime) {
        if (mCurrentPageInfo == null) {
            return;
        }
        mCurrentPageInfo.setEndTIme(currentTime);
    }

    /**
     * 清除缓存数据
     */
    private void clearCache() {
        mPageInfos.clear();
        mExceptionMessage = null;
        mExcetpionCause = null;
        mCurrentClassName = null;
        mCurrentPageName = null;
    }

    private void onExit() {
        saveStatisticsResult();
    }

    public void onErrorExit() {
        ExceptionInfo exceptionInfo = new ExceptionInfo();
        exceptionInfo.setExceptionMessage(mExceptionMessage);
        exceptionInfo.setExceptionCause(mExcetpionCause);
        exceptionInfo.setExcetpionTime(System.currentTimeMillis());
        if (mContext != null) {
            exceptionInfo.setAppVersion(DeviceUtils.getVersionName(mContext));
        }
        mStatisticsItem.setExceptionInfo(exceptionInfo);

        // 保存退出的时间
        mStatisticsResult.setEndTime(System.currentTimeMillis());
        saveStatisticsResult();
        // System.exit(1);
    }

    public void onKillProcess() {
        saveStatisticsResult();
    }

    private String getShortClassName(Context context) {
        String packageName = DeviceBasicInfo.getPackageName(context);
        String className = context.getClass().getName();
        if (TextUtils.isEmpty(packageName)) {
            return className;
        }
        className = className.substring(packageName.length() + 1);
        return className;
    }

    /**
     * 上报统计结果
     *
     * @param statisticsResult
     * @return true代表发送成功,fasle代表发送失败
     */
    public boolean reportResult(StatisticsResult statisticsResult) {

        /* AppKey是否合法 */
        boolean isEmpty = TextUtils.isEmpty(DeviceBasicInfo.getInstance().getAppkey(mContext));
        if (isEmpty || DeviceBasicInfo.getInstance().getAppkey(mContext).length() != 10) {
            LogUtils.E("appKey is null or appKey is wrong,check your config");
            return false;
        }

        // 网络连接是否正常
        if (!NetUtils.isNetworkEnable(mContext)) {
            LogUtils.D("net disable");
            return false;
        }

        LogUtils.D("net enable");
        boolean sendResult = false;

        JSONObject reportData = new JSONObject();
        JSONArray pageStatArray = new JSONArray();
        JSONArray eventArray = new JSONArray();
        JSONArray exceptionArray = new JSONArray();
        ArrayList<StatisticsItem> statisticItems = statisticsResult.getStatisticItems();

        String vertionCode = DeviceUtils.getVersionCode(mContext);
        String vertionName = DeviceUtils.getVersionName(mContext);
        String packageName = DeviceBasicInfo.getPackageName(mContext);
        String launguage = DeviceUtils.getLocal(mContext);

        for (StatisticsItem item : statisticItems) {

            try {
                JSONObject sessionStat = new JSONObject();

                // 封装要上传的Sesion 访问次数信息
                sessionStat.put("s", statisticsResult.getStartTime());
                sessionStat.put("e", statisticsResult.getEndTime());
                sessionStat.put("vc", vertionCode);
                sessionStat.put("vn", vertionName);
                sessionStat.put("pn", packageName);
                sessionStat.put("la", launguage);

                /* 封装要上传的单个页面访问信息 */
                JSONArray pages = new JSONArray();

                for (PageInfo pageTmp : item.getPageInfos()) {

                    JSONObject page = new JSONObject();

                    page.put("n", pageTmp.getPageName());
                    page.put("s", pageTmp.getStartTime());
                    page.put("e", pageTmp.getEndTime());

                    pages.put(page);
                }

                ExceptionInfo exceptionInfo = item.getExceptionInfo();

                /** Exception的信息要重新组织吗? --TODO **/
                if (exceptionInfo != null) {

                    JSONObject exception = new JSONObject();
                    exception.put("m", exceptionInfo.getExceptionMessage());
                    exception.put("c", exceptionInfo.getExceptionCause());
                    exception.put("vc", vertionCode);
                    exception.put("vn", vertionName);
                    exception.put("t", exceptionInfo.getExcetpionTime());

                    exceptionArray.put(exception);
                }
                sessionStat.put("pg", pages);
                pageStatArray.put(sessionStat);

            } catch (Exception e) {
                sendResult = false;
                return sendResult;
            }
        }

        try {
            // 无参数的事件
            List<EventItem> noParamEvents = statisticsResult.getNoParamEvents();
            for (EventItem eventItem : noParamEvents) {
                JSONObject event = new JSONObject();

                event.put("s", eventItem.getStartTime());
                event.put("d", eventItem.getEventValue());
                event.put("c", eventItem.getEventName());
                event.put("i", eventItem.getEventExtrInfo());
                eventArray.put(event);
            }
            // 带参数的事件
            List<EventItem> hasParamEvents = statisticsResult.getHasParamEvents();
            for (EventItem eventItem : hasParamEvents) {
                JSONObject event = new JSONObject();

                event.put("s", eventItem.getStartTime());
                event.put("d", eventItem.getEventValue());
                event.put("c", eventItem.getEventName());
                event.put("i", eventItem.getEventExtrInfo());

                if (eventItem.getEventParamMap() != null && eventItem.getEventParamMap().size() > 0) {
                    JSONArray eventParamArray = new JSONArray();
                    for (Map.Entry<String, String> eventParamEntry : eventItem.getEventParamMap().entrySet()) {
                        JSONObject eventParam = new JSONObject();
                        eventParam.put("k", eventParamEntry.getKey());
                        eventParam.put("v", eventParamEntry.getValue());
                        eventParamArray.put(eventParam);
                    }
                    event.put("p", eventParamArray);
                }
                eventArray.put(event);
            }

            reportData.put("se", pageStatArray);
            reportData.put("ex", exceptionArray);
            reportData.put("ev", eventArray);

            JSONObject appinfo = new JSONObject();
            DeviceBasicInfo.getInstance().setAppinfo(mContext, appinfo);
            reportData.put("us", appinfo);

        } catch (JSONException e) {
            e.printStackTrace();
            sendResult = false;
            return sendResult;
        }
        LogUtils.D("上报消息长度:" + reportData.toString().length() + "\n消息：" + reportData.toString());
        // 联网上报成功，清空上报内容
        boolean result = StatisticsApi.sendLog(mContext, reportData.toString(), 30 * 1000, 30 * 1000);
        LogUtils.D("上报结果:" + result);
        sendResult = result;

        Logger.json(reportData.toString());

        return sendResult;
    }


    private void sendLog() {
        if (isSendLog == false) {
            synchronized (StatisticsHandler.this) {
                if (isSendLog == false) {
                    isSendLog = true;
                    if (reportResult(mStatisticsResult)) {
                        // 清空缓存
                        mPageInfos.clear();
                        mExceptionMessage = null;
                        mExcetpionCause = null;
                        mCurrentPageName = null;
                        mNoParamEvents.clear();
                        mHasParamEvents.clear();
                        mStatisticsResult =
                                new StatisticsResult(mNoParamEvents, mHasParamEvents, System.currentTimeMillis());
                    }
                    if (mStatisticsResult == null) {
                        mStatisticsResult =
                                new StatisticsResult(mNoParamEvents, mHasParamEvents, System.currentTimeMillis());
                    }
                    initNewStatisticsItem();
                    // 增加新的统计
                    mStatisticsResult.getStatisticItems().add(mStatisticsItem);

                    mPageInfos.add(new PageInfo(mCurrentClassName));

                    isSendLog = false;
                }
            }
        }
    }

    private String convertStatisticResultToJson(StatisticsResult statisticsResult) {
        JSONObject reportData = new JSONObject();
        JSONArray pageStatArray = new JSONArray();
        JSONArray eventArray = new JSONArray();
        JSONArray exceptionArray = new JSONArray();

        ArrayList<StatisticsItem> statisticItems = statisticsResult.getStatisticItems();
        for (StatisticsItem item : statisticItems) {

            try {
                JSONObject pageStat = new JSONObject();
                pageStat.put("e", statisticsResult.getEndTime());
                pageStat.put("s", statisticsResult.getStartTime());
                pageStat.put("i", System.currentTimeMillis());
                pageStat.put("c", statisticItems.size());
                JSONArray pages = new JSONArray();
                for (PageInfo pageTimeCalculateTool : item.getPageInfos()) {
                    JSONObject page = new JSONObject();
                    page.put("n", pageTimeCalculateTool.getPageName());
                    // page.put("d", pageTimeCalculateTool.getScanTime());
                    page.put("ps", 0);
                    pages.put(page);
                }
                ExceptionInfo exceptionInfo = item.getExceptionInfo();
                if (exceptionInfo != null) {
                    JSONObject exception = new JSONObject();
                    exception.put("c", exceptionInfo.getExceptionMessage());
                    exception.put("v", exceptionInfo.getAppVersion());
                    exception.put("y", exceptionInfo.getExceptionCause());
                    exception.put("t", exceptionInfo.getExcetpionTime());
                    exceptionArray.put(exception);
                }
                pageStat.put("p", pages);
                pageStatArray.put(pageStat);
            } catch (Exception e) {}
        }

        try {
            // 无参数的事件
            List<EventItem> noParamEvents = statisticsResult.getNoParamEvents();
            for (EventItem eventItem : noParamEvents) {
                JSONObject event = new JSONObject();
                event.put("d", eventItem.getEventValue());
                event.put("t", eventItem.getStartTime());
                // event.put("s", eventItem.getHappenTime());
                event.put("c", eventItem.getCount());
                event.put("i", eventItem.getEventName());
                event.put("p", new JSONArray());
                eventArray.put(event);
            }
            // 带参数的事件
            List<EventItem> hasParamEvents = statisticsResult.getHasParamEvents();
            for (EventItem eventItem : hasParamEvents) {
                JSONObject event = new JSONObject();
                event.put("d", eventItem.getEventValue());
                event.put("t", eventItem.getStartTime());
                // event.put("s", eventItem.getHappenTime());
                event.put("c", eventItem.getCount());
                event.put("i", eventItem.getEventName());

                if (eventItem.getEventParamMap() != null && eventItem.getEventParamMap().size() > 0) {
                    JSONArray eventParamArray = new JSONArray();
                    for (Map.Entry<String, String> eventParamEntry : eventItem.getEventParamMap().entrySet()) {
                        JSONObject eventParam = new JSONObject();
                        eventParam.put("k", eventParamEntry.getKey());
                        eventParam.put("v", eventParamEntry.getValue());
                        eventParamArray.put(eventParam);
                    }
                    event.put("p", eventParamArray);
                }
                eventArray.put(event);
            }

            reportData.put("pr", pageStatArray);
            reportData.put("ex", exceptionArray);
            reportData.put("ev", eventArray);

            JSONObject appinfo = new JSONObject();
            DeviceBasicInfo.getInstance().setAppinfo(mContext, appinfo);
            reportData.put("he", appinfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reportData.toString();
    }

    /**
     * 保存统计结果
     */
    private static void saveStatisticsResult() {
        LogUtils.E("准备保存数据");
        try {
            FileSerializableUtils.getInstence().saveStatisticsResultToFile(mContext, mStatisticsResult);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存统计结果为历史文件
     */
    private static void saveStatisticsResultToHistoryFile() {
        LogUtils.D("准备保存文件为历史数据");
        try {
            FileSerializableUtils.getInstence().saveStatisticsResultToLastDayFile(mContext, mStatisticsResult);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 从本地文件获取统计结果
     *
     * @return
     */
    private static StatisticsResult fetchStatisticsResultFromFile() {
        if (mContext == null) {
            LogUtils.I("fetchStatisticsResult,context is null");
            return null;
        }
        StatisticsResult statisticsResult = null;
        try {
            statisticsResult = (StatisticsResult) FileSerializableUtils.getInstence().getObjectFromFile(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statisticsResult;
    }
}
