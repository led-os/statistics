package com.tcl.statisticsdk.util;

import android.content.Context;
import android.text.TextUtils;

import com.tcl.statisticsdk.agent.StatisticsHandler;
import com.tcl.statisticsdk.bean.StatisticsResult;
import com.tcl.statisticsdk.systeminfo.DeviceBasicInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.HashMap;

/**
 * 完成对对像的序列化
 */
public class FileSerializableUtils {
    public static final String STATISTICS_LOG_NAME = "statistics.text";
    private static final String LOG_DIR = "log";
    private static FileSerializableUtils fileSerializableUtils;

    public static FileSerializableUtils getInstence() {
        if (fileSerializableUtils == null) synchronized (FileSerializableUtils.class) {
            if (fileSerializableUtils == null) fileSerializableUtils = new FileSerializableUtils();
        }


        return fileSerializableUtils;
    }

    public boolean saveStatisticsResultToFile(Context context, StatisticsResult statisticsResult) throws IOException {

        File filesDir = context.getFilesDir();
        File logFileDir = new File(filesDir, "log");
        if (!(logFileDir.exists())) logFileDir.mkdir();

        File logFile = new File(logFileDir, DateUtils.getCurrentDay() + "_" + "statistics.text");
        FileOutputStream fos = new FileOutputStream(logFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(statisticsResult);
        oos.flush();
        oos.close();

        // --TODO 测试数据 debug版本有用，发布时去掉
        String saveJson = resultToJson(statisticsResult, context);
        LogUtils.D("此次保存到文件消息长度:" + saveJson.length() + "\n此次保存结果：" + saveJson);


        // --TODO 读取保存后当前文件里面的数据
        String fileCacheData = getJsonDataFromFileCache(context);
        LogUtils.D("当前缓存到文件中的内容为:" + fileCacheData);

        return true;
    }

    public String getJsonDataFromFileCache(Context context) {

        String jsonData = null;
        StatisticsResult result = null;

        try {
            result = (StatisticsResult) getObjectFromFile(context);
        } catch (IOException io) {
            io.printStackTrace();
        } catch (ClassNotFoundException caex) {
            caex.printStackTrace();
        }

        jsonData = resultToJson(result, context);

        return jsonData;
    }

    public Serializable getObjectFromFile(Context context) throws StreamCorruptedException, IOException,
            ClassNotFoundException {
        File filesDir = context.getFilesDir();
        File logFileDir = new File(filesDir, "log");
        if (!(logFileDir.exists())) logFileDir.mkdir();

        File logFile = new File(logFileDir, DateUtils.getCurrentDay() + "_" + "statistics.text");
        if (logFile.exists()) {
            FileInputStream fis = new FileInputStream(logFile);
            ObjectInputStream ois = new ObjectInputStream(fis);

            Serializable obj = (Serializable) ois.readObject();
            ois.close();
            return obj;
        }
        return null;
    }

    public HashMap<Serializable, String> getHistoryLogs(Context context) {
        HashMap historyLogs = null;

        File logFileDir = new File(context.getFilesDir(), "log");
        File[] logFiles = logFileDir.listFiles();
        if ((logFiles != null) && (logFiles.length > 0)) {
            File[] arrayOfFile1;
            int j = (arrayOfFile1 = logFiles).length;
            for (int i = 0; i < j; ++i) {
                File file = arrayOfFile1[i];

                if ((file.getName().endsWith("statistics.text"))
                        && (!(file.getName().contains(DateUtils.getCurrentDay())))) try {
                    FileInputStream fis = new FileInputStream(file.getAbsolutePath());
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    Serializable log = (Serializable) ois.readObject();
                    if (historyLogs == null) historyLogs = new HashMap();

                    historyLogs.put(log, file.getAbsolutePath());
                    ois.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        return historyLogs;
    }

    public void deleteTodayLogFile(Context context) {
        File filesDir = context.getFilesDir();
        File logFileDir = new File(filesDir, "log");
        if (!(logFileDir.exists())) logFileDir.mkdir();

        File logFile = new File(logFileDir, DateUtils.getCurrentDay() + "_" + "statistics.text");
        if ((logFile != null) && (logFile.exists())) logFile.delete();
    }

    public boolean saveStatisticsResultToLastDayFile(Context context, StatisticsResult statisticsResult) throws IOException {
        File filesDir = context.getFilesDir();
        File logFileDir = new File(filesDir, "log");
        if (!(logFileDir.exists())) logFileDir.mkdir();

        File logFile = new File(logFileDir, DateUtils.getLastDayTime() + "_" + "statistics.text");
        FileOutputStream fos = new FileOutputStream(logFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(statisticsResult);
        oos.flush();
        oos.close();

        return true;
    }

    /**
     * 
     * @param statisticsResult
     * @param mContext
     * @return
     */
    public String resultToJson(StatisticsResult statisticsResult, Context mContext) {
        if (TextUtils.isEmpty(DeviceBasicInfo.getInstance().getAppkey(mContext))
                || DeviceBasicInfo.getInstance().getAppkey(mContext).length() != 10) {
            LogUtils.E("appKey is null or appKey is wrong,check your config");
            return null;
        }
        return  StatisticsHandler.getInstance().convertStatisticResultToJson(statisticsResult);
    }

}
