package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;


public class DeviceBalanceDB {

    // Created By: Deepak Sharma
    // Created Date: 13 January 2020
    // Purpose: Device Balance Transaction
    public static boolean DeviceBalanceTransactionSave() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        boolean status = false;
        try {
            String billDate = Utility.getCurrentDate();
            String transactionDate = Utility.getCurrentUTCDateTime();

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select _id from " + MySQLiteOpenHelper.TABLE_DEVICE_BALANCE_TRANSACTION + " where BillDate=? and DeviceId=?", new String[]{billDate, Utility.IMEI});

            int _id = 0;
            if (cursor.moveToNext()) {
                _id = cursor.getInt(cursor.getColumnIndex("_id"));
            }

            // if balance is not deducted
            // we will deduct balance once per day if driver change his duty status to sleeper, driving,onduty and yard move
            if (_id == 0) {

                ContentValues values = new ContentValues();

                values.put("BillDate", billDate);
                values.put("TransactionDate", transactionDate);
                values.put("DeviceId", Utility.IMEI);
                values.put("VehicleId", Utility.vehicleId);
                values.put("CreatedBy", Utility.activeUserId);
                values.put("SyncFg", 0);

                database.insertOrThrow(MySQLiteOpenHelper.TABLE_DEVICE_BALANCE_TRANSACTION,
                        "_id", values);
            }
            status = true;

        } catch (Exception e) {
            Utility.printError(e.getMessage());

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
            }
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 13 January 2020
    // Purpose: get device balance for web sync
    public static JSONArray getDeviceBalanceTransactionSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id, BillDate , TransactionDate , DeviceId,CreatedBy,VehicleId, SyncFg  from " + MySQLiteOpenHelper.TABLE_DEVICE_BALANCE_TRANSACTION +
                            " where SyncFg=0"
                    , null);
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();

                obj.put("_id", cursor.getInt(cursor.getColumnIndex("_id")));
                obj.put("BillDate", cursor.getString(cursor.getColumnIndex("BillDate")));
                obj.put("TransactionDate", cursor.getString(cursor.getColumnIndex("TransactionDate")));
                obj.put("DeviceId", cursor.getString(cursor.getColumnIndex("DeviceId")));
                obj.put("CreatedBy", cursor.getInt(cursor.getColumnIndex("CreatedBy")));
                obj.put("VehicleId", cursor.getInt(cursor.getColumnIndex("VehicleId")));
                array.put(obj);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());

            LogDB.writeLogs(TripInspectionDB.class.getName(), "::getDVIRSync Error:", exe.getMessage(), Utility.printStackTrace(exe));
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


    // Created By: Pallavi Wattamwar
    // Created Date: 5 June 2019
    // Purpose: update CTPAT Inspection  for web sync
    public static JSONArray DeviceBalanceSync(int id) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();
        try {

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            ContentValues values = new ContentValues();

            values.put("SyncFg", 1);

            database.update(MySQLiteOpenHelper.TABLE_DEVICE_BALANCE_TRANSACTION,values,
                    "_id=?", new String[]{id + ""});


        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogDB.writeLogs(CTPATInspectionDB.class.getName(), "::DeviceBalanceSync Error:", exe.getMessage(), Utility.printStackTrace(exe));

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
