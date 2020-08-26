package com.bulb.support.beacon.task;


import com.bulb.support.beacon.callback.BulbOrderTaskCallback;
import com.bulb.support.beacon.entity.OrderType;
import com.bulb.support.beacon.utils.BulbUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.ConnectionModeTask
 */
public class ConnectionModeTask extends OrderTask {

    public byte[] data;

    public ConnectionModeTask(BulbOrderTaskCallback callback, int sendDataType) {
        super(OrderType.connectionMode, callback, sendDataType);
    }

    public void setData(String connectionMode) {
        data = BulbUtils.hex2bytes(connectionMode);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
