package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.DiagnosticIndicatorBean;
import com.hutchsystems.hutchconnect.beans.EventBean;
import com.hutchsystems.hutchconnect.beans.GPSData;
import com.hutchsystems.hutchconnect.beans.ReportBean;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.Constants;
import com.hutchsystems.hutchconnect.common.DiagnosticMalfunction;
import com.hutchsystems.hutchconnect.common.HutchConnect;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.common.ZoneList;
import com.kmaxsystems.reversegeocode.GeoName;

import java.io.InputStream;

public class ReportDB {

    public static boolean SpecialStatusFg = false;

    // name of class to log errors
    String TAG = ReportDB.class.getName();

    Context context;

    public ReportDB(Context context) {
        this.context = context;
    }

    // Created By: Deepak Sharma
    // Created Date: 12 Feb 2020
    // Purpose: save report to database
    public int Save(ReportBean bean) {
        // id of record after saving to database
        int id = 0;
        int reportId = bean.getReportId();
        int driverId = bean.getDriverId();

        int activeDriverId = Utility.activeUserId == 0 ? Utility.unIdentifiedDriverId : Utility.activeUserId;

        // set it true if you want to post data to server
        boolean postToServerFg = false;


        if (reportId == Constants.GPS_TRACKING) {
            if (bean.isBTConnectFg()) {
                int coDriverId = (Utility.activeUserId == Utility.user1.getAccountId() ? Utility.user2.getAccountId() : Utility.user1.getAccountId());
                int specialStatus = getSpecialStatusFg(activeDriverId);

                // device suppose to have following Unit Id as per app data
                String unitId = HutchConnect.getUnitId(Utility.vehicleId, activeDriverId, coDriverId, specialStatus);

                // current Unit Id set in device
                String currentUnitId = HutchConnect.getUnitId(bean.getVehicleId(), bean.getDriverId(), bean.getCoDriverId(), bean.getSpecialStatus());

                // if unit id mis matched
                if (!unitId.equals(currentUnitId)) {
                    if (mListener != null) {
                        mListener.onMismatchUnitId(unitId);
                    }
                } else {
                    if (mListener != null) {
                        mListener.onUnitMismatchDialog(false);
                    }
                }
            }
        }

        // special status from eld 2020
        SpecialStatusFg = bean.getSpecialStatus() == 1;

        if (bean.getUnitId().length() < 20) {
            return id;
        }

        switch (reportId) {
            case Constants.GPS_TRACKING:
                if (bean.isBTConnectFg() && bean.getGpsStatus() == 1) {
                    Utility.currentLocation.setLocationDate(bean.getGpsDate());
                    Utility.currentLocation.setLongitude(bean.getLongitude());
                    Utility.currentLocation.setLatitude(bean.getLatitude());
                    Utility.currentLocation.setBearing(bean.getHeading());
                    Utility.currentLocation.setGpsStatus(1);
                    Utility.callGeocodeTask();

                    // if user is logged in
                    // and auto rule change is on
                    if (Utility.activeUserId > 0 && Utility._appSetting.getAutomaticRuleChange() == 1) {
                        int currentRule = getCurrentRule(Utility.activeUserId);

                        // driver is in canada rule and entering to usa
                        if (currentRule <= 2 && bean.getLatitude() < 49d && bean.getLongitude() < -95d && bean.getLongitude() > -122.82) {
                            // fire auto rule change
                            autoRuleChange("US", bean.getReportDate());
                        }
                        // if current rule is us and driver is entering canada
                        else if (currentRule > 2 && bean.getLatitude() >= 49d && bean.getLongitude() < -95d) {
                            // fire auto rule change
                            autoRuleChange("CA", bean.getReportDate());
                        }
                    }
                }
                break;
            case Constants.ENGINE_POWER_UP:

                Utility.OdometerReadingSincePowerOn = Double.valueOf(bean.getOdometerReading()).intValue() + "";
                Utility.EngineHourSincePowerOn = String.format("%.1f", bean.getTotalEngineHour());

                bean.setEventType(6);
                bean.setEventCode(bean.getSpecialStatus() == 0 ? 1 : 2);
                bean.setEventCodeDescription(bean.getSpecialStatus() == 0 ? context.getString(R.string.powerup_event_conventional_description) : context.getString(R.string.powerup_event_reduced_description));


                // personal use--> power Off --> power on--> show dialog--> clear PU if driver want to clear it or vehicle is in motion
                //1. get previous event from db. if event type is 3 and event code is 1 if event code is 0 means personal use has already been clear
                //2. get event with type 6 and after event get from case 1. if it's code is 3 or 4 then show dialog box to clear or remaing in personal use
                //3. if driver do not response to dialog and start driving then personal use status should be cleared
                // if personal use and engine was off
                // un certify log after every event added or updated
                // if current status is PC and app finds Engine power off cycle (PC--> ENGINE OFF --> ENGINE ON) then show dialog box for confirmation to continue PC
                // if current status is YM and app finds  Engine power off cycle (PC--> ENGINE OFF --> ENGINE ON) then clear YM
                int status = showDialogToContinueSpecialStatus(bean);

                // save engine power up event
                EventCreateAuto(bean);
                Log.d("onDataReceived", "Special Status: " + 1);

                // show dialog to ask driver to continue personal use
                if (status == 1) {
                    // show dialog box in main activity
                    if (mListener != null) {
                        bean.setEventType(3);
                        bean.setEventCode(0);
                        bean.setEventCodeDescription(context.getString(R.string.pc_ym_yt_cleared));

                        Log.d("onDataReceived", "Show dialog:");
                        mListener.showPCDialog(bean);
                    }
                }
                // clear YM
                else if (status == 2) {
                    bean.setEventType(3);
                    bean.setEventCode(0);
                    bean.setEventCodeDescription(context.getString(R.string.pc_ym_yt_cleared));
                    EventCreateAuto(bean);

                    if (mListener != null)
                        mListener.onSpecialStatusChange(0);
                }
                postToServerFg = true;

                // fire event to make changes in elog fragment if required
                if (mListener != null)
                    mListener.onEventCapture(bean);
                break;
            case Constants.ENGINE_POWER_DOWN:
                Utility.NightModeFg = false;
                bean.setEventType(6);
                bean.setEventCode(bean.getSpecialStatus() == 0 ? 3 : 4);
                bean.setEventCodeDescription(bean.getSpecialStatus() == 0 ? context.getString(R.string.shutdown_event_conventional_description) : context.getString(R.string.shutdown_event_reduced_description));

                // save engine power down event

                EventCreateAuto(bean);

                postToServerFg = true;

                // fire event to make changes in elog fragment if required
                if (mListener != null) {

                    mListener.setVisionMode();
                    mListener.onEventCapture(bean);

                }
                break;
            case Constants.DEVICE_POWER_LOW:
                break;
            case Constants.DEVICE_POWER_LOSS: // Power compliance and engine synchronization (this would not be monitor for now)
/*
                if (!DiagnosticIndicatorBean.EngineSynchronizationDiagnosticFg) {

                    DiagnosticIndicatorBean.EngineSynchronizationDiagnosticFg = true;

                    // save data diagnostic event for engine synchronization
                    DiagnosticMalfunction.saveDiagnosticIndicatorByCode("2", 3, "EngineSynchronizationDiagnosticFg");

                    // startDiagnosticTimer();
                    Utility.saveDiagnosticTime(true);
                    postToServerFg = true;

                }*/

                break;
            case Constants.DRIVING: // if previous event is not driving or ilc then add driving
                // if current event is personal use/yard move and it is not cleared yet then add clear personal use/yard move only if power off cycle occurs
                // check recent record for event type 6 and event code (3,4)
                // if there is dialog box showing to continue personal use then dismiss that dialog
                // if previous status is driving and yard move
                // change status to driving and add it to database
                // un certify log after every event added or updated

                boolean triggeredFg = TriggerDriving(bean);

                // deduct balance if it's not deducted
                if (triggeredFg) {
                    Utility.DeviceBalancePost();
                }
                // if driving event is triggered then post data
                postToServerFg = triggeredFg;


                break;
            case Constants.ON_DUTY: // if previous event is driving then add on duty else skip this event

                bean.setEventType(1);

                if (bean.getDriverId() == Utility.unIdentifiedDriverId) {
                    bean.setEventCode(1);
                    bean.setEventCodeDescription(context.getString(R.string.duty_status_changed_to_off_duty));
                } else {
                    bean.setEventCode(4);
                    bean.setEventCodeDescription(context.getString(R.string.duty_status_changed_to_on_duty));
                }
                // get current duty status
                int currentStatus = getCurrentDutyStatus(bean.getDriverId());

                // only save automatic on duty status if current status is driving
                if (currentStatus == 3) {
                  /*     // save on Duty event
                    EventCreateAuto(bean);


                    postToServerFg = true;

                    // fire event to make changes in elog fragment if required
                    if (mListener != null)
                        mListener.onEventCapture(bean);*/
                    if (Utility.unIdentifiedDriverId == bean.getDriverId() || !bean.isBTConnectFg()) {
                        EventCreateAuto(bean);
                        postToServerFg = true;
                    } else if (mListener != null) {
                        mListener.promptToChangeStatus();
                    }

                    // deduct balance
                    Utility.DeviceBalancePost();
                }

                break;
            case Constants.ILC:
                bean.setEventType(2);
                bean.setEventCode(bean.getSpecialStatus() == 0 ? 1 : 2);
                bean.setEventCodeDescription(bean.getSpecialStatus() == 0 ? context.getString(R.string.IL_conventional_description) : context.getString(R.string.IL_reduced_description));

                // get current duty status
                EventBean currentEvent = getCurrentEvent(bean.getDriverId());

                float diff = Utility.getDiffMins(currentEvent.getEventDateTime(), bean.getReportDate());


                // if previous event is ILC or Driving and time elapsed from previous event is more than 30 minutes then add ILC
                if ((currentEvent.getEventType() == 2 || (currentEvent.getEventType() == 1 && currentEvent.getEventCode() == 3)) && diff > 30f) {

                    // save engine power up event
                    EventCreateAuto(bean);

                    postToServerFg = true;

                    // fire event to make changes in elog fragment if required
                    if (mListener != null)
                        mListener.onEventCapture(bean);
                }


                break;
            case Constants.DEVICE_POWER_GAIN:
/*
                currentEvent = getCurrentEvent(Utility.vehicleId, 7);

                // if diagnostic event is recorded
                if (currentEvent.getEventCode() == 3) {
                    diff = Utility.getDiffMins(currentEvent.getEventDateTime(), bean.getReportDate());

                    // enter malfunction if power gained after 30 minutes
                    if (diff >= 30f) {
                        DiagnosticIndicatorBean.EngineSynchronizationMalfunctionFg = true;
                        // save malfunction for engine synchronization compliance
                        DiagnosticMalfunction.saveDiagnosticIndicatorByCode("E", 1, "EngineSynchronizationMalfunctionFg");
                    }


                    // enter diagnostic
                    DiagnosticIndicatorBean.EngineSynchronizationDiagnosticFg = false;
                    // save data diagnostic event for engine synchronization
                    DiagnosticMalfunction.saveDiagnosticIndicatorByCode("2", 4, "EngineSynchronizationDiagnosticFg");

                    // clear malfunction
                    if (diff > 30f) {
                        DiagnosticIndicatorBean.EngineSynchronizationMalfunctionFg = false;
                        // clear malfunction for engine synchronization compliance
                        DiagnosticMalfunction.saveDiagnosticIndicatorByCode("E", 2, "EngineSynchronizationMalfunctionFg");
                    }

                    postToServerFg = true;
                }*/

                break;
            case Constants.MIDNIGHT_EVENT:
                break;
            case Constants.GAUGE_CLUSTER:

                // update current date from rtc date in ELD 2020 device
                if (bean.isBTConnectFg())
                    Utility._CurrentDateTime = bean.getReportDate();

                if (bean.getGpsStatus() == 1) {
                    Utility.currentLocation.setLocationDate(bean.getGpsDate());
                    Utility.currentLocation.setLongitude(bean.getLongitude());
                    Utility.currentLocation.setLatitude(bean.getLatitude());
                    Utility.currentLocation.setBearing(bean.getHeading());
                    Utility.currentLocation.setGpsStatus(1);
                }

                // if development device
                if (Utility.IMEI.equals("356961076533234")) {
                    Utility._CurrentDateTime = Utility.getCurrentDateTime();
                    Utility.currentLocation.setLatitude(49.0416568);
                    Utility.currentLocation.setLongitude(-122.2884158);

                }

                // refresh vehicle related data
                refreshVehicleData(bean);

                // check if driver id has been changed
                if (Utility.activeUserId == 0 && mLoginListener != null) {
                    mLoginListener.driverOnELD(driverId);
                }

                // if active driver is not same as set in eld 2020 device
                if (mListener != null && Utility.activeUserId > 0 && Utility.activeUserId != driverId) {
                    mListener.onMismatchDriver();
                }
                break;
            case Constants.BLUETOOTH_CONNECTED:
                Utility.motionFg = bean.getSpeed() > 8f;

                // hide gauge cluster
                if (mListener != null)
                    mListener.onVehicleMotionChange(Utility.motionFg);
                break;
            case Constants.BLUETOOTH_DISCONNECTED: // if bluetooth is disconnected don't login
                mListener.onVehicleMotionChange(false);
                break;
            case Constants.GPS_FIXED:
                break;
            case Constants.GPS_NOT_FIXED: // malfunction if older than 5 miles (it is skipped)
                break;
            case Constants.GAUGE_CLUSTER_SHOW:
            case Constants.GAUGE_CLUSTER_HIDE: // malfunction if older than 5 miles (it is skipped)

                // on receiving gauge cluster show
                if (reportId == Constants.GAUGE_CLUSTER_SHOW) {
                    // trigger driving if current event is non driving
                    triggeredFg = TriggerDriving(bean);

                    // deduct balance if it's not deducted
                    if (triggeredFg) {
                        Utility.DeviceBalancePost();
                    }
                    postToServerFg = triggeredFg;
                } else { // on receiving gauge cluster hide

                    if (Utility.UnidentifiedDrivingStartTime > 0) {

                        Utility.UnidentifiedDrivingTime = Utility.UnidentifiedDrivingTime + (System.currentTimeMillis() / 1000l - Utility.UnidentifiedDrivingStartTime);

                        Utility.UnidentifiedDrivingStartTime = 0;
                    }

                    // if unidentified driver then add on duty right way on stop
                    if (bean.getDriverId() == Utility.unIdentifiedDriverId) {

                        bean.setEventType(1);
                        bean.setEventCode(4);
                        bean.setEventCodeDescription(context.getString(R.string.duty_status_changed_to_on_duty));

                        // get current duty status
                        currentStatus = getCurrentDutyStatus(bean.getDriverId());

                        // only save automatic on duty status if current status is driving
                        if (currentStatus == 3) {
                            // save on Duty event
                            EventCreateAuto(bean);
                            postToServerFg = true;
                        }
                    }
                }

                // only show hide gauge cluster if btb is connected
                if (bean.isBTConnectFg()) {
                    // vehicle is not in motion
                    Utility.motionFg = (reportId == Constants.GAUGE_CLUSTER_SHOW);

                    // show/hide gauge cluster
                    if (mListener != null)
                        mListener.onVehicleMotionChange(Utility.motionFg);

                }

                if (postToServerFg) {

                    // fire event to make changes in elog fragment if required
                    if (mListener != null)
                        mListener.onEventCapture(bean);
                }

                break;
        }

        // post data to server
        if (postToServerFg && mListener != null) {
            mListener.postToServer();
        }

        //"355154088024746"
        //sendAck(bean.getSequenceId(), bean.getSerialNo());
        //sendAck(bean.getSequenceId(), "355154088024746");
        return id;
    }

