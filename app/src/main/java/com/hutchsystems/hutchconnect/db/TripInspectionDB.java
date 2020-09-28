package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.beans.TripInspectionBean;
import com.hutchsystems.hutchconnect.common.BitmapUtility;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class TripInspectionDB {

    public static TripInspectionBean CreateTripInspection(String dateTime, int driverId, String driverName, int type, int defect, int defectRepaired, int safeToDrive, String defectItems, String latitude, String longitude,
                                                          String location, String odometer, String truckNumber, String trailerNumber, String comments, String pictures,int TrailerDvirFg) {
        TripInspectionBean bean = new TripInspectionBean();
        bean.setInspectionDateTime(dateTime);
        bean.setDriverId(driverId);
        bean.setDriverName(driverName);
        bean.setType(type);
        bean.setDefect(defect);
        bean.setDefectRepaired(defectRepaired);
        bean.setSafeToDrive(safeToDrive);
        bean.setDefectItems(defectItems);
        bean.setLatitude(latitude);
        bean.setLongitude(longitude);
        bean.setLocation(location);
        bean.setOdometerReading(odometer);
        bean.setTruckNumber(truckNumber);
        bean.setTrailerNumber(trailerNumber);
        bean.setComments(comments);
        bean.setPictures(pictures);
        bean.setSyncFg(0);
        // Flag to check dvir is trailer or truck
        bean.setTrailerDvirFg(TrailerDvirFg);

        Save(bean);
        return bean;

    }

    public static void Save(TripInspectionBean bean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        int tripInspectionId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("DateTime", bean.getInspectionDateTime());
            values.put("DriverId", bean.getDriverId());
            values.put("DriverName", bean.getDriverName());
            values.put("Type", bean.getType());
            values.put("Defect", bean.getDefect());
            values.put("DefectRepaired", bean.getDefectRepaired());
            values.put("SafeToDrive", bean.getSafeToDrive());
            values.put("DefectItems", bean.getDefectItems());
            values.put("Latitude", bean.getLatitude());
            values.put("Longitude", bean.getLongitude());
            values.put("LocationDescription", bean.getLocation());
            values.put("Odometer", bean.getOdometerReading());
            values.put("TruckNumber", bean.getTruckNumber());
            values.put("TrailerNumber", bean.getTrailerNumber());
            values.put("Comments", bean.getComments());
            values.put("Pictures", bean.getPictures());
            values.put("SyncFg", bean.getSyncFg());
            values.put("StatusId",1);
            values.put("TrailerDvirFg",bean.getTrailerDvirFg());

            tripInspectionId = (int) database.insertOrThrow(MySQLiteOpenHelper.TABLE_TRIP_INSPECTION,
                    "_id", values);

            MainActivity.postData(CommonTask.Post_Dvir);
            Log.e(TripInspectionDB.class.getName(), "Saved " + tripInspectionId);
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            Log.e(TripInspectionDB.class.getName(), "::Save Error:" + e.getMessage());
            LogFile.write(TripInspectionDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TripInspectionDB.class.getName(),"::Save Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogDB.writeLogs(TripInspectionDB.class.getName(),"::Save Error:" ,e.getMessage(),Utility.printStackTrace(e));

                LogFile.write(TripInspectionDB.class.getName() + "::Save close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
    }


    // Created By: Deepak Sharma
    // Created Date: 10 April 2019
    // Purpose: save trip inspection from web service
    public static boolean Save(ArrayList<TripInspectionBean> list) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        boolean status = true;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            for (TripInspectionBean bean : list) {
                int id = 0;
                ContentValues values = new ContentValues();

                Cursor cursor = database.rawQuery("select _id from " + MySQLiteOpenHelper.TABLE_TRIP_INSPECTION +
                                " where DateTime=? and DriverId=? LIMIT 1 "

                        , new String[]{bean.getInspectionDateTime(), bean.getDriverId() + ""});

                if (cursor.moveToNext()) {
                    id = cursor.getInt(0);
                }

                values.put("DateTime", bean.getInspectionDateTime());
                values.put("DriverId", bean.getDriverId());
                values.put("DriverName", bean.getDriverName());
                values.put("Type", bean.getType());
                values.put("Defect", bean.getDefect());
                values.put("DefectRepaired", bean.getDefectRepaired());
                values.put("SafeToDrive", bean.getSafeToDrive());
                values.put("DefectItems", bean.getDefectItems());
                values.put("Latitude", bean.getLatitude());
                values.put("Longitude", bean.getLongitude());
                values.put("LocationDescription", bean.getLocation());
                values.put("Odometer", bean.getOdometerReading());
                values.put("TruckNumber", bean.getTruckNumber());
                values.put("TrailerNumber", bean.getTrailerNumber());
                values.put("Comments", bean.getComments());
                values.put("Pictures", bean.getPictures());
                values.put("SyncFg", 1);
                values.put("StatusId", 1);

                // if DVIR does not exist in database then insert else update
                if (id == 0) {

                    database.insertOrThrow(MySQLiteOpenHelper.TABLE_TRIP_INSPECTION,
                            "_id", values);
                } else {
                    database.update(MySQLiteOpenHelper.TABLE_TRIP_INSPECTION, values,
                            " _id= ?", new String[]{id
                                    + ""});
                }

                cursor.close();
            }


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            Log.e(TripInspectionDB.class.getName(), "::Save Error:" + e.getMessage());
            LogFile.write(TripInspectionDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TripInspectionDB.class.getName(), "::Save Error:", e.getMessage(), Utility.printStackTrace(e));
            status = false;
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogDB.writeLogs(TripInspectionDB.class.getName(), "::Save Error:", e.getMessage(), Utility.printStackTrace(e));

                LogFile.write(TripInspectionDB.class.getName() + "::Save close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return status;
    }


    public static ArrayList<TripInspectionBean> getInspections(String date) {
        ArrayList<TripInspectionBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id, DateTime, DriverId, DriverName, Type, Defect, DefectRepaired, SafeToDrive, DefectItems, Latitude, Longitude, " +
                    "LocationDescription, Odometer, TruckNumber, TrailerNumber, Comments, Pictures,SyncFg,TrailerDvirFg from " +
                    MySQLiteOpenHelper.TABLE_TRIP_INSPECTION + " where DateTime>=? and StatusId=1 order by DateTime desc", new String[]{date});
            while (cursor.moveToNext()) {
                TripInspectionBean bean = new TripInspectionBean();
                bean.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setInspectionDateTime(cursor.getString(cursor.getColumnIndex("DateTime")));
                bean.setDriverId(cursor.getInt(cursor.getColumnIndex("DriverId")));
                bean.setDriverName(cursor.getString(cursor.getColumnIndex("DriverName")));
                bean.setType(cursor.getInt(cursor.getColumnIndex("Type")));
                bean.setDefect(cursor.getInt(cursor.getColumnIndex("Defect")));
                bean.setDefectRepaired(cursor.getInt(cursor.getColumnIndex("DefectRepaired")));
                bean.setSafeToDrive(cursor.getInt(cursor.getColumnIndex("SafeToDrive")));
                bean.setDefectItems(cursor.getString(cursor.getColumnIndex("DefectItems")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocation(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("Odometer")));
                bean.setTruckNumber(cursor.getString(cursor.getColumnIndex("TruckNumber")));
                bean.setTrailerNumber(cursor.getString(cursor.getColumnIndex("TrailerNumber")));
                bean.setComments(cursor.getString(cursor.getColumnIndex("Comments")));
                bean.setPictures(cursor.getString(cursor.getColumnIndex("Pictures")));
                bean.setSyncFg(cursor.getInt(cursor.getColumnIndex("SyncFg")));
                bean.setTrailerDvirFg(cursor.getInt(cursor.getColumnIndex("TrailerDvirFg")));

                list.add(bean);
            }

        } catch (Exception e) {
            LogFile.write(TripInspectionDB.class.getName() + "::getInspections Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TripInspectionDB.class.getName(),"::getDVIRSync Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                LogFile.write(TripInspectionDB.class.getName() + "::getInspections close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return list;
    }

    public static long removeDVIR(String id) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        long res = -1;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String[] ids = id.split(",");
            for (String _id : ids) {
                res = database.delete(MySQLiteOpenHelper.TABLE_TRIP_INSPECTION,
                        "_id=?", new String[]{_id});
            }

        } catch (Exception e) {
            System.out.println("removeDVIR");
            e.printStackTrace();
            LogFile.write(TrackingDB.class.getName() + "::removeDVIR Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TripInspectionDB.class.getName(),"::removeDVIR Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return res;
    }


    // Created By: Pallavi Wattamwar
    // Created Date: 16 January 2018
    // Purpose: Delete DVIR / Update Status = 3

    public static void DeleteDVIR(int id) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            // Status == 1 means active
            // Status == 2 means inactive
            // Status == 3 means delete

            values.put("StatusId", 3);
            values.put("SyncFg",0);

            database.update(MySQLiteOpenHelper.TABLE_TRIP_INSPECTION, values,
                    " _id= ?",
                    new String[]{""+id});
        } catch (Exception e) {

            Utility.printError(e.getMessage());
            LogFile.write(CarrierInfoDB.class.getName() + ":: Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(NotificationDB.class.getName(),"::DeleteNotification:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            database.close();
            helper.close();
        }


    }

    public static ArrayList<TripInspectionBean> getInspectionsToRemove(String date) {
        ArrayList<TripInspectionBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,Pictures from " +
                    MySQLiteOpenHelper.TABLE_TRIP_INSPECTION + " where DateTime<? and SyncFg=1 order by DateTime desc", new String[]{date});
            while (cursor.moveToNext()) {
                TripInspectionBean bean = new TripInspectionBean();
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String pictures = cursor.getString(cursor.getColumnIndex("Pictures"));
                bean.setId(id);
                bean.setPictures(pictures);
                list.add(bean);
            }

        } catch (Exception e) {
            LogFile.write(TripInspectionDB.class.getName() + "::getInspections Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                LogFile.write(TripInspectionDB.class.getName() + "::getInspections close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return list;
    }


    public static boolean getInspections(String date, int driverId) {
        boolean status = false;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_TRIP_INSPECTION + " where DateTime>=? and driverId=?  and StatusId=1 order by DateTime desc Limit 1", new String[]{date, driverId + ""});
            if (cursor.moveToNext()) {
                status = true;
            }

        } catch (Exception e) {
            LogFile.write(TripInspectionDB.class.getName() + "::getInspections Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TripInspectionDB.class.getName(),"::getDVIRSync Error:" ,e.getMessage(),Utility.printStackTrace(e));
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                LogFile.write(TripInspectionDB.class.getName() + "::getInspections close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return status;
    }


    public static boolean getInspections(String date, int driverId, int driverId2) {
        boolean status = false;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_TRIP_INSPECTION + " where DateTime>=? and (driverId=? or driverId=?) order by DateTime desc Limit 1", new String[]{date, driverId + "", driverId2 + ""});
            if (cursor.moveToNext()) {
                status = true;
            }

        } catch (Exception e) {
            LogFile.write(TripInspectionDB.class.getName() + "::getInspections Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TripInspectionDB.class.getName(),"::getInspections Error:" ,e.getMessage(),Utility.printStackTrace(e));
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                LogFile.write(TripInspectionDB.class.getName() + "::getInspections close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 27 July 2016
    // Purpose: get DVIR for web sync
    public static JSONArray getDVIRSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id, DateTime , DriverId , DriverName , Type , Defect , DefectRepaired , SafeToDrive , DefectItems , Latitude " +
                            ", Longitude , LocationDescription , Odometer , TruckNumber , TrailerNumber , Comments , Pictures ,StatusId,SyncFg from " + MySQLiteOpenHelper.TABLE_TRIP_INSPECTION +
                            " where SyncFg=0"
                    , null);
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                int inspectionId = cursor.getInt(0);
                obj.put("InspectionId", inspectionId);
                obj.put("InspectionDateTime", cursor.getString(cursor.getColumnIndex("DateTime")));

                obj.put("UserId", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("InspectionType", cursor.getInt(cursor.getColumnIndex("Type")));
                obj.put("DefectFg", cursor.getInt(cursor.getColumnIndex("Defect")));
                obj.put("RepairedFg", cursor.getInt(cursor.getColumnIndex("DefectRepaired")));
                obj.put("SafeToDriveFg", cursor.getInt(cursor.getColumnIndex("SafeToDrive")));
                obj.put("DefectItems", cursor.getString(cursor.getColumnIndex("DefectItems")));
                obj.put("Latitude", cursor.getString(cursor.getColumnIndex("Latitude")));
                obj.put("Longitude", cursor.getString(cursor.getColumnIndex("Longitude")));
                obj.put("LocationDescription", cursor.getString(cursor.getColumnIndex("LocationDescription")));
                double odometer = 0d;
                try {
                    odometer = Double.parseDouble(cursor.getString(cursor.getColumnIndex("Odometer")));
                    JSONArray imgArray = new JSONArray();
                    String[] images = cursor.getString(cursor.getColumnIndex("Pictures")).split(",");
                    for (String img : images) {
                        String imageContent = BitmapUtility.convertToBase64String(img);
                        JSONObject objImage = new JSONObject();
                        objImage.put("InspectionId", inspectionId);
                        objImage.put("ImageContent", imageContent);
                        imgArray.put(objImage);
                        // break;
                    }
                    obj.put("tripImages", imgArray);

                } catch (Exception exe) {
                }

                obj.put("OdometerReading", odometer);
                obj.put("TruckNumber", cursor.getString(cursor.getColumnIndex("TruckNumber")));
                obj.put("TrailerNumber", cursor.getString(cursor.getColumnIndex("TrailerNumber")));
                obj.put("Comments", cursor.getString(cursor.getColumnIndex("Comments")));
                obj.put("CompanyId", Utility.companyId);
                obj.put("CreatedBy", Utility.activeUserId);
                obj.put("CreatedDate", cursor.getString(cursor.getColumnIndex("DateTime")));
                obj.put("StatusId", cursor.getInt(cursor.getColumnIndex("StatusId")));
                obj.put("VehicleId", Utility.vehicleId);
                // obj.put("tripImages", cursor.getString(cursor.getColumnIndex("")));

                array.put(obj);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(TripInspectionDB.class.getName() + "::getDVIRSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TripInspectionDB.class.getName(),"::getDVIRSync Error:" ,exe.getMessage(),Utility.printStackTrace(exe));
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


    // Created By: Deepak Sharma
    // Created Date: 27 July 2016
    // Purpose: update DVIR for web sync
    public static JSONArray DVIRSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();
        int inspectionId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_TRIP_INSPECTION, values,
                    " SyncFg=?", new String[]{"0"});
            /*cursor = database.rawQuery("select _id from " + MySQLiteOpenHelper.TABLE_TRIP_INSPECTION +
                            " where SyncFg=0 order by _id Limit 1"
                    , null);
            if (cursor.moveToNext()) {
                inspectionId = cursor.getInt(0);
                ContentValues values = new ContentValues();
                values.put("SyncFg", 1);
                database.update(MySQLiteOpenHelper.TABLE_TRIP_INSPECTION, values,
                        " _id=?", new String[]{inspectionId + ""});
            }*/
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(TripInspectionDB.class.getName() + "::getDVIRSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TripInspectionDB.class.getName(),"::getDVIRSync Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

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
