package com;

import android.content.Context;

import com.bulb.support.beacon.BulbSupport;
import com.bulb.support.beacon.callback.BulbScanDeviceCallback;
import com.bulb.support.beacon.entity.DeviceInfo;

import static com.bulb.support.beacon.BulbSupport.getInstance;


public class BulbMain implements BulbScanDeviceCallback {

    private final Context Appcontext;
    //private Promise connectedPromise;

    private Context mContext;

    public BulbMain(Context context) {
        this.Appcontext = context;
    }

    public String getName()
    {
        return "BulbMain";
    }

    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startscan() {
        System.out.println("startscan Bulbmain");
        BulbSupport.getInstance().startScanDevice(BulbMain.this);
    }

    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopscan()
    {
        getInstance().stopScanDevice();
    }

    @Override
    public void onStartScan() {
        System.out.println("onStartScan Bulbmain: ");
       // SendEvent.emit();

    }

    @Override
    public void onScanDevice(DeviceInfo device) {

        //params = Arguments.createMap();
        System.out.println("device" + device );
        System.out.println("mac" + device.mac );
        System.out.println("name" + device.name );
        System.out.println("rssi" + device.rssi );
        //System.out.println("data" + createMapWithDevice(device));

        //SendEvent.emit();

    }

    @Override
    public void onStopScan() {
        //connectedPromise = null;
        System.out.println("Objeto Bulbmain onStopScan");

        getInstance().stopScanDevice();
    }
}