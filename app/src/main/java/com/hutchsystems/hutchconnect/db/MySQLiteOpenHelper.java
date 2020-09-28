package com.hutchsystems.hutchconnect.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hutchsystems.hutchconnect.common.DiagnosticMalfunction;
import com.hutchsystems.hutchconnect.db.table.DrayageDispatchContract.DrayageDispatch;
import com.hutchsystems.hutchconnect.db.table.DrayageDispatchContract.DrayageDispatchDetail;
import com.hutchsystems.hutchconnect.db.table.DrayageDispatchContract.DrayageRouteDetail;

/**
 * Created by Deepak.Sharma on 7/20/2015.
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    public static final String TABLE_CARRIER = "Carrier";
    public static final String TABLE_ACCOUNT = "Account";
    public static final String TABLE_STATE = "State";
    public static final String TABLE_PLACE = "Place";
    public static final String TABLE_DAILYLOG = "DailyLog";
    public static final String TABLE_DAILYLOG_EVENT = "DailyLog_Event";
    public static final String TABLE_DAILYLOG_CODRIVER = "DailyLog_CoDriver";
    public static final String TABLE_RULE = "Rule";
    public static final String TABLE_DAILYLOG_RULE = "DailyLog_Rule";
    public static final String TABLE_EVENT_TYPE = "Event_Type";
    public static final String TABLE_TRACKING = "Event_Tracking";
    public static final String TABLE_GpsLocation = "GpsLocation";
    public static final String TABLE_Message = "Message";
    public static final String TABLE_TRIP_INSPECTION = "Trip_Inspection";
    public static final String TABLE_VERSION_INFORMATION = "Version_Information";
    public static final String TABLE_SETTINGS = "Settings";
    public static final String TABLE_DIAGNOSTIC_INDICATOR = "DiagnosticIndicator";
    public static final String TABLE_DTC = "DTCCODE";
    public static final String TABLE_ALERT = "Alert";
    public static final String TABLE_TPMS = "Tpms";
    public static final String TABLE_TRAILER = "Trailer";
    public static final String TABLE_TRAILER_STATUS = "TrailerStatus";

    public static final String TABLE_AXLE_INFO = "AxleInfo";
    public static final String TABLE_VEHICLE_INFO = "VehicleInfo";

    // table for eld4.0.0
    public static final String TABLE_FUEL_DETAIL = "FuelDetail";
    public static final String TABLE_CARD_DETAIL = "CardDetail";
    public static final String TABLE_INCIDENT_DETAIL = "IncidentDetail";
    public static final String TABLE_MAINTENANCE_DETAIL = "MaintenanceDetail";
    public static final String TABLE_DOCUMENT_UPLOAD_PENDING = "DocumentUploadPending";
    public static final String TABLE_VIOLATION_DETAIL = "ViolationDetail";
    public static final String TABLE_SCHEDULE = "ScheduleDetail";
    public static final String TABLE_MAINTENANCE_DUE = "MaintenanceDueDetail";
    public static final String TABLE_VEHICLE_MAINTENANCE_DETAIL = "VehicleMaintenance";
    public static final String TABLE_GEOFENCE = "GeoFence";
    public static final String TABLE_GEOFENCE_MONITOR = "GeoFenceMonitor";
    public static final String TABLE_DOCUMENT_DETAIL = "DocumentDetailDB";

    public static final String TABLE_TICKET_DETAIL = "TicketDetailDB";
    public static final String TABLE_LOG_DETAIL = "LogDetail";

    // table for notification
    public static final String TABLE_NOTIFICATION = "Notification";

    // Table configrable annotation
    public static final String TABLE_ANNOTATION_DETAIL = "AnnotationDetail";

    public static final String TABLE_CTPAT_INSPECTION = "CTPAT_Inspection";

    public static final String TABLE_DEVICE_BALANCE_TRANSACTION= "DEVICE_BALANCE_TRANSACTION";

    public static final String TABLE_TRAINING_DOCUMENT = "TrainingDocument";
    public static final String COLUMN_ID = "_id";
    private static final String DATABASE_NAME = "EDL.db";
    private static final int DATABASE_VERSION = 5;


    private static final String TABLE_CREATE_DOCUMENT_DETAIL = "create table "
            + TABLE_DOCUMENT_DETAIL
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, DocumentType INTEGER, DocumentPath text, DocumentNo text, DriverId INTEGER,CreatedDate text,SyncFg INTEGER)";

    private static final String TABLE_CREATE_GEOFENCE = "create table "
            + TABLE_GEOFENCE
            + "(portId INTEGER, PortRegionName text,Latitude text,Longitude text,Radius INTEGER,StatusId INTEGER,SyncFG INTEGER,fenceCreatedFg INTEGER )";

    private static final String TABLE_CREATE_GEOFENCE_MONITOR = "create table "
            + TABLE_GEOFENCE_MONITOR
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,portId INTEGER,vehicleId INTEGER,enterDate text,exitDate text,regionInFg INTEGER,enterByDriverId INTEGER,exitByDriverId INTEGER,SyncFg INTEGER)";

    private static final String TABLE_CREATE_MAINTENANCE_DUE = "create table "
            + TABLE_MAINTENANCE_DUE
            + "(_Id INTEGER PRIMARY KEY AUTOINCREMENT, ScheduleId INTEGER,EffectiveOn INTEGER, DueOn INTEGER,RepairFG INTEGER,DueStatus INTEGER, SyncFG INTEGER)";// duestatus: 0-Initial ,1-Upcoming,2-Due

    private static final String TABLE_CREATE_SCHEDULE = "create table "
            + TABLE_SCHEDULE
            + "(ScheduleId INTEGER, Schedule text,EffectiveDate text,Threshold INTEGER,Unit text,StatusId INTEGER,SyncFG INTEGER)";

    private static final String TABLE_CREATE_DOCUMENT_UPLOAD_PENDING = "create table "
            + TABLE_DOCUMENT_UPLOAD_PENDING
            + "(_Id INTEGER PRIMARY KEY AUTOINCREMENT, DocType text,DocPath text)";


    private static final String TABLE_CREATE_INCIDENT_DETAIL = "create table "
            + TABLE_INCIDENT_DETAIL
            + "(_Id INTEGER PRIMARY KEY AUTOINCREMENT,IncidentDate text, DriverId INTEGER, VehicleId INTEGER, Duration INTEGER , ReportNo text, Level INTEGER, Result INTEGER,FineAmount text," +
            "Comment text,Latitude text, Longitude text, Location text,type INTEGER,trailerNo text,ModifiedBy INTEGER,ModifiedDate text,SyncFg INTEGER,DocPath text)";

    private static final String TABLE_CREATE_CARD_DETAIL = "create table "
            + TABLE_CARD_DETAIL
            + "(CardId INTEGER, CardNo text,VehicleId INTEGER)";

    private static final String TABLE_CREATE_VEHICLE_INFO = "create table "
            + TABLE_VEHICLE_INFO
            + "(_Id INTEGER PRIMARY KEY AUTOINCREMENT, OdometerReading text ,Speed text ,RPM text ,Average text ,EngineHour text ,FuelUsed text ,IdleFuelUsed text ,IdleHours text ,Boost text ,CoolantTemperature text ,CoolantLevel text ,BatteryVoltage text ,WasherFluidLevel text ,EngineLoad text ,EngineOilLevel text ,CruiseSetFg INTEGER ,CruiseSpeed text ,PowerUnitABSFg INTEGER ,TrailerABSFg INTEGER ,DerateFg INTEGER ,BrakeApplication INTEGER ,RegenerationRequiredFg INTEGER ,WaterInFuelFg INTEGER ,MaxRoadSpeed text ,PTOEngagementFg INTEGER ,CuriseTime INTEGER ,SeatBeltFg INTEGER ,AirSuspension text ,TransmissionOilLevel text ,TransmissionGear INTEGER ,DEFTankLevel text ,DEFTankLevelLow text,ActiveDTCFg INTEGER,InActiveDTCFg INTEGER,PTOHours INTEGER,TPMSWarningFg INTEGER,FuelLevel INTEGER,EngineSerialNo text,EngineRatePower text,FuelPressure text,AirInletTemperature text,BarometricPressure text,EngineOilPressure text,CreatedDate text ,SyncFg INTEGER)";

    private static final String TABLE_CREATE_CARRIER = "create table "
            + TABLE_CARRIER
            + "(CompanyId INTEGER,CarrierName text,ELDManufacturer text,USDOT text,VehicleId INTEGER,UnitNo text,PlateNo text,VIN text,StatusId INTEGER,SerialNo text,MACAddress text,TimeZoneId text,TotalAxle INTEGER,CanBusFg INTEGER,StartOdometerReading text, ReferralCode text)";

    private static final String TABLE_CREATE_TRAILER = "create table "
            + TABLE_TRAILER
            + "(VehicleId INTEGER,UnitNo text,PlateNo text,TotalAxle INTEGER)";

    private static final String TABLE_CREATE_TRAILER_STATUS = "create table "
            + TABLE_TRAILER_STATUS
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,startOdometer text,endOdometer text,TrailerId INTEGER,driverId INTEGER,hookDate text,unhookDate text,hookedFg INTEGER,modifiedBy INTEGER,latitude1 text,longitude1 text,latitude2 text, longitude2 text,SyncFg INTEGER)";

    private static final String TABLE_CREATE_AXLE_INFO = "create table "
            + TABLE_AXLE_INFO
            + "(axleId INTEGER,VehicleId INTEGER,axleNo INTEGER,axlePosition INTEGER,doubleTireFg INTEGER,frontTireFg INTEGER,sensorIds text,pressures text,temperatures text,PowerUnitFg INTEGER)";

    private static final String TABLE_CREATE_ACCOUNT = "create table "
            + TABLE_ACCOUNT
            + "(AccountId INTEGER,FirstName text,LastName text,EmailId text,MobileNo text,AccountType INTEGER,DrivingLicense text,DLIssueState text,LicenseExpiryDate text,Username text," +
            "Password text,DotPassword text,Salt text,ExemptELDUseFg INTEGER,ExemptionRemarks text,SpecialCategory INTEGER,CurrentRule INTEGER,TimeZoneOffsetUTC text,LicenseAcceptFg INTEGER,StatusId INTEGER,SyncFg INTEGER)";

    private static final String TABLE_CREATE_PLACE = "create table "
            + TABLE_PLACE
            + "(FEATURE_ID INTEGER PRIMARY KEY,FEATURE_NAME text ,FEATURE_CLASS text ,STATE_ALPHA text ,STATE_NUMERIC text ,COUNTY_NAME text ,COUNTY_NUMERIC text ,PRIMARY_LAT_DMS text ,PRIM_LONG_DMS text ,PRIM_LAT_DEC text ,PRIM_LONG_DEC text ,SOURCE_LAT_DMS text ,SOURCE_LONG_DMS text ,SOURCE_LAT_DEC text ,SOURCE_LONG_DEC text ,ELEV_IN_M text ,ELEV_IN_FT text ,MAP_NAME text ,DATE_CREATED text ,DATE_EDITED text)";

    private static final String TABLE_CREATE_STATE = "create table "
            + TABLE_STATE
            + "(StateId INTEGER,CountryId INTEGER,StateName text,ShortStateName text)";
    private static final String TABLE_CREATE_DAILYLOG = "create table "
            + TABLE_DAILYLOG
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,OnlineDailyLogId INTEGER,LogDate text,DriverId INTEGER,ShippingId text,TrailerId text,Commodity text,StartTime text,StartOdometerReading text,EndOdometerReading text," +
            "CertifyFG INTEGER,certifyCount INTEGER,Signature text,DrivingTimeRemaining INTEGER,WorkShiftTimeRemaining INTEGER,TimeRemaining70 INTEGER,TimeRemaining120 INTEGER,TimeRemainingUS70 INTEGER,TimeRemainingReset INTEGER,CreatedBy INTEGER,CreatedDate text,ModifiedBy INTEGER,ModifiedDate text,StatusId INTEGER,SyncFg INTEGER)";

    private static final String TABLE_CREATE_DAILYLOG_EVENT = "create table "
            + TABLE_DAILYLOG_EVENT
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,DriverId INTEGER,OnlineEventId INTEGER,EventSequenceId INTEGER,EventType INTEGER,EventCode INTEGER,EventCodeDescription text,OdometerReading text,EngineHour text," +
            "EventRecordOrigin INTEGER,EventRecordStatus INTEGER,EventDateTime text,Latitude text,Longitude text,LocationDescription text,DailyLogId INTEGER," +
            "CreatedBy INTEGER,CreatedDate text,ModifiedBy INTEGER,ModifiedDate text,StatusId INTEGER,SyncFg INTEGER,VehicleId INTEGER, CheckSum text,DistanceSinceLastValidCoordinate text,MalfunctionIndicatorFg INTEGER,DataDiagnosticIndicatorFg INTEGER,DiagnosticCode text,Annotation text,AccumulatedVehicleMiles text,ElaspsedEngineHour text," +
            "MotorCarrier text,ShippingDocumentNo text,TrailerNo text,TimeZoneOffsetUTC text,CoDriverId INTEGER)";

    private static final String TABLE_CREATE_DAILYLOG_CODRIVER = "create table "
            + TABLE_DAILYLOG_CODRIVER
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,DriverId INTEGER,DriverId2 INTEGER,LoginTime text,LogoutTime text,SyncFg INTEGER)";

    private static final String TABLE_CREATE_RULE = "create table "
            + TABLE_RULE
            + "(RuleId INTEGER,RuleName text,RuleDescription text,CreatedBy INTEGER,CreatedDate text,ModifiedBy INTEGER,ModifiedDate text,StatusId INTEGER)";

    private static final String TABLE_CREATE_DAILYLOG_RULE = "create table "
            + TABLE_DAILYLOG_RULE
            + "(_Id INTEGER PRIMARY KEY AUTOINCREMENT,DailyLogId INTEGER,RuleId INTEGER,RuleStartTime text,RuelEndTime text,SyncFg INTEGER)";

    private static final String TABLE_CREATE_EVENT_TYPE = "create table "
            + TABLE_EVENT_TYPE
            + "(EventTypeId INTEGER,EventType text,EventCode INTEGER,EventCodeDescription text,CreatedBy INTEGER,CreatedDate text,ModifiedBy INTEGER,ModifiedDate text,StatusId INTEGER)";

    private static final String TABLE_CREATE_TRACKING = "create table "
            + TABLE_TRACKING
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,LocationDate text,Latitude text,Longitude text,DriverId INTEGER,VehicleId INTEGER,Speed text,OdometerReading text,EngineHours text,RPM text,Variation text,Heading text,LocationAddress text,StateName text,CreatedBy INTEGER,CreatedDate text,StatusId INTEGER,SyncFg INTEGER)";

    private static final String TABLE_CREATE_GpsLocation = "create table "
            + TABLE_GpsLocation
            + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT,GPSDate text,signal text,SyncFg INTEGER)";

    private static final String TABLE_CREATE_Message = "create table "
            + TABLE_Message
            + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT,Message text,MessageToId INTEGER,CreatedById INTEGER,MessageDate text,SendFg INTEGER,DeliveredFg INTEGER,ReadFg INTEGER,DeviceId text,SyncFg INTEGER)";

    private static final String TABLE_CREATE_TRIP_INSPECTION = "create table "
            + TABLE_TRIP_INSPECTION
            + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, DateTime text, DriverId INTEGER, DriverName text, Type INTEGER, Defect INTEGER, DefectRepaired INTEGER, SafeToDrive INTEGER, DefectItems text, Latitude text, Longitude text, LocationDescription text, Odometer text, TruckNumber text, TrailerNumber text, Comments text, Pictures text,SyncFg INTEGER)";

    private static final String TABLE_CREATE_VERSION_INFORMATION = "create table "
            + TABLE_VERSION_INFORMATION
            + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, AutoDownloadFg INTEGER, AutoUpdateFg INTEGER, CurrentVersion text, DownloadDate text, DownloadFg INTEGER, LiveFg INTEGER, PreviousVersion text, SerialNo text,"
            + " UpdateArchiveName text, UpdateDate text, UpdateUrl text, UpdatedFg INTEGER, VersionDate text, SyncFg INTEGER)";

    private static final String TABLE_CREATE_SETTINGS = "create table "
            + TABLE_SETTINGS
            + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, TimeZone text, DefaultRule INTEGER, GraphLine INTEGER, ColorLineUS INTEGER, ColorLineCanada INTEGER,"
            + " TimeFormat INTEGER, ViolationReading INTEGER, MessageReading INTEGER, StartTime INTEGER,"
            + " Orientation INTEGER, VisionMode INTEGER, CopyTrailer INTEGER, ShowViolation INTEGER, SyncTime INTEGER, AutomaticRuleChange INTEGER,"
            + " ViolationOnGrid INTEGER, FontSize INTEGER, DutyStatusReading INTEGER, Unit INTEGER, DrivingScreen INTEGER, DriverId INTEGER)";

    private static final String TABLE_CREATE_DIAGNOSTIC_INDICATOR = "create table "
            + TABLE_DIAGNOSTIC_INDICATOR
            + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT,PowerDiagnosticFg INTEGER, EngineSynchronizationDiagnosticFg INTEGER, MissingElementDiagnosticFg INTEGER, DataTransferDiagnosticFg INTEGER, UnidentifiedDrivingDiagnosticFg INTEGER, OtherELDIdentifiedDiagnosticFg INTEGER, PowerMalfunctionFg INTEGER, EngineSynchronizationMalfunctionFg INTEGER, TimingMalfunctionFg INTEGER, PositioningMalfunctionFg INTEGER, DataRecordingMalfunctionFg INTEGER, DataTransferMalfunctionFg INTEGER, OtherELDDetectedMalfunctionFg INTEGER)";

    private static final String TABLE_CREATE_DTC = "create table "
            + TABLE_DTC
            + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT,DateTime text,spn INTEGER, Protocol text, spnDescription text, fmi INTEGER, fmiDescription text,Occurrence INTEGER, SyncFg INTEGER, status INTEGER)";
    private static final String TABLE_CREATE_ALERT = "create table "
            + TABLE_ALERT
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,AlertCode text,AlertName text,AlertDateTime text,Duration INTEGER,Scores INTEGER,DriverId INTEGER,VehicleId INTEGER,Latitude text,Longitude text,Location text,Threshold text,SyncFg INTEGER)";

    private static final String TABLE_CREATE_TPMS = "create table "
            + TABLE_TPMS
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,SensorId text,Temperature Integer,Pressure Integer,Voltage text,CreatedDate text,ModifiedDate text,DriverId INTEGER,VehicleId INTEGER,SyncFg INTEGER)";

    private static final String TABLE_CREATE_FUEL_DETAIL = "create table "
            + TABLE_FUEL_DETAIL
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, CardId INTEGER, VehicleId INTEGER, DriverId INTEGER, Duration INTEGER, FuelDateTime text, Quantity text, Price text, DEFFuelQuantity text, DEFFuelPrice text, ReferFuelQuantity text, ReferFuelPrice text, CashAdvance text, Images text, ModifiedBy INTEGER, ModifiedDate text, Latitude text, Longitude text, Location text,trailerNo text, SyncFG INTEGER)";

    private static final String TABLE_CREATE_MAINTENANCE_DETAIL = "create table "
            + TABLE_MAINTENANCE_DETAIL
            + "(MaintenanceAlertId INTEGER, ScheduleId INTEGER,ScheduleName text, DueDate text,RepairFg INTEGER, MaintenanceId INTEGER, DUEKM text, DUEMILES text,DUEENGINEHOURS text )";

    private static final String TABLE_CREATE_VIOLATION_DETAIL = "create table "
            + TABLE_VIOLATION_DETAIL
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, ViolationDate text, Rule text,TotalMinute text,DriverId INTEGER, SyncFg INTEGER )";

    private static final String TABLE_CREATE_VEHICLE_MAINTENANCE_DETAIL = "create table "
            + TABLE_VEHICLE_MAINTENANCE_DETAIL
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, MaintenanceDate text,VehicleId INTEGER,UnitNo text,CurrencyId INTEGER,ItemId INTEGER,ScheduleId INTEGER,DriverId INTEGER, RepairedBy text,Comment text,Description text,InvoiceNo text,PartCost text, LabourCost text,OdometerReading text,dueOn INTEGER, DocPath text, SyncFg INTEGER)";

    private static final String TABLE_CREATE_TICKET_DETAIL = "create table "
            + TABLE_TICKET_DETAIL
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,TicketId INTEGER,Type INTEGER,TicketNo text, TicketDate text, Title text,Comment text,DriverId INTEGER,CreatedBy INTEGER, CreatedDate text,ModifiedBy INTEGER,ModifiedDate text,TicketStatus INTEGER, SyncFg INTEGER)";

    private static final String SQL_CREATE_DRAYAGE_DISPATCH =
            "CREATE TABLE IF NOT EXISTS " + DrayageDispatch.TABLE_NAME + "(" +
                    DrayageDispatch._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DrayageDispatch.COLUMN_DISPATCHID + " INTEGER," +
                    DrayageDispatch.COLUMN_DRIVERID + " INTEGER," +
                    DrayageDispatch.COLUMN_DISPATCHDATE + " TEXT," +
                    DrayageDispatch.COLUMN_DISPATCHNO + " TEXT," +
                    DrayageDispatch.COLUMN_BOOKINGNO + " TEXT," +
                    DrayageDispatch.COLUMN_CUSTOMER + " TEXT," +
                    DrayageDispatch.COLUMN_CUSTOMERADDRESS + " TEXT," +
                    DrayageDispatch.COLUMN_CUSTOMERLATITUDE + " TEXT," +
                    DrayageDispatch.COLUMN_CUSTOMERLONGITUDE + " TEXT," +

                    DrayageDispatch.COLUMN_STEAMSHIPLINECOMPANY + " TEXT," +
                    DrayageDispatch.COLUMN_SSLADDRESS + " TEXT," +
                    DrayageDispatch.COLUMN_SSLLATITUDE + " TEXT," +
                    DrayageDispatch.COLUMN_SSLLONGITUDE + " TEXT," +

                    DrayageDispatch.COLUMN_PICKUPCOMPANY + " TEXT," +
                    DrayageDispatch.COLUMN_PICKUPADDRESS + " TEXT," +
                    DrayageDispatch.COLUMN_PICKUPLATITUDE + " TEXT," +
                    DrayageDispatch.COLUMN_PICKUPLONGITUDE + " TEXT," +

                    DrayageDispatch.COLUMN_DROPCOMPANY + " TEXT," +
                    DrayageDispatch.COLUMN_DROPADDRESS + " TEXT," +
                    DrayageDispatch.COLUMN_DROPLATITUDE + " TEXT," +
                    DrayageDispatch.COLUMN_DROPLONGITUDE + " TEXT," +

                    DrayageDispatch.COLUMN_EMPTYRETURNCOMPANY + " TEXT," +
                    DrayageDispatch.COLUMN_EMPTYRETURNADDRESS + " TEXT," +
                    DrayageDispatch.COLUMN_EMPTYRETURNLATITUDE + " TEXT," +
                    DrayageDispatch.COLUMN_EMPTYRETURNLONGITUDE + " TEXT," +

                    DrayageDispatch.COLUMN_NOOFCONTAINER + " INTEGER," +
                    DrayageDispatch.COLUMN_STATUS + " INTEGER," +
                    DrayageDispatch.COLUMN_NOTES + " TEXT)";


    private static final String SQL_CREATE_DRAYAGE_DISPATCH_DETAIL =
            "CREATE TABLE IF NOT EXISTS " + DrayageDispatchDetail.TABLE_NAME + "(" +
                    DrayageDispatchDetail.COLUMN_DISPATCHID + " INTEGER," +
                    DrayageDispatchDetail.COLUMN_PICKDROPID + " INTEGER," +
                    DrayageDispatchDetail.COLUMN_DISPATCHDETAILID + " INTEGER," +
                    DrayageDispatchDetail.COLUMN_APPOINTMENTNO + " TEXT," +
                    DrayageDispatchDetail.COLUMN_APPOINTMENTDATE + " TEXT," +
                    DrayageDispatchDetail.COLUMN_CONTAINERNO + " TEXT," +
                    DrayageDispatchDetail.COLUMN_CONTAINERSIZE + " TEXT," +
                    DrayageDispatchDetail.COLUMN_CONTAINERTYPE + " TEXT," +
                    DrayageDispatchDetail.COLUMN_MAXGROSSWEIGHT + " INTEGER," +
                    DrayageDispatchDetail.COLUMN_TAREWEIGHT + " INTEGER," +
                    DrayageDispatchDetail.COLUMN_MAXPAYLOAD + " INTEGER," +
                    DrayageDispatchDetail.COLUMN_MANUFACTURINGDATE + " TEXT," +
                    DrayageDispatchDetail.COLUMN_SEALNO1 + " TEXT," +
                    DrayageDispatchDetail.COLUMN_SEALNO2 + " TEXT," +
                    DrayageDispatchDetail.COLUMN_DOCUMENT_PATH + " TEXT," +
                    DrayageDispatchDetail.COLUMN_CONTAINERGRADE + " TEXT," +
                    DrayageDispatchDetail.COLUMN_MODIFIEDDATE + " TEXT," +
                    DrayageDispatchDetail.COLUMN_SYNCFG + " INTEGER)";


    private static final String SQL_CREATE_ROUTE_DETAIL =
            "CREATE TABLE IF NOT EXISTS " + DrayageRouteDetail.TABLE_NAME + "(" +
                    DrayageRouteDetail.COLUMN_DISPATCHID + " INTEGER," +
                    DrayageRouteDetail.COLUMN_PICKDROPID + " INTEGER," +
                    DrayageRouteDetail.COLUMN_ARRIVALDATE + " TEXT," +
                    DrayageRouteDetail.COLUMN_DEPARTUREDATE + " TEXT," +
                    DrayageRouteDetail.COLUMN_SYNCFG + " INTEGER)";

    // Table for Notification
    private static final String TABLE_CREATE_NOTIFICATION = "create table "
            + TABLE_NOTIFICATION
            + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT,Title text,NotificationId INTEGER,NotificationDate text,StatusId INTEGER,Comment text)";


    // Table for Log Details   _Id,ExceptionDate,Title,StackTrace,Class,Method,SyncFg
    private static final String TABLE_CREATE_LOG_DETAIL = "create table "
            + TABLE_LOG_DETAIL
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, ExceptionDate text, Title text, StackTrace text, Class text,Method text,SyncFg INTEGER)";

    private static final String DATABASE_ALTER_DAILYLOG_TOTAL_DISTANCE = "ALTER TABLE " + TABLE_DAILYLOG + " ADD COLUMN TotalDistance INTEGER";
    private static final String DATABASE_ALTER_DAILYLOG_PC_DISTANCE = "ALTER TABLE " + TABLE_DAILYLOG + " ADD COLUMN PCDistance INTEGER";


    private static final String DATABASE_ALTER_TABLE_SETTINGS_VIOLATION_ON_DRIVING_SCREEN = "ALTER TABLE " + TABLE_SETTINGS + " ADD COLUMN ViolationOnDrivingScreen INTEGER";

    private static final String DATABASE_ALTER_DRAYAGE_DISPATCH_SYNCFG = "ALTER TABLE " + DrayageDispatch.TABLE_NAME + " ADD COLUMN " + DrayageDispatch.COLUMN_SYNCFG + "  INTEGER";

    private static final String DATABASE_ALTER_TABLE_ACCOUNT_TIMEZONEID = "ALTER TABLE " + TABLE_ACCOUNT + " ADD COLUMN TimeZoneId text";
    private static final String DATABASE_ALTER_TABLE_ACCOUNT_ADDRESS = "ALTER TABLE " + TABLE_ACCOUNT + " ADD COLUMN Address text";
    private static final String DATABASE_ALTER_TABLE_ACCOUNT_SIGNATURE = "ALTER TABLE " + TABLE_ACCOUNT + " ADD COLUMN Signature text";


    private static final String DATABASE_ALTER_TABLE_SETTINGS_Vehicle_OR_Unit = "ALTER TABLE " + TABLE_SETTINGS + " ADD COLUMN VehicleOdometerUnit INTEGER";

    private static final String DATABASE_ALTER_CARRIER_ELDCONFIG = "ALTER TABLE " + TABLE_CARRIER + " ADD COLUMN EldConfig text";
    private static final String DATABASE_ALTER_FUEL_DETAIL_FUEL_UNIT = "ALTER TABLE " + TABLE_FUEL_DETAIL + " ADD COLUMN FuelUnit INTEGER";
    private static final String DATABASE_ALTER_DAILYLOG_RULE_DRIVERID = "ALTER TABLE " + TABLE_DAILYLOG_RULE + " ADD COLUMN DriverId INTEGER";

    private static final String DATABASE_ALTER_CARRIER_SETUPFG = "ALTER TABLE " + TABLE_CARRIER + " ADD COLUMN SetupFg INTEGER";
    private static final String DATABASE_ALTER_CARRIER_PROTOCOL = "ALTER TABLE " + TABLE_CARRIER + " ADD COLUMN Protocol text";
    private static final String DATABASE_ALTER_CARRIER_ODOMETER_SOURCE = "ALTER TABLE " + TABLE_CARRIER + " ADD COLUMN OdometerSource INTEGER";
    private static final String DATABASE_ALTER_CARRIER_SYNC_FG = "ALTER TABLE " + TABLE_CARRIER + " ADD COLUMN SyncFg INTEGER";

    private static final String DATABASE_ALTER_TABLE_ACCOUNT_DAY_LIGHT_SAVING_FG = "ALTER TABLE " + TABLE_ACCOUNT + " ADD COLUMN DaylightSavingFg INTEGER";

    private static final String DATABASE_ALTER_TABLE_SETTINGS_SPLIT_SLEEP_ENABLE = "ALTER TABLE " + TABLE_SETTINGS + " ADD COLUMN EnableSplitSlip INTEGER";
    private static final String DATABASE_ALTER_TABLE_SETTINGS_SPLIT_SLEEP_SHOW = "ALTER TABLE " + TABLE_SETTINGS + " ADD COLUMN ShowAlertSplitSlip INTEGER";

    private static final String DATABASE_ALTER_TABLE_DAILYLOG_EVENT_SPLITFG = "ALTER TABLE " + TABLE_DAILYLOG_EVENT + " ADD COLUMN Splitfg INTEGER";
    private static final String DATABASE_ALTER_TABLE_DAILYLOG_EVENT_RULE_ID = "ALTER TABLE " + TABLE_DAILYLOG_EVENT + " ADD COLUMN RuleId INTEGER";
    private static final String DATABASE_ALTER_TABLE_DAILYLOG_EVENT_TIMEZONE = "ALTER TABLE " + TABLE_DAILYLOG_EVENT + " ADD COLUMN TimeZoneShortName  text";

    // column for rating and user comment
    private static final String DATABASE_ALTER_TABLE_TICKET_DETAIL_RATING = "ALTER TABLE " + TABLE_TICKET_DETAIL + " ADD COLUMN Rating  INTEGER";
    private static final String DATABASE_ALTER_TABLE_TICKET_DETAIL_USERFEEDBACK = "ALTER TABLE " + TABLE_TICKET_DETAIL + " ADD COLUMN UserFeedback  text";

    // Coloumn for Defered Day For Rules
    private static final String DATABASE_ALTER_TABLE_DAILYLOG_DEFERED_DAY = "ALTER TABLE " + TABLE_DAILYLOG + " ADD COLUMN DeferedDay INTEGER";

    // column for DVIR Status
    private static final String DATABASE_ALTER_TABLE_TRIP_INSPECTION = "ALTER TABLE " + TABLE_TRIP_INSPECTION + " ADD COLUMN StatusId INTEGER";

    // keep track if user has reduced hours
    private static final String DATABASE_ALTER_TABLE_DAILYLOG_EVENT_REDUCED_FG = "ALTER TABLE " + TABLE_DAILYLOG_EVENT + " ADD COLUMN ReducedFg INTEGER";
    // keep track if user has reduced hours
    private static final String DATABASE_ALTER_TABLE_DAILYLOG_EVENT_WAITING_FG = "ALTER TABLE " + TABLE_DAILYLOG_EVENT + " ADD COLUMN WaitingFg INTEGER";

    // column for Dashboard
    private static final String DATABASE_ALTER_TABLE_SETTINGS_DRIVING_SCREEN = "ALTER TABLE " + TABLE_SETTINGS + " ADD COLUMN SetDrivingScreen INTEGER";

    // coloumn for Support language
    private static final String DATABASE_ALTER_TABLE_ACCOUNT_LANGUAGE_FOR_SUPPORT = "ALTER TABLE " + TABLE_ACCOUNT + " ADD COLUMN LanguageForSupport text";

    // column for Annotation Detail
    private static final String TABLE_CREATE_ANNOTATION_DETAIL = "create table "
            + TABLE_ANNOTATION_DETAIL
            + "(AnnotaionId INTEGER, CompanyId INTEGER,DisplayOrder INTEGER,Annotation text)";


    // COLOUMN FOR  AppDownloadFromPlayStore
    private static final String TABLE_ALTER_VERSION_INFORMATION_APP_DOWNLOAD_PLAYSTORE = "ALTER TABLE " + TABLE_VERSION_INFORMATION + " ADD COLUMN AppDownloadFromPlayStore INTEGER";
    // COLOUMN FOR  SyncDate
    private static final String TABLE_ALTER_VERSION_INFORMATION_APP_SYNC_DATE = "ALTER TABLE " + TABLE_VERSION_INFORMATION + " ADD COLUMN SyncDate text";
    // Coloumn For File Content Length
    private static final String TABLE_ALTER_VERSION_INFORMATION_APP_FILECONTENT_LENGTH = "ALTER TABLE " + TABLE_VERSION_INFORMATION + " ADD COLUMN FileContentLength INTEGER";
    // Coloumn For Trailer DVIR
    private static final String DATABASE_ALTER_TABLE_TRIP_INSPECTION_TRAILER_DVIR_FG = "ALTER TABLE " + TABLE_TRIP_INSPECTION + " ADD COLUMN TrailerDvirFg INTEGER";

    // column for CTPAT Inspection
    private static final String TABLE_CREATE_TABLE_CTPAT_INSPECTION = "create table "
            + TABLE_CTPAT_INSPECTION
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, DateTime text,DriverId INTEGER, DriverName text,CompanyID INTEGER,CompanyName text,Type INTEGER, InspectionCriteria text,InspectionResult INTEGER, TruckNumber text, TrailerNumber text, Comments text, SealValue text,SyncFg INTEGER, StatusId INTEGER)";


    private static final String DATABASE_ALTER_TABLE_CARRIER_REFERRAL_CODE = "ALTER TABLE " + TABLE_CARRIER + " ADD COLUMN ReferralCode text";

    // column for Annotation Detail
    private static final String TABLE_CREATE_DEVICE_BALANCE_TRANSACTION = "create table "
            + TABLE_DEVICE_BALANCE_TRANSACTION
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, BillDate text, TransactionDate text, DeviceId text,VehicleId INTEGER,CreatedBy INTEGER,SyncFg INTEGER)";

    // column for training document
    private static final String TABLE_CREATE_TRAINING_DOCUMENT = "create table "
            + TABLE_TRAINING_DOCUMENT
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, CreatedDate text, DocumentContentType text,DocumentId INTEGER,DocumentName text,DocumentPath text,DocumentSize text,DocumentType text," +
            "ImportedDate text, ModifiedDate text,StatusId INTEGER )";

    private static final String DATABASE_ALTER_TABLE_ACCOUNT_DEditFg = "ALTER TABLE " + TABLE_ACCOUNT + " ADD COLUMN DEditFg INTEGER";
    private static final String DATABASE_ALTER_TABLE_SETTINGS_DEditFg = "ALTER TABLE " + TABLE_SETTINGS + " ADD COLUMN DEditFg INTEGER";
    private static final String DATABASE_ALTER_TABLE_ACCOUNT_TrainedFG = "ALTER TABLE " + TABLE_ACCOUNT + " ADD COLUMN TrainedFg INTEGER";

    public MySQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(TABLE_CREATE_CARRIER);
        db.execSQL(TABLE_CREATE_ACCOUNT);
        db.execSQL(TABLE_CREATE_STATE);
        db.execSQL(TABLE_CREATE_PLACE);
        db.execSQL(TABLE_CREATE_RULE);
        db.execSQL(TABLE_CREATE_EVENT_TYPE);
        db.execSQL(TABLE_CREATE_DAILYLOG);
        db.execSQL(TABLE_CREATE_DAILYLOG_EVENT);
        db.execSQL(TABLE_CREATE_DAILYLOG_CODRIVER);
        db.execSQL(TABLE_CREATE_DAILYLOG_RULE);
        db.execSQL(TABLE_CREATE_TRACKING);
        db.execSQL(TABLE_CREATE_GpsLocation);
        db.execSQL(TABLE_CREATE_Message);
        db.execSQL(TABLE_CREATE_TRIP_INSPECTION);
        db.execSQL(TABLE_CREATE_VERSION_INFORMATION);
        db.execSQL(TABLE_CREATE_SETTINGS);
        db.execSQL(TABLE_CREATE_DIAGNOSTIC_INDICATOR);
        db.execSQL(TABLE_CREATE_DTC);
        db.execSQL(TABLE_CREATE_ALERT);
        db.execSQL(TABLE_CREATE_TPMS);
        db.execSQL(TABLE_CREATE_TRAILER);
        db.execSQL(TABLE_CREATE_AXLE_INFO);
        db.execSQL(TABLE_CREATE_TRAILER_STATUS);
        db.execSQL(TABLE_CREATE_VEHICLE_INFO);
        db.execSQL(TABLE_CREATE_CARD_DETAIL);
        db.execSQL(TABLE_CREATE_FUEL_DETAIL);
        db.execSQL(TABLE_CREATE_INCIDENT_DETAIL);
        db.execSQL(TABLE_CREATE_MAINTENANCE_DETAIL);
        db.execSQL(TABLE_CREATE_DOCUMENT_UPLOAD_PENDING);
        db.execSQL(TABLE_CREATE_VIOLATION_DETAIL);
        db.execSQL(TABLE_CREATE_SCHEDULE);
        db.execSQL(TABLE_CREATE_MAINTENANCE_DUE);
        db.execSQL(TABLE_CREATE_VEHICLE_MAINTENANCE_DETAIL);
        db.execSQL(TABLE_CREATE_GEOFENCE);
        db.execSQL(TABLE_CREATE_GEOFENCE_MONITOR);
        db.execSQL(TABLE_CREATE_DOCUMENT_DETAIL);
        db.execSQL(SQL_CREATE_DRAYAGE_DISPATCH);
        db.execSQL(SQL_CREATE_DRAYAGE_DISPATCH_DETAIL);
        db.execSQL(SQL_CREATE_ROUTE_DETAIL);
        db.execSQL(DATABASE_ALTER_DRAYAGE_DISPATCH_SYNCFG);

        // new changes from aobrd
        db.execSQL(DATABASE_ALTER_DAILYLOG_TOTAL_DISTANCE);
        db.execSQL(DATABASE_ALTER_DAILYLOG_PC_DISTANCE);
        db.execSQL(TABLE_CREATE_TICKET_DETAIL);
        db.execSQL(DATABASE_ALTER_TABLE_ACCOUNT_TIMEZONEID);
        db.execSQL(DATABASE_ALTER_TABLE_ACCOUNT_ADDRESS);
        db.execSQL(DATABASE_ALTER_TABLE_SETTINGS_Vehicle_OR_Unit);
        db.execSQL(DATABASE_ALTER_CARRIER_ELDCONFIG);
        db.execSQL(DATABASE_ALTER_FUEL_DETAIL_FUEL_UNIT);
        db.execSQL(DATABASE_ALTER_DAILYLOG_RULE_DRIVERID);
        db.execSQL(DATABASE_ALTER_TABLE_ACCOUNT_SIGNATURE);
        // added columns for retain setup information after data clear or re setup
        db.execSQL(DATABASE_ALTER_CARRIER_SETUPFG);
        db.execSQL(DATABASE_ALTER_CARRIER_PROTOCOL);
        db.execSQL(DATABASE_ALTER_CARRIER_ODOMETER_SOURCE);
        db.execSQL(DATABASE_ALTER_CARRIER_SYNC_FG);

        // For Notification
        db.execSQL(TABLE_CREATE_NOTIFICATION);
        // for daylight saving
        db.execSQL(DATABASE_ALTER_TABLE_ACCOUNT_DAY_LIGHT_SAVING_FG);

        // for splitslip and splitflag

        db.execSQL(DATABASE_ALTER_TABLE_SETTINGS_SPLIT_SLEEP_ENABLE);
        db.execSQL(DATABASE_ALTER_TABLE_SETTINGS_SPLIT_SLEEP_SHOW);
        db.execSQL(DATABASE_ALTER_TABLE_DAILYLOG_EVENT_SPLITFG);

        // For Adding RuleID Coloumn

        db.execSQL(DATABASE_ALTER_TABLE_DAILYLOG_EVENT_RULE_ID);

        // For TimeZone short name
        db.execSQL(DATABASE_ALTER_TABLE_DAILYLOG_EVENT_TIMEZONE);

        // fOR Rating and user Comment in ticket table
        db.execSQL(DATABASE_ALTER_TABLE_TICKET_DETAIL_RATING);
        db.execSQL(DATABASE_ALTER_TABLE_TICKET_DETAIL_USERFEEDBACK);

        // Table For Log Details
        db.execSQL(TABLE_CREATE_LOG_DETAIL);

        // alter table defered day coloumn added
        db.execSQL(DATABASE_ALTER_TABLE_DAILYLOG_DEFERED_DAY);

        // alter table for Status
        db.execSQL(DATABASE_ALTER_TABLE_TRIP_INSPECTION);

        db.execSQL(DATABASE_ALTER_TABLE_DAILYLOG_EVENT_REDUCED_FG);

        db.execSQL(DATABASE_ALTER_TABLE_DAILYLOG_EVENT_WAITING_FG);

        // Set Driving Screen
        db.execSQL(DATABASE_ALTER_TABLE_SETTINGS_DRIVING_SCREEN);

        // alter table for support language
        db.execSQL(DATABASE_ALTER_TABLE_ACCOUNT_LANGUAGE_FOR_SUPPORT);

        // set Annotation table
        db.execSQL(TABLE_CREATE_ANNOTATION_DETAIL);

        // Alter version table for flag AppDownloadFromPlayStore ,SyncDate,FileContentlength
        db.execSQL(TABLE_ALTER_VERSION_INFORMATION_APP_DOWNLOAD_PLAYSTORE);
        db.execSQL(TABLE_ALTER_VERSION_INFORMATION_APP_SYNC_DATE);
        db.execSQL(TABLE_ALTER_VERSION_INFORMATION_APP_FILECONTENT_LENGTH);
        db.execSQL(DATABASE_ALTER_TABLE_TRIP_INSPECTION_TRAILER_DVIR_FG);

        // Crate table for
        db.execSQL(TABLE_CREATE_TABLE_CTPAT_INSPECTION);
        db.execSQL(DATABASE_ALTER_TABLE_SETTINGS_VIOLATION_ON_DRIVING_SCREEN);
        db.execSQL(TABLE_CREATE_DEVICE_BALANCE_TRANSACTION);
        db.execSQL(TABLE_CREATE_TRAINING_DOCUMENT);

        db.execSQL(DATABASE_ALTER_TABLE_ACCOUNT_DEditFg);
        db.execSQL(DATABASE_ALTER_TABLE_SETTINGS_DEditFg);
        db.execSQL(DATABASE_ALTER_TABLE_ACCOUNT_TrainedFG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion<2)
        {
            db.execSQL(DATABASE_ALTER_TABLE_CARRIER_REFERRAL_CODE);
            db.execSQL(TABLE_CREATE_DEVICE_BALANCE_TRANSACTION);
        }
        if (oldVersion<3)
        {
            db.execSQL(TABLE_CREATE_TRAINING_DOCUMENT);
        }

        if (oldVersion<4)
        {
            db.execSQL(DATABASE_ALTER_TABLE_ACCOUNT_DEditFg);
            db.execSQL(DATABASE_ALTER_TABLE_SETTINGS_DEditFg);
        }

        if (oldVersion < 5) {
            db.execSQL(DATABASE_ALTER_TABLE_ACCOUNT_TrainedFG);

            // update trained fg to default 1 for old driver
            UserDB.UpdateTrainedFg(db);
            // delete default malfunction and diagnostic event
            DiagnosticMalfunction.deleteData(db);
        }
    }
}