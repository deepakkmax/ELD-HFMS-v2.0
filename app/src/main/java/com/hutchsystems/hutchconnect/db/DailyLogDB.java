package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.beans.CoDriverBean;
import com.hutchsystems.hutchconnect.beans.DailyLogBean;
import com.hutchsystems.hutchconnect.beans.DutyStatusBean;
import com.hutchsystems.hutchconnect.beans.EventBean;
import com.hutchsystems.hutchconnect.beans.GPSData;
import com.hutchsystems.hutchconnect.beans.RuleBean;
import com.hutchsystems.hutchconnect.beans.VehicleBean;
import com.hutchsystems.hutchconnect.bll.HourOfService;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Deepak.Sharma on 1/14/2016.
 */
public class DailyLogDB {

    //Created By: Simran
    //Created Date: 30/01/2020
    //Purpose: get dailylog by id
    public static JSONObject DailyLogByIdSync(int dailyLogId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONObject obj = new JSONObject();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id ,LogDate ,DriverId ,ShippingId ,TrailerId ,StartTime ,StartOdometerReading ,EndOdometerReading ,CertifyFG ,certifyCount " +
                            ",Signature ,CreatedBy ,CreatedDate ,ModifiedBy ,ModifiedDate,DrivingTimeRemaining ,WorkShiftTimeRemaining ,TimeRemaining70 ,TimeRemaining120 ,TimeRemainingUS70 ,TimeRemainingReset ,DeferedDay  from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where SyncFg=? and _id=? "
                    , new String[]{"0", dailyLogId + ""});
            if (cursor.moveToNext()) {

                int logId = cursor.getInt(cursor.getColumnIndex("_id"));
                Utility.LogId = logId;

                obj.put("DAILYLOGID", logId);
                String logDate = cursor.getString(cursor.getColumnIndex("LogDate")) + " 00:00:00";
                obj.put("LOGDATE", logDate);
                obj.put("DRIVERID", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("SHIPPINGID", cursor.getString(cursor.getColumnIndex("ShippingId")));
                obj.put("TRAILERID", cursor.getString(cursor.getColumnIndex("TrailerId")));
                obj.put("STARTTIME", cursor.getString(cursor.getColumnIndex("StartTime")));
                obj.put("STARTODOMETERREADING", Double.parseDouble(cursor.getString(cursor.getColumnIndex("StartOdometerReading"))));
                obj.put("ENDODOMETERREADING", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EndOdometerReading"))));
                obj.put("CERTIFYFG", cursor.getInt(cursor.getColumnIndex("CertifyFG")));
                obj.put("CertifyCount", cursor.getInt(cursor.getColumnIndex("certifyCount")));
                String signature = cursor.getString(cursor.getColumnIndex("Signature"));
                obj.put("Signature", signature == null ? "" : signature);
                obj.put("COMPANYID", Utility.companyId);
                obj.put("VEHICLEID", Utility.vehicleId);
                obj.put("TIMEZONEOFFSETUTC", Utility.TimeZoneOffsetUTC);
                obj.put("LOGCREATEDBY", cursor.getInt(cursor.getColumnIndex("CreatedBy")));

                String createdDate = cursor.getString(cursor.getColumnIndex("CreatedDate"));

                if (createdDate.isEmpty()) {
                    createdDate = logDate + " 00:00:00";
                }
                obj.put("LOGCREATEDDATE", createdDate);
                obj.put("MODIFIEDBY", cursor.getInt(cursor.getColumnIndex("ModifiedBy")));
                String modifiedDate = cursor.getString(cursor.getColumnIndex("ModifiedDate"));
                modifiedDate = (modifiedDate == null || modifiedDate.isEmpty()) ? "1970-01-01 00:00:00" : modifiedDate;
                obj.put("MODIFIEDDATE", modifiedDate);
                obj.put("DrivingTimeRemaining", cursor.getInt(cursor.getColumnIndex("DrivingTimeRemaining")));
                obj.put("WorkShiftRemaining", cursor.getInt(cursor.getColumnIndex("WorkShiftTimeRemaining")));
                obj.put("TimeRemaining70", cursor.getInt(cursor.getColumnIndex("TimeRemaining70")));
                obj.put("TimeRemaining120", cursor.getInt(cursor.getColumnIndex("TimeRemaining120")));
                obj.put("TimeRemainingUS70", cursor.getInt(cursor.getColumnIndex("TimeRemainingUS70")));
                obj.put("TimeRemainingReset", cursor.getInt(cursor.getColumnIndex("TimeRemainingReset")));
                // get Defered Value from Database
                obj.put("DeferedDay", cursor.getInt(cursor.getColumnIndex("DeferedDay")));
                obj.put("EventList", EventDB.DailyLogEventGetSync(logId));
                obj.put("RuleList", DailyLogRuleGetSync(logId));

            }
        } catch (Exception exe) {
            Utility.LogId = 0;
            LogFile.write(DailyLogDB.class.getName() + "::getLogDataSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getLogDataSync Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {
                LogFile.write(DailyLogDB.class.getName() + "::getLogDataSync close DB Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::getLogDataSync close DB Error:", exe.getMessage(), Utility.printStackTrace(exe));

            }
        }
        return obj;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get rules for web sync
    public static JSONArray getDailyLogRuleSync(int logId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select DailyLogId ,RuleId ,RuleStartTime ,RuelEndTime from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE +
                            " where SyncFg=0 and DailyLogId=?"
                    , new String[]{logId + ""});
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("LogId", cursor.getInt(0));
                obj.put("RuleId", cursor.getInt(1));
                obj.put("RuleStartTime", Utility.getDateTimeForServer(cursor.getString(2)));
                obj.put("RuleEndTime", Utility.getDateTimeForServer(cursor.getString(3) == null ? "1970-01-01 00:00:00" : cursor.getString(3)));
                array.put(obj);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getDailyLogRuleSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getDailyLogRuleSync Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return array;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get rules for web sync
    public static ArrayList<RuleBean> getRuleByDriverId(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<RuleBean> list = new ArrayList<RuleBean>();
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select DailyLogId ,RuleId ,RuleStartTime ,RuelEndTime from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE +
                            " where DriverId=? order by RuleStartTime desc"
                    , new String[]{driverId + ""});
            while (cursor.moveToNext()) {

                RuleBean obj = new RuleBean();
                obj.setRuleId(cursor.getInt(cursor.getColumnIndex("RuleId")));
                Date ruleDate = Utility.parse(cursor.getString(cursor.getColumnIndex("RuleStartTime")));
                obj.setRuleStartTime(ruleDate);
                String endDate = cursor.getString(cursor.getColumnIndex("RuelEndTime"));
                if (endDate != null) {
                    Date ruleEndDate = Utility.parse(endDate);
                    obj.setRuleEndTime(ruleEndDate);
                }
                list.add(obj);
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getDailyLogRuleSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getDailyLogRuleSync Error:", exe.getMessage(), Utility.printStackTrace(exe));

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

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get rules for web sync
    public static ArrayList<RuleBean> getRuleByDate(String date, int driverId, int dailyLogId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<RuleBean> ruleList = new ArrayList<>();

        try {
            String nextDay = Utility.addDate(date, 1) + " 00:00:00";// Utility.sdf.format(Utility.addDays(new Date(), 1));
            Date selectedDate = Utility.parse(date);
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            String sql = "select * from  (select RuleId,RuleStartTime,RuelEndTime  from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE +
                    " where DriverId=" + driverId + " and RuleStartTime<'" + date + "'  order by RuleStartTime desc LIMIT 1)a union ";
            sql += "select RuleId,RuleStartTime,RuelEndTime  from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE + " where RuleStartTime>='" + date + "' and RuleStartTime<'" + nextDay + "' and DriverId=" + driverId + " order by RuleStartTime";
            cursor = database.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                RuleBean obj = new RuleBean();
                obj.setRuleId(cursor.getInt(cursor.getColumnIndex("RuleId")));
                Date ruleDate = Utility.parse(cursor.getString(cursor.getColumnIndex("RuleStartTime")));
                if (ruleDate.before(selectedDate)) {
                    ruleDate = selectedDate;
                }

                String endDate = cursor.getString(cursor.getColumnIndex("RuelEndTime"));
                endDate = endDate == null ? nextDay : endDate;

                Date ruleEndDate = Utility.parse(endDate);
                if (ruleEndDate.before(ruleDate)) {
                    ruleEndDate = Utility.parse(nextDay);
                }
                obj.setRuleStartTime(ruleDate);
                obj.setRuleEndTime(ruleEndDate);
                ruleList.add(obj);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getDailyLogRuleSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::AddDriver Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return ruleList;
    }

    public static int getCurrentRule(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        int ruleId = 1;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
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
            LogFile.write(DailyLogDB.class.getName() + "::getCurrentRule Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getCurrentRule Error:", exe.getMessage(), Utility.printStackTrace(exe));

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

    public static int getPreviousRule(int driverId, String except) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        int ruleId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();


            cursor = database.rawQuery("select RuleId from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE +
                            " where DriverId=? and RuleId not in(" + except + ")" +
                            "  order by RuleStartTime desc LIMIT 1"
                    , new String[]{Integer.toString(driverId)});
            if (cursor.moveToFirst()) {
                ruleId = cursor.getInt(0);
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getCurrentRule Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getCurrentRule Error:", exe.getMessage(), Utility.printStackTrace(exe));

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
    // Created Date: 27 March 2019
    // Purpose: get previous canada rule cycle 1 or cycle 2
    public static RuleBean getPreviousCanadaRule(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        RuleBean bean = new RuleBean();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            // query to get latest rule of canada
            cursor = database.rawQuery("select RuleId,RuleStartTime from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE +
                            " where DriverId=? and RuleId in (1,2,5,6,7)" +
                            "  order by RuleStartTime desc LIMIT 1"
                    , new String[]{Integer.toString(driverId)});

            if (cursor.moveToFirst()) {
                bean.setRuleId(cursor.getInt(0));
                Date ruleDate = Utility.parse(cursor.getString(cursor.getColumnIndex("RuleStartTime")));
                bean.setRuleStartTime(ruleDate);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getCurrentRule Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getCurrentRule Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return bean;
    }

    public static int DailyLogUserPreferenceRuleSave(int driverId, int ruleId, String ruleStartTime, String ruleEndTime) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int logId = 0;
        try {
            // update current rule in user table
            UserDB.Update("CurrentRule", ruleId + "");
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            updateRuleEndTime(driverId, ruleStartTime);
            ContentValues values = new ContentValues();

            logId = DailyLogCreate(driverId, "", "", "");
            values.put("DailyLogId", logId);
            values.put("DriverId", driverId);
            values.put("RuleId", ruleId);
            values.put("RuleStartTime", ruleStartTime);
            values.put("SyncFg", 0);
            database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG_RULE,
                    "RuleId", values);
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogRuleSave Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::AddDriver Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return logId;
    }

    private static void updateRuleEndTime(int driverId, String ruleStartTime) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String sql = "select _id  from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE +
                    " where DriverId=" + driverId + " and RuleStartTime<'" + ruleStartTime + "'  order by RuleStartTime desc LIMIT 1 ";


            cursor = database.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                ContentValues values = new ContentValues();
                values.put("RuelEndTime", ruleStartTime);
                values.put("SyncFg", 0);
                database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_RULE, values,
                        " _id= ?", new String[]{id
                                + ""});
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::updateRuleEndTime Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::updateRuleEndTime Error:", exe.getMessage(), Utility.printStackTrace(exe));

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

    public static int DailyLogRuleSave(int driverId, int ruleId, String ruleStartTime, String ruleEndTime) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        int logId = 0;
        try {
            UserDB.Update("CurrentRule", ruleId + "");

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            updateRuleEndTime(driverId, ruleStartTime);

            logId = getDailyLog(driverId, Utility.getCurrentDate());
            values.put("DailyLogId", logId);
            values.put("RuleId", ruleId);
            values.put("RuleStartTime", ruleStartTime);
            values.put("DriverId", driverId);
            // values.put("RuelEndTime", ruleEndTime);
            values.put("SyncFg", 0);
            database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG_RULE,
                    "RuleId", values);
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogRuleSave Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogRuleSave Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return logId;
    }

    // Created By: Deepak Sharma
    // Created Date: 09 April 2019
    // Purpose: save rule list into database
    public static boolean DailyLogRuleSave(ArrayList<RuleBean> list) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = true;

        try {

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            for (RuleBean item : list) {
                int ruleId = 0;
                ContentValues values = new ContentValues();

                int logId = getDailyLog(item.getDriverId(), Utility.dateOnlyStringGet(item.getStartDate()));

                Cursor cursor = database.rawQuery("select _id from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE +
                                " where RuleStartTime=? and driverId=? LIMIT 1 "

                        , new String[]{item.getStartDate(), item.getDriverId() + ""});
                values.put("DailyLogId", logId);
                values.put("RuleId", item.getRuleId());
                values.put("RuleStartTime", item.getStartDate());
                values.put("DriverId", item.getDriverId());
                values.put("RuelEndTime", item.getEndDate());
                values.put("SyncFg", 1);

                if (cursor.moveToNext()) {
                    ruleId = cursor.getInt(0);
                }

                // if rule does not exist in database then insert else update
                if (ruleId == 0) {

                    database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG_RULE,
                            "RuleId", values);
                } else {
                    database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_RULE, values,
                            " _id= ?", new String[]{ruleId
                                    + ""});
                }
                cursor.close();
            }

        } catch (Exception e) {
            status = false;
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogRuleSave Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogRuleSave Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get codriver for web sync
    public static JSONArray getCoDriverSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select DriverId ,DriverId2 ,LoginTime ,LogoutTime  from " + MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER +
                            " where SyncFg=0"
                    , null);
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("DriverId", cursor.getInt(0));
                obj.put("DriverId2", cursor.getInt(1));
                obj.put("LoginTime", Utility.getDateTimeForServer(cursor.getString(2)));
                obj.put("LogoutTime", Utility.getDateTimeForServer(cursor.getString(3) == null ? "1970-01-01 00:00:00" : cursor.getString(3)));
                array.put(obj);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getCoDriverSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::AddDriver Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return array;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get codriver for driver
    public static int getCoDriver(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select DriverId,DriverId2 from " + MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER +
                            " where (DriverId=? or DriverId2=?) order by LoginTime desc LIMIT 1"
                    , new String[]{driverId + "", driverId + ""});
            while (cursor.moveToNext()) {
                int driverId1 = cursor.getInt(0);
                int driverId2 = cursor.getInt(1);
                id = driverId1 == driverId ? driverId2 : driverId1;

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getCoDriver Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getCoDriver Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return id;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get codriver for driver
    public static String getCoDriver(int driverId, String logDate) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        String coDrivers = "";
        String coDriverIds = "";
        try {
            int dailyLogId = getDailyLog(driverId, logDate);

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            int coDriverId = 0;

            //get last login user
            Cursor cursor1 = database.rawQuery("select DriverId,EventCode,a.FirstName,a.LastName from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " e join " +
                    MySQLiteOpenHelper.TABLE_ACCOUNT + " a on a.AccountId=e.DriverId " + " where EventType = 5 and CoDriverId=? and EventDateTime<? order by EventDateTime desc LIMIT 1", new String[]{driverId + "", logDate + " 23:59:59"});

            if (cursor1.moveToNext()) {
                int eventCode = cursor1.getInt(1);
                if (eventCode == 1) {
                    coDriverId = cursor1.getInt(0);
                    coDriverIds += coDriverId + ",";
                    coDrivers += cursor1.getString(2) + ",";
                }
            }
            cursor1.close();

            // retrive co driver from event list except last loginned
            String query = "select distinct e.CoDriverId,a.FirstName,a.LastName from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " e join " +
                    MySQLiteOpenHelper.TABLE_ACCOUNT + " a on e.CoDriverId=a.AccountId where DailyLogId=?  and EventRecordStatus=1 and e.CoDriverId>0 and CoDriverId !=? ";

            cursor = database.rawQuery(query, new String[]{dailyLogId + "", coDriverId + ""});

            while (cursor.moveToNext()) {
                coDriverIds += cursor.getInt(0) + ",";
                coDrivers += cursor.getString(1) + ",";
            }

         /* cursor = database.rawQuery("select cd.DriverId, U.FirstName Driver1,cd.DriverId2,U1.FirstName Driver2 from " + MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER +
                            " cd join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " U on cd.DriverId=U.AccountId " +
                            " join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " U1 on cd.DriverId2=U1.AccountId where (DriverId=? or DriverId2=?) and date(LoginTime)=?"
                    , new String[]{driverId + "", driverId + "", logDate});*/

           /* while (cursor.moveToNext()) {
                int driver1Id = cursor.getInt(0);
                String driver1 = cursor.getString(1);

                int driver2Id = cursor.getInt(2);
                String driver2 = cursor.getString(3);

                String codDriver = (driverId == driver1Id ? driver2 : driver1);
                int codDriverId = (driverId == driver1Id ? driver2Id : driver1Id);
                if (coDrivers.indexOf(codDriver) == -1) {
                    coDriverIds += codDriverId + ",";
                    coDrivers += codDriver + ",";
                }
            }*/
            if (coDriverIds.contains(",")) {
                coDriverIds = coDriverIds.substring(0, coDriverIds.length() - 1);
                coDrivers = coDrivers.substring(0, coDrivers.length() - 1);
            }
        /*    else if (Utility.user2.getAccountId() > 0 && logDate.equals(Utility.getCurrentDate())) {
                UserBean user = Utility.user2;
                if (Utility.user2.getAccountId() == driverId) {
                    user = Utility.user1;
                }
                coDriverIds = user.getAccountId() + "";
                coDrivers = user.getFirstName();
            }*/

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getCoDriver Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getCoDriver Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        if ((coDriverIds.equals("") && coDrivers.equals("")))
            return "";
        /* if(coDriverIds.equals(Utility.user1.getAccountId())) {
             return "";
         }*/
        return coDriverIds + "#" + coDrivers;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get codriver for driver
    public static ArrayList<DutyStatusBean> getCoDriverList(int driverId, Date logDate) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        String coDriverIds = "";
        ArrayList<DutyStatusBean> listCoDriver = new ArrayList<>();
        Date toDate = Utility.dateOnlyGet(logDate);
        Date fromDate = Utility.addDays(toDate, -15);

        String start = Utility.format(fromDate, "yyyy-MM-dd HH:mm:ss");
        String end = Utility.format(toDate, "yyyy-MM-dd HH:mm:ss");
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select cd.DriverId,cd.DriverId2,LoginTime from " + MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER +
                            " cd join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " U on cd.DriverId=U.AccountId " +
                            " join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " U1 on cd.DriverId2=U1.AccountId where (DriverId=? or DriverId2=?) and LoginTime between ? and ?"
                    , new String[]{driverId + "", driverId + "", start, end});

            while (cursor.moveToNext()) {
                DutyStatusBean coDriver = new DutyStatusBean();
                int driver1Id = cursor.getInt(0);
                int driver2Id = cursor.getInt(1);
                String startTime = cursor.getString(2);
                int codDriverId = (driverId == driver1Id ? driver2Id : driver1Id);
                coDriver.setDriverId(codDriverId);
                coDriver.setStartTime(startTime);
                listCoDriver.add(coDriver);
                if (!coDriverIds.contains(codDriverId + ","))
                    coDriverIds += codDriverId + ",";

            }

            if (coDriverIds.contains(",")) {
                coDriverIds = coDriverIds.substring(0, coDriverIds.length() - 1);
            }

            HourOfService.coDriverIds = coDriverIds;

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getCoDriver Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getCoDriverList Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return listCoDriver;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add codriver
    public static void AddDriver(int driver1, int driver2, int _id) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 0);
            if (_id == 0) {

                values.put("DriverId", driver1);
                values.put("DriverId2", driver2);
                values.put("LoginTime", Utility.getCurrentDateTime());
                database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER,
                        "_id", values);
            } else {
                values.put("LogoutTime", Utility.getCurrentDateTime());
                database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER, values,
                        " _id= ?", new String[]{_id
                                + ""});
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::AddDriver Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::AddDriver Error:", e.getMessage(), Utility.printStackTrace(e));
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add codriver
    public static void CoDriverSave(ArrayList<CoDriverBean> list) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            for (CoDriverBean item : list) {
                int id = 0;
                ContentValues values = new ContentValues();

                Cursor cursor = database.rawQuery("select _id from " + MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER +
                                " where LoginTime=? and driverId=? LIMIT 1 "

                        , new String[]{item.getStartDate(), item.getDriverId() + ""});

                values.put("DriverId", item.getDriverId());
                values.put("DriverId2", item.getDriverId2());
                values.put("LoginTime", item.getStartDate());
                values.put("LogoutTime", item.getEndDate());
                values.put("SyncFg", 1);

                if (cursor.moveToNext()) {
                    id = cursor.getInt(0);
                }

                // if rule does not exist in database then insert else update
                if (id == 0) {

                    database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER,
                            "_id", values);
                } else {
                    database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER, values,
                            " _id= ?", new String[]{id
                                    + ""});
                }
                cursor.close();
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::AddDriver Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::AddDriver Error:", e.getMessage(), Utility.printStackTrace(e));
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get uncertified log data
    public static ArrayList<DailyLogBean> getUncertifiedDailyLog(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<DailyLogBean> list = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id ,LogDate,ShippingId,TrailerId,StartOdometerReading,EndOdometerReading from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where driverId=? and CertifyFG=0 and LogDate!=? order by LogDate"
                    , new String[]{driverId + "", Utility.getCurrentDate()});
            while (cursor.moveToNext()) {
                DailyLogBean bean = new DailyLogBean();
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setLogDate(cursor.getString(cursor.getColumnIndex("LogDate")));
                bean.setShippingId(cursor.getString(cursor.getColumnIndex("ShippingId")));
                bean.setTrailerId(cursor.getString(cursor.getColumnIndex("TrailerId")));
                bean.setStartOdometerReading(cursor.getString(cursor.getColumnIndex("StartOdometerReading")));
                bean.setEndOdometerReading(cursor.getString(cursor.getColumnIndex("EndOdometerReading")));
                list.add(bean);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getUncertifiedDailyLog Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getUncertifiedDailyLog Error:", exe.getMessage(), Utility.printStackTrace(exe));

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

    // Created By: Deepak Sharma
    // Created Date: 19 September 2016
    // Purpose: get uncertified log data
    public static boolean unCertifiedFg(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where driverId=? and CertifyFG=0 LIMIT 1"
                    , new String[]{driverId + ""});
            if (cursor.moveToNext()) {
                status = true;
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::unCertifiedFg Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::unCertifiedFg Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get uncertified log data
    public static boolean getUncertifiedLogFg(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        boolean uncertify = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id ,LogDate,ShippingId,TrailerId,StartOdometerReading,EndOdometerReading from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where driverId=? and CertifyFG=0 and LogDate!=? LIMIT 1"
                    , new String[]{driverId + "", Utility.getCurrentDate()});
            if (cursor.moveToNext()) {

                uncertify = true;
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getUncertifiedDailyLog Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getUncertifiedDailyLog Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return uncertify;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get count of certification
    public static int getCertifyCount(int logId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int count = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select certifyCount from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where _id =?"
                    , new String[]{logId + ""});
            if (cursor.moveToNext()) {
                count = cursor.getInt(0);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getCertifyCount Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getCertifyCount Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return count;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get daily log data
    public static int getDailyLog(int driverId, String logDate) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int logId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where LogDate=? and driverId=?"
                    , new String[]{logDate.toString(), driverId + ""});
            if (cursor.moveToNext()) {
                logId = cursor.getInt(0);
            }
        } catch (Exception exe) {
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getDailyLog Error:", exe.getMessage(), Utility.printStackTrace(exe));

            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getDailyLog Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return logId;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get daily log data
    public static String getLastDailyLogDate(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        String logDate = Utility.getCurrentDate();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select LogDate from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where LogDate<? and driverId=? order by LogDate desc LIMIT 1"
                    , new String[]{logDate, driverId + ""});
            if (cursor.moveToNext()) {
                logDate = cursor.getString(0);
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getLastDailyLogDate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::AddDriver Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return logDate;
    }

    // Created By: Deepak Sharma
    // Created Date: 8 July 2016
    // Purpose: get daily log data
    public static DailyLogBean getDailyLogInfo(int driverId, String logDate) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        DailyLogBean bean = new DailyLogBean();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id,CertifyFG, ShippingId, TrailerId,TotalDistance  from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where LogDate=? and driverId=?"
                    , new String[]{logDate.toString(), driverId + ""});
            if (cursor.moveToNext()) {
                bean.set_id(cursor.getInt(0));
                bean.setCertifyFG(cursor.getInt(1));
                bean.setShippingId(cursor.getString(2));
                bean.setTrailerId(cursor.getString(3));
                bean.setTotalDistance(cursor.getInt(4));
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getDailyLog Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getDailyLogInfo Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return bean;
    }

    public static int DailyLogCreateByDate(int driverId, String date, String shippingId, String trailerId, String commodity) {
        DailyLogBean bean = new DailyLogBean();
        bean.setDriverId(driverId);
        bean.setShippingId(shippingId);
        bean.setTrailerId(trailerId);
        bean.setCommodity(commodity);
        bean.setCreatedBy(driverId);
        bean.setModifiedBy(driverId);
        bean.setStartTime("00:00:00");
        bean.setLogDate(date);
        bean.setCreatedDate(date + " 00:00:00");
        bean.setModifiedDate(date + " 00:00:00");
        // if Previous day deferred value is 1 then update next days is 2 else get the current day value of deferred day.
        bean.setDeferedDay(getDeferedDay(driverId, -1) == 1 ? 2 : getDeferedDay(driverId, 0));
        return DailyLogSave(bean);
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: create daily log record
    public static int DailyLogCreate(int driverId, String shippingId, String trailerId, String commodity) {
        DailyLogBean bean = new DailyLogBean();
        bean.setDriverId(driverId);
        bean.setShippingId(shippingId);
        bean.setTrailerId(trailerId);
        bean.setCommodity(commodity);
        bean.setCreatedBy(driverId);
        bean.setModifiedBy(driverId);
        bean.setStartTime("00:00:00");
        bean.setLogDate(Utility.getCurrentDate());
        bean.setCreatedDate(Utility.getCurrentDateTime());
        bean.setModifiedDate(Utility.getCurrentDateTime());
        // if Previous day deferred value is 1 then update next days is 2 else get the current day value of deferred day.
        bean.setDeferedDay(getDeferedDay(driverId, -1) == 1 ? 2 : getDeferedDay(driverId, 0));
        return DailyLogSave(bean);
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add or update dailylog in database
    public static int DailyLogSave(DailyLogBean bean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int logId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("ShippingId", bean.getShippingId());
            values.put("TrailerId", bean.getTrailerId());
            values.put("Commodity", bean.getCommodity());
            values.put("EndOdometerReading", CanMessages.OdometerReading);
            values.put("StatusId", 1);
            values.put("SyncFg", 0);
            // get Defered Day value from bean
            values.put("DeferedDay", bean.getDeferedDay());
            logId = getDailyLog(bean.getDriverId(), bean.getLogDate());
            if (logId == 0) {
                values.put("CertifyFG", 0);
                values.put("OnlineDailyLogId", 0);
                values.put("DriverId", bean.getDriverId());
                values.put("LogDate", bean.getLogDate());
                values.put("StartTime", bean.getStartTime());
                values.put("CreatedBy", bean.getCreatedBy());
                values.put("CreatedDate", bean.getCreatedDate());
                values.put("StartOdometerReading", CanMessages.OdometerReading);

                values.put("DrivingTimeRemaining", GPSData.DrivingTimeRemaining);
                values.put("WorkShiftTimeRemaining", GPSData.WorkShiftRemaining);
                values.put("TimeRemaining70", GPSData.TimeRemaining70);
                values.put("TimeRemaining120", GPSData.TimeRemaining120);
                values.put("TimeRemainingUS70", GPSData.TimeRemainingUS70);
                values.put("TimeRemainingReset", GPSData.TimeRemainingReset);

                logId = (int) database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG,
                        "_id,ModifiedBy,ModifiedDate", values);
            } else {
                values.put("ModifiedBy", bean.getModifiedBy());
                values.put("ModifiedDate", bean.getModifiedDate());
                database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                        " _id= ?", new String[]{logId
                                + ""});
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogSave Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogSave Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::DailyLogSave close database Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogSave Error:", e.getMessage(), Utility.printStackTrace(e));
            }
        }
        return logId;
    }

    // Created By: Deepak Sharma
    // Created Date: 27 January 2015
    // Purpose: add or update multiple dailylog in database from web
    public static int DailyLogSave(ArrayList<DailyLogBean> arrBean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        ArrayList<EventBean> eventBeenList = new ArrayList<>();
        int logId = 0;
        try {

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            for (DailyLogBean bean : arrBean) {
                ContentValues values = new ContentValues();

                values.put("OnlineDailyLogId", bean.getOnlineDailyLogId());
                values.put("StatusId", 1);
                values.put("SyncFg", 1);
                logId = getDailyLog(bean.getDriverId(), bean.getLogDate());


                values.put("StartOdometerReading", bean.getStartOdometerReading());
                values.put("EndOdometerReading", bean.getEndOdometerReading());
                values.put("DrivingTimeRemaining", bean.getDrivingTimeRemaining());
                values.put("WorkShiftTimeRemaining", bean.getWorkShiftTimeRemaining());
                values.put("TimeRemaining70", bean.getTimeRemaining70());
                values.put("TimeRemaining120", bean.getTimeRemaining120());
                values.put("TimeRemainingUS70", bean.getTimeRemainingUS70());
                values.put("TimeRemainingReset", bean.getTimeRemainingReset());
                values.put("TotalDistance", bean.getTotalDistance());
                values.put("PCDistance", bean.getPCDistance());

                // get Defered Day value from bean
                values.put("DeferedDay", bean.getDeferedDay());

                if (logId == 0) {
                    values.put("ShippingId", bean.getShippingId());
                    values.put("TrailerId", bean.getTrailerId());
                    values.put("Commodity", bean.getCommodity());
                    values.put("CertifyFG", bean.getCertifyFG());
                    values.put("DriverId", bean.getDriverId());
                    values.put("LogDate", bean.getLogDate());
                    values.put("StartTime", bean.getStartTime());
                    values.put("CreatedBy", bean.getCreatedBy());
                    values.put("CreatedDate", bean.getCreatedDate());


                    logId = (int) database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG,
                            "_id,ModifiedBy,ModifiedDate", values);
                } else {
                    values.put("ModifiedBy", bean.getModifiedBy());
                    values.put("ModifiedDate", Utility.getCurrentDateTime());
                    database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                            " _id= ?", new String[]{logId
                                    + ""});
                }

                for (EventBean eBean : bean.getEventList()) {
                    eBean.setDailyLogId(logId);
                    eBean.setDriverId(bean.getDriverId());
                }
                eventBeenList.addAll(bean.getEventList());
                bean.set_id(logId);
            }


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogSave Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::AddDriver Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogDB.writeLogs(DailyLogDB.class.getName(), "::AddDriver Error:", e.getMessage(), Utility.printStackTrace(e));

                LogFile.write(DailyLogDB.class.getName() + "::DailyLogSave close database Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }

        if (eventBeenList.size() > 0) {
            EventDB.EventSave(eventBeenList);
        }

        return logId;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add or update dailylog in database
    public static Boolean DailyLogCertify(String sign, int driverId, String logIds) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("Signature", sign);
            values.put("CertifyFG", 1);
            values.put("StatusId", 1);
            values.put("SyncFg", 0);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            String[] arrayLog = logIds.split(",");

            for (String id : arrayLog) {

                database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                        " _id = ?", new String[]{id});
            }
            status = true;


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogCertify Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogCertify Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::DailyLogCertify close database Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogCertify close database Error Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }
        return status;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 29 November 2018
    // Purpose: get last duty status of onscreen user
    // Parameter : int : Driver ID
    // Parameter : int : Day : if value -1 then we get previous day differed day if 0 then we get current day differed day
    public static int getDeferedDay(int driverId, int day) {

        int deferDay = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            String lastDateTime = Utility.getPreviousDateOnly(day);

            cursor = database.rawQuery("select DeferedDay from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG + " where DriverId=? and LogDate=? order by LogDate desc LIMIT 1", new String[]{Integer.toString(driverId), lastDateTime});

            if (cursor.moveToNext()) {
                deferDay = cursor.getInt(cursor.getColumnIndex("DeferedDay"));
            }
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::getPreviosDeferedDay Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::getPreviosDeferedDay Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return deferDay;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 5 December 2018
    // Purpose: Event update for Defered Day.
    public static boolean LogUpdateForDeferedDay(String eventDateTime, int driverId, int deferedDay) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        boolean recordStatus = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            int id = 0;


            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG + " where DriverId=? and LogDate=?", new String[]{driverId + "", eventDateTime});

            while (cursor.moveToNext()) {
                id = cursor.getInt(0);

                ContentValues values = new ContentValues();
                values.put("ModifiedBy", driverId);
                values.put("ModifiedDate", Utility.getCurrentDateTime());
                values.put("DeferedDay", deferedDay);
                values.put("SyncFg", 0);

                if (id != 0) {
                    database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                            " _id= ?",
                            new String[]{id + ""});
                }
            }
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUpdateForDeferedDay Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::EventUpdateForDeferedDay Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
                cursor.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return recordStatus;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add or update dailylog in database
    public static Boolean CertifyCountUpdate(int id, int count) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("certifyCount", count);
            values.put("SyncFg", 0);

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                    " _id =?", new String[]{id
                            + ""});
            status = true;


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::CertifyCountUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::CertifyCountUpdate Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::CertifyCountUpdate close database Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::CertifyCountUpdate Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add or update dailylog in database
    public static Boolean DailyLogCertifyRevert(int driverId, int logId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("CertifyFG", 0);
            values.put("SyncFg", 0);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility._CurrentDateTime);


            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                    " _id = ?", new String[]{logId + ""});

            status = true;


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogCertifyRevert Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogCertifyRevert Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::DailyLogCertifyRevert close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogCertifyRevert Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: update hours in dailylog table
    public static Boolean DailyLogHoursReCertify(int driverId, int logId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("DrivingTimeRemaining", GPSData.DrivingTimeRemaining);
            values.put("WorkShiftTimeRemaining", GPSData.WorkShiftRemaining);
            values.put("TimeRemaining70", GPSData.TimeRemaining70);
            values.put("TimeRemaining120", GPSData.TimeRemaining120);
            values.put("TimeRemainingUS70", GPSData.TimeRemainingUS70);
            values.put("TimeRemainingReset", GPSData.TimeRemainingReset);
            values.put("CertifyFG", 0);
            values.put("SyncFg", 0);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());


            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                    " _id = ?", new String[]{logId + ""});

            status = true;


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogCertifyRevert Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogCertifyRevert Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::DailyLogCertifyRevert close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogCertifyRevert close DB Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: update hours in dailylog table
    public static Boolean DailyLogHoursReCertify(int driverId, String date) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("DrivingTimeRemaining", GPSData.DrivingTimeRemaining);
            values.put("WorkShiftTimeRemaining", GPSData.WorkShiftRemaining);
            values.put("TimeRemaining70", GPSData.TimeRemaining70);
            values.put("TimeRemaining120", GPSData.TimeRemaining120);
            values.put("TimeRemainingUS70", GPSData.TimeRemainingUS70);
            values.put("TimeRemainingReset", GPSData.TimeRemainingReset);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            values.put("SyncFg", 0);


            long id = database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                    " LogDate = ? and DriverId=? ", new String[]{date, driverId + ""});
            Log.i("TAG", id + "");
            status = true;


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogCertifyRevert Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogHoursReCertify Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::DailyLogCertifyRevert close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogHoursReCertify Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: update hours in dailylog table
    public static Boolean DailyLogDistanceSave(int logId, int driverId, int distance) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("EndOdometerReading", CanMessages.OdometerReading);
            values.put("TotalDistance", distance);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            values.put("SyncFg", 0);

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                    " _id = ?", new String[]{logId + ""});

            status = true;


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogDistanceSave Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogDistanceSave Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::DailyLogDistanceSave close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogDistanceSave Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: update hours in dailylog table
    public static Boolean DailyLogDistanceSave(int logId, int driverId, int distance, int endOdometerReading) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            //values.put("EndOdometerReading", CanMessages.OdometerReading);
            values.put("TotalDistance", distance);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            values.put("SyncFg", 0);

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                    " _id = ?", new String[]{logId + ""});

            status = true;


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogDistanceSave Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogDistanceSave Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::DailyLogDistanceSave  Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogDistanceSave Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add or update dailylog in database
    public static Boolean DailyLogSyncRevert(int driverId, int logId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("SyncFg", 0);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility._CurrentDateTime);

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                    " _id = ?", new String[]{logId + ""});

            status = true;

        } catch (Exception e) {
            Utility.printError(e.getMessage());

            LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogSyncRevert:", e.getMessage(), Utility.printStackTrace(e));

            LogFile.write(DailyLogDB.class.getName() + "::DailyLogSyncRevert Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::DailyLogSyncRevert close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogSyncRevert:", e.getMessage(), Utility.printStackTrace(e));

            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get log data to post
    public static JSONArray getLogDataSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id ,LogDate ,DriverId ,ShippingId ,TrailerId ,StartTime ,StartOdometerReading ,EndOdometerReading ,CertifyFG ,certifyCount " +
                            ",Signature ,CreatedBy ,CreatedDate ,ModifiedBy ,ModifiedDate,DrivingTimeRemaining ,WorkShiftTimeRemaining ,TimeRemaining70 ,TimeRemaining120 ,TimeRemainingUS70 ,TimeRemainingReset   from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where SyncFg=? order by 1 desc LIMIT 1"
                    , new String[]{"0"});
            if (cursor.moveToNext()) {

                JSONObject obj = new JSONObject();
                int logId = cursor.getInt(cursor.getColumnIndex("_id"));
                Utility.LogId = logId;

                obj.put("DAILYLOGID", logId);
                String logDate = cursor.getString(cursor.getColumnIndex("LogDate"));
                obj.put("LOGDATE", Utility.getDateForServer(logDate));
                obj.put("DRIVERID", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("SHIPPINGID", cursor.getString(cursor.getColumnIndex("ShippingId")));
                obj.put("TRAILERID", cursor.getString(cursor.getColumnIndex("TrailerId")));
                obj.put("STARTTIME", cursor.getString(cursor.getColumnIndex("StartTime")));
                obj.put("STARTODOMETERREADING", Double.parseDouble(cursor.getString(cursor.getColumnIndex("StartOdometerReading"))));
                obj.put("ENDODOMETERREADING", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EndOdometerReading"))));
                obj.put("CERTIFYFG", cursor.getInt(cursor.getColumnIndex("CertifyFG")));
                obj.put("CertifyCount", cursor.getInt(cursor.getColumnIndex("certifyCount")));
                String signature = cursor.getString(cursor.getColumnIndex("Signature"));
                obj.put("Signature", signature == null ? "" : signature);
                obj.put("COMPANYID", Utility.companyId);
                obj.put("LOGCREATEDBY", cursor.getInt(cursor.getColumnIndex("CreatedBy")));

                String createdDate = cursor.getString(cursor.getColumnIndex("CreatedDate"));
                createdDate = Utility.getDateTimeForServer(createdDate);
                if (createdDate.isEmpty()) {
                    createdDate = Utility.getDateTimeForServer(logDate + " 00:00:00");
                }
                obj.put("LOGCREATEDDATE", createdDate);
                obj.put("MODIFIEDBY", cursor.getInt(cursor.getColumnIndex("ModifiedBy")));
                String modifiedDate = cursor.getString(cursor.getColumnIndex("ModifiedDate"));
                modifiedDate = (modifiedDate == null || modifiedDate.isEmpty()) ? "1970-01-01 00:00:00" : modifiedDate;
                obj.put("MODIFIEDDATE", Utility.getDateTimeForServer(modifiedDate));
                obj.put("DrivingTimeRemaining", cursor.getInt(cursor.getColumnIndex("DrivingTimeRemaining")));
                obj.put("WorkShiftRemaining", cursor.getInt(cursor.getColumnIndex("WorkShiftTimeRemaining")));
                obj.put("TimeRemaining70", cursor.getInt(cursor.getColumnIndex("TimeRemaining70")));
                obj.put("TimeRemaining120", cursor.getInt(cursor.getColumnIndex("TimeRemaining120")));
                obj.put("TimeRemainingUS70", cursor.getInt(cursor.getColumnIndex("TimeRemainingUS70")));
                obj.put("TimeRemainingReset", cursor.getInt(cursor.getColumnIndex("TimeRemainingReset")));

                obj.put("EventList", EventDB.GetEventSync(logId));
                obj.put("RuleList", getDailyLogRuleSync(logId));
                array.put(obj);

            }
        } catch (Exception exe) {
            Utility.LogId = 0;
            LogFile.write(DailyLogDB.class.getName() + "::getLogDataSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getLogDataSync Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {
                LogFile.write(DailyLogDB.class.getName() + "::getLogDataSync close DB Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::getLogDataSync  close DB Error:", exe.getMessage(), Utility.printStackTrace(exe));

            }
        }
        return array;
    }

    // Created By: Deepak Sharma
    // Created Date: 29 January 2015
    // Purpose: update SyncFg=0 for all table whose data is posted from Post/All service
    public static void UpdateSyncStatusAll() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                    " SyncFg= ? and _id=?", new String[]{"0", Utility.LogId + ""});

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " SyncFg= ? and DailyLogId=?", new String[]{"0", Utility.LogId + ""});

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_RULE,
                    values, " SyncFg= ? and (DailyLogId=? or  DailyLogId=0)", new String[]{"0", Utility.LogId + ""});

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER,
                    values, " SyncFg= ? ", new String[]{"0"});
            Utility.LogId = 0;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::UpdateSyncStatusAll Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::UpdateSyncStatusAll Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::UpdateSyncStatusAll close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::UpdateSyncStatusAll Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }
    }

    // Created By: Simran
    // Created Date: 6 march 2020
    // Purpose: update SyncFg=0 for dailylog table whose data is posted from Post/All service
    public static void UpdateDailyLogStatus(int dailylogId, int onlineDailyLogId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            values.put("OnlineDailyLogId", onlineDailyLogId);

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                    " SyncFg= ? and _id=?", new String[]{"0", dailylogId + ""});

            Utility.LogId = 0;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::UpdateSyncStatusAll Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::UpdateSyncStatusAll Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::UpdateSyncStatusAll close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::UpdateSyncStatusAll Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }
    }

    // Created By: Simran
    // Created Date: 6 march 2020
    // Purpose: update SyncFg=0 for evebt table whose data is posted from Post/All service
    public static void UpdateEventStatus(int eventId, int onlineEventId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            values.put("OnlineEventId", onlineEventId);

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " SyncFg= ? and  _id=?", new String[]{"0",  eventId + ""});

            Utility.LogId = 0;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::UpdateSyncStatusAll Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::UpdateSyncStatusAll Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::UpdateSyncStatusAll close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::UpdateSyncStatusAll Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }
    }

    // Created By: Simran
    // Created Date: 6 march 2020
    // Purpose: update SyncFg=0 for rule table whose data is posted from Post/All service
    public static void UpdateRuleStatus(int ruleId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_RULE,
                    values, " SyncFg= ? and _Id=?", new String[]{"0", ruleId + ""});

            Utility.LogId = 0;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::UpdateSyncStatusAll Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::UpdateSyncStatusAll Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::UpdateSyncStatusAll close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::UpdateSyncStatusAll Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }
    }

    public static void certifyLogBook(int driverId, String logIds) {
        try {

            if (DailyLogDB.DailyLogCertify("", driverId, logIds)) {
                String[] logs = logIds.split(",");
                // need to discuss about this should we enter multiple event related to multiple certification
                for (int i = 0; i < logs.length; i++) {
                    int logId = Integer.parseInt(logs[i]);
                    int n = DailyLogDB.getCertifyCount(logId) + 1;
                    DailyLogDB.CertifyCountUpdate(logId, n);
                    if (n > 9)
                        n = 9;
                    // to be discuss about event
                    //123 LogFile.write(DailyLogDB.class.getName() + "::certifyLogBook: " + "Driver's " + n + "'th certification of a daily record" + " of driverId:" + driverId, LogFile.DIAGNOSTIC_MALFUNCTION, LogFile.DRIVEREVENT_LOG);
                    EventDB.EventCreate(Utility.getCurrentDateTime(), 4, n, "Driver's " + n + "'th certification of a daily record", 1, 1, logId, driverId, "", MainActivity.currentRule);
                }
            }
        } catch (Exception e) {
            LogFile.write(DailyLogDB.class.getName() + "::certifyLogBook Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::certifyLogBook Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add or update dailylog in database
    public static Boolean TrailerUpdate(int driverId, String trailerNo) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            String logDate = Utility.getCurrentDate();
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("TrailerId", trailerNo);
            values.put("SyncFg", 0);

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                    " DriverId =? and LogDate=?", new String[]{driverId
                            + "", logDate});
            status = true;


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::CertifyCountUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::CertifyCountUpdate Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::CertifyCountUpdate close database Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::CertifyCountUpdate close database Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 2 March 2017
    // Purpose: get log data to post
    public static JSONArray DailyLogGetSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id ,LogDate ,DriverId ,ShippingId ,TrailerId ,StartTime ,StartOdometerReading ,EndOdometerReading ,CertifyFG ,certifyCount " +
                            ",Signature ,CreatedBy ,CreatedDate ,ModifiedBy ,ModifiedDate,DrivingTimeRemaining ,WorkShiftTimeRemaining ,TimeRemaining70 ,TimeRemaining120 ,TimeRemainingUS70 ,TimeRemainingReset ,DeferedDay  from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where SyncFg=? order by 2 desc LIMIT 1"
                    , new String[]{"0"});
            if (cursor.moveToNext()) {

                JSONObject obj = new JSONObject();
                int logId = cursor.getInt(cursor.getColumnIndex("_id"));
                Utility.LogId = logId;

                obj.put("DAILYLOGID", logId);
                obj.put("_ID", logId);
                String logDate = cursor.getString(cursor.getColumnIndex("LogDate")) + " 00:00:00";
                obj.put("LOGDATE", logDate);
                obj.put("DRIVERID", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("SHIPPINGID", cursor.getString(cursor.getColumnIndex("ShippingId")));
                obj.put("TRAILERID", cursor.getString(cursor.getColumnIndex("TrailerId")));
                obj.put("STARTTIME", cursor.getString(cursor.getColumnIndex("StartTime")));
                obj.put("STARTODOMETERREADING", Double.parseDouble(cursor.getString(cursor.getColumnIndex("StartOdometerReading"))));
                obj.put("ENDODOMETERREADING", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EndOdometerReading"))));
                obj.put("CERTIFYFG", cursor.getInt(cursor.getColumnIndex("CertifyFG")));
                obj.put("CertifyCount", cursor.getInt(cursor.getColumnIndex("certifyCount")));
                String signature = cursor.getString(cursor.getColumnIndex("Signature"));
                obj.put("Signature", signature == null ? "" : signature);
                obj.put("COMPANYID", Utility.companyId);
                obj.put("VEHICLEID", Utility.vehicleId);
                obj.put("TIMEZONEOFFSETUTC", Utility.TimeZoneOffsetUTC);
                obj.put("LOGCREATEDBY", cursor.getInt(cursor.getColumnIndex("CreatedBy")));

                String createdDate = cursor.getString(cursor.getColumnIndex("CreatedDate"));

                if (createdDate.isEmpty()) {
                    createdDate = logDate + " 00:00:00";
                }
                obj.put("LOGCREATEDDATE", createdDate);
                obj.put("MODIFIEDBY", cursor.getInt(cursor.getColumnIndex("ModifiedBy")));
                String modifiedDate = cursor.getString(cursor.getColumnIndex("ModifiedDate"));
                modifiedDate = (modifiedDate == null || modifiedDate.isEmpty()) ? "1970-01-01 00:00:00" : modifiedDate;
                obj.put("MODIFIEDDATE", modifiedDate);
                obj.put("DrivingTimeRemaining", cursor.getInt(cursor.getColumnIndex("DrivingTimeRemaining")));
                obj.put("WorkShiftRemaining", cursor.getInt(cursor.getColumnIndex("WorkShiftTimeRemaining")));
                obj.put("TimeRemaining70", cursor.getInt(cursor.getColumnIndex("TimeRemaining70")));
                obj.put("TimeRemaining120", cursor.getInt(cursor.getColumnIndex("TimeRemaining120")));
                obj.put("TimeRemainingUS70", cursor.getInt(cursor.getColumnIndex("TimeRemainingUS70")));
                obj.put("TimeRemainingReset", cursor.getInt(cursor.getColumnIndex("TimeRemainingReset")));
                // get Defered Value from Database
                obj.put("DeferedDay", cursor.getInt(cursor.getColumnIndex("DeferedDay")));
                obj.put("EventList", EventDB.DailyLogEventGetSync(logId));
                obj.put("RuleList", DailyLogRuleGetSync(logId));
                array.put(obj);

            }
        } catch (Exception exe) {
            Utility.LogId = 0;
            LogFile.write(DailyLogDB.class.getName() + "::getLogDataSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getLogDataSync Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {
                LogFile.write(DailyLogDB.class.getName() + "::getLogDataSync close DB Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::getLogDataSync close DB Error:", exe.getMessage(), Utility.printStackTrace(exe));

            }
        }
        return array;
    }

    // Created By: Deepak Sharma
    // Created Date: 2 March 2017
    // Purpose: get log data to post
    public static JSONArray DailyLogGetSync(int dailyLogId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id ,LogDate ,DriverId ,ShippingId ,TrailerId ,StartTime ," +
                            "StartOdometerReading ,EndOdometerReading ,CertifyFG ,certifyCount " +
                            ",Signature ,CreatedBy ,CreatedDate ,ModifiedBy ,ModifiedDate,DrivingTimeRemaining ," +
                            "WorkShiftTimeRemaining ,TimeRemaining70 ,TimeRemaining120 ,TimeRemainingUS70 ,TimeRemainingReset ,DeferedDay  from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where SyncFg=? and _id=? order by 2 desc LIMIT 1"
                    , new String[]{"0", dailyLogId + ""});
            if (cursor.moveToNext()) {

                JSONObject obj = new JSONObject();
                int logId = cursor.getInt(cursor.getColumnIndex("_id"));
                Utility.LogId = logId;

                obj.put("DAILYLOGID", logId);
                String logDate = cursor.getString(cursor.getColumnIndex("LogDate")) + " 00:00:00";
                obj.put("LOGDATE", logDate);
                obj.put("DRIVERID", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("SHIPPINGID", cursor.getString(cursor.getColumnIndex("ShippingId")));
                obj.put("TRAILERID", cursor.getString(cursor.getColumnIndex("TrailerId")));
                obj.put("STARTTIME", cursor.getString(cursor.getColumnIndex("StartTime")));
                obj.put("STARTODOMETERREADING", Double.parseDouble(cursor.getString(cursor.getColumnIndex("StartOdometerReading"))));
                obj.put("ENDODOMETERREADING", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EndOdometerReading"))));
                obj.put("CERTIFYFG", cursor.getInt(cursor.getColumnIndex("CertifyFG")));
                obj.put("CertifyCount", cursor.getInt(cursor.getColumnIndex("certifyCount")));
                String signature = cursor.getString(cursor.getColumnIndex("Signature"));
                obj.put("Signature", signature == null ? "" : signature);
                obj.put("COMPANYID", Utility.companyId);
                obj.put("VEHICLEID", Utility.vehicleId);
                obj.put("TIMEZONEOFFSETUTC", Utility.TimeZoneOffsetUTC);
                obj.put("LOGCREATEDBY", cursor.getInt(cursor.getColumnIndex("CreatedBy")));

                String createdDate = cursor.getString(cursor.getColumnIndex("CreatedDate"));

                if (createdDate.isEmpty()) {
                    createdDate = logDate + " 00:00:00";
                }
                obj.put("LOGCREATEDDATE", createdDate);
                obj.put("MODIFIEDBY", cursor.getInt(cursor.getColumnIndex("ModifiedBy")));
                String modifiedDate = cursor.getString(cursor.getColumnIndex("ModifiedDate"));
                modifiedDate = (modifiedDate == null || modifiedDate.isEmpty()) ? "1970-01-01 00:00:00" : modifiedDate;
                obj.put("MODIFIEDDATE", modifiedDate);
                obj.put("DrivingTimeRemaining", cursor.getInt(cursor.getColumnIndex("DrivingTimeRemaining")));
                obj.put("WorkShiftRemaining", cursor.getInt(cursor.getColumnIndex("WorkShiftTimeRemaining")));
                obj.put("TimeRemaining70", cursor.getInt(cursor.getColumnIndex("TimeRemaining70")));
                obj.put("TimeRemaining120", cursor.getInt(cursor.getColumnIndex("TimeRemaining120")));
                obj.put("TimeRemainingUS70", cursor.getInt(cursor.getColumnIndex("TimeRemainingUS70")));
                obj.put("TimeRemainingReset", cursor.getInt(cursor.getColumnIndex("TimeRemainingReset")));
                // get Defered Value from Database
                obj.put("DeferedDay", cursor.getInt(cursor.getColumnIndex("DeferedDay")));
                obj.put("EventList", EventDB.DailyLogEventGetSync(logId));
                obj.put("RuleList", DailyLogRuleGetSync(logId));
                array.put(obj);

            }
        } catch (Exception exe) {
            Utility.LogId = 0;
            LogFile.write(DailyLogDB.class.getName() + "::getLogDataSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getLogDataSync Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {
                LogFile.write(DailyLogDB.class.getName() + "::getLogDataSync close DB Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::getLogDataSync close DB Error:", exe.getMessage(), Utility.printStackTrace(exe));

            }
        }
        return array;
    }

    // Created By: Deepak Sharma
    // Created Date: 22 March 2017
    // Purpose: get rules for web sync
    public static JSONArray DailyLogRuleGetSync(int logId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select DailyLogId ,RuleId ,RuleStartTime ,RuelEndTime, _Id from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE +
                            " where SyncFg=0 and DailyLogId=?"
                    , new String[]{logId + ""});
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("LogId", cursor.getInt(0));
                obj.put("RuleId", cursor.getInt(1));
                obj.put("RuleStartTime", cursor.getString(2));
                obj.put("RuleEndTime", (cursor.getString(3) == null ? "1970-01-01 00:00:00" : cursor.getString(3)));
                obj.put("_ID", cursor.getInt(4));
                array.put(obj);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getDailyLogRuleSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getDailyLogRuleSync Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return array;
    }

    // Created By: Deepak Sharma
    // Created Date: 2 March 2017
    // Purpose: get codriver for web sync
    public static JSONArray CoDriverGetSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select DriverId ,DriverId2 ,LoginTime ,LogoutTime  from " + MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER +
                            " where SyncFg=0"
                    , null);
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("DriverId", cursor.getInt(0));
                obj.put("DriverId2", cursor.getInt(1));
                obj.put("LoginTime", (cursor.getString(2)));
                obj.put("LogoutTime", (cursor.getString(3) == null ? "1970-01-01 00:00:00" : cursor.getString(3)));
                array.put(obj);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getCoDriverSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getCoDriverSync Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return array;
    }

    public static void updateRuleDriverId(SQLiteDatabase database) {

        String sql = "select _id, DailyLogId  from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE +
                "  order by RuleStartTime desc ";

        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, null);
            while (cursor.moveToNext()) {

                int _id = cursor.getInt(0);
                int logId = cursor.getInt(1);
                int driverId = 0;

                Cursor cursor1 = database.rawQuery("select driverId  from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                                " where _id=? "
                        , new String[]{logId + ""});
                if (cursor1.moveToNext()) {
                    driverId = cursor.getInt(0);
                }
                cursor1.close();

                ContentValues values = new ContentValues();
                values.put("DriverId", driverId);
                database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_RULE, values,
                        " _id= ?", new String[]{_id
                                + ""});
            }


        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::updateRuleDriverId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::updateRuleDriverId Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
            } catch (Exception e) {

            }
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get daily log data
    public static ArrayList<VehicleBean> getVehicleByLogId(int logId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<VehicleBean> list = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select distinct c.VehicleId, c.UnitNo, c.PlateNo,c.VIN  from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                            " e inner join " + MySQLiteOpenHelper.TABLE_CARRIER + " c on e.VehicleId=c.VehicleId where DailyLogId=? "
                    , new String[]{logId + ""});
            while (cursor.moveToNext()) {
                VehicleBean bean = new VehicleBean();
                bean.setVehicleId(cursor.getInt(cursor.getColumnIndex("VehicleId")));
                bean.setUnitNo(cursor.getString(cursor.getColumnIndex("UnitNo")));
                bean.setPlateNo(cursor.getString(cursor.getColumnIndex("PlateNo")));
                bean.setVIN(cursor.getString(cursor.getColumnIndex("VIN")));
                list.add(bean);
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getDriverByLogId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getVehicleByLogId Error:", exe.getMessage(), Utility.printStackTrace(exe));

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

    // Created By: Pallavi Wattamwar
    // Created Date: 14 January 2019
    // Purpose: get Daily Log Data by Date
    public static JSONArray DailyLogGetByDateSync(String dailylogDate, int driverID) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id ,LogDate ,DriverId ,ShippingId ,TrailerId ,StartTime ,StartOdometerReading ,EndOdometerReading ,CertifyFG ,certifyCount " +
                            ",Signature ,CreatedBy ,CreatedDate ,ModifiedBy ,ModifiedDate,DrivingTimeRemaining ,WorkShiftTimeRemaining ,TimeRemaining70 ,TimeRemaining120 ,TimeRemainingUS70 ,TimeRemainingReset ,DeferedDay  from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where LogDate=? and DriverId=? order by 1 desc LIMIT 1"
                    , new String[]{dailylogDate, "" + driverID});
            if (cursor.moveToNext()) {

                JSONObject obj = new JSONObject();
                int logId = cursor.getInt(cursor.getColumnIndex("_id"));
                Utility.LogId = logId;

                obj.put("DAILYLOGID", logId);
                String logDate = cursor.getString(cursor.getColumnIndex("LogDate")) + " 00:00:00";
                obj.put("LOGDATE", logDate);
                obj.put("DRIVERID", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("SHIPPINGID", cursor.getString(cursor.getColumnIndex("ShippingId")));
                obj.put("TRAILERID", cursor.getString(cursor.getColumnIndex("TrailerId")));
                obj.put("STARTTIME", cursor.getString(cursor.getColumnIndex("StartTime")));
                obj.put("STARTODOMETERREADING", Double.parseDouble(cursor.getString(cursor.getColumnIndex("StartOdometerReading"))));
                obj.put("ENDODOMETERREADING", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EndOdometerReading"))));
                obj.put("CERTIFYFG", cursor.getInt(cursor.getColumnIndex("CertifyFG")));
                obj.put("CertifyCount", cursor.getInt(cursor.getColumnIndex("certifyCount")));
                String signature = cursor.getString(cursor.getColumnIndex("Signature"));
                obj.put("Signature", signature == null ? "" : signature);
                obj.put("COMPANYID", Utility.companyId);
                obj.put("VEHICLEID", Utility.vehicleId);
                obj.put("TIMEZONEOFFSETUTC", Utility.TimeZoneOffsetUTC);
                obj.put("LOGCREATEDBY", cursor.getInt(cursor.getColumnIndex("CreatedBy")));

                String createdDate = cursor.getString(cursor.getColumnIndex("CreatedDate"));

                if (createdDate.isEmpty()) {
                    createdDate = logDate + " 00:00:00";
                }
                obj.put("LOGCREATEDDATE", createdDate);
                obj.put("MODIFIEDBY", cursor.getInt(cursor.getColumnIndex("ModifiedBy")));
                String modifiedDate = cursor.getString(cursor.getColumnIndex("ModifiedDate"));
                modifiedDate = (modifiedDate == null || modifiedDate.isEmpty()) ? "1970-01-01 00:00:00" : modifiedDate;
                obj.put("MODIFIEDDATE", modifiedDate);
                obj.put("DrivingTimeRemaining", cursor.getInt(cursor.getColumnIndex("DrivingTimeRemaining")));
                obj.put("WorkShiftRemaining", cursor.getInt(cursor.getColumnIndex("WorkShiftTimeRemaining")));
                obj.put("TimeRemaining70", cursor.getInt(cursor.getColumnIndex("TimeRemaining70")));
                obj.put("TimeRemaining120", cursor.getInt(cursor.getColumnIndex("TimeRemaining120")));
                obj.put("TimeRemainingUS70", cursor.getInt(cursor.getColumnIndex("TimeRemainingUS70")));
                obj.put("TimeRemainingReset", cursor.getInt(cursor.getColumnIndex("TimeRemainingReset")));
                // get Defered Value from Database
                obj.put("DeferedDay", cursor.getInt(cursor.getColumnIndex("DeferedDay")));
                obj.put("EventList", EventDB.DailyLogEventByDate(logId));
                obj.put("RuleList", DailyLogRuleGetByDate(logId));
                array.put(obj);

            }
        } catch (Exception exe) {
            Utility.LogId = 0;
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogGetByDateSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogGetByDateSync Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {
                LogFile.write(DailyLogDB.class.getName() + "::DailyLogGetByDateSync close DB Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(DailyLogDB.class.getName(), "::DailyLogGetByDateSync close DB Error:", exe.getMessage(), Utility.printStackTrace(exe));

            }
        }
        return array;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 14 January 2018
    // Purpose: get rules for web sync
    public static JSONArray DailyLogRuleGetByDate(int logId) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select DailyLogId ,RuleId ,RuleStartTime ,RuelEndTime from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE +
                            " where  DailyLogId=?"
                    , new String[]{logId + ""});
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("LogId", cursor.getInt(0));
                obj.put("RuleId", cursor.getInt(1));
                obj.put("RuleStartTime", Utility.getDateTimeForServer(cursor.getString(2)));
                obj.put("RuleEndTime", Utility.getDateTimeForServer(cursor.getString(3) == null ? "1970-01-01 00:00:00" : cursor.getString(3)));
                array.put(obj);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getDailyLogRuleSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDB.class.getName(), "::getDailyLogRuleSync Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return array;
    }

    //created By: Simran
    // created Date: 28 Feb 2020
    // purpose: edit rule
    public static boolean editRule(int ruleId, String ruleStartTime, int dailyLogId){
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean recordStatus = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("RuleStartTime", ruleStartTime);
            values.put("RuleId", ruleId);
            values.put("SyncFg", 0);

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_RULE, values, " _Id= ?",
                    new String[]{dailyLogId + ""});
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::RuleUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(EventDB.class.getName(), "::RuleUpdate Error:", e.getMessage(), Utility.printStackTrace(e));

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
}
