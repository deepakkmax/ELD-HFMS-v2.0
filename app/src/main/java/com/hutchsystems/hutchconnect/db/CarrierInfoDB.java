package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.beans.CarrierInfoBean;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.common.ZoneList;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Deepak.Sharma on 1/14/2016.
 */
public class CarrierInfoDB {

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: get company info
    public static void getCompanyInfo() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select VehicleId,CompanyId,CarrierName,ELDManufacturer,USDOT,UnitNo,VIN, MACAddress,PlateNo,TimeZoneId,CanBusFg,StartOdometerReading,EldConfig,Protocol,OdometerSource,SetupFg,StatusId,ReferralCode from "
                            + MySQLiteOpenHelper.TABLE_CARRIER + " Where SerialNo=? LIMIT 1"
                    , new String[]{Utility.IMEI}
            );
            if (cursor.moveToFirst()) {
                Utility.packageId = Utility.getPreferences("packageid", 1);
                Utility.Features = Utility.getPreferences("features", "1,2");
                Utility.vehicleId = cursor.getInt(0);
                Utility.companyId = cursor.getInt(1);
                Utility.CarrierName = cursor.getString(2);
                Utility.ELDManufacturer = cursor.getString(3);
                Utility.USDOT = cursor.getString(4);
                Utility.UnitNo = cursor.getString(5);
                Utility.VIN = cursor.getString(6);
                Utility.MACAddress = cursor.getString(7);
                Utility.PlateNo = cursor.getString(8);

                Utility.ReferralCode = cursor.getString(cursor.getColumnIndex("ReferralCode"));

                if (Utility.activeUserId == 0) {
                    Utility.TimeZoneId = cursor.getString(9);
                    Utility.TimeZoneOffset = ZoneList.getOffset(Utility.TimeZoneId);
                    Utility.TimeZoneOffsetUTC = ZoneList.getTimeZoneOffset(Utility.TimeZoneId);
                    Utility.sdf.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
                }

                Utility.canBusFg = cursor.getInt(cursor.getColumnIndex("CanBusFg"));
                Utility.StartOdometerReading = Double.parseDouble(Utility.getPreferences("start_odometer_reading", "0"));


                Utility.FullAddress = Utility.getPreferences("full_address", "");
                Utility.EldConfig = cursor.getString(cursor.getColumnIndex("EldConfig"));

                Utility.SetupFg = cursor.getInt(cursor.getColumnIndex("SetupFg")) == 1;

                CanMessages.protocol = cursor.getString(cursor.getColumnIndex("Protocol"));
                CanMessages.odometerSource = cursor.getInt(cursor.getColumnIndex("OdometerSource"));

                if (CanMessages.protocol == null) {
                    CanMessages.protocol = Utility.getPreferences("protocol_supported", "");
                    CanMessages.odometerSource = Utility.getPreferences("odometer_source", -1);

                    // for old builds having protocol set in shared preferences
                    updateOdometerSourceAndProtocol(CanMessages.odometerSource, CanMessages.protocol);
                }

                Utility.DeviceStatus = cursor.getInt(cursor.getColumnIndex("StatusId"));

                Utility._appSetting.setVehicleOdometerUnit(Utility.getPreferences("vehicle_odometer_unit", 1));


                Utility.ReferralLink = Utility.getPreferences("referral_link", "");


                Utility.RechargeLink = Utility.getPreferences("recharge_link", "");

                Utility.CompanyStatusId = Utility.getPreferences("company_status_id", 1);

                Utility.LatePaymentFg = Utility.getPreferences("late_payment_fg", false);

            }
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(CarrierInfoDB.class.getName() + "::getCompanyInfo Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(CarrierInfoDB.class.getName(), "::getCompanyInfo Error:", e.getMessage(), Utility.printStackTrace(e));
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
    // Created Date: 14 January 2016
    // Purpose: check duplicate trailer
    private static int checkDuplicate(int vehicleId) {
        int recordId = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select VehicleId from "
                    + MySQLiteOpenHelper.TABLE_CARRIER
                    + " where VehicleId=?", new String[]{vehicleId + ""});
            if (cursor.moveToFirst()) {
                recordId = cursor.getInt(0);

            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(CarrierInfoDB.class.getName() + "::checkDuplicate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(CarrierInfoDB.class.getName(), "::checkDuplicate Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }

        return recordId;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 April 2016
    // Purpose: add or update carrier in database
    public static boolean Save(ArrayList<CarrierInfoBean> lst) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            for (int i = 0; i < lst.size(); i++) {
                CarrierInfoBean bean = lst.get(i);
                if (bean.getStatusId() == 1) {
                    values.put("CompanyId", bean.getCompanyId());
                    values.put("CarrierName", bean.getCarrierName());
                    values.put("ELDManufacturer", bean.getELDManufacturer());
                    values.put("USDOT", bean.getUSDOT());
                    values.put("VehicleId", bean.getVehicleId());
                    values.put("UnitNo", bean.getUnitNo());
                    values.put("VIN", bean.getVIN());
                    values.put("PlateNo", bean.getPlateNo());
                    values.put("SerialNo", bean.getSerailNo());
                    values.put("MACAddress", bean.getMACAddress());
                    values.put("TimeZoneId", bean.getTimeZoneId());
                    values.put("TotalAxle", bean.getTotalAxle());
                    values.put("CanBusFg", bean.getCanBusFg());
                    values.put("StartOdometerReading", bean.getStartOdometerReading());
                    values.put("EldConfig", bean.getEldConfig());
                    values.put("SetupFg", (bean.isSetupFg() ? 1 : 0));
                    values.put("Protocol", bean.getProtocol());
                    values.put("OdometerSource", bean.getOdometerSource());
                    values.put("ReferralCode", bean.getReferralCode());
                    values.put("SyncFg", 1);
                }
                values.put("StatusId", bean.getStatusId());
                int vehicleId = checkDuplicate(bean.getVehicleId());
                if (vehicleId == 0) {
                    database.insert(MySQLiteOpenHelper.TABLE_CARRIER,
                            "modifiedDate", values);

                } else {
                    database.update(MySQLiteOpenHelper.TABLE_CARRIER, values,
                            " VehicleId= ?",
                            new String[]{bean.getVehicleId() + ""});
                }

                if (Utility.IMEI.equals(bean.getSerailNo())) {
                    Utility.packageId = bean.getPackageId();
                    Utility.Features = bean.getFeatures();
                    Utility.savePreferences("packageid", bean.getPackageId());
                    Utility.savePreferences("features", bean.getFeatures());
                    Utility.vehicleId = bean.getVehicleId();
                    Utility.companyId = bean.getCompanyId();
                    Utility.CarrierName = bean.getCarrierName();
                    Utility.ELDManufacturer = bean.getELDManufacturer();
                    Utility.USDOT = bean.getUSDOT();
                    Utility.UnitNo = bean.getUnitNo();
                    Utility.PlateNo = bean.getPlateNo();
                    Utility.VIN = bean.getVIN();
                    Utility.MACAddress = bean.getMACAddress();
                    Utility.DeviceStatus = bean.getStatusId();
                    Utility.CompanyStatusId = bean.getCompanyStatusId();
                    Utility.LatePaymentFg = bean.isLatePaymentFg();
                    Utility.ReferralCode = bean.getReferralCode();

                    if (Utility.activeUserId == 0) {
                        Utility.TimeZoneId = bean.getTimeZoneId();
                        Utility.TimeZoneOffset = ZoneList.getOffset(Utility.TimeZoneId);
                        Utility.TimeZoneOffsetUTC = ZoneList.getTimeZoneOffset(Utility.TimeZoneId);
                        Utility.sdf.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
                    }
                    Utility.canBusFg = bean.getCanBusFg();
                    Utility.StartOdometerReading = Double.parseDouble(Utility.getPreferences("start_odometer_reading", "0"));

                    Utility.FullAddress = bean.getFullAddress();
                    Utility.savePreferences("full_address", bean.getFullAddress());

                    Utility._appSetting.setVehicleOdometerUnit(bean.getVehicleOdometerUnit());
                    SettingsDB.CreateSettings();

                    Utility.savePreferences("vehicle_odometer_unit", bean.getVehicleOdometerUnit());

                    Utility.EldConfig = bean.getEldConfig();
                    Utility.copyEldConfig();

                    Utility.SetupFg = bean.isSetupFg();
                    CanMessages.protocol = bean.getProtocol();
                    CanMessages.odometerSource = bean.getOdometerSource();

                    if (CanMessages.protocol == null) {
                        CanMessages.protocol = Utility.getPreferences("protocol_supported", "");
                        CanMessages.odometerSource = Utility.getPreferences("odometer_source", -1);

                        // for old builds having protocol set in shared preferences
                        updateOdometerSourceAndProtocol(CanMessages.odometerSource, CanMessages.protocol);
                    }

                    Utility.ReferralLink = bean.getReferralLink();
                    Utility.savePreferences("referral_link", Utility.ReferralLink);

                    Utility.RechargeLink = bean.getRechargeLink();
                    Utility.savePreferences("recharge_link", Utility.RechargeLink);

                    Utility.CompanyStatusId = bean.getCompanyStatusId();
                    Utility.savePreferences("company_status_id", Utility.CompanyStatusId);

                    Utility.LatePaymentFg = bean.isLatePaymentFg();
                    Utility.savePreferences("late_payment_fg", Utility.LatePaymentFg);
                }
            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(CarrierInfoDB.class.getName() + "::save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(CarrierInfoDB.class.getName(), "::save Error:", e.getMessage(), Utility.printStackTrace(e));
        } finally {
            database.close();
            helper.close();
        }
        return status;
    }

    public static boolean SaveUnitNo() {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("CompanyId", Utility.companyId);
            values.put("CarrierName", Utility.CarrierName);
            values.put("ELDManufacturer", Utility.ELDManufacturer);
            values.put("USDOT", Utility.USDOT);
            values.put("UnitNo", Utility.UnitNo);
            values.put("PlateNo", Utility.PlateNo);
            values.put("VIN", Utility.VIN);
            values.put("SerialNo", Utility.IMEI);
            values.put("MACAddress", Utility.MACAddress);

            database.update(MySQLiteOpenHelper.TABLE_CARRIER, values,
                    " VehicleId= ?",
                    new String[]{Utility.vehicleId + ""});
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(CarrierInfoDB.class.getName() + "::SaveUnitNo Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(CarrierInfoDB.class.getName(), "::SaveUnitNo Error:", e.getMessage(), Utility.printStackTrace(e));
        } finally {
            database.close();
            helper.close();
        }
        return status;

    }

    // Created By: Deepak Sharma
    // Created Date: 24 July 2018
    // Purpose: duplicate device assignment check
    public static boolean duplicateDeviceAssignment() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select VehicleId from "
                            + MySQLiteOpenHelper.TABLE_CARRIER + " Where SerialNo=?"
                    , new String[]{Utility.IMEI}
            );
            if (cursor.getCount() > 1) {

                status = true;
            }
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(CarrierInfoDB.class.getName() + "::getCompanyInfo Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(CarrierInfoDB.class.getName(), "::duplicateDeviceAssignment Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }
        return status;
    }

    public static void updateOdometerSourceAndProtocol(int odometerSource, String protocol) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("SyncFg", 0);
            values.put("Protocol", protocol);
            values.put("OdometerSource", odometerSource);

            database.update(MySQLiteOpenHelper.TABLE_CARRIER, values,
                    " VehicleId= ?",
                    new String[]{Utility.vehicleId + ""});
        } catch (Exception e) {

            Utility.printError(e.getMessage());
            LogFile.write(CarrierInfoDB.class.getName() + ":: Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(CarrierInfoDB.class.getName(), "::updateOdometerSourceAndProtocol:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            database.close();
            helper.close();
        }


    }

    // Created By: Pallavi Wattamwar
    // Created Date: 25 July 2018
    // Purpose: update Setup Flag
    public static void updateSetupFlag() {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("SyncFg", 0);
            values.put("SetupFg", 1);


            database.update(MySQLiteOpenHelper.TABLE_CARRIER, values,
                    " VehicleId= ?",
                    new String[]{Utility.vehicleId + ""});

            //Post Setup info
            MainActivity.postData(CommonTask.Post_Setupinfo);
        } catch (Exception e) {

            Utility.printError(e.getMessage());
            LogFile.write(CarrierInfoDB.class.getName() + ":: Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(CarrierInfoDB.class.getName(), "::updateSetupFlag:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            database.close();
            helper.close();
        }


    }

    // Created By: Pallavi Wattamwar
    // Created Date: 25 July 2018
    // Purpose: update Setup info
    public static void SetupInfoSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;


        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_CARRIER, values,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {

            LogFile.write(GeofenceDB.class.getName() + "::SetupInfoSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(CarrierInfoDB.class.getName(), "::SetupInfoSyncUpdate:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }

    }


    // Created By: Pallavi Wattamwar
    // Created Date: 25 July 2018
    // Purpose: get setup info sync
    public static JSONObject getSetupInfoSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONObject obj = new JSONObject();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select OdometerSource,Protocol,SetupFg,VehicleId from "
                            + MySQLiteOpenHelper.TABLE_CARRIER + " Where SyncFg=0"
                    , null);

            while (cursor.moveToNext()) {

                obj.put("OdometerSource", cursor.getInt(cursor.getColumnIndex("OdometerSource")));
                obj.put("Protocol", cursor.getString(cursor.getColumnIndex("Protocol")));
                obj.put("SetupFg", cursor.getInt(cursor.getColumnIndex("SetupFg")));
                obj.put("VehicleId", cursor.getInt(cursor.getColumnIndex("VehicleId")));
            }


        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(TripInspectionDB.class.getName() + "::getSetupInfoSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(CarrierInfoDB.class.getName(), "::getSetupInfoSync Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return obj;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: get company info
    public static List<CarrierInfoBean> getAllUnitNo() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        List<CarrierInfoBean> list = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select VehicleId,CompanyId,CarrierName,ELDManufacturer,USDOT,UnitNo,VIN, MACAddress,PlateNo,TimeZoneId,CanBusFg,StartOdometerReading,EldConfig,Protocol,OdometerSource,SetupFg,StatusId,ReferralCode,SerialNo from "
                            + MySQLiteOpenHelper.TABLE_CARRIER
                    , new String[]{}
            );
            while (cursor.moveToNext()) {

                CarrierInfoBean carrierInfoBean = new CarrierInfoBean();
                carrierInfoBean.setUnitNo(cursor.getString(5));
                carrierInfoBean.setMACAddress(cursor.getString(7));
                carrierInfoBean.setPlateNo(cursor.getString(8));
                carrierInfoBean.setSerailNo(cursor.getString(cursor.getColumnIndex("SerialNo")));
                if (carrierInfoBean.getSerailNo().equals(Utility.IMEI))
                    continue;
                list.add(carrierInfoBean);

            }
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(CarrierInfoDB.class.getName() + "::getCompanyInfo Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(CarrierInfoDB.class.getName(), "::getCompanyInfo Error:", e.getMessage(), Utility.printStackTrace(e));
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
}
