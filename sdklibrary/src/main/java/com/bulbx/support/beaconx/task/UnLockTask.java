package com.bulbx.support.beaconx.task;

import com.bulbx.support.beaconx.callback.BulbOrderTaskCallback;
import com.bulbx.support.beaconx.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.UnLockTask
 */
public class UnLockTask extends OrderTask {

    public byte[] data;

    public UnLockTask(BulbOrderTaskCallback callback, int responseType) {
        super(OrderType.unLock, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(byte[] unLockBytes) {
        data = unLockBytes;
    }
}
