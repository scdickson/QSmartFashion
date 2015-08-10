package com.qualcomm.qsmartfashion;

import java.util.UUID;

/**
 * Created by sdickson on 7/26/15.
 */
public class Constants
{
    //Conversion constants
    public static final double KILOGRAMS_PER_POUND = 0.45359237;
    public static final int INCHES_PER_FOOT = 12;
    public static final double CENTIMETERS_PER_INCH = 2.54;

    //Parse keys
    public static final String PARSE_APPLICATION_KEY = APP_KEY;
    public static final String PARSE_CLIENT_KEY = CLIENT_KEY;

    //BLE Constants
    public static final int BLE_CUTOFF_RSSI = -100;
    public static final UUID QSF_SERVICE = UUID.fromString("713D0000-503E-4C75-BA94-3148F18D941E");
    public static final UUID QSF_DEVICE_RX_UUID = UUID.fromString("713D0002-503E-4C75-BA94-3148F18D941E");
    public static final UUID QSF_DEVICE_TX_UUID = UUID.fromString("713D0003-503E-4C75-BA94-3148F18D941E");
    public static final UUID QSF_DEVICE_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
}
