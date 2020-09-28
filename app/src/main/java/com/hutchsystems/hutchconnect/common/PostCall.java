package com.hutchsystems.hutchconnect.common;

import android.util.Log;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.db.AlertDB;
import com.hutchsystems.hutchconnect.db.CTPATInspectionDB;
import com.hutchsystems.hutchconnect.db.CarrierInfoDB;
import com.hutchsystems.hutchconnect.db.DTCDB;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.DeviceBalanceDB;
import com.hutchsystems.hutchconnect.db.DocumentDetailDB;
import com.hutchsystems.hutchconnect.db.FuelDetailDB;
import com.hutchsystems.hutchconnect.db.GeofenceDB;
import com.hutchsystems.hutchconnect.db.IncidentDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.ScheduleDB;
import com.hutchsystems.hutchconnect.db.TripInspectionDB;
import com.hutchsystems.hutchconnect.db.UserDB;
import com.hutchsystems.hutchconnect.db.VehicleMaintenanceDB;
import com.hutchsystems.hutchconnect.db.VersionInformationDB;
import com.hutchsystems.hutchconnect.db.ViolationDB;
import com.hutchsystems.hutchconnect.tasks.MaintenanceSyncData;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Deepak.Sharma on 1/27/2016.
 */
public class PostCall {


