package com.bulb.support.beacon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Message;
import android.text.TextUtils;

import com.bulb.support.beacon.callback.BulbConnStateCallback;
import com.bulb.support.beacon.callback.BulbOrderTaskCallback;
import com.bulb.support.beacon.callback.BulbResponseCallback;
import com.bulb.support.beacon.callback.BulbScanDeviceCallback;
import com.bulb.support.beacon.entity.BulbCharacteristic;
import com.bulb.support.beacon.entity.OrderType;
import com.bulb.support.beacon.handler.BaseMessageHandler;
import com.bulb.support.beacon.handler.BulbCharacteristicHandler;
import com.bulb.support.beacon.handler.BulbConnStateHandler;
import com.bulb.support.beacon.handler.BulbLeScanHandler;
import com.bulb.support.beacon.log.LogModule;
import com.bulb.support.beacon.task.OrderTask;
import com.bulb.support.beacon.utils.BleConnectionCompat;
import com.bulb.support.beacon.utils.BulbUtils;

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

import static androidx.core.content.PermissionChecker.checkSelfPermission;

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
    private BulbConnStateCallback mBulbConnStateCallback;

    private static volatile BulbSupport INSTANCE;

    private Context mContext;

    public BulbSupport() {
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

    public void init(Context context) {

        System.out.println("BulbSupport init");
        System.out.println(context);

        LogModule.init(context);
        mContext = context;
        mHandler = new ServiceMessageHandler(this);
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        System.out.println("mHandler");
        System.out.println(mHandler);
        System.out.println("mBluetoothAdapter");
        System.out.println(mBluetoothAdapter);
    }

    private ServiceMessageHandler mHandler;

    public class ServiceMessageHandler extends BaseMessageHandler<BulbSupport> {
        private BulbConnStateCallback bulbConnStateCallback;

        public ServiceMessageHandler(BulbSupport module) {
            super(module);
        }

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

    /**
     * @Date 2017/12/12 0012
     * @Author wenzheng.liu
     * @Description 蓝牙是否打开
     */
    public boolean isBluetoothOpen() {

        System.out.println("isBluetoothOpen");
        System.out.println(mBluetoothAdapter.isEnabled());

        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 是否连接设备
     */
    public boolean isConnDevice(Context context, String address) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        int connState = bluetoothManager.getConnectionState(mBluetoothAdapter.getRemoteDevice(address), BluetoothProfile.GATT);
        return connState == BluetoothProfile.STATE_CONNECTED;
    }

    public void startScanDevice(BulbScanDeviceCallback bulbScanDeviceCallback) {

        try {

            System.out.println("startScanDevice...");
            System.out.println("startScanDevice PackageManager: " + PackageManager.PERMISSION_GRANTED);

            /*if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                LogModule.i("Comece a escanear os Bulbs");
            }*/

            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();

            System.out.println("BluetoothLeScannerCompat scanner");
            System.out.println(scanner);

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            List<ScanFilter> filters = Collections.singletonList(new ScanFilter.Builder().build());
            mBulbLeScanHandler = new BulbLeScanHandler(bulbScanDeviceCallback);
            scanner.startScan(filters, settings, mBulbLeScanHandler);
            mBulbScanDeviceCallback = bulbScanDeviceCallback;

            bulbScanDeviceCallback.onStartScan();

        } catch (Exception e) {
            System.out.println("Erro de exceção");
            System.out.println(e);
        }

    }

    @SuppressLint("WrongConstant")
    public void stopScanDevice() {
        if (mBulbLeScanHandler != null && mBulbScanDeviceCallback != null) {
            if (checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                LogModule.i("Bulb");
            }
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(mBulbLeScanHandler);
            mBulbScanDeviceCallback.onStopScan();
            mBulbLeScanHandler = null;
            mBulbScanDeviceCallback = null;
        }
    }

    public void connDevice(final Context context, final String address, final BulbConnStateCallback bulbConnStateCallback) {
        if (TextUtils.isEmpty(address)) {
            LogModule.i("connDevice: Endereço está vazio");
            return;
        }
        if (!isBluetoothOpen()) {
            LogModule.i("connDevice: Bluetooth não está ligado");
            return;
        }
        if (isConnDevice(context, address)) {
            LogModule.i("connDevice: Dispositivo conectado");
            return;
        }
        final BulbConnStateHandler gattCallback = BulbConnStateHandler.getInstance();
        gattCallback.setBeaconResponseCallback(this);
        mBulbConnStateCallback = bulbConnStateCallback;
        mHandler.setBulbConnStateCallback(bulbConnStateCallback);
        gattCallback.setMessageHandler(mHandler);
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    LogModule.i("Comece a tentar conectar");
                    mBluetoothGatt = (new BleConnectionCompat(context)).connectGatt(device, false, gattCallback);
                }
            });
        } else {
            LogModule.i("\n" +
                    "Falha ao obter dispositivo Bluetooth");
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
    public void sendOrder(OrderTask... orderTasks) {
        if (orderTasks.length == 0) {
            return;
        }
        if (mQueue.isEmpty()) {
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

    private void executeTask(BulbOrderTaskCallback callback) {
        if (callback != null && mQueue.isEmpty()) {
            callback.onOrderFinish();
            return;
        }
        final OrderTask orderTask = mQueue.peek();
        if (mBluetoothGatt == null) {
            mQueue.clear();
            LogModule.i("executeTask : BluetoothGatt is null");
            return;
        }
        if (orderTask == null) {
            LogModule.i("executeTask : orderTask is null");
            return;
        }
        final BulbCharacteristic bulbCharacteristic = mCharacteristicMap.get(orderTask.orderType);
        if (bulbCharacteristic == null) {
            mQueue.clear();
            disConnectBle();
            mBulbConnStateCallback.onDisConnected();
            LogModule.i("executeTask : bulbCharacteristic is null");
            return;
        }
        if (orderTask.responseType == OrderTask.RESPONSE_TYPE_READ) {
            sendReadOrder(orderTask, bulbCharacteristic);
        }
        if (orderTask.responseType == OrderTask.RESPONSE_TYPE_WRITE) {
            sendWriteOrder(orderTask, bulbCharacteristic);
        }
        if (orderTask.responseType == OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE) {
            sendWriteNoResponseOrder(orderTask, bulbCharacteristic);
        }
        if (orderTask.responseType == OrderTask.RESPONSE_TYPE_NOTIFY) {
            sendNotifyOrder(orderTask, bulbCharacteristic);
        }
        orderTimeoutHandler(orderTask);
    }

    // 发送可监听命令
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
            @Override
            public void run() {
                mBluetoothGatt.readCharacteristic(bulbCharacteristic.characteristic);
            }
        });
    }

    // 直接发送命令(升级专用)
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
        long delayTime = 3000;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (orderTask.orderStatus != OrderTask.ORDER_STATUS_SUCCESS) {
                    LogModule.i("应答超时");
                    mQueue.poll();
                    orderTask.bulbOrderTaskCallback.onOrderTimeout(orderTask.orderType);
                    executeTask(orderTask.bulbOrderTaskCallback);
                }
            }
        }, delayTime);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (!mQueue.isEmpty()) {
            // 非延时应答
            OrderTask orderTask = mQueue.peek();
            if (value != null && value.length > 0) {
                switch (orderTask.orderType) {
                    case writeAndNotify:
                        formatCommonOrder(orderTask, value);
                        break;
                    case changePassword:
                        formatCommonOrder(orderTask, value);
                        break;
                }
            }
        } else {
            OrderType orderType = null;
            // 延时应答
            if (characteristic.getUuid().toString().equals(OrderType.writeAndNotify.getUuid())) {
                // 写通知命令
                orderType = OrderType.writeAndNotify;
            }

            if (orderType != null) {
                LogModule.i(orderType.getName());
                Intent intent = new Intent(BulbConstants.ACTION_RESPONSE_NOTIFY);
                intent.putExtra(BulbConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE, orderType);
                intent.putExtra(BulbConstants.EXTRA_KEY_RESPONSE_VALUE, value);
                mContext.sendOrderedBroadcast(intent, null);
            }
        }

    }

    @Override
    public void onCharacteristicWrite(byte[] value) {
        if (mQueue.isEmpty()) {
            return;
        }
        OrderTask orderTask = mQueue.peek();
        if (value != null && value.length > 0) {
            switch (orderTask.orderType) {
                case overtime:
                case iBeaconUuid:
                case major:
                case minor:
                case measurePower:
                case transmission:
                case broadcastingInterval:
                case serialID:
                case iBeaconName:
                case connectionMode:
                    formatCommonOrder(orderTask, value);
                    break;
            }
        }
    }

    @Override
    public void onCharacteristicRead(byte[] value) {
        if (mQueue.isEmpty()) {
            return;
        }
        OrderTask orderTask = mQueue.peek();
        if (value != null && value.length > 0) {
            switch (orderTask.orderType) {
                case battery:
                case firmname:
                case softVersion:
                case devicename:
                case iBeaconDate:
                case hardwareVersion:
                case firmwareVersion:
                case iBeaconUuid:
                case major:
                case minor:
                case measurePower:
                case transmission:
                case broadcastingInterval:
                case serialID:
                case iBeaconName:
                case connectionMode:
                case softReboot:
                case iBeaconMac:
                    formatCommonOrder(orderTask, value);
                    break;
            }
        }
    }

    @Override
    public void onDescriptorWrite() {
        if (mQueue.isEmpty()) {
            return;
        }
        OrderTask orderTask = mQueue.peek();
        LogModule.i("device to app notify : " + orderTask.orderType.getName());
        orderTask.orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        mQueue.poll();
        executeTask(orderTask.bulbOrderTaskCallback);
    }

    private void formatCommonOrder(OrderTask task, byte[] value) {
        task.orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        mQueue.poll();
        task.bulbOrderTaskCallback.onOrderResult(task.orderType, value, task.responseType);
        executeTask(task.bulbOrderTaskCallback);
    }

    public boolean isSyncData() {
        return mQueue != null && !mQueue.isEmpty();
    }
}
