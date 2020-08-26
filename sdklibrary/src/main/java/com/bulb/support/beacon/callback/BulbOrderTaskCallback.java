package com.bulb.support.beacon.callback;

import com.bulb.support.beacon.entity.OrderType;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 返回数据回调类
 * @ClassPath com.bulb.support.callback.OrderCallback
 */
public interface BulbOrderTaskCallback {

    void onOrderResult(OrderType orderType, byte[] value, int responseType);

    void onOrderTimeout(OrderType orderType);

    void onOrderFinish();
}
