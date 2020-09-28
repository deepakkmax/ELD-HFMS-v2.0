package com.hutchsystems.hutchconnect.dashboard;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.beans.GPSData;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.MessageDB;
import com.hutchsystems.hutchconnect.db.TripInspectionDB;
import com.hutchsystems.hutchconnect.util.LetterSpacingTextView;
import com.hutchsystems.hoursofservice.common.TimeUtility;
import com.hutchsystems.hoursofservice.model.ViolationModel;


public class DashboardWithGauageCluster extends AppCompatActivity {

    private LinearLayout tvFreeze, flagBarFreeze;
    RelativeLayout tvGauge;
    ImageView icFreezeActiveUser, icFreezeGPS, icFreezeNetwork, icFreezeWebService, icFreezeCanbus, icFreezeBattery, icFreezeInspection, icFreezeMessage, icFreezeViolation, icFreezeTPMS, imgreezeSpeed, imgFreezeVoltage, imgFreezeCoolantTemp, imgFreezeThrPos, imgFreezeRPM, icFreezeWifi, imgViolation;
    Bitmap batteryBmp;
    Canvas canvas;
    TextView tvFreezeLoginName, tvSpeed, tvVoltage, tvCoolant, tvPosition, tvRPM, tvViolation, tvViolationDate;
    String driverName;
    LetterSpacingTextView tvOdometer, tvEngineHours, tvDrivingRemainingFreeze;
    CheckBox chkRules;
    private FrameLayout frameDiagnostic, frameMalfunction, frameSpeedometer, frameRPM, frameBoost;

    Thread threadUpdateCANInfos;
    int resetOdometerReading = 1;
    String previousOdometerReading = "-1";
    String TAG = MainActivity.class.getName();
    Boolean fromMainActivity;
    ImageButton btnBack;
    LinearLayout endLayout;

    Runnable runnableCAN = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    if (Thread.interrupted())
                        break;

                    updateInformation();

