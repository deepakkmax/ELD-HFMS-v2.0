package com.hutchsystems.hutchconnect.fragments;


import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.RuleBean;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hoursofservice.common.TimeUtility;
import com.hutchsystems.hoursofservice.model.DutyStatus;

import java.util.ArrayList;
import java.util.Date;

public class RuleChangeDialog extends DialogFragment implements View.OnClickListener {

    public RuleChangeDialogInterface mListener;
    private final int CANADA_RULE_1 = 1;
    private final int CANADA_RULE_2 = 2;
    private final int US_RULE_1 = 3;
    private final int US_RULE_2 = 4;
    private final int CANADA_RULE_AB = 5;
    private final int CANADA_LOGGING_TRUCK = 6;
    private final int CANADA_OIL_WELL_SERVICE = 7;

    ColorStateList ruleColorStateList;

    LinearLayout layoutCanadaRule1;
    LinearLayout layoutCanadaRule2;
    LinearLayout layoutCanadaRuleAB;

    LinearLayout layoutLoggingTruck;
    LinearLayout layoutOilWellService;


    LinearLayout layoutUSRule1;
    LinearLayout layoutUSRule2;

    TextView tvCanadaRule1;
    TextView tvLoggingTruck;
    TextView tvOilWellService;

    TextView tvCanadaRule2;
    TextView tvCanadaRuleAB;

    TextView tvUSRule1;
    TextView tvUSRule2;

    Button butSave;

    ImageButton imgCancel;
    int currentRule = 1;


    public RuleChangeDialog() {

    }

