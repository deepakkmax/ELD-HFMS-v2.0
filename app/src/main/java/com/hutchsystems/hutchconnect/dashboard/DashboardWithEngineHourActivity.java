package com.hutchsystems.hutchconnect.dashboard;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.beans.DailyLogBean;
import com.hutchsystems.hutchconnect.beans.DutyStatusBean;
import com.hutchsystems.hutchconnect.beans.GPSData;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.ChatClient;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.common.ZoneList;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.EventDB;
import com.hutchsystems.hutchconnect.db.HourOfServiceDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.MessageDB;
import com.hutchsystems.hutchconnect.db.NotificationDB;
import com.hutchsystems.hutchconnect.db.TripInspectionDB;
import com.hutchsystems.hutchconnect.fragments.DetailFragment;
import com.hutchsystems.hutchconnect.fragments.ELogFragment;
import com.hutchsystems.hutchconnect.util.LetterSpacingTextView;
import com.hutchsystems.hoursofservice.common.TimeUtility;
import com.hutchsystems.hoursofservice.model.ViolationModel;

import java.util.ArrayList;
import java.util.Date;

import static com.hutchsystems.hutchconnect.MainActivity.currentRule;

public class DashboardWithEngineHourActivity extends Activity implements ChatClient.INotifyDashboards {
    private LinearLayout layout1;
    ConstraintLayout currentTimeZone, currentWorkShiftHour, usRemaningLayout, layout2, drivinghourslayout;
    private TextView tvNotificationCount, tvLoginName, tvCoDriver, tvcurrentAddressValue, tvspeedValue,
            tvUnitNo, tvPlateNo, tvTimeZone, tvTotalWorkShiftHours, tvCanadaRuleValue, tvUSRuleValue,
            tvTotalDrivingHours, tvViolation, tvOffDutyTime, tvSleeperTime, tvDrivingTime, tvOnDutyTime,
            tvSpeed, tvRPM, tvOdometerUnit, tvTotalMiles, tvDrivingValue, tvOndutyValue, tvWorkShiftLabel,
            tvDriverStatus, tvCodriverStatus,tvCurrentTimeValue;
    private AnimatorSet mSetRightOut;
    private AnimatorSet mSetLeftIn;
    private boolean mIsBackVisible = false;
    Canvas canvas;
    int finalHeight = 0, finalWidth = 0, imageWidth = 0, imageHeight = 0;
    private ImageView imgDutyStatus, icGPS, icBattery, icWifi, icWebService, icInspection, icViolation, imgViolation;
    String TAG = DashboardWithEngineHourActivity.class.getName();
    CardView llMiddleLayout, clUnitLayout, DrivingLayout;
    String driverName;
    CheckBox chkRules;
    ProgressBar pbTotalDrivingHours;
    private ArrayList<DutyStatusBean> dutyStatusList = new ArrayList<>();
    static String fullFormat = "yyyy-MM-dd HH:mm:ss";
    public Date statusDT;
    int driverId = 1;
    Date selectedDate;
    ImageButton fabback;
    private int currentStatus;
    Thread threadUpdateInfos;
    Boolean fromMainActivity;
    Boolean IsNightMode;

    Handler handler = new Handler();
    Runnable updateValues = new Runnable() {
        @Override
        public void run() {
            if (DashboardWithEngineHourActivity.this != null) {
                while (true) {
                    try {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateInformation();
                            }

                        });
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            //   handler.postDelayed(this, 1000);
        }
    };

    // time elapsed in seconds
    int timeElapsed = 0;
    private ImageView notificationIcon;

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Update Information On the screen
    private void updateInformation() {


        // triggers method every minutes
        if (timeElapsed >= 60) {
            timeElapsed = 0;
            setTvEngineHours();
            setTvOdometerReading();
            //update Remaning Hours
            MainActivity.ProcessViolation(false);
            ShowRemainingHours();
            // update current location of vehicle
            tvcurrentAddressValue.setText(Utility.currentLocation.getLocationDescription());
            // Update Total Distance
            updateTodayDistance();
            checkMsgOrNotification();
        }
        tvCurrentTimeValue.setText(Utility._CurrentDateTime);
        timeElapsed++;
        /* ---- Update Icon --------------*/
        updateWifiAndWebserviceIcon(Utility.isInternetOn());

        updateGPSIcon();

        //Update battery icon
        onUpdateBatteryIcon();

        // update btb icon
        onUpdateCanbusIcon(CanMessages.mState);

        /* ---- Update Hours  --------------*/
        // set engine Hours to textview

        setRPMTextViewAndRoatation();

        // Update Speed
        updateSpeed();

    }

