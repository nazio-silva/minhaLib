package com.bulbxpro.support.beaconxpro.task;

import com.bulbxpro.support.beaconxpro.callback.BulbOrderTaskCallback;
import com.bulbxpro.support.beaconxpro.entity.OrderEnum;
import com.bulbxpro.support.beaconxpro.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.NotifyConfigTask
 */
public class NotifyConfigTask extends OrderTask {

    public byte[] data;

    public NotifyConfigTask(BulbOrderTaskCallback callback, int responseType) {
        super(OrderType.notifyConfig, OrderEnum.OPEN_NOTIFY, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
