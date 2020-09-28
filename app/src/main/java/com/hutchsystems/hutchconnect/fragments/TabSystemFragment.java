package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.common.ZoneList;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.SettingsDB;

import java.util.ArrayList;

public class TabSystemFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, RuleChangeDialog.RuleChangeDialogInterface {
    final String TAG = TabSystemFragment.class.getName();
    int driverId = 0;
    int currentRuleIdx;
    ArrayList<String> listRules;
    TextView tvDefaultRule;
    TextView tvRule;
    TextView tvTimeFromat;
    Button btnSystemInformation;
    TextView tvLegal;
    TextView tvTimeZone, tvTimeZoneName;
    Switch switchCopyTrailer, switchChangeUnitOnRule, switchAutoRuleChange,switchNewUSSplit;
//    RadioButton rbEnglish,rbPunjabi,rbSpanish,rbFrench;

    RadioGroup rgSelectTimeFormat;

    RadioButton rbTime12;
    RadioButton rbTime24;
    Button  btnProtocol;

    LinearLayout layoutOrientation;


    SettingsFragment settingsFragment;
    LegalDialog legalDialog;

    private OnFragmentInteractionListener mListener;
    private RadioButton rbSygic, rbtext, rbgoogleMap;
    private Switch switchEnableSplitSleep;
    private Switch switchShowAlertSplitSleep;
    Spinner spinnerLanguage;


    public TabSystemFragment() {
    }

    public void setSettingsFragment(SettingsFragment frag) {
        settingsFragment = frag;
    }


    private void initialize(View view) {
        try {
            layoutOrientation = (LinearLayout) view.findViewById(R.id.layoutOrientation);

            spinnerLanguage = (Spinner) view.findViewById(R.id.spinnerLanguage);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, getResources().getStringArray(R.array.language_list));
            spinnerLanguage.setAdapter(adapter);

            spinnerLanguage.setSelection(Utility._appSetting.getSupportLanguage()-1);
            spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.i("position", String.valueOf(i));
                    Utility._appSetting.setSupportLanguage(i + 1);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            tvDefaultRule = (TextView) view.findViewById(R.id.tvDefaultRule);
            tvDefaultRule.setOnClickListener(this);
            tvRule = (TextView) view.findViewById(R.id.tvRule);
            tvRule.setOnClickListener(this);

            tvTimeFromat = (TextView) view.findViewById(R.id.tvTimeFormat);
            rgSelectTimeFormat = (RadioGroup) view.findViewById(R.id.rgSelectTimeFormat);
            rbTime12 = (RadioButton) view.findViewById(R.id.rbTime12);
            rbTime12.setOnCheckedChangeListener(this);
            rbTime24 = (RadioButton) view.findViewById(R.id.rbTime24);
            rbTime24.setOnCheckedChangeListener(this);
            if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR12.ordinal()) {
                rbTime12.setChecked(true);
            } else {
                rbTime24.setChecked(true);
            }

            btnProtocol = (Button) view.findViewById(R.id.btnProtocol);
            btnProtocol.setOnClickListener(this);

            rbSygic = (RadioButton) view.findViewById(R.id.rbSygic);
            rbSygic.setChecked(Utility._appSetting.getLocationSource() == 0);
            rbSygic.setOnCheckedChangeListener(this);

            rbtext = (RadioButton) view.findViewById(R.id.rbtext);
            rbtext.setChecked(Utility._appSetting.getLocationSource() == 1);
            rbtext.setOnCheckedChangeListener(this);


            rbgoogleMap = (RadioButton) view.findViewById(R.id.rbgoogleMap);
            rbgoogleMap.setChecked(Utility._appSetting.getLocationSource() == 2);
            rbgoogleMap.setOnCheckedChangeListener(this);


            btnSystemInformation = (Button) view.findViewById(R.id.btnSystemInformation);
            btnSystemInformation.setOnClickListener(this);
            tvLegal = (TextView) view.findViewById(R.id.tvLegal);
            tvLegal.setOnClickListener(this);
            tvTimeZone = (TextView) view.findViewById(R.id.tvTimeZone);
            tvTimeZone.setText(Utility.TimeZoneOffsetUTC);

            tvTimeZoneName = (TextView) view.findViewById(R.id.tvTimeZoneName);
            tvTimeZoneName.setText(ZoneList.getTimezoneName(true));

            switchCopyTrailer = (Switch) view.findViewById(R.id.switchCopyTrailer);
            switchCopyTrailer.setOnCheckedChangeListener(this);
            switchCopyTrailer.setChecked(Utility._appSetting.getCopyTrailer() == 1 ? true : false);

            switchAutoRuleChange = (Switch) view.findViewById(R.id.switchAutoRuleChange);
            switchAutoRuleChange.setOnCheckedChangeListener(this);
            switchAutoRuleChange.setChecked(Utility._appSetting.getAutomaticRuleChange() == 1 ? true : false);

