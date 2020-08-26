package com.bulb.support.beacon.task;


import com.bulb.support.beacon.callback.BulbOrderTaskCallback;
import com.bulb.support.beacon.entity.OrderType;
import com.bulb.support.beacon.utils.BulbUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.TransmissionTask
 */
public class TransmissionTask extends OrderTask {

    public byte[] data;

    public TransmissionTask(BulbOrderTaskCallback callback, int sendDataType) {
        super(OrderType.transmission, callback, sendDataType);
    }

    public void setData(int transmission) {
        data = BulbUtils.hex2bytes(Integer.toHexString(transmission));
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
