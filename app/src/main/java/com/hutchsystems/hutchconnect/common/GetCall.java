package com.hutchsystems.hutchconnect.common;

import android.util.Log;

import com.hutchsystems.hutchconnect.beans.Annotation;
import com.hutchsystems.hutchconnect.beans.AppInfoBean;
import com.hutchsystems.hutchconnect.beans.CTPATInspectionBean;
import com.hutchsystems.hutchconnect.beans.CardBean;
import com.hutchsystems.hutchconnect.beans.CarrierInfoBean;
import com.hutchsystems.hutchconnect.beans.CoDriverBean;
import com.hutchsystems.hutchconnect.beans.DailyLogBean;
import com.hutchsystems.hutchconnect.beans.EventBean;
import com.hutchsystems.hutchconnect.beans.GeofenceBean;
import com.hutchsystems.hutchconnect.beans.RuleBean;
import com.hutchsystems.hutchconnect.beans.ScheduleBean;
import com.hutchsystems.hutchconnect.beans.TripInspectionBean;
import com.hutchsystems.hutchconnect.beans.UserBean;
import com.hutchsystems.hutchconnect.beans.VehicleBean;
import com.hutchsystems.hutchconnect.db.AnnotationDetailDB;
import com.hutchsystems.hutchconnect.db.CTPATInspectionDB;
import com.hutchsystems.hutchconnect.db.CarrierInfoDB;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.EventDB;
import com.hutchsystems.hutchconnect.db.FuelDetailDB;
import com.hutchsystems.hutchconnect.db.GeofenceDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.NotificationDB;
import com.hutchsystems.hutchconnect.db.ScheduleDB;
import com.hutchsystems.hutchconnect.db.TrainingDocumentBean;
import com.hutchsystems.hutchconnect.db.TrainingDocumentDB;
import com.hutchsystems.hutchconnect.db.TripInspectionDB;
import com.hutchsystems.hutchconnect.db.UserDB;
import com.hutchsystems.hutchconnect.db.VehicleDB;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Deepak.Sharma on 1/14/2016.
 */
