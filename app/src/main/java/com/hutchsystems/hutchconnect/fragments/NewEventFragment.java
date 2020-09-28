package com.hutchsystems.hutchconnect.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.DiagnosticIndicatorBean;
import com.hutchsystems.hutchconnect.beans.EventBean;
import com.hutchsystems.hutchconnect.beans.GPSData;
import com.hutchsystems.hutchconnect.beans.ViolationBean;
import com.hutchsystems.hutchconnect.bll.HourOfService;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.ConstantFlag;
import com.hutchsystems.hutchconnect.common.CustomDateFormat;
import com.hutchsystems.hutchconnect.common.DiagnosticMalfunction;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.EventDB;
import com.hutchsystems.hutchconnect.db.HourOfServiceDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.LoginDB;
import com.hutchsystems.hutchconnect.tasks.GeocodeTask;
import com.hutchsystems.hutchconnect.tasks.NearestDistanceTask;
import com.hutchsystems.hoursofservice.model.DutyStatus;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class NewEventFragment extends Fragment implements View.OnClickListener, DutyStatusChangeDialog.DutyStatusChangeDialogInterface, CompoundButton.OnCheckedChangeListener {
    private final int OFF_DUTY = 1;
    private final int SLEEPER = 2;
    private final int DRIVING = 3;
    private final int ON_DUTY = 4;
    private final int PERSONAL_USE = 5;
    private final int YARD_MOVE = 6;
    //TextView tvEventType;
    TextView tvEventTime;
    TextView tvOdometer, tvDateValue, tvEventDateLabel;
    TextView tvEngineHours;
    TextView tvOrigin;
    TextView tvLocationLabel;
    EditText edLocationDescription;
    EditText edComments, etPassword;
    Switch swTransfer;
    LinearLayout layoutTransfer;
    ScrollView sv_grid;
    boolean bDialogShowing;
    //Button butBack;
    //static Button butSave;
    ImageButton fabSave;
    String driverName = "";
    int currentStatus = 1, eventId = 0;
    public static boolean CurrentEventFg = false;
    EventBean eventData;
    String shortStatus;
    int driverId;
    int logId;
    String eventDescription = "";

    int eventType;
    int eventCode;
    int eventRecordOrigin;
    int eventRecordStatus;
    String textToSpeech = "";
    boolean isEditEvent;
    String selectedEventDateTime;
    ColorStateList dutyStatusColorStateList;
    DutyStatusChangeDialog dutyDialog;
    Button butOffDuty;
    Button butSleeper;
    Button butDriving;
    /* Button butDisableDriving;*/
    Button butOnDuty;
    Button butPersonalUse;
    Button butYardMove;
    private OnFragmentInteractionListener mListener;

    int second = 0;
    private Spinner spinnerRule;
    private Switch switchShowAlertSplitSleep, switchReduceTime, switchWaitingTime;
    ConstraintLayout splitSleeprow;
    private ConstraintLayout layoutReduceTime, layoutWaitingTime;
    private String duration;

    ConstraintLayout layoutPersonalUse, layoutYardMove;

    public NewEventFragment() {
    }

    private void initialize(View view) {
        try {

            driverId = Utility.onScreenUserId;
            driverName = Utility.user1.isOnScreenFg() ? Utility.user1.getFirstName() : Utility.user2.getFirstName();
            isEditEvent = false;
            etPassword = (EditText) view.findViewById(R.id.etPassword);
            etPassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //butSave.setEnabled(true);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            swTransfer = (Switch) view.findViewById(R.id.swTransfer);
            swTransfer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        etPassword.setVisibility(View.VISIBLE);
                        sv_grid.setVisibility(View.GONE);

                    } else {
                        etPassword.setVisibility(View.GONE);
                        sv_grid.setVisibility(View.VISIBLE);
                    }
                }
            });
            layoutTransfer = (LinearLayout) view.findViewById(R.id.layoutTransfer);
            sv_grid = (ScrollView) view.findViewById(R.id.sv_grid);

            //   tvEventType = (TextView) view.findViewById(R.id.tvEventType);


            butOffDuty = (Button) view.findViewById(R.id.butOffDuty);

            butSleeper = (Button) view.findViewById(R.id.butSleeper);

            butDriving = (Button) view.findViewById(R.id.butDriving);

            /*butDisableDriving = (Button) view.findViewById(R.id.butDisableDriving);*/
            butOnDuty = (Button) view.findViewById(R.id.butOnDuty);

            butPersonalUse = (Button) view.findViewById(R.id.butPersonalUse);

            butYardMove = (Button) view.findViewById(R.id.butYardMove);


            tvDateValue = (TextView) view.findViewById(R.id.tvDateValue);
            tvEventDateLabel = (TextView) view.findViewById(R.id.tvEventDateLabel);
            tvEventTime = (TextView) view.findViewById(R.id.tvTimeValue);
            tvOdometer = (TextView) view.findViewById(R.id.tvOdometerValue);
            tvEngineHours = (TextView) view.findViewById(R.id.tvEngineHoursValue);
            tvOrigin = (TextView) view.findViewById(R.id.tvOriginValue);

            layoutPersonalUse = (ConstraintLayout) view.findViewById(R.id.lPersonalUse);
            layoutYardMove = (ConstraintLayout) view.findViewById(R.id.lYardMove);

            edLocationDescription = (EditText) view.findViewById(R.id.edLocationDescription);

            tvLocationLabel = (TextView) view.findViewById(R.id.tvDescriptionLabel);
            //always show these for create new event
            //create new event is always manually
            edLocationDescription.setVisibility(View.VISIBLE);
            tvLocationLabel.setVisibility(View.VISIBLE);

            edComments = (EditText) view.findViewById(R.id.edComments);

            tvEventTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimePicker();
                }
            });

            //butBack = (Button) view.findViewById(R.id.butEventBack);
            //butBack.setOnClickListener(this);
            //butSave = (Button) view.findViewById(R.id.butEventSave);
            //butSave.setOnClickListener(this);
            fabSave = (ImageButton) view.findViewById(R.id.fabSave);
            fabSave.setOnClickListener(this);

            double odometerReading = Double.valueOf(CanMessages.OdometerReading); // odometer from can bus is in km

            String unit = "Kms";
            if (Utility._appSetting.getUnit() == 2) {
                odometerReading = odometerReading * .62137d;
                unit = "Miles";
            }
            tvEventTime.setText(Utility.getCurrentTime());
            tvOdometer.setText(String.format("%.0f", odometerReading));
            tvEngineHours.setText(CanMessages.EngineHours);

            spinnerRule = (Spinner) view.findViewById(R.id.spinnerRule);
            spinnerRule.setEnabled(false);

            switchShowAlertSplitSleep = (Switch) view.findViewById(R.id.switchShowAlertSplitSleep);
            switchReduceTime = (Switch) view.findViewById(R.id.switchReduceTime);


            switchWaitingTime = (Switch) view.findViewById(R.id.switchWaitingTime);

            splitSleeprow = (ConstraintLayout) view.findViewById(R.id.splitSleeprow);
            layoutReduceTime = (ConstraintLayout) view.findViewById(R.id.layoutReduceTime);
            layoutReduceTime.setVisibility(View.GONE);

            layoutWaitingTime = (ConstraintLayout) view.findViewById(R.id.layoutWaitingTime);
            layoutWaitingTime.setVisibility(View.GONE);

            populateRule();

            updateEventType();

            if (eventData != null) {
                logId = eventData.getDailyLogId();
                eventId = eventData.get_id();
                duration = eventData.getDuration();
                //tvOrigin;
                //tvEngineHours.setText(eventData.getEngineHour());
                tvEventTime.setText(Utility.getTime(eventData.getEventDateTime()));
                //tvOdometer.setText(eventData.getOdometerReading());
                selectedEventDateTime = eventData.getEventDateTime();
                isEditEvent = true;
                String statusText = "";
                switch (eventData.getEventType()) {
                    case 1:
                        if (eventData.getEventCode() == 1) {
                            statusText = getResources().getString(R.string.off_duty_long);
                            currentStatus = 1;
                        } else if (eventData.getEventCode() == 2) {
                            statusText = getResources().getString(R.string.sleeper_long);
                            currentStatus = 2;
                        } else if (eventData.getEventCode() == 3) {
                            statusText = getResources().getString(R.string.driving_long);
                            currentStatus = 3;
                        } else {
                            statusText = getResources().getString(R.string.on_duty_long);
                            currentStatus = 4;
                        }
                        break;
                    case 2:
                        statusText = getResources().getString(R.string.driving_long);
                        currentStatus = 3;
                        break;
                    case 3:
                        if (eventData.getEventCode() == 1) {
                            statusText = getResources().getString(R.string.personal_use_long);
                            currentStatus = 5;
                        } else if (eventData.getEventCode() == 2) {
                            statusText = getResources().getString(R.string.yard_move_long);
                            currentStatus = 6;
                        }
                        break;
                }

                // we do not allow changing time of driving from android
                if (currentStatus == 3 && Utility._appSetting.getdEditFg() == 0) {
                    tvEventTime.setEnabled(ConstantFlag.Flag_DRIVING_EDIT);
                }

                if (currentStatus == 3 && Utility.user2.getAccountId() > 0 && eventData.getCoDriverId() > 0) {
                    layoutTransfer.setVisibility(View.VISIBLE);
                } else {
                    layoutTransfer.setVisibility(View.GONE);

                }

                if (Utility.user1.isOnScreenFg()) {
                    if (Utility.user1.getSpecialCategory().equals("1")) {
                        layoutYardMove.setVisibility(View.INVISIBLE);
                    } else if (Utility.user1.getSpecialCategory().equals("2")) {
                        layoutPersonalUse.setVisibility(View.INVISIBLE);
                    } else if (Utility.user1.getSpecialCategory().equals("0")) {
                        layoutYardMove.setVisibility(View.INVISIBLE);
                        layoutPersonalUse.setVisibility(View.INVISIBLE);
                    }
                } else if (Utility.user2.isOnScreenFg()) {
                    if (Utility.user2.getSpecialCategory().equals("1")) {
                        layoutYardMove.setVisibility(View.INVISIBLE);
                    } else if (Utility.user2.getSpecialCategory().equals("2")) {
                        layoutPersonalUse.setVisibility(View.INVISIBLE);
                    } else if (Utility.user2.getSpecialCategory().equals("0")) {
                        layoutYardMove.setVisibility(View.INVISIBLE);
                        layoutPersonalUse.setVisibility(View.INVISIBLE);
                    }
                }
                clearDutyStatus();
                switch (currentStatus) {
                    case 1:
                        butOffDuty.setSelected(true);
                        break;
                    case 2:
                        butSleeper.setSelected(true);
                        break;
                    case 3:
                        butDriving.setSelected(true);
                        break;
                    case 4:
                        butOnDuty.setSelected(true);
                        break;
                    case 5:
                        butPersonalUse.setSelected(true);
                        break;
                    case 6:
                        butYardMove.setSelected(true);
                        break;
                }
                //  tvEventType.setText(statusText);
                if (eventData.getEventRecordOrigin() == 1)
                    tvOrigin.setText(getResources().getString(R.string.event_origin_automatically));
                else
                    tvOrigin.setText(getResources().getString(R.string.event_origin_edited));

                String location = eventData.getLocationDescription();
                edLocationDescription.setText(location);

                if (location != null && !location.isEmpty())
                    edLocationDescription.setEnabled(false);

                odometerReading = Double.valueOf(eventData.getOdometerReading()); // odometer from can bus is in km

                unit = "Kms";
                if (Utility._appSetting.getUnit() == 2) {
                    odometerReading = odometerReading * .62137d;
                    unit = "Miles";
                }

                tvOdometer.setText(String.format("%.0f", odometerReading));
                tvEngineHours.setText(eventData.getEngineHour());
                spinnerRule.setSelection(eventData.getRuleId() - 1);

                // Show switch only in case of offduty and sleeperbirth
                if (currentStatus <= 2) {
                    splitSleeprow.setVisibility(View.VISIBLE);
                    switchShowAlertSplitSleep.setChecked(eventData.getSplitSleep() == 1);
                    switchShowAlertSplitSleep.setOnCheckedChangeListener(this);

                    if (eventData.getRuleId() == 5) {
                        // show reduce time layout
                        layoutReduceTime.setVisibility(View.VISIBLE);
                        switchReduceTime.setChecked(eventData.isReduceFg());
                        switchReduceTime.setOnCheckedChangeListener(this);
                    } else if (eventData.getRuleId() == 7) {
                        // show reduce time layout
                        layoutWaitingTime.setVisibility(View.VISIBLE);
                        switchWaitingTime.setChecked(eventData.isWaitingFg());
                        switchWaitingTime.setOnCheckedChangeListener(this);

                    }
                } else {
                    layoutReduceTime.setVisibility(View.GONE);
                    splitSleeprow.setVisibility(View.GONE);
                    layoutReduceTime.setVisibility(View.GONE);

                    layoutWaitingTime.setVisibility(View.GONE);

                }

            } else {
                initializeDatePicker();
                callGeocodeTask();
               /* if (!ConstantFlag.ELDFg) {
                    edLocationDescription.setVisibility(View.VISIBLE);
                    tvLocationLabel.setVisibility(View.VISIBLE);
                }*/

                tvDateValue.setVisibility(View.VISIBLE);
                tvEventDateLabel.setVisibility(View.VISIBLE);
                selectedEventDateTime = Utility.getCurrentDateTime();
                tvDateValue.setText(Utility.getDate(selectedEventDateTime));

                logId = DailyLogDB.getDailyLog(driverId, Utility.getDate(selectedEventDateTime));
            }
            if (currentStatus != 3 || ConstantFlag.Flag_DRIVING_EDIT || Utility._appSetting.getdEditFg() == 1) {
                butSleeper.setOnClickListener(this);
                butOffDuty.setOnClickListener(this);
                butDriving.setOnClickListener(this);
                butOnDuty.setOnClickListener(this);
                butPersonalUse.setOnClickListener(this);
                butYardMove.setOnClickListener(this);
            }


            // driving can change duty status from driving to personal use
            if (Utility._appSetting.getdEditFg() == 0 && currentStatus == 3)
            {
                butDriving.setOnClickListener(this);
                butPersonalUse.setOnClickListener(this);

            }
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(NewEventFragment.class.getName(), "initialize", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void clearDutyStatus() {
        try {
            butOffDuty.setSelected(false);
            butSleeper.setSelected(false);
            butDriving.setSelected(false);
            butOnDuty.setSelected(false);
            butPersonalUse.setSelected(false);
            butYardMove.setSelected(false);

            butOffDuty.setTextColor(dutyStatusColorStateList);
            butSleeper.setTextColor(dutyStatusColorStateList);
            butDriving.setTextColor(dutyStatusColorStateList);
            butOnDuty.setTextColor(dutyStatusColorStateList);
            butPersonalUse.setTextColor(dutyStatusColorStateList);
            butYardMove.setTextColor(dutyStatusColorStateList);
        } catch (Exception e) {
            LogFile.write(DutyStatusChangeDialog.class.getName() + "::clearDutyStatus Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DutyStatusChangeDialog.class.getName(), "::clearDutyStatus Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void populateRule() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Utility.context, R.layout.spinner_rule_item, getResources().getStringArray(R.array.rule_list));
        spinnerRule.setAdapter(adapter);
    }

    DatePickerDialog.OnDateSetListener datepicker = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {


            Calendar myCalendar = Calendar.getInstance();
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            Date todayDate = Utility.newDateOnly();
            Date prevDate = Utility.getDate(todayDate, -15);
            if (prevDate.after(myCalendar.getTime())) {
                Utility.showMsg("Date before 15 days is not allowed!");
            } else {
                String date = Utility.format(myCalendar.getTime(), CustomDateFormat.dt1);
                selectedEventDateTime = date;
                String logDate = Utility.getDate(date);
                tvDateValue.setText(logDate);
                logId = DailyLogDB.getDailyLog(driverId, Utility.getDate(selectedEventDateTime));

                if (logId == 0) {
                    logId = DailyLogDB.DailyLogCreateByDate(driverId, logDate, "", "", "");
                }
            }
        }

    };

    private void initializeDatePicker() {

        tvDateValue.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    Date date = Utility.parse(tvDateValue.getText().toString() + " 00:00:00");
                    Calendar myCalendar = Calendar.getInstance();
                    myCalendar.setTime(date);
                    DatePickerDialog dialog = new DatePickerDialog(getContext(), datepicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH));

                    dialog.show();
                } catch (Exception exe) {
                    String message = exe.getMessage();
                }
            }
        });
    }

    public static NewEventFragment newInstance() {
        return new NewEventFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_new_event, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dutyStatusColorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_selected}, //selected
                        new int[]{android.R.attr.state_enabled}, //un-selected
                },
                new int[]{
                        ContextCompat.getColor(getActivity().getBaseContext(), R.color.white), //1
                        ContextCompat.getColor(getActivity().getBaseContext(), R.color.sixsix) //2
                }
        );


        //Intent intent = getActivity().getIntent();
        currentStatus = getArguments().getInt("current_status", 1);
        eventData = null;
        if (getArguments().getBoolean("edit_event", false)) {
            eventData = (EventBean) getArguments().getSerializable("selected_event");
        }
        eventRecordOrigin = 2; //change by driver
        eventRecordStatus = 1; //active
        initialize(view);
        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
      /*  try {

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.activity_new_event, null);

            ViewGroup viewGroup = (ViewGroup) getView();

            viewGroup.removeAllViews();
            viewGroup.addView(view);

            initialize(view);
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::onConfigurationChanged Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }*/
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.butOffDuty:
                    if (!butOffDuty.isSelected()) {
                        clearDutyStatus();
                        butOffDuty.setSelected(true);

                        setupKeyboard();
                        currentStatus = OFF_DUTY;
                        eventDescription = getResources().getString(R.string.duty_status_changed_to_off_duty);
                        textToSpeech = driverName + " " + getResources().getString(R.string.texttospeech_duty_status_changed_to_off_duty);
                        eventType = 1;
                        eventCode = 1;
                        shortStatus = getResources().getString(R.string.off_duty);
                    }
                    break;
                case R.id.butSleeper:
                    if (!butSleeper.isSelected()) {
                        clearDutyStatus();
                        butSleeper.setSelected(true);

                        setupKeyboard();
                        currentStatus = SLEEPER;
                        eventDescription = getResources().getString(R.string.duty_status_changed_to_sleeper_berth);
                        textToSpeech = driverName + " " + getResources().getString(R.string.texttospeech_duty_status_changed_to_sleeper_berth);
                        eventType = 1;
                        eventCode = 2;
                        shortStatus = getResources().getString(R.string.sleeper);
                    }
                    break;
                case R.id.butDriving:
                    if (!butDriving.isSelected()) {
                        if ((Utility.user1.isOnScreenFg() && !Utility.user1.isActive()) || (Utility.user2.isOnScreenFg() && !Utility.user2.isActive())) {
                            showWarningMessage(getString(R.string.change_duty_status_alert));
                            break;
                        }

                        clearDutyStatus();
                        butDriving.setSelected(true);

                        setupKeyboard();
                        currentStatus = DRIVING;
                        eventDescription = getResources().getString(R.string.duty_status_changed_to_driving);
                        textToSpeech = driverName + " " + getResources().getString(R.string.texttospeech_duty_status_changed_to_driving);
                        eventType = 1;
                        eventCode = 3;
                        shortStatus = getResources().getString(R.string.driving);
                    }
                    break;
                case R.id.butOnDuty:
                    if (!butOnDuty.isSelected()) {
                        clearDutyStatus();
                        butOnDuty.setSelected(true);

                        setupKeyboard();
                        currentStatus = ON_DUTY;
                        eventDescription = getResources().getString(R.string.duty_status_changed_to_on_duty);
                        textToSpeech = driverName + " " + getResources().getString(R.string.texttospeech_duty_status_changed_to_on_duty);
                        eventType = 1;
                        eventCode = 4;
                        shortStatus = getResources().getString(R.string.on_duty);
                    }
                    break;
                case R.id.butPersonalUse:
                    if (!butPersonalUse.isSelected()) {
                        if ((Utility.user1.isOnScreenFg() && !Utility.user1.isActive()) || (Utility.user2.isOnScreenFg() && !Utility.user2.isActive())) {
                            showWarningMessage(getString(R.string.change_dutystatus_personal_user));
                            break;
                        }

                        clearDutyStatus();
                        butPersonalUse.setSelected(true);

                        setupKeyboard();
                        currentStatus = PERSONAL_USE;
                        eventDescription = getResources().getString(R.string.duty_status_changed_to_personal_use);
                        textToSpeech = driverName + " " + getResources().getString(R.string.texttospeech_duty_status_changed_to_personal_use);
                        eventType = 3;
                        eventCode = 1;
                        shortStatus = getResources().getString(R.string.personal_use);
                    }
                    break;
                case R.id.butYardMove:
                    if (!butYardMove.isSelected()) {
                        if ((Utility.user1.isOnScreenFg() && !Utility.user1.isActive()) || (Utility.user2.isOnScreenFg() && !Utility.user2.isActive())) {
                            showWarningMessage(getString(R.string.change_dutystatus_yardmove));
                            break;
                        }

                        clearDutyStatus();
                        butYardMove.setSelected(true);

                        setupKeyboard();
                        currentStatus = YARD_MOVE;
                        eventDescription = getResources().getString(R.string.duty_status_changed_to_yard_move);
                        textToSpeech = driverName + " " + getResources().getString(R.string.texttospeech_duty_status_changed_to_yard_move);
                        eventType = 3;
                        eventCode = 2;
                        shortStatus = getResources().getString(R.string.yard_move);
                    }
                    break;

         /*       case R.id.tvEventType:
                    //call activity Duty Status Change
                    launchDutyStatusChange();
                    break;*/
                case R.id.butEventBack:
                    Utility.hideKeyboard(getActivity(), view);
                    if (isEditEvent) {
                        mListener.onEditEventFinished();
                    } else {
                        mListener.onNewEventFinished();
                    }

                    break;

                case R.id.butEventSave:
                case R.id.fabSave:
                    Utility.hideKeyboard(getActivity(), view);
                    if (swTransfer.isChecked()) {
                        String password = etPassword.getText().toString();
                        if (password.isEmpty()) {
                            Utility.showAlertMsg(getString(R.string.password_alert));
                            break;
                        } else {
                            if (!LoginDB.authCoDriver(eventData.getCoDriverId(), password)) {
                                Utility.showMsg(getString(R.string.valid_password));
                            } else {
                                String logDate = Utility.dateOnlyStringGet(eventData.getEventDateTime());

                                EventDB.EventUpdate(eventId, 2, driverId);
                                DailyLogDB.DailyLogSyncRevert(driverId, logId);

                                int dailyLogId = DailyLogDB.getDailyLog(eventData.getCoDriverId(), logDate);
                                // update codriver id
                                EventDB.EventCopy(eventId, 4, 1, eventData.getCoDriverId(), dailyLogId, driverId);
                                DailyLogDB.DailyLogCertifyRevert(driverId, dailyLogId);

                                // Post rule
                                MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo);
                                // EventDB.EventTransfer(eventData.get_id(), eventData.getDriverId(), eventData.getCoDriverId(), logDate);
                                mListener.onEditEventFinished();
                                new ViolationSync(true, true).execute();
                            }
                        }
                        break;
                    }

                    Date currentSelectedTime = Utility.parse(Utility.getDate(selectedEventDateTime) + " " + tvEventTime.getText());
                    if (currentSelectedTime.after(Utility.newDate())) {
                        Utility.showAlertMsg(Utility.context.getString(R.string.future_time_alert));
                        break;
                    }

                    if (edComments.length() < 4 && Utility._appSetting.getdEditFg() == 0) {
                        Utility.showAlertMsg(getString(R.string.comment_alert));
                        break;
                    }
                    String location = edLocationDescription.getText().toString();
                  /*  if (Utility.currentLocation.getLatitude() < 0 && edLocationDescription.getText().toString().isEmpty()) {
                        Utility.showAlertMsg(getString(R.string.location_alert));
                        break;
                    }*/


                    if (location.isEmpty() || location.length() < 5) {
                        Utility.showAlertMsg(getString(R.string.location_alert));
                        return;
                    }

                    if (isEditEvent) {

                        Date previousTime = Utility.parse(selectedEventDateTime);
                        if (eventData.getEventRecordOrigin() != 4 && Utility._appSetting.getdEditFg() == 0) {
                            EventBean previousEvent = EventDB.previousDutyStatusGet(driverId, selectedEventDateTime);

                            if (currentSelectedTime.before(Utility.parse(previousEvent.getEventDateTime()))) {
                                Utility.showAlertMsg(getString(R.string.previous_event_alert));
                                break;
                            }

                            if (ConstantFlag.ELDFg && previousEvent.getEventCode() == 3 && currentSelectedTime.before(previousTime)) {
                                Utility.showAlertMsg(getString(R.string.driving_shortening_alert));
                                break;
                            }

                            if (ConstantFlag.ELDFg && currentSelectedTime.after(previousTime) && currentStatus == 3) {
                                Utility.showAlertMsg(getString(R.string.driving_shortening_alert));
                                break;
                            }
                        }

                    }
                    saveEvent();

                    //finish();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(NewEventFragment.class.getName(), "onClick", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    AlertDialog ad;

    private void showWarningMessage(String message) {
        if (ad == null) {
            ad = new AlertDialog.Builder(getActivity()).create();
        }
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle(getString(R.string.e_log));
        ad.setIcon(R.drawable.ic_launcher);
        ad.setMessage(message);
        ad.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.ok),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ad.dismiss();
                    }
                });
        ad.show();
    }

    private void refreshHoursOnStatusUpdate() {
        try {
            Date cDate = Utility.dateOnlyGet(Utility.newDate());
            Utility.dutyStatusList = HourOfServiceDB.DutyStatusGet15Days(cDate, Utility.onScreenUserId + "", false);
            HourOfService.listDutyStatus = Utility.dutyStatusList;
            int timeLeft = 0;

            int currentRule = DailyLogDB.getCurrentRule(Utility.onScreenUserId);
            if (currentRule == 1) {
                timeLeft = HourOfService.CanadaHours70(Utility.getCurrentDateTime());
                timeLeft = timeLeft < 0 ? 0 : timeLeft;
                GPSData.TimeRemaining70 = timeLeft;
            } else if (currentRule == 2) {
                timeLeft = HourOfService.CanadaHours120(Utility.getCurrentDateTime());
                timeLeft = timeLeft < 0 ? 0 : timeLeft;
                GPSData.TimeRemaining120 = timeLeft;
            } else if (currentRule == 3) {
                timeLeft = HourOfService.US70HoursGet(Utility.getCurrentDateTime());
                timeLeft = timeLeft < 0 ? 0 : timeLeft;
                GPSData.TimeRemainingUS70 = timeLeft;
            }

            if (currentRule == 1 || currentRule == 2) {
                ViolationBean chs = HourOfService.CanadaHoursSummary(Utility.getCurrentDateTime(), false);

                GPSData.WorkShiftRemaining = chs.getTimeLeft16();

                // predictive us rule calculation when under canada rule
                timeLeft = HourOfService.US70HoursGet(Utility.getCurrentDateTime());
                timeLeft = timeLeft < 0 ? 0 : timeLeft;
                GPSData.TimeRemainingUS70 = timeLeft;
            } else {
                ViolationBean chs = HourOfService.USHoursSummaryGet(Utility.getCurrentDateTime(), false);
                GPSData.WorkShiftRemaining = chs.getTimeLeft14US();
                // predictive canada rule calculation when under US rule
                timeLeft = HourOfService.CanadaHours70(Utility.getCurrentDateTime());
                timeLeft = timeLeft < 0 ? 0 : timeLeft;
                GPSData.TimeRemaining70 = timeLeft;
            }

            DailyLogDB.DailyLogHoursReCertify(Utility.onScreenUserId, Utility.getCurrentDate());


        } catch (Exception exe) {
            LogFile.write(ELogFragment.class.getName() + "::AutoViolationCalculate Error:" + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(NewEventFragment.class.getName(), "AutoViolationCalculate", exe.getMessage(), Utility.printStackTrace(exe));

        }
    }

    private void showTimePicker() {
        try {
           /* DialogFragment newFragment = new TimePickerFragment();
            newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");*/

            String[] times = tvEventTime.getText().toString().split(":");
            int hour = Integer.valueOf(times[0]);
            int minute = Integer.valueOf(times[1]);
            second = Integer.valueOf(times[2]);


            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    Calendar calendar = Calendar.getInstance();
           /* if (hourOfDay > calendar.get(Calendar.HOUR_OF_DAY)) {
                Utility.showAlertMsg(Utility.context.getString(R.string.future_time_alert));
                return;
            } else if (hourOfDay == calendar.get(Calendar.HOUR_OF_DAY) && minute > calendar.get(Calendar.MINUTE)) {
                Utility.showAlertMsg(Utility.context.getString(R.string.future_time_alert));
                return;
            }*/
                    String s;
                    Format formatter;

                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, second); //reset seconds to zero

                    formatter = new SimpleDateFormat("HH:mm:ss");
                    s = formatter.format(calendar.getTime());
                    tvEventTime.setText(s);
                    //butSave.setEnabled(true);
                }
            }, hour, minute, DateFormat.is24HourFormat(getActivity()));
            mTimePicker.show();
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::showTimePicker Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(com.hutchsystems.hutchconnect.fragments.NewEventFragment.class.getName(), "showTimePicker", e.getMessage(), Utility.printStackTrace(e));

        }
    }

   /* private void showTimePicker() {
        try {
            DialogFragment newFragment = new TimePickerFragment();
            newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::showTimePicker Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }*/

    private void launchDutyStatusChange() {
        try {
            /*if (getFragmentManager().getBackStackEntryCount() > 0) {
                for (int i = 0; i < getFragmentManager().getBackStackEntryCount(); i++)
                    getFragmentManager().popBackStackImmediate();
            }*/

            FragmentManager manager = getFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();

            Fragment prev = manager.findFragmentByTag("dutystatus_dialog");
            if (prev != null) {
                ft.remove(prev);
                //ft.addToBackStack(null);
                ft.commitNow();
                ft = manager.beginTransaction();
            }
            DutyStatusChangeDialog dutyDialog = DutyStatusChangeDialog.newInstance();
            dutyDialog.mListener = this;

            bDialogShowing = true;
            dutyDialog.setFromNewEvent(1);
            dutyDialog.setCurrentStatus(currentStatus);
            dutyDialog.show(ft, "dutystatus_dialog");
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::launchDutyStatusChange Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(NewEventFragment.class.getName(), "launchDutyStatusChange", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void updateEventType() {
        try {
            String eventText = "";
            switch (currentStatus) {
                case 1:
                    eventType = 1;
                    eventCode = 1;
                    eventDescription = getResources().getString(R.string.duty_status_changed_to_off_duty);
                    eventText = getString(R.string.off_duty_long);
                    break;
                case 2:
                    eventType = 1;
                    eventCode = 2;
                    eventDescription = getResources().getString(R.string.duty_status_changed_to_sleeper_berth);
                    eventText = getString(R.string.sleeper_long);
                    break;
                case 3:
                    eventType = 1;
                    eventCode = 3;
                    eventDescription = getResources().getString(R.string.duty_status_changed_to_driving);
                    eventText = getString(R.string.driving_long);
                    break;
                case 4:
                    eventType = 1;
                    eventCode = 4;
                    eventDescription = getResources().getString(R.string.duty_status_changed_to_on_duty);
                    eventText = getString(R.string.on_duty_long);
                    break;
                case 5:
                    eventType = 3;
                    eventCode = 1;
                    eventDescription = getResources().getString(R.string.duty_status_changed_to_personal_use);
                    eventText = getString(R.string.personal_use_long);
                    break;
                case 6:
                    eventType = 3;
                    eventCode = 2;
                    eventDescription = getResources().getString(R.string.duty_status_changed_to_yard_move);
                    eventText = getString(R.string.yard_move_long);
                    break;
            }


            clearDutyStatus();
            switch (currentStatus) {
                case 1:
                    butOffDuty.setSelected(true);
                    break;
                case 2:
                    butSleeper.setSelected(true);
                    break;
                case 3:
                    butDriving.setSelected(true);
                    break;
                case 4:
                    butOnDuty.setSelected(true);
                    break;
                case 5:
                    butPersonalUse.setSelected(true);
                    break;
                case 6:
                    butYardMove.setSelected(true);
                    break;
            }
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::updateEventType Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(NewEventFragment.class.getName(), "updateEventType", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            if (requestCode == 1) {
                if (resultCode == Activity.RESULT_OK) {
                    //butSave.setEnabled(true);
                    setupKeyboard();
                    currentStatus = intent.getIntExtra("selected_duty_status", 1);

                    updateEventType();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.i("NewEventFragment", "Duty Status is not changed");
                }
            }
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::onActivityResult Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(NewEventFragment.class.getName(), "onActivityResult", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        tvEventTime = null;
        //butSave = null;
        mListener = null;
        try {
            Utility.hideKeyboard(getActivity(), getActivity().getCurrentFocus());
        } catch (Exception exe) {

        }
    }

    @Override
    public void onSavedDutyStatus(int status, boolean saveForActiveDriver, String annotation, String location) {
        try {
            dutyDialog = null;
            //butSave.setEnabled(true);
            setupKeyboard();
            currentStatus = status;

            updateEventType();
            if (!annotation.isEmpty())
                edComments.setText(annotation);

            if (!location.isEmpty())
                edLocationDescription.setText(location);
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::onSavedDutyStatus Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(NewEventFragment.class.getName(), "onSavedDutyStatus", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onDissmisDialog() {
        bDialogShowing = false;
    }

    public void setupKeyboard() {
//        if (edLocationDescription == null || edComments == null) {
//            return;
//        }
        edLocationDescription.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on Enter key press
                    edLocationDescription.clearFocus();
                    //etPassword.requestFocus();
                    (new Handler()).postDelayed(new Runnable() {
                        public void run() {
                            edComments.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                            edComments.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                        }
                    }, 100);

                    return true;
                }
                return false;
            }
        });

        edComments.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            saveEvent();
                        }
                    }, 100);
                }
                return false;
            }
        });
    }

    private void saveEvent() {
        // Check annotaion is dvir, fuel and update flag
        String annotation = edComments.getText() + "";
        if (annotation != null && currentStatus == 4) {

            MainActivity.iSFueling = annotation.toLowerCase().contains("fueling");
            MainActivity.isDVIR = annotation.toLowerCase().contains("dvir");

        }

        if (isEditEvent) {
            if (Utility._appSetting.getdEditFg() == 1) {
                EventBean bean = new EventBean();
                bean.set_id(eventData.get_id());
                bean.setDriverId(driverId);
                bean.setEventType(eventType);
                bean.setEventCode(eventCode);
                bean.setEventCodeDescription(eventDescription);
                bean.setAnnotation(edComments.getText() + "");
                bean.setLatitude(eventData.getLatitude());
                bean.setLongitude(eventData.getLongitude());
                bean.setOdometerReading(eventData.getOdometerReading());
                bean.setEngineHour(eventData.getEngineHour());
                bean.setEventDateTime(Utility.getDate(selectedEventDateTime) + " " + tvEventTime.getText());

                bean.setSplitSleep(switchShowAlertSplitSleep.isChecked() ? 1 : 0);

                bean.setSyncFg(0);
                EventDB.EventUpdate(bean);
            } else {
                String location = edLocationDescription.getText().toString();
                EventBean newEvent = eventData;
                // set new editted informations
                newEvent.setEventType(eventType);
                newEvent.setEventCode(eventCode);
                newEvent.setEventCodeDescription(eventDescription);
                newEvent.setEventRecordOrigin(eventRecordOrigin);
                newEvent.setEventRecordStatus(eventRecordStatus);
                newEvent.setAnnotation(edComments.getText() + "");
                newEvent.setEventSequenceId(EventDB.getEventSequenceId());
                newEvent.setCreatedBy(driverId);
                newEvent.setCreatedDate(Utility.getCurrentDateTime());
                newEvent.setEventDateTime(Utility.getDate(selectedEventDateTime) + " " + tvEventTime.getText());
                newEvent.setRuleId(spinnerRule.getSelectedItemPosition() + 1);
                newEvent.setSplitSleep(switchShowAlertSplitSleep.isChecked() ? 1 : 0);
                newEvent.setReduceFg(switchReduceTime.isChecked());
                newEvent.setWaitingFg(switchWaitingTime.isChecked());
                newEvent.setSyncFg(0);

                if (!location.isEmpty() && !location.equals(eventData.getLocationDescription())) {
                    newEvent.setLatitude("0");
                    newEvent.setLongitude("0");
                }
                newEvent.setLocationDescription(location);
                EventDB.EventSave(newEvent);

                // EventDB.EventCreateWithLocation(Utility.getDate(selectedEventDateTime) + " " + tvEventTime.getText(), eventType, eventCode, eventDescription, eventRecordOrigin, eventRecordStatus, logId, driverId, edLocationDescription.getText() + "", edComments.getText() + "");
                //update to change status of the old to inactive
                eventRecordStatus = 2;
                if (eventId != 0) {
                    EventDB.EventUpdate(eventId, eventRecordStatus, driverId);
                } else
                    EventDB.EventUpdate(selectedEventDateTime, eventRecordOrigin, eventRecordStatus, driverId, logId);

                // clear missing data diagnostic event
                if (DiagnosticIndicatorBean.MissingElementDiagnosticFg && eventType == 1 && eventCode >= 3) {
                    if (!EventDB.getMissingDataFg(driverId)) {
                        DiagnosticIndicatorBean.MissingElementDiagnosticFg = false;
                        // save malfunction for Missing element Diagnostic event
                        DiagnosticMalfunction.saveDiagnosticIndicatorByCode("3", 4, "MissingElementDiagnosticFg");
                    }
                }
            }

            DailyLogDB.DailyLogCertifyRevert(driverId, logId);

            // Post Data
            MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo_DailyLogId, logId);

            mListener.onEditEventSaved();

            if (CurrentEventFg) {
                MainActivity.currentDutyStatus = currentStatus;
                if (Utility.activeUserId == Utility.onScreenUserId)
                    MainActivity.activeCurrentDutyStatus = currentStatus;
            }
        } else {
            //123 LogFile.write(NewEventFragment.class.getName() + "::Save clicked: " + "Create new event" + " of driverId:" + driverId, LogFile.USER_INTERACTION, LogFile.DRIVEREVENT_LOG);
            EventDB.EventCreateManually(tvDateValue.getText() + " " + tvEventTime.getText(), eventType, eventCode, eventDescription, eventRecordOrigin, eventRecordStatus, logId, driverId, edLocationDescription.getText() + "", edComments.getText().toString(), spinnerRule.getSelectedItemPosition() + 1, switchShowAlertSplitSleep.isChecked() ? 1 : 0);

            DailyLogDB.DailyLogCertifyRevert(driverId, logId);

            // Post Data
            MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo_DailyLogId, logId);
            mListener.onNewEventSaved();
        }
        new ViolationSync(true, true).execute();
    }


    // Check event is applicable for split sleep
    public Boolean checkEventforSplitSleep() {

        try {
            if (eventData != null) {
                float duration=EventDB.SleeperDurationGet(driverId,selectedEventDateTime);
                Boolean teamDriver = Utility.user2.getAccountId() > 0;

                /*String[] durationFromList = duration.split(":");
                int duration = Integer.parseInt(durationFromList[0]);*/
                int rule = spinnerRule.getSelectedItemPosition() + 1;

                // For Canda Rule
                if (rule <= 2 && eventType == 1) {

                    if ((!teamDriver && eventCode == 2 && duration >= 2f) || (teamDriver && eventCode == 2 && duration >= 4f)) {

                        return true;

                    }

                }// For US Rule
                else if (rule > 2 && eventType == 1) {

                    if (eventCode == 2 && duration >= 2f && duration < 10f) {
                        return true;
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return false;
    }


    // Created By: Deepak Sharma
    // Created Date: 22 March 2019
    // check if this event qualify for reduced reset workshift for alberate
    public Boolean checkReducedReset() {

        try {
            if (eventData != null) {
                Date eventDate = Utility.parse(eventData.getEventDateTime());

                Date nextEventDate = Utility.newDate();
                Date previousEventDate = eventDate;

                // set initial value before 7 days
                Date lastReduceDate = Utility.getDate(eventDate, -7);
                // Reset could be reduced once a week
                for (int j = MainActivity.dutyStatusArrayList.size() - 2; j >= 0; j--) {
                    DutyStatus item = MainActivity.dutyStatusArrayList.get(j);

                    if (item.getStatus() <= 2 && item.isReducedFg()) {
                        lastReduceDate = item.getEventDateTime();
                        break;
                    }
                }

                long diff = Utility.getDiffDay(eventDate, lastReduceDate);

                // once in 7 days
                if (diff < 7) {
                    return false;
                }


                // get next event date
                for (DutyStatus ds : MainActivity.dutyStatusArrayList) {
                    if (ds.getEventDateTime().after(eventDate)) {
                        nextEventDate = ds.getEventDateTime();
                        break;
                    }
                }

                for (int j = MainActivity.dutyStatusArrayList.size() - 2; j >= 0; j--) {
                    DutyStatus item = MainActivity.dutyStatusArrayList.get(j);

                    if (item.getStatus() > 2 && item.getEventDateTime().before(eventDate)) {
                        previousEventDate = MainActivity.dutyStatusArrayList.get(j).getEventDateTime();
                        break;
                    }
                }

                long duration = (nextEventDate.getTime() - previousEventDate.getTime()) / (1000);

                if (duration >= 4 * 60 * 60 && duration < 8 * 60 * 60)
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return false;
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {
            case R.id.switchWaitingTime: {
                switchWaitingTime.setChecked(isChecked);

                break;
            }
            case R.id.switchReduceTime: {
                if (isChecked) {
                    if (checkReducedReset()) {
                        switchReduceTime.setChecked(true);
                    } else {
                        switchReduceTime.setChecked(false);
                        Toast.makeText(Utility.context, "This event is not applicable for Reduced work shift reset time.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    switchReduceTime.setChecked(false);

                }

                break;
            }
            case R.id.switchShowAlertSplitSleep: {
                if (isChecked) {
                    if (checkEventforSplitSleep()) {
                        switchShowAlertSplitSleep.setChecked(true);
                    } else {
                        switchShowAlertSplitSleep.setChecked(false);
                        Toast.makeText(Utility.context, "This event is not applicable for Split Sleep", Toast.LENGTH_LONG).show();
                    }

                } else {
                    switchShowAlertSplitSleep.setChecked(false);
                }
                break;
            }
        }


    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        //void onFragmentInteraction(Uri uri);
        void onNewEventSaved();

        void onNewEventFinished();

        void onEditEventSaved();

        void onEditEventFinished();

    }

    // Created By: Deepak Sharma
    // Created Date: 8 April 2017
    // Purpose: calculate violation
    private class ViolationSync extends AsyncTask<Void, Void, Boolean> {
        boolean dutyStatusRefresh;
        boolean reCertifyFg;

        public ViolationSync(boolean data, boolean reCertifyFg) {
            this.dutyStatusRefresh = data;
            this.reCertifyFg = reCertifyFg;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {


            MainActivity.ProcessViolation(dutyStatusRefresh);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (reCertifyFg)
                DailyLogDB.DailyLogHoursReCertify(Utility.onScreenUserId, logId);


        }
    }

    private void callGeocodeTask() {
        edLocationDescription.setText("");

        new NearestDistanceTask(nearestLocationTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    NearestDistanceTask.PostTaskListener<Boolean> nearestLocationTaskListener = new NearestDistanceTask.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean status) {
            if (status) {
                edLocationDescription.setText(Utility.currentLocation.getLocationDescription());
            } else {
                edLocationDescription.setText("");
            }
        }
    };

    GeocodeTask.PostTaskListener<Address> geocodeTaskListener = new GeocodeTask.PostTaskListener<Address>() {
        @Override
        public void onPostTask(Address address) {
            if (address != null) {
                String addressName = "";

                String fulladdress = address.getAddressLine(0);

                String city = address.getLocality();
                String state = address.getAdminArea();
                String country = address.getCountryName();
                String postalCode = address.getPostalCode();
                addressName = fulladdress;// city + ", " + state;

                edLocationDescription.setText(addressName);
            }
        }
    };
}
