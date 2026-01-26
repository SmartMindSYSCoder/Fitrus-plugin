package com.smartmind.sm_fitrus.Interfaces;


import android.content.IntentFilter;


public interface FitrusServiceInterface extends DeviceServiceInterface {
    int SCAN_STATE_WAIT = 0;
    int SCAN_STATE_SCAN = 1;
    int CONN_STATE_DISCONNECTED = 0;
    int CONN_STATE_CONNECTING = 1;
    int CONN_STATE_CONNECTED = 2;
    int ERROR_NONE = 0;
    int ERROR_NOT_INIT = -1;
    int ERROR_ALREAY_COMMAND_PROCESSING = -2;
    int ERROR_NO_FIRMWARE_INFO = -5;
    int ERROR_NOT_SUPPORT_DEVICE = -10;
    int ERROR_NOT_SUPPORT_FIRMWARE = -11;
    int ERROR_SET_VALUE_OUT_OF_RANGE = 1;
    int ERROR_SET_VALUE_NONE = 2;
    String ACTION_SCAN_SEARCHED = "ACTION_SCAN_SEARCHED";
    String ACTION_SCAN_FAILED = "ACTION_SCAN_FAILED";
    String ACTION_SCAN_COMPLETED = "ACTION_SCAN_COMPLETED";
    String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
    String EXTRA_DATA = "EXTRA_DATA";
    String EXTRA_TYPE = "EXTRA_TYPE";
    String EXTRA_TYPE_NONE = "NONE";
    String EXTRA_TYPE_DEVICE_INFO = "INFO";
    String EXTRA_TYPE_BATTERY = "BATT";
    String EXTRA_TYPE_HRV = "HRV";
    String EXTRA_TYPE_HRV_LOCAL = "HRV_L";
    String EXTRA_TYPE_BFP = "BFP";
    String EXTRA_TYPE_BFP_LOCAL = "BFP_L";
    String EXTRA_TYPE_CAL = "CALI";
    String EXTRA_TYPE_SET_VALUE = "SETV";
    String EXTRA_TYPE_SET_CHAR = "SETC";
    String EXTRA_TYPE_BFP_RAW = "RAWB";
    String EXTRA_TYPE_HRV_RAW = "RAWH";
    String EXTRA_TYPE_CAL_RAW = "RAWC";
    String EXTRA_TYPE_CAL_READ_YN = "READ_CALI_YN";
    String EXTRA_TYPE_CAL_READ_VALUE = "READ_CALI_V";
    String EXTRA_TYPE_ERROR = "ERROR";
    String EXTRA_TYPE_STRESS = "STRESS";
    String EXTRA_TYPE_OBJECT_TEMP = "TEMP_O";
    String EXTRA_TYPE_SKIN_TEMP = "TEMP_S";
    String EXTRA_TYPE_BP = "BP";
    String EXTRA_TYPE_MEASURE_PROGRESS = "PROGRESS";
    int SEND_SET_COMMAND_OK = 0;
    int SEND_SET_PARAM_RANGE_ERROR = 1;
    int SEND_SET_PARAM_NULL = 2;

    // API Configuration
    void setApiConfig(String apiUrl, String apiKey);

    IntentFilter getGattUpdateIntentFilter();

    boolean startFitrusScan(int var1, int var2);

    boolean stopFitrusScan();

    boolean connectFitrus(String var1, String var2);

    void disconnectFitrus();

    void closeFitrus();

    int getScanState();

    String getFitrusAddress();

    String getFitrusName();

    int startBFP(String var1, double var2, double var4, String var6, String var7, String var8);

    void bfpLocalMeasureStart();

    void stopBFP();

    void sendBFPResult(double var1);

    void spo2MeasureStart(String var1);

    void spo2LocalMeasureStart();

    void spo2MeasureStop();

    int startHR(String var1);

    void stopHR();

    int startStress(String var1);

    void stopStress();

    int startObjectTemp(String var1);

    void stopObjectTemp();

    int startSkinTemp(String var1);

    void stopSkinTemp();

    int startBP(String var1, double var2, double var4);

    void stopBP();

    void getDeviceInfo();

    void getBatteryLevel();

    void calModeStart();

    void calModeStop();

    int setDeviceBrightLevel(int var1);

    int setDeviceMeasureCheckTime(int var1);

    int setDeviceBFPMeasureCycleCount(int var1);

    int setDeviceBFPMeasureCycleDelay(int var1);

    int setDeviceBFPMeasurePrecision(int var1);

    int setDeviceSerialNumber(String var1);

    int setDeviceSoftwareRevision(String var1);

    int setEndTimeAfterMeasure(int var1);

    void setPPGMeasureTime(int var1);

    int readCalibrationYn();

    int readCalibrationValue();
}

