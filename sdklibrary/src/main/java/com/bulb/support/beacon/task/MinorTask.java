package com.bulb.support.beacon.task;


import com.bulb.support.beacon.callback.BulbOrderTaskCallback;
import com.bulb.support.beacon.entity.OrderType;
import com.bulb.support.beacon.utils.BulbUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.MinorTask
 */
public class MinorTask extends OrderTask {

    public byte[] data;

    public MinorTask(BulbOrderTaskCallback callback, int sendDataType) {
        super(OrderType.minor, callback, sendDataType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(int minor) {
        byte[] minorBytes = BulbUtils.hex2bytes(Integer.toHexString(minor));
        if (minorBytes.length < 2) {
            data = new byte[2];
            data[0] = 0;
            data[1] = minorBytes[0];
        } else {
            data = minorBytes;
        }
    }
}
