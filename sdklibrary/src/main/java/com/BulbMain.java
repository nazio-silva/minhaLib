package com;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import com.bulb.support.beacon.BulbSupport;
import com.bulbxpro.support.beaconxpro.callback.BulbScanDeviceCallback;
import com.bulbxpro.support.beaconxpro.entity.DeviceInfo;
import com.utils.SendEvent;

public class BulbMain implements BulbScanDeviceCallback {

    private final Context Appcontext;
    //private Promise connectedPromise;

    public BulbMain(Context context) {
        this.Appcontext = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startscan()
    {
        BulbSupport.getInstance().startScanDevice((com.bulb.support.beacon.callback.BulbScanDeviceCallback) this.Appcontext);
    }

    public void stopscan()
    {
        BulbSupport.getInstance().stopScanDevice();
    }

    public String getName()
    {
        return "BulbMain";
    }

    @Override
    public void onStartScan() {
        System.out.print("scaneando: " + true);
        System.out.println("Iniciado o scaneamento dos beacons!");

        SendEvent.emit();
    }

    @Override
    public void onScanDevice(DeviceInfo device) {

        //params = Arguments.createMap();
        System.out.println("device" + device );
        System.out.println("mac" + device.mac );
        System.out.println("name" + device.name );
        System.out.println("rssi" + device.rssi );
        //System.out.println("data" + createMapWithDevice(device));

        SendEvent.emit();

    }

    @Override
    public void onStopScan() {
        //connectedPromise = null;
        BulbSupport.getInstance().stopScanDevice();
    }

    /*private WritableMap createMapWithDevice(DeviceInfo device) {

        WritableMap b = new WritableNativeMap();

        System.out.println("ModelBeaconXPro", beaconxpro.getData(device));
        System.out.println("ModelBeaconX", beaconx.getData(device));
        System.out.println("ModelBeacon", beacon.getData(device));

        return b;

    }
    */
}