package com.hutchsystems.hutchconnect.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.ChangeEventListner;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.ConstantFlag;
import com.hutchsystems.hutchconnect.common.CustomDateFormat;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.AnnotationDetailDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.ReportDB;
import com.hutchsystems.hutchconnect.db.VersionInformationDB;
import com.hutchsystems.hutchconnect.tasks.GeocodeTask;
import com.hutchsystems.hutchconnect.tasks.NearestDistanceTask;

import java.util.ArrayList;
import java.util.Collections;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

public class DutyStatusChangeDialog extends DialogFragment implements View.OnClickListener, View.OnFocusChangeListener, View.OnKeyListener, CompoundButton.OnCheckedChangeListener {
    String TAG = DutyStatusChangeDialog.class.getName();

    public DutyStatusChangeDialogInterface mListener;
    public ChangeEventListner listner;
    private final int OFF_DUTY = 1;
    private final int SLEEPER = 2;
    private final int DRIVING = 3;
    private final int ON_DUTY = 4;
    private final int PERSONAL_USE = 5;
    private final int YARD_MOVE = 6;

    Button btnAnnotation1, btnAnnotation2, btnAnnotation3, btnAnnotation4, btnAnnotation5, btnAnnotation6, btnAnnotation7;
    ColorStateList dutyStatusColorStateList;

    Button butOffDuty;
    Button butSleeper;
    Button butDriving;
    /* Button butDisableDriving;*/
    Button butOnDuty;
    Button butPersonalUse;
    Button butYardMove;
    Button butBack;
    Button butSave;
    EditText edAnnotation;
    EditText edLocation;
    TextView tvCoordinates;
    ImageButton imgCancel, btnLocation;


    ConstraintLayout layoutPersonalUse;
    ConstraintLayout layoutYardMove;
    ConstraintLayout layoutWaitingTime;
    Switch switchWaitingTime;

    int currentStatus = 1;
    int previousStatus = 1;
    int driverId;
    public String eventDescription = "";

    String textToSpeech = "";

    public int eventType;
    public int eventCode;
    public int eventRecordOrigin;
    public int eventRecordStatus;
    String shortStatus;

    int fromCreateEvent = 0;
    boolean saveForActiveDriver = false;
    String driverName = "";

    AlertDialog ad;
    Runnable clearText = new Runnable() {
        @Override
        public void run() {
            edAnnotation.setText("");
        }
    };

    public DutyStatusChangeDialog() {

    }

    public static DutyStatusChangeDialog newInstance() {

        DutyStatusChangeDialog fragment = new DutyStatusChangeDialog();

        return fragment;
    }

    public void clear() {
        try {

            edAnnotation.post(clearText);
            edLocation.setText("");
        } catch (Exception exe) {

        }
    }

    public void setCurrentStatus(int status) {
        currentStatus = status;
        previousStatus = currentStatus;
    }

    public void setFromNewEvent(int isNewEvent) {
        fromCreateEvent = isNewEvent;
    }

    public void setActiveDriver(boolean value) {
        saveForActiveDriver = value;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.activity_duty_status_change, container);
        // if development device
        /*if (Utility.IMEI.equals("356961076533234")) {
            Utility._CurrentDateTime = Utility.getCurrentDateTime();
            Utility.currentLocation.setLatitude(49.0416568);
            Utility.currentLocation.setLongitude(-122.2884158);

        }*/
        Context context = getActivity();
        if (context instanceof ChangeEventListner) {
            listner = (ChangeEventListner) context;

        }

