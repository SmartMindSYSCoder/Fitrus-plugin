package com.smartmind.sm_fitrus;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import io.flutter.plugin.common.MethodChannel;

import com.smartmind.sm_fitrus.Interfaces.DeviceServiceBinder;
import com.smartmind.sm_fitrus.Interfaces.FitrusServiceInterface;
import com.smartmind.sm_fitrus.Results.FitrusLtResultData;

import org.json.JSONException;
import org.json.JSONObject;

import io.flutter.plugin.common.EventChannel;

public class FitrusHandler implements EventChannel.StreamHandler {

    private static final String TAG = "FitrusHandler";
    private Activity activity;
    private final Context applicationContext;
    private static EventChannel.EventSink events;

    private String connectState = "Disconnected";
    private FitrusServiceInterface mFitLtServiceInterface = null;
    private com.smartmind.sm_fitrus.Utils.PermissionHelper permissionHelper;

    FitrusHandler(Activity activity, Context applicationContext) {
        this.activity = activity;
        this.applicationContext = applicationContext;
        this.permissionHelper = new com.smartmind.sm_fitrus.Utils.PermissionHelper(applicationContext, activity);
        Log.e(TAG, "FitrusHandler Instance Created: " + System.identityHashCode(this));
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onListen(Object args, EventChannel.EventSink newEvents) {
        Log.e(TAG, "[" + System.identityHashCode(this) + "] onListen called. New events: " + newEvents + " (Previous: " + events + ")");
        events = newEvents;
        sendConnectionUpdate(connectState);
    }

    @Override
    public void onCancel(Object args) {
        Log.e(TAG, "[" + System.identityHashCode(this) + "] onCancel called. Releasing events: " + events);
        events = null;
    }

    private String pendingApiUrl = null;
    private String pendingApiKey = null;
    
    // Store last used inputs for manual calculations
    private double lastHeight = 0;
    private double lastWeight = 0;

    private MethodChannel.Result pendingInitResult;
    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable initTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (pendingInitResult != null) {
                Log.d(TAG, "Init timeout reached (2s). Returning result=false (or partial success if scanning).");
                try {
                    // We return true here to allow the app to proceed even if not fully ready yet,
                    // but the user asked for a wait. If we return false, init fails on Dart side.
                    // Usually returning true is safer to avoid blocking app flow, 
                    // but let's stick to true (meaning 'init called successfully, proceeded').
                    // OR return false to indicate 'not ready yet'? 
                    // User said: "check until the service become service discoverd the return true or false"
                    // So returning false on timeout is appropriate.
                    pendingInitResult.success(false);
                } catch (Exception e) {
                    Log.e(TAG, "Error sending timeout result", e);
                }
                pendingInitResult = null;
            }
        }
    };

    private static final String DEFAULT_API_URL = "https://api.thefitrus.com/fitrus-ml/measure/bodyfat";

    public void init(String apiUrl, String apiKey, MethodChannel.Result result) {
        Log.e(TAG, "[" + System.identityHashCode(this) + "] init() called. State: " + connectState + ", apiUrl: " + apiUrl);

        // cleanup previous pending result if any
        if (this.pendingInitResult != null) {
             try { this.pendingInitResult.success(false); } catch(Exception e) {}
             this.pendingInitResult = null;
             mainHandler.removeCallbacks(initTimeoutRunnable);
        }

        this.pendingInitResult = result;

        // Store for later when service connects
        if (apiUrl == null || apiUrl.isEmpty()) {
            this.pendingApiUrl = DEFAULT_API_URL;
             Log.d(TAG, "Using Default API URL: " + DEFAULT_API_URL);
        } else {
            this.pendingApiUrl = apiUrl;
        }
        this.pendingApiKey = apiKey;
        
        // If already ready, return immediately
        if ("Service Discovered".equals(connectState) || "Connected".equals(connectState)) {
             Log.d(TAG, "Already connected/ready. Updating API config and returning true.");
             if (mFitLtServiceInterface != null) {
                 mFitLtServiceInterface.setApiConfig(pendingApiUrl, pendingApiKey);
             }
             if (pendingInitResult != null) {
                 pendingInitResult.success(true);
                 pendingInitResult = null;
             }
             return;
        }

        // Start timeout (2 seconds)
        mainHandler.postDelayed(initTimeoutRunnable, 2000);

        // Dispose existing if needed (logic from before)
        if (mFitLtServiceInterface != null) {
            // ... (keep existing dispose logic if intended, but be careful not to break aggressive re-init)
            // Existing logic disposed connection. We keep it.
             Log.d(TAG, "Disposing existing connection before init");
            try {
                mFitLtServiceInterface.stopFitrusScan();
                mFitLtServiceInterface.closeFitrus();
            } catch (Exception e) {
                Log.w(TAG, "Error during pre-init dispose: " + e.getMessage());
            }
            try {
                applicationContext.unbindService(mServiceConnection);
            } catch (Exception e) { /* Ignore */ }
            try {
                LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(mGattUpdateReceiver);
            } catch (Exception e) { /* Ignore */ }
            mFitLtServiceInterface = null;
            connectState = "Disconnected";
        }

        if (!isBluetoothEnabled()) {
            Toast.makeText(applicationContext, "Bluetooth is not enabled. Please enable Bluetooth.", Toast.LENGTH_SHORT).show();
            // result.success(false) handled by timeout or immediate return? 
            // Better to return false immediately if blocking issues exist.
            mainHandler.removeCallbacks(initTimeoutRunnable);
            if (pendingInitResult != null) {
                pendingInitResult.success(false);
                pendingInitResult = null;
            }
            return;
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(applicationContext, "Internet connection is required.", Toast.LENGTH_SHORT).show();
            mainHandler.removeCallbacks(initTimeoutRunnable);
            if (pendingInitResult != null) {
                pendingInitResult.success(false);
                pendingInitResult = null;
            }
            return; 
        }

        if (isPermissionsGranted()) {
            Log.d(TAG, "Initializing Fitrus Service (fresh start)");
            Intent serviceIntent = new Intent(applicationContext, DeviceService.class);
            applicationContext.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            Toast.makeText(applicationContext, "Permission not granted\nPlease check permission first", Toast.LENGTH_SHORT).show();
            checkPermissions();
            // We let timeout handle the false return since permissions dialog is async
        }
    }

    private void sendConnectionUpdate(String state) {
        Log.e(TAG, "[" + System.identityHashCode(this) + "] sendConnectionUpdate: state=" + state + ", events=" + (events != null ? "VALID" : "NULL"));
        if (events == null) return;
        try {
            JSONObject inputObject = new JSONObject();
            inputObject.put("connectState", state);
            String json = inputObject.toString();
            Log.e(TAG, "Forwarding to Flutter: " + json);
            events.success(json);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating connection update JSON", e);
        }
    }

    private void sendDataUpdate(JSONObject data) {
        Log.e(TAG, "[" + System.identityHashCode(this) + "] sendDataUpdate: data=" + data.toString() + ", events=" + (events != null ? "VALID" : "NULL"));
        if (events == null) {
             Log.e(TAG, "sendDataUpdate: EventSink is NULL! Cannot send data.");
             return;
        }
        Log.e(TAG, "Forwarding to Flutter: " + data.toString());
        events.success(data.toString());
    }

    public void checkPermissions() {
        if (permissionHelper != null) {
            permissionHelper.checkPermissions(activity);
        }
    }

    public boolean isPermissionsGranted() {
        return permissionHelper != null && permissionHelper.isPermissionsGranted();
    }

    private boolean isBluetoothEnabled() {
        return permissionHelper != null && permissionHelper.isBluetoothEnabled();
    }

    private boolean isNetworkAvailable() {
        return permissionHelper != null && permissionHelper.isNetworkAvailable();
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "[" + System.identityHashCode(this) + "] Service Connected");
            mFitLtServiceInterface = (FitrusServiceInterface) ((DeviceServiceBinder) iBinder).getService();
            if (mFitLtServiceInterface != null) {
                // Pass API configuration to service
                if (pendingApiUrl != null || pendingApiKey != null) {
                    mFitLtServiceInterface.setApiConfig(pendingApiUrl, pendingApiKey);
                }
                LocalBroadcastManager.getInstance(applicationContext)
                     .registerReceiver(mGattUpdateReceiver, mFitLtServiceInterface.getGattUpdateIntentFilter());
                mFitLtServiceInterface.startFitrusScan(ScanSettings.SCAN_MODE_LOW_LATENCY, 10000);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "[" + System.identityHashCode(this) + "] Service Disconnected");
            mFitLtServiceInterface = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.e(TAG, "[" + System.identityHashCode(this) + "] onReceive action: " + action);
            if (action == null) return;

            switch (action) {
                case FitrusConstants.ACTION_SCAN_SEARCHED:
                    connectState = "Searched";
                    sendConnectionUpdate(connectState);
                    break;
                case FitrusConstants.ACTION_SCAN_FAILED:
                    connectState = "Scan Failed";
                    sendConnectionUpdate(connectState);
                    break;
                case FitrusConstants.ACTION_GATT_CONNECTED:
                    connectState = "Connected";
                    sendConnectionUpdate(connectState);
                    break;
                case FitrusConstants.ACTION_GATT_DISCONNECTED:
                    connectState = "Disconnected";
                    sendConnectionUpdate(connectState);
                    break;
                case FitrusConstants.ACTION_GATT_SERVICES_DISCOVERED:
                    connectState = "Service Discovered";
                    sendConnectionUpdate(connectState);
                    
                    if (pendingInitResult != null) {
                        Log.d(TAG, "Services discovered. Resolving pending init result to true.");
                        mainHandler.removeCallbacks(initTimeoutRunnable);
                        try {
                            pendingInitResult.success(true);
                        } catch (Exception e) {
                            Log.e(TAG, "Error resolving init result", e);
                        }
                        pendingInitResult = null;
                    }
                    break;
                case FitrusConstants.ACTION_DATA_AVAILABLE:
                    handleData(intent);
                    break;
            }
        }
    };

    private void handleData(final Intent intent) {
        String type = intent.getStringExtra(FitrusConstants.EXTRA_TYPE);
        if (type == null) return;

        try {
            JSONObject inputObject = new JSONObject();
            inputObject.put("connectState", connectState);

            switch (type) {
                case FitrusConstants.EXTRA_TYPE_MEASURE_PROGRESS:
                    FitrusLtResultData.Progress progress = (FitrusLtResultData.Progress) intent.getSerializableExtra(FitrusConstants.EXTRA_DATA);
                    if (progress != null) {
                        inputObject.put("hasProgress", true);
                        inputObject.put("progress", progress.progressValue);
                        sendDataUpdate(inputObject);
                    }
                    break;

                case FitrusConstants.EXTRA_TYPE_BFP:
                    FitrusLtResultData.Body bfpResult = (FitrusLtResultData.Body) intent.getSerializableExtra(FitrusConstants.EXTRA_DATA);
                    if (bfpResult != null) {
                        inputObject.put("hasData", true);
                        inputObject.put("bmi", bfpResult.bmi);
                        inputObject.put("bmr", bfpResult.bmr);
                        inputObject.put("waterPercentage", bfpResult.waterPercentage);
                        inputObject.put("fatMass", bfpResult.fatMass);
                        inputObject.put("fatPercentage", bfpResult.fatPercentage);
                        inputObject.put("muscleMass", bfpResult.muscleMass);
                        inputObject.put("protein", bfpResult.protein);
                        inputObject.put("calorie", bfpResult.calorie);
                        inputObject.put("minerals", bfpResult.minerals);
                        inputObject.put("icw", bfpResult.icw);
                        inputObject.put("ecw", bfpResult.ecw);

                        // Manual Calculation overrides
                        if (lastWeight > 0 && lastHeight > 0) {
                            double hM = lastHeight / 100.0;
                            double calculatedBmi = lastWeight / (hM * hM);
                            inputObject.put("bmi", calculatedBmi); 
                        }

                        if (lastWeight > 0 && bfpResult.icw > 0 && bfpResult.ecw > 0) {
                             double calculatedWaterPct = ((bfpResult.icw + bfpResult.ecw) / lastWeight) * 100.0;
                             inputObject.put("waterPercentage", calculatedWaterPct);
                        }
                        
                        sendDataUpdate(inputObject);
                    }
                    break;

                case FitrusConstants.EXTRA_TYPE_BATTERY:
                    int batteryLevel = intent.getIntExtra(FitrusConstants.EXTRA_DATA, -1);
                    Log.d(TAG, "Battery Level: " + batteryLevel);
                    break;
                case FitrusConstants.EXTRA_TYPE_ERROR:
                     String errorMsg = intent.getStringExtra(FitrusConstants.EXTRA_DATA);
                     if (errorMsg == null) errorMsg = "Unknown Error";
                     inputObject.put("connectState", "Error: " + errorMsg);
                     sendDataUpdate(inputObject);
                     // Do NOT disconnect here - DeviceService sends BFP result command
                     // which needs the connection to still be open
                     connectState = "Error";
                     break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error handling data", e);
        }
    }

    public void startBFP(String birth, double height, double weight, String gender) {
        Log.i(TAG, "[" + System.identityHashCode(this) + "] startBFP: " + connectState);
        this.lastHeight = height;
        this.lastWeight = weight;
        
        if (mFitLtServiceInterface != null) {
             mFitLtServiceInterface.startFitrusScan(ScanSettings.SCAN_MODE_LOW_LATENCY, 10000);
             
             int result = mFitLtServiceInterface.startBFP(
                    birth,
                    height,
                    weight,
                    gender,
                    "P",
                    "3"
            );
            Log.i(TAG, "startBFP result: " + result);
        } else {
            Log.e(TAG, "startBFP: Service not connected");
        }
    }

    public void dispose() {
        Log.i(TAG, "[" + System.identityHashCode(this) + "] dispose() called");
        if (mFitLtServiceInterface != null) {
            mFitLtServiceInterface.stopFitrusScan();
            mFitLtServiceInterface.closeFitrus();
            mFitLtServiceInterface = null;
        }
        try {
            applicationContext.unbindService(mServiceConnection);
            LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(mGattUpdateReceiver);
        } catch (Exception e) {
            Log.w(TAG, "Error during dispose: " + e.getMessage());
        }
    }
}
