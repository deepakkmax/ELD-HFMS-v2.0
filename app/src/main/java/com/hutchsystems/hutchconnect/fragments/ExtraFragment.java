package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;


public class ExtraFragment extends Fragment implements View.OnClickListener, SignatureDialogFragment.OnFragmentInteractionListener {

    private OnFragmentInteractionListener mListener;
    ImageButton btnDTC, btnScoreCard, btnTrailerManagement, btnVehicleInfo, btnFuelDetail, btnIncidentDetail, btnMaintenance, btnDocument, btnSignature, btnHelp;
    private ImageButton btnNotification, btnInstall;
    private ImageButton btnTicketHistory;
    private ImageButton btnCTPATInspection;
    private ImageButton btnReferFriend, btnCheckBalance;
    LinearLayout llInstall;

    public ExtraFragment() {
        // Required empty public constructor
    }

    public static ExtraFragment newInstance() {
        ExtraFragment fragment = new ExtraFragment();
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
        View view = inflater.inflate(R.layout.fragment_extra_, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {

        btnTrailerManagement = (ImageButton) view.findViewById(R.id.btnTrailerManagement);
        btnTrailerManagement.setOnClickListener(this);

        btnDTC = (ImageButton) view.findViewById(R.id.btnDTC);
        btnDTC.setOnClickListener(this);

        btnScoreCard = (ImageButton) view.findViewById(R.id.btnScoreCard);
        btnScoreCard.setOnClickListener(this);
        btnFuelDetail = (ImageButton) view.findViewById(R.id.btnFuelDetail);
        btnFuelDetail.setOnClickListener(this);

        btnIncidentDetail = (ImageButton) view.findViewById(R.id.btnIncidentDetail);
        btnIncidentDetail.setOnClickListener(this);

        btnMaintenance = (ImageButton) view.findViewById(R.id.btnMaintenance);
        btnMaintenance.setOnClickListener(this);

        btnDocument = (ImageButton) view.findViewById(R.id.btnDocument);
        btnDocument.setOnClickListener(this);

        btnSignature = (ImageButton) view.findViewById(R.id.btnSignature);
        btnSignature.setOnClickListener(this);

        btnHelp = (ImageButton) view.findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(this);

        // For Notification List
        btnNotification = (ImageButton) view.findViewById(R.id.btnNotification);
        btnNotification.setOnClickListener(this);

        // For Ticket History
        btnTicketHistory = (ImageButton) view.findViewById(R.id.btnTicketHistory);
        btnTicketHistory.setOnClickListener(this);

        // For Refer a friend
        btnReferFriend = (ImageButton) view.findViewById(R.id.btnReferFriend);
        btnReferFriend.setOnClickListener(this);

        // For checking balance
        //btnCheckBalance = (ImageButton) view.findViewById(R.id.btnCheckBalance);
        //btnCheckBalance.setOnClickListener(this);

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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDTC:
                if (!Utility.hasFeature("5")) {
                    Utility.showAlertMsg(getString(R.string.feature_na_alert));
                    return;
                }

                break;
            case R.id.btnScoreCard:
                if (!Utility.hasFeature("13")) {
                    Utility.showAlertMsg(getString(R.string.feature_na_alert));
                    return;
                }
                break;
            case R.id.btnTrailerManagement:
                if (!Utility.hasFeature("9")) {
                    Utility.showAlertMsg(getString(R.string.feature_na_alert));
                    return;
                }
                break;
            case R.id.btnVehicleInfo:
                if (!Utility.hasFeature("11")) {
                    Utility.showAlertMsg(getString(R.string.feature_na_alert));
                    return;
                }
                mListener.onLoadVehicleInfo();
                break;
            case R.id.btnFuelDetail:
                if (!Utility.hasFeature("3")) {
                    Utility.showAlertMsg(getString(R.string.feature_na_alert));
                    return;
                }
                mListener.onLoadFuelDetail();
                break;
            case R.id.btnIncidentDetail:

                mListener.onLoadIncidentDetail();

                break;
            case R.id.btnMaintenance:
                if (!Utility.hasFeature("7")) {
                    Utility.showAlertMsg(getString(R.string.feature_na_alert));
                    return;
                }

                mListener.onLoadMaintenanceDetail();
                break;
            case R.id.btnNavigation:
                if (!Utility.hasFeature("8")) {
                    Utility.showAlertMsg(getString(R.string.feature_na_alert));
                    return;
                }
                break;
            case R.id.btnGeofence:

                if (Utility.packageId > 2) {
                }
                break;
            case R.id.btnDocument:
                mListener.onLoadDocumentDetailList();
                break;
            case R.id.btnSignature:
                launchSignature();
                break;
            case R.id.btnRule:
                mListener.onLoadRuleList();
                break;
            case R.id.btnHelp:
                mListener.onLoadHelp();
                break;
            case R.id.btnNotification:
                mListener.onLoadNotificationList();
                break;
            case R.id.btnTicketHistory:
                mListener.onLoadTicketHistory();
                break;
            case R.id.btnReferFriend:
                mListener.onReferFriend();
                break;
          /*  case R.id.btnCheckBalance:
                if (Utility.isInternetOn())
                    mListener.onCheckBalance();
                else Utility.showMsg("Please check internet connection");
                break;*/
        }
    }


    public void launchSignature() {
        try {

            FragmentManager manager = getFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();

            Fragment prev = manager.findFragmentByTag("signature_dialog");
            if (prev != null) {
                ft.remove(prev);
                //ft.addToBackStack(null);
                ft.commitNow();
                ft = manager.beginTransaction();
            }

            SignatureDialogFragment signatureDialog = SignatureDialogFragment.newInstance();
            signatureDialog.mListner = this;
            signatureDialog.show(ft, "signature_dialog");

        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::launchRuleChange Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ExtraFragment.class.getName(), "launchSignature", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onSignatureUpload(String data, String path) {
        mListener.onSignatureUpload(data, path);
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);


        void onLoadVehicleInfo();

        void onLoadFuelDetail();

        void onLoadNotificationList();

        void onSignatureUpload(String data, String path);

        void onLoadHelp();

        void onLoadRuleList();

        void onLoadTicketHistory();

        void onCheckBalance();

        void onReferFriend();

        void onLoadMaintenanceDetail();

        void onLoadIncidentDetail();
        void onLoadDocumentDetailList();
    }
}
