package com.hutchsystems.hutchconnect.db;

import java.util.ArrayList;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.beans.UserBean;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.common.ZoneList;

import org.json.JSONObject;

public class UserDB {

    public static String getAddress(int userId) {
        UserBean user = userInfoGet(userId);
        return user.getFullAddress();
    }


    public static String getMobileNo(int userId) {
        UserBean user = userInfoGet(userId);
        return user.getMobileNo();
    }

    // Created By: Deepak Sharma
    // Created Date: 23 August 2016
    // Purpose: get User Info
    public static UserBean userInfoGet(int userId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        UserBean user = new UserBean();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String args[] = {userId + ""};
            cursor = database.rawQuery("select AccountId,Username,FirstName,LastName,AccountType,Password,Salt,ExemptELDUseFg,SpecialCategory,DrivingLicense,DLIssueState,LicenseAcceptFg,LicenseExpiryDate,EmailId,MobileNo,DotPassword,TimeZoneId,Address,LanguageForSupport  from "
                    + MySQLiteOpenHelper.TABLE_ACCOUNT
                    + " where AccountId=?", args);
            while (cursor.moveToNext()) {
                String encryptedPassword = cursor.getString(cursor.getColumnIndex("Password"));
                String salt = cursor.getString(cursor.getColumnIndex("Salt"));

                user.setPassword(encryptedPassword);
                user.setSalt(salt);
                user.setAccountId(cursor.getInt(cursor.getColumnIndex("AccountId")));
                user.setFirstName(cursor.getString(cursor.getColumnIndex("FirstName")));
                user.setLastName(cursor.getString(cursor.getColumnIndex("LastName")));
                user.setAccountType(cursor.getInt(cursor.getColumnIndex("AccountType")));
                user.setExemptELDUseFg(cursor.getInt(cursor.getColumnIndex("ExemptELDUseFg")));
                user.setSpecialCategory(cursor.getString(cursor.getColumnIndex("SpecialCategory")));

                user.setUserName(cursor.getString(cursor.getColumnIndex("Username")));
                user.setDrivingLicense(cursor.getString(cursor.getColumnIndex("DrivingLicense")));
                user.setDlIssueState(cursor.getString(cursor.getColumnIndex("DLIssueState")));
                user.setLicenseAcceptFg(cursor.getInt(cursor.getColumnIndex("LicenseAcceptFg")));
                user.setLicenseExpiryDate(cursor.getString(cursor.getColumnIndex("LicenseExpiryDate")));
                user.setEmailId(cursor.getString(cursor.getColumnIndex("EmailId")));
                user.setMobileNo(cursor.getString(cursor.getColumnIndex("MobileNo")));
                user.setDotPassword(cursor.getString(cursor.getColumnIndex("DotPassword")));
                user.setTimeZoneId(cursor.getString(cursor.getColumnIndex("TimeZoneId")));
                user.setFullAddress(cursor.getString(cursor.getColumnIndex("Address")));
                // set LanguageForSupport
                user.setSupportLanguage(cursor.getString(cursor.getColumnIndex("LanguageForSupport")));
                // null handling for supporting language
                if (user.getSupportLanguage() == null) {
                    user.setSupportLanguage("1");
                }

            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(LoginDB.class.getName() + "::LoginUser Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(UserDB.class.getName(), "::LoginUser Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }
        return user;
    }

    // Created By: Deepak Sharma
    // Created Date: 15 June 2016
    // Purpose:  update accounts in database
    public static void accountSyncUpdate() {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_ACCOUNT, values,
                    " SyncFg= ?", new String[]{"0"});

        } catch (Exception e) {
            LogFile.write(UserDB.class.getName() + "::accountSyncUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(UserDB.class.getName(), "::accountSyncGet Error:", e.getMessage(), Utility.printStackTrace(e));
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                LogFile.write(UserDB.class.getName() + "::accountSyncUpdate close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(UserDB.class.getName(), "::accountSyncUpdate Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }

    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: get account to sync to server
    public static String accountSyncGet() {
        String result = "";
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select AccountId ,CurrentRule ,TimeZoneOffsetUTC ,LicenseAcceptFg,LanguageForSupport from "
                    + MySQLiteOpenHelper.TABLE_ACCOUNT
                    + " where SyncFg=0", null);
            if (cursor.moveToFirst()) {
                JSONObject obj = new JSONObject();
                obj.put("UserId", cursor.getInt(0));
                obj.put("CurrentRule", cursor.getInt(1));
                obj.put("TimeZoneOffset", cursor.getString(2));
                obj.put("LicenseAcceptFg", cursor.getInt(3));
                obj.put("SupportLanguage", cursor.getString(4));

                result = obj.toString();
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(UserDB.class.getName() + "::accountSyncGet Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(UserDB.class.getName(), "::accountSyncGet Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }

        return result;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: check duplicate account
    private static int checkDuplicate(int accountId) {
        int userId = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select AccountId from "
                    + MySQLiteOpenHelper.TABLE_ACCOUNT
                    + " where AccountId=?", new String[]{accountId + ""});
            if (cursor.moveToFirst()) {
                userId = cursor.getInt(0);

            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(UserDB.class.getName() + "::checkDuplicate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(UserDB.class.getName(), "::checkDuplicate Error:", e.getMessage(), Utility.printStackTrace(e));
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }

        return userId;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: check duplicate account
    public static String getUserName(int accountId) {
        String useName = "";
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select FirstName,LastName from "
                    + MySQLiteOpenHelper.TABLE_ACCOUNT
                    + " where AccountId=?", new String[]{accountId + ""});
            if (cursor.moveToFirst()) {
                useName = cursor.getString(0) + " " + cursor.getString(1);

            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(UserDB.class.getName() + "::getUserName Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(UserDB.class.getName(), "::getUserName Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }

        return useName;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: add or update accounts in database
    public static void Save(ArrayList<UserBean> lst) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            for (int i = 0; i < lst.size(); i++) {
                UserBean bean = lst.get(i);

                values.put("AccountId", bean.getAccountId());
                values.put("FirstName", bean.getFirstName());
                values.put("LastName", bean.getLastName());
                values.put("EmailId", bean.getEmailId());
                values.put("MobileNo", bean.getMobileNo());
                values.put("AccountType", bean.getAccountType());
                values.put("DrivingLicense", bean.getDrivingLicense());
                values.put("DLIssueState", bean.getDlIssueState());
                values.put("Username", bean.getUserName());
                values.put("Password", bean.getPassword());
                values.put("Salt", bean.getSalt());
                values.put("ExemptELDUseFg", bean.getExemptELDUseFg());
                values.put("ExemptionRemarks", bean.getExemptionRemark());
                values.put("SpecialCategory", bean.getSpecialCategory());

                values.put("CurrentRule", bean.getCurrentRule());
                values.put("TimeZoneOffsetUTC", bean.getTimeZoneOffsetUTC());
                values.put("LicenseAcceptFg", bean.getLicenseAcceptFg());
                values.put("LicenseExpiryDate", bean.getLicenseExpiryDate());
                values.put("DotPassword", bean.getDotPassword());
                values.put("StatusId", bean.getStatusId());
                values.put("TimeZoneId", bean.getTimeZoneId());
                values.put("Address", bean.getFullAddress());
                values.put("LanguageForSupport", bean.getSupportLanguage());
                values.put("DEditFg", bean.getdEditFg());
                values.put("TrainedFg", bean.getTrainedFg());
                values.put("SyncFg", 1);
                int accountId = checkDuplicate(bean.getAccountId());
                if (accountId == 0) {
                    database.insert(MySQLiteOpenHelper.TABLE_ACCOUNT,
                            null, values);
                } else {
                    database.update(MySQLiteOpenHelper.TABLE_ACCOUNT, values,
                            " AccountId= ?", new String[]{bean.getAccountId()
                                    + ""});
                }
            }
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(UserDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(UserDB.class.getName(), "::Save Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(UserDB.class.getName() + "::Save close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(UserDB.class.getName(), "::Save Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }

    }

    public static String getSpecialCategory(int accountId) {
        String specialCategory = "";
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select SpecialCategory from "
                    + MySQLiteOpenHelper.TABLE_ACCOUNT
                    + " where AccountId=?", new String[]{accountId + ""});
            if (cursor.moveToFirst()) {
                specialCategory = cursor.getString(0);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(UserDB.class.getName() + "::getSpecialCategory Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(UserDB.class.getName(), "::getSpecialCategory Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }

        return specialCategory;
    }

    // Created By: Deepak Sharma
    // Created Date: 15 June 2016
    // Purpose:  update accounts in database
    public static void Update(String col, String val) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(col, val);
            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_ACCOUNT, values,
                    " AccountId= ?", new String[]{Utility.onScreenUserId
                            + ""});
            MainActivity.postData(CommonTask.Post_Account);
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(UserDB.class.getName() + "::Update 2 Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(UserDB.class.getName(), "::Update 2 Error:", e.getMessage(), Utility.printStackTrace(e));
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(UserDB.class.getName() + "::Update 2 close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                LogDB.writeLogs(UserDB.class.getName(), "::Update 2 Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }

    }

    // Created By: Deepak Sharma
    // Created Date: 15 June 2016
    // Purpose:  update accounts in database
    public static void Update(int rule, float timeZone, int acceptFg) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("CurrentRule", rule);
            values.put("TimeZoneOffsetUTC", timeZone);
            values.put("LicenseAcceptFg", acceptFg);
            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_ACCOUNT, values,
                    " AccountId= ?", new String[]{Utility.onScreenUserId
                            + ""});
            // Post account data
            MainActivity.postData(CommonTask.Post_Account);


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(UserDB.class.getName() + "::Update Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(UserDB.class.getName(), "::Update Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(UserDB.class.getName() + "::Update close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }

    }


    // Created By: Deepak Sharma
    // Created Date: 06 June 2020
    // Purpose: get and set User time zone
    public static void getUserTimeZone(int userId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String args[] = {userId + ""};
            cursor = database.rawQuery("select TimeZoneId  from "
                    + MySQLiteOpenHelper.TABLE_ACCOUNT
                    + " where AccountId=?", args);
            while (cursor.moveToNext()) {
                String timeZoneId = cursor.getString(cursor.getColumnIndex("TimeZoneId"));
                Utility.TimeZoneId = timeZoneId;
                Utility.TimeZoneOffset = ZoneList.getOffset(Utility.TimeZoneId);
                Utility.TimeZoneOffsetUTC = ZoneList.getTimeZoneOffset(Utility.TimeZoneId);
                Utility.sdf.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());

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
    // Created Date: 15 June 2016
    // Purpose:  update accounts in database
    public static void UpdateTrainedFg(SQLiteDatabase database) {

        try {
            ContentValues values = new ContentValues();
            values.put("TrainedFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_ACCOUNT, values,
                    null, null);
        } catch (Exception e) {

        }

    }

}