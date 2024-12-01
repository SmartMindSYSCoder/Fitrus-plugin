package com.smartmind.sm_fitrus.Interfaces;


import android.content.IntentFilter;

public interface DeviceServiceInterface {
    int SCAN_STATE_WAIT = 0;
    int SCAN_STATE_SCAN = 1;
    int CONN_STATE_DISCONNECTED = 0;
    int CONN_STATE_CONNECTING = 1;
    int CONN_STATE_CONNECTED = 2;
    String ACTION_SCAN_SEARCHED = "com.osd.fitruslib.ACTION_SCAN_SEARCHED";
    String ACTION_SCAN_FAILED = "com.osd.fitruslib.ACTION_SCAN_FAILED";
    String ACTION_SCAN_COMPLETED = "com.osd.fitruslib.ACTION_SCAN_COMPLETED";
    String ACTION_GATT_CONNECTED = "com.osd.fitruslib.ACTION_GATT_CONNECTED";
    String ACTION_GATT_DISCONNECTED = "com.osd.fitruslib.ACTION_GATT_DISCONNECTED";
    String ACTION_GATT_SERVICES_DISCOVERED = "com.osd.fitruslib.ACTION_GATT_SERVICES_DISCOVERED";
    String ACTION_DATA_AVAILABLE = "com.osd.fitruslib.ACTION_DATA_AVAILABLE";
    String EXTRA_DATA = "com.osd.fitruslib.EXTRA_DATA";
    String EXTRA_TYPE = "com.osd.fitruslib.EXTRA_TYPE";
    String EXTRA_NAME = "com.osd.fitruslib.EXTRA_NAME";

    IntentFilter getGattUpdateIntentFilter();

    boolean startScan(int var1, int var2, String... var3);

    boolean stopScan();

    boolean connect(String var1, String var2);

    void disconnect(String var1);

    void close(String var1);

    int getScanState();

    String getDeviceAddress(String var1);
}

