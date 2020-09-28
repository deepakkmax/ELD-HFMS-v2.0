package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.beans.VehicleInfoBean;
import com.hutchsystems.hutchconnect.common.ConstantFlag;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by SAMSUNG on 16-01-2017.
 */

public class VehicleInfoDB {

    // Created By: Deepak Sharma
    // Created Date: 16 January 2017
    // Purpose: Save vehicle info
    public static boolean Save(VehicleInfoBean bean) {

        boolean status = true;

        if (ConstantFlag.HutchConnectFg)
            return status;

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("CreatedDate", bean.getCreatedDate());
            values.put("OdometerReading", bean.getOdometerReading());
            values.put("Speed", bean.getSpeed());
            values.put("RPM", bean.getRPM());
            values.put("Average", bean.getAverage());
            values.put("EngineHour", bean.getEngineHour());
            values.put("FuelUsed", bean.getFuelUsed());
            values.put("IdleFuelUsed", bean.getIdleFuelUsed());
            values.put("IdleHours", bean.getIdleHours());
            values.put("Boost", bean.getBoost());
            values.put("CoolantTemperature", bean.getCoolantTemperature());
            values.put("CoolantLevel", bean.getCoolantLevel());
            values.put("BatteryVoltage", bean.getBatteryVoltage());
            values.put("WasherFluidLevel", bean.getWasherFluidLevel());
            values.put("EngineLoad", bean.getEngineLoad());
            values.put("EngineOilLevel", bean.getEngineOilLevel());
            values.put("CruiseSpeed", bean.getCruiseSpeed());
            values.put("MaxRoadSpeed", bean.getMaxRoadSpeed());
            values.put("AirSuspension", bean.getAirSuspension());
            values.put("TransmissionOilLevel", bean.getTransmissionOilLevel());
            values.put("DEFTankLevel", bean.getDEFTankLevel());
            values.put("DEFTankLevelLow", bean.getDEFTankLevelLow());
            values.put("EngineSerialNo", bean.getEngineSerialNo());
            values.put("EngineRatePower", bean.getEngineRatePower());

            values.put("CruiseSetFg", bean.getCruiseSetFg());
            values.put("PowerUnitABSFg", bean.getPowerUnitABSFg());
            values.put("TrailerABSFg", bean.getTrailerABSFg());
            values.put("DerateFg", bean.getDerateStatus());
            values.put("BrakeApplication", bean.getBrakeApplication());
            values.put("RegenerationRequiredFg", bean.getRegenerationRequiredFg());
            values.put("WaterInFuelFg", bean.getWaterInFuelFg());
            values.put("PTOEngagementFg", bean.getPTOEngagementFg());
            values.put("CuriseTime", bean.getCuriseTime());
            values.put("SeatBeltFg", bean.getSeatBeltFg());
            values.put("TransmissionGear", bean.getTransmissionGear());
            values.put("ActiveDTCFg", bean.getActiveDTCFg());
            values.put("InActiveDTCFg", bean.getInActiveDTCFg());
            values.put("PTOHours", bean.getPTOHours());
            values.put("TPMSWarningFg", bean.getTPMSWarningFg());
            values.put("FuelLevel", bean.getFuelLevel());

            values.put("FuelPressure", bean.getFuelPressure());
            values.put("AirInletTemperature", bean.getAirInletTemperature());
            values.put("BarometricPressure", bean.getBarometricPressure());
            values.put("EngineOilPressure", bean.getEngineOilPressure());

            values.put("SyncFg", 0);

            database.insert(MySQLiteOpenHelper.TABLE_VEHICLE_INFO,
                    "_id", values);

        } catch (Exception e) {
            status = false;
            LogFile.write(VehicleInfoDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(VehicleInfoDB.class.getName(), "::Save Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            database.close();
            helper.close();
        }
        return status;

    }

    // Created By: Deepak Sharma
    // Created Date: 16 January 2017
    // Purpose: get Vehicle Info for web sync
    public static JSONArray getVehicleInfoSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select _Id ,FuelLevel, OdometerReading   ,Speed   ,RPM   ,Average   ,EngineHour   ,FuelUsed   ,IdleFuelUsed   ,IdleHours   ,Boost   ,CoolantTemperature   ,CoolantLevel   ,BatteryVoltage   ,WasherFluidLevel   ,EngineLoad   ,EngineOilLevel   ,CruiseSetFg  ,CruiseSpeed   ,PowerUnitABSFg  ,TrailerABSFg  ,DerateFg  ,BrakeApplication  ,RegenerationRequiredFg  ,WaterInFuelFg  ,MaxRoadSpeed   ,PTOEngagementFg  ,CuriseTime  ,SeatBeltFg  ,AirSuspension   ,TransmissionOilLevel   ,TransmissionGear  ,DEFTankLevel   ,DEFTankLevelLow  ,ActiveDTCFg ,InActiveDTCFg ,PTOHours ,TPMSWarningFg ,EngineSerialNo  ,EngineRatePower,FuelPressure ,AirInletTemperature,BarometricPressure,EngineOilPressure  ,CreatedDate from "
                            + MySQLiteOpenHelper.TABLE_VEHICLE_INFO + " Where SyncFg=0"
                    , null);

            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("VehicleId", Utility.vehicleId);
                obj.put("CompanyId", Utility.companyId);
                obj.put("CreatedDate", cursor.getString(cursor.getColumnIndex("CreatedDate")));
                obj.put("EngineSerialNo", cursor.getString(cursor.getColumnIndex("EngineSerialNo")));

