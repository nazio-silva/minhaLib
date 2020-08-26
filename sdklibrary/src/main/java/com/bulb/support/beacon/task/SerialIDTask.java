package com.bulb.support.beacon.task;

import com.bulb.support.beacon.callback.BulbOrderTaskCallback;
import com.bulb.support.beacon.entity.OrderType;
import com.bulb.support.beacon.utils.BulbUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.SerialIDTask
 */
public class SerialIDTask extends OrderTask {

    public byte[] data;

    public SerialIDTask(BulbOrderTaskCallback callback, int sendDataType) {
        super(OrderType.serialID, callback, sendDataType);
    }

    public void setData(String deviceId) {
        data = BulbUtils.hex2bytes(BulbUtils.string2Hex(deviceId));
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
