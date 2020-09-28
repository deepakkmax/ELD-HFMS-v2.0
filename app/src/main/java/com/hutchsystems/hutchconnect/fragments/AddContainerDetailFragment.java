package com.hutchsystems.hutchconnect.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.DispatchDetailBean;
import com.hutchsystems.hutchconnect.beans.DrayageDispatchBean;
import com.hutchsystems.hutchconnect.common.DocumentType;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DrayageDispatchDB;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddContainerDetailFragment extends Fragment
        implements DatePickerDialog.OnDateSetListener {
    TextView tvDispatchNo, tvBookingNo, tvContainerType, tvGrade;
    EditText etContainerNo, etManufacturingDate, etMaxPayLoad, etMaxGrossWeight, etTareWeight, etSealNo1, etSealNo2;
    ImageButton btnSave;
    Button btnAttach;
    private static final String ARG_DISPATCH_DETAIL_ID = "dispatchdetailid";

    private int dispatchDetailId, dispatchId;

    private OnFragmentInteractionListener mListener;

    public AddContainerDetailFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static AddContainerDetailFragment newInstance(int dispatchDetailId) {
        AddContainerDetailFragment fragment = new AddContainerDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DISPATCH_DETAIL_ID, dispatchDetailId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dispatchDetailId = getArguments().getInt(ARG_DISPATCH_DETAIL_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_container_detail, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {
        final Calendar myCalendar = Calendar.getInstance();
        tvDispatchNo = (TextView) view.findViewById(R.id.tvDispatchNo);
        tvBookingNo = (TextView) view.findViewById(R.id.tvBookingNo);
        tvContainerType = (TextView) view.findViewById(R.id.tvContainerType);
        tvGrade = (TextView) view.findViewById(R.id.tvGrade);
        etContainerNo = (EditText) view.findViewById(R.id.etContainerNo);
        etManufacturingDate = (EditText) view.findViewById(R.id.etManufacturingDate);
        etManufacturingDate.setKeyListener(null);
        etManufacturingDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getContext(), AddContainerDetailFragment.this, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        etMaxPayLoad = (EditText) view.findViewById(R.id.etMaxPayLoad);
        etMaxGrossWeight = (EditText) view.findViewById(R.id.etMaxGrossWeight);
        etTareWeight = (EditText) view.findViewById(R.id.etTareWeight);
        etSealNo1 = (EditText) view.findViewById(R.id.etSealNo1);
        etSealNo2 = (EditText) view.findViewById(R.id.etSealNo2);

        btnSave = (ImageButton) view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String containerNo = etContainerNo.getText().toString().trim();
                if (containerNo.isEmpty()) {
                    Utility.showMsg(getString(R.string.container_no_alert));
                    return;
                }

                DispatchDetailBean bean = new DispatchDetailBean();
                bean.setDispatchDetailId(dispatchDetailId);
                bean.setContainerNo(etContainerNo.getText().toString());
                bean.setManufacturingDate(etManufacturingDate.getText().toString());

                if (etMaxPayLoad.length() > 0)
                    bean.setMaxPayLoad(Integer.valueOf(etMaxPayLoad.getText().toString()));

                if (etMaxGrossWeight.length() > 0)
                    bean.setMaxGrossWeight(Integer.valueOf(etMaxGrossWeight.getText().toString()));

                if (etTareWeight.length() > 0)
                    bean.setTareWeight(Integer.valueOf(etTareWeight.getText().toString()));

                bean.setSealNo1(etSealNo1.getText().toString());
                bean.setSealNo2(etSealNo2.getText().toString());
                if (attachedFg) {
                    String filename = _fileName.substring(_fileName.lastIndexOf("/") + 1);
                    bean.setDocumentPath(filename);
                } else
                    bean.setDocumentPath("");

                DrayageDispatchDB.UpdateContainerDetail(bean);
                mListener.backToDispatchDetail();
                //  mListener.onDispatchDetail(dispatchId);
            }
        });

        btnAttach = (Button) view.findViewById(R.id.btnAttach);
        btnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (attachedFg) {
                    if (!_fileName.isEmpty()) {
                        File file = new File(_fileName);

                        if (file.exists()) {
                            Uri path = Uri.fromFile(file);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(path, "application/pdf");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }
                } else {
                    _fileName = Utility.GetDocumentName(DocumentType.DOCUMENT_OTHER);
                    // if Flag_Scan_Dynmsoft = true Document Scan using dynmsoft sdk


                }

            }
        });
        fillDetail();
    }

    private void fillDetail() {
        DispatchDetailBean bean = DrayageDispatchDB.GetDetailById(dispatchDetailId);
        dispatchId = bean.getDispatchId();
        DrayageDispatchBean dispatch = DrayageDispatchDB.GetById(dispatchId);
        tvDispatchNo.setText("Dispatch No: " + dispatch.getDispatchNo());
        tvBookingNo.setText("Booking No: " + dispatch.getBookingNo());
        if (bean.getDispatchDetailId() > 0) {
            tvContainerType.setText(bean.getContainerType());
            tvGrade.setText(bean.getContainerGrade());
            etContainerNo.setText(bean.getContainerNo());
            etManufacturingDate.setText(bean.getManufacturingDate());
            if (bean.getMaxPayLoad() > 0)
                etMaxPayLoad.setText(bean.getMaxPayLoad() + "");
            if (bean.getMaxGrossWeight() > 0)
                etMaxGrossWeight.setText(bean.getMaxGrossWeight() + "");
            if (bean.getTareWeight() > 0)
                etTareWeight.setText(bean.getTareWeight() + "");
            etSealNo1.setText(bean.getSealNo1());
            etSealNo2.setText(bean.getSealNo2());

            if (bean.getDocumentPath().isEmpty()) {
                attachedFg = false;
           /*     btnAttach.setEnabled(false);
                btnAttach.setText(R.string.NO_DOCUMENT_ATTACHED);*/
            } else {
                _fileName = Utility.GetDocumentFullPath(DocumentType.DOCUMENT_OTHER, bean.getDocumentPath());
                btnAttach.setText(R.string.VIEW_DOCUMENT);
                attachedFg = true;
            }
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

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day); //reset seconds to zero

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            String date = formatter.format(calendar.getTime());
            etManufacturingDate.setText(date);

        } catch (Exception ex) {
        }
    }


    public interface OnFragmentInteractionListener {
        void onDispatchDetail(int id);

        void backToDispatchDetail();
    }


    boolean attachedFg = false;
    String _fileName = "";
    int REQUEST_CODE = 99;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            try {

                _fileName = data.getExtras().getString(MediaStore.EXTRA_OUTPUT);
                if (!_fileName.isEmpty()) {

                    attachedFg = true;
                    btnAttach.setText(getString(R.string.view_document));

                    /*if (attachedFg)
                        mListener.onUploadDocument(DocumentType.DOCUMENT_FUEL, _fileName);*/
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
