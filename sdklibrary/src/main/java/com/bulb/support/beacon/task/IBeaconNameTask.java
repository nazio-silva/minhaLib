package com.bulb.support.beacon.task;


import com.bulb.support.beacon.callback.BulbOrderTaskCallback;
import com.bulb.support.beacon.entity.OrderType;
import com.bulb.support.beacon.utils.BulbUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.IBeaconNameTask
 */
public class IBeaconNameTask extends OrderTask {

    public byte[] data;

    public IBeaconNameTask(BulbOrderTaskCallback callback, int sendDataType) {
        super(OrderType.iBeaconName, callback, sendDataType);
    }

    public void setData(String deviceName) {
        data = BulbUtils.hex2bytes(BulbUtils.string2Hex(deviceName));
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
