package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.UserBean;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.UserDB;

import java.io.File;

public class DriverProfileFragment extends Fragment  implements SignatureDialogFragment.OnFragmentInteractionListener  {
    TextView tvUserName, tvName, tvLicenseNo, tvJurisdiction, tvLicenseExpiry, tvExemptFg, tvSpecialCategory, tvEmail, tvMobileNo, tvDOTPassword;

    private OnFragmentInteractionListener mListener;
    Button btnAddSignature;
    ImageView imgSignature;
    public DriverProfileFragment() {

    }


    public static DriverProfileFragment newInstance() {
        return new DriverProfileFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_profile, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {
        tvUserName = (TextView) view.findViewById(R.id.tvUserName);
        tvName = (TextView) view.findViewById(R.id.tvName);
        tvLicenseNo = (TextView) view.findViewById(R.id.tvLicenseNo);
        tvJurisdiction = (TextView) view.findViewById(R.id.tvJurisdiction);
        tvLicenseExpiry = (TextView) view.findViewById(R.id.tvLicenseExpiry);
        tvExemptFg = (TextView) view.findViewById(R.id.tvExemptFg);
        tvSpecialCategory = (TextView) view.findViewById(R.id.tvSpecialCategory); //0-none,1-PU,2-YM,3 Both
        tvEmail = (TextView) view.findViewById(R.id.tvEmail);
        tvMobileNo = (TextView) view.findViewById(R.id.tvMobileNo);
        tvDOTPassword = (TextView) view.findViewById(R.id.tvDOTPassword);
        UserBean user = UserDB.userInfoGet(Utility.onScreenUserId);

        tvUserName.setText(user.getUserName());
        tvName.setText(user.getFirstName() + " " + user.getLastName());
        tvLicenseNo.setText(user.getDrivingLicense());
        tvJurisdiction.setText(user.getDlIssueState());
        tvLicenseExpiry.setText(user.getLicenseExpiryDate());
        tvExemptFg.setText(user.getExemptELDUseFg() == 0 ? getString(R.string.no_exemption) : getString(R.string.exempt_user));
        tvDOTPassword.setText(user.getDotPassword());
        String specialCategory;
        switch (user.getSpecialCategory()) {
            case "0":
                specialCategory = getString(R.string.none);
                break;
            case "1":
                specialCategory = getString(R.string.personaluse);
                break;
            case "2":
                specialCategory = getString(R.string.yard_move_long);
                break;
            case "3":
                specialCategory =getString(R.string.personaluse)+", "+ getString(R.string.yard_move_long);
                break;
            default:
                specialCategory = getString(R.string.none);
                break;
        }
        tvSpecialCategory.setText(specialCategory);
        tvEmail.setText(user.getEmailId());
        tvMobileNo.setText(user.getMobileNo());
        imgSignature = (ImageView) view.findViewById(R.id.imgSignature);
        btnAddSignature = (Button) view.findViewById(R.id.btnAddSignature);
        btnAddSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchSignature();
            }
        });
        drawBackground();
    }
    private void drawBackground() {
        try {
            String filePath = Utility.GetSignaturePath();
            File file = new File(filePath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                imgSignature.setBackground(bitmapDrawable);
                imgSignature.setVisibility(View.VISIBLE);
                btnAddSignature.setText("Update Signature");
            } else {
                btnAddSignature.setVisibility(View.VISIBLE);
                btnAddSignature.setText("Add Signature");
            }
        } catch (Exception exe) {
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
            LogDB.writeLogs(DriverProfileFragment.class.getName(),"::launchSignature Error:" ,e.getMessage(),Utility.printStackTrace(e));
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
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        void onSignatureUpload(String data, String path);
    }

    @Override
    public void onSignatureUpload(String data, String path) {
        drawBackground();
        mListener.onSignatureUpload(data, path);
    }
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        try {
//            //lvEvents.removeHeaderView(inforHeader);
//            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            View view = inflater.inflate(R.layout.fragment_driver_profile, null);
//
//            ViewGroup viewGroup = (ViewGroup) getView();
//
//            viewGroup.removeAllViews();
//            viewGroup.addView(view);
//            initialize(view);
//        } catch (Exception e) {
//        }
//    }
}
