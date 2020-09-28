package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.dashboard.DashboardWithEngineHourActivity;
import com.hutchsystems.hutchconnect.dashboard.DashboardWithGauageCluster;
import com.hutchsystems.hutchconnect.dashboard.DashboardWithGraphActivity;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.SettingsDB;

public class TabDisplayFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, ColorPickerDialog.ColorPickerInterface {
    final String TAG = TabDisplayFragment.class.getName();

    int driverId = 0;
    RadioButton rbMetric, rbImperial, rbGaugeCluster, rbNavigation;
    TextView tvGraphLine;
    ConstraintLayout layoutCanadaColor;
    ConstraintLayout layoutUsColor;
    Switch switchViolationOnGrid, switchAutoStatusChange, switchViolationOnDrivingScreen;
    TextView tvOrientation;
    TextView tvVisionMode;
    Switch switchShowViolation, switchEventDetail, switchDEdit;

    SeekBar seekBrightness;

    //we dont support to change font size
    Spinner spinFontSize;
    Spinner spinGraphLine;
    Spinner spinVisionMode;

    RadioButton rbBluetooth;
    RadioButton rbUSB;

    RadioButton rbKms;
    RadioButton rbMiles;

    Button butCanadaColor;
    Button butUSColor;

    LinearLayout layoutOrientation, layoutAutoStatusChange;

    int canadaColor;
    int usColor;

    int currentFontSize;
    Spinner spinnerDrivingScreenDisplay;
    ColorPickerDialog colorPickerDialog;
    ColorPickerDialog usColorPickerDialog;
    private RadioButton rbHosWithGraph, rbHosWithEngineHour, rbGauageClusterDisplay;
    private Button btnPreview;

    public TabDisplayFragment() {
        // Required empty public constructor
    }

