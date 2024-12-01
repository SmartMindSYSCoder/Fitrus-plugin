package com.smartmind.sm_fitrus.Models;

import android.bluetooth.BluetoothGatt;

public class DeviceInfo {
    public BluetoothGatt gatt;
    public boolean notifyDisconnect = true;
    public int mConnectionState = 0;

    public DeviceInfo() {
    }
}
