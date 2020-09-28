package com.hutchsystems.hutchconnect.common;

public class ConstantFlag {

    public static boolean PRODUCTION_BUILD = true;

    public static boolean AUTOSTART_MODE = true;

    public static boolean Flag_Log_CanBus = false;
    public static boolean Flag_Log_NoLogin = false;
    public static boolean Flag_Log_DriverEvent = false;
    public static boolean Flag_Log_AutoSyncTask = false;
    public static boolean Flag_Log_TCPClient = false;
    public static boolean Flag_Log_GPS = false;
    public static boolean Flag_Auto_Driving_Event = true;
    public static boolean Flag_DRIVING_EDIT = false;
    public static boolean FLAG_GAUGE_SHOW = true;
    public static boolean FLAG_BACKUP_DB = false;
    public static int FLAG_BTB_PORT = 0;

    // set false before going live
    public static boolean Flag_Development = false;
    // set true if vehicle support OBD2 protocol. usually for Light duty vehicle
    public static boolean OBD2FG = false;
    // set LiveFg false if you want to connect to ecompliance server else it will be connected to ELD server
    public static boolean LiveFg = true;
    public static boolean ELDFg = true;
    // set true if scan using dynmsoft sdk else scan using default functionality
    public static boolean Flag_Scan_Dynmsoft = false;
    public static boolean HutchConnectFg = true;
}