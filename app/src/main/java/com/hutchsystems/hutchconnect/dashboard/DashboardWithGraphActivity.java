package com.hutchsystems.hutchconnect.dashboard;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
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

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.beans.DutyStatusBean;
import com.hutchsystems.hutchconnect.beans.GPSData;
import com.hutchsystems.hutchconnect.beans.RuleBean;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.ChatClient;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.EventDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.MessageDB;
import com.hutchsystems.hutchconnect.db.NotificationDB;
import com.hutchsystems.hutchconnect.db.TripInspectionDB;
import com.hutchsystems.hutchconnect.fragments.ELogFragment;
import com.hutchsystems.hoursofservice.common.TimeUtility;
import com.hutchsystems.hoursofservice.model.ViolationModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import androidx.constraintlayout.widget.ConstraintLayout;

import static com.hutchsystems.hutchconnect.MainActivity.currentRule;

public class DashboardWithGraphActivity extends Activity implements ChatClient.INotifyDashboards {
    private LinearLayout layout1;
    ConstraintLayout layout2, currentWorkShiftHour, currentTimeZone, usRemaningLayout;
    private TextView tvNotificationCount, tvLoginName, tvCoDriver, tvcurrentAddressValue, tvspeedValue, tvUnitNo, tvPlateNo, tvTotalMiles, tvTotalWorkShiftHours, tvCanadaRuleValue, tvUSRuleValue, tvTotalDrivingHours, tvViolation, tvOffDutyTime, tvSleeperTime, tvDrivingTime, tvOnDutyTime;
    private AnimatorSet mSetRightOut;
    private AnimatorSet mSetLeftIn;
    private boolean mIsBackVisible = false;
    Canvas canvas, batteryCanvas;
    Bitmap bmp;
    int finalHeight = 0, finalWidth = 0, imageWidth = 0, imageHeight = 0;
    private ImageView imgDutyStatus, icGPS, icBattery, icWifi, icWebService, icInspection, icViolation, imgViolation, notificationIcon;
    String TAG = DashboardWithGraphActivity.class.getName();
    private final static int levelThreshold = 60;
    Thread thBatteryMonitor;
    String driverName;
    CheckBox chkRules;
    ProgressBar pbTotalWorkShiftHour, pbTotalCanadaRule, pbTotalUSRule, pbTotalDrivingHours;
    private ArrayList<DutyStatusBean> dutyStatusList = new ArrayList<>();
    static String fullFormat = "yyyy-MM-dd HH:mm:ss";
    public Date statusDT;
    int driverId = 1;
    Date selectedDate;
    ImageButton fabback;
    Thread threadUpdateInfos;
    Bitmap batteryBmp;
    Handler handler = new Handler();
    LinearLayout endLayout;
    Runnable updateValues = new Runnable() {
        @Override
        public void run() {
            if (DashboardWithGraphActivity.this != null) {
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
                        Log.d(TAG, "ERROR update CAN: " + e.getMessage());
                        break;
                    }
                }

            }

