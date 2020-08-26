package com.bulbx.support.beaconx.task;

import com.bulbx.support.beaconx.callback.BulbOrderTaskCallback;
import com.bulbx.support.beaconx.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.AdvIntervalTask
 */
public class AdvIntervalTask extends OrderTask {

    public byte[] data;

    public AdvIntervalTask(BulbOrderTaskCallback callback, int responseType) {
        super(OrderType.advInterval, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
