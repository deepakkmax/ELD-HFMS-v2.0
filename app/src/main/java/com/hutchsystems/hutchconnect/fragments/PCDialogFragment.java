package com.hutchsystems.hutchconnect.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.ReportBean;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.ReportDB;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class PCDialogFragment extends DialogFragment implements View.OnClickListener {

    Button btnYes, btnNo;
    ReportBean data;

    public PCDialogFragment(ReportBean data) {
        this.data = data;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pcdialog, container, false);
        setCancelable(false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {
        mListener = (IReport) getContext();
        btnYes = (Button) view.findViewById(R.id.btnYes);
        btnYes.setOnClickListener(this);

        btnNo = (Button) view.findViewById(R.id.btnNo);
        btnNo.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Utility.PCDialogFg = false;

        switch (view.getId()) {
            case R.id.btnYes:
                dismiss();
                break;
            case R.id.btnNo:
                ReportDB obj = new ReportDB(getContext());
                // save to db
                obj.EventCreateAuto(data);
                mListener.onSpecialStatusChange(0);
                dismiss();
                break;

        }
    }

    private IReport mListener;

    public interface IReport {
        void onSpecialStatusChange(int specialStatusFg);
    }
}
