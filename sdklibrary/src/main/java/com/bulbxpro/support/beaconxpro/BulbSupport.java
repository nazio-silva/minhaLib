package com.bulbxpro.support.beaconxpro;

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

import com.bulbxpro.support.beaconxpro.callback.BulbConnStateCallback;
import com.bulbxpro.support.beaconxpro.callback.BulbOrderTaskCallback;
import com.bulbxpro.support.beaconxpro.callback.BulbResponseCallback;
import com.bulbxpro.support.beaconxpro.callback.BulbScanDeviceCallback;
import com.bulbxpro.support.beaconxpro.entity.BulbCharacteristic;
import com.bulbxpro.support.beaconxpro.entity.OrderType;
import com.bulbxpro.support.beaconxpro.handler.BaseMessageHandler;
import com.bulbxpro.support.beaconxpro.handler.BulbCharacteristicHandler;
import com.bulbxpro.support.beaconxpro.handler.BulbConnStateHandler;
import com.bulbxpro.support.beaconxpro.handler.BulbLeScanHandler;
import com.bulbxpro.support.beaconxpro.log.LogModule;
import com.bulbxpro.support.beaconxpro.task.OrderTask;
import com.bulbxpro.support.beaconxpro.utils.BleConnectionCompat;
import com.bulbxpro.support.beaconxpro.utils.BulbUtils;

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
 * @ClassPath com.bulb.support.BulbSupport
 */
public class BulbSupport implements BulbResponseCallback {
    public static final int HANDLER_MESSAGE_WHAT_CONNECTED = 1;
    public static final int HANDLER_MESSAGE_WHAT_DISCONNECTED = 2;
    public static final int HANDLER_MESSAGE_WHAT_SERVICES_DISCOVERED = 3;
    public static final int HANDLER_MESSAGE_WHAT_DISCONNECT = 4;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BlockingQueue<OrderTask> mQueue;

    private Context mContext;
    private BulbLeScanHandler mBulbLeScanHandler;
    private BulbScanDeviceCallback mBulbScanDeviceCallback;
    private BulbConnStateCallback mBulbConnStateCallback;
    private HashMap<OrderType, BulbCharacteristic> mCharacteristicMap;
    private static final UUID DESCRIPTOR_UUID_NOTIFY = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
//    private static final UUID EDDYSTONE_UUID = UUID.fromString("0000feaa-0000-1000-8000-00805f9b34fb");
//    private static final UUID SERVICE_UUID = UUID.fromString("0000feab-0000-1000-8000-00805f9b34fb");

    private static volatile BulbSupport INSTANCE;

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
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void startScanDevice(BulbScanDeviceCallback bulbScanDeviceCallback) {
        LogModule.w("开始扫描");
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                // Hardware filtering has some issues on selected devices
                .setUseHardwareFilteringIfSupported(false)
                .build();
        List<ScanFilter> scanFilterList = Collections.singletonList(new ScanFilter.Builder().build());
//        List<ScanFilter> scanFilterList = new ArrayList<>();
//        ScanFilter.Builder eddystoneBuilder = new ScanFilter.Builder();
//        eddystoneBuilder.setServiceUuid(new ParcelUuid(EDDYSTONE_UUID));
//        scanFilterList.add(eddystoneBuilder.build());
//        ScanFilter.Builder serviceBuilder = new ScanFilter.Builder();
//        serviceBuilder.setServiceUuid(new ParcelUuid(SERVICE_UUID));
//        scanFilterList.add(serviceBuilder.build());
        mBulbLeScanHandler = new BulbLeScanHandler(bulbScanDeviceCallback);
        scanner.startScan(scanFilterList, settings, mBulbLeScanHandler);
        mBulbScanDeviceCallback = bulbScanDeviceCallback;
        bulbScanDeviceCallback.onStartScan();
    }

