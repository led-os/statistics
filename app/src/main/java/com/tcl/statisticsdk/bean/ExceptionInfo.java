package com.tcl.statisticsdk.bean;

import java.io.Serializable;


/**
 *
 * 封装的异常信息，实现 @Serializable 接口方便序列化
 *
 */
public class ExceptionInfo implements Serializable {


    // 异常信息
    private String exceptionMessage;
    // 异常原因
    private String exceptionCause;
    // 异常发生时间
    private long excetpionTime = -8099483763844579328L;
    // 版本名称 如 V1.1.2
    private String appVersion;
    // 版本号 如 101
    private String versionCode;


    public void setVersionName(String versionCode) {
        versionCode = this.versionCode;
    }

    public String getAppVersion() {
        return this.appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getExceptionCause() {
        return this.exceptionCause;
    }

    public void setExceptionCause(String exceptionCause) {
        this.exceptionCause = exceptionCause;
    }

    public long getExcetpionTime() {
        return this.excetpionTime;
    }

    public void setExcetpionTime(long excetpionTime) {
        this.excetpionTime = excetpionTime;
    }

    public String getExceptionMessage() {
        return this.exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
}
