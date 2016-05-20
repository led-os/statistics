package com.tcl.statisticsdk.systeminfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.tcl.statisticsdk.util.CPUInfoUtils;
import com.tcl.statisticsdk.util.DeviceUtils;
import com.tcl.statisticsdk.util.LogUtils;
import com.tcl.statisticsdk.util.MD5;
import com.tcl.statisticsdk.util.NetUtils;

import org.json.JSONObject;

import java.util.HashMap;


/**
 * 封装一些的基础统计信息 通用信息对于每一次上传都是一样的，不会做多份。
 *
 */
public class DeviceBasicInfo {

    private static final String TCL_STATISTICS_APP_KEY = "TCL_STATISTICS_APP_KEY";
    private static final String TCL_CHANNEL = "CHANNEL";

    // AndroidId
    private String mAndroidId;
    // ROM类型
    private String mRom;
    // 机型
    private String mModel;
    // 国家
    private String mCountry;
    // IMEA
    private String mImea;
    // 是否ROOT
    private boolean isRoot;
    // 分辨率
    private float mFre;
    // CPU型号
    private String mCpuType;
    // 频率
    private String mCpuFre;
    // 核数
    private int mCpuCount;
    // RAM可用容量
    private long mRAM;
    // ROM可用容量
    private long mROM;
    // 渠道
    private String mChannel;
    // MAC地址
    private String mMacAddress;
    // MCC
    private String mOp;
    // 连接类型 wifi / 2G/3G/4G/网络
    private String mNetworkOperator;
    // 高
    private int mHeight;
    // 宽
    private int mWidght;
    // 包名
    private String mPackageMame;
    // 程序版本号
    private  static final int mVersionCode = 1;
    // 程序版本名称
    private String mVersionName;
    // 当前系统版本
    private String mOsVersion;


    // 唯一编号
    private static String mUUID = null;
    // IMEA
    private static String mUUID2 = null;

    private static final String SDKVERSION = "1.0.0";
    private static DeviceBasicInfo sInstance = null;
    private static Context mContext;
    public static String mAppId = null;
    public static String mAdMode = null;
    public static String mLaunchActivityName = null;
    public static HashMap<String, String> mConfigmap = new HashMap();
    private String mSDKVersion;


    private DeviceBasicInfo() {}

    public static DeviceBasicInfo getInstance() {
        synchronized (DeviceBasicInfo.class) {
            if (sInstance == null) sInstance = new DeviceBasicInfo();
        }
        return sInstance;
    }

    /**
     * 初始化需要统计的基本信息
     * 
     * @param context 上下文,传兼容的时候注意
     */
    private void init(Context context) {

        mContext = context.getApplicationContext();
        this.mSDKVersion = android.os.Build.VERSION.SDK;
        mOsVersion = android.os.Build.VERSION.RELEASE;

        mAndroidId = DeviceUtils.getAndroidId(mContext);
        mRom = android.os.Build.DISPLAY;
        mModel = DeviceUtils.getModel();
        mCountry = DeviceUtils.getLocal(mContext);
        mImea = DeviceUtils.getIMEA(mContext);
        isRoot = DeviceUtils.isRootSystem();
        mFre = DeviceUtils.getDensity(mContext);
        mCpuType = CPUInfoUtils.getCpuModel();
        mCpuCount = CPUInfoUtils.getCpuCoreNums();
        mCpuFre = CPUInfoUtils.getMaxCpuFreq();
        mRAM = DeviceUtils.getTotalMemory(mContext)/1024L/1024L;
        mROM = DeviceUtils.getTotalExternalMemorySize()/1024L/1024L;
        mChannel = getChannelId(mContext);
        mMacAddress = DeviceUtils.getMacAddress();
        mOp = DeviceUtils.getIMSI(mContext);
        mNetworkOperator = NetUtils.getConnectType(mContext);
        mWidght = DeviceUtils.getWidth(mContext);
        mHeight = DeviceUtils.getHeight(mContext);

        // 序列号
        String serialno = DeviceUtils.getSerialNumber();
        // 合成的唯一编码
        mUUID = serialno + "_" + mImea + "_" + this.mMacAddress;
        mUUID = MD5.getMD5(mUUID);
        mUUID2 = mImea;
        if ((mUUID2 == null) || (mUUID2.equals(""))) {
            mUUID2 = "000000000000000";
        }

        getPackageInfo(context);
    }