    //Created By: Simran
    //Created Date: 30/01/2020
    //Purpose: get log data by id to post
    public static boolean postDailyLogById(int dailyLogId) {
        boolean status = true;
        WebService ws = new WebService();

        try {
            JSONObject obj = new JSONObject();
            // get daily log data with events and rules w.r.t daily log
            obj.put("logData", DailyLogDB.DailyLogByIdSync(dailyLogId));
            // get codriver data
            obj.put("coDriverList", DailyLogDB.CoDriverGetSync());
            //124 LogFile.write(PostCall.class.getName() + "::PostAll:\n" + obj.toString(), LogFile.AUTOMATICALLY_TASK, LogFile.AUTOSYNC_LOG);
            // post record to server
            String data = obj.toString();

            if (data.equals("{\"logData\":{},\"coDriverList\":[]}")) {
                return status;
            }
            String result = ws.doPost(
                    WebUrl.DAILY_LOG_POST_ALL,
                    obj.toString());
            if (result != null) {
                JSONObject response = new JSONObject(result);

                int id = response.getInt("Id");
                if (id != -1) {
                    // update syncFG column for the posted record
                    DailyLogDB.UpdateSyncStatusAll();
                }

            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST EVENT SYSNC
    public static boolean POSTEVENTSYNC(int eventRecordStatus, int eventRecordOrigin) {
        boolean status = true;
        WebService ws = new WebService();

        try {
            JSONObject obj = new JSONObject();
            obj.put("DriverId", Utility.onScreenUserId);
            obj.put("EVENTRECORDORIGIN", eventRecordOrigin);
            obj.put("EVENTRECORDSTATUS", eventRecordStatus);

            ws.doPost(
                    WebUrl.POST_EVENT_SYNC,
                    obj.toString());

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST EVENT SYSNC
    public static boolean PostAccount() {
        boolean status = true;
        WebService ws = new WebService();

        try {

            String data = UserDB.accountSyncGet();

            if (data.equals(""))
                return status;
            String result = ws.doPost(
                    WebUrl.POST_ACCOUNT,
                    data);
            if (result != null) {
                UserDB.accountSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST EVENT SYSNC
    public static boolean PostDVIR() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String data = TripInspectionDB.getDVIRSync().toString();

            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.POST_DVIR,
                    data);

            if (result != null) {
                TripInspectionDB.DVIRSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST DTC SYNC
    public static boolean PostDTC() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String data = DTCDB.getDTCCodeSync().toString();
            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.POST_DTC,
                    data);
            if (result != null) {
                DTCDB.DTCSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST Alerts SYNC
    public static boolean PostAlert() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String data = AlertDB.getAlertSync().toString();
            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.POST_ALERT,
                    data);
            if (result != null) {
                AlertDB.AlertSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 16 January 2017
    // Purpose: POST Fuel INFO SYNC
    public static boolean PostFuelDetail() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String data = FuelDetailDB.getFuelDetailSync().toString();
            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.POST_FUEL_DETAIL,
                    data);
            if (result != null) {
                FuelDetailDB.FuelDetailSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 16 January 2017
    // Purpose: POST Fuel INFO SYNC
    public static boolean PostIncidentDetail() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String data = IncidentDB.getIncidentSync().toString();
            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.POST_INCIDENT_DETAIL,
                    data);

            if (result != null) {
                IncidentDB.IncidentSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 15 March 2017
    // Purpose: POST EVENT SYSNC
    public static void PostDeviceInfo(final boolean onStart) {

        // if internet is not available don't check update
        if (!Utility.isInternetOn())
            return;

        String lastPostedDate = Utility.getPreferences("Device_Info_Date", "");

        if (lastPostedDate.equals(Utility.getCurrentDate()))
            return;

        Thread thDeviceInfo = new Thread(new Runnable() {
            @Override
            public void run() {

                WebService ws = new WebService();

                try {
                    if (onStart)
                        Thread.sleep(60000);
                    JSONObject obj = MainActivity.DeviceInfoGetSync();
                    String result = ws.doPost(WebUrl.POST_DEVICE_INFO, obj.toString());
                    if (result != null) {
                        //posted device info successfully
                        Utility.savePreferences("Device_Info_Date", Utility.getCurrentDate());
                        Utility.savePreferences("Device_Info_Last_Time", System.currentTimeMillis());
                    }

                } catch (Exception e) {
                    Utility.printError(e.getMessage());
                }
            }
        });
        thDeviceInfo.setName("thDeviceInfo");
        thDeviceInfo.start();
    }


    public static void PostDaily() {

        // if internet is not available don't check update
        if (!Utility.isInternetOn())
            return;

        String lastPostedDate = Utility.getPreferences("post_daily", "");
        if (lastPostedDate.equals(Utility.getCurrentDate()))
            return;

        new MaintenanceSyncData().execute();

    }


    // Created By: Deepak Sharma
    // Created Date: 16 January 2017
    // Purpose: POST Fuel INFO SYNC
    public static boolean PostViolation() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String data = ViolationDB.getViolationSync().toString();
            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.POST_VIOLATION,
                    data);
            if (result != null) {
                ViolationDB.ViolationSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }


    // Created By: Simran
    // Created Date: 6 March 2020
    // Purpose: get log data to post
    public static boolean LogPostAll() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            JSONObject obj = new JSONObject();
            // get daily log data with events and rules w.r.t daily log
            obj.put("logData", DailyLogDB.DailyLogGetSync());
            // get codriver data
            obj.put("coDriverList", DailyLogDB.CoDriverGetSync());
            //124 LogFile.write(PostCall.class.getName() + "::PostAll:\n" + obj.toString(), LogFile.AUTOMATICALLY_TASK, LogFile.AUTOSYNC_LOG);
            // post record to server
            String data = obj.toString();

            Log.i("EventPost: ", data);

            if (data.equals("{\"logData\":[],\"coDriverList\":[]}")) {
                return status;
            }
            String result = ws.doPost(WebUrl.DAILY_LOG_EVENT_POST_ALL, obj.toString());

            Log.i("EventPost: ", result);

            if (result != null) {
                JSONObject response = new JSONObject(result);

                int id = response.getInt("DailylogOnlineId");
                int _id = response.getInt("_ID");
                if (id != -1 && id != 0) {
                    DailyLogDB.UpdateDailyLogStatus(_id, id);
                    // update syncFG column for the posted record
                    JSONArray eventJsonArray = response.getJSONArray("EventList");
                    JSONArray ruleJsonArray = response.getJSONArray("RuleList");

                    for (int i = 0; i < eventJsonArray.length(); i++) {
                        JSONObject jsonObject = eventJsonArray.getJSONObject(i);
                        if (jsonObject.getInt("EventOnlineId") != 0) {
                            DailyLogDB.UpdateEventStatus(jsonObject.getInt("_ID"), jsonObject.getInt("EventOnlineId"));
                        }

                    }

                    for (int i = 0; i < ruleJsonArray.length(); i++) {
                        JSONObject jsonObject = ruleJsonArray.getJSONObject(i);
                        if (jsonObject.getInt("RulelOnlineId") != 0) {
                            DailyLogDB.UpdateRuleStatus(jsonObject.getInt("_ID"));
                        }
                    }
                }

            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        Log.i("EventPost: ", String.valueOf(status));
        return status;
    }

    // Created By: Simran
    // Created Date: 6 March 2020
    // Purpose: get log data to post
    public static boolean LogPostById(int logId) {
        boolean status = true;
        WebService ws = new WebService();

        try {
            JSONObject obj = new JSONObject();
            // get daily log data with events and rules w.r.t daily log
            obj.put("logData", DailyLogDB.DailyLogGetSync(logId));
            // get codriver data
            obj.put("coDriverList", DailyLogDB.CoDriverGetSync());
            //124 LogFile.write(PostCall.class.getName() + "::PostAll:\n" + obj.toString(), LogFile.AUTOMATICALLY_TASK, LogFile.AUTOSYNC_LOG);
            // post record to server
            String data = obj.toString();

            Log.i("EventPost: ", data);

            if (data.equals("{\"logData\":[],\"coDriverList\":[]}")) {
                return status;
            }
            String result = ws.doPost(WebUrl.DAILY_LOG_EVENT_POST_ALL, obj.toString());

            Log.i("EventPost: ", result);

            if (result != null) {
                JSONObject response = new JSONObject(result);

                int id = response.getInt("DailylogOnlineId");
                int _id = response.getInt("_ID");
                if (id != -1 && id != 0) {
                    DailyLogDB.UpdateDailyLogStatus(_id, id);
                    // update syncFG column for the posted record
                    JSONArray eventJsonArray = response.getJSONArray("EventList");
                    JSONArray ruleJsonArray = response.getJSONArray("RuleList");

                    for (int i = 0; i < eventJsonArray.length(); i++) {
                        JSONObject jsonObject = eventJsonArray.getJSONObject(i);
                        if (jsonObject.getInt("EventOnlineId") != 0) {
                            DailyLogDB.UpdateEventStatus(jsonObject.getInt("_ID"), jsonObject.getInt("EventOnlineId"));
                        }
                    }

                    for (int i = 0; i < ruleJsonArray.length(); i++) {
                        JSONObject jsonObject = ruleJsonArray.getJSONObject(i);
                        if (jsonObject.getInt("RulelOnlineId") != 0) {
                            DailyLogDB.UpdateRuleStatus(jsonObject.getInt("_ID"));
                        }
                    }
                }

            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        Log.i("EventPost: ", String.valueOf(status));
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 10 May 2017
    // Purpose: POST Geo FEnce SYNC
    public static boolean PostGeofenceMonitor() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String data = GeofenceDB.GeofenceMonitorGetSync().toString();
            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.GEOFENCE_MONITOR_POST,
                    data);

            if (result != null) {
                GeofenceDB.GeofenceMonitorSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST Instalation detail
    public static boolean POSTInstallationDetail(String date, String installerId) {
        boolean status = true;
        WebService ws = new WebService();
        String data = "";
        try {
            JSONObject obj = new JSONObject();
            obj.put("InstallerId", installerId);
            obj.put("InstallationDate", date);
            obj.put("Latitude", Utility.currentLocation.getLatitude() + "");
            obj.put("Longitude", Utility.currentLocation.getLongitude() + "");
            obj.put("MdtSerialNo", Utility.IMEI);
            obj.put("VehicleId", Utility.vehicleId);
            obj.put("CompanyId", Utility.companyId);
            data = obj.toString();
            String result = ws.doPost(
                    WebUrl.INSTALLATION_DETAIL_POST,
                    data);

            if (result != null) {
                JSONObject response = new JSONObject(result);

                int id = response.getInt("Id");
                if (id != -1) {
                    status = false;
                }
            } else {
                status = false;
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        if (!status) {
            Utility.savePreferences("installation_data", data);
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 10 May 2017
    // Purpose: POST Geo FEnce SYNC
    public static boolean PostDocumentDetail() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String data = DocumentDetailDB.GetSync().toString();
            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.DOCUMENT_DETAIL_POST,
                    data);

            if (result != null) {
                DocumentDetailDB.DocumentDetailSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST EVENT SYNC
    public static boolean PostSignature(String data) {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String result = ws.doPost(
                    WebUrl.SIGNATURE_POST,
                    data);

            if (result != null) {
                JSONObject response = new JSONObject(result);

            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 25 July 2018
    // Purpose: Post setup details like odometersource,Protocol
    public static boolean PostSetupinfo() {
        boolean status = true;
        WebService ws = new WebService();

        try {

            String data = CarrierInfoDB.getSetupInfoSync().toString();
            if (data.equals("{}")) {
                return status;
            }
            String result = ws.doPost(
                    WebUrl.SETUPINFO_POST,
                    data);

            if (result != null) {
                CarrierInfoDB.SetupInfoSyncUpdate();

            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }


    // Created By: Pallavi Wattamwar
    // Created Date: 18 December 2018
    // Purpose: POST Logs
    public static boolean PostLogs() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String data = LogDB.postLog().toString();
            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.LOG_POST,
                    data);
            if (result != null) {
                LogDB.LogSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }


    // Created By: Pallavi Wattamwar
    // Created Date: 23 May 2019
    // Purpose: POST Version information
    public static boolean PostVersionInfo() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String data = VersionInformationDB.postVersionInfo().toString();
            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.POST_UPDATE,
                    data);
            if (result != null) {
                VersionInformationDB.VersionSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 14 January 2019
    // Purpose: post log data as per Date
    public static boolean PostLogEventByDate(String date, int driverID) {
        boolean status = true;
        WebService ws = new WebService();

        try {
            JSONObject obj = new JSONObject();
            // get daily log data with events and rules w.r.t date
            obj.put("logData", DailyLogDB.DailyLogGetByDateSync(date, driverID));
            // get codriver data
            obj.put("coDriverList", DailyLogDB.CoDriverGetSync());
            //124 LogFile.write(PostCall.class.getName() + "::PostAll:\n" + obj.toString(), LogFile.AUTOMATICALLY_TASK, LogFile.AUTOSYNC_LOG);
            // post record to server
            String data = obj.toString();

            if (data.equals("{\"logData\":[],\"coDriverList\":[]}")) {
                return status;
            }
            String result = ws.doPost(
                    WebUrl.DAILY_LOG_POST_ALL,
                    obj.toString());

            if (result != null) {
                JSONObject response = new JSONObject(result);

                int id = response.getInt("Id");
                if (id != -1) {
                    // update syncFG column for the posted record
                    DailyLogDB.UpdateSyncStatusAll();
                }
                else
                {
                    status=false;
                }

            } else {
                status = false;
            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 04 July 2019
    // Purpose: POST CTPAT Inspection
    public static boolean PostCTPATInspection() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String data = CTPATInspectionDB.getCTPATInspectionSync().toString();

            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.POST_CTPAT,
                    data);
            if (result != null) {
                CTPATInspectionDB.CTPATSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }

        return status;
    }


    // Created By: Pallavi Wattamwar
    // Created Date: 04 July 2019
    // Purpose: POST CTPAT Inspection
    public static boolean PostDeviceBalance() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            JSONArray array = DeviceBalanceDB.getDeviceBalanceTransactionSync();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                int _id = obj.getInt("_id");

                String data = array.get(i).toString();

                String result = ws.doPost(
                        WebUrl.DEVICE_BALANCE_POST,
                        data);

                if (result != null) {
                    JSONObject response = new JSONObject(result);

                    int id = response.getInt("Id");

                    // if no error
                    if (id != -1) {
                        // remaining balance returned via services
                        String remainingBalance = response.getString("ExtraData");

                        // save remaining balance
                        Utility.savePreferences("RemainingBalance", remainingBalance);

                        DeviceBalanceDB.DeviceBalanceSync(_id);
                    }

                }

            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }

        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 01 May 2020
    // Purpose: post record to server if driver cancel update
    public static boolean AppUpdateCancel() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            JSONObject data = new JSONObject();

            data.put("CancelDate", Utility.getCurrentDateTime());
            data.put("DriverId", Utility.onScreenUserId);
            data.put("VehicleId", Utility.vehicleId);
            data.put("CompanyId", Utility.companyId);

            String result = ws.doPost(
                    WebUrl.APP_UPDATE_CANCEL,
                    data.toString());


        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }



    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST EVENT SYNC
    public static boolean POSTScheduleSYNC() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            JSONObject obj = new JSONObject();
            obj.put("VehicleId", Utility.vehicleId);

            ws.doPost(
                    WebUrl.POST_SCHEDULE_DETAIL,
                    obj.toString());

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST EVENT SYNC
    public static boolean POSTMaintenanceDueHistory() {
        boolean status = true;
        WebService ws = new WebService();

        try {

            String data = ScheduleDB.MaintenanceDueGetSync().toString();
            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.POST_MAINTENANCE_DUE,
                    data);

            if (result != null) {
                JSONObject response = new JSONObject(result);

                int id = response.getInt("Id");
                if (id != -1) {
                    ScheduleDB.MaintenanceDueSyncUpdate();
                }
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST EVENT SYNC
    public static boolean POSTMaintenance() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String data = VehicleMaintenanceDB.MaintenanceGetSync().toString();
            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.POST_MAINTENANCE,
                    data);

            if (result != null) {
                JSONObject response = new JSONObject(result);

                int id = response.getInt("Id");
                if (id != -1) {
                    VehicleMaintenanceDB.MaintenanceSyncUpdate();
                }
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

}
