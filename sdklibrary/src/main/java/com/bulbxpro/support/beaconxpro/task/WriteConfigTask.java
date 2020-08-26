package com.bulbxpro.support.beaconxpro.task;

import com.bulbxpro.support.beaconxpro.callback.BulbOrderTaskCallback;
import com.bulbxpro.support.beaconxpro.entity.ConfigKeyEnum;
import com.bulbxpro.support.beaconxpro.entity.OrderEnum;
import com.bulbxpro.support.beaconxpro.entity.OrderType;
import com.bulbxpro.support.beaconxpro.utils.BulbUtils;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.WriteConfigTask
 */
public class WriteConfigTask extends OrderTask {
    public byte[] data;

    public WriteConfigTask(BulbOrderTaskCallback callback) {
        super(OrderType.writeConfig, OrderEnum.WRITE_CONFIG, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(ConfigKeyEnum key) {
        switch (key) {
//            case GET_SLOT_TYPE:
            case GET_DEVICE_MAC:
//            case GET_DEVICE_NAME:
            case GET_CONNECTABLE:
            case GET_IBEACON_UUID:
            case GET_IBEACON_INFO:
            case SET_CLOSE:
            case GET_AXIX_PARAMS:
            case GET_TH_PERIOD:
            case GET_STORAGE_CONDITION:
            case GET_DEVICE_TIME:
            case SET_TH_EMPTY:
            case GET_TRIGGER_DATA:
                createGetConfigData(key.getConfigKey());
                break;
        }
    }

    private void createGetConfigData(int configKey) {
        data = new byte[]{(byte) 0xEA, (byte) configKey, (byte) 0x00, (byte) 0x00};
    }

    public void setiBeaconData(int major, int minor, int advTxPower) {
        String value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_IBEACON_INFO.getConfigKey()) + "0005"
                + String.format("%04X", major) + String.format("%04X", minor) + BulbUtils.int2HexString(Math.abs(advTxPower));
        data = BulbUtils.hex2bytes(value);
    }

    public void setiBeaconUUID(String uuidHex) {
        String value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_IBEACON_UUID.getConfigKey()) + "0010"
                + uuidHex;
        data = BulbUtils.hex2bytes(value);
    }

    public void setConneactable(boolean isConnectable) {
        String value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_CONNECTABLE.getConfigKey()) + "0001"
                + (isConnectable ? "01" : "00");
        data = BulbUtils.hex2bytes(value);
    }

    public void setAxisParams(int rate, int scale, int sensitivity) {
        String value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_AXIX_PARAMS.getConfigKey()) + "0003"
                + String.format("%02X", rate) + String.format("%02X", scale) + String.format("%02X", sensitivity);
        data = BulbUtils.hex2bytes(value);
    }

    public void setTHPriod(int period) {
        String value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_TH_PERIOD.getConfigKey()) + "0002"
                + String.format("%04X", period);
        data = BulbUtils.hex2bytes(value);
    }

    public void setStorageCondition(int storageType, String storageData) {
        String value = "00";
        switch (storageType) {
            case 0:
                value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_STORAGE_CONDITION.getConfigKey()) + "0003"
                        + String.format("%02X", storageType) + storageData;
                break;
            case 1:
                value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_STORAGE_CONDITION.getConfigKey()) + "0003"
                        + String.format("%02X", storageType) + storageData;
                break;
            case 2:
                value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_STORAGE_CONDITION.getConfigKey()) + "0005"
                        + String.format("%02X", storageType) + storageData;
                break;
            case 3:
                value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_STORAGE_CONDITION.getConfigKey()) + "0002"
                        + String.format("%02X", storageType) + storageData;
                break;
        }

        data = BulbUtils.hex2bytes(value);
    }

    public void setDeviceTime(int year, int month, int day, int hour, int minute, int second) {
        String value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_DEVICE_TIME.getConfigKey()) + "0006"
                + String.format("%02X", year) + String.format("%02X", month) + String.format("%02X", day)
                + String.format("%02X", hour) + String.format("%02X", minute) + String.format("%02X", second);
        data = BulbUtils.hex2bytes(value);
    }

    public void setTriggerData() {
        String value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_TRIGGER_DATA.getConfigKey()) + "000100";
        data = BulbUtils.hex2bytes(value);
    }

    public void setTriggerData(int triggerType, boolean isAbove, int params, boolean isStart) {
        String value = "00";
        byte[] paramsBytes = BulbUtils.short2Byte((short) params);
        switch (triggerType) {
            case 1:
                value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_TRIGGER_DATA.getConfigKey()) + "0005"
                        + "01" + (isAbove ? "01" : "02") + BulbUtils.bytesToHexString(paramsBytes) + (isStart ? "01" : "02");
                break;
            case 2:
                value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_TRIGGER_DATA.getConfigKey()) + "0005"
                        + "02" + (isAbove ? "01" : "02") + BulbUtils.bytesToHexString(paramsBytes) + (isStart ? "01" : "02");
                break;
        }
        data = BulbUtils.hex2bytes(value);
    }

    public void setTriggerData(int triggerType, int params, boolean isStart) {
        String value = "00";
        switch (triggerType) {
            case 3:
                value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_TRIGGER_DATA.getConfigKey()) + "0004"
                        + "03" + String.format("%04X", params) + (isStart ? "01" : "02");
                break;
            case 4:
                value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_TRIGGER_DATA.getConfigKey()) + "0004"
                        + "04" + String.format("%04X", params) + (isStart ? "01" : "02");
                break;
            case 5:
                value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_TRIGGER_DATA.getConfigKey()) + "0004"
                        + "05" + String.format("%04X", params) + (isStart ? "02" : "01");
                break;
        }
        data = BulbUtils.hex2bytes(value);
    }
}