    /**
     * 程序包名
     *
     * @param context 上下文
     * @return
     */
    public static String getPackageName(Context context) {
        PackageInfo info;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }


    public void getPackageInfo(Context context) {
        PackageInfo info;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            mPackageMame = info.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getMetaData(Context context, String key) {
        if (context == null) return "";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            ApplicationInfo appInfo =
                    context.getPackageManager().getApplicationInfo(info.packageName, PackageManager.GET_META_DATA);
            return appInfo.metaData.getString(key);
        } catch (Exception e) {}
        return "";
    }

    /**
     * 得到渠道号
     *
     * @param context
     * @return
     */
    public String getChannelId(Context context) {
        return getMetaData(context, "CHANNEL");
    }


    /**
     * getAppKey
     *
     * @param context
     * @return
     */
    public String getAppkey(Context context) {
        return getMetaData(context, "TCL_STATISTICS_APP_KEY");
    }

    public void setAppinfo(Context context, JSONObject appinfo) {

        init(context);

        if ((!(TextUtils.isEmpty(this.mVersionName))) && (this.mVersionName.length() > 15))

            throw new IllegalArgumentException(
                    "VersionName is too long , limit length is 15,Please modify your VersionName!!!");
        try {

            appinfo.put("ai", mAndroidId);
            appinfo.put("r", mRom);
            appinfo.put("t", mModel);
            appinfo.put("c", mCountry);
            appinfo.put("im", mImea);
            appinfo.put("ir", isRoot);
            appinfo.put("fr", mFre);
            appinfo.put("ct", mCpuType);
            appinfo.put("cr", mCpuFre);
            appinfo.put("cc", mCpuCount);
            appinfo.put("am", mRAM);
            appinfo.put("om", mROM);
            appinfo.put("ch", mChannel);
            appinfo.put("mac", mMacAddress);
            appinfo.put("op", mOp);
            appinfo.put("i", mNetworkOperator);
            appinfo.put("h", mHeight);
            appinfo.put("w", mWidght);
            appinfo.put("sv", mVersionCode);


        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("mAndroidId");
        sb.append(":\t");
        sb.append(mAndroidId);
        sb.append("\n");

        sb.append("mROM");
        sb.append(":\t");
        sb.append(mROM);
        sb.append("\n");

        sb.append("mModel");
        sb.append(":\t");
        sb.append(mModel);
        sb.append("\n");

        sb.append("mCountry");
        sb.append(":\t");
        sb.append(mCountry);
        sb.append("\n");

        sb.append("mImea");
        sb.append(":\t");
        sb.append(mImea);
        sb.append("\n");

        sb.append("isRoot");
        sb.append(":\t");
        sb.append(isRoot);
        sb.append("\n");

        sb.append("mCpuFre");
        sb.append(":\t");
        sb.append(mCpuFre);
        sb.append("\n");

        sb.append("mCpuType");
        sb.append(":\t");
        sb.append(mCpuType);
        sb.append("\n");

        sb.append("mRAM");
        sb.append(":\t");
        sb.append(mRAM);
        sb.append("\n");

        sb.append("mROM");
        sb.append(":\t");
        sb.append(mROM);
        sb.append("\n");

        sb.append("mCpuCount");
        sb.append(":\t");
        sb.append(mCpuCount);
        sb.append("\n");

        sb.append("channel");
        sb.append(":\t");
        sb.append(mChannel);
        sb.append("\n");

        sb.append("mMacAddress");
        sb.append(":\t");
        sb.append(mMacAddress);
        sb.append("\n");

        sb.append("mOp");
        sb.append(":\t");
        sb.append(mOp);
        sb.append("\n");

        sb.append("mNetworkOperator");
        sb.append(":\t");
        sb.append(mNetworkOperator);
        sb.append("\n");

        sb.append("mWidght");
        sb.append(":\t");
        sb.append(mWidght);
        sb.append("\n");

        sb.append("mHeight");
        sb.append(":\t");
        sb.append(mHeight);
        sb.append("\n");

        LogUtils.D("DeviceInfo:\t" + sb.toString());

        return sb.toString();
    }
}