    private LetterSpacingTextView tvEngineHours, tvOdometer;

    // Created By: Pallavi Wattamwar
    // Created Date: 5 april
    // purpose: Update Speed textview and rotation
    public void updateSpeed() {

        float speed = Float.parseFloat(CanMessages.Speed);
        String speedUnit = " km/h";
        if (Utility._appSetting.getUnit() == 2) {
            speed = speed * .62137f;
            speedUnit = " MPH";
        }
        if (tvspeedValue != null) {
            tvspeedValue.setText(Math.round(speed) + " " + speedUnit);
        }
        if (tvSpeed != null) {
            tvSpeed.setText(Math.round(speed) + " " + speedUnit);
        }

    }

    // Created By: Deepak Sharma
    // Created Date: 5 November 2019
    // Purpose: set UI mode Day/Night
    private void setUIMode() {
        // switch theme according to day/night mode
        setTheme(Utility.NightModeFg ? R.style.DashboardWithGraphActivityThemeDark : R.style.DashboardWithGraphActivityThemeLight);
        setBrightness(Utility.NightModeFg ? 1 : 255);
    }

    private ImageView icCanbus;
    LinearLayout endLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUIMode();
        super.onCreate(savedInstanceState);

        // Check Activity Launches From MainActivity ( At Time of Driving Start) or From Settings
        setData();
    }

    private void setData() {
        try {
            if (getIntent() != null) {

                if (getIntent().getBooleanExtra("FromMainActivity", false)) {
                    fromMainActivity = true;
                } else {
                    fromMainActivity = false;
                }
            }

            setContentView(R.layout.layout_dashboard_hos_enginehours);
            setRequestedOrientation(AppSettings.getOrientation());
            // Utility.setOrientation(this);
            Utility.dashboardContext = this;

            // initialize view
            initView();


            selectedDate = Utility.dateOnlyGet(Utility.newDate());

            driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
            DailyLogBean dailyLog = DailyLogDB.getDailyLogInfo(driverId, Utility.GetString(selectedDate));

            // get current day's duty status
            dutyStatusList = HourOfServiceDB.DutyStatusGetToday(Utility.onScreenUserId);
            initializeStatus();

            threadUpdateInfos = new Thread(updateValues);
            threadUpdateInfos.start();
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Update icon and Hos
        //   handler.postDelayed(updateValues, 500);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadUpdateInfos.interrupt();


    }

    // Created By: Pallavi Wattamwar
    // Created Date: 27 March 2019
    // purpose: intialize the Duty Status
    private void initializeStatus() {
        try {
            statusDT = Utility.dateOnlyGet(Utility.newDate());

            if (dutyStatusList.size() > 0) {
                try {
                    int index = dutyStatusList.size() - 1; // last index
                    currentStatus = dutyStatusList.get(index).getStatus();
                    statusDT = Utility.parse(dutyStatusList.get(index).getStartTime());
                } catch (Exception exe) {
                }
            } else {
                currentStatus = 1;
            }


        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::initializeStatus Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DetailFragment.class.getName(), "::initializeStatus Error:", e.getMessage(), Utility.printStackTrace(e));
        }
    }


    public void initView() {

        tvCurrentTimeValue =findViewById(R.id.tvCurrentTimeValue);
        tvCurrentTimeValue.setText(Utility._CurrentDateTime);
        // current status
        tvCodriverStatus = findViewById(R.id.tvCodriverStatus);
        tvDriverStatus = findViewById(R.id.tvDriverStatus);

        // Gps initialization
        icGPS = (ImageView) findViewById(R.id.icGPS);
        icGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //setTheme(R.style.DashboardWithGraphActivityThemeLight);
                //recreateActivity(true);
                //setBrightness(255);
            }
        });

        icBattery = (ImageView) findViewById(R.id.icBattery);
        icWifi = (ImageView) findViewById(R.id.icWifi);
        icWebService = (ImageView) findViewById(R.id.icWebService);
        llMiddleLayout = (CardView) findViewById(R.id.llMiddleLayout);
        clUnitLayout = (CardView) findViewById(R.id.clUnitLayout);
        DrivingLayout = (CardView) findViewById(R.id.DrivingLayout);
        drivinghourslayout = (ConstraintLayout) findViewById(R.id.drivinghourslayout);

        icCanbus = (ImageView) findViewById(R.id.icCanbus);

        icInspection = (ImageView) findViewById(R.id.icInspection);
        boolean inspections = TripInspectionDB.getInspections(Utility.getCurrentDate(), Utility.user1.getAccountId(), Utility.user2.getAccountId());
        onUpdateInspectionIcon(inspections);

        endLayout = (LinearLayout) findViewById(R.id.endLayout);
        if (Utility._appSetting.getViolationOnDrivingScreen() == 1) {
            endLayout.setVisibility(View.VISIBLE);
        } else {
            endLayout.setVisibility(View.INVISIBLE);
        }
        icViolation = (ImageView) findViewById(R.id.icViolation);

        imgViolation = (ImageView) findViewById(R.id.imgViolation);
        imgViolation.setVisibility(View.GONE);

        // Login name
        tvLoginName = (TextView) findViewById(R.id.tvLoginName);
        setDriverName();

        // codriver name
        tvCoDriver = (TextView) findViewById(R.id.tvCoDriverValue);
        tvCoDriver.setText("N/A");

        //update co-driver name if existed
        if (Utility.user1.isOnScreenFg()) {
            if (Utility.user2.getAccountId() > 0) {
                tvCoDriver.setText(Utility.user2.getFirstName() + " " + Utility.user2.getLastName());
                int currentStatus = EventDB.getCurrentDutyStatus(Utility.user2.getAccountId());
                tvCodriverStatus.setText(getResources().getStringArray(R.array.duty_status)[currentStatus - 1]);
            }
        } else {
            tvCoDriver.setText(Utility.user1.getFirstName() + " " + Utility.user1.getLastName());
            int currentStatus = EventDB.getCurrentDutyStatus(Utility.user1.getAccountId());
            tvCodriverStatus.setText(getResources().getStringArray(R.array.duty_status)[currentStatus - 1]);
        }


        // Current address
        tvcurrentAddressValue = (TextView) findViewById(R.id.tv_currentAddressValue);

        // update current location of vehicle
        tvcurrentAddressValue.setText(Utility.currentLocation.getLocationDescription());

        // speed
        tvspeedValue = (TextView) findViewById(R.id.tv_speedValue);


        // set unit number
        tvUnitNo = (TextView) findViewById(R.id.tvUnitNo);
        tvUnitNo.setText("Unit No:" + Utility.UnitNo);

        // set plate number
