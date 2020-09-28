package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.beans.GeofenceBean;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Deepak Sharma on 5/9/2017.
 */

public class GeofenceDB {


    // Created By: Deepak Sharma
    // Created Date: 10 May 2017
    // Purpose: update geo fence for web sync
    public static JSONArray GeofenceMonitorSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_GEOFENCE_MONITOR, values,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {

            LogFile.write(GeofenceDB.class.getName() + "::IncidentSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(GeofenceDB.class.getName(),"::IncidentSyncUpdate Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

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
    // Created Date: 21 April 2017
    // Purpose: update schedule get
    public static JSONArray GeofenceMonitorGetSync() {
        JSONArray array = new JSONArray();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select portId ,vehicleId ,enterDate ,exitDate ,regionInFg ,enterByDriverId ,exitByDriverId ,SyncFg  from " +
                    MySQLiteOpenHelper.TABLE_GEOFENCE_MONITOR + " where SyncFg=0", null);
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("PortId", cursor.getInt(cursor.getColumnIndex("portId")));
                obj.put("VehicleId", cursor.getInt(cursor.getColumnIndex("vehicleId")));
                obj.put("EnterDate", cursor.getString(cursor.getColumnIndex("enterDate")));
                obj.put("ExitDate", cursor.getString(cursor.getColumnIndex("exitDate")));
                obj.put("RegionInFg", cursor.getInt(cursor.getColumnIndex("regionInFg")));
                obj.put("EnterByDriverId", cursor.getInt(cursor.getColumnIndex("enterByDriverId")));
                obj.put("ExitByDriverId", cursor.getInt(cursor.getColumnIndex("exitByDriverId")));
                array.put(obj);
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e1) {

            }
        }
        return array;
    }

    // Created By: Deepak Sharma
    // Created Date: 12 May 2017
    // Purpose:
    public static ArrayList<GeofenceBean> GeofenceMonitorGet(int driverId) {
        ArrayList<GeofenceBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select gm.portId,g.PortRegionName ,enterDate ,exitDate ,regionInFg ,enterByDriverId ,exitByDriverId  from " +
                    MySQLiteOpenHelper.TABLE_GEOFENCE_MONITOR + " gm inner join " +
                    MySQLiteOpenHelper.TABLE_GEOFENCE + " g on g.portId=gm.portId" +
                    " where enterByDriverId=? order by enterDate desc ", new String[]{driverId + ""});
            while (cursor.moveToNext()) {
                GeofenceBean obj = new GeofenceBean();
                obj.setPortId(cursor.getInt(cursor.getColumnIndex("portId")));
                obj.setPortRegionName(cursor.getString(cursor.getColumnIndex("PortRegionName")));
                obj.setEnterDate(cursor.getString(cursor.getColumnIndex("enterDate")));
                obj.setExitDate(cursor.getString(cursor.getColumnIndex("exitDate")));
                obj.setRegionInFg(cursor.getInt(cursor.getColumnIndex("regionInFg")));
                obj.setEnterByDriverId(cursor.getInt(cursor.getColumnIndex("enterByDriverId")));
                obj.setExitByDriverId(cursor.getInt(cursor.getColumnIndex("exitByDriverId")));
                list.add(obj);
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e1) {

            }
        }
        return list;
    }


    // Created By: Deepak Sharma
    // Created Date: 21 April 2017
    // Purpose: update schedule get
    public static ArrayList<GeofenceBean> GeofenceGet() {
        ArrayList<GeofenceBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select portId, PortRegionName ,Latitude ,Longitude, Radius, StatusId  from " +
                    MySQLiteOpenHelper.TABLE_GEOFENCE + " where fenceCreatedFg=0", null);
            while (cursor.moveToNext()) {
                GeofenceBean bean = new GeofenceBean();
                bean.setPortId(cursor.getInt(cursor.getColumnIndex("portId")));
                bean.setPortRegionName(cursor.getString(cursor.getColumnIndex("PortRegionName")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setRadius(cursor.getInt(cursor.getColumnIndex("Radius")));
                bean.setStatusId(cursor.getInt(cursor.getColumnIndex("StatusId")));
                list.add(bean);
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e1) {

            }
        }
        return list;
    }

    public static boolean Save(ArrayList<GeofenceBean> list) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            for (GeofenceBean bean : list) {
                Cursor cursor = database.rawQuery("select portId from " +
                        MySQLiteOpenHelper.TABLE_GEOFENCE +
                        " where portId=?  ", new String[]{bean.getPortId() + ""});
                ContentValues values = new ContentValues();
                values.put("StatusId", bean.getStatusId());
                values.put("SyncFg", 0);
                values.put("PortRegionName", bean.getPortRegionName());
                values.put("Latitude", bean.getLatitude());
                values.put("Longitude", bean.getLongitude());
                values.put("Radius", bean.getRadius());
                if (!cursor.moveToNext()) {
                    values.put("portId", bean.getPortId());
                    values.put("fenceCreatedFg", 0);
                    database.insert(MySQLiteOpenHelper.TABLE_GEOFENCE,
                            "_id", values);

                } else {
                    if (bean.getStatusId() == 3) {
                        values.put("fenceCreatedFg", 0);
                    }
                    database.update(MySQLiteOpenHelper.TABLE_GEOFENCE, values, "portId=?", new String[]{bean.getPortId() + ""});
                }
                cursor.close();
            }
          //PostData
            MainActivity.postData(CommonTask.Post_GeofenceMonitor);
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

    public static boolean GeofenceMonitorSave(GeofenceBean bean) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select _id,regionInFg from " +
                    MySQLiteOpenHelper.TABLE_GEOFENCE_MONITOR +
                    " where portId=? and vehicleId=? order by _id desc Limit 1 ", new String[]{bean.getPortId() + "", Utility.vehicleId + ""});
            int id = 0;
            int regionInFg = 0;
            if (cursor.moveToNext()) {
                id = cursor.getInt(0);
                regionInFg = cursor.getInt(1);
            }

            ContentValues values = new ContentValues();
            values.put("regionInFg", bean.getRegionInFg());
            values.put("exitDate", bean.getExitDate());
            values.put("exitByDriverId", bean.getExitByDriverId());
            values.put("SyncFg", 0);

            if (bean.getRegionInFg() == 1) {
                values.put("portId", bean.getPortId());
                values.put("vehicleId", bean.getVehicleId());
                values.put("enterDate", bean.getEnterDate());
                values.put("enterByDriverId", bean.getEnterByDriverId());
                if (regionInFg == 0)
                    database.insert(MySQLiteOpenHelper.TABLE_GEOFENCE_MONITOR,
                            "_id", values);
            } else {
                if (regionInFg == 1)
                    database.update(MySQLiteOpenHelper.TABLE_GEOFENCE_MONITOR, values, "_id=?", new String[]{id + ""});

            }
            MainActivity.postData(CommonTask.Post_GeofenceMonitor);
        } catch (Exception e) {
            Utility.printError(e.getMessage());
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e1) {

            }
        }
        return status;
    }

}
