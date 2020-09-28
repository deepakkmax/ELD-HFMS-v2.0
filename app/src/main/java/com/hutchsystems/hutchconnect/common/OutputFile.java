package com.hutchsystems.hutchconnect.common;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.beans.UserBean;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.MySQLiteOpenHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Deepak Sharma on 3/31/2016.
 */
public class OutputFile {
    private static DateFormat dateFormat = new SimpleDateFormat("MMddyy");
    private static DateFormat timeFormat = new SimpleDateFormat("HHmmss");
    private static String carriage_return = "\r";
    public static String fileName = "";

    // Created By: Deepak Sharma
    // Created Date: 1 April 2016
    // Purpose: get header for output file
    public static String getHeader(String fileComment) {
        //get current date time with Date()
        Date currentDT = Utility.newDate();
        String currentDate = dateFormat.format(currentDT);
        String currentTime = timeFormat.format(currentDT);
        UserBean user1 = Utility.user1;
        UserBean user2 = Utility.user2;
        String driverInfo, coDriverInfo = "", vehicleInfo, carrierInfo, licenseNo = "";
        String checksum = ""; // it should be calculated separately for each line and in last for entire file. its line checksum
        fileName = "";
        String exempt = "0";
        if (user1.isOnScreenFg()) {
            checksum = Ascii.getLineDataCheckValue(user1.getLastName() + user1.getFirstName() + user1.getUserName() + user1.getDlIssueState() + user1.getDrivingLicense());

            driverInfo = user1.getLastName() + "," + user1.getFirstName() + "," + user1.getUserName() + "," + user1.getDlIssueState() + "," + user1.getDrivingLicense() + "," + checksum;
            fileDataCheckValue = checksum;
            licenseNo = user1.getDrivingLicense();
            if (user2.getAccountId() > 0) {
                checksum = Ascii.getLineDataCheckValue(user2.getLastName() + user2.getFirstName() + user2.getUserName());
                coDriverInfo = carriage_return + user2.getLastName() + "," + user2.getFirstName() + "," + user2.getUserName() + "," + checksum;
                fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, checksum);
            } else {
                // if driver is single then pass - for firstname,lastname and username as FMCSA reject file if there is empty line of codriver
                checksum = Ascii.getLineDataCheckValue("nullnullnull");
                coDriverInfo = carriage_return + "null,null,null," + checksum;
                fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, checksum);
            }

