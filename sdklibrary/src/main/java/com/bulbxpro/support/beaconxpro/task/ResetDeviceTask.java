package com.bulbxpro.support.beaconxpro.task;

import com.bulbxpro.support.beaconxpro.callback.BulbOrderTaskCallback;
import com.bulbxpro.support.beaconxpro.entity.OrderEnum;
import com.bulbxpro.support.beaconxpro.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.ResetDeviceTask
 */
public class ResetDeviceTask extends OrderTask {

    public byte[] data = {(byte) 0x0b};

    public ResetDeviceTask(BulbOrderTaskCallback callback) {
        super(OrderType.resetDevice, OrderEnum.RESET_DEVICE, callback, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
