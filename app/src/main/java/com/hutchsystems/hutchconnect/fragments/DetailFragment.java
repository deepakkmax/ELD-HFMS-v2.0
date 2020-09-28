package com.hutchsystems.hutchconnect.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.EventAdapter;
import com.hutchsystems.hutchconnect.beans.DailyLogBean;
import com.hutchsystems.hutchconnect.beans.DutyStatusBean;
import com.hutchsystems.hutchconnect.beans.EventBean;
import com.hutchsystems.hutchconnect.beans.GPSData;
import com.hutchsystems.hutchconnect.beans.RuleBean;
import com.hutchsystems.hutchconnect.beans.TripInfoBean;
import com.hutchsystems.hutchconnect.beans.UserBean;
import com.hutchsystems.hutchconnect.beans.VehicleBean;
import com.hutchsystems.hutchconnect.bll.HourOfService;
import com.hutchsystems.hutchconnect.common.ChatClient;
import com.hutchsystems.hutchconnect.common.CustomDateFormat;
import com.hutchsystems.hutchconnect.common.GetCall;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.PostCall;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.common.ZoneList;
import com.hutchsystems.hutchconnect.db.CarrierInfoDB;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.EventDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.UserDB;
import com.hutchsystems.hutchconnect.tasks.ModifiedSyncData;
import com.hutchsystems.hoursofservice.model.ViolationModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class DetailFragment extends Fragment implements View.OnClickListener, InputInformationDialog.InputInformationDialogInterface,
        InputTruckIDDialog.InputTruckIDDialogInterface, ChatClient.RefreshData,
        TotalMilesFragment.OnFragmentInteractionListener {
    String TAG = DetailFragment.class.getName();
    static String fullFormat = "yyyy-MM-dd HH:mm:ss";
    int driverId = 1;
    TextView tvRecordDate, tvUSDot, tvDriverLicense, tvDriverLicenseState, tvTimeZoneText, tvDriverName, tvDrivingRemaningHour, tvWorkShiftHour, tvRule;
    TextView tvCoDriverName, tvELDManufacturer, tvDataDiagnostic, tvCarrier, tvDriverID, tvCoDriverID, tvUnidentifiedDriverRecords, tvAddress, tvHomeTerminalAddress;
    TextView tvELDMalfunction, tvStartEndOdometer, tvMilesToday, tvTruckTractorVIN, tvExemptDriverStatus, tvStartEndEngineHours, tvStartEndOdometerLabel, tvStartEndEngineHoursLabel;
    EditText edTrailerID, edShippingID, edCommodity, tvTruckTractorID;
    ImageButton fabUncertify, imgSync, imgAction;
    TextView tvOffDutyTime, tvSleeperTime, tvDrivingTime, tvOnDutyTime, tvCurrentLocation;

    private ImageView imgDutyStatus;


    Bitmap bmp;
    Canvas canvas;
    public Date currentDate;
    Date selectedDate;
    public Date statusDT;

    ListView lvEvents;

    EventAdapter eventAdapter;
    List<EventBean> listEvents;
    int dailyLogId, certifyFG;

    int finalHeight = 0;
    int finalWidth = 0;
    int imageWidth = 0;
    int imageHeight = 0;
    int currentRule = 1;
    View inforHeader;
    boolean actionFg = false;
    InputTruckIDDialog truckIdDialog;
    InputInformationDialog infosDialog;

    LinearLayout layoutOption;
    public int currentStatus = 1;
    private ArrayList<DutyStatusBean> dutyStatusList = new ArrayList<>();

    int todayDistance = 0;
    private ImageButton imgPost, fab_mid_night;

    ModifiedSyncData.PostTaskListener<Boolean> postTaskListener = new ModifiedSyncData.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            mListener.showLoaderAnimation(false);
            if (result) {
                // Check current Day Edit Request is Pending
                if (EventDB.GetCurrentDayEventEditRequest(driverId, dailyLogId)) {
                    Utility.showErrorMessage(Utility.context, "You must Accept or Reject Edit Request prior to posting data.");

                } else // If Edit Request is not pending then post data
                {

                    callPost();
                }
            }
        }
    };

    @Override
    public void onTotalDistanceSave(String total) {
        try {
            tvMilesToday.setText(total);
            int distance = Integer.parseInt(total);
            todayDistance = distance;
            if (Utility._appSetting.getUnit() == 2) {
                distance = Double.valueOf(distance * 1.60934f).intValue();
            }
            DailyLogDB.DailyLogDistanceSave(dailyLogId, driverId, distance, 0);

            if (selectedDate.equals(Utility.dateOnlyGet(Utility.newDate()))) {
                if (Utility.onScreenUserId == Utility.activeUserId)
                    GPSData.TotalDistance = distance;
            }
        } catch (Exception exe) {
        }
    }

    @SuppressLint("ValidFragment")
    public class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            //int hour = c.get(Calendar.HOUR_OF_DAY);
            //int minute = c.get(Calendar.MINUTE);
            c.setTime(selectedDate);

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dilog = new DatePickerDialog(getActivity(), this, year, month, day);
            DatePicker picker = dilog.getDatePicker();
            c.setTime(Utility.newDate());
            c.add(Calendar.DATE, 1);
            picker.setMaxDate(c.getTime().getTime());
            c.setTime(Utility.dateOnlyGet(Utility.newDate()));
            c.add(Calendar.DATE, -14);
            picker.setMinDate(c.getTime().getTime());
            return dilog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day); //reset seconds to zero

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            try {
                calendar.setTime(formatter.parse(formatter.format(calendar.getTime())));
                selectedDate = calendar.getTime();
                refresh();
                Log.d(TAG, "Date:" + Utility.GetString(selectedDate));
            } catch (Exception ex) {
                Log.d(TAG, "Error: " + ex.getMessage());
                LogFile.write(DetailFragment.class.getName() + "::onDateSet Error: " + ex.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                LogDB.writeLogs(DetailFragment.class.getName(), "::onDateSet Error:", ex.getMessage(), Utility.printStackTrace(ex));

            }

        }
    }

    private OnFragmentInteractionListener mListener;

    public DetailFragment() {
        // Required empty public constructor
    }

    private void showhideAction() {
        if (!actionFg) {
            final Animation animationFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
            layoutOption.setVisibility(View.VISIBLE);
            android.animation.ObjectAnimator.ofFloat(imgAction, "rotation", 0, 45).start();
            layoutOption.startAnimation(animationFadeIn);
        } else {
            final Animation animationFadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
            layoutOption.setVisibility(View.INVISIBLE);
            android.animation.ObjectAnimator.ofFloat(imgAction, "rotation", 45, 0).start();
            layoutOption.startAnimation(animationFadeOut);

        }
        actionFg = !actionFg;
    }

    private void initialize(View view) {
        try {

            ChatClient.rListner = this;
            layoutOption = (LinearLayout) view.findViewById(R.id.layoutOption);
            imgAction = (ImageButton) view.findViewById(R.id.imgAction);

            imgAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showhideAction();
                }
            });
            //driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
            tvOffDutyTime = (TextView) view.findViewById(R.id.tvOffDutyTime);
            tvSleeperTime = (TextView) view.findViewById(R.id.tvSleeperTime);
            tvDrivingTime = (TextView) view.findViewById(R.id.tvDrivingTime);
            tvOnDutyTime = (TextView) view.findViewById(R.id.tvOnDutyTime);
            tvCurrentLocation = (TextView) view.findViewById(R.id.tvCurrentLocation);
            String location = Utility.currentLocation.getLocationDescription();

            // only show if it's available
            if (location == null || location.isEmpty()) {
                tvCurrentLocation.setText("Location is not found");
            } else {
                tvCurrentLocation.setText(location);
            }
            tvRecordDate = (TextView) view.findViewById(R.id.tvRecordDate);
            tvUSDot = (TextView) view.findViewById(R.id.tvUsDot);
