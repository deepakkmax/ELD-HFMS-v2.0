package com.hutchsystems.hutchconnect.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;

public class CountDownDialogFragment extends DialogFragment {

    Button btnConfirm, btnReject;
    ImageButton imgCancel;

    private CountDownTimer countDownTimer;
    private TextView tvCountdownView, tvMessage;
    public String message;
    public int minute = 5;

    public CountDownDialogFragment() {
        // Required empty public constructor
    }

    private void exitApp() {
        System.exit(0);
    }

    private void initialize(View view) {
        btnConfirm = (Button) view.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitApp();
            }
        });
        btnReject = (Button) view.findViewById(R.id.btnReject);
        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        tvCountdownView = (TextView) view.findViewById(R.id.tvCountdownView);
        tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);
    }

    public static CountDownDialogFragment newInstance() {
        CountDownDialogFragment fragment = new CountDownDialogFragment();
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
        View view = inflater.inflate(R.layout.fragment_count_down_dialog, container, false);
        initialize(view);
        return view;
    }

    // Created By: Sahil Bansal
    // purpose: dialog width match only for mobile
    // Date: 18 June 2020
    @Override
    public void onStart() {
        super.onStart();
        //if device is not tabler
        if (!Utility.isLargeScreen(getActivity())) {
            Dialog dialog = getDialog();
            if (dialog != null) {
                //dialog set width match for mobile
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                //set margin from sides
                int margin = 25;
                InsetDrawable inset = new InsetDrawable(new ColorDrawable(Color.TRANSPARENT), margin);
                dialog.getWindow().setBackgroundDrawable(inset);
            }
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            countDownTimer = new CountDownTimer(minute * 60 * 1000, 1000) {
                @Override
                public void onTick(long l) {
                    int numLeft = ((int) Math.round(l / 1000.0) - 1);

                    tvCountdownView.setText(Utility.getTimeInMinuteFromSeconds(numLeft));
                    if (Utility.motionFg) {
                        dismiss();
                    }
                }

                @Override
                public void onFinish() {
                    dismiss();
                    exitApp();
                }
            };
            countDownTimer.start();
        } catch (Exception e) {
            LogFile.write(PopupDialog.class.getName() + "::onActivityCreated Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(CountDownDialogFragment.class.getName(),"::onActivityCreated Error:" ,e.getMessage(),Utility.printStackTrace(e));

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tvCountdownView = null;

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}
