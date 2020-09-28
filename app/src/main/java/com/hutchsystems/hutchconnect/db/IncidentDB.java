package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.beans.IncidentBean;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by SAMSUNG on 01-02-2017.
 */

public class IncidentDB {


    // Created By: Deepak Sharma
    // Created Date: 02 Febuaray 2017
    // Purpose: update Incident for web sync
    public static JSONArray IncidentSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_INCIDENT_DETAIL, values,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {

            LogFile.write(FuelDetailDB.class.getName() + "::IncidentSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(IncidentDB.class.getName(),"::IncidentSyncUpdate Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

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
    // Created Date: 02 Febuary 2017
    // Purpose: get Incident for web sync
    public static JSONArray getIncidentSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select IncidentDate, FineAmount , DriverId , VehicleId , Duration  , ReportNo , Level , Result ,FineAmount ,Comment ,Latitude , Longitude , Location ,type ,ModifiedBy ,ModifiedDate,trailerNo, DocPath ,SyncFg from "
                            + MySQLiteOpenHelper.TABLE_INCIDENT_DETAIL + " Where SyncFg=0"
                    , null);

            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();


                obj.put("IncidentDate", cursor.getString(cursor.getColumnIndex("IncidentDate")));
                obj.put("VehicleId", cursor.getInt(cursor.getColumnIndex("VehicleId")));
                obj.put("DriverId", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("Duration", cursor.getInt(cursor.getColumnIndex("Duration")));
                obj.put("Latitude", cursor.getString(cursor.getColumnIndex("Latitude")));
                obj.put("Longitude", cursor.getString(cursor.getColumnIndex("Longitude")));
                obj.put("Location", cursor.getString(cursor.getColumnIndex("Location")));
                obj.put("ReportNo", cursor.getString(cursor.getColumnIndex("ReportNo")));
                obj.put("FineAmount", cursor.getString(cursor.getColumnIndex("FineAmount")));
                obj.put("Comment", cursor.getString(cursor.getColumnIndex("Comment")));
                obj.put("IncidentLevel", cursor.getString(cursor.getColumnIndex("Level")));
                obj.put("Result", cursor.getString(cursor.getColumnIndex("Result")));
                obj.put("CompanyId", Utility.companyId);
                obj.put("ModifiedBy", cursor.getInt(cursor.getColumnIndex("ModifiedBy")));
                obj.put("ModifiedDate", cursor.getString(cursor.getColumnIndex("ModifiedDate")));
                obj.put("TrailerNo", cursor.getString(cursor.getColumnIndex("trailerNo")));
                obj.put("IncidentType", cursor.getInt(cursor.getColumnIndex("type")));
                obj.put("DocPath", cursor.getString(cursor.getColumnIndex("DocPath")));
                obj.put("CompanyId", Utility.companyId);
                obj.put("StatusId", 1);
                array.put(obj);
            }

        } catch (Exception e) {
            LogFile.write(VehicleInfoDB.class.getName() + "::getIncidentSync Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(IncidentDB.class.getName(),"::getIncidentSync Error:" ,e.getMessage(),Utility.printStackTrace(e));

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
    // Created Date: 17 february 2017
    // Purpose: delete document older than 15 days for fuel
    public static void deleteDocument() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            String date = Utility.getPreviousDate(-15);
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select  type, DocPath from "
                            + MySQLiteOpenHelper.TABLE_INCIDENT_DETAIL
                            + " Where IncidentDate < ?"
                    , new String[]{date});

            while (cursor.moveToNext()) {
                String type = Utility.getDocumentType(cursor.getInt(0));
                String fileName = cursor.getString(cursor.getColumnIndex("DocPath"));
                Utility.DeleteFile(type, fileName);

            }

        } catch (Exception e) {
            LogFile.write(IncidentDB.class.getName() + "::deleteDocument Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(IncidentDB.class.getName(),"::deleteDocument Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 02 Febuary 2017
    // Purpose: get Incident Detail
    public static IncidentBean getIncidentById(int id) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        IncidentBean bean = new IncidentBean();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _Id,IncidentDate, DriverId , VehicleId , Duration , ReportNo, Level , Result ,FineAmount, Comment, type,trailerNo, DocPath from "
                            + MySQLiteOpenHelper.TABLE_INCIDENT_DETAIL + " Where _id=?"
                    , new String[]{id + ""});

            if (cursor.moveToNext()) {
                bean.set_id(id);
                bean.setIncidentDate(cursor.getString(cursor.getColumnIndex("IncidentDate")));
                bean.setVehicleId(cursor.getInt(cursor.getColumnIndex("VehicleId")));
                bean.setDriverId(cursor.getInt(cursor.getColumnIndex("DriverId")));
                bean.setDuration(cursor.getInt(cursor.getColumnIndex("Duration")));

                bean.setReportNo(cursor.getString(cursor.getColumnIndex("ReportNo")));
                bean.setLevel(cursor.getInt(cursor.getColumnIndex("Level")));
                bean.setResult(cursor.getInt(cursor.getColumnIndex("Result")));
                bean.setFineAmount(cursor.getString(cursor.getColumnIndex("FineAmount")));
                bean.setComment(cursor.getString(cursor.getColumnIndex("Comment")));
                bean.setType(cursor.getInt(cursor.getColumnIndex("type")));
                bean.setTrailerNo(cursor.getString(cursor.getColumnIndex("trailerNo")));
                bean.setDocPath(cursor.getString(cursor.getColumnIndex("DocPath")));
            }

        } catch (Exception e) {
            LogFile.write(IncidentDB.class.getName() + "::getIncidentById Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(IncidentDB.class.getName(),"::getIncidentById Error:" ,e.getMessage(),Utility.printStackTrace(e));
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {

            }
        }
        return bean;
    }


