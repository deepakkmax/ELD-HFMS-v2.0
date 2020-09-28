package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.CTPATInspectionAdapter;
import com.hutchsystems.hutchconnect.beans.CTPATInspectionBean;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.CTPATInspectionDB;
import com.hutchsystems.hutchconnect.db.EventDB;
import com.hutchsystems.hutchconnect.db.LogDB;

import java.util.List;


public class CTPATInspectionFragmentList extends Fragment implements View.OnClickListener, CTPATInspectionAdapter.ItemClickListener {
    final String TAG = CTPATInspectionFragmentList.class.getName();

    ListView lvCurrentInspections;
    ImageButton butNewInspection;
    CTPATInspectionAdapter adapter;
    int driverId = 0;

    private OnFragmentInteractionListener mListener;


    public CTPATInspectionFragmentList() {
        // Required empty public constructor
        //mInstance = this;
    }


    private void initialize(View view) {

        lvCurrentInspections = (ListView) view.findViewById(R.id.lvCTPATInspections);
        adapter = new CTPATInspectionAdapter(getContext(), this, getListInspections());
        lvCurrentInspections.setAdapter(adapter);

        butNewInspection = (ImageButton) view.findViewById(R.id.btnNewInspection);
        butNewInspection.setOnClickListener(this);

        if (Utility.InspectorModeFg) {
            butNewInspection.setVisibility(View.GONE);
        }

    }

    public static CTPATInspectionFragmentList newInstance() {

        CTPATInspectionFragmentList fragment = new CTPATInspectionFragmentList();
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
        View view = inflater.inflate(R.layout.fragment_ctpat_list, container, false);

        initialize(view);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
               CTPATInspectionDB.removeInspection();
                } catch (Exception e) {

                }
            }
        }).start();
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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btnNewInspection:
                        if (mListener != null) {
                            int currentStatus = EventDB.getCurrentDutyStatus(Utility.onScreenUserId);
                            String message = getString(R.string.ctpat_add_alert);
                            if (currentStatus == 4 || currentStatus == 5) {
                                mListener.newCTPATInspection();
                            } else {
                                Utility.showAlertMsg(message);
                            }

                    }
                    break;

            }
        } catch (Exception e) {
            LogFile.write(CTPATInspectionFragmentList.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(CTPATInspectionFragmentList.class.getName(), "::onClick Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }


    @Override
    public void viewInspection(CTPATInspectionBean bean) {
        Log.d(TAG, "View the inspection id:" + bean.getid() + " type " + bean.getType());
        if (mListener != null) {
            mListener.viewCTPATInspection(true, bean);
        }
    }

    private List<CTPATInspectionBean> getListInspections() {
        String previousDate = Utility.getPreviousDateOnly(-1);
        List<CTPATInspectionBean> listInspections = CTPATInspectionDB.getInspections(previousDate);

        return listInspections;
    }


    //these callbacks are used to call Activity update the fragment with new data or change to another fragment
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name

        void newCTPATInspection();

        void viewCTPATInspection(boolean viewMode, CTPATInspectionBean bean);
    }


}
