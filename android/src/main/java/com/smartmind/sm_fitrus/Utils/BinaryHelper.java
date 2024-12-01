package com.smartmind.sm_fitrus.Utils;

public class BinaryHelper {
    public BinaryHelper() {
    }

    public static String ByteToHex(int bInt) {
        return String.format("%02x", bInt);
    }

    public static int HexToInteger(String hex) {
        return Integer.parseInt(hex, 16);
    }

    public static int b1Int(byte b) {
        return b & 255;
    }

    public static int b3Int(byte b1, byte b2, byte b3) {
        return (b1Int(b1) << 16) + (b1Int(b2) << 8) + b1Int(b3);
    }
}
