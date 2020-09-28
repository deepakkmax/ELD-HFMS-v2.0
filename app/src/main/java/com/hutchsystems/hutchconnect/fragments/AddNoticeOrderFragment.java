package com.hutchsystems.hutchconnect.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.IncidentBean;
import com.hutchsystems.hutchconnect.beans.UserBean;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.IncidentDB;
import com.hutchsystems.hutchconnect.scanapi.ScanActivity;

import java.io.File;

public class AddNoticeOrderFragment extends Fragment {

    TextView tvDriver, tvPowerUnit, tvTrailer, tvDuration;
    EditText etComment, etFineAmount, etReportNo;

    Spinner spLevel, spinnerResultNO, spinnerResultCVSA;
    ImageButton btnNewInspection;
    LinearLayout layoutLevel, layoutFine;
    Button btnAttach;

    boolean attachedFg = false;
    String _fileName = "", docType = "";
    public int id = 0, levelId = 0;
    int duration = 0;
    String incidentDateTime;
    private Thread thVehicleInfo;
    int type = 0; // 0- NoticeOrder, 1- CVSA, 2- violation
    String[] trailers = {"", ""};

    private void startThread() {
        if (thVehicleInfo != null) {
            thVehicleInfo.interrupt();
            thVehicleInfo = null;
        }
        duration = 0;
        thVehicleInfo = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (Thread.interrupted())
                            break;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvDuration.setText(Utility.getTimeFromMinute(duration));
                            }
                        });
                        Thread.sleep(1000 * 60);
                        duration++;
                    } catch (Exception exe) {
                        break;
                    }
                }
            }
        });

        thVehicleInfo.setName("thVehicleInfo");
        thVehicleInfo.start();
    }

    private void stopThread() {
        if (thVehicleInfo != null) {
            thVehicleInfo.interrupt();
            thVehicleInfo = null;
        }
    }

    int REQUEST_CODE = 99;

    private void initialize(View view) {
        btnAttach = (Button) view.findViewById(R.id.btnAttach);
        btnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (attachedFg) {
                    if (!_fileName.isEmpty()) {
                        File file = new File(_fileName);

                        if (file.exists()) {
                            Uri path = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", file);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(path, "application/pdf");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        }
                    }
                } else {

                    _fileName = Utility.GetDocumentName(docType);
                    // if Flag_Scan_Dynmsoft = true Document Scan using dynmsoft sdk
                    Intent intent = new Intent(getContext(), ScanActivity.class);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, _fileName);
                    startActivityForResult(intent, REQUEST_CODE);


                }
            }
        });
        layoutLevel = (LinearLayout) view.findViewById(R.id.layoutLevel);
        layoutFine = (LinearLayout) view.findViewById(R.id.layoutFine);
        tvDriver = (TextView) view.findViewById(R.id.tvDriver);
        tvPowerUnit = (TextView) view.findViewById(R.id.tvPowerUnit);
        tvTrailer = (TextView) view.findViewById(R.id.tvTrailer);
        tvDuration = (TextView) view.findViewById(R.id.tvDuration);

        spLevel = (Spinner) view.findViewById(R.id.spLevel);
        etFineAmount = (EditText) view.findViewById(R.id.etFineAmount);
        etReportNo = (EditText) view.findViewById(R.id.etReportNo);
        etComment = (EditText) view.findViewById(R.id.etComment);

        spinnerResultNO = (Spinner) view.findViewById(R.id.spinnerResultNO);
        spinnerResultCVSA = (Spinner) view.findViewById(R.id.spinnerResultCVSA);
        btnNewInspection = (ImageButton) view.findViewById(R.id.btnNewInspection);
        btnNewInspection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etReportNo.getText().toString().isEmpty()) {
                    Utility.showMsg(getString(R.string.PLEASE_ENTER_REPORT_NO));
                    return;
                }

                IncidentBean bean = new IncidentBean();
                bean.set_id(id);
                bean.setType(type);
                bean.setIncidentDate(incidentDateTime);

                bean.setReportNo(etReportNo.getText().toString());
                bean.setVehicleId(Utility.vehicleId);
                bean.setDriverId(Utility.onScreenUserId);
                bean.setDuration(duration);
                bean.setLevel(spLevel.getSelectedItemPosition());
                bean.setComment(etComment.getText().toString());
                int resultId = spinnerResultCVSA.getSelectedItemPosition();

                if (type == 0) {
                    resultId = spinnerResultNO.getSelectedItemPosition();
                    bean.setFineAmount("0");
                } else if (type == 1) {
                    bean.setFineAmount("0");
                } else {
                    if (etFineAmount.getText().toString().isEmpty()) {
                        Utility.showMsg(getString(R.string.PLEAE_ENTER_FINE_AMOUNT));
                        return;
                    }
                    bean.setFineAmount(etFineAmount.getText().toString());
                }
                if (resultId == 0) {
                    Utility.showMsg(getString(R.string.result_alert));
                    return;
                }
                bean.setResult(resultId - 1);
                if (attachedFg) {
                    String filename = _fileName.substring(_fileName.lastIndexOf("/") + 1);
                    bean.setDocPath(filename);
                } else
                    bean.setDocPath("");
                bean.setTrailerNo(trailers[1]);
                IncidentDB.Save(bean);
                if (mListener != null) {
                    mListener.onLoadIncidentDetail();

                    if (attachedFg)
                        mListener.onUploadDocument(docType, _fileName);
                }
            }
        });

        if (type == 0) {
            spinnerResultNO.setVisibility(View.VISIBLE);
            spinnerResultCVSA.setVisibility(View.GONE);
            layoutFine.setVisibility(View.GONE);
            layoutLevel.setVisibility(View.VISIBLE);
        } else if (type == 1) {
            spinnerResultNO.setVisibility(View.GONE);
            spinnerResultCVSA.setVisibility(View.VISIBLE);
            layoutFine.setVisibility(View.GONE);
            layoutLevel.setVisibility(View.VISIBLE);
        } else {
            spinnerResultNO.setVisibility(View.GONE);
            spinnerResultCVSA.setVisibility(View.VISIBLE);
            layoutFine.setVisibility(View.VISIBLE);
            layoutLevel.setVisibility(View.GONE);
        }

        UserBean user;
        if (Utility.onScreenUserId == Utility.user1.getAccountId()) {
            user = Utility.user1;
        } else
            user = Utility.user2;
        tvDriver.setText(getString(R.string.DRIVER) + ": " + user.getFirstName() + " " + user.getLastName());
        tvPowerUnit.setText(getString(R.string.POWER_UNIT) + ": " + Utility.UnitNo);

        populateLevel();
        if (id == 0) {
            incidentDateTime = Utility.getCurrentDateTime();
            duration = 0;
           // trailers = TrailerDB.getTrailers();
            trailers = new String[]{"", Utility.TrailerNumber};
            startThread();
        } else {
            IncidentBean bean = IncidentDB.getIncidentById(id);
            incidentDateTime = bean.getIncidentDate();
            duration = bean.getDuration();
            etComment.setText(bean.getComment());
            etReportNo.setText(bean.getReportNo());
            spLevel.setSelection(bean.getLevel());
            levelId = bean.getLevel();
            _fileName = Utility.GetDocumentFullPath(docType, bean.getDocPath());
            int result = bean.getResult() + 1;
            if (type == 0) {
                spinnerResultNO.setSelection(result);

            } else if (type == 1) {
                spinnerResultCVSA.setSelection(result);

            } else {
                spinnerResultCVSA.setSelection(result);

                etFineAmount.setText(bean.getFineAmount());
            }

            if (bean.getDocPath().isEmpty()) {
                attachedFg = false;
                btnAttach.setEnabled(false);
                btnAttach.setText(R.string.NO_DOCUMENT_ATTACHED);
            } else {
                _fileName = Utility.GetDocumentFullPath(docType, bean.getDocPath());
                btnAttach.setText(R.string.VIEW_DOCUMENT);
                attachedFg = true;
            }
            trailers = new String[]{"", bean.getTrailerNo()}; //TrailerDB.getTrailers(bean.getTrailerNo());
            tvDuration.setText(Utility.getTimeFromMinute(duration));

        }

        tvTrailer.setText(getString(R.string.trailer) + ": " + trailers[1]);
    }

    // 0- NoticeOrder, 1- CVSA, 2- violation
    private void populateLevel() {

        int arrayId = (type == 0 ? R.array.notice_order_result : R.array.cvsa_result);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, getResources().getStringArray(arrayId));
        spLevel.setAdapter(adapter);

        for (int position = 0; position < adapter.getCount(); position++) {
            if (adapter.getItemId(position) == levelId) {
                spLevel.setSelection(position);
                break;
            }
        }
    }

    private OnFragmentInteractionListener mListener;

    public AddNoticeOrderFragment() {
        // Required empty public constructor
    }

    public static AddNoticeOrderFragment newInstance(int id, int type, String docType) {
        AddNoticeOrderFragment fragment = new AddNoticeOrderFragment();
        Bundle args = new Bundle();
        args.putInt("id", id);
        args.putInt("type", type);
        args.putString("doctype", docType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getInt("id");
            type = getArguments().getInt("type");
            docType = getArguments().getString("doctype");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_notice_order, container, false);
        initialize(view);
        return view;
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
        stopThread();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);

        void onLoadIncidentDetail();

        void onUploadDocument(String type, String path);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            try {

                _fileName = data.getExtras().getString(MediaStore.EXTRA_OUTPUT);
                if (!_fileName.isEmpty()) {
                    attachedFg = true;
                    btnAttach.setText(getString(R.string.VIEW_DOCUMENT));

                    /*if (attachedFg)
                        mListener.onUploadDocument(DocumentType.DOCUMENT_FUEL, _fileName);*/
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