            //  handler.postDelayed(this, 60000);
        }
    };
    private int currentStatus;
    private String currentDate;
    private int dailyLogId;
    private int coDriverId;
    private Boolean fromMainActivity;
    // time elapsed in seconds
    int timeElapsed = 0;

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Update Information On the screen
    private void updateInformation() {

        // triggers method every minutes
        if (timeElapsed >= 60) {
            timeElapsed = 0;

            MainActivity.ProcessViolation(false);
            // Update HOS Value
            ShowRemainingHours();
            //refresh graph
            refresh();

            checkMsgOrNotification();

        }
        timeElapsed++;

        //Update battery icon
        onUpdateBatteryIcon(Utility.BatteryLevel, GPSData.ACPowerFg == 1);
        // update btb icon
        onUpdateCanbusIcon(CanMessages.mState);
        // Update Speed
        updateSpeed();

        // Update Icon
        updateWifiAndWebserviceIcon(Utility.isInternetOn());
        updateGPSIcon();
        // update current location of vehicle
        tvcurrentAddressValue.setText(Utility.currentLocation.getLocationDescription());
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Update Battery icon depnds on level
    public void onUpdateBatteryIcon(int level, boolean isPlugged) {

        if (icBattery != null) {
            icBattery.setVisibility(View.VISIBLE);
            drawingBattery(level, isPlugged, batteryBmp, icBattery);
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
            notificationIcon.setBackgroundResource(Utility.NightModeFg  ? R.drawable.ic_message_dark : R.drawable.ic_message);
        }
        // Check Notification is received
        else {
            int unReadNotifications = NotificationDB.getNotification().size();
            if (unReadNotifications > 0) {
                tvNotificationCount.setText("" + NotificationDB.getNotification().size());
                notificationIcon.setBackgroundResource(Utility.NightModeFg  ? R.drawable.db_bell_small : R.drawable.bell_small_dark);
            }
            // Default Value count 0
            else {
                tvNotificationCount.setVisibility(View.GONE);
                tvNotificationCount.setText("" + 0);
                notificationIcon.setBackgroundResource(Utility.NightModeFg  ? R.drawable.bell_small_dark : R.drawable.db_bell_small);
            }
        }
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 24 april
    // purpose: Draw Violation area on graph
    private void drawViolationArea() {

        ArrayList<ViolationModel> vList = MainActivity.violationList;
        int startMinutes = 0;
        Date endTime = null, startTime = null;
        for (int i = 0; i < vList.size(); i++) {

            startTime = vList.get(i).getViolationDate();

            if (!Utility.dateOnlyGet(startTime).equals(Utility.newDateOnly()))
                continue;

            startMinutes = (int) (startTime.getTime() - selectedDate.getTime()) / (1000 * 60);
            if (endTime != null && startTime.before(endTime)) {
                continue;
            }
            endTime = Utility.addSeconds(startTime, (int) vList.get(i).getViolationDuration());
            if (Utility._appSetting.getGraphLine() == 1 && endTime.after(Utility.newDate())) {
                if (startTime.after(Utility.newDate()))
                    break;
                endTime = Utility.newDate();
            }

            int endMinutes = (int) (endTime.getTime() - selectedDate.getTime()) / (1000 * 60);
            drawRect(getX(startMinutes), getRectY(1), getX(endMinutes), getRectY(4));

        }

    }

    private int getRectY(int status) {
        float boxHeight = (float) (imageHeight - 64.0) / 4;
        if (!Utility.isLargeScreen(getApplicationContext())) {
            boxHeight = (float) (imageHeight - 22.0) / 4;
        }

        //int y = (int) ((status - 1) * boxHeight + boxHeight / 2 + 64.0);
        int y = 0;
        if (!Utility.isLargeScreen(getApplicationContext())) {
            y = (int) (status * boxHeight + 22.0);
            if (status == 1) {
                y = 22;
            }
        } else {
            if ((Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation)) {
                boxHeight = (float) (imageHeight - 44.0) / 4;

                y = (int) (status * boxHeight + 44.0);
                if (status == 1) {
                    y = 44;
                }

            } else {

                y = (int) (status * boxHeight + 54.0);
                if (status == 1) {
                    y = 66;
                }
            }

        }

        return y;
    }

    private void drawRect(float x, float y, float xend, float yend) {
        try {
            Paint p = new Paint();

            p.setColor(getResources().getColor(R.color.red15));
            //p.setStrokeWidth(1);
            canvas.drawRect(x, y, xend, yend, p);
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::drawRect Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "::drawRect Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: draw Battery icon
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

    public void updateSpeed() {

        float speed = Float.parseFloat(CanMessages.Speed);
        String speedUnit = " km/h";
        if (Utility._appSetting.getUnit() == 2) {
            speed = speed * .62137f;
            speedUnit = " MPH";
        }
        if (tvspeedValue != null) {
            tvspeedValue.setText(Math.round(speed) + speedUnit);
        }
    }


    // Created By: Deepak Sharma
    // Created Date: 22 April 2019
    // purpose: update gps icon
    public void updateGPSIcon() {
        icGPS.setBackgroundResource(MainActivity.isGPSEnabled ? R.drawable.ic_flagbar_gps_on : R.drawable.ic_flagbar_gps_off);
    }

    private ImageView icCanbus;

    private void setView(){
        if (getIntent() != null) {

            // Check Activity Launches From MainActivity ( At Time of Driving Start) or From Settings
            if (getIntent().getBooleanExtra("FromMainActivity", false)) {
                fromMainActivity = true;
            } else {
                fromMainActivity = false;
            }
        }
        setContentView(R.layout.layout_dashboard);
        setRequestedOrientation(AppSettings.getOrientation());
       // Utility.setOrientation(this);
        Utility.dashboardContext = this;
        // initialize view
        initView();

        selectedDate = Utility.dateOnlyGet(Utility.newDate());

        driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();

        if (Utility.user2.getAccountId() > 0)
            coDriverId = Utility.onScreenUserId == Utility.user2.getAccountId() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();

        ViewTreeObserver vto = imgDutyStatus.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imgDutyStatus.getViewTreeObserver().removeOnPreDrawListener(this);
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                finalHeight = displayMetrics.heightPixels;
                finalWidth = displayMetrics.widthPixels;

                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                float density = displayMetrics.density;
                finalWidth = Math.round(displayMetrics.widthPixels / density);
                finalHeight = Math.round(displayMetrics.heightPixels / density);
                //Log.e(TAG, "Height = " + finalHeight + " - Width = " + finalWidth);
                return false;
            }
        });

        Thread thBitMap = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    try {
                        Thread.sleep(1000);
                    } catch (Exception exe) {
                    }

                    if (finalHeight != 0 && finalWidth != 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initializeBitmap();
                            }
                        });

                        break;
                    }

                }
            }
        });
        thBitMap.setName("ElogDetail-Bitmap");
        thBitMap.start();

        // Update icon and Hos
        //  handler.postDelayed(updateValues, 500);

        threadUpdateInfos = new Thread(updateValues);
        threadUpdateInfos.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUIMode();
        super.onCreate(savedInstanceState);
        try {
            setView();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        dutyStatusList.clear();

        if (bmp != null) {
            bmp.recycle();
        }

        if (canvas != null) {
            canvas = null;
        }
        threadUpdateInfos.interrupt();
    }

    public void refresh() {
        try {

            if (canvas != null)
                DutyStatusGet();


        } catch (Exception e) {
            LogFile.write(DashboardWithGraphActivity.class.getName() + "::refresh Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DashboardWithGraphActivity.class.getName(), "::refresh Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    public void initView() {
        // Gps initialization
        icGPS = (ImageView) findViewById(R.id.icGPS);

        icBattery = (ImageView) findViewById(R.id.icBattery);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_flagbar_battery_full);
        batteryBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        batteryCanvas = new Canvas(batteryBmp);
        bmp.recycle();

        icWifi = (ImageView) findViewById(R.id.icWifi);
        icWebService = (ImageView) findViewById(R.id.icWebService);

        icCanbus = (ImageView) findViewById(R.id.icCanbus);

        icInspection = (ImageView) findViewById(R.id.icInspection);
        boolean inspections = TripInspectionDB.getInspections(Utility.getCurrentDate(), Utility.user1.getAccountId(), Utility.user2.getAccountId());
        onUpdateInspectionIcon(inspections);

        icViolation = (ImageView) findViewById(R.id.icViolaton);
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
            }

        } else {
            tvCoDriver.setText(Utility.user1.getFirstName() + " " + Utility.user1.getLastName());
        }


        // Current address
        tvcurrentAddressValue = (TextView) findViewById(R.id.tv_currentAddressValue);
        tvcurrentAddressValue.setText(Utility.currentLocation.getLocationDescription());

        // speed
        tvspeedValue = (TextView) findViewById(R.id.tv_speedValue);


        // set unit number
        tvUnitNo = (TextView) findViewById(R.id.tvUnitNo);
        tvUnitNo.setText("Unit No :" + Utility.UnitNo);

        // set plate number
        tvPlateNo = (TextView) findViewById(R.id.tvPlateNo);
        tvPlateNo.setText("Plate No :" + Utility.PlateNo);

        chkRules = (CheckBox) findViewById(R.id.chkRules);
        chkRules.setEnabled(false);
        chkRules.setChecked(DailyLogDB.getCurrentRule(Utility.onScreenUserId) < 3);

        tvTotalMiles = (TextView) findViewById(R.id.tvTotalMiles);
        // set the Theme According to the Settings screen
        currentTimeZone = (ConstraintLayout) findViewById(R.id.currentTimeZone);

        currentTimeZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utility._appSetting.getVisionMode() == 2) {
                    Utility.NightModeFg = !Utility.NightModeFg;
                    recreateActivity();
                }
            }
        });

        updateTodayDistance();

        // For drawing graph
        imgDutyStatus = (ImageView) findViewById(R.id.imgDutyStatus);


        // Hos Fields
        tvTotalWorkShiftHours = (TextView) findViewById(R.id.tvWorkShiftValue);
        pbTotalWorkShiftHour = (ProgressBar) findViewById(R.id.pbTotalWorkShiftHour);

        tvCanadaRuleValue = (TextView) findViewById(R.id.tvCanadaRuleValue);
        pbTotalCanadaRule = (ProgressBar) findViewById(R.id.pbTotalCanadaRule);


        tvUSRuleValue = (TextView) findViewById(R.id.tvUSRuleValue);
        pbTotalUSRule = (ProgressBar) findViewById(R.id.pbTotalUSRule);


        tvTotalDrivingHours = (TextView) findViewById(R.id.tvDrivingHoursValue);
        pbTotalDrivingHours = (ProgressBar) findViewById(R.id.pbTotalDrivingHours);


        tvOffDutyTime = (TextView) findViewById(R.id.tvOffDutyTime);
        tvSleeperTime = (TextView) findViewById(R.id.tvSleeperTime);
        tvDrivingTime = (TextView) findViewById(R.id.tvDrivingTime);
        tvOnDutyTime = (TextView) findViewById(R.id.tvOnDutyTime);
        endLayout = (LinearLayout) findViewById(R.id.endLayout);
        if (Utility._appSetting.getViolationOnDrivingScreen() == 1) {
            endLayout.setVisibility(View.VISIBLE);
        } else {
            endLayout.setVisibility(View.INVISIBLE);
        }

        tvViolation = (TextView) findViewById(R.id.tvViolation);
       /* tvViolation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!MainActivity._violationFg) {
                        return;
                    }

                    final AlertDialog alertDialog = new AlertDialog.Builder(Utility.context).create();
                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setTitle(MainActivity.ViolationTitle);
                    alertDialog.setIcon(Utility.DIALOGBOX_ICON);
                    alertDialog.setMessage(MainActivity.ViolationDescription);
                    alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.ok),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.cancel();
                                }
                            });
                    alertDialog.show();
                } catch (Exception ex) {
                    LogFile.write("onViolationClick Alert Msg: " + ex.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                    LogDB.writeLogs(ELogFragment.class.getName(), "::onViolationClick Alert Msg:", ex.getMessage(), Utility.printStackTrace(ex));
                }
            }
        });*/
        tvViolation.setSelected(true);


        pbTotalCanadaRule.setMax(currentRule == 2 ? 120 * 60 : 70 * 60);
        pbTotalUSRule.setMax(currentRule == 4 ? 60 * 60 : 70 * 60);

        //tvCurrentRuleLabel.setText(getRule(currentRule - 1));
        if (currentRule == 1 || currentRule == 2) //Canada rule
        {
            pbTotalDrivingHours.setMax(13 * 60);
            pbTotalWorkShiftHour.setMax(16 * 60);
        } else {
            pbTotalDrivingHours.setMax(11 * 60);
            pbTotalWorkShiftHour.setMax(14 * 60);
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
        changeCameraDistance();


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

/*    // Created By: Pallavi Wattamwar
    // Created Date: 2 April 2019
    // purpose: update BatteryIcon
    public void onUpdateBatteryIcon(final int level, boolean isPlugged) {
        //level is percent of battery

        if (icBattery != null) {
            icBattery.setVisibility(View.VISIBLE);

            if (level < 20) {
                icBattery.setBackgroundResource(R.drawable.battery_empty_red);
            } else if (level > 20 && level < 40) {
                icBattery.setBackgroundResource(R.drawable.battery_empty_green_30);
            } else if (level > 40 && level < 65) {
                icBattery.setBackgroundResource(R.drawable.battery_empty_green_50);
            } else if (level > 65 && level < 85) {
                icBattery.setBackgroundResource(R.drawable.battery_empty_green_75);
            } else {
                icBattery.setBackgroundResource(R.drawable.db_battery);
            }

        }


    }*/
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
        currentTimeZone.setCameraDistance(scale);
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
                } else {
                    Utility.onScreenUserId = Utility.user2.getAccountId();
                    driverName = Utility.user2.getFirstName() + (Utility.user2.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");
                    tvLoginName.setText(Utility.user2.getUserName());
                }
            } else {
                if (Utility.user1.isOnScreenFg()) {
                    Utility.onScreenUserId = Utility.user1.getAccountId();
                    driverName = Utility.user1.getFirstName() + " " + Utility.user1.getLastName() + (Utility.user1.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");
                    tvLoginName.setText(Utility.user1.getUserName());
                } else {
                    Utility.onScreenUserId = Utility.user2.getAccountId();
                    driverName = Utility.user2.getFirstName() + " " + Utility.user2.getLastName() + (Utility.user2.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");
                    tvLoginName.setText(Utility.user2.getUserName());
                }
            }
            tvLoginName.setText(driverName);

        } catch (Exception exe) {
        }

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 2 April 2019
    // purpose: initialize bitmap to draw the graph
    private void initializeBitmap() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (bmp != null) {
                        bmp.recycle();

                    }
                    bmp = Bitmap.createBitmap(imgDutyStatus.getWidth(),
                            imgDutyStatus.getHeight(), Bitmap.Config.ARGB_8888);
                    // get width of graph
                    imageWidth = imgDutyStatus.getWidth();

                    if (imageWidth == 0)
                        imageWidth = Utility._GridWidth;

                    // get height of graph
                    imageHeight = imgDutyStatus.getHeight();

                    if (imageHeight == 0)
                        imageHeight = Utility._GridHeight;

                    if (imageWidth != 0 && imageHeight != 0) {

                        // initialize bitmap of graph dimensions
                        bmp = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);

                        // initialize canvas to draw on
                        canvas = new Canvas(bmp);

                        DutyStatusGet();
                    }

                } catch (Exception exe) {

                    //Utility.printError(exe.getMessage());
                    Log.d(TAG, "Error: " + exe.getMessage());
                    LogFile.write(DashboardWithGraphActivity.class.getName() + "::initializeBitmap Error: " + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                    LogDB.writeLogs(DashboardWithGraphActivity.class.getName(), "::initializeBitmap Error:", exe.getMessage(), Utility.printStackTrace(exe));

                }
            }
        }, 50);
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 2 April 2019
    // purpose: Get the Duty Status
    private void DutyStatusGet() {
        try {
            clearCanvas();

            dutyStatusList = EventDB.DutyStatusGetToday(Utility.onScreenUserId);

            drawLine(dutyStatusList, Utility.dateOnlyGet(Utility.newDate()), false);

            if (dutyStatusList.size() > 0) {
                int index = dutyStatusList.size() - 1; // last index
                currentStatus = dutyStatusList.get(index).getStatus();

            }


            if (coDriverId > 0) {
                drawLine(EventDB.DutyStatusGetToday(coDriverId), Utility.dateOnlyGet(Utility.newDate()), true);
            }


            StatusHourGet(dutyStatusList);


            if (Utility._appSetting.getViolationOnGrid() == 1) {
                drawViolationArea();
            }
        } catch (Exception e) {
            LogFile.write(DashboardWithGraphActivity.class.getName() + "::DutyStatusGet Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TAG, "::DutyStatusGet Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    // Created By: Deepak Sharma
    // Created Date: 13 June 2019
    // purpose: drawline of Codriver Logs
    private void clearCanvas() {
        //clear bitmap bmp 333
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 2 April 2019
    // purpose: Get the value of offDuty , sleeper, driving, onDuty ;
    private void StatusHourGet(ArrayList<DutyStatusBean> dutyStatus) {
        try {
            int offDuty = 0, sleeper = 0, driving = 0, onDuty = 0;
            boolean firstStatus = true;
            for (DutyStatusBean bean : dutyStatus) {
                Date startTime = Utility.parse(bean.getStartTime());
                startTime = Utility.addSeconds(startTime, -startTime.getSeconds());

                Date endTime = Utility.parse(bean.getEndTime());
                endTime = Utility.addSeconds(endTime, -endTime.getSeconds());

                int totalHours = (int) Math.round((endTime.getTime() - startTime.getTime()) / (1000 * 60.0));

                if (Utility._appSetting.getGraphLine() == 1) {
                    if (firstStatus) {
                        firstStatus = false;
                        if (startTime.after(Utility.dateOnlyGet(Utility.newDate()))) {
                            startTime = Utility.dateOnlyGet(Utility.newDate());

                            totalHours = (int) Math.round((endTime.getTime() - startTime.getTime()) / (1000 * 60.0));
                            bean.setTotalMinutes(totalHours);
                        }
                    }

                    if (endTime.after(Utility.newDate())) {

                        totalHours = (int) Math.round(((Utility.newDate()).getTime() - startTime.getTime()) / (1000 * 60.0));
                        bean.setTotalMinutes(totalHours);
                    }
                }
                // status 1= offDuty,2= Sleeper,3=driving,4=onDuty
                switch (bean.getStatus()) {
                    case 1:
                        offDuty += totalHours;
                        break;
                    case 2:
                        sleeper += totalHours;
                        break;
                    case 3:
                        driving += totalHours;
                        break;
                    case 4:
                        onDuty += totalHours;
                        break;
                }
            }
            if (dutyStatus.size() == 0) {
                offDuty = 1440;
            }
            tvOffDutyTime.setText(Utility.getTimeFromMinute(offDuty));

            tvSleeperTime.setText(Utility.getTimeFromMinute(sleeper));
            tvDrivingTime.setText(Utility.getTimeFromMinute(driving));
            tvOnDutyTime.setText(Utility.getTimeFromMinute(onDuty));


        } catch (Exception e) {
            LogFile.write(DashboardWithGraphActivity.class.getName() + "::StatusHourGet Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DashboardWithGraphActivity.class.getName(), "::StatusHourGet Error:", e.getMessage(), Utility.printStackTrace(e));
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

        // show workshift remaingin time in progress bar
        pbTotalWorkShiftHour.setProgress(pbTotalWorkShiftHour.getMax() - GPSData.WorkShiftRemaining);

        // if current rule is canada
        if (currentRule == 1 || currentRule == 2) {

            pbTotalCanadaRule.setProgress(pbTotalCanadaRule.getMax() - (currentRule == 2 ? GPSData.TimeRemaining120 : GPSData.TimeRemaining70));
            tvCanadaRuleValue.setText(Utility.getTimeFromMinute((currentRule == 2 ? GPSData.TimeRemaining120 : GPSData.TimeRemaining70)));

            // get previous rule of driver other than current country rule
            String except = "'1','2'";
            int previousRule = DailyLogDB.getPreviousRule(Utility.user1.getAccountId(), except);

            pbTotalUSRule.setProgress(pbTotalCanadaRule.getMax() - (previousRule == 4 ? GPSData.TimeRemainingUS60 : GPSData.TimeRemainingUS70));
            tvUSRuleValue.setText(Utility.getTimeFromMinute((previousRule == 4 ? GPSData.TimeRemainingUS60 : GPSData.TimeRemainingUS70)));
        } else { // if current rule is US

            pbTotalUSRule.setProgress(pbTotalCanadaRule.getMax() - (currentRule == 4 ? GPSData.TimeRemainingUS60 : GPSData.TimeRemainingUS70));
            tvUSRuleValue.setText(Utility.getTimeFromMinute((currentRule == 4 ? GPSData.TimeRemainingUS60 : GPSData.TimeRemainingUS70)));

            // get previous rule of driver other than current country rule
            String except = "'3','4'";
            int previousRule = DailyLogDB.getPreviousRule(Utility.user1.getAccountId(), except);

            pbTotalCanadaRule.setProgress(pbTotalCanadaRule.getMax() - (previousRule == 2 ? GPSData.TimeRemaining120 : GPSData.TimeRemaining70));
            tvCanadaRuleValue.setText(Utility.getTimeFromMinute((previousRule == 2 ? GPSData.TimeRemaining120 : GPSData.TimeRemaining70)));
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

            if (Utility._appSetting.getViolationOnDrivingScreen() == 1 || hourLeft==0) {
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
                tvViolation.setText("You must stop driving before " + Utility.ConverDateFormat(MainActivity.ViolationDT)
                        + " otherwise you will be in violation for rule " + MainActivity.ViolationTitle);

            }

        } catch (Exception exe) {
        }
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 27 March 2019
    // purpose: drawline of driver Logs
    private void drawLine(ArrayList<DutyStatusBean> dutyStatus, Date logDate, Boolean coDriverFg) {
        try {


            String logDT = Utility.format(logDate, fullFormat);
            ArrayList<RuleBean> ruleList = DailyLogDB.getRuleByDate(logDT, Utility.onScreenUserId, DailyLogDB.getDailyLogInfo(Utility.onScreenUserId, Utility.getCurrentDate()).get_id());

            Collections.sort(ruleList, RuleBean.dateDesc);
            int ruleId = 1, startMinutes, endMinutes = 0;

            for (int i = 0; i < dutyStatus.size(); i++) {
                DutyStatusBean item = dutyStatus.get(i);
                int status = item.getStatus();
                int specialStatus = item.getSpecialStatus();
                Date startTime = Utility.parse(item.getStartTime()), endTime = Utility.parse(item.getEndTime());

                // graph line upto current time
                if (Utility._appSetting.getGraphLine() == 1 && i == dutyStatus.size() - 1) {
                    endTime = Utility.newDate();
                }

                RuleBean rule = eventRuleGet(startTime, ruleList);
                ruleId = rule.getRuleId();
                if (rule.getRuleEndTime().before(endTime)) {

                    startMinutes = (int) (startTime.getTime() - logDate.getTime()) / (1000 * 60);
                    endMinutes = (int) (rule.getRuleEndTime().getTime() - logDate.getTime()) / (1000 * 60);
                    if (i == 0 && startMinutes > 0) {
                        startMinutes = 0;
                    }
                    drawLine(getX(startMinutes), getY(status), getX(endMinutes), getY(status), ruleId, specialStatus, coDriverFg);

                    //Log.d(TAG, getX(startMinutes) +"/"+ getY(status)+"/"+ getX(endMinutes)+"/"+ getY(status) + "--" + ruleId + "|status=" + status);
                    startTime = rule.getRuleEndTime();
                    ArrayList<RuleBean> ruleEventList = eventRuleListGet(startTime, endTime, ruleList);
                    for (RuleBean ruleBean : ruleEventList) {
                        if (ruleBean.getRuleStartTime().equals(rule.getRuleStartTime()))
                            continue;
                        startMinutes = (int) (startTime.getTime() - logDate.getTime()) / (1000 * 60);
                        endMinutes = (int) ((ruleBean.getRuleEndTime().before(endTime) ? ruleBean.getRuleEndTime() : endTime).getTime() - logDate.getTime()) / (1000 * 60);
                        ruleId = ruleBean.getRuleId();
                        drawLine(getX(startMinutes), getY(status), getX(endMinutes), getY(status), ruleId, specialStatus, coDriverFg);

                        startTime = ruleBean.getRuleEndTime();
                    }
                } else {

                    startMinutes = (int) (startTime.getTime() - logDate.getTime()) / (1000 * 60);
                    endMinutes = (int) (endTime.getTime() - logDate.getTime()) / (1000 * 60);
                    if (i == 0 && startMinutes > 0) {
                        startMinutes = 0;
                    }

                    drawLine(getX(startMinutes), getY(status), getX(endMinutes), getY(status), ruleId, specialStatus, coDriverFg);
                }

                if (i < dutyStatus.size() - 1) {
                    item = dutyStatus.get(i + 1);
                    drawLine(getX(endMinutes), getY(status), getX(endMinutes), getY(item.getStatus()), ruleId, item.getSpecialStatus(), coDriverFg);
                }
            }

            if (dutyStatus.size() == 0) {
                if (ruleList.size() > 0) {
                    ruleId = ruleList.get(0).getRuleId();

                }
                endMinutes = 1439;
                drawLine(getX(0), getY(1), getX(endMinutes), getY(1), ruleId, 0, coDriverFg);
            } else {
                int status = dutyStatus.get(dutyStatus.size() - 1).getStatus();
                if (Utility._appSetting.getGraphLine() == 1) {
                    drawLine(getX(endMinutes), getY(status), getX(1439), getY(status), ruleId, 0, true, coDriverFg);
                }
            }
            imgDutyStatus.setImageDrawable(null);
            imgDutyStatus.setImageBitmap(bmp);

        } catch (Exception exe) {
            Log.d(TAG, "drawLine got exception");
            LogFile.write(DashboardWithGraphActivity.class.getName() + "::drawLine2 Error:" + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DashboardWithGraphActivity.class.getName(), "::drawLine2 Error:", exe.getMessage(), Utility.printStackTrace(exe));
        }
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
    // Created Date: 27 March 2019
    // purpose: set the colour to the lines
    private void drawLine(float x, float y, float xend, float yend, int ruleId, int specialStatus, Boolean coDriverFg) {
        try {
            Paint p = new Paint();
            int color = 0;

            if (ruleId <= 2) {
                color = Utility._appSetting.getColorLineCanada();
            } else {
                color = Utility._appSetting.getColorLineUS();
            }

            color = color == 0 ? Color.BLUE : color;
            int width = 4;

            if (specialStatus > 0) {
                width = 10;
            }
            if (coDriverFg) {
                color = Color.GRAY;
                y += 10;
                yend += 10;
                width = 4;
            }

            p.setColor(color);
            p.setStrokeWidth(width);

            canvas.drawLine(x, y, xend, yend, p);
            canvas.save();


        } catch (Exception e) {
            LogFile.write(DashboardWithGraphActivity.class.getName() + "::drawLine1 Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DashboardWithGraphActivity.class.getName(), "::drawLine1:", e.getMessage(), Utility.printStackTrace(e));
        }
    }

    DashPathEffect effects = new DashPathEffect(new float[]{4, 2, 4, 2}, 0);

    // Created By: Pallavi Wattamwar
    // Created Date: 27 March 2019
    // purpose: set dotted effect to line
    private void drawLine(float x, float y, float xend, float yend, int ruleId, int specialStatus, boolean isDotted, Boolean coDriverFg) {
        try {
            Paint p = new Paint();
            int color = 0;

            if (ruleId <= 2) {
                color = Utility._appSetting.getColorLineCanada();
            } else {
                color = Utility._appSetting.getColorLineUS();
            }


            color = color == 0 ? Color.BLUE : color;


            int width = 4;

            if (isDotted) {
                p.setPathEffect(effects);
            }

            if (specialStatus > 0) {
                width = 10;
            }

            if (coDriverFg) {
                color = Color.GRAY;
                y += 10;
                yend += 10;
                width = 4;
            }

            p.setColor(color);
            p.setStrokeWidth(width);
            canvas.drawLine(x, y, xend, yend, p);
            canvas.save();


        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + ":: dotted drawLine1 :" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DashboardWithGraphActivity.class.getName(), "::dotted drawLine1:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 27 March 2019
    // purpose: get Value of x coordinates
    private int getX(int minutes) {
        float boxWidth = (float) imageWidth;//(float) imgDutyStatus.getWidth();
        //return (int) ((float) Math.ceil(minutes / boxWidth) * boxWidth - boxWidth);
        int x = (int) ((float) boxWidth / 1440 * minutes);
        return x;
    }

    private int getY(int status) {

        int headerHeight = Math.round(imageHeight / 5.27f);
        float boxHeight = (float) (imageHeight - headerHeight) / 4;

        int y = (int) ((status - 1) * boxHeight + boxHeight / 2 + headerHeight);

        return y;
    }


    //Created By: Deepak Sharma
    //Created Date: 5/27/2016
    //Purpose: get rule when event changed
    private RuleBean eventRuleGet(Date eventTime, ArrayList<RuleBean> ruleList) {
        RuleBean obj = new RuleBean();
        obj.setRuleId(1);
        obj.setRuleStartTime(Utility.dateOnlyGet(Utility.newDate()));
        obj.setRuleEndTime(Utility.addDays(Utility.dateOnlyGet(Utility.newDate()), 1));
        for (RuleBean bean : ruleList) {
            obj.setRuleEndTime(bean.getRuleStartTime());
            if (bean.getRuleStartTime().before(eventTime) || bean.getRuleStartTime().equals(eventTime)) {
                return bean;
            }
        }
        return obj;
    }


    //Created By: Deepak Sharma
    //Created Date: 5/27/2016
    //Purpose: get rule when event changed
    private ArrayList<RuleBean> eventRuleListGet(Date eventStartTime, Date eventEndTime, ArrayList<RuleBean> ruleList) {
        ArrayList<RuleBean> obj = new ArrayList<>();

        for (RuleBean bean : ruleList) {
            if ((bean.getRuleStartTime().after(eventStartTime) || bean.getRuleStartTime().equals(eventStartTime)) && bean.getRuleStartTime().before(eventEndTime)) {
                obj.add(bean);
            }
        }
        Collections.sort(obj, RuleBean.dateAsc);
        return obj;
    }

    // Created By: Deepak Sharma
    // Created Date: 5 November 2019
    // Purpose: set UI mode Day/Night
    private void setUIMode() {
        if (Utility.NightModeFg) {
            setTheme(R.style.DashboardWithGraphActivityThemeDark);
            setBrightness(1);
        } else {
            setTheme(R.style.DashboardWithGraphActivityThemeLight);
            setBrightness(255);
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
