package com.bulbx.support.beaconx.task;

import com.bulbx.support.beaconx.callback.BulbOrderTaskCallback;
import com.bulbx.support.beaconx.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.NotifyConfigTask
 */
public class NotifyConfigTask extends OrderTask {

    public byte[] data;

    public NotifyConfigTask(BulbOrderTaskCallback callback, int responseType) {
        super(OrderType.notifyConfig, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
