package com.tcl.statisticsdk.bean;

import java.io.Serializable;
import java.util.Map;


/**
 * 事件信息Bean
 * 
 */
public class EventItem implements Serializable {

    // 事件名称 /操作代码 如 main_clean_file
    private String eventName;
    private int count;
    // 事件参数
    private Map<String, String> eventParamMap;
    // 事件值
    private int eventValue;
    // 事件开始时间
    private long startTime;

    /**
     * 构造方法
     *
     * @param startTime 开始时间
     * @param eventName 事件名称
     */
    public EventItem(long startTime, String eventName) {
        this.startTime = startTime;
        this.eventName = eventName;
        this.count = 1;
    }


    /**
     * 构造方法
     *
     * @param startTime 开始时间
     * @param eventName 事件名称
     * @param paramMap 事件参数
     */
    public EventItem(long startTime, String eventName, Map<String, String> paramMap) {
        this.startTime = startTime;
        this.eventName = eventName;
        this.eventParamMap = paramMap;
        this.count = 1;
    }

    /**
     * 构造方法
     *
     * @param startTime 开始时间
     * @param eventName 事件代码
     * @param paramMap 参数
     * @param value
     */
    public EventItem(long startTime, String eventName, Map<String, String> paramMap, int value) {
        this.startTime = startTime;
        this.eventName = eventName;
        this.eventParamMap = paramMap;
        this.eventValue = value;
        this.count = 1;
    }


    /**
     * 得到事件开始时间
     *
     * @return long
     */
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * 设置开始时间
     *
     * @param startTime
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getCount() {
        return this.count;
    }

    /**
     * 操作代码
     * 
     * @return
     */
    public String getEventName() {
        return this.eventName;
    }


    /**
     * 得到事件的参数
     * 
     * @return Map<String, String>
     */
    public Map<String, String> getEventParamMap() {
        return this.eventParamMap;
    }

    public int getEventValue() {
        return this.eventValue;
    }

}