//            tvDriverLicense = (TextView) view.findViewById(R.id.tvDriverLicense);
//            tvDriverLicenseState = (TextView) view.findViewById(R.id.tvDriverLicenseState);
            tvTimeZoneText = (TextView) view.findViewById(R.id.tvTimeZoneText);
            tvDriverName = (TextView) view.findViewById(R.id.tvDriverName);
            tvCoDriverName = (TextView) view.findViewById(R.id.tvCoDriverName);
            tvELDManufacturer = (TextView) view.findViewById(R.id.tvELDManufacturer);
            tvCarrier = (TextView) view.findViewById(R.id.tvCarrier);
            tvAddress = (TextView) view.findViewById(R.id.tvAddress);
            tvDriverID = (TextView) view.findViewById(R.id.tvDriverID);
            tvCoDriverID = (TextView) view.findViewById(R.id.tvCoDriverID);
            tvTruckTractorID = (EditText) view.findViewById(R.id.tvTruckTractorID);

            tvDrivingRemaningHour = (TextView) view.findViewById(R.id.tvDrivingRemaningHour);
            tvWorkShiftHour = (TextView) view.findViewById(R.id.tvWorkShiftHour);
            tvRule = (TextView) view.findViewById(R.id.tvRule);

            tvUnidentifiedDriverRecords = (TextView) view.findViewById(R.id.tvUnidentifiedDriverRecords);
            boolean unidentifiedFg = EventDB.UnIdentifiedEventFg();
            if (unidentifiedFg)
                tvUnidentifiedDriverRecords.setText(getString(R.string.yes));

            tvELDMalfunction = (TextView) view.findViewById(R.id.tvELDMalfunction);
            if (Utility.malFunctionIndicatorFg) {
                tvELDMalfunction.setText(getString(R.string.yes));
            }
            // tvCertify = (TextView) view.findViewById(R.id.tvCertify);
            tvStartEndOdometer = (TextView) view.findViewById(R.id.tvStartEndOdometer);
            tvStartEndOdometerLabel = (TextView) view.findViewById(R.id.tvStartEndOdometerLabel);
            tvMilesToday = (TextView) view.findViewById(R.id.tvMilesToday);
           /* tvMilesToday.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchTotalDistanceDialog();
                }
            });*/
            tvTruckTractorVIN = (TextView) view.findViewById(R.id.tvTruckTractorVIN);
            tvExemptDriverStatus = (TextView) view.findViewById(R.id.tvExemptDriverStatus);

            tvStartEndEngineHours = (TextView) view.findViewById(R.id.tvStartEndEngineHours);
            tvStartEndEngineHoursLabel = (TextView) view.findViewById(R.id.tvStartEndEngineHoursLabel);

            tvDataDiagnostic = (TextView) view.findViewById(R.id.tvDataDiagnostic);
            if (Utility.dataDiagnosticIndicatorFg) {
                tvDataDiagnostic.setText(getString(R.string.yes));
            }

            tvTimeZoneText.setText(ZoneList.getTimezoneName(false));

            edShippingID = (EditText) view.findViewById(R.id.edShippingID);

            edTrailerID = (EditText) view.findViewById(R.id.edTrailerID);

            fabUncertify = (ImageButton) view.findViewById(R.id.fab_certify);
            imgSync = (ImageButton) view.findViewById(R.id.imgSync);

            if (Utility.InspectorModeFg) {
                imgAction.setVisibility(View.INVISIBLE);
            } else {
                imgAction.setVisibility(View.VISIBLE);
            }

            if (!Utility.InspectorModeFg) {
                imgSync.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        showhideAction();

                        // do not try post if device is offline
                        if (!Utility.isInternetOn()) {
                            return;
                        }

                        mListener.showLoaderAnimation(true);
                        Thread thSyncEvent = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String date = Utility.format(selectedDate, CustomDateFormat.dt1);
                                boolean status = GetCall.DailyLogEventGetByDateSync(date);
                                if (status) {
                                    if (getActivity() != null)
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                refresh();
                                                mListener.showLoaderAnimation(false);
                                            }
                                        });
                                }
                            }
                        });
                        thSyncEvent.setName("thSyncEvent");
                        thSyncEvent.start();
                    }
                });
            }
            if (!Utility.InspectorModeFg) {
                fabUncertify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showhideAction();
                        if (dailyLogId == 0) {
                            //  Utility.showAlertMsg(getString(R.string.logbook_doesnot_exists));
                            showDialogForMissingEvent();
                            return;
                        }
                        certifyLogBook();
                    }
                });

                tvTruckTractorID.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "enter truck id");
                        launchEnterTruckID();
                    }
                });

                edShippingID.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchInputDialog();
                    }
                });

                edTrailerID.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchInputDialog();
                    }
                });
            }
            // Restrict user to certify current day log only allow user to certify previous day log
            if (selectedDate.equals(Utility.dateOnlyGet(Utility.newDate()))) {
                fabUncertify.setClickable(false);
                fabUncertify.setImageResource(R.drawable.ic_certified);
            } else if (certifyFG == 1) {
                fabUncertify.setClickable(false);
                fabUncertify.setImageResource(R.drawable.ic_verified_user_black);
            } else {
                fabUncertify.setClickable(!Utility.InspectorModeFg);
                fabUncertify.setImageResource(R.drawable.ic_fab_uncertified_red);
            }