    // Created By: Deepak Sharma
    // Created Date: 30 January 2017
    // Purpose: get Fuel Detail for web sync
    public static ArrayList<IncidentBean> getIncidentDetail(String date) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<IncidentBean> list = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _Id,IncidentDate, DriverId , VehicleId , Duration , ReportNo, Level , Result ,FineAmount, Comment, type, DocPath from "
                            + MySQLiteOpenHelper.TABLE_INCIDENT_DETAIL + " Where IncidentDate>=? order by _id desc"
                    , new String[]{date});

            while (cursor.moveToNext()) {
                IncidentBean bean = new IncidentBean();
                bean.set_id(cursor.getInt(0));
                bean.setIncidentDate(cursor.getString(cursor.getColumnIndex("IncidentDate")));
                bean.setVehicleId(cursor.getInt(cursor.getColumnIndex("VehicleId")));
                bean.setDriverId(cursor.getInt(cursor.getColumnIndex("DriverId")));
                bean.setDuration(cursor.getInt(cursor.getColumnIndex("Duration")));
                bean.setReportNo(cursor.getString(cursor.getColumnIndex("ReportNo")));
                bean.setLevel(cursor.getInt(cursor.getColumnIndex("Level")));
                bean.setResult(cursor.getInt(cursor.getColumnIndex("Result")));
                bean.setFineAmount(cursor.getString(cursor.getColumnIndex("FineAmount")));
                bean.setComment(cursor.getString(cursor.getColumnIndex("Comment")));
                bean.setType(cursor.getInt(cursor.getColumnIndex("type")));
                bean.setDocPath(cursor.getString(cursor.getColumnIndex("DocPath")));
                list.add(bean);
            }

        } catch (Exception e) {
            LogFile.write(FuelDetailDB.class.getName() + "::getIncidentDetail Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(IncidentDB.class.getName(),"::getIncidentDetail Error:" ,e.getMessage(),Utility.printStackTrace(e));

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
    // Created Date: 02 Febuary 2017
    // Purpose: Save Incident Detail
    public static boolean Save(IncidentBean bean) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            //_id, DriverId, VehicleId, Level, Result, type, SyncFg;
            //IncidentDate, ReportNo, FineAmount, Comment, Latitude, Longitude, Location, CardNo;
            ContentValues values = new ContentValues();


            values.put("ModifiedBy", Utility.onScreenUserId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            values.put("Level", bean.getLevel());
            values.put("Result", bean.getResult());
            values.put("Comment", bean.getComment());
            values.put("ReportNo", bean.getReportNo());
            values.put("FineAmount", bean.getFineAmount());
            values.put("SyncFg", 0);

            if (bean.get_id() == 0) {
                values.put("IncidentDate", bean.getIncidentDate());
                values.put("VehicleId", bean.getVehicleId());
                values.put("DriverId", bean.getDriverId());
                values.put("Duration", bean.getDuration());
                values.put("type", bean.getType());
                values.put("Latitude", Utility.currentLocation.getLatitude());
                values.put("Longitude", Utility.currentLocation.getLongitude());
                values.put("Location", Utility.currentLocation.getLocationDescription());
                values.put("DocPath", bean.getDocPath());
                values.put("trailerNo", bean.getTrailerNo());

                database.insert(MySQLiteOpenHelper.TABLE_INCIDENT_DETAIL,
                        "_id", values);
            } else {
                database.update(MySQLiteOpenHelper.TABLE_INCIDENT_DETAIL, values, "_id=?", new String[]{bean.get_id() + ""});
            }
            // Post Incident
            MainActivity.postData(CommonTask.Post_IncidentDetail);

        } catch (Exception e) {
            status = false;
            LogFile.write(VehicleInfoDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(IncidentDB.class.getName(),"::Save Error:" ,e.getMessage(),Utility.printStackTrace(e));
        } finally {
            database.close();
            helper.close();
        }
        return status;

    }

}
