package com.hutchsystems.hutchconnect.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hutchsystems.hutchconnect.FirstTimeUser;
import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.DiagnosticMalfunctionAdapter;
import com.hutchsystems.hutchconnect.adapters.InstructionAdapter;
import com.hutchsystems.hutchconnect.beans.EventBean;
import com.hutchsystems.hutchconnect.beans.MessageBean;
import com.hutchsystems.hutchconnect.beans.SettingsBean;
import com.hutchsystems.hutchconnect.beans.UserBean;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.ChatClient;
import com.hutchsystems.hutchconnect.common.ConstantFlag;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.UserPreferences;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.common.ZoneList;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.EventDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.LoginDB;
import com.hutchsystems.hutchconnect.db.MessageDB;
import com.hutchsystems.hutchconnect.db.ReportDB;
import com.hutchsystems.hutchconnect.db.SettingsDB;
import com.hutchsystems.hutchconnect.db.UserDB;
import com.hutchsystems.hutchconnect.tasks.SyncData;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class LoginFragment extends Fragment implements View.OnClickListener, ReportDB.ILogin {
    String TAG = LoginFragment.class.getName();

    private OnFragmentInteractionListener mListener;

    int SPEAK_OUT_PERIOD = 30;

    AutoCompleteTextView etUserName;
    EditText etPassword;
    Button btnLogin;
    ImageButton butDiagnostic, butMalfunction, fabSync, btnBack, btnBackInstructions, btnWifiHotspot,btnVehicleData;

    Dialog diagnosticDlg;

    Dialog malfunctionDlg;

    TextView tvVersion,tvCurrentDriver;

    TextToSpeech textToSpeech;
    boolean coDriver = false, InspectorModeFg;
    boolean firstLogin = true;

    RelativeLayout rlLoadingPanel;

    AlertDialog alertDialog;
    Thread thBTB = null;
    ConstraintLayout layoutInstruction;
    LinearLayout flagBar;
    boolean alertShownFg;
    ImageButton btnInfo;
    RecyclerView rvInstruction;
    TextView tvSwitch, tvUnitNo;

    // Deepak Sharma
    // 3 Aug 2016
    // send request to bluetooth device every 5 seconds
    private void startBTBThread() {

        if (thBTB != null) {
            thBTB.interrupt();
            thBTB = null;
        }
        thBTB = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException exe) {
                        break;
                    }

                    if (MainActivity.undockingMode) {
                        continue;
                    }

                    if (Utility.HutchConnectStatusFg) {
                        int timeDifference = (int) ((System.currentTimeMillis() - CanMessages.diagnosticEngineSynchronizationTime) / 1000);

                        if (timeDifference > 10) {
                            if (!alertShownFg && mListener != null) {
                                alertShownFg = true;
                                mListener.onAlertVehicleStart();
                            }
                        } else {

                            if (alertShownFg && mListener != null) {
                                alertShownFg = false;
                                mListener.onAlertClear();
                            }
                        }
                    }
                }
            }
        });
        thBTB.setName("thBTB");
        thBTB.start();
    }

    private void stopBTBThread() {
        if (thBTB != null) {
            thBTB.interrupt();
            thBTB = null;
        }
    }


    Handler checkLoginHandler = new Handler();
    Runnable checkLogin = new Runnable() {
        @Override
        public void run() {
            if (Utility.activeUserId == 0) {
                if (Utility.motionFg) {
                    textToSpeech.speak(getString(R.string.unidentified_driving_alert), TextToSpeech.QUEUE_ADD, null);
                }
                checkLoginHandler.postDelayed(this, SPEAK_OUT_PERIOD * 1000);
            }
        }
    };

    public LoginFragment() {

    }

    private void populateInstruction() {


        String[] data = getResources().getStringArray(R.array.instruction_list);
        InstructionAdapter adapter = new InstructionAdapter(data);
        rvInstruction.setAdapter(adapter);
    }

    private void initialize(View view) {
        try {
            layoutInstruction = (ConstraintLayout) view.findViewById(R.id.layoutInstruction);
            layoutInstruction.setVisibility(View.GONE);
            flagBar = (LinearLayout) getActivity().findViewById(R.id.flagBar);
            btnInfo = (ImageButton) view.findViewById(R.id.btnInfo);
            tvUnitNo = (TextView) view.findViewById(R.id.tvUnitNo);
            tvUnitNo.setText("Unit No: " + Utility.UnitNo);

            tvSwitch = (TextView) view.findViewById(R.id.tvSwitch);
            tvSwitch.setOnClickListener(this);
            btnInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    layoutInstruction.setVisibility(View.VISIBLE);
                    flagBar.setVisibility(View.GONE);
                    fabSync.setVisibility(View.GONE);
                    btnInfo.setVisibility(View.GONE);
                }
            });

            btnBackInstructions = (ImageButton) view.findViewById(R.id.btnBackInstructions);
            btnBackInstructions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    layoutInstruction.setVisibility(View.GONE);
                    flagBar.setVisibility(View.VISIBLE);
                    fabSync.setVisibility(View.VISIBLE);
                    btnInfo.setVisibility(View.VISIBLE);
                }
            });

            btnWifiHotspot = (ImageButton) view.findViewById(R.id.btnWifiHotspot);
            btnWifiHotspot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });

            tvSHowPassword = (TextView) view.findViewById(R.id.tvShowHidePassword);
            rvInstruction = (RecyclerView) view.findViewById(R.id.rvInstruction);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
            rvInstruction.setLayoutManager(mLayoutManager);
            rvInstruction.setItemAnimator(new DefaultItemAnimator());
            populateInstruction();

            tvVersion = (TextView) view.findViewById(R.id.tvVersion);
            tvCurrentDriver = (TextView) view.findViewById(R.id.tvCurrentDriver);
            if (coDriver) {
                tvCurrentDriver.setText("");
            }

            rlLoadingPanel = (RelativeLayout) getActivity().findViewById(R.id.loadingPanel);

            tvVersion.setText(getString(R.string.version) + " " + Utility.ApplicationVersion);
            fabSync = (ImageButton) view.findViewById(R.id.fab);

            fabSync.setOnClickListener(this);

            etUserName = (AutoCompleteTextView) view.findViewById(R.id.etUserName);
            etPassword = (EditText) view.findViewById(R.id.etPassword);
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
           /* etPassword.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getActionMasked();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        final int DRAWABLE_RIGHT = 2;
                        if (event.getX() >= (etPassword.getRight() - etPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            // your action here
                            int visible = (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            int invisible = (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            if (etPassword.getInputType() == visible) {
                                etPassword.setInputType(invisible);
                            } else {
                                etPassword.setInputType(visible);
                            }
                            return true;
                        }
                    }
                    return false;

                }
            });*/


            tvSHowPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tvSHowPassword.getText().toString().equals("Show")) {
                        etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        tvSHowPassword.setText("Hide");
                    } else {

                        etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        tvSHowPassword.setText("Show");
                    }
                }
            });

            etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                    if (actionId == EditorInfo.IME_ACTION_DONE) {

                        login();
                    }
                    return false;
                }
            });

            butDiagnostic = (ImageButton) view.findViewById(R.id.btnDiagnostic);
            if (!Utility.dataDiagnosticIndicatorFg) {
                butDiagnostic.setVisibility(View.GONE);
            }
            butDiagnostic.setOnClickListener(this);

            butMalfunction = (ImageButton) view.findViewById(R.id.btnMalfunction);
            if (!Utility.malFunctionIndicatorFg) {
                butMalfunction.setVisibility(View.GONE);
            }
            butMalfunction.setOnClickListener(this);


            btnLogin = (Button) view.findViewById(R.id.btnLogin);
            btnLogin.setOnClickListener(this);

            btnVehicleData = (ImageButton) view.findViewById(R.id.btnVehicleData);
            btnVehicleData.setOnClickListener(this);

            btnBack = (ImageButton) view.findViewById(R.id.btnBack);
            btnBack.setOnClickListener(this);
            btnBack.setVisibility(View.GONE);
            tvSwitch.setVisibility(View.VISIBLE);
            if (coDriver) {
                btnBack.setVisibility(View.VISIBLE);
                tvSwitch.setVisibility(View.GONE);
                Utility.user2.setFirstLoginFg(Utility.user2.getAccountId() == 0);
                if (!Utility.user2.isFirstLoginFg()) {
                    etUserName.setText(Utility.user2.getUserName());
                    etUserName.setEnabled(false);
                    firstLogin = false;
                }

            } else {
                Utility.user1.setFirstLoginFg(Utility.user1.getAccountId() == 0);
                if (!Utility.user1.isFirstLoginFg()) {
                    etUserName.setText(Utility.user1.getUserName());
                    etUserName.setEnabled(false);
                    firstLogin = false;
                    btnBack.setVisibility(View.VISIBLE);
                    tvSwitch.setVisibility(View.GONE);
                }

            }

            if (InspectorModeFg) {
                btnBack.setVisibility(View.GONE);
                tvSwitch.setVisibility(View.GONE);
            } else {
                if (firstLogin && !ConstantFlag.Flag_Development) {
                    startBTBThread();
                }
            }

            if (mListener != null) {
                mListener.updateFlagbar(false);
            }
        } catch (Exception e) {
            LogFile.write(LoginFragment.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(LoginFragment.class.getName(), "initialize", e.getMessage(), Utility.printStackTrace(e));
        }
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    TextView tvSHowPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_login, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        coDriver = getArguments().getBoolean("CoDriverFg");
        InspectorModeFg = getArguments().getBoolean("InspectorModeFg");
        if (Utility.user1.getAccountId() == 0) {
            if (autoLogin()) {
                return mainView;
            }
        }
        initialize(mainView);

        textToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });

        if (Utility.user1.getAccountId() == 0 && Utility.user2.getAccountId() == 0) {
            checkLoginHandler.postDelayed(checkLogin, 0);
        }

        // total minutes of unidentified driving for current 24 hours
        String date = Utility.getDateTime(Utility.getCurrentDate(), 0);

        // get total unidentified seconds
        Utility.UnidentifiedDrivingTime = EventDB.getUnidentifiedTime(date);

        if (ConstantFlag.Flag_Development) {
            String username = "deepak";
            String password = "#85Shar8";

            if (Utility.IMEI.equals("353608073208384")) {
                if (coDriver) {
                    username = "rakesh";
                    password = "#86Kesh";
                }

                if (etUserName.getText().toString().isEmpty())
                    etUserName.setText(username);
                if (etPassword.getText().toString().isEmpty())
                    etPassword.setText(password);
            }
        }
        return mainView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View view) {
        try {
            Utility.hideKeyboard(getActivity(), view);
            switch (view.getId()) {
                case R.id.tvSwitch:
                    mListener.switchToAllUnitNo();
                    break;
                case R.id.btnLogin:
                    login();
                    break;
                    case R.id.btnVehicleData:
                    if (mListener != null) {
                        mListener.setProtocol();
                    }
                    break;
                case R.id.btnBack:
                    mListener.backFromLogin();
                    break;
                case R.id.fab:

                    if (Utility.isInternetOn()) {
                        showLoaderAnimation(true);
                        new SyncData(syncDataPostTaskListener).execute();
                    } else {
                        Utility.showAlertMsg(getString(R.string.internet_connection_alert));
                    }
                    break;
                case R.id.btnDiagnostic:
                    if (diagnosticDlg == null) {
                        diagnosticDlg = new Dialog(getActivity());
                        diagnosticDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    }

                    if (diagnosticDlg != null && !diagnosticDlg.isShowing()) {
                        LayoutInflater li = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View v = li.inflate(R.layout.listview_dialog, null, false);

                        diagnosticDlg.setContentView(v);

                        TextView tvTitle = (TextView) diagnosticDlg.findViewById(R.id.tvTitle);
                        tvTitle.setText(getString(R.string.data_diagnostic));
                        ImageButton imgCancel = (ImageButton) diagnosticDlg.findViewById(R.id.imgCancel);
                        imgCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                diagnosticDlg.dismiss();
                            }
                        });

                        ListView listView = (ListView) diagnosticDlg.findViewById(R.id.lvDiagnosticMalfunctionEvent);
                        DiagnosticMalfunctionAdapter eventAdapter = new DiagnosticMalfunctionAdapter(getActivity(), getDiagnosticMalfunctionEvents(3));
                        listView.setAdapter(eventAdapter);
                        diagnosticDlg.show();
                    }
                    break;
                case R.id.btnMalfunction:
                    if (malfunctionDlg == null) {
                        malfunctionDlg = new Dialog(getActivity());
                        malfunctionDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    }
                    if (malfunctionDlg != null && !malfunctionDlg.isShowing()) {
                        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View malView = layoutInflater.inflate(R.layout.listview_dialog, null, false);

                        malfunctionDlg.setContentView(malView);

                        TextView tvTitle = (TextView) malfunctionDlg.findViewById(R.id.tvTitle);
                        tvTitle.setText(getString(R.string.malfunction));

                        ImageButton imgCancel = (ImageButton) malfunctionDlg.findViewById(R.id.imgCancel);
                        imgCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                malfunctionDlg.dismiss();
                            }
                        });

                        ListView listViewEvent = (ListView) malfunctionDlg.findViewById(R.id.lvDiagnosticMalfunctionEvent);
                        DiagnosticMalfunctionAdapter malEventAdapter = new DiagnosticMalfunctionAdapter(getActivity(), getDiagnosticMalfunctionEvents(1));
                        listViewEvent.setAdapter(malEventAdapter);
                        malfunctionDlg.show();
                    }
                    break;
            }
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(LoginFragment.class.getName(), "onClick", e.getMessage(), Utility.printStackTrace(e));

        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ReportDB.mLoginListener = this;
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

        // hide turn on ignition message
        if (Utility.HutchConnectStatusFg && alertShownFg && mListener != null) {
            mListener.onAlertClear();
        }

        mListener = null;
        checkLoginHandler.removeCallbacksAndMessages(null);
        if (textToSpeech != null) {
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        diagnosticDlg = null;
        malfunctionDlg = null;
        stopBTBThread();
    }

    public void updateDiagnosticMalfunction() {
        if (butMalfunction != null) {
            if (Utility.malFunctionIndicatorFg) {
                butMalfunction.setVisibility(View.VISIBLE);
            } else {
                butMalfunction.setVisibility(View.GONE);
            }

            if (Utility.dataDiagnosticIndicatorFg) {
                butDiagnostic.setVisibility(View.VISIBLE);
            } else {
                butDiagnostic.setVisibility(View.GONE);
            }
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 14 May 2020
    // Purpose: fire on connection changed
    public void onConnectionChanged() {
        if (btnWifiHotspot != null)
            btnWifiHotspot.setVisibility(Utility.isInternetOn() ? View.GONE : View.VISIBLE);
    }

    private List<EventBean> getDiagnosticMalfunctionEvents(int code) {
        List<EventBean> eventList = EventDB.DiagnosticMalFunctionEventGetByCode(code);

        return eventList;
    }

    private void showLoaderAnimation(boolean isShown) {
        if (isShown) {
            rlLoadingPanel.setVisibility(View.VISIBLE);
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            rlLoadingPanel.setVisibility(View.GONE);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private boolean autoLogin() {
        SharedPreferences prefs = getContext().getSharedPreferences("HutchGroup", getContext().MODE_PRIVATE);
        int driverId = prefs.getInt("driverid", 0);
        int coDriverId = prefs.getInt("codriverid", 0);
        int activeUserId = prefs.getInt("activeuserid", 0);
        int onScreenUserId = prefs.getInt("onscreenuserid", 0);
        if (driverId > 0) {


            // Purpose : Check the  Driver Status is active if Driver Status is inactive then driver needs to login again
            if (!LoginDB.checkDriverStatus(driverId, coDriverId)) {
                // Reset login info
                Utility.savePreferences("driverid", 0);
                Utility.savePreferences("codriverid", 0);
                Utility.savePreferences("activeuserid", 0);
                Utility.savePreferences("onscreenuserid", 0);
                return false;
            }

            MainActivity.activeCurrentDutyStatus = EventDB.getCurrentDutyStatus(activeUserId);
            LoginDB.autoLoginUser(driverId, coDriverId, activeUserId, onScreenUserId);
            boolean logSent = prefs.getBoolean("logfile_sent", false);
            if (!logSent) {
                LogFile.sendLogFile(LogFile.AFTER_MID_NIGHT);
            }

            boolean userConfigured = prefs.getBoolean("user_configured", false);

            if (userConfigured) {
                DailyLogDB.DailyLogUserPreferenceRuleSave(driverId, UserPreferences.getCurrentRule(), Utility.getCurrentDateTime(), Utility.getCurrentDateTime());
                prefs.edit().putBoolean("user_configured", false).commit();
            }

            // we'll have to fetch setting on each time switching user as driver and codriver may have different settings
            getSettings(onScreenUserId);

            try {


                int shippingDriverId = prefs.getInt("driverid", -1);
                if (shippingDriverId == Utility.user1.getAccountId() || shippingDriverId == Utility.user2.getAccountId()) {
                    Utility.ShippingNumber = prefs.getString("shipping_number", "");
                    Utility.TrailerNumber = prefs.getString("trailer_number", "");
                }
            } catch (Exception exe) {
            }
            Utility.LastEventDate = EventDB.getLastEventDate(Utility.onScreenUserId);
            EventDB.getEngineHourOdometerSincePowerOn(driverId);

            //connect to server and make driver online

            // connect to chat server
            ChatClient.connect();
            ChatClient.checkConnection();


            mListener.autologinSuccessfully();
        }
        return driverId > 0;
    }

    private void login() {
        if (!ConstantFlag.LiveFg) {
            Utility.HutchConnectStatusFg = true;
        }
        if (firstLogin && !Utility.HutchConnectStatusFg) {
            Utility.showAlertMsg("You are not allowed to login until bluetooth is disconnected!");
            return;
        }
        String userName = etUserName.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (userName.isEmpty()) {
            Utility.showAlertMsg(getString(R.string.user_name_alert));
        } else if (password.isEmpty()) {
            Utility.showAlertMsg(getString(R.string.password_alert));
        } else if (Utility.user1.getUserName().equalsIgnoreCase(etUserName.getText().toString().trim()) && Utility.user1.isOnScreenFg()) {
            Toast.makeText(getActivity(), "This User is already logged in", Toast.LENGTH_SHORT).show();
        } else {
            // check user credential
            boolean status = LoginDB.LoginUser(userName, password, coDriver);
            if (status) {
                try {
                    UserBean user = null;//Utility.user1.isOnScreenFg() ? Utility.user1 : Utility.user2;


                    user = coDriver ? Utility.user2 : Utility.user1;

                    if (user.getTrainedFg() == 0) {
                        Utility.showAlertMsg("You are not certified by Hutch Trainer. Please contact hutch support to schedule ELD training!");
                    }

                    if (user.getTimeZoneId() != null && !user.getTimeZoneId().isEmpty()) {
                        Utility.TimeZoneId = user.getTimeZoneId();
                        Utility.TimeZoneOffset = ZoneList.getOffset(Utility.TimeZoneId);
                        Utility.TimeZoneOffsetUTC = ZoneList.getTimeZoneOffset(Utility.TimeZoneId);
                        Utility.sdf.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
                    }
                } catch (Exception exe) {

                }

                SharedPreferences prefs = getActivity().getSharedPreferences("HutchGroup", getActivity().MODE_PRIVATE);
                boolean logSent = prefs.getBoolean("logfile_sent", false);
                if (!logSent) {
                    LogFile.sendLogFile(LogFile.AFTER_MID_NIGHT);
                }
                boolean userConfigured = prefs.getBoolean("user_configured", false);

                // get driverId for record
                int driverId = coDriver ? Utility.user2.getAccountId() : Utility.user1.getAccountId();
                // set on screen userid
                Utility.onScreenUserId = driverId;
                prefs.edit().putInt("onscreenuserid", driverId).commit();
                Utility.activeUserId = Utility.user2.isActive() ? Utility.user2.getAccountId() : Utility.user1.getAccountId();
                if (userConfigured) {
                    DailyLogDB.DailyLogUserPreferenceRuleSave(driverId, UserPreferences.getCurrentRule(), Utility.getCurrentDateTime(), Utility.getCurrentDateTime());
                    prefs.edit().putBoolean("user_configured", false).commit();
                }

                // we'll have to fetch setting on each time switching user as driver and codriver may have different settings
                getSettings(driverId);
                if (Utility.user1.isOnScreenFg()) {
                    Utility.user1.setOnScreenFg(false);
                    Utility.user2.setOnScreenFg(true);
                } else {
                    Utility.user1.setOnScreenFg(true);
                    Utility.user2.setOnScreenFg(false);
                }

                try {
                    //HourOfService.InvokeRule(Utility.newDate(), Utility.onScreenUserId);
                    SharedPreferences sp = getActivity().getSharedPreferences("HutchGroup", getActivity().MODE_PRIVATE);
                    int shippingDriverId = sp.getInt("driverid", -1);
                    if (shippingDriverId == Utility.user1.getAccountId() || shippingDriverId == Utility.user2.getAccountId()) {
                        Utility.ShippingNumber = sp.getString("shipping_number", "");
                        Utility.TrailerNumber = sp.getString("trailer_number", "");
                    } else {
                        Utility.ShippingNumber = "";
                        Utility.TrailerNumber = "";
                    }
                } catch (Exception exe) {
                }
                if (firstLogin) {
                    Utility.LastEventDate = EventDB.getLastEventDate(Utility.onScreenUserId);

                    // get engine hour and odometer reading since engine power on.
                    EventDB.getEngineHourOdometerSincePowerOn(driverId);
                    if (ConstantFlag.ELDFg) {
                        int logId = DailyLogDB.DailyLogCreate(driverId, Utility.ShippingNumber, Utility.TrailerNumber, "");
                        // create event on login
                        EventDB.EventCreate(Utility.getCurrentDateTime(), 5, 1, getString(R.string.login_event_description), 1, 1, logId, driverId, "", MainActivity.currentRule);
                    }

                    if (coDriver) {
                        Utility.saveLoginInfo(Utility.user1.getAccountId(), driverId, Utility.activeUserId, Utility.onScreenUserId);
                        // add codriver record
                        DailyLogDB.AddDriver(Utility.user1.getAccountId(), driverId, 0);

                        MessageBean bean = MessageDB.CreateMessage(Utility.IMEI, Utility.user2.getAccountId(), Utility.user2.getAccountId(), "Connect");
                        MessageDB.Send(bean);
                    } else {
                        Utility.saveLoginInfo(driverId, 0, Utility.activeUserId, Utility.onScreenUserId);

                        //connect to server and make driver online
                        if (ChatClient.in == null) {
                            // connect to chat server
                            ChatClient.connect();
                            ChatClient.checkConnection();

                        } else {
                            // make driver online on chat server if server is already connected
                            MessageBean bean = MessageDB.CreateMessage(Utility.IMEI, Utility.user1.getAccountId(), Utility.user1.getAccountId(), "Connect");
                            MessageDB.Send(bean);
                        }


                    }

                }


                int acceptLicense = coDriver ? Utility.user2.getLicenseAcceptFg() : Utility.user1.getLicenseAcceptFg();

                if (acceptLicense == 0) {
                    Intent i = new Intent(getContext(), FirstTimeUser.class);
                    startActivityForResult(i, 1);
                } else {
                    mListener.loginSuccessfully(firstLogin);

                }

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("E-Log");
                builder.setIcon(R.drawable.ic_launcher);
                builder.setMessage(Utility.errorMessage);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.cancel();
                    }
                });
                alertDialog = builder.show();

                TextView messageView = (TextView) alertDialog.findViewById(android.R.id.message);
                messageView.setGravity(Gravity.CENTER);
                Utility.errorMessage = "";
            }
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 25 March 2020
    // Purpose: get setting by driverID
    private void getSettings(int driverId) {

        SettingsBean bean = SettingsDB.getSettings(driverId);
        Utility._appSetting = bean;

        // setting from user table
        int dEditFg = driverId == Utility.user1.getAccountId() ? Utility.user1.getdEditFg() : Utility.user2.getdEditFg();

        // company admin has revoked the DEdit setting
        if (dEditFg == 0 && bean.getdEditFg() == 1) {

            SettingsDB.update("EditFg", "0", driverId);
            Utility._appSetting.setdEditFg(0);
        }

    }


    SyncData.PostTaskListener<Boolean> syncDataPostTaskListener = new SyncData.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            showLoaderAnimation(false);
        }
    };

    @Override
    public void driverOnELD(final int driverId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String userName = "Unidentified";

                    // if driver is not unidentified
                    if (driverId != Utility.unIdentifiedDriverId) {
                        userName = UserDB.getUserName(driverId);
                    }

                    tvCurrentDriver.setText("Current Driver: " + userName);
                } catch (Exception exe) {

                }
            }
        });
    }


    public interface OnFragmentInteractionListener {
        void loginSuccessfully(boolean firstLogin);

        void autologinSuccessfully();

        void backFromLogin();

        void updateFlagbar(boolean status);

        void onAlertVehicleStart();

        void onAlertClear();

        void switchToAllUnitNo();

        void setProtocol();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {
            mListener.loginSuccessfully(firstLogin);
        } else {
            if (Utility.user1.isOnScreenFg()) {
                Utility.user1.setOnScreenFg(false);
                Utility.user2.setOnScreenFg(true);
            } else {
                Utility.user1.setOnScreenFg(true);
                Utility.user2.setOnScreenFg(false);
            }
            Utility.showAlertMsg(getString(R.string.term_condition_login));
        }
    }

    private void rebootDevice() {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"});
            proc.waitFor();
        } catch (Exception exe) {
            Utility.showAlertMsg(exe.getMessage());
        }
    }

}
