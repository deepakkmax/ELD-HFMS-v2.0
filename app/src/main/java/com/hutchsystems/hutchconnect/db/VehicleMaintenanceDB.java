package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.beans.VehicleMaintenanceBean;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Deepak Sharma on 4/25/2017.
 */

public class VehicleMaintenanceDB {


    // Created By: Deepak Sharma
    // Created Date: 21 April 2017
    // Purpose: update Maintenance Due get
    public static VehicleMaintenanceBean MaintenanceGetById(int id) {

        VehicleMaintenanceBean bean = new VehicleMaintenanceBean();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select MaintenanceDate ,VehicleId ,UnitNo ,CurrencyId ,ItemId ,ScheduleId ,DriverId , RepairedBy ,Comment ,Description ,InvoiceNo ,PartCost , LabourCost,OdometerReading, DocPath  from " +
                    MySQLiteOpenHelper.TABLE_VEHICLE_MAINTENANCE_DETAIL + " where _Id=?", new String[]{id + ""});
            if (cursor.moveToNext()) {
                bean.setMaintenanceDate(cursor.getString(cursor.getColumnIndex("MaintenanceDate")));
                bean.setVehicleId(cursor.getInt(cursor.getColumnIndex("VehicleId")));
                bean.setUnitNo(cursor.getString(cursor.getColumnIndex("UnitNo")));
                bean.setCurrency(cursor.getInt(cursor.getColumnIndex("CurrencyId")));
                bean.setItemId(cursor.getInt(cursor.getColumnIndex("ItemId")));
                bean.setScheduleId(cursor.getInt(cursor.getColumnIndex("ScheduleId")));
                bean.setDriverId(cursor.getInt(cursor.getColumnIndex("DriverId")));
                bean.setRepairedBy(cursor.getString(cursor.getColumnIndex("RepairedBy")));
                bean.setComment(cursor.getString(cursor.getColumnIndex("Comment")));
                bean.setDescription(cursor.getString(cursor.getColumnIndex("Description")));
                bean.setInvoiceNo(cursor.getString(cursor.getColumnIndex("InvoiceNo")));
                bean.setPartCost(cursor.getString(cursor.getColumnIndex("PartCost")));
                bean.setLabourCost(cursor.getString(cursor.getColumnIndex("LabourCost")));
                bean.setFileName(cursor.getString(cursor.getColumnIndex("DocPath")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
            }
        } catch (Exception exe) {
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e1) {

            }
        }
        return bean;
    }

    // Created By: Deepak Sharma
    // Created Date: 21 April 2017
    // Purpose: update Maintenance Due get
    public static ArrayList<VehicleMaintenanceBean> MaintenanceGet() {
        ArrayList<VehicleMaintenanceBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select MaintenanceDate ,VehicleId ,UnitNo ,CurrencyId ,ItemId ,m.ScheduleId,s.Schedule ,DriverId , RepairedBy ,Comment ,Description ,InvoiceNo ,PartCost , LabourCost,OdometerReading, DocPath  from " +
                    MySQLiteOpenHelper.TABLE_VEHICLE_MAINTENANCE_DETAIL + " m left join " +
                    MySQLiteOpenHelper.TABLE_SCHEDULE + " s on s.ScheduleId=m.ScheduleId  where DriverId=? order by _Id ", new String[]{Utility.onScreenUserId + ""});
            while (cursor.moveToNext()) {
                VehicleMaintenanceBean bean = new VehicleMaintenanceBean();
                bean.setMaintenanceDate(cursor.getString(cursor.getColumnIndex("MaintenanceDate")));
                bean.setVehicleId(cursor.getInt(cursor.getColumnIndex("VehicleId")));
                bean.setUnitNo(cursor.getString(cursor.getColumnIndex("UnitNo")));
                bean.setCurrency(cursor.getInt(cursor.getColumnIndex("CurrencyId")));
                bean.setItemId(cursor.getInt(cursor.getColumnIndex("ItemId")));
                int scheduleId = cursor.getInt(cursor.getColumnIndex("ScheduleId"));
                bean.setScheduleId(scheduleId);
                if (scheduleId == 0) {
                    bean.setSchedule("N/A");
                } else {
                    bean.setSchedule(cursor.getString(cursor.getColumnIndex("Schedule")));
                }

                bean.setDriverId(cursor.getInt(cursor.getColumnIndex("DriverId")));
                bean.setRepairedBy(cursor.getString(cursor.getColumnIndex("RepairedBy")));
                bean.setComment(cursor.getString(cursor.getColumnIndex("Comment")));
                bean.setDescription(cursor.getString(cursor.getColumnIndex("Description")));
                bean.setInvoiceNo(cursor.getString(cursor.getColumnIndex("InvoiceNo")));
                bean.setPartCost(cursor.getString(cursor.getColumnIndex("PartCost")));
                bean.setLabourCost(cursor.getString(cursor.getColumnIndex("LabourCost")));
                bean.setFileName(cursor.getString(cursor.getColumnIndex("DocPath")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));

                list.add(bean);
            }
        } catch (Exception exe) {
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
    // Purpose: update Maintenance Due get
    public static JSONArray MaintenanceGetSync() {
        JSONArray array = new JSONArray();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select MaintenanceDate ,VehicleId ,UnitNo ,CurrencyId ,ItemId ,ScheduleId ,DriverId , RepairedBy ,Comment ,Description ,InvoiceNo ,PartCost , LabourCost,OdometerReading, DocPath,dueOn  from " +
                    MySQLiteOpenHelper.TABLE_VEHICLE_MAINTENANCE_DETAIL + " where SyncFg=0 order by _Id ", null);
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                String date = cursor.getString(cursor.getColumnIndex("MaintenanceDate"));
                if (date == null)
                    continue;
                obj.put("MaintenanceDate", date);
                obj.put("VehicleId", cursor.getInt(cursor.getColumnIndex("VehicleId")));
                obj.put("UnitNo", cursor.getString(cursor.getColumnIndex("UnitNo")));
                obj.put("Currency", cursor.getInt(cursor.getColumnIndex("CurrencyId")));
                obj.put("ItemId", cursor.getInt(cursor.getColumnIndex("ItemId")));
                obj.put("ScheduleId", cursor.getInt(cursor.getColumnIndex("ScheduleId")));
                obj.put("DriverId", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("RepairedBy", cursor.getString(cursor.getColumnIndex("RepairedBy")));
                obj.put("Comment", cursor.getString(cursor.getColumnIndex("Comment")));
                obj.put("Description", cursor.getString(cursor.getColumnIndex("Description")));
                obj.put("InvoiceNo", cursor.getString(cursor.getColumnIndex("InvoiceNo")));
                obj.put("PartCost", cursor.getString(cursor.getColumnIndex("PartCost")));
                obj.put("LabourCost", cursor.getString(cursor.getColumnIndex("LabourCost")));
                obj.put("FileName", cursor.getString(cursor.getColumnIndex("DocPath")));
                obj.put("OdometerReading", cursor.getString(cursor.getColumnIndex("OdometerReading")));
                obj.put("dueOn", cursor.getInt(cursor.getColumnIndex("dueOn")));
                obj.put("CompanyId", Utility.companyId);
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

    public static boolean Save(VehicleMaintenanceBean bean) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("MaintenanceDate", bean.getMaintenanceDate());
            values.put("VehicleId", bean.getVehicleId());
            values.put("UnitNo", bean.getUnitNo());
            values.put("CurrencyId", bean.getCurrency());
            values.put("ItemId", bean.getItemId());
            values.put("ScheduleId", bean.getScheduleId());
            values.put("DriverId", bean.getDriverId());
            values.put("RepairedBy", bean.getRepairedBy());
            values.put("Comment", bean.getComment());
            values.put("Description", bean.getDescription());
            values.put("InvoiceNo", bean.getInvoiceNo());
            values.put("PartCost", bean.getPartCost());
            values.put("LabourCost", bean.getLabourCost());
            values.put("DocPath", bean.getFileName());
            values.put("dueOn", bean.getDueOn());
            values.put("OdometerReading", bean.getOdometerReading());
            values.put("SyncFg", 0);

            if (bean.getId() == 0) {
                database.insert(MySQLiteOpenHelper.TABLE_VEHICLE_MAINTENANCE_DETAIL, "_id", values);
            } else {
                database.update(MySQLiteOpenHelper.TABLE_VEHICLE_MAINTENANCE_DETAIL, values, "_id=?", new String[]{bean.getId() + ""});
            }

            MainActivity.postData(CommonTask.Post_Maintenance);
        } catch (Exception e) {
            LogFile.write(VehicleMaintenanceDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(VehicleMaintenanceDB.class.getName(), "::Save Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e1) {
            }
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 21 April 2017
    // Purpose: update Maintenance Due for web sync
    public static JSONArray MaintenanceSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_VEHICLE_MAINTENANCE_DETAIL, values,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(AlertDB.class.getName() + "::MaintenanceDueSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(VehicleMaintenanceDB.class.getName(), "::MaintenanceSyncUpdate Error:", exe.getMessage(), Utility.printStackTrace(exe));
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
