package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.beans.NotificationBean;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import java.util.ArrayList;

/**
 * Created by Dev-1 on 7/24/2018.
 */
public class NotificationDB {

    // Created By: Pallavi Wattamwar
    // Created Date: 30 July 2018
    // Purpose: to check duplicate record
    private static int getNotificationId(String notificationDate, int notificationId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int _id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id from "
                    + MySQLiteOpenHelper.TABLE_NOTIFICATION
                    + " where NotificationId=? and NotificationDate=?"
                    + " LIMIT 1", new String[]{notificationId + "", notificationDate});

            if (cursor.moveToFirst()) {
                _id = cursor.getInt(0);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(NotificationDB.class.getName() + "::getNotificationId Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(NotificationDB.class.getName(),"::getNotificationId Error:" ,e.getMessage(),Utility.printStackTrace(e));
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }
        return _id;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 30 July 2018
    // Purpose: save list of Notification

    public static void Save(ArrayList<NotificationBean> notificationList) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            for (NotificationBean bean : notificationList) {

                ContentValues values = new ContentValues();
                values.put("Title", bean.getTitle());
                values.put("NotificationId", bean.getNotificationID());
                values.put("NotificationDate", bean.getNotiFicationDate());
                values.put("StatusId", 0);
                values.put("Comment", bean.getComment());

                int messageId = getNotificationId(bean.getNotiFicationDate(), bean.getNotificationID());

                if (messageId == 0) {
                    database.insertOrThrow(MySQLiteOpenHelper.TABLE_NOTIFICATION,
                            "_id", values);
                } else {
                    database.update(MySQLiteOpenHelper.TABLE_NOTIFICATION, values,
                            " _id= ?", new String[]{messageId + ""});
                }

            }
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(NotificationDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(NotificationDB.class.getName(),"::Save Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(NotificationDB.class.getName() + "::Save close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 24 July 2018
    // Purpose: save Notification
    public static void Save(NotificationBean bean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("Title", bean.getTitle());
            values.put("NotificationId", bean.getNotificationID());
            values.put("NotificationDate", bean.getNotiFicationDate());
            values.put("StatusId", 0);
            values.put("Comment", bean.getComment());

            int messageId = getNotificationId(bean.getNotiFicationDate(), bean.getNotificationID());

            if (messageId == 0) {
                database.insertOrThrow(MySQLiteOpenHelper.TABLE_NOTIFICATION,
                        "_id", values);
            } else {
                database.update(MySQLiteOpenHelper.TABLE_NOTIFICATION, values,
                        " _id= ?", new String[]{messageId + ""});
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(NotificationDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(NotificationDB.class.getName(),"::Save Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(UserDB.class.getName() + "::Save close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
    }


    // Created By: Pallavi Wattamwar
    // Created Date: 24 July 2018
    // Purpose: get List of Notification
    public static ArrayList<NotificationBean> getNotification() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<NotificationBean> list = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select * from "
                    + MySQLiteOpenHelper.TABLE_NOTIFICATION + " Where StatusId=0 ", null);

            while (cursor.moveToNext()) {
                NotificationBean bean = new NotificationBean();
                bean.setTitle(cursor.getString(cursor.getColumnIndex("Title")));
                bean.setNotificationID(cursor.getInt(cursor.getColumnIndex("NotificationId")));
                bean.setNotiFicationDate(cursor.getString(cursor.getColumnIndex("NotificationDate")));
                bean.setComment(cursor.getString(cursor.getColumnIndex("Comment")));
                bean.setStatus(cursor.getInt(cursor.getColumnIndex("StatusId")));


                list.add(bean);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(NotificationDB.class.getName() + "::getNotification Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(NotificationDB.class.getName(),"::getNotification Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
                LogDB.writeLogs(NotificationDB.class.getName(),"::getNotification:" ,e2.getMessage(),Utility.printStackTrace(e2));

            }
        }
        return list;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 27 July 2018
    // Purpose: get Max Notification ID

    public static int getLastNotificationId() {
        int maxId = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("SELECT max(NotificationId) from Notification ", new String[]{});
            if (cursor != null)
                if (cursor.moveToFirst()) {
                    maxId = cursor.getInt(0);
                }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(NotificationDB.class.getName() + "::getLastNotificationId :" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(NotificationDB.class.getName(),"::getLastNotificationId Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }
        return maxId;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 30 July 2018
    // Purpose: Delete Notification / Update Status Id.

    public static void DeleteNotification(int notificationId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();


            values.put("StatusId", 1);

            database.update(MySQLiteOpenHelper.TABLE_NOTIFICATION, values,
                    " NotificationId= ?",
                    new String[]{""+notificationId});
        } catch (Exception e) {

            Utility.printError(e.getMessage());
            LogFile.write(NotificationDB.class.getName() + ":: Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(NotificationDB.class.getName(),"::DeleteNotification:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            database.close();
            helper.close();
        }


    }


}
