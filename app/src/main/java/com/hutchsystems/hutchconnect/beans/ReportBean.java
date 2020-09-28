package com.hutchsystems.hutchconnect.beans;

import android.content.Context;

import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.CustomDateFormat;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.ReportDB;

import java.util.Date;

public class ReportBean {

    Context context;
    ReportDB objDb;

    public ReportBean(Context context) {
        this.context = context;
        objDb = new ReportDB(this.context);
    }

    // MILLION
    private int CONSTANT_MILION = 10000000;

    int length, reportId, gpsStatus, rpm, GSMStatus, TotalSatellite, vehicleId, driverId, coDriverId, specialStatus;
    int eventType, eventCode;
    long sequenceId;
    String prefix, unitId, serialNo, checkSum, eventCodeDescription, reportDate;
    double longitude, latitude, Altitude, heading, gpsOdometerReading, gpsSpeed, speed, totalEngineHour, odometerReading, fuelLevel, totalFuelUsed, coolantTemperature;
    double GSMSignalStrength, GPSStrength, DeviceBatteryLevel, VehicleBatteryLevel, TotalIdleEngineHours, TotalIdleFuelUsed;
    boolean BTConnectFg, HistoryLogFg, ServerConnectFg, RoamingFg, EnginePowerUpFg, EngineIdlingFg, GSMJammingFg, CheckEngineFg, OBD2StatusFg;

    Date gpsDate, rtcDate, positionSendingDate;

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public int getSpecialStatus() {
        return specialStatus;
    }

