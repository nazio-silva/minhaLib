package com.bulb.support.beacon.handler;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import com.bulb.support.beacon.entity.BulbCharacteristic;
import com.bulb.support.beacon.entity.OrderType;

import java.util.HashMap;
import java.util.List;

/**
 * @Date 2017/12/13 0013
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.handler.BulbCharacteristicHandler
 */
public class BulbCharacteristicHandler {
    private static BulbCharacteristicHandler INSTANCE;

    public static final String SERVICE_UUID_HEADER_BATTERY = "0000180f";
    public static final String SERVICE_UUID_HEADER_SYSTEM = "0000180a";
    public static final String SERVICE_UUID_HEADER_PARAMS = "0000ff00";
    public HashMap<OrderType, BulbCharacteristic> bulbCharacteristicMap;

    private BulbCharacteristicHandler() {
        //no instance
        bulbCharacteristicMap = new HashMap<>();
    }

    public static BulbCharacteristicHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (BulbCharacteristicHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BulbCharacteristicHandler();
                }
            }
        }
        return INSTANCE;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public HashMap<OrderType, BulbCharacteristic> getCharacteristics(BluetoothGatt gatt) {
        if (bulbCharacteristicMap != null && !bulbCharacteristicMap.isEmpty()) {
            bulbCharacteristicMap.clear();
        }
        List<BluetoothGattService> services = gatt.getServices();
        for (BluetoothGattService service : services) {
            String serviceUuid = service.getUuid().toString();
            if (TextUtils.isEmpty(serviceUuid)) {
                continue;
            }
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            if (serviceUuid.startsWith(SERVICE_UUID_HEADER_BATTERY)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.battery.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.battery, new BulbCharacteristic(characteristic, OrderType.battery));
                        continue;
                    }
                }
            }
            if (service.getUuid().toString().startsWith(SERVICE_UUID_HEADER_SYSTEM)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    // 软件版本
                    if (characteristicUuid.equals(OrderType.softVersion.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.softVersion, new BulbCharacteristic(characteristic, OrderType.softVersion));
                        continue;
                    }
                    // 厂商名称
                    if (characteristicUuid.equals(OrderType.firmname.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.firmname, new BulbCharacteristic(characteristic, OrderType.firmname));
                        continue;
                    }
                    // 设备名称
                    if (characteristicUuid.equals(OrderType.devicename.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.devicename, new BulbCharacteristic(characteristic, OrderType.devicename));
                        continue;
                    }
                    // 出厂日期
                    if (characteristicUuid.equals(OrderType.iBeaconDate.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.iBeaconDate, new BulbCharacteristic(characteristic, OrderType.iBeaconDate));
                        continue;
                    }
                    // 硬件版本号
                    if (characteristicUuid.equals(OrderType.hardwareVersion.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.hardwareVersion, new BulbCharacteristic(characteristic, OrderType.hardwareVersion));
                        continue;
                    }
                    // 固件版本号
                    if (characteristicUuid.equals(OrderType.firmwareVersion.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.firmwareVersion, new BulbCharacteristic(characteristic, OrderType.firmwareVersion));
                        continue;
                    }
                }
            }
            if (service.getUuid().toString().startsWith(SERVICE_UUID_HEADER_PARAMS)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    // 写和通知
                    if (characteristicUuid.equals(OrderType.writeAndNotify.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        bulbCharacteristicMap.put(OrderType.writeAndNotify, new BulbCharacteristic(characteristic, OrderType.writeAndNotify));
                        continue;
                    }
                    // uuid
                    if (characteristicUuid.equals(OrderType.iBeaconUuid.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.iBeaconUuid, new BulbCharacteristic(characteristic, OrderType.iBeaconUuid));
                        continue;
                    }
                    // major
                    if (characteristicUuid.equals(OrderType.major.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.major, new BulbCharacteristic(characteristic, OrderType.major));
                        continue;
                    }
                    // minor
                    if (characteristicUuid.equals(OrderType.minor.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.minor, new BulbCharacteristic(characteristic, OrderType.minor));
                        continue;
                    }
                    // measure_power
                    if (characteristicUuid.equals(OrderType.measurePower.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.measurePower, new BulbCharacteristic(characteristic, OrderType.measurePower));
                        continue;
                    }
                    // transmission
                    if (characteristicUuid.equals(OrderType.transmission.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.transmission, new BulbCharacteristic(characteristic, OrderType.transmission));
                        continue;
                    }
                    // change_password
                    if (characteristicUuid.equals(OrderType.changePassword.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        bulbCharacteristicMap.put(OrderType.changePassword, new BulbCharacteristic(characteristic, OrderType.changePassword));
                        continue;
                    }
                    // broadcasting_interval
                    if (characteristicUuid.equals(OrderType.broadcastingInterval.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.broadcastingInterval, new BulbCharacteristic(characteristic, OrderType.broadcastingInterval));
                        continue;
                    }
                    // serial_id
                    if (characteristicUuid.equals(OrderType.serialID.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.serialID, new BulbCharacteristic(characteristic, OrderType.serialID));
                        continue;
                    }
                    // iBeacon_name
                    if (characteristicUuid.equals(OrderType.iBeaconName.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.iBeaconName, new BulbCharacteristic(characteristic, OrderType.iBeaconName));
                        continue;
                    }
                    // connection_mode
                    if (characteristicUuid.equals(OrderType.connectionMode.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.connectionMode, new BulbCharacteristic(characteristic, OrderType.connectionMode));
                        continue;
                    }
                    // soft_reboot
                    if (characteristicUuid.equals(OrderType.softReboot.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.softReboot, new BulbCharacteristic(characteristic, OrderType.softReboot));
                        continue;
                    }
                    // iBeacon_mac
                    if (characteristicUuid.equals(OrderType.iBeaconMac.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.iBeaconMac, new BulbCharacteristic(characteristic, OrderType.iBeaconMac));
                        continue;
                    }
                    // overtime
                    if (characteristicUuid.equals(OrderType.overtime.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.overtime, new BulbCharacteristic(characteristic, OrderType.overtime));
                        continue;
                    }
                }
            }
//            LogModule.i("service uuid:" + service.getUuid().toString());
//            List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
//            for (BluetoothGattCharacteristic characteristic : characteristicList) {
//                LogModule.i("characteristic uuid:" + characteristic.getUuid().toString());
//                LogModule.i("characteristic properties:" + BulbUtils.getCharPropertie(characteristic.getProperties()));
//                List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
//                for (BluetoothGattDescriptor descriptor : descriptors) {
//                    LogModule.i("descriptor uuid:" + descriptor.getUuid().toString());
//                    LogModule.i("descriptor value:" + BulbUtils.bytesToHexString(descriptor.getValue()));
//                }
//            }
        }
        return bulbCharacteristicMap;
    }
}
