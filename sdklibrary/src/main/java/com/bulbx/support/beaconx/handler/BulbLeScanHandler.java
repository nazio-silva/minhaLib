package com.bulbx.support.beaconx.handler;

import android.bluetooth.BluetoothDevice;

import com.bulbx.support.beaconx.callback.BulbScanDeviceCallback;
import com.bulbx.support.beaconx.entity.DeviceInfo;
import com.bulbx.support.beaconx.utils.BulbUtils;

import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * @Date 2017/12/12 0012
 * @Author wenzheng.liu
 * @Description 搜索设备回调类
 * @ClassPath com.bulb.support.handler.BulbLeScanHandler
 */
public class BulbLeScanHandler extends ScanCallback {
    private BulbScanDeviceCallback callback;

    public BulbLeScanHandler(BulbScanDeviceCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        if (result != null) {
            BluetoothDevice device = result.getDevice();
            byte[] scanRecord = result.getScanRecord().getBytes();
            int rssi = result.getRssi();
            if (scanRecord.length == 0 || rssi < -127 || rssi == 127) {
                return;
            }
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.name = device.getName();
            deviceInfo.rssi = rssi;
            deviceInfo.mac = device.getAddress();
            String scanRecordStr = BulbUtils.bytesToHexString(scanRecord);
            deviceInfo.scanRecord = scanRecordStr;
            callback.onScanDevice(deviceInfo);
        }
    }
}