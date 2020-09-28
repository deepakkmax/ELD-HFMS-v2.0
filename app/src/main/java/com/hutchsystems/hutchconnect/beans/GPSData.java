package com.hutchsystems.hutchconnect.beans;

/**
 * Created by Dev-1 on 7/20/2016.
 */
public class GPSData {
    public static int PostRoadSpeed = 0;
    public static long LastStatusTime = System.currentTimeMillis();
    // time in minutes
    public static int DrivingTimeRemaining;
    public static int WorkShiftRemaining;
    public static int OnDutyRemaining;
    public static int DrivingRemaining;
    public static int DrivingWithoutBreak;
    public static int TimeRemaining70;
    public static int TimeRemaining80;
    public static int TimeRemaining120;
    public static int TimeRemainingUS70;
    public static int TimeRemainingUS60;
    public static int CurrentStatusRemaining;
    public static int TimeRemainingReset;
    public static int TotalDistance=0;
    public static int OdometerSinceDriving=0;
    public static int ETATimeRemaining;
    public static int CurrentStatus = 1;
    public static String StatusCode = "1";
    public static int ACPowerFg;
    public static int TripInspectionCompletedFg;
    public static int NoHOSViolationFgFg = 1;
    public static int CellOnlineFg;

    public static int BTDataStatusFg;
    public static int BTEnabledFg;
    public static int RoamingFg;
    public static int DTCOnFg;
    public static int WifiOnFg;
    public static int TPMSWarningOnFg;
    public static int NetworkType;
}