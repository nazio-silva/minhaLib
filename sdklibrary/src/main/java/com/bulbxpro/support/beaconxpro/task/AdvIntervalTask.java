package com.bulbxpro.support.beaconxpro.task;

import com.bulbxpro.support.beaconxpro.callback.BulbOrderTaskCallback;
import com.bulbxpro.support.beaconxpro.entity.OrderEnum;
import com.bulbxpro.support.beaconxpro.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.AdvIntervalTask
 */
public class AdvIntervalTask extends OrderTask {

    public byte[] data;

    public AdvIntervalTask(BulbOrderTaskCallback callback, int responseType) {
        super(OrderType.advInterval, OrderEnum.ADV_INTERVAL, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