    public void stopScanDevice() {
        if (isBluetoothOpen() && mBulbLeScanHandler != null && mBulbScanDeviceCallback != null) {
            LogModule.w("结束扫描");
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(mBulbLeScanHandler);
            mBulbScanDeviceCallback.onStopScan();
            mBulbLeScanHandler = null;
            mBulbScanDeviceCallback = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void connDevice(final Context context, final String address, final BulbConnStateCallback bulbConnStateCallback) {
        setConnStateCallback(bulbConnStateCallback);
        if (TextUtils.isEmpty(address)) {
            LogModule.w("connDevice: 地址为空");
            return;
        }
        if (!isBluetoothOpen()) {
            LogModule.w("connDevice: 蓝牙未打开");
            return;
        }
        if (isConnDevice(context, address)) {
            LogModule.w("connDevice: 设备已连接");
            disConnectBle();
            return;
        }
        final BulbConnStateHandler gattCallback = BulbConnStateHandler.getInstance();
        gattCallback.setBulbResponseCallback(this);
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
            LogModule.w("获取蓝牙设备失败");
        }
    }

    public void setConnStateCallback(final BulbConnStateCallback bulbConnStateCallback) {
        mHandler.setBulbConnStateCallback(bulbConnStateCallback);
        mBulbConnStateCallback = bulbConnStateCallback;
    }

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

    /**
     * @param callback
     * @Date 2017/5/11
     * @Author wenzheng.liu
     * @Description 执行命令
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void executeTask(BulbOrderTaskCallback callback) {
        if (callback != null && !isSyncData()) {
            callback.onOrderFinish();
            return;
        }
        if (mQueue.isEmpty()) {
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
        if (mCharacteristicMap == null || mCharacteristicMap.isEmpty()) {
            LogModule.i("executeTask : characteristicMap is null");
            disConnectBle();
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
        if (orderTask.response.responseType == OrderTask.RESPONSE_TYPE_DISABLE_NOTIFY) {
            sendDisableNotifyOrder(orderTask, bulbCharacteristic);
        }
        timeoutHandler(orderTask);
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

    public synchronized boolean isSyncData() {
        return mQueue != null && !mQueue.isEmpty();
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
     * @Date 2017/12/13 0013
     * @Author wenzheng.liu
     * @Description 断开连接
     */
    public void disConnectBle() {
        mHandler.sendEmptyMessage(BulbSupport.HANDLER_MESSAGE_WHAT_DISCONNECT);
    }

    public void enableBluetooth() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.enable();
        }
    }

    public void disableBluetooth() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.disable();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (!isSyncData()) {
            OrderType orderType = null;
            if (characteristic.getUuid().toString().equals(OrderType.notifyConfig.getUuid())) {
                // 写通知命令
                orderType = OrderType.notifyConfig;
            }
            if (characteristic.getUuid().toString().equals(OrderType.axisData.getUuid())) {
                // 3轴通知命令
                orderType = OrderType.axisData;
            }
            if (characteristic.getUuid().toString().equals(OrderType.htData.getUuid())) {
                // 温湿度通知命令
                orderType = OrderType.htData;
            }
            if (characteristic.getUuid().toString().equals(OrderType.htSavedData.getUuid())) {
                // 温湿度通知命令
                orderType = OrderType.htSavedData;
            }
            // 延时应答
            if (orderType != null) {
                LogModule.i(orderType.getName());
                Intent intent = new Intent(BulbConstants.ACTION_CURRENT_DATA);
                intent.putExtra(BulbConstants.EXTRA_KEY_CURRENT_DATA_TYPE, orderType);
                intent.putExtra(BulbConstants.EXTRA_KEY_RESPONSE_VALUE, value);
                mContext.sendBroadcast(intent);
            }
        } else {
            int key = value[1] & 0xff;
            if (key == 0x63 && value[4] != 0) {
                LogModule.i("状态发生改变");
                return;
            }
            // 非延时应答
            OrderTask orderTask = mQueue.peek();
            if (value != null && value.length > 0 && orderTask != null) {
                switch (orderTask.orderType) {
                    case writeConfig:
                    case lockState:
                        formatCommonOrder(orderTask, value);
                        break;
                }
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
                case connectable:
                case lockState:
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
                case slotType:
                case deviceType:
                case connectable:
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
        executeTask(orderTask.callback);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void formatCommonOrder(OrderTask task, byte[] value) {
        task.orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        task.response.responseValue = value;
        mQueue.poll();
        task.callback.onOrderResult(task.response);
        executeTask(task.callback);
    }

    public void onOpenNotifyTimeout() {
        if (!mQueue.isEmpty()) {
            mQueue.clear();
        }
        disConnectBle();
    }


    public void pollTask() {
        if (mQueue != null && !mQueue.isEmpty()) {
            OrderTask orderTask = mQueue.peek();
            LogModule.i("移除" + orderTask.order.getOrderName());
            mQueue.poll();
        }
    }

    public void timeoutHandler(OrderTask orderTask) {
        mHandler.postDelayed(orderTask.timeoutRunner, orderTask.delayTime);
    }


    ///////////////////////////////////////////////////////////////////////////
    // handler
    ///////////////////////////////////////////////////////////////////////////

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
                    synchronized (INSTANCE) {
                        LogModule.e("discoverServices!!!");
                        mBluetoothGatt.discoverServices();
                    }
                    break;
                case HANDLER_MESSAGE_WHAT_DISCONNECTED:
                    disConnectBle();
                    break;
                case HANDLER_MESSAGE_WHAT_SERVICES_DISCOVERED:
                    LogModule.i("连接成功！");
                    try {
                        synchronized (BulbSupport.class) {
                            mCharacteristicMap = BulbCharacteristicHandler.getInstance().getCharacteristics(mBluetoothGatt);
                        }
                        if (mCharacteristicMap == null || mCharacteristicMap.isEmpty()) {
                            LogModule.e("打开服务：特征为空！！！");
                            disConnectBle();
                            return;
                        }
                    } catch (Exception e) {
                        LogModule.e("打开服务：发生异常！！！");
                        LogModule.e(e.toString());
                        disConnectBle();
                        return;
                    }
                    mBulbConnStateCallback.onConnectSuccess();
                    break;
                case HANDLER_MESSAGE_WHAT_DISCONNECT:
                    if (mQueue != null && !mQueue.isEmpty()) {
                        mQueue.clear();
                    }
                    if (mBluetoothGatt != null) {
                        if (refreshDeviceCache()) {
                            LogModule.i("清理GATT层蓝牙缓存");
                        }
                        LogModule.e("断开连接");
                        mBluetoothGatt.close();
                        mBluetoothGatt.disconnect();
                        mBulbConnStateCallback.onDisConnected();
                    }
                    break;
            }
        }

        public void setBulbConnStateCallback(BulbConnStateCallback bulbConnStateCallback) {
            this.bulbConnStateCallback = bulbConnStateCallback;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    // 发送可监听命令
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void sendNotifyOrder(OrderTask orderTask, final BulbCharacteristic bulbCharacteristic) {
        LogModule.i("app set device notify : " + orderTask.orderType.getName());
        bulbCharacteristic.characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        final BluetoothGattDescriptor descriptor = bulbCharacteristic.characteristic.getDescriptor(DESCRIPTOR_UUID_NOTIFY);
        if (descriptor == null) {
            return;
        }
        if ((bulbCharacteristic.characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else if ((bulbCharacteristic.characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        });
    }

    // 发送可监听命令
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void sendDisableNotifyOrder(OrderTask orderTask, final BulbCharacteristic bulbCharacteristic) {
        LogModule.i("app set device notify : " + orderTask.orderType.getName());
        bulbCharacteristic.characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        final BluetoothGattDescriptor descriptor = bulbCharacteristic.characteristic.getDescriptor(DESCRIPTOR_UUID_NOTIFY);
        if (descriptor == null) {
            return;
        }
        if ((bulbCharacteristic.characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        } else if ((bulbCharacteristic.characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
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

    // 发送自定义命令（无队列）
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void sendCustomOrder(OrderTask orderTask) {
        final BulbCharacteristic bulbCharacteristic = mCharacteristicMap.get(orderTask.orderType);
        if (bulbCharacteristic == null) {
            LogModule.i("executeTask : bulbCharacteristic is null");
            return;
        }
        if (orderTask.response.responseType == OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE) {
            sendWriteNoResponseOrder(orderTask, bulbCharacteristic);
        }
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
}
