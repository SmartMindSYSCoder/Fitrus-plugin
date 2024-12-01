package com.smartmind.sm_fitrus.Attributes;


import java.util.Arrays;

public class FitrusAttributes {
    public static final int ERROR_NONE = 0;
    public static final int ERROR_NOT_INIT = -1;
    public static final int ERROR_COMMAND_PROCESSING = -2;
    public static final int ERROR_NO_FIRMWARE_INFO = -5;
    public static final int ERROR_NOT_SUPPORT_DEVICE = -10;
    public static final int ERROR_NOT_SUPPORT_FIRMWARE = -11;
    public static final int ERROR_SET_VALUE_OUT_OF_RANGE = 1;
    public static final int ERROR_SET_VALUE_NONE = 2;
    public static final String LT_OLD_NAME = "Fitrus";
    public static final String LT_NEW_NAME = "FitrusLight";
    public static final String A_NAME = "Fitrus_A";
    public static final String NEO_NAME = "FitrusNeo";
    public static final String PLUS_NAME = "FitrusPlus3";
    public static final String HC_Service = "0000FE00-EBAE-4526-9511-8357c35d7be2";
    public static final String HR_Service = "0000180D-0000-1000-8000-00805F9B34FB";
    public static final String BC_Service = "0000181B-0000-1000-8000-00805F9B34FB";
    public static final String SERVICE = "00000001-0000-1100-8000-00805f9b34fb";
    public static final String READ = "00000003-0000-1100-8000-00805f9b34fb";
    public static final String WRITE = "00000002-0000-1100-8000-00805f9b34fb";
    public static final String BFP_START = "*BFP:Start#\r\n";
    public static final String BFP_STOP = "*BFP:Stop#\r\n";
    public static final String SPO2_START = "*SpO2:Start#\r\n";
    public static final String SPO2_STOP = "*SpO2:Stop#\r\n";
    public static final String DEV_READ = "*Dev.Info:Read#\r\n";
    public static final String DEV_BATT = "*Dev.Info:Batt.Read#\r\n";
    public static final String CALMODE_START = "*Calmode:Start#\r\n";
    public static final String CALMODE_STOP = "*Calmode:Stop#\r\n";
    public static final String TEMP_O_START = "*Temp:Start#\r\n";
    public static final String TEMP_O_STOP = "*Temp:Stop#\r\n";
    public static final String TEMP_S_START = "*Temp.Body:Start#\r\n";
    public static final String TEMP_S_STOP = "*Temp.Body:Stop#\r\n";
    public static final String STRESS_START = "*Stress:Start#\r\n";
    public static final String STRESS_STOP = "*Stress:Stop#\r\n";
    public static final String BP_START = "*Press:Start#\r\n";
    public static final String BP_STOP = "*Press:Stop#\r\n";
    public static final String DEV_SOFT_READ = "*Dev.Info:SerialNum.Read#\r\n";
    public static final String DEV_SOFT_RIV = "*Dev.Info:SoftwareRev.Read#\r\n";
    public static final String RESULT_BFP = "*BFP:Result=%1$.1f#\r\n";
    public static final String RESULT_SPO2 = "*PPG:Result=%1$d,%2$d#\r\n";
    public static final String RESULT_STRESS_FL = "*Stress:Result=%1$d,%2$d#\r\n";
    public static final String RESULT_HR_SPO2 = "*PPG:Result=%1$d,%2$d#\r\n";
    public static final String RESULT_O_TEMP = "*Temp:Result=%.2f#\r\n";
    public static final String RESULT_S_TEMP = "*Temp.Body:Result=%.2f#\r\n";
    public static final String RESULT_STRESS_FP = "*Stress:Result=%S#\r\n";
    public static final String RESULT_BP = "*Press:Result=%1$d,%2$d#\r\n";
    public static final String SET_BRIGHT = "*Dev.Info:Set.Bright=%1$d#\r\n";
    public static final String SET_SPO2_TIME = "*Dev.Info:Set.SpO2.MeasurTime=%1$d#\r\n";
    public static final String SET_PRV_FORMAT = "*Dev.Info:Set.PRV.Format=%1$d#\r\n";
    public static final String SET_BFP_TIME = "*Dev.Info:Set.BFP.MeasurChk.Time=%1$d#\r\n";
    public static final String SET_BFP_COUNT = "*Dev.Info:Set.BFP.MeasurCycle.Count=%1$d#\r\n";
    public static final String SET_BFP_DELAY = "*Dev.Info:Set.BFP.MeasurCycle.Delay=%1$d#\r\n";
    public static final String SET_BFP_PRECISION = "*Dev.Info:Set.BFP.MeasurPre=%1$d#\n";
    public static final String SET_SERIAL_NUMBER = "*Dev.Info:Set.SerialNum=%10s#\r\n";
    public static final String SET_SW_REVISION = "*Dev.Info:Set.SoftwareRev=%s#\r\n";
    public static final String SET_TIME_AS = "*Dev.Info:Set.Time.AS=%1$d#\r\n";
    public static final String READ_CALI_YN = "*Dev.Info:calibration.Read#\r\n";
    public static final String READ_CALI_VALUE = "*Dev.Info:calibration_value.Read#\r\n";
    public static final String REQUEST_NONE = "NONE";
    public static final String REQUEST_DEVICE_INFO = "INFO";
    public static final String REQUEST_BATTERY = "BATT";
    public static final String REQUEST_HRV = "HRV";
    public static final String REQUEST_HRV_LOCAL = "HRV_L";
    public static final String REQUEST_BFP = "BFP";
    public static final String REQUEST_BFP_LOCAL = "BFP_L";
    public static final String REQUEST_SET_VALUE = "SETV";
    public static final String REQUEST_SET_CHAR = "SETC";
    public static final String REQUEST_CALIMODE = "CALI";
    public static final String REQUEST_BFP_RAW = "RAWB";
    public static final String REQUEST_HRV_RAW = "RAWH";
    public static final String REQUEST_CAL_RAW = "RAWC";
    public static final String REQUEST_CAL_READ_YN = "READ_CALI_YN";
    public static final String REQUEST_CAL_READ_V = "READ_CALI_V";
    public static final String REQUEST_TEMP_OBJECT = "TEMP_O";
    public static final String REQUEST_TEMP_SKIN = "TEMP_S";
    public static final String REQUEST_STRESS = "STRESS";
    public static final String REQUEST_BP = "BP";
    public static final String MEASURE_PROGRESS = "PROGRESS";
    public static final String RESULT_ERROR = "ERROR";

