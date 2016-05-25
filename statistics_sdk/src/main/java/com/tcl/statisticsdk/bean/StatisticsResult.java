package com.tcl.statisticsdk.bean;

import com.tcl.statisticsdk.util.LogUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 统计结果，可能包含多次统计条目的数据
 *
 *  一次要上传的数据
 *
 *  1：可能是一次统计产生的
 *  2：也有可能多次统计并且全并过本地历史的
 *
 * @author JiXiang
 */
public class StatisticsResult implements Serializable {

    private ArrayList<StatisticsItem> statisticItems = new ArrayList();
    private List<EventItem> hasParamEvents;
    private List<EventItem> noParamEvents;
    private long startTime;
    private long endTime;


    public StatisticsResult(List<EventItem> noParamEvents, List<EventItem> hasParamEvents, long startTime) {
        this.noParamEvents = noParamEvents;
        this.hasParamEvents = hasParamEvents;
        init(startTime);
    }

    private void init(long startTime) {
        this.startTime = (this.endTime = startTime);
        LogUtils.D("启动新Session，statTime:" + startTime + ",endTime" + this.endTime);
    }

    public List<EventItem> getNoParamEvents() {
        return this.noParamEvents;
    }

    public List<EventItem> getHasParamEvents() {
        return this.hasParamEvents;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public ArrayList<StatisticsItem> getStatisticItems() {
        return this.statisticItems;
    }

    public void setStatisticItems(ArrayList<StatisticsItem> statisticItems) {
        this.statisticItems = statisticItems;
    }
}