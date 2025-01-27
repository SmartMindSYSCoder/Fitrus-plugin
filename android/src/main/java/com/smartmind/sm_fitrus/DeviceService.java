package com.smartmind.sm_fitrus;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.smartmind.sm_fitrus.Attributes.FitrusAttributes;
import com.smartmind.sm_fitrus.Interfaces.DeviceServiceBinder;
import com.smartmind.sm_fitrus.Interfaces.FitrusServiceInterface;
import com.smartmind.sm_fitrus.Models.DeviceInfo;
import com.smartmind.sm_fitrus.Results.FitrusLtResultData;
import com.smartmind.sm_fitrus.Utils.BinaryHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class DeviceService extends Service implements FitrusServiceInterface {
//    private final AsyncHttpClient aClient = new SyncHttpClient();


    private static final String TAG = DeviceService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder();
    private static BluetoothManager mBluetoothManager;
    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothLeScanner mBluetoothScanner;
    private static LocalBroadcastManager localBroadcastManager;
    private DeviceInfo deviceInfo = new DeviceInfo();
    private JSONObject ppgObject = new JSONObject();
    private boolean isStress = false;
    private boolean isResulting = false;
    private static boolean isRealServer = true;
    private static String strConnBase;
    private int mScanState = 0;
    private static ArraySet<String> mScanName;
    private static ArrayMap<String, DeviceInfo> mBluetoothMap;
    private String commandType = "NONE";
    private static final int GATT_TIMEOUT = 700;
    String birth;
    double height;
    double weight;
    String gender;
    String bodyType;
    String version;
    private String connectAddress = "";
    private String connectName = "";
    int batteryResponseCount = 0;
    final int MAX_SERIAL = 10;
    final int MAX_REVISION = 5;
    private ScanCallback mScanCallback = new ScanCallback() {
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            this.processResult(result);
        }

        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Iterator var2 = results.iterator();

            while (var2.hasNext()) {
                ScanResult result = (ScanResult) var2.next();
                this.processResult(result);
            }

        }

        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            DeviceService.this.mScanState = 0;
            DeviceService.this.broadcastUpdate("ACTION_SCAN_FAILED", (String) null);
        }

        private void processResult(ScanResult result) {
            Log.d(DeviceService.TAG, "result.getDevice() == " + result.getDevice() + " result.getDevice().getName() = " + result.getDevice().getName());
            if (result.getDevice() != null && result.getDevice().getName() != null) {
                String name = DeviceService.this.realNameToImageName(result.getDevice().getName());
                if (result.getDevice().getName().equals("Fitrus")) {
                    List<ParcelUuid> uuid = new ArrayList(Arrays.asList(ParcelUuid.fromString("0000FE00-EBAE-4526-9511-8357c35d7be2"), ParcelUuid.fromString("0000180D-0000-1000-8000-00805F9B34FB"), ParcelUuid.fromString("0000181B-0000-1000-8000-00805F9B34FB")));
                    if (!uuid.containsAll(result.getScanRecord().getServiceUuids())) {
                        return;
                    }
                }

                if (DeviceService.mScanName.contains(name)) {
                    DeviceService.mScanName.clear();
                    DeviceService.this.stopScan();
                    DeviceService.this.broadcastUpdate("ACTION_SCAN_SEARCHED", name);
                    DeviceService.this.connectFitrus(result.getDevice().getAddress(), name);
                }

            }
        }
    };
    Handler mHandler = new Handler();
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.d(DeviceService.TAG, "onServicesDiscovered() : onMtuChanged()");
            Log.i(DeviceService.TAG, "ble mtu changed " + mtu);
            gatt.discoverServices();
        }

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String name;
            if (newState == 2) {
                Log.i(DeviceService.TAG, "ble connected");
                gatt.requestConnectionPriority(1);
                DeviceService.this.deviceInfo.mConnectionState = 2;
                name = DeviceService.this.realNameToImageName(gatt.getDevice().getName());
                DeviceService.this.broadcastUpdate("ACTION_GATT_CONNECTED", name);
                gatt.discoverServices();
            } else if (newState == 0) {
                Log.i(DeviceService.TAG, "ble disconnected");
                DeviceService.this.disconnectFitrus();
                name = DeviceService.this.realNameToImageName(gatt.getDevice().getName());
                DeviceService.this.broadcastUpdate("ACTION_GATT_DISCONNECTED", name);
            }

        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 0) {
                if (gatt.getServices().size() == 0) {
                    Log.i(DeviceService.TAG, "onServicesDiscovered with 0 size");
                } else {
                    String name = DeviceService.this.realNameToImageName(gatt.getDevice().getName());
                    DeviceService.this.broadcastUpdate("ACTION_GATT_SERVICES_DISCOVERED", name);
                }
            } else {
                Log.i(DeviceService.TAG, "onServicesDiscovered : " + status);
            }

        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0) {
                Log.d(DeviceService.TAG, "onServicesDiscovered() : onCharacteristicRead()");
                DeviceService.this.broadcastUpdate("ACTION_DATA_AVAILABLE", gatt, characteristic);
            }

        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(DeviceService.TAG, "onServicesDiscovered() : onCharacteristicChanged()");
            DeviceService.this.broadcastUpdate("ACTION_DATA_AVAILABLE", gatt, characteristic);
        }
    };
    int deviceInfoResponseCount = 0;
    private FitrusLtResultData.DeviceInfo mDeviceInfo = new FitrusLtResultData.DeviceInfo();
    private FitrusLtResultData.HRV mHRV = new FitrusLtResultData.HRV();
    private FitrusLtResultData.Stress mStress = new FitrusLtResultData.Stress();
    private FitrusLtResultData.Body mBody = new FitrusLtResultData.Body();
    private FitrusLtResultData.Temperature mTemperature = new FitrusLtResultData.Temperature();
    private FitrusLtResultData.BP mBP = new FitrusLtResultData.BP();
    private FitrusLtResultData.Progress mProgress = new FitrusLtResultData.Progress();
    private ArrayList<Object> mPpgResult = new ArrayList();
    private int PPGMeasureTime = 30000;
    Timer timer = new Timer();
    TimerTask TT;
    int mDeviceProgress = 0;
    private double baseSystolic = 0.0;
    private double baseDiastolic = 0.0;

    public DeviceService() {
    }

    private void Init() {
        Log.d(TAG, "Init()");
        if (mBluetoothManager == null) {
            try {
                mBluetoothManager = (BluetoothManager) this.getSystemService("bluetooth");
            } catch (Exception var5) {
                Log.e(TAG, "Init() : mBluetoothManager Failed Initialize!");
                return;
            }
        }

        if (mBluetoothAdapter == null) {
            if (mBluetoothManager == null) {
                Log.e(TAG, "Init() : mBluetoothManager is NULL! Can't Initialize mBluetoothAdapter!");
                return;
            }

            try {
                mBluetoothAdapter = mBluetoothManager.getAdapter();
            } catch (Exception var4) {
                Log.e(TAG, "Init() : mBluetoothAdapter Failed Initialize!");
                return;
            }
        }

        if (mBluetoothScanner == null) {
            if (mBluetoothAdapter == null) {
                Log.e(TAG, "Init() : mBluetoothAdapter is NULL! Can't Initialize mBluetoothScanner!");
                return;
            }

            try {
                mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
            } catch (Exception var3) {
                Log.e(TAG, "Init() : mBluetoothScanner Failed Initialize!");
                return;
            }
        }

        if (localBroadcastManager == null) {
            try {
                localBroadcastManager = LocalBroadcastManager.getInstance(this);
            } catch (Exception var2) {
                Log.e(TAG, "Init() : localBroadcastManager Failed Initialize!");
                return;
            }
        }

    }

    public IntentFilter getGattUpdateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("ACTION_SCAN_SEARCHED");
        intentFilter.addAction("ACTION_SCAN_FAILED");
        intentFilter.addAction("ACTION_SCAN_COMPLETED");
        intentFilter.addAction("ACTION_GATT_CONNECTED");
        intentFilter.addAction("ACTION_GATT_DISCONNECTED");
        intentFilter.addAction("ACTION_GATT_SERVICES_DISCOVERED");
        intentFilter.addAction("ACTION_DATA_AVAILABLE");
        return intentFilter;
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return this.mBinder;
    }

    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public boolean startFitrusScan(int scanMode, int timeOutMills) {
        return this.startScan(scanMode, timeOutMills, "Fitrus", "Fitrus_A", "FitrusLight", "FitrusPlus3");
    }

    public boolean stopFitrusScan() {
        return this.stopScan();
    }

    public void disconnectFitrus() {
        this.disconnect(this.connectName);
        this.commandType = "NONE";
        this.deviceInfoResponseCount = 0;
        if (this.TT != null) {
            this.TT.cancel();
        }

    }

    public void closeFitrus() {
        this.close(this.connectName);
    }

    public int startBFP(String birth, double height, double weight, String gender, String bodyType, String version) {
        Log.e(TAG, "start Measurement : " + this.commandType);
        if (this.commandType == null) {
            return -1;
        } else if (this.commandType != "NONE") {
            return -2;
        } else if (version == null) {
            return -5;
        } else {
            this.commandType = "BFP";
            this.birth = birth;
            this.height = height;
            this.weight = weight;
            this.gender = gender;
            this.bodyType = bodyType;
            this.version = version;
            AsyncTask.execute(new Runnable() {
                public void run() {
                    DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*BFP:Start#\r\n".getBytes());
                }
            });
            return 0;
        }
    }

    public void bfpLocalMeasureStart() {
        this.commandType = "BFP_L";
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*BFP:Start#\r\n".getBytes());
            }
        });
    }

    public void stopBFP() {
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*BFP:Stop#\r\n".getBytes());
            }
        });
    }

    private void sendBFPResult(final Double result) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", String.format("*BFP:Result=%1$.1f#\r\n", result).getBytes());
            }
        });
    }

    public void spo2MeasureStart(String version) {
        this.commandType = "HRV";
        this.version = version;
        this.ppgObject = new JSONObject();
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*SpO2:Start#\r\n".getBytes());
            }
        });
    }

    public void spo2LocalMeasureStart() {
        this.commandType = "HRV_L";
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*SpO2:Start#\r\n".getBytes());
            }
        });
    }

    public void spo2MeasureStop() {
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*SpO2:Stop#\r\n".getBytes());
            }
        });
    }

    public int startHR(String version) {
        if (this.commandType == null) {
            return -1;
        } else if (this.commandType != "NONE") {
            return -2;
        } else if (this.connectName.equals("Fitrus_A")) {
            return -10;
        } else {
            this.isStress = false;
            this.spo2MeasureStart(version);
            return 0;
        }
    }

    public void stopHR() {
        if (this.commandType.equals("HRV")) {
            this.spo2MeasureStop();
        }

    }

    public int startStress(String version) {
        if (this.commandType == null) {
            return -1;
        } else if (this.commandType != "NONE") {
            return -2;
        } else if (this.connectName.equals("Fitrus_A")) {
            return -10;
        } else if (version == null) {
            return -5;
        } else {
            this.version = version;
            this.ppgObject = new JSONObject();
            this.commandType = "STRESS";
            if ((!this.connectName.equals("FitrusLight") || !(Double.parseDouble(version) > 3.0)) && !this.connectName.equals("FitrusPlus3")) {
                AsyncTask.execute(new Runnable() {
                    public void run() {
                        DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*SpO2:Start#\r\n".getBytes());
                    }
                });
            } else {
                AsyncTask.execute(new Runnable() {
                    public void run() {
                        DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*Stress:Start#\r\n".getBytes());
                    }
                });
            }

            this.isStress = true;
            return 0;
        }
    }

    public void stopStress() {
        if ((!this.connectName.equals("FitrusLight") || !(Double.parseDouble(this.version) > 3.0)) && !this.connectName.equals("FitrusPlus3")) {
            this.spo2MeasureStop();
        } else {
            AsyncTask.execute(new Runnable() {
                public void run() {
                    DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*Stress:Stop#\r\n".getBytes());
                }
            });
        }

    }

    public int startObjectTemp(String version) {
        if (this.commandType == null) {
            return -1;
        } else if (this.commandType != "NONE") {
            return -2;
        } else if (!this.connectName.equals("FitrusPlus3")) {
            return -10;
        } else if (version == null) {
            return -5;
        } else {
            this.commandType = "TEMP_O";
            this.version = version;
            AsyncTask.execute(new Runnable() {
                public void run() {
                    DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*Temp:Start#\r\n".getBytes());
                }
            });
            return 0;
        }
    }

    public void stopObjectTemp() {
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*Temp:Stop#\r\n".getBytes());
            }
        });
    }

    public int startSkinTemp(String version) {
        if (this.commandType == null) {
            return -1;
        } else if (this.commandType != "NONE") {
            return -2;
        } else if (!this.connectName.equals("FitrusPlus3")) {
            return -10;
        } else if (version == null) {
            return -5;
        } else {
            this.commandType = "TEMP_S";
            this.version = version;
            AsyncTask.execute(new Runnable() {
                public void run() {
                    DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*Temp.Body:Start#\r\n".getBytes());
                }
            });
            return 0;
        }
    }

    public void stopSkinTemp() {
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*Temp.Body:Stop#\r\n".getBytes());
            }
        });
    }

    public int startBP(String version, double baseSystolic, double baseDiastolic) {
        if (this.commandType == null) {
            return -1;
        } else if (this.commandType != "NONE") {
            return -2;
        } else if (!this.connectName.equals("FitrusPlus3")) {
            return -10;
        } else if (Double.parseDouble(version) < 1.2) {
            return -11;
        } else if (version == null) {
            return -5;
        } else {
            this.commandType = "BP";
            this.version = version;
            this.baseDiastolic = baseDiastolic;
            this.baseSystolic = baseSystolic;
            this.ppgObject = new JSONObject();
            AsyncTask.execute(new Runnable() {
                public void run() {
                    DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*Press:Start#\r\n".getBytes());
                }
            });
            return 0;
        }
    }

    public void stopBP() {
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*Press:Stop#\r\n".getBytes());
            }
        });
    }

    public boolean connectFitrus(String address, String name) {
        return this.connect(address, name);
    }

    public String getFitrusAddress() {
        return this.getDeviceAddress(this.connectName);
    }

    public String getFitrusName() {
        return this.connectName;
    }

    public void getBatteryLevel() {
        this.commandType = "BATT";
        this.batteryResponseCount = 1;
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*Dev.Info:Batt.Read#\r\n".getBytes());
            }
        });
    }

    public void calModeStart() {
        this.commandType = "CALI";
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*Calmode:Start#\r\n".getBytes());
            }
        });
    }

    public void calModeStop() {
        this.commandType = "CALI";
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*Calmode:Stop#\r\n".getBytes());
            }
        });
    }

    public int setDeviceBrightLevel(final int bright) {
        Log.d(TAG, "setDeviceBrightLevel == " + bright);
        if (bright >= 10 && bright <= 100) {
            if (!this.connectName.equals("Fitrus_A") && !this.connectName.equals("FitrusPlus3")) {
                this.commandType = "SETV";
                AsyncTask.execute(new Runnable() {
                    public void run() {
                        DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", String.format("*Dev.Info:Set.Bright=%1$d#\r\n", bright).getBytes());
                    }
                });
                return 0;
            } else {
                return -10;
            }
        } else {
            return 1;
        }
    }

    public int setDeviceMeasureCheckTime(final int msec) {
        if (msec < 0) {
            return 1;
        } else {
            this.commandType = "SETV";
            AsyncTask.execute(new Runnable() {
                public void run() {
                    DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", String.format("*Dev.Info:Set.BFP.MeasurChk.Time=%1$d#\r\n", msec).getBytes());
                }
            });
            return 0;
        }
    }

    public int setDeviceBFPMeasureCycleCount(final int count) {
        if (count < 0) {
            return 1;
        } else {
            this.commandType = "SETV";
            AsyncTask.execute(new Runnable() {
                public void run() {
                    DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", String.format("*Dev.Info:Set.BFP.MeasurCycle.Count=%1$d#\r\n", count).getBytes());
                }
            });
            return 0;
        }
    }

    public int setDeviceBFPMeasureCycleDelay(final int msec) {
        if (msec < 0) {
            return 1;
        } else {
            this.commandType = "SETV";
            AsyncTask.execute(new Runnable() {
                public void run() {
                    DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", String.format("*Dev.Info:Set.BFP.MeasurCycle.Delay=%1$d#\r\n", msec).getBytes());
                }
            });
            return 0;
        }
    }

    public int setDeviceBFPMeasurePrecision(final int precision) {
        if (precision >= 1 && precision <= 8) {
            this.commandType = "SETV";
            AsyncTask.execute(new Runnable() {
                public void run() {
                    DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", String.format("*Dev.Info:Set.BFP.MeasurPre=%1$d#\n", precision).getBytes());
                }
            });
            return 0;
        } else {
            return 1;
        }
    }

    public int setDeviceSerialNumber(final String serial) {
        if (serial == null) {
            return 2;
        } else if (serial.length() > 10) {
            return 1;
        } else {
            this.commandType = "SETC";
            AsyncTask.execute(new Runnable() {
                public void run() {
                    DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", String.format("*Dev.Info:Set.SerialNum=%10s#\r\n", serial).getBytes());
                }
            });
            return 0;
        }
    }

    public int setDeviceSoftwareRevision(final String revision) {
        if (revision == null) {
            return 2;
        } else if (revision.length() > 5) {
            return 1;
        } else {
            this.commandType = "SETC";
            AsyncTask.execute(new Runnable() {
                public void run() {
                    DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", String.format("*Dev.Info:Set.SoftwareRev=%s#\r\n", revision).getBytes());
                }
            });
            return 0;
        }
    }

    private void SendSp2Result(final int red, final int ir) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", String.format("*PPG:Result=%1$d,%2$d#\r\n", red, ir).getBytes());
            }
        });
    }

    private void SendLightStressResult(final int red, final int ir) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", String.format("*Stress:Result=%1$d,%2$d#\r\n", red, ir).getBytes());
            }
        });
    }

    private void SendPlusStressResult(final String stress) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", String.format("*Stress:Result=%S#\r\n", stress).getBytes());
            }
        });
    }

    private void SendTempResult(String type, final double temp) {
        final String tempCommand = type.equals("SKIN") ? "*Temp.Body:Result=%.2f#\r\n" : "*Temp:Result=%.2f#\r\n";
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", String.format(tempCommand, temp).getBytes());
            }
        });
    }

    private void SendBpResult(final double sdp, final double dbp) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", String.format("*Press:Result=%1$d,%2$d#\r\n", (int) sdp, (int) dbp).getBytes());
            }
        });
    }

    public int readCalibrationYn() {
        this.commandType = "READ_CALI_YN";
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*Dev.Info:calibration.Read#\r\n".getBytes());
            }
        });
        return 0;
    }

    public int readCalibrationValue() {
        this.commandType = "READ_CALI_V";
        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*Dev.Info:calibration_value.Read#\r\n".getBytes());
            }
        });
        return 0;
    }

    private void sendFitrusDevice(String serviceUuid, byte[] confgData) {
        this.send(this.connectName, serviceUuid, FitrusAttributes.getConfigChar(serviceUuid), FitrusAttributes.getDataChar(serviceUuid), confgData);
    }

    public boolean startScan(int scanMode, int timeOutMills, String... names) {
        Log.d(TAG, "startScan()");
        if (this.deviceInfo.mConnectionState != 0) {
            Log.e(TAG, "startScan() : Already Scanning or Connecting!");
            return false;
        } else if (mBluetoothScanner == null) {
            Log.e(TAG, "startScan() : mBluetoothScanner is NULL!");
            return false;
        } else if (mBluetoothAdapter == null) {
            Log.e(TAG, "startScan() : mBluetoothAdapter is NULL!");
            return false;
        } else {
            this.mScanState = 1;
            mScanName.clear();
            mScanName.addAll(Arrays.asList(names));
            ScanSettings scanSettings = (new ScanSettings.Builder()).setScanMode(scanMode).build();
            mBluetoothScanner.startScan((List) null, scanSettings, this.mScanCallback);
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (DeviceService.this.mScanState == 1) {
                        DeviceService.this.mScanState = 0;
                        DeviceService.mBluetoothScanner.stopScan(DeviceService.this.mScanCallback);
                        DeviceService.this.broadcastUpdate("ACTION_SCAN_FAILED", (String) null);
                    }

                }
            }, (long) timeOutMills);
            return true;
        }
    }

    public boolean stopScan() {
        Log.d(TAG, "stopScan()");
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "stopScan() : mBluetoothAdapter is NULL!");
            return false;
        } else if (mBluetoothScanner == null) {
            Log.e(TAG, "stopScan() : mBluetoothScanner is NULL!");
            return false;
        } else {
            mScanName.clear();
            this.mScanState = 0;
            mBluetoothScanner.stopScan(this.mScanCallback);
            return true;
        }
    }

    public boolean connect(String address, String name) {
        Log.d(TAG, "connect() = " + address + ", " + name);
        this.connectAddress = address;
        this.connectName = name;
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "connect() : mBluetoothAdapter is NULL!");
            return false;
        } else if (address == null) {
            Log.e(TAG, "connect() : address is NULL!");
            return false;
        } else {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                Log.e(TAG, "connect() : device is NULL!");
                return false;
            } else {
                if (VERSION.SDK_INT >= 23) {
                    this.deviceInfo.gatt = device.connectGatt(this, false, this.mGattCallback, 2);
                } else {
                    this.deviceInfo.gatt = device.connectGatt(this, false, this.mGattCallback);
                }

                this.deviceInfo.mConnectionState = 1;
                return true;
            }
        }
    }

    public void disconnect(String name) {
        Log.d(TAG, "disconnect() = " + name);
        this.connectAddress = "";
        this.connectName = "";
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "disconnect() : mBluetoothAdapter is NULL!");
        } else if (this.deviceInfo.mConnectionState == 0) {
            Log.e(TAG, "disconnect() : Already Disconnected!");
        } else {
            this.deviceInfo.mConnectionState = 0;
            this.deviceInfo.gatt.disconnect();
            this.deviceInfo.gatt.close();
        }
    }

    public void close(String name) {
        Log.d(TAG, "close() = " + name);
        if (this.deviceInfo.mConnectionState == 0) {
            Log.e(TAG, "close() : Already Disconnected!");
        } else {
            this.deviceInfo.gatt.close();
        }
    }

    public int getScanState() {
        return this.mScanState;
    }

    public String getDeviceAddress(String name) {
        try {
            return ((DeviceInfo) mBluetoothMap.get(name)).gatt.getDevice().getAddress();
        } catch (Exception var3) {
            Log.e(TAG, "No connected device");
            return null;
        }
    }

    public int setEndTimeAfterMeasure(final int time) {
        if (time < 0) {
            return 1;
        } else {
            this.commandType = "SETV";
            AsyncTask.execute(new Runnable() {
                public void run() {
                    DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", String.format("*Dev.Info:Set.Time.AS=%1$d#\r\n", time).getBytes());
                }
            });
            return 0;
        }
    }

    public void getDeviceInfo() {
        this.commandType = "INFO";
        if (this.connectName.equals("Fitrus_A")) {
            this.deviceInfoResponseCount = 7;
        } else {
            this.deviceInfoResponseCount = 8;
        }

        AsyncTask.execute(new Runnable() {
            public void run() {
                DeviceService.this.sendFitrusDevice("00000001-0000-1100-8000-00805f9b34fb", "*Dev.Info:Read#\r\n".getBytes());
            }
        });
    }

    private void broadcastUpdate(String action, String name) {
        Intent intent = new Intent(action);
        intent.putExtra("EXTRA_NAME", name);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void broadcastUpdate(String action, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent(action);
        Serializable dataFitrusLt = this.wrapFitrusLtResult(characteristic);
        if (dataFitrusLt != null) {
            Log.i("dataFitrusLt", "************ \t " + dataFitrusLt);
            intent.putExtra("EXTRA_DATA", dataFitrusLt);
            intent.putExtra("EXTRA_NAME", gatt.getDevice().getName());
            intent.putExtra("EXTRA_TYPE", characteristic.getUuid().toString());
            localBroadcastManager.sendBroadcast(intent);
        }
    }

    private void sendLocalbroadcast(String ExtraType, String ExtraName, Serializable s) {
        Intent intent = new Intent("ACTION_DATA_AVAILABLE");
        intent.putExtra("EXTRA_TYPE", ExtraType);
        intent.putExtra("EXTRA_NAME", ExtraName);
        intent.putExtra("EXTRA_DATA", s);
        Log.i("dataFitrusLt", "************ from send \t" + s);

        localBroadcastManager.sendBroadcast(intent);
    }

    private Serializable wrapFitrusLtResult(BluetoothGattCharacteristic characteristic) {
        byte[] b = characteristic.getValue();
        if (this.respnoseParseData(b) == null) {
            if (this.commandType.equals("HRV") || this.commandType.equals("BP") || this.commandType.equals("STRESS") || this.commandType.equals("HRV_L")) {
                ArrayList<Integer> ppgResult = new ArrayList();
                String red1 = String.valueOf(BinaryHelper.b3Int(b[0], b[1], b[2]));
                String ir1 = String.valueOf(BinaryHelper.b3Int(b[3], b[4], b[5]));
                String extraType = String.valueOf(BinaryHelper.b3Int(b[6], b[7], b[8]));
                String ir2 = String.valueOf(BinaryHelper.b3Int(b[9], b[10], b[11]));
                String red3 = String.valueOf(BinaryHelper.b3Int(b[12], b[13], b[14]));
                String ir3 = String.valueOf(BinaryHelper.b3Int(b[15], b[16], b[17]));
                ppgResult.add(Integer.parseInt(red1));
                ppgResult.add(Integer.parseInt(ir1));
                ppgResult.add(Integer.parseInt(extraType));
                ppgResult.add(Integer.parseInt(ir2));
                ppgResult.add(Integer.parseInt(red3));
                ppgResult.add(Integer.parseInt(ir3));
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss:SSS");
                dateFormat.format(date);
                JSONArray jsonArray = new JSONArray(ppgResult);

                try {
                    this.ppgObject.put(String.valueOf(now), jsonArray);
                } catch (Exception var17) {
                    Exception e = var17;
                    e.printStackTrace();
                }

                this.sendLocalbroadcast("RAWH", this.connectName, ppgResult);
            }

            return null;
        } else {
            switch (characteristic.getUuid().toString()) {
                case "00000003-0000-1100-8000-00805f9b34fb":
                    FitrusAttributes.Data resData = FitrusAttributes.convertByteToData(b);
                    //  Log.d("resData",resData.category);
                    //  Log.d("resData",resData.toString());

                    if (resData.category.equals("Dev.Info")) {
                        if (this.commandType.equals("INFO")) {
                            Log.d(TAG, "resData.type = [" + resData.type + "] deviceInfoResponseCount = " + this.deviceInfoResponseCount);
                            switch (resData.type) {
                                case "Firm.Ver":
                                    this.mDeviceInfo.firmwareVersion = Float.parseFloat(resData.value);
                                    Log.d(TAG, "Firm.Ver = " + this.mDeviceInfo.firmwareVersion);
                                    if (resData.value != null) {
                                        this.version = resData.value;
                                    }
                                    break;
                                case "Battery":
                                    this.mDeviceInfo.batteryLevel = Integer.parseInt(resData.value);
                                    Log.d(TAG, "Battery = " + this.mDeviceInfo.batteryLevel);
                                    break;
                                case "Bright":
                                    this.mDeviceInfo.bright = Integer.parseInt(resData.value);
                                    Log.d(TAG, "Bright = " + this.mDeviceInfo.bright);
                                    break;
                                case "BFP.MeasurChk.Time":
                                    this.mDeviceInfo.measureCheckTime = Integer.parseInt(resData.value);
                                    Log.d(TAG, "BFP.MeasurChk.Time = " + this.mDeviceInfo.measureCheckTime);
                                    break;
                                case "BFP.MeasurCycle.Count":
                                    this.mDeviceInfo.measureCycleCount = Integer.parseInt(resData.value);
                                    Log.d(TAG, "BFP.MeasurCycle.Count = " + this.mDeviceInfo.measureCycleCount);
                                    break;
                                case "BFP.MeasurCycle.Delay":
                                    this.mDeviceInfo.measureCycleDelay = Integer.parseInt(resData.value);
                                    Log.d(TAG, "BFP.MeasurCycle.Delay = " + this.mDeviceInfo.measureCycleDelay);
                                    break;
                                case "BFP.MeasurPre":
                                    this.mDeviceInfo.measurePrecision = Integer.parseInt(resData.value);
                                    Log.d(TAG, "BFP.MeasurPre = " + this.mDeviceInfo.measurePrecision);
                                    break;
                                case "Time.AS":
                                    this.mDeviceInfo.endTimeAfterMeasure = Integer.parseInt(resData.value);
                                    Log.d(TAG, "Time.AS = " + this.mDeviceInfo.endTimeAfterMeasure);
                                    break;
                                default:
                                    Log.d(TAG, "Unknown Response Data");
                            }

                            if (--this.deviceInfoResponseCount == 0) {
                                this.mDeviceInfo.result = FitrusLtResultData.RESULT.SUCCESS;
                                this.sendLocalbroadcast(this.commandType, this.connectName, this.mDeviceInfo);
                                this.commandType = "NONE";
                                return null;
                            }
                        } else if (this.commandType.equals("BATT")) {
                            if (--this.batteryResponseCount == 0) {
                                this.sendLocalbroadcast(this.commandType, this.connectName, Integer.parseInt(resData.value));
                                this.commandType = "NONE";
                                return null;
                            }
                        } else {
                            if (this.commandType.equals("SETV")) {
                                this.sendLocalbroadcast(this.commandType, this.connectName, Integer.parseInt(resData.value));
                                this.commandType = "NONE";
                                return null;
                            }

                            if (this.commandType.equals("SETC")) {
                                this.sendLocalbroadcast(this.commandType, this.connectName, resData.value);
                                this.commandType = "NONE";
                                return null;
                            }

                            if (this.commandType.equals("READ_CALI_YN")) {
                                this.sendLocalbroadcast(this.commandType, this.connectName, Integer.parseInt(resData.value));
                                this.commandType = "NONE";
                                return null;
                            }

                            if (this.commandType.equals("READ_CALI_V")) {
                                this.sendLocalbroadcast(this.commandType, this.connectName, Integer.parseInt(resData.value));
                                this.commandType = "NONE";
                                return null;
                            }
                        }
                    }

                    if ("BFP".equals(resData.category) && "Raw".equals(resData.type)) {
                        String extraType = "";
                        switch (this.commandType) {
                            case "BFP":
                                extraType = "RAWB";
                                break;
                            case "CALI":
                                extraType = "RAWC";
                                break;
                            default:
                                extraType = "RAWB";
                        }

                        this.sendLocalbroadcast(extraType, this.connectName, Double.parseDouble(resData.value));
                        return null;
                    }

                    if ("BFP".equals(resData.category) && "End.Raw".equals(resData.type)) {
                        if (this.commandType.equals("BFP")) {
                            if (Double.parseDouble(resData.value) <= 0.0) {
                                Log.d(TAG, "Wrong Value. Next Err code.... ");
                                return null;
                            }

                            if (!this.isResulting) {
                                this.isResulting = true;
                                this.sendToServerBfp(Double.parseDouble(resData.value));
                            }
                        } else if (this.commandType.equals("BFP_L")) {
                            this.sendBFPResult(Double.parseDouble(resData.value));
                            this.sendLocalbroadcast(this.commandType, this.connectName, Double.parseDouble(resData.value));
                            this.commandType = "NONE";
                        } else if (this.commandType.equals("CALI")) {
                            Log.d(TAG, "Calibration Mode End.Raw");
                            this.sendLocalbroadcast(this.commandType, this.connectName, Double.parseDouble(resData.value));
                            this.stopBFP();
                            this.commandType = "NONE";
                        }

                        return null;
                    }

                    if ("BFP".equals(resData.category) && "CAL.END".equals(resData.type)) {
                        if (this.commandType.equals("CALI")) {
                            this.sendBFPResult(Double.parseDouble(resData.value));
                            this.sendLocalbroadcast(this.commandType, this.connectName, Double.parseDouble(resData.value));
                            this.commandType = "NONE";
                        }
                    } else if ("BFP".equals(resData.category) && "Prog".equals(resData.type)) {
                        this.mProgress.deviceName = this.getConnDeviceName(this.connectName);
                        this.mProgress.firmwareVersion = Float.parseFloat(this.version);
                        this.mProgress.strMeasureName = "Body Fat Percents";
                        this.mProgress.progressValue = Integer.parseInt(resData.value);
                        this.sendLocalbroadcast("PROGRESS", this.connectName, this.mProgress);
                    }

                    if ("PPG".equals(resData.category) && "End".equals(resData.type)) {
                        if (this.commandType.equals("HRV")) {
                            if (!this.isResulting) {
                                this.isResulting = true;
                                this.sendToServerPpg();
                            }
                        } else if (this.commandType.equals("HRV_L")) {
                            this.SendSp2Result(0, 0);
                            this.mHRV.result = FitrusLtResultData.RESULT.SUCCESS;
                            this.mHRV.dSp02 = 1000;
                            this.mHRV.dBPM = 77;
                            this.sendLocalbroadcast(this.commandType, this.connectName, this.mHRV);
                            this.commandType = "NONE";
                        } else if (this.commandType.equals("STRESS") && !this.isResulting) {
                            this.isResulting = true;
                            this.sendToServerPpg();
                        }

                        return null;
                    }

                    if ("SpO2".equals(resData.category) && "StartOK".equals(resData.type)) {
                        this.isResulting = false;
                        if (this.commandType.equals("HRV")) {
                            this.timer.schedule(this.TT = new TimerTask() {
                                public void run() {
                                    DeviceService.this.mProgress.deviceName = DeviceService.this.getConnDeviceName(DeviceService.this.connectName);
                                    DeviceService.this.mProgress.firmwareVersion = Float.parseFloat(DeviceService.this.version);
                                    DeviceService.this.mProgress.strMeasureName = "HRV";
                                    DeviceService.this.mProgress.progressValue = 100 * DeviceService.this.mDeviceProgress / (DeviceService.this.getPPGMeasureTime() / 1000);
                                    DeviceService.this.sendLocalbroadcast("PROGRESS", DeviceService.this.connectName, DeviceService.this.mProgress);
                                    if (DeviceService.this.mDeviceProgress < DeviceService.this.getPPGMeasureTime() / 1000) {
                                        ++DeviceService.this.mDeviceProgress;
                                    } else {
                                        DeviceService.this.spo2MeasureStop();
                                        DeviceService.this.mDeviceProgress = 0;
                                        DeviceService.this.TT.cancel();
                                    }
                                }
                            }, 0L, 1000L);
                        } else if (this.commandType.equals("STRESS")) {
                            this.timer.schedule(this.TT = new TimerTask() {
                                public void run() {
                                    DeviceService.this.mProgress.deviceName = DeviceService.this.getConnDeviceName(DeviceService.this.connectName);
                                    DeviceService.this.mProgress.firmwareVersion = Float.parseFloat(DeviceService.this.version);
                                    DeviceService.this.mProgress.strMeasureName = "STRESS";
                                    DeviceService.this.mProgress.progressValue = 100 * DeviceService.this.mDeviceProgress / (DeviceService.this.getPPGMeasureTime() / 1000);
                                    DeviceService.this.sendLocalbroadcast("PROGRESS", DeviceService.this.connectName, DeviceService.this.mProgress);
                                    if (DeviceService.this.mDeviceProgress < DeviceService.this.getPPGMeasureTime() / 1000) {
                                        ++DeviceService.this.mDeviceProgress;
                                    } else {
                                        DeviceService.this.spo2MeasureStop();
                                        DeviceService.this.mDeviceProgress = 0;
                                        DeviceService.this.TT.cancel();
                                    }
                                }
                            }, 0L, 1000L);
                        }
                    }

                    if ("Stress".equals(resData.category) && "End".equals(resData.type)) {
                        if (!this.isResulting && this.commandType == "STRESS") {
                            this.isResulting = true;
                            this.sendToServerPpg();
                        }

                        return null;
                    }

                    if ("Stress".equals(resData.category) && "StartOK".equals(resData.type)) {
                        this.isResulting = false;
                        if (this.commandType.equals("STRESS")) {
                            this.timer.schedule(this.TT = new TimerTask() {
                                public void run() {
                                    DeviceService.this.mProgress.deviceName = DeviceService.this.getConnDeviceName(DeviceService.this.connectName);
                                    DeviceService.this.mProgress.firmwareVersion = Float.parseFloat(DeviceService.this.version);
                                    DeviceService.this.mProgress.strMeasureName = "STRESS";
                                    DeviceService.this.mProgress.progressValue = 100 * DeviceService.this.mDeviceProgress / (DeviceService.this.getPPGMeasureTime() / 1000);
                                    DeviceService.this.sendLocalbroadcast("PROGRESS", DeviceService.this.connectName, DeviceService.this.mProgress);
                                    if (DeviceService.this.mDeviceProgress < DeviceService.this.getPPGMeasureTime() / 1000) {
                                        ++DeviceService.this.mDeviceProgress;
                                    } else {
                                        DeviceService.this.stopStress();
                                        DeviceService.this.mDeviceProgress = 0;
                                        DeviceService.this.TT.cancel();
                                    }
                                }
                            }, 0L, 1000L);
                        }
                    }

                    if ("Press".equals(resData.category) && "End".equals(resData.type)) {
                        if (!this.isResulting && this.commandType == "BP") {
                            this.isResulting = true;
                            this.sendToServerBp();
                        }

                        return null;
                    }

                    if ("Press".equals(resData.category) && "StartOK".equals(resData.type)) {
                        this.isResulting = false;
                        if (this.commandType.equals("BP")) {
                            Log.d(TAG, "timer.schedule() : " + this.getPPGMeasureTime());
                            this.timer.schedule(this.TT = new TimerTask() {
                                public void run() {
                                    DeviceService.this.mProgress.deviceName = DeviceService.this.getConnDeviceName(DeviceService.this.connectName);
                                    DeviceService.this.mProgress.firmwareVersion = Float.parseFloat(DeviceService.this.version);
                                    DeviceService.this.mProgress.strMeasureName = "Blood Pressure";
                                    DeviceService.this.mProgress.progressValue = 100 * DeviceService.this.mDeviceProgress / (DeviceService.this.getPPGMeasureTime() / 1000);
                                    DeviceService.this.sendLocalbroadcast("PROGRESS", DeviceService.this.connectName, DeviceService.this.mProgress);
                                    if (DeviceService.this.mDeviceProgress < DeviceService.this.getPPGMeasureTime() / 1000) {
                                        ++DeviceService.this.mDeviceProgress;
                                    } else {
                                        DeviceService.this.stopBP();
                                        DeviceService.this.mDeviceProgress = 0;
                                        DeviceService.this.TT.cancel();
                                    }
                                }
                            }, 0L, 1000L);
                        }
                    }

                    if ("Temp".equals(resData.category) && "End.Aver".equals(resData.type) && this.commandType.equals("TEMP_O") && !this.isResulting) {
                        this.isResulting = true;
                        this.sendToServerTemp("O", Double.parseDouble(resData.value));
                    }

                    if ("Temp.Body".equals(resData.category) && "End.Aver".equals(resData.type) && this.commandType.equals("TEMP_S") && !this.isResulting) {
                        this.isResulting = true;
                        this.sendToServerTemp("S", Double.parseDouble(resData.value));
                    }

                    if ("Temp".equals(resData.category)) {
                        if ("StartOK".equals(resData.type)) {
                            Log.d(TAG, "Temp  Start OK");
                            this.mProgress.deviceName = this.getConnDeviceName(this.connectName);
                            this.mProgress.firmwareVersion = Float.parseFloat(this.version);
                            this.mProgress.strMeasureName = "Object Temperature";
                            this.mProgress.progressValue = 0;
                            this.sendLocalbroadcast("PROGRESS", this.connectName, this.mProgress);
                        } else if ("StopOK".equals(resData.type)) {
                            Log.d(TAG, "Temp  Stop OK");
                        } else if ("End.Aver".equals(resData.type)) {
                            Log.d(TAG, "Temp Measure Completed!!");
                        } else {
                            Log.d(TAG, "Protocol Error");
                        }
                    } else if ("Temp.Body".equals(resData.category)) {
                        if ("StartOK".equals(resData.type)) {
                            Log.d(TAG, "Temp  Start OK");
                            this.mProgress.deviceName = this.getConnDeviceName(this.connectName);
                            this.mProgress.firmwareVersion = Float.parseFloat(this.version);
                            this.mProgress.strMeasureName = "Skin Temperature";
                            this.mProgress.progressValue = 0;
                            this.sendLocalbroadcast("PROGRESS", this.connectName, this.mProgress);
                        } else if ("StopOK".equals(resData.type)) {
                            Log.d(TAG, "Temp  Stop OK");
                        } else if ("End.Aver".equals(resData.type)) {
                            Log.d(TAG, "Temp Measure Completed!!");
                        } else {
                            Log.d(TAG, "Protocol Error");
                        }
                    }

                    if ("Calmode".equals(resData.category) && "StartOK".equals(resData.type)) {
                        Log.d(TAG, "Cal Mode Start OK");
                    } else if ("Calmode".equals(resData.category) && "StopOK".equals(resData.type)) {
                        Log.d(TAG, "Cal Mode Stop OK");
                    }

                    if ("Err".equals(resData.category)) {
                        Log.d(TAG, "Error Processing!! ");
                        switch (resData.type) {
                            case "227":
                                this.sendLocalbroadcast("ERROR", this.connectName, "Low Battery");
                                break;
                            case "226":
                                this.sendLocalbroadcast("ERROR", this.connectName, "electrode error");
                                break;
                            case "225":
                                this.sendLocalbroadcast("ERROR", this.connectName, "Motion detection during measurement");
                                break;
                            case "224":
                                this.sendLocalbroadcast("ERROR", this.connectName, "Timeout Error");
                                break;
                            default:
                                this.sendLocalbroadcast("ERROR", this.connectName, "Unknown Error");
                        }

                        if (this.commandType.equals("HRV")) {
                            this.spo2MeasureStop();
                        } else if (this.commandType.equals("BFP") || this.commandType.equals("CALI")) {
                            this.stopBFP();
                        }

                        this.isResulting = false;
                        this.commandType = "NONE";
                    }
                    break;
                default:
                    Log.d(TAG, "default");
            }

            return null;
        }
    }

    private String respnoseParseData(byte[] bdata) {
        String str = new String(bdata);
        int startIndex = str.indexOf(10) + 1;
        int endIndex = str.indexOf(13);
        if ((this.commandType == "HRV" || this.commandType == "HRV_L" || this.commandType == "STRESS" || this.commandType == "BP") && bdata.length == 18) {
            return null;
        } else {
            return startIndex >= 0 && endIndex >= 0 ? str.substring(startIndex, endIndex) : null;
        }
    }

    private void send(String name, String serviceUuid, String configUuid, String dataUuid, byte[] configData) {
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
        } else if (this.deviceInfo.mConnectionState == 0) {
            Log.e(TAG, "no device connected " + name);
        } else {
            UUID service = UUID.fromString(serviceUuid);
            UUID config = UUID.fromString(configUuid);
            BluetoothGattService gattService = this.deviceInfo.gatt.getService(service);
            if (gattService == null) {
                Log.e(TAG, "Bluetooth connection not completed");
            } else {
                BluetoothGattCharacteristic configChar = gattService.getCharacteristic(config);
                if (dataUuid != null) {
                    UUID data = UUID.fromString(dataUuid);
                    BluetoothGattCharacteristic dataChar = gattService.getCharacteristic(data);
                    this.setCharacteristicNotification(this.deviceInfo.gatt, dataChar);
                }

                this.writeCharacteristic(this.deviceInfo.gatt, configChar, configData);
            }
        }
    }

    private void setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
        } else {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("2902-0000-1000-8000-00805f9b34fb"));
            if (descriptor.getValue() == null) {
                gatt.setCharacteristicNotification(characteristic, true);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
                waitIdle(700);
            }

        }
    }

    private void writeCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] b) {
        characteristic.setValue(b);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//
//            return;
//        }
        gatt.writeCharacteristic(characteristic);
        waitIdle(100);
    }

    private static void waitIdle(int timeout) {
        timeout /= 100;

        while (true) {
            --timeout;
            if (timeout < 0) {
                return;
            }

            try {
                Thread.sleep(100L);
            } catch (InterruptedException var2) {
                InterruptedException e = var2;
                e.printStackTrace();
            }
        }
    }

    private String realNameToImageName(String name) {
        return name;
    }

    public void setPPGMeasureTime(int time) {
        this.PPGMeasureTime = time;
    }

    private int getPPGMeasureTime() {
        return this.PPGMeasureTime;
    }

    private String getDeviceCode() {
        switch (this.connectName) {
            case "Fitrus_A":
                return "FA";
            case "Fitrus":
            case "FitrusLight":
                return "FL";
            case "FitrusNeo":
            case "FitrusPlus3":
                return "FN";
            default:
                return "NONE";
        }
    }

    private String stressLevelConvert(String stresslevel) {
        switch (stresslevel) {
            case "HIGH":
                return "H";
            case "MID":
                return "N";
            case "LOW":
                return "L";
            default:
                return "";
        }
    }

    private void sendToServerPpg() {
        JSONObject inputObject = new JSONObject();

        try {
            inputObject.put("device", this.getDeviceCode());
            inputObject.put("list", this.ppgObject);
            inputObject.put("version", this.version);
        } catch (Exception var5) {
            Exception e = var5;
            e.printStackTrace();
        }


        if (DeviceService.this.isStress) {
            if (DeviceService.this.connectName.equals("FitrusLight") && Double.parseDouble(DeviceService.this.version) > 3.0) {
                DeviceService.this.SendLightStressResult(DeviceService.this.mStress.dSp02, DeviceService.this.mStress.dBPM);
            } else if (DeviceService.this.connectName.equals("FitrusPlus3")) {
                DeviceService.this.SendPlusStressResult(DeviceService.this.stressLevelConvert(DeviceService.this.mStress.StressLevel));
            } else {
                DeviceService.this.SendSp2Result(DeviceService.this.mHRV.dSp02, DeviceService.this.mHRV.dBPM);
            }

            DeviceService.this.sendLocalbroadcast(DeviceService.this.commandType, DeviceService.this.connectName, DeviceService.this.mStress);
        } else {
            DeviceService.this.SendSp2Result(DeviceService.this.mHRV.dSp02, DeviceService.this.mHRV.dBPM);
            DeviceService.this.sendLocalbroadcast(DeviceService.this.commandType, DeviceService.this.connectName, DeviceService.this.mHRV);
        }

        DeviceService.this.commandType = "NONE";
        DeviceService.this.isResulting = false;

//        StringEntity params = null;
//
//        try {
//            params = new StringEntity(inputObject.toString(), "UTF-8");
//        } catch (UnsupportedEncodingException var4) {
//            UnsupportedEncodingException e = var4;
//            e.printStackTrace();
//        }

        String Url = strConnBase + "heart/guestPpgMeasure";
        if (this.isStress) {
            Url = strConnBase + "stress/guestPpgMeasure";
        }

//        Singleton.getClient().post(this, Url, params, "application/json", new Singleton.JsonResponseHandler() {
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                super.onSuccess(statusCode, headers, response);
//                DeviceService.this.saveReultValue(response);
//
//            }
//
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                super.onFailure(statusCode, headers, throwable, errorResponse);
//                if (DeviceService.this.commandType.equals("STRESS")) {
//                    DeviceService.this.stopStress();
//                } else {
//                    DeviceService.this.spo2MeasureStop();
//                }
//
//                DeviceService.this.isResulting = false;
//                DeviceService.this.commandType = "NONE";
//                DeviceService.this.sendLocalbroadcast("ERROR", DeviceService.this.connectName, "Error Code : " + statusCode);
//            }
//
//            public boolean getUseSynchronousMode() {
//                return false;
//            }
//        });
    }


