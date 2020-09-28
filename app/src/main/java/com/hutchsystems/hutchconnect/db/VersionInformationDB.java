package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hutchsystems.hutchconnect.beans.VersionInformationBean;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

public class VersionInformationDB {
    // Created By: Deepak Sharma
    // Created Date: 22 May 2019
    // Purpose: Create Version info
    public static VersionInformationBean CreateVersion(boolean autoDownloadFg, boolean autoUpdateFg, String currentVersion, String downloadDate, boolean downloadFg, boolean liveFg,
                                                       String previousVersion, String serialNo, String updateArchiveName, String updateDate, String updateUrl, boolean updatedFg, String versionDate) {
        VersionInformationBean bean = new VersionInformationBean();
        bean.setAutoDownloadFg(autoDownloadFg);
        bean.setAutoUpdateFg(autoUpdateFg);
        bean.setCurrentVersion(currentVersion);
        bean.setDownloadDate(downloadDate);
        bean.setDownloadFg(downloadFg);
        bean.setLiveFg(liveFg);
        bean.setPreviousVersion(previousVersion);
        bean.setSerialNo(serialNo);
        bean.setUpdateArchiveName(updateArchiveName);
        bean.setUpdateDate(updateDate);
        bean.setUpdateUrl(updateUrl);
        bean.setUpdatedFg(updatedFg);
        bean.setVersionDate(versionDate);

        Save(bean);
        return bean;

    }
    // Created By: Deepak Sharma
    // Created Date: 22 May 2019
    // Purpose: Add or Update Version information
    public static void Save(VersionInformationBean bean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        int versionId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select _id,CurrentVersion from " + MySQLiteOpenHelper.TABLE_VERSION_INFORMATION + " order by _id desc Limit 1", null);

            if (cursor.moveToFirst()) {
                String version = cursor.getString(1);
                if (bean.getCurrentVersion().equals(version)) {
                    versionId = cursor.getInt(0);

                }
            }
            ContentValues values = new ContentValues();

            values.put("AutoDownloadFg", bean.getAutoDownloadFg() ? 1 : 0);
            values.put("AutoUpdateFg", bean.getAutoUpdateFg() ? 1 : 0);
            values.put("CurrentVersion", bean.getCurrentVersion());
            values.put("DownloadDate", bean.getDownloadDate());
            values.put("DownloadFg", bean.getDownloadFg() ? 1 : 0);
            values.put("LiveFg", bean.getLiveFg() ? 1 : 0);
            values.put("PreviousVersion", bean.getPreviousVersion());
            values.put("SerialNo", bean.getSerialNo());
            values.put("UpdateArchiveName", bean.getUpdateArchiveName());
            values.put("UpdateDate", bean.getUpdateDate());
            values.put("UpdateUrl", bean.getUpdateUrl());
            values.put("UpdatedFg", bean.getUpdatedFg() ? 1 : 0);
            values.put("VersionDate", bean.getVersionDate());
            values.put("AppDownloadFromPlayStore", bean.isAppDownloadFromPlayStore() ? 1:0);
            values.put("FileContentLength", bean.getFileContentLength());
            values.put("SyncFg",0);
         //  if(Utility.VersionNewFg(bean.getCurrentVersion()) && versionId == 0)
            {
                values.put("SyncDate", Utility.getCurrentDateTime());
            }


            if (versionId == 0)
                versionId = (int) database.insertOrThrow(MySQLiteOpenHelper.TABLE_VERSION_INFORMATION,
                        "_id", values);
            else
                database.update(MySQLiteOpenHelper.TABLE_VERSION_INFORMATION, values,
                        " _id= ?", new String[]{versionId
                                + ""});
        } catch (Exception e) {
            Log.e(VersionInformationDB.class.getName(), "::Save Error:" + e.getMessage());
            LogFile.write(VersionInformationDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(VersionInformationDB.class.getName(),"::Save Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                LogFile.write(VersionInformationDB.class.getName() + "::Save close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(VersionInformationDB.class.getName(),"::Save Error:" ,e.getMessage(),Utility.printStackTrace(e));

            }
        }
    }
    // Created By: Deepak Sharma
    // Created Date: 22 May 2019
    // Purpose: Get Version information
    public static VersionInformationBean getVersionInformation() {
        VersionInformationBean bean = new VersionInformationBean();

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select * from " + MySQLiteOpenHelper.TABLE_VERSION_INFORMATION +" order by _id desc Limit 1", null);
            if (cursor.moveToFirst()) {

                bean.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setAutoDownloadFg(cursor.getInt(cursor.getColumnIndex("AutoDownloadFg")) == 1 ? true : false);
                bean.setAutoUpdateFg(cursor.getInt(cursor.getColumnIndex("AutoUpdateFg")) == 1 ? true : false);
                bean.setCurrentVersion(cursor.getString(cursor.getColumnIndex("CurrentVersion")));
                bean.setDownloadDate(cursor.getString(cursor.getColumnIndex("DownloadDate")));
                bean.setDownloadFg(cursor.getInt(cursor.getColumnIndex("DownloadFg")) == 1 ? true : false);
                bean.setLiveFg(cursor.getInt(cursor.getColumnIndex("LiveFg")) == 1 ? true : false);
                bean.setPreviousVersion(cursor.getString(cursor.getColumnIndex("PreviousVersion")));
                bean.setSerialNo(cursor.getString(cursor.getColumnIndex("SerialNo")));
                bean.setUpdateArchiveName(cursor.getString(cursor.getColumnIndex("UpdateArchiveName")));
                bean.setUpdateDate(cursor.getString(cursor.getColumnIndex("UpdateDate")));
                bean.setUpdateUrl(cursor.getString(cursor.getColumnIndex("UpdateUrl")));
                bean.setUpdatedFg(cursor.getInt(cursor.getColumnIndex("UpdatedFg")) == 1 ? true : false);
                bean.setVersionDate(cursor.getString(cursor.getColumnIndex("VersionDate")));
                bean.setAppDownloadFromPlayStore(cursor.getInt(cursor.getColumnIndex("AppDownloadFromPlayStore")) == 1 ? true : false);
                bean.setSyncDate(cursor.getString(cursor.getColumnIndex("SyncDate")));
                bean.setFileContentLength(cursor.getInt(cursor.getColumnIndex("FileContentLength")));


            }

        } catch (Exception e) {
            LogFile.write(VersionInformationDB.class.getName() + "::getVersionInformation Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(VersionInformationDB.class.getName(),"::getVersionInformation Error:" ,e.getMessage(),Utility.printStackTrace(e));
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                LogFile.write(VersionInformationDB.class.getName() + "::getVersionInformation close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(VersionInformationDB.class.getName(),"::getVersionInformation Error:" ,e.getMessage(),Utility.printStackTrace(e));

            }
        }
        return bean;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 24 Oct 2018
    // Purpose: post Version information
    public static JSONArray postVersionInfo() {



        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();

        try {

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select * from " + MySQLiteOpenHelper.TABLE_VERSION_INFORMATION + " Where SyncFg=0"+" order by _id desc Limit 1", null);


            if (cursor.moveToFirst()) {
                JSONObject versioninfoJson = new JSONObject();
                versioninfoJson.put("AutoDownloadFg", cursor.getInt(cursor.getColumnIndex("AutoDownloadFg")) == 1 ? true : false);
                // need to discuss
                versioninfoJson.put("BackupDate", Utility.getCurrentDateTime());
                // need to discuss
                versioninfoJson.put("BackupFg", true);
                versioninfoJson.put("CurrentVersion", cursor.getString(cursor.getColumnIndex("CurrentVersion")));
                versioninfoJson.put("DownloadDate", cursor.getString(cursor.getColumnIndex("DownloadDate")));
                versioninfoJson.put("DownloadFg", cursor.getInt(cursor.getColumnIndex("DownloadFg")) == 1 ? true : false);
                versioninfoJson.put("LiveFg", cursor.getInt(cursor.getColumnIndex("LiveFg")) == 1 ? true : false);
                versioninfoJson.put("PreviousVersion", cursor.getString(cursor.getColumnIndex("PreviousVersion")));
                versioninfoJson.put("SerialNo", cursor.getString(cursor.getColumnIndex("SerialNo")));
                versioninfoJson.put("UpdateArchiveName", cursor.getString(cursor.getColumnIndex("UpdateArchiveName")));
                versioninfoJson.put("UpdateDate", cursor.getString(cursor.getColumnIndex("UpdateDate")));
                versioninfoJson.put("UpdateUrl", cursor.getString(cursor.getColumnIndex("UpdateUrl")));
                versioninfoJson.put("UpdatedFg", cursor.getInt(cursor.getColumnIndex("UpdatedFg")) == 1 ? true : false);
                versioninfoJson.put("VersionDate", cursor.getString(cursor.getColumnIndex("VersionDate")));
                array.put(versioninfoJson);
            }

            }


         catch (Exception e) {
            e.printStackTrace();
        }
        return array;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 24 MAy 2019
    // Purpose: after Sync update set syncfg flag = 1
    public static JSONArray VersionSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_VERSION_INFORMATION, values,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
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
