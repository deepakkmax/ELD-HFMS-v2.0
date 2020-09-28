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
import android.widget.Spinner;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.UserBean;
import com.hutchsystems.hutchconnect.beans.VehicleBean;
import com.hutchsystems.hutchconnect.beans.VehicleMaintenanceBean;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.DocumentType;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.ScheduleDB;
import com.hutchsystems.hutchconnect.db.TrailerDB;
import com.hutchsystems.hutchconnect.db.VehicleMaintenanceDB;
import com.hutchsystems.hutchconnect.scanapi.ScanActivity;

import java.io.File;
import java.util.ArrayList;

public class NewMaintenanceFragment extends Fragment {
    TextView tvDriver, tvDate, tvOdometer;
    Spinner spUnitNo, spCurrency, spItem;
    EditText etInvoice, etRepairedBy, etComments, etPartCost, etLabourCost, etDescription;
    Button btnAttach;
    ImageButton btnNewMaintenance;
    int scheduleId = 0;
    int id = 0;
    int dueOn = 0;

    private void initialize(View view) {
        date = Utility.getCurrentDateTime();
        tvDriver = (TextView) view.findViewById(R.id.tvDriver);
        tvDate = (TextView) view.findViewById(R.id.tvDate);
        tvOdometer = (TextView) view.findViewById(R.id.tvOdometer);

        spUnitNo = (Spinner) view.findViewById(R.id.spUnitNo);
        spCurrency = (Spinner) view.findViewById(R.id.spCurrency);
        spItem = (Spinner) view.findViewById(R.id.spItem);


        etInvoice = (EditText) view.findViewById(R.id.etInvoice);
        etRepairedBy = (EditText) view.findViewById(R.id.etRepairedBy);
        etComments = (EditText) view.findViewById(R.id.etComments);
        etPartCost = (EditText) view.findViewById(R.id.etPartCost);
        etLabourCost = (EditText) view.findViewById(R.id.etLabourCost);
        etDescription = (EditText) view.findViewById(R.id.etDescription);

        btnAttach = (Button) view.findViewById(R.id.btnAttach);
        btnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (attachedFg) {
                    if (!_fileName.isEmpty()) {
                        File file = new File(_fileName);

                        if (file.exists()) {
                            Uri path = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", file);

                            //Uri path = Uri.fromFile(file);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(path, "application/pdf");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        }
                    }
                } else {

                    _fileName = Utility.GetDocumentName(DocumentType.DOCUMENT_MAINTENANCE);
                    // if Flag_Scan_Dynmsoft = true Document Scan using dynmsoft sdk

                    Intent intent = new Intent(getContext(), ScanActivity.class);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, _fileName);
                    startActivityForResult(intent, REQUEST_CODE);

                }
            }
        });
        btnNewMaintenance = (ImageButton) view.findViewById(R.id.btnNewMaintenance);
        btnNewMaintenance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidate()) {
                    VehicleMaintenanceBean item = new VehicleMaintenanceBean();
                    item.setMaintenanceDate(date);
                    item.setVehicleId(list.get(spUnitNo.getSelectedItemPosition()).getVehicleId());
                    item.setUnitNo(list.get(spUnitNo.getSelectedItemPosition()).getUnitNo());
                    item.setOdometerReading(CanMessages.OdometerReading);
                    item.setCurrency(spCurrency.getSelectedItemPosition());
                    item.setItemId(spItem.getSelectedItemPosition());
                    item.setInvoiceNo(etInvoice.getText().toString());
                    item.setRepairedBy(etRepairedBy.getText().toString());
                    item.setComment(etComments.getText().toString());
                    item.setPartCost(etPartCost.getText().toString());
                    item.setLabourCost(etLabourCost.getText().toString());
                    item.setDescription(etDescription.getText().toString());
                    item.setScheduleId(scheduleId);
                    item.setDueOn(dueOn);
                    item.setDriverId(Utility.onScreenUserId);
                    if (attachedFg) {
                        String filename = _fileName.substring(_fileName.lastIndexOf("/") + 1);
                        item.setFileName(filename);
                    } else
                        item.setFileName("");
                    boolean status = VehicleMaintenanceDB.Save(item);

                    if (status && mListener != null) {
                        ScheduleDB.MaintenanceDueUpdate(scheduleId, dueOn);
                        mListener.onLoadMaintenanceDetail();
                        if (attachedFg)
                            mListener.onUploadDocument(DocumentType.DOCUMENT_MAINTENANCE, _fileName);
                    }
                }
            }
        });
        bindMaintenance();
    }

    String date;

    private void bindMaintenance() {
        UserBean user;
        if (Utility.onScreenUserId == Utility.user1.getAccountId()) {
            user = Utility.user1;
        } else
            user = Utility.user2;

        tvDriver.setText( user.getFirstName() + " " + user.getLastName());

        if (id > 0) {
            VehicleMaintenanceBean item = VehicleMaintenanceDB.MaintenanceGetById(id);
            scheduleId = item.getScheduleId();
            date = item.getMaintenanceDate();
            tvDate.setText(getString(R.string.record_date) + "Date: " + Utility.getStringCurrentDate(date));
            tvOdometer.setText("Odo: " + item.getOdometerReading() + " " + getString(R.string.kms));

            VehicleBean v = new VehicleBean();
            v.setVehicleId(item.getVehicleId());
            v.setUnitNo(item.getUnitNo());
            list.add(v);

            spCurrency.setSelection(item.getCurrency());
            spItem.setSelection(item.getItemId());
            etInvoice.setText(item.getInvoiceNo());
            etRepairedBy.setText(item.getRepairedBy());
            etComments.setText(item.getComment());
            etPartCost.setText(item.getPartCost());
            etLabourCost.setText(item.getLabourCost());
            etDescription.setText(item.getDescription());
        } else {
            list = TrailerDB.getHookedVehicleInfo();
            tvDate.setText( "Date: " + Utility.getStringCurrentDate(date));
            tvOdometer.setText("Odo: " + CanMessages.OdometerReading + " " + getString(R.string.kms));
        }
        bindVehicle();
    }

    ArrayList<VehicleBean> list = new ArrayList<>();

    private void bindVehicle() {

        ArrayList<String> data = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            VehicleBean bean = list.get(i);
            data.add(bean.getUnitNo());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, data);
        spUnitNo.setAdapter(adapter);
    }

    private boolean isValidate() {
        boolean status = true;
        String message = "";
        if (isEmpty(etInvoice)) {
            status = false;
            message = getString(R.string.invoice_alert);
        } else if (isEmpty(etPartCost)) {
            status = false;
            message = getString(R.string.part_cost_alert);
        } else if (isEmpty(etLabourCost)) {
            status = false;
            message = getString(R.string.labour_cost_alert);
        }
        if (!status)
            Utility.showMsg(message);
        return status;
    }

    private boolean isEmpty(EditText et) {
        return et.getText().toString().isEmpty();
    }

    private OnFragmentInteractionListener mListener;

    public NewMaintenanceFragment() {
        // Required empty public constructor
    }

    public static NewMaintenanceFragment newInstance(int id, int scheduleId, int dueOn) {
        NewMaintenanceFragment fragment = new NewMaintenanceFragment();
        Bundle args = new Bundle();
        args.putInt("id", id);
        args.putInt("scheduleId", scheduleId);
        args.putInt("dueOn", dueOn);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getInt("id");
            scheduleId = getArguments().getInt("scheduleId");
            dueOn = getArguments().getInt("dueOn");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_maintenance, container, false);
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
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void onLoadMaintenanceDetail();

        void onUploadDocument(String type, String path);
    }

    int REQUEST_CODE = 99;
    String _fileName = "";
    boolean attachedFg = false;

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