    // Created By: Deepak Sharma
    // Created Date: 15 January 2015
    // Purpose: get event sequence id for new record.
    public int getMaxEventSequenceId() {
        int id = -1;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select max(EventSequenceId) from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where VehicleId=?", new String[]{Utility.vehicleId + ""});
            if (cursor.moveToNext()) {

                id = cursor.getInt(0);
            }

        } catch (Exception exe) {
            LogDB.writeLogs(TAG, "::getMaxEventSequenceId Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return id;
    }

    // Created By: Deepak Sharma
    // Created Date: 16 December 2019
    // Purpose: get event sequence id for new record.
    public int getEventSequenceId() {
        int id = Utility.getPreferences("eventsequenceid", 0);

        // if event sequence id is not saved in preferences then get it from database
        if (id == 0) {
            id = getMaxEventSequenceId() + 1;
        }

        // save incremented value in preferences
        Utility.savePreferences("eventsequenceid", (id + 1));

        return id;
    }


    public void EventCreateAuto(int driverId, int eventType, int eventCode, String eventCodeDescription) {

        int coDriverId = Utility.user1.getAccountId() == driverId ? Utility.user2.getAccountId() : Utility.user1.getAccountId();

        String eventDateTime = Utility._CurrentDateTime;


        String odometerReading = Double.valueOf(CanMessages.OdometerReading).intValue() + "";
        String engineHours = String.format("%.1f", Double.valueOf(CanMessages.EngineHours));

        // if personal use then get lat,lon upto 1 decimal place else upto 2 decimal place
        String lat = (Utility.truncate(Utility.currentLocation.getLatitude(), 2)) + "";
        String lon = (Utility.truncate(Utility.currentLocation.getLongitude(), 2)) + "";

        // get current address
        String address = reverseGeoCode(lat, lon);

        // save event entered by driver
        EventCreate(driverId, eventDateTime, eventType, eventCode, eventCodeDescription, 1, 1, "", odometerReading, engineHours, lat, lon, address, coDriverId);

    }
    // Created By: Deepak Sharma
    // Created Date: 11 Feb 2020
    // Purpose: create events auto from bluetooth reports
    public void EventCreateAuto(ReportBean data) {
        try {
            // convert utc real time to time in driver's time zone
            String reportDateTime = "";
            EventBean bean = new EventBean();

            bean.setOnlineEventId(0);
            bean.setDriverId(data.getDriverId());
            bean.setEventSequenceId(getEventSequenceId());
            bean.setEventType(data.getEventType());
            bean.setEventCode(data.getEventCode());
            bean.setEventCodeDescription(data.getEventCodeDescription());
            bean.setEventRecordOrigin(1);
            bean.setEventRecordStatus(1);

            bean.setOdometerReading(Double.valueOf(data.getOdometerReading()).intValue() + "");
            bean.setEngineHour(String.format("%.1f", Double.valueOf(data.getTotalEngineHour())));
            bean.setEventDateTime(data.getReportDate());

            bean.setLatitude((data.getEventType() == 3 && data.getEventCode() == 1 ? Utility.truncate(data.getLatitude(), 1) : Utility.truncate(data.getLatitude(), 2)) + "");
            bean.setLongitude((data.getEventType() == 3 && data.getEventCode() == 1 ? Utility.truncate(data.getLongitude(), 1) : Utility.truncate(data.getLongitude(), 2)) + "");

            // get current address
            String address = reverseGeoCode(bean.getLatitude(), bean.getLongitude());

            bean.setLocationDescription(address);

            bean.setCreatedBy(data.getDriverId());
            bean.setCreatedDate(data.getReportDate());
            bean.setModifiedBy(data.getDriverId());
            bean.setModifiedDate(data.getReportDate());
            bean.setVehicleId(data.getVehicleId());
            bean.setMotorCarrier(Utility.CarrierName);
            bean.setTimeZoneOffsetUTC(Utility.TimeZoneOffsetUTC);
            bean.setAnnotation("");

            // set default values
            bean.setDistanceSinceLastValidCoordinate("0"); // 0 as we get location with every event and it's valid
            bean.setAccumulatedVehicleMiles("0");
            bean.setElaspsedEngineHour("0");
            bean.setMalfunctionIndicatorFg(Utility.malFunctionIndicatorFg ? 1 : 0);
            bean.setDataDiagnosticIndicatorFg(Utility.dataDiagnosticIndicatorFg ? 1 : 0);
            bean.setShippingDocumentNo(Utility.ShippingNumber);
            bean.setTrailerNo(Utility.TrailerNumber);
            bean.setDiagnosticCode("");
            bean.setMalfunctionIndicatorFg(0);
            bean.setDataDiagnosticIndicatorFg(0);
            bean.setCoDriverId(data.getCoDriverId());
            bean.setStatusId(1);
            bean.setSyncFg(0);

            // save event to database
            EventSave(bean);

        } catch (Exception e) {
            LogDB.writeLogs(TAG, "::EventCreateAuto Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    // Created By: Deepak Sharma
    // Created Date: 14 Feb 2020
    // Purpose: create events mannually
    public void EventCreate(int driverId, String eventDateTime, int eventType, int eventCode, String eventDescription, int eventOrigin, int eventStatus, String annotation, String odometerReading, String engineHours, String lat, String lon, String location, int coDriverId) {
        try {
            EventBean bean = new EventBean();

            bean.setOnlineEventId(0);
            bean.setDriverId(driverId);
            bean.setEventSequenceId(getEventSequenceId());
            bean.setEventType(eventType);
            bean.setEventCode(eventCode);
            bean.setEventCodeDescription(eventDescription);
            bean.setEventRecordOrigin(eventOrigin);
            bean.setEventRecordStatus(eventStatus);

            bean.setOdometerReading(odometerReading);
            bean.setEngineHour(engineHours);
            bean.setEventDateTime(eventDateTime);
            bean.setLatitude(lat);
            bean.setLongitude(lon);
            bean.setLocationDescription(location);

            bean.setCreatedBy(driverId);
            bean.setCreatedDate(eventDateTime);
            bean.setModifiedBy(driverId);
            bean.setModifiedDate(eventDateTime);
            bean.setVehicleId(Utility.vehicleId);
            bean.setMotorCarrier(Utility.CarrierName);
            bean.setTimeZoneOffsetUTC(Utility.TimeZoneOffsetUTC);
            bean.setAnnotation(annotation);

            // set default values
            bean.setDistanceSinceLastValidCoordinate("0"); // 0 as we get location with every event and it's valid
            bean.setAccumulatedVehicleMiles("0");
            bean.setElaspsedEngineHour("0");
            bean.setMalfunctionIndicatorFg(Utility.malFunctionIndicatorFg ? 1 : 0);
            bean.setDataDiagnosticIndicatorFg(Utility.dataDiagnosticIndicatorFg ? 1 : 0);
            bean.setShippingDocumentNo(Utility.ShippingNumber);
            bean.setTrailerNo(Utility.TrailerNumber);
            bean.setDiagnosticCode("");
            bean.setMalfunctionIndicatorFg(0);
            bean.setDataDiagnosticIndicatorFg(0);
            bean.setCoDriverId(coDriverId);
            bean.setStatusId(1);
            bean.setSyncFg(0);

            // save event to database
            EventSave(bean);

        } catch (Exception e) {
            LogDB.writeLogs(TAG, "::EventCreateAuto Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add or update events in database
    public int EventSave(EventBean bean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int eventId = 0;
        try {
            int dailylogId;
            int ruleId = 0;

            helper = new MySQLiteOpenHelper(context);
            database = helper.getWritableDatabase();
            String logDate = Utility.getDate(bean.getEventDateTime());

            // get dailylog id for particular event
            cursor = database.rawQuery("select _id from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where LogDate=? and driverId=?"
                    , new String[]{logDate, bean.getDriverId() + ""});

            // get dailylog for the event record
            if (cursor.moveToNext()) {
                dailylogId = cursor.getInt(0);
            } else {
                dailylogId = DailyLogSave(database, bean.getDriverId(), logDate, bean.getEventDateTime(), bean.getOdometerReading());
            }

            bean.setDailyLogId(dailylogId);

            cursor.close();

            // get current rule
            cursor = database.rawQuery("select RuleId from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE +
                    " where DriverId=?" +
                    " order by RuleStartTime desc LIMIT 1", new String[]{Integer.toString(bean.getDriverId())});

            if (cursor.moveToFirst()) {
                ruleId = cursor.getInt(0);
            }

            bean.setRuleId(ruleId);

            cursor.close();


            // get dailylog id
            // get rule id
            // get odometer since last power on event
            // get engine hours since last power on event
            int distanceSinceLastValidCoordinate = 0, accumulatedMiles = 0;

            double elapsedEngineHours = 0, engineHoursSincePowerOn = Double.valueOf(bean.getEngineHour());

            int odometerSincePowerOn = Double.valueOf(bean.getOdometerReading()).intValue();

            // if event type is 1,2 or 3 then calculate accumulated miles, elapsed engine hours
            if (bean.getEventType() <= 3) {
                try {


                    // get latest record with engine power up event
                    cursor = database.rawQuery("select OdometerReading, EngineHour from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                            " where VehicleId=? and EventType=6 and EventCode in (1,2) and EventDateTime<=? order by EventDateTime desc Limit 1 ", new String[]{Utility.vehicleId + "", bean.getEventDateTime()});

                    // if table has got record then get odometer reading and engine hours since last power on
                    if (cursor.moveToFirst()) {
                        odometerSincePowerOn = Double.valueOf(cursor.getDouble(cursor.getColumnIndex("OdometerReading"))).intValue();
                        engineHoursSincePowerOn = cursor.getDouble(cursor.getColumnIndex("EngineHour"));
                    }

                    // accumulated mile is the distance driven since last engine power on
                    accumulatedMiles = Double.valueOf(bean.getOdometerReading()).intValue() - odometerSincePowerOn;

                    // we allow value between 0 to 9999 as output file send logic will throw error
                    accumulatedMiles = (accumulatedMiles < 0 || accumulatedMiles > 9999) ? 0 : accumulatedMiles;

                    bean.setAccumulatedVehicleMiles(accumulatedMiles + "");

                    // elapsed engine hours is the time  elapsed since last engine power on
                    elapsedEngineHours = Double.valueOf(bean.getEngineHour()) - engineHoursSincePowerOn;

                    // we allow value between 0 to 99.9d as output file send logic will throw error
                    elapsedEngineHours = (elapsedEngineHours < 0d || elapsedEngineHours > 99.9d) ? 0d : elapsedEngineHours;

                    bean.setElaspsedEngineHour(String.format("%.1f", elapsedEngineHours));

                    cursor.close();


                } catch (Exception exe) {

                    LogDB.writeLogs(TAG, "::EventSave Accumulate Miles Error:", exe.getMessage(), Utility.printStackTrace(exe));
                }

            }

            ContentValues values = new ContentValues();

            values.put("OnlineEventId", bean.getOnlineEventId());
            values.put("DriverId", bean.getDriverId());
            values.put("VehicleId", bean.getVehicleId());
            values.put("EventSequenceId", bean.getEventSequenceId());
            values.put("EventType", bean.getEventType());
            values.put("EventCode", bean.getEventCode());
            values.put("EventCodeDescription", bean.getEventCodeDescription());
            values.put("OdometerReading", bean.getOdometerReading());
            values.put("EngineHour", bean.getEngineHour());
            values.put("EventRecordOrigin", bean.getEventRecordOrigin());
            values.put("EventRecordStatus", bean.getEventRecordStatus());
            values.put("EventDateTime", bean.getEventDateTime());
            values.put("Latitude", bean.getLatitude());
            values.put("Longitude", bean.getLongitude());
            values.put("LocationDescription", bean.getLocationDescription());
            values.put("DailyLogId", bean.getDailyLogId());
            values.put("CreatedBy", bean.getCreatedBy());
            values.put("CreatedDate", bean.getCreatedDate());
            values.put("StatusId", bean.getStatusId());
            values.put("SyncFg", bean.getSyncFg());

            values.put("DistanceSinceLastValidCoordinate", bean.getDistanceSinceLastValidCoordinate());
            values.put("MalfunctionIndicatorFg", bean.getMalfunctionIndicatorFg());
            values.put("DiagnosticCode", bean.getDiagnosticCode());
            values.put("DataDiagnosticIndicatorFg", bean.getDataDiagnosticIndicatorFg());
            values.put("Annotation", bean.getAnnotation());
            values.put("AccumulatedVehicleMiles", bean.getAccumulatedVehicleMiles());
            values.put("ElaspsedEngineHour", bean.getElaspsedEngineHour());
            values.put("MotorCarrier", bean.getMotorCarrier());
            values.put("ShippingDocumentNo", bean.getShippingDocumentNo());
            values.put("TrailerNo", bean.getTrailerNo());
            values.put("TimeZoneOffsetUTC", bean.getTimeZoneOffsetUTC());
            values.put("CheckSum", bean.getCheckSum());
            values.put("CoDriverId", bean.getCoDriverId());
            values.put("Splitfg", bean.getSplitSleep());
            values.put("RuleId", bean.getRuleId());
            values.put("TimeZoneShortName", ZoneList.getTimezoneName(false));
            values.put("ReducedFg", (bean.isReduceFg() ? 1 : 0));
            values.put("WaitingFg", (bean.isWaitingFg() ? 1 : 0));
            eventId = (int) database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT,
                    "_id,ModifiedBy,ModifiedDate", values);

            // revert certify status of daily log
            if (eventId > 0) {
                DailyLogCertifyRevert(bean.getDriverId(), bean.getEventDateTime(), dailylogId, database);
            }

        } catch (Exception e) {

            LogDB.writeLogs(TAG, "::EventSave Error:", e.getMessage(), Utility.printStackTrace(e));
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Log.i("EventDB", "Finally Error: " + e.getMessage());
                Utility.printError(e.getMessage());
            }
        }
        return eventId;
    }


    // Created By: Deepak Sharma
    // Created Date: 12 Feb 2020
    // Purpose: add or update dailylog in database
    public int DailyLogSave(SQLiteDatabase database, int driverId, String logDate, String createdDate, String odometerReading) {

        int logId = 0;
        try {
            ContentValues values = new ContentValues();

            values.put("ShippingId", Utility.ShippingNumber);
            values.put("TrailerId", Utility.TrailerNumber);
            values.put("Commodity", "");
            values.put("EndOdometerReading", odometerReading);
            // get Defered Day value from bean
            values.put("DeferedDay", 0);

            values.put("CertifyFG", 0);
            values.put("OnlineDailyLogId", 0);
            values.put("DriverId", driverId);
            values.put("LogDate", logDate);
            values.put("StartTime", "00:00:00");
            values.put("CreatedBy", driverId);
            values.put("CreatedDate", createdDate);
            values.put("StartOdometerReading", odometerReading);

            values.put("DrivingTimeRemaining", GPSData.DrivingTimeRemaining);
            values.put("WorkShiftTimeRemaining", GPSData.WorkShiftRemaining);
            values.put("TimeRemaining70", GPSData.TimeRemaining70);
            values.put("TimeRemaining120", GPSData.TimeRemaining120);
            values.put("TimeRemainingUS70", GPSData.TimeRemainingUS70);
            values.put("TimeRemainingReset", GPSData.TimeRemainingReset);
            values.put("StatusId", 1);
            values.put("SyncFg", 0);

            logId = (int) database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG,
                    "_id,ModifiedBy,ModifiedDate", values);


        } catch (Exception e) {
            LogDB.writeLogs(TAG, "::DailyLogSave Error:", e.getMessage(), Utility.printStackTrace(e));

        }
        return logId;
    }


    // Created By: Deepak Sharma
    // Created Date: 12 Feb 2020
    // Purpose: revert certify
    private void DailyLogCertifyRevert(int driverId, String eventDatetime, int logId, SQLiteDatabase database) {
        ContentValues values = new ContentValues();

        values.put("CertifyFG", 0);
        values.put("SyncFg", 0);
        values.put("ModifiedBy", driverId);
        values.put("ModifiedDate", eventDatetime);

        database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                " _id = ?", new String[]{logId + ""});
    }

    // Created By: Deepak Sharma
    // Created Date: 5 September 2018
    // Purpose: get nearest location
    private String reverseGeoCode(String lat, String lon) {

        String currentLocation = "";
        try {
            double latitude = Double.valueOf(lat);
            double longitude = Double.valueOf(lon);

            // if reverse geocoder is not initialized
            if (!MainActivity.objReverseGeocoder.loadedFg)
                return currentLocation;

            if (latitude <= 0 || longitude > -52d || longitude < -139d) {

                return currentLocation;
            }

            Log.i("ReverseGeocode", "ReverseGeocode:Starts");

            InputStream inputStream = null;

            String fileName = MainActivity.objReverseGeocoder.getFileName(latitude);

            if (!fileName.isEmpty()) {
                inputStream = Utility.context.getAssets().open("Locations/" + fileName);
            }

            GeoName nearestPlace = MainActivity.objReverseGeocoder.nearestPlace(latitude, longitude, inputStream);

            String distanceUnit = Utility._appSetting.getUnit() == 1 ? "KMs " : "Miles ";
            String location = String.format("%.2f", getDistance(nearestPlace, latitude, longitude)) + " " + distanceUnit + getBearing(nearestPlace, latitude, longitude) + " " + nearestPlace.name + ", " + nearestPlace.state;

            if (!location.isEmpty()) {
                currentLocation = location;
            }
        } catch (Exception exe) {

            LogDB.writeLogs(TAG, "reverseGeoCode", exe.getMessage(), Utility.printStackTrace(exe));

        }
        return currentLocation;
    }


    public float getDistance(GeoName nearestPlace, double latitude, double longitude) {

        Location loc1 = new Location("");  //for initial location ; here from gps
        loc1.setLatitude(latitude); //getting latitude reading from gps
        loc1.setLongitude(longitude); //getting longitude reading from gps

        Location loc2 = new Location(""); // for nearest palce location
        loc2.setLatitude(nearestPlace.latitude);
        loc2.setLongitude(nearestPlace.longitude);

        float distanceInMeters = loc1.distanceTo(loc2); //inbuilt menthod to return distance in meter between two locations
        return (distanceInMeters / (Utility._appSetting.getUnit() == 1 ? 1000f : 1609.34f)); //dividing distance in meters by 1609.34(approx) to get in miles
    }

    //    gives the direction
    public String getBearing(GeoName nearestPlace, double latitude, double longitude) {

        Location loc1 = new Location("");  //for initial location ; here from gps
        loc1.setLatitude(latitude); //getting latitude reading from gps
        loc1.setLongitude(longitude); //getting longitude reading from gps

        Location loc2 = new Location(""); // for nearest palce location
        loc2.setLatitude(nearestPlace.latitude);
        loc2.setLongitude(nearestPlace.longitude);

        float bearing = (loc2.bearingTo(loc1));
        if (bearing < 0) {
            bearing += 360f;
        }
        bearing = Math.round(bearing / 22.5f);

        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW", "N"};
        return directions[(int) bearing];

    }

    // Created By: Deepak Sharma
    // Created Date: 13 Feb 2020
    // Purpose:  call this function only when engine power on
    // this is to check power off cycle after special status
    // if current status of driver is spcial driving status and we found engine off after this status
    // then on engine power on call this method to open dialog box to ask him if he wants to continue using personal use
    // if he is in yard move then just clear status
    public int showDialogToContinueSpecialStatus(ReportBean bean) {

        // 0 Do nothing, 1-> show dialog for confirmation to continue PC, 2-> clear YM
        int status = 0;

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select EventDateTime,EventType ,EventCode from " +
                            MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventType in (1,3) and EventRecordStatus in (1,3) order by EventDateTime desc LIMIT 1"
                    , new String[]{Integer.toString(bean.getDriverId())});


            if (cursor.moveToNext()) {

                int eventType = cursor.getInt(cursor.getColumnIndex("EventType"));
                int eventCode = cursor.getInt(cursor.getColumnIndex("EventCode"));
                String eventDateTime = cursor.getString(cursor.getColumnIndex("EventDateTime"));

                // if special status is present
                if (eventType == 3 && eventCode > 0) {
                    Cursor cursor1 = database.rawQuery("select EventDateTime,EventType ,EventCode from " +
                                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventType=6 and EventRecordStatus in (1,3) and EventDateTime>? order by EventDateTime desc LIMIT 1"
                            , new String[]{Integer.toString(bean.getDriverId()), eventDateTime});

                    // if found engine on off record after special driving status
                    if (cursor1.moveToFirst()) {

                        // get if staus is on or off after pc/ym
                        boolean engineOffFg = cursor1.getInt(cursor.getColumnIndex("EventCode")) >= 3;

                        // engine is off after special driving status Personal use
                        if (engineOffFg && eventCode == 1) {
                            // show dialog
                            status = 1;
                        }
                        // engine is off after special driving status Yard Move
                        else if (engineOffFg && eventCode == 2) {
                            // Clear duty status
                            status = 2;
                        }
                    }
                    cursor1.close();
                }
            }

        } catch (Exception exe) {

            LogDB.writeLogs(TAG, "::showDialogToContinueSpecialStatus Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 13 Feb 2020
    // Purpose:  get current duty status
    public int getCurrentDutyStatus(int driverId) {

        // duty status
        int status = 1;

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select EventDateTime,EventType ,EventCode from " +
                            MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventType in (1,3) and EventRecordStatus in (1,3) and eventcode!=0 order by EventDateTime desc LIMIT 1"
                    , new String[]{Integer.toString(driverId)});


            if (cursor.moveToNext()) {

                int eventType = cursor.getInt(cursor.getColumnIndex("EventType"));
                int eventCode = cursor.getInt(cursor.getColumnIndex("EventCode"));
                status = (eventType == 1 ? eventCode : (eventCode + 4));
            }


        } catch (Exception exe) {

            LogDB.writeLogs(TAG, "::showDialogToContinueSpecialStatus Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 13 Feb 2020
    // Purpose:  get current duty status
    public EventBean getCurrentEvent(int driverId) {

        // duty status
        EventBean bean = new EventBean();

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id, EventDateTime,EventType ,EventCode from " +
                            MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventType in (1,2,3) and EventRecordStatus in (1,3) and eventcode!=0 order by EventDateTime desc LIMIT 1"
                    , new String[]{Integer.toString(driverId)});


            if (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String eventDateTime = cursor.getString(cursor.getColumnIndex("EventDateTime"));
                int eventType = cursor.getInt(cursor.getColumnIndex("EventType"));
                int eventCode = cursor.getInt(cursor.getColumnIndex("EventCode"));
                bean.set_id(id);
                bean.setEventDateTime(eventDateTime);
                bean.setEventType(eventType);
                bean.setEventCode(eventCode);
            }


        } catch (Exception exe) {

            LogDB.writeLogs(TAG, "::showDialogToContinueSpecialStatus Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return bean;
    }


    // Created By: Deepak Sharma
    // Created Date: 13 Feb 2020
    // Purpose:  get current duty status
    public EventBean getCurrentEvent(int vehicleId, int eventType) {

        // duty status
        EventBean bean = new EventBean();

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id, EventDateTime,EventType ,EventCode,OdometerReading from " +
                            MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where VehicleId=? and EventType=? and EventRecordStatus in (1,3) and eventcode!=0 order by EventDateTime desc LIMIT 1"
                    , new String[]{Integer.toString(vehicleId), eventType + ""});


            if (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String eventDateTime = cursor.getString(cursor.getColumnIndex("EventDateTime"));
                int eventCode = cursor.getInt(cursor.getColumnIndex("EventCode"));
                String odometer = cursor.getString(cursor.getColumnIndex("OdometerReading"));
                bean.set_id(id);
                bean.setEventDateTime(eventDateTime);
                bean.setEventType(eventType);
                bean.setEventCode(eventCode);
                bean.setOdometerReading(odometer);
            }


        } catch (Exception exe) {

            LogDB.writeLogs(TAG, "::showDialogToContinueSpecialStatus Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return bean;
    }

    // Created By: Deepak Sharma
    // Created Date: 13 Feb 2020
    // Purpose:  get if current duty status is special status
    public int getSpecialStatusFg(int driverId) {

        // duty status
        int status = 0;

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select EventDateTime,EventType ,EventCode from " +
                            MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventType in (1,3) and EventRecordStatus in (1,3) order by EventDateTime desc LIMIT 1"
                    , new String[]{Integer.toString(driverId)});


            if (cursor.moveToNext()) {

                int eventType = cursor.getInt(cursor.getColumnIndex("EventType"));
                int eventCode = cursor.getInt(cursor.getColumnIndex("EventCode"));

                if (eventType == 3 && eventCode != 0) {
                    status = 1;
                }
            }


        } catch (Exception exe) {

            LogDB.writeLogs(TAG, "::getSpecialStatusFg Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 13 Feb 2020
    // Purpose:  add driving event when meet required conditions
    private boolean TriggerDriving(ReportBean bean) {

        // return true if driving event is triggered
        boolean status = false;
        // get current duty status
        int currentStatus = getCurrentDutyStatus(bean.getDriverId());

        // if current status is not driving && current status is not special category
        if (currentStatus != 3) {

            status = true;
            // if personal use dialog is open then close it and clear pc
            if (Utility.PCDialogFg) {

                bean.setEventType(3);
                bean.setEventCode(0);
                bean.setEventCodeDescription(context.getString(R.string.pc_ym_yt_cleared));
                EventCreateAuto(bean);

                Utility.PCDialogFg = false;

                if (mListener != null) {
                    mListener.onSpecialStatusChange(0);

                    // hide pc dialog
                    mListener.hidePCDialog();
                }
            }

            if (Utility.PCDialogFg || bean.getSpecialStatus() == 0 || bean.getDriverId() == Utility.unIdentifiedDriverId) // if special status is cleared then
            {

                bean.setEventType(1);
                bean.setEventCode(3);
                bean.setEventCodeDescription(context.getString(R.string.duty_status_changed_to_driving));

                // add driving event
                EventCreateAuto(bean);
            }
        }

        // check if driving without login for more than half an hour
        if (bean.getDriverId() == Utility.unIdentifiedDriverId) {

            // record start time
            if (Utility.UnidentifiedDrivingStartTime == 0)
                Utility.UnidentifiedDrivingStartTime = System.currentTimeMillis() / 1000l;

            Utility.UnidentifiedDrivingTime = Utility.UnidentifiedDrivingTime + (System.currentTimeMillis() / 1000l - Utility.UnidentifiedDrivingStartTime);

            // if time is greater than half an hours
            if (Utility.UnidentifiedDrivingTime > Constants.HALF_HOUR_DRIVING) {

                //Data diagnostic event for unidentified driving time
                if (!DiagnosticIndicatorBean.UnidentifiedDrivingDiagnosticFg) {
                    DiagnosticIndicatorBean.UnidentifiedDrivingDiagnosticFg = true;
                    // save data diagnostic event for unidentified driver
                    DiagnosticMalfunction.saveDiagnosticIndicatorByCode("5", 3, "UnidentifiedDrivingDiagnosticFg");
                }
            }
        }

        // fire event to make changes in elog fragment if required
        if (mListener != null)
            mListener.onEventCapture(bean);
        return status;
    }

    private void refreshVehicleData(ReportBean data) {
        CanMessages.Speed = Double.valueOf(data.getSpeed()).intValue() + "";

        CanMessages.RPM = data.getRpm() + "";

        CanMessages.EngineHours = String.format("%.1f", data.getTotalEngineHour());

        CanMessages.OdometerReading = String.format("%.2f", data.getOdometerReading());

        // %FL, fuel level from
        // bit resolution is .4
        CanMessages.FuelLevel1 = String.format("%.2f", data.getFuelLevel());

        //%FC
        // Total fuel consumed
        // bit resolution is .1
        CanMessages.TotalFuelConsumed = String.format("%.2f", data.getTotalFuelUsed());

        // %ET, enginecoolant
        CanMessages.CoolantTemperature = data.getCoolantTemperature() + "";

        CanMessages.Voltage = String.format("%.1f", data.getVehicleBatteryLevel());
    }


    public int getCurrentRule(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        int ruleId = 1;
        try {
            helper = new MySQLiteOpenHelper(context);
            database = helper.getReadableDatabase();


            cursor = database.rawQuery("select RuleId from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE +
                            " where DriverId=?" +
                            " order by RuleStartTime desc LIMIT 1"
                    , new String[]{Integer.toString(driverId)});
            if (cursor.moveToFirst()) {
                ruleId = cursor.getInt(0);
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return ruleId;
    }

    // Created By: Deepak Sharma
    // Created Date: 24 March 2020
    // Purpose: fire auto rule change event
    private void autoRuleChange(String countryCode, String ruleDateTime) {
        int driverId = Utility.activeUserId;
        int coDriverId = (driverId == Utility.user1.getAccountId() ? Utility.user2.getAccountId() : Utility.user1.getAccountId());
        // automatic change rule
        int ruleId = 0;
        if (countryCode.equals("CA")) {
            String except = "'3','4'";
            ruleId = DailyLogDB.getPreviousRule(driverId, except);
            if (ruleId == 0) {
                ruleId = 1;
            }

            DailyLogDB.DailyLogRuleSave(driverId, ruleId, ruleDateTime, ruleDateTime);
            if (coDriverId > 0) {
                DailyLogDB.DailyLogRuleSave(coDriverId, ruleId, ruleDateTime, ruleDateTime);
            }

        } else if (countryCode.equals("US")) {
            String except = "'1','2'";
            ruleId = DailyLogDB.getPreviousRule(driverId, except);
            if (ruleId == 0) {
                ruleId = 3;
            }
            DailyLogDB.DailyLogRuleSave(driverId, ruleId, ruleDateTime, ruleDateTime);
            if (Utility.user2.getAccountId() > 0) {
                DailyLogDB.DailyLogRuleSave(coDriverId, ruleId, ruleDateTime, ruleDateTime);
            }
        }

        if (ruleId != 0 && mListener != null) {
            mListener.autoRuleChange(ruleId);
        }

    }

    private void sendAck(long sequenceId, String serialNo) {
        serialNo = String.format("%016X", Long.parseLong(serialNo));

        // acknowledgement conists of 3 parts 1. fix Header, 2. SerialNo in Hex, 3. Sequence Id in Hex
        // FE02 is fix header
        String ack = "FE02" + serialNo + String.format("%04X", sequenceId);

        if (mListener != null) {
            Log.d("onDataReceived", ack);
            mListener.onSendCommand(ack + "\n");
        }
        // return ack;
    }

    public static IReport mListener = null;
    public static ILogin mLoginListener = null;

    public interface ILogin {
        void driverOnELD(int driverId);
    }
    public interface IReport {
        void showPCDialog(ReportBean data);

        void hidePCDialog();

        void onVehicleMotionChange(boolean motionFg);

        void postToServer();

        void onEventCapture(ReportBean data);

        void onSpecialStatusChange(int specialStatusFg);

        void onMismatchDriver();

        void autoRuleChange(int ruleId);

        void setVisionMode();

        void onSendCommand(String command);

        void onMismatchUnitId(String unitId);

        void promptToChangeStatus();

        void onUnitMismatchDialog(boolean showFg);
    }
}