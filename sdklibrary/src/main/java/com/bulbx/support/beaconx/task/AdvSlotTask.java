package com.bulbx.support.beaconx.task;

import com.bulbx.support.beaconx.callback.BulbOrderTaskCallback;
import com.bulbx.support.beaconx.entity.OrderType;
import com.bulbx.support.beaconx.entity.SlotEnum;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.AdvSlotTask
 */
public class AdvSlotTask extends OrderTask {

    public byte[] data;

    public AdvSlotTask(BulbOrderTaskCallback callback, int responseType) {
        super(OrderType.advSlot, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(SlotEnum slot) {
        data = new byte[]{(byte) slot.getSlot()};
    }
}