        try {
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

            //Intent intent = getIntent();

            driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
            driverName = Utility.user1.isOnScreenFg() ? Utility.user1.getFirstName() : Utility.user2.getFirstName();

            if (saveForActiveDriver) {
                driverName = (Utility.user1.getAccountId() == Utility.activeUserId) ? Utility.user1.getFirstName() : Utility.user2.getFirstName();
            }

            initialize(view);

            eventRecordOrigin = 2; //change by driver
            eventRecordStatus = 1; //active

            //getDialog().setTitle("E-Log");
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            this.setCancelable(false);


        } catch (Exception e) {
            LogFile.write(DutyStatusChangeDialog.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DutyStatusChangeDialog.class.getName(), "::onCreateView:", e.getMessage(), Utility.printStackTrace(e));

        }
        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.activity_duty_status_change, null);

            ViewGroup viewGroup = (ViewGroup) getView();

            viewGroup.removeAllViews();
            viewGroup.addView(view);

            initialize(view);

        } catch (Exception e) {
            LogFile.write(DutyStatusChangeDialog.class.getName() + "::onConfigurationChanged Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {

        } catch (Exception e) {
            LogFile.write(DutyStatusChangeDialog.class.getName() + "::onActivityCreated Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DutyStatusChangeDialog.class.getName(), "::onActivityCreated Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    private void initialize(View view) {
        try {
            butOffDuty = (Button) view.findViewById(R.id.butOffDuty);
            butOffDuty.setOnClickListener(this);
            butSleeper = (Button) view.findViewById(R.id.butSleeper);
            butSleeper.setOnClickListener(this);
            butDriving = (Button) view.findViewById(R.id.butDriving);
            butDriving.setOnClickListener(this);
            /*butDisableDriving = (Button) view.findViewById(R.id.butDisableDriving);*/
            butOnDuty = (Button) view.findViewById(R.id.butOnDuty);
            butOnDuty.setOnClickListener(this);
            butPersonalUse = (Button) view.findViewById(R.id.butPersonalUse);
            butPersonalUse.setOnClickListener(this);
            butYardMove = (Button) view.findViewById(R.id.butYardMove);
            butYardMove.setOnClickListener(this);

            imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
            imgCancel.setOnClickListener(this);

            btnLocation = (ImageButton) view.findViewById(R.id.btnLocation);
            btnLocation.setOnClickListener(this);


            butSave = (Button) view.findViewById(R.id.butDutyStatusSave);
            butSave.setOnClickListener(this);
            butSave.setEnabled(fromCreateEvent == 1);
            butSave.setOnClickListener(this);

            btnAnnotation1 = (Button) view.findViewById(R.id.btnAnnotation1);
            btnAnnotation1.setOnClickListener(this);
            btnAnnotation1.setVisibility(View.GONE);

            btnAnnotation2 = (Button) view.findViewById(R.id.btnAnnotation2);
            btnAnnotation2.setOnClickListener(this);
            btnAnnotation2.setVisibility(View.GONE);

            btnAnnotation3 = (Button) view.findViewById(R.id.btnAnnotation3);
            btnAnnotation3.setOnClickListener(this);
            btnAnnotation3.setVisibility(View.GONE);

            btnAnnotation4 = (Button) view.findViewById(R.id.btnAnnotation4);
            btnAnnotation4.setOnClickListener(this);
            btnAnnotation4.setVisibility(View.GONE);

            btnAnnotation5 = (Button) view.findViewById(R.id.btnAnnotation5);
            btnAnnotation5.setOnClickListener(this);
            btnAnnotation5.setVisibility(View.GONE);

            btnAnnotation6 = (Button) view.findViewById(R.id.btnAnnotation6);
            btnAnnotation6.setOnClickListener(this);
            btnAnnotation6.setVisibility(View.GONE);

            btnAnnotation7 = (Button) view.findViewById(R.id.btnAnnotation7);
            btnAnnotation7.setOnClickListener(this);
            btnAnnotation7.setVisibility(View.GONE);

            edAnnotation = (EditText) view.findViewById(R.id.edDutyStatusAnnotation);
            edAnnotation.setOnFocusChangeListener(this);
            edAnnotation.setOnKeyListener(this);
            edAnnotation.setText("");

            edLocation = (EditText) view.findViewById(R.id.edLocation);
            edLocation.setVisibility(View.VISIBLE);

            tvCoordinates = (TextView) view.findViewById(R.id.tvCoordinates);
            tvCoordinates.setVisibility(View.GONE);
          /*  edLocation.setText("");
            if (Utility.currentLocation.getLatitude() < 0) {
                edLocation.setVisibility(View.VISIBLE);
            } else {
                edLocation.setVisibility(View.GONE);
            }*/

            // enable location from google map it was only unable previously only on AOBRD
            callGeocodeTask();
            layoutPersonalUse = (ConstraintLayout) view.findViewById(R.id.lPersonalUse);
            layoutYardMove = (ConstraintLayout) view.findViewById(R.id.lYardMove);
            layoutWaitingTime = (ConstraintLayout) view.findViewById(R.id.layoutWaitingTime);
            switchWaitingTime = (Switch) view.findViewById(R.id.switchWaitingTime);
            layoutWaitingTime.setVisibility(View.GONE);
            //butSave.setEnabled(false);
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

            if (currentStatus <= 2 && ELogFragment.currentRule == 7) {
                layoutWaitingTime.setVisibility(View.VISIBLE);
            }

            CheckUpdate();
            //CheckUpdate();
            createLayoutDynamically(view);
        } catch (Exception e) {
            LogFile.write(DutyStatusChangeDialog.class.getName() + "::initializeControls Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            Utility.printError(e.getMessage());
            LogDB.writeLogs(DutyStatusChangeDialog.class.getName(), "::initializeControls Error:", e.getMessage(), Utility.printStackTrace(e));
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            Utility.hideKeyboard(getActivity(), v);
        }
    }

    StringBuilder builder = new StringBuilder();

    // Created By: Pallavi Wattamwar
    // Created Date: 1 May 2019
    // Purpose: Display annotation Depends upon configuration on web
    public void createLayoutDynamically(final View v) {

        LinearLayout landScapeLayout = (LinearLayout) v.findViewById(R.id.dynamicLayout);
        LinearLayout portraitLayout = (LinearLayout) v.findViewById(R.id.dynamicLayout1);
        LinearLayout portraitLayout2 = (LinearLayout) v.findViewById(R.id.dynamicLayout2);

        ArrayList<String> list = AnnotationDetailDB.AnnotationGet();
        // Temporary annotation
        ArrayList<String> templist = new ArrayList<>();
        templist.add(btnAnnotation1.getText().toString());
        templist.add(btnAnnotation2.getText().toString());
        templist.add(btnAnnotation3.getText().toString());
        templist.add(btnAnnotation4.getText().toString());
        templist.add(btnAnnotation5.getText().toString());
        templist.add(btnAnnotation6.getText().toString());
        templist.add(btnAnnotation7.getText().toString());

        if (list.size() == 0) {
            list = templist;
        }

        Collections.reverse(list);

        // Get Annotation From Sync
        for (int i = 0; i < list.size(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1;

            params.setMargins(6, 6, 6, 6);


            // Annotation Button
            Button btnAnnotation = new Button(getContext(), null, 0, R.style.AnnotationStyle);
            btnAnnotation.setLayoutParams(params);
            btnAnnotation.setText(list.get(i));
            btnAnnotation.setId(i);
            btnAnnotation.setPadding(10, 10, 10, 10);
            // btnAnnotation.setHeight(WRA);
            final String annotation = btnAnnotation.getText().toString();
            btnAnnotation.setLayoutParams(params);

            //params.addRule(RelativeLayout.RIGHT_OF,i+1);

            // Depends on Screen Orientation Set Buttons
            if ((i > 2 && i < 6) && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                /*params1.addRule(RelativeLayout.RIGHT_OF,i+1);
                btnAnnotation.setLayoutParams(params1);*/
                portraitLayout.addView(btnAnnotation);

            } else if (i >= 6 && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
         /*       params2.addRule(RelativeLayout.RIGHT_OF,i+1);
                btnAnnotation.setLayoutParams(params2);*/
                portraitLayout2.addView(btnAnnotation);
            } else {
                landScapeLayout.addView(btnAnnotation);


            }


            btnAnnotation.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {

                    // Check duplicate annotation
                    if (!builder.toString().contains(annotation)) {

                        if (!builder.toString().isEmpty()) {
                            builder.append(",");
                        }
                        builder.append(annotation);
                        edAnnotation.setText(builder.toString());

                    }
                }
            });
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                (keyCode == KeyEvent.KEYCODE_ENTER)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (currentStatus != previousStatus) {
                        String annotation = edAnnotation.getText() + "";
                        String location = edLocation.getText().toString();
                        if (eventType == 3 && annotation.length() < 4) {
                            Utility.showMsg(getString(R.string.annotation_alert));
                            return;
                        }
                        saveDutyStatus();

                        edAnnotation.setOnKeyListener(null);
                        edAnnotation.setOnFocusChangeListener(null);
                        edAnnotation.setOnClickListener(null);
                        if (mListener != null) {
                            mListener.onSavedDutyStatus(currentStatus, saveForActiveDriver, annotation, location);
                        }
                    }
                    mListener = null;
                    dismiss();
                }
            }, 100);
        } else if (keyCode == KeyEvent.KEYCODE_DEL) {
            builder = new StringBuilder();
            edAnnotation.setText("");
        }
        return false;
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
                case R.id.imgCancel:
                    if (mListener != null) {
                        mListener.onDissmisDialog();
                    }
                    Utility.hideKeyboard(getActivity(), view);
                    mListener = null;
                    edAnnotation.setOnKeyListener(null);
                    edAnnotation.setOnFocusChangeListener(null);
                    edAnnotation.setOnClickListener(null);

                    dismiss();
                    break;
                case R.id.btnLocation:
                    callGeocodeTask();
                    break;
                case R.id.butDutyStatusSave:

                    Utility.hideKeyboard(getActivity(), view);
                    if (currentStatus != previousStatus) {
                        String annotation = edAnnotation.getText() + "";
                        String location = edLocation.getText().toString();
                        if (eventType == 3 && annotation.length() < 4) {
                            Utility.showMsg(getString(R.string.comment_alert));
                            return;
                        }

                        if (location.isEmpty() || location.length() < 5) {
                            Utility.showAlertMsg(getString(R.string.location_alert));
                            return;
                        }

                        saveDutyStatus();

                        edAnnotation.setOnKeyListener(null);
                        edAnnotation.setOnFocusChangeListener(null);
                        edAnnotation.setOnClickListener(null);

                        dismiss();

                        if (mListener != null) {
                            mListener.onSavedDutyStatus(currentStatus, saveForActiveDriver, annotation, location);
                        }
                    }

                    mListener = null;
                    break;
          /*      case R.id.btnAnnotation1:
                case R.id.btnAnnotation2:
                case R.id.btnAnnotation3:
                case R.id.btnAnnotation4:
                case R.id.btnAnnotation5:
                case R.id.btnAnnotation6:
                case R.id.btnAnnotation7:
                    try {
                        Button annotation = (Button) view;
                        edAnnotation.setText(annotation.getText());
                    } catch (Exception exe) {
                    }

                    break;
*/

                case R.id.btnAnnotation1:
                    if (!builder.toString().isEmpty()) {
                        builder.append(",");
                    }
                    builder.append(btnAnnotation1.getText());
                    edAnnotation.setText(builder.toString());
                    break;
                case R.id.btnAnnotation2:
                    if (!builder.toString().isEmpty()) {
                        builder.append(",");
                    }
                    builder.append(btnAnnotation2.getText());
                    edAnnotation.setText(builder.toString());
                    break;
                case R.id.btnAnnotation3:
                    if (!builder.toString().isEmpty()) {
                        builder.append(",");
                    }
                    builder.append(btnAnnotation3.getText());
                    edAnnotation.setText(builder.toString());
                    break;
                case R.id.btnAnnotation4:
                    if (!builder.toString().isEmpty()) {
                        builder.append(",");
                    }
                    builder.append(btnAnnotation4.getText());
                    edAnnotation.setText(builder.toString());
                    break;
                case R.id.btnAnnotation5:
                    if (!builder.toString().isEmpty()) {
                        builder.append(",");
                    }
                    builder.append(btnAnnotation5.getText());
                    edAnnotation.setText(builder.toString());
                    break;
                case R.id.btnAnnotation6:
                    if (!builder.toString().isEmpty()) {
                        builder.append(",");
                    }
                    builder.append(btnAnnotation6.getText());
                    edAnnotation.setText(builder.toString());
                    break;
                case R.id.btnAnnotation7:
                    if (!builder.toString().isEmpty()) {
                        builder.append(",");
                    }
                    builder.append(btnAnnotation7.getText());
                    edAnnotation.setText(builder.toString());

                    break;
            }

            if (currentStatus <= 2 && ELogFragment.currentRule == 7) {
                layoutWaitingTime.setVisibility(View.VISIBLE);
            } else {

                layoutWaitingTime.setVisibility(View.GONE);
            }

            if (fromCreateEvent == 0) {
                butSave.setEnabled(currentStatus != previousStatus);
            }


        } catch (Exception e) {
            LogFile.write(DutyStatusChangeDialog.class.getName() + "::onClick Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DutyStatusChangeDialog.class.getName(), "::onActivityCreated:", e.getMessage(), Utility.printStackTrace(e));

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

    private void saveDutyStatus() {

        // if testing device then get datetime from local device
        if (!ConstantFlag.LiveFg && !Utility.HutchConnectStatusFg) {
            Utility._CurrentDateTime = Utility.getCurrentDateTime();
        }

        String dateTime = Utility._CurrentDateTime;
        String annotation = edAnnotation.getText() + "";

        if (fromCreateEvent == 0) {

            // for Duty status  driving and onduty deduct balance
            if (currentStatus == 3 || currentStatus == 4) {
                Utility.DeviceBalancePost();
            }

            int driverId = Utility.onScreenUserId;
            String eventDateTime = Utility._CurrentDateTime;

            // it is commented as we will now save event with location of minimum 5 character long as per fmcsa regulation

            String location = edLocation.getText() + "";

            String odometerReading = Double.valueOf(CanMessages.OdometerReading).intValue() + "";
            String engineHours = String.format("%.1f", Double.valueOf(CanMessages.EngineHours));

            // if personal use then get lat,lon upto 1 decimal place else upto 2 decimal place
            String lat = (currentStatus == 5 ? Utility.truncate(Utility.currentLocation.getLatitude(), 1) : Utility.truncate(Utility.currentLocation.getLatitude(), 2)) + "";
            String lon = (currentStatus == 5 ? Utility.truncate(Utility.currentLocation.getLongitude(), 1) : Utility.truncate(Utility.currentLocation.getLongitude(), 2)) + "";

            ReportDB obj = new ReportDB(getContext());

            int coDriverId = Utility.user1.getAccountId() == driverId ? Utility.user2.getAccountId() : Utility.user1.getAccountId();

            // if previous status  was yard move or pc then clear it
            if (previousStatus >= 5 && currentStatus < 5) {
                // save event entered by driver
                obj.EventCreate(driverId, eventDateTime, 3, 0, "Driver Indication for PC, YM and WT cleared", 1, 1, annotation, odometerReading, engineHours, lat, lon, location, coDriverId);

                // add 1 seconds while adding next event from special driving status clear
                eventDateTime = Utility.format(Utility.addSeconds(Utility.parse(eventDateTime), 1), CustomDateFormat.dt1);

                listner.changeEventListner(dateTime, false, driverId, 3, 0);
            }

            // save event entered by driver
            obj.EventCreate(driverId, eventDateTime, eventType, eventCode, eventDescription, eventRecordOrigin, eventRecordStatus, annotation, odometerReading, engineHours, lat, lon, location, coDriverId);

            // fire change event listener
            listner.changeEventListner(dateTime, false, driverId, eventType, eventCode);


            // special status in blutooth data is only for active driver
            if (Utility.activeUserId == Utility.onScreenUserId)
                // if special category is selected then set unit id
                if (currentStatus >= 5) {
                    listner.onSpecialStatusChange(1);
                } else if (previousStatus > 4) {
                    listner.onSpecialStatusChange(0);
                }

            // set edit text location to Location Description same as aobrd need to discuss
            Utility.currentLocation.setLocationDescription(edLocation.getText() + "");

            if (Utility._appSetting.getDutyStatusReading() == 1) {
                if (MainActivity.textToSpeech != null)
                    MainActivity.textToSpeech.speak(textToSpeech, TextToSpeech.QUEUE_ADD, null);
            }
        }
    }

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

    private void setupKeyboard() {
    }

    private void CheckUpdate() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String newVersion = VersionInformationDB.getVersionInformation().getCurrentVersion();

                    if (newVersion != null && !newVersion.isEmpty()) {
                        boolean newFg = Utility.VersionNewFg(newVersion);
                        if (newFg) {
                            Thread.sleep(1000);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utility.showAlertMsg("New version of this app is available. Please update your app!");
                                }
                            });
                        }
                    }
                } catch (Exception exe) {

                }
            }
        }).start();
    }

    private void callGeocodeTask() {
        edLocation.setText("");
        double latitude = Utility.truncate(Utility.currentLocation.getLatitude(), 2);
        double longitude = Utility.truncate(Utility.currentLocation.getLongitude(), 2);
        tvCoordinates.setText(latitude + ", " + longitude);
        new NearestDistanceTask(nearestLocationTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    NearestDistanceTask.PostTaskListener<Boolean> nearestLocationTaskListener = new NearestDistanceTask.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean status) {

            if (status) {
                tvCoordinates.setVisibility(View.GONE);
                edLocation.setText(Utility.currentLocation.getLocationDescription());
                edLocation.setEnabled(false);
                edLocation.requestFocus();
             /*   new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utility.hideKeyboard(getActivity(), edAnnotation);
                                }
                            });
                        } catch (Exception exe) {
                        }
                    }
                }).start();*/
            } else {
                tvCoordinates.setVisibility(View.VISIBLE);
                edLocation.setText("");
                edLocation.setEnabled(true);
                //edLocation.requestFocus();
            }
        }
    };

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.switchWaitingTime: {
                switchWaitingTime.setChecked(isChecked);
                break;
            }
        }
    }

    public interface DutyStatusChangeDialogInterface {
        void onSavedDutyStatus(int status, boolean saveForActiveDriver, String annotation, String location);

        void onDissmisDialog();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ad = null;
        dismiss();
    }
}
