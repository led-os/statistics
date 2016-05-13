package com.tcl.statisticsdk.bean;

import java.io.Serializable;
import java.util.List;


/**
 * 统计条目，一次启动时间内的所有统计算一次条目
 *
 * @author JiXiang
 */
public class StatisticsItem implements Serializable {

    // 页面信息
    private List<PageInfo> pageInfos;
    // 异常信息
    private ExceptionInfo exceptionInfo;

    public List<PageInfo> getPageInfos() {
        return this.pageInfos;
    }

    public void setPageInfos(List<PageInfo> pageInfos) {
        this.pageInfos = pageInfos;
    }

    public ExceptionInfo getExceptionInfo() {
        return this.exceptionInfo;
    }

    public void setExceptionInfo(ExceptionInfo exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }
}
