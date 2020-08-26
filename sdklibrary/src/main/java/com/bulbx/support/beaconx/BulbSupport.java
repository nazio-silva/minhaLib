package com.bulbx.support.beaconx;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import com.bulbx.support.beaconx.callback.BulbConnStateCallback;
import com.bulbx.support.beaconx.callback.BulbOrderTaskCallback;
import com.bulbx.support.beaconx.callback.BulbResponseCallback;
import com.bulbx.support.beaconx.callback.BulbScanDeviceCallback;
import com.bulbx.support.beaconx.entity.BulbCharacteristic;
import com.bulbx.support.beaconx.entity.OrderType;
import com.bulbx.support.beaconx.handler.BaseMessageHandler;
import com.bulbx.support.beaconx.handler.BulbCharacteristicHandler;
import com.bulbx.support.beaconx.handler.BulbConnStateHandler;
import com.bulbx.support.beaconx.handler.BulbLeScanHandler;
import com.bulbx.support.beaconx.log.LogModule;
import com.bulbx.support.beaconx.task.OrderTask;
import com.bulbx.support.beaconx.utils.BleConnectionCompat;
import com.bulbx.support.beaconx.utils.BulbUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.bulb.support.beacon.BulbSupport
 */
public class BulbSupport implements BulbResponseCallback {
    public static final int HANDLER_MESSAGE_WHAT_CONNECTED = 1;
    public static final int HANDLER_MESSAGE_WHAT_DISCONNECTED = 2;
    public static final int HANDLER_MESSAGE_WHAT_SERVICES_DISCOVERED = 3;
    public static final int HANDLER_MESSAGE_WHAT_DISCONNECT = 4;
    public static final UUID DESCRIPTOR_UUID_NOTIFY = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BulbLeScanHandler mBulbLeScanHandler;
    private HashMap<OrderType, BulbCharacteristic> mCharacteristicMap;
    private BlockingQueue<OrderTask> mQueue;
    private BulbScanDeviceCallback mBulbScanDeviceCallback;

    private static volatile BulbSupport INSTANCE;

    private Context mContext;

    private BulbSupport() {
        //no instance
        mQueue = new LinkedBlockingQueue<>();
    }

    public static BulbSupport getInstance() {
        if (INSTANCE == null) {
            synchronized (BulbSupport.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BulbSupport();
                }
            }
        }
        return INSTANCE;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void init(Context context) {
        LogModule.init(context);
        mContext = context;
        mHandler = new ServiceMessageHandler(this);
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    private ServiceMessageHandler mHandler;

    public class ServiceMessageHandler extends BaseMessageHandler<BulbSupport> {
        private BulbConnStateCallback bulbConnStateCallback;

        public ServiceMessageHandler(BulbSupport module) {
            super(module);
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        protected void handleMessage(BulbSupport module, Message msg) {
            switch (msg.what) {
                case HANDLER_MESSAGE_WHAT_CONNECTED:
                    mBluetoothGatt.discoverServices();
                    break;
                case HANDLER_MESSAGE_WHAT_DISCONNECTED:
                    disConnectBle();
                    bulbConnStateCallback.onDisConnected();
                    break;
                case HANDLER_MESSAGE_WHAT_SERVICES_DISCOVERED:
                    LogModule.i("连接成功！");
                    mCharacteristicMap = BulbCharacteristicHandler.getInstance().getCharacteristics(mBluetoothGatt);
                    bulbConnStateCallback.onConnectSuccess();
                    break;
                case HANDLER_MESSAGE_WHAT_DISCONNECT:
                    if (mQueue != null && !mQueue.isEmpty()) {
                        mQueue.clear();
                    }
                    if (mBluetoothGatt != null) {
                        if (refreshDeviceCache()) {
                            LogModule.i("清理GATT层蓝牙缓存");
                        }
                        LogModule.i("断开连接");
                        mBluetoothGatt.close();
                        mBluetoothGatt.disconnect();
                    }
                    break;
            }
        }

        public void setBulbConnStateCallback(BulbConnStateCallback bulbConnStateCallback) {
            this.bulbConnStateCallback = bulbConnStateCallback;
        }
    }

    public void setConnStateCallback(final BulbConnStateCallback bulbConnStateCallback) {
        mHandler.setBulbConnStateCallback(bulbConnStateCallback);
    }

    /**
     * @Date 2017/12/12 0012
     * @Author wenzheng.liu
     * @Description 蓝牙是否打开
     */
    public boolean isBluetoothOpen() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    /**
     * @Date 2018/1/16
     * @Author wenzheng.liu
     * @Description 打开蓝牙
     */
    public void openBluetooth() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.enable();
        }
    }

