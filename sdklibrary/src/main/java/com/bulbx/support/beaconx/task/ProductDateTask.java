package com.bulbx.support.beaconx.task;

import com.bulbx.support.beaconx.callback.BulbOrderTaskCallback;
import com.bulbx.support.beaconx.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.ProductDateTask
 */
public class ProductDateTask extends OrderTask {

    public byte[] data;

    public ProductDateTask(BulbOrderTaskCallback callback, int responseType) {
        super(OrderType.productDate, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
