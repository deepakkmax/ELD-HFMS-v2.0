package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.DailyLogBean;
import com.hutchsystems.hutchconnect.beans.DiagnosticIndicatorBean;
import com.hutchsystems.hutchconnect.beans.DutyStatusBean;
import com.hutchsystems.hutchconnect.beans.EventBean;
import com.hutchsystems.hutchconnect.beans.RuleBean;
import com.hutchsystems.hutchconnect.beans.TripInfoBean;
import com.hutchsystems.hutchconnect.common.Ascii;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.ConstantFlag;
import com.hutchsystems.hutchconnect.common.CustomDateFormat;
import com.hutchsystems.hutchconnect.common.DiagnosticMalfunction;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.common.ZoneList;
import com.hutchsystems.hoursofservice.model.DutyStatus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by Deepak.Sharma on 1/14/2016.
 */
public class EventDB {
    // Created By: Deepak Sharma
    // Created Date: 05 August 2016
    // Purpose: get last event date
    public static boolean checkDuplicate(int dailyLogId, String datetime) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        boolean status = false;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DailyLogId=? and EventRecordStatus=1 and EventDateTime=?", new String[]{Integer.toString(dailyLogId), datetime});


            if (cursor.moveToNext()) {
                status = true;
            }
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::checkDuplicate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::checkDuplicate Error:", exe.getMessage(), Utility.printStackTrace(exe));

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
    // Created Date: 05 August 2016
    // Purpose: check duplicate event by time
    public static boolean checkDuplicate(String datetime, int driverId) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        boolean status = false;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventRecordStatus=1 and EventDateTime=?", new String[]{Integer.toString(driverId), datetime});


            if (cursor.moveToNext()) {
                status = true;
            }
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::checkDuplicate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::checkDuplicate Error:", exe.getMessage(), Utility.printStackTrace(exe));

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
    // Created Date: 05 August 2016
    // Purpose: get last event date
    public static String getLastEventDate(int driverId) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        String lastDateTime = Utility.getPreviousDate(-15);
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select EventDateTime from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and eventtype in (1,3) and EventDateTime>? order by EventDateTime desc LIMIT 1", new String[]{Integer.toString(driverId), lastDateTime});


