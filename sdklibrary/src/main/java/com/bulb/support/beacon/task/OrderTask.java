package com.bulb.support.beacon.task;

import com.bulb.support.beacon.callback.BulbOrderTaskCallback;
import com.bulb.support.beacon.entity.OrderType;

/**
 * @Date 2018/1/10
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.OrderTask
 */
public abstract class OrderTask {
    public static final int RESPONSE_TYPE_READ = 0;
    public static final int RESPONSE_TYPE_WRITE = 1;
    public static final int RESPONSE_TYPE_NOTIFY = 2;
    public static final int RESPONSE_TYPE_WRITE_NO_RESPONSE = 3;
    public static final int ORDER_STATUS_SUCCESS = 1;
    public OrderType orderType;
    public BulbOrderTaskCallback bulbOrderTaskCallback;
    public int responseType;
    public int orderStatus;


    public OrderTask(OrderType orderType, BulbOrderTaskCallback callback, int responseType) {
        this.orderType = orderType;
        this.bulbOrderTaskCallback = callback;
        this.responseType = responseType;
    }

    public abstract byte[] assemble();
}
