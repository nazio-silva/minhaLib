package com.bulbxpro.support.beaconxpro.task;


import com.bulbxpro.support.beaconxpro.BulbSupport;
import com.bulbxpro.support.beaconxpro.callback.BulbOrderTaskCallback;
import com.bulbxpro.support.beaconxpro.entity.OrderEnum;
import com.bulbxpro.support.beaconxpro.entity.OrderType;
import com.bulbxpro.support.beaconxpro.log.LogModule;

public class OpenNotifyTask extends OrderTask {
    public byte[] data;

    public OpenNotifyTask(OrderType orderType, OrderEnum orderEnum, BulbOrderTaskCallback callback) {
        super(orderType, orderEnum, callback, OrderTask.RESPONSE_TYPE_NOTIFY);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    @Override
    public boolean timeoutPreTask() {
        LogModule.i(order.getOrderName() + "超时");
        BulbSupport.getInstance().pollTask();
        BulbSupport.getInstance().onOpenNotifyTimeout();
        return false;
    }
}
