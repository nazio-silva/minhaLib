package com.bulbxpro.support.beaconxpro.task;

import com.bulbxpro.support.beaconxpro.callback.BulbOrderTaskCallback;
import com.bulbxpro.support.beaconxpro.entity.OrderEnum;
import com.bulbxpro.support.beaconxpro.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.ConnectableTask
 */
public class ConnectableTask extends OrderTask {

    public byte[] data;

    public ConnectableTask(BulbOrderTaskCallback callback, int responseType) {
        super(OrderType.connectable, OrderEnum.CONNECTABLE, callback, responseType);
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
