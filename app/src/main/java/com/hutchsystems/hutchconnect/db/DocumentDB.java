package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.beans.DocumentBean;
import com.hutchsystems.hutchconnect.common.Utility;

/**
 * Created by Deepak Sharma on 2/15/2017.
 */

public class DocumentDB {

    // Created By: Deepak Sharma
    // Created Date: 15 Febuary 2017
    // Purpose: get document to upload
    public static DocumentBean Get() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        DocumentBean bean = new DocumentBean();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select _id, DocType, DocPath from "
                    + MySQLiteOpenHelper.TABLE_DOCUMENT_UPLOAD_PENDING
                    + " order by _id desc LIMIT 1", null);

            if (cursor.moveToFirst()) {
                bean.set_id(cursor.getInt(0));
                bean.setType(cursor.getString(1));
                bean.setPath(cursor.getString(2));
            }


        } catch (Exception e) {
            Utility.printError(e.getMessage());
        } finally {
            cursor.close();
            database.close();
            helper.close();
        }
        return bean;
    }

    // Created By: Deepak Sharma
    // Created Date: 3 November 2016
    // Purpose: add or update dtc code in database
    public static boolean Save(String type, String path) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        //Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

           /* cursor = database.rawQuery("select _id from "
                    + MySQLiteOpenHelper.TABLE_DOCUMENT_UPLOAD_PENDING
                    + " where DocPath=?", new String[]{path});

            if (cursor.moveToFirst()) {*/
            ContentValues values = new ContentValues();
            values.put("DocType", type);
            values.put("DocPath", path);
            database.insert(MySQLiteOpenHelper.TABLE_DOCUMENT_UPLOAD_PENDING,
                    "_id", values);
            //}


        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        } finally {
            //cursor.close();
            database.close();
            helper.close();
        }
        return status;
    }

    public static long delete(int id) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        long res = -1;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            res = database.delete(MySQLiteOpenHelper.TABLE_DOCUMENT_UPLOAD_PENDING,
                    "_id=?", new String[]{id + ""});

        } catch (Exception e) {

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }

}