public class GetCall {

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: sync user accounts with web
    public static boolean AccountSync(int userId) {
        boolean status = true;
        WebService ws = new WebService();
        String result = "";
        try {

            result = ws.doGet(WebUrl.GET_ACCOUNT + Utility.IMEI + "&userId=" + userId);
            if (result == null || result.isEmpty())
                return status;

            JSONArray obja = new JSONArray(result);
            //Log.i("DB", obja.toString());
            ArrayList<UserBean> al = new ArrayList<UserBean>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                UserBean bean = new UserBean();
                bean.setAccountId(json.getInt("AccountId"));
                bean.setFirstName(json.getString("FirstName"));
                bean.setLastName(json.getString("LastName"));
                bean.setEmailId(json.getString("EmailId"));
                bean.setMobileNo(json.getString("MobileNo"));
                bean.setAccountType(json.getInt("AccountType"));
                bean.setDrivingLicense(json.getString("LicenseNo"));
                bean.setDlIssueState(json.getString("DLIssuerState"));
                bean.setUserName(json.getString("UserName"));
                bean.setPassword(json.getString("Password"));
                bean.setDotPassword(json.getString("DotPassword"));
                bean.setSalt(json.getString("Salt"));
                bean.setExemptELDUseFg(json.getBoolean("ExemptELDUseFg") ? 1 : 0);
                bean.setExemptionRemark(json.getString("ExemptionRemarks"));
                bean.setSpecialCategory(json.getString("SpecialCategory"));
                bean.setCurrentRule(json.getInt("CurrentRule"));
                bean.setTimeZoneOffsetUTC(json.getString("TimeZoneOffsetUTC"));
                bean.setLicenseAcceptFg(json.getInt("LicenseAcceptFg"));
                bean.setLicenseExpiryDate(json.getString("LicenseExpiryDate"));
                bean.setStatusId(json.getInt("StatusId"));
                bean.setTimeZoneId(json.getString("TimeZoneId"));
                bean.setFullAddress(json.getString("FullAddress"));

                bean.setSupportLanguage(json.getString("SupportLanguage"));
                bean.setdEditFg(json.getBoolean("DEditFg") ? 1 : 0);
                al.add(bean);
            }

            if (al.size() > 0) {
                UserDB.Save(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            // LogFile.write(GetCall.class.getName() + "::AccountSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: sync user accounts with web
    public static boolean CarrierInfoSync() {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws.doGet(WebUrl.GET_CARRIER_INFO + Utility.IMEI);
            if (result == null || result.isEmpty())
                return status;

            JSONArray obja = new JSONArray(result);
            ArrayList<CarrierInfoBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                CarrierInfoBean bean = new CarrierInfoBean();
                bean.setCompanyId(json.getInt("CompanyId"));
                bean.setCarrierName(json.getString("CarrierName"));
                bean.setELDManufacturer(json.getString("ELDManufacturer"));
                bean.setUSDOT(json.getString("USDOT"));
                bean.setVehicleId(json.getInt("VehicleId"));
                bean.setUnitNo(json.getString("UnitNo"));
                bean.setPlateNo(json.getString("PlateNo"));
                bean.setVIN(json.getString("VinNo"));
                bean.setStatusId(json.getInt("StatusId"));
                bean.setSerailNo(json.getString("SerialNo"));
                bean.setMACAddress(json.getString("MACAddress"));
                bean.setTimeZoneId(json.getString("TimeZoneId"));
                bean.setTotalAxle(json.getInt("Axle"));
                bean.setCanBusFg(json.getInt("CanBusFg"));
                bean.setStartOdometerReading(json.getDouble("StartOdometerReading") + "");
                bean.setPackageId(json.getInt("PackageId"));
                bean.setFeatures(json.getString("Features"));
                bean.setFullAddress(json.getString("FullAddress"));
                bean.setEldConfig(json.getString("EldConfig"));
                bean.setVehicleOdometerUnit(json.getInt("VehicleOdometerUnit"));
                bean.setSetupFg(json.getBoolean("SetupFg"));
                bean.setProtocol(json.getString("Protocol"));
                bean.setOdometerSource(json.getInt("OdometerSource"));
                bean.setReferralCode(json.getString("ReferralCode"));
                bean.setReferralLink(json.getString("ReferralLink"));
                bean.setRechargeLink(json.getString("RechargeLink"));
                bean.setCompanyStatusId(json.getInt("CompanyStatusId"));
                bean.setLatePaymentFg(json.getBoolean("PaymentLateFg"));
                al.add(bean);
            }

            if (al.size() > 0) {
                CarrierInfoDB.Save(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            //  LogFile.write(GetCall.class.getName() + "::CarrierInfoSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: sync trailer info with web
    public static boolean TrailerInfoGetSync() {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.GET_TRAILER_INFO
                            + Utility.companyId);
            if (result == null || result.isEmpty())
                return status;

            JSONArray obja = new JSONArray(result);
            ArrayList<VehicleBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                VehicleBean bean = new VehicleBean();
                bean.setVehicleId(json.getInt("VehicleId"));
                bean.setUnitNo(json.getString("UnitNo"));
                bean.setPlateNo(json.getString("PlateNo"));
                bean.setTotalAxle(json.getInt("Axle"));
                al.add(bean);
            }

            if (al.size() > 0) {
                VehicleDB.Save(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            //  LogFile.write(GetCall.class.getName() + "::CarrierInfoSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 14 april 2019
    // Purpose: get Remark info From Web
    public static boolean AnnotationInfoGetSync() {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.GET_ANNOTATION_INFO
                            + Utility.companyId);
            if (result == null || result.isEmpty())
                return status;

            JSONArray obja = new JSONArray(result);
            ArrayList<Annotation> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                Annotation bean = new Annotation();
                bean.setCompanyID(Utility.companyId);
                bean.setAnnotationId(json.getInt("Id"));
                bean.setAnnotationValue(json.getString("LookUpValue"));
                bean.setDisplayOrder(json.getInt("DisplayOrder"));
                bean.setDescription(json.getString("Description"));
                al.add(bean);
            }

            if (al.size() > 0) {
                // If Previous annotation Present remove that annotation
                if (AnnotationDetailDB.AnnotationGet().size() > 0) {
                    // Remove annotation from database
                    AnnotationDetailDB.removeAnnotation();
                }
                // Insert Annotation in the database
                AnnotationDetailDB.Save(al);
            }


        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(GetCall.class.getName() + "::CarrierInfoSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 27 January 2016
    // Purpose: sync daily log data with web incase driver using multiple vehicle
    public static boolean LogInfoSync(String fromDate, String toDate) {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.GET_LOG_DATA
                            + Utility.onScreenUserId + "&fromDate=" + fromDate + "&toDate=" + toDate);

            if (result == null || result.isEmpty())
                return status;
            JSONArray obja = new JSONArray(result);
            ArrayList<DailyLogBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                DailyLogBean bean = new DailyLogBean();
                bean.setOnlineDailyLogId(json.getInt("DAILYLOGID"));
                bean.setLogDate(Utility.ConvertFromJsonDate(json.getString("LOGDATE")));
                bean.setDriverId(json.getInt("DRIVERID"));
                bean.setShippingId(json.getString("SHIPPINGID"));
                bean.setTrailerId(json.getString("TRAILERID"));
                bean.setStartTime(json.getString("STARTTIME"));
                bean.setStartOdometerReading(json.getString("STARTODOMETERREADING"));
                bean.setEndOdometerReading(json.getString("ENDODOMETERREADING"));
                bean.setCertifyFG(json.getBoolean("CERTIFYFG") ? 1 : 0);
                //bean.setcertifyCount(json.getString("CERTIFYCOUNT"));
                //bean.setSignature(json.getString("SIGNATURE"));
                bean.setCreatedBy(json.getInt("LOGCREATEDBY"));
                bean.setCreatedDate(Utility.ConvertFromJsonDateTime(json.getString("LOGCREATEDDATE")));
                // bean.setModifiedBy(json.getInt("MODIFIEDBY"));
                //bean.setModifiedDate(json.getString("MODIFIEDDATE"));
                bean.setStatusId(1);
                bean.setSyncFg(1);

                Log.i("GetCall", "dailylogid=" + bean.getOnlineDailyLogId() + " / create date=" + bean.getCreatedDate());

                JSONArray jaEvent = json.getJSONArray("EventList");
                ArrayList<EventBean> arrEvent = new ArrayList<>();
                for (int j = 0; j < jaEvent.length(); j++) {
                    JSONObject joEvent = jaEvent.getJSONObject(j);
                    EventBean eventBean = new EventBean();
                    eventBean.setDriverId(joEvent.getInt("DRIVERID"));
                    eventBean.setOnlineEventId(joEvent.getInt("EVENTID"));
                    eventBean.setEventSequenceId(joEvent.getInt("EVENTSEQUENCEID"));
                    eventBean.setEventType(joEvent.getInt("EVENTTYPE"));
                    eventBean.setEventCode(joEvent.getInt("EVENTCODE"));
                    eventBean.setEventCodeDescription(joEvent.getString("EVENTCODEDESCRIPTION"));
                    eventBean.setOdometerReading(joEvent.getString("ODOMETERREADING"));
                    eventBean.setEngineHour(joEvent.getString("ENGINEHOUR"));
                    eventBean.setEventRecordOrigin(joEvent.getInt("EVENTRECORDORIGIN"));
                    eventBean.setEventRecordStatus(joEvent.getInt("EVENTRECORDSTATUS"));
                    eventBean.setEventDateTime(Utility.ConvertFromJsonDateTime(joEvent.getString("EVENTDATETIME")));
                    eventBean.setLatitude(joEvent.getString("LATITUDE"));
                    eventBean.setLongitude(joEvent.getString("LONGITUDE"));
                    eventBean.setLocationDescription(joEvent.getString("LOCATIONDESCRIPTION"));
                    eventBean.setDailyLogId(joEvent.getInt("DAILYLOGID"));
                    eventBean.setCreatedBy(joEvent.getInt("EVENTCREATEDBY"));
                    eventBean.setCreatedDate(Utility.ConvertFromJsonDateTime(joEvent.getString("EVENTCREATEDDATE")));
                    //eventBean.setModifiedBy(json.getInt("MODIFIEDBY"));
                    //eventBean.setModifiedDate(json.getString("MODIFIEDDATE"));
                    eventBean.setVehicleId(joEvent.getInt("VEHICLEID"));
                    eventBean.setStatusId(1);
                    eventBean.setSyncFg(1);

                    eventBean.setCheckSumWeb(joEvent.getString("CheckSum"));
                    eventBean.setDistanceSinceLastValidCoordinate(joEvent.getString("DistanceSinceLastValidCoordinate"));
                    eventBean.setMalfunctionIndicatorFg(joEvent.getBoolean("MalfunctionIndicatorFg") ? 1 : 0);
                    eventBean.setDataDiagnosticIndicatorFg(joEvent.getBoolean("DataDiagnosticIndicatorFg") ? 1 : 0);
                    eventBean.setDiagnosticCode(joEvent.getString("DiagnosticCode"));
                    eventBean.setAccumulatedVehicleMiles(joEvent.getString("AccumulatedVehicleMiles"));
                    eventBean.setElaspsedEngineHour(joEvent.getString("ElaspsedEngineHour"));
                    eventBean.setMotorCarrier(joEvent.getString("MotorCarrier"));
                    eventBean.setShippingDocumentNo(joEvent.getString("ShippingDocumentNo"));
                    eventBean.setTrailerNo(joEvent.getString("TrailerNo"));
                    eventBean.setTimeZoneOffsetUTC(joEvent.getString("TimeZoneOffsetUTC"));
                    eventBean.setCoDriverId(joEvent.getInt("CoDriverId"));

                    arrEvent.add(eventBean);
                }
                bean.setEventList(arrEvent);
                al.add(bean);
            }

            if (al.size() > 0) {
                DailyLogDB.DailyLogSave(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            Log.i("GetCall", "Err:" + e.getMessage());
            //   LogFile.write(GetCall.class.getName() + "::LogInfoSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }

    @Deprecated
    // Created By: Deepak Sharma
    // Created Date: 27 January 2016
    // Purpose: sync daily log data with web if editted in web
    public static boolean EditRequestSync() {
        boolean status = true;
        WebService ws = new WebService();
        String url = WebUrl.GET_ASSIGNED_EVENT + Utility.onScreenUserId + "&EventRecordOrigin=3&EventRecordStatus=3";
        try {
            String result = ws
                    .doGet(url);
            if (result == null || result.isEmpty())
                return status;
            JSONArray obja = new JSONArray(result);
            ArrayList<DailyLogBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                DailyLogBean bean = new DailyLogBean();
                bean.setOnlineDailyLogId(json.getInt("DAILYLOGID"));
                bean.setLogDate(Utility.ConvertFromJsonDate(json.getString("LOGDATE")));
                bean.setDriverId(json.getInt("DRIVERID"));
                bean.setShippingId(json.getString("SHIPPINGID"));
                bean.setTrailerId(json.getString("TRAILERID"));
                bean.setStartTime(json.getString("STARTTIME"));
                bean.setStartOdometerReading(json.getString("STARTODOMETERREADING"));
                bean.setEndOdometerReading(json.getString("ENDODOMETERREADING"));
                bean.setCertifyFG(json.getBoolean("CERTIFYFG") ? 1 : 0);
                //bean.setcertifyCount(json.getString("CERTIFYCOUNT"));
                //bean.setSignature(json.getString("SIGNATURE"));
                bean.setCreatedBy(json.getInt("LOGCREATEDBY"));
                bean.setCreatedDate(Utility.ConvertFromJsonDateTime(json.getString("LOGCREATEDDATE")));
                //bean.setModifiedBy(json.getInt("MODIFIEDBY"));
                //bean.setModifiedDate(json.getString("MODIFIEDDATE"));
                bean.setStatusId(1);
                bean.setSyncFg(1);

                JSONArray jaEvent = json.getJSONArray("EventList");
                ArrayList<EventBean> arrEvent = new ArrayList<>();
                for (int j = 0; j < jaEvent.length(); j++) {
                    JSONObject joEvent = jaEvent.getJSONObject(j);
                    EventBean eventBean = new EventBean();
                    eventBean.setDriverId(bean.getDriverId());
                    eventBean.setOnlineEventId(joEvent.getInt("EVENTID"));
                    eventBean.setEventSequenceId(joEvent.getInt("EVENTSEQUENCEID"));
                    eventBean.setEventType(joEvent.getInt("EVENTTYPE"));
                    eventBean.setEventCode(joEvent.getInt("EVENTCODE"));
                    eventBean.setEventCodeDescription(joEvent.getString("EVENTCODEDESCRIPTION"));
                    eventBean.setOdometerReading(joEvent.getString("ODOMETERREADING"));
                    eventBean.setEngineHour(joEvent.getString("ENGINEHOUR"));
                    eventBean.setEventRecordOrigin(joEvent.getInt("EVENTRECORDORIGIN"));
                    eventBean.setEventRecordStatus(joEvent.getInt("EVENTRECORDSTATUS"));
                    eventBean.setEventDateTime(Utility.ConvertFromJsonDateTime(joEvent.getString("EVENTDATETIME")));
                    eventBean.setLatitude(joEvent.getString("LATITUDE"));
                    eventBean.setLongitude(joEvent.getString("LONGITUDE"));
                    eventBean.setLocationDescription(joEvent.getString("LOCATIONDESCRIPTION"));
                    eventBean.setDailyLogId(joEvent.getInt("DAILYLOGID"));
                    eventBean.setCreatedBy(joEvent.getInt("EVENTCREATEDBY"));
                    eventBean.setCreatedDate(Utility.ConvertFromJsonDateTime(joEvent.getString("EVENTCREATEDDATE")));
                    eventBean.setVehicleId(joEvent.getInt("VEHICLEID"));
                    eventBean.setCoDriverId(joEvent.getInt("CoDriverId"));
                    //eventBean.setModifiedBy(json.getInt("MODIFIEDBY"));
                    //eventBean.setModifiedDate(json.getString("MODIFIEDDATE"));
                    eventBean.setStatusId(1);
                    eventBean.setSyncFg(1);

                    eventBean.setCheckSumWeb(joEvent.getString("CheckSum"));
                    eventBean.setDistanceSinceLastValidCoordinate(joEvent.getString("DistanceSinceLastValidCoordinate"));
                    eventBean.setMalfunctionIndicatorFg(joEvent.getBoolean("MalfunctionIndicatorFg") ? 1 : 0);
                    eventBean.setDataDiagnosticIndicatorFg(joEvent.getBoolean("DataDiagnosticIndicatorFg") ? 1 : 0);
                    eventBean.setDiagnosticCode(joEvent.getString("DiagnosticCode"));
                    eventBean.setAccumulatedVehicleMiles(joEvent.getString("AccumulatedVehicleMiles"));
                    eventBean.setElaspsedEngineHour(joEvent.getString("ElaspsedEngineHour"));
                    eventBean.setMotorCarrier(joEvent.getString("MotorCarrier"));
                    eventBean.setShippingDocumentNo(joEvent.getString("ShippingDocumentNo"));
                    eventBean.setTrailerNo(joEvent.getString("TrailerNo"));
                    eventBean.setTimeZoneOffsetUTC(joEvent.getString("TimeZoneOffsetUTC"));

                    arrEvent.add(eventBean);
                }
                bean.setEventList(arrEvent);
                al.add(bean);
            }

            if (al.size() > 0) {
                DailyLogDB.DailyLogSave(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            // LogFile.write(GetCall.class.getName() + "::EditRequestSync Error " + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: sync trailer info with web
    public static boolean CardDetailGetSync() {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.GET_CARD_DETAIL
                            + Utility.vehicleId);
            if (result == null || result.isEmpty())
                return status;

            JSONArray obja = new JSONArray(result);
            ArrayList<CardBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                CardBean bean = new CardBean();
                bean.setVehicleId(json.getInt("VehicleId"));
                bean.setCardId(json.getInt("CardId"));
                bean.setCardNo(json.getString("CardNo"));
                al.add(bean);
            }

            if (al.size() > 0) {
                FuelDetailDB.Save(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            //  LogFile.write(GetCall.class.getName() + "::CarrierInfoSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 22 March 2017
    // Purpose: sync daily log data with web in case driver using multiple vehicle
    public static boolean DailyLogEventGetSync(String lastEventDate) {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.DAILY_LOG_EVENT_DATA_GET
                            + Utility.onScreenUserId + "&lastEventDate=" + lastEventDate.replaceAll(" ", "%20"));

            if (result == null || result.isEmpty())
                return false;
            JSONArray obja = new JSONArray(result);
            ArrayList<DailyLogBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                DailyLogBean bean = new DailyLogBean();
                bean.setOnlineDailyLogId(json.getInt("DAILYLOGID"));
                bean.setLogDate(json.getString("LOGDATE"));
                bean.setDriverId(json.getInt("DRIVERID"));
                bean.setShippingId(json.getString("SHIPPINGID"));
                bean.setTrailerId(json.getString("TRAILERID"));
                bean.setStartTime(json.getString("STARTTIME"));
                bean.setStartOdometerReading(json.getString("STARTODOMETERREADING"));
                bean.setEndOdometerReading(json.getString("ENDODOMETERREADING"));
                bean.setCertifyFG(json.getBoolean("CERTIFYFG") ? 1 : 0);
                //bean.setcertifyCount(json.getString("CERTIFYCOUNT"));
                //bean.setSignature(json.getString("SIGNATURE"));
                bean.setCreatedBy(json.getInt("LOGCREATEDBY"));
                bean.setCreatedDate(json.getString("LOGCREATEDDATE"));
                // bean.setModifiedBy(json.getInt("MODIFIEDBY"));
                //bean.setModifiedDate(json.getString("MODIFIEDDATE"));
                bean.setStatusId(1);
                bean.setSyncFg(1);

                bean.setTotalDistance(json.getInt("TotalDistance"));
                bean.setPCDistance(json.getInt("PCDistance"));
                bean.setDrivingTimeRemaining(json.getInt("DrivingTimeRemaining"));
                bean.setWorkShiftTimeRemaining(json.getInt("WorkShiftRemaining"));
                bean.setTimeRemaining70(json.getInt("TimeRemaining70"));
                bean.setTimeRemaining120(json.getInt("TimeRemaining120"));
                bean.setTimeRemainingUS70(json.getInt("TimeRemainingUS70"));
                bean.setTimeRemainingReset(json.getInt("TimeRemainingReset"));

                // For Deferred Day
                //   bean.setDeferedDay(json.getInt("DeferredDay"));


                JSONArray jaEvent = json.getJSONArray("EventList");
                ArrayList<EventBean> arrEvent = new ArrayList<>();
                for (int j = 0; j < jaEvent.length(); j++) {
                    JSONObject joEvent = jaEvent.getJSONObject(j);
                    EventBean eventBean = new EventBean();
                    eventBean.setDriverId(joEvent.getInt("DRIVERID"));
                    eventBean.setOnlineEventId(joEvent.getInt("EVENTID"));
                    eventBean.setEventSequenceId(joEvent.getInt("EVENTSEQUENCEID"));
                    eventBean.setEventType(joEvent.getInt("EVENTTYPE"));
                    eventBean.setEventCode(joEvent.getInt("EVENTCODE"));
                    eventBean.setEventCodeDescription(joEvent.getString("EVENTCODEDESCRIPTION"));
                    eventBean.setOdometerReading(joEvent.getString("ODOMETERREADING"));
                    eventBean.setEngineHour(joEvent.getString("ENGINEHOUR"));
                    eventBean.setEventRecordOrigin(joEvent.getInt("EVENTRECORDORIGIN"));
                    eventBean.setEventRecordStatus(joEvent.getInt("EVENTRECORDSTATUS"));
                    eventBean.setEventDateTime(joEvent.getString("EVENTDATETIME"));
                    eventBean.setLatitude(joEvent.getString("LATITUDE"));
                    eventBean.setLongitude(joEvent.getString("LONGITUDE"));
                    eventBean.setLocationDescription(joEvent.getString("LOCATIONDESCRIPTION"));
                    eventBean.setDailyLogId(joEvent.getInt("DAILYLOGID"));
                    eventBean.setCreatedBy(joEvent.getInt("EVENTCREATEDBY"));
                    eventBean.setCreatedDate(joEvent.getString("EVENTCREATEDDATE"));
                    //eventBean.setModifiedBy(json.getInt("MODIFIEDBY"));
                    //eventBean.setModifiedDate(json.getString("MODIFIEDDATE"));
                    eventBean.setVehicleId(joEvent.getInt("VEHICLEID"));
                    eventBean.setStatusId(1);
                    eventBean.setSyncFg(1);

                    eventBean.setCheckSumWeb(joEvent.getString("CheckSum"));
                    eventBean.setDistanceSinceLastValidCoordinate(joEvent.getString("DistanceSinceLastValidCoordinate"));
                    eventBean.setMalfunctionIndicatorFg(joEvent.getBoolean("MalfunctionIndicatorFg") ? 1 : 0);
                    eventBean.setDataDiagnosticIndicatorFg(joEvent.getBoolean("DataDiagnosticIndicatorFg") ? 1 : 0);
                    eventBean.setDiagnosticCode(joEvent.getString("DiagnosticCode"));
                    eventBean.setAccumulatedVehicleMiles(joEvent.getString("AccumulatedVehicleMiles"));
                    eventBean.setElaspsedEngineHour(joEvent.getString("ElaspsedEngineHour"));
                    eventBean.setMotorCarrier(joEvent.getString("MotorCarrier"));
                    eventBean.setShippingDocumentNo(joEvent.getString("ShippingDocumentNo"));
                    eventBean.setTrailerNo(joEvent.getString("TrailerNo"));
                    eventBean.setTimeZoneOffsetUTC(joEvent.getString("TimeZoneOffsetUTC"));
                    eventBean.setCoDriverId(joEvent.getInt("CoDriverId"));
                    eventBean.setAnnotation(joEvent.getString("Annotation"));
                    eventBean.setSplitSleep(joEvent.getBoolean("SplitSleepFg") ? 1 : 0);
                    eventBean.setRuleId(joEvent.getInt("RuleId"));
                    eventBean.setTimeZoneShortName(joEvent.getString("TimeZoneShortName"));
                    arrEvent.add(eventBean);

                }
                bean.setEventList(arrEvent);
                al.add(bean);
            }

            if (al.size() > 0) {
                DailyLogDB.DailyLogSave(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            //  LogFile.write(GetCall.class.getName() + "::LogInfoSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 22 March 2017
    // Purpose: sync daily log data with web if editted in web
    public static boolean DailyLogEditRequestSync() {
        boolean status = true;
        WebService ws = new WebService();
        if (Utility.onScreenUserId == 0)
            return status;
        String url = WebUrl.EDIT_REQUEST_EVENT_GET + Utility.onScreenUserId + "&EventRecordOrigin=3&EventRecordStatus=3";
        try {
            String result = ws
                    .doGet(url);
            if (result == null || result.isEmpty())
                return status;
            JSONArray obja = new JSONArray(result);
            ArrayList<DailyLogBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                DailyLogBean bean = new DailyLogBean();
                bean.setOnlineDailyLogId(json.getInt("DAILYLOGID"));
                bean.setLogDate(json.getString("LOGDATE"));
                bean.setDriverId(json.getInt("DRIVERID"));
                bean.setShippingId(json.getString("SHIPPINGID"));
                bean.setTrailerId(json.getString("TRAILERID"));
                bean.setStartTime(json.getString("STARTTIME"));
                bean.setStartOdometerReading(json.getString("STARTODOMETERREADING"));
                bean.setEndOdometerReading(json.getString("ENDODOMETERREADING"));
                bean.setCertifyFG(json.getBoolean("CERTIFYFG") ? 1 : 0);
                //bean.setcertifyCount(json.getString("CERTIFYCOUNT"));
                //bean.setSignature(json.getString("SIGNATURE"));
                bean.setCreatedBy(json.getInt("LOGCREATEDBY"));
                bean.setCreatedDate(json.getString("LOGCREATEDDATE"));
                //bean.setModifiedBy(json.getInt("MODIFIEDBY"));
                //bean.setModifiedDate(json.getString("MODIFIEDDATE"));
                bean.setStatusId(1);
                bean.setSyncFg(1);

                JSONArray jaEvent = json.getJSONArray("EventList");
                ArrayList<EventBean> arrEvent = new ArrayList<>();
                for (int j = 0; j < jaEvent.length(); j++) {
                    JSONObject joEvent = jaEvent.getJSONObject(j);
                    EventBean eventBean = new EventBean();
                    eventBean.setDriverId(bean.getDriverId());
                    eventBean.setOnlineEventId(joEvent.getInt("EVENTID"));
                    eventBean.setEventSequenceId(joEvent.getInt("EVENTSEQUENCEID"));
                    eventBean.setEventType(joEvent.getInt("EVENTTYPE"));
                    eventBean.setEventCode(joEvent.getInt("EVENTCODE"));
                    eventBean.setEventCodeDescription(joEvent.getString("EVENTCODEDESCRIPTION"));
                    eventBean.setOdometerReading(joEvent.getString("ODOMETERREADING"));
                    eventBean.setEngineHour(joEvent.getString("ENGINEHOUR"));
                    eventBean.setEventRecordOrigin(joEvent.getInt("EVENTRECORDORIGIN"));
                    eventBean.setEventRecordStatus(joEvent.getInt("EVENTRECORDSTATUS"));
                    eventBean.setEventDateTime(joEvent.getString("EVENTDATETIME"));
                    eventBean.setLatitude(joEvent.getString("LATITUDE"));
                    eventBean.setLongitude(joEvent.getString("LONGITUDE"));
                    eventBean.setLocationDescription(joEvent.getString("LOCATIONDESCRIPTION"));
                    eventBean.setDailyLogId(joEvent.getInt("DAILYLOGID"));
                    eventBean.setCreatedBy(joEvent.getInt("EVENTCREATEDBY"));
                    eventBean.setCreatedDate(joEvent.getString("EVENTCREATEDDATE"));
                    eventBean.setVehicleId(joEvent.getInt("VEHICLEID"));
                    eventBean.setCoDriverId(joEvent.getInt("CoDriverId"));
                    //eventBean.setModifiedBy(json.getInt("MODIFIEDBY"));
                    //eventBean.setModifiedDate(json.getString("MODIFIEDDATE"));
                    eventBean.setStatusId(1);
                    eventBean.setSyncFg(1);

                    eventBean.setCheckSumWeb(joEvent.getString("CheckSum"));
                    eventBean.setDistanceSinceLastValidCoordinate(joEvent.getString("DistanceSinceLastValidCoordinate"));
                    eventBean.setMalfunctionIndicatorFg(joEvent.getBoolean("MalfunctionIndicatorFg") ? 1 : 0);
                    eventBean.setDataDiagnosticIndicatorFg(joEvent.getBoolean("DataDiagnosticIndicatorFg") ? 1 : 0);
                    eventBean.setDiagnosticCode(joEvent.getString("DiagnosticCode"));
                    eventBean.setAccumulatedVehicleMiles(joEvent.getString("AccumulatedVehicleMiles"));
                    eventBean.setElaspsedEngineHour(joEvent.getString("ElaspsedEngineHour"));
                    eventBean.setMotorCarrier(joEvent.getString("MotorCarrier"));
                    eventBean.setShippingDocumentNo(joEvent.getString("ShippingDocumentNo"));
                    eventBean.setTrailerNo(joEvent.getString("TrailerNo"));
                    eventBean.setTimeZoneOffsetUTC(joEvent.getString("TimeZoneOffsetUTC"));
                    eventBean.setAnnotation(joEvent.getString("Annotation"));

                    eventBean.setSplitSleep(joEvent.getBoolean("SplitSleepFg") ? 1 : 0);
                    eventBean.setRuleId(joEvent.getInt("RuleId"));
                    eventBean.setTimeZoneShortName(joEvent.getString("TimeZoneShortName"));
                    arrEvent.add(eventBean);

                }
                bean.setEventList(arrEvent);
                al.add(bean);
            }

            if (al.size() > 0) {
                DailyLogDB.DailyLogSave(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            // LogFile.write(GetCall.class.getName() + "::EditRequestSync Error " + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 22 March 2017
    // Purpose: sync daily log data with web in case driver using multiple vehicle
    public static boolean DailyLogEventGetByDateSync(String eventDate) {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.DAILY_LOG_EVENT_BY_DATE_GET
                            + Utility.onScreenUserId + "&eventDate=" + eventDate.replaceAll(" ", "%20"));

            if (result == null || result.isEmpty())
                return false;
            JSONArray obja = new JSONArray(result);
            ArrayList<DailyLogBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                DailyLogBean bean = new DailyLogBean();
                bean.setOnlineDailyLogId(json.getInt("DAILYLOGID"));
                bean.setLogDate(json.getString("LOGDATE"));
                bean.setDriverId(json.getInt("DRIVERID"));
                bean.setShippingId(json.getString("SHIPPINGID"));
                bean.setTrailerId(json.getString("TRAILERID"));
                bean.setStartTime(json.getString("STARTTIME"));
                bean.setStartOdometerReading(json.getString("STARTODOMETERREADING"));
                bean.setEndOdometerReading(json.getString("ENDODOMETERREADING"));
                bean.setCertifyFG(json.getBoolean("CERTIFYFG") ? 1 : 0);
                //bean.setcertifyCount(json.getString("CERTIFYCOUNT"));
                //bean.setSignature(json.getString("SIGNATURE"));
                bean.setCreatedBy(json.getInt("LOGCREATEDBY"));
                bean.setCreatedDate(json.getString("LOGCREATEDDATE"));
                // bean.setModifiedBy(json.getInt("MODIFIEDBY"));
                //bean.setModifiedDate(json.getString("MODIFIEDDATE"));
                bean.setStatusId(1);
                bean.setSyncFg(1);

                bean.setTotalDistance(json.getInt("TotalDistance"));
                bean.setPCDistance(json.getInt("PCDistance"));
                bean.setDrivingTimeRemaining(json.getInt("DrivingTimeRemaining"));
                bean.setWorkShiftTimeRemaining(json.getInt("WorkShiftRemaining"));
                bean.setTimeRemaining70(json.getInt("TimeRemaining70"));
                bean.setTimeRemaining120(json.getInt("TimeRemaining120"));
                bean.setTimeRemainingUS70(json.getInt("TimeRemainingUS70"));
                bean.setTimeRemainingReset(json.getInt("TimeRemainingReset"));

                JSONArray jaEvent = json.getJSONArray("EventList");
                ArrayList<EventBean> arrEvent = new ArrayList<>();
                for (int j = 0; j < jaEvent.length(); j++) {
                    JSONObject joEvent = jaEvent.getJSONObject(j);
                    EventBean eventBean = new EventBean();
                    eventBean.setDriverId(joEvent.getInt("DRIVERID"));
                    eventBean.setOnlineEventId(joEvent.getInt("EVENTID"));
                    eventBean.setEventSequenceId(joEvent.getInt("EVENTSEQUENCEID"));
                    eventBean.setEventType(joEvent.getInt("EVENTTYPE"));
                    eventBean.setEventCode(joEvent.getInt("EVENTCODE"));
                    eventBean.setEventCodeDescription(joEvent.getString("EVENTCODEDESCRIPTION"));
                    eventBean.setOdometerReading(joEvent.getString("ODOMETERREADING"));
                    eventBean.setEngineHour(joEvent.getString("ENGINEHOUR"));
                    eventBean.setEventRecordOrigin(joEvent.getInt("EVENTRECORDORIGIN"));
                    eventBean.setEventRecordStatus(joEvent.getInt("EVENTRECORDSTATUS"));
                    eventBean.setEventDateTime(joEvent.getString("EVENTDATETIME"));
                    eventBean.setLatitude(joEvent.getString("LATITUDE"));
                    eventBean.setLongitude(joEvent.getString("LONGITUDE"));
                    eventBean.setLocationDescription(joEvent.getString("LOCATIONDESCRIPTION"));
                    eventBean.setDailyLogId(joEvent.getInt("DAILYLOGID"));
                    eventBean.setCreatedBy(joEvent.getInt("EVENTCREATEDBY"));
                    eventBean.setCreatedDate(joEvent.getString("EVENTCREATEDDATE"));
                    //eventBean.setModifiedBy(json.getInt("MODIFIEDBY"));
                    //eventBean.setModifiedDate(json.getString("MODIFIEDDATE"));
                    eventBean.setVehicleId(joEvent.getInt("VEHICLEID"));
                    eventBean.setStatusId(1);
                    eventBean.setSyncFg(1);

                    eventBean.setCheckSumWeb(joEvent.getString("CheckSum"));
                    eventBean.setDistanceSinceLastValidCoordinate(joEvent.getString("DistanceSinceLastValidCoordinate"));
                    eventBean.setMalfunctionIndicatorFg(joEvent.getBoolean("MalfunctionIndicatorFg") ? 1 : 0);
                    eventBean.setDataDiagnosticIndicatorFg(joEvent.getBoolean("DataDiagnosticIndicatorFg") ? 1 : 0);
                    eventBean.setDiagnosticCode(joEvent.getString("DiagnosticCode"));
                    eventBean.setAccumulatedVehicleMiles(joEvent.getString("AccumulatedVehicleMiles"));
                    eventBean.setElaspsedEngineHour(joEvent.getString("ElaspsedEngineHour"));
                    eventBean.setMotorCarrier(joEvent.getString("MotorCarrier"));
                    eventBean.setShippingDocumentNo(joEvent.getString("ShippingDocumentNo"));
                    eventBean.setTrailerNo(joEvent.getString("TrailerNo"));
                    eventBean.setTimeZoneOffsetUTC(joEvent.getString("TimeZoneOffsetUTC"));
                    eventBean.setCoDriverId(joEvent.getInt("CoDriverId"));
                    eventBean.setAnnotation(joEvent.getString("Annotation"));

                    eventBean.setSplitSleep(joEvent.getBoolean("SplitSleepFg") ? 1 : 0);
                    eventBean.setRuleId(joEvent.getInt("RuleId"));
                    eventBean.setTimeZoneShortName(joEvent.getString("TimeZoneShortName"));

                    arrEvent.add(eventBean);
                }
                bean.setEventList(arrEvent);
                al.add(bean);
            }

            if (al.size() > 0) {
                int logId = DailyLogDB.DailyLogSave(al);
                status = (logId > 0);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            //  LogFile.write(GetCall.class.getName() + "::LogInfoSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 05 May 2017
    // Purpose: sync Geo Fence info with web
    public static boolean GeofenceGetSync() {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.GEOFENCE_GET
                            + Utility.companyId);
            if (result == null || result.isEmpty())
                return status;

            JSONArray obja = new JSONArray(result);
            ArrayList<GeofenceBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                GeofenceBean bean = new GeofenceBean();
                bean.setPortId(json.getInt("PortId"));
                bean.setPortRegionName(json.getString("PortRegionName"));
                bean.setLatitude(json.getString("Latitude"));
                bean.setLongitude(json.getString("Longitude"));
                bean.setRadius(json.getInt("Radius"));
                bean.setStatusId(json.getInt("StatusId"));
                al.add(bean);
            }

            if (al.size() > 0) {
                GeofenceDB.Save(al);
            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            //  LogFile.write(GetCall.class.getName() + "::CarrierInfoSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }


    public static void SyncMonthly() {

        // if internet is not available don't check update
        if (!Utility.isInternetOn())
            return;

        final long days = System.currentTimeMillis() / (24 * 60 * 60 * 1000);
        long lastPostedDate = Utility.getPreferences("sync_monthly", (days - 30));
        if ((days - lastPostedDate) < 30)
            return;
        Thread thSyncMonthly = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(60000);
                    if (Utility.packageId > 2)
                        GeofenceGetSync();
                    Utility.savePreferences("sync_monthly", days);
                } catch (Exception e) {
                    Utility.printError(e.getMessage());
                }
            }
        });
        thSyncMonthly.setName("thSyncMonthly");
        thSyncMonthly.start();

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 30 July 2018
    // Purpose: get Notification From Web
    public static String getNotification() {
        String result = "";

        WebService ws = new WebService();
        try {
            result = ws
                    .doGet(WebUrl.NOTIFICATION_GET
                            + NotificationDB.getLastNotificationId());
            if (result == null || result.isEmpty())
                return result;


        } catch (Exception e) {
            result = "";
            Utility.printError(e.getMessage());
            LogFile.write(GetCall.class.getName() + "::getNotification Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
            LogDB.writeLogs(GetCall.class.getName(), "getNotification Error:", e.getMessage(), Utility.printStackTrace(e));
        }
        return result;
    }


    // Created By: Deepak Sharma
    // Created Date: 9 April 2019
    // Purpose: sync DailylogRule info with web
    public static boolean DailylogRuleGetSync(String fromDate, String toDate) {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.GET_DAILYLOG_RULE
                            + Utility.onScreenUserId + "&fromDate=" + fromDate.replaceAll(" ", "%20") + "&toDate=" + toDate.replaceAll(" ", "%20"));
            if (result == null || result.isEmpty())
                return status;

            JSONArray obja = new JSONArray(result);
            ArrayList<RuleBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                RuleBean bean = new RuleBean();
                bean.setRuleId(json.getInt("RuleId"));
                bean.setStartDate(json.getString("StartDate"));
                bean.setEndDate(json.getString("EndDate"));
                bean.setDriverId(json.getInt("DriverId"));
                al.add(bean);
            }

            if (al.size() > 0) {
                DailyLogDB.DailyLogRuleSave(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            //  LogFile.write(GetCall.class.getName() + "::CarrierInfoSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 9 April 2019
    // Purpose: sync DailylogRule info with web
    public static boolean DailylogCoDriverGetSync(String fromDate, String toDate) {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.GET_DAILYLOG_CODRIVER
                            + Utility.onScreenUserId + "&fromDate=" + fromDate.replaceAll(" ", "%20") + "&toDate=" + toDate.replaceAll(" ", "%20"));
            if (result == null || result.isEmpty())
                return status;

            JSONArray obja = new JSONArray(result);
            ArrayList<CoDriverBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                CoDriverBean bean = new CoDriverBean();
                bean.setDriverId(json.getInt("DriverId"));
                bean.setDriverId2(json.getInt("DriverId2"));
                bean.setEndDate(json.getString("EndDate"));
                bean.setStartDate(json.getString("StartDate"));
                bean.setEndDate(json.getString("EndDate"));
                al.add(bean);
            }

            if (al.size() > 0) {
                DailyLogDB.CoDriverSave(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            //  LogFile.write(GetCall.class.getName() + "::CarrierInfoSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 9 April 2019
    // Purpose: sync DailylogRule info with web
    public static boolean DVIRGetSync(String fromDate) {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.GET_DAILYLOG_DVIR
                            + Utility.onScreenUserId + "&fromDate=" + fromDate.replaceAll(" ", "%20"));
            if (result == null || result.isEmpty())
                return status;

            // get driver name
            String driverName = Utility.onScreenUserId == Utility.user1.getAccountId() ? (Utility.user1.getFirstName() + " " + Utility.user1.getLastName()) : (Utility.user2.getFirstName() + " " + Utility.user2.getLastName());

            JSONArray obja = new JSONArray(result);
            ArrayList<TripInspectionBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                TripInspectionBean bean = new TripInspectionBean();
                bean.setInspectionDateTime(json.getString("DVIRDateTime"));
                bean.setDriverId(json.getInt("UserId"));
                bean.setDriverName(driverName);
                bean.setType(json.getInt("InspectionType"));
                bean.setDefect(json.getBoolean("DefectFg") ? 1 : 0);
                bean.setDefectRepaired(json.getBoolean("RepairedFg") ? 1 : 0);
                bean.setSafeToDrive(json.getBoolean("SafeToDriveFg") ? 1 : 0);
                bean.setDefectItems(json.getString("DefectItems"));
                bean.setLatitude(json.getString("Latitude"));
                bean.setLongitude(json.getString("Longitude"));
                bean.setLocation(json.getString("LocationDescription"));
                bean.setOdometerReading(json.getString("OdometerReading"));
                bean.setTruckNumber(json.getString("TruckNumber"));
                bean.setTrailerNumber(json.getString("TrailerNumber"));
                bean.setComments(json.getString("Comments"));
                bean.setPictures("");
                bean.setSyncFg(1);
                al.add(bean);
            }

            if (al.size() > 0) {
                TripInspectionDB.Save(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }


    // Created By: Pallavi Wattamwar
    // Created Date: 6 June 2019
    // Purpose: get CTPAT Inspection
    public static boolean CTPATGetSync(String fromDate) {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.GET_CTPAT
                            + Utility.onScreenUserId + "&fromDate=" + fromDate.replaceAll(" ", "%20"));
            if (result == null || result.isEmpty())
                return status;

            // get driver name
            String driverName = Utility.onScreenUserId == Utility.user1.getAccountId() ? (Utility.user1.getFirstName() + " " + Utility.user1.getLastName()) : (Utility.user2.getFirstName() + " " + Utility.user2.getLastName());

            JSONArray obja = new JSONArray(result);
            ArrayList<CTPATInspectionBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                CTPATInspectionBean bean = new CTPATInspectionBean();
                bean.setDateTime(json.getString("DateTime"));
                bean.setDriverId(json.getInt("UserId"));
                bean.setDriverName(driverName);
                bean.setType(json.getInt("InspectionType"));
                bean.setTruckNumber(json.getString("TruckNumber"));
                bean.setTrailer(json.getString("TrailerNumber"));
                bean.setComments(json.getString("Comments"));
                bean.setStatusId(json.getInt("StatusId"));
                bean.setInspectionCriteria(json.getString("InspectionCriteria"));
                bean.setCompanyId(Utility.companyId);
                bean.setCompanyName(Utility.CarrierName);
                bean.setSyncFg(1);
                al.add(bean);
            }

            if (al.size() > 0) {
                CTPATInspectionDB.Save(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 30 jan 2020
    // Purpose: get Notification From Web
    public static String DeviceBalanceGet() {
        String result = "";

        WebService ws = new WebService();
        try {
            result = ws
                    .doGet(WebUrl.DEVICE_BALANCE_GET
                            + Utility.IMEI);
            if (result == null || result.isEmpty())
                return result;

            JSONObject obj = new JSONObject(result);

            result = obj.getString("BalanceRemaining");
            Utility.savePreferences("RemainingBalance", result);


        } catch (Exception e) {
            result = "";
            Utility.printError(e.getMessage());
        }
        return result;
    }

    // Created By: Sahil Bansal
    // Created Date: 28 February 2020
    // Purpose: get the all training document like pdf and videos from web
    public static boolean DocumentGetSync() {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.DOCUMENT_GET + "FAQ,Manual,TrainingVideos" + "&companyid=" + "1");
            if (result == null || result.isEmpty())
                return status;

            JSONArray obja = new JSONArray(result);
            List<TrainingDocumentBean> list = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                TrainingDocumentBean bean = new TrainingDocumentBean();
                bean.setCreatedDate(Utility.getJsonString(json, Constants.CREATEDDATE));
                bean.setDocumentContentType(Utility.getJsonString(json, Constants.DOCUMENTCONTENTTYPE));
                bean.setDocumentId(json.getInt(Constants.DOCUMENTID));
                bean.setDocumentName(Utility.getJsonString(json, Constants.DOCUMENTNAME));

                bean.setDocumentPath(Utility.getJsonString(json, Constants.DOCUMENTPATH));
                bean.setDocumentSize(json.getInt(Constants.DOCUMENTSIZE));
                bean.setDocumentType(Utility.getJsonString(json, Constants.DOCUMENTTYPE));

                bean.setImportedDate(Utility.getJsonString(json, Constants.IMPORTEDDATE));
                bean.setModifiedDate(Utility.getJsonString(json, Constants.MODIFIEDDATE));
                bean.setStatusId(json.getInt(Constants.STATUSID));
                list.add(bean);
            }


            if (list.size() > 0) {
                TrainingDocumentDB.Save(list);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 26 March 2020
    // Purpose: get vehicle data by date
    public static int MidnightEventGet(String logDate, int driverId) {
        int status = 1;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.GET_VEHICLE_DATA + driverId + "&logDate=" + logDate);
            if (result == null || result.isEmpty())
                return -1;
            JSONObject jsonObject = new JSONObject(result);

            String odometer = Double.valueOf(jsonObject.getDouble("OdometerReading")).intValue() + "";
            String engineHours = Double.valueOf(jsonObject.getDouble("EngineHours")) + "";
            String lat = Utility.truncate(Double.valueOf(jsonObject.getDouble("Latitude")), 2) + "";
            String lon = Utility.truncate(Double.valueOf(jsonObject.getDouble("Longitude")), 2) + "";

            // add mid night event
            status = EventDB.MidnightEventSave(logDate, lat, lon, odometer, engineHours, driverId);


        } catch (Exception e) {
            status = 0;
            Utility.printError(e.getMessage());
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 01 May 2020
    // Purpose: get app info
    public static AppInfoBean AppInfoGet() {
        String result = "";
        AppInfoBean appInfo = null;

        WebService ws = new WebService();
        try {
            result = ws
                    .doGet(WebUrl.APP_INFO_GET);
            if (result == null || result.isEmpty()) {
                return appInfo;
            }
            JSONObject obj = new JSONObject(result);
            appInfo = new AppInfoBean();
            appInfo.setVersionCode(obj.getLong("VersionCode"));
            appInfo.setVersionName(obj.getString("VersionName"));
            appInfo.setVersionDate(Utility.parse(obj.getString("VersionDate")));
            appInfo.setMandatoryUpdateFg(obj.getBoolean("MandatoryUpdateFg"));


        } catch (Exception e) {
            appInfo = null;
            Utility.printError(e.getMessage());
        }

        return appInfo;
    }


    // Created By: Deepak Sharma
    // Created Date: 20 April 2017
    // Purpose: sync schedule info with web
    public static boolean ScheduleGetSync() {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.SCHEDULE_DETAIL_GET
                            + Utility.vehicleId);
            if (result == null || result.isEmpty())
                return status;

            JSONArray obja = new JSONArray(result);
            ArrayList<ScheduleBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                ScheduleBean bean = new ScheduleBean();
                bean.setScheduleId(json.getInt("ScheduleId"));
                bean.setSchedule(json.getString("Schedule"));
                bean.setEffectiveDate(json.getString("EffectiveDate"));
                bean.setThreshold(json.getInt("Threshold"));
                bean.setUnit(json.getString("Unit"));
                bean.setStatusId(json.getInt("StatusId"));
                al.add(bean);
            }

            if (al.size() > 0) {
                ScheduleDB.Save(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            //  LogFile.write(GetCall.class.getName() + "::CarrierInfoSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }
}