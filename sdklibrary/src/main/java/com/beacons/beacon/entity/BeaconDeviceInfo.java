package com.beacons.beacon.entity;

import java.io.Serializable;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.beacon.entity.BeaconDeviceInfo
 */
public class BeaconDeviceInfo implements Serializable{

    public String softVersion;
    public String firmname;
    public String deviceName;
    public String iBeaconDate;
    public String iBeaconMac;
    public String chipModel;
    public String hardwareVersion;
    public String firmwareVersion;
    public String runtime;

    @Override
    public String toString() {
        return "BeaconDeviceInfo{" +
                "softVersion='" + softVersion + '\'' +
                ", firmname='" + firmname + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", iBeaconDate='" + iBeaconDate + '\'' +
                ", iBeaconMac='" + iBeaconMac + '\'' +
                ", chipModel='" + chipModel + '\'' +
                ", hardwareVersion='" + hardwareVersion + '\'' +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", runtime='" + runtime + '\'' +
                '}';
    }
}
