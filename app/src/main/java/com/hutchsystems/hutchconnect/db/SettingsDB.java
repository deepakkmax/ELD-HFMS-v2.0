package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.beans.SettingsBean;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

public class SettingsDB {
    public static SettingsBean CreateSettings() {
        SettingsBean bean = new SettingsBean();

        int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        Log.i("Settings", "Save for driverId=" + driverId);
        bean.setDriverId(driverId);
        float offset = Utility.TimeZoneOffset * 1f;
        bean.setTimeZone(offset);
        bean.setDefaultRule(Utility._appSetting.getDefaultRule());
        bean.setGraphLine(Utility._appSetting.getGraphLine());
        bean.setColorLineUS(Utility._appSetting.getColorLineUS());
        bean.setColorLineCanada(Utility._appSetting.getColorLineCanada());
        bean.setTimeFormat(Utility._appSetting.getTimeFormat());
        bean.setViolationReading(Utility._appSetting.getViolationReading());
        bean.setViolationOnGrid(Utility._appSetting.getViolationOnGrid());
        bean.setMessageReading(Utility._appSetting.getMessageReading());
        bean.setStartTime(Utility._appSetting.getStartTime());
        bean.setOrientation(Utility._appSetting.getOrientation());
        bean.setVisionMode(Utility._appSetting.getVisionMode());
        bean.setCopyTrailer(Utility._appSetting.getCopyTrailer());
        bean.setShowViolation(Utility._appSetting.getShowViolation());
        bean.setSyncTime(Utility._appSetting.getSyncTime());
        bean.setAutomaticRuleChange(Utility._appSetting.getAutomaticRuleChange());
        bean.setFontSize(Utility._appSetting.getFontSize());
        bean.setDutyStatusReading(Utility._appSetting.getDutyStatusReading());
        bean.setUnit(Utility._appSetting.getUnit());
        bean.setDrivingScreen(Utility._appSetting.getDrivingScreen());

        // split Slip
        bean.setEnableSplitSlip(Utility._appSetting.getEnableSplitSlip());
        bean.setShowAlertSplitSlip(Utility._appSetting.getShowAlertSplitSlip());
        //Set Dashboard Design
        bean.setDashboardDesign(Utility._appSetting.getDashboardDesign());
        bean.setViolationOnDrivingScreen(Utility._appSetting.getViolationOnDrivingScreen());
        bean.setdEditFg(Utility._appSetting.getdEditFg());
        Save(bean);

        return bean;
    }


