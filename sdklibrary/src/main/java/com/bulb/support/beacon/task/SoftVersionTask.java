package com.bulb.support.beacon.task;


import com.bulb.support.beacon.callback.BulbOrderTaskCallback;
import com.bulb.support.beacon.entity.OrderType;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.SoftVersionTask
 */
public class SoftVersionTask extends OrderTask {

    public byte[] data;

    public SoftVersionTask(BulbOrderTaskCallback callback, int sendDataType) {
        super(OrderType.softVersion, callback, sendDataType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