//            if (certifyFG == 1) {
//                tvCertify.setText("Certify");
//            } else {
//                tvCertify.setText("Not Certify");
//            }


            //set values into controls
            UserBean user = null;//Utility.user1.isOnScreenFg() ? Utility.user1 : Utility.user2;
            UserBean coUser = null;
            if (Utility.user1.isOnScreenFg()) {
                user = UserDB.userInfoGet(Utility.onScreenUserId);
            } else {
                user = UserDB.userInfoGet(Utility.onScreenUserId);
            }

            // show work shift remaining time in text view
            tvWorkShiftHour.setText(Utility.getTimeFromMinute(GPSData.WorkShiftRemaining) + " WS Hrs");

            int hourLeft = 0;
            //show driving remaning hours
            if (MainActivity.ViolationDT != null) {
                hourLeft = (int) Math.round((MainActivity.ViolationDT.getTime() - Utility.newDate().getTime()) / (1000 * 60d));
                if (hourLeft < 0) {
                    hourLeft = 0;
                }

            }

            tvDrivingRemaningHour.setText(Utility.getTimeFromMinute(hourLeft) + " D Hrs");

            // show hours according to rule selected
            currentRule = DailyLogDB.getCurrentRule(Utility.onScreenUserId);
            if (currentRule == 1) {
                tvRule.setText(Utility.getTimeFromMinute(GPSData.TimeRemaining70) + " Hrs");
            } else if (currentRule == 2) {
                tvRule.setText(Utility.getTimeFromMinute(GPSData.TimeRemaining120) + " Hrs");
            } else {
                tvRule.setText(Utility.getTimeFromMinute(GPSData.TimeRemainingUS70) + " Hrs");
            }

            tvRecordDate.setText(Utility.GetStringDate(selectedDate));
            tvDriverName.setText(user.getFirstName() + " " + user.getLastName());
            tvDriverID.setText(user.getUserName());
