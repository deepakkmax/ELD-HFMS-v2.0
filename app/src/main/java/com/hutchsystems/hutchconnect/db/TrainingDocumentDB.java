package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.common.Constants;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import java.util.ArrayList;
import java.util.List;

public class TrainingDocumentDB {




    // Created By: Sahil Bansal
    // Created Date: 28 February 2020
    // Purpose: add or update training code in database
    public static boolean Save(List<TrainingDocumentBean> list) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        //Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();


           for (TrainingDocumentBean bean : list) {

               ContentValues values = new ContentValues();
               values.put(Constants.CREATEDDATE, bean.getCreatedDate());
               values.put(Constants.DOCUMENTCONTENTTYPE, bean.getDocumentContentType());

               values.put(Constants.DOCUMENTID, bean.getDocumentId());
               values.put(Constants.DOCUMENTNAME, bean.getDocumentName());
               values.put(Constants.DOCUMENTPATH, bean.getDocumentPath());

               values.put(Constants.DOCUMENTSIZE, bean.getDocumentSize());
               values.put(Constants.DOCUMENTTYPE, bean.getDocumentType());
               values.put(Constants.IMPORTEDDATE, bean.getImportedDate());
               values.put(Constants.MODIFIEDDATE, bean.getModifiedDate());
               values.put(Constants.STATUSID, bean.getStatusId());

               //check the duplicate Document
               int id = checkDuplicateDocument( bean.getDocumentId());

               if (id == 0) {
                   database.insert(MySQLiteOpenHelper.TABLE_TRAINING_DOCUMENT,
                           Constants.DOCUMENTID, values);
               }else {
                   //check the document is require for update or not
                   int documentId = checkDocument( bean.getDocumentId(),bean.getModifiedDate());
                   // skip the document if update is not require
                    if(documentId == 0){

                        database.update(MySQLiteOpenHelper.TABLE_TRAINING_DOCUMENT,
                                values,Constants.DOCUMENTID+" =?" , new String[]{String.valueOf(bean.getDocumentId())});
                    }
               }
           }

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



    // Created By: Sahil Bansal
    // Created Date: 28 February 2020
    // Purpose: check duplicate Document
    private static int checkDuplicateDocument(int DocumentId) {
        int documentId = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select "+Constants.DOCUMENTID+" from "
                    + MySQLiteOpenHelper.TABLE_TRAINING_DOCUMENT
                    + " where " + Constants.DOCUMENTID + "=?", new String[]{DocumentId+""});

            if (cursor.moveToFirst()) {
                documentId = cursor.getInt(0);

            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(TrainingDocumentDB.class.getName() + "::checkDuplicate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TrainingDocumentDB.class.getName(), "::checkDuplicate Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }

        return documentId;
    }

    // Created By: Sahil Bansal
    // Created Date: 28 February 2020
    // Purpose:  //check the document is require for update or not
    private static int checkDocument(int DocumentId,String modifyDate) {
        int documentId = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select "+Constants.DOCUMENTID+" from "
                    + MySQLiteOpenHelper.TABLE_TRAINING_DOCUMENT
                    + " where " + Constants.MODIFIEDDATE + "=?"+" and " + Constants.DOCUMENTID + "=?", new String[]{modifyDate,DocumentId+""});

            if (cursor.moveToFirst()) {
                documentId = cursor.getInt(0);

            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(TrainingDocumentDB.class.getName() + "::checkDuplicate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TrainingDocumentDB.class.getName(), "::checkDuplicate Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
            }
        }

        return documentId;
    }


    // Created By: Sahil Bansal
    // Created Date: 28 February 2020
    // Purpose: get all training document
    // Modified By: Deepak Sharma
    // Modified Date: 25 March 2020
    // Purpose: bug in method. wrong table has been used instead of TABLE_TRAINING_DOCUMENT and was only picking 1 record
    public static ArrayList<TrainingDocumentBean> getDocuments() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        ArrayList<TrainingDocumentBean> list = new ArrayList<>();


        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select _id, CreatedDate, DocumentContentType, DocumentId, DocumentName, DocumentPath, DocumentSize, DocumentType, ImportedDate, ModifiedDate, StatusId from "
                    + MySQLiteOpenHelper.TABLE_TRAINING_DOCUMENT
                    + " order by ImportedDate", null);

            while (cursor.moveToNext()) {
                TrainingDocumentBean bean = new TrainingDocumentBean();
                bean.set_id(cursor.getInt(0));
                bean.setCreatedDate(cursor.getString(1));
                bean.setDocumentContentType(cursor.getString(2));

                bean.setDocumentId(cursor.getInt(3));
                bean.setDocumentName(cursor.getString(4));
                bean.setDocumentPath(cursor.getString(5));

                bean.setDocumentSize(cursor.getInt(6));
                bean.setDocumentType(cursor.getString(7));
                bean.setImportedDate(cursor.getString(8));

                bean.setModifiedDate(cursor.getString(9));
                bean.setStatusId(cursor.getInt(10));
                list.add(bean);
            }


        } catch (Exception e) {
            Utility.printError(e.getMessage());
        } finally {
            cursor.close();
            database.close();
            helper.close();
        }
        return list;
    }
    public static long delete(int id) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        long res = -1;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            res = database.delete(MySQLiteOpenHelper.TABLE_TRAINING_DOCUMENT,
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