                obj.put("OdometerReading", Double.parseDouble(cursor.getString(cursor.getColumnIndex("OdometerReading"))));
                obj.put("Speed", Double.parseDouble(cursor.getString(cursor.getColumnIndex("Speed"))));
                obj.put("RPM", Double.parseDouble(cursor.getString(cursor.getColumnIndex("RPM"))));
                obj.put("Average", Double.parseDouble(cursor.getString(cursor.getColumnIndex("Average"))));
                obj.put("EngineHour", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineHour"))));
                obj.put("FuelUsed", Double.parseDouble(cursor.getString(cursor.getColumnIndex("FuelUsed"))));
                obj.put("IdleFuelUsed", Double.parseDouble(cursor.getString(cursor.getColumnIndex("IdleFuelUsed"))));
                obj.put("IdleHours", Double.parseDouble(cursor.getString(cursor.getColumnIndex("IdleHours"))));
                obj.put("Boost", Double.parseDouble(cursor.getString(cursor.getColumnIndex("Boost"))));
                obj.put("CoolantTemperature", Double.parseDouble(cursor.getString(cursor.getColumnIndex("CoolantTemperature"))));
                obj.put("CoolantLevel", Double.parseDouble(cursor.getString(cursor.getColumnIndex("CoolantLevel"))));
                obj.put("BatteryVoltage", Double.parseDouble(cursor.getString(cursor.getColumnIndex("BatteryVoltage"))));
                obj.put("WasherFluidLevel", Double.parseDouble(cursor.getString(cursor.getColumnIndex("WasherFluidLevel"))));
                obj.put("EngineLoad", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineLoad"))));
                obj.put("EngineOilLevel", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineOilLevel"))));
                obj.put("CruiseSpeed", Double.parseDouble(cursor.getString(cursor.getColumnIndex("CruiseSpeed"))));
                obj.put("MaxRoadSpeed", Double.parseDouble(cursor.getString(cursor.getColumnIndex("MaxRoadSpeed"))));
                obj.put("AirSuspension", Double.parseDouble(cursor.getString(cursor.getColumnIndex("AirSuspension"))));
                obj.put("TransmissionOilLevel", Double.parseDouble(cursor.getString(cursor.getColumnIndex("TransmissionOilLevel"))));
                obj.put("DEFTankLevel", Double.parseDouble(cursor.getString(cursor.getColumnIndex("DEFTankLevel"))));
                obj.put("DEFTankLevelLow", Double.parseDouble(cursor.getString(cursor.getColumnIndex("DEFTankLevelLow"))));
                obj.put("EngineRatePower", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineRatePower"))));
                obj.put("CuriseTime", Double.parseDouble(cursor.getString(cursor.getColumnIndex("CuriseTime"))));
                obj.put("PTOHours", Double.parseDouble(cursor.getString(cursor.getColumnIndex("PTOHours"))));

                obj.put("CruiseSetFg", cursor.getInt(cursor.getColumnIndex("CruiseSetFg")));
                obj.put("PowerUnitABSFg", cursor.getInt(cursor.getColumnIndex("PowerUnitABSFg")));
                obj.put("TrailerABSFg", cursor.getInt(cursor.getColumnIndex("TrailerABSFg")));
                obj.put("DerateFg", cursor.getInt(cursor.getColumnIndex("DerateFg"))); // not renaming database column as there is too many records in this column to be posted
                obj.put("BrakeApplication", cursor.getInt(cursor.getColumnIndex("BrakeApplication")));
                obj.put("RegenerationRequiredFg", cursor.getInt(cursor.getColumnIndex("RegenerationRequiredFg")));
                obj.put("WaterInFuelFg", cursor.getInt(cursor.getColumnIndex("WaterInFuelFg")));
                obj.put("PTOEngagementFg", cursor.getInt(cursor.getColumnIndex("PTOEngagementFg")));
                obj.put("SeatBeltFg", cursor.getInt(cursor.getColumnIndex("SeatBeltFg")));
                obj.put("TransmissionGear", cursor.getInt(cursor.getColumnIndex("TransmissionGear")));
                obj.put("ActiveDTCFg", cursor.getInt(cursor.getColumnIndex("ActiveDTCFg")));
                obj.put("InActiveDTCFg", cursor.getInt(cursor.getColumnIndex("InActiveDTCFg")));
                obj.put("TPMSWarningFg", cursor.getInt(cursor.getColumnIndex("TPMSWarningFg")));
                obj.put("FuelLevel", cursor.getInt(cursor.getColumnIndex("FuelLevel")));

                obj.put("FuelPressure", Double.parseDouble(cursor.getString(cursor.getColumnIndex("FuelPressure"))));
                obj.put("AirInletTemperature", Double.parseDouble(cursor.getString(cursor.getColumnIndex("AirInletTemperature"))));
                obj.put("BarometricPressure", Double.parseDouble(cursor.getString(cursor.getColumnIndex("BarometricPressure"))));
                obj.put("EngineOilPressure", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineOilPressure"))));
                array.put(obj);
            }

        } catch (Exception e) {
            LogFile.write(VehicleInfoDB.class.getName() + "::getVehicleInfoSync Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(VehicleInfoDB.class.getName(), "::getVehicleInfoSync Error:", e.getMessage(), Utility.printStackTrace(e));
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
    // Created Date: 22 December 2016
    // Purpose: update TPMS for web sync
    public static JSONArray VehicleInfoSyncDelete() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            database.delete(MySQLiteOpenHelper.TABLE_VEHICLE_INFO,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {
            LogFile.write(VehicleInfoDB.class.getName() + "::VehicleInfoSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(VehicleInfoDB.class.getName(), "::VehicleInfoSyncUpdate Error:", exe.getMessage(), Utility.printStackTrace(exe));

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
    // Created Date: 28 March 2017
    // Purpose: update Incident for web sync
    public static JSONArray VehicleInfoUpdate(String data) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String[] ids = data.split(",");
            for (String id : ids) {
                ContentValues values = new ContentValues();
                values.put("SyncFg", 1);
                database.update(MySQLiteOpenHelper.TABLE_VEHICLE_INFO, values,
                        " SyncFg=? and _Id=?", new String[]{"0", id});
            }

        } catch (Exception exe) {

            LogFile.write(TrackingDB.class.getName() + "::GPSSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(VehicleInfoDB.class.getName(), "::VehicleInfoUpdate Error:", exe.getMessage(), Utility.printStackTrace(exe));

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
    // Created Date: 28 March 2017
    // Purpose: get Vehicle Info for socket
    public static String[] getVehicleInfo() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        StringBuilder obj = new StringBuilder();
        String id = "0";
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _Id ,FuelLevel, OdometerReading   ,Speed   ,RPM   ,Average   ,EngineHour   ,FuelUsed   ,IdleFuelUsed   ,IdleHours   ,Boost   ,CoolantTemperature   ,CoolantLevel   ,BatteryVoltage   ,WasherFluidLevel   ,EngineLoad   ,EngineOilLevel   ,CruiseSetFg  ,CruiseSpeed   ,PowerUnitABSFg  ,TrailerABSFg  ,DerateFg  ,BrakeApplication  ,RegenerationRequiredFg  ,WaterInFuelFg  ,MaxRoadSpeed   ,PTOEngagementFg  ,CuriseTime  ,SeatBeltFg  ,AirSuspension   ,TransmissionOilLevel   ,TransmissionGear  ,DEFTankLevel   ,DEFTankLevelLow  ,ActiveDTCFg ,InActiveDTCFg ,PTOHours ,TPMSWarningFg ,EngineSerialNo  ,EngineRatePower,FuelPressure ,AirInletTemperature,BarometricPressure,EngineOilPressure  ,CreatedDate from "
                            + MySQLiteOpenHelper.TABLE_VEHICLE_INFO + " Where SyncFg=0 order by _Id desc LIMIT 10"
                    , null);
            while (cursor.moveToNext()) {
                id += "," + cursor.getInt(0);
                obj.append(Utility.IMEI).append(",");
                obj.append(Utility._productCode).append(":").append(Utility.ApplicationVersion).append(",");
                obj.append("VI").append(",");
                obj.append(Utility.vehicleId).append(",");
                obj.append(Utility.companyId).append(",");
                obj.append(cursor.getString(cursor.getColumnIndex("CreatedDate"))).append(",");
                obj.append(cursor.getString(cursor.getColumnIndex("EngineSerialNo"))).append(",");

                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("OdometerReading")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("Speed")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("RPM")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("Average")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineHour")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("FuelUsed")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("IdleFuelUsed")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("IdleHours")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("Boost")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("CoolantTemperature")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("CoolantLevel")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("BatteryVoltage")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("WasherFluidLevel")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineLoad")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineOilLevel")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("CruiseSpeed")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("MaxRoadSpeed")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("AirSuspension")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("TransmissionOilLevel")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("DEFTankLevel")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("DEFTankLevelLow")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineRatePower")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("CuriseTime")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("PTOHours")))).append(",");

                obj.append(cursor.getInt(cursor.getColumnIndex("CruiseSetFg"))).append(",");
                obj.append(cursor.getInt(cursor.getColumnIndex("PowerUnitABSFg"))).append(",");
                obj.append(cursor.getInt(cursor.getColumnIndex("TrailerABSFg"))).append(",");
                obj.append(cursor.getInt(cursor.getColumnIndex("DerateFg"))).append(","); // not renaming database column as there is too many records in this column to be posted
                obj.append(cursor.getInt(cursor.getColumnIndex("BrakeApplication"))).append(",");
                obj.append(cursor.getInt(cursor.getColumnIndex("RegenerationRequiredFg"))).append(",");
                obj.append(cursor.getInt(cursor.getColumnIndex("WaterInFuelFg"))).append(",");
                obj.append(cursor.getInt(cursor.getColumnIndex("PTOEngagementFg"))).append(",");
                obj.append(cursor.getInt(cursor.getColumnIndex("SeatBeltFg"))).append(",");
                obj.append(cursor.getInt(cursor.getColumnIndex("TransmissionGear"))).append(",");
                obj.append(cursor.getInt(cursor.getColumnIndex("ActiveDTCFg"))).append(",");
                obj.append(cursor.getInt(cursor.getColumnIndex("InActiveDTCFg"))).append(",");
                obj.append(cursor.getInt(cursor.getColumnIndex("TPMSWarningFg"))).append(",");
                obj.append(cursor.getInt(cursor.getColumnIndex("FuelLevel"))).append(",");

                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("FuelPressure")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("AirInletTemperature")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("BarometricPressure")))).append(",");
                obj.append(Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineOilPressure")))).append(";");
            }

        } catch (Exception e) {
            LogFile.write(VehicleInfoDB.class.getName() + "::getVehicleInfo Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(VehicleInfoDB.class.getName(), "::getVehicleInfo Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }
        return new String[]{id, obj.toString()};
    }


}
