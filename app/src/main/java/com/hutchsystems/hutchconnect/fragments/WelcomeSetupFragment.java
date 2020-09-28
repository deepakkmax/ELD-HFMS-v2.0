package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.ConstantFlag;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;


public class WelcomeSetupFragment extends Fragment implements View.OnClickListener {
    final String TAG = WelcomeSetupFragment.class.getName();

    private OnFragmentInteractionListener mListener;

    TextView tvVersion, tvTitle, tvDescription;
    ImageButton butGo;
    EditText   edSerialNo;
    SharedPreferences prefs;



    private TextView tv_UniqueId;

    public WelcomeSetupFragment() {

    }



    private void initialize(View view) {
        try {



            edSerialNo = (EditText) view.findViewById(R.id.edSerialNo);
            if (ConstantFlag.HutchConnectFg) {

            } else {
                edSerialNo.setVisibility(View.GONE);
            }

            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            tvDescription = (TextView) view.findViewById(R.id.tvDescription);
            tvDescription.setVisibility(View.VISIBLE);




            tvVersion = (TextView) view.findViewById(R.id.tvAppVersion);

            tvVersion.setText(getString(R.string.version) + " " + Utility.ApplicationVersion);

            butGo = (ImageButton) view.findViewById(R.id.butGo);
            butGo.setVisibility(View.GONE);
            butGo.setOnClickListener(this);


            butGo.setVisibility(View.VISIBLE);
            edSerialNo.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int DRAWABLE_RIGHT = 2;
                    if(event.getAction() == MotionEvent.ACTION_UP) {
                        if(event.getRawX() >= (edSerialNo.getRight() - edSerialNo.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            // open barcode reader
                            IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
                            intentIntegrator.initiateScan();
                        }
                    }
                    return false;
                }
            });

        } catch (Exception e) {
            LogFile.write(WelcomeSetupFragment.class.getName() + "::initialize Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            LogDB.writeLogs(WelcomeSetupFragment.class.getName(), "onViolationClick", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    public static WelcomeSetupFragment newInstance() {
        WelcomeSetupFragment fragment = new WelcomeSetupFragment();
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
        View view = inflater.inflate(R.layout.activity_setup_welcome_screen, container, false);
        prefs = getActivity().getSharedPreferences("HutchGroup", getActivity().getBaseContext().MODE_PRIVATE);

        initialize(view);

        return view;
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.butGo:

                    Utility.hideKeyboard(getActivity(), view);

                    if (ConstantFlag.OBD2FG) {

                        Utility.savePreferences("engineMinute", Utility.engineMinute);
                        Utility.savePreferences("start_odometer_reading", String.format("%.2f", Utility.StartOdometerReading));
                        Utility.savePreferences("OBD2FG", ConstantFlag.OBD2FG);
                    }

                    Utility.savePreferences("is_live_server", ConstantFlag.LiveFg);
                    goToNextScreen();

                    break;
            }
        } catch (Exception e) {
            LogFile.write(WelcomeSetupFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            LogDB.writeLogs(WelcomeSetupFragment.class.getName(), "onClick", e.getMessage(), Utility.printStackTrace(e));

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
        try {
            mListener = null;

        } catch (Exception e) {
            LogFile.write(WelcomeSetupFragment.class.getName() + "::onDetach Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            LogDB.writeLogs(WelcomeSetupFragment.class.getName(), "onDetach", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void goToNextScreen() {
        String installerId = "10000001";

        if (installerId.equals("")) {
            Utility.showAlertMsg(getString(R.string.installer_id_empty_alert));
            return;
        }

        int id = Integer.parseInt(installerId);
        if (!checkInstallerId(id)) {
            Utility.showAlertMsg(getString(R.string.installer_id_valid_alert));
            return;
        }

        if (ConstantFlag.HutchConnectFg)
        {
            String serialNo = edSerialNo.getText().toString();
            if (serialNo.equals("")) {

                Utility.showAlertMsg(getString(R.string.hutch_connect_IMEI_empty_alert));
                return;
            }

            if (serialNo.length() != 15) {
                Utility.showAlertMsg(getString(R.string.hutch_connect_IMEI_valid_alert));
                return;
            }
            Utility.savePreferences("imei_no", serialNo);
            Utility.IMEI = serialNo;
        }

        Utility.savePreferences("installer_id", installerId);

        if (mListener != null) {
            mListener.onNextToWirelessConnectivity();
        }

    }

    private boolean checkInstallerId(int id) {
        boolean match = false;
        int initialValue = 10000000;
        for (int i = 1; i <= 1000; i++) {
            int autoId = initialValue + (i * i);
            if (id == autoId) {
                match = true;
                break;
            } else if (autoId > id) {
                break;
            }
        }
        return match;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onNextToWirelessConnectivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if (result.getContents() != null) {
                edSerialNo.setText(result.getContents());
                edSerialNo.setSelection(edSerialNo.getText().toString().length());
            }
        }
    }
}