    /**
     * @Date 2018/1/16
     * @Author wenzheng.liu
     * @Description 关闭蓝牙
     */
    public void closeBluetooth() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.disable();
        }
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 是否连接设备
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean isConnDevice(Context context, String address) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        int connState = bluetoothManager.getConnectionState(mBluetoothAdapter.getRemoteDevice(address), BluetoothProfile.GATT);
        return connState == BluetoothProfile.STATE_CONNECTED;
    }

    public void startScanDevice(BulbScanDeviceCallback bulbScanDeviceCallback) {
        LogModule.i("Beacon");
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        List<ScanFilter> filters = Collections.singletonList(new ScanFilter.Builder().build());
        mBulbLeScanHandler = new BulbLeScanHandler(bulbScanDeviceCallback);
        scanner.startScan(filters, settings, mBulbLeScanHandler);
        mBulbScanDeviceCallback = bulbScanDeviceCallback;
        bulbScanDeviceCallback.onStartScan();
    }

    public void stopScanDevice() {
        if (mBulbLeScanHandler != null && mBulbScanDeviceCallback != null) {
            LogModule.i("结束扫描Beacon");
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(mBulbLeScanHandler);
            mBulbScanDeviceCallback.onStopScan();
            mBulbLeScanHandler = null;
            mBulbScanDeviceCallback = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void connDevice(final Context context, final String address, final BulbConnStateCallback bulbConnStateCallback) {
        if (TextUtils.isEmpty(address)) {
            LogModule.i("connDevice: 地址为空");
            return;
        }
        if (!isBluetoothOpen()) {
            LogModule.i("connDevice: 蓝牙未打开");
            return;
        }
        if (isConnDevice(context, address)) {
            LogModule.i("connDevice: 设备已连接");
            return;
        }
        final BulbConnStateHandler gattCallback = BulbConnStateHandler.getInstance();
        gattCallback.setBeaconResponseCallback(this);
        setConnStateCallback(bulbConnStateCallback);
        gattCallback.setMessageHandler(mHandler);
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    LogModule.i("开始尝试连接");
                    mBluetoothGatt = (new BleConnectionCompat(context)).connectGatt(device, false, gattCallback);
                }
            });
        } else {
            LogModule.i("获取蓝牙设备失败");
        }
    }

    /**
     * @Date 2017/12/13 0013
     * @Author wenzheng.liu
     * @Description 断开连接
     */
    public void disConnectBle() {
        mHandler.sendEmptyMessage(BulbSupport.HANDLER_MESSAGE_WHAT_DISCONNECT);
    }

    /**
     * @Date 2017/12/13 0013
     * @Author wenzheng.liu
     * @Description Clears the internal cache and forces a refresh of the services from the
     * remote device.
     */
    private boolean refreshDeviceCache() {
        if (mBluetoothGatt != null) {
            try {
                BluetoothGatt localBluetoothGatt = mBluetoothGatt;
                Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                if (localMethod != null) {
                    boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                    return bool;
                }
            } catch (Exception localException) {
                LogModule.i("An exception occured while refreshing device");
            }
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void sendOrder(OrderTask... orderTasks) {
        if (orderTasks.length == 0) {
            return;
        }
        if (!isSyncData()) {
            for (OrderTask ordertask : orderTasks) {
                if (ordertask == null) {
                    continue;
                }
                mQueue.offer(ordertask);
            }
            executeTask(null);
        } else {
            for (OrderTask ordertask : orderTasks) {
                if (ordertask == null) {
                    continue;
                }
                mQueue.offer(ordertask);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void executeTask(BulbOrderTaskCallback callback) {
        if (callback != null && !isSyncData()) {
            callback.onOrderFinish();
            return;
        }
        final OrderTask orderTask = mQueue.peek();
        if (mBluetoothGatt == null) {
            LogModule.i("executeTask : BluetoothGatt is null");
            return;
        }
        if (orderTask == null) {
            LogModule.i("executeTask : orderTask is null");
            return;
        }
        final BulbCharacteristic bulbCharacteristic = mCharacteristicMap.get(orderTask.orderType);
        if (bulbCharacteristic == null) {
            LogModule.i("executeTask : bulbCharacteristic is null");
            return;
        }
        if (orderTask.response.responseType == OrderTask.RESPONSE_TYPE_READ) {
            sendReadOrder(orderTask, bulbCharacteristic);
        }
        if (orderTask.response.responseType == OrderTask.RESPONSE_TYPE_WRITE) {
            sendWriteOrder(orderTask, bulbCharacteristic);
        }
        if (orderTask.response.responseType == OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE) {
            sendWriteNoResponseOrder(orderTask, bulbCharacteristic);
        }
        if (orderTask.response.responseType == OrderTask.RESPONSE_TYPE_NOTIFY) {
            sendNotifyOrder(orderTask, bulbCharacteristic);
        }
        orderTimeoutHandler(orderTask);
    }

    // 发送可监听命令
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void sendNotifyOrder(OrderTask orderTask, final BulbCharacteristic bulbCharacteristic) {
        LogModule.i("app set device notify : " + orderTask.orderType.getName());
        final BluetoothGattDescriptor descriptor = bulbCharacteristic.characteristic.getDescriptor(DESCRIPTOR_UUID_NOTIFY);
        if (descriptor == null) {
            return;
        }
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        });
    }

    // 发送可写命令
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void sendWriteOrder(OrderTask orderTask, final BulbCharacteristic bulbCharacteristic) {
        LogModule.i("app to device write : " + orderTask.orderType.getName());
        LogModule.i(BulbUtils.bytesToHexString(orderTask.assemble()));
        bulbCharacteristic.characteristic.setValue(orderTask.assemble());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeCharacteristic(bulbCharacteristic.characteristic);
            }
        });
    }

    // 发送可写无应答命令
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void sendWriteNoResponseOrder(OrderTask orderTask, final BulbCharacteristic bulbCharacteristic) {
        LogModule.i("app to device write no response : " + orderTask.orderType.getName());
        LogModule.i(BulbUtils.bytesToHexString(orderTask.assemble()));
        bulbCharacteristic.characteristic.setValue(orderTask.assemble());
        bulbCharacteristic.characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeCharacteristic(bulbCharacteristic.characteristic);
            }
        });
    }

    // 发送可读命令
    private void sendReadOrder(OrderTask orderTask, final BulbCharacteristic bulbCharacteristic) {
        LogModule.i("app to device read : " + orderTask.orderType.getName());
        mHandler.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run() {
                mBluetoothGatt.readCharacteristic(bulbCharacteristic.characteristic);
            }
        });
    }

    // 直接发送命令(升级专用)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void sendDirectOrder(OrderTask orderTask) {
        final BulbCharacteristic bulbCharacteristic = mCharacteristicMap.get(orderTask.orderType);
        if (bulbCharacteristic == null) {
            LogModule.i("executeTask : bulbCharacteristic is null");
            return;
        }
        LogModule.i("app to device write no response : " + orderTask.orderType.getName());
        LogModule.i(BulbUtils.bytesToHexString(orderTask.assemble()));
        bulbCharacteristic.characteristic.setValue(orderTask.assemble());
        bulbCharacteristic.characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeCharacteristic(bulbCharacteristic.characteristic);
            }
        });
    }

    private void orderTimeoutHandler(final OrderTask orderTask) {
        mHandler.postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run() {
                if (orderTask.orderStatus != OrderTask.ORDER_STATUS_SUCCESS) {
                    LogModule.i("应答超时");
                    mQueue.poll();
                    orderTask.bulbOrderTaskCallback.onOrderTimeout(orderTask.response);
                    executeTask(orderTask.bulbOrderTaskCallback);
                }
            }
        }, orderTask.delayTime);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (isSyncData()) {
            int key = value[1] & 0xff;
            if (key == 0x63 && value[4] != 0) {
                LogModule.i("状态发生改变");
                return;
            }
            // 非延时应答
            OrderTask orderTask = mQueue.peek();
            if (value != null && value.length > 0) {
                switch (orderTask.orderType) {
                    case writeConfig:
                    case lockState:
                        formatCommonOrder(orderTask, value);
                        break;
                }
            }
        } else {
            OrderType orderType = null;
            if (characteristic.getUuid().toString().equals(OrderType.notifyConfig.getUuid())) {
                // 写通知命令
                orderType = OrderType.notifyConfig;
            }
            // 延时应答
            if (orderType != null) {
                LogModule.i(orderType.getName());
                Intent intent = new Intent(BulbConstants.ACTION_RESPONSE_NOTIFY);
                intent.putExtra(BulbConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE, orderType);
                intent.putExtra(BulbConstants.EXTRA_KEY_RESPONSE_VALUE, value);
                mContext.sendOrderedBroadcast(intent, null);
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCharacteristicWrite(byte[] value) {
        if (!isSyncData()) {
            return;
        }
        OrderTask orderTask = mQueue.peek();
        if (value != null && value.length > 0) {
            switch (orderTask.orderType) {
                case advSlot:
                case advInterval:
                case radioTxPower:
                case advTxPower:
                case unLock:
                case advSlotData:
                case resetDevice:
                    formatCommonOrder(orderTask, value);
                    break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCharacteristicRead(byte[] value) {
        if (!isSyncData()) {
            return;
        }
        OrderTask orderTask = mQueue.peek();
        if (value != null && value.length > 0) {
            switch (orderTask.orderType) {
                case manufacturer:
                case deviceModel:
                case productDate:
                case hardwareVersion:
                case firmwareVersion:
                case softwareVersion:
                case battery:
                case advSlot:
                case advInterval:
                case radioTxPower:
                case advTxPower:
                case lockState:
                case unLock:
                case advSlotData:
                    formatCommonOrder(orderTask, value);
                    break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onDescriptorWrite() {
        if (!isSyncData()) {
            return;
        }
        OrderTask orderTask = mQueue.peek();
        LogModule.i("device to app notify : " + orderTask.orderType.getName());
        orderTask.orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        mQueue.poll();
        executeTask(orderTask.bulbOrderTaskCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void formatCommonOrder(OrderTask task, byte[] value) {
        task.orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        task.response.responseValue = value;
        mQueue.poll();
        task.bulbOrderTaskCallback.onOrderResult(task.response);
        executeTask(task.bulbOrderTaskCallback);
    }

    public synchronized boolean isSyncData() {
        return mQueue != null && !mQueue.isEmpty();
    }
}