                    Thread.sleep(1000);


                } catch (Exception e) {
                    Log.d(TAG, "ERROR update CAN: " + e.getMessage());
                    break;
                }
            }
        }
    };
    private ImageButton fabback;


    // Created By: Pallavi Wattamwar
    // Created Date: 27 March 2019
    // purpose: Check country is US Or Canada
    private Boolean CheckCountryIsCanada() {
        Boolean canadaFg = false;
        String[] addresses = Utility.FullAddress.split(",");
        if (addresses.length >= 4) {
            if (addresses[3].trim().toUpperCase().equals("US")) {
                canadaFg = false;
            } else {
                canadaFg = true;
            }
        }
        return canadaFg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUIMode();
        super.onCreate(savedInstanceState);
        // Check Activity Launches From MainActivity At Time of Driving Start
        if (getIntent() != null) {

            if (getIntent().getBooleanExtra("FromMainActivity", false)) {
                fromMainActivity = true;
            } else {
                fromMainActivity = false;
            }
        }
        setContentView(R.layout.activity_dashboard_with_gauage_cluster);
        setRequestedOrientation(AppSettings.getOrientation());
        //Utility.setOrientation(this);
        Utility.dashboardContext = this;
        try {
            intView();
        }catch (Exception e){
            e.printStackTrace();
        }
        // Initialization of thread to update Values
        threadUpdateCANInfos = new Thread(runnableCAN);
        threadUpdateCANInfos.start();


    }

    @Override
    protected void onStop() {
        super.onStop();

        if (threadUpdateCANInfos != null)
            threadUpdateCANInfos.interrupt();

    }

    public void intView() {
        tvFreeze = (LinearLayout) findViewById(R.id.tvFreeze);
        flagBarFreeze = (LinearLayout) findViewById(R.id.flagBarFreeze);


        /* ----------------------------------menu icon------------------------------------------------*/

        // Active User Icon
        icFreezeActiveUser = (ImageView) findViewById(R.id.icFreezeDriver);
        int userIcon = Utility.onScreenUserId == Utility.activeUserId ? R.drawable.ic_flagbar_driver_active : R.drawable.ic_flagbar_driver_inactive;
        if (icFreezeActiveUser != null) {
            icFreezeActiveUser.setBackgroundResource(userIcon);
        }

        // gps icon  update
        icFreezeGPS = (ImageView) findViewById(R.id.icFreezeGPS);
        icFreezeGPS.setBackgroundResource(R.drawable.ic_flagbar_gps_on);

        // network icon
        icFreezeNetwork = (ImageView) findViewById(R.id.icFreezeNetwork);
        icFreezeNetwork.setVisibility(View.GONE);

        //wifi icon
        icFreezeWifi = (ImageView) findViewById(R.id.icFreezeWifi);


        // Web service icon
        icFreezeWebService = (ImageView) findViewById(R.id.icFreezeWebService);


        // btb icon
        icFreezeCanbus = (ImageView) findViewById(R.id.icFreezeCanbus);
        icFreezeCanbus.setBackgroundResource(R.drawable.ic_flagbar_canbus_disconnect);
        icFreezeCanbus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utility.executeQuery();
            }
        });
        // Update btb icon
        onUpdateCanbusIcon(CanMessages.mState);

        // Battery icon
        icFreezeBattery = (ImageView) findViewById(R.id.icFreezeBattery);
        // Initialization of Bitmap
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_flagbar_battery_full);
        batteryBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(batteryBmp);
        bmp.recycle();

        // Update inspection icon
        icFreezeInspection = (ImageView) findViewById(R.id.icFreezeInspection);
        onUpdateInspectionIcon();

        icFreezeMessage = (ImageView) findViewById(R.id.icFreezeMessage);
        icFreezeMessage.setBackgroundResource(R.drawable.ic_flagbar_message);
        icFreezeMessage.setVisibility(View.GONE);

        int unreadCount = MessageDB.getUnreadCount();
        if (unreadCount > 0) {
            icFreezeMessage.setVisibility(View.VISIBLE);

        }

        icFreezeViolation = (ImageView) findViewById(R.id.icFreezeViolation);


        tvViolation = (TextView) findViewById(R.id.tvViolation);
        tvViolation.setSelected(true);


        if (Utility._appSetting.getViolationOnDrivingScreen() == 1) {
            tvViolation.setVisibility(View.VISIBLE);
        } else {
            tvViolation.setVisibility(View.INVISIBLE);
        }
        tvViolation.setVisibility(View.INVISIBLE);

        tvViolationDate = (TextView) findViewById(R.id.tvViolationDate);
        imgViolation = (ImageView) findViewById(R.id.imgViolation);
        imgViolation.setVisibility(View.GONE);

        // Set Login name
        tvFreezeLoginName = (TextView) findViewById(R.id.tvFreezeLoginName);
        setDriverName();

        tvGauge = (RelativeLayout) findViewById(R.id.layoutGauge);

        // set Rule
        chkRules = (CheckBox) findViewById(R.id.chkRules);
        chkRules.setClickable(false);
        chkRules.setChecked(MainActivity.currentRule < 3);

        frameDiagnostic = (FrameLayout) findViewById(R.id.frameDiagnostic);
        frameMalfunction = (FrameLayout) findViewById(R.id.frameMalfunction);

        /* ----------------------------------Initialization of fields related Gauge ------------------------------------------------*/
        frameSpeedometer = (FrameLayout) findViewById(R.id.frameSpeedometer);

        // Speed Gauage Cluster
        imgreezeSpeed = (ImageView) findViewById(R.id.imgreezeSpeed);
        imgreezeSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility._appSetting.getVisionMode() == 2) {
                    Utility.NightModeFg = !Utility.NightModeFg;
                    recreateActivity();
                }
            }
        });

        // Speed
        tvSpeed = (TextView) findViewById(R.id.tvSpeed);

        tvOdometer = (LetterSpacingTextView) findViewById(R.id.tvOdometer);

        imgFreezeVoltage = (ImageView) findViewById(R.id.imgFreezeVoltage);
        tvVoltage = (TextView) findViewById(R.id.tvVoltage);

        imgFreezeCoolantTemp = (ImageView) findViewById(R.id.imgFreezeCoolantTemp);
        tvCoolant = (TextView) findViewById(R.id.tvCoolant);

        imgFreezeThrPos = (ImageView) findViewById(R.id.imgFreezeThrPos);
        tvPosition = (TextView) findViewById(R.id.tvPosition);

        frameRPM = (FrameLayout) findViewById(R.id.frameRPM);

        /* ---- On Click of RPM Layout Decrease Screen Brightness  --------------*/
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
        /* ---- On Click of Boost Layout Increase Screen Brightness  --------------*/
        frameBoost = (FrameLayout) findViewById(R.id.frameFuelLevel);
        frameBoost.setOnClickListener(new View.OnClickListener() {
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

        imgFreezeRPM = (ImageView) findViewById(R.id.imgFreezeRPM);
        tvRPM = (TextView) findViewById(R.id.tvRPM);

        tvEngineHours = (LetterSpacingTextView) findViewById(R.id.tvEngineHours);
        tvDrivingRemainingFreeze = (LetterSpacingTextView) findViewById(R.id.tvDrivingRemainingFreeze);


        if (Utility.isLargeScreen(getApplicationContext())) {
            tvOdometer.setLetterSpacing(21);
            tvEngineHours.setLetterSpacing(18);
            tvDrivingRemainingFreeze.setLetterSpacing(22);

        } else {
            tvOdometer.setLetterSpacing(23);
            tvEngineHours.setLetterSpacing(20);
            tvDrivingRemainingFreeze.setLetterSpacing(24);

        }
        setTvEngineHours();
        setTvOdometerReading();

        if (MainActivity.activeCurrentDutyStatus != 3) {
            int secondsleft = (int) Math.round((MainActivity.ViolationDT.getTime() - Utility.newDate().getTime()) / (1000));
            if (secondsleft < 0) {
                secondsleft = 0;
            }

            String timeRemaining = Utility.getTimeFromSeconds(secondsleft);
            tvDrivingRemainingFreeze.setText(timeRemaining);
        }

        if (tvDrivingRemainingFreeze != null) {
            tvDrivingRemainingFreeze.setBackgroundResource(R.drawable.engine_hours_bg);

        }
        setVisionMode();

        //back button
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        // if activity Launches from main activity then gone visiblity of back button
        if (fromMainActivity) {
            btnBack.setVisibility(View.GONE);
        }
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        boolean violationAlertFg = false;
        for (ViolationModel vBean : MainActivity.violationList) {
            if (vBean.getViolationDate().before(Utility.newDate()) && !vBean.isVirtualFg() &&
                    vBean.getViolationDate().after(Utility.parse(Utility.getPreviousDate(CheckCountryIsCanada() ? -14 : -7)))) {
                violationAlertFg = true;
                break;
            }
        }
        onUpdateViolation(violationAlertFg);


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

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: set the vision mode depends on settings
    public void setVisionMode() {
        if (Utility._appSetting.getVisionMode() == 0) {
            Utility.NightModeFg = false;

        } else if (Utility._appSetting.getVisionMode() == 1) {
            Utility.NightModeFg = true;
        }
        setUIMode();
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

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Set Rotation of RPM and Set RPM Textview
    private void setRPMTextViewAndRoatation() {

        float RPM = Float.parseFloat(CanMessages.RPM);

        //if RPM is 1000 then angle is 60
        float angle = 60 * RPM / 1000;

        imgFreezeRPM.setRotation(angle);

        int rpm = Math.round(Float.parseFloat(CanMessages.RPM));
        if (tvRPM != null)
            tvRPM.setText(rpm + "");

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Set Odometer Reading
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

    }

    // time elapsed in seconds
    int timeElapsed = 0;

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Update Information On the screen
    private void updateInformation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (timeElapsed >= 60) {
                    timeElapsed = 0;
                    /* ---- Update Violation Icon -------------*/
                    MainActivity.ProcessViolation(false);
                    // check if violation exists in list voilation icon shows when voilation exists for canada we show last 14 days violation and last 7 day violation for us
                    boolean violationAlertFg = false;
                    for (ViolationModel vBean : MainActivity.violationList) {
                        if (vBean.getViolationDate().before(Utility.newDate()) && !vBean.isVirtualFg() && vBean.getViolationDate().after(Utility.parse(Utility.getPreviousDate(CheckCountryIsCanada() ? -14 : -7)))) {
                            violationAlertFg = true;
                            break;
                        }
                    }
                    onUpdateViolation(violationAlertFg);


                    setTvEngineHours();
                    setTvOdometerReading();

                }
                timeElapsed++;



                /* ---- Set Value of Voltage  --------------*/
                float voltage = Float.parseFloat(CanMessages.Voltage);
                if (voltage < 48f)
                    setVoltageRoatation(voltage);
                if (voltage < 48f) {
                    if (tvVoltage != null)
                        tvVoltage.setText(Float.parseFloat(CanMessages.Voltage) + " V");
                }

                /* ---- check Unit of Speed in Setting and Set Speed Value   --------------*/
                float speed = Float.parseFloat(CanMessages.Speed);
                String speedUnit = " km/h";
                String distanceUnit = " Kms";
                String tempUnit = " ° F";
                if (Utility._appSetting.getUnit() == 2) {
                    speed = speed * .62137f;
                    speedUnit = " MPH";
                    distanceUnit = " Miles";
                }
                // set rotations
                setSpeedRoatation(speed);
                if (tvSpeed != null)
                    tvSpeed.setText(Math.round(speed) + speedUnit);

                /* ---- Set Coolant temp  --------------*/
                float coolantTemp = Float.parseFloat(CanMessages.CoolantTemperature);
                setCoolantRoatation(coolantTemp);

                /* ---- set RPM To Textview and Rotation --------------*/
                setRPMTextViewAndRoatation();
                setFuelLevel(Float.parseFloat(CanMessages.FuelLevel1));


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



                /* ---- Set Boost Value-------------*/
                int fuelLevel = Math.round(Float.parseFloat(CanMessages.FuelLevel1));
                if (tvPosition != null)
                    tvPosition.setText(fuelLevel + "%");
                int coolant = Math.round(coolantTemp);
                if (tvCoolant != null)
                    tvCoolant.setText(coolant + tempUnit);



                /* ---- Set GPS Icon -------------*/
                updateGPSIcon();
                /* ---- Set Wifi icon, WebService Icon,Network Icon  -------------*/
                updateWifiAndWebserviceIcon(Utility.isInternetOn());

                /* ---- Set BTB Icon  -------------*/
                onUpdateCanbusIcon(CanMessages.mState);

                /* ---- Set Battery Icon  -------------*/
                onUpdateBatteryIcon(Utility.BatteryLevel, GPSData.ACPowerFg == 1);

                /* ---- Update Active User Icon -------------*/
                updateActiveIcon();



                /* ---- Set Driving time and Violation Fields  -------------*/
                if (tvDrivingRemainingFreeze != null && MainActivity.activeCurrentDutyStatus == 3 && MainActivity.ViolationDT != null) {
                    int secondsLeft = (int) (MainActivity.ViolationDT.getTime() - (Utility.newDate()).getTime()) / 1000;
                    if (secondsLeft < 0) {
                        secondsLeft = 0;
                    }

                    if (secondsLeft == 0) {
                        if (GPSData.NoHOSViolationFgFg == 1) {
                            //   onUpdateViolation(true);

                        }
                        tvDrivingRemainingFreeze.setBackgroundResource(R.drawable.remaining_driving_hours_bg_red);
                    } else if (secondsLeft <= 3600) {
                        tvDrivingRemainingFreeze.setBackgroundResource(R.drawable.remaining_driving_hours_bg_yellow);
                    }


                    String timeRemaining = Utility.getTimeFromSeconds(secondsLeft);
                    tvDrivingRemainingFreeze.setText(timeRemaining);

                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        tvFreeze.setBackgroundResource(0);
        if (tvFreeze != null) {
            if (Utility.activeUserId == 0)
                tvFreeze.setBackgroundResource(R.drawable.login_freeze);
            else {
                setUIMode();
            }
        }
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 27 March 2019
    // purpose: update webserice icon, wifi icon ,network icon depends on connectivity

    public void updateWifiAndWebserviceIcon(boolean isConnected) {
        icFreezeWifi.setVisibility(View.VISIBLE);

        if (isConnected) {

            //set Wifi icon to solid GREEN
            //Log.d(TAG, "Show wifi GREEN");
            if (GPSData.WifiOnFg == 1) {
                icFreezeWifi.setBackgroundResource(R.drawable.ic_flagbar_wifi_on);
            } else if (GPSData.CellOnlineFg == 1) {
                icFreezeNetwork.setBackgroundResource(R.drawable.ic_flagbar_web_service_on);
            }

            if (Utility.webServiceConnectivity) {
                icFreezeWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_on);
            }

        } else {


            //set Wifi icon to GREY
            //Log.d(TAG, "Show wifi GREY");
            icFreezeWifi.setBackgroundResource(R.drawable.ic_flagbar_wifi_off);
            icFreezeWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_off);


        }

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Set Throttle Rotation
    private void setFuelLevel(float position) {

        //if position is 100 then angle is 180
        float angle = 180 * position / 100;

        // float from = imgFreezeThrPos.getRotation();
        imgFreezeThrPos.setRotation(angle);
        // android.animation.ObjectAnimator.ofFloat(imgFreezeThrPos, "rotation", from, angle).start();

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Set Coolant  Rotation
    private void setCoolantRoatation(float coolant) {

        //if voltage 200 then angle is 90
        float angle = (coolant - 100) * 90 / 100.0f;

        //  float from = imgFreezeCoolantTemp.getRotation();
        imgFreezeCoolantTemp.setRotation(angle);

        //android.animation.ObjectAnimator.ofFloat(imgFreezeCoolantTemp, "rotation", from, angle).start();

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Set Voltage  Rotation
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

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Set speed  Rotation
    private void setSpeedRoatation(float speed) {

        //if speed is 10 then angle is 15
        float angle = 15 * speed / 10;

        // float from = imgreezeSpeed.getRotation();
        imgreezeSpeed.setRotation(angle);
        //   android.animation.ObjectAnimator.ofFloat(imgreezeSpeed, "rotation", from, angle).start();

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Set Driver Name
    private void setDriverName() {
        try {
            if (!Utility.isLargeScreen(getApplicationContext())) {
                if (Utility.user1.isOnScreenFg()) {
                    Utility.onScreenUserId = Utility.user1.getAccountId();
                    driverName = Utility.user1.getFirstName() + (Utility.user1.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");

                } else {
                    Utility.onScreenUserId = Utility.user2.getAccountId();
                    driverName = Utility.user2.getFirstName() + (Utility.user2.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");

                }
            } else {
                if (Utility.user1.isOnScreenFg()) {
                    Utility.onScreenUserId = Utility.user1.getAccountId();
                    driverName = Utility.user1.getFirstName() + " " + Utility.user1.getLastName() + (Utility.user1.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");

                } else {
                    Utility.onScreenUserId = Utility.user2.getAccountId();
                    driverName = Utility.user2.getFirstName() + " " + Utility.user2.getLastName() + (Utility.user2.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");

                }
            }

            tvFreezeLoginName.setText(driverName);
        } catch (Exception exe) {
        }

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Update Violation
    public void onUpdateViolation(boolean status) {
        int visibility = status ? View.VISIBLE : View.GONE;
        try {

            tvDrivingRemainingFreeze.setBackgroundResource(status ? R.drawable.remaining_driving_hours_bg_red : R.drawable.engine_hours_bg);

            String message="";
            // status true if driver is in violation
            if (status) {
                icFreezeViolation.setBackgroundResource(R.drawable.ic_flagbar_violation);
                imgViolation.setBackgroundResource(R.drawable.ic_flagbar_violation);
                message="You must stop driving! You are in violation since  " + Utility.ConverDateFormat(MainActivity.ViolationDT) + " for rule " + MainActivity.ViolationTitle;

            } else {

                // time elpased untill next violation
                double timeElpased = TimeUtility.getDiff(Utility.newDate(), MainActivity.ViolationDT, TimeUtility.Unit.MIN);

                // if time elpased is less than 1 hour then show yellow icon
                if (timeElpased < 60d) {
                    icFreezeViolation.setBackgroundResource(R.drawable.ic_flagbar_violation_upcoming);
                    imgViolation.setBackgroundResource(R.drawable.ic_flagbar_violation_upcoming);
                } else {

                    icFreezeViolation.setBackgroundResource(R.drawable.ic_flagbar_violation_off);
                    imgViolation.setBackgroundResource(R.drawable.ic_flagbar_violation_off);
                }

                message="You must stop driving  before " + Utility.ConverDateFormat(MainActivity.ViolationDT) + " otherwise you will be in violation for rule " + MainActivity.ViolationTitle;

            }

                tvViolation.setText(message);


        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Update Inspection Icon
    public void onUpdateInspectionIcon() {
        boolean inspections = TripInspectionDB.getInspections(Utility.getCurrentDate(), Utility.user1.getAccountId(), Utility.user2.getAccountId());
        if (inspections) {
            if (icFreezeInspection != null) {
                icFreezeInspection.setVisibility(View.VISIBLE);
                icFreezeInspection.setBackgroundResource(R.drawable.ic_flagbar_dvir_done);
            }
        } else {
            if (icFreezeInspection != null) {
                icFreezeInspection.setVisibility(View.VISIBLE);
                icFreezeInspection.setBackgroundResource(R.drawable.ic_flagbar_dvir_pending);
            }
        }
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Update Battery icon depnds on level
    public void onUpdateBatteryIcon(int level, boolean isPlugged) {

        if (icFreezeBattery != null) {
            icFreezeBattery.setVisibility(View.VISIBLE);
            drawingBattery(level, isPlugged, batteryBmp, icFreezeBattery);
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
    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Update Battery icon
    public void onUpdateCanbusIcon(final int state) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                if (icFreezeCanbus != null) {
                    icFreezeCanbus.setVisibility(View.VISIBLE);

                    switch (state) {
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

    public void onUpdateWebServiceIcon(boolean result) {


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


    // Created By: Deepak Sharma
    // Created Date: 22 April 2019
    // purpose: update gps icon
    public void updateGPSIcon() {
        icFreezeGPS.setBackgroundResource(MainActivity.isGPSEnabled ? R.drawable.ic_flagbar_gps_on : R.drawable.ic_flagbar_gps_off);

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 11 april
    // purpose: Update Active User Icon
    public void updateActiveIcon() {

        int userIcon = Utility.onScreenUserId == Utility.activeUserId ? R.drawable.ic_flagbar_driver_active : R.drawable.ic_flagbar_driver_inactive;
        if (icFreezeActiveUser != null) {
            icFreezeActiveUser.setBackgroundResource(userIcon);
        }
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

    @Override
    public void onBackPressed() {

    }


}
