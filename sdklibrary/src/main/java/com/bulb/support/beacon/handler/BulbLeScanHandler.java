package com.bulb.support.beacon.handler;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.bulb.support.beacon.callback.BulbScanDeviceCallback;
import com.bulb.support.beacon.entity.DeviceInfo;
import com.bulb.support.beacon.utils.BulbUtils;

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
            if (TextUtils.isEmpty(device.getName()) || scanRecord.length == 0 || rssi == 127) {
                return;
            }
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.name = device.getName();
            deviceInfo.rssi = rssi;
            deviceInfo.mac = device.getAddress();
            String scanRecordStr = BulbUtils.bytesToHexString(scanRecord);
            deviceInfo.scanRecord = scanRecordStr;
            deviceInfo.scanResult = result;
            callback.onScanDevice(deviceInfo);
        }
    }
}