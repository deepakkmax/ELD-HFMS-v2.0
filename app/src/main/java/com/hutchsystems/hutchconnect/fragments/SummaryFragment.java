package com.hutchsystems.hutchconnect.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.SetupCompleteAdapter;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.PostCall;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.CarrierInfoDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.tasks.MailSync;

public class SummaryFragment extends Fragment implements View.OnClickListener {
    final String TAG = SummaryFragment.class.getName();

    OnFragmentInteractionListener mListener;
    TextView tvAppVersion;
    Button butProceed;

    AlertDialog alertDialog;
    AlertDialog warningDialog;

    AsyncTask mailAsyncTask;
    MailSync.PostTaskListener<Boolean> mailPostTaskListener = new MailSync.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            if (result) {
                Log.i("Summary", "Mail successfully");
                sendInstallerDetail();
            } else {
                Log.i("Summary", "Mail failed");
                callError();
            }
        }
    };

    public SummaryFragment() {
    }

    ListView lvData;

    private void initialize(View view) {
        try {
            lvData = (ListView) view.findViewById(R.id.lvData);
          /*  tvAppVersion = (TextView) view.findViewById(R.id.tvAppVersion);
            tvAppVersion.setText(getString(R.string.version) + " " + Utility.ApplicationVersion);*/
            String[] dataItems = {"Internet Connection Check", "Hutch Systems Connection Check", "Downloading Configuration", "Bluetooth Check", "BTB Connection Check", "BTB Heartbeat Check", "GPS Satellite Check", "UTC Time Check", "GPS Coordinates Check", "Current Location Check"};
            SetupCompleteAdapter adapter = new SetupCompleteAdapter(getContext(), R.layout.setup_activity, dataItems);
            lvData.setAdapter(adapter);
            butProceed = (Button) view.findViewById(R.id.butProceed);
            butProceed.setOnClickListener(this);
        } catch (Exception e) {
            LogFile.write(SummaryFragment.class.getName() + "::initialize Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            LogDB.writeLogs(SummaryFragment.class.getName(),"initialize",e.getMessage(), Utility.printStackTrace(e));

        }
    }

    public static SummaryFragment newInstance() {
        SummaryFragment fragment = new SummaryFragment();
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
        View view = inflater.inflate(R.layout.activity_setup_complete, container, false);
        initialize(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.butProceed:
                sendMail();
                break;
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
        mListener = null;
        if (mailAsyncTask != null) {
            mailAsyncTask.cancel(true);
        }
    }

    public void sendMail() {
        if (getActivity() != null) {
            if ((warningDialog != null && warningDialog.isShowing())) {
                return;
            }
            warningDialog = new AlertDialog.Builder(getActivity()).create();
            warningDialog.setCancelable(false);
            warningDialog.setCanceledOnTouchOutside(false);
            warningDialog.setTitle("E-Log");
            warningDialog.setIcon(R.drawable.ic_launcher);
            warningDialog.setMessage("Posting installation Detail to Hutch Server");
            warningDialog.show();
        }
        sendInstallerDetail();

       /* SharedPreferences prefs = getActivity().getSharedPreferences("HutchGroup", getActivity().getBaseContext().MODE_PRIVATE);
        String installerID = prefs.getString("installer_id", "");
        String content = "Installer ID: " + installerID + "\n";
        content += "Protocol: " + Utility.getPreferences("protocol_supported", "") + "\n" + "Odometer Source: " + Utility.getPreferences("odometer_source", -1);
        mailAsyncTask = new MailSync(mailPostTaskListener, false).execute("Installation completed", content);*/
    }

    public void sendInstallerDetail() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String installerID = Utility.getPreferences("installer_id", "");
                String date = Utility.getCurrentDateTime();

                // update setupfg to 1 and syncfg to 0
                CarrierInfoDB.updateSetupFlag();

                // Post Odometer source and Protocol
                PostCall.PostSetupinfo();

                PostCall.POSTInstallationDetail(date, installerID);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callNextScreen();
                    }
                });
            }
        }).start();
    }

    public void callNextScreen() {
        warningDialog.dismiss();

// update setupfg to 1 and syncfg to 0
        CarrierInfoDB.updateSetupFlag();
       
        if (mListener != null) {

            mListener.proceedToELD();
        }
    }

    public void callError() {
        if (getActivity() != null) {
            warningDialog.dismiss();
            if ((alertDialog != null && alertDialog.isShowing())) {
                return;
            }
            alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setTitle("E-Log");
            alertDialog.setIcon(R.drawable.ic_launcher);
            alertDialog.setMessage("Cannot connect to Hutch Systems! Please try again!");
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Retry",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendMail();
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void proceedToELD();
    }

}
