package com.bulb.support.beacon.task;

import com.bulb.support.beacon.callback.BulbOrderTaskCallback;
import com.bulb.support.beacon.entity.OrderType;
import com.bulb.support.beacon.utils.BulbUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.ChangePasswordTask
 */
public class ChangePasswordTask extends OrderTask {

    public byte[] data;

    public ChangePasswordTask(BulbOrderTaskCallback callback, int sendDataType) {
        super(OrderType.changePassword, callback, sendDataType);
    }

    public void setData(String password) {
        String passwordHex = BulbUtils.string2Hex(password);
        data = BulbUtils.hex2bytes(passwordHex);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
