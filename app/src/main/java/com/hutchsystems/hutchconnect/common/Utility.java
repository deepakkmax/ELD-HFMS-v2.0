package com.hutchsystems.hutchconnect.common;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.usage.NetworkStatsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.beans.DTCBean;
import com.hutchsystems.hutchconnect.beans.DutyStatusBean;
import com.hutchsystems.hutchconnect.beans.GPSData;
import com.hutchsystems.hutchconnect.beans.LocationBean;
import com.hutchsystems.hutchconnect.beans.PermissionBean;
import com.hutchsystems.hutchconnect.beans.SettingsBean;
import com.hutchsystems.hutchconnect.beans.TimeZoneBean;
import com.hutchsystems.hutchconnect.beans.UserBean;
import com.hutchsystems.hutchconnect.db.DeviceBalanceDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.MySQLiteOpenHelper;
import com.hutchsystems.hutchconnect.db.UserDB;
import com.hutchsystems.hutchconnect.tasks.NearestDistanceTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

public class Utility implements ActivityCompat.OnRequestPermissionsResultCallback {

    // list of violation
    final static String[] _ViolationList = {"N/A", "3C", "6(2a)", "6(2b)", "7(1a)", "7(1b)", "12A", "12B", "13A", "13B", "13C", "14A", "14C", "16B", "16D", "25", "26", "27A",
            "27B", "29A", "37.15(1)(a)(i)", "37.15(1)(a)(ii)", "37.15(1)(b)(i)", "37.15(1)(b)(ii)", "37.15(2)(a)", "37.15(2)(b)", "63(2)(a)",
            "11 Hrs 395.3(a)(3)(i)", "Take 30 min Rest", "14 Hrs 395.3(a)(2)", "60 Hrs 395.3(b)(1)", "70 Hrs 395.3(b)(2)"};


    public static int _GridWidth = 0;
    public static int _GridHeight = 0;
    public static String LastEventDate;
    public static String _productCode = "12";
    public static SettingsBean _appSetting = new SettingsBean();
    public static AlarmSetter as;
    public static int NO_OPTIONS = 0;
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public final static String DIALOGBOX_TITLE = "Hutch Connect";
    public static final int DIALOGBOX_ICON = R.mipmap.icon; //android.R.drawable.sym_def_app_icon;
    public static final int DIALOGBOX_ERROR_ICON = R.drawable.ic_flagbar_violation_upcoming; //android.R.drawable.sym_def_app_icon;

    public static String IMEI = "", SupportIMEI = "";
    public static String errorMessage = "";
    public static Context context;
    // Context for Dashboard For Hiding Dashboard
    public static Context dashboardContext;

    public static UserBean user1 = new UserBean();
    public static UserBean user2 = new UserBean();
    public static int onScreenUserId = 0, vehicleId = 0, companyId = 0, unIdentifiedDriverId = 0,
            multiDayBasisUsed = 8, activeUserId = 0, packageId = 1, DeviceStatus = 1, VehicleStatusId = 1, CompanyStatusId = 1;
    public static boolean LatePaymentFg=false;
    //1-E-Logs, 2-DVIR, 3-IFTA, 4-TRACKING, 5-DIAGNOSTIC, 6-MESSAGING, 7-MAINTENANCE, 8-NAVIGATION, 9-TRAILER MANAGEMENT SYSTEM, 10- PUSH TO TALK, 11-SAFETY &COMPLIANCE, 12-MANAGED SERVICES,13- SCORECARD,14-TPMS
    public static String Features = "1,2";
    public static ArrayList<String> onlineUserList = new ArrayList<>();
    public static boolean malFunctionIndicatorFg, dataDiagnosticIndicatorFg;
    public static String CarrierName = "", ELDManufacturer = "", USDOT = "", UnitNo = "", VIN = "",
            TimeZoneOffsetUTC = "08", ShippingNumber = "", TrailerNumber = "", MACAddress = "", PlateNo = "", TimeZoneId, FullAddress = "", EldConfig = "", ReferralCode = "", ReferralLink = "", RechargeLink = "";
    public static int TimeZoneOffset = 0;
    public static boolean SetupFg = false;
    public static int canBusFg = 1;
    public static double StartOdometerReading = 0.0;
    public static String OdometerReadingSincePowerOn = "0", EngineHourSincePowerOn = "0", DiagnosticCode = "", FuelUsedSincePowerOn = "0";
    public static String OdometerReadingStart = "0", FuelUsedStart = "0";
    public static long engineStartDT = System.currentTimeMillis();
    public static long engineMinute = 0l;
    public static long UnidentifiedDrivingTime = 0;
    public static long UnidentifiedDrivingStartTime = 0;
    public static int DrivingTime = 0, offTime = 0;
    public static boolean bEventUnidentifiedDriverDiagnostic = false;
    public static int DataTransferDiagnosticCount = 0;
    public static int DataTransferMalfunctionCheckingNumber = 0;

    public static LocationBean currentLocation = new LocationBean();
    public static boolean motionFg = false;
    // if true means Hutch connect is connected via Bluetooth or USB
    public static boolean HutchConnectStatusFg = false;

    public static int statusFlag = 0; //0 : none, 1: PU, 2: YM
    public static int powerOnOff = -1; //0: off, 1: on

    // set it true if personal use dialog is shown
    // set it false if user close dialog or vehicle is in motion
    public static boolean PCDialogFg = false;

    public static int BatteryLevel = 100;
    public static int BatteryHealth = 1;
    private static AlertDialog alertDialog = null;

    public static ArrayList<DutyStatusBean> dutyStatusList = new ArrayList<>();
    public static final int READ_PHONE_STATE = 1, ACCESS_FINE_LOCATION = 2, INTERNET = 3, PERMISSION_ALL = 999;
    public static final String RegistrationId = "0004";
    public static final String EldIdentifier = "HELD16";

   /* public static final String RegistrationId = "14FZ";
    public static final String EldIdentifier = "ELD20A";*/
    public static String ApplicationVersion = "-";
    public static long ApplicationVersionCode = 1;
    public static boolean NightModeFg = false;
    public static boolean InspectorModeFg = false;
    public static int LogId = 0;
    public static ArrayList<DTCBean> dtcList = new ArrayList<>();
    public static boolean pgn65217Fg = false;
    public static ArrayList<String> hookedTrailers = new ArrayList<>();
    public static boolean mapLoadedFg = false;
    public static Date lastOffStatusDate = null;
    public static String DEV_EMAIL = "dev@hutchsystems.com", FMCSA_OUTPUT_FILE_EMAIL = "fmcsaeldsub@dot.gov";
    // Flag to check Webservice Connectivity
    public static boolean webServiceConnectivity = false;
    public static String _CurrentDateTime = "";

    public static boolean hasPermissions(String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }

        return true;
    }


    public static String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.RECEIVE_BOOT_COMPLETED, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS,
            Manifest.permission.DISABLE_KEYGUARD, Manifest.permission.VIBRATE, Manifest.permission.CAMERA};

    // do not run on emulator
    //, "com.android.vending.BILLING", "com.android.vending.CHECK_LICENSE"
    public static void checkAndGrantPermissions() {

        if (!hasPermissions(PERMISSIONS)) {
            ActivityCompat.requestPermissions((Activity) context, PERMISSIONS, PERMISSION_ALL);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        }
    }

    public static PermissionBean getPermissionInfo(String permission) {
        PermissionBean bean = new PermissionBean();
        bean.setName(permission);
        try {
            PermissionInfo info = context.getPackageManager().getPermissionInfo(permission, PackageManager.GET_META_DATA);
            bean.setLabel(info.loadLabel(context.getPackageManager()) + "");
            bean.setDescription(info.loadDescription(context.getPackageManager()) + "");


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return bean;
    }

    // if method returns 0: All right has given,1: missing permission in permission array,2: missing write permission,3: missing data usage permission
    public static ArrayList<PermissionBean> hasAllPermissions() {

        ArrayList<PermissionBean> missingPermission = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermission.add(getPermissionInfo(permission));
                }
            }

//            if (!Settings.System.canWrite(context)) {
//                PermissionBean bean = new PermissionBean();
//                bean.setName("WriteSystemSetting");
//                bean.setLabel("Write System Setting");
//                bean.setDescription("Allows the app to write system setting of your android device");
//                missingPermission.add(bean);
//            }

