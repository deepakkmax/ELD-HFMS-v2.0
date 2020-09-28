package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.beans.ViolationBean;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.CustomDateFormat;
import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by SAMSUNG on 16-03-2017.
 */

public class ViolationDB {

    // Created By: Deepak Sharma
    // Created Date: 16 March 2017
    // Purpose: get Violation for web sync
    public static ArrayList<ViolationBean> getViolation(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<ViolationBean> list = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select Rule,ViolationDate,TotalMinute,DriverId,SyncFg from "
                            + MySQLiteOpenHelper.TABLE_VIOLATION_DETAIL + " where DriverId=? order by ViolationDate desc"
                    , new String[]{driverId + ""});
            while (cursor.moveToNext()) {
                ViolationBean obj = new ViolationBean();
                obj.setRule(cursor.getString(cursor.getColumnIndex("Rule")));
                obj.setStartTime(Utility.parse(cursor.getString(cursor.getColumnIndex("ViolationDate"))));
                obj.setTotalMinutes(cursor.getInt(cursor.getColumnIndex("TotalMinute")));
                if (!obj.getRule().equals("-999"))
                    list.add(obj);
            }

        } catch (Exception e) {

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }
        return list;
    }

    // Created By: Deepak Sharma
    // Created Date: 16 March 2017
    // Purpose: get Violation for web sync
    public static JSONArray ViolationSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_VIOLATION_DETAIL, values,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
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

    // Created By: Deepak Sharma
    // Created Date: 16 March 2017
    // Purpose: get Violation for web sync
    public static JSONArray getViolationSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select Rule,ViolationDate,TotalMinute,DriverId,SyncFg from "
                            + MySQLiteOpenHelper.TABLE_VIOLATION_DETAIL + " Where SyncFg=0"
                    , null);
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("Rule", cursor.getString(cursor.getColumnIndex("Rule")));
                obj.put("ViolationDate", cursor.getString(cursor.getColumnIndex("ViolationDate")));
                obj.put("TotalMinute", cursor.getInt(cursor.getColumnIndex("TotalMinute")));
                obj.put("DriverId", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("VehicleId", Utility.vehicleId);
                obj.put("CompanyId", Utility.companyId);
                array.put(obj);
            }

        } catch (Exception e) {

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

    public static boolean Delete(String date, int driverId) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            database.delete(MySQLiteOpenHelper.TABLE_VIOLATION_DETAIL,
                    "driverId=? and ViolationDate>=?", new String[]{driverId + "", date});

        } catch (Exception e) {

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e1) {

            }
        }
        return status;

    }

    public static boolean Save(ArrayList<ViolationBean> list, int driverId) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            for (ViolationBean bean : list) {
                String violationDate = Utility.format(bean.getStartTime(), CustomDateFormat.dt1);
                Date date = Utility.dateOnlyGet(violationDate);
                Date cDate = Utility.newDateOnly();
                if (date.equals(cDate) || date.after(cDate)) {
                    continue;
                }

                Cursor cursor = database.rawQuery("select _id from " +
                        MySQLiteOpenHelper.TABLE_VIOLATION_DETAIL +
                        " where DriverId=? and ViolationDate=? and Rule=? ", new String[]{driverId + "", violationDate, bean.getRule()});

                if (!cursor.moveToNext()) {
                    ContentValues values = new ContentValues();
                    values.put("Rule", bean.getRule());
                    values.put("ViolationDate", violationDate);
                    values.put("TotalMinute", bean.getTotalMinutes());
                    values.put("DriverId", driverId);
                    values.put("SyncFg", 0);
                    database.insert(MySQLiteOpenHelper.TABLE_VIOLATION_DETAIL,
                            "_id", values);

                }
                cursor.close();

                MainActivity.postData(CommonTask.Post_Violation);
            }
        } catch (Exception e) {
            Utility.printError(e.getMessage());
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e1) {

            }
        }
        return status;
    }
}
