package com.hutchsystems.hutchconnect.common;

/**
 * Created by Deepak.Sharma on 7/15/2015.
 */
public class WebUrl {
    //  static String port = ConstantFlag.LiveFg ? "3393" : "80";
    // test server port change
    public static String DOCUMENT_URL = "https://apps.hutchsystems.com/uploads/";
    static String port = ConstantFlag.LiveFg ? "3394" : "3394"; // ELD: port 3393, Test Server Port 80
    static String server = ConstantFlag.LiveFg ? "http://207.194.137.58" : "http://209.97.200.208";
    public final static String WS_BASE_URL = server + ":" + port + "/";
    public final static String BASE_URL = WS_BASE_URL + "ELogService.svc/";
    public final static String GET_CARRIER_INFO = BASE_URL + "Carrier/Get/?serialNo=";
    public final static String GET_ACCOUNT = BASE_URL + "Account/Get/?serialNo=";
    public final static String GET_LOG_DATA = BASE_URL + "LogData/Get/?driverId=";

    public final static String GET_LOG_EVENT_DATA = BASE_URL + "LogEventData/Get/?driverId=";

    public final static String GET_ASSIGNED_EVENT = BASE_URL + "AssignedEventGet/Get/?driverId=";
    public final static String GET_TRAILER_INFO = BASE_URL + "TrailerInfo/Get/?companyId=";
    public final static String GET_CARD_DETAIL = BASE_URL + "Card/Get/?vehicleId=";

    public final static String GET_DAILYLOG_RULE = BASE_URL + "DailyLogRule/Get/?driverId=";
    public final static String GET_DAILYLOG_CODRIVER = BASE_URL + "DailyLogCoDriver/Get/?driverId=";
    public final static String GET_DAILYLOG_DVIR = BASE_URL + "DVIR/Get/?driverId=";
    public final static String GET_VEHICLE_DATA = BASE_URL + "Vehicle/Data/Get/?driverId=";

    public final static String POST_ALL = BASE_URL + "Post/All";

    public final static String GET_UPDATE = BASE_URL + "Version/Get/?version=";

    public final static String POST_UPDATE = BASE_URL + "VersionUpdateProgress/Post";

    public final static String POST_EVENT_SYNC = BASE_URL + "Post/EventSync";
    public final static String POST_MESSAGE_SYNC = BASE_URL + "Post/MessageSync";
    public final static String POST_ACCOUNT = BASE_URL + "Account/Post";
    public final static String POST_DVIR = BASE_URL + "ELOGDVIR/Post";
    public final static String POST_DTC = BASE_URL + "DTC/Post";
    public final static String POST_ALERT = BASE_URL + "Alert/Post";

    public final static String POST_TPMS = BASE_URL + "TPMS/Post";
    public final static String POST_TRAILER_STATUS = BASE_URL + "TrailerStatus/Post";
    public final static String POST_VEHICLE_INFO = BASE_URL + "VehicleInfo/Post";
    public final static String POST_FUEL_DETAIL = BASE_URL + "Fuel/Post";
    public final static String POST_INCIDENT_DETAIL = BASE_URL + "Incident/Post";
    public final static String POST_MAINTENANCE_DETAIL = BASE_URL + "Maintenance/Post";
    public final static String FILE_UPLOAD_URL = BASE_URL + "Document/Upload";
    public final static String POST_DEVICE_INFO = BASE_URL + "DeviceInfo/Post";
    public final static String POST_VIOLATION = BASE_URL + "Violation/Post";


    public final static String DAILY_LOG_POST_ALL = BASE_URL + "DailyLogPost/All";
    public final static String DAILY_LOG_EVENT_POST_ALL = BASE_URL + "DailyLogEventPost/All";

    public final static String DAILY_LOG_EVENT_DATA_GET = BASE_URL + "DailyLogEventData/Get/?driverId=";


    public final static String EDIT_REQUEST_EVENT_GET = BASE_URL + "EditRequestGet/Get/?driverId=";
    public final static String SCHEDULE_DETAIL_GET = BASE_URL + "Schedule/Get/?vehicleId=";
    public final static String POST_SCHEDULE_DETAIL = BASE_URL + "Schedule/Post";
    public final static String POST_MAINTENANCE_DUE = BASE_URL + "MaintenanceDueHistory/Post";
    public final static String POST_MAINTENANCE = BASE_URL + "VehicleMaintenance/Post";


    public final static String DAILY_LOG_EVENT_BY_DATE_GET = BASE_URL + "DailyLogEventByDate/Get/?driverId=";
    public final static String GEOFENCE_GET = BASE_URL + "Geofence/Get/?companyId=";
    public final static String GEOFENCE_MONITOR_POST = BASE_URL + "GeofenceMonitor/Post";
    public final static String INSTALLATION_DETAIL_POST = BASE_URL + "InstallationDetail/Post";
    public final static String DOCUMENT_DETAIL_POST = BASE_URL + "DocumentDetail/Post";
    public final static String DISPATCH_DETAIL_POST = BASE_URL + "DispatchDetail/Post";
    public final static String ROUTE_DETAIL_POST = BASE_URL + "RouteDetail/Post";
    public final static String DISPATCH_STATUS_POST = BASE_URL + "DispatchStatus/Post";

    public final static String TICKET_POST = BASE_URL + "TicketPost/Post";
    public final static String SIGNATURE_POST = BASE_URL + "SignaturePost/Post";
    public final static String FMCSA_OUTPUT_FILE_POST = "https://eldws.fmcsa.dot.gov/ELDSubmissionService.svc";
    public final static String OUTPUT_FILE_UPLOAD_URL = BASE_URL + "OutputFile/Post";
    public final static String SEND_OUTPUT_FILE_TO_FMCSA = BASE_URL + "SendToFMCSA/Post";

    // for sending setupinfo details
    public final static String SETUPINFO_POST = BASE_URL + "SetupInfo/Post";

    // Notification URL
    public final static String NOTIFICATION_GET = BASE_URL + "Notification/Get/?notificationId=";

    //PostRating nd Feedback
    public final static String TICKET_RATING_POST = BASE_URL + "TicketRatingPost/Post";

    // for posting Logs
    public final static String LOG_POST = BASE_URL + "ErrorLog/Post";


    // For Posting Email Address and Fax number
    public final static String MAIL_POST = BASE_URL + "SendTo/Post";

    // Get Annotaion From WebService
    public final static String GET_ANNOTATION_INFO = BASE_URL + "AnnotationSetting/Get/?companyId=";

    //Post CTPAT Inspection
    public final static String POST_CTPAT = BASE_URL + "CTPAT/Post";
    public final static String GET_CTPAT = BASE_URL + "CTPAT/Get/?driverId=";

    public final static String DEVICE_BALANCE_POST = BASE_URL + "DeviceBalance/Post";
    public final static String DEVICE_BALANCE_GET = BASE_URL + "DeviceBalance/Get/?deviceId=";

    public final static String DOCUMENT_GET = BASE_URL + "Document/Get/?documenttype=";
    public final static String APP_INFO_GET = BASE_URL + "AppInfo/Get/?appname=Hutch Connect&operatingsystem=Android";

    public final static String APP_UPDATE_CANCEL = BASE_URL + "AppUpdate/Cancel";
}
