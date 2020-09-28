package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.InspectionCriteriaAdapter;
import com.hutchsystems.hutchconnect.beans.CTPATInspectionBean;
import com.hutchsystems.hutchconnect.beans.InspectionCriteriaBean;

import com.hutchsystems.hutchconnect.beans.UserBean;
import com.hutchsystems.hutchconnect.beans.VehicleBean;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.CTPATInspectionDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.db.TrailerDB;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.util.ArrayList;


public class CTPATInspectionFragment extends Fragment implements View.OnClickListener, SignatureDialogFragment.OnFragmentInteractionListener {

    private static final String XMLTAG_INSPECTIONS = "inspection";
    private ListView lvInspectionList;
    private InspectionCriteriaAdapter adapter;
    private TextView tvDriver;
    private TextView tvTrucknum, tvCompany;
    private Spinner spinnerTrailerValue;
    private TextView tvDate, tvTime;
    private EditText edComments;
    private ImageView imgSignature;
    private Button btnAddSignature;
    private ImageButton btnSave;
    private String driverName;
    private RadioGroup rbgType;
    int inspectionType = 0;
    private String truckNum;
    private boolean viewMode;
    private String company;
    private ArrayList<Object> trailerList = new ArrayList<>();
    private String currentDateTime;
    private OnFragmentInteractionListener mListener;
    private EditText edSealValue;
    View inforHeader;

    public CTPATInspectionFragment() {
        // Required empty public constructor
    }

    public static CTPATInspectionFragment newInstance() {
        CTPATInspectionFragment fragment = new CTPATInspectionFragment();
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
        View view = inflater.inflate(R.layout.fragment_ctpat_inspection, container, false);

        viewMode = false;
        if (getArguments() != null) {
            if (getArguments().getBoolean("view_mode", false)) {
                viewMode = true;
            }
        }

        try {

            lvInspectionList = (ListView) view.findViewById(R.id.lvInspectionList);
            inforHeader = inflater.inflate(R.layout.inspection_header, null, false);
            lvInspectionList.addHeaderView(inforHeader);

            // intialize view
            initialize(view);

            // Set data To View
            setDataToView();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    private void initialize(View view) {

        tvDriver = (TextView) view.findViewById(R.id.tvDriver);
        tvTrucknum = (TextView) view.findViewById(R.id.tvPowerUnit);
        tvCompany = (TextView) view.findViewById(R.id.tvCompany);
        // Spinner Trailer Value
        spinnerTrailerValue = (Spinner) view.findViewById(R.id.spinnerTrailerValue);
        // For Date
        tvDate = (TextView) view.findViewById(R.id.tvDate);
        edComments = (EditText) view.findViewById(R.id.edComments);
        edSealValue = (EditText) view.findViewById(R.id.edSealValue);
        imgSignature = (ImageView) view.findViewById(R.id.imgSignature);
        rbgType = (RadioGroup) view.findViewById(R.id.rbgType);

        btnAddSignature = (Button) view.findViewById(R.id.btnAddSignature);
        btnAddSignature.setOnClickListener(this);

        btnSave = (ImageButton) view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);

    }

    // Created By: Pallavi Wattamwar
    // Created Date: 4 July 2019
    // Purpose: check Mandatory field and save the inspection
    public void saveInspection() {
        try {
            JSONObject inspectionJson = new JSONObject();
            int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();

            // inspectionCheckList value is 3 means inspection criteria is not checked
            if (InspectionCriteriaAdapter.inspectionCheckList.contains(3)) {
                Toast.makeText(getContext(), "Please Check All Inspection Criteria", Toast.LENGTH_LONG).show();
                return;
            }

            if (btnAddSignature.getVisibility() == View.VISIBLE) {
                Utility.showMsg(getResources().getString(R.string.add_signature));
                return;
            }

            if (edSealValue.getText().toString().isEmpty()) {
                Utility.showMsg("Please Add Seal");
                return;
            }
            // get the value of selected answers from custom adapter
            for (int i = 0; i < InspectionCriteriaAdapter.inspectionCheckList.size(); i++) {

                inspectionJson.put("" + i, InspectionCriteriaAdapter.inspectionCheckList.get(i));
            }

            CTPATInspectionDB.CreateCTPATInspection(Utility.getDate(Utility.getCurrentDateTime()) + " " + Utility.getCurrentTime(), driverId, driverName, inspectionType, truckNum, (spinnerTrailerValue.getSelectedItem() != null && spinnerTrailerValue != null) ? spinnerTrailerValue.getSelectedItem().toString() : "", edComments.getText().toString(), edSealValue.getText().toString(), Utility.companyId, Utility.CarrierName, inspectionJson.toString());
            mListener.finishCTPATInspection();
        } catch (Exception e) {
            e.printStackTrace();
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

            // Clear the Signature befor opening signature dialog
            Bundle bunle = new Bundle();
            bunle.putBoolean("Clear", true);
            signatureDialog.setArguments(bunle);
            signatureDialog.mListner = this;
            signatureDialog.show(ft, "signature_dialog");

        } catch (Exception e) {
            LogFile.write(CTPATInspectionFragment.class.getName() + "::launchRuleChange Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(CTPATInspectionFragment.class.getName(), "launchSignature", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    public void setDataToView() {
        // new CTPAT Inspection
        if (!viewMode) {
            UserBean user;
            if (Utility.onScreenUserId == Utility.user1.getAccountId()) {
                user = Utility.user1;
            } else
                user = Utility.user2;
            driverName = user.getFirstName() + " " + user.getLastName();

            String plateNo = Utility.PlateNo == null || Utility.PlateNo.isEmpty() ? "" : " (" + Utility.PlateNo + ")";
            truckNum = Utility.UnitNo + plateNo;

            ArrayList<VehicleBean> hookedTrailers = TrailerDB.getHookedVehicleInfo();
            hookedTrailers.remove(0);
            for (VehicleBean bean : hookedTrailers) {
                trailerList.add(bean.getUnitNo());
            }

            currentDateTime = Utility.getDate(Utility.getCurrentDateTime()) + " " + Utility.getCurrentTime();

            rbgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.rbPre:
                            inspectionType = 0;
                            break;

                        case R.id.rbPreArrival:
                            inspectionType = 1;
                            break;

                        case R.id.rbPreArrivalBorder:
                            inspectionType = 2;
                            break;
                    }
                }
            });
            adapter = new InspectionCriteriaAdapter(getContext(), getInspectionCriteria(), "");
            lvInspectionList.setAdapter(adapter);


        } else {
            // View From List
            CTPATInspectionBean bean = (CTPATInspectionBean) getArguments().getSerializable("ctpat_inspection");
            driverName = bean.getDriverName();
            inspectionType = bean.getType();
            truckNum = bean.getTruckNumber();
            company = bean.getCompanyName();
            trailerList.add(bean.getTrailer());
            currentDateTime = bean.getDateTime();
            edComments.setText(bean.getComments());
            edComments.setEnabled(false);
            edSealValue.setText(bean.getSealValue());
            edSealValue.setEnabled(false);

            ((RadioButton) rbgType.getChildAt(inspectionType)).setChecked(true);
            for (View v : rbgType.getTouchables()) {
                v.setEnabled(false);
            }

            adapter = new InspectionCriteriaAdapter(getContext(), getInspectionCriteria(), bean.getInspectionCriteria());
            lvInspectionList.setAdapter(adapter);

            btnSave.setVisibility(View.GONE);
            spinnerTrailerValue.setEnabled(false);
            drawBackground();

        }

