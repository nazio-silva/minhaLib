package com.bulb.support.beacon.task;


import com.bulb.support.beacon.callback.BulbOrderTaskCallback;
import com.bulb.support.beacon.entity.OrderType;
import com.bulb.support.beacon.utils.BulbUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.MeasurePowerTask
 */
public class MeasurePowerTask extends OrderTask {

    public byte[] data;

    public MeasurePowerTask(BulbOrderTaskCallback callback, int sendDataType) {
        super(OrderType.measurePower, callback, sendDataType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(int measurePower) {
        data = BulbUtils.hex2bytes(Integer.toHexString(measurePower));
    }
}