            if (user1.getExemptELDUseFg() == 1) {
                exempt = "E";
            }
            // first five character of Last name
            if (user1.getLastName().length() >= 5) {
                fileName = user1.getLastName().substring(0, 5);
            } else {
                // if last name shorter than five character then pad with _
                fileName = String.format("%-5s", user1.getLastName()).replace(' ', '_');
            }
            fileName += user1.getDrivingLicense().substring(user1.getDrivingLicense().length() - 2);
        } else {
            checksum = Ascii.getLineDataCheckValue(user2.getLastName() + user2.getFirstName() + user2.getUserName() + user2.getDlIssueState() + user2.getDrivingLicense());
            fileDataCheckValue = checksum;
            driverInfo = user2.getLastName() + "," + user2.getFirstName() + "," + user2.getUserName() + "," + user2.getDlIssueState() + "," + user2.getDrivingLicense() + "," + checksum;

            // fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, checksum);
            checksum = Ascii.getLineDataCheckValue(user1.getLastName() + user1.getFirstName() + user1.getUserName());
            coDriverInfo = carriage_return + user1.getLastName() + "," + user1.getFirstName() + "," + user1.getUserName() + "," + checksum;
            fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, checksum);
            licenseNo = user2.getDrivingLicense();
            if (user2.getExemptELDUseFg() == 1) {
                exempt = "E";
            }

            // first five character of Last name
            if (user1.getLastName().length() >= 5) {
                fileName = user2.getLastName().substring(0, 5);
            } else {
                // if last name shorter than five character then pad with _
                fileName = String.format("%-5s", user2.getLastName()).replace(' ', '_');
            }
            fileName += user2.getDrivingLicense().substring(user2.getDrivingLicense().length() - 2);
        }
        int sumLicense = 0;
        for (char c : licenseNo.toCharArray()) {
            if (Character.isDigit(c)) {
                sumLicense += Integer.valueOf(c);
            }
        }
        //sum of all digits of driving license no. complite its static value;
        if (sumLicense > 99) {
            sumLicense -= 100;
            fileName += sumLicense;
        } else if (sumLicense < 10) {
            fileName += "0" + sumLicense;
        } else {
            fileName += sumLicense;

        }

        fileName += currentDate + "-000" + currentTime + ".csv";
        String trailerNo = Utility.TrailerNumber.replace(",", ";");

        checksum = Ascii.getLineDataCheckValue(Utility.UnitNo + Utility.VIN + trailerNo);
        vehicleInfo = carriage_return + Utility.UnitNo + "," + Utility.VIN + "," + trailerNo + "," + checksum;
        fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, checksum);
        String timeZoneOffsetUTC = ZoneList.getTimeZoneOutputFile(Utility.TimeZoneOffset);

        checksum = Ascii.getLineDataCheckValue(Utility.USDOT + Utility.CarrierName + Utility.multiDayBasisUsed + "000000" + timeZoneOffsetUTC);
        fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, checksum);

        carrierInfo = carriage_return + Utility.USDOT + "," + Utility.CarrierName + "," + Utility.multiDayBasisUsed + "," + "000000" + "," + timeZoneOffsetUTC + "," + checksum;

        String shippingNumber = Utility.ShippingNumber.replace(",", ";");

        checksum = Ascii.getLineDataCheckValue(shippingNumber + exempt);
        fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, checksum);

        carrierInfo += carriage_return + shippingNumber + "," + exempt + "," + checksum;

        String latitude = Utility.truncate(Utility.currentLocation.getLatitude(), 2) + "";
        String longitude = Utility.truncate(Utility.currentLocation.getLongitude(), 2) + "";
        checksum = Ascii.getLineDataCheckValue(currentDate + currentTime + latitude + longitude + Double.valueOf(CanMessages.OdometerReading).intValue()
                + String.format("%.1f", Double.valueOf(CanMessages.EngineHours == null ? "0.0" : CanMessages.EngineHours)));

        fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, checksum);
        carrierInfo += carriage_return + currentDate + "," + currentTime + "," + latitude + "," + longitude + "," + Double.valueOf(CanMessages.OdometerReading).intValue()
                + "," + String.format("%.1f", Double.valueOf(CanMessages.EngineHours == null ? "0.0" : CanMessages.EngineHours)) + "," + checksum; // location info and vehicle engine info and miles info

        // get authentication value
        String authValue = Utility.getAuthenticationValue();
        checksum = Ascii.getLineDataCheckValue(Utility.RegistrationId + Utility.EldIdentifier + authValue + fileComment);

        fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, checksum);
        carrierInfo += carriage_return + Utility.RegistrationId + "," + Utility.EldIdentifier + "," + authValue + "," + fileComment + "," + checksum; //device registration info
        String header = driverInfo + coDriverInfo + vehicleInfo + carrierInfo;
        return header;
    }

    static String fileDataCheckValue = "";

    // Created By: Deepak Sharma
    // Created Date: 17 June 2016
    // Purpose: get output file
    public static String getOutputFile(String comment) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        StringBuilder sb = new StringBuilder();
        try {
            fileDataCheckValue = "";
            sb.append("ELD File Header Segment:").append(carriage_return);
            sb.append(getHeader(comment)).append(carriage_return);
            sb.append("User List:").append(carriage_return);

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String fromDate = Utility.addDate(Utility.getCurrentDate(), -Utility.multiDayBasisUsed);
            int driverId = Utility.onScreenUserId;

            // get userList query. with list of driver co driver and unidentified driver
            String query = "select UserId,AccountType,FirstName,LastName,max(EventDateTime) as EventDateTime from (" +
                    " select a.AccountId as UserId, a.AccountType,a.FirstName,a.LastName,EventDateTime from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on  e.DriverId=a.AccountId" +
                    " where EventDateTime>=? and (a.AccountType=0 or a.AccountType=2)" +
                    // get list of support personnal who sent edit request
                    " union select a.AccountId as UserId,a.AccountType,a.FirstName,a.LastName,EventDateTime from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on  e.ModifiedBy=a.AccountId" +
                    " where EventDateTime>=? and a.AccountType!=0 and e.EventRecordOrigin=3)a group by UserId,a.AccountType,a.FirstName,a.LastName order by max(EventDateTime) desc";

            cursor = database.rawQuery(query, new String[]{fromDate, fromDate});
            int i = 1;
            String lineCheckValue = "";
            Map<Integer, Integer> userList = new LinkedHashMap<>();

            while (cursor.moveToNext()) {
                userList.put(cursor.getInt(0), i);
                sb.append(i).append(",");
                int userType = cursor.getInt(cursor.getColumnIndex("AccountType"));

                // if Driver or Unidentified driver then "D", for non driver "S"
                String accountType = ((userType == 0 || userType == 2) ? "D" : "S");

                String lastName = cursor.getString(cursor.getColumnIndex("LastName"));
                String firstName = cursor.getString(cursor.getColumnIndex("FirstName"));

                sb.append(accountType).append(",");
                sb.append(lastName).append(",");
                sb.append(firstName).append(",");
                lineCheckValue = Ascii.getLineDataCheckValue(i + accountType + lastName + firstName);
                fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, lineCheckValue);

                sb.append(lineCheckValue).append(carriage_return);
                i++;
            }

            cursor.close();

            // get CMV List
            sb.append("CMV List:").append(carriage_return);
            query = "select distinct e.VehicleId,c.UnitNo,c.VIN,max(EventDateTime)  from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " e join " + MySQLiteOpenHelper.TABLE_CARRIER + " c on  e.VehicleId=c.VehicleId" +
                    " where EventDateTime>=? and DriverId=? group by e.VehicleId,c.UnitNo,c.VIN order by max(EventDateTime)  desc";
            cursor = database.rawQuery(query, new String[]{fromDate, driverId + ""});

            i = 1;

            Map<Integer, Integer> cmvList = new LinkedHashMap<>();
            while (cursor.moveToNext()) {

                cmvList.put(cursor.getInt(0), i);
                sb.append(i).append(",");
                String unitNo = cursor.getString(cursor.getColumnIndex("UnitNo"));
                String vinNo = cursor.getString(cursor.getColumnIndex("VIN"));
                sb.append(unitNo).append(",");
                sb.append(vinNo).append(",");
                lineCheckValue = Ascii.getLineDataCheckValue(i + unitNo + vinNo);
                fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, lineCheckValue);
                sb.append(lineCheckValue).append(carriage_return);
                i++;
            }
            cursor.close();

            // get ELD Event List
            sb.append("ELD Event List:").append(carriage_return);
            query = "select EventSequenceId,EventRecordStatus,EventRecordOrigin,EventType,EventCode,EventDateTime,AccumulatedVehicleMiles ,ElaspsedEngineHour,Latitude ,Longitude" +
                    ",DistanceSinceLastValidCoordinate,MalfunctionIndicatorFg ,DataDiagnosticIndicatorFg,e.VehicleId,e.DriverId,c.UnitNo,a.Username,e.Annotation from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on e.DriverId=a.AccountId join " + MySQLiteOpenHelper.TABLE_CARRIER +
                    " c on c.VehicleId=e.VehicleId " +
                    " where EventType<=3  and EventDateTime>=? and e.DriverId=? order by EventDateTime desc";
            cursor = database.rawQuery(query, new String[]{fromDate, driverId + ""});

            while (cursor.moveToNext()) {

                // Event sequence id should be sent to fmcsa in Hex
                String eventSequenceId = Integer.toHexString(cursor.getInt(cursor.getColumnIndex("EventSequenceId")));

                // length of event sequence id should be 4
                eventSequenceId = Utility.padLeft(eventSequenceId, 4);

                String eventRecordStatus = cursor.getString(cursor.getColumnIndex("EventRecordStatus"));
                String eventRecordOrigin = cursor.getString(cursor.getColumnIndex("EventRecordOrigin"));
                String eventType = cursor.getString(cursor.getColumnIndex("EventType"));
                String eventCode = cursor.getString(cursor.getColumnIndex("EventCode"));
                Date eventDateTime = Utility.parse(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                String eventDate = dateFormat.format(eventDateTime);
                String eventTime = timeFormat.format(eventDateTime);
                int accumulatedVehicleMiles = cursor.getInt(cursor.getColumnIndex("AccumulatedVehicleMiles"));
                if (accumulatedVehicleMiles < 0 || accumulatedVehicleMiles > 9999) {
                    accumulatedVehicleMiles = 0;
                }

                double elapsedEngineHours = Double.parseDouble(cursor.getString(cursor.getColumnIndex("ElaspsedEngineHour")));
                if (elapsedEngineHours < 0d || elapsedEngineHours > 99.9d) {
                    elapsedEngineHours = 0d;
                }

                String latitude = cursor.getString(cursor.getColumnIndex("Latitude"));
                String longitude = cursor.getString(cursor.getColumnIndex("Longitude"));
                int distanceSinceLastValidCoordinate = cursor.getInt(cursor.getColumnIndex("DistanceSinceLastValidCoordinate"));

                if (distanceSinceLastValidCoordinate > 6 || distanceSinceLastValidCoordinate < 0) {
                    distanceSinceLastValidCoordinate = 0;
                }
                int vehicleId = cursor.getInt(cursor.getColumnIndex("VehicleId"));
                String vehicleOrderNo = cmvList.get(vehicleId) + "";

                // this should not be done but to be on safe side
                if (vehicleOrderNo == null) {
                    vehicleOrderNo = "1";
                }

                String userOrderNo = userList.get(cursor.getInt(cursor.getColumnIndex("DriverId"))) + "";
                String malFunctionIndicatorFg = cursor.getString(cursor.getColumnIndex("MalfunctionIndicatorFg"));
                String dataDiagnosticIndicatorFg = cursor.getString(cursor.getColumnIndex("DataDiagnosticIndicatorFg"));
                String unitNo = cursor.getString(cursor.getColumnIndex("UnitNo"));
                String userName = cursor.getString(cursor.getColumnIndex("Username"));

                sb.append(eventSequenceId).append(",");
                sb.append(eventRecordStatus).append(",");
                sb.append(eventRecordOrigin).append(",");
                sb.append(eventType).append(",");
                sb.append(eventCode).append(",");
                sb.append(eventDate).append(",");
                sb.append(eventTime).append(",");
                sb.append(accumulatedVehicleMiles).append(",");
                sb.append(elapsedEngineHours).append(",");
                sb.append(latitude).append(",");
                sb.append(longitude).append(",");
                sb.append(distanceSinceLastValidCoordinate).append(",");

                sb.append(vehicleOrderNo).append(","); // CMV order no
                sb.append(userOrderNo).append(","); // User order no

                sb.append(malFunctionIndicatorFg).append(",");
                sb.append(dataDiagnosticIndicatorFg).append(",");

                String eventDataCheckValue = Ascii.getEventDataCheckValue(eventType + eventCode + eventDate + eventTime + accumulatedVehicleMiles + elapsedEngineHours + latitude + longitude + unitNo + userName);

                sb.append(eventDataCheckValue).append(",");

                lineCheckValue = Ascii.getLineDataCheckValue(eventSequenceId + eventRecordStatus + eventRecordOrigin + eventType + eventCode + eventDate + eventTime + accumulatedVehicleMiles + elapsedEngineHours + latitude + longitude + distanceSinceLastValidCoordinate + vehicleOrderNo + userOrderNo + malFunctionIndicatorFg + dataDiagnosticIndicatorFg + eventDataCheckValue);

                fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, lineCheckValue);
                sb.append(lineCheckValue).append(carriage_return);

            }
            cursor.close();

            // get ELD Event Annotations or Comments:
            sb.append("ELD Event Annotations or Comments:").append(carriage_return);
            query = "select EventSequenceId,EventDateTime,LocationDescription,Annotation,a.Username from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on e.DriverId=a.AccountId where EventType<=3 and EventDateTime>=? and e.DriverId=? and ((LocationDescription <> '' and LocationDescription is not null) or (Annotation <> '' and Annotation is not null))  order by EventDateTime desc";
            cursor = database.rawQuery(query, new String[]{fromDate, driverId + ""});

            while (cursor.moveToNext()) {
                String annotation = cursor.getString(cursor.getColumnIndex("Annotation"));
                // Event sequence id should be sent to fmcsa in Hex
                String eventSequenceId = Integer.toHexString(cursor.getInt(cursor.getColumnIndex("EventSequenceId")));
                // length of event sequence id should be 4
                eventSequenceId = Utility.padLeft(eventSequenceId, 4);

                String userName = cursor.getString(cursor.getColumnIndex("Username"));
                annotation = annotation.replace(",", ";");
                if (annotation.length() > 60) {
                    annotation = annotation.substring(0, 59);
                }

                String locationDescription = cursor.getString(cursor.getColumnIndex("LocationDescription"));
                if (!locationDescription.isEmpty()) {
                    locationDescription = locationDescription.replace(",", ";");
                    if (locationDescription.length() > 60) {
                        locationDescription = locationDescription.substring(0, 59);
                    } else if (locationDescription.length() < 5) {
                        locationDescription = String.format("%-5s", locationDescription).replace(' ', ';');

                    }
                }

                sb.append(eventSequenceId).append(",");
                sb.append(userName).append(",");
                sb.append(annotation).append(",");

                Date eventDateTime = Utility.parse(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                String eventDate = dateFormat.format(eventDateTime);
                String eventTime = timeFormat.format(eventDateTime);
                sb.append(eventDate).append(",");
                sb.append(eventTime).append(",");
                sb.append(locationDescription).append(",");
                lineCheckValue = Ascii.getLineDataCheckValue(eventSequenceId + userName + annotation + eventDate + eventTime + locationDescription);
                fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, lineCheckValue);
                sb.append(lineCheckValue).append(carriage_return);
                i++;
            }

            cursor.close();

            // get Driver's Certification/Recertification Actions:
            sb.append("Driver's Certification/Recertification Actions:").append(carriage_return);

            query = "select EventSequenceId,EventCode,EventDateTime,LogDate,VehicleId from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " e join " + MySQLiteOpenHelper.TABLE_DAILYLOG + " d on d._id=e.DailyLogId where EventType=4  and EventDateTime>=? and e.DriverId=? order by EventDateTime desc";
            cursor = database.rawQuery(query, new String[]{fromDate, driverId + ""});

            while (cursor.moveToNext()) {
                // Event sequence id should be sent to fmcsa in Hex
                String eventSequenceId = Integer.toHexString(cursor.getInt(cursor.getColumnIndex("EventSequenceId")));
                // length of event sequence id should be 4
                eventSequenceId = Utility.padLeft(eventSequenceId, 4);

                String eventCode = cursor.getString(cursor.getColumnIndex("EventCode"));
                sb.append(eventSequenceId).append(",");
                sb.append(eventCode).append(",");
                int vehicleId = cursor.getInt(cursor.getColumnIndex("VehicleId"));
                Date eventDateTime = Utility.parse(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                String eventDate = dateFormat.format(eventDateTime);
                String eventTime = timeFormat.format(eventDateTime);
                String logDate = dateFormat.format(new SimpleDateFormat("yyyy-MM-dd").parse(cursor.getString(cursor.getColumnIndex("LogDate"))));
                String cmvOrderNo = cmvList.get(vehicleId) + "";

                sb.append(eventDate).append(",");
                sb.append(eventTime).append(",");
                sb.append(logDate).append(",");
                sb.append(cmvOrderNo).append(","); // CMV order no

                lineCheckValue = Ascii.getLineDataCheckValue(eventSequenceId + eventCode + eventDate + eventTime + logDate + cmvOrderNo);
                fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, lineCheckValue);
                sb.append(lineCheckValue).append(carriage_return);
            }

            cursor.close();

            // get Malfunctions and Data Diagnostic Events:
            sb.append("Malfunctions and Data Diagnostic Events:").append(carriage_return);

            query = "select EventSequenceId,EventCode,DiagnosticCode,EventDateTime,OdometerReading,EngineHour,VehicleId from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " where EventType=7  and EventDateTime>=? and DriverId=? order by EventDateTime desc";
            cursor = database.rawQuery(query, new String[]{fromDate, driverId + ""});

            while (cursor.moveToNext()) {
                // Event sequence id should be sent to fmcsa in Hex
                String eventSequenceId = Integer.toHexString(cursor.getInt(cursor.getColumnIndex("EventSequenceId")));
                // length of event sequence id should be 4
                eventSequenceId = Utility.padLeft(eventSequenceId, 4);

                String eventCode = cursor.getString(cursor.getColumnIndex("EventCode"));
                String diagnosticCode = cursor.getString(cursor.getColumnIndex("DiagnosticCode"));

                sb.append(eventSequenceId).append(",");
                sb.append(eventCode).append(",");
                sb.append(diagnosticCode).append(",");

                int vehicleId = cursor.getInt(cursor.getColumnIndex("VehicleId"));

                Date eventDateTime = Utility.parse(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                String eventDate = dateFormat.format(eventDateTime);
                String eventTime = timeFormat.format(eventDateTime);
                String odometerReading = Double.valueOf(cursor.getString(cursor.getColumnIndex("OdometerReading"))).intValue() + "";
                String engineHour = String.format("%.1f", Double.valueOf(cursor.getString(cursor.getColumnIndex("EngineHour"))));

                sb.append(eventDate).append(",");
                sb.append(eventTime).append(",");

                sb.append(odometerReading).append(",");
                sb.append(engineHour).append(",");
                String cmvOrderNo = cmvList.get(vehicleId) + "";
                // this should not be done but to be on safe side
                if (cmvOrderNo == null) {
                    cmvOrderNo = "1";
                }

                sb.append(cmvOrderNo).append(","); // CMV order no

                lineCheckValue = Ascii.getLineDataCheckValue(eventSequenceId + eventCode + diagnosticCode + eventDate + eventTime + odometerReading + engineHour + cmvOrderNo);
                fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, lineCheckValue);
                sb.append(lineCheckValue).append(carriage_return);
            }

            cursor.close();

            // get ELD Login/Logout Report:
            sb.append("ELD Login/Logout Report:").append(carriage_return);

            query = "select EventSequenceId,EventCode,a.Username,EventDateTime,OdometerReading,EngineHour from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on a.AccountId=e.DriverId where EventType=5  and EventDateTime>=? and e.DriverId=? order by EventDateTime desc";
            cursor = database.rawQuery(query, new String[]{fromDate, driverId + ""});

            while (cursor.moveToNext()) {
                // Event sequence id should be sent to fmcsa in Hex
                String eventSequenceId = Integer.toHexString(cursor.getInt(cursor.getColumnIndex("EventSequenceId")));
                // length of event sequence id should be 4
                eventSequenceId = Utility.padLeft(eventSequenceId, 4);

                String eventCode = cursor.getString(cursor.getColumnIndex("EventCode"));
                String username = cursor.getString(cursor.getColumnIndex("Username"));

                sb.append(eventSequenceId).append(",");
                sb.append(eventCode).append(",");
                sb.append(username).append(",");

                Date eventDateTime = Utility.parse(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                String eventDate = dateFormat.format(eventDateTime);
                String eventTime = timeFormat.format(eventDateTime);
                String odometerReading = Double.valueOf(cursor.getString(cursor.getColumnIndex("OdometerReading"))).intValue() + "";
                String engineHour = String.format("%.1f", Double.valueOf(cursor.getString(cursor.getColumnIndex("EngineHour"))));

                sb.append(eventDate).append(",");
                sb.append(eventTime).append(",");

                sb.append(odometerReading).append(",");
                sb.append(engineHour).append(",");

                lineCheckValue = Ascii.getLineDataCheckValue(eventSequenceId + eventCode + username + eventDate + eventTime + odometerReading + engineHour);
                fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, lineCheckValue);
                sb.append(lineCheckValue).append(carriage_return);
            }

            cursor.close();

            // get CMV Engine Power-Up and Shut Down Activity:
            sb.append("CMV Engine Power-Up and Shut Down Activity:").append(carriage_return);

            query = "select EventSequenceId,EventCode,EventDateTime,OdometerReading,EngineHour,Latitude ,Longitude,UnitNo,VIN,ShippingDocumentNo,TrailerNo from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " e join " + MySQLiteOpenHelper.TABLE_CARRIER + " c on e.VehicleId=c.VehicleId where EventType=6  and EventDateTime>=?  order by EventDateTime desc";
            cursor = database.rawQuery(query, new String[]{fromDate});

            while (cursor.moveToNext()) {
                // Event sequence id should be sent to fmcsa in Hex
                String eventSequenceId = Integer.toHexString(cursor.getInt(cursor.getColumnIndex("EventSequenceId")));
                // length of event sequence id should be 4
                eventSequenceId = Utility.padLeft(eventSequenceId, 4);

                String eventCode = cursor.getString(cursor.getColumnIndex("EventCode"));
                String latitude = cursor.getString(cursor.getColumnIndex("Latitude"));
                String longitude = cursor.getString(cursor.getColumnIndex("Longitude"));
                String unitNo = cursor.getString(cursor.getColumnIndex("UnitNo"));
                String VIN = cursor.getString(cursor.getColumnIndex("VIN"));
                String shippingDocumentNo = cursor.getString(cursor.getColumnIndex("ShippingDocumentNo"));
                shippingDocumentNo = shippingDocumentNo.replace(",", ";");

                String trailerNo = cursor.getString(cursor.getColumnIndex("TrailerNo"));
                trailerNo = trailerNo.replace(",", ";");

                // max length is 32 allowed by FMCSA
                if (!trailerNo.isEmpty() && trailerNo.length() > 32) {
                    trailerNo = trailerNo.substring(0, 31);
                }

                sb.append(eventSequenceId).append(",");
                sb.append(eventCode).append(",");

                Date eventDateTime = Utility.parse(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                String eventDate = dateFormat.format(eventDateTime);
                String eventTime = timeFormat.format(eventDateTime);

                String odometerReading = Double.valueOf(cursor.getString(cursor.getColumnIndex("OdometerReading"))).intValue() + "";
                String engineHour = String.format("%.1f", Double.valueOf(cursor.getString(cursor.getColumnIndex("EngineHour"))));

                sb.append(eventDate).append(",");
                sb.append(eventTime).append(",");

                sb.append(odometerReading).append(",");
                sb.append(engineHour).append(",");

                sb.append(latitude).append(",");
                sb.append(longitude).append(",");
                sb.append(unitNo).append(",");
                sb.append(VIN).append(",");
                sb.append(trailerNo).append(",");
                sb.append(shippingDocumentNo).append(",");

                lineCheckValue = Ascii.getLineDataCheckValue(eventSequenceId + eventCode + eventDate + eventTime + odometerReading + engineHour + latitude + longitude + unitNo + VIN + trailerNo + shippingDocumentNo);
                fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, lineCheckValue);
                sb.append(lineCheckValue).append(carriage_return);
            }

            cursor.close();

            //get Unidentified Driver Profile Records:
            sb.append("Unidentified Driver Profile Records:").append(carriage_return);
