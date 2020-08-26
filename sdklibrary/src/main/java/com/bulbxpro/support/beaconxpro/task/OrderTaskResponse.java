package com.bulbxpro.support.beaconxpro.task;

import com.bulbxpro.support.beaconxpro.entity.OrderEnum;
import com.bulbxpro.support.beaconxpro.entity.OrderType;

import java.io.Serializable;

/**
 * @Date 2018/1/23
 * @Author wenzheng.liu
 * @Description 任务反馈
 * @ClassPath com.bulb.support.task.OrderTaskResponse
 */
public class OrderTaskResponse implements Serializable {
    public OrderType orderType;
    public OrderEnum order;
    public int responseType;
    public byte[] responseValue;
}
