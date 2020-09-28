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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.DocumentDetailBean;
import com.hutchsystems.hutchconnect.beans.UserBean;
import com.hutchsystems.hutchconnect.common.DocumentType;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DocumentDetailDB;
import com.hutchsystems.hutchconnect.scanapi.ScanActivity;

import java.io.File;

public class DocumentDetailFragment extends Fragment {
    TextView tvDriver, tvPowerUnit, tvDate;
    EditText etDocumentNo;
    Spinner spDocumentType;
    Button btnAttach;
    ImageButton btnSave;

    public DocumentDetailFragment() {

    }

    public static DocumentDetailFragment newInstance() {
        return new DocumentDetailFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_document_detail, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {

        tvDriver = (TextView) view.findViewById(R.id.tvDriver);
        tvPowerUnit = (TextView) view.findViewById(R.id.tvPowerUnit);
        tvDate = (TextView) view.findViewById(R.id.tvDate);

        etDocumentNo = (EditText) view.findViewById(R.id.etDocumentNo);
        spDocumentType = (Spinner) view.findViewById(R.id.spDocumentType);

        btnAttach = (Button) view.findViewById(R.id.btnAttach);
        btnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (attachedFg) {
                    if (!_fileName.isEmpty()) {
                        File file = new File(_fileName);

                        if (file.exists()) {

                            Uri path = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", file);

                            // after nougat this is not permitted so commented
                            //Uri path = Uri.fromFile(file);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(path, "application/pdf");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        }
                    }
                } else {
                    _fileName = Utility.GetDocumentName(DocumentType.DOCUMENT_OTHER);

                    Intent intent = new Intent(getContext(), ScanActivity.class);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, _fileName);
                    startActivityForResult(intent, REQUEST_CODE);

                   // Scan Document using dynmsoft scanner
                  /*  Intent intent = new Intent(getContext(),ScanActivityDynm.class);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, _fileName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, REQUEST_CODE);*/

                }

            }
        });
        btnSave = (ImageButton) view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!attachedFg) {
                    Utility.showMsg("Please scan document!");
                    return;
                }

                int documentType = spDocumentType.getSelectedItemPosition();
                String documentNo = etDocumentNo.getText().toString().trim();
                if (documentNo.isEmpty()) {

                    Utility.showMsg("Please enter Document No!");
                    return;
                }

                DocumentDetailBean bean = new DocumentDetailBean();
                bean.setCreatedDate(Utility.getCurrentDateTime());
                bean.setDriverId(Utility.onScreenUserId);
                bean.setDocumentType(documentType);
                bean.setDocumentNo(documentNo);
                if (attachedFg) {
                    String filename = _fileName.substring(_fileName.lastIndexOf("/") + 1);
                    bean.setDocumentPath(filename);
                } else
                    bean.setDocumentPath("");

                boolean status = DocumentDetailDB.Save(bean);
                if (status && mListener != null) {
                    mListener.onLoadDocumentDetailList();
                    if (attachedFg)
                        mListener.onUploadDocument(DocumentType.DOCUMENT_OTHER, _fileName);
                }
            }
        });
        UserBean user;
        if (Utility.onScreenUserId == Utility.user1.getAccountId()) {
            user = Utility.user1;
        } else
            user = Utility.user2;
        tvDriver.setText(getString(R.string.driver) + ": " + user.getFirstName() + " " + user.getLastName());
        tvPowerUnit.setText(getString(R.string.power_unit) + ": " + Utility.UnitNo);
        String date = Utility.getStringCurrentDate();
        tvDate.setText("Date: " + date);

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

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void onLoadDocumentDetailList();

        void onUploadDocument(String type, String path);
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