//        tvPlateNo = (TextView) findViewById(R.id.tvPlateNo);
//        tvPlateNo.setText("Plate No:" + Utility.PlateNo);

        chkRules = (CheckBox) findViewById(R.id.chkRules);
        chkRules.setEnabled(false);
        chkRules.setChecked(DailyLogDB.getCurrentRule(Utility.onScreenUserId) < 3);

        tvTimeZone = (TextView) findViewById(R.id.tvTimeZoneValue);


        currentTimeZone = (ConstraintLayout) findViewById(R.id.currentTimeZone);

        tvTimeZone.setText(ZoneList.getTimezoneName(false));


        /* ---- Hos Fields  --------------*/
        tvTotalWorkShiftHours = (TextView) findViewById(R.id.tvWorkShiftValue);
        tvDrivingValue = (TextView) findViewById(R.id.tvDrivingValue);
        tvOndutyValue = (TextView) findViewById(R.id.tvOndutyValue);
        tvWorkShiftLabel = (TextView) findViewById(R.id.tvWorkShiftLabel);
//        pbTotalWorkShiftHour = (ProgressBar) findViewById(R.id.pbTotalWorkShiftHour);

        tvCanadaRuleValue = (TextView) findViewById(R.id.tvCanadaRuleValue);
//        pbTotalCanadaRule = (ProgressBar) findViewById(R.id.pbTotalCanadaRule);


        tvUSRuleValue = (TextView) findViewById(R.id.tvUSRuleValue);
