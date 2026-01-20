package com.smartmind.sm_fitrus;

import java.util.UUID;

public class FitrusConstants {
    // UUIDs
    public static final String SERVICE_UUID_STRING = "00000001-0000-1100-8000-00805f9b34fb";
    public static final UUID SERVICE_UUID = UUID.fromString(SERVICE_UUID_STRING);

    // Commands
    public static final String CMD_BFP_START = "*BFP:Start#\r\n";
    public static final String CMD_BFP_STOP = "*BFP:Stop#\r\n";
    public static final String CMD_SPO2_START = "*SpO2:Start#\r\n";
    public static final String CMD_SPO2_STOP = "*SpO2:Stop#\r\n";
    public static final String CMD_STRESS_START = "*Stress:Start#\r\n";
    public static final String CMD_STRESS_STOP = "*Stress:Stop#\r\n";
    public static final String CMD_TEMP_START = "*Temp:Start#\r\n";
    public static final String CMD_TEMP_STOP = "*Temp:Stop#\r\n";
    public static final String CMD_TEMP_BODY_START = "*Temp.Body:Start#\r\n";
    public static final String CMD_TEMP_BODY_STOP = "*Temp.Body:Stop#\r\n";
    public static final String CMD_PRESS_START = "*Press:Start#\r\n";
    public static final String CMD_PRESS_STOP = "*Press:Stop#\r\n";
    public static final String CMD_BATT_READ = "*Dev.Info:Batt.Read#\r\n";
    public static final String CMD_CALMODE_START = "*Calmode:Start#\r\n";
    public static final String CMD_CALMODE_STOP = "*Calmode:Stop#\r\n";
    public static final String CMD_CALI_READ = "*Dev.Info:calibration.Read#\r\n";
    public static final String CMD_CALI_VALUE_READ = "*Dev.Info:calibration_value.Read#\r\n";

    // Intent Actions
    public static final String ACTION_SCAN_SEARCHED = "ACTION_SCAN_SEARCHED";
    public static final String ACTION_SCAN_FAILED = "ACTION_SCAN_FAILED";
    public static final String ACTION_SCAN_COMPLETED = "ACTION_SCAN_COMPLETED";
    public static final String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";

    // Intent Extras
    public static final String EXTRA_DATA = "EXTRA_DATA";
    public static final String EXTRA_TYPE = "EXTRA_TYPE";
    
    // Extra Types (Values match Interface where possible or standard conventions)
    public static final String EXTRA_TYPE_MEASURE_PROGRESS = "PROGRESS";
    public static final String EXTRA_TYPE_BFP = "BFP";
    public static final String EXTRA_TYPE_HRV = "HRV";
    public static final String EXTRA_TYPE_BATTERY = "BATT";
    public static final String EXTRA_TYPE_STRESS = "STRESS";
    public static final String EXTRA_TYPE_SKIN_TEMP = "TEMP_S";
    public static final String EXTRA_TYPE_OBJECT_TEMP = "TEMP_O";
    public static final String EXTRA_TYPE_BP = "BP";
    public static final String EXTRA_TYPE_ERROR = "ERROR";
    public static final String EXTRA_TYPE_DEVICE_INFO = "INFO";
    public static final String EXTRA_TYPE_BFP_RAW = "RAWB";
    public static final String EXTRA_TYPE_CAL_RAW = "RAWC";
    public static final String EXTRA_TYPE_CAL_READ_YN = "READ_CALI_YN";
    public static final String EXTRA_TYPE_CAL_READ_VALUE = "READ_CALI_V";
    public static final String EXTRA_TYPE_SET_VALUE = "SETV";
    public static final String EXTRA_TYPE_SET_CHAR = "SETC";

    // Device Names
    public static final String DEVICE_FITRUS = "Fitrus";
    public static final String DEVICE_FITRUS_A = "Fitrus_A";
    public static final String DEVICE_FITRUS_LIGHT = "FitrusLight";
    public static final String DEVICE_FITRUS_PLUS3 = "FitrusPlus3";

    // Command Types (Internal State)
    public static final String TYPE_NONE = "NONE";
    public static final String TYPE_BFP = "BFP";
    public static final String TYPE_BFP_L = "BFP_L";
    public static final String TYPE_HRV = "HRV";
    public static final String TYPE_HRV_L = "HRV_L";
    public static final String TYPE_STRESS = "STRESS";
    public static final String TYPE_TEMP_O = "TEMP_O";
    public static final String TYPE_TEMP_S = "TEMP_S";
    public static final String TYPE_BP = "BP";
    public static final String TYPE_BATT = "BATT";
    public static final String TYPE_CALI = "CALI";
    public static final String TYPE_READ_CALI_YN = "READ_CALI_YN";
    public static final String TYPE_READ_CALI_V = "READ_CALI_V";
    public static final String TYPE_SETV = "SETV";
    public static final String TYPE_SETC = "SETC";
}
