package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.beans.Annotation;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;

import java.util.ArrayList;

public class AnnotationDetailDB {

    // Created By: Pallavi Wattamwar
    // Created Date: 1 May 2019
    // Purpose: add or update annotation in database
    public static boolean Save(ArrayList<Annotation> lst) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();


            ContentValues values = new ContentValues();
            for (int i = 0; i < lst.size(); i++) {
                Annotation bean = lst.get(i);

                values.put("AnnotaionId", bean.getAnnotationId());
                values.put("CompanyId", bean.getCompanyID());
                values.put("DisplayOrder", bean.getDisplayOrder());
                values.put("Annotation", bean.getAnnotationValue());


                database.insert(MySQLiteOpenHelper.TABLE_ANNOTATION_DETAIL,
                        null, values);

            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(AnnotationDetailDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(AnnotationDetailDB.class.getName(), "::Save Error:", e.getMessage(), Utility.printStackTrace(e));
        } finally {
            database.close();
            helper.close();
        }
        return status;
    }

    public static long removeAnnotation() {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        long res = -1;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            res = database.delete(MySQLiteOpenHelper.TABLE_ANNOTATION_DETAIL,
                    null, null);


        } catch (Exception e) {
            System.out.println("removeAnnotation");
            e.printStackTrace();
            LogFile.write(TrackingDB.class.getName() + "::removeAnnotation Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TripInspectionDB.class.getName(), "::removeAnnotation Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return res;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 1 May 2019
    // Purpose: get List of annotation
    public static ArrayList<String> AnnotationGet() {
        ArrayList<String> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select Annotation from "
                            + MySQLiteOpenHelper.TABLE_ANNOTATION_DETAIL + "  order by DisplayOrder"
                    , null);

            while (cursor.moveToNext()) {
                list.add(cursor.getString(0));
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(AnnotationDetailDB.class.getName() + "::AnnotationGet Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(AnnotationDetailDB.class.getName(), "::AnnotationGet Error:", e.getMessage(), Utility.printStackTrace(e));
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