        tvDriver.setText(getString(R.string.driver) + ": " + driverName);
        tvTrucknum.setText(getResources().getString(R.string.power_unit) + ": " + truckNum);
        tvCompany.setText(getString(R.string.company) + ": " + Utility.CarrierName);
        ArrayAdapter trailerArray = new ArrayAdapter(Utility.context, R.layout.spinner_rule_item, trailerList);
        trailerArray.setDropDownViewResource(R.layout.spinner_rule_item);
        //Setting the ArrayAdapter data on the Spinner
        spinnerTrailerValue.setAdapter(trailerArray);
        tvDate.setText("DateTime :" + currentDateTime);
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
                btnAddSignature.setVisibility(View.GONE);

            } else {
                btnAddSignature.setVisibility(View.VISIBLE);
                imgSignature.setVisibility(View.GONE);
            }
        } catch (Exception exe) {
        }
    }

    // Created By: Pallavi Wattamwar
    // Created Date: 4 July 2019
    // Purpose: get Inspection Criteria List  from xml file
    public static ArrayList<InspectionCriteriaBean> getInspectionCriteria() {
        ArrayList<InspectionCriteriaBean> inspectionCriteriaList = new ArrayList<>();
        try {
            // XML Parsing
            XmlResourceParser xrp = Utility.context.getResources().getXml(R.xml.inspection);
            while (xrp.next() != XmlResourceParser.START_TAG)
                continue;
            xrp.next();
            while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                while (xrp.getEventType() != XmlResourceParser.START_TAG) {

                    if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
                        return inspectionCriteriaList;
                    }
                    xrp.next();
                }
                InspectionCriteriaBean bean = new InspectionCriteriaBean();
                if (xrp.getName().equals(XMLTAG_INSPECTIONS)) {
                    bean.setTitle(xrp.getAttributeValue(0));
                    bean.setSubTitle(xrp.nextText());
                    inspectionCriteriaList.add(bean);

                }
                while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                    xrp.next();
                }
                xrp.next();
            }
            xrp.close();
        } catch (XmlPullParserException xppe) {
        } catch (java.io.IOException ioe) {

        }

        return inspectionCriteriaList;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CTPATInspectionFragment.OnFragmentInteractionListener) {
            mListener = (CTPATInspectionFragment.OnFragmentInteractionListener) context;
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAddSignature:
                launchSignature();
                break;
            case R.id.btnSave:
                saveInspection();
                break;

        }

    }

    @Override
    public void onSignatureUpload(String data, String path) {
        drawBackground();
        mListener.onSignatureUpload(data, path);

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void finishCTPATInspection();

        void onSignatureUpload(String data, String path);
    }
}
