package com.bulbxpro.support.beaconxpro.handler;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.bulbxpro.support.beaconxpro.BulbSupport;
import com.bulbxpro.support.beaconxpro.callback.BulbResponseCallback;
import com.bulbxpro.support.beaconxpro.log.LogModule;
import com.bulbxpro.support.beaconxpro.utils.BulbUtils;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 自定义蓝牙连接回调
 * @ClassPath com.bulb.support.handler.BulbConnStateHandler
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BulbConnStateHandler extends BluetoothGattCallback {

    private static volatile BulbConnStateHandler INSTANCE;

    private BulbResponseCallback mBulbResponseCallback;
    private BulbSupport.ServiceMessageHandler mHandler;

    public BulbConnStateHandler() {
    }

    public static BulbConnStateHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (BulbConnStateHandler.class) {
                if (INSTANCE == null) {
                    LogModule.v("创建BluetoothGattCallback！");
                    INSTANCE = new BulbConnStateHandler();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        LogModule.e("onConnectionStateChange");
        LogModule.i("status : " + status);
        LogModule.i("newState : " + newState);
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mHandler.sendEmptyMessage(BulbSupport.HANDLER_MESSAGE_WHAT_CONNECTED);
                return;
            }
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mHandler.sendEmptyMessage(BulbSupport.HANDLER_MESSAGE_WHAT_DISCONNECTED);
            return;
        }
        mHandler.sendEmptyMessage(BulbSupport.HANDLER_MESSAGE_WHAT_DISCONNECTED);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        LogModule.e("onServicesDiscovered");
        LogModule.i("status : " + status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mHandler.sendEmptyMessage(BulbSupport.HANDLER_MESSAGE_WHAT_SERVICES_DISCOVERED);
        } else {
            mHandler.sendEmptyMessage(BulbSupport.HANDLER_MESSAGE_WHAT_DISCONNECTED);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        LogModule.d("onCharacteristicChanged");
        LogModule.e("device to app : " + BulbUtils.bytesToHexString(characteristic.getValue()));
        mBulbResponseCallback.onCharacteristicChanged(characteristic, characteristic.getValue());
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        LogModule.d("onCharacteristicWrite");
        LogModule.e("device to app : " + BulbUtils.bytesToHexString(characteristic.getValue()));
        mBulbResponseCallback.onCharacteristicWrite(characteristic.getValue());
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        LogModule.d("onCharacteristicRead");
        LogModule.e("device to app : " + BulbUtils.bytesToHexString(characteristic.getValue()));
        mBulbResponseCallback.onCharacteristicRead(characteristic.getValue());
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        mBulbResponseCallback.onDescriptorWrite();
    }

    public void setBulbResponseCallback(BulbResponseCallback mBulbResponseCallback) {
        this.mBulbResponseCallback = mBulbResponseCallback;
    }

    public void setMessageHandler(BulbSupport.ServiceMessageHandler messageHandler) {
        this.mHandler = messageHandler;
    }
}
