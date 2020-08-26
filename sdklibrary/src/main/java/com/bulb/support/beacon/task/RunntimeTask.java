package com.bulb.support.beacon.task;


import com.bulb.support.beacon.callback.BulbOrderTaskCallback;
import com.bulb.support.beacon.entity.OrderType;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.RunntimeTask
 */
public class RunntimeTask extends OrderTask {

    public byte[] data;

    public RunntimeTask(BulbOrderTaskCallback callback, int sendDataType) {
        super(OrderType.writeAndNotify, callback, sendDataType);
        setData();
    }

    public void setData() {
        data = new byte[4];
        data[0] = Integer.valueOf(Integer.toHexString(234), 16).byteValue();
        data[1] = (byte) 89;
        data[2] = 0;
        data[3] = 0;
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
