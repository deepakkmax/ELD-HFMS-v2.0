package com.hutchsystems.hutchconnect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.usage.NetworkStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.hutchsystems.hutchconnect.adapters.DiagnosticMalfunctionAdapter;
import com.hutchsystems.hutchconnect.adapters.DrawerItemAdapter;
import com.hutchsystems.hutchconnect.adapters.EventAdapter;
import com.hutchsystems.hutchconnect.beans.AppInfoBean;
import com.hutchsystems.hutchconnect.beans.CTPATInspectionBean;
import com.hutchsystems.hutchconnect.beans.ChangeEventListner;
import com.hutchsystems.hutchconnect.beans.DailyLogBean;
import com.hutchsystems.hutchconnect.beans.DiagnosticIndicatorBean;
import com.hutchsystems.hutchconnect.beans.DocumentBean;
import com.hutchsystems.hutchconnect.beans.DrawerItemBean;
import com.hutchsystems.hutchconnect.beans.EventBean;
import com.hutchsystems.hutchconnect.beans.GPSData;
import com.hutchsystems.hutchconnect.beans.MessageBean;
import com.hutchsystems.hutchconnect.beans.PermissionBean;
import com.hutchsystems.hutchconnect.beans.ReportBean;
import com.hutchsystems.hutchconnect.beans.ScheduleBean;
import com.hutchsystems.hutchconnect.beans.TripInspectionBean;
import com.hutchsystems.hutchconnect.beans.UserBean;
import com.hutchsystems.hutchconnect.beans.VehicleBean;
import com.hutchsystems.hutchconnect.common.AlarmSetter;
import com.hutchsystems.hutchconnect.common.AlertMonitor;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.ChatClient;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.ConstantFlag;
import com.hutchsystems.hutchconnect.common.CustomDateFormat;
import com.hutchsystems.hutchconnect.common.DeviceInfo;
import com.hutchsystems.hutchconnect.common.DiagnosticMalfunction;
import com.hutchsystems.hutchconnect.common.DocumentType;
import com.hutchsystems.hutchconnect.common.GPSTracker;
import com.hutchsystems.hutchconnect.common.GetCall;
import com.hutchsystems.hutchconnect.common.HutchConnect;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.NetworkStatsHelper;
import com.hutchsystems.hutchconnect.common.PostCall;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.dashboard.DashboardWithEngineHourActivity;
import com.hutchsystems.hutchconnect.dashboard.DashboardWithGauageCluster;
import com.hutchsystems.hutchconnect.dashboard.DashboardWithGraphActivity;
import com.hutchsystems.hutchconnect.db.CarrierInfoDB;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.DocumentDB;
import com.hutchsystems.hutchconnect.db.EventDB;
import com.hutchsystems.hutchconnect.db.HourOfServiceDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.LoginDB;
import com.hutchsystems.hutchconnect.db.MessageDB;
import com.hutchsystems.hutchconnect.db.ReportDB;
import com.hutchsystems.hutchconnect.db.ScheduleDB;
import com.hutchsystems.hutchconnect.db.SettingsDB;
import com.hutchsystems.hutchconnect.db.TrailerDB;
import com.hutchsystems.hutchconnect.db.TripInspectionDB;
import com.hutchsystems.hutchconnect.db.UserDB;
import com.hutchsystems.hutchconnect.fragments.AddNoticeOrderFragment;
import com.hutchsystems.hutchconnect.fragments.BTBConnectionFragment;
import com.hutchsystems.hutchconnect.fragments.BluetoothConnectivityFragment;
import com.hutchsystems.hutchconnect.fragments.CTPATInspectionFragment;
import com.hutchsystems.hutchconnect.fragments.CTPATInspectionFragmentList;
import com.hutchsystems.hutchconnect.fragments.CanBusDataFragment;
import com.hutchsystems.hutchconnect.fragments.CountDownDialogFragment;
import com.hutchsystems.hutchconnect.fragments.DailyLogDashboardFragment;
import com.hutchsystems.hutchconnect.fragments.DetailFragment;
import com.hutchsystems.hutchconnect.fragments.DockingFragment;
import com.hutchsystems.hutchconnect.fragments.DocumentDetailFragment;
import com.hutchsystems.hutchconnect.fragments.DocumentDetailListFragment;
import com.hutchsystems.hutchconnect.fragments.DrayageDispatchFragment;
import com.hutchsystems.hutchconnect.fragments.DriverProfileFragment;
import com.hutchsystems.hutchconnect.fragments.DvirFragment;
import com.hutchsystems.hutchconnect.fragments.ELogFragment;
import com.hutchsystems.hutchconnect.fragments.ExtraFragment;
import com.hutchsystems.hutchconnect.fragments.FuelDetailFragment;
import com.hutchsystems.hutchconnect.fragments.FuelListFragment;
import com.hutchsystems.hutchconnect.fragments.GeofenceDetailFragment;
import com.hutchsystems.hutchconnect.fragments.HelpFragment;
import com.hutchsystems.hutchconnect.fragments.IncidentListFragment;
import com.hutchsystems.hutchconnect.fragments.InspectLogFragment;
import com.hutchsystems.hutchconnect.fragments.LoginFragment;
import com.hutchsystems.hutchconnect.fragments.MaintenanceDetailFragment;
import com.hutchsystems.hutchconnect.fragments.MessageFragment;
import com.hutchsystems.hutchconnect.fragments.ModifiedFragment;
import com.hutchsystems.hutchconnect.fragments.NewEventFragment;
import com.hutchsystems.hutchconnect.fragments.NewInspectionFragment;
import com.hutchsystems.hutchconnect.fragments.NewMaintenanceFragment;
import com.hutchsystems.hutchconnect.fragments.NotificationFragment;
import com.hutchsystems.hutchconnect.fragments.OutputFileSendDialog;
import com.hutchsystems.hutchconnect.fragments.PCDialogFragment;
import com.hutchsystems.hutchconnect.fragments.PopupDialog;
import com.hutchsystems.hutchconnect.fragments.RecapFragment;
import com.hutchsystems.hutchconnect.fragments.RuleListFragment;
import com.hutchsystems.hutchconnect.fragments.SettingsFragment;
import com.hutchsystems.hutchconnect.fragments.ShutDownDeviceDialog;
import com.hutchsystems.hutchconnect.fragments.TabDisplayFragment;
import com.hutchsystems.hutchconnect.fragments.TabSystemFragment;
import com.hutchsystems.hutchconnect.fragments.TicketHistoryListFragment;
import com.hutchsystems.hutchconnect.fragments.TpmsFragment;
import com.hutchsystems.hutchconnect.fragments.UnCertifiedFragment;
import com.hutchsystems.hutchconnect.fragments.UnidentifyFragment;
import com.hutchsystems.hutchconnect.fragments.UnitNoListFragment;
import com.hutchsystems.hutchconnect.fragments.UserListFragment;
import com.hutchsystems.hutchconnect.fragments.VehicleInfoFragment;
import com.hutchsystems.hutchconnect.fragments.ViolationFragment;
import com.hutchsystems.hutchconnect.services.AutoStartService;
import com.hutchsystems.hutchconnect.services.UsbService;
import com.hutchsystems.hutchconnect.tasks.AutoSyncData;
import com.hutchsystems.hutchconnect.tasks.NotificationTask;
import com.hutchsystems.hutchconnect.tasks.PostDailyLog;
import com.hutchsystems.hutchconnect.tasks.ReportIssue;
import com.hutchsystems.hutchconnect.tasks.UploadFileToServer;
import com.hutchsystems.hutchconnect.util.LetterSpacingTextView;
import com.hutchsystems.hoursofservice.Repository.DurationRepository;
import com.hutchsystems.hoursofservice.Violation;
import com.hutchsystems.hoursofservice.model.DutyStatus;
import com.hutchsystems.hoursofservice.model.ViolationModel;
import com.kmaxsystems.reversegeocode.ReverseGeoCoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static com.hutchsystems.hutchconnect.common.Utility.DIALOGBOX_ICON;
import static com.hutchsystems.hutchconnect.common.Utility.DIALOGBOX_TITLE;

