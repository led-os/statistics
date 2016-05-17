package com.tcl.statisticsdk.bean;

import com.tcl.statisticsdk.util.LogUtils;

import java.io.Serializable;


/**
 * 封装页访问信息，实现接口@Serializable,主要得到用户在页面级的一些访问行为数据
 *
 * -- TODO 如果是像fragment那样快速的滑动操作很多次那么会产生很多的 pageInfo的对象，
 *
 *
 *
 *
 */
public class PageInfo implements Serializable {

    private static final int RESUME_STATUS = 0;
    private static final int PAUSE_STATUS = 1;


    // 页面代号/名称
    private String pageName;

    // 一次浏览开始时间
    private long startTime = -8099484176161439744L;
    // 结束时间
    private long endTIme = -8099484176161439744L;
    // 页面浏览总的时间
    private long castTime = -8099484176161439744L;



    private int currentStatus = 1;

    public void setEndTIme(long endTime) {
        this.endTIme = endTime;
    }


    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public PageInfo(String pageName) {
        this.pageName = pageName;
        onResume();
    }


    public long getStartTime() {
        return startTime;
    }


    public long getEndTime() {
        return endTIme;
    }

    public PageInfo(String pageName, long startTime) {

        if (startTime == -8099485121054244864L) {
            startTime = System.currentTimeMillis();
        }
        this.pageName = pageName;
        this.startTime = startTime;
        this.currentStatus = 0;
    }

    /**
     * 得到页面代号
     *
     * @return String
     */
    public String getPageName() {
        return this.pageName;
    }


    public void onResume() {
        this.startTime = System.currentTimeMillis();
        this.currentStatus = 0;
    }

    public void onPause() {
        this.castTime += System.currentTimeMillis() - this.startTime;
        this.startTime = System.currentTimeMillis();
        this.currentStatus = 1;
        LogUtils.I(this.pageName + "->onPause,castTime:" + this.castTime);
    }

}