//        pbTotalUSRule = (ProgressBar) findViewById(R.id.pbTotalUSRule);


        tvTotalDrivingHours = (TextView) findViewById(R.id.tvDrivingHoursValue);
        pbTotalDrivingHours = (ProgressBar) findViewById(R.id.pbTotalDrivingHours);


        tvViolation = (TextView) findViewById(R.id.tvViolation);
        tvViolation.setSelected(true);


//        pbTotalCanadaRule.setMax(currentRule == 2 ? 120 * 60 : 70 * 60);
//        pbTotalUSRule.setMax(currentRule == 4 ? 60 * 60 : 70 * 60);

        //tvCurrentRuleLabel.setText(getRule(currentRule - 1));
        if (currentRule == 1 || currentRule == 2) //Canada rule
        {
            pbTotalDrivingHours.setMax(13 * 60);
            // pbTotalWorkShiftHour.setMax(16 * 60);
        } else {
            pbTotalDrivingHours.setMax(11 * 60);
            //pbTotalWorkShiftHour.setMax(14 * 60);
        }
        // Update Driving Hours
        MainActivity.ProcessViolation(false);
        ShowRemainingHours();

        //back button
        fabback = (ImageButton) findViewById(R.id.fabback);
        // if activity Launches from main activity then gone visiblity of back button
        if (fromMainActivity) {
            fabback.setVisibility(View.GONE);
        }
        fabback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Notification count
        tvNotificationCount = (TextView) findViewById(R.id.notificationCount);
        notificationIcon = (ImageView) findViewById(R.id.notificationIcon);
        checkMsgOrNotification();
        manageBlinkEffect();

        // Flip animation
        layout1 = (LinearLayout) findViewById(R.id.notificationlayout);
        layout2 = (ConstraintLayout) findViewById(R.id.drivinghourslayout);
        loadAnimations();
        currentTimeZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utility._appSetting.getVisionMode() == 2) {
                    Utility.NightModeFg = !Utility.NightModeFg;
                    recreateActivity();
                }
            }
        });
        changeCameraDistance();

        tvEngineHours = (LetterSpacingTextView) findViewById(R.id.tvEngineHours);
        setTvEngineHours();

        tvOdometer = (LetterSpacingTextView) findViewById(R.id.tvOdometer);
        tvOdometerUnit = (TextView) findViewById(R.id.tvOdometerUnit);
        setTvOdometerReading();

        if (Utility.isLargeScreen(getApplicationContext())) {
            tvOdometer.setLetterSpacing(21);
            tvEngineHours.setLetterSpacing(21);
        } else {
            tvOdometer.setLetterSpacing(23);
            tvEngineHours.setLetterSpacing(23);
        }


        tvTotalMiles = (TextView) findViewById(R.id.tvTotalMiles);
        updateTodayDistance();

        tvSpeed = (TextView) findViewById(R.id.tvSpeed);
        tvRPM = (TextView) findViewById(R.id.tvRPM);

        setRPMTextViewAndRoatation();

        // Brightness decrease
        currentWorkShiftHour = (ConstraintLayout) findViewById(R.id.currentWorkShiftHour);
        currentWorkShiftHour.setOnClickListener(new View.OnClickListener() {
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


        // Brightness increase
        usRemaningLayout = (ConstraintLayout) findViewById(R.id.usRemaningLayout);
        usRemaningLayout.setOnClickListener(new View.OnClickListener() {
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

        Bitmap bmp1 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_flagbar_battery_full);
        // bitmap to draw battery
        batteryBmp = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(batteryBmp);

        // these variable helps to set card view background color
        int layoutColor = Utility.NightModeFg ? 0 : Color.WHITE;
        int drivingHourLayoutColor = Utility.NightModeFg ? ContextCompat.getColor(this, R.color.purple) : Color.WHITE;
        llMiddleLayout.setCardBackgroundColor(layoutColor);
        clUnitLayout.setCardBackgroundColor(layoutColor);
        DrivingLayout.setCardBackgroundColor(layoutColor);
        drivinghourslayout.setBackgroundColor(drivingHourLayoutColor);
    }


    // Created By: Deepak Sharma
    // Created Date: 31 Aug 2016
    // Purpose: set brightness of tablet
    private void setBrightness(int brightness) {
        try {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            android.provider.Settings.System.putInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = (float) brightness / 255;
            getWindow().setAttributes(lp);
        } catch (Exception exe) {

        }
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Set Rotation of RPM and Set RPM Textview
    private void setRPMTextViewAndRoatation() {

        tvRPM.setText(CanMessages.RPM + " " + "rpm");

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Calculate Total Distance
    public void updateTodayDistance() {
        int distance = EventDB.calculateDistance();
        try {
            if (tvTotalMiles != null) {
                try {
                    String unit = "Kms";
                    if (Utility._appSetting.getUnit() == 2) {
                        distance = Double.valueOf(distance * .62137d).intValue();
                        unit = "Miles";
                    }


                    //String odoText = df.format(odo);
                    String totalDistance = Integer.toString(distance);


                    if (tvTotalMiles != null) {
                        tvTotalMiles.setText(totalDistance + " " + unit);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::updateTodayDistance(distance) Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "updateTodayDistance(distance)", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Set Engine Hours To Textview
    public void setTvEngineHours() {
        String zero = "";
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
    }

    public void setTvOdometerReading() {
        String zero = "";
        String unit = "";
        Double odometerReading = Double.parseDouble(CanMessages.OdometerReading);

        if (Utility._appSetting.getUnit() == 2) {
            odometerReading = odometerReading * .62137d;
            unit = "Miles";

        } else {
            unit = "Kms";
        }

        //String odoText = df.format(odo);
        String odoText = String.format("%.1f", odometerReading);
        //Log.d(TAG, "odo=" + odoText);
        odoText = odoText.replace(".", "");
        zero = "";
        if (odoText.length() < 8) {
            for (int i = odoText.length(); i < 8; i++) {
                zero += "0";
            }
        }
        odoText = zero + odoText;

        if (tvOdometer != null)
            tvOdometer.setText(odoText);
        if (tvOdometerUnit != null)
            tvOdometerUnit.setText("Odometer (" + unit + ")");
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 2 April 2019
    // purpose: update inspection icon
    public void onUpdateInspectionIcon(Boolean Isinspection) {

        if (icInspection != null && Isinspection) {
            icInspection.setVisibility(View.VISIBLE);
            icInspection.setBackgroundResource(R.drawable.ic_flagbar_dvir_done);
            //change the icon to GREEN
            //Log.d(TAG, "Change Inspection GREEN");
        } else if (icInspection != null && !Isinspection) {
            icInspection.setBackgroundResource(R.drawable.ic_flagbar_dvir_pending);
        }

    }


    Bitmap batteryBmp;

    // Created By: Pallavi Wattamwar
    // Created Date: 2 April 2019
    // purpose: update BatteryIcon
    public void onUpdateBatteryIcon() {
        //level is percent of battery
        if (icBattery != null) {
            canvas = new Canvas((batteryBmp));
            drawingBattery(batteryBmp, icBattery);
        }


    }


    // Created By: Pallavi Wattamwar
    // Created Date: 24 april
    // purpose: checkMessage is recived or notification is recived
    public void checkMsgOrNotification() {
        int unReadMessages = MessageDB.getUnreadCount();
        tvNotificationCount.setVisibility(View.VISIBLE);
        if (unReadMessages > 0) {
            tvNotificationCount.setText("" + unReadMessages);
            notificationIcon.setBackgroundResource(Utility.NightModeFg ? R.drawable.ic_message_dark : R.drawable.ic_message);
        }
        // Check Notification is received
        else {
            int unReadNotifications = NotificationDB.getNotification().size();
            if (unReadNotifications > 0) {
                tvNotificationCount.setText("" + NotificationDB.getNotification().size());
                notificationIcon.setBackgroundResource(Utility.NightModeFg ? R.drawable.db_bell_small : R.drawable.bell_small_dark);
            }
            // Default Value count 0
            else {
                tvNotificationCount.setVisibility(View.GONE);
                tvNotificationCount.setText("" + 0);
                notificationIcon.setBackgroundResource(Utility.NightModeFg ? R.drawable.bell_small_dark : R.drawable.db_bell_small);
            }
        }
    }

    private void drawingBattery(Bitmap bmp, ImageView view) {
        boolean isPlugged = GPSData.ACPowerFg == 1;
        Canvas c = new Canvas(bmp);
        int level = Utility.BatteryLevel;

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


    // Created By: Deepak Sharma
    // Created Date: 22 April 2019
    // purpose: update gps icon
    public void updateGPSIcon() {
        icGPS.setBackgroundResource(MainActivity.isGPSEnabled ? R.drawable.ic_flagbar_gps_on : R.drawable.ic_flagbar_gps_off);

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 2 April 2019
    // purpose: Blink Notification Count
    private void manageBlinkEffect() {
        AlphaAnimation blinkanimation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        blinkanimation.setDuration(300); // duration
        blinkanimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        blinkanimation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        blinkanimation.setRepeatMode(2);
        tvNotificationCount.startAnimation(blinkanimation);
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 2 April 2019
    // purpose: Distance For Flipping Animation
    private void changeCameraDistance() {
        int distance = 7000;
        float scale = getResources().getDisplayMetrics().density * distance;
        layout1.setCameraDistance(scale);
        layout2.setCameraDistance(scale);
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 2 April 2019
    // purpose: load Distance
    private void loadAnimations() {
        mSetRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.out_animation);
        mSetRightOut.setDuration(1000);
        mSetLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.in_animation);
        mSetLeftIn.setDuration(1000);
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 2 April 2019
    // purpose: upadte btb icon
    public void onUpdateCanbusIcon(final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (icCanbus != null) {
                    switch (state) {
                        case CanMessages.STATE_NONE:
                        case CanMessages.STATE_LISTEN:

                            icCanbus.setBackgroundResource(R.drawable.ic_flagbar_canbus_disconnect);
                            break;

                        case CanMessages.STATE_CONNECTING:

                            icCanbus.setBackgroundResource(R.drawable.trans);
                            icCanbus.setBackgroundResource(R.drawable.flagbar_canbus_blinking);

                            AnimationDrawable frameAnimation = (AnimationDrawable) icCanbus.getBackground();
                            frameAnimation.start();
                            break;
                        case CanMessages.STATE_CONNECTED:
                            icCanbus.setBackgroundResource(R.drawable.ic_flagbar_canbus_connect);
                            break;
                    }
                }
            }
        });
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 2 April 2019
    // purpose: change the Flip view
    public void flipCard(View view) {
        if (mIsBackVisible) {
            hideHoursRemaining();
        } else {
            showNotification();
        }
    }

    private void hideHoursRemaining() {
        mSetRightOut.setTarget(layout1); // notification layout
        mSetLeftIn.setTarget(layout2); // hours layout
        mSetRightOut.start(); // hide notification layout
        mSetLeftIn.start(); // show driving layout
        mIsBackVisible = false;
    }

    private void showNotification() {

        mSetLeftIn.setTarget(layout1); // show notification layout
        mSetRightOut.setTarget(layout2); // hide hour layout
        mSetLeftIn.start();
        mSetRightOut.start();
        mIsBackVisible = true;
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 2 April 2019
    // purpose: set the driver name according to the active Driver
    private void setDriverName() {
        try {
            if (!Utility.isLargeScreen(getApplicationContext())) {
                if (Utility.user1.isOnScreenFg()) {
                    Utility.onScreenUserId = Utility.user1.getAccountId();
                    driverName = Utility.user1.getFirstName() + (Utility.user1.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");
                    tvLoginName.setText(Utility.user1.getUserName());
                    int currentStatus = EventDB.getCurrentDutyStatus(Utility.user1.getAccountId());
                    tvDriverStatus.setText(getResources().getStringArray(R.array.duty_status)[currentStatus - 1]);

                } else {
                    Utility.onScreenUserId = Utility.user2.getAccountId();
                    driverName = Utility.user2.getFirstName() + (Utility.user2.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");
                    tvLoginName.setText(Utility.user2.getUserName());
                    int currentStatus = EventDB.getCurrentDutyStatus(Utility.user2.getAccountId());
                    tvDriverStatus.setText(getResources().getStringArray(R.array.duty_status)[currentStatus - 1]);
                }
            } else {
                if (Utility.user1.isOnScreenFg()) {
                    Utility.onScreenUserId = Utility.user1.getAccountId();
                    driverName = Utility.user1.getFirstName() + " " + Utility.user1.getLastName() + (Utility.user1.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");
                    tvLoginName.setText(Utility.user1.getUserName());
                    int currentStatus = EventDB.getCurrentDutyStatus(Utility.user1.getAccountId());
                    tvDriverStatus.setText(getResources().getStringArray(R.array.duty_status)[currentStatus - 1]);
                } else {
                    Utility.onScreenUserId = Utility.user2.getAccountId();
                    driverName = Utility.user2.getFirstName() + " " + Utility.user2.getLastName() + (Utility.user2.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");
                    tvLoginName.setText(Utility.user2.getUserName());
                    int currentStatus = EventDB.getCurrentDutyStatus(Utility.user2.getAccountId());
                    tvDriverStatus.setText(getResources().getStringArray(R.array.duty_status)[currentStatus - 1]);
                }
            }
            tvLoginName.setText(driverName);

        } catch (Exception exe) {
        }

    }


    // Created By: Pallavi Wattamwar
    // Created Date: 27 March 2019
    // purpose: update webserice icon, wifi icon ,network icon depends on connectivity

    public void updateWifiAndWebserviceIcon(boolean isConnected) {
        icWifi.setVisibility(View.VISIBLE);

        if (isConnected) {

            //set Wifi icon to solid GREEN
            //Log.d(TAG, "Show wifi GREEN");
            if (GPSData.WifiOnFg == 1) {
                icWifi.setBackgroundResource(R.drawable.ic_flagbar_wifi_on);
            } else if (GPSData.CellOnlineFg == 1) {
                icWifi.setBackgroundResource(R.drawable.ic_flagbar_network_on);
            }

            if (Utility.webServiceConnectivity) {
                icWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_on);
            }

        } else {

            //set Wifi icon to GREY
            //Log.d(TAG, "Show wifi GREY");
            icWifi.setBackgroundResource(R.drawable.ic_flagbar_wifi_off);
            icWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_off);
        }
    }


    //Created By: Deepak Sharma
    //Created Date: 11/06/2018
    //Purpose: Show remaing hours
    private void ShowRemainingHours() {

        // show work shift remaining time in text view
        tvTotalWorkShiftHours.setText(Utility.getTimeFromMinute(GPSData.WorkShiftRemaining));
        tvDrivingValue.setText(Utility.getTimeFromMinute(GPSData.DrivingRemaining));

        // show workshift remaingin time in progress bar
        //pbTotalWorkShiftHour.setProgress(pbTotalWorkShiftHour.getMax() - GPSData.WorkShiftRemaining);

        // if current rule is canada
        if (currentRule == 1 || currentRule == 2) {

            //pbTotalCanadaRule.setProgress(pbTotalCanadaRule.getMax() - (currentRule == 2 ? GPSData.TimeRemaining120 : GPSData.TimeRemaining70));
            tvCanadaRuleValue.setText(Utility.getTimeFromMinute((currentRule == 2 ? GPSData.TimeRemaining120 : GPSData.TimeRemaining70)));

            // get previous rule of driver other than current country rule
            String except = "'1','2'";
            int previousRule = DailyLogDB.getPreviousRule(Utility.user1.getAccountId(), except);

            //pbTotalUSRule.setProgress(pbTotalCanadaRule.getMax() - (previousRule == 4 ? GPSData.TimeRemainingUS60 : GPSData.TimeRemainingUS70));
            tvUSRuleValue.setText(Utility.getTimeFromMinute((previousRule == 4 ? GPSData.TimeRemainingUS60 : GPSData.TimeRemainingUS70)));
            tvWorkShiftLabel.setText("(Work Shift, Driving , Onduty)");
            tvOndutyValue.setText(Utility.getTimeFromMinute(GPSData.OnDutyRemaining));

        } else { // if current rule is US

            //pbTotalUSRule.setProgress(pbTotalCanadaRule.getMax() - (currentRule == 4 ? GPSData.TimeRemainingUS60 : GPSData.TimeRemainingUS70));
            tvUSRuleValue.setText(Utility.getTimeFromMinute((currentRule == 4 ? GPSData.TimeRemainingUS60 : GPSData.TimeRemainingUS70)));

            // get previous rule of driver other than current country rule
            String except = "'3','4'";
            int previousRule = DailyLogDB.getPreviousRule(Utility.user1.getAccountId(), except);

            //pbTotalCanadaRule.setProgress(pbTotalCanadaRule.getMax() - (previousRule == 2 ? GPSData.TimeRemaining120 : GPSData.TimeRemaining70));
            tvCanadaRuleValue.setText(Utility.getTimeFromMinute((previousRule == 2 ? GPSData.TimeRemaining120 : GPSData.TimeRemaining70)));
            tvWorkShiftLabel.setText("(Work Shift, Driving, 30 min break)");
            tvOndutyValue.setText(Utility.getTimeFromMinute(GPSData.DrivingWithoutBreak));

        }

        // check if violation time has been passed
        boolean violationFg = false;
        if (MainActivity._violationFg) {

            // check how much time is left to violation
            int hourLeft = (int) Math.round((MainActivity.ViolationDT.getTime() - Utility.newDate().getTime()) / (1000 * 60d));
            if (hourLeft < 0) {
                hourLeft = 0;
            }
            violationFg = hourLeft == 0;

            tvViolation.setText((hourLeft == 0 && currentStatus == 3 ? ("(" + getString(R.string.stop_driving) + ") ") : "") + MainActivity.ViolationTitle);

            tvTotalDrivingHours.setText(Utility.getTimeFromMinute(hourLeft));
            pbTotalDrivingHours.setProgress(pbTotalDrivingHours.getMax() - hourLeft);
            GPSData.DrivingTimeRemaining = hourLeft;

            Utility.setViolationColor(hourLeft, tvViolation, getApplicationContext());

            if (Utility._appSetting.getViolationOnDrivingScreen() == 1 || hourLeft == 0) {
                endLayout.setVisibility(View.VISIBLE);
            } else {
                endLayout.setVisibility(View.INVISIBLE);
            }

        } else {

            tvViolation.setText("N/A");

            //   tvRemaingTime.setText("N/A");
            tvTotalDrivingHours.setText("N/A");
        }


        boolean violationAlertFg = false;
        // check if violation exists in list voilation icon shows when voilation exists for canada we show last 14 days violation and last 7 day violation for us
        for (ViolationModel vBean : MainActivity.violationList) {
            if (vBean.getViolationDate().before(Utility.newDate()) && !vBean.isVirtualFg() && vBean.getViolationDate().after(Utility.parse(Utility.getPreviousDate(ELogFragment.canadaFg ? -14 : -7)))) {
                violationAlertFg = true;
                break;
            }
        }

        onUpdateViolation(violationAlertFg);
    }


    // Created By: Pallavi Wattamwar
    // Created Date: 27 March 2019
    // purpose: update Violation icon
    public void onUpdateViolation(boolean status) {
        try {

            // status true if driver is in violation
            if (status) {
                icViolation.setBackgroundResource(R.drawable.ic_flagbar_violation);
                imgViolation.setBackgroundResource(R.drawable.ic_flagbar_violation);

                tvViolation.setText("You must stop driving! You are in violation since  " + Utility.ConverDateFormat(MainActivity.ViolationDT) + " for rule " + MainActivity.ViolationTitle);
            } else {

                // time elpased untill next violation
                double timeElpased = TimeUtility.getDiff(Utility.newDate(), MainActivity.ViolationDT, TimeUtility.Unit.MIN);

                // if time elpased is less than 1 hour then show yellow icon
                if (timeElpased < 60d) {
                    icViolation.setBackgroundResource(R.drawable.ic_flagbar_violation_upcoming);
                    imgViolation.setBackgroundResource(R.drawable.ic_flagbar_violation_upcoming);
                } else {

                    icViolation.setBackgroundResource(R.drawable.ic_flagbar_violation_off);
                    imgViolation.setBackgroundResource(R.drawable.ic_flagbar_violation_off);
                }

                tvViolation.setText("You must stop driving before " + Utility.ConverDateFormat(MainActivity.ViolationDT) + " otherwise you will be in violation for rule " + MainActivity.ViolationTitle);

            }

        } catch (Exception exe) {
        }
    }

    // Created By: Deepak sharma
    // Created Date: 5 November 2019
    // Purpose: recreate activity
    public void recreateActivity() {

        Intent intent = getIntent();
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {

    }


    @Override
    public void onMessageReceived() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mIsBackVisible) {
                    showNotification();
                }

                checkMsgOrNotification();
            }
        });
    }
}