    public FitrusAttributes() {
    }

    public static String getCommend(String cmd, int val) {
        return String.format(cmd, val);
    }

    public static Data convertByteToData(byte[] result) {
        return new Data(result);
    }

    public static String getConfigChar(String service) {
        return "00000002-0000-1100-8000-00805f9b34fb";
    }

    public static String getDataChar(String service) {
        return "00000003-0000-1100-8000-00805f9b34fb";
    }

    public static class Data {
        public String category;
        public String type;
        public String value;

        public Data(byte[] bytes) {
            byte[] buffer = new byte[bytes.length];
            int bufferLength = 0;
            boolean isValue = false;

            try {
                label32:
                for(int i = 0; i < bytes.length; ++i) {
                    switch (bytes[i]) {
                        case 0:
                            break label32;
                        case 10:
                        case 13:
                            break;
                        case 58:
                            this.category = new String(Arrays.copyOf(buffer, bufferLength));
                            buffer = new byte[bytes.length - i];
                            bufferLength = 0;
                            break;
                        case 61:
                            isValue = true;
                            this.type = new String(Arrays.copyOf(buffer, bufferLength));
                            buffer = new byte[bytes.length - i];
                            bufferLength = 0;
                            break;
                        default:
                            buffer[bufferLength] = bytes[i];
                            ++bufferLength;
                    }
                }

                if (isValue) {
                    this.value = new String(Arrays.copyOf(buffer, bufferLength));
                } else {
                    this.type = new String(Arrays.copyOf(buffer, bufferLength));
                }
            } catch (Exception var6) {
                Exception e = var6;
                e.printStackTrace();
            }

        }
    }
}

