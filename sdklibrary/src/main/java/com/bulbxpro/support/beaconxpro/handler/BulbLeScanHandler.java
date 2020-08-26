package com.bulbxpro.support.beaconxpro.handler;

import android.bluetooth.BluetoothDevice;

import com.bulbxpro.support.beaconxpro.callback.BulbScanDeviceCallback;
import com.bulbxpro.support.beaconxpro.entity.DeviceInfo;
import com.bulbxpro.support.beaconxpro.utils.BulbUtils;

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
            if (scanRecord.length == 0 || rssi == 127) {
                return;
            }
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.name = result.getScanRecord().getDeviceName();
            deviceInfo.rssi = rssi;
            deviceInfo.mac = device.getAddress();
            String scanRecordStr = BulbUtils.bytesToHexString(scanRecord);
            deviceInfo.scanRecord = scanRecordStr;
            deviceInfo.scanResult = result;
            callback.onScanDevice(deviceInfo);
        }
    }
}