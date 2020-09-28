package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.beans.DocumentDetailBean;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.DocumentType;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Deepak Sharma on 6/8/2017.
 */

public class DocumentDetailDB {


    // Created By: Deepak Sharma
    // Created Date: 9 June 2017
    // Purpose: update Document detail
    public static JSONArray DocumentDetailSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_DOCUMENT_DETAIL, values,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {

            LogFile.write(DocumentDetailDB.class.getName() + "::DocumentDetailSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DocumentDetailDB.class.getName(),"::DocumentDetailSyncUpdate Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

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
    // Created Date: 15 Febuary 2017
    // Purpose: get document to sync
    public static JSONArray GetSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select DocumentNo, DocumentType,DocumentPath,CreatedDate,DriverId  from "
                    + MySQLiteOpenHelper.TABLE_DOCUMENT_DETAIL
                    + " where SyncFg=0 order by _id desc", null);

            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("DocumentNo", cursor.getString(cursor.getColumnIndex("DocumentNo")));
                obj.put("DocumentType", cursor.getInt(cursor.getColumnIndex("DocumentType")));
                obj.put("DocumentPath", cursor.getString(cursor.getColumnIndex("DocumentPath")));
                obj.put("CreatedDate", cursor.getString(cursor.getColumnIndex("CreatedDate")));
                obj.put("DriverId", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("VehicleId", Utility.vehicleId);
                obj.put("CompanyId", Utility.companyId);
                array.put(obj);
            }


        } catch (Exception e) {
            Utility.printError(e.getMessage());
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e1) {
            }
        }
        return array;
    }

    // Created By: Deepak Sharma
    // Created Date: 09 June 2017
    // Purpose: get document to upload
    public static ArrayList<DocumentDetailBean> Get(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<DocumentDetailBean> list = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select DocumentNo, DocumentType,DocumentPath,CreatedDate  from "
                    + MySQLiteOpenHelper.TABLE_DOCUMENT_DETAIL
                    + " where DriverId=? order by _id desc", new String[]{driverId + ""});

            while (cursor.moveToNext()) {
                DocumentDetailBean bean = new DocumentDetailBean();
                bean.setDocumentNo(cursor.getString(cursor.getColumnIndex("DocumentNo")));
                bean.setDocumentType(cursor.getInt(cursor.getColumnIndex("DocumentType")));
                bean.setDocumentPath(cursor.getString(cursor.getColumnIndex("DocumentPath")));
                bean.setCreatedDate(cursor.getString(cursor.getColumnIndex("CreatedDate")));
                list.add(bean);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e1) {
            }
        }
        return list;
    }


    // Created By: Deepak Sharma
    // Created Date: 15 Febuary 2017
    // Purpose: get document to upload
    public static ArrayList<DocumentDetailBean> GetById(int id) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<DocumentDetailBean> list = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select DocumentNo, DocumentType,DocumentPath,CreatedDate  from "
                    + MySQLiteOpenHelper.TABLE_DOCUMENT_DETAIL
                    + "where _id=? ", new String[]{id + ""});

            while (cursor.moveToNext()) {
                DocumentDetailBean bean = new DocumentDetailBean();
                bean.setDocumentNo(cursor.getString(cursor.getColumnIndex("DocumentNo")));
                bean.setDocumentType(cursor.getInt(cursor.getColumnIndex("DocumentType")));
                bean.setDocumentPath(cursor.getString(cursor.getColumnIndex("DocumentPath")));
                bean.setCreatedDate(cursor.getString(cursor.getColumnIndex("CreatedDate")));
                list.add(bean);
            }


        } catch (Exception e) {
            Utility.printError(e.getMessage());
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e1) {
            }
        }
        return list;
    }

    // Created By: Deepak Sharma
    // Created Date: 08 June 2017
    // Purpose: add document to database
    public static boolean Save(DocumentDetailBean bean) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("DocumentType", bean.getDocumentType());
            values.put("DocumentNo", bean.getDocumentNo());
            values.put("DocumentPath", bean.getDocumentPath());
            values.put("DriverId", bean.getDriverId());
            values.put("CreatedDate", bean.getCreatedDate());
            values.put("SyncFg", 0);
            database.insert(MySQLiteOpenHelper.TABLE_DOCUMENT_DETAIL,
                    "_id", values);
           //Post Document
            MainActivity.postData(CommonTask.Post_DocumentDetail);

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e1) {
            }
        }
        return status;
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

            cursor = database.rawQuery("select  DocumentPath from "
                            + MySQLiteOpenHelper.TABLE_DOCUMENT_DETAIL
                            + " Where CreatedDate < ?"
                    , new String[]{date});

            while (cursor.moveToNext()) {
                String fileName = cursor.getString(cursor.getColumnIndex("DocumentPath"));
                Utility.DeleteFile(DocumentType.DOCUMENT_OTHER, fileName);

            }

        } catch (Exception e) {
            LogFile.write(DocumentDetailDB.class.getName() + "::deleteDocument Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DocumentDetailDB.class.getName(),"::deleteDocument Error:" ,e.getMessage(),Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }
    }
}
