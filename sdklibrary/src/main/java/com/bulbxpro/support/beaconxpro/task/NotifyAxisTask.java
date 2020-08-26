package com.bulbxpro.support.beaconxpro.task;

import com.bulbxpro.support.beaconxpro.callback.BulbOrderTaskCallback;
import com.bulbxpro.support.beaconxpro.entity.OrderEnum;
import com.bulbxpro.support.beaconxpro.entity.OrderType;

/**
 * @Date 2019/6/14
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.NotifyAxisTask
 */
public class NotifyAxisTask extends OrderTask {

    public byte[] data;

    public NotifyAxisTask(BulbOrderTaskCallback callback, int responseType) {
        super(OrderType.axisData, OrderEnum.AXIS_NOTIFY, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
