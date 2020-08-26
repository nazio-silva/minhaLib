package com.bulbx.support.beaconx.handler;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import com.bulbx.support.beaconx.entity.BulbCharacteristic;
import com.bulbx.support.beaconx.entity.OrderType;

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

    public static final String SERVICE_UUID_HEADER_DEVICE = "0000180a";
    public static final String SERVICE_UUID_HEADER_BATTERY = "0000180f";
    public static final String SERVICE_UUID_HEADER_NOTIFY = "e62a0001";
    public static final String SERVICE_UUID_HEADER_EDDYSTONE = "a3c87500";
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
            if (serviceUuid.startsWith("00001800") || serviceUuid.startsWith("00001801")) {
                continue;
            }
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            if (service.getUuid().toString().startsWith(SERVICE_UUID_HEADER_DEVICE)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }

                    if (characteristicUuid.equals(OrderType.manufacturer.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.manufacturer, new BulbCharacteristic(characteristic, OrderType.manufacturer));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.deviceModel.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.deviceModel, new BulbCharacteristic(characteristic, OrderType.deviceModel));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.productDate.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.productDate, new BulbCharacteristic(characteristic, OrderType.productDate));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.hardwareVersion.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.hardwareVersion, new BulbCharacteristic(characteristic, OrderType.hardwareVersion));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.firmwareVersion.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.firmwareVersion, new BulbCharacteristic(characteristic, OrderType.firmwareVersion));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.softwareVersion.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.softwareVersion, new BulbCharacteristic(characteristic, OrderType.softwareVersion));
                        continue;
                    }
                }
            }
            if (service.getUuid().toString().startsWith(SERVICE_UUID_HEADER_BATTERY)) {
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
            if (service.getUuid().toString().startsWith(SERVICE_UUID_HEADER_NOTIFY)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.notifyConfig.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        bulbCharacteristicMap.put(OrderType.notifyConfig, new BulbCharacteristic(characteristic, OrderType.notifyConfig));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.writeConfig.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.writeConfig, new BulbCharacteristic(characteristic, OrderType.writeConfig));
                        continue;
                    }
                }
            }
            if (service.getUuid().toString().startsWith(SERVICE_UUID_HEADER_EDDYSTONE)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.advSlot.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.advSlot, new BulbCharacteristic(characteristic, OrderType.advSlot));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.advInterval.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.advInterval, new BulbCharacteristic(characteristic, OrderType.advInterval));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.radioTxPower.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.radioTxPower, new BulbCharacteristic(characteristic, OrderType.radioTxPower));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.advTxPower.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.advTxPower, new BulbCharacteristic(characteristic, OrderType.advTxPower));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.lockState.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.lockState, new BulbCharacteristic(characteristic, OrderType.lockState));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.unLock.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.unLock, new BulbCharacteristic(characteristic, OrderType.unLock));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.advSlotData.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.advSlotData, new BulbCharacteristic(characteristic, OrderType.advSlotData));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.resetDevice.getUuid())) {
                        bulbCharacteristicMap.put(OrderType.resetDevice, new BulbCharacteristic(characteristic, OrderType.resetDevice));
                        continue;
                    }
                }
            }
//            LogModule.i("service uuid:" + service.getUuid().toString());
//            List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
//            for (BluetoothGattCharacteristic characteristic : characteristicList) {
//                LogModule.i("   characteristic uuid:" + characteristic.getUuid().toString());
//                LogModule.i("   characteristic properties:" + BulbUtils.getCharPropertie(characteristic.getProperties()));
//                List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
//                for (BluetoothGattDescriptor descriptor : descriptors) {
//                    LogModule.i("       descriptor uuid:" + descriptor.getUuid().toString());
//                    LogModule.i("       descriptor value:" + BulbUtils.bytesToHexString(descriptor.getValue()));
//                }
//            }
        }
        return bulbCharacteristicMap;
    }
}