//    private void sendToServerBfp(Double value) {
//        JSONObject inputObject = new JSONObject();
//
//        try {
//            inputObject.put("birth", this.birth);
//            inputObject.put("gender", this.gender);
//            inputObject.put("device", this.getDeviceCode());
//            inputObject.put("height", this.height);
//            inputObject.put("weight", this.weight);
//            inputObject.put("value", value);
//            inputObject.put("bodyType", this.bodyType);
//            inputObject.put("version", this.version);
//        } catch (Exception var6) {
//            Exception e = var6;
//            e.printStackTrace();
//        }
//
//        Log.d("json_data",inputObject.toString());
//
////        StringEntity params = null;
//
//
////        HttpEntity entity = new HttpEntity();
////        ByteArrayEntity entity=null;
//        //   entity=new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8"));
////        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
////        params = new StringEntity(inputObject.toString(), "UTF-8");
//
//
//        //        DeviceService.this.sendBFPResult(DeviceService.this.mBody.fatPercentage);
////        DeviceService.this.sendLocalbroadcast(DeviceService.this.commandType, DeviceService.this.connectName, DeviceService.this.mBody);
////        DeviceService.this.commandType = "NONE";
////        DeviceService.this.isResulting = false;
//        String Url = strConnBase + "body/guest";
////        String Url = strConnBase + "http://52.188.66.123:8381/body/guest";
//
//
//        volleySend(value);
//
//
//
////        Singleton.getClient().post(this, Url, params, "application/json", new Singleton.JsonResponseHandler() {
////            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
////                super.onSuccess(statusCode, headers, response);
////                DeviceService.this.saveReultValue(response);
////                DeviceService.this.sendBFPResult(DeviceService.this.mBody.fatPercentage);
////                DeviceService.this.sendLocalbroadcast(DeviceService.this.commandType, DeviceService.this.connectName, DeviceService.this.mBody);
////                DeviceService.this.commandType = "NONE";
////                DeviceService.this.isResulting = false;
////            }
////
////            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
////                super.onFailure(statusCode, headers, throwable, errorResponse);
////                DeviceService.this.stopBFP();
////                DeviceService.this.commandType = "NONE";
////                DeviceService.this.isResulting = false;
////               // Log.d("errorResponse",errorResponse.toString());
////
////                DeviceService.this.sendLocalbroadcast("ERROR", DeviceService.this.connectName, "Error Code : " + statusCode);
////            }
////
////            public boolean getUseSynchronousMode() {
////                return false;
////            }
////        });
//    }

    private void sendToServerBfp(Double value) {

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        String postUrl = "http://52.188.66.123:8381/body/guest";


        JSONObject inputObject = new JSONObject();

        try {
            inputObject.put("birth", this.birth);
            inputObject.put("gender", this.gender);
            inputObject.put("device", this.getDeviceCode());
            inputObject.put("height", this.height);
            inputObject.put("weight", this.weight);
            inputObject.put("value", value);
            inputObject.put("bodyType", this.bodyType);
            inputObject.put("version", this.version);
        } catch (Exception var6) {
            Exception e = var6;
            e.printStackTrace();
        }

// Create a JsonObjectRequest
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, postUrl, inputObject,


                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle the response
                        System.out.println("Response: " + response.toString());
                        DeviceService.this.saveReultValue(response);

                        DeviceService.this.sendBFPResult(DeviceService.this.mBody.fatPercentage);
                        DeviceService.this.sendLocalbroadcast(DeviceService.this.commandType, DeviceService.this.connectName, DeviceService.this.mBody);
                        DeviceService.this.commandType = "NONE";
                        DeviceService.this.isResulting = false;

                    }
                },


                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle the error
                        System.err.println("Error: " + error.getMessage());
                    }
                }


        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // add headers <key,value>
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };




// Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);

}


    private void sendToServerBp() {
        JSONObject inputObject = new JSONObject();

        try {
            inputObject.put("device", this.getDeviceCode());
            inputObject.put("version", this.version);
            inputObject.put("baseDiastolic", this.baseDiastolic);
            inputObject.put("baseSystolic", this.baseSystolic);
            inputObject.put("list", this.ppgObject);
        } catch (Exception var5) {
            Exception e = var5;
            e.printStackTrace();
        }

//        StringEntity params = null;

//        try {
//            params = new StringEntity(inputObject.toString(), "UTF-8");
//        } catch (UnsupportedEncodingException var4) {
//            UnsupportedEncodingException e = var4;
//            e.printStackTrace();
//        }
        DeviceService.this.SendBpResult(DeviceService.this.mBP.sbp, DeviceService.this.mBP.dbp);
        DeviceService.this.sendLocalbroadcast(DeviceService.this.commandType, DeviceService.this.connectName, DeviceService.this.mBP);
        DeviceService.this.commandType = "NONE";
        DeviceService.this.isResulting = false;
        String Url = strConnBase + "bp/guestPpgMeasure";
//        Singleton.getClient().post(this, Url, params, "application/json", new Singleton.JsonResponseHandler() {
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                super.onSuccess(statusCode, headers, response);
//                DeviceService.this.saveReultValue(response);
//                DeviceService.this.SendBpResult(DeviceService.this.mBP.sbp, DeviceService.this.mBP.dbp);
//                DeviceService.this.sendLocalbroadcast(DeviceService.this.commandType, DeviceService.this.connectName, DeviceService.this.mBP);
//                DeviceService.this.commandType = "NONE";
//                DeviceService.this.isResulting = false;
//            }
//
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                super.onFailure(statusCode, headers, throwable, errorResponse);
//                DeviceService.this.stopBP();
//                DeviceService.this.isResulting = false;
//                DeviceService.this.commandType = "NONE";
//                DeviceService.this.sendLocalbroadcast("ERROR", DeviceService.this.connectName, "Error Code : " + statusCode);
//            }
//
//            public boolean getUseSynchronousMode() {
//                return false;
//            }
//        });
    }

    private void sendToServerTemp(String type, double temp) {
        JSONObject inputObject = new JSONObject();

        try {
            inputObject.put("device", this.getDeviceCode());
            inputObject.put("temp", Math.ceil(temp * 10.0) / 10.0);
            inputObject.put("type", type);
            inputObject.put("version", this.version);
        } catch (Exception var8) {
            Exception e = var8;
            e.printStackTrace();
        }

//        StringEntity params = null;

//        try {
//            params = new StringEntity(inputObject.toString(), "UTF-8");
//        } catch (UnsupportedEncodingException var7) {
//            UnsupportedEncodingException e = var7;
//            e.printStackTrace();
//        }


      //  DeviceService.this.saveReultValue(response);
        DeviceService.this.SendTempResult(DeviceService.this.mTemperature.type, DeviceService.this.mTemperature.value);
        DeviceService.this.sendLocalbroadcast(DeviceService.this.commandType, DeviceService.this.connectName, DeviceService.this.mTemperature);
        DeviceService.this.commandType = "NONE";
        DeviceService.this.isResulting = false;

//        String Url = strConnBase + "temp/guest";
//        Singleton.getClient().post(this, Url, params, "application/json", new Singleton.JsonResponseHandler() {
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                super.onSuccess(statusCode, headers, response);
//
//            }
//
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                super.onFailure(statusCode, headers, throwable, errorResponse);
//                if (DeviceService.this.commandType.equals("TEMP_S")) {
//                    DeviceService.this.stopSkinTemp();
//                } else {
//                    DeviceService.this.stopObjectTemp();
//                }
//
//                DeviceService.this.isResulting = false;
//                DeviceService.this.commandType = "NONE";
//                DeviceService.this.sendLocalbroadcast("ERROR", DeviceService.this.connectName, "Error Code : " + statusCode);
//            }
//
//            public boolean getUseSynchronousMode() {
//                return false;
//            }
//        });
    }

    private void saveReultValue(JSONObject response) {
        try {
            JSONObject jsonObject = response;
            double date;
            if (this.commandType.equals("BFP")) {
                date = jsonObject.getDouble("date");
                String device = jsonObject.getString("device");
                double bfp = jsonObject.getDouble("bfp");
                double bfm = jsonObject.getDouble("bfm");
                double smm = jsonObject.getDouble("smm");
                double bmr = jsonObject.getDouble("bmr");
                double bmi = jsonObject.getDouble("bmi");
                double bwp = jsonObject.getDouble("bwp");
                double calorie = jsonObject.getDouble("calorie");
                double protein = jsonObject.getDouble("protein");
                double minerals = jsonObject.getDouble("minerals");
                this.mBody.measureDate = date;
                this.mBody.bmi = bmi;
                this.mBody.bmr = bmr;
                this.mBody.fatMass = bfm;
                this.mBody.waterPercentage = bwp;
                this.mBody.fatPercentage = bfp;
                this.mBody.muscleMass = smm;
                this.mBody.calorie = calorie;
                this.mBody.minerals = minerals;
                this.mBody.protein = protein;
                this.mBody.result = FitrusLtResultData.RESULT.SUCCESS;
                this.mBody.deviceName = this.getConnDeviceName(this.connectName);
                this.mBody.firmwareVersion = Float.parseFloat(this.version);
            } else if (!this.commandType.equals("HRV") && !this.commandType.equals("STRESS")) {
                double temp;
                if (!this.commandType.equals("TEMP_S") && !this.commandType.equals("TEMP_O")) {
                    if (this.commandType.equals("BP")) {
                        date = (double)(new Date()).getTime();
                        temp = (double)jsonObject.getInt("dbpPredictMean");
                        double sbp = (double)jsonObject.getInt("sbpPredictMean");
                        this.mBP.result = FitrusLtResultData.RESULT.SUCCESS;
                        this.mBP.measureDate = date;
                        this.mBP.dbp = temp;
                        this.mBP.sbp = sbp;
                        this.mBP.deviceName = this.getConnDeviceName(this.connectName);
                        this.mBP.firmwareVersion = Float.parseFloat(this.version);
                    }
                } else {
                    date = jsonObject.getDouble("date");
                    temp = jsonObject.getDouble("temp");
                    this.mTemperature.result = FitrusLtResultData.RESULT.SUCCESS;
                    this.mTemperature.measureDate = date;
                    if (this.commandType.equals("TEMP_S")) {
                        this.mTemperature.type = "SKIN";
                    } else {
                        this.mTemperature.type = "OBJECT";
                    }

                    this.mTemperature.value = temp;
                    this.mTemperature.deviceName = this.getConnDeviceName(this.connectName);
                    this.mTemperature.firmwareVersion = Float.parseFloat(this.version);
                }
            } else {
                date = jsonObject.getDouble("date");
                int oxygen = jsonObject.getInt("oxygen");
                int bpm = jsonObject.getInt("bpm");
                String device = "";
                String stressLevel = "";
                int stressValue = 0;
                if (this.isStress) {
                    device = jsonObject.getString("device");
                    stressLevel = jsonObject.getString("stressLevel");
                    stressValue = jsonObject.getInt("stressValue");
                }

                this.mHRV.result = FitrusLtResultData.RESULT.SUCCESS;
                this.mHRV.measureDate = date;
                this.mHRV.dSp02 = oxygen;
                this.mHRV.dBPM = bpm;
                this.mHRV.deviceName = this.getConnDeviceName(this.connectName);
                this.mHRV.firmwareVersion = Float.parseFloat(this.version);
                if (this.isStress) {
                    this.mStress.result = FitrusLtResultData.RESULT.SUCCESS;
                    this.mStress.measureDate = date;
                    this.mStress.dBPM = bpm;
                    this.mStress.dSp02 = oxygen;
                    this.mStress.StressLevel = stressLevel;
                    this.mStress.StressValue = stressValue;
                    this.mStress.deviceName = this.getConnDeviceName(this.connectName);
                    this.mStress.firmwareVersion = Float.parseFloat(this.version);
                }
            }
        } catch (Exception var18) {
            Exception e = var18;
            e.printStackTrace();
        }

    }

    private String getConnDeviceName(String connectName) {
        String device;
        switch (connectName) {
            case "Fitrus_A":
                device = "Fitrus A";
                break;
            case "FitrusLight":
            case "Fitrus":
                device = "Fitrus Light";
                break;
            case "FitrusPlus3":
                device = "Fitrus Plus";
                break;
            default:
                device = "Unknown Device Name";
        }

        return device;
    }

    static {
        strConnBase = isRealServer ? "http://52.188.66.123:8381/" : "http://210.104.190.226:8381/";
        mScanName = new ArraySet();
        mBluetoothMap = new ArrayMap();
    }

    public class LocalBinder extends Binder implements DeviceServiceBinder {
        public LocalBinder() {
        }

        public DeviceService getService() {
            DeviceService.this.Init();
            return DeviceService.this;
        }
    }
}

