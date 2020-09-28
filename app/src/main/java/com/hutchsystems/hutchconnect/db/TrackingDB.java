package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.beans.GpsSignalBean;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by Vaneet.Sethi on 4/6/2016.
 */
public class TrackingDB {

    public static void addGpsSignal(String signal) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("GPSDate", Utility.getCurrentDateTime());
            values.put("signal", signal);
            values.put("SyncFg", 0);
            database.insert(MySQLiteOpenHelper.TABLE_GpsLocation, "_id", values);

        } catch (Exception e) {
            e.printStackTrace();
            LogFile.write(TrackingDB.class.getName() + "::addGpsSignal Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TrackingDB.class.getName(),"::addGpsSignal Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
    }

    public static long removeGpsSignal(String id) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        long res = -1;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String[] ids = id.split(",");
            for (String _id : ids) {
                res = database.delete(MySQLiteOpenHelper.TABLE_GpsLocation,
                        "_id=?", new String[]{_id});
            }

        } catch (Exception e) {
            System.out.println("removeGpsSignal");
            e.printStackTrace();
            LogFile.write(TrackingDB.class.getName() + "::removeGpsSignal Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TrackingDB.class.getName(),"::removeGpsSignal Error:" ,e.getMessage(),Utility.printStackTrace(e));
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return res;
    }

    // Created By: Deepak Sharma
    // Created Date: 28 March 2017
    // Purpose: update Incident for web sync
    public static JSONArray GPSSyncUpdate(String data) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String[] ids = data.split(",");
            for (String id : ids) {
                ContentValues values = new ContentValues();
                values.put("SyncFg", 1);
                database.update(MySQLiteOpenHelper.TABLE_GpsLocation, values,
                        " SyncFg=? and _Id=?", new String[]{"0", id});
            }

        } catch (Exception exe) {

            LogFile.write(TrackingDB.class.getName() + "::GPSSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TrackingDB.class.getName(),"::GPSSyncUpdate Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return array;
    }

    public static ArrayList<GpsSignalBean> getGpsSignalList() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<GpsSignalBean> lstSignal = new ArrayList<GpsSignalBean>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,signal from "
                            + MySQLiteOpenHelper.TABLE_GpsLocation + " where SyncFg=0 order by _id LIMIT 30"
                    , null);

            while (cursor.moveToNext()) {
                GpsSignalBean objBean = new GpsSignalBean();
                objBean.set_id(cursor.getInt(0));
                objBean.set_gpsSignal(cursor.getString(1));
                lstSignal.add(objBean);
            }

        } catch (Exception e) {
            LogFile.write(TrackingDB.class.getName() + "::getGpsSignalList Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TrackingDB.class.getName(),"::getGpsSignalList Error:" ,e.getMessage(),Utility.printStackTrace(e));


        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return lstSignal;
    }


    public static int getPendingLocationCount() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int count = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id from "
                            + MySQLiteOpenHelper.TABLE_GpsLocation + " where SyncFg=0"
                    , null);
            count = cursor.getCount();

        } catch (Exception e) {
            LogFile.write(TrackingDB.class.getName() + "::getGpsSignalList Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TrackingDB.class.getName(),"::getPendingLocationCount Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return count;
    }
}