            switchChangeUnitOnRule = (Switch) view.findViewById(R.id.switchChangeUnitOnRule);
            switchChangeUnitOnRule.setOnCheckedChangeListener(this);
            switchChangeUnitOnRule.setChecked(Utility._appSetting.getChangeUnitOnRuleChange() == 1 ? true : false);


            switchNewUSSplit = (Switch) view.findViewById(R.id.switchNewUSSplit);
            switchNewUSSplit.setOnCheckedChangeListener(this);
            switchNewUSSplit.setChecked(Utility._appSetting.getNewsplitSleepUSA() == 1 ? true : false);

            // purpose For Handling onCheckedChanged  Exception : Changed the  Sequence
            switchShowAlertSplitSleep = (Switch) view.findViewById(R.id.switchShowAlertSplitSleep);
            switchShowAlertSplitSleep.setOnCheckedChangeListener(this);
            switchShowAlertSplitSleep.setChecked(Utility._appSetting.getEnableSplitSlip() == 1 && Utility._appSetting.getShowAlertSplitSlip() == 1 ? true : false);

            // Switch for Split Slip
            switchEnableSplitSleep = (Switch) view.findViewById(R.id.switchEnableSplitSleep);
            switchEnableSplitSleep.setOnCheckedChangeListener(this);
            switchEnableSplitSleep.setChecked(Utility._appSetting.getEnableSplitSlip() == 1 ? true : false);

            getRules();
            driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();