    public void setCurrentRule(int value) {
        currentRule = value;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_rule_change_, container);
        try {
            ruleColorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_selected}, //selected
                            new int[]{android.R.attr.state_enabled}, //un-selected
                    },
                    new int[]{
                            ContextCompat.getColor(getContext(), R.color.white), //1
                            ContextCompat.getColor(getContext(), R.color.colorPrimary) //2
                    }
            );

            //Intent intent = getIntent();
            //currentRule = intent.getIntExtra("current_rule", 1);

            initialize(view);
            //getDialog().setTitle("Hutch ELD supported rules");
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            this.setCancelable(false);


        } catch (Exception e) {
            LogFile.write(RuleChangeDialog.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(RuleChangeDialog.class.getName(), "onCreateView", e.getMessage(), Utility.printStackTrace(e));

        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {

        } catch (Exception e) {
            LogFile.write(RuleChangeDialog.class.getName() + "::onActivityCreated Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(RuleChangeDialog.class.getName(), "onActivityCreated", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    private void initialize(View view) {
        try {
            layoutCanadaRule1 = (LinearLayout) view.findViewById(R.id.layoutCanadaRule1);
            layoutCanadaRule1.setOnClickListener(this);
            layoutCanadaRule2 = (LinearLayout) view.findViewById(R.id.layoutCanadaRule2);
            layoutCanadaRule2.setOnClickListener(this);

            layoutCanadaRuleAB = (LinearLayout) view.findViewById(R.id.layoutCanadaRuleAB);
            layoutCanadaRuleAB.setOnClickListener(this);


            layoutLoggingTruck = (LinearLayout) view.findViewById(R.id.layoutLoggingTruck);
            layoutLoggingTruck.setOnClickListener(this);


            layoutOilWellService = (LinearLayout) view.findViewById(R.id.layoutOilWellService);
            layoutOilWellService.setOnClickListener(this);

            layoutUSRule1 = (LinearLayout) view.findViewById(R.id.layoutUSRule1);
            layoutUSRule1.setOnClickListener(this);
            layoutUSRule2 = (LinearLayout) view.findViewById(R.id.layoutUSRule2);
            layoutUSRule2.setOnClickListener(this);

            tvCanadaRule1 = (TextView) view.findViewById(R.id.tvCanadaRule1);
            tvCanadaRule2 = (TextView) view.findViewById(R.id.tvCanadaRule2);
            tvCanadaRuleAB = (TextView) view.findViewById(R.id.tvCanadaRuleAB);


            tvLoggingTruck = (TextView) view.findViewById(R.id.tvLoggingTruck);
            tvOilWellService = (TextView) view.findViewById(R.id.tvOilWellService);

            tvUSRule1 = (TextView) view.findViewById(R.id.tvUSRule1);
            tvUSRule2 = (TextView) view.findViewById(R.id.tvUSRule2);

            imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
            imgCancel.setOnClickListener(this);
            butSave = (Button) view.findViewById(R.id.butRuleSave);
            butSave.setOnClickListener(this);

            butSave.setEnabled(false);

            clearRules();

            switch (currentRule) {
                case 1:
                    layoutCanadaRule1.setSelected(true);
                    tvCanadaRule1.setSelected(true);
                    break;
                case 2:
                    layoutCanadaRule2.setSelected(true);
                    tvCanadaRule2.setSelected(true);
                    break;
                case 3:
                    layoutUSRule1.setSelected(true);
                    tvUSRule1.setSelected(true);
                    break;
                case 4:
                    layoutUSRule2.setSelected(true);
                    tvUSRule2.setSelected(true);
                    break;
                case 5:
                    layoutCanadaRuleAB.setSelected(true);
                    tvCanadaRuleAB.setSelected(true);
                    break;

                case 6:
                    layoutLoggingTruck.setSelected(true);
                    tvLoggingTruck.setSelected(true);
                    break;

                case 7:
                    layoutOilWellService.setSelected(true);
                    tvOilWellService.setSelected(true);
                    break;
            }
        } catch (Exception e) {
            LogFile.write(RuleChangeDialog.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(RuleChangeDialog.class.getName(), "initialize", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.layoutCanadaRule1:
                    if (!layoutCanadaRule1.isSelected()) {
                        clearRules();
                        layoutCanadaRule1.setSelected(true);
                        tvCanadaRule1.setSelected(true);
                        butSave.setEnabled(true);
                        currentRule = CANADA_RULE_1;
                    }
                    break;
                case R.id.layoutCanadaRule2:
                    if (!layoutCanadaRule2.isSelected()) {
                        clearRules();
                        layoutCanadaRule2.setSelected(true);
                        tvCanadaRule2.setSelected(true);
                        butSave.setEnabled(true);
                        currentRule = CANADA_RULE_2;
                    }
                    break;
                case R.id.layoutCanadaRuleAB:
                    if (!layoutCanadaRuleAB.isSelected()) {
                        clearRules();
                        layoutCanadaRuleAB.setSelected(true);
                        tvCanadaRuleAB.setSelected(true);
                        butSave.setEnabled(true);
                        currentRule = CANADA_RULE_AB;
                    }
                    break;

                case R.id.layoutLoggingTruck:
                    if (!layoutLoggingTruck.isSelected()) {
                        clearRules();
                        layoutLoggingTruck.setSelected(true);
                        tvLoggingTruck.setSelected(true);
                        butSave.setEnabled(true);
                        currentRule = CANADA_LOGGING_TRUCK;
                    }
                    break;

                case R.id.layoutOilWellService:
                    if (!layoutOilWellService.isSelected()) {
                        clearRules();
                        layoutOilWellService.setSelected(true);
                        tvOilWellService.setSelected(true);
                        butSave.setEnabled(true);
                        currentRule = CANADA_OIL_WELL_SERVICE;
                    }
                    break;
                case R.id.layoutUSRule1:
                    if (!layoutUSRule1.isSelected()) {
                        clearRules();
                        layoutUSRule1.setSelected(true);
                        tvUSRule1.setSelected(true);
                        butSave.setEnabled(true);
                        currentRule = US_RULE_1;
                    }
                    break;
                case R.id.layoutUSRule2:
                    if (!layoutUSRule2.isSelected()) {
                        clearRules();
                        layoutUSRule2.setSelected(true);
                        tvUSRule2.setSelected(true);
                        butSave.setEnabled(true);
                        currentRule = US_RULE_2;
                    }
                    break;
                case R.id.imgCancel:

                    dismiss();
                    break;
                case R.id.butRuleSave:
                    if (mListener != null) {

                        boolean status = ruleValidationGet(currentRule);
                        if (status) {
                            mListener.onSavedRule(currentRule);
                        } else {
                            Utility.showAlertMsg(errorMessage);
                        }
                    }

                    dismiss();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(RuleChangeDialog.class.getName() + "::onClick Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(RuleChangeDialog.class.getName(), "onClick", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    // error message
    String errorMessage = "";

    // Created By: Deepak Sharma
    // Created Date: 27 March 2019
    // Purpose: to check if driver has taken mandatory reset to switch cycle
    private boolean ruleValidationGet(int rule) {

        boolean status = true;
        RuleBean previousRule = DailyLogDB.getPreviousCanadaRule(Utility.onScreenUserId);
        // if driver rule is of canada
        if (rule <= 2 || previousRule.getRuleId() == 7) {

            double requiredBreak = 0;
            // if current rule selected by driver is of cycle 1and previous rule
            if (rule == 1 && previousRule.getRuleId() == 2) {
                // if driver wants to switch from cycle 2 to cycle 1 he should have 72 hours of break.
                requiredBreak = 72 * 60 * 60d;
                errorMessage = getContext().getString(R.string.B_29_title);
            } else if (rule == 2 && previousRule.getRuleId() == 1) {
                // if driver wants to switch from cycle 2 to cycle 1 he should have 36 hours of break.
                requiredBreak = 36 * 60 * 60d;
                errorMessage = getContext().getString(R.string.A_29_title);
            } else if (previousRule.getRuleId() == 7) {
                // the driver takes at least 72 consecutive hours of off-duty time after the completion of driving in accordance with this subsection and before beginning to follow a cycle in accordance with section 37.16.02, 37.16.03 or 37.16.04.
                requiredBreak = 72 * 60 * 60d;
                errorMessage = "Driver must take at least 72 consecutive hours of off-duty time to change this rule";
            }

            // if driver is switching cycle
            if (requiredBreak > 0) {
                Date previousRuleDate = previousRule.getRuleStartTime();
                ArrayList<DutyStatus> list = MainActivity.dutyStatusArrayList;

                double reset = 0;
                // check if driver has taken required reset to switch cycle
                for (int i = 0; i < list.size(); i++) {
                    DutyStatus item = list.get(i);

                    // if event date is before previous rule date then continue to next event
                    if (item.getEventDateTime().before(previousRuleDate)) {
                        continue;
                    }

                    // declare variable to hold next duty status
                    DutyStatus nextItem = null;

                    // if next duty status is same then increase value of i till we get differnt next duty status.
                    while (i < list.size() - 1 && list.get(i + 1).getStatus() == item.getStatus()) { //&& list.get(i + 1).getRule() == item.getRule()
                        i++;
                    }

                    // pick next Duty Status if exists
                    nextItem = i == list.size() - 1 ? null : list.get(i + 1);

                    // Date of duty status
                    Date eventDate = item.getEventDateTime();

                    // Date of next duty status if it exists else put to Date in next date
                    Date nextDate = nextItem == null ? Utility.newDate() : nextItem.getEventDateTime();

                    // Duration of duty status
                    //  double duration = nextDate.Subtract(eventDate).TotalSeconds;
                    double duration = TimeUtility.getDiff(eventDate, nextDate, TimeUtility.Unit.SECONDS);

                    // if sleeper or on duty
                    reset = (item.getStatus() <= 2 ? (reset + duration) : 0);

                    // if required reset found then break the loop
                    if (reset >= requiredBreak)
                        break;

                }

                // if required break is missing
                if (reset < requiredBreak)
                    status = false;


            }
        }
        return status;
    }

    private void clearRules() {
        try {
            layoutCanadaRule1.setSelected(false);
            layoutCanadaRule2.setSelected(false);
            layoutCanadaRuleAB.setSelected(false);
            layoutLoggingTruck.setSelected(false);
            layoutOilWellService.setSelected(false);
            layoutUSRule1.setSelected(false);
            layoutUSRule2.setSelected(false);

            tvCanadaRule1.setSelected(false);
            tvCanadaRule2.setSelected(false);
            tvCanadaRuleAB.setSelected(false);
            tvLoggingTruck.setSelected(false);
            tvOilWellService.setSelected(false);
            tvUSRule1.setSelected(false);
            tvUSRule2.setSelected(false);

            tvCanadaRule1.setTextColor(ruleColorStateList);
            tvCanadaRule2.setTextColor(ruleColorStateList);
            tvCanadaRuleAB.setTextColor(ruleColorStateList);

            tvLoggingTruck.setTextColor(ruleColorStateList);
            tvOilWellService.setTextColor(ruleColorStateList);

            tvUSRule1.setTextColor(ruleColorStateList);
            tvUSRule2.setTextColor(ruleColorStateList);
        } catch (Exception e) {
            LogFile.write(RuleChangeDialog.class.getName() + "::clearRules Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(RuleChangeDialog.class.getName(), "clearRules Error", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    public interface RuleChangeDialogInterface {
        void onSavedRule(int rule);
    }
}