public class MainActivity extends ELogMainActivity
        implements NavigationView.OnNavigationItemSelectedListener, FragmentManager.OnBackStackChangedListener,
        ELogFragment.OnFragmentInteractionListener, DockingFragment.OnFragmentInteractionListener,
        UnCertifiedFragment.OnFragmentInteractionListener, UnidentifyFragment.OnFragmentInteractionListener,
        ModifiedFragment.OnFragmentInteractionListener, ViolationFragment.OnFragmentInteractionListener,
        EventAdapter.ItemClickListener, DiagnosticMalfunction.DiagnosticMalfunctionNotification,
        NewEventFragment.OnFragmentInteractionListener, DetailFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener, UserListFragment.OnFragmentInteractionListener,
        BluetoothConnectivityFragment.OnFragmentInteractionListener, DvirFragment.OnFragmentInteractionListener,
        DailyLogDashboardFragment.OnFragmentInteractionListener, TpmsFragment.OnFragmentInteractionListener,
        TabSystemFragment.OnFragmentInteractionListener, LoginFragment.OnFragmentInteractionListener,
        MessageFragment.OnFragmentInteractionListener, NewInspectionFragment.OnFragmentInteractionListener,
        InspectLogFragment.OnFragmentInteractionListener, ChatClient.ChatMessageReceiveIndication, PopupDialog.DialogActionInterface,
        ShutDownDeviceDialog.OnFragmentInteractionListener,
        ExtraFragment.OnFragmentInteractionListener,
        VehicleInfoFragment.OnFragmentInteractionListener, FuelListFragment.OnFragmentInteractionListener,
        FuelDetailFragment.OnFragmentInteractionListener,
        IncidentListFragment.OnFragmentInteractionListener,
        AddNoticeOrderFragment.OnFragmentInteractionListener,
        GPSTracker.IGPS,
        GeofenceDetailFragment.OnFragmentInteractionListener,
        DocumentDetailFragment.OnFragmentInteractionListener,
        DocumentDetailListFragment.OnFragmentInteractionListener,
        TabDisplayFragment.IDisplaySetting,
        CanBusDataFragment.OnFragmentInteractionListener,
        BTBConnectionFragment.OnFragmentInteractionListener,
        DriverProfileFragment.OnFragmentInteractionListener,
        HelpFragment.OnFragmentInteractionListener,
        RuleListFragment.OnFragmentInteractionListener, ChangeEventListner, HutchConnect.ICanMessage, CTPATInspectionFragmentList.OnFragmentInteractionListener,
        CTPATInspectionFragment.OnFragmentInteractionListener, DetailFragment.RefresData,
        ReportDB.IReport, UnitNoListFragment.OnFragmentChangeSerialNoListener, PCDialogFragment.IReport, RecapFragment.OnFragmentInteractionListener,
        MaintenanceDetailFragment.OnFragmentInteractionListener, NewMaintenanceFragment.OnFragmentInteractionListener {

    public int REFER_FRIEND_INBOX_MESSAGE = 1111;

    private PopupDialog ponDutyChangeDialog;
    private boolean onDutyChangeDialogResponse = false, isDialogShown = false;

    AlertDialog.Builder onDutyChangeBuilder;

    private final int DATASTORAGE_CHECKING_TIME = 1800000; //30 mins

    private final int DailyLog_Screen = 0;
    private final int Inspect_DailyLog_Screen = 1;
    private final int Uncertified_LogBook_Screen = 2;
    private final int Unidentified_Data_Screen = 3;
    private final int Edit_Request_Screen = 4;
    private final int Violation_History_Screen = 5;
    private final int Unidentified_Event_Screen = 6;
    private final int Input_Information_Screen = 7;
    private final int New_Event_Screen = 8;
    private final int Message = 9;
    private final int DVIR = 10;
    private final int TPMS = 11;
    public final int Login_Screen = 12;
    public final int Driver_Profile_Screen = 13;
    private final int Extra = 14;
    private final int DTC = 15;
    private final int ScoreCard = 16;
    private final int TrailerManagement = 17;
    private final int VehicleInfo = 18;
    private final int Fuel = 19;
    private final int Incident = 20;
    private final int Maintenance = 21;
    private final int Navigation = 22;
    private final int NewMaintenance = 23;
    private final int GeofenceDetail = 24;
    private final int DocumentDetail = 25;
    private final int DrayageDispatchList = 26;
    private final int DispatchDetail = 27;
    private final int ContainerDetail = 28;
    private final int SetProtocolDetail = 29;
    private final int Setting_Screen = 30;
    private final int FIX_BTB = 31;
    private final int HelpDetail = 32;
    private final int RuleScreen = 33;
    private final int Notification = 34;
    private final int TicketHistoryList = 35;
    private final int CTPATInspectionList = 36;
    private final int CTPATInspectionForm = 37;
    private final int UnitNoList = 37;
    private final int RECAP_SCREEN = 38;
    BluetoothAdapter adapter = null;
    ImageView ivActiveUser, ivDrivingImage;

    static TextView tvUserName;
    RelativeLayout tvFreeze;
    RelativeLayout tvLoginFreeze;
    RelativeLayout tvGauge;
    LinearLayout layoutAlertForInternet;
    RelativeLayout rlLoadingPanel;
    String driverName;
    String title = "";
    public static int currentDutyStatus = 1;
    public static int activeCurrentDutyStatus = 1;
    int totalDistance;
    int startOdometer;
    int startHourEngine;

    public static boolean undockingMode;

    String TAG = MainActivity.class.getName();

    private boolean bEditEvent = false;
    private boolean bWebEvent = false;
    private EventBean selectedEvent;
    private boolean isOnDailyLog;
    private boolean bInspectDailylog;
    private boolean bLogin;

    Toolbar toolbar;
    public static Date currentInspectionDate = Utility.newDateOnly();
    private int previousScreen;
    public int currentScreen;

    boolean bEventPowerOn = false;
    boolean bEventPowerOff = false;

    boolean bBluetoothConnectionSuccess = false;
    boolean bBluetoothConnectionError = false;
    boolean bBluetoothConnecting = false;
    ProgressDialog progressBarDialog;
    OutputFileSendDialog outputFileDialog;
    AlertDialog messageDialog;
    AlertDialog successDialog;

    ELogFragment elogFragment;
    /* DetailFragment inspectFragment;*/
    LoginFragment loginFragment;

    boolean specialCategoryChanged = false;

    AlertDialog specialCategoryDialog;
    boolean bSaveDriving = false;
    FrameLayout frameSpeedometer, frameFuelLevel, frameRPM;


    // Flag For  Displaying DVIR Form
    public static boolean isDVIR = false;

    // Flag For  Displaying Fuel Detail Form
    public static boolean iSFueling = false;

    LinearLayout ll_drivingRemaning, ll_enginehours;

    final android.os.Handler handler = new android.os.Handler();

    Runnable autoSync = new Runnable() {
        @Override
        public void run() {

            if (Utility.isInternetOn()) {
                new AutoSyncData(autoSyncDataPostTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                onUploadDocument();
            }

            // and here comes the "trick"
            handler.postDelayed(this, Utility._appSetting.getSyncTime() * 60 * 1000);
        }
    };

    final Handler checkDataStorageHandler = new Handler();
    Runnable checkDataStorage = new Runnable() {
        @Override
        public void run() {
            try {

                UserBean currentUser = Utility.user1.isOnScreenFg() ? Utility.user1 : Utility.user2;
                if (currentUser.getExemptELDUseFg() == 0) {
                    float memory = Utility.gigabytesAvailable(Environment.getExternalStorageDirectory());
                    if (memory < .1f) {
                        //memory less than 100MB
                        //data recording malfunction
                        if (!DiagnosticIndicatorBean.DataRecordingMalfunctionFg) {
                            DiagnosticIndicatorBean.DataRecordingMalfunctionFg = true;
                            // save malfunction for storage compliance
                            DiagnosticMalfunction.saveDiagnosticIndicatorByCode("R", 1, "DataRecordingMalfunctionFg");
                        }
                    } else {
                        if (DiagnosticIndicatorBean.DataRecordingMalfunctionFg) {
                            // clear malfunction for storage compliance
                            DiagnosticIndicatorBean.DataRecordingMalfunctionFg = false;
                            DiagnosticMalfunction.saveDiagnosticIndicatorByCode("R", 2, "DataRecordingMalfunctionFg");
                        }

                    }
                }
            } catch (Exception ex) {

            }
            checkDataStorageHandler.postDelayed(this, DATASTORAGE_CHECKING_TIME);
        }
    };

    Handler checkDataTransferMalfunctionHandler = new Handler();
    Runnable checkDataTransferMalfunction = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "checkDataTransferMalfunction");
            UserBean currentUser = Utility.user1.isOnScreenFg() ? Utility.user1 : Utility.user2;
            if (currentUser.getExemptELDUseFg() == 0) {
                //count number of checking after diagnostic happens, only check 3 times, period is 24 hours
                Utility.DataTransferMalfunctionCheckingNumber++;

                SharedPreferences sp = getApplicationContext().getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);
                String transferSuccessDate = sp.getString("data_transfer", "");
                if (transferSuccessDate != "") {
                    if (Utility.getDiffDay(transferSuccessDate, Utility.getCurrentDate()) < 3) {
                        //has data transfer success in 3 next days -> clear diagnostic
                    }
                } else {
                    Utility.DataTransferDiagnosticCount++;
                }

                if (Utility.DataTransferMalfunctionCheckingNumber > 3) {
                    if (Utility.DataTransferDiagnosticCount >= 3) {

                        Utility.DataTransferDiagnosticCount = 0;
                    }

                    Utility.DataTransferMalfunctionCheckingNumber = 0;
                    checkDataTransferMalfunctionHandler.removeCallbacks(this);
                }
            }
            checkDataTransferMalfunctionHandler.postDelayed(this, 24 * 60 * 60 * 1000);
        }
    };

    Handler checkDataTransferDiagnosticHandler = new Handler();
    Runnable checkDataTransferDiagnostic = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "checkDataTransferDiagnostic");
            boolean bDiagnotic;
            UserBean currentUser = Utility.user1.isOnScreenFg() ? Utility.user1 : Utility.user2;
            if (currentUser.getExemptELDUseFg() == 0) {
                SharedPreferences sp = getApplicationContext().getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);
                String transferSuccessDate = sp.getString("data_transfer", "");
                if (transferSuccessDate != "") {
                    if (Utility.getDiffDay(transferSuccessDate, Utility.getCurrentDate()) > 7) {
                        //diagnostic
                        bDiagnotic = true;
                    } else {
                        bDiagnotic = false;
                    }
                } else {
                    //diagnostic
                    bDiagnotic = true;
                }

                if (bDiagnotic) {
                    //data transfer diagnostic
                    String diagnosticDate = sp.getString("data_diagnostic_date", "");
                    //have not save data transfer diagnostic or it is already saved more than 7 days
                    if (diagnosticDate.equals("") || (Utility.getDiffDay(diagnosticDate, Utility.getCurrentDate()) > 7)) {

                        sp.edit().putString("data_diagnostic_date", Utility.getCurrentDate()).commit();
                    }

                    checkDataTransferMalfunctionHandler.postDelayed(checkDataTransferMalfunction, 50);
                } else {
                    //clear diagnostic
                    //clear cache about diagnostic date
                    sp.edit().putString("data_diagnostic_date", "").commit();
                }
            }
            checkDataTransferDiagnosticHandler.postDelayed(this, 7 * 24 * 60 * 60 * 1000);
        }
    };
    public static boolean isGPSEnabled = true;
    private BroadcastReceiver gpsChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Receive GPS change");
            LocationManager locationManager = (LocationManager) Utility.context
                    .getSystemService(getBaseContext().LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                turnOnGPSIcon();
            } else {
                turnOffGPSIcon();

            }
        }
    };

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo localNetworkInfo = ((ConnectivityManager) getBaseContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();

            invalidateOptionsMenu();

            if (localNetworkInfo != null) {
                GPSData.RoamingFg = localNetworkInfo.isRoaming() ? 1 : 0;
                if (localNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    onUpdateWifiIcon(localNetworkInfo.isConnected());

                } else if (localNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

                    onUpdateNetworkIcon(localNetworkInfo.isConnected());
                } else {
                    GPSData.WifiOnFg = 0;
                    GPSData.CellOnlineFg = 0;
                }

                // Purpose : If Internet Connection Disconnect after Connecting the internet after one minute we will post the data
                if (localNetworkInfo.isConnected()) {
                    Boolean internetDisconnected = Utility.getPreferences("InternetDisconnected", false);
                    try {

                        if (internetDisconnected) {

                            Handler handler = new Handler();
                            Runnable r = new Runnable() {
                                public void run() {
                                    //what ever you do here will be done after 1 minute delay.
                                    new AutoSyncData(autoSyncDataPostTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                                    onUploadDocument();
                                }
                            };
                            // after one minute we will post the data
                            handler.postDelayed(r, 60000);

                            Utility.savePreferences("InternetDisconnected", false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } else {

                GPSData.WifiOnFg = 0;
                GPSData.CellOnlineFg = 0;
                onUpdateWifiIcon(false);
                // Save Internet Disconnected Value
                Utility.savePreferences("InternetDisconnected", true);
            }

            if (loginFragment != null) {
                loginFragment.onConnectionChanged();
            }
        }
    };
    private View viewColour;
    //private LinearLayout violationLayout;
    private boolean isTrailerDVIR;

    private void onAcPower() {
        Utility.showMsg("On AC Power");
      /*  new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    if (gForceMonitor != null) {
                        gForceMonitor.highPassFilter = true;
                        resumeGforce();
                    }
                } catch (Exception exe) {
                }
            }
        }).start();*/
    }

    private void onBatterPower() {

        // pauseGforce();
        Utility.showMsg("On Battery Power");
    }

    Thread thBatteryMonitor;
    private final static int levelThreshold = 60;
    private final static int shutDownThreshold = 30;
    ConstraintLayout layoutAlertMessage;
    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            Utility.BatteryHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 1);

            Utility.BatteryLevel = level;
            //Intent intent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean isPlugged = (plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB);
            if (GPSData.ACPowerFg == 0) {
                if (isPlugged) {
                    onAcPower();
                }
            } else {
                if (!isPlugged) {
                    onBatterPower();
                }
            }
            GPSData.ACPowerFg = (isPlugged ? 1 : 0);
            onUpdateBatteryIcon(level, isPlugged);
            if (level <= levelThreshold && !isPlugged) {
                if (thBatteryMonitor == null) {
                    thBatteryMonitor = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                try {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            layoutAlertMessage.setVisibility(View.VISIBLE);
                                        }
                                    });

                                    int waitTime = 5 * 60 * 1000; // 5 minutes wait time
                                    Thread.sleep(waitTime);

                                } catch (InterruptedException exe) {
                                    break;
                                }
                            }
                        }
                    });
                    thBatteryMonitor.setName("th-BatteryMonitor");
                    thBatteryMonitor.start();
                }
            } else {
                if (thBatteryMonitor != null) {
                    layoutAlertMessage.setVisibility(View.GONE);
                    Log.i("BatterMonitor-Removed: ", Utility.BatteryLevel + " %");
                    thBatteryMonitor.interrupt();
                    thBatteryMonitor = null;
                }
            }
        }
    };

    AutoSyncData.PostTaskListener<Boolean> autoSyncDataPostTaskListener = new AutoSyncData.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            showLoaderAnimation(false);
            if (!result) {
                Log.e(GetCall.class.getName(), "AutoSyncData Error:" + Utility.errorMessage);
                //LogFile.write(GetCall.class.getName() + "::AutoSyncData Error:" + Utility.errorMessage, LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
            }
            onUpdateWebServiceIcon(true);

            specialCategoryChanged = Utility.specialCategoryChanged();
        }
    };


    // Created By: Pallavi Wattamwar
    // purpose: common method for posting Data Seprately
    // Parameter : String : Service Which you want to post.
    public static void postData(String serviceType) {
        new CommonTask(serviceType).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // Created By: Pallavi Wattamwar
    // purpose: common method for posting Data Seprately
    // Parameter : String : Service Which you want to post.
    // Parameter :int :LogId
    public static void postData(String serviceType, int logId) {
        new CommonTask(serviceType, logId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // update the main content by replacing fragments
    FragmentManager fragmentManager = getSupportFragmentManager();

    LinearLayout flagBar, flagBarFreeze;

    boolean bHaveUnAssignedEvent;
    boolean bHaveLogbookToCertify;
    boolean firstLogin;

    FrameLayout frameDiagnostic;
    FrameLayout frameMalfunction;

    TextView tvSpeed, tvRPM, tvPosition, tvCoolant, tvVoltage;
    LetterSpacingTextView tvOdometer, tvEngineHours, tvDrivingRemainingFreeze;
    TextView tvViolation, tvViolationDate;
    Thread threadUpdateCANInfos;

    ImageView imgreezeSpeed, imgFreezeRPM, imgFreezeVoltage, imgFreezeCoolantTemp, imgFreezeThrPos;
    ImageView icGPS, icViolation, icNetwork, icWifi, icWebService, icCanbus, icBattery, icInspection, icMessage, icTPMS, icCertifyLog;

    ImageView icFreezeGPS;
    ImageView icFreezeViolation;
    ImageView icFreezeNetwork;
    ImageView icFreezeWifi;
    ImageView icFreezeWebService;
    ImageView icFreezeCanbus;
    ImageView icFreezeBattery;
    ImageView icFreezeInspection;
    ImageView icFreezeMessage;
    ImageView icFreezeActiveUser, ivalert;
    TextView tvLoginName, tvFreezeLoginName, tvalertMessage;
    CheckBox chkRules;

    int canbusState;
    Bitmap batteryBmp;
    Canvas canvas;

    ListView lvDrawer;
    ArrayList<DrawerItemBean> lstDrawerItems;
    public static TextToSpeech textToSpeech;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawer;

    boolean stopService = false;

    private void getDrawerItem() {
        lstDrawerItems = new ArrayList<>();
        DrawerItemBean bean;

        if (Utility.InspectorModeFg) {
            bean = new DrawerItemBean();
            bean.setId(R.id.inspect_dailylog);
            bean.setItem(getString(R.string.inspect_log_title));
            bean.setIcon(R.drawable.ic_drawer_daliy_log_inspect_drawer);
        } else {
            bean = new DrawerItemBean();
            bean.setId(R.id.daily_log);
            bean.setItem(getString(R.string.title_eld));
            bean.setIcon(R.drawable.ic_drawer_eld);
        }

        lstDrawerItems.add(bean);
        bean = new DrawerItemBean();
        bean.setId(R.id.dvir);
        bean.setItem(getString(R.string.dvir_title));
        bean.setIcon(R.drawable.ic_drawer_dvir);
        lstDrawerItems.add(bean);
        if (!Utility.InspectorModeFg) {

            bean = new DrawerItemBean();
            bean.setId(R.id.message);
            bean.setItem(getString(R.string.message_title));
            bean.setIcon(R.drawable.ic_drawer_messages);
            lstDrawerItems.add(bean);

            bean = new DrawerItemBean();
            bean.setId(R.id.help);
            bean.setItem(getString(R.string.help));
            bean.setIcon(R.drawable.ic_help);
            lstDrawerItems.add(bean);

            bean = new DrawerItemBean();
            bean.setId(R.id.extra);
            bean.setItem(getString(R.string.more_title));
            bean.setIcon(R.drawable.ic_drawer_more);
            lstDrawerItems.add(bean);


            bean = new DrawerItemBean();
            bean.setId(R.id.settings);
            bean.setItem(getString(R.string.settings_title));
            bean.setIcon(R.drawable.ic_drawer_setting);
            lstDrawerItems.add(bean);

        }

        bean = new DrawerItemBean();
        bean.setId(R.id.logout);
        bean.setItem(getString(R.string.logout_title));
        bean.setIcon(R.drawable.ic_drawer_logout);
        lstDrawerItems.add(bean);

        if (!Utility.InspectorModeFg) {
            bean = new DrawerItemBean();
            bean.setId(R.id.exit);
            bean.setItem(getString(R.string.menu_exit));
            bean.setIcon(R.drawable.ic_drawer_extra);
            lstDrawerItems.add(bean);
        }
    }

    private void bindDrawerItem() {
        getDrawerItem();
        DrawerItemAdapter drawerItemAdapter = new DrawerItemAdapter(R.layout.activity_main, lstDrawerItems);
        lvDrawer.setAdapter(drawerItemAdapter);
    }

    public boolean onNavigationItemSelected(int id) {

        elogFragment = null;
        //inspectFragment = null;
        loginFragment = null;
        // Handle navigation view item clicks here.
        if (id == R.id.daily_log) {
            if (undockingMode) {
                replaceFragment(new DockingFragment());
            } else {
                replaceFragment(DailyLogDashboardFragment.newInstance());
            }
            bInspectDailylog = false;
            bEditEvent = false;
            isOnDailyLog = false;
            getSupportActionBar().setTitle(getString(R.string.eld_title));
        } else if (id == R.id.inspect_dailylog) {
            callInspectELog();
        } else if (id == R.id.dvir) {
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(DvirFragment.newInstance());
            getSupportActionBar().setTitle(getString(R.string.dvir_title));
            previousScreen = currentScreen;
            currentScreen = DVIR;
            title = getString(R.string.dvir_title);
        } else if (id == R.id.tpms) {
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(TpmsFragment.newInstance());
            getSupportActionBar().setTitle(getString(R.string.tpms_title));
            previousScreen = currentScreen;
            currentScreen = TPMS;
            title = getString(R.string.tpms_title);
        } else if (id == R.id.settings) {
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(SettingsFragment.newInstance());
            getSupportActionBar().setTitle(getString(R.string.settings_title));
            title = getString(R.string.settings_title);
            previousScreen = currentScreen;
            currentScreen = Setting_Screen;
            title = getString(R.string.settings_title);
        } else if (id == R.id.extra) {
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(ExtraFragment.newInstance());
            getSupportActionBar().setTitle(getString(R.string.more_title));
            previousScreen = currentScreen;
            currentScreen = Extra;
            title = getString(R.string.more_title);
        } else if (id == R.id.navigation) {

            title = getString(R.string.navigation_title);
            isOnDailyLog = false;
            bInspectDailylog = false;

            //replaceFragment(mapFragment);
            getSupportActionBar().setTitle(title);
            previousScreen = currentScreen;
            currentScreen = Navigation;

        } else if (id == R.id.message) {
            title = getApplicationContext().getResources().getString(R.string.message_title);
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(UserListFragment.newInstance());
            getSupportActionBar().setTitle(title);
            previousScreen = currentScreen;
            currentScreen = Message;
        } else if (id == R.id.logout) {

            if (!Utility.InspectorModeFg) {
                if (!Utility.HutchConnectStatusFg) {
                    Utility.showAlertMsg("You are not allowed to logout untill bluetooth is disconnected!");
                    return true;
                }

                int currentStatus = EventDB.getCurrentDutyStatus(Utility.onScreenUserId);
                if (currentStatus != 1) {
                    Utility.showAlertMsg(getString(R.string.logout_offduty_alert));
                    return true;
                }
            }

            if (specialCategoryChanged) {
                showSpecialCategory(true);
            } else {
                showLogoutDialog();
            }
        } else if (id == R.id.drayage) {
            title = getString(R.string.drayage);
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(DrayageDispatchFragment.newInstance());
            getSupportActionBar().setTitle(title);
            previousScreen = currentScreen;
            currentScreen = DrayageDispatchList;
        } else if (id == R.id.help) {
            onLoadHelp();

        } else if (id == R.id.exit) {
            exitApp();
        }

        invalidateOptionsMenu();
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 24 July 2018
    // Purpose: App will show warning if device is offline for last 7 days. If devices remains offline
    // for more than 30 days then app will block user to take any action
    public void checkConnectivity() {

        String previousDate = Utility.getPreferences("InternetConnectedDate", "");
        String currentDate = Utility.getCurrentDateTime();

        if (!previousDate.isEmpty()) {
            int diffDay = Utility.getDiffDay(previousDate, currentDate);
            if (!Utility.isInternetOn()) {
                // if the current date and last internet connection date difference is more than 7 day and less than 30 day
                if (diffDay >= 7 && diffDay < 30) {

                    layoutAlertForInternet.setVisibility(View.VISIBLE);
                    viewColour.setBackgroundColor(getColor(R.color.yellow));
                    ivalert.setBackgroundResource(R.drawable.ic_flagbar_violation_upcoming);
                    tvalertMessage.setText(getResources().getString(R.string.please_connect_to_internet));

                    layoutAlertForInternet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            layoutAlertForInternet.setVisibility(View.GONE);
                        }
                    });

                    // if the current date and last internet connection date difference is more than 30 day
                } else if (diffDay >= 30) {
                    layoutAlertForInternet.setVisibility(View.VISIBLE);
                    viewColour.setBackgroundColor(getColor(R.color.red));
                    ivalert.setBackgroundResource(R.drawable.ic_flagbar_violation);
                    tvalertMessage.setText(getResources().getString(R.string.connect_to_internet_you_cant_proceed));
                    layoutAlertForInternet.setOnClickListener(null);
                }
            } else {
                // popup will disapear once device comes onlne
                if (layoutAlertForInternet != null) {
                    layoutAlertForInternet.setVisibility(View.GONE);
                    layoutAlertForInternet.setOnClickListener(null);
                }
            }

        }
    }

    // Created By: Deepak Sharma
    // Created Date: 9 July 2019
    // Purpose: initialize preferences
    private void initPreferences() {
        stopService = false;
        undockingMode = Utility.getPreferences("undocking", false);
        // get ecu connectivity setting
        Utility._appSetting.setECUConnectivity(Utility.getPreferences("ecu_connectivity", 1));

        String upgradeFile = Utility.getPreferences("upgrade_file", "");

        if (!upgradeFile.equals("")) {
            File file = new File(upgradeFile);
            if (file.exists()) {
                file.delete();
            }
        }

        int defalutOdometerSinceDriving = 0;
        try {
            defalutOdometerSinceDriving = Double.valueOf(CanMessages.OdometerReading).intValue();
        } catch (Exception exe) {

        }
        GPSData.OdometerSinceDriving = Utility.getPreferences("odometer_since_driving", defalutOdometerSinceDriving);
    }

    // Created By: Deepak Sharma
    // Created Date: 24 July 2019
    // Purpose: initialize listener from other activity/fragment
    private void initListener() {

        Utility.context = this;
        // set alarm
        if (Utility.as == null) {
            Utility.as = new AlarmSetter();
            Utility.as.SetAlarm(this);
        }
        DiagnosticMalfunction.mListener = this;
        ChatClient.icListner = this;


        HutchConnect.mHutchConnectListner = this;

        GPSTracker.mListner = this;

        ReportDB.mListener = this;
    }


    // Created By: Deepak Sharma
    // Created Date: 9 July 2019
    // Purpose: initialize broadcast receiver
    private void initBroadCastReceiver() {
        this.registerReceiver(batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        this.registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        this.registerReceiver(gpsChangeReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        this.registerReceiver(dateChangedReceiver, new IntentFilter("MID_NIGHT"));

    }

    // Created By: Deepak Sharma
    // Created Date: 9 July 2019
    // Purpose: initialize login fragment
    private void initLoginFragment() {
        setDrawerState(false);
        toolbar.setVisibility(View.GONE);
        //flagBar.setVisibility(View.GONE);
        bLogin = true;
        previousScreen = -1;
        currentScreen = Login_Screen;
        if (loginFragment == null) {
            loginFragment = new LoginFragment();
        }
        Bundle bun = new Bundle();
        bun.putBoolean("CoDriverFg", false);
        loginFragment.setArguments(bun);
        replaceFragment(loginFragment);
    }

    // Created By: Deepak Sharma
    // Created Date: 9 July 2019
    // Purpose: initialize popup PCDialog fragment
    private void initPopupDialog() {


        ponDutyChangeDialog = new PopupDialog();


        onDutyChangeBuilder = new AlertDialog.Builder(this);
        onDutyChangeBuilder.setMessage(R.string.contine_driving_confirmation).setPositiveButton(getString(R.string.ok), onDutyChangeDialogClickListener);

        initializeBTBAlertDialog();

        layoutUnitMismatch = (ConstraintLayout) findViewById(R.id.layoutUnitMismatch);
        progress_unit_mismatch = (ProgressBar) findViewById(R.id.progress_unit_mismatch);
        tvUnitMismatchProgress = (TextView) findViewById(R.id.tvUnitMismatchProgress);
    }

    //Created By: Simran
    //Created Date: 30/01/2020
    //Purpose: post dailyLogEvents
    private void postDailyLogEvents() {

        // if internet is not available don't check update
        if (!Utility.isInternetOn())
            return;

        JSONArray jsonArray = EventDB.getUnpostedEvents();

        if (jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    new PostDailyLog(postTaskListener).execute(String.valueOf(jsonObject.getInt("DAILYLOGID")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    PostDailyLog.PostTaskListener<Boolean> postTaskListener = new PostDailyLog.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            setTheme(R.style.AppTheme);

            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_main);

            objHutchConnect = new HutchConnect(getApplicationContext());

            // initialize reverse geocode
            initializeReverseGeoCode();

            Utility.IMEIGet();

            initPreferences();

            Utility.setOrientation(MainActivity.this);

            //checkDataTransferDiagnosticHandler.postDelayed(checkDataTransferDiagnostic, 50);

            checkDataStorageHandler.postDelayed(checkDataStorage, 50);

            Utility.VersionGet(this);

            // initialize controls of this activity
            initializeControls();

            if (!Utility.getPreferences("firstrun", true)) {
                // Test device and restore data
                //Utility.TestDevice();

                // initialize event listner
                initListener();

                initBroadCastReceiver();

                initPopupDialog();

                //handlerOD.postDelayed(runnableStatus, 5000);
                threadUpdateCANInfos = new Thread(runnableCAN);
                threadUpdateCANInfos.start();

                //checkDataTransferDiagnosticHandler.postDelayed(checkDataTransferDiagnostic, 50);

                checkDataStorageHandler.postDelayed(checkDataStorage, 50);

                // delete old records from some tables in database
                Utility.DeleteOldData();

                runDailyTask();

                setVisionMode();


                // if internet is not available don't check update
                if (Utility.isInternetOn()) {

                    checkUpdate();

                    PostCall.PostDeviceInfo(true);

                    // posting event at once will cause issue
                    //postDailyLogEvents();

                    PostCall.PostDaily();

                    GetCall.SyncMonthly();
                    //CheckBalance(false);
                    // Notification Task
                    new NotificationTask().execute();

                    // Post Data
                    new AutoSyncData(autoSyncDataPostTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

            }
        } catch (Exception exe) {
            String sStackTrace = exe.getMessage() + "\n" + Utility.printStackTrace(exe);
            LogFile.write(MainActivity.class.getName() + "::onCreate error:" + sStackTrace, LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TAG, "::onCreate Error:", exe.getMessage(), Utility.printStackTrace(exe));
        }

    }

    // Created By: Deepak Sharma
    // Created Date: 1 May 2020
    // Purpose: check update for latest version
    private void checkUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final AppInfoBean appInfo = GetCall.AppInfoGet();

                if (appInfo != null && appInfo.getVersionCode() > Utility.ApplicationVersionCode) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog alertDialog = new AlertDialog.Builder(Utility.context).create();
                            alertDialog.setCancelable(true);
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.setTitle(DIALOGBOX_TITLE);
                            alertDialog.setIcon(DIALOGBOX_ICON);
                            alertDialog.setMessage("New Verison of Hutch Connect App is Available. Please click on Update button to redirect to Play store.");
                            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Update",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            alertDialog.cancel();
                                            // If Application installed from playstore then open google playstore else download apk from link

                                            Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                                            i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.hutchsystems.hutchconnect"));
                                            Utility.context.startActivity(i);
                                        }
                                    });

                            // Disable cancel button more than 3 days
                            if (Utility.getDiffDay(currentDate, appInfo.getVersionDate()) <= 3 || !appInfo.isMandatoryUpdateFg()) {
                                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                UpdateCancel();
                                                alertDialog.cancel();

                                            }
                                        });
                            }
                            alertDialog.show();
                        }
                    });
                }
            }
        }).start();
    }

    // Created By: Deepak  Sharma
    // Created Date: 1 May 2020
    // Purpose: log record to server if driver cancel update
    private void UpdateCancel() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PostCall.AppUpdateCancel();
            }
        }).start();
    }

    // Created By: Deepak Sharma
    // Created Date: 09 July 2019
    // Purpose: initialize all controls present on main activity
    private void initializeControls() {
        ll_enginehours = findViewById(R.id.ll_enginehours);
        ll_drivingRemaning = findViewById(R.id.ll_drivingRemaning);

        icGPS = (ImageView) findViewById(R.id.icGPS);
        icGPS.setBackgroundResource(R.drawable.ic_flagbar_gps_on);
        icGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportIssue();
            }
        });
        //icGPS.setVisibility(View.GONE);
        icViolation = (ImageView) findViewById(R.id.icViolation);
        ivDrivingImage = (ImageView) findViewById(R.id.ivDrivingImage);

        icNetwork = (ImageView) findViewById(R.id.icNetwork);
        icNetwork.setVisibility(View.GONE);
        icWifi = (ImageView) findViewById(R.id.icWifi);
        icWifi.setVisibility(View.GONE);
        icWebService = (ImageView) findViewById(R.id.icWebService);
        icCanbus = (ImageView) findViewById(R.id.icCanbus);
        icCanbus.setBackgroundResource(R.drawable.ic_flagbar_canbus_disconnect);

        icCanbus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
         /*       ReportBean objReport = new ReportBean(getApplicationContext());
                String data = "@K,3671,214,2662,356961075172794,20200217220522,20200217220522,20200217220522,-122892835,49203869,226,101,249145,6,0,1,0,0,,2000,2000,\u001A,356961075172794,31,0,4,40,0,10,105,61,12,1,1,1,0,1,0,240,0,0,39,2,424,619658,14602703,";
                data = "@K,335A,240,11832,10000081000241000000,20200317022837,20200317022837,20200317022912,-121121443,49595684,262,115,18349,9,1,10,2,0,,2000,2000,\u001A,352009110507444,29,0,1200,40,0,10,139,54,10,1,5,1,0,1,0,0,0,0,188,13,5077,848381,16148892,842350D";
                objReport.ReceiveData(data);*/
                Utility.executeQuery();
            }
        });
        icBattery = (ImageView) findViewById(R.id.icBattery);
        icBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Utility.isInternetOn()) {
                    Utility.showMsg("Internet is not working!");
                    return;
                }

                LogFile.sendEld2020Data();
            }
        });
        icInspection = (ImageView) findViewById(R.id.icInspection);
        icMessage = (ImageView) findViewById(R.id.icMessage);
        icMessage.setBackgroundResource(R.drawable.ic_flagbar_message);
        icMessage.setVisibility(View.GONE);

        icTPMS = (ImageView) findViewById(R.id.icTPMS);

        icCertifyLog = (ImageView) findViewById(R.id.icCertify_Log);
        icCertifyLog.setVisibility(View.GONE);
        ivActiveUser = (ImageView) findViewById(R.id.ivDriver);

        icFreezeGPS = (ImageView) findViewById(R.id.icFreezeGPS);
        icFreezeGPS.setBackgroundResource(R.drawable.ic_flagbar_gps_on);
        icFreezeViolation = (ImageView) findViewById(R.id.icFreezeViolation);
        icFreezeViolation.setVisibility(View.GONE);
        icFreezeNetwork = (ImageView) findViewById(R.id.icFreezeNetwork);
        icFreezeNetwork.setVisibility(View.GONE);
        icFreezeWifi = (ImageView) findViewById(R.id.icFreezeWifi);
        icFreezeWifi.setVisibility(View.GONE);

        // backup database on click
        icFreezeGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportIssue();
            }
        });

        icFreezeWebService = (ImageView) findViewById(R.id.icFreezeWebService);
        icFreezeCanbus = (ImageView) findViewById(R.id.icFreezeCanbus);
        icFreezeCanbus.setBackgroundResource(R.drawable.ic_flagbar_canbus_disconnect);
        icFreezeCanbus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utility.executeQuery();
            }
        });
        icFreezeBattery = (ImageView) findViewById(R.id.icFreezeBattery);
        icFreezeInspection = (ImageView) findViewById(R.id.icFreezeInspection);
        icFreezeMessage = (ImageView) findViewById(R.id.icFreezeMessage);
        icFreezeMessage.setBackgroundResource(R.drawable.ic_flagbar_message);
        icFreezeMessage.setVisibility(View.GONE);

        icFreezeActiveUser = (ImageView) findViewById(R.id.icFreezeDriver);
        flagBar = (LinearLayout) findViewById(R.id.flagBar);
        flagBarFreeze = (LinearLayout) findViewById(R.id.flagBarFreeze);


        frameSpeedometer = (FrameLayout) findViewById(R.id.frameSpeedometer);


        frameRPM = (FrameLayout) findViewById(R.id.frameRPM);
        frameRPM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                    brightness -= 51;
                    if (brightness < 1) {
                        brightness = 1;
                    }

                    setBrightness(brightness);
                } catch (Exception exe) {
                }
            }
        });

        frameFuelLevel = (FrameLayout) findViewById(R.id.frameFuelLevel);
        frameFuelLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                    brightness += 51;
                    if (brightness > 255) {
                        brightness = 255;
                    }

                    setBrightness(brightness);
                } catch (Exception exe) {
                }
            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.findViewById(R.id.title);

        toolbar.setOnTouchListener(new View.OnTouchListener() {
            Rect hitrect = new Rect();

            public boolean onTouch(View v, MotionEvent event) {
                /*if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                    boolean hit = false;
                    for (int i = toolbar.getChildCount() - 1; i >= 0; i--) {
                        View view = toolbar.getChildAt(i);
                        if (view instanceof TextView) {
                            view.getHitRect(hitrect);
                            if (hitrect.contains((int) event.getX(), (int) event.getY())) {
                                hit = true;
                                break;
                            }
                        }
                    }
                }*/
                return false;
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);

                //Log.d(TAG, "State Changed");
                if (bLogin) {
                    drawer.closeDrawers();
                }
            }
        };
        toggle.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        originalToolbarListener = toggle.getToolbarNavigationClickListener();
        fragmentManager.addOnBackStackChangedListener(this);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //  navigationView.setNavigationItemSelectedListener(this);
        lvDrawer = (ListView) findViewById(R.id.lvDrawer);
        lvDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                onNavigationItemSelected(lstDrawerItems.get(position).getId());

            }
        });
        rlLoadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
        tvUserName = (TextView) navigationView.findViewById(R.id.tvUserName);
        tvFreeze = (RelativeLayout) findViewById(R.id.tvFreeze);
        tvLoginFreeze = (RelativeLayout) findViewById(R.id.tvLoginFreeze);
        tvGauge = (RelativeLayout) findViewById(R.id.layoutGauge);

        imgreezeSpeed = (ImageView) findViewById(R.id.imgreezeSpeed);
        imgreezeSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility._appSetting.getVisionMode() == 2) {
                    Utility.NightModeFg = !Utility.NightModeFg;
                    setUIMode();
                }
            }
        });

        imgFreezeRPM = (ImageView) findViewById(R.id.imgFreezeRPM);
        imgFreezeVoltage = (ImageView) findViewById(R.id.imgFreezeVoltage);
        imgFreezeCoolantTemp = (ImageView) findViewById(R.id.imgFreezeCoolantTemp);
        imgFreezeThrPos = (ImageView) findViewById(R.id.imgFreezeThrPos);


        tvOdometer = (LetterSpacingTextView) findViewById(R.id.tvOdometer);
        tvSpeed = (TextView) findViewById(R.id.tvSpeed);
        tvRPM = (TextView) findViewById(R.id.tvRPM);
        tvPosition = (TextView) findViewById(R.id.tvPosition);
        tvCoolant = (TextView) findViewById(R.id.tvCoolant);
        tvVoltage = (TextView) findViewById(R.id.tvVoltage);
        tvEngineHours = (LetterSpacingTextView) findViewById(R.id.tvEngineHours);
        tvDrivingRemainingFreeze = (LetterSpacingTextView) findViewById(R.id.tvDrivingRemainingFreeze);


        tvViolationDate = (TextView) findViewById(R.id.tvViolationDate);
        tvViolation = (TextView) findViewById(R.id.tvViolation);
        tvViolation.setSelected(true);
        tvViolation.setVisibility(View.GONE);
        tvViolationDate.setVisibility(View.GONE);


        if (Utility._appSetting.getViolationOnDrivingScreen() == 1) {
            tvViolation.setVisibility(View.VISIBLE);
        } else {
            tvViolation.setVisibility(View.INVISIBLE);
        }

        ImageView imgViolation = (ImageView) findViewById(R.id.imgViolation);
        imgViolation.setVisibility(View.GONE);

        layoutAlertMessage = (ConstraintLayout) findViewById(R.id.layoutAlertMessage);

        layoutAlertMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutAlertMessage.setVisibility(View.GONE);
            }
        });

        if (Utility.isLargeScreen(getApplicationContext())) {
            tvOdometer.setLetterSpacing(21);
            tvEngineHours.setLetterSpacing(18);
            tvDrivingRemainingFreeze.setLetterSpacing(22);

        } else {
            tvOdometer.setLetterSpacing(23);
            tvEngineHours.setLetterSpacing(20);
            tvDrivingRemainingFreeze.setLetterSpacing(24);

        }
        tvDrivingRemainingFreeze.setText("00:00:00");

        frameDiagnostic = (FrameLayout) findViewById(R.id.frameDiagnostic);
        frameMalfunction = (FrameLayout) findViewById(R.id.frameMalfunction);

        tvLoginName = (TextView) findViewById(R.id.tvLoginName);
        tvLoginName.setText(driverName);

        tvLoginName.setSelected(true);
        tvFreezeLoginName = (TextView) findViewById(R.id.tvFreezeLoginName);
        tvFreezeLoginName.setText(driverName);
        chkRules = (CheckBox) findViewById(R.id.chkRules);
        chkRules.setClickable(false);

        //violationLayout = (LinearLayout) findViewById(R.id.violationLayout);


        textToSpeech = new TextToSpeech(Utility.context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_flagbar_battery_full);
        batteryBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(batteryBmp);
        bmp.recycle();

        bindDrawerItem();

        // initalization of internet alert PCDialog
        initInternetAlertDialog();

        // initialize balance alert PCDialog
        //initBalanceAlertLayout();
    }

    @Override
    public void changeEventListner(String currenteventDate, Boolean isEdited, int driverId, int currentEventType, int currentEventCode) {

        if (currentEventType == 1 && currentEventCode >= 3) {
            Boolean teamDriver = Utility.user2.getAccountId() > 0;
            String splitEventDate = EventDB.splitSleepQualifiedFg(driverId, currentRule, currenteventDate, teamDriver);
            if (!splitEventDate.isEmpty() && !_SplitFg) {
                ShowSplitSleep(splitEventDate, driverId);
            }
        }

    }

    @Override
    public void onSpecialStatusChange(int specialStatusFg) {

        LogFile.ELD2020DataLog("Send Unit ID start: ");
        objHutchConnect.setUnitId(specialStatusFg);
        // show dialog while driver id is setting up
        onUnitMismatchDialog(true);
    }


    // For Split Sleep popup
    public void ShowSplitSleep(String date, int driverId) {
        if (Utility._appSetting.getEnableSplitSlip() == 1) {
            Log.d(TAG, "Split Slip Started");
            if (Utility._appSetting.getShowAlertSplitSlip() == 1) {
                ShowAlertforSplitSlip(date, driverId);

            } else {
                EventDB.EventUpdateForSplitSleep(date, driverId, 1, currentRule);

                if (elogFragment != null) {
                    elogFragment.refresh();
                } else {
                    ProcessViolation(true);
                }
            }
        }


    }

    private void ShowAlertforSplitSlip(final String eventDateTime, final int driverId) {
        final AlertDialog alertDialog = new AlertDialog.Builder(Utility.context).create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Split Sleep");
        alertDialog.setIcon(DIALOGBOX_ICON);
        alertDialog.setMessage(getString(R.string.split_sleep_alert));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, Utility.context.getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventDB.EventUpdateForSplitSleep(eventDateTime, driverId, 1, currentRule);
                        if (elogFragment != null) {
                            elogFragment.refresh();
                        }
                    }
                });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, Utility.context.getString(R.string.no),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        alertDialog.show();
    }


    @Override
    public void setVisionMode() {
        if (Utility._appSetting.getVisionMode() == 0) {
            Utility.NightModeFg = false;

        } else if (Utility._appSetting.getVisionMode() == 1) {
            Utility.NightModeFg = true;
        }
        setUIMode();
    }


    AlertMonitor alertMonitor;
    ConstraintLayout layoutAlertBTB;
    TextView tvAlertHeader, tvAlertMessage;
    View vAlertBorder;
    ImageView imgAlertIcon;
    Button btnFixBTB;

    private void exitApp() {
        System.exit(0);
    }

    // purpose initialize PCDialog for BTB alert
    private void initializeBTBAlertDialog() {
        layoutAlertBTB = (ConstraintLayout) findViewById(R.id.layoutAlertBTB);

        btnFixBTB = (Button) findViewById(R.id.btnFixBTB);
        btnFixBTB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentScreen == FIX_BTB)
                    return;
                fixBTB();
            }
        });
        imgAlertIcon = (ImageView) findViewById(R.id.imgIcon);
        imgAlertIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportIssue();
            }
        });
        vAlertBorder = findViewById(R.id.vBorder);
        tvAlertHeader = (TextView) findViewById(R.id.tvHeader);
        tvAlertMessage = (TextView) findViewById(R.id.tvMessage);
       /* if (CanMessages.mState != CanMessages.STATE_CONNECTED && !ConstantFlag.Flag_Development && !undockingMode) {
            onAlertWarning();
        }*/
    }

    public void fixBTB() {
        if (ConstantFlag.HutchConnectFg) {
            if (!adapter.isEnabled()) {
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn, REQUEST_ENABLE_BT);
                return;
            }

            String address = CanMessages.deviceAddress;
            objHutchConnect.connect(address);
            //Connecting Dialog
            objHutchConnect.showConnectionMessage();
        }
    }

    public void onAlertError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                imgAlertIcon.setImageResource(R.drawable.ic_error_outline_red_36dp);
                vAlertBorder.setBackgroundColor(getResources().getColor(R.color.red1));
                tvAlertHeader.setTextColor(getResources().getColor(R.color.red1));

                tvAlertHeader.setText(R.string.btb_connection_title);
                //tvAlertMessage.setText(R.string.btb_connection_message);

                // set layout visible
                layoutAlertBTB.setVisibility(View.VISIBLE);
            }
        });

    }


    @Override
    public void onLoadNotificationList() {
        isOnDailyLog = false;
        bInspectDailylog = false;

        replaceFragment(NotificationFragment.newInstance());
        previousScreen = currentScreen;
        currentScreen = Notification;
        title = getString(R.string.notification_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }

    @Override
    public void onLoadTicketHistory() {
        isOnDailyLog = false;
        bInspectDailylog = false;

        replaceFragment(TicketHistoryListFragment.newInstance());
        previousScreen = currentScreen;
        currentScreen = TicketHistoryList;
        title = getString(R.string.ticket_history);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);

    }

    @Override
    public void onAlertWarning() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    imgAlertIcon.setImageResource(R.drawable.ic_flagbar_canbus_disconnect);
                    vAlertBorder.setBackgroundColor(getResources().getColor(R.color.red1));
                    tvAlertHeader.setTextColor(getResources().getColor(R.color.red1));

                    Utility.HutchConnectStatusFg = false;
                    CanMessages.mState = CanMessages.STATE_LISTEN;

                    if (Utility._appSetting.getECUConnectivity() == 1)
                        btnFixBTB.setVisibility(View.VISIBLE);
                    else
                        btnFixBTB.setVisibility(View.GONE);

                    tvAlertHeader.setText(R.string.btb_connection_in_progress);
                    tvAlertMessage.setText(R.string.please_wait_while_eld_is_connecting_to_btb);


                    layoutAlertBTB.setVisibility(View.VISIBLE);

                } catch (Exception exe) {
                }
            }
        });
    }

    @Override
    public void onAlertVehicleStart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                imgAlertIcon.setImageResource(R.drawable.ic_flagbar_violation_upcoming);
                vAlertBorder.setBackgroundColor(getResources().getColor(R.color.yellow2));
                tvAlertHeader.setTextColor(getResources().getColor(R.color.yellow3));

                tvAlertHeader.setText(R.string.ignition_on_title);
                tvAlertMessage.setText(R.string.ignition_on_message);

                layoutAlertBTB.setVisibility(View.VISIBLE);
                btnFixBTB.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onAlertClear() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutAlertBTB.setVisibility(View.GONE);
                btnFixBTB.setVisibility(View.VISIBLE);

            }
        });

    }

    @Override
    public void connectMacAddressWithBluetooth(final String macAddress, final String serialNo) {

        try {
            toolbar.setVisibility(View.GONE);
            fragmentManager.popBackStack();
        } catch (Exception exe) {

        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                // if bluetooth is already connected then first disconnect it and try to connect to other vehicle
              /*  if (Utility.HutchConnectStatusFg) {
                    objHutchConnect.disconnect();

                    // wait for 3 seconds after disconnecting
                    try {

                        Thread.sleep(3000);
                    } catch (Exception exe) {

                    }
                }*/

                objHutchConnect.serialNo = serialNo;
                objHutchConnect.macAddress = macAddress;
                // Connect with atrack blutooth device
                objHutchConnect.connect(macAddress);
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkConnectivity();
        isAppActive = true;
        // execute below code on first startup
        if (Utility.vehicleId == 0) {
            initActivity();
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 9 July 2019
    // Purpose: Connect to hutch connect device
    private void initHutchConnect() {

        // Connect Via Bluetooth
        if (Utility._appSetting.getECUConnectivity() == 1) {
            initializeBluetooth();
        } else if (Utility._appSetting.getECUConnectivity() == 2) // Connect Via USB
        {
            initializeUSB();
        }

        HutchConnectThread();
    }

    // Created By: Deepak Sharma
    // Created Date: 9 July 2019
    // Purpose: check permissions and load activity
    private void initActivity() {

        ArrayList<PermissionBean> permission = Utility.hasAllPermissions();

        // OPEN FIRST ACTIVITY IF PERMISSIONS ARE NOT PRESENT
        if (permission.size() > 0) {
            startActivityForResult(new Intent(getApplicationContext(), FirstActivity.class), REQUEST_FIRST_ACTIVITY);
        } else if (Utility.getPreferences("firstrun", true)) {

            startActivity(new Intent(getApplicationContext(), SetupActivity.class));
        } else {
            // if data synced already then load company configuration
            DiagnosticMalfunction.getDiagnosticIndicator();

            // load company info
            CarrierInfoDB.getCompanyInfo();

            Utility._CurrentDateTime = Utility.getCurrentDateTime();

            Utility.unIdentifiedDriverId = LoginDB.getUnidentifiedDriverId();

            // check status of company, vehicle, device and unidentified driver
            boolean deviceStatus = checkDeviceStatus();

            if (deviceStatus) {
                currentInspectionDate = Utility.newDateOnly();
                currentDate = Utility.newDateOnly();
                firstLogin = true;

                checkConnectivity();

                initHutchConnect();

                initLoginFragment();
            } else {
                startActivityForResult(new Intent(getApplicationContext(), FirstActivity.class), REQUEST_FIRST_ACTIVITY);
            }
        }

    }


    // Created By: Deepak Sharma
    // Created Date: 10 July 2019
    // Purpose: check status of company, vehicle, device and unidentified driver
    private boolean checkDeviceStatus() {

        boolean status = (!CarrierInfoDB.duplicateDeviceAssignment() && Utility.CompanyStatusId == 1 && Utility.VehicleStatusId == 1 && Utility.DeviceStatus == 1 && !Utility.LatePaymentFg);

        return status;
    }


    static boolean isAppActive = false;

    @Override
    public void onPause() {

        // pauseGforce();
        isAppActive = false;
        super.onPause();
    }

    @Override
    public void onBackPressed() {
      /*  if (Utility.InspectorModeFg) {
            return;
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (undockingMode) {
                return;
            }

            //super.onBackPressed();
            if (previousScreen != -1) {
                if (currentScreen == Login_Screen) {
                    toolbar.setVisibility(View.VISIBLE);
                    flagBar.setVisibility(View.VISIBLE);
                    toggle.setDrawerIndicatorEnabled(true);
                }
                setDrawerState(true);

                currentScreen = previousScreen;
                if (previousScreen == Unidentified_Event_Screen) {
                    ArrayList<EventBean> unAssignedEventList = EventDB.EventUnAssignedGet();
                    if (unAssignedEventList.size() > 0) {
                        isOnDailyLog = false;
                        bInspectDailylog = false;
                        replaceFragment(UnidentifyFragment.newInstance());
                        title = getApplicationContext().getResources().getString(R.string.title_unidentified_event);

                        previousScreen = -1;
                    }
                    return;
                } else if (previousScreen == Message) {
                    fragmentManager.popBackStack();
                    previousScreen = -1;

                } else if (previousScreen == DVIR) {
                    fragmentManager.popBackStack();
                    previousScreen = -1;
                    getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_dvir));
                } else {
                    bLogin = false;
                    isOnDailyLog = true;
                    bInspectDailylog = false;
                    if (elogFragment == null) {
                        elogFragment = new ELogFragment();
                    }
                    replaceFragment(elogFragment);
                    title = getSupportActionBar().getTitle().toString();
                    previousScreen = -1;
                }
                getSupportActionBar().setTitle(title);
                invalidateOptionsMenu();

                drawer.closeDrawer(GravityCompat.START);
            } else {
                return;
            }

        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //MenuItem item = menu.findItem(R.id.action_set_active);
        //MenuItem editItem = menu.findItem(R.id.action_edit_event);
        //MenuItem backItem = menu.findItem(R.id.action_back_edit_event);
        MenuItem confirmItem = menu.findItem(R.id.action_confirm_web_event);
        MenuItem rejectItem = menu.findItem(R.id.action_reject_web_event);
      /*  MenuItem backOneDayItem = menu.findItem(R.id.action_back_one_day);
        MenuItem forwardOneDayItem = menu.findItem(R.id.action_forward_one_day);*/
        MenuItem malfunctionItem = menu.findItem(R.id.action_malfunction);
        MenuItem diagnosticItem = menu.findItem(R.id.action_diagnostic);
        MenuItem inspectorModeItem = menu.findItem(R.id.action_inspector_mode);
        MenuItem sendItem = menu.findItem(R.id.action_send);
        MenuItem dashboard = menu.findItem(R.id.action_dashboard);
        MenuItem wifiHotspot = menu.findItem(R.id.action_wifi_hotspot);

        inspectorModeItem.setCheckable(false);
        inspectorModeItem.setVisible(Utility.InspectorModeFg);

        if (Utility.motionFg || ((Utility.user1.isActive() && Utility.user1.isOnScreenFg()) || (Utility.user2.isActive() && Utility.user2.isOnScreenFg()))) {
            //item.setIcon(R.drawable.ic_driver_active);
            //item.setEnabled(false);
            if (ivActiveUser != null) {
                ivActiveUser.setBackgroundResource(R.drawable.ic_flagbar_driver_active);
            }
            if (icFreezeActiveUser != null) {
                icFreezeActiveUser.setBackgroundResource(R.drawable.ic_flagbar_driver_active);
            }
            //item.setVisible(false);
        }

        if (Utility.dataDiagnosticIndicatorFg) {
            diagnosticItem.setVisible(true);
        } else {
            diagnosticItem.setVisible(false);
        }

        if (Utility.malFunctionIndicatorFg) {
            malfunctionItem.setVisible(true);
        } else {
            malfunctionItem.setVisible(false);
        }

        if (Utility.InspectorModeFg) {
            malfunctionItem.setVisible(false);
            diagnosticItem.setVisible(false);
            sendItem.setVisible(true);
            dashboard.setVisible(false);
        } else {
            sendItem.setVisible(true);
            dashboard.setVisible(true);
        }

        if (!ConstantFlag.ELDFg) {
            malfunctionItem.setVisible(false);
            diagnosticItem.setVisible(false);
            sendItem.setVisible(false);
        }

        wifiHotspot.setVisible(!Utility.isInternetOn());


        if (bEditEvent) {

            if (bWebEvent) {
                confirmItem.setVisible(true);
                rejectItem.setVisible(true);
            } else {
                confirmItem.setVisible(false);
                rejectItem.setVisible(false);
            }

        } else {
            confirmItem.setVisible(false);
            rejectItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.home) {
            //Log.d(TAG, "Works here 1!");
        } else if (id == R.id.action_diagnostic) {
            final Dialog dlg = new Dialog(MainActivity.this);
            LayoutInflater li = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = li.inflate(R.layout.listview_dialog, null, false);
            dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dlg.setContentView(view);

            TextView tvTitle = (TextView) dlg.findViewById(R.id.tvTitle);
            tvTitle.setText(getString(R.string.data_diagnostic));

            ImageButton imgCancel = (ImageButton) dlg.findViewById(R.id.imgCancel);
            imgCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dlg.dismiss();
                }
            });

            ListView listView = (ListView) dlg.findViewById(R.id.lvDiagnosticMalfunctionEvent);
            DiagnosticMalfunctionAdapter eventAdapter = new DiagnosticMalfunctionAdapter(MainActivity.this, DiagnosticMalfunction.getActiveDiagnosticMalfunctionList(3)); // code 3 means diagnostic
            listView.setAdapter(eventAdapter);
            dlg.show();
        } else if (id == R.id.action_malfunction) {
            final Dialog dlg = new Dialog(MainActivity.this);
            LayoutInflater li = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = li.inflate(R.layout.listview_dialog, null, false);
            dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dlg.setContentView(view);

            TextView tvTitle = (TextView) dlg.findViewById(R.id.tvTitle);
            tvTitle.setText(getString(R.string.malfunction));

            ImageButton imgCancel = (ImageButton) dlg.findViewById(R.id.imgCancel);
            imgCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dlg.dismiss();
                }
            });
            ListView listView = (ListView) dlg.findViewById(R.id.lvDiagnosticMalfunctionEvent);
            DiagnosticMalfunctionAdapter eventAdapter = new DiagnosticMalfunctionAdapter(MainActivity.this, DiagnosticMalfunction.getActiveDiagnosticMalfunctionList(1));// code 1 means malfunction
            listView.setAdapter(eventAdapter);
            dlg.show();
        } else if (id == R.id.action_reject_web_event) {
            if (undockingMode) {
                return super.onOptionsItemSelected(item);
            }
            bEditEvent = false;
            bWebEvent = false;
            invalidateOptionsMenu();
            int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
            int eventId = selectedEvent.get_id();

            EventDB.EventUpdate(eventId, 4, driverId, selectedEvent.getDailyLogId());
        } else if (id == R.id.action_confirm_web_event) {
            if (undockingMode) {
                return super.onOptionsItemSelected(item);
            }
            bEditEvent = false;
            bWebEvent = false;
            invalidateOptionsMenu();

            int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();

            int eventId = EventDB.getEventId(driverId, selectedEvent.getCreatedDate(), 1);
            EventDB.EventUpdate(eventId, 2, driverId, selectedEvent.getDailyLogId());

            eventId = selectedEvent.get_id();
            //  EventDB.EventCopy(eventId, bean.getEventRecordOrigin(), 1, driverId, bean.getDailyLogId());
            EventDB.EventUpdate(eventId, 1, driverId, selectedEvent.getDailyLogId());
            DailyLogDB.DailyLogCertifyRevert(driverId, selectedEvent.getDailyLogId());
        } else if (id == R.id.action_send) {
            if (undockingMode) {
                return super.onOptionsItemSelected(item);
            }

            outputFileDialog = new OutputFileSendDialog();
            outputFileDialog.show(getSupportFragmentManager(), "outputfile_dialog");
        } else if (id == R.id.action_dashboard) {
            int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
            ArrayList<DailyLogBean> logList = DailyLogDB.getUncertifiedDailyLog(driverId);
            if (logList.size() > 0) {
                bHaveLogbookToCertify = true;
                ShowUncertifiedRecord();
            } else {
                callELog();
            }

        } else if (id == R.id.action_wifi_hotspot) {
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
        /*else if (id == R.id.action_motion) {
            if (CanMessages.Speed == "0") {
                Log.d(TAG, "Moving");
                Utility.motionFg = true;
                CanMessages.Speed = "80";
                CanMessages.RPM = "1000";
            } else {
                Log.d(TAG, "STOP");
                Utility.motionFg = false;
                CanMessages.Speed = "0";
            }
        } else if (id == R.id.action_PowerOnOff) {
            if (CanMessages.RPM == "0") {
                Log.d(TAG, "Power On");
                CanMessages.Speed = "0";
                Utility.motionFg = false;
                CanMessages.RPM = "1000";
            } else {
                Log.d(TAG, "Power Off");
                CanMessages.Speed = "0";
                CanMessages.RPM = "0";
                Utility.motionFg = false;
            }
        }*/
        /* else if (id == R.id.action_change_rule) {
            if (isOnDailyLog) {
                ELogFragment.newInstance().launchRuleChange();
            }
        } else if (id == R.id.action_certify) {
            if (isOnDailyLog) {
                ELogFragment.newInstance().launchCertifyLog();
            }
        }*/


        return super.onOptionsItemSelected(item);
    }

    public void initInternetAlertDialog() {
        layoutAlertForInternet = (LinearLayout) findViewById(R.id.layoutAlertForInternet);
        viewColour = (View) findViewById(R.id.viewColour);
        ivalert = (ImageView) findViewById(R.id.ivalert);
        tvalertMessage = (TextView) findViewById(R.id.tvalertMessage);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.daily_log) {
            if (undockingMode) {
                replaceFragment(new DockingFragment());
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
            isOnDailyLog = true;
            bInspectDailylog = false;
            if (elogFragment == null) {
                elogFragment = new ELogFragment();
            }
            replaceFragment(elogFragment);
            title = getSupportActionBar().getTitle().toString();
            previousScreen = currentScreen;
            currentScreen = DailyLog_Screen;
            // Handle the camera action
        } else if (id == R.id.inspect_dailylog) {
            if (undockingMode) {
                return true;
            }
            bInspectDailylog = true;
            bEditEvent = false;
            isOnDailyLog = false;
           /* if (inspectFragment == null) {
                inspectFragment = new DetailFragment();
            }*/

            replaceFragment(InspectLogFragment.newInstance());
            previousScreen = currentScreen;
            currentScreen = Inspect_DailyLog_Screen;
            title = getApplicationContext().getResources().getString(R.string.title_inspect_elog);
            title += Utility.getStringCurrentDate();
        } else if (id == R.id.create_event) {
            if (undockingMode) {
                return true;
            }
            Bundle bundle = new Bundle();
            bundle.putInt("current_status", currentDutyStatus);

            NewEventFragment fragment = NewEventFragment.newInstance();
            fragment.setArguments(bundle);
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(fragment);
            previousScreen = currentScreen;
            currentScreen = New_Event_Screen;
            title = getApplicationContext().getResources().getString(R.string.menu_create_event);
        } else if (id == R.id.violation_history) {
            if (undockingMode) {
                return true;
            }
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(ViolationFragment.newInstance());
            previousScreen = currentScreen;
            currentScreen = Violation_History_Screen;
            title = getApplicationContext().getResources().getString(R.string.menu_violation_history);
        } else if (id == R.id.uncertified_logbook) {
            if (undockingMode) {
                return true;
            }
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(UnCertifiedFragment.newInstance());
            previousScreen = currentScreen;
            currentScreen = Uncertified_LogBook_Screen;
            title = getApplicationContext().getResources().getString(R.string.title_certify_log_book);
        } else if (id == R.id.Unidentified) {
            if (undockingMode) {
                return true;
            }
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(UnidentifyFragment.newInstance());
            previousScreen = currentScreen;
            currentScreen = Unidentified_Data_Screen;
            title = getApplicationContext().getResources().getString(R.string.menu_unidentified_event);
        } else if (id == R.id.edit_request) {
            if (undockingMode) {
                return true;
            }
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(ModifiedFragment.newInstance());
            previousScreen = currentScreen;
            currentScreen = Edit_Request_Screen;
            title = getApplicationContext().getResources().getString(R.string.menu_edit_request);
        } else if (id == R.id.send_report) {
            LogFile.sendLogFile(LogFile.AFTER_MID_NIGHT);

        } else if (id == R.id.dvir) {
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(DvirFragment.newInstance());
            previousScreen = currentScreen;
            currentScreen = DVIR;
            title = getApplicationContext().getResources().getString(R.string.menu_dvir);
        } else if (id == R.id.message) {
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(UserListFragment.newInstance());
            previousScreen = currentScreen;
            currentScreen = Message;
            title = getApplicationContext().getResources().getString(R.string.menu_Message);
        } else if (id == R.id.logout) {
            if (!Utility.InspectorModeFg) {
                if (!Utility.HutchConnectStatusFg) {
                    Utility.showAlertMsg("You are not allowed to logout until bluetooth is disconnected!");
                    return true;
                }

                int currentStatus = EventDB.getCurrentDutyStatus(Utility.onScreenUserId);
                if (currentStatus != 1) {
                    Utility.showAlertMsg(getString(R.string.logout_offduty_alert));
                    return true;
                }
            }

            boolean unCertifyFg = DailyLogDB.getUncertifiedLogFg(Utility.onScreenUserId);
            String message = "";

            if (unCertifyFg && !Utility.InspectorModeFg) {
                message = getString(R.string.uncertified_log_message);
            }
            final AlertDialog ad = new AlertDialog.Builder(this)
                    .create();
            ad.setCancelable(true);
            ad.setCanceledOnTouchOutside(false);
            ad.setTitle(getString(R.string.logout_confirmation));
            ad.setIcon(R.drawable.ic_launcher);
            ad.setMessage(getString(R.string.logout_alert) + message);
            ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            isOnDailyLog = false;
                            if (Utility.InspectorModeFg) {
                                RedirectToLogin(Utility.user2.isOnScreenFg());
                                setInspectorMode(false);
                            } else
                                Logout();
                        }
                    });
            ad.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            ad.cancel();
                        }
                    });
            ad.show();
        } else if (id == R.id.exit) {
            exitApp();
        }

        invalidateOptionsMenu();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
        return true;
    }


    @Override
    public void switchToAllUnitNo() {
        layoutAlertBTB.setVisibility(View.GONE);
        isOnDailyLog = false;
        bInspectDailylog = false;
        toolbar.setVisibility(View.VISIBLE);
        replaceFragmentWithBackStack(UnitNoListFragment.newInstance());
        previousScreen = currentScreen;
        currentScreen = UnitNoList;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Serial");
    }
 /*   @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            updateTitle = true;
            tvFreeze.setBackgroundResource(0);
            if (tvFreeze != null) {
                if (Utility.activeUserId == 0)
                    tvFreeze.setBackgroundResource(R.drawable.login_freeze);
                else {
                    setUIMode();
                }
            }
        } catch (Exception exe) {
        }
    }*/

    boolean updateTitle = true;

    private void replaceFragment(Fragment fragment) {

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            // fragmentManager.popBackStack();
            updateTitle = false;
            if (currentScreen == Inspect_DailyLog_Screen || currentScreen == DailyLog_Screen) {
                updateTitle = true;
            }

            try {
                Utility.hideKeyboard(MainActivity.this, MainActivity.this.getCurrentFocus());
            } catch (Exception exe) {
            }
        }

        FragmentManager manager = fragmentManager;
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.container, fragment);
        ft.commitAllowingStateLoss();
    }

    private void replaceFragmentWithBackStack(Fragment fragment) {
        try {
            Utility.hideKeyboard(MainActivity.this, MainActivity.this.getCurrentFocus());


            String backStateName = fragment.getClass().getName();

            FragmentManager manager = fragmentManager;
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.container, fragment);
            //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack(backStateName);
            ft.commit();
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }


    @Override
    public void setDutyStatus(int status) {
        currentDutyStatus = status;
        if (Utility.onScreenUserId == Utility.activeUserId) {
            activeCurrentDutyStatus = status;
            if (status != 3) {
                bSaveDriving = false;
            }
        }
    }

    @Override
    public void setActiveDutyStatus(int status) {
        if (Utility.activeUserId == Utility.onScreenUserId)
            activeCurrentDutyStatus = status;

        if (status != 3) {
            bSaveDriving = false;
        }
        //Log.d(TAG, "setActiveDutyStatus activeCurrentDutyStatus=" + activeCurrentDutyStatus);
    }

    @Override
    public void updateTitle(String date) {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getApplicationContext().getResources().getString(R.string.title_inspect_elog));
    }

    @Override
    public void setTotalDistance(int total) {
        totalDistance = total;
    }

    @Override
    public void setStartOdoMeter(int odo) {
        startOdometer = odo;
    }

    @Override
    public void setStartEngineHour(int value) {
        startHourEngine = value;
    }

    @Override
    public void onEditEventSaved() {
        // Check annotation selected is DVIR or fuel
        if (isDVIR || iSFueling) {
            displayAnnotation();
        } else if (bInspectDailylog) {
            loadInspectDailylog();
        } else {
            loadDailyLog();
        }
    }

    @Override
    public void onEditEventFinished() {
        if (bInspectDailylog) {
            loadInspectDailylog();
        } else {
            loadDailyLog();
        }
    }

    @Override
    public void onNewEventSaved() {
        //complete Create Event -> load ElogFragment
        // Chck annotation selected is DVIR or fuel
        if (isDVIR || iSFueling) {
            displayAnnotation();
        } else {
            loadDailyLog();
        }
    }

    @Override
    public void onNewEventFinished() {
        //complete Create Event -> load ElogFragment
        loadDailyLog();
    }

    @Override
    public void onAssumeRecord() {
        if (firstLogin) {
            //Log.d(TAG, "first login");
            if (bHaveLogbookToCertify) {

                ShowUncertifiedRecord();
              /*  replaceFragment(new UnCertifiedFragment());
                previousScreen = Unidentified_Event_Screen;
                currentScreen = Uncertified_LogBook_Screen;
                getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_certify_log_book));*/
            } else {
                //Log.d(TAG, "relogin");
                if (elogFragment == null) {
                    elogFragment = new ELogFragment();
                }
                elogFragment.setFirstLogin(firstLogin);
                replaceFragment(elogFragment);

                previousScreen = Unidentified_Event_Screen;
                currentScreen = DailyLog_Screen;
                isOnDailyLog = true;
                firstLogin = false;
            }
        } else {
            //relogin
            //Log.d(TAG, "relogin");
            if (elogFragment == null) {
                elogFragment = new ELogFragment();
            }
            replaceFragment(elogFragment);

            previousScreen = Unidentified_Event_Screen;
            currentScreen = DailyLog_Screen;
            isOnDailyLog = true;
        }

    }

    @Override
    public void onSkipAssumeRecord() {

        if (firstLogin) {
            //Log.d(TAG, "first login");
            if (bHaveLogbookToCertify) {
                ShowUncertifiedRecord();
                /*replaceFragment(new UnCertifiedFragment());
                previousScreen = Unidentified_Event_Screen;
                currentScreen = Uncertified_LogBook_Screen;
                getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_certify_log_book));*/
            } else {
                //Log.d(TAG, "relogin");
                if (elogFragment == null) {
                    elogFragment = new ELogFragment();
                }
                elogFragment.setFirstLogin(firstLogin);
                replaceFragment(elogFragment);
                previousScreen = Unidentified_Event_Screen;
                currentScreen = DailyLog_Screen;
                isOnDailyLog = true;
                firstLogin = false;
            }
        } else {
            //relogin
            //Log.d(TAG, "relogin");
            if (elogFragment == null) {
                elogFragment = new ELogFragment();
            }
            replaceFragment(elogFragment);
            previousScreen = Unidentified_Event_Screen;
            currentScreen = DailyLog_Screen;
            isOnDailyLog = true;
        }


    }

    @Override
    public void onLogbookCertified() {
        //relogin
        //Log.d(TAG, "relogin");
        if (elogFragment == null) {
            elogFragment = new ELogFragment();
        }
        elogFragment.setFirstLogin(firstLogin);
        replaceFragment(elogFragment);
        previousScreen = Uncertified_LogBook_Screen;
        currentScreen = DailyLog_Screen;

        isOnDailyLog = true;
        firstLogin = false;
    }

    @Override
    public void onSkipCertify() {
        //relogin
        //Log.d(TAG, "relogin");
        if (elogFragment == null) {
            elogFragment = new ELogFragment();
        }
        elogFragment.setFirstLogin(firstLogin);
        replaceFragment(elogFragment);
        previousScreen = Uncertified_LogBook_Screen;
        currentScreen = DailyLog_Screen;

        isOnDailyLog = true;
        firstLogin = false;
    }

    private void loadDailyLog() {
        //fragmentManager.beginTransaction()
        //        .replace(R.id.container, ELogFragment.newInstance())
        //        .commit();

        isOnDailyLog = true;
        bInspectDailylog = false;
        if (elogFragment == null) {
            elogFragment = new ELogFragment();
        }
        firstLogin = false;
        elogFragment.setFirstLogin(firstLogin);
        replaceFragment(elogFragment);
        previousScreen = currentScreen;
        currentScreen = DailyLog_Screen;
        title = getApplicationContext().getResources().getString(R.string.title_daily_log);
        title += " - " + Utility.convertDate(Utility.newDate(), CustomDateFormat.d10);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }

    private void loadInspectDailylog() {
        bInspectDailylog = true;
        bEditEvent = false;
        isOnDailyLog = false;
        /*if (inspectFragment == null) {
            inspectFragment = new DetailFragment();
        }*/
        InspectLogFragment inspectFragment = InspectLogFragment.newInstance();
        // set date if previous date event edited from inspect dailylog
        if (selectedEvent != null) {
            invalidateOptionsMenu();
            int position = Utility.getDiffDay(selectedEvent.getEventDateTime(), Utility.getCurrentDateTime()); //Utility.dateOnlyGet(selectedEvent.getEventDateTime());
            inspectFragment = InspectLogFragment.newInstance(position);
        }
        replaceFragment(inspectFragment);
        previousScreen = currentScreen;
        currentScreen = Inspect_DailyLog_Screen;
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_inspect_elog));
    }

    // Created By: Deepak Sharma
    // Created Date: 11 August 2016
    // Purpose: logout all user before install
    private void LogoutAllOnInstall() {
        int driverId = Utility.user1.getAccountId();
        int coDriverId = Utility.user2.getAccountId();

        // logout coDriver if any
        if (coDriverId != 0) {
            Logout(coDriverId);
            Utility.user2 = new UserBean();
        }

        // logout driver if any
        if (driverId != 0) {
            Logout(driverId);
            Utility.user1 = new UserBean();
        }

        Utility.activeUserId = Utility.onScreenUserId = 0;
        // disconnect chat server when both of the users are offline
        ChatClient.disconnect();
        finish();
    }

    // Created By: Deepak Sharma
    // Created Date: 11 August 2016
    // Purpose: logout user set common cached user data manually after executing this method
    private void Logout(int userId) {

        // make  driver offline on chat server
        MessageBean bean = MessageDB.CreateMessage(Utility.IMEI, userId, userId, "Disconnect");
        MessageDB.Send(bean);

        int logId = DailyLogDB.getDailyLog(userId, Utility.getCurrentDate());

        // get co driver record primary key
        int cId = DailyLogDB.getCoDriver(userId);
        if (cId > 0) {
            DailyLogDB.AddDriver(0, 0, cId);
        }

        if (ConstantFlag.ELDFg) {
            EventDB.EventCreate(Utility.getCurrentDateTime(), 5, 2, getString(R.string.logout_event_description), 1, 1, logId, userId, "", currentRule);
        }
    }

    private void Logout() {

        // make  driver offline on chat server
        MessageBean bean = MessageDB.CreateMessage(Utility.IMEI, Utility.onScreenUserId, Utility.onScreenUserId, "Disconnect");
        MessageDB.Send(bean);

        int driverId = Utility.user1.getAccountId();


        // get co driver record primary key
        int cId = DailyLogDB.getCoDriver(driverId);
        if (cId > 0) {
            DailyLogDB.AddDriver(0, 0, cId);
        }

        // create event on logout
        driverId = Utility.onScreenUserId;

        int logId = DailyLogDB.getDailyLog(driverId, Utility.getCurrentDate());
        EventDB.EventCreate(Utility.getCurrentDateTime(), 5, 2, getString(R.string.logout_event_description), 1, 1, logId, driverId, "", currentRule);
        DailyLogDB.DailyLogCertifyRevert(driverId, logId);

        if (Utility.user1.isOnScreenFg()) {
            if (Utility.user2.getAccountId() > 0) {
                Utility.user1 = Utility.user2;
                Utility.user1.setActive(true);
                Utility.user1.setOnScreenFg(true);
                Utility.activeUserId = Utility.onScreenUserId = Utility.user1.getAccountId();
                Utility.user2 = new UserBean();
                RedirectToMain();
                Utility.saveLoginInfo(Utility.user1.getAccountId(), 0, Utility.activeUserId, Utility.onScreenUserId);
            } else {
                Utility.user1 = new UserBean();
                Utility.activeUserId = Utility.onScreenUserId = 0;
                RedirectToLogin(false);
                // disconnect chat server when both of the users are offline
                ChatClient.disconnect();
                Utility.saveLoginInfo(0, 0, Utility.activeUserId, Utility.onScreenUserId);
            }
        } else {
            Utility.user2 = new UserBean();
            Utility.user1.setActive(true);
            Utility.user1.setOnScreenFg(true);
            Utility.activeUserId = Utility.onScreenUserId = Utility.user1.getAccountId();
            RedirectToMain();
            Utility.saveLoginInfo(Utility.user1.getAccountId(), 0, Utility.activeUserId, Utility.onScreenUserId);
        }

        if (Utility.isInternetOn()) {
            showLoaderAnimation(true);
            new AutoSyncData(autoSyncDataPostTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        // set Unit Id
        driverId = Utility.activeUserId == 0 ? Utility.unIdentifiedDriverId : Utility.activeUserId;

        int coDriverId = 0;

        if (Utility.user2.getAccountId() > 0)
            // set co driverid
            coDriverId = Utility.user1.getAccountId() == driverId ? Utility.user2.getAccountId() : Utility.user1.getAccountId();

        if (Utility.activeUserId == 0) {
            CarrierInfoDB.getCompanyInfo();
        }

        // set unit id
        objHutchConnect.setUnitId(0);

        // show dialog while driver id is setting up
        onUnitMismatchDialog(true);
    }

    private void RedirectToMain() {
        loginSuccessfully(false);
    }

    private void RedirectToLogin(boolean coDriver) {
        elogFragment = null;
        setDrawerState(false);
        toolbar.setVisibility(View.GONE);
        //   flagBar.setVisibility(View.GONE);

        if (coDriver) {
            previousScreen = DailyLog_Screen;
        } else {
            previousScreen = -1;
        }
        currentScreen = Login_Screen;

        isOnDailyLog = false;
        bInspectDailylog = false;


        loginFragment = new LoginFragment();

        Bundle bun = new Bundle();
        bun.putBoolean("CoDriverFg", coDriver);
        bun.putBoolean("InspectorModeFg", Utility.InspectorModeFg);
        loginFragment.setArguments(bun);
        replaceFragment(loginFragment);
    }

    // Switch driver
    private void SwitchDriver() {


        // prevent co driver from switching to driver if driver is driving vehicle
        if (Utility.motionFg) {
            Utility.showAlertMsg("ELD does not allow driver to switch while vehicle is moving!");
            return;
        }

        RedirectToLogin(Utility.user1.isOnScreenFg());
    }

    public void onFragmentInteraction(Uri id) {
        //you can leave it empty
    }

    @Override
    public void onAddFuel() {

        previousScreen = Fuel;

        replaceFragmentWithBackStack(FuelDetailFragment.newInstance());
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_new_Fuel));
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 1 May 2019
    // Purpose:  display Fuel fragment when annotation selected as fuel
    public void displayFuel() {

        previousScreen = Fuel;
        replaceFragment(FuelDetailFragment.newInstance());
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_new_Fuel));
    }


    @Override
    public void onViewFuel(int id) {

        previousScreen = Fuel;
        replaceFragmentWithBackStack(FuelDetailFragment.newInstance(id));
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_new_Fuel));
    }

    public static int currentRule = 1;

    // Created By: Pallavi
    // Created Date: 2 April
    // Purpose: set Dashboard Depends on Settings

    public void showDashboard() {
        //   tvGauge.setVisibility(View.VISIBLE);
        if (Utility._appSetting.getDashboardDesign() == 0) {
            Intent intent = new Intent(MainActivity.this, DashboardWithGraphActivity.class);
            intent.putExtra("FromMainActivity", true);
            intent.putExtra("NighMode", Utility._appSetting.getVisionMode() == 1);
            startActivity(intent);

        } else if (Utility._appSetting.getDashboardDesign() == 1) {
            Intent intent = new Intent(MainActivity.this, DashboardWithEngineHourActivity.class);
            intent.putExtra("FromMainActivity", true);
            intent.putExtra("NighMode", Utility._appSetting.getVisionMode() == 1);
            startActivity(intent);
        } else if (Utility._appSetting.getDashboardDesign() == 2) {

            showGaugeCluster();
            /*ShowDashboardWithEngineHours();*/
         /*   Intent intent = new Intent(MainActivity.this, DashboardWithGauageCluster.class);
            intent.putExtra("FromMainActivity", true);
            startActivity(intent);*/
            //     tvGauge.setVisibility(View.VISIBLE);
        }
    }

    private void showGaugeCluster() {
        if (!ConstantFlag.FLAG_GAUGE_SHOW)
            return;
        ivDrivingImage.setVisibility(View.GONE);
        tvFreeze.setBackgroundColor(0);
        tvFreeze.setBackgroundResource(Utility.NightModeFg ? R.drawable.freeze_background_night : R.drawable.freeze_background);
        tvFreeze.setVisibility(View.GONE);
        tvLoginFreeze.setVisibility(View.GONE);
        chkRules.setVisibility(View.VISIBLE);
        setDrawerState(true);
        if (Utility.motionFg) {

            if (Utility._appSetting.getViolationOnDrivingScreen() == 1) {
                tvViolation.setVisibility(View.VISIBLE);
            } else {
                tvViolation.setVisibility(View.INVISIBLE);
            }

            toolbar.setVisibility(View.GONE);
            flagBar.setVisibility(View.GONE);

            if (Utility.user1.isOnScreenFg() && Utility.user1.isActive()) {
                // freeze activity
                //Log.d(TAG, "freeze activity");
                tvLoginFreeze.setVisibility(View.GONE);
                tvGauge.setVisibility(View.VISIBLE);
                ll_enginehours.setVisibility(View.VISIBLE);
                ll_drivingRemaning.setVisibility(View.VISIBLE);
                tvFreeze.setVisibility(View.VISIBLE);
                // tvFreeze.setBackgroundResource(R.drawable.freeze_background);

                setDrawerState(false);
            } else if (Utility.user2.isOnScreenFg() && Utility.user2.isActive()) {
                // freeze activity
                tvLoginFreeze.setVisibility(View.GONE);
                tvGauge.setVisibility(View.VISIBLE);
                ll_enginehours.setVisibility(View.VISIBLE);
                ll_drivingRemaning.setVisibility(View.VISIBLE);
                tvFreeze.setVisibility(View.VISIBLE);
                // tvFreeze.setBackgroundResource(R.drawable.freeze_background);

                setDrawerState(false);
            } else {
                tvLoginFreeze.setVisibility(View.GONE);
                tvGauge.setVisibility(View.VISIBLE);
                ll_enginehours.setVisibility(View.VISIBLE);
                ll_drivingRemaning.setVisibility(View.VISIBLE);
                tvFreeze.setVisibility(View.VISIBLE);
                // tvFreeze.setBackgroundResource(R.drawable.freeze_background);

                setDrawerState(false);
            }

        }


    }

    private void hideGaugeCluster() {
        try {
            tvDrivingRemainingFreeze.setBackgroundResource(R.drawable.engine_hours_bg);
            tvFreeze.setVisibility(View.GONE);
            tvLoginFreeze.setVisibility(View.GONE);
            tvGauge.setVisibility(View.INVISIBLE);
            ll_enginehours.setVisibility(View.INVISIBLE);
            ll_drivingRemaning.setVisibility(View.INVISIBLE);

            // if user is logged in
            if (Utility.activeUserId > 0) {
                if (currentScreen != Login_Screen) {
                    toolbar.setVisibility(View.VISIBLE);
                    flagBar.setVisibility(View.VISIBLE);
                } else {
                    flagBar.setVisibility(View.VISIBLE);
                }
                setDrawerState(true);
            }

            if (Utility.activeUserId == Utility.onScreenUserId && elogFragment != null) {
                elogFragment.updateTodayDistance();
            }
        } catch (Exception exe) {

        }
    }

    // Created By: Pallavi
    // Created Date: 2 April
    // Purpose: hide Dashboard Depends on which Dashboard is open
    // Paramter : Context of Dashboard
    public void hideDashboard(Context context) {

        if (Utility._appSetting.getDashboardDesign() == 0 && context instanceof DashboardWithGraphActivity) {
            ((DashboardWithGraphActivity) context).finish();
        } else if (Utility._appSetting.getDashboardDesign() == 1 && context instanceof DashboardWithEngineHourActivity) {
            ((DashboardWithEngineHourActivity) context).finish();
        } else if (Utility._appSetting.getDashboardDesign() == 2 && context instanceof DashboardWithGauageCluster) {
            ((DashboardWithGauageCluster) context).finish();
        }
    }


    @Override
    public void setTitle(final String title) {
        if (!updateTitle) {
            updateTitle = true;
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                    getSupportActionBar().setTitle(title);
                } catch (Exception exe) {
                }
            }
        });
    }

    //End Implement method of ELogMainActivity

    public static Date currentDate = Utility.newDateOnly();
    Runnable runnableCAN = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {

                    Thread.sleep(1000);

                 /*    if (Utility.IMEI.equals("356961076533234")) {
                        Utility.HutchConnectStatusFg = true;
                        Utility._CurrentDateTime = Utility.getCurrentDateTime();
                    }*/

                    if (Utility.motionFg) {

                        updateCanInformation();
                    }

                    onUpdateCanbusIcon(CanMessages.mState);

                    resetIconOnFirstDutyStatus();

                    Date now = Utility.newDateOnly();
                    if (currentDate.before(now)) {
                        long difference = (Utility.newDate().getTime() - Utility.newDateOnly().getTime()) / (60 * 1000);
                        if (difference > 30)
                            continue;
                        onDateChanged();

                        // monitory balance every day
                        //CheckBalance(false);

                        currentDate = now;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "ERROR update CAN: " + e.getMessage());
                    break;
                }
            }
        }
    };

    private void onDateChanged() {
        try {

            int eventType = 2;
            int eventCode = 1;
            String eventDescription = getString(R.string.IL_conventional_description);
            if (Utility.activeUserId > 0 && Utility.HutchConnectStatusFg) {
                String previousDate = Utility.getPreviousDateOnly(-1);

                // get previous dailylog id
                int dailyLogId = DailyLogDB.getDailyLog(Utility.activeUserId, previousDate);
                int currentStatus = EventDB.getCurrentDutyStatus(Utility.activeUserId);

                if (dailyLogId > 0) {
                    if (EventDB.checkDuplicate(dailyLogId, previousDate + " 23:59:59")) {
                        return;
                    }
                    if (currentStatus == 3) {
                        // enter intermediate event for midnight
                        EventDB.EventCreate(previousDate + " 23:59:59", eventType, eventCode, eventDescription, 1, 1, dailyLogId, Utility.activeUserId, "mid night event", currentRule);
                    } else {

                        EventBean bean = EventDB.LastEventGetByDriverId(Utility.activeUserId);
                        EventDB.EventCreate(previousDate + " 23:59:59", bean.getEventType(), bean.getEventCode(), bean.getEventCodeDescription(), 1, 1, dailyLogId, Utility.activeUserId, "mid night event", currentRule);
                    }

                    calculateDistance(dailyLogId);
                    DailyLogDB.DailyLogCertifyRevert(Utility.activeUserId, dailyLogId);

                    // Post Data
                    MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo_DailyLogId, dailyLogId);

                    Thread.sleep(2000);
                }
                dailyLogId = DailyLogDB.DailyLogCreate(Utility.activeUserId, Utility.ShippingNumber, Utility.TrailerNumber, "");

                String eventDateTime = Utility.getCurrentDateTime();
                if (currentStatus == 3) {
                    EventDB.EventCreate(eventDateTime, 2, 1, getString(R.string.IL_conventional_description), 1, 1, dailyLogId, Utility.activeUserId, "mid night event", currentRule);
                } else {

                    EventBean bean = EventDB.LastEventGetByDriverId(Utility.activeUserId);
                    EventDB.EventCreate(eventDateTime, bean.getEventType(), bean.getEventCode(), bean.getEventCodeDescription(), 1, 1, dailyLogId, Utility.activeUserId, "mid night event", currentRule);
                }


                // Post Data
                MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo_DailyLogId, dailyLogId);

                // if co driver exists then we will add previous day event in mid night to co driver
                if (Utility.user2.getAccountId() > 0) {

                    int coDriverId = Utility.activeUserId == Utility.user2.getAccountId() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
                    EventBean bean = EventDB.LastEventGetByDriverId(coDriverId);
                    if (bean.getEventType() == 0) {
                        bean.setEventType(1);
                        bean.setEventCode(1);
                        bean.setEventCodeDescription(getString(R.string.duty_status_changed_to_off_duty));
                    }

                    // get dailylogid for previous date for co driver
                    dailyLogId = DailyLogDB.getDailyLog(coDriverId, previousDate);

                    if (dailyLogId > 0) {
                        // enter intermediate event for midnight
                        EventDB.EventCreate(previousDate + " 23:59:59", bean.getEventType(), bean.getEventCode(), bean.getEventCodeDescription(), 1, 1, dailyLogId, coDriverId, "mid night event", currentRule);
                        DailyLogDB.DailyLogCertifyRevert(Utility.activeUserId, dailyLogId);

                        // Post Data
                        MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo_DailyLogId, dailyLogId);
                        // this sleep is to change created date of event other wise it will overwrite previous event created on same date
                        Thread.sleep(2000);
                    }

                    //get dailylogid for current date of co driver
                    dailyLogId = DailyLogDB.DailyLogCreate(coDriverId, Utility.ShippingNumber, Utility.TrailerNumber, "");
                    // get last event to copy

                    EventDB.EventCreate(eventDateTime, bean.getEventType(), bean.getEventCode(), bean.getEventCodeDescription(), 1, 1, dailyLogId, coDriverId, "mid night event", currentRule);
                    //EventDB.EventCopy(eventDateTime, 1, 1, coDriverId, dailyLogId);

                    // Post Data
                    MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo_DailyLogId, dailyLogId);
                }

            }
        } catch (Exception exe) {

        }
    }

    private void calculateDistance(int logId) {
        try {
            if (logId > 0) {
                int todayDistance = EventDB.CalculateDistanceByLogId(logId);
                DailyLogDB.DailyLogDistanceSave(logId, Utility.activeUserId, todayDistance);
            }
        } catch (Exception exe) {
        }
    }

    private void updateCanInformation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                float voltage = Float.parseFloat(CanMessages.Voltage);
                float speed = Float.parseFloat(CanMessages.Speed);
                Double odometerReading = Double.parseDouble(CanMessages.OdometerReading);
                float coolantTemp = Float.parseFloat(CanMessages.CoolantTemperature);
                String speedUnit = " km/h";
                String distanceUnit = " Kms";
                String tempUnit = " ° F";
                if (Utility._appSetting.getUnit() == 2) {
                    odometerReading = odometerReading * .62137d;
                    speed = speed * .62137f;
                    speedUnit = " MPH";
                    distanceUnit = " Miles";
                } else {
                   /* coolantTemp = ((coolantTemp - 32) * 5) / 9;
                    tempUnit = "° C";*/
                }
                // set rotations
                setSpeedRoatation(speed);
                setRPMRoatation();
                setFuelLevel(Float.parseFloat(CanMessages.FuelLevel1));
                setCoolantRoatation(coolantTemp);
                if (voltage < 48f)
                    setVoltageRoatation(voltage);

                if (frameDiagnostic != null) {
                    if (Utility.dataDiagnosticIndicatorFg) {
                        frameDiagnostic.setVisibility(View.VISIBLE);
                    } else {
                        frameDiagnostic.setVisibility(View.GONE);
                    }
                }

                if (frameMalfunction != null) {
                    if (Utility.malFunctionIndicatorFg) {
                        frameMalfunction.setVisibility(View.VISIBLE);
                    } else {
                        frameMalfunction.setVisibility(View.GONE);
                    }
                }

                // set label
                if (tvSpeed != null)
                    tvSpeed.setText(Math.round(speed) + speedUnit);

                DecimalFormat df = new DecimalFormat("#######.#");
                //String odoText = df.format(odo);
                String odoText = String.format("%.1f", odometerReading);
                //Log.d(TAG, "odo=" + odoText);
                odoText = odoText.replace(".", "");

                String zero = "";
                if (odoText.length() < 8) {
                    for (int i = odoText.length(); i < 8; i++) {
                        zero += "0";
                    }
                }
                odoText = zero + odoText;

                if (tvOdometer != null)
                    tvOdometer.setText(odoText);

                int rpm = Math.round(Float.parseFloat(CanMessages.RPM));
                if (tvRPM != null)
                    tvRPM.setText(rpm + "");

                float engineHrs = Float.parseFloat(CanMessages.EngineHours);
                //String engineText = df.format(engineHrs);
                String engineText = String.format("%.1f", engineHrs);
                //Log.d(TAG, "engineHrs=" + engineText);
                engineText = engineText.replace(".", "");
                zero = "";
                if (engineText.length() < 8) {
                    for (int i = engineText.length(); i < 8; i++) {
                        zero += "0";
                    }
                }
                engineText = zero + engineText;

                if (tvEngineHours != null)
                    tvEngineHours.setText(engineText);

                int fuelLevel = Math.round(Float.parseFloat(CanMessages.FuelLevel1));
                if (tvPosition != null)
                    tvPosition.setText(fuelLevel + "%");

                int coolant = Math.round(coolantTemp);
                if (tvCoolant != null)
                    tvCoolant.setText(coolant + tempUnit);

                if (voltage < 48f) {
                    if (tvVoltage != null)
                        tvVoltage.setText(Float.parseFloat(CanMessages.Voltage) + " V");
                }

                if (tvDrivingRemainingFreeze != null && activeCurrentDutyStatus == 3 && ViolationDT != null) {
                    int secondsLeft = (int) (ViolationDT.getTime() - (Utility.newDate()).getTime()) / 1000;
                    if (secondsLeft < 0) {
                        secondsLeft = 0;
                    }

                    if (secondsLeft == 0) {
                        if (GPSData.NoHOSViolationFgFg == 1) {
                            onUpdateViolation(true);
                            GPSData.NoHOSViolationFgFg = 0;
                        }
                        tvDrivingRemainingFreeze.setBackgroundResource(R.drawable.remaining_driving_hours_bg_red);

                    } else if (secondsLeft <= 3600) {
                        tvDrivingRemainingFreeze.setBackgroundResource(R.drawable.remaining_driving_hours_bg_yellow);

                    }

                    String timeRemaining = Utility.getTimeFromSeconds(secondsLeft);
                    tvDrivingRemainingFreeze.setText(timeRemaining);

                    Utility.setViolationColor((secondsLeft / 60), tvViolation, getApplicationContext());
                    // Utility.setViolationColor((secondsLeft / 60), tvViolation, getApplicationContext());
                    if (secondsLeft > 0) {
                        if (GPSData.NoHOSViolationFgFg == 0) {
                            onUpdateViolation(false);
                            GPSData.NoHOSViolationFgFg = 1;
                        }
                    }
                }
            }
        });
    }


    private void setSpeedRoatation(float speed) {

        //if speed is 10 then angle is 15
        float angle = 15 * speed / 10;

        // float from = imgreezeSpeed.getRotation();
        imgreezeSpeed.setRotation(angle);
        //   android.animation.ObjectAnimator.ofFloat(imgreezeSpeed, "rotation", from, angle).start();

    }

    private void setRPMRoatation() {

        float RPM = Float.parseFloat(CanMessages.RPM);

        //if RPM is 1000 then angle is 60
        float angle = 60 * RPM / 1000;

        // float from = imgFreezeRPM.getRotation();
        imgFreezeRPM.setRotation(angle);
        // android.animation.ObjectAnimator.ofFloat(imgFreezeRPM, "rotation", from, angle).start();

    }

    private void setFuelLevel(float position) {

        //if position is 100 then angle is 180
        float angle = 180 * position / 100;

        // float from = imgFreezeThrPos.getRotation();
        imgFreezeThrPos.setRotation(angle);
        // android.animation.ObjectAnimator.ofFloat(imgFreezeThrPos, "rotation", from, angle).start();

    }

    private void setCoolantRoatation(float coolant) {

        //if voltage 200 then angle is 90
        float angle = (coolant - 100) * 90 / 100.0f;

        //  float from = imgFreezeCoolantTemp.getRotation();
        imgFreezeCoolantTemp.setRotation(angle);

        //android.animation.ObjectAnimator.ofFloat(imgFreezeCoolantTemp, "rotation", from, angle).start();

    }

    private void setVoltageRoatation(float voltage) {
        if (voltage > 48) {
            return;
        }

        // if voltage is 9 then angle is 0 and if 18 then 180
        float angle = (voltage - 8) * 18;

        // float from = imgFreezeVoltage.getRotation();
        imgFreezeVoltage.setRotation(angle);
        //android.animation.ObjectAnimator.ofFloat(imgFreezeVoltage, "rotation", from, angle).start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //handler.removeCallbacks(autoSync);
        //handlerOD.removeCallbacks(runnableStatus);
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        if (!Utility.getPreferences("firstrun", true)) {
            this.unregisterReceiver(batteryInfoReceiver);
            this.unregisterReceiver(networkChangeReceiver);
            this.unregisterReceiver(gpsChangeReceiver);
            this.unregisterReceiver(dateChangedReceiver);
        }

        if (threadUpdateCANInfos != null)
            threadUpdateCANInfos.interrupt();
        ChatClient.disconnect();

        if (Utility._appSetting.getECUConnectivity() == 1) {
            objHutchConnect.disconnect();
        }


        if (alertMonitor != null)
            alertMonitor.stopAlertMonitor();

        Utility.user1 = Utility.user2 = new UserBean();

        specialCategoryDialog = null;
        messageDialog = null;
        successDialog = null;
        dialogHandler.removeCallbacksAndMessages(null);
        if (thInitializeBluetooth != null) {
            thInitializeBluetooth.interrupt();
        }

        // unbindCopilot();
        android.os.Process.killProcess(android.os.Process.myPid());

    }

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FIRST_ACTIVITY = 2;
    private static final int REQUEST_SPLASH_ACTIVITY = 3;
    private static final int REQUEST_SETUP_ACTIVITY = 4;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQUEST_ENABLE_BT) {
                initializeBluetooth();
            } else if (requestCode == REQUEST_NAVIGATION_CODE) {

            } else if (requestCode == REQUEST_FIRST_ACTIVITY) {

                Utility.restart(getApplicationContext(), 3000);

            } else if (requestCode == REQUEST_SETUP_ACTIVITY) {
            } else if (requestCode == REFER_FRIEND_INBOX_MESSAGE) {


                Uri contactUri = data.getData();

                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                Cursor cursor = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursor.moveToFirst();
                int numberColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                String number = cursor.getString(numberColumn);

                referAFriendWithInbox(number);
            }
        } catch (Exception e) {
            LogFile.write(LoginFragment.class.getName() + "::onActivityResult Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TAG, "::onActivityResult:  ", e.getMessage(), Utility.printStackTrace(e));
        }
    }

    private void referAFriendWithInbox(String number) {

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, textToShare, null, null);
    }

    @Override
    public void editEvent(EventBean bean) {
        if (bean.getEventType() <= 3) {
            selectedEvent = bean;
            bEditEvent = false;
            if (bean.getEventRecordStatus() == 1) {
                bEditEvent = true;
            }
            bWebEvent = false;
            if (bean.getEventRecordOrigin() == 3 && bean.getEventRecordStatus() == 3) {
                bEditEvent = true;
                bWebEvent = true;
            }
            /*if (!Utility.isLargeScreen(getApplicationContext())) {

            } else {
                invalidateOptionsMenu();
            }*/
        }
    }

    @Override
    public void onEventEdited() {

        if (undockingMode || selectedEvent == null) {
            return;
        }
        invalidateOptionsMenu();
        Bundle bundle = new Bundle();

        int status = 1;
        int type = selectedEvent.getEventType();
        int code = selectedEvent.getEventCode();
        if (type == 1) {
            if (code == 1) {
                status = 1;
            } else if (code == 2) {
                status = 2;
            } else if (code == 3) {
                status = 3;
            } else if (code == 4) {
                status = 4;
            }
        } else if (type == 2) {
            status = 3;
        } else if (type == 3) {
            if (code == 1) {
                status = 5;
            } else if (code == 2) {
                status = 6;
            }
        }

        bundle.putInt("current_status", status);
        bundle.putBoolean("edit_event", true);
        bundle.putSerializable("selected_event", selectedEvent);
        NewEventFragment fragment = new NewEventFragment();

        fragment.setArguments(bundle);
        replaceFragmentWithBackStack(fragment);
        prevTitle = getSupportActionBar().getTitle().toString();
        title = getApplicationContext().getResources().getString(R.string.title_edit_event);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle(title);
        bWebEvent = false;

        previousScreen = currentScreen;
        currentScreen = New_Event_Screen;
    }

    String prevTitle = "";

    @Override
    public void onDetailClosed() {
        loadDailyLog();
    }

    @Override
    public void onDetailSaved() {
        loadDailyLog();
    }

    @Override
    public void onDocking() {

        //Demo build
        SharedPreferences prefs = getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);
        prefs.edit().putBoolean("undocking", false).commit();
        undockingMode = false;
        showLoaderAnimation(true);
        Thread thOnDocking = new Thread(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        loadDailyLog();

                        showLoaderAnimation(false);
                    }
                });
            }
        });
        thOnDocking.setName("th-OnDocking");
        thOnDocking.start();

    }

    @Override
    public void showBluetoothConnectionMessage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!bBluetoothConnecting) {
                    progressBarDialog = new ProgressDialog(MainActivity.this);
                    progressBarDialog.setIcon(R.drawable.ic_launcher);
                    progressBarDialog.setCancelable(true);
                    progressBarDialog.setCanceledOnTouchOutside(false);
                    progressBarDialog.setTitle("Connecting...");
                    progressBarDialog.setMessage(getResources().getString(R.string.bluetooth_connection_infos));
                    progressBarDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                    progressBarDialog.show();
                    bBluetoothConnecting = true;
                }
            }
        });

    }

    @Override
    public void showConnectionSuccessfull() {
        if (!bBluetoothConnectionSuccess) {
            progressBarDialog.dismiss();

            final AlertDialog ad = new AlertDialog.Builder(this).create();
            ad.setCancelable(true);
            ad.setCanceledOnTouchOutside(false);
            ad.setIcon(R.drawable.ic_launcher);
            ad.setTitle("E-Log");
            ad.setMessage(getResources().getString(R.string.bluetooth_connection_ok));
            ad.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ad.cancel();

                            //if in undocking mode, call login screen
                            SharedPreferences prefs = getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);
                            if (prefs.getBoolean("undocking", false)) {
                                //turn off undocking mode
                                prefs.edit().putBoolean("undocking", false).commit();
                                undockingMode = false;

                                if (elogFragment == null) {
                                    elogFragment = new ELogFragment();
                                }
                                replaceFragment(elogFragment);
                                if (ConstantFlag.AUTOSTART_MODE) {
                                    startService(new Intent(MainActivity.this, AutoStartService.class));
                                }
                            }
                        }
                    });
            ad.show();
            bBluetoothConnectionSuccess = true;
        }
    }

    @Override
    public void showConnectionError() {
        if (!bBluetoothConnectionError) {
            progressBarDialog.dismiss();

            final AlertDialog ad = new AlertDialog.Builder(this).create();
            ad.setCancelable(true);
            ad.setCanceledOnTouchOutside(false);
            ad.setIcon(R.drawable.ic_launcher);
            ad.setTitle("E-Log");
            ad.setMessage(getResources().getString(R.string.setup_error_msg));
            ad.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ad.cancel();
                            finish();
                        }
                    });
            ad.show();
            bBluetoothConnectionError = true;
        }
    }

    @Override
    public void onButtonClicked() {

    }

    @Override
    public void showLoaderAnimation(boolean isShown) {
        if (isShown) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showLoaderAnimation(false);
                        }
                    });
                }
            }, 30000);
            rlLoadingPanel.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            rlLoadingPanel.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    public void updateActiveIcon() {
        if (ivActiveUser != null) {
            ivActiveUser.setBackgroundResource(R.drawable.ic_flagbar_driver_active);
        }
        if (icFreezeActiveUser != null) {
            icFreezeActiveUser.setBackgroundResource(R.drawable.ic_flagbar_driver_active);
        }
    }

    @Override
    public void onMessageReceived() {
        if (icMessage != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    icMessage.setVisibility(View.VISIBLE);
                    icFreezeMessage.setVisibility(View.VISIBLE);
                }
            });

        }
    }

    @Override
    public void onMessageRead() {
        if (icMessage != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    icMessage.setVisibility(View.GONE);
                    icFreezeMessage.setVisibility(View.GONE);
                }
            });
        }
    }

    public void turnOffGPSIcon() {
        if (icGPS != null) {
            icGPS.setVisibility(View.VISIBLE);
            icGPS.setBackgroundResource(R.drawable.ic_flagbar_gps_off);
        }

        if (icFreezeGPS != null) {
            icFreezeGPS.setVisibility(View.VISIBLE);
            icFreezeGPS.setBackgroundResource(R.drawable.ic_flagbar_gps_off);
        }
    }

    public void turnOnGPSIcon() {
        if (icGPS != null) {
            icGPS.setVisibility(View.VISIBLE);
            icGPS.setBackgroundResource(R.drawable.ic_flagbar_gps_on);
        }

        if (icFreezeGPS != null) {
            icFreezeGPS.setVisibility(View.VISIBLE);
            icFreezeGPS.setBackgroundResource(R.drawable.ic_flagbar_gps_on);
        }
    }

    @Override
    public void onUpdateInspectionIcon() {
        GPSData.TripInspectionCompletedFg = 1;
        if (icInspection != null) {
            icInspection.setVisibility(View.VISIBLE);
            icInspection.setBackgroundResource(R.drawable.ic_flagbar_dvir_done);
            //change the icon to GREEN
            //Log.d(TAG, "Change Inspection GREEN");
        }
        if (icFreezeInspection != null) {
            icFreezeInspection.setVisibility(View.VISIBLE);
            icFreezeInspection.setBackgroundResource(R.drawable.ic_flagbar_dvir_done);
        }
    }


    @Override
    public void resetInspectionIcon() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (icInspection != null) {
                    icInspection.setVisibility(View.VISIBLE);
                    //change the icon to RED
                    icInspection.setBackgroundResource(R.drawable.ic_flagbar_dvir_pending);
                    //Log.d(TAG, "Change Inspection RED");
                }
                if (icFreezeInspection != null) {
                    icFreezeInspection.setVisibility(View.VISIBLE);
                    icFreezeInspection.setBackgroundResource(R.drawable.ic_flagbar_dvir_pending);
                }
            }
        });
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            Bundle bundle = intent.getExtras();
            int userId = bundle.getInt("UserId", 0);
            String userName = bundle.getString("UserName", "");
            if (userId != 0) {
                onNavigationItemSelected(R.id.message);
                navigateToMessage(userId, userName);
            } else if (bundle != null && bundle.getString("intentFrom") != null && bundle.getString("intentFrom").equalsIgnoreCase("Notification")) {
                onLoadNotificationList();
            }

        } catch (Exception exe) {
        }
    }

    public void onUpdateWebServiceIcon(boolean result) {
        Utility.webServiceConnectivity = result;
        if (icWebService != null) {
            if (result) {
                icWebService.setVisibility(View.VISIBLE);
                //set icon to Server icon
                icWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_on);
                //Log.d(TAG, "Show WebService Icon");
            } else {
                icWebService.setVisibility(View.VISIBLE);
                icWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_off);
            }
        }

        if (icFreezeWebService != null) {
            if (result) {
                icFreezeWebService.setVisibility(View.VISIBLE);
                icFreezeWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_on);
            } else {
                icFreezeWebService.setVisibility(View.VISIBLE);
                icFreezeWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_off);
            }
        }
    }

    @Override
    public void onServerStatusChanged(final boolean status) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onUpdateWebServiceIcon(status);
            }
        });
    }

    public void onUpdateCanbusIcon(int state) {
        canbusState = state;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (icCanbus != null) {
                    icCanbus.setVisibility(View.VISIBLE);

                    switch (canbusState) {
                        case CanMessages.STATE_NONE:
                        case CanMessages.STATE_LISTEN:
                            //no connection with canbus
                            //set icon to RED
                            //Log.d(TAG, "Show Canbus RED");
                            icCanbus.setBackgroundResource(R.drawable.ic_flagbar_canbus_disconnect);
                            break;

                        case CanMessages.STATE_CONNECTING:
                            //set to blinking icon
                            //Log.d(TAG, "Show Canbus blinking");
                            icCanbus.setBackgroundResource(R.drawable.trans);
                            icCanbus.setBackgroundResource(R.drawable.flagbar_canbus_blinking);

                            AnimationDrawable frameAnimation = (AnimationDrawable) icCanbus.getBackground();
                            frameAnimation.start();
                            break;
                        case CanMessages.STATE_CONNECTED:
                            //set icon to GREEN
                            //Log.d(TAG, "Show Canbus GREEN");
                            icCanbus.setBackgroundResource(R.drawable.ic_flagbar_canbus_connect);

                            break;
                    }
                }

                if (icFreezeCanbus != null) {
                    icFreezeCanbus.setVisibility(View.VISIBLE);

                    switch (canbusState) {
                        case CanMessages.STATE_NONE:
                        case CanMessages.STATE_LISTEN:
                            icFreezeCanbus.setBackgroundResource(R.drawable.ic_flagbar_canbus_disconnect);
                            break;

                        case CanMessages.STATE_CONNECTING:
                            icFreezeCanbus.setBackgroundResource(R.drawable.trans);
                            icFreezeCanbus.setBackgroundResource(R.drawable.flagbar_canbus_blinking);

                            AnimationDrawable frameAnimation = (AnimationDrawable) icFreezeCanbus.getBackground();
                            frameAnimation.start();
                            break;
                        case CanMessages.STATE_CONNECTED:
                            icFreezeCanbus.setBackgroundResource(R.drawable.ic_flagbar_canbus_connect);
                            break;
                    }
                }
            }
        });
    }

    public void onUpdateNetworkIcon(boolean isConnected) {
        icWifi.setVisibility(View.GONE);
        icNetwork.setVisibility(View.VISIBLE);
        icFreezeWifi.setVisibility(View.GONE);
        icFreezeNetwork.setVisibility(View.VISIBLE);
        if (isConnected) {
            GPSData.CellOnlineFg = 1;
            //set Network icon to solid GREEN bar
            //Log.d(TAG, "Show Network GREEN");
            icNetwork.setBackgroundResource(R.drawable.ic_flagbar_network_on);
            icFreezeNetwork.setBackgroundResource(R.drawable.ic_flagbar_network_on);
        } else {
            GPSData.CellOnlineFg = 0;
            //set Network icon to RED
            //Log.d(TAG, "Show Network RED");
            icNetwork.setBackgroundResource(R.drawable.ic_flagbar_network_off);
            icWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_off);
            icFreezeNetwork.setBackgroundResource(R.drawable.ic_flagbar_network_off);
            icFreezeWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_off);
        }
    }

    public void onUpdateWifiIcon(boolean isConnected) {
        icWifi.setVisibility(View.VISIBLE);
        icNetwork.setVisibility(View.GONE);
        icFreezeWifi.setVisibility(View.VISIBLE);
        icFreezeNetwork.setVisibility(View.GONE);
        if (isConnected) {
            GPSData.WifiOnFg = 1;
            //set Wifi icon to solid GREEN
            //Log.d(TAG, "Show wifi GREEN");
            icWifi.setBackgroundResource(R.drawable.ic_flagbar_wifi_on);
            icFreezeWifi.setBackgroundResource(R.drawable.ic_flagbar_wifi_on);
        } else {

            GPSData.WifiOnFg = 0;
            //set Wifi icon to GREY
            //Log.d(TAG, "Show wifi GREY");
            icWifi.setBackgroundResource(R.drawable.ic_flagbar_wifi_off);
            icWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_off);
            icFreezeWifi.setBackgroundResource(R.drawable.ic_flagbar_wifi_off);
            icFreezeWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_off);
        }
    }

    public void onUpdateBatteryIcon(int level, boolean isPlugged) {
        //level is percent of battery
        if (icBattery != null) {
            icBattery.setVisibility(View.VISIBLE);
            canvas = new Canvas((batteryBmp));
            drawingBattery(level, isPlugged, batteryBmp, icBattery);
        }

        if (icFreezeBattery != null) {
            icFreezeBattery.setVisibility(View.VISIBLE);
            drawingBattery(level, isPlugged, batteryBmp, icFreezeBattery);
        }
    }


    private void drawingBattery(int level, boolean isPlugged, Bitmap bmp, ImageView view) {
        Canvas c = new Canvas(bmp);
        if (level < 20) {
            view.setImageResource(R.drawable.ic_flagbar_battery_empty);
        } else {
            float topPercent = 8;

            float top = bmp.getHeight() * 0.08f;
            float x = (1f * bmp.getWidth()) / 2f - (bmp.getWidth() * 0.2f);
            float xEnd = (1f * bmp.getWidth()) / 2f + (bmp.getWidth() * 0.2f);
            float xTop = x + (bmp.getWidth() * 0.1f);
            float xTopEnd = xEnd - (bmp.getWidth() * 0.1f);
            float yTop = (topPercent / 100f) * bmp.getHeight() + top;

            Paint pUsed = new Paint();
            pUsed.setColor(Color.LTGRAY);
            pUsed.setStrokeWidth(1);

            Paint pLeft = new Paint();
            pLeft.setColor(Color.argb(255, 78, 163, 42));
            pLeft.setStrokeWidth(1);

            float height = bmp.getHeight() - (2 * top);
            int levelUsed = 100 - level;
            float y = ((1f * levelUsed) / 100f) * height + top;

            if (levelUsed < topPercent) {
                c.drawRect(xTop, top, xTopEnd, y, pUsed);
                c.drawRect(xTop, y, xTopEnd, yTop, pLeft);
                c.drawRect(x, yTop, xEnd, height, pLeft);
            } else {
                c.drawRect(xTop, top, xTopEnd, yTop, pUsed);
                float yUsed = yTop + ((1f * (levelUsed - topPercent) / 100f) * height);
                c.drawRect(x, yTop, xEnd, yUsed, pUsed);
                c.drawRect(x, yUsed, xEnd, height, pLeft);
            }

            if (isPlugged) {
                Paint p = new Paint();
                p.setColor(Color.WHITE);
                p.setStyle(Paint.Style.FILL);
                p.setStrokeWidth(1);

                float chargedX1 = x + (bmp.getWidth() * 0.1f);
                float chargedX2 = chargedX1 + (bmp.getWidth() * 0.2f);
                float chargedX3 = chargedX2 - (bmp.getWidth() * 0.1f);
                float chargedX4 = chargedX3 + (bmp.getWidth() * 0.15f);
                float chargedX5 = chargedX1 + (bmp.getWidth() * 0.05f);
                float chargedX6 = chargedX5 + (bmp.getWidth() * 0.05f);
                float chargedX7 = chargedX6 - (bmp.getWidth() * 0.1f);

                float chargedY1 = yTop + bmp.getHeight() * 0.1f;
                float chargedY2 = chargedY1 + bmp.getHeight() * 0.18f;
                float chargedY3 = chargedY2 + bmp.getHeight() * 0.08f;
                float chargedY4 = height - bmp.getHeight() * 0.05f;

                Path path = new Path();
                path.reset();
                path.moveTo(chargedX1, chargedY1);
                path.lineTo(chargedX2, chargedY1);
                path.lineTo(chargedX3, chargedY2);
                path.lineTo(chargedX4, chargedY2);
                path.lineTo(chargedX5, chargedY4);
                path.lineTo(chargedX6, chargedY3);
                path.lineTo(chargedX7, chargedY3);
                path.lineTo(chargedX1, chargedY1);

                c.drawPath(path, p);
            }

            view.setImageBitmap(bmp);
        }
    }


    @Override
    public void callELog() {
        if (undockingMode) {
            replaceFragment(new DockingFragment());
            getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_eld));
            //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            //drawer.closeDrawer(GravityCompat.START);
            return;
        }
        if (stopService) {
            if (ConstantFlag.AUTOSTART_MODE) {
                startService(new Intent(this, AutoStartService.class));
            }
        }
        isOnDailyLog = true;
        bInspectDailylog = false;
        previousScreen = currentScreen;
        currentScreen = DailyLog_Screen;
        if (elogFragment == null) {
            elogFragment = new ELogFragment();
        }
        replaceFragment(elogFragment);

        invalidateOptionsMenu();
    }

    @Override
    public void callInspectELog() {
        if (undockingMode) {
            return;
        }
        bInspectDailylog = true;
        bEditEvent = false;
        isOnDailyLog = false;

       /* if (inspectFragment == null) {
            inspectFragment = new DetailFragment();
        }*/
        replaceFragment(InspectLogFragment.newInstance());

        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_inspect_elog));
        previousScreen = currentScreen;
        currentScreen = Inspect_DailyLog_Screen;
        invalidateOptionsMenu();
    }

    @Override
    public void callNewEvent() {
        if (undockingMode) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("current_status", currentDutyStatus);

        NewEventFragment fragment = NewEventFragment.newInstance();
        fragment.setArguments(bundle);
        isOnDailyLog = false;
        bInspectDailylog = false;
        replaceFragment(fragment);
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_create_event));
        previousScreen = currentScreen;
        currentScreen = New_Event_Screen;
        invalidateOptionsMenu();
    }

    @Override
    public void callEditRequest() {
        if (undockingMode) {
            return;
        }
        isOnDailyLog = false;
        bInspectDailylog = false;
        replaceFragment(ModifiedFragment.newInstance());
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_edit_request));
        previousScreen = currentScreen;
        currentScreen = Edit_Request_Screen;
        title = getApplicationContext().getResources().getString(R.string.menu_edit_request);
        invalidateOptionsMenu();
    }

    @Override
    public void callUnidentifiedEvent() {
        if (undockingMode) {
            return;
        }
        isOnDailyLog = false;
        bInspectDailylog = false;
        replaceFragment(UnidentifyFragment.newInstance());
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_unidentified_event));
        previousScreen = currentScreen;
        currentScreen = Unidentified_Data_Screen;
        title = getApplicationContext().getResources().getString(R.string.menu_unidentified_event);
        invalidateOptionsMenu();
    }

    @Override
    public void callCertifyLogBook() {
        if (undockingMode) {
            return;
        }
        isOnDailyLog = false;
        bInspectDailylog = false;
        replaceFragment(UnCertifiedFragment.newInstance());
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_certify_log_book));
        previousScreen = currentScreen;
        currentScreen = Uncertified_LogBook_Screen;
        title = getApplicationContext().getResources().getString(R.string.title_certify_log_book);
        invalidateOptionsMenu();
    }

    @Override
    public void callViolationHistory() {
        if (undockingMode) {
            return;
        }
        isOnDailyLog = false;
        bInspectDailylog = false;
        replaceFragment(ViolationFragment.newInstance());
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.menu_violation_history));
        previousScreen = currentScreen;
        currentScreen = Violation_History_Screen;
        title = getApplicationContext().getResources().getString(R.string.menu_violation_history);
        invalidateOptionsMenu();
    }

    @Override
    public void callDriverProfile() {
        if (undockingMode) {
            return;
        }
        isOnDailyLog = false;
        bInspectDailylog = false;
        replaceFragment(DriverProfileFragment.newInstance());
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.menu_driver_profile));
        previousScreen = currentScreen;
        currentScreen = Driver_Profile_Screen;
        title = getApplicationContext().getResources().getString(R.string.menu_driver_profile);
        invalidateOptionsMenu();
    }

    final Handler dialogHandler = new Handler();
    final Runnable dialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (successDialog.isShowing()) {
                successDialog.dismiss();
            }
        }
    };

    private void showMessageDialog(String message) {
        if (messageDialog == null) {
            messageDialog = new AlertDialog.Builder(this).create();
        }

        messageDialog.setCancelable(true);
        messageDialog.setCanceledOnTouchOutside(false);
        messageDialog.setTitle("E-Log");
        messageDialog.setIcon(R.drawable.ic_launcher);
        messageDialog.setMessage(message);
        messageDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Retry",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        messageDialog.cancel();
                    }
                });
        messageDialog.show();
    }

    // update diagnostic/malfunction indicator
    @Override
    public void onDiagnoticMalfunctionUpdated(boolean malfunctionFg) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
                if (loginFragment != null) {
                    loginFragment.updateDiagnosticMalfunction();
                }
            }
        });

    }

    @Override
    public void backFromLogin() {
        setDrawerState(true);
        bLogin = false;
        toolbar.setVisibility(View.VISIBLE);
        flagBar.setVisibility(View.VISIBLE);

        // handling of driver icon visibility
        updateFlagbar(true);

        toggle.setDrawerIndicatorEnabled(true);

        isOnDailyLog = true;
        bInspectDailylog = false;
        if (elogFragment == null) {
            elogFragment = new ELogFragment();
        }
        replaceFragment(elogFragment);
        //title = getApplicationContext().getResources().getString(R.string.menu_daily_log);
        previousScreen = -1;
        currentScreen = DailyLog_Screen;
        invalidateOptionsMenu();

        drawer.closeDrawer(GravityCompat.START);
        setDriverName();
    }

    @Override
    public void updateWebserviceIcon(boolean active) {
        onUpdateWebServiceIcon(active);
    }

    @Override
    public void updateSpecialCategoryChanged(boolean value) {
        specialCategoryChanged = value;
    }

    @Override
    public void activeUser() {
        int specialStatusFg = 0;
        if (currentDutyStatus >= 5) {
            ReportDB obj = new ReportDB(getApplicationContext());
            specialStatusFg = obj.getSpecialStatusFg(Utility.activeUserId);
        }

        objHutchConnect.setUnitId(specialStatusFg);

        // show dialog while driver id is setting up
        onUnitMismatchDialog(true);
        updateActiveIcon();
    }

    @Override
    public void changeUser() {
        SwitchDriver();
    }

    @Override
    public void undocking() {
        SharedPreferences prefs = getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);
        prefs.edit().putBoolean("undocking", true).commit();
        undockingMode = true;

        if (ConstantFlag.AUTOSTART_MODE) {
            AutoStartService.stopTask = true;
            stopService(new Intent(MainActivity.this, AutoStartService.class));
        }

        // if atrack device is attached in vehicle
        if (ConstantFlag.HutchConnectFg) {
            // stop atrack heart beat
            //objHutchConnect.StopAtrackHB();
            HutchConnectThreadStop();
        }

        //disable all
        bEditEvent = false;
        bEditEvent = false;
        bInspectDailylog = false;
        invalidateOptionsMenu();

        //show DockingFragment
        replaceFragment(new DockingFragment());
    }

    @Override
    public void setCertify(final int certifyFg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                icCertifyLog.setVisibility(View.VISIBLE);
                if (certifyFg == 1) {
                    // Restrict user to certify current day Logs
                  /*  boolean uncertifyFg = DailyLogDB.unCertifiedFg(Utility.onScreenUserId);
                    if (uncertifyFg)
                        icCertifyLog.setBackground(getResources().getDrawable(R.drawable.ic_fab_uncertified_red));
                    else
                        icCertifyLog.setBackground(getResources().getDrawable(R.drawable.ic_flagbar_certify));*/

                    icCertifyLog.setBackground(getResources().getDrawable(R.drawable.ic_flagbar_certify));
                } else if (certifyFg == 0) {

                    icCertifyLog.setBackground(getResources().getDrawable(R.drawable.ic_flagbar_uncertify));
                }
            }
        });
    }

    @Override
    public void changeRule(int rule) {

        currentRule = rule;
        chkRules.setChecked(currentRule < 3);

    }

    @Override
    public void autoRuleChange(final int rule) {

        currentRule = rule;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    chkRules.setChecked(currentRule < 3);
                    String country = "United States Of America";
                    if (currentRule < 3) {
                        country = "Canada";
                    }
                    String[] Rules = new String[]{getString(R.string.canada_rule_1), getString(R.string.canada_rule_2), getString(R.string.us_rule_1), getString(R.string.us_rule_2), getString(R.string.canada_rule_AB), getString(R.string.canada_logging_truck), getString(R.string.canada_oil_well_service)};

                    String ruleText = Rules[rule - 1];
                    Utility.showMsg("Welcome to " + country + ". Hutch ELD has changed your rule to " + ruleText);

                    if (Utility.onScreenUserId == Utility.activeUserId) {
                        if (elogFragment != null) {
                            elogFragment.autoChagneRule(rule);
                            elogFragment.refresh();
                        }
                    }

                } catch (Exception exe) {
                    Log.i(TAG, "AutoRuleChange: " + exe.getMessage());
                }
            }
        });

    }

    @Override
    public void autologinSuccessfully() {
        setUIModeNoBrightness();
        //update inspection icon
        boolean inspections = TripInspectionDB.getInspections(Utility.getCurrentDate(), Utility.user1.getAccountId(), Utility.user2.getAccountId());
        if (inspections) {
            GPSData.TripInspectionCompletedFg = 1;
            onUpdateInspectionIcon();
        } else {
            GPSData.TripInspectionCompletedFg = 0;
            resetInspectionIcon();

        }
        updateFlagbar(true);
        int unreadCount = MessageDB.getUnreadCount();
        if (unreadCount > 0) {
            onMessageReceived();
        }
        setCertify(1);

        if (firstLogin) {

            currentRule = DailyLogDB.getCurrentRule(Utility.activeUserId);
            chkRules.setChecked(currentRule < 3);
            // Utility.setOrientation(this);

            showSpecialCategory(false);
            // sync message
            ProcessViolation(true);
        }

        //Log.d(TAG, "loginSuccessfully");
        this.firstLogin = false;
        setDrawerState(true);
        //already login
        setDriverName();
        bLogin = false;
        int userIcon = Utility.onScreenUserId == Utility.activeUserId ? R.drawable.ic_flagbar_driver_active : R.drawable.ic_flagbar_driver_inactive;
        if (ivActiveUser != null) {
            ivActiveUser.setBackgroundResource(userIcon);
        }
        if (icFreezeActiveUser != null) {
            icFreezeActiveUser.setBackgroundResource(userIcon);
        }

        bHaveUnAssignedEvent = false;
        bHaveLogbookToCertify = false;

        int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        ArrayList<DailyLogBean> logList = DailyLogDB.getUncertifiedDailyLog(driverId);
        if (logList.size() > 0) {
            bHaveLogbookToCertify = true;
        }
        isOnDailyLog = false;

        if (undockingMode) {
            replaceFragment(new DockingFragment());
            getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_eld));
        } else if (bHaveLogbookToCertify) {
            ShowUncertifiedRecord();
        } else {
            if (elogFragment == null) {
                elogFragment = new ELogFragment();
            }
            elogFragment.setFirstLogin(firstLogin);
            replaceFragment(elogFragment);
            isOnDailyLog = true;
            previousScreen = -1;
            currentScreen = DailyLog_Screen;
            if (Utility.InspectorModeFg) {
                setInspectorMode(true);
            }
        }

        toolbar.setVisibility(View.VISIBLE);
        flagBar.setVisibility(View.VISIBLE);

    }

    @Override
    public void loginSuccessfully(boolean firstLogin) {
        setUIModeNoBrightness();
        //update inspection icon
        boolean inspections = TripInspectionDB.getInspections(Utility.getCurrentDate(), Utility.user1.getAccountId(), Utility.user2.getAccountId());
        if (inspections) {
            GPSData.TripInspectionCompletedFg = 1;
            onUpdateInspectionIcon();
        } else {
            GPSData.TripInspectionCompletedFg = 0;
            resetInspectionIcon();

        }
        updateFlagbar(true);

        setCertify(1);


        if (firstLogin) {

            currentRule = DailyLogDB.getCurrentRule(Utility.activeUserId);
            chkRules.setChecked(currentRule < 3);
            // Utility.setOrientation(this);
            showSpecialCategory(false);

            // set unit id
            objHutchConnect.setUnitId(0);

            // show dialog while driver id is setting up
            onUnitMismatchDialog(true);

            if (Utility.isInternetOn()) {
                showLoaderAnimation(true);
                new AutoSyncData(autoSyncDataPostTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            ProcessViolation(true);
        }

        //Log.d(TAG, "loginSuccessfully");
        this.firstLogin = firstLogin;
        setDrawerState(true);
        //already login
        setDriverName();
        bLogin = false;
        int userIcon = Utility.onScreenUserId == Utility.activeUserId ? R.drawable.ic_flagbar_driver_active : R.drawable.ic_flagbar_driver_inactive;
        if (ivActiveUser != null) {
            ivActiveUser.setBackgroundResource(userIcon);
        }
        if (icFreezeActiveUser != null) {
            icFreezeActiveUser.setBackgroundResource(userIcon);
        }

        bHaveUnAssignedEvent = false;
        bHaveLogbookToCertify = false;
        if (firstLogin) {
            ArrayList<EventBean> unAssignedEventList = EventDB.EventUnAssignedGet();
            if (unAssignedEventList.size() > 0) {
                bHaveUnAssignedEvent = true;
            }

            int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
            ArrayList<DailyLogBean> logList = DailyLogDB.getUncertifiedDailyLog(driverId);
            if (logList.size() > 0) {
                bHaveLogbookToCertify = true;
            }
        }

        isOnDailyLog = false;

        if (undockingMode) {
            replaceFragment(new DockingFragment());
            getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_eld));
        } else {
            if (!bHaveUnAssignedEvent && !bHaveLogbookToCertify) {
                //relogin
                if (elogFragment == null) {
                    elogFragment = new ELogFragment();
                }
                elogFragment.setFirstLogin(firstLogin);
                replaceFragment(elogFragment);
                isOnDailyLog = true;
                previousScreen = -1;
                currentScreen = DailyLog_Screen;
            } else {
                if (bHaveUnAssignedEvent) {
                    ShowUnidentifiedEvent();
                } else {
                    ShowUncertifiedRecord();
                }
            }
        }

        toolbar.setVisibility(View.VISIBLE);
        flagBar.setVisibility(View.VISIBLE);


    }

    private void ShowUnidentifiedEvent() {
        final AlertDialog alertDialog = new AlertDialog.Builder(Utility.context).create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle(Utility.context.getString(R.string.unidentified_driver_records));
        alertDialog.setIcon(DIALOGBOX_ICON);
        alertDialog.setMessage("ELD has unidentified driver records. Do you want to review these records");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, Utility.context.getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        replaceFragment(new UnidentifyFragment());
                        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_unidentified_event));
                        isOnDailyLog = false;
                        bInspectDailylog = false;
                        previousScreen = -1;
                        currentScreen = Unidentified_Event_Screen;
                    }
                });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, Utility.context.getString(R.string.no),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (bHaveLogbookToCertify) {
                            ShowUncertifiedRecord();
                        } else {
                            if (elogFragment == null) {
                                elogFragment = new ELogFragment();
                            }
                            elogFragment.setFirstLogin(firstLogin);
                            replaceFragment(elogFragment);
                            isOnDailyLog = true;
                            previousScreen = -1;
                            currentScreen = DailyLog_Screen;
                        }
                        alertDialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private void ShowUncertifiedRecord() {

        replaceFragment(new UnCertifiedFragment());
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_certify_log_book));
        isOnDailyLog = false;
        bInspectDailylog = false;
        previousScreen = -1;
        currentScreen = Uncertified_LogBook_Screen;
