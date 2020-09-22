package com.bulb.support.beacon.entity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.bulb.support.beacon.utils.BulbUtils;

import java.io.Serializable;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.entity.BulbCharacteristic
 */
public class BulbCharacteristic implements Serializable {
    public BluetoothGattCharacteristic characteristic;
    public String charPropertie;
    public OrderType orderType;

    public BulbCharacteristic(BluetoothGattCharacteristic characteristic, String charPropertie, OrderType orderType) {
        this.characteristic = characteristic;
        this.charPropertie = charPropertie;
        this.orderType = orderType;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public BulbCharacteristic(BluetoothGattCharacteristic characteristic, OrderType orderType) {
        this(characteristic, BulbUtils.getCharPropertie(characteristic.getProperties()), orderType);
    }
}
