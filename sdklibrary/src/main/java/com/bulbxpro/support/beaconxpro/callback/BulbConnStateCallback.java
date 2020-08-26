package com.bulbxpro.support.beaconxpro.callback;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description
 */

public interface BulbConnStateCallback {

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 连接成功
     */
    void onConnectSuccess();

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 连接断开
     */
    void onDisConnected();
}