//            tvDriverLicense.setText(user.getDrivingLicense());
//            tvDriverLicenseState.setText(user.getDlIssueState());
            tvCarrier.setText(Utility.CarrierName);
            tvELDManufacturer.setText(Utility.ELDManufacturer);
            tvUSDot.setText(Utility.USDOT);
            String plateNo = Utility.PlateNo == null || Utility.PlateNo.isEmpty() ? "" : " (" + Utility.PlateNo + ")";
            tvTruckTractorID.setText(Utility.UnitNo + plateNo);
            tvTruckTractorVIN.setText(Utility.VIN);
            tvExemptDriverStatus.setText(user.getExemptELDUseFg() == 1 ? getString(R.string.yes) : getString(R.string.no));


            tvHomeTerminalAddress = (TextView) view.findViewById(R.id.tvHomeTerminalAddress);
            tvHomeTerminalAddress.setText(user.getFullAddress());
            currentRule = DailyLogDB.getCurrentRule(Utility.onScreenUserId);
            getVehicleList();

            // post Daily Log data as per Date
            imgPost = (ImageButton) view.findViewById(R.id.fab_post);

            // post Daily Log data as per Date
            fab_mid_night = (ImageButton) view.findViewById(R.id.fab_mid_night);
            if (!Utility.InspectorModeFg) {
                fab_mid_night.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // do not try post if device is offline
                        if (!Utility.isInternetOn()) {
                            Utility.showMsg("Please check internet connection");
                            return;
                        }

                        MidnightEventCreate();
                    }
                });
                imgPost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showhideAction();

                        // do not try post if device is offline
                        if (!Utility.isInternetOn()) {
                            return;
                        }

                        // First Check edit Request is pending
                        mListener.showLoaderAnimation(true);
                        new ModifiedSyncData(postTaskListener).execute();
                        // callPost();
                    }
                });
            } else {
                fab_mid_night.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::initialize Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DetailFragment.class.getName(), "::initialize Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    boolean canadaFg;

    //Created By: Pallavi Wattamwar
    //Created Date: 28 November 2018
    //Purpose: Show Dialog for missing event
    public void showDialogForMissingEvent() {
        final AlertDialog alertDialog = new AlertDialog.Builder(Utility.context).create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("E-Log");
        alertDialog.setIcon(Utility.DIALOGBOX_ICON);
        alertDialog.setMessage(getString(R.string.Manually_Create_Logs));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, Utility.context.getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // create missing logs
                        int logId = DailyLogDB.DailyLogCreateByDate(Utility.onScreenUserId, Utility.GetString(selectedDate), "", "", "");
                        // EventDB.EventCreate(Utility.GetStringDate(selectedDate) + " 00:00:00", 1, 1, getString(R.string.duty_status_changed_to_off_duty), 1, 1, logId, Utility.onScreenUserId, "", MainActivity.currentRule);
                        // Depends on logid we Certify the logs
                        DailyLogDB.DailyLogCertify("", Utility.onScreenUserId, logId + "");
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refresh();

                                }
                            });
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

    private void CheckCountry() {
        String[] addresses = Utility.FullAddress.split(",");
        if (addresses.length >= 4) {
            tvAddress.setText(Utility.FullAddress);
            if (addresses[3].trim().toUpperCase().equals("US")) {
              /*  tvStartEndOdometer.setVisibility(View.GONE);
                tvStartEndOdometerLabel.setVisibility(View.GONE);
                tvStartEndEngineHours.setVisibility(View.GONE);
                tvStartEndEngineHoursLabel.setVisibility(View.GONE);*/
                canadaFg = false;
            } else {
                canadaFg = true;
            }
        } else {
         /*   tvStartEndOdometer.setVisibility(View.GONE);
            tvStartEndOdometerLabel.setVisibility(View.GONE);
            tvStartEndEngineHours.setVisibility(View.GONE);
            tvStartEndEngineHoursLabel.setVisibility(View.GONE);*/
        }
    }


    private void getVehicleList() {
        try {
            ArrayList<VehicleBean> list = DailyLogDB.getVehicleByLogId(dailyLogId);
            StringBuilder sbVehicle = new StringBuilder();
            StringBuilder sbVIN = new StringBuilder();

            for (VehicleBean bean : list) {
                sbVehicle.append(bean.getUnitNo()).append(" (" + bean.getPlateNo() + ") \n");
                sbVIN.append(bean.getVIN() + " \n");
            }

            if (sbVehicle.length() > 1) {
                sbVehicle.setLength(sbVehicle.length() - 1);
                tvTruckTractorID.setText(sbVehicle.toString());
            }

            if (sbVIN.length() > 1) {
                sbVIN.setLength(sbVIN.length() - 1);
                tvTruckTractorVIN.setText(sbVIN.toString());
            }
        } catch (Exception exe) {

        }
    }

    // Created By: Sahil Bansal
    // Created Date: 24 October 2019
    // Purpose: initialize bitmap to draw graph
    private void initializeBitmap() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (bmp != null) {
                        bmp.recycle();

                    }


                    // get width of graph
                    imageWidth = imgDutyStatus.getWidth();

                    // get height of graph
                    imageHeight = imgDutyStatus.getHeight();

                    if (imageWidth != 0 && imageHeight != 0) {

                        // initialize bitmap of graph dimensions
                        bmp = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);

                        // initialize canvas to draw on
                        canvas = new Canvas(bmp);

                        initializeStatus();
                    }
                } catch (Exception exe) {

                    //Utility.printError(exe.getMessage());
                    Log.d(TAG, "Error: " + exe.getMessage());
                    LogFile.write(DetailFragment.class.getName() + "::initializeBitmap Error: " + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                    LogDB.writeLogs(DetailFragment.class.getName(), "::initializeBitmap Error:", exe.getMessage(), Utility.printStackTrace(exe));

                }
            }
        }, 50);
    }

    //Created By: Pallavi Wattamwar
    //Purpose: Show Dialog for Data Post Confirmation
    // Created Date: 14 January 2019
    public void callPost() {
        final AlertDialog ad = new AlertDialog.Builder(getActivity())
                .create();
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle(getString(R.string.send_data_confirmation_title));
        ad.setIcon(R.drawable.ic_launcher);
        ad.setMessage(getString(R.string.send_data_message));
        ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {

                        mListener.showLoaderAnimation(true);
                        Thread thPostEvent = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String date = Utility.format(selectedDate, CustomDateFormat.dt1);
                                // Post Daily Log Data as per Date
                                boolean status = PostCall.PostLogEventByDate(Utility.getDate(date), Utility.onScreenUserId);
                                if (status) {
                                    if (getActivity() != null)
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                refresh();
                                                mListener.showLoaderAnimation(false);
                                            }
                                        });
                                }
                            }
                        });
                        thPostEvent.setName("thPostEvent");
                        thPostEvent.start();
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


    //Created By: Pallavi Wattamwar
    //Purpose: Show Dialog for Data Post Confirmation
    // Created Date: 14 January 2019
    public void MidnightEventCreate() {
        final AlertDialog ad = new AlertDialog.Builder(getActivity())
                .create();
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle(getString(R.string.midnight_create_Title));
        ad.setIcon(R.drawable.ic_launcher);
        ad.setMessage(getString(R.string.midnight_create_alert));
        ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {

                        mListener.showLoaderAnimation(true);
                        Thread thPostEvent = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String date = Utility.format(selectedDate, CustomDateFormat.dt1);

                                // Call service to get vehicle data for mid night event
                                final int status = GetCall.MidnightEventGet(Utility.getDate(date), Utility.onScreenUserId);

                                if (getActivity() != null)
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String message = "Unable to create";
                                            if (status == 1) {
                                                message = "Mid night event is created successfully.";
                                                refresh();
                                            } else if (status == -1) {
                                                message = "Unable to fetch odometer reading for mid night event. Please contact administrator";
                                            } else if (status == -2) {
                                                message = "Midnight event for previous date is already exists.";
                                            } else if (status == -3) {
                                                message = "Midnight event for selected date is already exists.";
                                            }

                                            mListener.showLoaderAnimation(false);
                                            Utility.showAlertMsg(message);
                                        }
                                    });

                            }
                        });
                        thPostEvent.setName("thMidnightEventThread");
                        thPostEvent.start();
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

    public static DetailFragment newInstance() {
        return new DetailFragment();

    }

    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context).inflate(R.layout.tabdesign, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    private void initializeTab(View view) {
        TabHost host = (TabHost) view.findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        View tabview = createTabView(host.getContext(), getString(R.string.driver));
        TabHost.TabSpec spec = host.newTabSpec(getString(R.string.driver)).setIndicator(tabview);
        spec.setContent(R.id.tabDriver);
        host.addTab(spec);

        //Tab 2
        tabview = createTabView(host.getContext(), getString(R.string.vehicle));
        spec = host.newTabSpec(getString(R.string.vehicle)).setIndicator(tabview);
        spec.setContent(R.id.tabVehicle);
        host.addTab(spec);

        //Tab 3
        tabview = createTabView(host.getContext(), getString(R.string.company));
        spec = host.newTabSpec(getString(R.string.company)).setIndicator(tabview);
        spec.setContent(R.id.tabCompany);
        host.addTab(spec);
    }

    public static final String ARG_Date = "date";
    private int mPageNumber, totalPage = 15;

    public static DetailFragment newInstance(Date date) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_Date, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDate = (Date) getArguments().getSerializable(ARG_Date);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Configuration config = getResources().getConfiguration();
        View view = inflater.inflate(R.layout.fragment_inspectdailylog, container, false);
        try {
            // currentDate, Utility.dutyStatusList was null so intializing
            if (currentDate == null) {
                currentDate = Utility.dateOnlyGet(Utility.newDate());
            }
            selectedDate = currentDate;

            driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
            DailyLogBean dailyLog = DailyLogDB.getDailyLogInfo(driverId, Utility.GetString(selectedDate));
            dailyLogId = dailyLog.get_id();
            certifyFG = dailyLog.getCertifyFG();
            todayDistance = dailyLog.getTotalDistance();

            lvEvents = (ListView) view.findViewById(R.id.lvEvent);

            // get current day's duty status
            dutyStatusList = EventDB.DutyStatusGetByDate(Utility.onScreenUserId, currentDate);

            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                inforHeader = inflater.inflate(R.layout.fragment_detail, null, false);
                //lvEvents.addHeaderView(header);
            } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                inforHeader = inflater.inflate(R.layout.fragment_detail_portrait, null, false);
                //lvEvents.addHeaderView(header);
            }
            //lvEvents.addHeaderView(header);

            lvEvents.addHeaderView(inforHeader);

            initialize(view);

            initializeTab(view);

            CheckCountry();

            /* if (mListener != null) {
                String title = getContext().getResources().getString(R.string.title_inspect_elog);
                mListener.setTitle(title);
            }*/

            listEvents = getListEvents(Utility.GetString(selectedDate));
            eventAdapter = new EventAdapter(this.getActivity(), listEvents);

            eventAdapter.setFromInspect(true);
            lvEvents.setAdapter(eventAdapter);


            imgDutyStatus = (ImageView) view.findViewById(R.id.imgDutyStatus_portrait);
            ViewTreeObserver vto = imgDutyStatus.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    imgDutyStatus.getViewTreeObserver().removeOnPreDrawListener(this);
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    finalHeight = displayMetrics.heightPixels;
                    finalWidth = displayMetrics.widthPixels;

                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    float density = displayMetrics.density;
                    finalWidth = Math.round(displayMetrics.widthPixels / density);
                    finalHeight = Math.round(displayMetrics.heightPixels / density);
                    //Log.e(TAG, "Height = " + finalHeight + " - Width = " + finalWidth);
                    return false;
                }
            });

            initializeBitmap();
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DetailFragment.class.getName(), "::onCreateView Error:", e.getMessage(), Utility.printStackTrace(e));

        }
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        try {
            Log.d(TAG, "onResume inspect");
//            if (lvEvents != null) {
//                //eventAdapter.changeItems(getListEvents());
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        //eventAdapter = new EventAdapter(Utility.context, getListEvents());
//                        if (selectedDate != null) {
//                            listEvents = getListEvents(Utility.GetString(selectedDate));
//                            eventAdapter.changeItems(listEvents);
//                        }
//                    }
//                });
//            }
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::onResume Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DetailFragment.class.getName(), "::onResume Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;

            rListner = (RefresData) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            mListener = null;
            finalHeight = 0;
            finalWidth = 0;
            canvas = null;
            listEvents.clear();
            eventAdapter.clear();
            lvEvents.setAdapter(null);
            listEvents = null;
            infosDialog = null;
            truckIdDialog = null;
            rListner = null;
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::onDetach Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DetailFragment.class.getName(), "::onDetach Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.butBack:
                    //setResult(Activity.RESULT_CANCELED);
                    mListener.onDetailClosed();
                    //finish();
                    break;
                case R.id.butSave:
                    SharedPreferences.Editor e = (getActivity().getSharedPreferences("HutchGroup", getActivity().getBaseContext().MODE_PRIVATE))
                            .edit();
                    e.putString("shipping_number", edShippingID.getText().toString());
                    e.putString("trailer_number", edTrailerID.getText().toString());
                    //e.putInt("driverid", Utility.onScreenUserId);
                    e.commit();
                    DailyLogDB.DailyLogCreate(driverId, edShippingID.getText().toString(), edTrailerID.getText().toString(), "");
                    mListener.onDetailSaved();
                    //finish();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DetailFragment.class.getName(), "::onClick Error:", e.getMessage(), Utility.printStackTrace(e));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            if (requestCode == 1) {
                if (resultCode == Activity.RESULT_OK) {
                    edShippingID.setText(Utility.ShippingNumber);
                    edTrailerID.setText(Utility.TrailerNumber);
                }
            }
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::onActivityResult Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DetailFragment.class.getName(), "::onActivityResult Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void launchEnterTruckID() {
        try {
            if (truckIdDialog == null) {
                truckIdDialog = new InputTruckIDDialog();
            }
            truckIdDialog.setTrucID(tvTruckTractorID.getText().toString());
            truckIdDialog.mListener = this;

            truckIdDialog.show(getFragmentManager(), "truckid_dialog");
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::launchEnterTruckID Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DetailFragment.class.getName(), "::launchEnterTruckID Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void launchInputDialog() {
        try {
            // if (infosDialog == null) {
            infosDialog = new InputInformationDialog();
            // }
            if (!infosDialog.isVisible()) {
                infosDialog.mListener = this;
                infosDialog.setSelectedDate(Utility.GetString(selectedDate));

                infosDialog.show(getFragmentManager(), "infos_dialog");
            }
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::launchInputDialog Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DetailFragment.class.getName(), "::launchInputDialog Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }


    @Override
    public void onInputFinished() {
        Log.d("Input", "fragment onInputFinished");
        //  infosDialog.mListener = null;
    }

    @Override
    public void onInputSaved(String shippId, String trailerId) {
        Log.d("Input", "fragment onInputSaved");
        edShippingID.setText(shippId);
        edTrailerID.setText(trailerId);

        //DailyLogDB.DailyLogCreate(driverId, shippId, trailerId, "");
        infosDialog.mListener = null;
    }

    @Override
    public void onTruckIDSaved(String truckID) {
        Utility.UnitNo = truckID;
        tvTruckTractorID.setText(truckID);

        CarrierInfoDB.SaveUnitNo();
        truckIdDialog.mListener = null;
    }

    @Override
    public void onTruckIDFinished() {
        truckIdDialog.mListener = null;
    }

    public void refresh() {
        try {
         /*   if (mListener != null) {
                mListener.updateTitle(Utility.GetStringDate(selectedDate));
            }*/
            DailyLogBean dailyLog = DailyLogDB.getDailyLogInfo(driverId, Utility.GetString(selectedDate));
            dailyLogId = dailyLog.get_id();
            todayDistance = dailyLog.getTotalDistance();

            tvRecordDate.setText(Utility.GetStringDate(selectedDate));

            dutyStatusList = EventDB.DutyStatusGetByDate(Utility.onScreenUserId, currentDate);
            DutyStatusGet();


            certifyFG = dailyLog.getCertifyFG();
            if (selectedDate.equals(Utility.dateOnlyGet(Utility.newDate()))) {
                fabUncertify.setClickable(false);
                fabUncertify.setImageResource(R.drawable.ic_certified);
            } else if (certifyFG == 1) {
                fabUncertify.setClickable(false);
                fabUncertify.setImageResource(R.drawable.ic_verified_user_black);
            } else {
                fabUncertify.setClickable(!Utility.InspectorModeFg);
                fabUncertify.setImageResource(R.drawable.ic_fab_uncertified_red);
            }

            if (lvEvents != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        eventAdapter.changeItems(getListEvents(Utility.GetString(selectedDate)));
                    }
                });
            }
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::refresh Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DetailFragment.class.getName(), "::refresh Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void initializeStatus() {
        try {
            statusDT = selectedDate;

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

            DutyStatusGet();
            new ViolationSync(false).execute();

        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::initializeStatus Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DetailFragment.class.getName(), "::initializeStatus Error:", e.getMessage(), Utility.printStackTrace(e));
        }
    }

    private List<EventBean> getListEvents(String date) {

        List<EventBean> dutyStatusList = EventDB.EventGetByLogId(driverId, dailyLogId);

        // if its mid night event then don't show fab button
        if (EventDB.checkDuplicate(date + " 00:00:02", driverId)) {
            fab_mid_night.setVisibility(View.GONE);
        }

        //Log.d(TAG, "duty list size=" + dutyStatusList.size());
        this.DailyLogInfoGet(driverId, date);
        //return dutyStatusList;
        return AddViolationToEvents(dutyStatusList);
    }

    // Deepak Sharma
    // 3 June 2016
    // add violation to events
    private List<EventBean> AddViolationToEvents(List<EventBean> eventList) {
        if (Utility._appSetting.getShowViolation() == 0)
            return eventList;

        ArrayList<ViolationModel> vList = MainActivity.violationList;

        Date currTime = Utility.newDate();
        for (int i = 0; i < vList.size(); i++) {

            Date startTime = vList.get(i).getViolationDate();
            int totalMinutes = (int) (vList.get(i).getViolationDuration() / 60d);
            // Date endTime = Utility.addMinutes(startTime, totalMinutes);

            Date vDate = Utility.dateOnlyGet(startTime);
            boolean isCurrent = vDate.equals(currentDate);
            if (!isCurrent)
                continue;

           /* if (startTime.after(currTime) || endTime.before(currentDate))
                continue;
*/
            EventBean event = new EventBean();
            event.setEventType(-1); //-1 for violation
            event.setEventDateTime(Utility.format(startTime, fullFormat));
            event.setViolation(vList.get(i).getRule());
            event.setViolationTitle(vList.get(i).getTitle());
            event.setViolationExplanation(vList.get(i).getDescription());
            event.setViolationMintes(Utility.getTimeFromMinute(totalMinutes));


            eventList.add(event);
        }


        Collections.sort(eventList, EventBean.dateDesc);
        return eventList;
    }

    private void InvokeRule() {
        try {
            HourOfService.InvokeRule(Utility.newDate(), driverId);
            //AutoHoursCalculate();
        } catch (Exception exe) {
            //Utility.printError(exe.getMessage());
            LogFile.write(DetailFragment.class.getName() + "::InvokeRule Error: " + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TAG, "::InvokeRule Error:", exe.getMessage(), Utility.printStackTrace(exe));
        }
    }

    private void DutyStatusGet() {
        try {
            drawLine(dutyStatusList, currentDate);
            StatusHourGet(dutyStatusList);
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::DutyStatusGet Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TAG, "::DutyStatusGet Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    //Created By: Deepak Sharma
    //Created Date: 24 april 2019
    //Purpose: Draw Violation Area on Graph
    private void drawViolationArea() {

        ArrayList<ViolationModel> vList = MainActivity.violationList;
        int startMinutes = 0;
        Date endTime = null, startTime = null;
        for (int i = 0; i < vList.size(); i++) {

            startTime = vList.get(i).getViolationDate();

            // Purpose -> Draw Violation on selected day
          /*  if (!Utility.dateOnlyGet(startTime).equals(Utility.newDateOnly()))
                continue;*/

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

    //Created By: Deepak Sharma
    //Created Date: 5/27/2016
    //Purpose: get rule when event changed
    private RuleBean eventRuleGet(Date eventTime, ArrayList<RuleBean> ruleList) {
        RuleBean obj = new RuleBean();
        obj.setRuleId(1);
        obj.setRuleStartTime(selectedDate);
        obj.setRuleEndTime(Utility.addDays(selectedDate, 1));
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

    private void drawLine(ArrayList<DutyStatusBean> dutyStatus, Date logDate) {
        //clear bitmap bmp 333
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
        try {
            String logDT = Utility.format(logDate, fullFormat);
            ArrayList<RuleBean> ruleList = DailyLogDB.getRuleByDate(logDT, driverId, dailyLogId);
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
                    //Log.d(TAG, "1- " + i + "," + status + " :start time=" + startMinutes + " - end time=" + endMinutes);
                    drawLine(getX(startMinutes), getY(status), getX(endMinutes), getY(status), ruleId, specialStatus);
                    startTime = rule.getRuleEndTime();
                    ArrayList<RuleBean> ruleEventList = eventRuleListGet(startTime, endTime, ruleList);
                    for (RuleBean ruleBean : ruleEventList) {
                        if (ruleBean.getRuleStartTime().equals(rule.getRuleStartTime()))
                            continue;
                        startMinutes = (int) (startTime.getTime() - logDate.getTime()) / (1000 * 60);
                        endMinutes = (int) ((ruleBean.getRuleEndTime().before(endTime) ? ruleBean.getRuleEndTime() : endTime).getTime() - logDate.getTime()) / (1000 * 60);
                        ruleId = ruleBean.getRuleId();
                        //Log.d(TAG, "1- " + i + "," + status + " X=" + getX(endMinutes) + " - Y1=" + getY(status) + "/Y2=" + getY(status));
                        drawLine(getX(startMinutes), getY(status), getX(endMinutes), getY(status), ruleId, specialStatus);
                        startTime = ruleBean.getRuleEndTime();
                    }
                } else {

                    startMinutes = (int) (startTime.getTime() - logDate.getTime()) / (1000 * 60);
                    endMinutes = (int) (endTime.getTime() - logDate.getTime()) / (1000 * 60);
                    if (i == 0 && startMinutes > 0) {
                        startMinutes = 0;
                    }

                   /* if (status == 1) {
                        drawRect(getX(startMinutes), getRectY(1), getX(endMinutes), getRectY(4), ruleId);
                    }*/
                    Log.d(TAG, i + "," + status + " :start time=" + startMinutes + " - end time=" + endMinutes);
                    drawLine(getX(startMinutes), getY(status), getX(endMinutes), getY(status), ruleId, specialStatus);

                }
                if (i < dutyStatus.size() - 1) {
                    item = dutyStatus.get(i + 1);
                    Log.d(TAG, i + "," + status + " X=" + getX(endMinutes) + " - Y1=" + getY(status) + "/Y2=" + getY(item.getStatus()));
                    drawLine(getX(endMinutes), getY(status), getX(endMinutes), getY(item.getStatus()), ruleId, item.getSpecialStatus());
                }
            }

            if (dutyStatus.size() == 0) {
                if (ruleList.size() > 0) {
                    ruleId = ruleList.get(0).getRuleId();

                }
                endMinutes = 1439;
                drawLine(getX(0), getY(1), getX(endMinutes), getY(1), ruleId, 0);
            } else {
                int status = dutyStatus.get(dutyStatus.size() - 1).getStatus();
                if (Utility._appSetting.getGraphLine() == 1) {
                    drawLine(getX(endMinutes), getY(status), getX(1439), getY(status), ruleId, 0, true);
                }
            }

            imgDutyStatus.setImageDrawable(null);
            imgDutyStatus.setImageBitmap(bmp);
        } catch (Exception exe) {
            Log.d(TAG, "drawLine got exception");
            LogFile.write(ELogFragment.class.getName() + "::drawLine Error:" + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TAG, "::drawLine Error:", exe.getMessage(), Utility.printStackTrace(exe));

        }
    }

    DashPathEffect effects = new DashPathEffect(new float[]{4, 2, 4, 2}, 0);

    private void drawLine(float x, float y, float xend, float yend, int ruleId, int specialStatus, boolean isDotted) {
        try {
            Paint p = new Paint();
            int color;
            if (ruleId <= 2) {
                color = Utility._appSetting.getColorLineCanada();
            } else {
                color = Utility._appSetting.getColorLineUS();
            }
            color = color == 0 ? Color.BLUE : color;
            p.setColor(color);
            int width = 4;

            if (specialStatus > 0) {
                width = 10;
            }
            p.setStrokeWidth(width);
            if (isDotted) {
                p.setPathEffect(effects);
            }
          /*  if (personalUseFg == 1) {
                p.setPathEffect(effects);
            }*/
            canvas.drawLine(x, y, xend, yend, p);
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::drawLine1 Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void drawLine(float x, float y, float xend, float yend, int ruleId, int specialStatus) {
        try {
            Paint p = new Paint();
            int color;
            if (ruleId <= 2) {
                color = Utility._appSetting.getColorLineCanada();
            } else {
                color = Utility._appSetting.getColorLineUS();
            }
            color = color == 0 ? Color.BLUE : color;
            p.setColor(color);
            int width = 4;

            if (specialStatus > 0) {
                width = 10;
            }
            p.setStrokeWidth(width);
            canvas.drawLine(x, y, xend, yend, p);
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::drawLine Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TAG, "::drawLine Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void drawRect(float x, float y, float xend, float yend) {
        try {
            Paint p = new Paint();

            p.setColor(getResources().getColor(R.color.red15));
            //p.setStrokeWidth(1);
            canvas.drawRect(x, y, xend, yend, p);
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::drawRect Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TAG, "::drawRect Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }


    private int getX(int minutes) {
        float boxWidth = (float) imageWidth;
        int x = (int) (boxWidth / 1440f * minutes);
        return x;
    }

    // created By: Sahil Bansal
    // Created Date: 25 November 2019
    // Purpose: get y position by duty status
    private int getY(int status) {

        int headerHeight = Math.round(imageHeight / 5.27f);
        float boxHeight = (float) (imageHeight - headerHeight) / 4;

        int y = (int) ((status - 1) * boxHeight + boxHeight / 2 + headerHeight);

        return y;
    }

/*    private int getY(int status) {
        float boxHeight = (float) (imageHeight - 64.0) / 4;
        if (!Utility.isLargeScreen(getContext().getApplicationContext())) {
            boxHeight = (float) (imageHeight - 22.0) / 4;
        }

        int y = (int) ((status - 1) * boxHeight + boxHeight / 2 + 64.0);

        if (!Utility.isLargeScreen(getContext().getApplicationContext())) {
            y = (int) ((status - 1) * boxHeight + boxHeight / 2 + 22.0);
        } else {
            if ((Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation)) {
                boxHeight = (float) (imageHeight - 52.0) / 4;
                y = (int) ((status - 1) * boxHeight + boxHeight / 2 + 52.0);
            } else {
                y = (int) ((status - 1) * boxHeight + boxHeight / 2 + 64.0);
            }
        }

        return y;
    }*/

    private int getRectY(int status) {
        float boxHeight = (float) (imageHeight - 54.0) / 4;
        if (!Utility.isLargeScreen(getContext().getApplicationContext())) {
            boxHeight = (float) (imageHeight - 22.0) / 4;
        }

        //int y = (int) ((status - 1) * boxHeight + boxHeight / 2 + 64.0);
        int y = 0;
        if (!Utility.isLargeScreen(getContext().getApplicationContext())) {
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
                    y = 54;
                }
            }

        }

        return y;
    }

    private void StatusHourGet(ArrayList<DutyStatusBean> dutyStatus) {
        try {
            int offDuty = 0, sleeper = 0, driving = 0, onDuty = 0;
            boolean firstStatus = true;
            for (DutyStatusBean bean : dutyStatus) {
                Date startTime = Utility.parse(bean.getStartTime());
                // startTime = Utility.addSeconds(startTime, -startTime.getSeconds());

                Date endTime = Utility.parse(bean.getEndTime());
                //    endTime = Utility.addSeconds(endTime, -endTime.getSeconds());

                int totalHours = (int) Math.round((endTime.getTime() - startTime.getTime()) / (1000));

                if (Utility._appSetting.getGraphLine() == 1) {
                    if (firstStatus) {
                        firstStatus = false;
                        if (startTime.after(currentDate)) {
                            startTime = currentDate;

                            totalHours = (int) Math.round((endTime.getTime() - startTime.getTime()) / (1000));
                            bean.setTotalMinutes(totalHours);
                        }
                    }

                    if (endTime.after(Utility.newDate())) {

                        totalHours = (int) Math.round(((Utility.newDate()).getTime() - startTime.getTime()) / (1000));
                        bean.setTotalMinutes(totalHours);
                    }
                }

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
            tvOffDutyTime.setText(Utility.getTimeFromSecondsInMin(offDuty));
            tvSleeperTime.setText(Utility.getTimeFromSecondsInMin(sleeper));
            tvDrivingTime.setText(Utility.getTimeFromSecondsInMin(driving));
            tvOnDutyTime.setText(Utility.getTimeFromSecondsInMin(onDuty));
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::StatusHourGet Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DetailFragment.class.getName(), "::StatusHourGet Error:", e.getMessage(), Utility.printStackTrace(e));
        }
    }

    public static ArrayList<DutyStatusBean> DutyStatusGet(Date date, ArrayList<DutyStatusBean> list) {

        ArrayList<DutyStatusBean> data = new ArrayList<>();
        try {
            Date nextDay = Utility.addDays(date, 1);

            for (int i = 0; i < list.size(); i++) {
                Date startDate = Utility.parse(list.get(i).getStartTime());
                Date endDate = Utility.parse(list.get(i).getEndTime());
                //Log.d(TAG, "Duty Status= " + list.get(i).getStatus() + " - Start Time=" + list.get(i).getStartTime() + " / End Time=" + list.get(i).getEndTime());
                int status = list.get(i).getStatus();
                int personalUseFg = list.get(i).getPersonalUse();
                startDate = startDate.before(date) ? date : startDate;
                endDate = endDate.after(nextDay) ? nextDay : endDate;
                if ((startDate.after(date) || endDate.after(date)) && startDate.before(nextDay)) {
                    int totalMinutes = (int) Math.round((endDate.getTime() - startDate.getTime()) / (1000 * 60.0));

                    DutyStatusBean bean = new DutyStatusBean();
                    bean.setStartTime(Utility.format(startDate, fullFormat));
                    bean.setEndTime(Utility.format(endDate, fullFormat));
                    bean.setStatus(status);
                    bean.setTotalMinutes(totalMinutes);
                    bean.setPersonalUse(personalUseFg);
                    data.add(bean);
                }
            }

        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::DutyStatusGet Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DetailFragment.class.getName(), "::DutyStatusGet Error:", e.getMessage(), Utility.printStackTrace(e));
        }
        Collections.sort(data, DutyStatusBean.dateAsc);
        return data;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void onDetailClosed();

        void onDetailSaved();

        void updateTitle(String selectedDate);

        void setCertify(int certifyFg);

        void setTitle(String title);

        void showLoaderAnimation(boolean isShown);
    }

    //Created By: Deepak Sharma
    //Created Date: 12 March 2016
    //Purpose: Total distance of driver of day
    private void DailyLogInfoGet(int driverId, String date) {
        try {
            Log.d(TAG, "DailyLogInfoGet " + date);
            String currentDate = Utility.getCurrentDate();

            //if (canadaFg || currentDate.equals(date)) {
            todayDistance = EventDB.CalculateDistanceByLogId(dailyLogId);
            //}

            List<TripInfoBean> tripInfo = EventDB.TripInfoGet(dailyLogId, date);
            String unitNos = "", VINs = "", startEndOR = "", startEndEH = "", odoUnit = "Kms";
            for (int i = 0; i < tripInfo.size(); i++) {
                TripInfoBean bean = tripInfo.get(i);
                unitNos += bean.getUnitNo() + " (" + bean.getPlateNo() + ")";
                VINs += bean.getVIN();

                startEndOR += bean.getStartOdometerReading() + " - " + bean.getEndOdometerReading();
                startEndEH += bean.getStartEngineHour() + " - " + bean.getEndEngineHour();
                odoUnit = bean.getOdoUnit();
                if (i < tripInfo.size() - 1) {
                    unitNos += ", \n";
                    VINs += ", \n";
                    startEndOR += " \n";
                    startEndEH += " \n";
                }
            }
            if (tripInfo.size() > 0) {
                tvStartEndOdometer.setText(startEndOR + " " + odoUnit);
                tvStartEndEngineHours.setText(startEndEH);
            } else {

            }

            String unit = "Kms";
            if (Utility._appSetting.getUnit() == 2) {
                todayDistance = Double.valueOf(todayDistance * .62137d).intValue();
                unit = "Miles";
            }

            // tvMilesToday.setText(totalDistance + " " + unit);
            tvMilesToday.setText(todayDistance + " " + unit);

            String CoDrivers = DailyLogDB.getCoDriver(driverId, date);

            if (CoDrivers.contains("#")) {
                String arrCD[] = CoDrivers.split("#");

                tvCoDriverID.setText(arrCD[0]);
                tvCoDriverName.setText(arrCD[1]);
            }
            if (!unitNos.isEmpty()) {
                tvTruckTractorID.setText(unitNos);
            }

            if (!VINs.isEmpty()) {
                tvTruckTractorVIN.setText(VINs);
            }

            DailyLogBean dailyBean = DailyLogDB.getDailyLogInfo(driverId, date);
            edShippingID.setText(dailyBean.getShippingId());
            edTrailerID.setText(dailyBean.getTrailerId());
        } catch (Exception exe) {
            Log.d(DetailFragment.class.getName(), "::DailyLogInfoGet Error: " + exe.getMessage());
            LogFile.write(DetailFragment.class.getName() + "::DailyLogInfoGet Error: " + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public void certifyLogBook() {
        try {
            final AlertDialog ad = new AlertDialog.Builder(Utility.context)
                    .create();
            ad.setCancelable(true);
            ad.setCanceledOnTouchOutside(false);
            ad.setTitle(getString(R.string.certify_log_title));
            ad.setIcon(R.drawable.ic_launcher);
            ad.setMessage(getString(R.string.certify_log_message));
            ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.certify),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            DailyLogDB.certifyLogBook(driverId, dailyLogId + "");
                            listEvents = getListEvents(Utility.GetString(selectedDate));
                            eventAdapter.changeItems(listEvents);
                            fabUncertify.setClickable(false);
                            fabUncertify.setImageResource(R.drawable.ic_verified_user_black);

                            if (mListener != null)
                                mListener.setCertify(1);
                            //  tvCertify.setText("Certify");
                        }
                    });
            ad.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.not_ready),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            ad.cancel();
                        }
                    });
            ad.show();
        } catch (Exception e) {
            LogFile.write(DailyLogDB.class.getName() + "::certifyLogBook Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            LogDB.writeLogs(TAG, "::certifyLogBook Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }


    private class ViolationSync extends AsyncTask<Void, Void, Boolean> {
        boolean dutyStatusRefresh;

        public ViolationSync(boolean data) {
            dutyStatusRefresh = data;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
           /* if (dutyStatusRefresh)
                Utility.dutyStatusList = HourOfServiceDB.DutyStatusGet15Days(currentDate, Utility.onScreenUserId + "", false);
            HourOfService.listDutyStatus = Utility.dutyStatusList;*/

            // process violation if any duty status created from this activity
            MainActivity.ProcessViolation(dutyStatusRefresh);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            /*InvokeRule();*/

            if (Utility._appSetting.getViolationOnGrid() == 1) {
                if (getContext() != null)
                    drawViolationArea();
            }
        }
    }


    @Override
    public void onCallPost() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                postData();
            }
        });
    }

    @Override
    public void onCallSync() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callSync();
            }
        });
    }

    @Override
    public void onRefresh() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rListner.onRefresh();
            }
        });
    }

    private void callSync() {
// do not try post if device is offline
        if (!Utility.isInternetOn()) {
            return;
        }

        mListener.showLoaderAnimation(true);
        Thread thSyncEvent = new Thread(new Runnable() {
            @Override
            public void run() {
                String date = Utility.format(selectedDate, CustomDateFormat.dt1);
                boolean status = GetCall.DailyLogEventGetByDateSync(date);
                if (status) {
                    if (getActivity() != null)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refresh();
                                mListener.showLoaderAnimation(false);
                            }
                        });
                }
            }
        });
        thSyncEvent.setName("thSyncEvent");
        thSyncEvent.start();
    }

    private void postData() {
        // do not try post if device is offline
        if (!Utility.isInternetOn()) {
            return;
        }

        // First Check edit Request is pending
        mListener.showLoaderAnimation(true);
        new ModifiedSyncData(postTaskListener).execute();
    }


    private RefresData rListner;

    public interface RefresData {
        void onRefresh();
    }
}
