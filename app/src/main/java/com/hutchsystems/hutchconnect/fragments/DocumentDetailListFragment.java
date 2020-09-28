package com.hutchsystems.hutchconnect.fragments;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.DocumentDetailAdapter;
import com.hutchsystems.hutchconnect.beans.DocumentDetailBean;
import com.hutchsystems.hutchconnect.common.DocumentType;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DocumentDetailDB;

import java.io.File;
import java.util.ArrayList;

public class DocumentDetailListFragment extends Fragment implements DocumentDetailAdapter.IDocument {
    RecyclerView rvDocuments;
    ImageButton btnNewDocument;
    DocumentDetailAdapter adapter;

    public DocumentDetailListFragment() {
        // Required empty public constructor
    }


    public static DocumentDetailListFragment newInstance() {
        DocumentDetailListFragment fragment = new DocumentDetailListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_document_detail_list, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {
        DocumentDetailAdapter.mlistner = this;
        rvDocuments = (RecyclerView) view.findViewById(R.id.rvDocuments);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvDocuments.setLayoutManager(layoutManager);
        rvDocuments.setItemAnimator(new DefaultItemAnimator());
        btnNewDocument = (ImageButton) view.findViewById(R.id.btnNewDocument);
        btnNewDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onAddDocumentDetail();

             /*   int currentStatus = EventDB.getCurrentDutyStatus(Utility.onScreenUserId);
                String message = getString(R.string.add_document_onduty_alert);
                if (currentStatus == 4 || currentStatus == 5) {
                    mListener.onAddDocumentDetail();
                } else {
                    Utility.showAlertMsg(message);
                }*/
            }
        });
        DocumentGet();
        new Thread(new Runnable() {
            @Override
            public void run() {
                DocumentDetailDB.deleteDocument();
            }
        }).start();
    }

    private void DocumentGet() {
        ArrayList<DocumentDetailBean> data = DocumentDetailDB.Get(Utility.onScreenUserId);
        adapter = new DocumentDetailAdapter(data);
        rvDocuments.setAdapter(adapter);
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

    @Override
    public void onClick(String fileName) {
        String fullPath = Utility.GetDocumentFullPath(DocumentType.DOCUMENT_OTHER, fileName);

        File file = new File(fullPath);

        if (file.exists()) {
            Uri path = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(path, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);

        void onAddDocumentDetail();
    }
}