/*        final AlertDialog alertDialog = new AlertDialog.Builder(Utility.context).create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle(Utility.context.getString(R.string.menu_uncertified_log_book));
        alertDialog.setIcon(DIALOGBOX_ICON);
        alertDialog.setMessage("ELD has uncertified log books. Do you want to review these records");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, Utility.context.getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        replaceFragment(new UnCertifiedFragment());
                        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_certify_log_book));
                        isOnDailyLog = false;
                        bInspectDailylog = false;
                        previousScreen = -1;
                        currentScreen = Uncertified_LogBook_Screen;
                    }
                });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, Utility.context.getString(R.string.no),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (elogFragment == null) {
                            elogFragment = new ELogFragment();
                        }
                        elogFragment.setFirstLogin(firstLogin);
                        replaceFragment(elogFragment);
                        isOnDailyLog = true;
                        previousScreen = -1;
                        currentScreen = DailyLog_Screen;
                        alertDialog.cancel();
                    }
                });
        alertDialog.show();*/
    }

    public void setDrawerState(boolean isEnabled) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (isEnabled) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            toggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_UNLOCKED);
            toggle.setDrawerIndicatorEnabled(true);
            toggle.syncState();
            if (getActionBar() != null)
                getActionBar().setHomeButtonEnabled(true);

        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toggle.setDrawerIndicatorEnabled(false);
            toggle.syncState();
            if (getActionBar() != null)
                getActionBar().setHomeButtonEnabled(false);
        }
    }

    private void setDriverName() {
        try {
            if (!Utility.isLargeScreen(getApplicationContext())) {
                if (Utility.user1.isOnScreenFg()) {
                    Utility.onScreenUserId = Utility.user1.getAccountId();
                    driverName = Utility.user1.getFirstName() + (Utility.user1.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");
                    tvUserName.setText(Utility.user1.getUserName());
                } else {
                    Utility.onScreenUserId = Utility.user2.getAccountId();
                    driverName = Utility.user2.getFirstName() + (Utility.user2.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");
                    tvUserName.setText(Utility.user2.getUserName());
                }
            } else {
                if (Utility.user1.isOnScreenFg()) {
                    Utility.onScreenUserId = Utility.user1.getAccountId();
                    driverName = Utility.user1.getFirstName() + " " + Utility.user1.getLastName() + (Utility.user1.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");
                    tvUserName.setText(Utility.user1.getUserName());
                } else {
                    Utility.onScreenUserId = Utility.user2.getAccountId();
                    driverName = Utility.user2.getFirstName() + " " + Utility.user2.getLastName() + (Utility.user2.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");
                    tvUserName.setText(Utility.user2.getUserName());
                }
            }
            tvLoginName.setText("Driver: " + driverName);

            tvFreezeLoginName.setText(driverName);
        } catch (Exception exe) {
        }

    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    private static class USBHandler extends Handler {


        long lastResetTime = System.currentTimeMillis();
        private final WeakReference<MainActivity> mActivity;

        public USBHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;

                    long differ = (System.currentTimeMillis() - lastResetTime) / 1000;

                    break;
                case UsbService.USB_STATUS:
                    boolean status = (boolean) msg.obj;

                    Log.i("USBStatus", "Status: " + status);

                    break;
                case UsbService.USB_DIAGNOSTIC:
                    int statusCode = (int) msg.obj;

                    if (statusCode == UsbService.USB_ALERT_WARNING) {
                        Log.i("USBStatus", "Warning ");

                    } else if (statusCode == UsbService.USB_ALERT_ERROR) {
                        Log.i("USBStatus", "Error ");

                    } else if (statusCode == UsbService.USB_ALERT_CLEAR) {
                        Log.i("USBStatus", "Clear ");

                    }


                    break;
            }
        }

    }

    private UsbService usbService;
    private USBHandler usbHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(usbHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    // initialize USB Service
    private void initializeUSB() {

        if (Utility._appSetting.getECUConnectivity() == 2 && !UsbService.SERVICE_CONNECTED) {
            setFilters();
            startService(UsbService.class, usbConnection, null);
            //objUSB.StartUSBHB();
        }
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }


    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    // stop usb service
    private void stopUSB() {
        if (UsbService.SERVICE_CONNECTED) {
            stopService(new Intent(MainActivity.this, UsbService.class));
            //objUSB.StopUSBHB();
        }

    }

    Thread thInitializeBluetooth;

    // Created By: Deepak Sharma
    // Created Date: 26 June 2016
    // Purpose: initialize bluetooth
    private void initializeBluetooth() {
        adapter = BluetoothAdapter.getDefaultAdapter();

        if (adapter == null) {
            Utility.showMsg(getString(R.string.device_BTB_unsupport));
            return;
        }

        thInitializeBluetooth = new Thread(new Runnable() {
            public void run() {
                try {
                    if (!adapter.isEnabled()) {
                        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(turnOn, REQUEST_ENABLE_BT);
                        return;
                    }
                    CanMessages.deviceAddress = Utility.MACAddress;
                    objHutchConnect.serialNo = Utility.IMEI;
                    objHutchConnect.macAddress = Utility.MACAddress;

                    Thread.sleep(3000);

                    objHutchConnect.connect(Utility.MACAddress);

                    onAlertWarning();

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        });
        thInitializeBluetooth.setName("thInitializeBluetooth");
        thInitializeBluetooth.start();

    }

    CanMessages objCan = new CanMessages();

    // initialize atrack class
    HutchConnect objHutchConnect;

    String messageTitle = "";

    @Override
    public void navigateToMessage(int userId, String userName) {
        //create canbus fragment to show data
        previousScreen = Message;
        //call replace to show the fragment
        replaceFragmentWithBackStack(MessageFragment.newInstance(userId, userName));

        getSupportActionBar().setTitle(userName);
        messageTitle = userName;
    }

    View.OnClickListener originalToolbarListener = null;

    @Override
    public void onBackStackChanged() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            toggle.setDrawerIndicatorEnabled(false);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.hideKeyboard(MainActivity.this, MainActivity.this.getCurrentFocus());
                    String currentTitle = getSupportActionBar().getTitle().toString();
                    if (currentTitle.equals(getString(R.string.new_inspection)) || currentTitle.equals(getString(R.string.inspection)) || currentTitle.equals(getString(R.string.title_new_inspection_trailer))) {
                        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.dvir_title));
                    } else if (currentTitle.equals(getString(R.string.edit_event))) {
                        getSupportActionBar().setTitle(prevTitle);
                    } else if (currentTitle.equals(messageTitle)) {
                        getSupportActionBar().setTitle(R.string.title_Message);
                        try {
                            Utility.hideKeyboard(MainActivity.this, MainActivity.this.getCurrentFocus());
                        } catch (Exception exe) {
                        }
                        messageTitle = "";
                    } else if (currentScreen == Incident) {
                        getSupportActionBar().setTitle(R.string.title_incident_detail);
                        try {
                            Utility.hideKeyboard(MainActivity.this, MainActivity.this.getCurrentFocus());
                        } catch (Exception exe) {
                        }
                    } else if (currentScreen == NewMaintenance) {
                        getSupportActionBar().setTitle(R.string.title_maintenance_detail);
                        currentScreen = previousScreen;
                    } else if (currentScreen == ContainerDetail) {
                        getSupportActionBar().setTitle(R.string.dispatch_detail);
                        currentScreen = previousScreen;
                    } else if (currentScreen == DispatchDetail) {
                        getSupportActionBar().setTitle(R.string.drayage);
                        currentScreen = previousScreen;
                    } else if (currentScreen == SetProtocolDetail) {
                        getSupportActionBar().setTitle(R.string.settings_title);
                        currentScreen = previousScreen;

                        if (Utility.activeUserId == 0) {
                            toolbar.setVisibility(View.GONE);
                        }
                    } else if (currentScreen == FIX_BTB) {
                        getSupportActionBar().setTitle(prevTitle);
                        currentScreen = previousScreen;
                    } else if (currentScreen == UnitNoList) {
                        toolbar.setVisibility(View.GONE);

                        if (Utility.HutchConnectStatusFg) {
                            layoutAlertBTB.setVisibility(View.GONE);
                        } else {
                            layoutAlertBTB.setVisibility(View.VISIBLE);
                        }
                    }

                   /* else
                    {
                        getSupportActionBar().setTitle(title);
                    }*/
                    fragmentManager.popBackStack();
                }
            });
        } else {
            toggle.setDrawerIndicatorEnabled(true);
            toggle.setToolbarNavigationClickListener(originalToolbarListener);
        }
        toggle.syncState();
    }

    @Override
    public void newInspection(Boolean fromTrailer) {
        previousScreen = DVIR;
        NewInspectionFragment fragment = NewInspectionFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isTrailerDVIR", fromTrailer);
        fragment.setArguments(bundle);
        replaceFragmentWithBackStack(fragment);
        getSupportActionBar().setTitle(fromTrailer ? getApplicationContext().getResources().getString(R.string.title_new_inspection_trailer) : getApplicationContext().getResources().getString(R.string.title_new_inspection));

    }

    @Override
    public void viewInspection(boolean viewMode, TripInspectionBean bean) {
        previousScreen = DVIR;
        NewInspectionFragment fragment = NewInspectionFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putBoolean("view_mode", viewMode);
        bundle.putSerializable("trip_inspection", bean);
        fragment.setArguments(bundle);
        replaceFragmentWithBackStack(fragment);
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_inspection));
    }

    @Override
    public void finishInspection() {

        //Annotation Selected is dvir after finish inspection truck
        if (isDVIR) {
            //After Completion of truck Check Hooked Trailer
            ArrayList<VehicleBean> hookedList = TrailerDB.getHookedVehicleInfo();
            // in Vehicle List default added  Utility.UnitNo, Utility.vehicleId so remove it.
            hookedList.remove(0);

            //Trailer Inspection
            if (hookedList.size() > 0) {
                Utility.hideKeyboard(MainActivity.this, MainActivity.this.getCurrentFocus());
                newInspection(true);
                isTrailerDVIR = true;
            } else {
                // Daily Log Screen
                onUpdateInspectionIcon();
                Utility.hideKeyboard(MainActivity.this, MainActivity.this.getCurrentFocus());
                isOnDailyLog = true;
                bInspectDailylog = false;
                elogFragment = new ELogFragment();
                replaceFragment(elogFragment);
                previousScreen = currentScreen;
                currentScreen = DailyLog_Screen;
                title = getApplicationContext().getResources().getString(R.string.title_daily_log);
                title += " - " + Utility.convertDate(Utility.newDate(), CustomDateFormat.d10);
                ActionBar actionBar = getSupportActionBar();
                actionBar.setTitle(title);
            }

        } else if (isTrailerDVIR) {
            onUpdateInspectionIcon();
            Utility.hideKeyboard(MainActivity.this, MainActivity.this.getCurrentFocus());
            isOnDailyLog = true;
            bInspectDailylog = false;
            elogFragment = new ELogFragment();
            replaceFragment(elogFragment);
            previousScreen = currentScreen;
            currentScreen = DailyLog_Screen;
            title = getApplicationContext().getResources().getString(R.string.title_daily_log);
            title += " - " + Utility.convertDate(Utility.newDate(), CustomDateFormat.d10);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle(title);
            isTrailerDVIR = false;
        } else {
            fragmentManager.popBackStack();
            previousScreen = -1;
            getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_dvir));
        }
        isDVIR = false;


    }

    @Override
    public void stopService() {
        stopService = true;
        AutoStartService.stopTask = true;
        stopService(new Intent(MainActivity.this, AutoStartService.class));
    }

    @Override
    public void reportIssue() {
       /* if (Utility.IMEI.equals("353608073208384")) {
            restoreDB();
            return;
        }
*/
        String dbName = Utility.backupDB();

        if (!ConstantFlag.FLAG_BACKUP_DB)
            return;

        if (dbName.isEmpty())
            return;
        showLoaderAnimation(true);
        String content = "App Version: " + Utility.ApplicationVersion + "\nCompany: " + Utility.CarrierName + "\nCompanyId: " + Utility.companyId + "\nVehicleID: " + Utility.vehicleId + "\n" + "Unit No: " + Utility.UnitNo + "\n" + "IMEI: " + Utility.IMEI;
        String title = "Database from ELD: " + Utility.ApplicationVersion + " - " + Utility.IMEI + " - Unit No: " + Utility.UnitNo;
        new ReportIssue(mailListner).execute(title, content, dbName);
    }

    private void showSpecialCategory(final boolean whenLogout) {
        String specialCategory = "";
        if (Utility.user1.isOnScreenFg()) {
            specialCategory = Utility.user1.getSpecialCategory();
        } else if (Utility.user2.isOnScreenFg()) {
            specialCategory = Utility.user2.getSpecialCategory();
        }

        String message = "";
        if (specialCategory.equals("1")) {
            message = getString(R.string.special_category_configure_PU);
        } else if (specialCategory.equals("2")) {
            message = getString(R.string.special_category_configure_YM);
        } else if (specialCategory.equals("0") || specialCategory.equals("")) {
            message = getString(R.string.special_category_configure_none);
        } else if (specialCategory.equals("3")) {
            message = getString(R.string.special_category_configure_PU_YM);
        }

        final Snackbar snackbar = Snackbar.make(drawer, message, Snackbar.LENGTH_INDEFINITE);

        if (whenLogout) {
            showLogoutDialog();
        }

        snackbar.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    snackbar.dismiss();
                } catch (Exception exe) {
                }
            }
        }).start();
    }

    private void showLogoutDialog() {
        boolean unCertifyFg = DailyLogDB.getUncertifiedLogFg(Utility.onScreenUserId);
        String message = "";
        if (unCertifyFg && !Utility.InspectorModeFg) {
            message = getString(R.string.uncertified_log_exists);
        }
        final AlertDialog ad = new AlertDialog.Builder(this)
                .create();
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle(getString(R.string.logout_confirmation));
        ad.setIcon(R.drawable.ic_launcher);
        ad.setMessage(getString(R.string.logout_alert) + message);
        ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        isOnDailyLog = false;
                        if (Utility.InspectorModeFg) {
                            RedirectToLogin(Utility.user2.isOnScreenFg());
                            setInspectorMode(false);
                        } else {
                            Logout();
                        }
                    }
                });
        ad.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        ad.cancel();
                    }
                });
        ad.show();
    }


    //this receiver to use when mid night comes to refresh the Duty Status chart
    private final BroadcastReceiver dateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                LogFile.sendLogFile(LogFile.MID_NIGHT);


                Utility.UnidentifiedDrivingTime = 0;
                Utility.bEventUnidentifiedDriverDiagnostic = false;

             /*   GPSData.TripInspectionCompletedFg = 0;
                resetInspectionIcon();*/

                Utility.DeleteOldData();

                if (!ConstantFlag.HutchConnectFg)
                    PostCall.PostDeviceInfo(false);

                PostCall.PostDaily();
                GetCall.SyncMonthly();

                runDailyTask();

            } catch (Exception e) {
                LogFile.write(ELogFragment.class.getName() + "::dateChangedReceiver Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                LogDB.writeLogs(TAG, "::dateChangedReceiver Error:", e.getMessage(), Utility.printStackTrace(e));

            }
        }
    };

    // Created By: Deepak Sharma
    // Created Date: 31 Aug 2016
    // Purpose: set brightness of tablet
    private void setBrightness(int brightness) {
        try {
//            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
//            android.provider.Settings.System.putInt(getContentResolver(),
//                    android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = (float) brightness / 255;
            getWindow().setAttributes(lp);
        } catch (Exception exe) {
            exe.getMessage();
        }
    }


    // Created By: Deepak Sharma
    // Created Date: 31 Aug 2016
    // Purpose: check mode night/day and set it

    public void setUIMode() {
        if (Utility.NightModeFg) {

            tvFreeze.setBackgroundResource(R.drawable.freeze_background_night);
            flagBarFreeze.setBackgroundResource(R.drawable.flagbar_freeze_bg_night);

            setBrightness(1);
        } else {
            tvFreeze.setBackgroundResource(R.drawable.freeze_background);
            flagBarFreeze.setBackgroundResource(R.drawable.flagbar_freeze_bg);


            setBrightness(255);
        }

    }


    public void setUIModeNoBrightness() {
        if (Utility.NightModeFg) {

            tvFreeze.setBackgroundResource(R.drawable.freeze_background_night);
            flagBarFreeze.setBackgroundResource(R.drawable.flagbar_freeze_bg_night);

        } else {
            tvFreeze.setBackgroundResource(R.drawable.freeze_background);
            flagBarFreeze.setBackgroundResource(R.drawable.flagbar_freeze_bg);

        }

    }

    DialogInterface.OnClickListener onDutyChangeDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    //continue to drive
                    onDutyChangeDialogResponse = false;
                    break;


            }
        }
    };

    public void resetOnDutyChangeDialogResponse() {
        onDutyChangeDialogResponse = false;
    }


    @Override
    public void dialogDismiss() {
        try {
            ponDutyChangeDialog.close();

            onDutyChangeDialogResponse = false;
            activeCurrentDutyStatus = 4;
            //save event that it automatically change
            int driverId = Utility.activeUserId;

            int logId = DailyLogDB.getDailyLog(driverId, Utility.getCurrentDate());
            // if dailylog record does not exists for the day
            if (logId == 0) {
                logId = DailyLogDB.DailyLogCreate(driverId, Utility.ShippingNumber, Utility.TrailerNumber, "");
            }

            int eventType = 1;
            int eventCode = 4;
            String eventCodeDescription = getString(R.string.duty_status_changed_to_on_duty);
            if (Utility._appSetting.getAutoEventAfterStop() == 1) {
                eventCode = 1;
                eventCodeDescription = getString(R.string.duty_status_changed_to_off_duty);
            }

            EventDB.EventCreate(Utility.getCurrentDateTime(), eventType, eventCode, eventCodeDescription, 1, 1, logId, driverId, "", currentRule);
            DailyLogDB.DailyLogCertifyRevert(driverId, logId);
            activeCurrentDutyStatus = eventCode;
            EventDB.calculateDistance(logId);


            if (Utility.onScreenUserId == Utility.activeUserId) {
                setActiveDutyStatus(activeCurrentDutyStatus);
                if (elogFragment != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            elogFragment.refresh();
                        }
                    });
                } else {
                    ProcessViolation(true);
                    DailyLogDB.DailyLogHoursReCertify(Utility.activeUserId, logId);
                }
            } else {
                ProcessViolation(true);
                DailyLogDB.DailyLogHoursReCertify(Utility.activeUserId, logId);
            }

            // Post Event and driver info
            MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo);

            // Save and post device balance
            Utility.DeviceBalancePost();

        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::dialogDismiss Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TAG, "::dialogDismiss Error:  ", e.getMessage(), Utility.printStackTrace(e));

        }
    }


    @Override
    public void changeStatusPressed() {
        try {
            Log.d(TAG, "change status");
            onDutyChangeDialogResponse = false;
            ponDutyChangeDialog.close();
            if (elogFragment != null)
                elogFragment.launchDutyStatusChange(true);
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        callELog();
                        if (elogFragment != null)
                            elogFragment.launchDutyStatusChange(true);
                    }
                });
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::changeStatusPressed Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TAG, "::changeStatusPressed Error:  ", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void keepDrivingPressed() {
        try {
            Log.d(TAG, "keep driving");
            onDutyChangeDialogResponse = false;
            ponDutyChangeDialog.close();
            promptToChangeStatus();
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::keepDrivingPressed Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TAG, "::keepDrivingPressed Error:  ", e.getMessage(), Utility.printStackTrace(e));
        }
    }

    public void promptToAppExit(final String message, final int minute) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isAppActive) {
                        CountDownDialogFragment dialog = new CountDownDialogFragment();
                        dialog.message = message;
                        dialog.minute = minute;
                        dialog.show(fragmentManager, "countdown_dialog");
                    } else {
                        System.exit(0);
                    }
                } catch (Exception exe) {

                }
            }
        });
    }

    public void updateFlagbar(final boolean status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (status) {

                    ivActiveUser.setVisibility(View.VISIBLE);
                    icCertifyLog.setVisibility(View.VISIBLE);
                    //  icViolation.setVisibility(View.VISIBLE);
                    icInspection.setVisibility(View.VISIBLE);
                    icCertifyLog.setVisibility(View.VISIBLE);
                } else {
                    tvLoginName.setText("");
                    tvFreezeLoginName.setText("");
                    ivActiveUser.setVisibility(View.GONE);
                    icCertifyLog.setVisibility(View.GONE);
                    icViolation.setVisibility(View.GONE);
                    icMessage.setVisibility(View.GONE);
                    icInspection.setVisibility(View.GONE);
                    icCertifyLog.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void setInspectorMode(boolean status) {
        Utility.InspectorModeFg = status;
        Utility.savePreferences("inspector_mode", status);
        if (status) {
            callInspectELog();
            flagBar.setVisibility(View.GONE);
            getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.inspect_log_action_bar));
        } else {
            bInspectDailylog = false;
            getSupportActionBar().setTitle("ELD");
            flagBar.setVisibility(View.VISIBLE);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff007dc1));
            invalidateOptionsMenu();
            if (Utility.user2.isOnScreenFg()) {

                Utility.user1.setOnScreenFg(true);
                Utility.user2.setOnScreenFg(false);
            } else {
                Utility.user1.setOnScreenFg(false);
            }

        }
        bindDrawerItem();
    }

    @Override
    public void onUpdateViolation(boolean status) {
        // handling for violation icon  if Virtuafg is false and status true then  only we display the violation icon
        // int visibility = status && !MainActivity._virtualFg ? View.VISIBLE : View.GONE;
        int visibility = status ? View.VISIBLE : View.GONE;
        try {
            icViolation.setVisibility(visibility);
            icFreezeViolation.setVisibility(visibility);

            String message = "";
            if (status) {
                message = "You must stop driving! You are in violation since  " + Utility.ConverDateFormat(ViolationDT) + " for rule " + ViolationTitle;
            } else {
                message = "You must stop driving before " + Utility.ConverDateFormat(ViolationDT) + " otherwise you will be in violation for rule " + ViolationTitle;
            }

            tvViolation.setText(message);

        } catch (Exception exe) {
        }
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 1 May 2019
    // Purpose: check if annotation is dvir or fuel then display dvir fragment or fuel fragment
    @Override
    public void displayAnnotation() {
        if (iSFueling) {
            displayFuel();
        } else if (isDVIR) {
            displayDVIR(false);
        }
    }

    @Override
    public void onLoadVehicleInfo() {
        isOnDailyLog = false;
        bInspectDailylog = false;

        replaceFragment(VehicleInfoFragment.newInstance());
        previousScreen = currentScreen;
        currentScreen = VehicleInfo;
        title = getApplicationContext().getResources().getString(R.string.title_vehicle_info);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }

    @Override
    public void onLoadFuelDetail() {
        isOnDailyLog = false;
        bInspectDailylog = false;

        replaceFragment(FuelListFragment.newInstance());
        previousScreen = currentScreen;
        currentScreen = Fuel;
        title = getApplicationContext().getResources().getString(R.string.title_new_Fuel);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }

    @Override
    public void finishFuelDetail() {
        // if annotation = isDVIR then open DVIR Fragment
        Utility.hideKeyboard(MainActivity.this, MainActivity.this.getCurrentFocus());
        if (isDVIR) {
            displayDVIR(false);

        } else {
            isOnDailyLog = true;
            bInspectDailylog = false;
            elogFragment = new ELogFragment();
            replaceFragment(elogFragment);
            previousScreen = currentScreen;
            currentScreen = DailyLog_Screen;
            title = getApplicationContext().getResources().getString(R.string.title_daily_log);
            title += " - " + Utility.convertDate(Utility.newDate(), CustomDateFormat.d10);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle(title);
        }
        iSFueling = false;

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 1 May 2019
    // Purpose:  display dvir fragment
    public void displayDVIR(Boolean trailerDVIR) {
        previousScreen = DVIR;
        NewInspectionFragment fragment = NewInspectionFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isTrailerDVIR", trailerDVIR);
        fragment.setArguments(bundle);
        replaceFragment(NewInspectionFragment.newInstance());
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_new_inspection));
    }

    @Override
    public void onLoadIncidentDetail() {
        isOnDailyLog = false;
        bInspectDailylog = false;

        replaceFragment(IncidentListFragment.newInstance());
        previousScreen = currentScreen;
        currentScreen = Incident;
        title = getApplicationContext().getResources().getString(R.string.title_incident_detail);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }


    @Override
    public void onNewMaintenance(int id, int scheduleId, int dueOn) {
        previousScreen = Maintenance;

        replaceFragmentWithBackStack(NewMaintenanceFragment.newInstance(id, scheduleId, dueOn));
        getSupportActionBar().setTitle(R.string.new_maintenance);
        currentScreen = NewMaintenance;
    }

    @Override
    public void onLoadMaintenanceDetail() {
        isOnDailyLog = false;
        bInspectDailylog = false;

        replaceFragment(MaintenanceDetailFragment.newInstance());
        previousScreen = currentScreen;
        currentScreen = Maintenance;
        title = getApplicationContext().getResources().getString(R.string.title_maintenance_detail);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }

    @Override
    public void maintenaceDueMonitor() {

        if (!Utility.hasFeature("7"))
            return;
        try {
            ArrayList<ScheduleBean> list = ScheduleDB.ScheduleGet();
            ArrayList<ScheduleBean> dueList = new ArrayList<>();
            ArrayList<ScheduleBean> dueStatusUpdate = new ArrayList<>();
            for (ScheduleBean item : list) {
                ScheduleBean s = ScheduleDB.MaintenanceDueGet(item.getScheduleId());// get previous created schedule
                int effectiveOn = s.getEffectiveOn();
                int dueOn = s.getDueOn();
                int currentValue = 0;
                switch (item.getUnit()) {
                    case "Days":
                        currentValue = (int) (Utility.newDate().getTime() / (1000 * 60 * 60 * 24l));

                        Date effectiveDate = Utility.parse(item.getEffectiveDate());
                        if (s.getScheduleId() == 0)
                            effectiveOn = (int) (effectiveDate.getTime() / (1000 * 60 * 60 * 24l));
                            /*Date dueDate = Utility.addDays(effectiveDate, item.getThreshold());
                            dueOn = (int) dueDate.getTime() / (1000 * 60 * 60 * 24);*/
                        break;
                    case "Km":
                        currentValue = (int) Double.parseDouble(CanMessages.OdometerReading);
                        break;
                    case "Miles":
                        currentValue = (int) Double.parseDouble(CanMessages.OdometerReading) * 62137 / 100000;// km to miles
                        break;
                    case "Engine Hours":
                        currentValue = (int) Double.parseDouble(CanMessages.EngineHours);
                        break;
                }

                if (s.getScheduleId() == 0) {
                    effectiveOn = item.getUnit().equals("Days") ? effectiveOn : currentValue;
                    dueOn = effectiveOn + item.getThreshold();

                    ScheduleBean due = new ScheduleBean();
                    due.setScheduleId(item.getScheduleId());
                    due.setEffectiveOn(effectiveOn);
                    due.setDueOn(dueOn);
                    due.setDueStatus(0);
                    dueList.add(due);
                } else {
                    if (currentValue >= dueOn) {
                        if (s.getDueStatus() < 2) {
                            ScheduleBean due = new ScheduleBean();
                            due.setScheduleId(s.getScheduleId());
                            due.setDueOn(s.getDueOn());
                            due.setDueStatus(2);
                            dueStatusUpdate.add(due);
                        }

                        ScheduleBean due = new ScheduleBean();

                        effectiveOn = currentValue;
                        dueOn = currentValue + item.getThreshold();

                        due.setScheduleId(item.getScheduleId());
                        due.setEffectiveOn(effectiveOn);
                        due.setDueOn(dueOn);
                        due.setDueStatus(0);
                        dueList.add(due);
                    } else if (dueOn - currentValue <= item.getThreshold() / 10) {
                        if (s.getDueStatus() == 0) {

                            ScheduleBean due = new ScheduleBean();
                            due.setScheduleId(s.getScheduleId());
                            due.setDueOn(s.getDueOn());
                            due.setDueStatus(1);
                            dueStatusUpdate.add(due);
                        }
                    }
                }
            }

            if (dueList.size() > 0) {
                ScheduleDB.MaintenanceDueSave(dueList);
            }

            if (dueStatusUpdate.size() > 0) {
                ScheduleDB.MaintenanceDueUpdate(dueStatusUpdate);
            }
        } catch (Exception exe) {

        }
    }

    public void showMaintenanceAlert() {
        final int dueStatus = ScheduleDB.MaintenanceDueStatusGet();

        if (dueStatus != 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String maintenance = "Upcoming";
                    if (dueStatus == 2) {
                        maintenance = "Overdue";
                    }
                    Utility.showAlertMsg("Vehicle has " + maintenance + " Maintenance!");
                }
            });
        }
    }

    @Override
    public void onLoadDocumentDetailList() {
        isOnDailyLog = false;
        bInspectDailylog = false;

        replaceFragment(DocumentDetailListFragment.newInstance());
        previousScreen = currentScreen;
        currentScreen = DocumentDetail;
        title = getString(R.string.document_detail);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }

    @Override
    public void onLoadRuleList() {
        isOnDailyLog = false;
        bInspectDailylog = false;

        replaceFragment(RuleListFragment.newInstance());
        previousScreen = currentScreen;
        currentScreen = RuleScreen;
        title = getString(R.string.rule_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }


    @Override
    public void onLoadRecap() {
        isOnDailyLog = false;
        bInspectDailylog = false;

        replaceFragment(RecapFragment.newInstance());
        previousScreen = currentScreen;
        currentScreen = RuleScreen;
        title = getString(R.string.recap);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }

    @Override
    public void onLoadHelp() {
        isOnDailyLog = false;
        bInspectDailylog = false;

        replaceFragment(HelpFragment.newInstance());
        previousScreen = currentScreen;
        currentScreen = HelpDetail;
        title = getString(R.string.help);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }

    @Override
    public void onAddDocumentDetail() {
        replaceFragmentWithBackStack(DocumentDetailFragment.newInstance());
    }

    int REQUEST_NAVIGATION_CODE = 90;

    @Override
    public void onLoadNewIncident(int id, int type, String docType) {
        isOnDailyLog = false;
        bInspectDailylog = false;

        replaceFragmentWithBackStack(AddNoticeOrderFragment.newInstance(id, type, docType));
        previousScreen = currentScreen;
        currentScreen = Incident;
        title = getApplicationContext().getResources().getString(R.string.title_incident_detail);
        ActionBar actionBar = getSupportActionBar();
        if (type == 0) {
            title = getString(R.string.add_notice_order);
        } else if (type == 1) {
            title = getString(R.string.cvsa_inspection);
        } else {
            title = getString(R.string.violation_ticket);
        }

        actionBar.setTitle(title);
    }

    int currentSelected = 0;
    String textToShare = "";

    @Override
    public void onReferFriend() {
        UserBean user = Utility.user1;
        if (Utility.user2.isOnScreenFg()) {
            user = Utility.user2;
        }


        String fullName = user.getLastName() + " " + user.getFirstName();

        //String textToShare ="Your friend "+fullName+ " refers you to install Hutch Connect by Hutch Systems. Please <a href=\"http://www.hutchSystems.com/" + Utility.ReferralCode + "\">Click Here</a> to order Hutch Hutch Connect";
        textToShare = "Your friend " + fullName + " refers you to install Hutch Connect by Hutch Systems. Please click " + Utility.ReferralLink + " to order Hutch Hutch Connect";
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        /* PCBuilder.setTitle("Refer a friend").setIcon(ContextCompat.getDrawable(this,R.drawable.ic_launcher));*/
        builder.setCancelable(false);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.refer_friend, null);

        final LinearLayout llContacts = (LinearLayout) view.findViewById(R.id.llContacts);
        final LinearLayout llEmail = (LinearLayout) view.findViewById(R.id.llEmail);
        final LinearLayout llWhatsapp = (LinearLayout) view.findViewById(R.id.llWhatsapp);
        final LinearLayout llFacebook = (LinearLayout) view.findViewById(R.id.llFacebook);
        final ImageView imgCancel = (ImageView) view.findViewById(R.id.imgCancel);

        builder.setView(view);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        //send the message to contacts
        llContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


               /* Intent intent1 = new Intent(Intent.ACTION_PICK);
                intent1.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                intent1.putExtra("message", textToShare);
                startActivityForResult(intent1, REFER_FRIEND_INBOX_MESSAGE);*/
            }
        });
        //send the message to configured email contacts
        llEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(Intent.ACTION_SENDTO); // it's not ACTION_SEND
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Refer a Friend");
                    intent.putExtra(Intent.EXTRA_TEXT, textToShare);
                    intent.setData(Uri.parse("mailto:")); // or just "mailto:" for blank
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            }
        });
        //send the message to whatsapp contacts
        llWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (appInstalledOrNot("com.whatsapp")) {
                        Intent sendIntent = new Intent("android.intent.action.MAIN");
                        sendIntent.setAction(Intent.ACTION_SEND);

                        sendIntent.setType("text/plain");
                        sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
                        sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators("91**********") + "@s.whatsapp.net");
                        sendIntent.setPackage("com.whatsapp");
                        startActivity(sendIntent);
                    } else {
                        Toast.makeText(MainActivity.this, "WhatsApp not installed", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "WhatsApp not installed", Toast.LENGTH_LONG).show();
                }
            }
        });

        //send the message to facebook contacts
        llFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent
                        .putExtra(Intent.EXTRA_TEXT,
                                textToShare);
                sendIntent.setType("text/plain");
                sendIntent.setPackage("com.facebook.orca");
                try {
                    startActivity(sendIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "Facebook mesenger not installed", Toast.LENGTH_LONG).show();
                }


            }
        });


    }

    private boolean appInstalledOrNot(String packageName) {


        try {
            getApplicationContext().getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

    }

    @Override
    public void onCheckBalance() {

        //CheckBalance(true);
    }


    static UploadFileToServer.IFileProgress fileUploadListner = new UploadFileToServer.IFileProgress() {

        @Override
        public void onUploadStart() {

        }

        @Override
        public void onFileUploading(int progress) {

        }

        @Override
        public void onFileUploaded(boolean status) {

        }

    };

    ReportIssue.IMailProgress mailListner = new ReportIssue.IMailProgress() {


        @Override
        public void onMailSent(boolean status) {
            showLoaderAnimation(false);
        }
    };


    @Override
    public void onSignatureUpload(final String data, final String path) {
        Thread thSignatureUpload = new Thread(new Runnable() {
            @Override
            public void run() {


                try {
                    if (Utility.isInternetOn()) {

                        String filename = path.substring(path.lastIndexOf("/") + 1);
                        JSONObject obj = new JSONObject();

                        obj.put("fileType", DocumentType.DOCUMENT_SIGNATURE);
                        obj.put("fileName", filename);
                        obj.put("fileData", data);

                        PostCall.PostSignature(obj.toString());
                    }

                } catch (Exception e) {
                    Utility.printError(e.getMessage());
                }
            }
        });
        thSignatureUpload.setName("thSignatureUpload");
        thSignatureUpload.start();
    }

    @Override
    public void onUploadDocument() {
        if (Utility.isInternetOn()) {
            DocumentBean doc = DocumentDB.Get();
            if (doc.get_id() > 0)
                new UploadFileToServer(fileUploadListner, false).execute(doc.getType(), doc.getPath());
        }
    }

    @Override
    public void onUploadDocument(String type, String path) {
        if (Utility.isInternetOn()) {
            new UploadFileToServer(fileUploadListner, true).execute(type, path);
        } else {
            DocumentDB.Save(type, path);
        }
    }

    public static DeviceInfo DeviceInfoGet() {
        DeviceInfo bean = new DeviceInfo();
        try {


            bean.setBtbMac(Utility.MACAddress);
            String osVersion = Build.VERSION.RELEASE + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
            bean.setOSVersion(osVersion);
            bean.setDeviceModel(Build.MANUFACTURER + " " + Build.MODEL);
            WifiManager wifiManager = (WifiManager) Utility.context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            String ssid = info.getSSID();
            bean.setWifiSSID(ssid);

            long uptime = SystemClock.elapsedRealtime() / (60 * 1000);
            bean.setDeviceUptime(uptime + "");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                NetworkStatsManager networkStatsManager = (NetworkStatsManager) Utility.context.getSystemService(Context.NETWORK_STATS_SERVICE);

                NetworkStatsHelper helper = new NetworkStatsHelper(networkStatsManager, (Activity) Utility.context);
                String[] dataUsage = helper.getDataUsageAllApp();
                if (dataUsage.length == 3) {
                    bean.setDataUsageApp(dataUsage[0]);
                    bean.setTotalDataUsage(dataUsage[1]);
                    bean.setEldDataUsage(dataUsage[2]);
                }
            }
            bean.setSerialNo(Utility.IMEIDeviceGet(Utility.context));
            bean.setBatteryHealth(Utility.getBatteryHealth());
            bean.setStorage(Utility.megabytesAvailable(Environment.getExternalStorageDirectory()) + "");
            TelephonyManager tm = (TelephonyManager)
                    Utility.context.getSystemService(Context.TELEPHONY_SERVICE);
            bean.setSimCardNo(tm.getSimSerialNumber());
            bean.setTelephoneNo(tm.getLine1Number());
        } catch (SecurityException se) {

        } catch (Exception exe) {

        }

        return bean;
    }

    // Created By: Deepak Sharma
    // Modified Date: 8 May 2020
    // Purpose: get device info in json object format to post to server
    public static JSONObject DeviceInfoGetSync() {

        DeviceInfo info = DeviceInfoGet();
        JSONObject obj = new JSONObject();
        try {
            obj.put("CreatedDate", Utility.getCurrentDateTime());
            obj.put("VehicleId", Utility.vehicleId);
            obj.put("CompanyId", Utility.companyId);
            obj.put("SimCardNo", info.getSimCardNo());
            obj.put("TelephoneNo", info.getTelephoneNo());
            obj.put("Storage", info.getStorage());
            obj.put("BatteryHealth", info.getBatteryHealth());
            obj.put("BtbMac", info.getBtbMac());
            obj.put("WifiSSID", info.getWifiSSID());
            obj.put("DeviceUptime", info.getDeviceUptime());
            obj.put("DataUsageApp", info.getDataUsageApp());
            obj.put("TotalDataUsage", info.getTotalDataUsage());
            obj.put("EldDataUsage", info.getEldDataUsage());
            obj.put("AppVersion", Utility.ApplicationVersion);

            obj.put("DeviceModel", info.getDeviceModel());
            obj.put("OSVersion", info.getOSVersion());
            obj.put("SerialNo", info.getSerialNo());
        } catch (Exception exe) {

        }

        Log.i("dataUsage:  ", obj.toString());
        return obj;

    }

    @Override
    protected void onStart() {
        super.onStart();
        Utility.showMsg("Onstart");
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    public static ReverseGeoCoder objReverseGeocoder;

    // Created By: Deepak Sharma
    // Created Date: 5 September 2018
    // Purpose: initialize object for reverse geocode
    private void initializeReverseGeoCode() {
        try {
            Log.i("ReverseGeocode", "ReverseGeocode: initialize Start");
            objReverseGeocoder = new ReverseGeoCoder();

            InputStream indexStream = getAssets().open("Locations/index.txt");
            objReverseGeocoder.initialize(indexStream);
            objReverseGeocoder.loadedFg = true;
            indexStream.close();

            // load address
            Utility.callGeocodeTask();

            Utility.showMsg("Nearest Distance Module is Initialized!!!");
            Log.i("ReverseGeocode", "ReverseGeocode: initialize End");
        } catch (Exception exe) {
            Log.e(TAG, "initializeReverseGeoCode: " + exe.getMessage());
        }
    }

    private void runDailyTask() {
        /*if (!Utility.hasFeature("7"))
            return;*/

        Thread thDailyTask = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(15 * 1000);
                    if (Utility.vehicleId == 0) {
                        LogFile.write(TAG + " ELD Exit. Vehicle info is not found..", LogFile.ERROR_LOG, LogFile.ERROR_LOG);
                        promptToAppExit("Device Info is missing, application will exit in..", 1);
                    } else {

                        showMaintenanceAlert();
                        Thread.sleep(45 * 1000);
                        maintenaceDueMonitor();
                    }
                } catch (Exception exe) {
                    LogDB.writeLogs(TAG, "::AOBRD Exit. Vehicle info is not found:", exe.getMessage(), Utility.printStackTrace(exe));
                }

            }
        });
        thDailyTask.setName("thDailyTask");
        thDailyTask.start();
    }

    @Override
    public void copyEldConfig() {
        try {
            if (Utility.EldConfig != null && !Utility.EldConfig.isEmpty()) {
                LogFile.writeELDConfig();
            } else {
                LogFile.deleteEldConfigFile();
            }
        } catch (Exception exe) {

        }
    }

    @Override
    public void setProtocol() {

        previousScreen = currentScreen;
        currentScreen = SetProtocolDetail;
        toolbar.setVisibility(View.VISIBLE);
        CanBusDataFragment fragment = new CanBusDataFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("afterSetupFg", true);
        fragment.setArguments(bundle);
        replaceFragmentWithBackStack(fragment);
        getSupportActionBar().setTitle("Vehicle Data");
    }

    @Override
    public void onNextToCanbusRead() {
        try {
            fragmentManager.popBackStack();
            currentScreen = previousScreen;
        } catch (Exception exe) {
        }
    }

    @Override
    public void onNextFromCanBusDataFragment() {
        isOnDailyLog = false;
        bInspectDailylog = false;
        replaceFragment(SettingsFragment.newInstance());
        getSupportActionBar().setTitle(getString(R.string.settings_title));
        title = getString(R.string.settings_title);
        previousScreen = currentScreen;
        currentScreen = Setting_Screen;
    }

    // Hours of service logic starts
    public static ArrayList<DutyStatus> dutyStatusArrayList = new ArrayList<>();
    public static ArrayList<ViolationModel> violationList = new ArrayList<>();
    public static Date ViolationDT;
    public static String ViolationTitle, ViolationDescription;
    public static int ViolationId = 0;
    public static boolean _violationFg = false, _SplitFg = false;
    private static ArrayList<DutyStatus> ruleList = new ArrayList<>();

    // Created By: Deepak Sharma
    // Created Date: 12 November 2018
    // Purpose: process violations and remaining hours
    public static void ProcessViolation(boolean dutyStatusRefreshFg) {

        String startDate = Utility.getPreviousDateOnly(-23) + " 00:00:00";

        Log.i("HOS", "HOS-1");

        // refresh duty status for last 15 days
        if (dutyStatusRefreshFg) {
            dutyStatusArrayList = EventDB.DutyStatusGet(startDate, Utility.onScreenUserId, Utility.context);

            ruleList = EventDB.DutyStatusByRuleGet(startDate, Utility.onScreenUserId, Utility.context, dutyStatusArrayList);
        }
        Log.i("HOS", "HOS-2");

        // check current rule of on screen user
        int currentRuleOnScreen = DailyLogDB.getCurrentRule(Utility.onScreenUserId);

        // current date as per driver time zone
        Date currentDate = Utility.newDate();

        // if current duty status is driving then add 24 hours day to end date to get future violation
        int futureTIme = 14 * 60; // this time is in minutes

        // this is new end date
        Date endDate = Utility.addMinutes(currentDate, futureTIme);

        int newSplitSleepUsa = Utility._appSetting.getNewsplitSleepUSA();

        if (newSplitSleepUsa == 0 && currentDate.after(Utility.parse("2020-09-29 00:00:00"))) {
            newSplitSleepUsa = 1;
            Utility._appSetting.setNewsplitSleepUSA(newSplitSleepUsa);
            Utility.savePreferences("new_us_split_sleep_rule", Utility._appSetting.getNewsplitSleepUSA());
        }

        // initialize violation object pass start date, end date and duty status list
        // added 1 days to future to predict future violations
        Violation obj = new Violation(Utility.parse(startDate), endDate, dutyStatusArrayList, ruleList, futureTIme, currentRuleOnScreen, newSplitSleepUsa);

        // calculate hours
        violationList = obj.Calculate();


        // it is the datetime after we will consider current violation
        // some times there are multiple violation occurs
        Date currentViolationDate = currentDate;
        if (currentDutyStatus == 3) {
            if (dutyStatusArrayList.size() > 0) {
                currentViolationDate = dutyStatusArrayList.get(dutyStatusArrayList.size() - 1).getEventDateTime();

                try {
                    // check if previous duty status are same as current duty status
                    for (int j = MainActivity.dutyStatusArrayList.size() - 2; j >= 0; j--) {
                        DutyStatus item = MainActivity.dutyStatusArrayList.get(j);

                        if (item.getStatus() != 3) {
                            currentViolationDate = MainActivity.dutyStatusArrayList.get(j + 1).getEventDateTime();
                            break;
                        }
                    }
                } catch (Exception exe) {

                }
            }
        }
        // check if violation exists in list
        for (ViolationModel vBean : violationList) {
            if (!vBean.getViolationDate().before(currentViolationDate) || (Utility.getDate(vBean.getViolationDate(), 0).equals(Utility.newDateOnly()) && vBean.getRule().equals("63(2)(a)"))) {
                String rule = vBean.getRule();

                // These are day violation which does not affect another day. if violation date is other than current date then we will skip this violation
                if ((rule.equals("12A") || rule.equals("12B") || rule.equals("14A") || rule.equals("14C")) && Utility.getDate(vBean.getViolationDate(), 0).after(Utility.newDateOnly()))
                    continue;

                _violationFg = true;
                ViolationDT = vBean.getViolationDate();
                ViolationTitle = vBean.getRule() + ": " + vBean.getTitle();
                ViolationDescription = vBean.getDescription();
                // get violation id
                ViolationId = Utility.getViolationId(vBean.getRule());

                // only show violation as per current selected rule
                if ((currentRuleOnScreen <= 2 && vBean.getRuleId() > 2) || (currentRuleOnScreen > 2 && vBean.getRuleId() <= 2))
                    continue;

                break;
            }
        }


        // canada hours
        DurationRepository objCanada = obj.HoursGet(true);

        // total on duty time as per canada rule
        // subtract future minute added to to date as it would be added in work shift hours
        double canadaOnDuty = (objCanada.getRuleOnDuty() / 60d) - futureTIme;

        // us hours
        DurationRepository objUS = obj.HoursGet(false);

        // populate this field to show alert to driver only when he has not set split rule
        _SplitFg = currentRuleOnScreen <= 2 ? objCanada.splitFg : objUS.splitFg;

        // total on duty time as per us rule
        // subtract future minute added to to date as it would be added in work shift hours
        double usOnDuty = (objUS.getRuleOnDuty() / 60d) - futureTIme;

        // subtract future minute added to to date as it would be added in work shift hours
        double workShift = (((currentRuleOnScreen == 1 || currentRuleOnScreen == 2) ? (objCanada.totalWorkshift) : (objUS.totalWorkshift)) / 60d) - futureTIme;
        double workShiftOnduty = (((currentRuleOnScreen == 1 || currentRuleOnScreen == 2) ? (objCanada.workShiftOnDuty) : (objUS.workShiftOnDuty)) / 60d) - futureTIme;
        double workShiftdriving = (((currentRuleOnScreen == 1 || currentRuleOnScreen == 2) ? (objCanada.workShiftDriving) : (objUS.workShiftDriving)) / 60d) - futureTIme;
        double drivingWithoutBreak = (((currentRuleOnScreen == 1 || currentRuleOnScreen == 2) ? (objCanada.drivingWithoutBreak) : (objUS.drivingWithoutBreak)) / 60d) - futureTIme;

        // time remaining as per canada cycle 1
        GPSData.TimeRemaining70 = (int) Math.round(70 * 60 - canadaOnDuty);
        if (GPSData.TimeRemaining70 < 0) GPSData.TimeRemaining70 = 0;

        // time remaining as per canada cycle 2
        GPSData.TimeRemaining120 = (int) Math.round(120 * 60 - canadaOnDuty);
        if (GPSData.TimeRemaining120 < 0) GPSData.TimeRemaining120 = 0;

        // time remaining as per US 70Hours/8 Days rule
        GPSData.TimeRemainingUS70 = (int) Math.round(70 * 60 - usOnDuty);
        if (GPSData.TimeRemainingUS70 < 0) GPSData.TimeRemainingUS70 = 0;

        // time remaining as per US 60Hours/7 Days rule
        GPSData.TimeRemainingUS60 = (int) Math.round(60 * 60 - usOnDuty);
        if (GPSData.TimeRemainingUS60 < 0) GPSData.TimeRemainingUS60 = 0;

        // workshift remaing as per current rule
        // workshift hours in canada are 16 and in US 14
        GPSData.DrivingWithoutBreak = (int) Math.round(((currentRuleOnScreen == 1 || currentRuleOnScreen == 2 ||
                currentRuleOnScreen == 7) ? 13 : 8)
                * 60d - drivingWithoutBreak);
        GPSData.DrivingRemaining = (int) Math.round(((currentRuleOnScreen == 1 || currentRuleOnScreen == 2 ||
                currentRuleOnScreen == 7) ? 13 : 11)
                * 60d - workShiftdriving);
        GPSData.OnDutyRemaining = (int) Math.round(((currentRuleOnScreen == 1 || currentRuleOnScreen == 2 ||
                currentRuleOnScreen == 7) ? 14 : 11)
                * 60d - workShiftOnduty);
        GPSData.WorkShiftRemaining = (int) Math.round(((currentRuleOnScreen == 1 || currentRuleOnScreen == 2 ||
                currentRuleOnScreen == 7) ? 16 : (currentRuleOnScreen == 5 || currentRuleOnScreen == 6 ? 15 : 14)) * 60d - workShift);
        if (GPSData.WorkShiftRemaining < 0) GPSData.WorkShiftRemaining = 0;
        if (GPSData.DrivingRemaining < 0) GPSData.DrivingRemaining = 0;
        if (GPSData.OnDutyRemaining < 0) GPSData.OnDutyRemaining = 0;
        if (GPSData.DrivingWithoutBreak < 0) GPSData.DrivingWithoutBreak = 0;
    }

    // Created By: Pallavi Wattamwar
    // Purpose:reset Inspection Icon on First Duty Status
    public void resetIconOnFirstDutyStatus() {
        // As Per Requirement Rest Inspection icon on the First duty status Change of that day event must be offduty,PC,SB, YM,D,ON Duty
        Date nowInspectionDate = Utility.newDateOnly();

        // Check for next day
        if (currentInspectionDate.before(nowInspectionDate)) {
            // triggers method every minutes need to discuss

            // get current day's duty status if Duty Status list size 2 means Current Day Duty Status is present
            if (HourOfServiceDB.DutyStatusGetToday(Utility.user1.getAccountId(), Utility.user2.getAccountId()).size() == 2) {

                // When First Duty Status change
                if (currentDutyStatus == 4) {
                    // Check inspection is completed
                    boolean inspections = TripInspectionDB.getInspections(Utility.getCurrentDate(), Utility.user1.getAccountId(), Utility.user2.getAccountId());
                    if (inspections) {
                        GPSData.TripInspectionCompletedFg = 1;
                        onUpdateInspectionIcon();
                    } else {
                        GPSData.TripInspectionCompletedFg = 0;
                        resetInspectionIcon();

                    }
                } else {
                    // Reset inspection icon
                    resetInspectionIcon();
                    GPSData.TripInspectionCompletedFg = 0;

                }
                currentInspectionDate = nowInspectionDate;
            }

        }
    }

    @Override
    public void finishCTPATInspection() {
        fragmentManager.popBackStack();
        previousScreen = -1;
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.ctpat_list_title));

    }

    @Override
    public void newCTPATInspection() {

        previousScreen = CTPATInspectionList;
        CTPATInspectionFragment fragment = CTPATInspectionFragment.newInstance();
        replaceFragmentWithBackStack(CTPATInspectionFragment.newInstance());
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.ctpat_title));

    }

    @Override
    public void viewCTPATInspection(boolean viewMode, CTPATInspectionBean bean) {
        previousScreen = CTPATInspectionList;
        CTPATInspectionFragment fragment = CTPATInspectionFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putBoolean("view_mode", viewMode);
        bundle.putSerializable("ctpat_inspection", bean);
        fragment.setArguments(bundle);
        replaceFragmentWithBackStack(fragment);
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.ctpat_title));

    }

    public static int connectRequest = 1;
    private static int tryCount = 0;
    Thread thHutchConnect = null;

    private void HutchConnectThreadStop() {
        if (thHutchConnect != null) {
            thHutchConnect.interrupt();
            thHutchConnect = null;
        }
    }

    private void HutchConnectThread() {
        if (!ConstantFlag.HutchConnectFg)
            return;

        if (thHutchConnect != null) {
            HutchConnectThreadStop();
        }

        thHutchConnect = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean firstFg = true;
                while (true) {
                    try {
                        if (Thread.interrupted()) {
                            break;
                        }
                        // on application loading first time wait 10 second so that hutch connect connects
                        // else check every second for hutch connection
                        Thread.sleep((firstFg ? 30 : 1) * 1000);
                        firstFg = false;

                        if (MainActivity.undockingMode)
                            continue;

                        // reconnect logic for bluetooth
                        if (!Utility.HutchConnectStatusFg && Utility._appSetting.getECUConnectivity() == 1) {
                            if (connectRequest == 60) {
                                connectRequest = 1;
                                if (tryCount <= 3) {
                                    // Attempt to connect to the device
                                    objHutchConnect.connect(Utility.MACAddress);

                                    // commented this condition to try it forever
                                    // didn't remove if clause cause it may be required in future again
                                    //tryCount++;

                                }
                            } else {
                                connectRequest++;
                            }
                        }

                    } catch (InterruptedException exe) {
                        break;
                    } catch (Exception exe) {
                        break;
                    }

                }
            }
        });
        thHutchConnect.start();
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2020
    // Purpose: check balance of device
    private void CheckBalance(final boolean show) {

        // if internet is not available don't check update
        if (!Utility.isInternetOn())
            return;

        if (show) {
            showLoaderAnimation(true);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String balance = GetCall.DeviceBalanceGet();

                if (balance != null && !balance.isEmpty())
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showLoaderAnimation(false);
                            BalanceMonitor();

                            if (show) {
                                balanceAlertLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    });
            }
        }).start();
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2020
    // Purpose: check balance of device
    private void BalanceMonitor() {
        double balance = Double.parseDouble(Utility.getPreferences("RemainingBalance", ""));
        if (balance <= 0d) {
            viewBalanceAlert.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
            tvBalanceAlertTitle.setTextColor(ContextCompat.getColor(this, R.color.red));
            ivBalanceAlert.setImageResource(R.drawable.ic_flagbar_violation);

            tvBalanceAlertTitle.setText("Device balance is over");
            tvBalance.setText("Please recharge your device ");
            balanceAlertLayout.setVisibility(View.VISIBLE);
        } else if (balance < 30d) {
            viewBalanceAlert.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow2));
            tvBalanceAlertTitle.setTextColor(ContextCompat.getColor(this, R.color.yellow2));
            ivBalanceAlert.setImageResource(0);

            tvBalanceAlertTitle.setText("Device balance is very low");
            tvBalance.setText("Your device balance is " + String.format("$%.2f", balance));
            balanceAlertLayout.setVisibility(View.VISIBLE);
        } else {
            viewBalanceAlert.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
            tvBalanceAlertTitle.setTextColor(ContextCompat.getColor(this, R.color.green));

            // we won't show any icon if everything is fine
            ivBalanceAlert.setImageResource(0);

            tvBalanceAlertTitle.setText("Device Balance");
            tvBalance.setText("Your device balance is " + String.format("$%.2f", balance));
            balanceAlertLayout.setVisibility(View.GONE);
        }

    }

    ConstraintLayout balanceAlertLayout;
    ImageView ivBalanceAlert;
    TextView tvBalanceAlertTitle, tvBalance;
    View viewBalanceAlert;
    Button btnRecharge, btnCheckBalance;

    // Created By: Deepak Sharma
    // Created Date: 14 January 2020
    // Purpose: initialize View related to balance alert layout
    private void initBalanceAlertLayout() {

        balanceAlertLayout = (ConstraintLayout) findViewById(R.id.layoutBalanceAlert);
        ivBalanceAlert = (ImageView) findViewById(R.id.ivBalanceAlert);
        tvBalanceAlertTitle = (TextView) findViewById(R.id.tvBalanceAlertTitle);
        tvBalance = (TextView) findViewById(R.id.tvBalance);
        viewBalanceAlert = (View) findViewById(R.id.viewBalanceAlert);

        btnRecharge = (Button) findViewById(R.id.btnRecharge);
        btnRecharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rechargeDevice();
            }
        });

        btnCheckBalance = (Button) findViewById(R.id.btnCheckBalance);
        btnCheckBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //CheckBalance(true);
            }
        });

        balanceAlertLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double balance = Double.parseDouble(Utility.getPreferences("RemainingBalance", "0"));

                // only hide alert if balance is greater than 0
                if (balance > 0)
                    balanceAlertLayout.setVisibility(View.GONE);
            }
        });


    }


    // Created by:Simran
    // Created date: 24/01/2020
    // purpose: recharge device popup
    public void rechargeDevice() {
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.recharge_popup, null);

        // init recharge popup views
        ImageView imgCancel = view.findViewById(R.id.imgCancel);
        TextView tvDeviceId = view.findViewById(R.id.tvDeviceId);
        final EditText etAmount = view.findViewById(R.id.etAmount);
        Button btnProceed = view.findViewById(R.id.btnProceed);

        // set device id
        tvDeviceId.append(" " + Utility.IMEI);
        builder.setView(view);
        final androidx.appcompat.app.AlertDialog alertDialog = builder.create();

        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etAmount.getText().toString().trim().equals("")) {
                    Utility.showMsg("Please enter amount");
                } else if (Double.parseDouble(etAmount.getText().toString()) < 30d) {
                    Utility.showMsg("Minimum amount should be at lease $30");
                } else {
                    //open link in browser
                    String url = Utility.RechargeLink;
                    url = String.format(url, Utility.IMEI, etAmount.getText().toString());
                    Log.i("RechargeUrl", url);

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }

            }
        });

        alertDialog.show();

    }

    // save report get from server
    @Override
    public void onReportGet() {

    }

    // Created By: Deepak Sharma
    //
    @Override
    public void showPCDialog(final ReportBean data) {
        if (!Utility.PCDialogFg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FragmentManager manager = getSupportFragmentManager();
                        FragmentTransaction ft = manager.beginTransaction();
                        Fragment prev = manager.findFragmentByTag("pc_dialog");
                        if (prev != null) {
                            ft.remove(prev);
                            //ft.addToBackStack(null);
                            ft.commitNow();
                            ft = manager.beginTransaction();
                        }

                        Log.d("onDataReceived", "Show dialog1:");
                        PCDialogFragment pcDialogFragment = new PCDialogFragment(data);

                        pcDialogFragment.show(ft, "pc_dialog");
                    } catch (Exception e) {
                        Log.d("onDataReceived", "Show dialog error1:" + e.getMessage());
                    }
                    Utility.PCDialogFg = true;

                }
            });
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 19 Feb 2020
    // Purpose: fire when vehicle is in motion after engine power off cycle
    @Override
    public void hidePCDialog() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                Fragment prev = manager.findFragmentByTag("pc_dialog");
                if (prev != null) {
                    ft.remove(prev);
                    ft.commitNow();
                }
            }
        });
    }

    // Created By: Deepak Sharma
    // Created Date: 14 Feb 2020
    // Purpose: fire when vehicle motion change
    // while moving it shows freeze screen
    // when stops it hide freeze screen
    @Override
    public void onVehicleMotionChange(final boolean motionFg) {

        final int totalDistance = EventDB.calculateDistance();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (motionFg) {

                    // disable dedit on motion
                    if (Utility._appSetting.getdEditFg() == 1) {
                        Utility._appSetting.setdEditFg(0);
                        SettingsDB.update("DEditFg", "0", Utility.onScreenUserId);
                    }

                    // user is not loginned
                    if (Utility.activeUserId == 0) {
                        //login screen
                        tvLoginFreeze.setVisibility(View.VISIBLE);
                        tvGauge.setVisibility(View.INVISIBLE);
                        ll_enginehours.setVisibility(View.INVISIBLE);
                        ll_drivingRemaning.setVisibility(View.INVISIBLE);
                        tvFreeze.setVisibility(View.VISIBLE);
                        ivDrivingImage.setVisibility(View.VISIBLE);
                        tvFreeze.setBackgroundColor(getResources().getColor(R.color.red));
                        chkRules.setVisibility(View.GONE);
                        flagBar.setVisibility(View.VISIBLE);
                        setDrawerState(false);
                        //   violationLayout.setVisibility(View.GONE);
                    } else if (Utility.onScreenUserId == Utility.activeUserId) {

                        showDashboard();
                    }
                } else {

                    if (Utility.activeUserId == Utility.onScreenUserId && elogFragment != null) {
                        elogFragment.updateTodayDistance(totalDistance);
                    }

                /*if (Utility.activeUserId == Utility.onScreenUserId && todayDistance > 0 && elogFragment != null) {
                    elogFragment.updateTodayDistance(todayDistance);
                }*/
                    if (Utility._appSetting.getDashboardDesign() == 2) {
                        hideGaugeCluster();
                    } else if (Utility.dashboardContext != null) {
                        hideDashboard(Utility.dashboardContext);
                    }
                }
            }
        });

    }

    @Override
    public void postToServer() {

        // process violations
        ProcessViolation(true);

        MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo);
    }

    @Override
    public void onELD2020Status(int status) {
        onUpdateCanbusIcon(status);

        // if Hutch Connect is connected
        if (status == CanMessages.STATE_CONNECTED) {

            ReportDB obj = new ReportDB(getApplicationContext());
            int specialStatusFg = obj.getSpecialStatusFg(Utility.activeUserId);

            objHutchConnect.setUnitId(specialStatusFg);
            alertTimeDifference();

        } else if (status == CanMessages.STATE_SET_UNIT_ID) {

        } else if (status == CanMessages.STATE_DISCONNECTED) {

            // show dialog while driver id is setting up
            onUnitMismatchDialog(false);
        }

    }


    // Created By: Deepak Sharma
    // Created Date: 22 July 2020
    // Purpose: alert if difference of time of eld device and android device is greater than 2
    private void alertTimeDifference() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * 60);

                    if (Utility.TimeDifferentFg()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utility.showAlertMsg("There is difference between android and ELD2020 Device Time. Time on Android is : " + Utility.getCurrentDateTime() + "\n" +
                                        "Time on ELD2020 is : " + Utility._CurrentDateTime);
                            }
                        });
                    }
                } catch (Exception exe) {

                }
            }
        }).start();
    }

    @Override
    public void onSendCommand(String command) {
        objHutchConnect.sendData(command);
    }

    // Created By: Deepak Sharma
    // Created Date: 17 Feb 2020
    // Purpose: fire when any event related to daily log received (e.g driving, ILC , on duty, engine up, engine down)
    @Override
    public void onEventCapture(ReportBean data) {
        if (Utility.onScreenUserId == Utility.activeUserId && elogFragment != null) {
            elogFragment.refresh();
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 12 June 2020
    // Purpose: set unit id if mismatched
    @Override
    public void onMismatchUnitId(String unitId) {
        try {
            objHutchConnect.setUnitId(unitId);
        } catch (Exception exe) {
            LogFile.ELD2020DataLog("OnMismatchUnitId Exception: " + exe.getMessage() + unitId);
        }
    }

    @Override
    public void onMismatchDriver() {
     /*   try {

            int specialStatusFg = 0;

            if (Utility.activeUserId > 0) {
                ReportDB obj = new ReportDB(getApplicationContext());
                specialStatusFg = obj.getSpecialStatusFg(Utility.activeUserId);
            }

            objHutchConnect.setUnitId(specialStatusFg);
        } catch (Exception exe) {

        }*/
    }


    ConstraintLayout layoutUnitMismatch;
    ProgressBar progress_unit_mismatch;
    TextView tvUnitMismatchProgress;
    Thread thUnitMismatch;

    // Created By: Deepak Sharma
    // Created Date: 2 september 2020
    // purpose: if active driver id, login driver id, unidentified driverid and special status
    // does not match with data set on eld 2020 device then show this dialog
    @Override
    public void onUnitMismatchDialog(boolean showFg) {
    /*    if (unitDialogShowFg && !showFg) {
            unitDialogShowFg = false;
            layoutUnitMismatch.setVisibility(View.GONE);
        } else if (!unitDialogShowFg && showFg) {

            unitDialogShowFg = true;
            layoutUnitMismatch.setVisibility(View.VISIBLE);
        }*/

        // delete thread if possible
        if (thUnitMismatch != null) {
            thUnitMismatch.interrupt();
            thUnitMismatch = null;
        }

        if (showFg) {

            updateMismatchUnitProgress(0);
            thUnitMismatch = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int i = 1;
                        while (i < 60) {
                            updateMismatchUnitProgress(i);
                            Thread.sleep(1000);
                            i++;
                        }
                    } catch (Exception exe) {

                    }
                }
            });
            thUnitMismatch.setName("thUnitMismatch");
            thUnitMismatch.start();

        } else {
            updateMismatchUnitProgress(60);
        }

        layoutUnitMismatch.setVisibility(showFg ? View.VISIBLE : View.GONE);
    }

    // Created by: Deepak Sharma
    // Created Date: 03 September 2020
    // Purpose: update mismatch progress
    private void updateMismatchUnitProgress(final int data) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int progress = data;
                    tvUnitMismatchProgress.setText((progress * 100 / 60) + "%");
                    //tvUnitMismatchProgress.setText((progress * 100 / 120) + " %");
                    progress_unit_mismatch.setProgress(progress);
                }
            });
        } catch (Exception exe) {

        }
    }

    // Created By: Deepak Sharma
    // Created Date: 27 FEb 2020
    // Purpose: change item id from hutch support device
    public void onRemoteNavigationItemChanged(final int position, final boolean drawerFg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (drawerFg)
                    onNavigationItemSelected(lstDrawerItems.get(position).getId());
                else {
                    onRemoteMenuChange(position);
                }
            }
        });
    }


    public void onRemoteMenuChange(int itemId) {
        try {
            switch (itemId) {
                case 0:
                    callELog();

                    break;
                case 1:
                    callInspectELog();

                    break;
                case 2:
                    callNewEvent();

                    break;
                case 3:
                    callEditRequest();

                    break;
                case 4:
                    callUnidentifiedEvent();

                    break;
                case 5:
                    callCertifyLogBook();

                    break;
                case 6:
                    callViolationHistory();

                    break;
                case 7:
                    callDriverProfile();

                    break;
                case 8: {
                    int currentStatus = EventDB.getCurrentDutyStatus(Utility.onScreenUserId);
                    if (currentStatus == 4 || currentStatus == 5)
                        setInspectorMode(true);
                    else
                        Utility.showAlertMsg(getString(R.string.onduty_inspector_mode_alert));

                }

                case 12:
                    if (!Utility.hasFeature("11")) {
                        Utility.showAlertMsg(getString(R.string.feature_na_alert));
                        return;
                    }
                    onLoadVehicleInfo();
                    break;
                case 13:
                    if (!Utility.hasFeature("3")) {
                        Utility.showAlertMsg(getString(R.string.feature_na_alert));
                        return;
                    }
                    onLoadFuelDetail();
                    break;
                case 14:
                    if (!Utility.hasFeature("11")) {
                        Utility.showAlertMsg(getString(R.string.feature_na_alert));
                        return;
                    }
                    onLoadIncidentDetail();
                    break;
                case R.id.btnNavigation:

                    break;
                case 16:

                    break;
                case 17:
                    onLoadDocumentDetailList();
                    break;
                case 18:

                    break;
                case 19:
                    onLoadRuleList();
                    break;
                case 20:
                    onLoadHelp();
                    break;
                case 21:
                    onLoadNotificationList();
                    break;
                case 22:
                    onLoadTicketHistory();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(DailyLogDashboardFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DailyLogDashboardFragment.class.getName(), "::onClick Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onRefresh() {
        replaceFragment(DailyLogDashboardFragment.newInstance());
        getSupportActionBar().setTitle(getString(R.string.eld_title));
    }

    @Override
    public void onSyncData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (elogFragment != null)
                    elogFragment.callSync();
            }
        });
    }

    @Override
    public void onPostData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (elogFragment != null)
                    elogFragment.callPost();
            }
        });
    }

    Thread thPromptDutyStatusDialog = null;

    @Override
    public void promptToChangeStatus() {
        if (thPromptDutyStatusDialog != null) {
            thPromptDutyStatusDialog.interrupt();
            thPromptDutyStatusDialog = null;
        }

        thPromptDutyStatusDialog = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    int i = 0;
                    while (i < 180) {
                        Thread.sleep(1000);
                        i++;
                        if (Utility.motionFg)
                            return;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                String title = "ELD";
                                int status = activeCurrentDutyStatus;
                                title += " - " + UserDB.getUserName(Utility.activeUserId);

                                if (status == 3) {
                                    if (isAppActive) {
                                        if (ponDutyChangeDialog != null) {
                                            if (!ponDutyChangeDialog.isShowing() && !onDutyChangeDialogResponse) {
                                                onDutyChangeDialogResponse = true;
                                                ponDutyChangeDialog.setTitle(title);
                                                ponDutyChangeDialog.mListener = MainActivity.this;

                                                ponDutyChangeDialog.show(getSupportFragmentManager(), "popup_dialog");
                                            } else {
                                                // not opened
                                                LogFile.write(ELogFragment.class.getName() + "::ponDutyChangeDialog Error: Dialog has already been showing ", LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                                            }
                                        } else {
                                            // not opened
                                            LogFile.write(ELogFragment.class.getName() + "::promptToChangeStatus Error: Dialog is null", LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                                        }
                                    } else {

                                        //save event that it automatically change
                                        int driverId = Utility.activeUserId;

                                        int eventType = 1;
                                        int eventCode = 4;
                                        String eventCodeDescription = getString(R.string.duty_status_changed_to_on_duty);

                                        ReportDB obj = new ReportDB(MainActivity.this);
                                        obj.EventCreateAuto(driverId, eventType, eventCode, eventCodeDescription);


                                        setActiveDutyStatus(4);
                                        onDutyChangeDialogResponse = false;

                                        if (elogFragment != null) {
                                            elogFragment.refresh();
                                        } else {

                                            ProcessViolation(true);
                                        }

                                        if (Utility._appSetting.getDutyStatusReading() == 1) {
                                            String textToSpeech = driverName + " " + getResources().getString(R.string.texttospeech_duty_status_changed_to_driving);
                                            if (MainActivity.textToSpeech != null)
                                                MainActivity.textToSpeech.speak(textToSpeech, TextToSpeech.QUEUE_ADD, null);
                                        }
                                    }
                                    // Post Event and driver info
                                    MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo);
                                }
                            } catch (Exception e) {
                                onDutyChangeDialogResponse = false;
                                LogFile.write(ELogFragment.class.getName() + "::promptToChangeStatus Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                                LogDB.writeLogs(TAG, "::promptToChangeStatus Error:", e.getMessage(), Utility.printStackTrace(e));
                            }

                        }
                    });
                } catch (Exception exe) {
                    String message = exe.getMessage();
                }

            }
        });
        thPromptDutyStatusDialog.setName("thPromptDutyStatusDialog");
        thPromptDutyStatusDialog.start();
    }

}