//            query = "select EventSequenceId,EventRecordStatus,EventRecordOrigin,EventType,EventCode,EventDateTime,AccumulatedVehicleMiles ,ElaspsedEngineHour,Latitude ,Longitude" +
//                    ",DistanceSinceLastValidCoordinate,MalfunctionIndicatorFg ,DataDiagnosticIndicatorFg,VehicleId,DriverId,UnitNo,Username  from " + MySQLiteOpenHelper.TABLE_CARRIER + " c join " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
//                    " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on e.DriverId=a.AccountId and e.VehicleId=c.VehicleId and a.AccountType=2 where EventType<=3  and EventDateTime>=? order by EventDateTime desc";
            query = "select *  from " + MySQLiteOpenHelper.TABLE_CARRIER + " c join " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on c.VehicleId=e.VehicleId and e.DriverId=a.AccountId and a.AccountType=2 where EventType<=3  and EventDateTime>=? order by EventDateTime desc";
            cursor = database.rawQuery(query, new String[]{fromDate});

            while (cursor.moveToNext()) {

                // Event sequence id should be sent to fmcsa in Hex
                String eventSequenceId = Integer.toHexString(cursor.getInt(cursor.getColumnIndex("EventSequenceId")));
                // length of event sequence id should be 4
                eventSequenceId = Utility.padLeft(eventSequenceId, 4);

                String eventRecordStatus = cursor.getString(cursor.getColumnIndex("EventRecordStatus"));
                String eventRecordOrigin = cursor.getString(cursor.getColumnIndex("EventRecordOrigin"));
                String eventType = cursor.getString(cursor.getColumnIndex("EventType"));
                String eventCode = cursor.getString(cursor.getColumnIndex("EventCode"));
                Date eventDateTime = Utility.parse(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                String eventDate = dateFormat.format(eventDateTime);
                String eventTime = timeFormat.format(eventDateTime);
                //String accumulatedVehicleMiles = cursor.getString(cursor.getColumnIndex("AccumulatedVehicleMiles"));

                int accumulatedVehicleMiles = cursor.getInt(cursor.getColumnIndex("AccumulatedVehicleMiles"));
                if (accumulatedVehicleMiles < 0 || accumulatedVehicleMiles > 9999) {
                    accumulatedVehicleMiles = 0;
                }

                //String elapsedEngineHours = cursor.getString(cursor.getColumnIndex("ElaspsedEngineHour"));
                double elapsedEngineHours = Double.parseDouble(cursor.getString(cursor.getColumnIndex("ElaspsedEngineHour")));
                if (elapsedEngineHours < 0d || elapsedEngineHours > 99.9d) {
                    elapsedEngineHours = 0d;
                }

                String latitude = cursor.getString(cursor.getColumnIndex("Latitude"));
                String longitude = cursor.getString(cursor.getColumnIndex("Longitude"));
                //String distanceSinceLastValidCoordinate = cursor.getString(cursor.getColumnIndex("DistanceSinceLastValidCoordinate"));

                int distanceSinceLastValidCoordinate = cursor.getInt(cursor.getColumnIndex("DistanceSinceLastValidCoordinate"));
                if (distanceSinceLastValidCoordinate > 6 || distanceSinceLastValidCoordinate < 0) {
                    distanceSinceLastValidCoordinate = 0;
                }

                int vehicleId = cursor.getInt(cursor.getColumnIndex("VehicleId"));
                String vehicleOrderNo = cmvList.get(vehicleId) + "";

                // this should not be done but to be on safe side
                if (vehicleOrderNo == null) {
                    vehicleOrderNo = "1";
                }

                String malFunctionIndicatorFg = cursor.getString(cursor.getColumnIndex("MalfunctionIndicatorFg"));
                String dataDiagnosticIndicatorFg = cursor.getString(cursor.getColumnIndex("DataDiagnosticIndicatorFg"));
                String unitNo = cursor.getString(cursor.getColumnIndex("UnitNo"));
                String userName = cursor.getString(cursor.getColumnIndex("Username"));

                sb.append(eventSequenceId).append(",");
                sb.append(eventRecordStatus).append(",");
                sb.append(eventRecordOrigin).append(",");
                sb.append(eventType).append(",");
                sb.append(eventCode).append(",");
                sb.append(eventDate).append(",");
                sb.append(eventTime).append(",");
                sb.append(accumulatedVehicleMiles).append(",");
                sb.append(elapsedEngineHours).append(",");
                sb.append(latitude).append(",");
                sb.append(longitude).append(",");
                sb.append(distanceSinceLastValidCoordinate).append(",");

                sb.append(vehicleOrderNo).append(","); // CMV order no

                sb.append(malFunctionIndicatorFg).append(",");
                //sb.append(dataDiagnosticIndicatorFg).append(",");

                String eventDataCheckValue = Ascii.getEventDataCheckValue(eventType + eventCode + eventDate + eventTime + accumulatedVehicleMiles + elapsedEngineHours + latitude + longitude + unitNo + userName);

                sb.append(eventDataCheckValue).append(",");

                lineCheckValue = Ascii.getLineDataCheckValue(eventSequenceId + eventRecordStatus + eventRecordOrigin + eventType + eventCode + eventDate + eventTime + accumulatedVehicleMiles + elapsedEngineHours + latitude + longitude + distanceSinceLastValidCoordinate + vehicleOrderNo + malFunctionIndicatorFg + eventDataCheckValue);
                fileDataCheckValue = Ascii.AddHex(fileDataCheckValue, lineCheckValue);
                sb.append(lineCheckValue).append(carriage_return);
            }
            sb.append("End of File:").append(carriage_return);
            fileDataCheckValue = Ascii.getCheckValueForFile(fileDataCheckValue);
            sb.append(fileDataCheckValue).append(carriage_return);

        } catch (Exception e) {
            //Utility.printError(e.getMessage());
            LogFile.write(OutputFile.class.getName() + "::getOutputFile: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(OutputFile.class.getName(), "getOutputFile Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {

                LogFile.write(OutputFile.class.getName() + "::getOutputFile1: " + e2.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                LogDB.writeLogs(OutputFile.class.getName(), "OutputFile Error:", e2.getMessage(), Utility.printStackTrace(e2));

            }
        }
        return sb.toString();
    }

}

