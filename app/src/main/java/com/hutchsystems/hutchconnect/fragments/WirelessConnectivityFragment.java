package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.GetCall;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.LoginDB;
import com.hutchsystems.hutchconnect.util.AnimationUtil;


public class WirelessConnectivityFragment extends Fragment implements View.OnClickListener {
    final String TAG = WirelessConnectivityFragment.class.getName();

    private OnFragmentInteractionListener mListener;

    TextView tvInternet, tvHutchConnection, tvDownloadConfiguration, tvVehicle, tvUnidentifiedDriver, tvBTBMacAddress, tvAppVersion;

    ImageView icInternet, ivHutchConnection, ivDownloadConfiguration, icVehicle, icUnidentified, icBTBMacAddress;

    ImageButton butTryAgain;

    public WirelessConnectivityFragment() {

    }


    public static WirelessConnectivityFragment newInstance() {

        WirelessConnectivityFragment fragment = new WirelessConnectivityFragment();
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
        View view = inflater.inflate(R.layout.activity_wireless_connectivity_check, container, false);

        initialize(view);

        return view;
    }


    private void initialize(View view) {
        try {
            tvAppVersion = (TextView) view.findViewById(R.id.tvAppVersion);
            tvAppVersion.setText(getString(R.string.version) + " " + Utility.ApplicationVersion);
            tvInternet = (TextView) view.findViewById(R.id.tvInternet);
            tvHutchConnection = (TextView) view.findViewById(R.id.tvHutchConnection);
            tvDownloadConfiguration = (TextView) view.findViewById(R.id.tvDownloadConfiguration);
            tvVehicle = (TextView) view.findViewById(R.id.tvVehicle);
            tvUnidentifiedDriver = (TextView) view.findViewById(R.id.tvUnidentifiedDriver);
            tvBTBMacAddress = (TextView) view.findViewById(R.id.tvBTBMacAddress);

            icInternet = (ImageView) view.findViewById(R.id.icInternet);
            ivHutchConnection = (ImageView) view.findViewById(R.id.icHutchConnectionChecking);
            ivDownloadConfiguration = (ImageView) view.findViewById(R.id.icDownloadConfiguration);

            icVehicle = (ImageView) view.findViewById(R.id.icVehicle);
            icUnidentified = (ImageView) view.findViewById(R.id.icUnidentified);
            icBTBMacAddress = (ImageView) view.findViewById(R.id.icBTBMacAddress);


            butTryAgain = (ImageButton) view.findViewById(R.id.btnWirelessConnectionTryAgain);
            butTryAgain.setOnClickListener(this);
            butTryAgain.callOnClick();

        } catch (Exception e) {
            LogFile.write(WirelessConnectivityFragment.class.getName() + "::initialize Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            LogDB.writeLogs(WirelessConnectivityFragment.class.getName(),"initialize",e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void resetControlState() {

        tvDownloadConfiguration.setVisibility(View.GONE);
        ivDownloadConfiguration.setVisibility(View.GONE);

        tvHutchConnection.setVisibility(View.GONE);
        ivHutchConnection.setVisibility(View.GONE);

        tvInternet.setVisibility(View.GONE);
        icInternet.setVisibility(View.GONE);

        tvVehicle.setVisibility(View.GONE);
        icVehicle.setVisibility(View.GONE);

        tvUnidentifiedDriver.setVisibility(View.GONE);
        icUnidentified.setVisibility(View.GONE);

        tvBTBMacAddress.setVisibility(View.GONE);
        icBTBMacAddress.setVisibility(View.GONE);

        butTryAgain.setVisibility(View.GONE);
    }

    private void startProcess() {

        resetControlState();
        new HutchConnectivitySync().execute();
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {

                case R.id.btnWirelessConnectionTryAgain:
                    startProcess();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(WirelessConnectivityFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            LogDB.writeLogs(WirelessConnectivityFragment.class.getName(),"onClick",e.getMessage(), Utility.printStackTrace(e));

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
            LogFile.write(WirelessConnectivityFragment.class.getName() + "::onDetach Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            LogDB.writeLogs(WirelessConnectivityFragment.class.getName(),"onDetach",e.getMessage(), Utility.printStackTrace(e));

        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onNextFromWirelessConnectivity();
        void onFinishSetup();
    }

    private class HutchConnectivitySync extends AsyncTask<Void, Void, Boolean> {

        public HutchConnectivitySync() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            startAnimation(icInternet, tvInternet);

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean status = checkNetworkConnectivity();
            if (status) {
                // check hutch service with vehicle record
                status = GetCall.CarrierInfoSync();
                // update flags for results
                updateServiceStatus(status);

                if (status) {
                    // download configuration
                    GetCall.AccountSync(0);

                    Utility.unIdentifiedDriverId = LoginDB.getUnidentifiedDriverId();

                    status = updateConfigurationStatus();
                }
            }

            return status;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            super.onPostExecute(status);
            if (status) {
                if (mListener != null) {
                    mListener.onFinishSetup();
                }
            } else {
                butTryAgain.setVisibility(View.VISIBLE);
            }
        }
    }


    private boolean checkNetworkConnectivity() {
        final boolean isInternet = Utility.isInternetOn();
        try {
            Thread.sleep(100);
        } catch (Exception exe) {
        }

        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {

                        icInternet.setBackgroundResource(isInternet ? R.drawable.ic_setup_passed : R.drawable.ic_setup_failed);
                        tvInternet.setTextColor(getResources().getColor(isInternet ? R.color.green2 : R.color.red1));

                        if (isInternet) {
                            startAnimation(ivHutchConnection, tvHutchConnection);
                        }
                    } catch (Exception exe) {

                    }
                }
            });
        return (isInternet);
    }

    private void updateServiceStatus(final boolean isHutchConnected) {

        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {

                        ivHutchConnection.setBackgroundResource(isHutchConnected ? R.drawable.ic_setup_passed : R.drawable.ic_setup_failed);
                        tvHutchConnection.setTextColor(getResources().getColor(isHutchConnected ? R.color.green2 : R.color.red1));

                        if (isHutchConnected) {
                            startAnimation(ivDownloadConfiguration, tvDownloadConfiguration);
                        }
                    } catch (Exception exe) {

                    }
                }
            });
    }

    private boolean updateConfigurationStatus() {

        final boolean vehicleStatus = Utility.vehicleId > 0;
        final boolean unidentifiedStatus = Utility.unIdentifiedDriverId > 0;
        final boolean macStatus = Utility.MACAddress != null && !Utility.MACAddress.isEmpty();
        final boolean status = unidentifiedStatus && vehicleStatus && macStatus;
        try {

            if (getActivity() != null)
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        icVehicle.setVisibility(View.VISIBLE);
                        tvVehicle.setVisibility(View.VISIBLE);
                    }
                });

            Thread.sleep(100);

            if (getActivity() != null)
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        tvVehicle.setTextColor(getResources().getColor(vehicleStatus ? R.color.green2 : R.color.red1));
                        icVehicle.setBackgroundResource(vehicleStatus ? R.drawable.ic_setup_passed : R.drawable.ic_setup_failed);

                        icUnidentified.setVisibility(View.VISIBLE);
                        tvUnidentifiedDriver.setVisibility(View.VISIBLE);
                    }
                });

            Thread.sleep(100);

            if (getActivity() != null)
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        tvUnidentifiedDriver.setTextColor(getResources().getColor(unidentifiedStatus ? R.color.green2 : R.color.red1));
                        icUnidentified.setBackgroundResource(unidentifiedStatus ? R.drawable.ic_setup_passed : R.drawable.ic_setup_failed);

                        icBTBMacAddress.setVisibility(View.VISIBLE);
                        tvBTBMacAddress.setVisibility(View.VISIBLE);
                    }
                });

            Thread.sleep(100);

            if (getActivity() != null)
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        tvBTBMacAddress.setTextColor(getResources().getColor(macStatus ? R.color.green2 : R.color.red1));
                        icBTBMacAddress.setBackgroundResource(macStatus ? R.drawable.ic_setup_passed : R.drawable.ic_setup_failed);
                        /*if (Utility.unIdentifiedDriverId == 0) {

                            Utility.showAlertMsg(getString(R.string.unidentified_driver_record_missing));
                        }*/

                        ivDownloadConfiguration.setBackgroundResource(status ? R.drawable.ic_setup_passed : R.drawable.ic_setup_failed);
                        tvDownloadConfiguration.setTextColor(getResources().getColor(status ? R.color.green2 : R.color.red1));
                    }
                });
        } catch (Exception exe) {

        }
        return status;
    }

    private void startAnimation(ImageView imgView, TextView tvView) {
        tvView.setVisibility(View.VISIBLE);
        imgView.setVisibility(View.VISIBLE);

        tvView.setTextColor(getResources().getColor(R.color.yellow2));
        AnimationUtil.startSetupProcessingAnimation(getResources(), imgView);
    }

}
