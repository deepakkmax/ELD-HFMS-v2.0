package com.hutchsystems.hutchconnect.common;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;

import com.hutchsystems.hutchconnect.beans.GPSData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class GPSTracker extends Service implements LocationListener {

    public static String gpsTime;

    public GPSTracker() {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    // Deepak Sharma
    // 21 July 2016
    // set status code for gps data
    private static void setStatusCode() {
        String code1 = Utility.convertBinaryToHex((GPSData.ACPowerFg == 0 ? "1" : "0") + "" + GPSData.TripInspectionCompletedFg + "" + (GPSData.NoHOSViolationFgFg == 1 ? "0" : "1") + "" + GPSData.CellOnlineFg);
        String code2 = Utility.convertBinaryToHex(GPSData.RoamingFg + "" + GPSData.DTCOnFg + "" + GPSData.WifiOnFg + "" + GPSData.TPMSWarningOnFg);
        String code3 = Utility.convertBinaryToHex((Utility.dataDiagnosticIndicatorFg ? "1" : "0") + (Utility.malFunctionIndicatorFg ? "1" : "0") + (Float.valueOf(CanMessages.RPM) == 0f ? "0" : "1") + "0");
        String code4 = "0";
        GPSData.StatusCode = code1 + code2 + code3 + code4;
    }

    private static String MilisecondsToDate(long ms) {

        final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date(ms));
        return utcTime;
    }

    private static String getUtcTime() {

        final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date());
        return utcTime;
    }

    public static String getShutDownEvent() {
        String signal = "";
        try {
            double gpsOdometerReading = 0;
            GPSData.CurrentStatus = 4;
            GPSData.LastStatusTime = System.currentTimeMillis();
            setStatusCode();
            double latitude = (Utility.currentLocation.getLatitude() < 0 ? -Utility.currentLocation.getLatitude() : Utility.currentLocation.getLatitude());
            double longitude = (Utility.currentLocation.getLongitude() < 0 ? -Utility.currentLocation.getLongitude() : Utility.currentLocation.getLongitude());

            signal = Utility.IMEI + "," +// IMEI
                    Utility._productCode + ":" + Utility.ApplicationVersion + "," + // Product Code
                    "1G," + // Message Type
                    getUtcTime() + "," +  // Signal Date
                    "A," + // GPS Status
                    latitude + "," +
                    (Utility.currentLocation.getLatitude() > 0 ? "N" : "S") + "," +
                    longitude + "," +
                    (Utility.currentLocation.getLongitude() > 0 ? "E" : "W") + "," +
                    CanMessages.Speed + "," + //can Speed
                    Utility.currentLocation.getBearing() + "," + // Heading
                    CanMessages.OdometerReading + "," + // Odometer Reading
                    Utility.currentLocation.getAccuracy() + "," + //accuracy
                    CanMessages.Speed + "," + // gps speed
                    GPSData.PostRoadSpeed + "," + // Post Road speed
                    CanMessages.EngineHours + "," +
                    CanMessages.TotalFuelConsumed + "," +
                    CanMessages.TotalIdleHours + "," +
                    CanMessages.TotalIdleFuelConsumed + "," +
                    CanMessages.TotalAverage + "," +
                    MilisecondsToDate(GPSData.LastStatusTime) + "," +
                    GPSData.ETATimeRemaining + "," +
                    GPSData.CurrentStatus + "," +
                    GPSData.CurrentStatusRemaining + "," +
                    Utility.activeUserId + "," +
                    Utility.vehicleId + "," +
                    GPSData.StatusCode + "," +
                    Utility.currentLocation.getAddress().replaceAll(",", " ") + "," +
                    Utility.currentLocation.getState() + "," +
                    Utility.currentLocation.getCity() + "," +
                    Utility.currentLocation.getCountry() + "," +
                    Utility.currentLocation.getFullAddress().replaceAll(",", " ") + "," +
                    gpsOdometerReading + "," +
                    Utility.TotalDeviceBytes + ";";


        } catch (Exception exe) {

        }
        return signal;
    }

    public static IGPS mListner;

    public interface IGPS {

        void promptToAppExit(String message, int minute);
    }
}