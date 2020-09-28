package com.hutchsystems.hutchconnect.common;

import android.os.AsyncTask;

public class CommonTask extends AsyncTask<String, Void, Boolean> {
    public static final String Post_DailyLog_DriverInfo = "DailyLogPostAll";
    public static final String Post_DailyLog_DriverInfo_DailyLogId = "DailyLogPostAcordingToDailyLogId";
    public static final String Post_Account = "PostAccount";
    public static final String Post_Dvir = "PostDVIR";
    public static final String Post_Alert = "PostAlert";
    public static final String Post_TMPS = "PostTPMS";
    public static final String Post_TrailerStatus = "PostTrailerStatus";
    public static final String Post_FuelDetail = "PostFuelDetail";
    public static final String Post_IncidentDetail = "PostIncidentDetail";
    public static final String Post_MaintenanceDueHistory = "POSTMaintenanceDueHistory";
    public static final String Post_Maintenance = "POSTMaintenance";
    public static final String Post_GeofenceMonitor = "PostGeofenceMonitor";
    public static final String Post_Violation = "PostViolation";
    public static final String Post_DocumentDetail = "PostDocumentDetail";
    public static final String Post_DispatchDetail = "PostDispatchDetail";
    public static final String Post_RouteDetail = "PostRouteDetail";
    public static final String Post_DispatchStatus = "PostDispatchStatus";
    public static final String Post_Setupinfo = "PostSetupinfo";
    public static final String Post_DTC = "PostDTC";
    public static final String Post_LOG = "PostLOG";
    public static final String POST_CTPAT = "POSTCTPAT";
    public static final String POST_VERSION_INFO = "PostVersionInfo";
    public static final String POST_DEVICE_BALANCE = "PostDeviceBalance";
    String Servicetype;
    int logId;

    public CommonTask(String servicetype) {
        Servicetype = servicetype;

    }

    public CommonTask(String servicetype, int logid) {
        Servicetype = servicetype;
        logId = logid;
    }

    // Common doInBackground task for Data Post
    @Override
    protected Boolean doInBackground(String... strings) {
        Boolean status = false;
        if (!Utility.isInternetOn()) {
            return status;
        }

        switch (Servicetype) {
            case Post_DailyLog_DriverInfo:
                if (Utility.isInternetOn())
                    status = PostCall.LogPostAll();
                break;
            case Post_DailyLog_DriverInfo_DailyLogId:
                status = PostCall.LogPostById(logId);
                break;
            case Post_Account:
                status = PostCall.PostAccount();
                break;
            case Post_Dvir:
                status = PostCall.PostDVIR();
                break;
            // Posting CTPAT Inspection
            case POST_CTPAT:
                status = PostCall.PostCTPATInspection();
                break;
            case Post_Alert:
                status = PostCall.PostAlert();
                break;
            case Post_FuelDetail:
                status = PostCall.PostFuelDetail();
                break;
            case Post_IncidentDetail:
                status = PostCall.PostIncidentDetail();
                break;
            case Post_MaintenanceDueHistory:
                status = PostCall.POSTMaintenanceDueHistory();
                break;
            case Post_Maintenance:
                status = PostCall.POSTMaintenance();
                break;
            case Post_GeofenceMonitor:
                status = PostCall.PostGeofenceMonitor();
                break;
            case Post_Violation:
                status = PostCall.PostViolation();
                break;
            case Post_DocumentDetail:
                status = PostCall.PostDocumentDetail();
                break;
            case Post_Setupinfo:
                status = PostCall.PostSetupinfo();
                break;
            case Post_LOG:
                status = PostCall.PostLogs();
                break;
            // Posting Version information
            case POST_VERSION_INFO:
                status = PostCall.PostVersionInfo();
                break;
            case POST_DEVICE_BALANCE:
                status = PostCall.PostDeviceBalance();
                break;
        }
        return status;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
    }
}
