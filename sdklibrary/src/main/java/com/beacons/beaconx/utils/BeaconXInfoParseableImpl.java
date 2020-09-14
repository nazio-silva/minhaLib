package com.beacons.beaconx.utils;

import com.beacons.beaconx.entity.BeaconXInfo;
import com.bulb.support.beacon.entity.DeviceInfo;
import com.bulb.support.beacon.service.DeviceInfoParseable;
import com.bulb.support.beacon.utils.BulbUtils;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @Date 2018/1/16
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.beaconx.utils.BeaconXInfoParseableImpl
 */
public class BeaconXInfoParseableImpl implements DeviceInfoParseable<BeaconXInfo> {
    private HashMap<String, BeaconXInfo> beaconXInfoHashMap = new HashMap<>();

    @Override
    public BeaconXInfo parseDeviceInfo(DeviceInfo deviceInfo) {
        byte[] scanRecord = BulbUtils.hex2bytes(deviceInfo.scanRecord);
        // filter
        boolean isEddystone = false;
        boolean isBeacon = false;
        boolean isDeviceInfo = false;
        int length = 0;
        if (((int) scanRecord[5] & 0xff) == 0xAA && ((int) scanRecord[6] & 0xff) == 0xFE) {
            length = (int) scanRecord[7];
            isEddystone = true;
        }
        if (((int) scanRecord[5] & 0xff) == 0x20 && ((int) scanRecord[6] & 0xff) == 0xFF) {
            length = (int) scanRecord[3];
            isBeacon = true;
        }
        if (((int) scanRecord[5] & 0xff) == 0x10 && ((int) scanRecord[6] & 0xff) == 0xFF) {
            length = (int) scanRecord[3];
            isDeviceInfo = true;
        }
        if (!isEddystone && !isBeacon && !isDeviceInfo) {
            return null;
        }
        // avoid repeat
        BeaconXInfo beaconXInfo;
        if (beaconXInfoHashMap.containsKey(deviceInfo.mac)) {
            beaconXInfo = beaconXInfoHashMap.get(deviceInfo.mac);
            beaconXInfo.rssi = deviceInfo.rssi;
            beaconXInfo.scanRecord = deviceInfo.scanRecord;
        } else {
            beaconXInfo = new BeaconXInfo();
            beaconXInfo.name = deviceInfo.name;
            beaconXInfo.mac = deviceInfo.mac;
            beaconXInfo.rssi = deviceInfo.rssi;
            beaconXInfo.scanRecord = deviceInfo.scanRecord;
            beaconXInfo.validDataHashMap = new HashMap<>();
            beaconXInfoHashMap.put(deviceInfo.mac, beaconXInfo);
        }
        String data = null;
        if (isBeacon || isDeviceInfo) {
            data = BulbUtils.bytesToHexString(Arrays.copyOfRange(scanRecord, 7, length + 4));
        }
        if (isEddystone) {
            data = BulbUtils.bytesToHexString(Arrays.copyOfRange(scanRecord, 11, length + 8));
        }
        if (beaconXInfo.validDataHashMap.containsKey(data)) {
            return beaconXInfo;
        } else {
            BeaconXInfo.ValidData validData = new BeaconXInfo.ValidData();
            validData.data = data;
            if (isBeacon) {
                validData.type = BeaconXInfo.VALID_DATA_FRAME_TYPE_IBEACON;
            }
            if (isDeviceInfo) {
                validData.type = BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO;
                beaconXInfo.name = BulbUtils.hex2String(data.substring(22, data.length()));
            }
            if (isEddystone) {
                String frameType = data.substring(0, 2);
                if ("00".equals(frameType)) {
                    // UID
                    validData.type = BeaconXInfo.VALID_DATA_FRAME_TYPE_UID;
                } else if ("10".equals(frameType)) {
                    // URL
                    validData.type = BeaconXInfo.VALID_DATA_FRAME_TYPE_URL;
                } else if ("20".equals(frameType)) {
                    // TLM（only one）
                    validData.type = BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM;
                    beaconXInfo.validDataHashMap.put(frameType, validData);
                    return beaconXInfo;
                }
            }
            beaconXInfo.validDataHashMap.put(data, validData);
        }
        return beaconXInfo;
    }
}
