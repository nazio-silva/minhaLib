package com.bulbx.support.beaconx.task;

import com.bulbx.support.beaconx.callback.BulbOrderTaskCallback;
import com.bulbx.support.beaconx.entity.ConfigKeyEnum;
import com.bulbx.support.beaconx.entity.OrderType;
import com.bulbx.support.beaconx.utils.BulbUtils;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.task.WriteConfigTask
 */
public class WriteConfigTask extends OrderTask {
    public byte[] data;

    public WriteConfigTask(BulbOrderTaskCallback callback, int responseType) {
        super(OrderType.writeConfig, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(ConfigKeyEnum key) {
        switch (key) {
            case GET_SLOT_TYPE:
            case GET_DEVICE_MAC:
            case GET_DEVICE_NAME:
            case GET_CONNECTABLE:
            case GET_IBEACON_UUID:
            case GET_IBEACON_INFO:
            case SET_CLOSE:
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

    public void setDeviceName(String deviceName) {
        String deviceNameHex = BulbUtils.string2Hex(deviceName);
        String value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_DEVICE_NAME.getConfigKey()) + "00"
                + BulbUtils.int2HexString(deviceNameHex.length() / 2) + deviceNameHex;
        data = BulbUtils.hex2bytes(value);
    }

    public void setConneactable(boolean isConnectable) {
        String value = "EA" + BulbUtils.int2HexString(ConfigKeyEnum.SET_CONNECTABLE.getConfigKey()) + "0001"
                + (isConnectable ? "01" : "00");
        data = BulbUtils.hex2bytes(value);
    }
}
