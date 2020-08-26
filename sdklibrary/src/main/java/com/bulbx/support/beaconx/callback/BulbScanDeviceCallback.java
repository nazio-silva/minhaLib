package com.bulbx.support.beaconx.callback;

import com.bulbx.support.beaconx.entity.DeviceInfo;

/**
 * @Date 2017/12/8 0008
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.callback.BulbScanDeviceCallback
 */
public interface BulbScanDeviceCallback {
    void onStartScan();

    void onScanDevice(DeviceInfo device);

    void onStopScan();
}
