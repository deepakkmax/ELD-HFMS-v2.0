package com.hutchsystems.hutchconnect.db;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.beans.CardBean;
import com.hutchsystems.hutchconnect.beans.FuelDetailBean;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.DocumentType;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by SAMSUNG on 30-01-2017.
 */

public class FuelDetailDB {

    public static ArrayList<CardBean> CardGet() {
        ArrayList<CardBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select CardId, CardNo from "
                            + MySQLiteOpenHelper.TABLE_CARD_DETAIL + "  order by CardNo"
                    , null);
            while (cursor.moveToNext()) {
                CardBean bean = new CardBean();
                bean.setCardId(cursor.getInt(0));
                bean.setCardNo(cursor.getString(1));
                list.add(bean);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(TrailerDB.class.getName() + "::CardGet Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(FuelDetailDB.class.getName(),"::CardGet Error:" ,e.getMessage(),Utility.printStackTrace(e));
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
    // Created Date: 30 January 2017
    // Purpose: update FuelDetail for web sync
    public static JSONArray FuelDetailSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_FUEL_DETAIL, values,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {

            LogFile.write(FuelDetailDB.class.getName() + "::FuelDetailSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(FuelDetailDB.class.getName(),"::FuelDetailSyncUpdate Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

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
    // Created Date: 30 January 2017
    // Purpose: get Fuel Detail for web sync
    public static JSONArray getFuelDetailSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id, CardId , VehicleId , DriverId , Duration , FuelDateTime , Quantity , Price , DEFFuelQuantity , DEFFuelPrice , ReferFuelQuantity , ReferFuelPrice , CashAdvance , Images ,ModifiedBy , ModifiedDate, Latitude, Longitude, Location,trailerNo,FuelUnit from "
                            + MySQLiteOpenHelper.TABLE_FUEL_DETAIL + " Where SyncFg=0"
                    , null);

            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("VehicleId", cursor.getInt(cursor.getColumnIndex("VehicleId")));
                obj.put("DriverId", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("Duration", cursor.getInt(cursor.getColumnIndex("Duration")));
                obj.put("Quantity", cursor.getString(cursor.getColumnIndex("Quantity")));
                obj.put("DEFFuelQuantity", cursor.getString(cursor.getColumnIndex("DEFFuelQuantity")));
                obj.put("ReferFuelQuantity", cursor.getString(cursor.getColumnIndex("ReferFuelQuantity")));

                obj.put("FuelDateTime", cursor.getString(cursor.getColumnIndex("FuelDateTime")));
                obj.put("Price", cursor.getString(cursor.getColumnIndex("Price")));
                obj.put("DEFFuelPrice", cursor.getString(cursor.getColumnIndex("DEFFuelPrice")));
                obj.put("ReferFuelPrice", cursor.getString(cursor.getColumnIndex("ReferFuelPrice")));
                obj.put("CashAdvance", cursor.getString(cursor.getColumnIndex("CashAdvance")));

                obj.put("Latitude", cursor.getString(cursor.getColumnIndex("Latitude")));
                obj.put("Longitude", cursor.getString(cursor.getColumnIndex("Longitude")));
                obj.put("Location", cursor.getString(cursor.getColumnIndex("Location")));

                obj.put("CompanyId", Utility.companyId);
                obj.put("ModifiedBy", cursor.getInt(cursor.getColumnIndex("ModifiedBy")));
                obj.put("ModifiedDate", cursor.getString(cursor.getColumnIndex("ModifiedDate")));
                obj.put("trailerNo", cursor.getString(cursor.getColumnIndex("trailerNo")));
                obj.put("cardId", cursor.getString(cursor.getColumnIndex("CardId")));
                obj.put("Images", cursor.getString(cursor.getColumnIndex("Images")));
                obj.put("FuelUnit", cursor.getInt(cursor.getColumnIndex("FuelUnit")));

                array.put(obj);
            }

        } catch (Exception e) {
            LogFile.write(FuelDetailDB.class.getName() + "::getFuelDetailSync Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(FuelDetailDB.class.getName(),"::getFuelDetailSync Error:" ,e.getMessage(),Utility.printStackTrace(e));

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
    // Created Date: 30 January 2017
    // Purpose: get Fuel Detail for web sync
    public static ArrayList<FuelDetailBean> getFuelDetail(String date) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<FuelDetailBean> list = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id ,fd.CardId ,cd.CardNo, fd.VehicleId , DriverId , Duration , FuelDateTime , Quantity , Price , DEFFuelQuantity , DEFFuelPrice , ReferFuelQuantity , ReferFuelPrice , CashAdvance , Images, Latitude, Longitude, Location,FuelUnit  from "
                            + MySQLiteOpenHelper.TABLE_FUEL_DETAIL
                            + " fd inner join "
                            + MySQLiteOpenHelper.TABLE_CARD_DETAIL
                            + " cd on fd.CardId=cd.CardId "
                            + " Where fd.FuelDateTime>=? order by _id desc"
                    , new String[]{date});

            while (cursor.moveToNext()) {
                FuelDetailBean bean = new FuelDetailBean();
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setCardId(cursor.getInt(cursor.getColumnIndex("CardId")));
                bean.setVehicleId(cursor.getInt(cursor.getColumnIndex("VehicleId")));
                bean.setDriverId(cursor.getInt(cursor.getColumnIndex("DriverId")));
                bean.setDuration(cursor.getInt(cursor.getColumnIndex("Duration")));

                bean.setCardNo(cursor.getString(cursor.getColumnIndex("CardNo")));
                bean.setQuantity(cursor.getString(cursor.getColumnIndex("Quantity")));
                bean.setDEFFuelQuantity(cursor.getString(cursor.getColumnIndex("DEFFuelQuantity")));
                bean.setReferFuelQuantity(cursor.getString(cursor.getColumnIndex("ReferFuelQuantity")));
                bean.setFuelDateTime(cursor.getString(cursor.getColumnIndex("FuelDateTime")));
                bean.setPrice(cursor.getString(cursor.getColumnIndex("Price")));
                bean.setDEFFuelPrice(cursor.getString(cursor.getColumnIndex("DEFFuelPrice")));
                bean.setReferFuelPrice(cursor.getString(cursor.getColumnIndex("ReferFuelPrice")));
                bean.setCashAdvance(cursor.getString(cursor.getColumnIndex("CashAdvance")));
                bean.setImages(cursor.getString(cursor.getColumnIndex("Images")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocation(cursor.getString(cursor.getColumnIndex("Location")));
                bean.setFuelUnit(cursor.getInt(cursor.getColumnIndex("FuelUnit")));
                list.add(bean);
            }

        } catch (Exception e) {
            LogFile.write(FuelDetailDB.class.getName() + "::getFuelDetail Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(FuelDetailDB.class.getName(),"::getFuelDetail Error:" ,e.getMessage(),Utility.printStackTrace(e));
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

            cursor = database.rawQuery("select  Images from "
                            + MySQLiteOpenHelper.TABLE_FUEL_DETAIL
                            + " Where FuelDateTime < ?"
                    , new String[]{date});

            while (cursor.moveToNext()) {
                String fileName = cursor.getString(cursor.getColumnIndex("Images"));
                Utility.DeleteFile(DocumentType.DOCUMENT_FUEL, fileName);

            }

        } catch (Exception e) {
            LogFile.write(FuelDetailDB.class.getName() + "::deleteDocument Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(FuelDetailDB.class.getName(),"::deleteDocument Error:" ,e.getMessage(),Utility.printStackTrace(e));
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
    // Created Date: 30 January 2017
    // Purpose: get Fuel Detail by id
    public static FuelDetailBean getFuelDetailById(int id) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        FuelDetailBean bean = new FuelDetailBean();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select _id , VehicleId , DriverId , Duration , FuelDateTime , Quantity , Price , DEFFuelQuantity , DEFFuelPrice , ReferFuelQuantity , ReferFuelPrice , CashAdvance,trailerNo , Images,CardId,FuelUnit  from "
                            + MySQLiteOpenHelper.TABLE_FUEL_DETAIL + " Where _id=?"
                    , new String[]{id + ""});

            if (cursor.moveToNext()) {
                bean.set_id(id);
                bean.setVehicleId(cursor.getInt(cursor.getColumnIndex("VehicleId")));
                bean.setDriverId(cursor.getInt(cursor.getColumnIndex("DriverId")));
                bean.setDuration(cursor.getInt(cursor.getColumnIndex("Duration")));
                bean.setCardId(cursor.getInt(cursor.getColumnIndex("CardId")));

                bean.setQuantity(cursor.getString(cursor.getColumnIndex("Quantity")));
                bean.setDEFFuelQuantity(cursor.getString(cursor.getColumnIndex("DEFFuelQuantity")));
                bean.setReferFuelQuantity(cursor.getString(cursor.getColumnIndex("ReferFuelQuantity")));
                bean.setFuelDateTime(cursor.getString(cursor.getColumnIndex("FuelDateTime")));
                bean.setPrice(cursor.getString(cursor.getColumnIndex("Price")));
                bean.setDEFFuelPrice(cursor.getString(cursor.getColumnIndex("DEFFuelPrice")));
                bean.setReferFuelPrice(cursor.getString(cursor.getColumnIndex("ReferFuelPrice")));
                bean.setCashAdvance(cursor.getString(cursor.getColumnIndex("CashAdvance")));
                bean.setImages(cursor.getString(cursor.getColumnIndex("Images")));

                bean.setTrailerNo(cursor.getString(cursor.getColumnIndex("trailerNo")));
                bean.setFuelUnit(cursor.getInt(cursor.getColumnIndex("FuelUnit")));
            }

        } catch (Exception e) {
            LogFile.write(FuelDetailDB.class.getName() + "::getFuelDetailById Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(FuelDetailDB.class.getName(),"::getFuelDetailById Error:" ,e.getMessage(),Utility.printStackTrace(e));

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
    // Purpose: Save Fuel Detail
    public static boolean Save(FuelDetailBean bean) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("FuelUnit", bean.getFuelUnit());
            values.put("Quantity", bean.getQuantity());
            values.put("DEFFuelQuantity", bean.getDEFFuelQuantity());
            values.put("ReferFuelQuantity", bean.getReferFuelQuantity());
            values.put("Price", bean.getPrice());
            values.put("DEFFuelPrice", bean.getDEFFuelPrice());
            values.put("ReferFuelPrice", bean.getReferFuelPrice());
            values.put("CashAdvance", bean.getCashAdvance());
            values.put("SyncFg", 0);
            values.put("ModifiedBy", Utility.onScreenUserId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            values.put("CardId", bean.getCardId());

            if (bean.get_id() == 0) {
                values.put("FuelDateTime", bean.getFuelDateTime());
                values.put("VehicleId", bean.getVehicleId());
                values.put("DriverId", bean.getDriverId());
                values.put("Duration", bean.getDuration());
                values.put("Images", bean.getImages());
                values.put("Latitude", Utility.currentLocation.getLatitude());
                values.put("Longitude", Utility.currentLocation.getLongitude());
                values.put("Location", Utility.currentLocation.getLocationDescription());
                values.put("trailerNo", bean.getTrailerNo());

                long id = database.insert(MySQLiteOpenHelper.TABLE_FUEL_DETAIL,
                        "_id", values);
                Log.i("FuelId", id + "");
            } else {
                database.update(MySQLiteOpenHelper.TABLE_FUEL_DETAIL, values, "_id=?", new String[]{bean.get_id() + ""});
            }
            // Post Fuel details
            MainActivity.postData(CommonTask.Post_FuelDetail);
        } catch (Exception e) {
            status = false;
            LogFile.write(VehicleInfoDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(FuelDetailDB.class.getName(),"::Save Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            database.close();
            helper.close();
        }
        return status;

    }

    // Created By: Deepak Sharma
    // Created Date: 28 December 2016
    // Purpose: add or update vehicles in database
    public static boolean Save(ArrayList<CardBean> lst) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            for (int i = 0; i < lst.size(); i++) {
                CardBean bean = lst.get(i);
                values.put("VehicleId", bean.getVehicleId());
                values.put("CardId", bean.getCardId());
                values.put("CardNo", bean.getCardNo());
                Cursor cursor = database.rawQuery("select CardId from "
                        + MySQLiteOpenHelper.TABLE_CARD_DETAIL
                        + " where CardId=?", new String[]{bean.getCardId() + ""});

                if (cursor.moveToNext()) {
                    database.update(MySQLiteOpenHelper.TABLE_CARD_DETAIL, values,
                            " CardId= ?",
                            new String[]{bean.getCardId() + ""});


                } else {
                    database.insert(MySQLiteOpenHelper.TABLE_CARD_DETAIL,
                            null, values);
                }
                cursor.close();
            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(VehicleDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(FuelDetailDB.class.getName(),"::Save Error:" ,e.getMessage(),Utility.printStackTrace(e));
        } finally {
            database.close();
            helper.close();
        }
        return status;
    }
}
