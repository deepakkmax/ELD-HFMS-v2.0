package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.beans.TrailerBean;
import com.hutchsystems.hutchconnect.beans.VehicleBean;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by SAMSUNG on 30-12-2016.
 */

public class TrailerDB {

    // Created By: Deepak Sharma
    // Created Date: 22 December 2016
    // Purpose: update TPMS for web sync
    public static JSONArray TrailerStatusSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_TRAILER_STATUS, values,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(TrailerDB.class.getName() + "::TpmsSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TrailerDB.class.getName(),"::TpmsSyncUpdate Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

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
    // Created Date: 04 January 2017
    // Purpose: get Trailer STatus for web sync
    public static JSONArray getTrailerStatusDataSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select startOdometer ,endOdometer ,TrailerId ,driverId ,hookDate ,unhookDate ,hookedFg ,modifiedBy ,latitude1 ,longitude1 ,latitude2 , longitude2 from "
                            + MySQLiteOpenHelper.TABLE_TRAILER_STATUS + " Where SyncFg=0"
                    , null);
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("StartOdometerReading", cursor.getString(cursor.getColumnIndex("startOdometer")));
                obj.put("EndOdometerReading", cursor.getString(cursor.getColumnIndex("endOdometer")));
                obj.put("TrailerId", cursor.getInt(cursor.getColumnIndex("TrailerId")));
                obj.put("VehicleId", Utility.vehicleId);
                obj.put("DriverId", cursor.getInt(cursor.getColumnIndex("driverId")));
                obj.put("HookDate", cursor.getString(cursor.getColumnIndex("hookDate")));
                obj.put("UnhookDate", cursor.getString(cursor.getColumnIndex("unhookDate")));
                obj.put("latitude1", cursor.getString(cursor.getColumnIndex("latitude1")));
                obj.put("longitude1", cursor.getString(cursor.getColumnIndex("longitude1")));
                obj.put("latitude2", cursor.getString(cursor.getColumnIndex("latitude2")));
                obj.put("longitude2", cursor.getString(cursor.getColumnIndex("longitude2")));
                obj.put("modifiedBy", cursor.getString(cursor.getColumnIndex("modifiedBy")));
                obj.put("HookedFg", cursor.getString(cursor.getColumnIndex("hookedFg")));
                array.put(obj);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(TpmsDB.class.getName() + "::getTpmsDataSync Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TrailerDB.class.getName(),"::getTpmsDataSync Error:" ,e.getMessage(),Utility.printStackTrace(e));

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

    // Created By: Deepak Sharma
    // Created Date: 30 December 2016
    // Purpose: hook trailer info
    public static boolean hook(TrailerBean bean) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("hookedFg", 1);
            values.put("hookDate", bean.getHookDate());
            values.put("startOdometer", bean.getStartOdometer());
            values.put("TrailerId", bean.getTrailerId());
            values.put("driverId", bean.getDriverId());
            values.put("latitude1", bean.getLatitude1());
            values.put("longitude1", bean.getLongitude1());
            values.put("SyncFg", 0);
            database.insert(MySQLiteOpenHelper.TABLE_TRAILER_STATUS,
                    "_id,unhookDate,endOdometer,latitude2,longitude2,modifiedBy", values);

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(TrailerDB.class.getName() + "::hook Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TrailerDB.class.getName(),"::hook Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            database.close();
            helper.close();
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 30 December 2016
    // Purpose: add trailer info
    public static boolean unhook(TrailerBean bean) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("hookedFg", 0);
            values.put("unhookDate", bean.getUnhookDate());
            values.put("endOdometer", bean.getEndOdometer());
            values.put("modifiedBy", bean.getDriverId());
            values.put("latitude2", bean.getLatitude2());
            values.put("longitude2", bean.getLongitude2());
            values.put("SyncFg", 0);
            Cursor cursor = database.rawQuery("select _id from "
                    + MySQLiteOpenHelper.TABLE_TRAILER_STATUS
                    + " where TrailerId=? order by _id desc LIMIT 1", new String[]{bean.getTrailerId() + ""});
            if (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                database.update(MySQLiteOpenHelper.TABLE_TRAILER_STATUS, values,
                        " _id= ?",
                        new String[]{id + ""});
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(TrailerDB.class.getName() + "::hook Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TrailerDB.class.getName(),"::hook Error:" ,e.getMessage(),Utility.printStackTrace(e));
        } finally {
            database.close();
            helper.close();
        }
        return status;
    }

    public static ArrayList<String> getHookedTrailer() {
        ArrayList<String> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            //Compulsory Power Unit
            list.add(Utility.vehicleId + "");
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select TrailerId from "
                            + MySQLiteOpenHelper.TABLE_TRAILER_STATUS + " Where hookedFg=1 order by _id"
                    , null);
            while (cursor.moveToNext()) {
                list.add(cursor.getString(0));
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(TrailerDB.class.getName() + "::getHookedTrailer Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TrailerDB.class.getName(),"::getHookedTrailer Error:" ,e.getMessage(),Utility.printStackTrace(e));

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

    // get
    public static ArrayList<VehicleBean> getHookedVehicleInfo() {
        ArrayList<VehicleBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            VehicleBean bean = new VehicleBean();
            bean.setVehicleId(Utility.vehicleId);
            bean.setUnitNo(Utility.UnitNo);
            //Compulsory Power Unit
            list.add(bean);

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select t.VehicleId,t.UnitNo from "
                            + MySQLiteOpenHelper.TABLE_TRAILER_STATUS + " ts inner join "
                            + MySQLiteOpenHelper.TABLE_TRAILER + " t on ts.TrailerId=t.VehicleId"
                            + " Where hookedFg=1 order by t.UnitNo"
                    , null);
            while (cursor.moveToNext()) {
                bean = new VehicleBean();
                bean.setVehicleId(cursor.getInt(0));
                bean.setUnitNo(cursor.getString(1));
                list.add(bean);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(TrailerDB.class.getName() + "::getHookedTrailer Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TrailerDB.class.getName(),"::getHookedTrailer Error:" ,e.getMessage(),Utility.printStackTrace(e));

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

    // get
    public static String[] getTrailers() {
        ArrayList<VehicleBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        String ids = "";
        String unitNos = "";
        try {
            VehicleBean bean = new VehicleBean();
            bean.setVehicleId(Utility.vehicleId);
            bean.setUnitNo(Utility.UnitNo);
            //Compulsory Power Unit
            list.add(bean);

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select t.VehicleId,t.UnitNo from "
                            + MySQLiteOpenHelper.TABLE_TRAILER_STATUS + " ts inner join "
                            + MySQLiteOpenHelper.TABLE_TRAILER + " t on ts.TrailerId=t.VehicleId"
                            + " Where hookedFg=1 order by t.UnitNo"
                    , null);
            while (cursor.moveToNext()) {
                ids += cursor.getInt(0) + ",";
                unitNos += cursor.getString(1) + ",";
            }
            ids = ids.replaceAll(",$", "");
            unitNos = unitNos.replaceAll(",$", "");
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(TrailerDB.class.getName() + "::getTrailers Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TrailerDB.class.getName(),"::getTrailers Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }
        return new String[]{ids, unitNos};
    }

    // get
    public static String[] getTrailers(String ids) {
        ArrayList<VehicleBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        String unitNos = "";
        try {
            VehicleBean bean = new VehicleBean();
            bean.setVehicleId(Utility.vehicleId);
            bean.setUnitNo(Utility.UnitNo);
            //Compulsory Power Unit
            list.add(bean);

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select UnitNo from "
                            + MySQLiteOpenHelper.TABLE_TRAILER + " Where VehicleId in (" + ids + ") order by UnitNo"
                    , null);
            while (cursor.moveToNext()) {
                unitNos += cursor.getString(0) + ",";
            }
            unitNos = unitNos.replaceAll(",$", "");
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(TrailerDB.class.getName() + "::getTrailers(ids) Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TrailerDB.class.getName(),"::getTrailers(ids) Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }
        return new String[]{ids, unitNos};
    }
}
