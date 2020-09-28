package com.hutchsystems.hutchconnect.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.beans.DispatchDetailBean;
import com.hutchsystems.hutchconnect.beans.DrayageDispatchBean;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.table.DrayageDispatchContract.*;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Deepak Sharma on 7/21/2017.
 */

public class DrayageDispatchDB {

    // Created By: Deepak Sharma
    // Created Date: 21 July 2017
    // Purpose: save data from web to dispatch
    public static boolean Save(ArrayList<DrayageDispatchBean> list) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        ArrayList<DispatchDetailBean> dispatchDetail = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            for (DrayageDispatchBean bean : list) {
                ContentValues values = new ContentValues();
                values.put(DrayageDispatch.COLUMN_DISPATCHID, bean.getDispatchId());
                values.put(DrayageDispatch.COLUMN_DRIVERID, bean.getDriverId());
                values.put(DrayageDispatch.COLUMN_DISPATCHDATE, bean.getDispatchDate());
                values.put(DrayageDispatch.COLUMN_DISPATCHNO, bean.getDispatchNo());
                values.put(DrayageDispatch.COLUMN_BOOKINGNO, bean.getBookingNo());

                values.put(DrayageDispatch.COLUMN_CUSTOMER, bean.getCustomer());
                values.put(DrayageDispatch.COLUMN_CUSTOMERADDRESS, bean.getCustomerAddress());
                values.put(DrayageDispatch.COLUMN_CUSTOMERLATITUDE, bean.getCustomerLatitude());
                values.put(DrayageDispatch.COLUMN_CUSTOMERLONGITUDE, bean.getCustomerLongitude());

                values.put(DrayageDispatch.COLUMN_STEAMSHIPLINECOMPANY, bean.getSteamshipLineCompany());
                values.put(DrayageDispatch.COLUMN_SSLADDRESS, bean.getSslAddress());
                values.put(DrayageDispatch.COLUMN_SSLLATITUDE, bean.getSslLatitude());
                values.put(DrayageDispatch.COLUMN_SSLLONGITUDE, bean.getSslLongitude());

                values.put(DrayageDispatch.COLUMN_PICKUPCOMPANY, bean.getPickupCompany());
                values.put(DrayageDispatch.COLUMN_PICKUPADDRESS, bean.getPickupAddress());
                values.put(DrayageDispatch.COLUMN_PICKUPLATITUDE, bean.getPickupLatitude());
                values.put(DrayageDispatch.COLUMN_PICKUPLONGITUDE, bean.getPickupLongitude());

                values.put(DrayageDispatch.COLUMN_DROPCOMPANY, bean.getDropCompany());
                values.put(DrayageDispatch.COLUMN_DROPADDRESS, bean.getDropAddress());
                values.put(DrayageDispatch.COLUMN_DROPLATITUDE, bean.getDropLatitude());
                values.put(DrayageDispatch.COLUMN_DROPLONGITUDE, bean.getDropLongitude());

                values.put(DrayageDispatch.COLUMN_EMPTYRETURNCOMPANY, bean.getEmptyReturnCompany());
                values.put(DrayageDispatch.COLUMN_EMPTYRETURNADDRESS, bean.getEmptyReturnAddress());
                values.put(DrayageDispatch.COLUMN_EMPTYRETURNLATITUDE, bean.getEmptyReturnLatitude());
                values.put(DrayageDispatch.COLUMN_EMPTYRETURNLONGITUDE, bean.getEmptyReturnLongitude());

                values.put(DrayageDispatch.COLUMN_NOOFCONTAINER, bean.getNoOfContainer());
                values.put(DrayageDispatch.COLUMN_NOTES, bean.getNotes());


                String[] projection = {DrayageDispatch.COLUMN_DISPATCHID};
                String selections = DrayageDispatch.COLUMN_DISPATCHID + "= ?";
                String[] selectionArgs = {bean.getDispatchId() + ""};
                Cursor cursor = database.query(DrayageDispatch.TABLE_NAME, projection, selections, selectionArgs, null, null, null);
                if (cursor.moveToNext()) {
                    database.update(DrayageDispatch.TABLE_NAME, values, selections, selectionArgs);
                } else {
                    values.put(DrayageDispatch.COLUMN_STATUS, 1);
                    values.put(DrayageRouteDetail.COLUMN_SYNCFG, 1);
                    database.insertOrThrow(DrayageDispatch.TABLE_NAME, DrayageDispatch._ID, values);
                }
                cursor.close();
                dispatchDetail.addAll(bean.getDispatchDetailBean());
            }

        } catch (Exception exe) {
            status = false;
            Utility.printError(exe.getMessage());
            LogFile.write(DrayageDispatchDB.class.getName() + "::Save Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DrayageDispatchDB.class.getName(),"::Save Error:" ,exe.getMessage(),Utility.printStackTrace(exe));
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e1) {

            }
        }

        if (dispatchDetail.size() > 0) {
            SaveDetail(dispatchDetail);
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 25 July 2017
    // Purpose: save data of dispatch detail from server
    public static boolean SaveDetail(ArrayList<DispatchDetailBean> list) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            for (DispatchDetailBean bean : list) {
                ContentValues values = new ContentValues();
                values.put(DrayageDispatchDetail.COLUMN_DISPATCHID, bean.getDispatchId());
                values.put(DrayageDispatchDetail.COLUMN_PICKDROPID, bean.getPickDropId());
                values.put(DrayageDispatchDetail.COLUMN_DISPATCHDETAILID, bean.getDispatchDetailId());
                values.put(DrayageDispatchDetail.COLUMN_APPOINTMENTNO, bean.getAppointmentNo());
                values.put(DrayageDispatchDetail.COLUMN_APPOINTMENTDATE, bean.getAppointmentDate());
                values.put(DrayageDispatchDetail.COLUMN_CONTAINERNO, bean.getContainerNo());
                values.put(DrayageDispatchDetail.COLUMN_CONTAINERSIZE, bean.getContainerSize());
                values.put(DrayageDispatchDetail.COLUMN_CONTAINERTYPE, bean.getContainerType());
                values.put(DrayageDispatchDetail.COLUMN_CONTAINERGRADE, bean.getContainerGrade());
                values.put(DrayageDispatchDetail.COLUMN_SYNCFG, 1);

                String[] projection = {DrayageDispatchDetail.COLUMN_DISPATCHDETAILID};
                String selections = DrayageDispatchDetail.COLUMN_DISPATCHDETAILID + "= ?";
                String[] selectionArgs = {bean.getDispatchDetailId() + ""};

                Cursor cursor = database.query(DrayageDispatchDetail.TABLE_NAME, projection, selections, selectionArgs, null, null, null);
                if (cursor.moveToNext()) {
                    database.update(DrayageDispatchDetail.TABLE_NAME, values, selections, selectionArgs);
                } else {
                    database.insertOrThrow(DrayageDispatchDetail.TABLE_NAME, null, values);
                }
                cursor.close();
            }

        } catch (Exception exe) {
            status = false;
            Utility.printError(exe.getMessage());
            LogFile.write(DrayageDispatchDB.class.getName() + "::SaveDetail Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DrayageDispatchDB.class.getName(),"::SaveDetail Error:" ,exe.getMessage(),Utility.printStackTrace(exe));
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
    // Created Date: 26 July 2017
    // Purpose: save time of arrival and depart from a particular pick or drop point
    public static boolean SaveRoute(String date, int pickDropId, int dispatchId, int enterFg) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DrayageRouteDetail.COLUMN_DISPATCHID, dispatchId);
            values.put(DrayageRouteDetail.COLUMN_PICKDROPID, pickDropId);
            values.put(DrayageRouteDetail.COLUMN_SYNCFG, 0);

            String[] projection = {DrayageRouteDetail.COLUMN_DISPATCHID};
            String selections = DrayageRouteDetail.COLUMN_DISPATCHID + "= ? & " + DrayageRouteDetail.COLUMN_PICKDROPID + "= ? ";
            String[] selectionArgs = {dispatchId + "", pickDropId + ""};

            cursor = database.query(DrayageRouteDetail.TABLE_NAME, projection, selections, selectionArgs, null, null, null);
            if (cursor.moveToNext()) {
                values.put(DrayageRouteDetail.COLUMN_DEPARTUREDATE, date);
                database.update(DrayageRouteDetail.TABLE_NAME, values, selections, selectionArgs);
            } else {
                values.put(DrayageRouteDetail.COLUMN_ARRIVALDATE, date);
                database.insertOrThrow(DrayageRouteDetail.TABLE_NAME, null, values);
            }
            MainActivity.postData(CommonTask.Post_RouteDetail);
        } catch (Exception exe) {
            status = false;
            Utility.printError(exe.getMessage());
            LogFile.write(DrayageDispatchDB.class.getName() + "::SaveRoute Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DrayageDispatchDB.class.getName(),"::SaveRoute Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e1) {

            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 25 July 2017
    // Purpose: get dispatch list for specific driver of specific date
    public static ArrayList<DrayageDispatchBean> Get(int driverId, String date) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<DrayageDispatchBean> list = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            String[] projection = {DrayageDispatch._ID,
                    DrayageDispatch.COLUMN_DISPATCHID,
                    DrayageDispatch.COLUMN_DRIVERID,
                    DrayageDispatch.COLUMN_DISPATCHDATE,
                    DrayageDispatch.COLUMN_DISPATCHNO,
                    DrayageDispatch.COLUMN_BOOKINGNO,
                    DrayageDispatch.COLUMN_CUSTOMER,
                    DrayageDispatch.COLUMN_CUSTOMERADDRESS,
                    DrayageDispatch.COLUMN_CUSTOMERLATITUDE,
                    DrayageDispatch.COLUMN_CUSTOMERLONGITUDE,

                    DrayageDispatch.COLUMN_STEAMSHIPLINECOMPANY,
                    DrayageDispatch.COLUMN_SSLADDRESS,
                    DrayageDispatch.COLUMN_SSLLATITUDE,
                    DrayageDispatch.COLUMN_SSLLONGITUDE,

                    DrayageDispatch.COLUMN_PICKUPCOMPANY,
                    DrayageDispatch.COLUMN_PICKUPADDRESS,
                    DrayageDispatch.COLUMN_PICKUPLATITUDE,
                    DrayageDispatch.COLUMN_PICKUPLONGITUDE,

                    DrayageDispatch.COLUMN_DROPCOMPANY,
                    DrayageDispatch.COLUMN_DROPADDRESS,
                    DrayageDispatch.COLUMN_DROPLATITUDE,
                    DrayageDispatch.COLUMN_DROPLONGITUDE,

                    DrayageDispatch.COLUMN_EMPTYRETURNCOMPANY,
                    DrayageDispatch.COLUMN_EMPTYRETURNADDRESS,
                    DrayageDispatch.COLUMN_EMPTYRETURNLATITUDE,
                    DrayageDispatch.COLUMN_EMPTYRETURNLONGITUDE,

                    DrayageDispatch.COLUMN_NOOFCONTAINER,
                    DrayageDispatch.COLUMN_STATUS,
                    DrayageDispatch.COLUMN_NOTES};
            String selection = DrayageDispatch.COLUMN_DRIVERID + "= ? and " + DrayageDispatch.COLUMN_DISPATCHDATE + "= ?";
            String[] selectionArgs = {driverId + "", date};
            cursor = database.query(DrayageDispatch.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
            while (cursor.moveToNext()) {
                DrayageDispatchBean bean = new DrayageDispatchBean();
                bean.setDispatchId(cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_DISPATCHID)));
                bean.setDispatchDate(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_DISPATCHDATE)));
                bean.setDispatchNo(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_DISPATCHNO)));
                bean.setBookingNo(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_BOOKINGNO)));
                bean.setCustomer(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_CUSTOMER)));
                bean.setCustomerAddress(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_CUSTOMERADDRESS)));
                bean.setCustomerLatitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_CUSTOMERLATITUDE)));
                bean.setCustomerLongitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_CUSTOMERLONGITUDE)));
                bean.setSteamshipLineCompany(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_STEAMSHIPLINECOMPANY)));
                bean.setSslAddress(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_SSLADDRESS)));
                bean.setSslLatitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_SSLLATITUDE)));
                bean.setSslLongitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_SSLLONGITUDE)));
                bean.setPickupCompany(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_PICKUPCOMPANY)));
                bean.setPickupAddress(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_PICKUPADDRESS)));
                bean.setPickupLatitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_PICKUPLATITUDE)));
                bean.setPickupLongitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_PICKUPLONGITUDE)));
                bean.setDropCompany(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_DROPCOMPANY)));
                bean.setDropAddress(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_DROPADDRESS)));
                bean.setDropLatitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_DROPLATITUDE)));
                bean.setDropLongitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_DROPLONGITUDE)));
                bean.setEmptyReturnCompany(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_EMPTYRETURNCOMPANY)));
                bean.setEmptyReturnAddress(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_EMPTYRETURNADDRESS)));
                bean.setEmptyReturnLatitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_EMPTYRETURNLATITUDE)));
                bean.setEmptyReturnLongitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_EMPTYRETURNLONGITUDE)));
                bean.setNoOfContainer(cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_NOOFCONTAINER)));
                bean.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_NOTES)));
                bean.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_STATUS)));
                list.add(bean);
            }

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DrayageDispatchDB.class.getName() + "::Get Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DrayageDispatchDB.class.getName(),"::Get Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

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
    // Created Date: 25 July 2017
    // Purpose: get dispatch info by dispatch id
    public static DrayageDispatchBean GetById(int dispatchId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        DrayageDispatchBean bean = new DrayageDispatchBean();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            String[] projection = {DrayageDispatch._ID,
                    DrayageDispatch.COLUMN_DISPATCHID,
                    DrayageDispatch.COLUMN_DRIVERID,
                    DrayageDispatch.COLUMN_DISPATCHDATE,
                    DrayageDispatch.COLUMN_DISPATCHNO,
                    DrayageDispatch.COLUMN_BOOKINGNO,
                    DrayageDispatch.COLUMN_CUSTOMER,
                    DrayageDispatch.COLUMN_CUSTOMERADDRESS,
                    DrayageDispatch.COLUMN_CUSTOMERLATITUDE,
                    DrayageDispatch.COLUMN_CUSTOMERLONGITUDE,

                    DrayageDispatch.COLUMN_STEAMSHIPLINECOMPANY,
                    DrayageDispatch.COLUMN_SSLADDRESS,
                    DrayageDispatch.COLUMN_SSLLATITUDE,
                    DrayageDispatch.COLUMN_SSLLONGITUDE,

                    DrayageDispatch.COLUMN_PICKUPCOMPANY,
                    DrayageDispatch.COLUMN_PICKUPADDRESS,
                    DrayageDispatch.COLUMN_PICKUPLATITUDE,
                    DrayageDispatch.COLUMN_PICKUPLONGITUDE,

                    DrayageDispatch.COLUMN_DROPCOMPANY,
                    DrayageDispatch.COLUMN_DROPADDRESS,
                    DrayageDispatch.COLUMN_DROPLATITUDE,
                    DrayageDispatch.COLUMN_DROPLONGITUDE,

                    DrayageDispatch.COLUMN_EMPTYRETURNCOMPANY,
                    DrayageDispatch.COLUMN_EMPTYRETURNADDRESS,
                    DrayageDispatch.COLUMN_EMPTYRETURNLATITUDE,
                    DrayageDispatch.COLUMN_EMPTYRETURNLONGITUDE,

                    DrayageDispatch.COLUMN_NOOFCONTAINER,
                    DrayageDispatch.COLUMN_STATUS,
                    DrayageDispatch.COLUMN_NOTES};
            String selection = DrayageDispatch.COLUMN_DISPATCHID + "= ?";
            String[] selectionArgs = {dispatchId + ""};
            cursor = database.query(DrayageDispatch.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
            if (cursor.moveToNext()) {
                bean.setDispatchId(cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_DISPATCHID)));
                bean.setDispatchDate(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_DISPATCHDATE)));
                bean.setDispatchNo(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_DISPATCHNO)));
                bean.setBookingNo(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_BOOKINGNO)));
                bean.setCustomer(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_CUSTOMER)));
                bean.setCustomerAddress(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_CUSTOMERADDRESS)));
                bean.setCustomerLatitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_CUSTOMERLATITUDE)));
                bean.setCustomerLongitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_CUSTOMERLONGITUDE)));
                bean.setSteamshipLineCompany(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_STEAMSHIPLINECOMPANY)));
                bean.setSslAddress(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_SSLADDRESS)));
                bean.setSslLatitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_SSLLATITUDE)));
                bean.setSslLongitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_SSLLONGITUDE)));
                bean.setPickupCompany(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_PICKUPCOMPANY)));
                bean.setPickupAddress(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_PICKUPADDRESS)));
                bean.setPickupLatitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_PICKUPLATITUDE)));
                bean.setPickupLongitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_PICKUPLONGITUDE)));
                bean.setDropCompany(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_DROPCOMPANY)));
                bean.setDropAddress(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_DROPADDRESS)));
                bean.setDropLatitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_DROPLATITUDE)));
                bean.setDropLongitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_DROPLONGITUDE)));
                bean.setEmptyReturnCompany(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_EMPTYRETURNCOMPANY)));
                bean.setEmptyReturnAddress(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_EMPTYRETURNADDRESS)));
                bean.setEmptyReturnLatitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_EMPTYRETURNLATITUDE)));
                bean.setEmptyReturnLongitude(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_EMPTYRETURNLONGITUDE)));
                bean.setNoOfContainer(cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_NOOFCONTAINER)));
                bean.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_NOTES)));
                bean.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_STATUS)));

            }

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DrayageDispatchDB.class.getName() + "::GetById Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DrayageDispatchDB.class.getName(),"::GetById Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e1) {

            }
        }
        return bean;
    }

    // Created By: Deepak Sharma
    // Created Date: 25 July 2017
    // Purpose: get detail of dispatch by dispatch id
    public static ArrayList<DispatchDetailBean> GetDetailByDispatchId(int dispatchId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<DispatchDetailBean> list = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            String[] projection = {DrayageDispatchDetail.COLUMN_DISPATCHID,
                    DrayageDispatchDetail.COLUMN_DISPATCHDETAILID,
                    DrayageDispatchDetail.COLUMN_PICKDROPID,
                    DrayageDispatchDetail.COLUMN_APPOINTMENTNO,
                    DrayageDispatchDetail.COLUMN_APPOINTMENTDATE,
                    DrayageDispatchDetail.COLUMN_CONTAINERNO,
                    DrayageDispatchDetail.COLUMN_CONTAINERSIZE,
                    DrayageDispatchDetail.COLUMN_CONTAINERTYPE,
                    DrayageDispatchDetail.COLUMN_CONTAINERGRADE
            };
            String selection = DrayageDispatchDetail.COLUMN_DISPATCHID + "= ? ";
            String[] selectionArgs = {dispatchId + ""};
            cursor = database.query(DrayageDispatchDetail.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

            while (cursor.moveToNext()) {
                DispatchDetailBean bean = new DispatchDetailBean();
                bean.setDispatchId(cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_DISPATCHID)));
                bean.setDispatchDetailId(cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_DISPATCHDETAILID)));
                bean.setPickDropId(cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_PICKDROPID)));
                bean.setAppointmentNo(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_APPOINTMENTNO)));
                bean.setAppointmentDate(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_APPOINTMENTDATE)));
                bean.setContainerNo(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_CONTAINERNO)));
                bean.setContainerSize(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_CONTAINERSIZE)));
                bean.setContainerType(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_CONTAINERTYPE)));
                bean.setContainerGrade(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_CONTAINERGRADE)));


                list.add(bean);
            }

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DrayageDispatchDB.class.getName() + "::GetDetailByDispatchId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DrayageDispatchDB.class.getName(),"::GetDetailByDispatchId Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

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
    // Created Date: 25 July 2017
    // Purpose: get dispatch detail by dispatchdetailid
    public static DispatchDetailBean GetDetailById(int dispatchDetailId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        DispatchDetailBean bean = new DispatchDetailBean();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            String[] projection = {DrayageDispatchDetail.COLUMN_DISPATCHID,
                    DrayageDispatchDetail.COLUMN_DISPATCHDETAILID,
                    DrayageDispatchDetail.COLUMN_PICKDROPID,
                    DrayageDispatchDetail.COLUMN_APPOINTMENTNO,
                    DrayageDispatchDetail.COLUMN_APPOINTMENTDATE,
                    DrayageDispatchDetail.COLUMN_CONTAINERNO,
                    DrayageDispatchDetail.COLUMN_CONTAINERSIZE,
                    DrayageDispatchDetail.COLUMN_CONTAINERTYPE,
                    DrayageDispatchDetail.COLUMN_CONTAINERGRADE,
                    DrayageDispatchDetail.COLUMN_MAXGROSSWEIGHT,
                    DrayageDispatchDetail.COLUMN_TAREWEIGHT,
                    DrayageDispatchDetail.COLUMN_MAXPAYLOAD,
                    DrayageDispatchDetail.COLUMN_MANUFACTURINGDATE,
                    DrayageDispatchDetail.COLUMN_SEALNO1,
                    DrayageDispatchDetail.COLUMN_SEALNO2,
                    DrayageDispatchDetail.COLUMN_DOCUMENT_PATH
            };
            String selection = DrayageDispatchDetail.COLUMN_DISPATCHDETAILID + "= ? ";
            String[] selectionArgs = {dispatchDetailId + ""};
            cursor = database.query(DrayageDispatchDetail.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

            if (cursor.moveToNext()) {
                bean.setDispatchId(cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_DISPATCHID)));
                bean.setDispatchDetailId(cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_DISPATCHDETAILID)));
                bean.setPickDropId(cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_PICKDROPID)));
                bean.setAppointmentNo(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_APPOINTMENTNO)));
                bean.setAppointmentDate(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_APPOINTMENTDATE)));
                bean.setContainerNo(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_CONTAINERNO)));
                bean.setContainerSize(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_CONTAINERSIZE)));
                bean.setContainerType(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_CONTAINERTYPE)));
                bean.setContainerGrade(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_CONTAINERGRADE)));

                bean.setMaxGrossWeight(cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_MAXGROSSWEIGHT)));
                bean.setTareWeight(cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_TAREWEIGHT)));
                bean.setMaxPayLoad(cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_MAXPAYLOAD)));
                bean.setManufacturingDate(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_MANUFACTURINGDATE)));
                bean.setSealNo1(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_SEALNO1)));
                bean.setSealNo2(cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_SEALNO2)));
                String docPath = cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_DOCUMENT_PATH));
                bean.setDocumentPath(docPath == null ? "" : docPath);

            }

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DrayageDispatchDB.class.getName() + "::GetDetailById Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DrayageDispatchDB.class.getName(),"::GetDetailById Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e1) {

            }
        }
        return bean;
    }

    // Created By: Deepak Sharma
    // Created Date: 25 July 2017
    // Purpose: update container detail associated with specific appointment
    public static boolean UpdateContainerDetail(DispatchDetailBean bean) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DrayageDispatchDetail.COLUMN_CONTAINERNO, bean.getContainerNo());
            values.put(DrayageDispatchDetail.COLUMN_MAXGROSSWEIGHT, bean.getMaxGrossWeight());
            values.put(DrayageDispatchDetail.COLUMN_TAREWEIGHT, bean.getTareWeight());
            values.put(DrayageDispatchDetail.COLUMN_MAXPAYLOAD, bean.getMaxPayLoad());
            values.put(DrayageDispatchDetail.COLUMN_MANUFACTURINGDATE, bean.getManufacturingDate());
            values.put(DrayageDispatchDetail.COLUMN_SEALNO1, bean.getSealNo1());
            values.put(DrayageDispatchDetail.COLUMN_SEALNO2, bean.getSealNo2());
            values.put(DrayageDispatchDetail.COLUMN_DOCUMENT_PATH, bean.getDocumentPath());
            values.put(DrayageDispatchDetail.COLUMN_MODIFIEDDATE, Utility.getCurrentDateTime());
            values.put(DrayageDispatchDetail.COLUMN_SYNCFG, 0);

            String selection = DrayageDispatchDetail.COLUMN_DISPATCHDETAILID + "= ? ";
            String[] selectionArgs = {bean.getDispatchDetailId() + ""};
            database.update(DrayageDispatchDetail.TABLE_NAME, values, selection, selectionArgs);

            // Post Data
            MainActivity.postData(CommonTask.Post_DispatchDetail);

        } catch (Exception exe) {
            status = false;
            Utility.printError(exe.getMessage());
            LogFile.write(DrayageDispatchDB.class.getName() + "::UpdateContainerDetail Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DrayageDispatchDB.class.getName(),"::UpdateContainerDetail Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

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
    // Created Date: 28 July 2017
    // Purpose: update dispatch status
    public static boolean UpdateDispatchStatus(int dispatchId, int s) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DrayageDispatch.COLUMN_STATUS, s);
            values.put(DrayageDispatch.COLUMN_SYNCFG, 0);

            String selection = DrayageDispatch.COLUMN_DISPATCHID + "= ? ";
            String[] selectionArgs = {dispatchId + ""};
            database.update(DrayageDispatch.TABLE_NAME, values, selection, selectionArgs);

            // Post Data
            MainActivity.postData(CommonTask.Post_DispatchStatus);

        } catch (Exception exe) {
            status = false;
            Utility.printError(exe.getMessage());
            LogFile.write(DrayageDispatchDB.class.getName() + "::UpdateDispatchStatus Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DrayageDispatchDB.class.getName(),"::UpdateDispatchStatus Error:" ,exe.getMessage(),Utility.printStackTrace(exe));
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e1) {

            }
        }

        return status;
    }

    public static int getLastDispatchId(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int dispatchId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            String[] projection = {
                    DrayageDispatch.COLUMN_DISPATCHID
            };
            String selection = DrayageDispatch.COLUMN_DRIVERID + "= ?";
            String[] selectionArgs = {driverId + ""};
            cursor = database.query(DrayageDispatch.TABLE_NAME, projection, selection, selectionArgs, null, null, DrayageDispatch.COLUMN_DISPATCHID + " desc", "1");

            if (cursor.moveToNext()) {
                dispatchId = cursor.getInt(0);
            }

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DrayageDispatchDB.class.getName() + "::getLastDispatchId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DrayageDispatchDB.class.getName(),"::getLastDispatchId Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e1) {

            }
        }
        return dispatchId;
    }

    // Created By: Deepak Sharma
    // Created Date: 25 July 2017
    // Purpose: get update container info to sync to the server
    public static JSONArray GetDetailSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            String[] projection = {
                    DrayageDispatchDetail.COLUMN_DISPATCHDETAILID,
                    DrayageDispatchDetail.COLUMN_CONTAINERNO,
                    DrayageDispatchDetail.COLUMN_MAXGROSSWEIGHT,
                    DrayageDispatchDetail.COLUMN_TAREWEIGHT,
                    DrayageDispatchDetail.COLUMN_MAXPAYLOAD,
                    DrayageDispatchDetail.COLUMN_MANUFACTURINGDATE,
                    DrayageDispatchDetail.COLUMN_SEALNO1,
                    DrayageDispatchDetail.COLUMN_SEALNO2,
                    DrayageDispatchDetail.COLUMN_DOCUMENT_PATH,
                    DrayageDispatchDetail.COLUMN_MODIFIEDDATE
            };
            String selection = DrayageDispatchDetail.COLUMN_SYNCFG + "= ? ";
            String[] selectionArgs = {"0"};
            cursor = database.query(DrayageDispatchDetail.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

            while (cursor.moveToNext()) {
                JSONObject bean = new JSONObject();
                bean.put("DispatchDetailId", cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_DISPATCHDETAILID)));
                bean.put("ContainerNo", cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_CONTAINERNO)));
                bean.put("MaxGrossWeight", cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_MAXGROSSWEIGHT)));
                bean.put("TareWeight", cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_TAREWEIGHT)));
                bean.put("MaxPayLoad", cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_MAXPAYLOAD)));
                bean.put("ManufacturingDate", cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_MANUFACTURINGDATE)));
                bean.put("SealNo1", cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_SEALNO1)));
                bean.put("SealNo2", cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_SEALNO2)));
                bean.put("DocumentPath", cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_DOCUMENT_PATH)));
                bean.put("ModifiedDate", cursor.getString(cursor.getColumnIndexOrThrow(DrayageDispatchDetail.COLUMN_MODIFIEDDATE)));
                array.put(bean);
            }

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DrayageDispatchDB.class.getName() + "::GetDetailSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DrayageDispatchDB.class.getName(),"::GetDetailSync Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

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
    // Created Date: 26 July 2017
    // Purpose: get route info to sync to the server
    public static JSONArray RouteGetSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            String[] projection = {
                    DrayageRouteDetail.COLUMN_DISPATCHID,
                    DrayageRouteDetail.COLUMN_PICKDROPID,
                    DrayageRouteDetail.COLUMN_ARRIVALDATE,
                    DrayageRouteDetail.COLUMN_DEPARTUREDATE
            };
            String selection = DrayageRouteDetail.COLUMN_SYNCFG + "= ? ";
            String[] selectionArgs = {"0"};
            cursor = database.query(DrayageRouteDetail.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

            while (cursor.moveToNext()) {
                JSONObject bean = new JSONObject();
                bean.put("DispatchId", cursor.getInt(cursor.getColumnIndexOrThrow(DrayageRouteDetail.COLUMN_DISPATCHID)));
                bean.put("PickDropId", cursor.getString(cursor.getColumnIndexOrThrow(DrayageRouteDetail.COLUMN_PICKDROPID)));
                bean.put("ArriveDate", cursor.getInt(cursor.getColumnIndexOrThrow(DrayageRouteDetail.COLUMN_ARRIVALDATE)));
                bean.put("DepartDate", cursor.getInt(cursor.getColumnIndexOrThrow(DrayageRouteDetail.COLUMN_DEPARTUREDATE)));
                bean.put("CompanyId", Utility.companyId);
                array.put(bean);
            }

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DrayageDispatchDB.class.getName() + "::RouteGetSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DrayageDispatchDB.class.getName(),"::RouteGetSync Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

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
    // Created Date: 26 July 2017
    // Purpose: get dispatch status info to sync to the server
    public static JSONArray DispatchGetSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            String[] projection = {
                    DrayageDispatch.COLUMN_DISPATCHID,
                    DrayageDispatch.COLUMN_STATUS
            };
            String selection = DrayageDispatch.COLUMN_SYNCFG + "= ? ";
            String[] selectionArgs = {"0"};
            cursor = database.query(DrayageDispatch.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

            while (cursor.moveToNext()) {
                JSONObject bean = new JSONObject();
                bean.put("DispatchId", cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_DISPATCHID)));
                bean.put("Status", cursor.getInt(cursor.getColumnIndexOrThrow(DrayageDispatch.COLUMN_STATUS)));
                array.put(bean);
            }

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DrayageDispatchDB.class.getName() + "::DispatchGetSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DrayageDispatchDB.class.getName(),"::DispatchGetSync Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

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
    // Created Date: 25 July 2017
    // Purpose: update Dispatch detail
    public static JSONArray DispatchDetailSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DrayageRouteDetail.COLUMN_SYNCFG, 1);
            String selection = DrayageDispatchDetail.COLUMN_SYNCFG + "= ? ";
            String[] selectionArgs = {"1"};
            database.update(DrayageDispatchDetail.TABLE_NAME, values, selection, selectionArgs);

        } catch (Exception exe) {

            LogFile.write(GeofenceDB.class.getName() + "::DispatchDetailSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DrayageDispatchDB.class.getName(),"::DispatchDetailSyncUpdate Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

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
    // Created Date: 26 July 2017
    // Purpose: update Dispatch detail
    public static JSONArray RouteDetailSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DrayageRouteDetail.COLUMN_SYNCFG, 1);
            String selection = DrayageRouteDetail.COLUMN_SYNCFG + "= ? ";
            String[] selectionArgs = {"1"};
            database.update(DrayageRouteDetail.TABLE_NAME, values, selection, selectionArgs);

        } catch (Exception exe) {

            LogFile.write(GeofenceDB.class.getName() + "::RouteDetailSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DrayageDispatchDB.class.getName(),"::RouteDetailSyncUpdate Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

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
    // Created Date: 26 July 2017
    // Purpose: update Dispatch detail
    public static JSONArray DispatchSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DrayageRouteDetail.COLUMN_SYNCFG, 1);
            String selection = DrayageDispatch.COLUMN_SYNCFG + "= ? ";
            String[] selectionArgs = {"1"};
            database.update(DrayageDispatch.TABLE_NAME, values, selection, selectionArgs);

        } catch (Exception exe) {

            LogFile.write(GeofenceDB.class.getName() + "::RouteDetailSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(DrayageDispatchDB.class.getName(),"::RouteDetailSyncUpdate Error:" ,exe.getMessage(),Utility.printStackTrace(exe));

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
