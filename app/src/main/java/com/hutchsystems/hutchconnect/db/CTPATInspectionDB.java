package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.beans.CTPATInspectionBean;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class CTPATInspectionDB {

    // Created By: Pallavi Wattamwar
    // Created Date: 3 June 2019
    // Purpose: Create CTPAT Inspection
    public static CTPATInspectionBean CreateCTPATInspection(String dateTime, int driverId, String driverName, int type, String truckNumber, String trailerNumber, String comments,String SealValue,int companyId,String companyName,String inspectionCriteria) {
        CTPATInspectionBean bean = new CTPATInspectionBean();
        bean.setDateTime(dateTime);
        bean.setDriverId(driverId);
        bean.setDriverName(driverName);
        bean.setType(type);
        bean.setTruckNumber(truckNumber);
        bean.setTrailer(trailerNumber);
        bean.setComments(comments);
        bean.setCompanyId(companyId);
        bean.setCompanyName(companyName);
        bean.setInspectionCriteria(inspectionCriteria);
        bean.setSealValue(SealValue);
        bean.setSyncFg(0);


        Save(bean);
        return bean;

    }
    // Created By: Pallavi Wattamwar
    // Created Date: 3 June 2019
    // Purpose: Save CTPAT Inspection
    public static void Save(CTPATInspectionBean bean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        int tripInspectionId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("DateTime", bean.getDateTime());
            values.put("DriverId", bean.getDriverId());
            values.put("DriverName", bean.getDriverName());
            values.put("Type", bean.getType());
            values.put("TruckNumber", bean.getTruckNumber());
            values.put("TrailerNumber", bean.getTrailer());
            values.put("Comments", bean.getComments());
            values.put("CompanyID",bean.getCompanyId());
            values.put("CompanyName",bean.getCompanyName());
            values.put("InspectionCriteria",bean.getInspectionCriteria());
            values.put("SyncFg", bean.getSyncFg());
            values.put("SealValue",bean.getSealValue());
            values.put("StatusId",1);

            tripInspectionId = (int) database.insertOrThrow(MySQLiteOpenHelper.TABLE_CTPAT_INSPECTION,
                    "_id", values);

            MainActivity.postData(CommonTask.POST_CTPAT);

            Log.e(CTPATInspectionDB.class.getName(), "Saved " + tripInspectionId);
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            Log.e(CTPATInspectionDB.class.getName(), "::Save Error:" + e.getMessage());
            LogFile.write(CTPATInspectionDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(CTPATInspectionDB.class.getName(),"::Save Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogDB.writeLogs(CTPATInspectionDB.class.getName(),"::Save Error:" ,e.getMessage(),Utility.printStackTrace(e));

                LogFile.write(CTPATInspectionDB.class.getName() + "::Save close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
    }


    // Created By: Pallavi Wattamwar
    // Created Date: 3 June 2019
    // Purpose: Save CTPAT Inspection
    public static void Save(ArrayList<CTPATInspectionBean> list) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        int tripInspectionId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            for (CTPATInspectionBean bean : list) {
                int id = 0;
                ContentValues values = new ContentValues();

                Cursor cursor = database.rawQuery("select _id from " + MySQLiteOpenHelper.TABLE_CTPAT_INSPECTION +
                                " where DateTime=? and DriverId=? LIMIT 1 "

                        , new String[]{bean.getDateTime(), bean.getDriverId() + ""});

                if (cursor.moveToNext()) {
                    id = cursor.getInt(0);
                }

                values.put("DateTime", bean.getDateTime());
                values.put("DriverId", bean.getDriverId());
                values.put("DriverName", bean.getDriverName());
                values.put("Type", bean.getType());
                values.put("TruckNumber", bean.getTruckNumber());
                values.put("TrailerNumber", bean.getTrailer());
                values.put("Comments", bean.getComments());
                values.put("CompanyID", bean.getCompanyId());
                values.put("CompanyName", bean.getCompanyName());
                values.put("InspectionCriteria", bean.getInspectionCriteria());
                values.put("SyncFg", 1);
                values.put("SealValue", bean.getSealValue());
                values.put("StatusId", 1);

                // if DVIR does not exist in database then insert else update
                if (id == 0) {

                    database.insertOrThrow(MySQLiteOpenHelper.TABLE_CTPAT_INSPECTION,
                            "_id", values);
                } else {
                    database.update(MySQLiteOpenHelper.TABLE_CTPAT_INSPECTION, values,
                            " _id= ?", new String[]{id
                                    + ""});
                }
            }

            Log.e(CTPATInspectionDB.class.getName(), "Saved " + tripInspectionId);
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            Log.e(CTPATInspectionDB.class.getName(), "::Save Error:" + e.getMessage());
            LogFile.write(CTPATInspectionDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(CTPATInspectionDB.class.getName(),"::Save Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogDB.writeLogs(CTPATInspectionDB.class.getName(),"::Save Error:" ,e.getMessage(),Utility.printStackTrace(e));

                LogFile.write(CTPATInspectionDB.class.getName() + "::Save close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 5 June 2019
    // Purpose: get CTPAT Inspection
    public static ArrayList<CTPATInspectionBean> getInspections(String date) {
        ArrayList<CTPATInspectionBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select * from " +
                    MySQLiteOpenHelper.TABLE_CTPAT_INSPECTION + " where DateTime>=? and StatusId=1 order by DateTime desc", new String[]{date});
            while (cursor.moveToNext()) {
                CTPATInspectionBean bean = new CTPATInspectionBean();
                bean.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setDateTime(cursor.getString(cursor.getColumnIndex("DateTime")));
                bean.setDriverId(cursor.getInt(cursor.getColumnIndex("DriverId")));
                bean.setDriverName(cursor.getString(cursor.getColumnIndex("DriverName")));
                bean.setType(cursor.getInt(cursor.getColumnIndex("Type")));
                bean.setTruckNumber(cursor.getString(cursor.getColumnIndex("TruckNumber")));
                bean.setTrailer(cursor.getString(cursor.getColumnIndex("TrailerNumber")));
                bean.setComments(cursor.getString(cursor.getColumnIndex("Comments")));
                bean.setSyncFg(cursor.getInt(cursor.getColumnIndex("SyncFg")));
                bean.setCompanyName(cursor.getString(cursor.getColumnIndex("CompanyName")));
                bean.setCompanyId(cursor.getInt(cursor.getColumnIndex("CompanyID")));
                bean.setInspectionCriteria(cursor.getString(cursor.getColumnIndex("InspectionCriteria")));
                bean.setSealValue(cursor.getString(cursor.getColumnIndex("SealValue")));

                list.add(bean);
            }

        } catch (Exception e) {
            LogFile.write(CTPATInspectionDB.class.getName() + "::getInspections Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(CTPATInspectionDB.class.getName(),"::getDVIRSync Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                LogFile.write(CTPATInspectionDB.class.getName() + "::getInspections close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return list;
    }
    // Created By: Pallavi Wattamwar
    // Created Date: 4 June 2019
    // Purpose: Post CTPAT Inspection to the database
    public static JSONArray getCTPATInspectionSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select * from " + MySQLiteOpenHelper.TABLE_CTPAT_INSPECTION +
                            " where SyncFg=0"
                    , null);
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                int inspectionId = cursor.getInt(0);
                obj.put("InspectionId", inspectionId);
                obj.put("InspectionDateTime", Utility.getDateTimeForServer(cursor.getString(cursor.getColumnIndex("DateTime"))));
                obj.put("UserId", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("InspectionType", cursor.getInt(cursor.getColumnIndex("Type")));
                obj.put("TruckNumber", cursor.getString(cursor.getColumnIndex("TruckNumber")));
                obj.put("TrailerNumber", cursor.getString(cursor.getColumnIndex("TrailerNumber")));
                obj.put("Comments", cursor.getString(cursor.getColumnIndex("Comments")));
                obj.put("CompanyId", Utility.companyId);
                obj.put("CreatedBy", Utility.activeUserId);
                obj.put("CreatedDate", Utility.getDateTimeForServer(cursor.getString(cursor.getColumnIndex("DateTime"))));
                obj.put("InspectionCriteria", cursor.getString(cursor.getColumnIndex("InspectionCriteria")));
                obj.put("SealValue", cursor.getString(cursor.getColumnIndex("SealValue")));
                obj.put("StatusId", cursor.getInt(cursor.getColumnIndex("StatusId")));


                array.put(obj);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(CTPATInspectionDB.class.getName() + "::getCTPATInspectionSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(CTPATInspectionDB.class.getName(),"::getCTPATInspectionSync Error:" ,exe.getMessage(),Utility.printStackTrace(exe));
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
    public static JSONArray CTPATSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_CTPAT_INSPECTION, values,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(CTPATInspectionDB.class.getName() + "::CTPATSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(CTPATInspectionDB.class.getName(),"::CTPATSyncUpdate Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

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
    // Created Date: 5 June 2019
    // Purpose: remove CTPAT Inspection
    public static void removeInspection() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        String previousDate = Utility.getPreviousDateOnly(-1);
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_CTPAT_INSPECTION + " where DateTime<? and SyncFg=1 order by DateTime desc", new String[]{previousDate});
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                database.delete(MySQLiteOpenHelper.TABLE_CTPAT_INSPECTION,
                        "_id=?", new String[]{""+id});
            }

        } catch (Exception e) {
            LogFile.write(CTPATInspectionDB.class.getName() + "::getInspections Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(CTPATInspectionDB.class.getName(),"::getInspections Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                LogFile.write(CTPATInspectionDB.class.getName() + "::getInspections close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(CTPATInspectionDB.class.getName(),"::getInspections Error:" ,e.getMessage(),Utility.printStackTrace(e));

            }
        }
    }

}
