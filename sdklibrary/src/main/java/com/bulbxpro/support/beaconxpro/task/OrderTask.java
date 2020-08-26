package com.bulbxpro.support.beaconxpro.task;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.bulbxpro.support.beaconxpro.BulbSupport;
import com.bulbxpro.support.beaconxpro.callback.BulbOrderTaskCallback;
import com.bulbxpro.support.beaconxpro.entity.OrderEnum;
import com.bulbxpro.support.beaconxpro.entity.OrderType;
import com.bulbxpro.support.beaconxpro.log.LogModule;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description 命令任务
 */
public abstract class OrderTask {
    public static final long DEFAULT_DELAY_TIME = 3000;
    public static final int RESPONSE_TYPE_READ = 0;
    public static final int RESPONSE_TYPE_WRITE = 1;
    public static final int RESPONSE_TYPE_NOTIFY = 2;
    public static final int RESPONSE_TYPE_DISABLE_NOTIFY = 4;
    public static final int RESPONSE_TYPE_WRITE_NO_RESPONSE = 3;
    public static final int ORDER_STATUS_SUCCESS = 1;
    public OrderType orderType;
    public OrderEnum order;
    public BulbOrderTaskCallback callback;
    public OrderTaskResponse response;
    public long delayTime = DEFAULT_DELAY_TIME;
    public int orderStatus;

    public OrderTaskResponse getResponse() {
        return response;
    }

    public void setResponse(OrderTaskResponse response) {
        this.response = response;
    }

    public OrderTask(OrderType orderType, OrderEnum order, BulbOrderTaskCallback callback, int responseType) {
        response = new OrderTaskResponse();
        this.orderType = orderType;
        this.order = order;
        this.callback = callback;
        this.response.order = order;
        this.response.orderType = orderType;
        this.response.responseType = responseType;
    }

    public abstract byte[] assemble();

    public BulbOrderTaskCallback getCallback() {
        return callback;
    }

    public void setCallback(BulbOrderTaskCallback callback) {
        this.callback = callback;
    }


    public OrderEnum getOrder() {
        return order;
    }

    public void setOrder(OrderEnum order) {
        this.order = order;
    }


    public void parseValue(byte[] value) {
    }

    public Runnable timeoutRunner = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            if (orderStatus != OrderTask.ORDER_STATUS_SUCCESS) {
                if (timeoutPreTask()) {
                    BulbSupport.getInstance().pollTask();
                    callback.onOrderTimeout(response);
                    BulbSupport.getInstance().executeTask(callback);
                }
            }
        }
    };

    public boolean timeoutPreTask() {
        LogModule.i(order.getOrderName() + "超时");
        return true;
    }
}