    private void initialize(View view) {
        try {

            switchEventDetail = view.findViewById(R.id.switchEventDetail);
            switchEventDetail.setOnCheckedChangeListener(this);
            boolean eventDetail = Utility.getPreferences("event_detail", false);
            switchEventDetail.setChecked(eventDetail);

            switchDEdit = view.findViewById(R.id.switchDEdit);
            int dEditFg = Utility.onScreenUserId == Utility.user1.getAccountId() ? Utility.user1.getdEditFg() : Utility.user2.getdEditFg();

            if (dEditFg == 1) {
                switchDEdit.setChecked(Utility._appSetting.getdEditFg() == 1);
                switchDEdit.setOnCheckedChangeListener(this);
            } else {
                switchDEdit.setEnabled(false);
            }

            spinnerDrivingScreenDisplay = (Spinner) view.findViewById(R.id.spinnerDrivingScreenDisplay);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, getResources().getStringArray(R.array.screen_dispplay));
            spinnerDrivingScreenDisplay.setAdapter(adapter);
            spinnerDrivingScreenDisplay.setSelection(Utility._appSetting.getDashboardDesign());
            spinnerDrivingScreenDisplay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.i("position", String.valueOf(i));
                    Utility._appSetting.setDashboardDesign(i);
                    SettingsDB.CreateSettings();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            // View Dashboard
            btnPreview = (Button) view.findViewById(R.id.btnPreview);
            btnPreview.setOnClickListener(this);

            layoutOrientation = (LinearLayout) view.findViewById(R.id.layoutOrientation);
            layoutAutoStatusChange = (LinearLayout) view.findViewById(R.id.layoutAutoStatusChange);

            layoutAutoStatusChange.setVisibility(View.GONE);

            tvGraphLine = (TextView) view.findViewById(R.id.tvGraphLine);
            layoutCanadaColor = (ConstraintLayout) view.findViewById(R.id.layoutCanadaColor);
            layoutCanadaColor.setOnClickListener(this);
            layoutUsColor = (ConstraintLayout) view.findViewById(R.id.layoutUSColor);
            layoutUsColor.setOnClickListener(this);
            switchViolationOnGrid = (Switch) view.findViewById(R.id.switchViolationOnGrid);
            switchViolationOnGrid.setChecked(Utility._appSetting.getViolationOnGrid() == 1 ? true : false);
            switchViolationOnGrid.setOnCheckedChangeListener(this);


            switchViolationOnDrivingScreen = (Switch) view.findViewById(R.id.switchViolationOnDrivingScreen);
            switchViolationOnDrivingScreen.setChecked(Utility._appSetting.getViolationOnDrivingScreen() == 1 ? true : false);
            switchViolationOnDrivingScreen.setOnCheckedChangeListener(this);

            switchAutoStatusChange = (Switch) view.findViewById(R.id.switchAutoStatusChange);
            switchAutoStatusChange.setOnCheckedChangeListener(this);

            int autoStatusChange = Utility.getPreferences("auto_status_change", Utility._appSetting.getAutoChangeStatus());
            Utility._appSetting.setAutoChangeStatus(autoStatusChange);
            switchAutoStatusChange.setChecked(autoStatusChange == 1 ? true : false);

            rbImperial = (RadioButton) view.findViewById(R.id.rbImperial);
            rbImperial.setOnCheckedChangeListener(this);
            rbImperial.setChecked(Utility._appSetting.getUnit() == 2);

            rbMetric = (RadioButton) view.findViewById(R.id.rbMetric);
            rbMetric.setOnCheckedChangeListener(this);
            rbMetric.setChecked(Utility._appSetting.getUnit() == 1);

            rbKms = (RadioButton) view.findViewById(R.id.rbKms);
            rbKms.setOnCheckedChangeListener(this);
            rbKms.setChecked(Utility._appSetting.getVehicleOdometerUnit() == 1);
            rbKms.setEnabled(false);

            rbMiles = (RadioButton) view.findViewById(R.id.rbMiles);
            rbMiles.setOnCheckedChangeListener(this);
            rbMiles.setChecked(Utility._appSetting.getVehicleOdometerUnit() == 2);
            rbMiles.setEnabled(false);


            rbBluetooth = (RadioButton) view.findViewById(R.id.rbBluetooth);
            rbBluetooth.setOnCheckedChangeListener(this);
            rbBluetooth.setChecked(Utility._appSetting.getECUConnectivity() == 1);

            rbUSB = (RadioButton) view.findViewById(R.id.rbUSB);
            rbUSB.setOnCheckedChangeListener(this);
            rbUSB.setEnabled(false);
            rbUSB.setChecked(Utility._appSetting.getECUConnectivity() == 2);


            rbGaugeCluster = (RadioButton) view.findViewById(R.id.rbGaugeCluster);
            rbGaugeCluster.setOnCheckedChangeListener(this);
            rbGaugeCluster.setChecked(Utility._appSetting.getDrivingScreen() == 0);


            rbNavigation = (RadioButton) view.findViewById(R.id.rbNavigation);
            rbNavigation.setOnCheckedChangeListener(this);
            rbNavigation.setChecked(Utility._appSetting.getDrivingScreen() == 1);

            tvVisionMode = (TextView) view.findViewById(R.id.tvVisionMode);

            currentFontSize = Utility._appSetting.getFontSize();

            spinFontSize = (Spinner) view.findViewById(R.id.spinnerFontSize);
            spinFontSize.setSelection(currentFontSize);
            spinFontSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.i(TAG, "Item font size selected");

                    if (position != currentFontSize) {
                        Utility._appSetting.setFontSize(position);
                        SettingsDB.CreateSettings();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinGraphLine = (Spinner) view.findViewById(R.id.spinnerGraphLine);
            spinGraphLine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Utility._appSetting.setGraphLine(position);
                    SettingsDB.CreateSettings();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spinGraphLine.setSelection(Utility._appSetting.getGraphLine());

            spinVisionMode = (Spinner) view.findViewById(R.id.spinnerVisionMode);
            spinVisionMode.setSelection(Utility._appSetting.getVisionMode());
            spinVisionMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Utility._appSetting.setVisionMode(position);
                    SettingsDB.CreateSettings();
                    if (mListener != null)
                        mListener.setVisionMode();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            switchShowViolation = (Switch) view.findViewById(R.id.switchShowViolation);
            switchShowViolation.setOnCheckedChangeListener(this);
            switchShowViolation.setChecked(Utility._appSetting.getShowViolation() == 1 ? true : false);

            seekBrightness = (SeekBar) view.findViewById(R.id.seekBrightness);
            seekBrightness.setMax(255);
            int brightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            seekBrightness.setProgress(brightness);
            seekBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                    Settings.System.putInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
//                    android.provider.Settings.System.putInt(getContext().getContentResolver(),
//                            android.provider.Settings.System.SCREEN_BRIGHTNESS, progress);

                    WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                    lp.screenBrightness = (float) progress / 255; //...and put it here
                    getActivity().getWindow().setAttributes(lp);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            butCanadaColor = (Button) view.findViewById(R.id.butCanadaColor);
            butCanadaColor.setOnClickListener(this);
            butUSColor = (Button) view.findViewById(R.id.butUSColor);
            butUSColor.setOnClickListener(this);
            canadaColor = Utility._appSetting.getColorLineCanada(); //use same value in ColorPickerDialog

            usColor = Utility._appSetting.getColorLineUS(); //use same value in ColorPickerDialog

            butCanadaColor.setBackgroundColor(canadaColor);
            butUSColor.setBackgroundColor(usColor);

//            // Dashboard Screens
//            rbHosWithGraph = (RadioButton) view.findViewById(R.id.rbHosWithGraph);
//            rbHosWithGraph.setOnCheckedChangeListener(this);
//            rbHosWithGraph.setChecked(Utility._appSetting.getDashboardDesign() == 0);
//
//
//            rbHosWithEngineHour = (RadioButton) view.findViewById(R.id.rbHosWithEngineHour);
//            rbHosWithEngineHour.setOnCheckedChangeListener(this);
//            rbHosWithEngineHour.setChecked(Utility._appSetting.getDashboardDesign() == 1);
//
//
//            rbGauageClusterDisplay = (RadioButton) view.findViewById(R.id.rbGauageClusterDisplay);
//            rbGauageClusterDisplay.setOnCheckedChangeListener(this);
//            rbGauageClusterDisplay.setChecked(Utility._appSetting.getDashboardDesign() == 2);

        } catch (Exception e) {
            LogFile.write(TabDisplayFragment.class.getName() + "::initialize error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TabDisplayFragment.class.getName(), "initialize", e.getMessage(), Utility.printStackTrace(e));

        }

    }

    public static TabDisplayFragment newInstance() {
        TabDisplayFragment fragment = new TabDisplayFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i("Settings", "onCreateView Display");
        View view = inflater.inflate(R.layout.tab_fragment_display, container, false);
        initialize(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {

                case R.id.layoutCanadaColor:
                case R.id.butCanadaColor:
                    if (colorPickerDialog == null) {
                        colorPickerDialog = new ColorPickerDialog();
                        colorPickerDialog.mListener = this;
                    }
                    colorPickerDialog.setInitialColor(canadaColor);
                    colorPickerDialog.setColorType(0);
                    colorPickerDialog.show(getFragmentManager(), "canada_color_dlg");
                    break;
                case R.id.layoutUSColor:
                case R.id.butUSColor:
                    if (usColorPickerDialog == null) {
                        usColorPickerDialog = new ColorPickerDialog();
                        usColorPickerDialog.mListener = this;
                    }
                    usColorPickerDialog.setInitialColor(usColor);
                    usColorPickerDialog.setColorType(1);
                    usColorPickerDialog.show(getFragmentManager(), "us_color_dlg");
                    break;
                case R.id.btnPreview:
                    // Dashboard With Hos and Graph
                    if (Utility._appSetting.getDashboardDesign() == 0) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), DashboardWithGraphActivity.class);
                        intent.putExtra("NighMode", Utility._appSetting.getVisionMode() == 1);
                        intent.putExtra("FromMainActivity", false);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        // Dashboard With Hos and Engine Hours
                    } else if (Utility._appSetting.getDashboardDesign() == 1) {
                        Intent intent = new Intent(getActivity(), DashboardWithEngineHourActivity.class);
                        intent.putExtra("NighMode", Utility._appSetting.getVisionMode() == 1);
                        intent.putExtra("FromMainActivity", false);

                        startActivity(intent);
                        //dashboardListner.ShowDashboardWithEngineHours();
                    }
                    // Dashboard With Gauage Cluster
                    else if (Utility._appSetting.getDashboardDesign() == 2) {
                        Intent intent = new Intent(getActivity(), DashboardWithGauageCluster.class);
                        intent.putExtra("FromMainActivity", false);
                        startActivity(intent);

                    }
            }
        } catch (Exception e) {
            LogFile.write(TabDisplayFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            switch (buttonView.getId()) {
                case R.id.switchEventDetail:
                    Utility.savePreferences("event_detail", switchEventDetail.isChecked());
                    break;
                case R.id.rbMetric: // Canada
                    if (isChecked) {
                        Utility._appSetting.setUnit(1);
                        SettingsDB.CreateSettings();
                    }
                    break;
                case R.id.rbImperial: // US
                    if (isChecked) {
                        Utility._appSetting.setUnit(2);
                        SettingsDB.CreateSettings();
                    }
                    break;
                case R.id.rbKms: // Canada
                    if (isChecked) {
                        Utility._appSetting.setVehicleOdometerUnit(1);
                        SettingsDB.CreateSettings();
                    }
                    break;
                case R.id.rbMiles: // US
                    if (isChecked) {
                        Utility._appSetting.setVehicleOdometerUnit(2);
                        SettingsDB.CreateSettings();
                    }
                    break;
                case R.id.rbBluetooth: // Canada
                    if (isChecked) {
                        Utility._appSetting.setECUConnectivity(1);

                        Utility.savePreferences("ecu_connectivity", Utility._appSetting.getECUConnectivity());
                    }
                    break;
                case R.id.rbUSB: // US
                    if (isChecked) {
                        Utility._appSetting.setECUConnectivity(2);

                        Utility.savePreferences("ecu_connectivity", Utility._appSetting.getECUConnectivity());
                    }
                    break;
                case R.id.rbGaugeCluster:
                    if (isChecked) {
                        Utility._appSetting.setDrivingScreen(0);
                        SettingsDB.CreateSettings();
                    }
                    break;
                case R.id.rbNavigation:
                    if (isChecked) {
                        Utility._appSetting.setDrivingScreen(1);
                        SettingsDB.CreateSettings();
                    }
                    break;
                case R.id.switchShowViolation:

                    Utility._appSetting.setShowViolation(isChecked ? 1 : 0);
                    SettingsDB.CreateSettings();
                    break;
                case R.id.switchViolationOnGrid:
                    Utility._appSetting.setViolationOnGrid(isChecked ? 1 : 0);
                    SettingsDB.CreateSettings();
                    break;
                case R.id.switchViolationOnDrivingScreen:
                    Utility._appSetting.setViolationOnDrivingScreen(isChecked ? 1 : 0);
                    SettingsDB.CreateSettings();
                    break;
                case R.id.switchAutoStatusChange:
                    Utility._appSetting.setAutoChangeStatus(isChecked ? 1 : 0);
                    Utility.savePreferences("auto_status_change", Utility._appSetting.getAutoChangeStatus());
                    break;
                case R.id.switchDEdit:
                    int dEditFg = (isChecked ? 1 : 0);
                    Utility._appSetting.setdEditFg(dEditFg);
                    SettingsDB.update("DEditFg", dEditFg + "", Utility.onScreenUserId);

                    // show message while dEdit is enable
                    if (isChecked) {
                        Utility.showAlertMsg("This function must only be used when faced technical difficulty. You must keep it disabled under normal circumstances. ELD will not require edit comment during this function.");
                    }
                    break;

            }

            Log.i(TAG, "violation reading = " + AppSettings.getViolationReading());
            Log.i(TAG, "copy trailer = " + AppSettings.getCopyTrailer());
            //change screen orientation by selection from user
            //need permission WRITE_SETTINGS for this feature
          /*  if (Utility.isLargeScreen(getContext())) {
                if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.PORTRAIT.ordinal()) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.LANSCAPE.ordinal()) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.AUTO.ordinal()) {
                    Settings.System.putInt(getActivity().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            } else {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }*/

            Log.i(TAG, "orientation = " + Utility._appSetting.getOrientation());
        } catch (Exception e) {
            LogFile.write(TabDisplayFragment.class.getName() + "::onCheckedChanged Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TabDisplayFragment.class.getName(), "onCheckedChanged", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void colorChanged(int color, int colorType) {
        try {
            if (colorType == 0) {
                canadaColor = color;
                butCanadaColor.setBackgroundColor(color);
                Utility._appSetting.setColorLineCanada(canadaColor);
            } else {
                usColor = color;
                butUSColor.setBackgroundColor(color);
                Utility._appSetting.setColorLineUS(usColor);
            }
            SettingsDB.CreateSettings();
        } catch (Exception e) {
            LogFile.write(TabDisplayFragment.class.getName() + "::colorChanged Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TabDisplayFragment.class.getName(), "colorChanged", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    IDisplaySetting mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IDisplaySetting) {
            mListener = (IDisplaySetting) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface IDisplaySetting {
        void setVisionMode();
    }

}