    public static void Save(SettingsBean bean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        int settingsId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("TimeZone", bean.getTimeZone());
            values.put("DefaultRule", bean.getDefaultRule());
            values.put("GraphLine", bean.getGraphLine());
            values.put("ColorLineUS", bean.getColorLineUS());
            values.put("ColorLineCanada", bean.getColorLineCanada());
            values.put("TimeFormat", bean.getTimeFormat());
            values.put("ViolationReading", bean.getViolationReading());
            values.put("MessageReading", bean.getMessageReading());
            values.put("StartTime", bean.getStartTime());
            values.put("Orientation", bean.getOrientation());
            values.put("VisionMode", bean.getVisionMode());
            values.put("CopyTrailer", bean.getCopyTrailer());
            values.put("ShowViolation", bean.getShowViolation());
            values.put("SyncTime", bean.getSyncTime());
            values.put("AutomaticRuleChange", bean.getAutomaticRuleChange());
            values.put("ViolationOnGrid", bean.getViolationOnGrid());
            values.put("FontSize", bean.getFontSize());
            values.put("DriverId", bean.getDriverId());
            values.put("DutyStatusReading", bean.getDutyStatusReading());
            values.put("Unit", bean.getUnit());
            values.put("DrivingScreen", bean.getDrivingScreen());

            // Split Sleep
            values.put("EnableSplitSlip", bean.getEnableSplitSlip());
            values.put("ShowAlertSplitSlip", bean.getShowAlertSplitSlip());
            // Set Driving Screen
            values.put("SetDrivingScreen", bean.getDashboardDesign());
            values.put("ViolationOnDrivingScreen", bean.getViolationOnDrivingScreen());
            values.put("DEditFg", bean.getdEditFg());

            int driverId = bean.getDriverId();
            settingsId = getSettingsId(bean.getDriverId());
            if (settingsId == 0) {
                settingsId = (int) database.insertOrThrow(MySQLiteOpenHelper.TABLE_SETTINGS,
                        "_id", values);
            } else {
                database.update(MySQLiteOpenHelper.TABLE_SETTINGS, values,
                        " _id= ?",
                        new String[]{settingsId + ""});
            }
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            Log.e(SettingsDB.class.getName(), "::Save Error:" + e.getMessage());
            LogFile.write(SettingsDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(SettingsDB.class.getName(), "::Save Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(SettingsDB.class.getName() + "::Save close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(SettingsDB.class.getName(), "::Save close DB Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }
    }

    public static SettingsBean getSettings(int driverId) {
        SettingsBean bean = new SettingsBean();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();


            cursor = database.rawQuery("select _id, TimeZone, DefaultRule, GraphLine, ColorLineUS, ColorLineCanada, TimeFormat, ViolationReading, ViolationOnGrid, MessageReading, " +
                    "StartTime, Orientation, VisionMode, CopyTrailer, ShowViolation, SyncTime, AutomaticRuleChange, FontSize, DutyStatusReading,Unit,DrivingScreen,EnableSplitSlip,ShowAlertSplitSlip ,SetDrivingScreen, ViolationOnDrivingScreen,DEditFg from " + MySQLiteOpenHelper.TABLE_SETTINGS + " where DriverId=?", new String[]{Integer.toString(driverId)});
            if (cursor.getCount() == 0) {
                Log.i("SettingsDB", "Get nothing from Settings table");
                CreateSettings(); //save default bean
                bean.setTimeZone(AppSettings.getTimeZone());
                bean.setDefaultRule(AppSettings.getDefaultRule());
                bean.setGraphLine(AppSettings.getGraphLine());
                bean.setColorLineUS(AppSettings.getColorLineUS());
                bean.setColorLineCanada(AppSettings.getColorLineCanada());
                bean.setTimeFormat(AppSettings.getTimeFormat());
                bean.setViolationReading(AppSettings.getViolationReading());
                bean.setMessageReading(AppSettings.getMessageReading());
                bean.setStartTime(AppSettings.getStartTime());
                bean.setOrientation(AppSettings.getOrientation());
                bean.setVisionMode(AppSettings.getVisionMode());
                bean.setCopyTrailer(AppSettings.getCopyTrailer());
                bean.setShowViolation(AppSettings.getShowViolation());
                bean.setSyncTime(AppSettings.getSyncTime());
                bean.setAutomaticRuleChange(AppSettings.getAutomaticRuleChange());
                bean.setViolationOnGrid(AppSettings.getViolationOnGrid());
                bean.setFontSize(AppSettings.getFontSize());
                bean.setDutyStatusReading(AppSettings.getDutyStatusReading());
                bean.setUnit(AppSettings.getUnit());
                bean.setDrivingScreen(AppSettings.getDrivingScreen());

                // split slip
                bean.setEnableSplitSlip(AppSettings.getEnableSplitSlip());
                bean.setShowAlertSplitSlip(AppSettings.getShowAlertSplitSlip());
                // set Dashboard Design
                bean.setDashboardDesign(AppSettings.getDashboardScreen());
                bean.setViolationOnDrivingScreen(AppSettings.getViolationOnDrivingScreen());;
                bean.setdEditFg(0);
            } else {
                if (cursor.moveToLast()) {

                    bean.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                    bean.setTimeZone(cursor.getFloat(cursor.getColumnIndex("TimeZone")));
                    bean.setDefaultRule(cursor.getInt(cursor.getColumnIndex("DefaultRule")));
                    bean.setGraphLine(cursor.getInt(cursor.getColumnIndex("GraphLine")));
                    bean.setColorLineUS(cursor.getInt(cursor.getColumnIndex("ColorLineUS")));
                    bean.setColorLineCanada(cursor.getInt(cursor.getColumnIndex("ColorLineCanada")));
                    bean.setTimeFormat(cursor.getInt(cursor.getColumnIndex("TimeFormat")));
                    bean.setViolationReading(cursor.getInt(cursor.getColumnIndex("ViolationReading")));

                    bean.setMessageReading(cursor.getInt(cursor.getColumnIndex("MessageReading")));
                    bean.setStartTime(cursor.getString(cursor.getColumnIndex("StartTime")));
                    bean.setOrientation(cursor.getInt(cursor.getColumnIndex("Orientation")));
                    bean.setVisionMode(cursor.getInt(cursor.getColumnIndex("VisionMode")));
                    bean.setCopyTrailer(cursor.getInt(cursor.getColumnIndex("CopyTrailer")));
                    bean.setShowViolation(cursor.getInt(cursor.getColumnIndex("ShowViolation")));
                    bean.setShowViolation(0);
                    bean.setSyncTime(cursor.getInt(cursor.getColumnIndex("SyncTime")));
                    bean.setAutomaticRuleChange(cursor.getInt(cursor.getColumnIndex("AutomaticRuleChange")));
                    bean.setViolationOnGrid(cursor.getInt(cursor.getColumnIndex("ViolationOnGrid")));
                    //  bean.setViolationOnGrid(0);
                    bean.setFontSize(cursor.getInt(cursor.getColumnIndex("FontSize")));
                    bean.setDutyStatusReading(cursor.getInt(cursor.getColumnIndex("DutyStatusReading")));
                    bean.setUnit(cursor.getInt(cursor.getColumnIndex("Unit")));
                    bean.setDrivingScreen(cursor.getInt(cursor.getColumnIndex("DrivingScreen")));

                    // Split Slip
                    bean.setEnableSplitSlip(cursor.getInt(cursor.getColumnIndex("EnableSplitSlip")));
                    bean.setShowAlertSplitSlip(cursor.getInt(cursor.getColumnIndex("ShowAlertSplitSlip")));
                    bean.setDashboardDesign(cursor.getInt(cursor.getColumnIndex("SetDrivingScreen")));
                    bean.setViolationOnDrivingScreen(cursor.getInt(cursor.getColumnIndex("ViolationOnDrivingScreen")));
                    bean.setdEditFg(cursor.getInt(cursor.getColumnIndex("DEditFg")));

                }

                int autoStatusChange = Utility.getPreferences("auto_status_change", Utility._appSetting.getAutoChangeStatus());
                Utility._appSetting.setAutoChangeStatus(autoStatusChange);
            }
            bean.setChangeUnitOnRuleChange(Utility.getPreferences("change_unit_on_rule_change", 0));
            bean.setNewsplitSleepUSA(Utility.getPreferences("new_us_split_sleep_rule", 0));

            int locationSource = Utility.getPreferences("location_source", Utility._appSetting.getLocationSource());
            bean.setLocationSource(locationSource);
// Langauage settings
            bean.setSupportLanguage(Integer.valueOf(UserDB.userInfoGet(driverId).getSupportLanguage()));

            bean.setECUConnectivity(Utility.getPreferences("ecu_connectivity", 1));

        } catch (Exception e) {
            LogFile.write(SettingsDB.class.getName() + "::getSettings Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(SettingsDB.class.getName(), "::getSettings Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                LogFile.write(SettingsDB.class.getName() + "::getSettings close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(SettingsDB.class.getName(), "::getSettings  close DB Error:", e.getMessage(), Utility.printStackTrace(e));
            }
        }
        return bean;
    }

    public static int getSettingsId(int driverId) {
        int id = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_SETTINGS + " where DriverId=?", new String[]{Integer.toString(driverId)});
            if (cursor.moveToLast()) {
                id = cursor.getInt(0);
            }

        } catch (Exception exe) {
            LogFile.write(SettingsDB.class.getName() + "::getSettingsId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(SettingsDB.class.getName(), "::getSettingsId Error:", exe.getMessage(), Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return id;
    }

    public static void update(String column, String value, int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(column, value);

            database.update(MySQLiteOpenHelper.TABLE_SETTINGS, values,
                    " DriverId= ?",
                    new String[]{driverId + ""});

        } catch (Exception e) {
            Utility.printError(e.getMessage());

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(SettingsDB.class.getName() + "::Save close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(SettingsDB.class.getName(), "::Save close DB Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }
    }
}
