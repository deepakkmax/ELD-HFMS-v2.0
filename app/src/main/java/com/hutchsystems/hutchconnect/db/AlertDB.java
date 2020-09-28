package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.beans.AlertBean;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Deepak on 11/30/2016.
 */

public class AlertDB {
    public static IScoreCard mListener;

    public interface IScoreCard {
        void onUpdate(String code);
    }

    public static boolean getDuplicate(int driverId, String code, String date) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select AlertDateTime from "
                            + MySQLiteOpenHelper.TABLE_ALERT + " Where AlertCode=? and DriverId=? and AlertDateTime>=? order by 1 desc Limit 1 "
                    , new String[]{code, driverId + "", date});

            if (cursor.moveToNext()) {
                status = true;
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(AlertDB.class.getName() + "::getAlertSync Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(AlertDB.class.getName(),"::getDuplicate Error:" ,e.getMessage(),Utility.printStackTrace(e));
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 12 December 2016
    // Purpose: update Alert for web sync
    public static void AlertSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_ALERT, values,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(AlertDB.class.getName() + "::AlertSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(AlertDB.class.getName(),"::AlertSyncUpdate Error:" ,exe.getMessage(),Utility.printStackTrace(exe));
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
    // Created Date: 12 December 2016
    // Purpose: get Alert for web sync
    public static JSONArray getAlertSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id,AlertCode,AlertName,AlertDateTime,Duration,Scores ,DriverId ,VehicleId,Latitude ,Longitude ,Location ,Threshold  ,SyncFg from "
                            + MySQLiteOpenHelper.TABLE_ALERT + " Where SyncFg=0"
                    , null);

            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("AlertDateTime", cursor.getString(cursor.getColumnIndex("AlertDateTime")));
                obj.put("AlertCode", cursor.getString(cursor.getColumnIndex("AlertCode")));
                obj.put("AlertName", cursor.getString(cursor.getColumnIndex("AlertName")));
                obj.put("Duration", cursor.getInt(cursor.getColumnIndex("Duration")));
                obj.put("Scores", cursor.getInt(cursor.getColumnIndex("Scores")));
                obj.put("VehicleId", cursor.getInt(cursor.getColumnIndex("VehicleId")));
                obj.put("DriverId", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("Latitude", cursor.getString(cursor.getColumnIndex("Latitude")));
                obj.put("Longitude", cursor.getString(cursor.getColumnIndex("Longitude")));
                obj.put("Location", cursor.getString(cursor.getColumnIndex("Location")));
                obj.put("Threshold", cursor.getString(cursor.getColumnIndex("Threshold")));
                obj.put("CompanyId", Utility.companyId);
                array.put(obj);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(AlertDB.class.getName() + "::getAlertSync Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(AlertDB.class.getName(),"::getAlertSync Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }
        return array;
    }

    public static boolean Save(String code, String name, String date, int score, int duration, int driverId, String threshold) {
        AlertBean bean = new AlertBean();
        bean.setAlertCode(code);
        bean.setAlertName(name);
        bean.setAlertDateTime(date);
        bean.setScores(score);
        bean.setDuration(duration);
        bean.setDriverId(driverId);
        bean.setThreshold(threshold);
        return AlertDB.Save(bean);
    }

    private static boolean Save(AlertBean bean) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("AlertCode", bean.getAlertCode());
            values.put("AlertName", bean.getAlertName());
            values.put("AlertDateTime", bean.getAlertDateTime());
            values.put("Duration", bean.getDuration());
            values.put("Scores", bean.getScores());
            values.put("DriverId", bean.getDriverId());
            values.put("VehicleId", Utility.vehicleId);
            values.put("Latitude", Utility.currentLocation.getLatitude());
            values.put("Longitude", Utility.currentLocation.getLongitude());
            values.put("Location", Utility.currentLocation.getLocationDescription());
            values.put("Threshold", bean.getThreshold());
            values.put("SyncFg", 0);
            database.insert(MySQLiteOpenHelper.TABLE_ALERT,
                    "_id", values);
          //Post Alert
            MainActivity.postData(CommonTask.Post_Alert);

            if (mListener != null) {
                mListener.onUpdate(bean.getAlertCode());
            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(AlertDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(AlertDB.class.getName(),"::Save Error:" ,e.getMessage(),Utility.printStackTrace(e));
        } finally {
            database.close();
            helper.close();
        }
        return status;

    }

    public static boolean Update(String code, int duration, int score) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select _id from "
                    + MySQLiteOpenHelper.TABLE_ALERT
                    + " where AlertCode=? order by _id desc LIMIT 1", new String[]{code});

            if (cursor.moveToFirst()) {

                ContentValues values = new ContentValues();

                values.put("Duration", duration);
                values.put("Scores", score);
                values.put("SyncFg", 0);

                int id = cursor.getInt(0);
                database.update(MySQLiteOpenHelper.TABLE_ALERT, values,
                        " _id= ?",
                        new String[]{id + ""});
                if (mListener != null) {
                    mListener.onUpdate(code);
                }
            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(AlertDB.class.getName() + "::Update Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(AlertDB.class.getName(),"::Update Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            cursor.close();
            database.close();
            helper.close();
        }
        return status;

    }

    // Created By: Deepak Sharma
    // Created Date: 12 December 2016
    // Purpose: get driver Score card
    public static ArrayList<AlertBean> getScoreCard(int driverId, String date) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        ArrayList<AlertBean> alertList = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select AlertCode,AlertName,max(AlertDateTime) LastOccurrenceDate,sum(Duration) as Duration,sum(Scores) as Score,count(AlertCode) as TotalCount from "
                            + MySQLiteOpenHelper.TABLE_ALERT + " Where DriverId=? and AlertDateTime>=? group by AlertCode,AlertName order by max(AlertDateTime) desc "
                    , new String[]{driverId + "", date});

            while (cursor.moveToNext()) {
                AlertBean obj = new AlertBean();
                obj.setAlertCode(cursor.getString(cursor.getColumnIndex("AlertCode")));
                obj.setAlertName(cursor.getString(cursor.getColumnIndex("AlertName")));
                obj.setAlertDateTime(cursor.getString(cursor.getColumnIndex("LastOccurrenceDate")));
                obj.setDuration(cursor.getInt(cursor.getColumnIndex("Duration")));
                obj.setScores(cursor.getInt(cursor.getColumnIndex("Score")));
                obj.setCount(cursor.getInt(cursor.getColumnIndex("TotalCount")));
                alertList.add(obj);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(AlertDB.class.getName() + "::getAlertSync Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(AlertDB.class.getName(),"::getScoreCard Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }
        return alertList;
    }


}
