package com.smartmind.sm_fitrus.Results;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class FitrusLtResultData {
    public FitrusLtResultData() {
    }

    public static class Temperature implements Serializable {
        public RESULT result;
        public double measureDate;
        public String type;
        public double value;
        public String deviceName;
        public float firmwareVersion;

        public Temperature() {
        }

        @NonNull
        public String toString() {
            return "Device Name : " + this.deviceName + " (F/N) : " + this.firmwareVersion + "\nMeasure Type : " + this.type + "\nvalue : " + this.value + "\n";
        }
    }

    public static class BP implements Serializable {
        public RESULT result;
        public double measureDate;
        public double sbp;
        public double dbp;
        public String deviceName;
        public float firmwareVersion;

        public BP() {
        }

        @NonNull
        public String toString() {
            return "Device Name : " + this.deviceName + " (F/N) : " + this.firmwareVersion + "\nsbp : " + this.sbp + "\ndbp : " + this.dbp + "\n";
        }
    }

    public static class Stress implements Serializable {
        public RESULT result;
        public double measureDate;
        public int dSp02;
        public int dBPM;
        public int StressValue;
        public String StressLevel;
        public String deviceName;
        public float firmwareVersion;

        public Stress() {
        }

        @NonNull
        public String toString() {
            return "Device Name : " + this.deviceName + " (F/N) : " + this.firmwareVersion + "\nStressLevel : " + this.StressLevel + "\nBPM : " + this.dBPM + "\nOxygen : " + this.dSp02 + "\nstressValue : " + this.StressValue + "\n";
        }
    }

    public static class HRV implements Serializable {
        public RESULT result;
        public double measureDate;
        public int dSp02;
        public int dBPM;
        public String deviceName;
        public float firmwareVersion;

        public HRV() {
        }

        @NonNull
        public String toString() {
            return "Device Name : " + this.deviceName + " (F/N) : " + this.firmwareVersion + "\nOxygen : " + this.dSp02 + "\nBPM : " + this.dBPM + "\n";
        }
    }

    public static class DeviceInfo implements Serializable {
        public RESULT result;
        public float firmwareVersion;
        public int batteryLevel;
        public int bright;
        public int measureCheckTime;
        public int measureCycleCount;
        public int measureCycleDelay;
        public int measurePrecision;
        public int endTimeAfterMeasure;

        public DeviceInfo() {
        }

        @NonNull
        public String toString() {
            return "CheckTime : " + this.measureCheckTime + "\nTurnOffTime : " + this.endTimeAfterMeasure + "\nFirmVersion : " + this.firmwareVersion + "\nPrecision : " + this.measurePrecision + "\nCycleCount : " + this.measureCycleCount + "\nCycleDelay : " + this.measureCycleDelay + "\nBatteryLevel : " + this.batteryLevel + "\nBrightLevel : " + this.bright;
        }
    }

    public static class Body implements Serializable {
        public RESULT result;
        public double measureDate;
        public double fatPercentage;
        public double fatMass;
        public double muscleMass;
        public double bmi;
        public double bmr;
        public double waterPercentage;
        public double icw;
        public double ecw;
        public double protein;
        public double minerals;
        public double calorie;
        public String deviceName;
        public float firmwareVersion;

        public Body() {
        }

        @NonNull
        public String toString() {
            return "\nBFP : " + this.fatPercentage + "\nBFM : " + this.fatMass + "\nSMM : " + this.muscleMass + "\nBMI : " + this.bmi + "\nBMR : " + this.bmr + "\nBWP : " + this.waterPercentage;
        }
    }

    public static class Battery implements Serializable {
        public RESULT result;
        public int batteryLevel;

        public Battery() {
        }

        @NonNull
        public String toString() {
            return "BatteryLevel : " + this.batteryLevel;
        }
    }

    public static class Progress implements Serializable {
        public String strMeasureName;
        public int progressValue;
        public String deviceName;
        public float firmwareVersion;

        public Progress() {
        }

        @NonNull
        public String toString() {
            return "Device Name : " + this.deviceName + " (F/N) : " + this.firmwareVersion + "\nMeasure : " + this.strMeasureName + "\nProcessing : " + this.progressValue + " %";
        }
    }

    public static enum RESULT {
        BUSY,
        SUCCESS,
        ERROR_BATTERY,
        ERROR_ELECTRODE,
        ERROR_MOVEMENT,
        ERROR_UNKNOWN;

        private RESULT() {
        }
    }}
