package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.beans.ScheduleBean;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Deepak Sharma on 4/20/2017.
 */

public class ScheduleDB {


    // Created By: Deepak Sharma
    // Created Date: 21 April 2017
    // Purpose: update Maintenance Due get
    public static ScheduleBean MaintenanceDueGet(int scheduleId) {

        ScheduleBean bean = new ScheduleBean();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select EffectiveOn , DueOn ,RepairFG,DueStatus  from " +
                    MySQLiteOpenHelper.TABLE_MAINTENANCE_DUE + " where ScheduleId=? order by _Id desc", new String[]{scheduleId + ""});
            if (cursor.moveToNext()) {
                bean.setScheduleId(scheduleId);
                bean.setEffectiveOn(cursor.getInt(cursor.getColumnIndex("EffectiveOn")));
                bean.setDueOn(cursor.getInt(cursor.getColumnIndex("DueOn")));
                bean.setDueStatus(cursor.getInt(cursor.getColumnIndex("DueStatus")));
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
    // Purpose: update schedule get
    public static ArrayList<ScheduleBean> ScheduleGet() {
        ArrayList<ScheduleBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            String currentDate = Utility.getCurrentDate() + " 00:00:00";
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select ScheduleId, Schedule ,Threshold ,Unit,EffectiveDate  from " +
                    MySQLiteOpenHelper.TABLE_SCHEDULE + " where StatusId=1 and EffectiveDate<=?", new String[]{currentDate});
            while (cursor.moveToNext()) {
                ScheduleBean bean = new ScheduleBean();
                bean.setScheduleId(cursor.getInt(cursor.getColumnIndex("ScheduleId")));
                bean.setSchedule(cursor.getString(cursor.getColumnIndex("Schedule")));
                bean.setThreshold(cursor.getInt(cursor.getColumnIndex("Threshold")));
                bean.setUnit(cursor.getString(cursor.getColumnIndex("Unit")));
                bean.setEffectiveDate(cursor.getString(cursor.getColumnIndex("EffectiveDate")));
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

    public static boolean Save(ArrayList<ScheduleBean> list) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            for (ScheduleBean bean : list) {
                Cursor cursor = database.rawQuery("select ScheduleId from " +
                        MySQLiteOpenHelper.TABLE_SCHEDULE +
                        " where ScheduleId=?  ", new String[]{bean.getScheduleId() + ""});

                ContentValues values = new ContentValues();
                values.put("ScheduleId", bean.getScheduleId());
                values.put("Schedule", bean.getSchedule());
                values.put("EffectiveDate", bean.getEffectiveDate());
                values.put("Threshold", bean.getThreshold());
                values.put("Unit", bean.getUnit());
                values.put("StatusId", bean.getStatusId());
                values.put("SyncFg", 0);

                if (!cursor.moveToNext()) {
                    database.insert(MySQLiteOpenHelper.TABLE_SCHEDULE,
                            "_id", values);

                } else {
                    database.update(MySQLiteOpenHelper.TABLE_SCHEDULE, values, "ScheduleId=?", new String[]{bean.getScheduleId() + ""});
                }
                cursor.close();
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

    // Created By: Deepak Sharma
    // Created Date: 21 April 2017
    // Purpose: update Maintenance Due get
    public static int MaintenanceDueStatusGet() {
        int dueStatus = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select DueStatus  from " +
                    MySQLiteOpenHelper.TABLE_MAINTENANCE_DUE +
                    " where DueStatus>0 and RepairFG=0 order by _id desc", null);

            while (cursor.moveToNext()) {
                dueStatus = cursor.getInt(cursor.getColumnIndex("DueStatus"));
                if (dueStatus == 2) {
                    break;
                }

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
        return dueStatus;
    }

    // Created By: Deepak Sharma
    // Created Date: 21 April 2017
    // Purpose: update Maintenance Due get
    public static ArrayList<ScheduleBean> MaintenanceDueGet() {
        ArrayList<ScheduleBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select s.ScheduleId, s.Schedule ,s.Threshold ,s.Unit, md.EffectiveOn, " +
                    "md.DueOn,md._id,md.DueStatus  from " +
                    MySQLiteOpenHelper.TABLE_SCHEDULE + " s inner join  " +
                    MySQLiteOpenHelper.TABLE_MAINTENANCE_DUE + " md on s.ScheduleId=md.ScheduleId" +
                    " where s.StatusId=1 and RepairFG=0 and DueStatus>0", null);
            while (cursor.moveToNext()) {
                ScheduleBean bean = new ScheduleBean();
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setScheduleId(cursor.getInt(cursor.getColumnIndex("ScheduleId")));
                bean.setSchedule(cursor.getString(cursor.getColumnIndex("Schedule")));
                bean.setThreshold(cursor.getInt(cursor.getColumnIndex("Threshold")));
                bean.setUnit(cursor.getString(cursor.getColumnIndex("Unit")));
                bean.setEffectiveOn(cursor.getInt(cursor.getColumnIndex("EffectiveOn")));
                bean.setDueOn(cursor.getInt(cursor.getColumnIndex("DueOn")));
                bean.setDueStatus(cursor.getInt(cursor.getColumnIndex("DueStatus")));
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
    // Purpose: update Maintenance Due save
    public static boolean MaintenanceDueSave(ArrayList<ScheduleBean> list) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            for (ScheduleBean bean : list) {
                Cursor cursor = database.rawQuery("select _id from " +
                        MySQLiteOpenHelper.TABLE_MAINTENANCE_DUE +
                        " where ScheduleId=? and DueOn=?", new String[]{bean.getScheduleId() + "", bean.getDueOn() + ""});

                if (!cursor.moveToNext()) {
                    ContentValues values = new ContentValues();
                    values.put("ScheduleId", bean.getScheduleId());
                    values.put("EffectiveOn", bean.getEffectiveOn());
                    values.put("DueOn", bean.getDueOn());
                    values.put("DueStatus", bean.getDueStatus());
                    values.put("RepairFG", 0);
                    values.put("SyncFg", 0);
                    database.insert(MySQLiteOpenHelper.TABLE_MAINTENANCE_DUE,
                            "_id", values);

                }
                cursor.close();
            }
            // Post Maintence History
            MainActivity.postData(CommonTask.Post_MaintenanceDueHistory);
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


    // Created By: Deepak Sharma
    // Created Date: 21 April 2017
    // Purpose: update Maintenance Due update
    public static boolean MaintenanceDueUpdate(ArrayList<ScheduleBean> list) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            for (ScheduleBean bean : list) {
                Cursor cursor = database.rawQuery("select _id from " +
                        MySQLiteOpenHelper.TABLE_MAINTENANCE_DUE +
                        " where ScheduleId=? and DueOn=?", new String[]{bean.getScheduleId() + "", bean.getDueOn() + ""});

                if (cursor.moveToNext()) {
                    ContentValues values = new ContentValues();
                    values.put("DueStatus", bean.getDueStatus());
                    values.put("SyncFg", 0);
                    int id = cursor.getInt(0);
                    database.update(MySQLiteOpenHelper.TABLE_MAINTENANCE_DUE, values, "_id=?", new String[]{id + ""});
                }
                cursor.close();


            }

            MainActivity.postData(CommonTask.Post_MaintenanceDueHistory);
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

    // Created By: Deepak Sharma
    // Created Date: 21 April 2017
    // Purpose: update Maintenance Due update
    public static boolean MaintenanceDueUpdate(int scheduleId, int dueOn) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("RepairFG", 1);
            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_MAINTENANCE_DUE, values, " ScheduleId=? and DueOn=?", new String[]{scheduleId + "", dueOn + ""});
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

    // Created By: Deepak Sharma
    // Created Date: 21 April 2017
    // Purpose: update Maintenance Due get for web sync
    public static JSONArray MaintenanceDueGetSync() {
        JSONArray array = new JSONArray();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select ScheduleId ,EffectiveOn , DueOn ,RepairFG ,DueStatus  from " +
                    MySQLiteOpenHelper.TABLE_MAINTENANCE_DUE +
                    " where SyncFG=0", null);
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("ScheduleId", cursor.getInt(cursor.getColumnIndex("ScheduleId")));
                obj.put("EffectiveOn", cursor.getInt(cursor.getColumnIndex("EffectiveOn")));
                obj.put("DueOn", cursor.getInt(cursor.getColumnIndex("DueOn")));
                obj.put("RepairFG", cursor.getInt(cursor.getColumnIndex("RepairFG")));
                obj.put("DueStatus", cursor.getInt(cursor.getColumnIndex("DueStatus")));
                obj.put("VehicleId", Utility.vehicleId);
                obj.put("CompanyId", Utility.companyId);
                array.put(obj);
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
        return array;
    }

    // Created By: Deepak Sharma
    // Created Date: 21 April 2017
    // Purpose: update Maintenance Due for web sync
    public static JSONArray MaintenanceDueSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_MAINTENANCE_DUE, values,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(AlertDB.class.getName() + "::MaintenanceDueSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(ScheduleDB.class.getName(),"::MaintenanceDueSyncUpdate Error:" ,exe.getMessage(),Utility.printStackTrace(exe));
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
