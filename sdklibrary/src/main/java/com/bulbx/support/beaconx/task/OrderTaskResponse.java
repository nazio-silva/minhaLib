package com.bulbx.support.beaconx.task;

import com.bulbx.support.beaconx.entity.OrderType;

import java.io.Serializable;

/**
 * @Date 2018/1/23
 * @Author wenzheng.liu
 * @Description 任务反馈
 * @ClassPath com.bulb.support.task.OrderTaskResponse
 */
public class OrderTaskResponse implements Serializable {
    public OrderType orderType;
    public int responseType;
    public byte[] responseValue;
}
