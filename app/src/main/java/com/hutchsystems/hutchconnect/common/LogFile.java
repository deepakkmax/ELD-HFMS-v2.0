package com.hutchsystems.hutchconnect.common;

import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.hutchsystems.hutchconnect.tasks.MailSync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class LogFile {
    final static String TAG = LogFile.class.getName();
    final static public String LOG_ERROR_NAME = "LogError";
    final static public String LOG_CANBUS_NAME = "Canbus";
    final static public String LOG_NOLOGIN_NAME = "UnidentifiedEvent";
    final static public String LOG_DRIVEREVENT_NAME = "DriverEvent";
    final static public String LOG_AUTOSYNC_NAME = "AutoSync";
    final static public String LOG_TCPCLIENT_NAME = "TCPClient";
    final static public String LOG_GPS_NAME = "TrackingLocation";
    final static public String DATABASE_BACKUP_NAME = "ELDbackup.db";
    final static public String ELD_CONFIG_NAME = "eldconfig";
    final static public String LOG_EXTENSION = ".txt";

    public final static int MID_NIGHT = 1;
    public final static int AFTER_MID_NIGHT = 2;

    final static public int BLUETOOTH_CONNECTIVITY = 99;
    final static public int CAN_BUS_READ = 100;
    final static public int DATABASE = 101;
    final static public int WEB_SERVICE = 102;
    final static public int SETUP = 103;
    final static public int USER_INTERACTION = 104;
    final static public int DIAGNOSTIC_MALFUNCTION = 105;
    final static public int APPLICATION = 106;
    final static public int AUTOMATICALLY_TASK = 106;

    final static public int ERROR_LOG = 120;
    final static public int CANBUS_LOG = 121;
    final static public int NOLOGIN_LOG = 122;
    final static public int DRIVEREVENT_LOG = 123;
    final static public int AUTOSYNC_LOG = 124;
    final static public int TCPCLIENT_LOG = 125;
    final static public int GPS_LOG = 126;
    final static public int DATABASE_BACKUP = 127;
    final static public int ELD_CONFIG = 128;

    static boolean logFileSent = false;
    static int sentTime;

    static MailSync.PostTaskListener<Boolean> mailPostTaskListener = new MailSync.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            logFileSent = result;
            if (result) {
                Log.i(TAG, "Mail successfully");
                //save status that already sent mail
                SharedPreferences prefs = Utility.context.getSharedPreferences("HutchGroup", Utility.context.MODE_PRIVATE);
                prefs.edit().putBoolean("logfile_sent", true).commit();
                /*//create new logfile
                createLogFile(ERROR_LOG);*/

                if (ConstantFlag.Flag_Log_CanBus)
                    createLogFile(CANBUS_LOG);
                if (ConstantFlag.Flag_Log_NoLogin)
                    createLogFile(NOLOGIN_LOG);
                if (ConstantFlag.Flag_Log_DriverEvent)
                    createLogFile(DRIVEREVENT_LOG);
                if (ConstantFlag.Flag_Log_TCPClient)
                    createLogFile(TCPCLIENT_LOG);
                if (ConstantFlag.Flag_Log_GPS)
                    createLogFile(GPS_LOG);
                if (ConstantFlag.Flag_Log_AutoSyncTask)
                    createLogFile(AUTOSYNC_LOG);

            } else {
                Log.i(TAG, "Mail failed");
                SharedPreferences prefs = Utility.context.getSharedPreferences("HutchGroup", Utility.context.MODE_PRIVATE);
                prefs.edit().putBoolean("logfile_sent", false).commit();
            }
        }
    };

    public LogFile() {

    }

    public static void createLogFile(int logType) {
        try {
            String path = getLogFilePath(logType);
            Log.i(TAG, path);
            File file = Environment.getExternalStorageDirectory();
            String[] array = path.split("/");

            File f = new File(file, array[array.length - 1]);
            //FileOutputStream fileout = this.openFileOutput(f, Context.MODE_APPEND);
            FileOutputStream fileout = new FileOutputStream(f, false);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.append(Utility.IMEI + "\n" + Utility.getCurrentDateTime() + "\n\n");
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "CANNOT CREATE LOG FILE " + e.getMessage());
        }
    }

    public static void write(String infos, int errorCode, int logType) {
        // add-write text into file
        try {
            if (!ConstantFlag.Flag_Log_CanBus && logType == CANBUS_LOG)
                return;
            if (!ConstantFlag.Flag_Log_NoLogin && logType == NOLOGIN_LOG)
                return;
            if (!ConstantFlag.Flag_Log_DriverEvent && logType == DRIVEREVENT_LOG)
                return;
            if (!ConstantFlag.Flag_Log_AutoSyncTask && logType == AUTOSYNC_LOG)
                return;
            if (!ConstantFlag.Flag_Log_TCPClient && logType == TCPCLIENT_LOG)
                return;
            if (!ConstantFlag.Flag_Log_GPS && logType == GPS_LOG)
                return;
            String path = getLogFilePath(logType);
            //Log.i(TAG, path);
            File file = Environment.getExternalStorageDirectory();
            String[] array = path.split("/");

            File f = new File(file, array[array.length - 1]);
            //FileOutputStream fileout = this.openFileOutput(f, Context.MODE_APPEND);
            FileOutputStream fileout = new FileOutputStream(f, true);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            infos = errorCode + " - Date: " + getCurrentDateTime() + " \n " + infos + "\n";
            outputWriter.append(infos);
            outputWriter.close();
            fileout.close();

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "CANNOT CREATE LOG FILE " + e.getMessage());
        }
    }

    public static void ELD2020DataLog(String data) {
        // add-write text into file
        try {

            String folderPath = "/HutchConnectLogs/";

            if (isExternalStorageWritable()) {
                folderPath = Environment.getExternalStorageDirectory() + folderPath;
            }
            File folder = new File(folderPath);

            if (!folder.exists()) {
                folder.mkdirs();
            }

            String path = folderPath + "eldhutchconnect-data-" + Utility.getCurrentDate() + ".txt";

            File f = new File(path);

            FileOutputStream fileout = new FileOutputStream(f, true);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.append(data + "\n");
            outputWriter.close();
            fileout.close();

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "CANNOT CREATE LOG FILE " + e.getMessage());
        }
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static String getCurrentDateTime() {
        Date d = Utility.newDate();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss",
                Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
        return sdf.format(d);
    }

    public static String getLogFilePath(int logType) {
        String path = "";
        String logName = "";
        switch (logType) {
            case ERROR_LOG:
                logName = LOG_ERROR_NAME;
                break;
            case CANBUS_LOG:
                logName = LOG_CANBUS_NAME + "-" + Utility.getCurrentDate() + "-" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                break;
            case NOLOGIN_LOG:
                logName = LOG_NOLOGIN_NAME;
                break;
            case DRIVEREVENT_LOG:
                logName = LOG_DRIVEREVENT_NAME;
                break;
            case AUTOSYNC_LOG:
                logName = LOG_AUTOSYNC_NAME;
                break;
            case TCPCLIENT_LOG:
                logName = LOG_TCPCLIENT_NAME;
                break;
            case GPS_LOG:
                logName = LOG_GPS_NAME;
                break;
            case ELD_CONFIG:
                logName = ELD_CONFIG_NAME;
                break;
        }
        logName += LOG_EXTENSION;
        if (isExternalStorageWritable()) {
            path = Environment.getExternalStorageDirectory() + "/" + logName;
        } else {
            path = logName;
        }
        return path;
    }

    public static void writeELDConfig() {
        deleteEldConfigFile();
        try {
            String path = LogFile.getLogFilePath(LogFile.ELD_CONFIG);
            String[] configs = Utility.EldConfig.split(",");
            File file = Environment.getExternalStorageDirectory();
            String[] array = path.split("/");

            File f = new File(file, array[array.length - 1]);
            //FileOutputStream fileout = this.openFileOutput(f, Context.MODE_APPEND);
            FileOutputStream fileout = new FileOutputStream(f, true);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            for (String c : configs) {
                outputWriter.append(c + "\n");
            }

            outputWriter.close();
            fileout.close();
            Utility.loadSetting();
        } catch (Exception exe) {

        }
    }

    public static void deleteEldConfigFile() {
        try {
            String path = LogFile.getLogFilePath(LogFile.ELD_CONFIG);
            File f = new File(path);
            f.delete();
        } catch (Exception exe) {

        }

    }

    public static void sendLogFile(int time) {
        sentTime = time;
        String content = "App Version: " + Utility.ApplicationVersion + "\nCompany: " + Utility.CarrierName + "\nCompanyId: " + Utility.companyId + "\nVehicleID: " + Utility.vehicleId + "\n" + "Unit No: " + Utility.UnitNo + "\n" + "IMEI: " + Utility.IMEI;
        String title = "Log file from ELD: " + Utility.ApplicationVersion + " - " + Utility.IMEI + " - Unit No: " + Utility.UnitNo;
        new MailSync(mailPostTaskListener, true).execute(title, content);
    }


    public static void sendEld2020Data() {
        if (!Utility.isInternetOn()) {
            return;
        }
        String content = "App Version: " + Utility.ApplicationVersion + "\nCompany: " + Utility.CarrierName + "\nCompanyId: " + Utility.companyId + "\nVehicleID: " + Utility.vehicleId + "\n" + "Unit No: " + Utility.UnitNo + "\n" + "IMEI: " + Utility.IMEI;
        String title = "Hutch Connect Data file : " + Utility.ApplicationVersion + " - " + Utility.IMEI + " - Unit No: " + Utility.UnitNo;
        new MailSync().execute(title, content);
    }
}

