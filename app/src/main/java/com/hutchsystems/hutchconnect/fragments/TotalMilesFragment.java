package com.hutchsystems.hutchconnect.fragments;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;

public class TotalMilesFragment extends DialogFragment implements View.OnClickListener {

    public OnFragmentInteractionListener mListener;

    EditText edTotalDistance;
    TextView tvHeader;
    ImageButton imgCancel;
    Button butSave;
    String totalDistance;

    public TotalMilesFragment() {
        // Required empty public constructor
    }

    public static TotalMilesFragment newInstance(String totalDistance) {
        TotalMilesFragment fragment = new TotalMilesFragment();
        Bundle args = new Bundle();
        args.putString("total_distance", totalDistance);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            totalDistance = getArguments().getString("total_distance");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_total_miles, container, false);

        try {
            initialize(view);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            this.setCancelable(false);

        } catch (Exception e) {
            LogFile.write(InputTruckIDDialog.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TotalMilesFragment.class.getName(),"onCreateView",e.getMessage(), Utility.printStackTrace(e));

        }
        return view;
    }


    private void initialize(View view) {
        try {
            imgCancel = view.findViewById(R.id.imgCancel);
            imgCancel.setOnClickListener(this);
            butSave = view.findViewById(R.id.butSave);
            butSave.setOnClickListener(this);
            tvHeader = (TextView) view.findViewById(R.id.tvHeader);


            String unit = (Utility._appSetting.getUnit() == 1) ? "(in Kms)" : "(in Miles)";
            String header = getString(R.string.total_distance) + unit;
            tvHeader.setText(header);
            edTotalDistance = view.findViewById(R.id.edTotalDistance);
            edTotalDistance.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            edTotalDistance.setText(totalDistance);

        } catch (Exception e) {
            LogFile.write(InputTruckIDDialog.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TotalMilesFragment.class.getName(),"initialize",e.getMessage(), Utility.printStackTrace(e));

        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onTotalDistanceSave(String total);
    }


    @Override
    public void onClick(View view) {
        try {
            Utility.hideKeyboard(getActivity(), view);
            switch (view.getId()) {
                case R.id.imgCancel:
                    dismiss();
                    break;
                case R.id.butSave:
                    String total = edTotalDistance.getText().toString();
                    if (total.isEmpty()) {
                        Utility.showMsg("Please enter valid Today Distance");
                        return;
                    }
                    if (mListener != null) {
                        mListener.onTotalDistanceSave(edTotalDistance.getText().toString());
                    }

                    dismiss();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(InputTruckIDDialog.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(TotalMilesFragment.class.getName(),"onClick",e.getMessage(), Utility.printStackTrace(e));

        }
    }
}
