package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.beans.MaintenanceBean;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import java.util.ArrayList;

/**
 * Created by Deepak Sharma on 2/3/2017.
 */

@Deprecated
public class MaintenanceDB {

    public static ArrayList<MaintenanceBean> MaintenanceDetailGet(String date) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<MaintenanceBean> list = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            cursor = database.rawQuery("select MaintenanceAlertId , ScheduleId ,ScheduleName , DueDate ,RepairFg , MaintenanceId , DUEKM , DUEMILES ,DUEENGINEHOURS from " +
                    MySQLiteOpenHelper.TABLE_MAINTENANCE_DETAIL +
                    " where RepairFg=0 order by DueDate ", null);
            while (cursor.moveToNext()) {
                MaintenanceBean bean = new MaintenanceBean();
                bean.setAlertId(cursor.getInt(cursor.getColumnIndex("MaintenanceAlertId")));
                bean.setScheduleId(cursor.getInt(cursor.getColumnIndex("ScheduleId")));
                bean.setScheduleName(cursor.getString(cursor.getColumnIndex("ScheduleName")));
                bean.setDueDate(cursor.getString(cursor.getColumnIndex("DueDate")));
                bean.setRepairFg(cursor.getInt(cursor.getColumnIndex("RepairFg")));
                bean.setMaintenanceId(cursor.getInt(cursor.getColumnIndex("MaintenanceId")));
                bean.setDueKM(cursor.getString(cursor.getColumnIndex("DUEKM")));
                bean.setDueMiles(cursor.getString(cursor.getColumnIndex("DUEMILES")));
                bean.setDueEngineHour(cursor.getString(cursor.getColumnIndex("DUEENGINEHOURS")));
                list.add(bean);
            }

        } catch (Exception e) {
            LogFile.write(MaintenanceDB.class.getName() + "::MaintenanceDetailGet Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(MaintenanceDB.class.getName(),"::MaintenanceDetailGet Error:" ,e.getMessage(),Utility.printStackTrace(e));
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
    // Created Date: 02 Febuary 2017
    // Purpose: Save Incident Detail
    public static boolean Save(ArrayList<MaintenanceBean> data) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            for (int i = 0; i < data.size(); i++) {
                MaintenanceBean bean = data.get(i);

                ContentValues values = new ContentValues();
                values.put("MaintenanceAlertId", bean.getAlertId());
                values.put("ScheduleId", bean.getScheduleId());
                values.put("ScheduleName", bean.getScheduleName());
                values.put("DueDate", bean.getDueDate());
                values.put("RepairFg", bean.getRepairFg());
                values.put("MaintenanceId", bean.getMaintenanceId());
                values.put("DUEKM", bean.getDueKM());
                values.put("DUEMILES", bean.getDueMiles());
                values.put("DUEENGINEHOURS", bean.getDueEngineHour());
                Cursor cursor = database.rawQuery("select 1 from " +
                        MySQLiteOpenHelper.TABLE_MAINTENANCE_DETAIL +
                        " where MaintenanceAlertId=? ", new String[]{bean.getAlertId() + ""});

                if (cursor.moveToNext()) {

                    database.update(MySQLiteOpenHelper.TABLE_MAINTENANCE_DETAIL, values, "MaintenanceAlertId=?", new String[]{bean.getAlertId() + ""});
                } else {
                    database.insert(MySQLiteOpenHelper.TABLE_MAINTENANCE_DETAIL, null
                            , values);
                }
                cursor.close();
            }

        } catch (Exception e) {
            status = false;
            LogFile.write(MaintenanceDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(MaintenanceDB.class.getName(),"::Save Error:" ,e.getMessage(),Utility.printStackTrace(e));
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
