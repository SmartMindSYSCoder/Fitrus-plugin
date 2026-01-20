package com.smartmind.sm_fitrus.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    private final Context context;
    private final Activity activity;

    public PermissionHelper(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void updateActivity(Activity activity) {
        // Since Activity can change (e.g. config changes), we might need to update it?
        // But PermissionHelper instance is likely tied to Handler which is tied to Plugin...
        // Actually, let's just keep activity as a mutable field if needed, or better pass it to methods usage?
        // But the previous code had setActivity in Handler.
        // Let's assume we can re-create this helper or just use it.
    }
    
    // Helper needs mutable activity because FitrusHandler has setActivity
    // So let's make activity settable or just pass it in checkPermissions?
    // checkPermissions uses 'activity' to request permissions.
    
    public void checkPermissions(Activity currentActivity) {
        final int PERMISSIONS_REQUEST_CODE = 1;
        if (Build.VERSION.SDK_INT < 31) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (currentActivity != null) {
                    ActivityCompat.requestPermissions(currentActivity,
                            new String[]{
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            },
                            PERMISSIONS_REQUEST_CODE);
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                 if (currentActivity != null) {
                    ActivityCompat.requestPermissions(currentActivity,
                            new String[]{
                                    Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_CONNECT,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            },
                            PERMISSIONS_REQUEST_CODE);
                 }
            }
        }
    }

    public boolean isPermissionsGranted() {
        if (Build.VERSION.SDK_INT < 31) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public boolean isBluetoothEnabled() {
        android.bluetooth.BluetoothAdapter mBluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    public boolean isNetworkAvailable() {
        android.net.ConnectivityManager connectivityManager = (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}
