package com.bulb.support.beacon.task;

import com.bulb.support.beacon.callback.BulbOrderTaskCallback;
import com.bulb.support.beacon.entity.OrderType;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.BatteryTask
 */
public class BatteryTask extends OrderTask {

    public byte[] data;

    public BatteryTask(BulbOrderTaskCallback callback, int sendDataType) {
        super(OrderType.battery, callback, sendDataType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
