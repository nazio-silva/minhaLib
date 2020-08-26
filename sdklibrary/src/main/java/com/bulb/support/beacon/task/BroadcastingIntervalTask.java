package com.bulb.support.beacon.task;

import com.bulb.support.beacon.callback.BulbOrderTaskCallback;
import com.bulb.support.beacon.entity.OrderType;
import com.bulb.support.beacon.utils.BulbUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.BroadcastingIntervalTask
 */
public class BroadcastingIntervalTask extends OrderTask {

    public byte[] data;

    public BroadcastingIntervalTask(BulbOrderTaskCallback callback, int sendDataType) {
        super(OrderType.broadcastingInterval, callback, sendDataType);
    }

    public void setData(int broadcastInterval) {
        data = BulbUtils.hex2bytes(Integer.toHexString(broadcastInterval));
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