//            if (!ConstantFlag.HutchConnectFg) {
//                final AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
//                int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
//                        android.os.Process.myUid(), context.getPackageName());
//                if (mode != AppOpsManager.MODE_ALLOWED) {
//
//                    PermissionBean bean = new PermissionBean();
//                    bean.setName("DataUsagePermission");
//                    bean.setLabel("Data Usage Permission");
//                    bean.setDescription("Allows the app to get data usage of your android device");
//                    missingPermission.add(bean);
//                }
        }
        return missingPermission;
    }

    public static void IMEIGet() {
        IMEI = getPreferences("imei_no", "");
    }

    // Created By: Deepak Sharma
    // Created Date: 8 May 2020
    // Purpose: get table/phone IMEI no
    public static String IMEIDeviceGet(Context context) {
        String serialNo = "";
        try {

            if (checkGrantPermission(Manifest.permission.READ_PHONE_STATE, READ_PHONE_STATE)) {

                TelephonyManager mTelephony = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
                serialNo = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

                // if device id is null then use unique id
                if (mTelephony.getDeviceId() != null && mTelephony.getDeviceId() != "") {
                    serialNo = mTelephony.getDeviceId();
                } else {
                    serialNo = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                }
            }
        } catch (Exception exe) {

        }
        return serialNo;
    }

    public static void VersionGet(Activity activity) {
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            Utility.ApplicationVersion = pInfo.versionName;
            Utility.ApplicationVersionCode = pInfo.versionCode;
        } catch (Exception e) {
            Utility.ApplicationVersion = "";
        }

    }

    public static boolean VersionNewFg(String version) {
        boolean status = false;
        try {
            int runningVersion = Integer.parseInt(Utility.ApplicationVersion.replace(".", ""));
            int availableVersion = Integer.parseInt(version.replace(".", ""));
            if (runningVersion < availableVersion) {
                status = true;
            }
        } catch (Exception e) {
        }
        return status;
    }

    public static boolean checkGrantPermission(String permission, int responseCode) {
        try {

            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, responseCode);
                return false;
            } else {
                return true;
            }
        } catch (Exception exe) {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case READ_PHONE_STATE:

                    break;
                case ACCESS_FINE_LOCATION:
                    break;
                case PERMISSION_ALL:
                    Utility.loadSetting();
                    break;

            }
        }
    }

    public static void showMsg(String msg) {

        Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        t.show();
    }

    public static void showAlertMsg(String msg) {
        if (alertDialog != null && alertDialog.isShowing()) {
            return;
        }
        try {
            alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setCancelable(true);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setTitle(DIALOGBOX_TITLE);
            alertDialog.setIcon(DIALOGBOX_ICON);
            alertDialog.setMessage(msg);
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.cancel();
                        }
                    });
            alertDialog.show();
            //return alertDialog;
        } catch (Exception ex) {
            LogFile.write("Show Alert Msg: " + ex.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(Utility.class.getName(), "Show Alert Msg:", ex.getMessage(), Utility.printStackTrace(ex));

        }

    }


    public static void showAlertMsg(String msg, Context ctx) {
        if (alertDialog != null && alertDialog.isShowing()) {
            return;
        }
        try {
            alertDialog = new AlertDialog.Builder(ctx).create();
            alertDialog.setCancelable(true);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setTitle(DIALOGBOX_TITLE);
            alertDialog.setIcon(DIALOGBOX_ICON);
            alertDialog.setMessage(msg);
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.cancel();
                        }
                    });
            alertDialog.show();
            //return alertDialog;
        } catch (Exception ex) {
            LogFile.write("Show Alert Msg: " + ex.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(Utility.class.getName(), "Show Alert Msg:", ex.getMessage(), Utility.printStackTrace(ex));
        }

    }

    public static boolean isInternetOn() {
        try {

            NetworkInfo localNetworkInfo = ((ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();

            if (localNetworkInfo != null) {
                //if (localNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {

                return localNetworkInfo.isConnected();
                //}
            }
        } catch (Exception exe) {
            errorMessage = exe.getMessage();
        }

        //  return true;
        return false;
    }

    // Created By: Deepak Sharma
    // Created Date: 21 Nov 2016 4:31 PM
    // get current system date time and format it using User TimeZoneId into 12 hours format
    public static String getCurrentDateTime12() {
        Date d = new Date();

        SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd hh:mm:a");

        sdfLocal.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));

        String datetime = sdfLocal.format(d);
       /* if (datetime.length() > 19) {
             datetime = correctDate(datetime);
        }*/
        return datetime;
    }

    // Created By: Deepak Sharma
    // Created Date: 21 Nov 2016 4:31 PM
    // get current system date time and format it using User TimeZoneId
    public static String getCurrentDateTime() {
        Date d = new Date();

        SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        sdfLocal.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));

        String datetime = sdfLocal.format(d);
       /* if (datetime.length() > 19) {
             datetime = correctDate(datetime);
        }*/
        return datetime;
    }


    // Created By: Deepak Sharma
    // Created Date: 12 Feb 2020 5:20 PM
    // format utc datetime to  it using User TimeZoneId
    public static String convertUTCtoDriverTime(String realTime) {
        try {

            // convert real time in utc to date format
            SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyyMMddHHmmss");

            sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date datetime = sdfUTC.parse(realTime);

            SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            sdfLocal.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));

            return sdfLocal.format(datetime);
        } catch (Exception exe) {
            return realTime;
        }
    }

    public static String correctDate(String datetime) {
        try {
            String cDate = datetime.replace(":", "-").replace(" ", "-");
            String[] arrDate = cDate.split("-");
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < arrDate.length; i++) {
                int item = Integer.valueOf(arrDate[i]);
                sb.append(String.format("%02d", item)).append(i <= 1 ? "-" : (i == 3 || i == 4 ? ":" : " "));
            }
            return sb.toString();
        } catch (Exception exe) {
            Date d = new Date();

            datetime = sdf.format(d);
            return datetime;
        }
    }

    public static Date newDate12() {
        Date date = parse(Utility.getCurrentDateTime12());
        return date;
    }

    public static Date newDate() {
        Date date = parse(Utility.getCurrentDateTime());
        return date;
    }


    public static Date newDateOnly() {
        Date date = parse(Utility.getCurrentDateTime(), "yyyy-MM-dd");
        return date;
    }

    public static Date parse(String dateTime) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime);
        } catch (ParseException pe) {
            return new Date();
        }
    }

    public static Date parse(String dateTime, String format) {
        try {
            return new SimpleDateFormat(format).parse(dateTime);
        } catch (ParseException pe) {
            return new Date();
        }
    }

    static String format(Date date) {
        return sdf.format(date);
    }

    public static String format(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        //dateFormat.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
        return dateFormat.format(date);
    }

    static String format(String dateTime, String format) {

        Date date = parse(dateTime);
        return format(date, format);
    }

    public static String getTime12(String dateTime) {
        return format(dateTime, "hh:mm:a");
    }

    public static String getTime(String dateTime) {
        return format(dateTime, "HH:mm:ss");
    }

    public static String getStringTime(String dateTime) {
        return format(dateTime, "HHmmss");
    }

    public static String getTimeHHMM(String dateTime) {
        if (dateTime == null || dateTime.isEmpty())
            return "";
        return format(dateTime, "hh:mm a");
    }

    // Created By: Deepak Sharma
    // Created Date: 1 Aug 2018
    // purpose: get duration in hh:mm format between to dates
    public static String getDuration(Date fromDate, Date toDate) {

        int duration = (int) Math.round((toDate.getTime() - fromDate.getTime()) / (1000));
        String time = getTimeFromSeconds(duration);
        return time;
    }

    public static float getDiffMins(String dateTime1, String dateTime2) {
        float mins = 0f;
        try {
            Date d1 = parse(dateTime1);
            Date d2 = parse(dateTime2);

            long millis = d2.getTime() - d1.getTime();
            mins = (1.0f * millis) / (1000f * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //int mins = millis % (1000*60*60);
        return mins;
    }

    public static float getDiffTime(String dateTime1, String dateTime2) {
        float hours = 0f;
        try {
            Date d1 = parse(dateTime1);
            Date d2 = parse(dateTime2);

            long millis = d2.getTime() - d1.getTime();
            hours = (1.0f * millis) / (1000f * 60 * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //int mins = millis % (1000*60*60);
        return hours;
    }

    public static int getDiffDay(String dateTime1, String dateTime2) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        //float hours = 0f;
        float days = 0;
        try {
            Date d1 = sf.parse(dateTime1);
            Date d2 = sf.parse(dateTime2);

            long millis = d2.getTime() - d1.getTime();
            days = (1.0f * millis) / (1000f * 60 * 60 * 24);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //int mins = millis % (1000*60*60);
        return (int) days;
    }


    public static long getDiffDay(Date d1, Date d2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            Date datetime1 = sdf.parse(sdf.format(d1));
            Date datetime2 = sdf.parse(sdf.format(d2));
            long diff = (datetime1.getTime() - datetime2.getTime()) / (24 * 3600 * 1000);
            return diff;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getTimeByFormat(String dateTime) {
        String str = "";
        try {
            String format = "hh:mm:ss a"; //12hr
            if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                format = "HH:mm:ss";
            }
            str = format(dateTime, format);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getAppCurrentDateTime() {
        String DateTime = "";
        try {
            String format = CustomDateFormat.d6;
            if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                format += " HH:mm:ss";
            } else {
                format += " hh:mm:ss a"; //12hr
            }

            SimpleDateFormat sf = new SimpleDateFormat(format);
            sf.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
            DateTime = sf.format(new Date());
        } catch (Exception exe) {

        }
        return DateTime;
    }

    public static String getDateTimeForServer(String date) {
        String str = "";
        try {
            Date d = sdf.parse(date);

            int offset = Utility.TimeZoneOffset;

            String gmtTZ = String.format("%s%02d%02d", offset < 0 ? "-" : "+",
                    Math.abs(offset) / 3600000, Math.abs(offset) / 60000 % 60);
            str = "/Date(" + d.getTime() + "" + gmtTZ + ")/";
            System.out.println(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getDateForServer(String date) {

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        String str = "";
        try {

            sdfDate.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
            Date d = sdfDate.parse(date);
            int offset = Utility.TimeZoneOffset;

            String gmtTZ = String.format("%s%02d%02d", offset < 0 ? "-" : "+",
                    Math.abs(offset) / 3600000, Math.abs(offset) / 60000 % 60);
            str = "/Date(" + d.getTime() + "" + gmtTZ + ")/";
            System.out.println(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getCurrentUTCDate() {

        SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd");
        sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdfUTC.format(new Date());

        return utcTime;
    }

    public static String getCurrentUTCDateTime() {

        SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdfUTC.format(new Date());

        return utcTime;
    }

    public static String getCurrentPSTDateTime() {

        SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfUTC.setTimeZone(TimeZone.getTimeZone("PST"));
        final String utcTime = sdfUTC.format(new Date());

        return utcTime;
    }

    public static String convertUTCToLocalDateTime(String date) {

        try {
            SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdfLocal.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date utcDate = sdfLocal.parse(date);

            final String utcTime = sdf.format(utcDate);
            return utcTime;
        } catch (Exception e) {
        }
        return date;
    }

    public static String getStringCurrentDate() {
        String dateTime = getCurrentDateTime();
        return format(dateTime, "MMM dd,yyyy");

    }

    public static String getStringCurrentDate(String date) {
        return format(date, "MMM dd,yyyy");

    }

    public static String getCurrentDate() {
        String dateTime = getCurrentDateTime();
        return format(dateTime, "yyyy-MM-dd");
    }

    public static String getCurrentTime() {
        String dateTime = getCurrentDateTime();
        return format(dateTime, "HH:mm:ss");
    }

    public static String getCurrentTime12() {
        String dateTime = getCurrentDateTime();
        return format(dateTime, "hh:mm a");
    }

    public static String getDate(String dateTime) {
        if (dateTime == null || dateTime.isEmpty())
            return "";
        return format(dateTime, "yyyy-MM-dd");
    }

    public static Date getDate(Date date, int numGapDays) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(sdf.format(date)));
            c.add(Calendar.DATE, numGapDays);
            c.setTime(sdf.parse(sdf.format(c.getTime())));
            return c.getTime();
        } catch (Exception e) {

        }
        return null;
    }


    public static String getDateFromString(String date, int numGapDays) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(date));
            c.add(Calendar.DATE, numGapDays);
            c.setTime(sdf.parse(sdf.format(c.getTime())));
            return sdf.format(c.getTime());
        } catch (Exception e) {

        }
        return null;
    }

    // Created By: Deepak Sharma
    // Created Date: 18 July 2016
    // get date time after adding gap of given days
    public static String getDateTime(String date, int numGapDays) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(sdfDate.parse(date));
            c.add(Calendar.DATE, numGapDays);
            return sdf.format(c.getTime());
        } catch (Exception e) {

        }
        return null;
    }

    public static String addDate(String date, int timeFrequency) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(date));
            c.add(Calendar.DATE, timeFrequency);
            String duedate = sdf.format(c.getTime());
            return duedate;
        } catch (Exception e) {
        }
        return null;
    }

    public static Date addDays(Date date, int timeFrequency) {

        try {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DATE, timeFrequency);
            return c.getTime();
        } catch (Exception e) {
        }
        return null;
    }

    public static String getPreviousDate(int timeFrequency) {

        try {
            Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
            c.add(Calendar.DATE, timeFrequency);
            return sdf.format(c.getTime());
        } catch (Exception e) {
            return getCurrentDateTime();
        }
    }


    public static String getPreviousDateOnly(int timeFrequency) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
            Calendar c = Calendar.getInstance();
            //c.setTime(Utility.newDate());
            //  c.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
            c.add(Calendar.DATE, timeFrequency);
            return dateFormat.format(c.getTime());
        } catch (Exception e) {
            return getCurrentDateTime();
        }
    }


    public static Date dateOnlyGet(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
            c.setTime(sdf.parse(sdf.format(date)));
            return c.getTime();
        } catch (Exception e) {
        }
        return null;
    }

    public static Date dateOnlyGet(String date) {
        return parse(date, "yyyy-MM-dd");
    }

    public static String dateOnlyStringGet(String date) {
        return format(date, "yyyy-MM-dd");
    }

    public static String parseDate(String date) {
        return format(date, "yyyy-MM-dd");
    }

    public static String timeOnlyGet(String date) {
        try {
            return date.substring(11, 16);
        } catch (Exception e) {
            String message = e.getMessage();
        }
        return date;
    }

    public static Date addMinutes(Date date, int timeFrequency) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.MINUTE, timeFrequency);
            return c.getTime();
        } catch (Exception e) {
        }
        return null;
    }

    public static Date addSeconds(Date date, int timeFrequency) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.SECOND, timeFrequency);
            return c.getTime();
        } catch (Exception e) {
        }
        return null;
    }

    public static String ConverDateFormat(Date date) {
        return format(date, "MMM dd,yyyy hh:mm a");
    }

    public static String ConverDateFormat(String date) {
        return format(date, "MMM dd,yyyy hh:mm a");
    }

    public static String getTimeFromMinute(int totalMinutes) {
        String hours = Integer.toString(totalMinutes / 60);
        hours = hours.length() == 1 ? "0" + hours : hours;
        String minutes = Integer.toString(totalMinutes % 60);
        minutes = minutes.length() == 1 ? "0" + minutes : minutes;
        return hours + ":" + minutes;
    }

    public static String getTimeFromSeconds(int totalSeconds) {
        String hours = Integer.toString(totalSeconds / 3600);
        hours = hours.length() == 1 ? "0" + hours : hours;
        String minutes = Integer.toString((totalSeconds % 3600) / 60);
        minutes = minutes.length() == 1 ? "0" + minutes : minutes;

        String seconds = Integer.toString(totalSeconds % 60);
        seconds = seconds.length() == 1 ? "0" + seconds : seconds;
        return hours + ":" + minutes + ":" + seconds;
    }

    public static String getTimeFromSecondsInMin(int totalSeconds) {
        String hours = Integer.toString(totalSeconds / 3600);
        hours = hours.length() == 1 ? "0" + hours : hours;

        String minutes = Integer.toString((totalSeconds % 3600) / 60);
        minutes = minutes.length() == 1 ? "0" + minutes : minutes;

        return hours + ":" + minutes;
    }

    public static String getTimeInMinuteFromSeconds(int totalSeconds) {

        String minutes = Integer.toString(totalSeconds / 60);
        minutes = minutes.length() == 1 ? "0" + minutes : minutes;

        String seconds = Integer.toString(totalSeconds % 60);
        seconds = seconds.length() == 1 ? "0" + seconds : seconds;
        return minutes + ":" + seconds;
    }

    public static boolean isAlpha(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!((c >= 'a' && c <= 'z') || c == ' ')) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmail(String str) {
        if (str.indexOf("@") > 0 && str.indexOf(".") > 0) {
            return true;
        }
        return false;
    }

    public static boolean isNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!(c >= '0' && c <= '9')) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasSpace(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ' ') {
                return true;
            }
        }
        return false;
    }

    public static void printError(String message) {
        if (message == null) {
            message = "An unexpected error occurred. Please contact admin!";
        }
        errorMessage = message;
        System.out.println("errorLog: " + message);
    }

    // Created By: Deepak Sharma
    // Created Date: 22 April 2015
    // Purpose: show error message
    public static AlertDialog showErrorMessage(Context ctx) {

        final AlertDialog ad = new AlertDialog.Builder(ctx).create();
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle(DIALOGBOX_TITLE);
        ad.setIcon(DIALOGBOX_ICON);
        ad.setMessage(errorMessage);
        ad.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ad.cancel();
                    }
                });
        ad.show();
        return ad;

    }

    // Created By : Pallavi Wattamwar
    // Created Date: 20 May 2019
    // Purpose: show error message
    public static AlertDialog showErrorMessage(Context ctx, String msg) {

        final AlertDialog ad = new AlertDialog.Builder(ctx).create();
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle(DIALOGBOX_TITLE);
        ad.setIcon(DIALOGBOX_ERROR_ICON);
        ad.setMessage(msg);
        ad.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ad.cancel();
                    }
                });
        ad.show();
        return ad;

    }

    public static String ConvertFromJsonDateTime(String date) {

        String splitChar = date.contains("-") ? "-" : "+";
        Calendar calendar = Calendar.getInstance();
        String datereip = date.replace("/Date(", "").replace(")/", "").split(splitChar)[0];
        Long timeInMillis = Long.valueOf(datereip);
        calendar.setTimeInMillis(timeInMillis);

        SimpleDateFormat sdfPacific = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfPacific.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        String pstTime = sdfPacific.format(calendar.getTime());
        return pstTime;
    }

    public static String ConvertFromJsonDate(String date) {

        String splitChar = date.contains("-") ? "-" : "+";
        Calendar calendar = Calendar.getInstance();
        String datereip = date.replace("/Date(", "").replace(")/", "").split(splitChar)[0];
        Long timeInMillis = Long.valueOf(datereip);
        calendar.setTimeInMillis(timeInMillis);

        SimpleDateFormat sdfPacific = new SimpleDateFormat("yyyy-MM-dd");
        sdfPacific.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        String pstTime = sdfPacific.format(calendar.getTime());
        return pstTime;
    }

    private static String convertToHex(byte[] data) throws java.io.IOException {


        StringBuffer sb = new StringBuffer();
        String hex = null;

        hex = Base64.encodeToString(data, 0, data.length, NO_OPTIONS);

        sb.append(hex);

        return sb.toString();
    }


    // Created By: Deepak Sharma
    // Created Date: 21 July 2016
    // Purpose: convert binary to hex
    public static String convertBinaryToHex(String val) {
        int decimal = Integer.parseInt(val, 2);
        return Integer.toString(decimal, 16).toUpperCase();
    }

    // Created By: Deepak Sharma
    // Created Date: 18 March 2020
    // Purpose: convert file to bytes
    public static byte[] convertToByte(File file) throws Exception {
        int size = (int) file.length();
        byte[] bytes = new byte[size];

        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
        buf.read(bytes, 0, bytes.length);
        buf.close();
        return bytes;
    }

    public static String computeSHAHash(String password, String salt) {
        password = password.concat(salt);
        byte[] key = password.getBytes();
        MessageDigest mdSha1 = null;
        try {
            mdSha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e1) {

        }
        String result = "";
        byte[] hash = mdSha1.digest(key);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(hash[i]);
            if (hex.length() == 1) hex = "0" + hex;
            hex = hex.substring(hex.length() - 2);
            result += hex;
        }

        return result.toUpperCase();
    }

    public static LocationBean getLocation() {
        LocationBean bean = new LocationBean();

        return bean;
    }

    public static String GetStringDate(Date date) {
        return format(date, "MMM dd,yyyy");
    }

    // Created By: Deepak Sharma
    // Created Date: 12 September 2016
    // Purpose: convert to specified date format
    public static String convertDate(Date date, String f) {
        return format(date, f);
    }


    // Created By: Deepak Sharma
    // Created Date: 12 September 2016
    // Purpose: convert to specified date format
    public static String convertDate(String date, String f) {
        return format(date, f);
    }

    public static String GetString(Date date) {

        return format(date, "yyyy-MM-dd");
    }

    public static float gigabytesAvailable(File f) {
        StatFs stat = new StatFs(f.getPath());
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        return bytesAvailable / (1024.f * 1024.f * 1024.f);
    }

    public static long megabytesAvailable(File f) {
        StatFs stat = new StatFs(f.getPath());
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        return bytesAvailable / (1024 * 1024);
    }

    // save the diagnostic time of last diagnostic event occurrence
    public static void saveDiagnosticTime(boolean diagnosticFg) {
        SharedPreferences.Editor e = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE)).edit();
        e.putBoolean("diagnostic_engine_error", diagnosticFg);
        e.putLong("diagnostic_time", CanMessages.diagnosticEngineSynchronizationTime);
        e.commit();
    }

    // save odometer reading and engine hours
    public static void saveVehicleCanInfo() {
        SharedPreferences.Editor e = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE)).edit();
        e.putString("odometer", CanMessages.OdometerReading);
        e.putString("engine_hours", CanMessages.EngineHours);
        e.commit();
    }

    public static void saveAlerts(String vName, boolean vValue, String vTimeName, long vTimeValue) {
        SharedPreferences.Editor e = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE)).edit();
        e.putBoolean(vName, vValue);
        e.putLong(vTimeName, vTimeValue);
        e.commit();
    }

    public static void savePreferences(String parm, boolean value) {
        SharedPreferences.Editor e = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE)).edit();
        e.putBoolean(parm, value);
        e.commit();
    }

    public static void savePreferences(String parm, float value) {
        SharedPreferences.Editor e = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE)).edit();
        e.putFloat(parm, value);
        e.commit();
    }

    public static void savePreferences(String parm, String value) {
        SharedPreferences.Editor e = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE)).edit();
        e.putString(parm, value);
        e.commit();
    }

    public static void savePreferences(String parm, long value) {
        SharedPreferences.Editor e = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE)).edit();
        e.putLong(parm, value);
        e.commit();
    }

    public static void savePreferences(String parm, int value) {
        SharedPreferences.Editor e = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE)).edit();
        e.putInt(parm, value);
        e.commit();
    }

    public static boolean getPreferences(String parm, boolean defaultValue) {
        SharedPreferences sp = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE));
        return sp.getBoolean(parm, defaultValue);
    }

    public static long getPreferences(String parm, long defaultValue) {
        SharedPreferences sp = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE));
        return sp.getLong(parm, defaultValue);
    }

    public static int getPreferences(String parm, int defaultValue) {
        SharedPreferences sp = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE));
        return sp.getInt(parm, defaultValue);
    }

    public static float getPreferences(String parm, float defaultValue) {
        SharedPreferences sp = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE));
        return sp.getFloat(parm, defaultValue);
    }

    public static String getPreferences(String parm, String defaultValue) {
        SharedPreferences sp = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE));
        return sp.getString(parm, defaultValue);
    }

    // save odometer reading and engine hours
    public static void saveLoginInfo(int driverId, int coDriverId, int activeUserId, int onScreenUserId) {
        SharedPreferences.Editor e = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE)).edit();
        e.putInt("driverid", driverId);
        e.putInt("codriverid", coDriverId);
        e.putInt("activeuserid", activeUserId);
        e.putInt("onscreenuserid", onScreenUserId);
        e.commit();
    }

    public static boolean getvAlert(String name) {
        SharedPreferences sp = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE));
        return sp.getBoolean(name, false);
    }

    public static long gettAlert(String name) {
        SharedPreferences sp = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE));
        return sp.getLong(name, 0);
    }

    public static void hideKeyboard(Activity activity, View view) {
        try {
            if (view == null)
                return;

            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception exe) {

        }
    }

    public static boolean isLargeScreen(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    // Created By: Deepak Sharma
    // Created Date: 17 June 2016
    // Purpose: populate Time Zone
    public static ArrayList<TimeZoneBean> populateTimeZone() {
        return ZoneList.getZones();
    }

    @Deprecated
    // Created By: Deepak Sharma
    // Created Date: 17 June 2016
    // Purpose: populate Time Zone
    public static ArrayList<TimeZoneBean> populateTimeZone1() {

        ArrayList<TimeZoneBean> list = new ArrayList<>();
        TimeZoneBean bean = new TimeZoneBean();
        bean.setTimeZoneName("Hawaii Time");
        bean.setTimeZoneValue("UTC-10:00");
        bean.setTimeZoneOffset(-10f);
        list.add(bean);

        bean = new TimeZoneBean();
        bean.setTimeZoneName("Alaska Time");
        bean.setTimeZoneValue("UTC-09:00");
        bean.setTimeZoneOffset(-9f);
        list.add(bean);

        bean = new TimeZoneBean();
        bean.setTimeZoneName("Pacific Time");
        bean.setTimeZoneValue("UTC-08:00");
        bean.setTimeZoneOffset(-8f);
        list.add(bean);

        bean = new TimeZoneBean();
        bean.setTimeZoneName("Mountain Time");
        bean.setTimeZoneValue("UTC-07:00");
        bean.setTimeZoneOffset(-7f);
        list.add(bean);

        bean = new TimeZoneBean();
        bean.setTimeZoneName("Central Time");
        bean.setTimeZoneValue("UTC-06:00");
        bean.setTimeZoneOffset(-6f);
        list.add(bean);

        bean = new TimeZoneBean();
        bean.setTimeZoneName("Eastern Time");
        bean.setTimeZoneValue("UTC-05:00");
        bean.setTimeZoneOffset(-5f);
        list.add(bean);


        bean = new TimeZoneBean();
        bean.setTimeZoneName("Alantic Time");
        bean.setTimeZoneValue("UTC-04:00");
        bean.setTimeZoneOffset(-4f);
        list.add(bean);

        bean = new TimeZoneBean();
        bean.setTimeZoneName("West Greenland Time");
        bean.setTimeZoneValue("UTC-03:30");
        bean.setTimeZoneOffset(-3.5f);
        list.add(bean);

        bean = new TimeZoneBean();
        bean.setTimeZoneName("Saint Pierre and Miquelon Time");
        bean.setTimeZoneValue("UTC-03:00");
        bean.setTimeZoneOffset(-3f);
        list.add(bean);

        bean = new TimeZoneBean();
        bean.setTimeZoneName("East Greenland Time");
        bean.setTimeZoneValue("UTC-01:00");
        bean.setTimeZoneOffset(-1f);
        list.add(bean);

        return list;
    }

    // Created By: Deepak Sharma
    // Created Date: 30 June 2016
    // Purpose: calcualte authenticationvalue
    public static String getAuthenticationValue() {
        String authValue = "";
        String vin = Utility.VIN;
        int length = vin.length();

        // calculate checksum
        int checksum = 0;
        // sum of all numeric character of vin number
        for (int i = 0; i < length; i++) {
            Character character = vin.charAt(i);
            if (Character.isDigit(character)) {
                checksum += Character.getNumericValue(character);
            }
        }

        // five consective circular shift left
        checksum = (checksum << 5);

        // xor with number 150 as per document
        checksum = checksum ^ 168;

        // get lower 8-bit byte
        checksum = checksum & 0xFF;

        // Concatenate Vin no and checksum
        authValue = vin + checksum;

        return authValue;
    }

    public static boolean specialCategoryChanged() {
        //check the specialCategory
        boolean result = false;
        int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        String specialCategory = UserDB.getSpecialCategory(driverId);
        Log.i("Special", "specialCategory=" + specialCategory);
        UserBean currentUser = Utility.user1.isOnScreenFg() ? Utility.user1 : Utility.user2;
        Log.i("Special", "currentUser specialCategory=" + currentUser.getSpecialCategory());
        if (specialCategory != "" && currentUser != null && !specialCategory.equals(currentUser.getSpecialCategory())) {
            result = true;
        }
        return result;
    }

    public static String GetDBBackupName() {
        return "ELDDBBackup_" + IMEI + "_" + format(Utility.newDate(), CustomDateFormat.dt7) + ".db";
    }

    public static String GetDocumentName(String type) {
        File folder = new File(Environment.getExternalStorageDirectory().toString()
                + "/" + type);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        return Environment.getExternalStorageDirectory().toString()
                + "/" + type + "/DOC-" + type + "-" + Utility.vehicleId + "-"
                + format(Utility.newDate(), CustomDateFormat.dt7) // time stamp
                + ".pdf";
    }


    public static String GetDocumentName(String type, String name) {
        File folder = new File(Environment.getExternalStorageDirectory().toString()
                + "/" + type);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        return Environment.getExternalStorageDirectory().toString()
                + "/" + type + "/" + name;
    }


    public static String GetDocumentFileName(String type, String value) {

        return "/DOC-" + type + "-" + value + "-"
                + format(Utility.newDate(), CustomDateFormat.dt7) // time stamp
                + ".pdf";
    }

    public static String GetSignaturePath() {
        File folder = new File(Environment.getExternalStorageDirectory().toString()
                + "/" + DocumentType.DOCUMENT_SIGNATURE);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        return Environment.getExternalStorageDirectory().toString()
                + "/" + DocumentType.DOCUMENT_SIGNATURE + "/signature-" + Utility.onScreenUserId + ".PNG";
    }

    public static String GetDocumentFullPath(String type, String fileName) {

        return Environment.getExternalStorageDirectory().toString() + "/" + type + "/" + fileName;
    }

    public static String GetDocFullPath(String type) {

        return Environment.getExternalStorageDirectory().toString() + "/" + type + "/";
    }

    public static void DeleteFile(String type, String fileName) {
        try {
            String fullPath = GetDocumentFullPath(type, fileName);
            File file = new File(fullPath);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception exe) {

        }
    }

    public static String getDocumentType(int type) {
        if (type == 0) {
            return DocumentType.DOCUMENT_NOTICE_ORDER;
        } else if (type == 1) {
            return DocumentType.DOCUMENT_CVSA_INSPECTION;
        } else if (type == 2) {
            return DocumentType.DOCUMENT_VIOLATION_TICKET;
        } else {
            return "";
        }
    }

    public static boolean isEmptyString(String text) {
        return (text == null || text.trim().equals("null") || text.trim()
                .length() <= 0);
    }

    public static boolean VehicleInfo() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            database.delete(MySQLiteOpenHelper.TABLE_VEHICLE_INFO,
                    "SyncFg=0", null);

            status = true;

        } catch (Exception e) {

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add or update dailylog in database
    public static boolean DeleteOldData() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String date = Utility.getPreviousDateOnly(-30);

            database.delete(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT,
                    "SyncFg=1 and EventDateTime<?", new String[]{date});

            database.delete(MySQLiteOpenHelper.TABLE_DAILYLOG,
                    "SyncFg=1 and LogDate<?", new String[]{date});
            date = Utility.getPreviousDateOnly(-2);
            database.delete(MySQLiteOpenHelper.TABLE_ALERT,
                    "SyncFg=1 and AlertDateTime<?", new String[]{date});

            database.delete(MySQLiteOpenHelper.TABLE_GpsLocation,
                    "SyncFg=1 and GPSDate<?", new String[]{date});

            database.delete(MySQLiteOpenHelper.TABLE_VEHICLE_INFO,
                    "SyncFg=1 and CreatedDate<?", new String[]{date});

            database.delete(MySQLiteOpenHelper.TABLE_GEOFENCE_MONITOR,
                    "SyncFg=1 and regionInFg=0 and enterDate<?", new String[]{date});

            date = Utility.getPreviousDateOnly(-15);
            database.delete(MySQLiteOpenHelper.TABLE_FUEL_DETAIL,
                    "SyncFg=1 and FuelDateTime<?", new String[]{date});

            database.delete(MySQLiteOpenHelper.TABLE_INCIDENT_DETAIL,
                    "SyncFg=1 and IncidentDate<?", new String[]{date});

            database.delete(MySQLiteOpenHelper.TABLE_Message,
                    "SyncFg=1 and MessageDate<?", new String[]{date});

            date = Utility.getPreviousDateOnly(-2);
            database.delete(MySQLiteOpenHelper.TABLE_TPMS,
                    "SyncFg=1 and CreatedDate<?", new String[]{date});

            date = Utility.getPreviousDateOnly(-15);
            database.delete(MySQLiteOpenHelper.TABLE_TRIP_INSPECTION,
                    "SyncFg=1 and DateTime<?", new String[]{date});

            database.delete(MySQLiteOpenHelper.TABLE_VIOLATION_DETAIL,
                    "SyncFg=1 and ViolationDate<?", new String[]{date});

            database.delete(MySQLiteOpenHelper.TABLE_TICKET_DETAIL,
                    "TicketStatus in (5,7) and TicketDate<?", new String[]{date});


            database.delete(MySQLiteOpenHelper.TABLE_DEVICE_BALANCE_TRANSACTION,
                    "SyncFg=1 and DateTime<?", new String[]{date});

            status = true;

        } catch (Exception e) {

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return status;
    }

    public static String getBatteryHealth() {
        String health = "UNKNOWN";
        switch (BatteryHealth) {
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                health = "UNKNOWN";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                health = "GOOD";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                health = "OVERHEAT";
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                health = "DEAD";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                health = "VOLTAGE";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                health = "FAILURE";
                break;
            case BatteryManager.BATTERY_HEALTH_COLD:
                health = "COLD";
                break;
        }
        return health;
    }

    public static String[] violationInfoGet(String rule) {
        String vTitle = "";
        String vDetail = "";
        int ruleId = 0;
        switch (rule) {
            case "3C":
                vTitle = "Must not drive beyond 13 hours without taking 8 consecutive hours off-duty";
                vDetail = "According to Canadian law the aggregate of the driving time immediately preceding and immediately following the resting time in the sleeper berth does not exceed 13 hours in total.";
                ruleId = 5;
                break;
            case "6(2a)":
                vTitle = "Must not drive beyond 13 hours without taking 8 consecutive hours off-duty";
                vDetail = "Canadian law permits you to only drive 13 hours after taking 8 consecutive hours off-duty. You are required to take another 8 consecutive hours off before driving is permitted.";
                ruleId = 5;
                break;
            case "6(2b)":
                vTitle = "Must not drive after 15 hours of on-duty time unless 8 consecutive hours of off-duty time is taken.";
                vDetail = "Canadian law permits you to only be on-duty for 15 hours after taking 8 consecutive hours off-duty. You will be required to take another 8 consecutive hours off before driving is permitted.";
                ruleId = 5;
                break;
            case "7(1a)":
                vTitle = "Must not drive beyond 4 hours without taking 10 consecutive minutes of off duty or non-driving time.";
                vDetail = "Canadian law permits you to only drive 4 hours after taking 10 consecutive minutes of off-duty or non-driving time. You are required to take another 10 consecutive minutes off before driving is permitted.";
                ruleId = 5;
                break;
            case "7(1b)":
                vTitle = "Must not drive beyond 6 hours without taking 30 consecutive minutes of off duty or non-driving time.";
                vDetail = "Canadian law permits you to only drive 6 hours after taking 30 consecutive minutes of off-duty or non-driving time. You are required to take another 30 consecutive minutes off before driving is permitted.";
                ruleId = 5;
                break;
            case "12A":
                vTitle = context.getString(R.string.A_12_title);
                vDetail = context.getString(R.string.A_12_warning);
                ruleId = 1;
                break;
            case "12B":
                vTitle = context.getString(R.string.B_12_title);
                vDetail = context.getString(R.string.B_12_message);
                ruleId = 1;
                break;
            case "13A":
                vTitle = context.getString(R.string.A_13_title);
                vDetail = context.getString(R.string.A_13_message);
                ruleId = 1;
                break;
            case "13B":
                vTitle = context.getString(R.string.B_13_title);
                vDetail = context.getString(R.string.B_13_message);
                ruleId = 1;
                break;
            case "13C":
                vTitle = context.getString(R.string.C_13_title);
                vDetail = context.getString(R.string.C_13_message);
                ruleId = 1;
                break;
            case "14A":
                vTitle = context.getString(R.string.A_14_title);
                vDetail = context.getString(R.string.A_14_message);
                ruleId = 1;
                break;
            case "14C":
                vTitle = context.getString(R.string.C_14_title);
                vDetail = context.getString(R.string.C_14_message);
                ruleId = 1;
                break;
            case "16B":
                vTitle = context.getString(R.string.B_16_title);
                vDetail = context.getString(R.string.B_16_message);
                ruleId = 1;
                break;
            case "16D":
                vTitle = context.getString(R.string.D_16_title);
                vDetail = context.getString(R.string.D_16_message);
                ruleId = 1;
                break;
            case "25":
                vTitle = context.getString(R.string.V_25_title);
                vDetail = context.getString(R.string.V_25_message);
                ruleId = 1;
                break;
            case "26":
                vTitle = context.getString(R.string.V_26_title);
                vDetail = context.getString(R.string.V_26_message);
                ruleId = 1;
                break;
            case "27A":
                vTitle = context.getString(R.string.A_27_title);
                vDetail = context.getString(R.string.A_27_message);
                ruleId = 2;
                break;
            case "27B":
                vTitle = context.getString(R.string.B_27_title);
                vDetail = context.getString(R.string.B_27_message);
                ruleId = 2;
                break;
            case "29A":
                vTitle = context.getString(R.string.A_29_title);
                vDetail = context.getString(R.string.A_29_message);
                ruleId = 1;
                break;

            case "37.15(1)(a)(i)":
                vTitle = "Must not drive beyond 13 hours without taking 9 consecutive hours off-duty";
                vDetail = "Canadian law permits you to only drive 13 hours after taking 9 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You are required to take another 9 consecutive hours off before driving is permitted.";

                ruleId = 6;
                break;
            case "37.15(1)(a)(ii)":
                vTitle = "Must not drive after 15 hours of on-duty time unless 9 consecutive hours of off-duty time is taken.";
                vDetail = "Canadian law permits you to only be on-duty for 15 hours after taking 9 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You will be required to take another 9 consecutive hours off before driving is permitted.";

                ruleId = 6;
                break;
            case "37.15(1)(b)(i)":
                vTitle = "Must not drive beyond 15 hours of elapsed time after last 9 consecutive hours off-duty";
                vDetail = "Canadian law permits you to only have 15 hours of elapsed time after taking 9 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You are required to take another 9 consecutive hours off before driving is permitted. Drivers normally refer to this as their work-shift.";

                ruleId = 6;
                break;
            case "37.15(1)(b)(ii)":
                vTitle = "Must not drive unless 24 consecutive off-duty hours are taken in every period of 7 days.";
                vDetail = "Canadian law requires a minimum 24 consecutive off-duty hours or sleeper-berth hours or any combination of off-duty and sleeper-berth to be taken every 7 days.";

                ruleId = 6;
                break;

            case "37.15(2)(a)":
                vTitle = "Must not drive after 65 hours of driving time in 7  consecutive days.";
                vDetail = "Canadian law does not permit you to drive after 65 hours of driving time in any period of 7 consecutive days. You can wait until the 8th day to gain hours back.";

                ruleId = 6;
                break;
            case "37.15(2)(b)":
                vTitle = "Must not drive after 80 hours of on-duty time in 7  consecutive days.";
                vDetail = "Canadian law does not permit you to drive after 80 hours of on-duty time in any period of 7 consecutive days. You can wait until the 8th day to gain hours back.";

                ruleId = 6;
                break;

            case "63(2)(a)":
                vTitle = "Must not drive unless at least 3 period of off-duty time each at least 24 hours long are taken in any period of 24 days.";
                vDetail = "Canadian law requires at least 3 period of consecutive off-duty hours  each at least 24 hours long or sleeper-berth hours or any combination of off-duty and sleeper-berth to be taken in any period of 24 days.";

                ruleId = 7;
                break;


            case "11 Hrs 395.3(a)(3)(i)":
            case "11 Hrs 395.3(a)(3)(i) ***EGREGIOUS***":
                vTitle = context.getString(R.string.US_395_3_a_3_i_title);
                vDetail = context.getString(R.string.US_395_3_a_3_i_message);
                ruleId = 3;
                break;
            case "Take 30 min Rest":
                vTitle = context.getString(R.string.US_30min_rest_title);
                vDetail = context.getString(R.string.US_30min_rest_message);
                ruleId = 3;
                break;
            case "14 Hrs 395.3(a)(2)":
                vTitle = context.getString(R.string.US_395_3_a_2_title);
                vDetail = context.getString(R.string.US_395_3_a_2_message);
                ruleId = 3;
                break;
            case "60 Hrs 395.3(b)(1)":
                vTitle = "Must not drive after 60 hours on-duty within 7 days.";
                vDetail = "US law does not permit you to drive after 60 hours on-duty time in any 7 days period. You can take 34 hours off-duty or sleeper-berth or any such combination to reset or you can wait until the 9th day to gain the hours back.";

                ruleId = 4;
                break;
            case "70 Hrs 395.3(b)(2)":
                vTitle = context.getString(R.string.US_70hr_title);
                vDetail = context.getString(R.string.US_70hr_message);

                ruleId = 3;
                break;
        }
        return new String[]{vTitle, vDetail};
    }

    public static long TotalDeviceBytes = 0;

    public static long getTotalDeviceBytes(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                NetworkStatsManager networkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
                NetworkStatsHelper helper = new NetworkStatsHelper(networkStatsManager, activity);
                long totalDeviceBytes = helper.getAllBytesMobile(context);
                return totalDeviceBytes;
            } catch (Exception exe) {
            }
        }
        return 0;
    }

    public static boolean hasFeature(String featureId) {
        boolean status = false;

        String[] obj = Features.split(",");
        for (String f : obj) {
            if (f.equals(featureId)) {
                status = true;
                break;
            }
        }
        return status;
    }

    public static String printStackTrace(Exception exe) {
        String sStackTrace = "";
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exe.printStackTrace(pw);
            sStackTrace = sw.toString();
        } catch (Exception e) {
        }
        return sStackTrace;
    }


    // Created By: Deepak Sharma
    // Created Date: 13 September 2017
    // Purpose: load settings on the fly
    public static void loadSetting() {
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            //Get the text file
            File file = new File(sdcard, "eldconfig.txt");

            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    String[] settings = line.split(":");

                    String setting = settings[0].toUpperCase().trim();
                    String value = settings[1].toUpperCase().trim();

                    switch (setting) {
                       /* case "PROTOCOL":
                            CanMessages.protocol = value;
                            Utility.savePreferences("protocol_supported", CanMessages.protocol);
                            break;
                        case "ODOMETERSOURCE":
                            CanMessages.odometerSource = Integer.valueOf(value);
                            Utility.savePreferences("odometer_source", CanMessages.odometerSource);
                            break;*/
                        case "CANBUS":
                            ConstantFlag.Flag_Log_CanBus = (value.equals("1"));
                            break;
                       /* case "LIVE":
                            ConstantFlag.LiveFg = (value.equals("1"));
                            break;*/
                        case "AUTOSTART":
                            ConstantFlag.AUTOSTART_MODE = (value.equals("1"));
                            break;
                        case "AUTODRIVING":
                            ConstantFlag.Flag_Auto_Driving_Event = (value.equals("1"));
                            break;
                        case "EDITDRIVING":
                            ConstantFlag.Flag_DRIVING_EDIT = (value.equals("1"));
                            break;
                        case "GAUGESHOW":
                            ConstantFlag.FLAG_GAUGE_SHOW = (value.equals("1"));
                            break;
                        case "DEVELOPMENTMODE":
                            ConstantFlag.Flag_Development = (value.equals("1"));
                            break;
                        case "GPS":
                            ConstantFlag.Flag_Log_GPS = (value.equals("1"));
                            break;
                        case "TCPCLIENT":
                            ConstantFlag.Flag_Log_TCPClient = (value.equals("1"));
                            break;
                        case "AUTOSYNCTASK":
                            ConstantFlag.Flag_Log_AutoSyncTask = (value.equals("1"));
                            break;
                        case "BACKUPDB":
                            ConstantFlag.FLAG_BACKUP_DB = (value.equals("1"));
                            break;
                        case "BTBPORT":
                            try {
                                ConstantFlag.FLAG_BTB_PORT = Integer.parseInt(value);
                            } catch (Exception exe) {

                            }
                            break;
                        case "RESET":
                            try {
                                Utility.savePreferences("odometer", "0");
                                Utility.savePreferences("engine_hours", "0");
                            } catch (Exception exe) {

                            }
                            break;

                    }

                    /*text.append(line);
                    text.append('\n');*/
                }
                br.close();
            }

            // ConstantFlag.LiveFg = Utility.getPreferences("is_live_server", ConstantFlag.LiveFg);
           /* CanMessages.protocol = Utility.getPreferences("protocol_supported", CanMessages.protocol);
            CanMessages.odometerSource = Utility.getPreferences("odometer_source", CanMessages.odometerSource);*/

        } catch (Exception e) {
            LogFile.write("LoadSetting: Error:" + e.getMessage(), LogFile.ERROR_LOG, LogFile.ERROR_LOG);
            LogDB.writeLogs(Utility.class.getName(), "LoadSetting: Error:", e.getMessage(), Utility.printStackTrace(e));
        }
    }

    public static void restart(Context context, int delay) {
         /*  Intent restartIntent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());*/

        Intent restartIntent = new Intent(context, MainActivity.class);
        restartIntent.setAction(Intent.ACTION_MAIN);
        restartIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent intent = PendingIntent.getActivity(
                context, 0,
                restartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, intent);
        System.exit(2);
    }

    // Created By: Deepak Sharma
    // Created Date: 13 September 2017
    // Purpose: load settings on the fly
    public static void executeQuery() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(context);
            database = helper.getWritableDatabase();

            File sdcard = Environment.getExternalStorageDirectory();
            //Get the text file
            File file = new File(sdcard, "query.txt");

            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                if ((line = br.readLine()) != null) {
                    database.execSQL(line);
                }
                br.close();
                file.delete();
            }

        } catch (Exception e) {
            LogFile.write("executeQuery: Error:" + e.getMessage(), LogFile.ERROR_LOG, LogFile.ERROR_LOG);
            LogDB.writeLogs(Utility.class.getName(), "executeQuery: Error:", e.getMessage(), Utility.printStackTrace(e));

        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception exe1) {
            }
        }
    }


    public static String backupDB() {
        String backupDBPath = "";
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            // write externa storage is used by legacy permission which would not be available from next version
            if (sd.canWrite()) {
                String currentDBPath = "/data/data/" + context.getPackageName() + "/databases/EDL.db";
                backupDBPath = Utility.GetDBBackupName(); // LogFile.DATABASE_BACKUP_NAME;
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {

        }
        return backupDBPath;
    }

    public static void restoreDB() {
        try {

            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/data/data/" + context.getPackageName() + "/databases/EDL.db";
                String backupDBPath = LogFile.DATABASE_BACKUP_NAME;
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {

        }
    }

    // load location as per current coordinate
    // used to reverse geocode where ever background events are stored
    private static void loadCurrentAddress() {
        if (Utility.mapLoadedFg)
            return;

        try {
            // initialize geocoder
            Geocoder geocoder = new Geocoder(Utility.context, Locale.getDefault());
            // get location address from gps coordinate.
            // we will get only 1 address
            List<Address> addresses = geocoder.getFromLocation(Utility.currentLocation.getLatitude(), Utility.currentLocation.getLongitude(), 1);

            // if google api returns address then we will update our ic_flagbar_web_service_on variable
            if (addresses != null && addresses.size() > 0) {
                String addressName = addresses.get(0).getAddressLine(0);
                Utility.currentLocation.setLocationDescription(addressName);
            }
        } catch (Exception exe) {
            exe.printStackTrace();
        }


    }

    // Created By: Deepak Sharma
    // Created Date: 24 July 2018
    // Purpose: copy eld config file that were synced from data base
    public static void copyEldConfig() {
        try {
            if (Utility.EldConfig != null && !Utility.EldConfig.isEmpty()) {
                LogFile.writeELDConfig();
            } else {
                LogFile.deleteEldConfigFile();
            }
        } catch (Exception exe) {

        }
    }

    // Created By: Deepak Sharma
    // Created Date: 24 July 2018
    // Purpose: clear application data
    public static void clearApplicationData() {
        try {
            ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                    .clearApplicationUserData();
        } catch (Exception exe) {

        }
    }

    public static void TestDevice() {
        if (IMEI.equals("353608073208384") || IMEI.equals("356961076533234")) {
            restoreDB();


            CanMessages.protocol = "OBD2";
            savePreferences("syncStatus", true);
            IMEI = "352009110525404";
            savePreferences("driverid", 1923);
            savePreferences("codriverid", 0);
            savePreferences("activeuserid", 1923);
            savePreferences("onscreenuserid", 1923);
        }
    }

    //created By: Simran
    //created Date: 19 March 2020
    // Purpose: get data from db
    public static void getDataFromDB() {

    }

    // Created By: Deepak Sharma
    // Created Date: 5 September 2018
    // Purpose: call nearest distance from auto event
    public static void callGeocodeTask() {

        if (_appSetting.getLocationSource() == 1) {
            new NearestDistanceTask(new NearestDistanceTask.PostTaskListener<Boolean>() {
                @Override
                public void onPostTask(Boolean status) {

                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    // Created By: Deepak Sharma
    // Created Date: 19 December 2019
    // Purpose: change color of textview as per hours left
    public static void setViolationColor(int hourLeft, TextView tvViolation, Context context) {

        if (hourLeft == 0) {
            tvViolation.setTextColor(ContextCompat.getColor(context, R.color.red1));
        } else if (hourLeft < 60) {
            tvViolation.setTextColor(ContextCompat.getColor(context, R.color.yellow2));
        } else {

            tvViolation.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 16 December 2019
    // Purpose: concat string to the left
    public static String padLeft(String data, int length) {

        String result = String.format("%" + length + "s", data).replace(' ', '0');
        return result;
    }

    // Created By: Deepak Sharma
    // Created Date: 13 January 2020
    // Purpose: Device Balance Post
    public static void DeviceBalancePost() {

        try {

            // deduct device balance if has not been deducted
            DeviceBalanceDB.DeviceBalanceTransactionSave();

            // post device balance if deducted else skip
            MainActivity.postData(CommonTask.POST_DEVICE_BALANCE);
        } catch (Exception exe) {

        }
    }


    // Created By: Sahil Bansal
    // Created Date: 14 January 2019
    // Purpose: screen orientation will work landscape and portrait for tab, for mobile app will work only in portrait
    public static void setOrientation(Activity activity) {

        if (!isLargeScreen(activity)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            Utility._appSetting.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            AppSettings.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            Configuration config = activity.getResources().getConfiguration();


            //fix the app orientation when app launch
            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
                Utility._appSetting.setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
                AppSettings.setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
                //lvEvents.addHeaderView(header);
            } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
                Utility._appSetting.setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
                AppSettings.setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
                //lvEvents.addHeaderView(header);
            }
            /*if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.PORTRAIT.ordinal()) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            } else if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.LANSCAPE.ordinal()) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.AUTO.ordinal()) {
                Settings.System.putInt(activity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }*/
        }
    }


    // Created By: Sahil Bansal
    // Created Date: 28 February 2020
    // Purpose: handle json null value
    public static String getJsonString(JSONObject jsonObject, String key) {
        if (jsonObject.has(key)) {
            if (!jsonObject.isNull(key)) {
                try {
                    if (!jsonObject.getString(key).equals("null")) {
                        return jsonObject.getString(key);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";

    }

    // Created By: Deepak Sharma
    // Created Date: 6 March 2020
    // Purpose: truncate value upto provided decimal place
    public static double truncate(double value, double precision) {
        precision = Math.pow(10, precision);
        return value > 0 ? (Math.floor(value * precision) / precision) : (Math.ceil(value * precision) / precision);
    }

    // Created By: Deepak Sharma
    // Created Date: 26 March 2020
    // Purpose: open pdf from you tube
    public static void openPdf(String path) {
        File file = new File(path);
        Uri Uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);

        try {

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.setDataAndType(Uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Instruct the user to install a PDF reader here, or something
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }


    // Created By: Deepak Sharma
    // Created Date: 26 March 2020
    // Purpose: open video from you tube
    public static void openVideo(String url) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + url));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
        }
    }


    //Created by: simran
    //Created date: 25 Feb 2020
    //Purpose: get shared prefrence data
    public static String getSharedPrefrences() {
        String sharedPrefrence = "";
        SharedPreferences sp = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE));

        sharedPrefrence = sp.getString("odometer", CanMessages.OdometerReading) + "," + sp.getString("engine_hours", CanMessages.EngineHours)
                + "," + sp.getInt("odometer_since_driving", 0) + "," + sp.getString("timezoneid", Utility.TimeZoneId)
                + "," + sp.getInt("packageid", 0) + "," + sp.getString("features", "").replace(",", "-") + "," +
                sp.getBoolean("OBD2FG", false) + "," + sp.getBoolean("inspector_mode", false) + "," +
                sp.getString("protocol_supported", CanMessages.protocol) + "," + sp.getString("shipping_number", Utility.ShippingNumber)
                + "," + sp.getString("trailer_number", Utility.TrailerNumber) + "," + sp.getInt("driverid", 0) + "," +
                sp.getInt("codriverid", 0) + "," + sp.getInt("activeuserid", 0) + "," +
                sp.getInt("onscreenuserid", 0) + "," + sp.getBoolean("logfile_sent", false) + "," +
                sp.getBoolean("user_configured", false) + "," + sp.getInt("auto_status_change", 0) + "," +
                sp.getInt("location_source", 0) + "," + sp.getInt("ecu_connectivity", 1);
        return sharedPrefrence;
    }


    //Created by: simran
    //Created date: 25 Feb 2020
    //Purpose: get vehicle data
    public static String getVehicleData() {
        String vehicleData = "";
        vehicleData = vehicleData + CanMessages.FuelLevel1 + "," + CanMessages.CoolantTemperature
                + "," + CanMessages.Speed + "," + CanMessages.RPM + "," + CanMessages.EngineHours
                + "," + CanMessages.OdometerReading + "," + CanMessages.Voltage + "," +
                CanMessages.TotalFuelConsumed;
        return vehicleData;
    }

    //Created by: simran
    //Created date: 28 Feb 2020
    //Purpose: get flags data
    public static String getFlagsData() {
        String flagData = "@F,";
        flagData = flagData + MainActivity.isGPSEnabled + "," + GPSData.CellOnlineFg
                + "," + GPSData.WifiOnFg + "," + GPSData.NetworkType + "," + CanMessages.mState + "," +
                Utility.BatteryLevel + "," + GPSData.ACPowerFg + "," + Utility.webServiceConnectivity;
        return flagData;
    }


    // Created By: Deepak Sharma
    // Created Date: 28 April 2020
    // Purpose: get violation id
    public static int getViolationId(String rule) {
        int id = 0;
        for (int i = 0; i < _ViolationList.length; i++) {
            if (rule.equals(_ViolationList[i])) {
                id = i;
                break;
            }
        }
        return id;
    }

    // Created By: Deepak Sharma
    // Created Date: 22 July 2020
    // Purpose: get if difference in time of eld2020 device and android device is greater than 2
    public static boolean TimeDifferentFg() {
        float diff = Math.abs(getDiffMins(getCurrentDateTime(), _CurrentDateTime));

        Log.d("TimeDifference", "Android Time: " + getCurrentDateTime() + ", ELD Time: " + _CurrentDateTime+", Diff: " + diff + "");
        return diff > 2;
    }
}