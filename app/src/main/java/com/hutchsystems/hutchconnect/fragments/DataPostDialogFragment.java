package com.hutchsystems.hutchconnect.fragments;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.DataUnPostedAdapter;
import com.hutchsystems.hutchconnect.beans.DailyLogBean;
import com.hutchsystems.hutchconnect.common.PostCall;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.EventDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class DataPostDialogFragment extends DialogFragment implements View.OnClickListener {
    RecyclerView rvData;
    Button butOK;
    ImageButton imgCancel;
    DataUnPostedAdapter adapter;
    ArrayList<DailyLogBean> data;
    int driverId, dailylogId;
    public static final String ARG_Data = "data";
    public static final String ARG_Driver_ID = "driver_id";
    public static final String ARG_Dailylog_ID = "dailylog_id";

    public DataPostDialogFragment() {
        // Required empty public constructor
    }


    public static DataPostDialogFragment newInstance(ArrayList<DailyLogBean> data, int driverId, int dailylogId) {

        DataPostDialogFragment fragment = new DataPostDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_Data, data);
        args.putInt(ARG_Driver_ID, driverId);
        args.putInt(ARG_Dailylog_ID, dailylogId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = (ArrayList<DailyLogBean>) getArguments().getSerializable(ARG_Data);
        driverId = getArguments().getInt(ARG_Driver_ID);
        dailylogId = getArguments().getInt(ARG_Dailylog_ID);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_data_post_dialog, container);
        try {

            initialize(view);

            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            this.setCancelable(false);

        } catch (Exception e) {

        }
        return view;
    }

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    // Created By: Deepak Sharma
    // Created Date: 6 May 2020
    // Purpose: initialize views
    private void initialize(View view) {
        rvData = (RecyclerView) view.findViewById(R.id.rvData);
        butOK = (Button) view.findViewById(R.id.butOK);
        butOK.setOnClickListener(this);
        imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
        imgCancel.setOnClickListener(this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        rvData.setLayoutManager(mLayoutManager);
        rvData.setItemAnimator(new DefaultItemAnimator());

        refresh();
    }

    private void refresh() {
        adapter = new DataUnPostedAdapter(data);
        rvData.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.butOK:
                PostData();
                break;
            case R.id.imgCancel:
                dismiss();

                // to open duty status dialog
                if (mListener != null) {
                    mListener.onDismissDataPost();
                }
                break;
        }

    }

    private void PostData() {

        if (data.size() > 0) {
            if (!Utility.isInternetOn()) {
                Utility.showAlertMsg("Internet is not connected!");
                return;
            }
            butOK.setText("Posting...");
            butOK.setEnabled(false);

            Thread thPostEvent = new Thread(new Runnable() {
                @Override
                public void run() {

                    final String date = data.get(0).getLogDate();
                    // Post Daily Log Data as per Date
                    boolean status = PostCall.PostLogEventByDate(Utility.getDate(date), Utility.onScreenUserId);
                    if (status) {

                        data = EventDB.DailyLogUnPostedGet(driverId, dailylogId);

                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String d = new SimpleDateFormat("MMM dd,yyyy").format(Utility.parse(date));
                                    if (data.size() == 0) {
                                        dismiss();

                                        // to open duty status dialog
                                        if (mListener != null) {
                                            mListener.onDismissDataPost();
                                        }
                                    } else {

                                        Utility.showAlertMsg("Data of " + d + " is Posted successfully.");
                                        refresh();
                                        butOK.setText("Post");
                                        butOK.setEnabled(true);
                                    }
                                }
                            });
                    } else {

                    }
                }
            });
            thPostEvent.setName("thPostEvent");
            thPostEvent.start();
        } else {
            dismiss();
        }
    }

    public IDataPostDialog mListener;

    public interface IDataPostDialog {
        void onDismissDataPost();
    }
}
