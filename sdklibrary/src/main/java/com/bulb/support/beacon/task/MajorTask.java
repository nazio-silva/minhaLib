package com.bulb.support.beacon.task;

import com.bulb.support.beacon.callback.BulbOrderTaskCallback;
import com.bulb.support.beacon.entity.OrderType;
import com.bulb.support.beacon.utils.BulbUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.MajorTask
 */
public class MajorTask extends OrderTask {

    public byte[] data;

    public MajorTask(BulbOrderTaskCallback callback, int sendDataType) {
        super(OrderType.major, callback, sendDataType);
    }

    public void setData(int marjor) {
        byte[] marjorBytes = BulbUtils.hex2bytes(Integer.toHexString(marjor));
        if (marjorBytes.length < 2) {
            data = new byte[2];
            data[0] = 0;
            data[1] = marjorBytes[0];
        } else {
            data = marjorBytes;
        }
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
