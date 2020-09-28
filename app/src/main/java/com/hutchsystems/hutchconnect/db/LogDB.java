package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

public class LogDB {

    // Created By: Pallavi Wattamwar
    // Created Date: 24/10/2018
    // Purpose: add exception logs in log file
    // Parameter : String : className :  Name of the Exception Class
    // Parameter : String : methodName : Name of the Exception method
    // Parameter : String : exceptionMessage : Title of the Exception
    //Parameter : String : exceptionStackTrace : StackTrace of the Exception

    public static void writeLogs(String className, String methodName, String exceptionMessage, String exceptionStackTrace) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursur = null;
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursur = database.rawQuery("select _id from " + MySQLiteOpenHelper.TABLE_LOG_DETAIL +
                            " where Class=? and Method=? and Title=? and  ExceptionDate Like ?"
                    , new String[]{className, methodName, exceptionMessage,Utility.getCurrentDate()+'%'});
            if (cursur.moveToNext()) {
                id = cursur.getInt(0);
            }

            cursur.close();
            ContentValues values = new ContentValues();
            values.put("ExceptionDate", Utility.getCurrentDateTime());
            values.put("Title", exceptionMessage);
            values.put("StackTrace", exceptionStackTrace);
            values.put("Class", className);
            values.put("Method", methodName);
            values.put("SyncFg", 0);

            // Only Insert Exception one time in day
            if (id == 0) {
                database.insert(MySQLiteOpenHelper.TABLE_LOG_DETAIL,
                        "_id", values);
            }

             MainActivity.postData(CommonTask.Post_LOG);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }

    }

    // Created By: Pallavi
    // Created Date: 24 Oct 2018
    // Purpose: post Logs
    public static JSONArray postLog() {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select ExceptionDate,Title,StackTrace,Class,Method from "
                            + MySQLiteOpenHelper.TABLE_LOG_DETAIL + " Where SyncFg=0"
                    , null);

            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("ExceptionDate", cursor.getString(cursor.getColumnIndex("ExceptionDate")));
                obj.put("ErrorTitle", cursor.getString(cursor.getColumnIndex("Title")));
                obj.put("StackTrace", cursor.getString(cursor.getColumnIndex("StackTrace")));
                obj.put("Class", cursor.getString(cursor.getColumnIndex("Class")));
                obj.put("Method", cursor.getString(cursor.getColumnIndex("Method")));
                obj.put("SerialNo", Utility.IMEI);
                obj.put("CompanyId", Utility.companyId);
                obj.put("VehicleId", Utility.vehicleId);
                array.put(obj);
            }


        } catch (Exception e) {
            e.printStackTrace();
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


    // Created By: Pallavi Wattamwar
    // Created Date: 24  Octomber 2018
    // Purpose: after Sync update set syncfg flag = 1
    public static JSONArray LogSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_LOG_DETAIL, values,
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


}
