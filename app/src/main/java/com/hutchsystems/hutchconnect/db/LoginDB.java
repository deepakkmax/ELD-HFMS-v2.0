package com.hutchsystems.hutchconnect.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.beans.UserBean;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.common.ZoneList;

import java.util.TimeZone;

/**
 * Created by Deepak.Sharma on 1/14/2016.
 */
public class LoginDB {


    // Created By: Deepak Sharma
    // Created Date: 29 March 2016
    // Purpose: get Unidentified DriverId
    public static int getUnidentifiedDriverId() {
        int userId = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select AccountId from "
                    + MySQLiteOpenHelper.TABLE_ACCOUNT
                    + " where AccountType=2", null);
            if (cursor.moveToFirst()) {
                userId = cursor.getInt(0);

            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(LoginDB.class.getName() + "::getUnidentifiedDriverId Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(LoginDB.class.getName(),"::getUnidentifiedDriverId Error:" ,e.getMessage(),Utility.printStackTrace(e));

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
    // Created Date: 20 April 2015
    // Purpose: login user methods
    public static boolean LoginUser(String username, String password, boolean coDriverFg) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String args[] = {username.toLowerCase()};
            cursor = database.rawQuery("select AccountId,Username,FirstName,LastName,AccountType,Password,Salt,ExemptELDUseFg,SpecialCategory,DrivingLicense,DLIssueState,LicenseAcceptFg,LicenseExpiryDate,EmailId,MobileNo,DotPassword,TimeZoneId,Address,DEditFg,TrainedFg    from "
                    + MySQLiteOpenHelper.TABLE_ACCOUNT
                    + " where LOWER(userName)=? and StatusId=1", args);
            if (cursor.moveToFirst()) {
                int accountType = cursor.getInt(cursor.getColumnIndex("AccountType"));
                if (accountType > 2) {
                    Utility.errorMessage = "Please use valid driver Account to login.";
                    return false;
                }

                if (accountType == 1) {
                    //that's support personnel
                    Utility.errorMessage = "Support Personnel not authorized for ELD login";
                    return false;
                }

                int trainedFg= cursor.getInt(cursor.getColumnIndex("TrainedFg"));

                String encryptedPassword = cursor.getString(cursor.getColumnIndex("Password"));
                String salt = cursor.getString(cursor.getColumnIndex("Salt"));
                if (!Utility.computeSHAHash(password, salt).equals(encryptedPassword)) {
                    Utility.errorMessage = "Password is incorrect!";
                    return false;
                }
                UserBean user = new UserBean();
                user.setTrainedFg(trainedFg);
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
                user.setdEditFg(cursor.getInt(cursor.getColumnIndex("DEditFg")));
                if (coDriverFg) {
                    // prevent unidentified driver login if try to login as co driver
                    if (user.getAccountType() == 2) {
                        Utility.errorMessage = "Unidentified driver account is not allowed as co driver";
                        return false;
                    }

                    if (Utility.user1.getTimeZoneId() != null && !Utility.user1.getTimeZoneId().equals(user.getTimeZoneId())) {
                        Utility.errorMessage = "Team driver should be in same time zone. Please click the sync button after setting timezone from web!";
                        return false;
                    }

                    user.setFirstLoginFg(Utility.user2.isFirstLoginFg());

                    //re-login by co-driver
                    if (Utility.user2.getAccountId() > 0) {
                        user.setActive(Utility.user2.isActive());
                    }

                    Utility.user2 = user;
                } else {
                    user.setFirstLoginFg(Utility.user1.isFirstLoginFg());
                    //re-login by driver
                    if (Utility.user1.getAccountId() > 0) {
                        user.setActive(Utility.user1.isActive());
                    } else {
                        user.setActive(true);
                    }

                    Utility.user1 = user;

                }
                return true;
            } else {
                Utility.errorMessage = "Username is not found or incorrect\nPlease click the Sync button to retry";
                return false;
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(LoginDB.class.getName() + "::LoginUser Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(LoginDB.class.getName(),"::LoginUser Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }
        return false;
    }

    // Created By: Deepak Sharma
    // Created Date: 20 April 2015
    // Purpose: relogin
    public static void autoLoginUser(int driverId, int coDriverId, int activeDriverId, int onScreenUserId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String args[] = {driverId + "", coDriverId + ""};
            cursor = database.rawQuery("select AccountId,Username,FirstName,LastName,AccountType,Password,Salt,ExemptELDUseFg,SpecialCategory,DrivingLicense,DLIssueState,LicenseAcceptFg,LicenseExpiryDate,EmailId,MobileNo,DotPassword,TimeZoneId,Address,DEditFg    from "
                    + MySQLiteOpenHelper.TABLE_ACCOUNT
                    + " where AccountId in (?,?)", args);
            while (cursor.moveToNext()) {
                String encryptedPassword = cursor.getString(cursor.getColumnIndex("Password"));
                String salt = cursor.getString(cursor.getColumnIndex("Salt"));

                UserBean user = new UserBean();
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
                user.setdEditFg(cursor.getInt(cursor.getColumnIndex("DEditFg")));

                user.setFirstLoginFg(false);
                Utility.onScreenUserId = onScreenUserId;
                Utility.activeUserId = activeDriverId;
                if (user.getAccountId() == driverId) {

                    Utility.user1 = user;
                    Utility.user1.setActive(user.getAccountId() == activeDriverId);
                    Utility.user1.setOnScreenFg(user.getAccountId() == onScreenUserId);
                } else {
                    Utility.user2 = user;
                    Utility.user2.setActive(user.getAccountId() == activeDriverId);
                    Utility.user2.setOnScreenFg(user.getAccountId() == onScreenUserId);

                }

                if (user.getAccountId() == onScreenUserId) {
                    if (user.getTimeZoneId() != null && !user.getTimeZoneId().isEmpty()) {

                        Utility.TimeZoneId = user.getTimeZoneId();
                        Utility.TimeZoneOffset = ZoneList.getOffset(Utility.TimeZoneId);
                        Utility.TimeZoneOffsetUTC = ZoneList.getTimeZoneOffset(Utility.TimeZoneId);
                        Utility.sdf.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
                    }
                }
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(LoginDB.class.getName() + "::LoginUser Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(LoginDB.class.getName(),"::LoginUser Error:" ,e.getMessage(),Utility.printStackTrace(e));

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
    // Created Date: 20 April 2015
    // Purpose: authenticate codriver
    public static boolean authCoDriver(int coDriverId, String password) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String args[] = {coDriverId + ""};
            cursor = database.rawQuery("select AccountId,Username,FirstName,LastName,AccountType,Password,Salt,ExemptELDUseFg,SpecialCategory,DrivingLicense,DLIssueState,LicenseAcceptFg from "
                    + MySQLiteOpenHelper.TABLE_ACCOUNT
                    + " where AccountId=? and StatusId=1", args);
            if (cursor.moveToFirst()) {
                String encryptedPassword = cursor.getString(cursor.getColumnIndex("Password"));
                String salt = cursor.getString(cursor.getColumnIndex("Salt"));

                if (Utility.computeSHAHash(password, salt).equals(encryptedPassword)) {
                    status = true;
                }
            }

        } catch (Exception e) {

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


    // Created By: Pallavi Wattamwar
    // Created Date: 14/12/2018
    // Purpose: get Status of the driver to check driver status is active
    // Parameter: int : driver Id
    // Parameter : int : co Driver Id
    public static boolean checkDriverStatus(int driverId,int coDriverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            String args[] = {driverId + "", coDriverId + ""};

            cursor = database.rawQuery("select StatusId from "
                    + MySQLiteOpenHelper.TABLE_ACCOUNT
                    + " where AccountId in (?,?) ", args);


            if (cursor.moveToFirst()) {
                int statusId = cursor.getInt(cursor.getColumnIndex("StatusId"));
                // if statusId == 1 then driver Status is active, if it is 0 means driver status in inactive
                if(statusId == 1)
                    return  true;
            }

        } catch (Exception e) {
            LogFile.write(LoginDB.class.getName() + "::checkDriverStatus Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(LoginDB.class.getName(),"::checkDriverStatus Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {

            }
        }
        return false;
    }

}