            if (cursor.moveToNext()) {
                lastDateTime = cursor.getString(0);
            }
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::previousDutyStatusGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::previousDutyStatusGet Error:", exe.getMessage(), Utility.printStackTrace(exe));
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return lastDateTime;
    }

    // Created By: Minh Tran
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    // Modified By: Deepak Sharma
    // Modified Date: 8 January 2019
    // purpose: fixed issue if previous event does not exists
    public static EventBean previousDutyStatusGet(int driverId, String eventDatetime) {

        EventBean bean = new EventBean();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select EventDateTime,EventType ,EventCode,CoDriverId from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventDateTime<? and EventType in (1,3) and EventRecordStatus in (1,3)  order by EventDateTime desc LIMIT 1", new String[]{Integer.toString(driverId), eventDatetime});


            if (cursor.moveToNext()) {
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
                bean.setCoDriverId(cursor.getInt(cursor.getColumnIndex("CoDriverId")));
            } else {

                // if previous event doesn't exist then set default midnight time to previous date and off duty event
                String previousDate = Utility.getDate(eventDatetime) + " 00:00:00";
                bean.setEventDateTime(previousDate);
                bean.setEventType(1);
                bean.setEventCode(1);
            }
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::previousDutyStatusGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::previousDutyStatusGet Error:", exe.getMessage(), Utility.printStackTrace(exe));
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

    // Created By: Pallavi Wattamwar
    // Created Date: 6 August 2018
    // Purpose: get EventDurationGet
    public static String EventDurationGet(int driverId, String eventDatetime) {

        EventBean bean = new EventBean();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        Date currentDate = Utility.newDate();
        Date nextEventDate = currentDate;
        String time = "";
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select EventDateTime from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventDateTime > " +
                    "? and EventType in (1,3) and EventRecordStatus=1 and EventCode!=0 order by" +
                    " EventDateTime LIMIT 1", new String[]{Integer.toString(driverId), eventDatetime});

            if (cursor.moveToNext()) {
                nextEventDate = Utility.parse(cursor.getString(cursor.getColumnIndex("EventDateTime")));
            }

            time = Utility.getDuration(Utility.parse(eventDatetime), nextEventDate);

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::EventDurationGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventDurationGet Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return time;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 22 Sept 2020
    // Purpose: get sleeper duration
    public static float SleeperDurationGet(int driverId, String eventDatetime) {

        EventBean bean = new EventBean();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        String currentDate = Utility.getCurrentDateTime();
        String nextEventDate = currentDate;
        float time = 0f;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select EventDateTime from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventDateTime > " +
                    "? and EventType in (1,3) and eventcode!=2 and EventRecordStatus in (1,3) and EventCode!=0 order by" +
                    " EventDateTime LIMIT 1", new String[]{driverId + "", eventDatetime});

            if (cursor.moveToNext()) {
                nextEventDate = cursor.getString(cursor.getColumnIndex("EventDateTime"));
            }

            time = Utility.getDiffMins(eventDatetime, nextEventDate);

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::EventDurationGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventDurationGet Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return time / 60f;
    }

    // Created By: Deepak Sharma
    // Created Date: 20 Oct 2016
    // Purpose: get last duty status of onscreen user
    public static int getCurrentDutyStatus(int driverId) {

        int status = 1;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select EventDateTime,EventType ,EventCode from " +
                            MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventType in (1,3) and EventRecordStatus=1 order by EventDateTime desc LIMIT 1"
                    , new String[]{Integer.toString(driverId)});

            if (cursor.moveToNext()) {
                int eventType = cursor.getInt(cursor.getColumnIndex("EventType"));
                int eventCode = cursor.getInt(cursor.getColumnIndex("EventCode"));
                if (eventType == 1)
                    status = eventCode;
                else
                    status = eventCode + 4;
            }
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::previousDutyStatusGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::previousDutyStatusGet Error:", exe.getMessage(), Utility.printStackTrace(exe));

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

    // Created By: Minh Tran
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static boolean getMissingDataFg(int driverId) {

        boolean status = false;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select EventCode from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventType=1 and EventCode>=3 and EventRecordStatus=1 and Latitude=? order by EventDateTime desc LIMIT 1", new String[]{Integer.toString(driverId), "X"});


            if (cursor.moveToNext()) {
                status = true;
            }
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::previousDutyStatusGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::previousDutyStatusGet Error:", exe.getMessage(), Utility.printStackTrace(exe));

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
    // Created Date: 14 January 2015
    // Purpose: get Engine hours and odometer reading when power on
    public static void getEngineHourOdometerSincePowerOn(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select OdometerReading ,EngineHour from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                            " where EventType=6 and EventCode<=2 order by EventDateTime desc LIMIT 1"
                    , null);
            if (cursor.moveToNext()) {
                Utility.OdometerReadingSincePowerOn = cursor.getString(0);
                Utility.EngineHourSincePowerOn = cursor.getString(1);
            } else {
                Utility.OdometerReadingSincePowerOn = CanMessages.OdometerReading;
                Utility.EngineHourSincePowerOn = CanMessages.EngineHours;

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(EventDB.class.getName() + "::getEngineHourOdometerSincePowerOn Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::getEngineHourOdometerSincePowerOn Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 16 December 2019
    // Purpose: get event sequence id for new record.
    public static int getEventSequenceId() {
        int id = Utility.getPreferences("eventsequenceid", 0);

        // if event sequence id is not saved in preferences then get it from database
        if (id == 0) {
            id = getMaxEventSequenceId() + 1;
        }

        // as per FMCSA requirement event sequenceid must be between 0 to 65535 i.e it must track at least 65536 unique records
        if (id > 65535) {
            id = 0;
        }

        // save incremented value in preferences
        Utility.savePreferences("eventsequenceid", (id + 1));

        return id;
    }

    // Created By: Deepak Sharma
    // Created Date: 15 January 2015
    // Purpose: get event sequence id for new record.
    public static int getMaxEventSequenceId() {
        int id = -1;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select max(EventSequenceId) from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where VehicleId=?", new String[]{Utility.vehicleId + ""});
            if (cursor.moveToNext()) {

                id = cursor.getInt(0);
            }

        } catch (Exception exe) {
            Log.i("EventDB", "Cannot get Event Sequence Id");
            LogFile.write(EventDB.class.getName() + "::getMaxEventSequenceId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::getMaxEventSequenceId Error:", exe.getMessage(), Utility.printStackTrace(exe));

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
    // Created Date: 27 January 2015
    // Purpose: get event id if exists
    private static int getEventId(int driverId, String eventDateTime) {
        int id = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventDateTime=?", new String[]{driverId + "", eventDateTime});
            if (cursor.moveToNext()) {
                id = cursor.getInt(0);
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::getEventId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::getEventId Error:", exe.getMessage(), Utility.printStackTrace(exe));

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
    // Created Date: 27 January 2015
    // Purpose: get event id by created date if exists
    private static int getEventId(String createdDatetime, int driverId) {
        int id = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and CreatedDate=?", new String[]{driverId + "", createdDatetime});
            if (cursor.moveToNext()) {
                id = cursor.getInt(0);
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::getEventId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::getEventId Error:", exe.getMessage(), Utility.printStackTrace(exe));

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
    // Created Date: 27 January 2015
    // Purpose: get event id by created date and eventrecordstatus if exists
    public static int getEventId(int driverId, String createdDatetime, int eventRecordStatus) {
        int id = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and CreatedDate=? and EventRecordStatus=?", new String[]{driverId + "", createdDatetime, eventRecordStatus + ""});
            if (cursor.moveToNext()) {
                id = cursor.getInt(0);
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::getEventId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::getEventId Error:", exe.getMessage(), Utility.printStackTrace(exe));
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
    // Created Date: 27 January 2015
    // Purpose: get event id by created date if exists
    public static int getEventId(String createdDatetime, int driverId, int eventRecordOrigin) {
        int id = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and CreatedDate=? and EventRecordOrigin=?", new String[]{driverId + "", createdDatetime, eventRecordOrigin + ""});
            if (cursor.moveToNext()) {
                id = cursor.getInt(0);
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::getEventId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::getEventId Error:", exe.getMessage(), Utility.printStackTrace(exe));

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

    // Created By: Minh Tran
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static ArrayList<EventBean> EventGet(int driverId) {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,EventDateTime,EventType ,EventCode ,EventCodeDescription,EventRecordOrigin, EventRecordStatus, Latitude ,Longitude ,LocationDescription,OdometerReading from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=?", new String[]{Integer.toString(driverId)});
            while (cursor.moveToNext()) {
                EventBean bean = new EventBean();
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
                bean.setEventRecordOrigin(cursor.getInt(cursor.getColumnIndex("EventRecordOrigin")));
                bean.setEventRecordStatus(cursor.getInt(cursor.getColumnIndex("EventRecordStatus")));
                bean.setEventCodeDescription(cursor.getString(cursor.getColumnIndex("EventCodeDescription")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocationDescription(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setChecked(false);
                list.add(bean);
            }

            Collections.sort(list, EventBean.dateDesc);
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::EventGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventGet Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }

    public static ArrayList<EventBean> EventGetByLogId(int driverId, int logId) {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,DailyLogId,EventDateTime,EventType ,EventCode ,EventCodeDescription,EventRecordOrigin, EventRecordStatus, Latitude ,Longitude ,LocationDescription,OdometerReading,EngineHour,AccumulatedVehicleMiles" +
                    ",ElaspsedEngineHour,ShippingDocumentNo,CoDriverId,Annotation,DistanceSinceLastValidCoordinate,VehicleId,DiagnosticCode,TimeZoneOffsetUTC,TrailerNo,MotorCarrier,MalfunctionIndicatorFg, DataDiagnosticIndicatorFg,RuleId,Splitfg, ReducedFg, WaitingFg from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId in (?) and DailyLogId=? and DailyLogId!=0 and EventRecordStatus in (1,3) order by EventDateTime desc, EventSequenceId desc", new String[]{Integer.toString(driverId), Integer.toString(logId)});

            // true if want to show inactive record and ILC record
            boolean eventDetailFg = Utility.getPreferences("event_detail", false);
            while (cursor.moveToNext()) {
                EventBean bean = new EventBean();
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setDailyLogId(cursor.getInt(cursor.getColumnIndex("DailyLogId")));
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
                bean.setEventRecordOrigin(cursor.getInt(cursor.getColumnIndex("EventRecordOrigin")));
                bean.setEventRecordStatus(cursor.getInt(cursor.getColumnIndex("EventRecordStatus")));
                bean.setEventCodeDescription(cursor.getString(cursor.getColumnIndex("EventCodeDescription")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocationDescription(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setAnnotation(cursor.getString(cursor.getColumnIndex("Annotation")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                if (bean.getOdometerReading() == null) {
                    bean.setOdometerReading("0");
                }

                // if annotation value is null then we ser it as empty
                if (bean.getAnnotation() == null) {
                    bean.setAnnotation("");
                }
                bean.setEngineHour(cursor.getString(cursor.getColumnIndex("EngineHour")));
                bean.setAccumulatedVehicleMiles(cursor.getString(cursor.getColumnIndex("AccumulatedVehicleMiles")));
                bean.setElaspsedEngineHour(cursor.getString(cursor.getColumnIndex("ElaspsedEngineHour")));
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setShippingDocumentNo(cursor.getString(cursor.getColumnIndex("ShippingDocumentNo")));
                bean.setCoDriverId(cursor.getInt(cursor.getColumnIndex("CoDriverId")));

                bean.setVehicleId(cursor.getInt(cursor.getColumnIndex("VehicleId")));
                bean.setDiagnosticCode(cursor.getString(cursor.getColumnIndex("DiagnosticCode")));
                bean.setTimeZoneOffsetUTC(cursor.getString(cursor.getColumnIndex("TimeZoneOffsetUTC")));
                bean.setTrailerNo(cursor.getString(cursor.getColumnIndex("TrailerNo")));
                bean.setMotorCarrier(cursor.getString(cursor.getColumnIndex("MotorCarrier")));
                bean.setMalfunctionIndicatorFg(cursor.getInt(cursor.getColumnIndex("MalfunctionIndicatorFg")));
                bean.setDataDiagnosticIndicatorFg(cursor.getInt(cursor.getColumnIndex("DataDiagnosticIndicatorFg")));

                bean.setDistanceSinceLastValidCoordinate(cursor.getString(cursor.getColumnIndex("DistanceSinceLastValidCoordinate")));

                bean.setReduceFg(cursor.getInt(cursor.getColumnIndex("ReducedFg")) == 1);
                bean.setWaitingFg(cursor.getInt(cursor.getColumnIndex("WaitingFg")) == 1);

                bean.setDriverId(driverId);
                bean.setChecked(false);


                bean.setRuleId(cursor.getInt(cursor.getColumnIndex("RuleId")));
                bean.setSplitSleep(cursor.getInt(cursor.getColumnIndex("Splitfg")));

                if (!ConstantFlag.ELDFg && bean.getEventRecordStatus() != 1)
                    continue;
                // set duration

                if (bean.getEventType() <= 3) {
                    bean.setDuration(EventDurationGet(driverId, bean.getEventDateTime()));
                }
                // as per Gary sir requirement we will allow user to edit mid night event
                /* if (bean.getAnnotation() != null && bean.getAnnotation().equals("mid night event"))
                    continue;*/

                if ((bean.getEventRecordStatus() == 2 || bean.getEventType() == 2) && !eventDetailFg)
                    continue;

                list.add(bean);
            }

            Collections.sort(list, EventBean.dateDesc);
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::EventGetByLogId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventGetByLogId Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }

    public static ArrayList<EventBean> DiagnosticMalFunctionEventGetByCode(int eventcode) {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,EventDateTime,EventType ,EventCode ,EventCodeDescription,EventRecordOrigin, EventRecordStatus, Latitude ,Longitude ,LocationDescription,OdometerReading,ShippingDocumentNo,DiagnosticCode from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where EventType=7 and EventCode=? Order by EventDateTime desc", new String[]{Integer.toString(eventcode)});
            while (cursor.moveToNext()) {
                EventBean bean = new EventBean();
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
                bean.setEventRecordOrigin(cursor.getInt(cursor.getColumnIndex("EventRecordOrigin")));
                bean.setEventRecordStatus(cursor.getInt(cursor.getColumnIndex("EventRecordStatus")));
                bean.setEventCodeDescription(cursor.getString(cursor.getColumnIndex("EventCodeDescription")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocationDescription(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                if (bean.getOdometerReading() == null) {
                    bean.setOdometerReading("0");
                }
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setShippingDocumentNo(cursor.getString(cursor.getColumnIndex("ShippingDocumentNo")));
                bean.setDiagnosticCode(cursor.getString(cursor.getColumnIndex("DiagnosticCode")));
                bean.setChecked(false);
                list.add(bean);
            }

            Collections.sort(list, EventBean.dateDesc);
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::DiagnosticMalFunctionEventGetByCode Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::DiagnosticMalFunctionEventGetByCode Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }

    // Created By: Minh Tran
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static ArrayList<EventBean> DutyStatusChangedEventGetByLogId(int driverId, int logId) {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,DailyLogId,EventDateTime,EventType ,EventCode ,EventCodeDescription,EventRecordOrigin, EventRecordStatus, Latitude ,Longitude ,LocationDescription,Annotation, OdometerReading,EngineHour" +
                    ",AccumulatedVehicleMiles,ElaspsedEngineHour ,ShippingDocumentNo,CoDriverId,CreatedDate,DistanceSinceLastValidCoordinate,VehicleId,DiagnosticCode,TimeZoneOffsetUTC,TrailerNo,MotorCarrier,MalfunctionIndicatorFg, DataDiagnosticIndicatorFg,Splitfg,RuleId,ReducedFg,WaitingFg from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId =? and DailyLogId=? and EventRecordStatus=1 and DailyLogId!=0 and EventType in (1,2,3) and EventCode!=0", new String[]{Integer.toString(driverId), Integer.toString(logId)});

            boolean eventDetailFg = Utility.getPreferences("event_detail", false);
            while (cursor.moveToNext()) {
                EventBean bean = new EventBean();

                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setDailyLogId(cursor.getInt(cursor.getColumnIndex("DailyLogId")));
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
                bean.setEventRecordOrigin(cursor.getInt(cursor.getColumnIndex("EventRecordOrigin")));
                bean.setEventRecordStatus(cursor.getInt(cursor.getColumnIndex("EventRecordStatus")));
                bean.setEventCodeDescription(cursor.getString(cursor.getColumnIndex("EventCodeDescription")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocationDescription(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setAnnotation(cursor.getString(cursor.getColumnIndex("Annotation")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                if (bean.getOdometerReading() == null) {
                    bean.setOdometerReading("0");
                }
                bean.setEngineHour(cursor.getString(cursor.getColumnIndex("EngineHour")));
                bean.setAccumulatedVehicleMiles(cursor.getString(cursor.getColumnIndex("AccumulatedVehicleMiles")));
                bean.setElaspsedEngineHour(cursor.getString(cursor.getColumnIndex("ElaspsedEngineHour")));
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setShippingDocumentNo(cursor.getString(cursor.getColumnIndex("ShippingDocumentNo")));
                bean.setCoDriverId(cursor.getInt(cursor.getColumnIndex("CoDriverId")));

                bean.setVehicleId(cursor.getInt(cursor.getColumnIndex("VehicleId")));
                bean.setDiagnosticCode(cursor.getString(cursor.getColumnIndex("DiagnosticCode")));
                bean.setTimeZoneOffsetUTC(cursor.getString(cursor.getColumnIndex("TimeZoneOffsetUTC")));
                bean.setTrailerNo(cursor.getString(cursor.getColumnIndex("TrailerNo")));
                bean.setMotorCarrier(cursor.getString(cursor.getColumnIndex("MotorCarrier")));
                bean.setMalfunctionIndicatorFg(cursor.getInt(cursor.getColumnIndex("MalfunctionIndicatorFg")));
                bean.setDataDiagnosticIndicatorFg(cursor.getInt(cursor.getColumnIndex("DataDiagnosticIndicatorFg")));

                bean.setDistanceSinceLastValidCoordinate(cursor.getString(cursor.getColumnIndex("DistanceSinceLastValidCoordinate")));

                bean.setReduceFg(cursor.getInt(cursor.getColumnIndex("ReducedFg")) == 1);
                bean.setWaitingFg(cursor.getInt(cursor.getColumnIndex("WaitingFg")) == 1);

                bean.setDriverId(driverId);
                bean.setChecked(false);
                bean.setCreatedDate(cursor.getString(cursor.getColumnIndex("CreatedDate")));
                // if annotation value is null then we ser it as empty
                if (bean.getAnnotation() == null) {
                    bean.setAnnotation("");
                }
                if (bean.getEventType() <= 3) {
                    bean.setDuration(EventDurationGet(driverId, bean.getEventDateTime()));
                }

                bean.setSplitSleep(cursor.getInt(cursor.getColumnIndex("Splitfg")));

                bean.setRuleId(cursor.getInt(cursor.getColumnIndex("RuleId")));

                // as per gary sir requirement we will allow user to edit mid night event
/*                if (bean.getAnnotation() != null && bean.getAnnotation().equals("mid night event"))
                    continue;*/

                if (!eventDetailFg && bean.getEventType() == 2)
                    continue;

                list.add(bean);
            }

            Collections.sort(list, EventBean.dateDesc);
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::DutyStatusChangedEventGetByLogId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::DutyStatusChangedEventGetByLogId Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }

    // Created By: Deepak Sharma
    // Created Date: 12 March 2015
    // Purpose: get events to calculate total distance
    public static ArrayList<TripInfoBean> TripInfoGet(int logId, String date) {
        ArrayList<TripInfoBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            int currentOdometer = Double.valueOf(CanMessages.OdometerReading).intValue();
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select distinct c.VehicleId,c.UnitNo,c.PlateNo,c.VIN from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " e join " +
                    MySQLiteOpenHelper.TABLE_CARRIER + " c on e.VehicleId=c.VehicleId " +
                    " where e.DailyLogId=? and e.EventRecordStatus=1", new String[]{Integer.toString(logId)});

            String currentDate = Utility.getCurrentDate();
            while (cursor.moveToNext()) {
                int vehicleId = cursor.getInt(cursor.getColumnIndex("VehicleId"));
                String unitNo = cursor.getString(cursor.getColumnIndex("UnitNo"));
                String plateNo = cursor.getString(cursor.getColumnIndex("PlateNo"));
                String VIN = cursor.getString(cursor.getColumnIndex("VIN"));

                TripInfoBean bean = new TripInfoBean();
                bean.setVehicleId(vehicleId);
                bean.setUnitNo(unitNo);
                bean.setPlateNo(plateNo);
                bean.setVIN(VIN);

                Cursor cursor1 = database.rawQuery("select EventDateTime, OdometerReading, EngineHour from " +
                        MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DailyLogId=? and VehicleId=? and EventRecordStatus=1 order by EventDateTime Limit 1", new String[]{Integer.toString(logId), Integer.toString(vehicleId)});
                int startOR = 0, endOR = 0;
                String startEngineHour = "", endEngineHour = "";
                if (cursor1.moveToNext()) {

                    startOR = cursor1.getInt(cursor1.getColumnIndex("OdometerReading"));
                    startEngineHour = cursor1.getString(cursor1.getColumnIndex("EngineHour"));

                }

                cursor1.close();

                cursor1 = database.rawQuery("select  EventDateTime,OdometerReading, EngineHour from " +
                        MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DailyLogId=? and VehicleId=? and OdometerReading>0 and EventRecordStatus=1 order by EventDateTime desc Limit 1", new String[]{Integer.toString(logId), Integer.toString(vehicleId)});
                if (cursor1.moveToNext()) {
                    endOR = cursor1.getInt(cursor1.getColumnIndex("OdometerReading"));
                    endEngineHour = cursor1.getString(cursor1.getColumnIndex("EngineHour"));
                }

                if (bean.getVehicleId() == Utility.vehicleId && currentDate.equals(date) && currentOdometer > 0) {
                    endOR = Double.valueOf(CanMessages.OdometerReading).intValue();
                    endEngineHour = CanMessages.EngineHours;
                }

                cursor1.close();
                String unit = "Kms";
                if (Utility._appSetting.getUnit() == 2) {
                    startOR = Double.valueOf(startOR * .62137d).intValue();
                    endOR = Double.valueOf(endOR * .62137d).intValue();
                    unit = "Miles";
                }

                bean.setStartOdometerReading(startOR);
                bean.setStartEngineHour(startEngineHour);
                bean.setEndOdometerReading(endOR);
                bean.setEndEngineHour(endEngineHour);
                bean.setOdoUnit(unit);
                list.add(bean);
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::TripInfoGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::TripInfoGet Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }

    // Created By: Deepak Sharma
    // Created Date: 12 March 2015
    // Purpose: get events to calculate total distance
    public static ArrayList<EventBean> TotalDistanceGetByLogId(int logId) {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select EventType,EventCode ,OdometerReading, EngineHour from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DailyLogId=? and EventType<=3 and EventRecordStatus=1 order by EventDateTime", new String[]{Integer.toString(logId)});
            while (cursor.moveToNext()) {
                EventBean bean = new EventBean();
                int eventType = cursor.getInt(cursor.getColumnIndex("EventType"));
                int eventCode = cursor.getInt(cursor.getColumnIndex("EventCode"));
                bean.setEventType(eventType);
                bean.setEventCode(eventCode);
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                if (bean.getOdometerReading() == null) {
                    bean.setOdometerReading("0");
                }
                bean.setEngineHour(cursor.getString(cursor.getColumnIndex("EngineHour")));
                list.add(bean);
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::TotalDistanceGetByLogId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::TotalDistanceGetByLogId Error:", exe.getMessage(), Utility.printStackTrace(exe));
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }


    // Created By: Deepak Sharma
    // Created Date: 27 April 2018
    // Purpose: get events to calculate total distance
    public static int calculateDistance() {
        int todayDistance = 0;
        try {

            String currentDate = Utility.getCurrentDate();
            int logId = DailyLogDB.getDailyLog(Utility.activeUserId, currentDate);

            if (logId > 0) {
                todayDistance = CalculateDistanceByLogId(logId);
                DailyLogDB.DailyLogDistanceSave(logId, Utility.activeUserId, todayDistance);
            }
        } catch (Exception exe) {
        }
        return todayDistance;
    }

    // Created By: Deepak Sharma
    // Created Date: 27 April 2018
    // Purpose: get events to calculate total distance

    public static int calculateDistance(int driverId, String date) {
        int todayDistance = 0;
        try {
            int logId = DailyLogDB.getDailyLog(driverId, date);
            if (logId > 0) {
                todayDistance = CalculateDistanceByLogId(logId);
                DailyLogDB.DailyLogDistanceSave(logId, Utility.activeUserId, todayDistance);
            }
        } catch (Exception exe) {
        }
        return todayDistance;
    }

    // Created By: Deepak Sharma
    // Created Date: 27 April 2018
    // Purpose: get events to calculate total distance

    public static int calculateDistance(int logId) {
        int todayDistance = 0;
        try {
            if (logId > 0) {
                todayDistance = CalculateDistanceByLogId(logId);
                DailyLogDB.DailyLogDistanceSave(logId, Utility.activeUserId, todayDistance);
            }
        } catch (Exception exe) {
        }
        return todayDistance;
    }

    // Created By: Deepak Sharma
    // Created Date: 27 April 2018
    // Purpose: get events to calculate total distance
    public static int CalculateDistanceByLogId(int logId) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        double totalDistance = 0d;
        try {
            String currentDate = Utility.getCurrentDate();
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select EventType,EventCode,EventDateTime ,OdometerReading,VehicleId from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DailyLogId=? and (EventType=2 or (EventType=1 and EventCode=3)) and EventRecordStatus=1 order by VehicleId,EventDateTime", new String[]{Integer.toString(logId)});

            while (cursor.moveToNext()) {
                String eventDateTime = cursor.getString(cursor.getColumnIndex("EventDateTime"));
                int vehicleId = cursor.getInt(cursor.getColumnIndex("VehicleId"));
                double startOdometer = cursor.getDouble(cursor.getColumnIndex("OdometerReading"));
                Cursor cursor1 = database.rawQuery("select EventType,EventCode,EventDateTime ,OdometerReading,VehicleId from " +
                                MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where EventDateTime>? and vehicleId=? and DailyLogId=? and EventType<=3 and EventRecordStatus=1  order by EventDateTime Limit 1",
                        new String[]{eventDateTime, Integer.toString(vehicleId), Integer.toString(logId)});
                double endOdometer = 0d;
                if (cursor1.moveToFirst()) {
                    endOdometer = cursor1.getDouble(cursor1.getColumnIndex("OdometerReading"));
                } else if (vehicleId == Utility.vehicleId && eventDateTime.contains(currentDate) && Utility.onScreenUserId == Utility.activeUserId) {
                    endOdometer = Double.parseDouble(CanMessages.OdometerReading);
                }
                cursor1.close();

                double distance = (endOdometer - startOdometer);
                if (distance > 0)
                    totalDistance += distance;
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::TotalDistanceGetByLogId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::TotalDistanceGetByLogId Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return Double.valueOf(totalDistance).intValue();
    }


    // Created By: Deepak Sharma
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static ArrayList<EventBean> EventUnAssignedGet() {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,EventDateTime,EventType ,EventCode ,EventCodeDescription,Latitude ,Longitude ,LocationDescription,OdometerReading,DailyLogId from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on e.DriverId=a.AccountId and a.AccountType=2 where EventRecordStatus=1 and EventType!=7 order by EventDateTime desc", null);

            while (cursor.moveToNext()) {
                EventBean bean = new EventBean();
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
                bean.setEventCodeDescription(cursor.getString(cursor.getColumnIndex("EventCodeDescription")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocationDescription(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                if (bean.getOdometerReading() == null) {
                    bean.setOdometerReading("0");
                }
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setDailyLogId(cursor.getInt(cursor.getColumnIndex("DailyLogId")));
                bean.setChecked(false);
                list.add(bean);
            }

        } catch (Exception exe) {
            Log.i("EventDB", "Error:" + exe.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUnAssignedGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventUnAssignedGet Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }

    // Created By: Deepak Sharma
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static boolean UnIdentifiedEventFg() {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id  from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on e.DriverId=a.AccountId and a.AccountType=2 where EventRecordStatus=1" +
                    " and EventType!=7 Limit 1", null);

            if (cursor.moveToNext()) {
                status = true;
            }

        } catch (Exception exe) {
            Log.i("EventDB", "Error:" + exe.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUnAssignedGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventUnAssignedGet Error:", exe.getMessage(), Utility.printStackTrace(exe));

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
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static ArrayList<EventBean> EventEditRequestedGet(int driverId) {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,EventDateTime,EventType ,EventCode ,EventCodeDescription,Latitude ,Longitude ,LocationDescription,OdometerReading,CreatedDate,DailyLogId from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventRecordStatus=3 and EventRecordOrigin=3", new String[]{driverId + ""});
            while (cursor.moveToNext()) {
                EventBean bean = new EventBean();
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
                bean.setEventCodeDescription(cursor.getString(cursor.getColumnIndex("EventCodeDescription")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocationDescription(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setCreatedDate(cursor.getString(cursor.getColumnIndex("CreatedDate")));
                bean.setDailyLogId(cursor.getInt(cursor.getColumnIndex("DailyLogId")));
                bean.setChecked(false);
                bean.setEventRecordOrigin(3);
                bean.setEventRecordStatus(3);
                list.add(bean);
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::EventEditRequestedGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventEditRequestedGet Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }

    // Created By: Deepak Sharma
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static EventBean EventGetById(int id) {
        EventBean bean = new EventBean();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,EventType ,EventCode ,EventCodeDescription ,OdometerReading ,EngineHour ,EventRecordOrigin ,EventRecordStatus " +
                    ",EventDateTime ,Latitude ,Longitude ,LocationDescription ,DailyLogId ,CreatedBy ,CreatedDate ,ModifiedBy ,ModifiedDate ,StatusId ,SyncFg,DistanceSinceLastValidCoordinate,AccumulatedVehicleMiles ,ElaspsedEngineHour" +
                    ",MalfunctionIndicatorFg ,DataDiagnosticIndicatorFg,DiagnosticCode,MotorCarrier,ShippingDocumentNo,TrailerNo,TimeZoneOffsetUTC,Annotation,CoDriverId,VehicleId from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where _id=?", new String[]{id + ""});
            if (cursor.moveToNext()) {
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
                bean.setEventCodeDescription(cursor.getString(cursor.getColumnIndex("EventCodeDescription")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                bean.setEngineHour(cursor.getString(cursor.getColumnIndex("EngineHour")));
                bean.setEventRecordOrigin(cursor.getInt(cursor.getColumnIndex("EventRecordOrigin")));
                bean.setEventRecordStatus(cursor.getInt(cursor.getColumnIndex("EventRecordStatus")));
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocationDescription(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setDailyLogId(cursor.getInt(cursor.getColumnIndex("DailyLogId")));
                bean.setCreatedBy(cursor.getInt(cursor.getColumnIndex("CreatedBy")));
                bean.setCreatedDate(cursor.getString(cursor.getColumnIndex("CreatedDate")));
                bean.setModifiedBy(cursor.getInt(cursor.getColumnIndex("ModifiedBy")));
                bean.setModifiedDate(cursor.getString(cursor.getColumnIndex("ModifiedDate")));
                bean.setStatusId(cursor.getInt(cursor.getColumnIndex("StatusId")));
                bean.setSyncFg(cursor.getInt(cursor.getColumnIndex("SyncFg")));
                bean.setDistanceSinceLastValidCoordinate(cursor.getString(cursor.getColumnIndex("DistanceSinceLastValidCoordinate")));
                bean.setAccumulatedVehicleMiles(cursor.getString(cursor.getColumnIndex("AccumulatedVehicleMiles")));
                bean.setElaspsedEngineHour(cursor.getString(cursor.getColumnIndex("ElaspsedEngineHour")));

                bean.setMalfunctionIndicatorFg(cursor.getInt(cursor.getColumnIndex("MalfunctionIndicatorFg")));
                bean.setDataDiagnosticIndicatorFg(cursor.getInt(cursor.getColumnIndex("DataDiagnosticIndicatorFg")));
                bean.setDiagnosticCode(cursor.getString(cursor.getColumnIndex("DiagnosticCode")));
                bean.setMotorCarrier(cursor.getString(cursor.getColumnIndex("MotorCarrier")));
                bean.setShippingDocumentNo(cursor.getString(cursor.getColumnIndex("ShippingDocumentNo")));
                bean.setTrailerNo(cursor.getString(cursor.getColumnIndex("TrailerNo")));
                bean.setTimeZoneOffsetUTC(cursor.getString(cursor.getColumnIndex("TimeZoneOffsetUTC")));
                bean.setCoDriverId(cursor.getInt(cursor.getColumnIndex("CoDriverId")));
                bean.setVehicleId(cursor.getInt(cursor.getColumnIndex("VehicleId")));

                // get annotation
                bean.setAnnotation(cursor.getString(cursor.getColumnIndex("Annotation")));
                // null handling of annotation
                if (bean.getAnnotation() == null) {
                    bean.setAnnotation("");
                }


            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::EventGetById Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventGetById Error:", exe.getMessage(), Utility.printStackTrace(exe));

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

    public static void EventCreateWithLocation(String eventDateTime, int eventType, int eventCode, String eventDescription, int eventOrigin, int eventStatus, int dailyLogId, int driverId, String location, String annotation, int ruleId, boolean WaitingFg) {
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
            bean.setDailyLogId(dailyLogId);
            bean.setOdometerReading(Double.valueOf(CanMessages.OdometerReading).intValue() + "");
            bean.setEngineHour(String.format("%.1f", Double.valueOf(CanMessages.EngineHours)));
            bean.setEventDateTime(eventDateTime);
            if (Utility.currentLocation.getLatitude() < 0) {
                bean.setLatitude(Utility.currentLocation.getLatitude() == -1 ? "0" : "-2");
                bean.setLongitude(Utility.currentLocation.getLongitude() == -1 ? "0" : "-2");
            } else {
                bean.setLatitude((bean.getEventType() == 3 && bean.getEventCode() == 1 ? Utility.truncate(Utility.currentLocation.getLatitude(), 1) : Utility.truncate(Utility.currentLocation.getLatitude(), 2)) + "");
                bean.setLongitude((bean.getEventType() == 3 && bean.getEventCode() == 1 ? Utility.truncate(Utility.currentLocation.getLongitude(), 1) : Utility.truncate(Utility.currentLocation.getLongitude(), 2)) + "");

            }
            bean.setLocationDescription(location);

            bean.setCreatedBy(driverId);
            bean.setCreatedDate(Utility.getCurrentDateTime());
            bean.setModifiedBy(driverId);
            bean.setModifiedDate(Utility.getCurrentDateTime());
            bean.setStatusId(1);
            bean.setSyncFg(0);
            bean.setVehicleId(Utility.vehicleId);
            bean.setMotorCarrier(Utility.CarrierName);
            bean.setTimeZoneOffsetUTC(Utility.TimeZoneOffsetUTC);
            bean.setAnnotation(annotation);

            // set default values
            bean.setDistanceSinceLastValidCoordinate("0");
            bean.setAccumulatedVehicleMiles("0");
            bean.setElaspsedEngineHour("0");
            bean.setMalfunctionIndicatorFg(Utility.malFunctionIndicatorFg ? 1 : 0);
            bean.setDataDiagnosticIndicatorFg(Utility.dataDiagnosticIndicatorFg ? 1 : 0);
            bean.setShippingDocumentNo(Utility.ShippingNumber);
            bean.setTrailerNo(Utility.TrailerNumber);
            bean.setDiagnosticCode("");
            bean.setRuleId(ruleId);

            if (Utility.currentLocation.getOdometerReadingSinceLastValidCoordinate().equals("0")) {
                Utility.currentLocation.setOdometerReadingSinceLastValidCoordinate(CanMessages.OdometerReading);
            }

            //-----------------------------------------//
            if (eventType <= 3) {
                bean.setDistanceSinceLastValidCoordinate((Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.currentLocation.getOdometerReadingSinceLastValidCoordinate()).intValue()) + "");
                bean.setAccumulatedVehicleMiles((Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.OdometerReadingSincePowerOn).intValue()) + "");
                bean.setElaspsedEngineHour(String.format("%.1f", Double.valueOf(CanMessages.EngineHours) - Double.valueOf(Utility.EngineHourSincePowerOn)));
                bean.setMalfunctionIndicatorFg(Utility.malFunctionIndicatorFg ? 1 : 0);
                bean.setDataDiagnosticIndicatorFg(Utility.dataDiagnosticIndicatorFg ? 1 : 0);
                //MainActivity.textToSpeech.speak(eventDescription, TextToSpeech.QUEUE_ADD, null);
            } else if (eventType == 6) {
                bean.setDistanceSinceLastValidCoordinate((Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.currentLocation.getOdometerReadingSinceLastValidCoordinate()).intValue()) + "");
            } else if (eventType == 7) {
                bean.setDiagnosticCode(Utility.DiagnosticCode);
            }

            int coDriverId = 0;
            if (Utility.user1.getAccountId() == driverId) {
                coDriverId = Utility.user2.getAccountId();
            } else {
                coDriverId = Utility.user1.getAccountId();
            }
            bean.setCoDriverId(coDriverId);
            bean.setWaitingFg(WaitingFg);

            EventSave(bean);

        } catch (Exception e) {
            Log.i("EventDB", "EventCreate Error:" + e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventCreate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventCreateWithLocation Error:", e.getMessage(), Utility.printStackTrace(e));

        }

    }

    public static void EventCreateManually(String eventDateTime, int eventType, int eventCode, String eventDescription, int eventOrigin, int eventStatus, int dailyLogId, int driverId, String location, String annotation, int ruleId, int splitFg) {
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
            bean.setDailyLogId(dailyLogId);
            bean.setOdometerReading(Double.valueOf(CanMessages.OdometerReading).intValue() + "");
            bean.setEngineHour(String.format("%.1f", Double.valueOf(CanMessages.EngineHours)));
            bean.setEventDateTime(eventDateTime);

//            if (Utility.currentLocation.getLatitude() < 0) {
//                bean.setLatitude(Utility.currentLocation.getLatitude() == -1 ? "M" : "E");
//                bean.setLongitude(Utility.currentLocation.getLongitude() == -1 ? "M" : "E");
//            } else {
//                bean.setLatitude(eventType == 3 && eventCode == 1 ? String.format("%.1f", Utility.currentLocation.getLatitude()) : String.format("%.2beanf", Utility.currentLocation.getLatitude()));
//                bean.setLongitude(eventType == 3 && eventCode == 1 ? String.format("%.1f", Utility.currentLocation.getLongitude()) : String.format("%.2f", Utility.currentLocation.getLongitude()));
//            }
            //latitude and longitude must set to M because this is manually event 0 means M as we have decimal column for latitude and longitude
            bean.setLatitude("0");
            bean.setLongitude("0");
            bean.setLocationDescription(location);
            bean.setCreatedBy(driverId);
            bean.setCreatedDate(Utility.getCurrentDateTime());
            bean.setModifiedBy(driverId);
            bean.setModifiedDate(Utility.getCurrentDateTime());
            bean.setStatusId(1);
            bean.setSyncFg(0);
            bean.setVehicleId(Utility.vehicleId);
            bean.setMotorCarrier(Utility.CarrierName);
            bean.setTimeZoneOffsetUTC(Utility.TimeZoneOffsetUTC);
            bean.setAnnotation(annotation);

            // set default values
            bean.setDistanceSinceLastValidCoordinate("0");
            bean.setAccumulatedVehicleMiles("0");
            bean.setElaspsedEngineHour("0");
            bean.setMalfunctionIndicatorFg(Utility.malFunctionIndicatorFg ? 1 : 0);
            bean.setDataDiagnosticIndicatorFg(Utility.dataDiagnosticIndicatorFg ? 1 : 0);
            bean.setShippingDocumentNo(Utility.ShippingNumber);
            bean.setTrailerNo(Utility.TrailerNumber);
            bean.setDiagnosticCode("");
            bean.setRuleId(ruleId);
            bean.setSplitSleep(splitFg);
            //-----------------------------------------//
            int distanceSinceLastValidCoordinate, accumulatedMiles;
            double elapsedEngineHours;
            if (eventType <= 3) {
                distanceSinceLastValidCoordinate = (Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.currentLocation.getOdometerReadingSinceLastValidCoordinate()).intValue());
                if (distanceSinceLastValidCoordinate > 6 || distanceSinceLastValidCoordinate < 0) {
                    distanceSinceLastValidCoordinate = 0;
                }
                bean.setDistanceSinceLastValidCoordinate(distanceSinceLastValidCoordinate + "");
                accumulatedMiles = (Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.OdometerReadingSincePowerOn).intValue());

                if (accumulatedMiles < 0 || accumulatedMiles > 9999) {
                    accumulatedMiles = 0;
                }

                bean.setAccumulatedVehicleMiles(accumulatedMiles + "");

                elapsedEngineHours = Double.valueOf(CanMessages.EngineHours) - Double.valueOf(Utility.EngineHourSincePowerOn);

                if (elapsedEngineHours < 0d || elapsedEngineHours > 99.9d) {
                    elapsedEngineHours = 0d;
                }
                bean.setElaspsedEngineHour(String.format("%.1f", elapsedEngineHours));
                bean.setMalfunctionIndicatorFg(Utility.malFunctionIndicatorFg ? 1 : 0);
                bean.setDataDiagnosticIndicatorFg(Utility.dataDiagnosticIndicatorFg ? 1 : 0);
                //MainActivity.textToSpeech.speak(eventDescription, TextToSpeech.QUEUE_ADD, null);
            } else if (eventType == 6) {
                distanceSinceLastValidCoordinate = (Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.currentLocation.getOdometerReadingSinceLastValidCoordinate()).intValue());
                if (distanceSinceLastValidCoordinate > 6 || distanceSinceLastValidCoordinate < 0) {
                    distanceSinceLastValidCoordinate = 0;
                }
                bean.setDistanceSinceLastValidCoordinate(distanceSinceLastValidCoordinate + "");
            } else if (eventType == 7) {
                bean.setDiagnosticCode(Utility.DiagnosticCode);
            }

            int coDriverId = 0;
            if (Utility.user1.getAccountId() == driverId) {
                coDriverId = Utility.user2.getAccountId();
            } else {
                coDriverId = Utility.user1.getAccountId();
            }
            bean.setCoDriverId(coDriverId);
            EventSave(bean);
        } catch (Exception e) {
            Log.i("EventDB", "EventCreate Error:" + e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventCreate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventCreateManually Error:", e.getMessage(), Utility.printStackTrace(e));
        }

    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: create events
    public static void EventCreate(String eventDateTime, int eventType, int eventCode, String eventDescription, int eventOrigin, int eventStatus, int dailyLogId, int driverId, String annotation, int ruleId) {
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
            bean.setDailyLogId(dailyLogId);
            bean.setOdometerReading(Double.valueOf(CanMessages.OdometerReading).intValue() + "");
            bean.setEngineHour(String.format("%.1f", Double.valueOf(CanMessages.EngineHours)));
            bean.setEventDateTime(eventDateTime);
            // while event created automatically we should enter X if malfunction is not occurred
            if (Utility.currentLocation.getLatitude() < 0) {
                bean.setLatitude(Utility.currentLocation.getLatitude() + "");
                bean.setLongitude(Utility.currentLocation.getLongitude() + "");
            } else {
                bean.setLatitude((bean.getEventType() == 3 && bean.getEventCode() == 1 ? Utility.truncate(Utility.currentLocation.getLatitude(), 1) : Utility.truncate(Utility.currentLocation.getLatitude(), 2)) + "");
                bean.setLongitude((bean.getEventType() == 3 && bean.getEventCode() == 1 ? Utility.truncate(Utility.currentLocation.getLongitude(), 1) : Utility.truncate(Utility.currentLocation.getLongitude(), 2)) + "");
            }

            bean.setLocationDescription(Utility.currentLocation.getLocationDescription() == null ? "" : Utility.currentLocation.getLocationDescription());

            // we need location with every event in ELD as per requirement
            /*if (annotation.equals("mid night event")) {
                bean.setLocationDescription("");
            }*/

            bean.setCreatedBy(driverId);
            bean.setCreatedDate(Utility._CurrentDateTime);
            bean.setModifiedBy(driverId);
            bean.setModifiedDate(Utility._CurrentDateTime);
            bean.setStatusId(1);
            bean.setSyncFg(0);
            bean.setVehicleId(Utility.vehicleId);
            bean.setMotorCarrier(Utility.CarrierName);
            bean.setTimeZoneOffsetUTC(Utility.TimeZoneOffsetUTC);
            bean.setAnnotation(annotation);

            // set default values
            bean.setDistanceSinceLastValidCoordinate("0");
            bean.setAccumulatedVehicleMiles("0");
            bean.setElaspsedEngineHour("0");
            bean.setMalfunctionIndicatorFg(Utility.malFunctionIndicatorFg ? 1 : 0);
            bean.setDataDiagnosticIndicatorFg(Utility.dataDiagnosticIndicatorFg ? 1 : 0);
            bean.setShippingDocumentNo(Utility.ShippingNumber);
            bean.setTrailerNo(Utility.TrailerNumber);
            bean.setDiagnosticCode("");
            // Upadate RuleID
            bean.setRuleId(ruleId);

            //-----------------------------------------//
            int distanceSinceLastValidCoordinate, accumulatedMiles;
            double elapsedEngineHours;
            if (eventType <= 3) {
                distanceSinceLastValidCoordinate = (Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.currentLocation.getOdometerReadingSinceLastValidCoordinate()).intValue());
                if (distanceSinceLastValidCoordinate > 6 || distanceSinceLastValidCoordinate < 0) {
                    distanceSinceLastValidCoordinate = 0;
                }
                bean.setDistanceSinceLastValidCoordinate(distanceSinceLastValidCoordinate + "");
                accumulatedMiles = (Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.OdometerReadingSincePowerOn).intValue());

                if (accumulatedMiles < 0 || accumulatedMiles > 9999) {
                    accumulatedMiles = 0;
                }

                bean.setAccumulatedVehicleMiles(accumulatedMiles + "");

                elapsedEngineHours = Double.valueOf(CanMessages.EngineHours) - Double.valueOf(Utility.EngineHourSincePowerOn);

                if (elapsedEngineHours < 0d || elapsedEngineHours > 99.9d) {
                    elapsedEngineHours = 0d;
                }
                bean.setElaspsedEngineHour(String.format("%.1f", elapsedEngineHours));
                bean.setMalfunctionIndicatorFg(Utility.malFunctionIndicatorFg ? 1 : 0);
                bean.setDataDiagnosticIndicatorFg(Utility.dataDiagnosticIndicatorFg ? 1 : 0);
                //MainActivity.textToSpeech.speak(eventDescription, TextToSpeech.QUEUE_ADD, null);
            } else if (eventType == 6) {
                distanceSinceLastValidCoordinate = (Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.currentLocation.getOdometerReadingSinceLastValidCoordinate()).intValue());
                if (distanceSinceLastValidCoordinate > 6 || distanceSinceLastValidCoordinate < 0) {
                    distanceSinceLastValidCoordinate = 0;
                }
                bean.setDistanceSinceLastValidCoordinate(distanceSinceLastValidCoordinate + "");
            } else if (eventType == 7) {
                bean.setDiagnosticCode(Utility.DiagnosticCode);
            }

            int coDriverId = 0;
            if (Utility.user1.getAccountId() == driverId) {
                coDriverId = Utility.user2.getAccountId();
            } else {
                coDriverId = Utility.user1.getAccountId();
            }
            bean.setCoDriverId(coDriverId);

            EventSave(bean);
            Log.i("EventTYPE", String.valueOf(bean.getEventType()));
            Log.i("EventCODE", String.valueOf(bean.getEventCode()));
            Log.i("EventCODEDESC", String.valueOf(bean.getEventCodeDescription()));
            if (eventType == 1 && eventCode >= 3 && eventOrigin == 1 && eventStatus == 1 && Utility.currentLocation.getLatitude() == -1) {
                if (!DiagnosticIndicatorBean.MissingElementDiagnosticFg) {
                    DiagnosticIndicatorBean.MissingElementDiagnosticFg = true;
                    // save malfunction for Missing element Diagnostic event
                    DiagnosticMalfunction.saveDiagnosticIndicatorByCode("3", 3, "MissingElementDiagnosticFg");
                }
            }

        } catch (Exception e) {
            Log.i("EventDB", "EventCreate Error:" + e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventCreate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventCreate Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    // Created By: Deepak Sharma
    // Created Date: 27 January 2015
    // Purpose: add or update multiple events in database from web
    public static int EventSave(ArrayList<EventBean> arrBean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        int eventId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            for (EventBean bean : arrBean) {
                ContentValues values = new ContentValues();
                // in case edit request from server
                if (bean.getEventRecordOrigin() == 3) {

                    eventId = getEventId(bean.getCreatedDate(), bean.getDriverId(), bean.getEventRecordOrigin());
                } else {
                    eventId = getEventId(bean.getCreatedDate(), bean.getDriverId());
                }

                if (eventId == 0) {
                    values.put("OnlineEventId", bean.getOnlineEventId());
                    // these are the values which we edit from web
                    values.put("EventRecordOrigin", bean.getEventRecordOrigin());
                    values.put("EventRecordStatus", bean.getEventRecordStatus());
                    values.put("EventType", bean.getEventType());
                    values.put("EventCode", bean.getEventCode());
                    values.put("EventDateTime", bean.getEventDateTime());
                    values.put("OdometerReading", bean.getOdometerReading());
                    values.put("EventCodeDescription", bean.getEventCodeDescription());
                    values.put("LocationDescription", bean.getLocationDescription());

                    values.put("DriverId", bean.getDriverId());
                    values.put("VehicleId", bean.getVehicleId());
                    values.put("EventSequenceId", bean.getEventSequenceId());
                    values.put("EngineHour", bean.getEngineHour());
                    values.put("Latitude", bean.getLatitude());
                    values.put("Longitude", bean.getLongitude());
                    values.put("DailyLogId", bean.getDailyLogId());
                    values.put("CreatedBy", bean.getCreatedBy());
                    values.put("CreatedDate", bean.getCreatedDate());
                    values.put("StatusId", 1);
                    values.put("SyncFg", 1);
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
                    values.put("CoDriverId", bean.getCoDriverId());
                    values.put("CheckSum", bean.getCheckSumWeb());

                    // For Split Sleep
                    values.put("Splitfg", bean.getSplitSleep());
                    // For RuleId
                    values.put("RuleId", bean.getRuleId());

                    values.put("TimeZoneShortName", bean.getTimeZoneShortName());

                    eventId = (int) database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT,
                            "_id,ModifiedBy,ModifiedDate", values);
                } else {
                    if (bean.getEventRecordOrigin() == 3 || Utility._appSetting.getdEditFg() == 1) {
                        values.put("EventDateTime", bean.getEventDateTime());
                        values.put("EventType", bean.getEventType());
                        values.put("EventCode", bean.getEventCode());
                        values.put("EventCodeDescription", bean.getEventCodeDescription());
                        values.put("LocationDescription", bean.getLocationDescription());
                        values.put("VehicleId", bean.getVehicleId());

                        values.put("OnlineEventId", bean.getOnlineEventId());
                        // these are the values which we edit from web
                        values.put("EventRecordOrigin", bean.getEventRecordOrigin());
                        values.put("EventRecordStatus", bean.getEventRecordStatus());
                        values.put("OdometerReading", bean.getOdometerReading());
                        values.put("EngineHour", bean.getEngineHour());

                        // For Split Sleep
                        values.put("Splitfg", bean.getSplitSleep());
                        // For RuleId
                        values.put("RuleId", bean.getRuleId());
                        values.put("TrailerNo", bean.getTrailerNo());
                        values.put("ShippingDocumentNo", bean.getShippingDocumentNo());
                        values.put("Annotation", bean.getAnnotation());

                    } else if (bean.getEventRecordStatus() == 2) { // event record staus if inactive then set inactive
                        values.put("EventRecordStatus", bean.getEventRecordStatus());
                    }
                    values.put("CheckSum", bean.getCheckSum());
                    //122 if (bean.getDriverId() == Utility.unIdentifiedDriverId)
                    //122     LogFile.write(EventDB.class.getName() + "::EventSave save list event: " + "create checksum " + bean.getCheckSum(), LogFile.USER_INTERACTION, LogFile.NOLOGIN_LOG);
                    //123 if (bean.getDriverId() != Utility.unIdentifiedDriverId)
                    //123     LogFile.write(EventDB.class.getName() + "::EventSave save list event: " + "create checksum " + bean.getCheckSum(), LogFile.USER_INTERACTION, LogFile.DRIVEREVENT_LOG);
                    database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                            " _id= ?",
                            new String[]{eventId + ""});
                }

            }
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventSave Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventSave Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return eventId;
    }


    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add or update events in database
    public static int EventSave(EventBean bean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        int eventId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
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

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventSave Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventSave Error:", e.getMessage(), Utility.printStackTrace(e));
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

    public static boolean EventUpdate(String eventDateTime, int recordOrigin, int status, int driverId, int dailyLogId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean recordStatus = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            int id = getEventId(driverId, eventDateTime);

            ContentValues values = new ContentValues();
            values.put("EventRecordOrigin", recordOrigin);
            values.put("EventRecordStatus", status);
            values.put("DailyLogId", dailyLogId);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            values.put("StatusId", 1);
            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " _id= ?",
                    new String[]{id + ""});
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventUpdate Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return recordStatus;
    }

    // Created By: Deepak Sharma
    // Created Date: 26 Aug 2020
    // Purpose: update event
    public static boolean EventUpdate(EventBean bean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean recordStatus = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("EventType", bean.getEventType());
            values.put("EventCode", bean.getEventCode());
            values.put("EventCodeDescription", bean.getEventCodeDescription());
            values.put("OdometerReading", bean.getOdometerReading());
            values.put("EngineHour", bean.getEngineHour());
            values.put("EventDateTime", bean.getEventDateTime());

            values.put("Annotation", bean.getAnnotation());

            values.put("CheckSum", bean.getCheckSum());

            values.put("Splitfg", bean.getSplitSleep());

            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " _id= ?",
                    new String[]{bean.get_id() + ""});
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventUpdate Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return recordStatus;
    }

    public static boolean EventUpdate(int id, int status, int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean recordStatus = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("EventRecordStatus", status);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility._CurrentDateTime);

            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " _id= ?",
                    new String[]{id + ""});
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventUpdate Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return recordStatus;
    }

    public static boolean EventUpdate(int id, int status, int driverId, int dailyLogId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean recordStatus = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            // values.put("EventRecordOrigin", recordOrigin);
            values.put("EventRecordStatus", status);
            values.put("DailyLogId", dailyLogId);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility._CurrentDateTime);
            values.put("StatusId", 1);
            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " _id= ?",
                    new String[]{id + ""});
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventUpdate Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return recordStatus;
    }

    public static boolean EventTransfer(int eventId, int driverId, int coDriverId, String logDate) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int dailyLogId = 0;
        boolean recordStatus = false;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            cursor = database.rawQuery("select _id from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where LogDate=? and driverId=?"
                    , new String[]{logDate.toString(), coDriverId + ""});
            if (cursor.moveToNext()) {
                dailyLogId = cursor.getInt(0);
            }

            ContentValues values = new ContentValues();

            values.put("DailyLogId", dailyLogId);
            values.put("CoDriverId", driverId);
            values.put("DriverId", coDriverId);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            values.put("StatusId", 1);
            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " _id= ?",
                    new String[]{eventId + ""});
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventUpdate Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return recordStatus;
    }

    public static boolean EventUpdate(int id, int recordOrigin, int status, int driverId, int dailyLogId, String shipId, String trailerId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean recordStatus = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("EventRecordOrigin", recordOrigin);
            values.put("EventRecordStatus", status);
            values.put("DailyLogId", dailyLogId);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            values.put("ShippingDocumentNo", shipId);
            values.put("TrailerNo", trailerId);
            values.put("StatusId", 1);
            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " _id= ?",
                    new String[]{id + ""});
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventUpdate Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return recordStatus;
    }

    public static boolean EventUpdate(int id, int recordOrigin, int status, int driverId, int dailyLogId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean recordStatus = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("EventRecordOrigin", recordOrigin);
            values.put("EventRecordStatus", status);
            values.put("DailyLogId", dailyLogId);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            values.put("StatusId", 1);
            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " _id= ?",
                    new String[]{id + ""});
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventUpdate Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return recordStatus;
    }


    public static boolean EventCopy(int eventId, int origin, int status, int driverId, int dailyLogId, int codriverId) {
        boolean recordStatus = false;
        try {
            EventBean bean = EventGetById(eventId);
            bean.setEventSequenceId(getEventSequenceId());
            bean.setEventRecordOrigin(origin);
            bean.setEventRecordStatus(status);
            bean.setDriverId(driverId);
            bean.setDailyLogId(dailyLogId);
            bean.setCreatedDate(Utility._CurrentDateTime);
            bean.setSyncFg(0);
            // update codriver id
            bean.setCoDriverId(codriverId);
            EventSave(bean);
            recordStatus = true;
        } catch (Exception e) {
            LogFile.write(EventDB.class.getName() + "::EventCopy Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventCopy Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {

        }
        return recordStatus;
    }


    // Created By: Deepak Sharma
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static JSONArray GetEventSync(int logId) {
        JSONArray array = new JSONArray();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,DriverId ,OnlineEventId ,EventSequenceId ,EventType ,EventCode ,EventCodeDescription ,OdometerReading ,EngineHour," +
                    " EventRecordOrigin ,EventRecordStatus ,EventDateTime ,Latitude ,Longitude ,LocationDescription ,DailyLogId ,CreatedBy ,CreatedDate ,ModifiedBy " +
                    ",ModifiedDate ,StatusId ,SyncFg,VehicleId,CheckSum,DistanceSinceLastValidCoordinate ,MalfunctionIndicatorFg ,DataDiagnosticIndicatorFg" +
                    " ,DiagnosticCode ,AccumulatedVehicleMiles ,ElaspsedEngineHour ,MotorCarrier,ShippingDocumentNo,TrailerNo ,TimeZoneOffsetUTC,Annotation,CoDriverId,RuleId,Splitfg,TimeZoneShortName from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where SyncFg=? and DailyLogId=?", new String[]{"0", logId + ""});
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();

                obj.put("VEHICLEID", cursor.getInt(cursor.getColumnIndex("VehicleId")));
                obj.put("EVENTID", cursor.getInt(cursor.getColumnIndex("_id")));
                obj.put("EVENTSEQUENCEID", cursor.getInt(cursor.getColumnIndex("EventSequenceId")));
                obj.put("EVENTTYPE", cursor.getInt(cursor.getColumnIndex("EventType")));
                obj.put("EVENTCODE", cursor.getInt(cursor.getColumnIndex("EventCode")));
                String description = cursor.getString(cursor.getColumnIndex("EventCodeDescription"));

                obj.put("EVENTCODEDESCRIPTION", description);
                obj.put("ODOMETERREADING", Double.parseDouble(cursor.getString(cursor.getColumnIndex("OdometerReading"))));
                obj.put("ENGINEHOUR", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineHour"))));
                obj.put("EVENTRECORDORIGIN", cursor.getInt(cursor.getColumnIndex("EventRecordOrigin")));
                obj.put("EVENTRECORDSTATUS", cursor.getInt(cursor.getColumnIndex("EventRecordStatus")));
                obj.put("EVENTDATETIME", Utility.getDateTimeForServer(cursor.getString(cursor.getColumnIndex("EventDateTime"))));
                try {
                    obj.put("LATITUDE", Double.parseDouble(cursor.getString(cursor.getColumnIndex("Latitude"))));
                    obj.put("LONGITUDE", Double.parseDouble(cursor.getString(cursor.getColumnIndex("Longitude"))));
                } catch (Exception exe) {

                    obj.put("LATITUDE", "0");
                    obj.put("LONGITUDE", "0");
                }
                obj.put("LOCATIONDESCRIPTION", cursor.getString(cursor.getColumnIndex("LocationDescription")));
                obj.put("EVENTCREATEDBY", cursor.getInt(cursor.getColumnIndex("CreatedBy")));
                obj.put("EVENTCREATEDDATE", Utility.getDateTimeForServer(cursor.getString(cursor.getColumnIndex("CreatedDate"))));
                obj.put("MODIFIEDBY", cursor.getInt(cursor.getColumnIndex("ModifiedBy")));
                String modifiedDate = cursor.getString(cursor.getColumnIndex("ModifiedDate"));
                modifiedDate = (modifiedDate == null || modifiedDate.isEmpty()) ? "1970-01-01 00:00:00" : modifiedDate;
                obj.put("MODIFIEDDATE", Utility.getDateTimeForServer(modifiedDate));
                obj.put("DRIVERID", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("DAILYLOGID", cursor.getInt(cursor.getColumnIndex("DailyLogId")));
                obj.put("Annotation", cursor.getString(cursor.getColumnIndex("Annotation")));
                obj.put("CheckSum", cursor.getString(cursor.getColumnIndex("CheckSum")));

                double distanceSince = 0d;
                double accumulatedVehicleMiles = 0d;
                double elaspsedEngineHour = 0d;

                try {
                    distanceSince = Double.parseDouble(cursor.getString(cursor.getColumnIndex("DistanceSinceLastValidCoordinate")));
                    accumulatedVehicleMiles = Double.parseDouble(cursor.getString(cursor.getColumnIndex("AccumulatedVehicleMiles")));
                    elaspsedEngineHour = Double.parseDouble(cursor.getString(cursor.getColumnIndex("ElaspsedEngineHour")));
                } catch (Exception exe) {
                }

                obj.put("DistanceSinceLastValidCoordinate", distanceSince);
                obj.put("AccumulatedVehicleMiles", accumulatedVehicleMiles);
                obj.put("ElaspsedEngineHour", elaspsedEngineHour);
                obj.put("MalfunctionIndicatorFg", cursor.getInt(cursor.getColumnIndex("MalfunctionIndicatorFg")));
                obj.put("DataDiagnosticIndicatorFg", cursor.getInt(cursor.getColumnIndex("DataDiagnosticIndicatorFg")));
                obj.put("DiagnosticCode", cursor.getString(cursor.getColumnIndex("DiagnosticCode")));
                obj.put("MotorCarrier", cursor.getString(cursor.getColumnIndex("MotorCarrier")));
                obj.put("ShippingDocumentNo", cursor.getString(cursor.getColumnIndex("ShippingDocumentNo")));
                obj.put("TrailerNo", cursor.getString(cursor.getColumnIndex("TrailerNo")));
                obj.put("TimeZoneOffsetUTC", cursor.getString(cursor.getColumnIndex("TimeZoneOffsetUTC")));
                obj.put("CoDriverId", cursor.getString(cursor.getColumnIndex("CoDriverId")));
                obj.put("RuleId", cursor.getInt(cursor.getColumnIndex("RuleId")));
                obj.put("SplitSleepFg", cursor.getInt(cursor.getColumnIndex("Splitfg")));
                obj.put("TimeZoneShortName", cursor.getString(cursor.getColumnIndex("TimeZoneShortName")));
                array.put(obj);
            }

        } catch (Exception exe) {
            Log.e("EventList", exe.getMessage());
            LogFile.write(EventDB.class.getName() + "::GetEventSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::GetEventSync Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return array;
    }


    // Created By: Deepak Sharma
    // Created Date: 17 July 2016
    // Purpose: get unidentified driving time
    public static int getUnidentifiedTime(String startDate) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int time = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            String query = "select EventDateTime,case when EventType=1 and EventCode=3 then 1 else 0 end DrivingFg from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " where ((EventType=1 and EventCode>=3 and DriverId=?) or (EventType=5 and EventCode=1)) and EventDateTime>=? and EventRecordStatus=1 order by EventDateTime";
            cursor = database.rawQuery(query, new String[]{Utility.unIdentifiedDriverId + "", startDate});
            String eventDate = "";
            int drivingFg = 0;
            while (cursor.moveToNext()) {
                drivingFg = cursor.getInt(cursor.getColumnIndex("DrivingFg"));
                String date = cursor.getString(cursor.getColumnIndex("EventDateTime"));
                // if login record
                if (drivingFg == 0) {
                    if (!eventDate.isEmpty()) {
                        time += Utility.getDiffMins(eventDate, date) * 60;
                        eventDate = "";
                    }
                } else {
                    if (eventDate.isEmpty())
                        eventDate = date;
                }

            }

            if (!eventDate.isEmpty()) {
                time += Utility.getDiffMins(eventDate, Utility.getCurrentDateTime()) * 60;
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::getUnidentifiedTime Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::getUnidentifiedTime Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return time;
    }


    // Created By: Deepak Sharma
    // Created Date: 22 March 2017
    // Purpose: get events
    public static JSONArray DailyLogEventGetSync(int logId) {
        JSONArray array = new JSONArray();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,DriverId ,OnlineEventId ,EventSequenceId ,EventType ,EventCode ,EventCodeDescription ,OdometerReading ,EngineHour," +
                    " EventRecordOrigin ,EventRecordStatus ,EventDateTime ,Latitude ,Longitude ,LocationDescription ,DailyLogId ,CreatedBy ,CreatedDate ,ModifiedBy " +
                    ",ModifiedDate ,StatusId ,SyncFg,VehicleId,CheckSum,DistanceSinceLastValidCoordinate ,MalfunctionIndicatorFg ,DataDiagnosticIndicatorFg" +
                    " ,DiagnosticCode ,AccumulatedVehicleMiles ,ElaspsedEngineHour ,MotorCarrier,ShippingDocumentNo,TrailerNo ,TimeZoneOffsetUTC,Annotation,CoDriverId,RuleId,Splitfg,TimeZoneShortName from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where SyncFg=? and DailyLogId=?", new String[]{"0", logId + ""});
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();

                obj.put("VEHICLEID", cursor.getInt(cursor.getColumnIndex("VehicleId")));
                obj.put("EVENTID", cursor.getInt(cursor.getColumnIndex("_id")));
                obj.put("_ID", cursor.getInt(cursor.getColumnIndex("_id")));
                obj.put("EVENTSEQUENCEID", cursor.getInt(cursor.getColumnIndex("EventSequenceId")));
                obj.put("EVENTTYPE", cursor.getInt(cursor.getColumnIndex("EventType")));
                obj.put("EVENTCODE", cursor.getInt(cursor.getColumnIndex("EventCode")));
                String description = cursor.getString(cursor.getColumnIndex("EventCodeDescription"));

                obj.put("EVENTCODEDESCRIPTION", description);
                obj.put("ODOMETERREADING", Double.parseDouble(cursor.getString(cursor.getColumnIndex("OdometerReading"))));
                obj.put("ENGINEHOUR", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineHour"))));
                obj.put("EVENTRECORDORIGIN", cursor.getInt(cursor.getColumnIndex("EventRecordOrigin")));
                obj.put("EVENTRECORDSTATUS", cursor.getInt(cursor.getColumnIndex("EventRecordStatus")));
                obj.put("EVENTDATETIME", cursor.getString(cursor.getColumnIndex("EventDateTime")));
                try {
                    obj.put("LATITUDE", Double.parseDouble(cursor.getString(cursor.getColumnIndex("Latitude"))));
                    obj.put("LONGITUDE", Double.parseDouble(cursor.getString(cursor.getColumnIndex("Longitude"))));
                } catch (Exception exe) {
                    obj.put("LATITUDE", "0");
                    obj.put("LONGITUDE", "0");
                }
                obj.put("LOCATIONDESCRIPTION", cursor.getString(cursor.getColumnIndex("LocationDescription")));
                obj.put("EVENTCREATEDBY", cursor.getInt(cursor.getColumnIndex("CreatedBy")));
                obj.put("EVENTCREATEDDATE", cursor.getString(cursor.getColumnIndex("CreatedDate")));
                obj.put("MODIFIEDBY", cursor.getInt(cursor.getColumnIndex("ModifiedBy")));
                String modifiedDate = cursor.getString(cursor.getColumnIndex("ModifiedDate"));
                modifiedDate = (modifiedDate == null || modifiedDate.isEmpty()) ? "1970-01-01 00:00:00" : modifiedDate;
                obj.put("MODIFIEDDATE", modifiedDate);
                obj.put("DRIVERID", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("DAILYLOGID", cursor.getInt(cursor.getColumnIndex("DailyLogId")));
                obj.put("Annotation", cursor.getString(cursor.getColumnIndex("Annotation")));
                obj.put("CheckSum", cursor.getString(cursor.getColumnIndex("CheckSum")));

                double distanceSince = 0d;
                double accumulatedVehicleMiles = 0d;
                double elaspsedEngineHour = 0d;

                try {
                    distanceSince = Double.parseDouble(cursor.getString(cursor.getColumnIndex("DistanceSinceLastValidCoordinate")));
                    accumulatedVehicleMiles = Double.parseDouble(cursor.getString(cursor.getColumnIndex("AccumulatedVehicleMiles")));
                    elaspsedEngineHour = Double.parseDouble(cursor.getString(cursor.getColumnIndex("ElaspsedEngineHour")));
                } catch (Exception exe) {
                }

                obj.put("DistanceSinceLastValidCoordinate", distanceSince);
                obj.put("AccumulatedVehicleMiles", accumulatedVehicleMiles);
                obj.put("ElaspsedEngineHour", elaspsedEngineHour);
                obj.put("MalfunctionIndicatorFg", cursor.getInt(cursor.getColumnIndex("MalfunctionIndicatorFg")));
                obj.put("DataDiagnosticIndicatorFg", cursor.getInt(cursor.getColumnIndex("DataDiagnosticIndicatorFg")));
                obj.put("DiagnosticCode", cursor.getString(cursor.getColumnIndex("DiagnosticCode")));
                obj.put("MotorCarrier", cursor.getString(cursor.getColumnIndex("MotorCarrier")));
                obj.put("ShippingDocumentNo", cursor.getString(cursor.getColumnIndex("ShippingDocumentNo")));
                obj.put("TrailerNo", cursor.getString(cursor.getColumnIndex("TrailerNo")));
                obj.put("TimeZoneOffsetUTC", cursor.getString(cursor.getColumnIndex("TimeZoneOffsetUTC")));
                obj.put("CoDriverId", cursor.getString(cursor.getColumnIndex("CoDriverId")));
                obj.put("RuleId", cursor.getInt(cursor.getColumnIndex("RuleId")));
                obj.put("SplitSleepFg", cursor.getInt(cursor.getColumnIndex("Splitfg")));
                obj.put("TimeZoneShortName", cursor.getString(cursor.getColumnIndex("TimeZoneShortName")));
                array.put(obj);
            }

        } catch (Exception exe) {
            Log.e("EventDB", exe.getMessage());
            LogFile.write(EventDB.class.getName() + "::DailyLogEventGetSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::DailyLogEventGetSync Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return array;
    }

    // Created By: Deepak Sharma
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static EventBean LastEventGetByDriverId(int driverId) {
        EventBean bean = new EventBean();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,EventType ,EventCode ,EventCodeDescription ,OdometerReading ,EngineHour ,EventRecordOrigin ,EventRecordStatus " +
                    ",EventDateTime ,Latitude ,Longitude ,LocationDescription ,DailyLogId ,CreatedBy ,CreatedDate ,ModifiedBy ,ModifiedDate ,StatusId ,SyncFg,DistanceSinceLastValidCoordinate,AccumulatedVehicleMiles ,ElaspsedEngineHour" +
                    ",MalfunctionIndicatorFg ,DataDiagnosticIndicatorFg,DiagnosticCode,MotorCarrier,ShippingDocumentNo,TrailerNo,TimeZoneOffsetUTC,Annotation,CoDriverId,VehicleId from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventType in (1,3) and EventRecordStatus=1  order by EventDateTime desc LIMIT 1", new String[]{driverId + ""});
            if (cursor.moveToNext()) {
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
                bean.setEventCodeDescription(cursor.getString(cursor.getColumnIndex("EventCodeDescription")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                bean.setEngineHour(cursor.getString(cursor.getColumnIndex("EngineHour")));
                bean.setEventRecordOrigin(cursor.getInt(cursor.getColumnIndex("EventRecordOrigin")));
                bean.setEventRecordStatus(cursor.getInt(cursor.getColumnIndex("EventRecordStatus")));
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocationDescription(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setDailyLogId(cursor.getInt(cursor.getColumnIndex("DailyLogId")));
                bean.setCreatedBy(cursor.getInt(cursor.getColumnIndex("CreatedBy")));
                bean.setCreatedDate(cursor.getString(cursor.getColumnIndex("CreatedDate")));
                bean.setModifiedBy(cursor.getInt(cursor.getColumnIndex("ModifiedBy")));
                bean.setModifiedDate(cursor.getString(cursor.getColumnIndex("ModifiedDate")));
                bean.setStatusId(cursor.getInt(cursor.getColumnIndex("StatusId")));
                bean.setSyncFg(cursor.getInt(cursor.getColumnIndex("SyncFg")));
                bean.setDistanceSinceLastValidCoordinate(cursor.getString(cursor.getColumnIndex("DistanceSinceLastValidCoordinate")));
                bean.setAccumulatedVehicleMiles(cursor.getString(cursor.getColumnIndex("AccumulatedVehicleMiles")));
                bean.setElaspsedEngineHour(cursor.getString(cursor.getColumnIndex("ElaspsedEngineHour")));

                bean.setMalfunctionIndicatorFg(cursor.getInt(cursor.getColumnIndex("MalfunctionIndicatorFg")));
                bean.setDataDiagnosticIndicatorFg(cursor.getInt(cursor.getColumnIndex("DataDiagnosticIndicatorFg")));
                bean.setDiagnosticCode(cursor.getString(cursor.getColumnIndex("DiagnosticCode")));
                bean.setMotorCarrier(cursor.getString(cursor.getColumnIndex("MotorCarrier")));
                bean.setShippingDocumentNo(cursor.getString(cursor.getColumnIndex("ShippingDocumentNo")));
                bean.setTrailerNo(cursor.getString(cursor.getColumnIndex("TrailerNo")));
                bean.setTimeZoneOffsetUTC(cursor.getString(cursor.getColumnIndex("TimeZoneOffsetUTC")));
                bean.setCoDriverId(cursor.getInt(cursor.getColumnIndex("CoDriverId")));
                bean.setVehicleId(cursor.getInt(cursor.getColumnIndex("VehicleId")));
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::LastEventGetByDriverId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::LastEventGetByDriverId Error:", exe.getMessage(), Utility.printStackTrace(exe));
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
    // Created Date: 06 November 2018
    // purpose: get duty status of month to process violations
    public static ArrayList<DutyStatus> DutyStatusGet(String startDate, int driverId, Context context) {
        ArrayList<DutyStatus> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(context);
            database = helper.getReadableDatabase();

            // get rule at startdate
            cursor = database.rawQuery("select RuleId from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE +
                    " where DriverId=? and RuleStartTime<? ", new String[]{driverId + "", startDate});

            int ruleId = 0;
            if (cursor.moveToNext()) {
                ruleId = cursor.getInt(0);
            }

            //query change for deferred day
            String query = "select e._id, EventDateTime dutyStatusTime ,case when EventType=3 and EventCode=2 then 4 else EventCode end dutyStatus," +
                    "case when EventType=3 and EventCode==1 then 1 else 0 end " + " personalUseFg, e.RuleId,e.CoDriverId,e.Splitfg,d.DeferedDay,e.ReducedFg, e.WaitingFg from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT
                    + " e inner join " + MySQLiteOpenHelper.TABLE_DAILYLOG + " d on e.DailyLogId=d._id " +
                    "where e.EventRecordStatus=1 and e.EventType in (1,3) and e.EventCode>0 and  e.DriverId=? and e.EventDateTime < ? AND e.Annotation!=? order by e.EventDateTime desc LIMIT 1 ";

            cursor = database.rawQuery(query, new String[]{driverId + "", startDate, "mid night event"});

            if (cursor.moveToNext()) {

                if (ruleId == 0) {
                    ruleId = cursor.getInt(cursor.getColumnIndex("RuleId"));
                }
                // create object ot save first event into list
                DutyStatus bean = new DutyStatus();
                bean.setEventId(cursor.getInt(cursor.getColumnIndex("_id")));

                bean.setStatus(cursor.getInt(cursor.getColumnIndex("dutyStatus")));
                bean.setPersonalUseFg(cursor.getInt(cursor.getColumnIndex("personalUseFg")));
                bean.setCoDriverId(cursor.getInt(cursor.getColumnIndex("CoDriverId")));
                bean.setSplitFg(cursor.getInt(cursor.getColumnIndex("Splitfg")) == 1);
                // set event date as start date
                bean.setEventDateTime(Utility.parse(startDate));
                bean.setRule(ruleId);
                // get Defered Day value
                bean.setDeferedDay(cursor.getInt(cursor.getColumnIndex("DeferedDay")));
                bean.setReducedFg(cursor.getInt(cursor.getColumnIndex("ReducedFg")) == 1);
                bean.setWaitingFg(cursor.getInt(cursor.getColumnIndex("WaitingFg")) == 1);

                list.add(bean);
            } else {

                // create default off duty object if don't have status before start date
                DutyStatus bean = DutyStatusObjectGet(1, Utility.parse(startDate), 0, 0, false, 1, 0);
                list.add(bean);
            }
            cursor.close();

            //query change for deferred day
            query = "select e._id, EventDateTime dutyStatusTime, case when EventType=3 and EventCode=2 then 4 else EventCode end dutyStatus,case when EventType=3 and EventCode==1 then 1 else 0 end " + " personalUseFg,e.RuleId,e.CoDriverId,e.Splitfg,d.DeferedDay,e.ReducedFg, e.WaitingFg " +
                    " from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " e inner join " + MySQLiteOpenHelper.TABLE_DAILYLOG + " d on e.DailyLogId=d._id " + " where e.EventRecordStatus=1 and e.EventType in (1,3) and  e.DriverId=? and e.EventCode>0  and e.EventDateTime>=? and e.Annotation!=? order by EventDateTime ";


            cursor = database.rawQuery(query, new String[]{driverId + "", startDate, "mid night event"});

            while (cursor.moveToNext()) {

                DutyStatus bean = new DutyStatus();
                bean.setEventId(cursor.getInt(cursor.getColumnIndex("_id")));
                ruleId = cursor.getInt(cursor.getColumnIndex("RuleId"));
                bean.setStatus(cursor.getInt(cursor.getColumnIndex("dutyStatus")));
                bean.setPersonalUseFg(cursor.getInt(cursor.getColumnIndex("personalUseFg")));
                bean.setCoDriverId(cursor.getInt(cursor.getColumnIndex("CoDriverId")));
                bean.setSplitFg(cursor.getInt(cursor.getColumnIndex("Splitfg")) == 1);
                // set event date as start date
                bean.setEventDateTime(Utility.parse(cursor.getString(cursor.getColumnIndex("dutyStatusTime"))));
                bean.setRule(ruleId);
                bean.setDeferedDay(cursor.getInt(cursor.getColumnIndex("DeferedDay")));
                bean.setReducedFg(cursor.getInt(cursor.getColumnIndex("ReducedFg")) == 1);
                bean.setWaitingFg(cursor.getInt(cursor.getColumnIndex("WaitingFg")) == 1);
                list.add(bean);
            }
        } catch (Exception exe) {
            exe.printStackTrace();

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }

    // Created By: Deepak Sharma
    // Created Date: 06 November 2018
    // purpose: get duty status of month to process violations
    public static ArrayList<DutyStatus> DutyStatusByRuleGet(String startDate, int driverId, Context context, ArrayList<DutyStatus> listDS) {
        ArrayList<DutyStatus> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(context);
            database = helper.getReadableDatabase();

            // get rule at startdate
            cursor = database.rawQuery("select RuleId,RuleStartTime from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE +
                    " where DriverId=? and RuleStartTime<? order by RuleStartTime desc LIMIT 1", new String[]{driverId + "", startDate});

            if (cursor.moveToNext()) {

                int ruleId = cursor.getInt(cursor.getColumnIndex("RuleId"));
                Date ruleDate = Utility.parse(cursor.getString(cursor.getColumnIndex("RuleStartTime")));

                if (listDS.size() > 0) {
                    DutyStatus item = new DutyStatus(listDS.get(0));
                    item.setRule(ruleId);
                    item.setEventDateTime(ruleDate);
                    list.add(item);
                }
            }

            cursor.close();

            // get rule list after startdate
            cursor = database.rawQuery("select RuleId,RuleStartTime from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE +
                    " where DriverId=? and RuleStartTime>=? order by RuleStartTime", new String[]{driverId + "", startDate});

            // iterate rules
            while (cursor.moveToNext()) {
                RuleBean bean = new RuleBean();
                int ruleId = cursor.getInt(cursor.getColumnIndex("RuleId"));
                Date ruleDate = Utility.parse(cursor.getString(cursor.getColumnIndex("RuleStartTime")));

                for (int i = listDS.size() - 1; i >= 0; i--) {
                    DutyStatus item = new DutyStatus(listDS.get(i));
                    if (item.getEventDateTime().before(ruleDate)) {
                        item.setRule(ruleId);
                        item.setEventDateTime(ruleDate);
                        list.add(item);
                        break;
                    }
                }
            }

        } catch (Exception exe) {
            String message = exe.getMessage();

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }

    // Created By: Deepak Sharma
    // Created Date: 06 November 2018
    // purpose: create duty status Object
    public static DutyStatus DutyStatusObjectGet(int status, Date eventDate, int personalUseFg, int coDriverId, boolean splitFg, int ruleId, int deferFg) {
        DutyStatus bean = new DutyStatus();
        bean.setStatus(status);
        bean.setPersonalUseFg(personalUseFg);
        bean.setCoDriverId(coDriverId);
        bean.setSplitFg(splitFg);
        // set event date as start date
        bean.setEventDateTime(eventDate);
        bean.setRule(ruleId);
        bean.setDeferedDay(deferFg);
        return bean;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 14 January 2019
    // Purpose: post Event By Date
    public static JSONArray DailyLogEventByDate(int logId) {

        JSONArray array = new JSONArray();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();


            cursor = database.rawQuery("select _id,DriverId ,OnlineEventId ,EventSequenceId ,EventType ,EventCode ,EventCodeDescription ,OdometerReading ,EngineHour," +
                    " EventRecordOrigin ,EventRecordStatus ,EventDateTime ,Latitude ,Longitude ,LocationDescription ,DailyLogId ,CreatedBy ,CreatedDate ,ModifiedBy " +
                    ",ModifiedDate ,StatusId ,SyncFg,VehicleId,CheckSum,DistanceSinceLastValidCoordinate ,MalfunctionIndicatorFg ,DataDiagnosticIndicatorFg" +
                    " ,DiagnosticCode ,AccumulatedVehicleMiles ,ElaspsedEngineHour ,MotorCarrier,ShippingDocumentNo,TrailerNo ,TimeZoneOffsetUTC,Annotation,CoDriverId,RuleId,Splitfg,TimeZoneShortName from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DailyLogId=?", new String[]{logId + ""});
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();

                obj.put("VEHICLEID", cursor.getInt(cursor.getColumnIndex("VehicleId")));
                obj.put("EVENTID", cursor.getInt(cursor.getColumnIndex("_id")));
                obj.put("EVENTSEQUENCEID", cursor.getInt(cursor.getColumnIndex("EventSequenceId")));
                obj.put("EVENTTYPE", cursor.getInt(cursor.getColumnIndex("EventType")));
                obj.put("EVENTCODE", cursor.getInt(cursor.getColumnIndex("EventCode")));
                String description = cursor.getString(cursor.getColumnIndex("EventCodeDescription"));

                obj.put("EVENTCODEDESCRIPTION", description);
                obj.put("ODOMETERREADING", Double.parseDouble(cursor.getString(cursor.getColumnIndex("OdometerReading"))));
                obj.put("ENGINEHOUR", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineHour"))));
                obj.put("EVENTRECORDORIGIN", cursor.getInt(cursor.getColumnIndex("EventRecordOrigin")));
                obj.put("EVENTRECORDSTATUS", cursor.getInt(cursor.getColumnIndex("EventRecordStatus")));
                obj.put("EVENTDATETIME", cursor.getString(cursor.getColumnIndex("EventDateTime")));
                try {
                    obj.put("LATITUDE", Double.parseDouble(cursor.getString(cursor.getColumnIndex("Latitude"))));
                    obj.put("LONGITUDE", Double.parseDouble(cursor.getString(cursor.getColumnIndex("Longitude"))));
                } catch (Exception exe) {

                    obj.put("LATITUDE", "0");
                    obj.put("LONGITUDE", "0");
                }
                obj.put("LOCATIONDESCRIPTION", cursor.getString(cursor.getColumnIndex("LocationDescription")));
                obj.put("EVENTCREATEDBY", cursor.getInt(cursor.getColumnIndex("CreatedBy")));
                obj.put("EVENTCREATEDDATE", cursor.getString(cursor.getColumnIndex("CreatedDate")));
                obj.put("MODIFIEDBY", cursor.getInt(cursor.getColumnIndex("ModifiedBy")));
                String modifiedDate = cursor.getString(cursor.getColumnIndex("ModifiedDate"));
                modifiedDate = (modifiedDate == null || modifiedDate.isEmpty()) ? "1970-01-01 00:00:00" : modifiedDate;
                obj.put("MODIFIEDDATE", modifiedDate);
                obj.put("DRIVERID", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("DAILYLOGID", cursor.getInt(cursor.getColumnIndex("DailyLogId")));
                obj.put("Annotation", cursor.getString(cursor.getColumnIndex("Annotation")));
                obj.put("CheckSum", cursor.getString(cursor.getColumnIndex("CheckSum")));

                double distanceSince = 0d;
                double accumulatedVehicleMiles = 0d;
                double elaspsedEngineHour = 0d;

                try {
                    distanceSince = Double.parseDouble(cursor.getString(cursor.getColumnIndex("DistanceSinceLastValidCoordinate")));
                    accumulatedVehicleMiles = Double.parseDouble(cursor.getString(cursor.getColumnIndex("AccumulatedVehicleMiles")));
                    elaspsedEngineHour = Double.parseDouble(cursor.getString(cursor.getColumnIndex("ElaspsedEngineHour")));
                } catch (Exception exe) {
                }

                obj.put("DistanceSinceLastValidCoordinate", distanceSince);
                obj.put("AccumulatedVehicleMiles", accumulatedVehicleMiles);
                obj.put("ElaspsedEngineHour", elaspsedEngineHour);
                obj.put("MalfunctionIndicatorFg", cursor.getInt(cursor.getColumnIndex("MalfunctionIndicatorFg")));
                obj.put("DataDiagnosticIndicatorFg", cursor.getInt(cursor.getColumnIndex("DataDiagnosticIndicatorFg")));
                obj.put("DiagnosticCode", cursor.getString(cursor.getColumnIndex("DiagnosticCode")));
                obj.put("MotorCarrier", cursor.getString(cursor.getColumnIndex("MotorCarrier")));
                obj.put("ShippingDocumentNo", cursor.getString(cursor.getColumnIndex("ShippingDocumentNo")));
                obj.put("TrailerNo", cursor.getString(cursor.getColumnIndex("TrailerNo")));
                obj.put("TimeZoneOffsetUTC", cursor.getString(cursor.getColumnIndex("TimeZoneOffsetUTC")));
                obj.put("CoDriverId", cursor.getString(cursor.getColumnIndex("CoDriverId")));
                obj.put("RuleId", cursor.getInt(cursor.getColumnIndex("RuleId")));
                obj.put("SplitSleepFg", cursor.getInt(cursor.getColumnIndex("Splitfg")));
                obj.put("TimeZoneShortName", cursor.getString(cursor.getColumnIndex("TimeZoneShortName")));
                array.put(obj);
            }

        } catch (Exception exe) {
            Log.e("EventDB", exe.getMessage());
            LogFile.write(EventDB.class.getName() + "::DailyLogEventGetSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::DailyLogEventGetSync Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return array;
    }

    // Created By: Deepak Sharma
    // Created Date: 8 april 2019
    // Purpose: to check if off duty qualified for split sleep
    public static String splitSleepQualifiedFg(int driverId, int currentRule, String currentDate, boolean teamDriver) {
        boolean splitFg = false;
        String splitEventDate = "";
        float offHours = 0f;
        float sleeperHours = 0f;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            String sql = "select EventDateTime dutyStatusTime, case when EventType=3 and EventCode=2 then 4 else EventCode end dutyStatus,case when EventType=3 and EventCode!=0 then 1 else 0 end personalUseFg, Splitfg" +
                    " from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where EventRecordStatus=1 and EventType in (1,3) and EventCode>0  and EventDateTime < ? and DriverId =? order by EventDateTime desc ";

            cursor = database.rawQuery(sql, new String[]{currentDate, driverId + ""});

            // iterate duty status from date of event to previous non driving event
            while (cursor.moveToNext()) {
                int status = cursor.getInt(cursor.getColumnIndex("dutyStatus"));

                // if driver is already using split sleep then by pass the process
                if (cursor.getInt(cursor.getColumnIndex("Splitfg")) == 1) {
                    sleeperHours = 0;
                    offHours = 0;
                    splitFg = false;
                    break;
                }

                // if duty status is non driving i.e off or sleeper
                if (status < 3) {

                    // event date
                    String statusDateString = cursor.getString(cursor.getColumnIndex("dutyStatusTime"));

                    // calculate off and sleeper hours seprately
                    offHours += status == 1 ? Utility.getDiffTime(statusDateString, currentDate) : 0;

                    // calculate off and sleeper hours seprately
                    sleeperHours += status == 2 ? Utility.getDiffTime(statusDateString, currentDate) : 0;
                    currentDate = statusDateString;
                } else
                    break;
            }

            // if sleeper hours greater than or equal to 8 then reset shift
            // if combination of sleeper and off duty is 8 then reset shift
            if (currentRule <= 2 && sleeperHours >= (teamDriver ? 4 : 2) && sleeperHours <= 8 && (sleeperHours + offHours) < 8) {
                splitFg = true;
            } else if (currentRule >= 3) {
                // if sleeper hours or off duty hours or combination of both greater than equal to 10 then reset shift
                if ((sleeperHours >= 8 && sleeperHours < 10 && (sleeperHours + offHours) < 10) || (offHours + sleeperHours >= 2 && offHours + sleeperHours < 8)) {
                    splitFg = true;
                }
            }
            if (splitFg) {
                splitEventDate = currentDate;
            }

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(HourOfServiceDB.class.getName() + "::DutyStatusGet15Days Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(HourOfServiceDB.class.getName(), "::DutyStatusGet15Days Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {

            }
        }
        return splitEventDate;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 5 September 2018
    // Purpose: Event update for Split Sleep
    public static boolean EventUpdateForSplitSleep(String eventDateTime, int driverId, int splitFg, int currentRule) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean recordStatus = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            int eventId = 0;
            // check if previous duty status are same as current duty status
            for (int j = MainActivity.dutyStatusArrayList.size() - 1; j >= 0; j--) {
                DutyStatus item = MainActivity.dutyStatusArrayList.get(j);
                Date eventDate = item.getEventDateTime();

                // if event is on or before passed eventdatetime
                if (eventDate.before(Utility.parse(eventDateTime)) || eventDate.equals(Utility.parse(eventDateTime))) {

                    // if current rule is canada then check for sleeper berth
                    if ((currentRule <= 2 && item.getStatus() != 2) || item.getStatus() >= 3)
                        break;
                    else
                        eventId = item.getEventId();
                }
            }

            // if eventid is zero then search from database
            if (eventId == 0) {
                eventId = getEventId(driverId, eventDateTime);

            }

            ContentValues values = new ContentValues();
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            values.put("Splitfg", splitFg);
            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " _id= ?",
                    new String[]{eventId + ""});
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUpdateForSplitSleep Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventUpdateForSplitSleep Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return recordStatus;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 20  May 2019
    // Purpose: get Selected day edit request event
    // Parameter :  int driverid,int dailyLogid
    public static Boolean GetCurrentDayEventEditRequest(int driverId, int dailyLogId) {
        Boolean isCurrentDayEditRequest = false;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,EventDateTime,EventType ,EventCode ,EventCodeDescription,Latitude ,Longitude ,LocationDescription,OdometerReading,CreatedDate,DailyLogId from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and DailyLogId=? and EventRecordStatus=3 and EventRecordOrigin=3 order by EventDateTime desc LIMIT 1", new String[]{driverId + "", dailyLogId + ""});
            if (cursor.moveToNext()) {
                isCurrentDayEditRequest = true;
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::GetCurrentDayEventEditRequest Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::GetCurrentDayEventEditRequest Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return isCurrentDayEditRequest;
    }

    public static ArrayList<DutyStatusBean> DutyStatusGetToday(int driverId) {
        ArrayList<DutyStatusBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            String logDate = Utility.getCurrentDate();
            String logDateEnd = Utility.getPreviousDateOnly(1);
            Date endDate = Utility.parse(logDateEnd + " 00:00:00");
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            String sql = "select EventDateTime dutyStatusTime ,case when EventType=3 and EventCode=2 then 4 else EventCode end dutyStatus," +
                    "case when EventType=3 and EventCode!=0 then 1 else 0 end personalUseFg,EventType,EventCode from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " where EventRecordStatus=1 and EventType in (1,3) and EventCode>0 and  DriverId=? and EventDateTime < ? order by EventDateTime desc LIMIT 1 ";

            cursor = database.rawQuery(sql, new String[]{driverId + "", logDate});
            int lastDutyStatus = 1;
            int personalUseFg = 0;
            int eventType = 1;
            int eventCode = 1;
            int specialStatus = 0; // 0: none, 1: PC, 2: YM

            if (cursor.moveToNext()) {
                lastDutyStatus = cursor.getInt(cursor.getColumnIndex("dutyStatus"));
                personalUseFg = cursor.getInt(cursor.getColumnIndex("personalUseFg"));

                eventType = cursor.getInt(cursor.getColumnIndex("EventType"));
                eventCode = cursor.getInt(cursor.getColumnIndex("EventCode"));

                if (eventType == 3) {
                    specialStatus = eventCode;
                }
            }
            cursor.close();
            sql = "select CREATEDDATE,EVENTRECORDSTATUS, EventDateTime dutyStatusTime, case when EventType=3 and EventCode=2 then 4 else EventCode end dutyStatus,case when EventType=3 and EventCode!=0 then 1 else 0 end personalUseFg,EventType,EventCode" +
                    " from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where EventRecordStatus=1 and EventType in (1,3) and EventCode>0  and EventDateTime between ? and ? and DriverId =? order by EventDateTime ";

            cursor = database.rawQuery(sql, new String[]{logDate, logDateEnd, driverId + ""});
            DutyStatusBean bean = DutyStatusObjectGet(lastDutyStatus, Utility.newDateOnly(), endDate, 0, personalUseFg, specialStatus);
            bean.setEventType(eventType);
            bean.setEventCode(eventCode);

            while (cursor.moveToNext()) {
                String statusDateString = cursor.getString(cursor.getColumnIndex("dutyStatusTime"));
                Date statusDate = Utility.parse(statusDateString);
                bean.setEndTime(statusDateString); // start time of this status is end time of previous status

                int status = cursor.getInt(cursor.getColumnIndex("dutyStatus"));
                personalUseFg = cursor.getInt(cursor.getColumnIndex("personalUseFg"));

                eventType = cursor.getInt(cursor.getColumnIndex("EventType"));
                eventCode = cursor.getInt(cursor.getColumnIndex("EventCode"));

                if (eventType == 3) {
                    specialStatus = eventCode;
                } else {
                    specialStatus = 0;
                }

                int previousStatus = bean.getStatus();

                if (status != previousStatus || eventType != bean.getEventType()) {
                    // update total duration of previous status
                    Date startTime = Utility.parse(bean.getStartTime()); // start time of previous status
                    int totalMinute = (int) Math.round((statusDate.getTime() - startTime.getTime()) / (1000 * 60.0));  // total duration of previous status
                    bean.setTotalMinutes(totalMinute); // set total minute of previous status
                    list.add(bean); // add status to list

                    // create object for next status where cursor position is right now
                    bean = DutyStatusObjectGet(status, statusDate, endDate, 0, personalUseFg, specialStatus); // end time of previous status
                    bean.setEventType(eventType);
                    bean.setEventCode(eventCode);
                }
            }
            Date startTime = Utility.parse(bean.getStartTime()); // start time of previous status
            int totalMinute = (int) Math.round((endDate.getTime() - startTime.getTime()) / (1000 * 60.0));
            bean.setEndTime(Utility.format(endDate, fullFormat));
            bean.setTotalMinutes(totalMinute);

            list.add(bean);

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(HourOfServiceDB.class.getName() + "::DutyStatusGetToday Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(HourOfServiceDB.class.getName(), "::DutyStatusGetToday Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return list;
    }

    public static ArrayList<DutyStatusBean> DutyStatusGetByDate(int driverId, Date date) {
        ArrayList<DutyStatusBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            String logDate = Utility.format(date, CustomDateFormat.d1);
            String logDateEnd = Utility.format(Utility.getDate(date, 1), CustomDateFormat.d1);
            Date endDate = Utility.parse(logDateEnd + " 00:00:00");
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            String sql = "select EventDateTime dutyStatusTime ,case when EventType=3 and EventCode=2 then 4 else EventCode end dutyStatus," +
                    "case when EventType=3 and EventCode!=0 then 1 else 0 end personalUseFg,EventType,EventCode from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " where EventRecordStatus=1 and EventType in (1,3) and EventCode>0 and  DriverId=? and EventDateTime < ? order by EventDateTime desc LIMIT 1 ";

            cursor = database.rawQuery(sql, new String[]{driverId + "", logDate});
            int lastDutyStatus = 1;
            int personalUseFg = 0;

            int eventType = 1;
            int eventCode = 1;
            int specialStatus = 0; // 0: none, 1: PC, 2: YM

            if (cursor.moveToNext()) {
                lastDutyStatus = cursor.getInt(cursor.getColumnIndex("dutyStatus"));
                personalUseFg = cursor.getInt(cursor.getColumnIndex("personalUseFg"));

                eventType = cursor.getInt(cursor.getColumnIndex("EventType"));
                eventCode = cursor.getInt(cursor.getColumnIndex("EventCode"));
                if (eventType == 3) {
                    specialStatus = eventCode;
                }
            }
            cursor.close();
            sql = "select EventDateTime dutyStatusTime, case when EventType=3 and EventCode=2 then 4 else EventCode end dutyStatus,case when EventType=3 and EventCode!=0 then 1 else 0 end personalUseFg,EventType,EventCode" +
                    " from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where EventRecordStatus=1 and EventType in (1,3) and EventCode>0  and EventDateTime between ? and ? and DriverId =? order by EventDateTime ";

            cursor = database.rawQuery(sql, new String[]{logDate, logDateEnd, driverId + ""});
            DutyStatusBean bean = DutyStatusObjectGet(lastDutyStatus, date, endDate, 0, personalUseFg, specialStatus);
            bean.setEventType(eventType);
            bean.setEventCode(eventCode);
            while (cursor.moveToNext()) {
                String statusDateString = cursor.getString(cursor.getColumnIndex("dutyStatusTime"));
                Date statusDate = Utility.parse(statusDateString);
                bean.setEndTime(statusDateString); // start time of this status is end time of previous status

                int status = cursor.getInt(cursor.getColumnIndex("dutyStatus"));
                personalUseFg = cursor.getInt(cursor.getColumnIndex("personalUseFg"));

                eventType = cursor.getInt(cursor.getColumnIndex("EventType"));
                eventCode = cursor.getInt(cursor.getColumnIndex("EventCode"));

                if (eventType == 3) {
                    specialStatus = eventCode;
                } else {
                    specialStatus = 0;
                }

                int previousStatus = bean.getStatus();

                // if event type is different then add separate item
                if (status != previousStatus || eventType != bean.getEventType()) {
                    // update total duration of previous status
                    Date startTime = Utility.parse(bean.getStartTime()); // start time of previous status
                    int totalMinute = (int) Math.round((statusDate.getTime() - startTime.getTime()) / (1000 * 60.0));  // total duration of previous status
                    bean.setTotalMinutes(totalMinute); // set total minute of previous status
                    list.add(bean); // add status to list

                    // create object for next status where cursor position is right now
                    bean = DutyStatusObjectGet(status, statusDate, endDate, 0, personalUseFg, specialStatus); // end time of previous status
                    bean.setEventType(eventType);
                    bean.setEventCode(eventCode);
                }
            }
            Date startTime = Utility.parse(bean.getStartTime()); // start time of previous status
            int totalMinute = (int) Math.round((endDate.getTime() - startTime.getTime()) / (1000 * 60.0));
            bean.setEndTime(Utility.format(endDate, fullFormat));
            bean.setTotalMinutes(totalMinute);
            list.add(bean);

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(HourOfServiceDB.class.getName() + "::DutyStatusGetByDate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(HourOfServiceDB.class.getName(), "::DutyStatusGetByDate Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return list;
    }


    static String fullFormat = "yyyy-MM-dd HH:mm:ss";

    public static DutyStatusBean DutyStatusObjectGet(int status, Date startTime, Date endTime, int totalMinutes, int personalUseFg, int specialStatus) {
        DutyStatusBean bean = new DutyStatusBean();
        bean.setStatus(status);
        bean.setStartTime(Utility.format(startTime, fullFormat));
        bean.setEndTime(Utility.format(endTime, fullFormat));
        bean.setTotalMinutes(totalMinutes);
        bean.setPersonalUse(personalUseFg);
        bean.setSpecialStatus(specialStatus);
        return bean;
    }

    //Created By: Simran
    //Created Date: 30/01/2020
    //Purpose: get unposted dailylog events
    public static JSONArray getUnpostedEvents() {
        JSONArray array = new JSONArray();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id, DailyLogId  from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where SyncFg=? ", new String[]{"0"});
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();

                obj.put("DAILYLOGID", cursor.getInt(cursor.getColumnIndex("DailyLogId")));

                array.put(obj);
            }

        } catch (Exception exe) {
            Log.e("EventDB", exe.getMessage());
            LogFile.write(EventDB.class.getName() + "::DailyLogEventGetSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::DailyLogEventGetSync Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return array;
    }

    public static int MidnightEventSave(String logDate, String lat, String lon, String odometerReading, String engineHours, int driverId) {

        // if midnight event exists
        if (EventDB.checkDuplicate(logDate + " 00:00:02", driverId)) {
            return -3;
        }

        String previousDate = Utility.getDateFromString(logDate, -1);

        // return if already have mid night event for log date
        if (EventDB.checkDuplicate(previousDate + " 23:59:59", driverId)) {
            return -2;
        }

        int eventType = 2;
        int eventCode = 1;
        String eventDescription = Utility.context.getString(R.string.IL_conventional_description);

        // get last event before mid night
        EventBean lastEvent = EventDB.previousDutyStatusGet(driverId, logDate);

        // if last event is driving then add ILC else copy previous event type, code and description
        if (!(lastEvent.getEventType() == 1 && lastEvent.getEventCode() == 3)) {
            eventType = lastEvent.getEventType();
            eventCode = lastEvent.getEventCode();
            eventDescription = lastEvent.getEventCodeDescription();
        }

        ReportDB obj = new ReportDB(Utility.context);

        // add midnight event of previous day
        obj.EventCreate(driverId, previousDate + " 23:59:59", eventType, eventCode, eventDescription, 1, 1, "mid night event", odometerReading, engineHours, lat, lon, "", lastEvent.getCoDriverId());


        // add midnight event of log date
        obj.EventCreate(driverId, logDate + " 00:00:02", eventType, eventCode, eventDescription, 1, 1, "mid night event", odometerReading, engineHours, lat, lon, "", lastEvent.getCoDriverId());

        // success
        return 1;
    }


    //created By: Simran
    //created date: 27 Feb 2020
    // purpose: update event after getting update from hutch support
    public static boolean UpdateEvent(int _id, String eventDateTime, int driverId, int eventType, int eventCode, String odoReading,
                                      String engineHours, String latitude, String longitude, String location, String eventCodeDes,
                                      String vehicleMiles, String elapsedEngineHour, String shippingNo, String trailerNo,
                                      int eventRecordStatus) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean recordStatus = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("DriverId", driverId);
            values.put("EventDateTime", eventDateTime);
            values.put("EventType", eventType);
            values.put("EventCode", eventCode);
            values.put("EventCodeDescription", eventCodeDes);
            values.put("OdometerReading", odoReading);
            values.put("EngineHour", engineHours);
            values.put("Latitude", latitude);
            values.put("Longitude", longitude);
            values.put("LocationDescription", location);
            values.put("AccumulatedVehicleMiles", vehicleMiles);
            values.put("ElaspsedEngineHour", elapsedEngineHour);
            values.put("ShippingDocumentNo", shippingNo);
            values.put("TrailerNo", trailerNo);
            values.put("EventRecordStatus", eventRecordStatus);
            values.put("CheckSum", getCheckSum(eventType, eventCode, eventDateTime, odoReading,
                    engineHours, latitude, longitude, driverId));
            values.put("SyncFg", 0);

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values, " _id= ?",
                    new String[]{_id + ""});
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventUpdate Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return recordStatus;
    }

    //created By: Simran
    //created Date: 27 Feb 2020
    // Purpose: calculate checksum according to fields chnaged
    public static String getCheckSum(int eventType, int eventCode, String eventDateTime, String odoReading,
                                     String engineHour, String latitude, String longitude, int driverId) {
        int sum = 0;
        sum += Ascii.getSum(Integer.toString(eventType));
        sum += Ascii.getSum(Integer.toString(eventCode));
        sum += Ascii.getSum(Utility.parseDate(eventDateTime));
        sum += Ascii.getSum(Utility.timeOnlyGet(eventDateTime));
        sum += Ascii.getSum(odoReading);
        sum += Ascii.getSum(engineHour);
        sum += Ascii.getSum(latitude);
        sum += Ascii.getSum(longitude);
        sum += Ascii.getSum(Utility.UnitNo);
        String userName = UserDB.getUserName(driverId);

        sum += Ascii.getSum(userName);

        return Ascii.calculateCheckSum(sum);
    }


    // Created By: Deepak Sharma
    // Created Date: 06 May 2020
    // Purpose: get date for which events where not posted
    public static ArrayList<DailyLogBean> DailyLogUnPostedGet(int driverId, int dailyLogId) {
        ArrayList<DailyLogBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select d._id,d.LogDate from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " e join " + MySQLiteOpenHelper.TABLE_DAILYLOG + " d on d._id=e.dailylogid " +
                    " where d.DriverId=? and d._id<>? and e.syncfg=0 group by d.LogDate,d._id", new String[]{Integer.toString(driverId), dailyLogId + ""});
            while (cursor.moveToNext()) {
                DailyLogBean bean = new DailyLogBean();
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setLogDate(cursor.getString(cursor.getColumnIndex("LogDate")) + " 00:00:00");

                list.add(bean);
            }

            Collections.sort(list, DailyLogBean.dateDesc);
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::EventGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventGet Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }
}
