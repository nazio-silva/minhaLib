package com.bulb.support.beacon.task;

import com.bulb.support.beacon.callback.BulbOrderTaskCallback;
import com.bulb.support.beacon.entity.OrderType;

/**
 * @Date 2019/3/12
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.CloseTask
 */
public class CloseTask extends OrderTask {

    public byte[] data;

    public CloseTask(BulbOrderTaskCallback callback, int sendDataType) {
        super(OrderType.writeAndNotify, callback, sendDataType);
        setData();
    }

    public void setData() {
        data = new byte[5];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x6D;
        data[2] = 0;
        data[3] = 1;
        data[4] = 0;
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
