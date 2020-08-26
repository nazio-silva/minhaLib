package com.bulb.support.beacon.task;


import com.bulb.support.beacon.callback.BulbOrderTaskCallback;
import com.bulb.support.beacon.entity.OrderType;
import com.bulb.support.beacon.utils.BulbUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.IBeaconUuidTask
 */
public class IBeaconUuidTask extends OrderTask {

    public byte[] data;

    public IBeaconUuidTask(BulbOrderTaskCallback callback, int sendDataType) {
        super(OrderType.iBeaconUuid, callback, sendDataType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(String uuid) {
        String uuidHex = uuid.replaceAll("-", "");
        data = BulbUtils.hex2bytes(uuidHex);
    }
}