            currentRuleIdx = Utility._appSetting.getDefaultRule(); //DailyLogDB.getCurrentRule(driverId);
            if (currentRuleIdx == 0) {
                currentRuleIdx = 1;
            }
            tvRule.setText(listRules.get(currentRuleIdx - 1));

        } catch (Exception e) {
            LogFile.write(TabSystemFragment.class.getName() + "::initialize error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(com.hutchsystems.hutchconnect.fragments.TabSystemFragment.class.getName(), "initialize", e.getMessage(), Utility.printStackTrace(e));
        }
    }


    public static TabSystemFragment newInstance() {
        TabSystemFragment fragment = new TabSystemFragment();
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
        Log.i("Settings", "onCreateView System");
        View view = inflater.inflate(R.layout.tab_fragment_system, container, false);
        initialize(view);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            //mListener.onFragmentInteraction(uri);
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach");
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
        Log.i(TAG, "Detach");
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.tvRule:
                case R.id.tvDefaultRule:
                    if (!Utility.HutchConnectStatusFg) {
                        Utility.showAlertMsg("App does not allow to change status while bluetooth is disconnected!");
                        return;
                    }

                    launchRuleChange();
                    break;
                case R.id.btnProtocol:
                    if (mListener != null) {
                        mListener.setProtocol();
                    }
                    break;
                case R.id.tvLegal:
                    //show legal information
                    if (legalDialog == null) {
                        legalDialog = new LegalDialog();
                    }
                    if (legalDialog.isAdded()) {
                        break;
                    }
                    legalDialog.show(getFragmentManager(), "legal_dialog");
                    break;
                case R.id.btnSystemInformation:
                    showSystemInformation();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(TabSystemFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TabSystemFragment.class.getName(), "onClick", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            switch (buttonView.getId()) {
                case R.id.rbTime12:
                    if (isChecked) {
                        Utility._appSetting.setTimeFormat(AppSettings.AppTimeFormat.HR12.ordinal());
                        SettingsDB.CreateSettings();
                    }
                    break;
                case R.id.rbTime24:
                    if (isChecked) {
                        Utility._appSetting.setTimeFormat(AppSettings.AppTimeFormat.HR24.ordinal());
                        SettingsDB.CreateSettings();
                    }
                    break;
                case R.id.switchCopyTrailer:
                    Utility._appSetting.setCopyTrailer(isChecked ? 1 : 0);
                    SettingsDB.CreateSettings();
                    break;
                case R.id.switchChangeUnitOnRule:
                    Utility._appSetting.setChangeUnitOnRuleChange(isChecked ? 1 : 0);

                    Utility.savePreferences("change_unit_on_rule_change", Utility._appSetting.getChangeUnitOnRuleChange());
                    break;

                case R.id.switchNewUSSplit:
                    Utility._appSetting.setNewsplitSleepUSA(isChecked ? 1 : 0);

                    Utility.savePreferences("new_us_split_sleep_rule", Utility._appSetting.getNewsplitSleepUSA());
                    break;
                case R.id.switchAutoRuleChange:
                    Utility._appSetting.setAutomaticRuleChange(isChecked ? 1 : 0);
                    SettingsDB.CreateSettings();
                    break;
                case R.id.rbSygic:
                    if (isChecked) {

                        Utility.savePreferences("location_source", 0);
                        Utility._appSetting.setLocationSource(0);
                    }
                    break;
                case R.id.rbtext:
                    if (isChecked) {

                        Utility.savePreferences("location_source", 1);
                        Utility._appSetting.setLocationSource(1);
                    }
                    break;
                case R.id.rbgoogleMap:
                    if (isChecked) {

                        Utility.savePreferences("location_source", 2);
                        Utility._appSetting.setLocationSource(2);
                    }
                    break;
                case R.id.switchEnableSplitSleep:
                    if (isChecked) {
                        switchShowAlertSplitSleep.setEnabled(true);
                    } else {
                        switchShowAlertSplitSleep.setChecked(false);
                        switchShowAlertSplitSleep.setEnabled(false);
                    }
                    Utility._appSetting.setEnableSplitSlip(isChecked ? 1 : 0);
                    SettingsDB.CreateSettings();
                    break;
                case R.id.switchShowAlertSplitSleep:
                    Utility._appSetting.setShowAlertSplitSlip(isChecked ? 1 : 0);
                    SettingsDB.CreateSettings();
                    break;
//                case R.id.rbEnglish:
//                    if (isChecked) {
//                        Utility._appSetting.setSupportLanguage(1);
//                        UserDB.Update("LanguageForSupport", "1");
//                    }
//                    break;
//                case R.id.rbPunjabi:
//                    if (isChecked) {
//                        Utility._appSetting.setSupportLanguage(2);
//                        UserDB.Update("LanguageForSupport", "2");
//                    }
//                    break;
//                case R.id.rbSpanish:
//                    if (isChecked) {
//                        Utility._appSetting.setSupportLanguage(3);
//                        UserDB.Update("LanguageForSupport", "3");
//                    }
//                    break;
//                case R.id.rbFrench:
//                    if (isChecked) {
//                        Utility._appSetting.setSupportLanguage(4);
//                        UserDB.Update("LanguageForSupport", "4");
//                    }
//                    break;
            }

        } catch (Exception e) {
            LogFile.write(TabSystemFragment.class.getName() + "::onCheckedChanged Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TabSystemFragment.class.getName(), "onCheckedChanged", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void showSystemInformation() {
        if (settingsFragment != null) {
            settingsFragment.showSystemInformation();
        }
    }

    //these default rules should be saved in server,
    // it will help us expand the app easier than using default array in code like this
    private void getRules() {
        listRules = new ArrayList<String>();
        listRules.add(getResources().getString(R.string.canada_rule_1));
        listRules.add(getResources().getString(R.string.canada_rule_2));
        listRules.add(getResources().getString(R.string.us_rule_1));
        listRules.add(getResources().getString(R.string.us_rule_2));
        listRules.add(getResources().getString(R.string.canada_rule_AB));
        listRules.add(getResources().getString(R.string.canada_logging_truck));
        listRules.add(getResources().getString(R.string.canada_oil_well_service));
    }

    private void launchRuleChange() {
        try {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();

            Fragment prev = manager.findFragmentByTag("rulechange_dialog");
            if (prev != null) {
                ft.remove(prev);
                //ft.addToBackStack(null);
                ft.commitNow();
                ft = manager.beginTransaction();
            }

            RuleChangeDialog dialog = new RuleChangeDialog();
            dialog.mListener = this;
            dialog.setCurrentRule(currentRuleIdx);
            dialog.show(ft, "rulechange_dialog");
        } catch (Exception e) {
            //LogFile.write(ELogFragment.class.getName() + "::launchRuleChange Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onSavedRule(int rule) {
        try {
            if (rule != currentRuleIdx) {
                DailyLogDB.DailyLogRuleSave(Utility.onScreenUserId, rule, Utility._CurrentDateTime, Utility._CurrentDateTime);
                currentRuleIdx = rule;
                /*this logic for posting ruleid */
                // get dailylogid of current date to sync revert
                int dailyLogId = DailyLogDB.getDailyLog(driverId, Utility.getCurrentDate());

                DailyLogDB.DailyLogSyncRevert(Utility.onScreenUserId, dailyLogId);

                // Post rule
                MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo);
            }
            tvRule.setText(listRules.get(currentRuleIdx - 1));
            Utility._appSetting.setDefaultRule(currentRuleIdx);
            if (rule <= 2) {
                Utility._appSetting.setUnit(1);
            } else
                Utility._appSetting.setUnit(2);
            SettingsDB.CreateSettings();

            if (mListener != null) {
                mListener.changeRule(rule);
            }
        } catch (Exception e) {
            LogFile.write(TabSystemFragment.class.getName() + "::onSavedRule Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TabSystemFragment.class.getName(), "onSavedRule", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        //void onFragmentInteraction(Uri uri);
        void reportIssue();

        void changeRule(int rule);

        void setProtocol();

        void copyEldConfig();
    }

}