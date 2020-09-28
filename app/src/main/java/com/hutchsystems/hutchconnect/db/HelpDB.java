package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.beans.TicketBean;
import com.hutchsystems.hutchconnect.common.Utility;

import java.util.ArrayList;

/**
 * Created by Deepak Sharma on 1/12/2018.
 */

public class HelpDB {

    // Created By: Deepak Sharma
    // Created Date: 3 November 2016
    // Purpose: add or update dtc code in database
    public static boolean Save(int type, int ticketId, String ticketNo, String title, String comment, String ticketDate, int ticketStatus) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select _id from "
                    + MySQLiteOpenHelper.TABLE_TICKET_DETAIL
                    + " where TicketId=?", new String[]{ticketId + ""});

            ContentValues values = new ContentValues();
            if (cursor.moveToFirst()) {
                values.put("TicketNo", ticketNo);
                values.put("ModifiedBy", Utility.onScreenUserId);
                values.put("ModifiedDate", ticketDate);
                values.put("TicketStatus", ticketStatus);
                database.update(MySQLiteOpenHelper.TABLE_TICKET_DETAIL,
                        values,
                        " TicketId=?", new String[]{ticketId + ""});
            } else {
                values.put("TicketId", ticketId);
                values.put("Type", type);
                values.put("TicketNo", ticketNo);
                values.put("TicketDate", ticketDate);
                values.put("Title", title);
                values.put("Comment", comment);
                values.put("TicketStatus", ticketStatus);
                values.put("DriverId", Utility.onScreenUserId);
                values.put("CreatedBy", Utility.onScreenUserId);
                values.put("CreatedDate", ticketDate);

                // for rating and user comment
                values.put("Rating",0);
                values.put("UserFeedback","");
                database.insert(MySQLiteOpenHelper.TABLE_TICKET_DETAIL,
                        "_id", values);
            }


        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        } finally {
            // for handling of database connection issue
            cursor.close();
            database.close();
            helper.close();
        }
        return status;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 26 September 2018
    // Purpose: getAllTicket
    public static ArrayList<TicketBean> GetAllTicket() {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<TicketBean> list = new ArrayList<>();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select TicketId,TicketNo,TicketStatus,Type,Title,Rating,UserFeedback from "
                    + MySQLiteOpenHelper.TABLE_TICKET_DETAIL
                    + " where DriverId=? order by TicketId DESC", new String[]{Utility.onScreenUserId + ""});

            while (cursor.moveToNext()) {
                TicketBean bean = new TicketBean();
                bean.setTicketId(cursor.getInt(cursor.getColumnIndex("TicketId")));
                bean.setTicketNo(cursor.getString(cursor.getColumnIndex("TicketNo")));
                bean.setTicketStatus(cursor.getInt(cursor.getColumnIndex("TicketStatus")));
                bean.setType(cursor.getInt(cursor.getColumnIndex("Type")));

                // for rating and user comment
                bean.setRating(cursor.getInt(cursor.getColumnIndex("Rating")));
                bean.setUserFeedback(cursor.getString(cursor.getColumnIndex("UserFeedback")));
                bean.setTitle(cursor.getString(cursor.getColumnIndex("Title")));

                list.add(bean);
            }


        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        } finally {
            cursor.close();
            database.close();
            helper.close();
        }
        return list;
    }

    // Created By: Deepak Sharma
    // Created Date: 3 November 2016
    // Purpose: add or update dtc code in database
    public static ArrayList<TicketBean> Get() {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<TicketBean> list = new ArrayList<>();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select TicketId,TicketNo,TicketStatus,Type,Comment,Title from "
                    + MySQLiteOpenHelper.TABLE_TICKET_DETAIL
                    + " where DriverId=? and TicketStatus in (1,2,3,4)", new String[]{Utility.onScreenUserId + ""});

            while (cursor.moveToNext()) {
                TicketBean bean = new TicketBean();
                bean.setTicketId(cursor.getInt(cursor.getColumnIndex("TicketId")));
                bean.setTicketNo(cursor.getString(cursor.getColumnIndex("TicketNo")));
                bean.setTicketStatus(cursor.getInt(cursor.getColumnIndex("TicketStatus")));
                bean.setType(cursor.getInt(cursor.getColumnIndex("Type")));
                bean.setComment(cursor.getString(cursor.getColumnIndex("Comment")));
                bean.setTitle(cursor.getString(cursor.getColumnIndex("Title")));
                list.add(bean);
            }


        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        } finally {
            cursor.close();
            database.close();
            helper.close();
        }
        return list;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 25 September 2018
    // Purpose: update cooment and rating in database
    public static boolean updateCommentAndRating( int TicketId, String UserFeedback, int rating) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            ContentValues values = new ContentValues();

                values.put("SyncFg", 0);
                values.put("Rating",rating);
                values.put("UserFeedback",UserFeedback);

                database.update(MySQLiteOpenHelper.TABLE_TICKET_DETAIL,
                        values,
                        " TicketId=?", new String[]{TicketId + ""});


        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        } finally {
            database.close();
            helper.close();
        }
        return status;
    }
}
