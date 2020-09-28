package com.hutchsystems.hutchconnect;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hutchsystems.hutchconnect.beans.GPSData;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.ConstantFlag;
import com.hutchsystems.hutchconnect.common.GPSTracker;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.UserPreferences;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.common.ZoneList;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.LoginDB;
import com.hutchsystems.hutchconnect.tasks.MailSync;
import com.telit.terminalio.TIOManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.TimeZone;

//import android.support.multidex.MultiDex;
//import com.a.a.a;
//import org.litepal.LitePal;


public class ELogApplication extends Application {
    String TAG = ELogApplication.class.getName();

    public static GPSTracker objGps = new GPSTracker();

    private static Application mApp;


    public static Application get() {
        return mApp;
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
    }

    AsyncTask mailAsyncTask;

    @Override
    public void onCreate() {
        super.onCreate();

        // catch unhandled exception
        CatchUnhandledException();

        try {

            Utility.context = getApplicationContext();
            mApp = this;
            initializeUserPreferences();

            // Initialize for hutch connect
            TIOManager.initialize(this.getApplicationContext());

            // For Enable Logs
            TIOManager.getInstance().enableTrace(true);

        } catch (Exception e) {
            LogFile.write(ELogApplication.class.getName() + "::onCreate Error: " + e.getMessage(), LogFile.APPLICATION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TAG, "::onCreate Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onTerminate() {
        //objGps.stopUsingGPS();
        Utility.showMsg("Application terminated");

        super.onTerminate();
        mApp = null;
        if (mailAsyncTask != null) {
            mailAsyncTask.cancel(true);
        }
    }

    private void initializeUserPreferences() {
        try {
            UserPreferences.setCurrentRule(1);
            UserPreferences.setTimeZone("UTC-08:00");
            UserPreferences.setStartTime("12:00 AM");

            CanMessages.OdometerReading = Utility.getPreferences("odometer", "0");
            CanMessages.EngineHours = Utility.getPreferences("engine_hours", "0");

            GPSData.OdometerSinceDriving = Utility.getPreferences("odometer_since_driving", Double.valueOf(CanMessages.OdometerReading).intValue());

            Utility.TimeZoneId = Utility.getPreferences("timezoneid", TimeZone.getDefault().getID());
            Utility.TimeZoneOffset = ZoneList.getOffset(Utility.TimeZoneId);
            Utility.TimeZoneOffsetUTC = ZoneList.getTimeZoneOffset(Utility.TimeZoneId);
            Utility.sdf.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
            Utility.packageId = Utility.getPreferences("packageid", Utility.packageId);
            Utility.Features = Utility.getPreferences("features", Utility.Features);
            ConstantFlag.OBD2FG = Utility.getPreferences("OBD2FG", false);
            Utility.InspectorModeFg = Utility.getPreferences("inspector_mode", false);
            CanMessages.protocol = Utility.getPreferences("protocol_supported", CanMessages.protocol);

            Utility.ShippingNumber = Utility.getPreferences("shipping_number", "");
            Utility.TrailerNumber = Utility.getPreferences("trailer_number", "");

            Utility.unIdentifiedDriverId = LoginDB.getUnidentifiedDriverId();
        } catch (Exception e) {
            LogFile.write(ELogApplication.class.getName() + "::initializeUserPreferences Error: " + e.getMessage(), LogFile.APPLICATION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TAG, "::initializeUserPreferences Error:  ", e.getMessage(), Utility.printStackTrace(e));
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 14 Feb 2020
    // Purpose: catch unhandled exception
    private void CatchUnhandledException()
    {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {

                String threadName = "";
                try {
                    if (thread != null) {
                        threadName = "Thread: " + thread.getName();
                    }
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String sStackTrace = sw.toString(); // stack trace as a string

                    String error = threadName + " - " + e.getMessage() + " \n " + sStackTrace;
                    Log.e("Unhandled Exception+++", error);
                    //Utility.showMsg("System Encounter Error: " + e.getMessage());
                    LogFile.write(ELogApplication.class.getName() + "::Unhandled Exception: " + error, LogFile.APPLICATION, LogFile.ERROR_LOG);

                    String title = "Crash in Eld: " + Utility.ApplicationVersion + " - " + Utility.IMEI + "- " + threadName;
                    LogDB.writeLogs(TAG, "::Unhandled Exception: ", e.getMessage(), error);
                    mailAsyncTask = new MailSync(mailPostTaskListener, false).execute(title, error);
                    Thread.sleep(3000);
                    //System.exit(0);
                } catch (Exception exe) {

                    LogFile.write(ELogApplication.class.getName() + "::Unhandled Exception: " + e.getMessage() + ", " + threadName, LogFile.APPLICATION, LogFile.ERROR_LOG);
                    LogDB.writeLogs(TAG, "::Unhandled Exception: ", e.getMessage(), Utility.printStackTrace(exe));
                }
            }
        });
    }

    MailSync.PostTaskListener<Boolean> mailPostTaskListener = new MailSync.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            System.exit(0);
        }
    };


}
