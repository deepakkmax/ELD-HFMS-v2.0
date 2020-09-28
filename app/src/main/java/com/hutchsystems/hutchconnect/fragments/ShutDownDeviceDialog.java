package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;


public class ShutDownDeviceDialog extends DialogFragment {

    public OnFragmentInteractionListener mListener;

    private CountDownTimer countDownTimer;
    private TextView tvCountdownView;
    private TextView tvMessage;

    private boolean bShowing;

    public ShutDownDeviceDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shut_down_device_dialog, container, false);
        try {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            this.setCancelable(false);
            tvCountdownView = (TextView) view.findViewById(R.id.countdownTimer);
            tvMessage = (TextView) view.findViewById(R.id.dialogMessage);
            bShowing = true;
        } catch (Exception e) {
            LogFile.write(PopupDialog.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(PopupDialog.class.getName(),"onCreateView",e.getMessage(), Utility.printStackTrace(e));

        }
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            countDownTimer = new CountDownTimer(3 * 60 * 1000, 1000) {
                @Override
                public void onTick(long l) {
                    int numLeft = ((int) Math.round(l / 1000.0) - 1);
                    String strNum = Utility.getTimeInMinuteFromSeconds(numLeft);
                    tvCountdownView.setText(strNum);
                    if (Float.valueOf(CanMessages.RPM) > 0f) {

                        dismiss();
                    }
                }

                @Override
                public void onFinish() {
                    bShowing = false;
                    shutdownDevice();
                    dismiss();
                }
            };
            countDownTimer.start();
        } catch (Exception e) {
            LogFile.write(PopupDialog.class.getName() + "::onActivityCreated Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(PopupDialog.class.getName(),"onActivityCreated",e.getMessage(), Utility.printStackTrace(e));

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
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        mListener = null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        tvCountdownView = null;

        mListener = null;

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

    }


    private void shutdownDevice() {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot -p"});
            proc.waitFor();
        } catch (Exception exe) {
            Utility.showAlertMsg(exe.getMessage());
        }
    }
}
