package com.bulbxpro.support.beaconxpro.callback;

import com.bulbxpro.support.beaconxpro.task.OrderTaskResponse;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 返回数据回调类
 * @ClassPath com.bulb.support.callback.OrderCallback
 */
public interface BulbOrderTaskCallback {

    void onOrderResult(OrderTaskResponse response);

    void onOrderTimeout(OrderTaskResponse response);

    void onOrderFinish();
}
