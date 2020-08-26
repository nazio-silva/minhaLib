package com.bulbxpro.support.beaconxpro.task;

import com.bulbxpro.support.beaconxpro.callback.BulbOrderTaskCallback;
import com.bulbxpro.support.beaconxpro.entity.OrderEnum;
import com.bulbxpro.support.beaconxpro.entity.OrderType;

/**
 * @Date 2019/6/14
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.NotifySavedHTTask
 */
public class NotifySavedHTTask extends OrderTask {

    public byte[] data;

    public NotifySavedHTTask(BulbOrderTaskCallback callback, int responseType) {
        super(OrderType.htSavedData, OrderEnum.SAVED_HT_NOTIFY, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