    public void setSpecialStatus(int specialStatus) {
        this.specialStatus = specialStatus;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public int getEventCode() {
        return eventCode;
    }

    public void setEventCode(int eventCode) {
        this.eventCode = eventCode;
    }

    public String getEventCodeDescription() {
        return eventCodeDescription;
    }

    public void setEventCodeDescription(String eventCodeDescription) {
        this.eventCodeDescription = eventCodeDescription;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public int getCoDriverId() {
        return coDriverId;
    }

    public void setCoDriverId(int coDriverId) {
        this.coDriverId = coDriverId;
    }

    public double getTotalIdleEngineHours() {
        return TotalIdleEngineHours;
    }

    public void setTotalIdleEngineHours(double totalIdleEngineHours) {
        TotalIdleEngineHours = totalIdleEngineHours;
    }

    public double getTotalIdleFuelUsed() {
        return TotalIdleFuelUsed;
    }

    public void setTotalIdleFuelUsed(double totalIdleFuelUsed) {
        TotalIdleFuelUsed = totalIdleFuelUsed;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getGSMStatus() {
        return GSMStatus;
    }

    public void setGSMStatus(int GSMStatus) {
        this.GSMStatus = GSMStatus;
    }

    public int getTotalSatellite() {
        return TotalSatellite;
    }

    public void setTotalSatellite(int totalSatellite) {
        TotalSatellite = totalSatellite;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public double getAltitude() {
        return Altitude;
    }

    public void setAltitude(double altitude) {
        Altitude = altitude;
    }

    public double getGSMSignalStrength() {
        return GSMSignalStrength;
    }

    public void setGSMSignalStrength(double GSMSignalStrength) {
        this.GSMSignalStrength = GSMSignalStrength;
    }

    public double getGPSStrength() {
        return GPSStrength;
    }

    public void setGPSStrength(double GPSStrength) {
        this.GPSStrength = GPSStrength;
    }

    public double getDeviceBatteryLevel() {
        return DeviceBatteryLevel;
    }

    public void setDeviceBatteryLevel(double deviceBatteryLevel) {
        DeviceBatteryLevel = deviceBatteryLevel;
    }

    public double getVehicleBatteryLevel() {
        return VehicleBatteryLevel;
    }

    public void setVehicleBatteryLevel(double vehicleBatteryLevel) {
        VehicleBatteryLevel = vehicleBatteryLevel;
    }

    public boolean isBTConnectFg() {
        return BTConnectFg;
    }

    public void setBTConnectFg(boolean BTConnectFg) {
        this.BTConnectFg = BTConnectFg;
    }

    public boolean isHistoryLogFg() {
        return HistoryLogFg;
    }

    public void setHistoryLogFg(boolean historyLogFg) {
        HistoryLogFg = historyLogFg;
    }

    public boolean isServerConnectFg() {
        return ServerConnectFg;
    }

    public void setServerConnectFg(boolean serverConnectFg) {
        ServerConnectFg = serverConnectFg;
    }

    public boolean isRoamingFg() {
        return RoamingFg;
    }

    public void setRoamingFg(boolean roamingFg) {
        RoamingFg = roamingFg;
    }

    public boolean isEnginePowerUpFg() {
        return EnginePowerUpFg;
    }

    public void setEnginePowerUpFg(boolean enginePowerUpFg) {
        EnginePowerUpFg = enginePowerUpFg;
    }

    public boolean isEngineIdlingFg() {
        return EngineIdlingFg;
    }

    public void setEngineIdlingFg(boolean engineIdlingFg) {
        EngineIdlingFg = engineIdlingFg;
    }

    public boolean isGSMJammingFg() {
        return GSMJammingFg;
    }

    public void setGSMJammingFg(boolean GSMJammingFg) {
        this.GSMJammingFg = GSMJammingFg;
    }

    public boolean isCheckEngineFg() {
        return CheckEngineFg;
    }

    public void setCheckEngineFg(boolean checkEngineFg) {
        CheckEngineFg = checkEngineFg;
    }

    public boolean isOBD2StatusFg() {
        return OBD2StatusFg;
    }

    public void setOBD2StatusFg(boolean OBD2StatusFg) {
        this.OBD2StatusFg = OBD2StatusFg;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    public int getGpsStatus() {
        return gpsStatus;
    }

    public void setGpsStatus(int gpsStatus) {
        this.gpsStatus = gpsStatus;
    }

    public int getRpm() {
        return rpm;
    }

    public void setRpm(int rpm) {
        this.rpm = rpm;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public Date getGpsDate() {
        return gpsDate;
    }

    public void setGpsDate(Date gpsDate) {
        this.gpsDate = gpsDate;
    }

    public Date getRtcDate() {
        return rtcDate;
    }

    public void setRtcDate(Date rtcDate) {
        this.rtcDate = rtcDate;
    }

    public Date getPositionSendingDate() {
        return positionSendingDate;
    }

    public void setPositionSendingDate(Date positionSendingDate) {
        this.positionSendingDate = positionSendingDate;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public double getGpsOdometerReading() {
        return gpsOdometerReading;
    }

    public void setGpsOdometerReading(double gpsOdometerReading) {
        this.gpsOdometerReading = gpsOdometerReading;
    }

    public double getGpsSpeed() {
        return gpsSpeed;
    }

    public void setGpsSpeed(double gpsSpeed) {
        this.gpsSpeed = gpsSpeed;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getTotalEngineHour() {
        return totalEngineHour;
    }

    public void setTotalEngineHour(double totalEngineHour) {
        this.totalEngineHour = totalEngineHour;
    }

    public double getOdometerReading() {
        return odometerReading;
    }

    public void setOdometerReading(double odometerReading) {
        this.odometerReading = odometerReading;
    }

    public double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(double fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public double getTotalFuelUsed() {
        return totalFuelUsed;
    }

    public void setTotalFuelUsed(double totalFuelUsed) {
        this.totalFuelUsed = totalFuelUsed;
    }

    public double getCoolantTemperature() {
        return coolantTemperature;
    }

    public void setCoolantTemperature(double coolantTemperature) {
        this.coolantTemperature = coolantTemperature;
    }


    // Created By: Deepak Sharma
    // Created Date: 11 Feb 2020
    // Purpose: parse data from OBD2 protocol in case of light duty vehicle
    // AT$FORM=0,@O,1,"%ME%GQ%BT%AT%BV%DT%GS%MV%RL%SA%CS%CR%EG%DL%SF%JD%ML%OC%RP%VS%EH%OD%FL%FC%ET"
    // Sample Data: @O,DA02,192,68,355154088024746,20200210100442,20200210100442,20200210100442,76739708,30677926,225,110,73,6,0,0,3,0,,2000,2000,,355154088024746,99,1,318,38,0,1,130,0,18,0,255,0,0,1,0,0,0,0,0,1,73,0,0,0
    public ReportBean obd2Get(String[] data) {

        try {
            this.prefix = data[0];
            this.checkSum = data[1];
            this.length = Integer.parseInt(data[2]);
            this.sequenceId = Long.parseLong(data[3]);

            // we will save vehicle id,driver id and co driver id in this field
            // first seven digits for vehicle id, next 6 for driveri d, next 6 for co driver id and next 1 from special status (0,1,2,3) i.e PU,YM,WT(Waiting Time),Clear Special status
            // vehicle id starts from 1000000, driver id starts from 100000, co driver id from 100000
            this.unitId = data[4];

            // pick first 7 digit from unit id
            // convert it to int and subtract 1000000 to get vehicle id
            if (this.unitId.length() >= 7) {
                this.vehicleId = Integer.parseInt(this.unitId.substring(0, 7)) - 1000000;
            }

            // pick 8th to 13th digit from unit id
            // convert it to int and subtract 100000 to get driver id
            if (this.unitId.length() >= 13) {

                this.driverId = Integer.parseInt(this.unitId.substring(7, 13)) - 100000;
            }

            // pick 14th to 20th digit from unit id
            // convert it to int and subtract 100000 to get co driver id
            if (this.unitId.length() >= 19) {

                this.coDriverId = Integer.parseInt(this.unitId.substring(13, 19)) - 100000;
            }

            // pick 14th to 20th digit from unit id
            // convert it to int
            if (this.unitId.length() >= 20) {

                this.specialStatus = Integer.parseInt(this.unitId.substring(19, 20));
            }


            // below dates are in utc
            this.gpsDate = Utility.parse(data[5], CustomDateFormat.dt7);
            this.rtcDate = Utility.parse(data[6], CustomDateFormat.dt7);
            // get utc real time to time in driver timezone
            this.reportDate = Utility.convertUTCtoDriverTime(data[6]);
            this.positionSendingDate = Utility.parse(data[7], CustomDateFormat.dt7);

            // Longitude and latitude comes in multiple of 1 Million so divide by 1 Million
            this.longitude = Double.parseDouble(data[8]) * 0.000001f;
            this.latitude = Double.parseDouble(data[9]) * 0.000001f;
            this.heading = Double.parseDouble(data[10]);

            // It helps in identify type of data. we get multiple types of data from hutch connect device
            // e.g Location Records, Reports for engine up/down, Bluetooth connect/disconnect report and so on
            // each type of data has different report id
            this.reportId = Integer.parseInt(data[11]);

            // Gps odometer reading comes in multiple of 10 so divide it by 10 to get actual odometer reading
            // Odometer reading would be always in KMs
            this.gpsOdometerReading = Double.parseDouble(data[12]) / 10;

            // this would be in Km/h
            this.gpsSpeed = Double.parseDouble(data[15]);

            // GPS Default Data would be till index 15, then at index 16 there would be separator
            // From Index 22 to 40 we get additional data with gps "%ME%GQ%BT%AT%BV%DT%GS%MV%RL%SA%CS%CR%EG%DL%SF%JD%ML%OC%RP%VS%EH%OD%FL%FC%ET"

            // %ME
            this.serialNo = data[22];

            // %GQ
            this.GSMSignalStrength = Double.parseDouble(data[23]);

            // %BT, Bluetooth connected/disconnected. if 1 then bluetooth is connected
            this.BTConnectFg = data[24].equals("1");

            // %AT
            this.Altitude = Double.parseDouble(data[25]);

            // %BV
            this.DeviceBatteryLevel = Double.parseDouble(data[26]) * .1f;

            // %DT, if 1 means history log and 0 means realtime data
            this.HistoryLogFg = data[27].equals("1");

            // %GS, GSM Status
            this.GSMStatus = Integer.parseInt(data[28]);

            // %MV
            this.VehicleBatteryLevel = Double.parseDouble(data[29]) * .1f;

            // %RL, Current RXLev of GSM Module or GPS Module
            this.GPSStrength = Double.parseDouble(data[30]);

            // %SA
            this.TotalSatellite = Integer.parseInt(data[31]);

            // %CS. indicates if device is connected to hutch server
            this.ServerConnectFg = data[32].equals("1");

            // %CR, Roaming Status
            this.RoamingFg = data[33].equals("0");

            // %EG  Vehicle Engine Status PowerUp/Down
            this.EnginePowerUpFg = data[34].equals("1");

            // %DL  Idle event Status, Engine Idling
            this.EngineIdlingFg = data[35].equals("1");

            // %SF  GPS Fix Status
            this.gpsStatus = Integer.parseInt(data[36]);

            // %JD  GSM Jammming
            this.GSMJammingFg = data[37].equals("1");

            // %ML  Check Engine
            this.CheckEngineFg = data[38].equals("1");

            //% OC  OBD2 Status
            this.OBD2StatusFg = data[39].equals("1");

            // %RP, Engine speed
            this.rpm = Double.valueOf(data[40]).intValue();

            // %VS, Wheel based speed
            this.speed = Double.parseDouble(data[41]);

            //%EH, resolution .5
            this.totalEngineHour = Double.parseDouble(data[42]) * .1f;

            //%OD
            this.odometerReading = Double.parseDouble(data[43]) * .1f;

            // %FL, resolution is .4
            this.fuelLevel = Double.parseDouble(data[44]) * .4f;

            //%FC
            this.totalFuelUsed = Double.parseDouble(data[45]) * .1f;

            // %ET, enginecoolant
            this.coolantTemperature = Double.parseDouble(data[46]);
        } catch (Exception exe) {

        }
        return this;
    }

    // Created By: Deepak Sharma
    // Created Date: 1 April 2019
    // Purpose: parse data from J1939 protocol in case of light duty vehicle
    /// Sample Command GPS: AT$FORM=0,@J,1,"%ME%GQ%BT%AT%BV%DT%GS%MV%RL%SA%CS%CR%EG%DL%SF%JD%ML%OC" upto 39
    /// Sample Command J1939 Data: AT$FMSC=1,"%JO5%JO11%JH1%JH2%JL2%JL3%JL1%JN1%JN2%JN3"
    // Sample Data: @J,92C4,208,438,352009110544090,20200211044704,20200211044704,20200211044704,-122612790,49134769,222,106,4662,7,0,0,2,0,,2000,2000,,352009110544090,22,0,9,36,0,10,0,41,12,1,1,0,0,1,0,0,0,0,0,0,0,279688,856635,0,0,0,0
    public ReportBean j1939Get(String[] data) {

        try {
            this.prefix = data[0];
            this.checkSum = data[1];
            this.length = Integer.parseInt(data[2]);
            this.sequenceId = Long.parseLong(data[3]);


            // we will save vehicle id,driver id and co driver id in this field
            // first seven digits for vehicle id, next 6 for driveri d, next 6 for co driver id and next 1 from special status (0,1,2,3) i.e PU,YM,WT(Waiting Time),Clear Special status
            // vehicle id starts from 1000000, driver id starts from 100000, co driver id from 100000
            this.unitId = data[4];

            // pick first 7 digit from unit id
            // convert it to int and subtract 1000000 to get vehicle id
            if (this.unitId.length() >= 7) {
                this.vehicleId = Integer.parseInt(this.unitId.substring(0, 7)) - 1000000;
            }

            // pick 8th to 13th digit from unit id
            // convert it to int and subtract 100000 to get driver id
            if (this.unitId.length() >= 13) {

                this.driverId = Integer.parseInt(this.unitId.substring(7, 13)) - 100000;
            }

            // pick 14th to 20th digit from unit id
            // convert it to int and subtract 100000 to get co driver id
            if (this.unitId.length() >= 19) {

                this.coDriverId = Integer.parseInt(this.unitId.substring(13, 19)) - 100000;
            }

            // pick 14th to 20th digit from unit id
            // convert it to int
            if (this.unitId.length() >= 20) {

                this.specialStatus = Integer.parseInt(this.unitId.substring(19, 20));
            }


            // below dates are in utc
            this.gpsDate = Utility.parse(data[5], CustomDateFormat.dt7);
            this.rtcDate = Utility.parse(data[6], CustomDateFormat.dt7);

            // get utc real time to time in driver timezone
            this.reportDate = Utility.convertUTCtoDriverTime(data[6]);

            this.positionSendingDate = Utility.parse(data[7], CustomDateFormat.dt7);

            // Longitude and latitude comes in multiple of 1 Million so divide by 1 Million
            this.longitude = Double.parseDouble(data[8]) * 0.000001f;
            this.latitude = Double.parseDouble(data[9]) * 0.000001f;
            this.heading = Double.parseDouble(data[10]);

            // It helps in identify type of data. we get multiple types of data from hutch connect device
            // e.g Location Records, Reports for engine up/down, Bluetooth connect/disconnect report and so on
            // each type of data has different report id
            this.reportId = Integer.parseInt(data[11]);

            // Gps odometer reading comes in multiple of 10 so divide it by 10 to get actual odometer reading
            // Odometer reading would be always in KMs
            this.gpsOdometerReading = Double.parseDouble(data[12]) / 10;

            // this would be in Km/h
            this.gpsSpeed = Double.parseDouble(data[15]);

            // GPS Default Data would be till index 15, then at index 16 there would be separator
            // From Index 22 to 40 we get additional data with gps "%ME%GQ%BT%AT%BV%DT%GS%MV%RL%SA%CS%CR%EG%DL%SF%JD%ML%OC%RP%VS%EH%OD%FL%FC%ET"

            // %ME
            this.serialNo = data[22];

            // %GQ
            this.GSMSignalStrength = Double.parseDouble(data[23]);

            // %BT, Bluetooth connected/disconnected. if 1 then bluetooth is connected
            this.BTConnectFg = data[24].equals("1");

            // %AT
            this.Altitude = Double.parseDouble(data[25]);

            // %BV
            this.DeviceBatteryLevel = Double.parseDouble(data[26]) * .1f;

            // %DT, if 1 means history log and 0 means realtime data
            this.HistoryLogFg = data[27].equals("1");

            // %GS, GSM Status
            this.GSMStatus = Integer.parseInt(data[28]);

            // %MV
            this.VehicleBatteryLevel = Double.parseDouble(data[29]) * .1f;

            // %RL, Current RXLev of GSM Module or GPS Module
            this.GPSStrength = Double.parseDouble(data[30]);

            // %SA
            this.TotalSatellite = Integer.parseInt(data[31]);

            // %CS. indicates if device is connected to hutch server
            this.ServerConnectFg = data[32].equals("1");

            // %CR, Roaming Status
            this.RoamingFg = data[33].equals("0");

            // %EG  Vehicle Engine Status PowerUp/Down
            this.EnginePowerUpFg = data[34].equals("1");

            // %DL  Idle event Status, Engine Idling
            this.EngineIdlingFg = data[35].equals("1");

            // %SF  GPS Fix Status
            this.gpsStatus = Integer.parseInt(data[36]);

            // %JD  GSM Jammming
            this.GSMJammingFg = data[37].equals("1");

            // %ML  Check Engine
            this.CheckEngineFg = data[38].equals("1");

            //% OC  OBD2 Status
            this.OBD2StatusFg = data[39].equals("1");

            // %JO5, resolution is .4
            this.fuelLevel = Double.parseDouble(data[40]) * .4f;

            // %JO11, enginecoolant, 40 degree celcius offset
            this.coolantTemperature = Double.parseDouble(data[41]) - 40;

            // %JH1, Wheel based speed
            this.speed = Double.parseDouble(data[42]) / 256;

            // copy from gps speed if garbage value
            if (this.speed >= 200) {
                this.speed = this.gpsSpeed;
            }

            // %JH2, Engine speed
            this.rpm =(int) (Double.valueOf(data[43]) * 125) / 1000;


            //%JL2, resolution .05
            this.totalEngineHour = Double.parseDouble(data[44]) * .05f;

            //%JL3
            double odometerRawValue = Double.parseDouble(data[45]);

            // if odometer reading is greater than million then skip
            // only accept value less than a million
            if (odometerRawValue <= CONSTANT_MILION) {
                this.odometerReading = odometerRawValue;
            }

            //%JL1
            this.totalFuelUsed = Double.parseDouble(data[46]) * .5f;

            // JN2 in hours
            this.TotalIdleEngineHours = Double.parseDouble(data[48]) * .05f;

            // JN3 in liters
            this.TotalIdleFuelUsed = Double.parseDouble(data[49]) * .5f;


        } catch (Exception exe) {

        }
        return this;
    }

    // Created By: Deepak Sharma
    // Created Date: 1 April 2019
    // Purpose: parse data from J1708 protocol in case of light duty vehicle
    /// Sample Command GPS: AT$FORM=0,@K,1,"%ME%GQ%BT%AT%BV%DT%GS%MV%RL%SA%CS%CR%EG%DL%SF%JD%ML%OC" upto 39
    /// Sample Command J1708 Data: AT$1708="%ZO10%ZO14%ZO6%ZH3%ZL4%ZL2%ZS1"
    public ReportBean j1708Get(String[] data) {

        try {
            this.prefix = data[0];
            this.checkSum = data[1];
            this.length = Integer.parseInt(data[2]);
            this.sequenceId = Long.parseLong(data[3]);

            // we will save vehicle id,driver id and co driver id in this field
            // first seven digits for vehicle id, next 6 for driveri d, next 6 for co driver id and next 1 from special status (0,1,2,3) i.e PU,YM,WT(Waiting Time),Clear Special status
            // vehicle id starts from 1000000, driver id starts from 100000, co driver id from 100000
            this.unitId = data[4];

            // pick first 7 digit from unit id
            // convert it to int and subtract 1000000 to get vehicle id
            if (this.unitId.length() >= 7) {
                this.vehicleId = Integer.parseInt(this.unitId.substring(0, 7)) - 1000000;
            }

            // pick 8th to 13th digit from unit id
            // convert it to int and subtract 100000 to get driver id
            if (this.unitId.length() >= 13) {

                this.driverId = Integer.parseInt(this.unitId.substring(7, 13)) - 100000;
            }

            // pick 14th to 20th digit from unit id
            // convert it to int and subtract 100000 to get co driver id
            if (this.unitId.length() >= 19) {

                this.coDriverId = Integer.parseInt(this.unitId.substring(13, 19)) - 100000;
            }

            // pick 14th to 20th digit from unit id
            // convert it to int
            if (this.unitId.length() >= 20) {

                this.specialStatus = Integer.parseInt(this.unitId.substring(19, 20));
            }


            // below dates are in utc
            this.gpsDate = Utility.parse(data[5], CustomDateFormat.dt7);
            this.rtcDate = Utility.parse(data[6], CustomDateFormat.dt7);
            // get utc real time to time in driver timezone
            this.reportDate = Utility.convertUTCtoDriverTime(data[6]);

            this.positionSendingDate = Utility.parse(data[7], CustomDateFormat.dt7);

            // Longitude and latitude comes in multiple of 1 Million so divide by 1 Million
            this.longitude = Double.parseDouble(data[8]) * 0.000001f;
            this.latitude = Double.parseDouble(data[9]) * 0.000001f;
            this.heading = Double.parseDouble(data[10]);

            // It helps in identify type of data. we get multiple types of data from hutch connect device
            // e.g Location Records, Reports for engine up/down, Bluetooth connect/disconnect report and so on
            // each type of data has different report id
            this.reportId = Integer.parseInt(data[11]);

            // Gps odometer reading comes in multiple of 10 so divide it by 10 to get actual odometer reading
            // Odometer reading would be always in KMs
            this.gpsOdometerReading = Double.parseDouble(data[12]) / 10;

            // this would be in Km/h
            this.gpsSpeed = Double.parseDouble(data[15]);

            // GPS Default Data would be till index 15, then at index 16 there would be separator
            // From Index 22 to 40 we get additional data with gps "%ME%GQ%BT%AT%BV%DT%GS%MV%RL%SA%CS%CR%EG%DL%SF%JD%ML%OC%RP%VS%EH%OD%FL%FC%ET"

            // %ME
            this.serialNo = data[22];

            // %GQ
            this.GSMSignalStrength = Double.parseDouble(data[23]);

            // %BT, Bluetooth connected/disconnected. if 1 then bluetooth is connected
            this.BTConnectFg = data[24].equals("1");

            // %AT
            this.Altitude = Double.parseDouble(data[25]);

            // %BV
            this.DeviceBatteryLevel = Double.parseDouble(data[26]) * .1f;

            // %DT, if 1 means history log and 0 means realtime data
            this.HistoryLogFg = data[27].equals("1");

            // %GS, GSM Status
            this.GSMStatus = Integer.parseInt(data[28]);

            // %MV
            this.VehicleBatteryLevel = Double.parseDouble(data[29]) * .1f;

            // %RL, Current RXLev of GSM Module or GPS Module
            this.GPSStrength = Double.parseDouble(data[30]);

            // %SA
            this.TotalSatellite = Integer.parseInt(data[31]);

            // %CS. indicates if device is connected to hutch server
            this.ServerConnectFg = data[32].equals("1");

            // %CR, Roaming Status
            this.RoamingFg = data[33].equals("0");

            // %EG  Vehicle Engine Status PowerUp/Down
            this.EnginePowerUpFg = data[34].equals("1");

            // %DL  Idle event Status, Engine Idling
            this.EngineIdlingFg = data[35].equals("1");

            // %SF  GPS Fix Status
            this.gpsStatus = Integer.parseInt(data[36]);

            // %JD  GSM Jammming
            this.GSMJammingFg = data[37].equals("1");

            // %ML  Check Engine
            this.CheckEngineFg = data[38].equals("1");

            //% OC  OBD2 Status
            this.OBD2StatusFg = data[39].equals("1");

            // %ZO10, resolution is .4
            this.fuelLevel = Double.parseDouble(data[40]) * .4f;

            // %ZO14, enginecoolant, 40 degree celcius offset
            this.coolantTemperature = Double.parseDouble(data[41]) - 40;

            // %ZO6, Wheel based speed
            this.speed = Double.parseDouble(data[42]) * .806f;

            // copy from gps speed if garbage value
            if (this.speed >= 200) {
                this.speed = this.gpsSpeed;
            }

            // %ZH3, Engine speed
            this.rpm =(int) (Double.parseDouble(data[43]) * 25) / 100;


            //%ZL4, resolution .05
            this.totalEngineHour = Double.parseDouble(data[44]) * .05f;

            //%ZL2
            double odometerRawValue = Double.parseDouble(data[45]) * .161f;

            // if odometer reading is greater than million then skip
            // only accept value less than a million
            if (odometerRawValue <= CONSTANT_MILION) {
                this.odometerReading = odometerRawValue;
            }

         /*   //%JL1
            this.totalFuelUsed = Integer.parseInt(data[46]) * .5f;

            // JN2 in hours
            this.TotalIdleEngineHours = Double.parseDouble(data[48]) * .05f;

            // JN3 in liters
            this.TotalIdleFuelUsed = Double.parseDouble(data[49]) * .5f;*/

        } catch (Exception exe) {

        }
        return this;
    }


    public void ReceiveData(String completeData) {
        String[] AllData = completeData.split("\r\n");
        for (String item : AllData) {

            // if data is incomplete and next @ sentence appends
            // to prevent garbage data
            if (item.indexOf("@", 1) != -1)
                continue;

            String[] dataArray = item.split(",");


            // @P means data is coming from OBD2 protocol only
            if (item.startsWith("@O")) {
                CanMessages.protocol = "OBD2";
                CanMessages.OBD2SupportFg = true;
                ReportBean bean = obd2Get(dataArray);

                // save to db
                objDb.Save(bean);

            }
            // data is coming only from J1939 protocol
            else if (item.startsWith("@J")) {
                CanMessages.J1939SupportFg = true;
                CanMessages.protocol = "J1939";
                ReportBean bean = j1939Get(dataArray);

                // save to db
                objDb.Save(bean);
            }
            // data is coming from J1708 protocol
            else if (item.startsWith("@K")) {
                CanMessages.protocol = "J1708";
                CanMessages.J1708SupportFg = true;

                ReportBean bean = j1708Get(dataArray);

                // save to db
                objDb.Save(bean);
            }
            //LocationGet(item);

        }
    }


    // save when last location is saved
    long lastLocationTime = 0;

    private void LocationGet(String item) {

        try {

            // get location data e.g lat, lon etc..
            if (item.startsWith("@")) {
                String[] dataArray = item.split(",");
                Date currentDate = Utility.newDate();
                float difference = (currentDate.getTime() - lastLocationTime) / 60000f;

                // only save location every 1 minute
                if (difference >= 1) {
                    Date locationDate = Utility.parse(dataArray[5], CustomDateFormat.dt7);

                    double longitude = Double.parseDouble(dataArray[8]) * 0.000001d;
                    double latitude = Double.parseDouble(dataArray[9]) * 0.000001d;
                    float heading = Float.parseFloat(dataArray[10]);
                    int gpsStatus = Integer.parseInt(dataArray[35]);


                    // only save location if gps status is 1
                    if (gpsStatus == 1 || Utility.currentLocation.getLatitude() == 0) {
                        Utility.currentLocation.setLocationDate(locationDate);
                        Utility.currentLocation.setLongitude(longitude);
                        Utility.currentLocation.setLatitude(latitude);
                        Utility.currentLocation.setBearing(heading);
                        Utility.callGeocodeTask();
                        lastLocationTime = currentDate.getTime();
                    }

                    Utility.currentLocation.setGpsStatus(gpsStatus);
                }
            }
        } catch (Exception exe) {

        }
    }

}
