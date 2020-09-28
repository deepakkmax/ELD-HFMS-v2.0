package com.hutchsystems.hutchconnect.fragments;


import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.AxleRecycleAdapter;
import com.hutchsystems.hutchconnect.beans.AxleBean;
import com.hutchsystems.hutchconnect.beans.TPMSBean;
import com.hutchsystems.hutchconnect.beans.TrailerBean;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.Tpms;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.TrailerDB;
import com.hutchsystems.hutchconnect.db.VehicleDB;

import java.util.ArrayList;


public class TpmsFragment extends Fragment implements View.OnClickListener, Tpms.ITPMS, AxleRecycleAdapter.IHookTrailer, TrailerDialogFragment.OnFragmentInteractionListener {
    String TAG = TpmsFragment.class.getName();
    RecyclerView rvTPMS;
    AxleRecycleAdapter rAdapter;
    ArrayList<AxleBean> list;
    private OnFragmentInteractionListener mListener;

    public TpmsFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static TpmsFragment newInstance() {
        TpmsFragment fragment = new TpmsFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tpms_test, container, false);
        initialize(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        rAdapter.notifyDataSetChanged();
    }

    private void initialize(View view) {
        AxleRecycleAdapter.mListner = this;
        Tpms.mListner = this;
        rvTPMS = (RecyclerView) view.findViewById(R.id.rvTPMS);
        Configuration config = getResources().getConfiguration();
        RecyclerView.LayoutManager mLayoutManager;
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        } else {
            mLayoutManager = new LinearLayoutManager(getContext());
        }

        rvTPMS.setLayoutManager(mLayoutManager);
        rvTPMS.setItemAnimator(new DefaultItemAnimator());
        TPMSDataGet();
    }

    private void TPMSDataGet() {

        ArrayList<String> trailerList = TrailerDB.getHookedTrailer(); // including power unit
        Utility.hookedTrailers = trailerList;
        list = VehicleDB.AxleInfoGet(trailerList);

        int hooked = trailerList.size() - 1;
        if (hooked < 5) {
            AxleBean bean = new AxleBean();
            bean.setEmptyFg(true);
            bean.setAxlePosition(1);
            list.add(bean);
        }

        rAdapter = new AxleRecycleAdapter(list);
        rvTPMS.setAdapter(rAdapter);
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup viewGroup = (ViewGroup) getView();
            View view = inflater.inflate(R.layout.fragment_tpms_test, viewGroup, false);
            viewGroup.removeAllViews();
            viewGroup.addView(view);
            initialize(view);

        } catch (Exception exe) {
        }

    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void update(final TPMSBean data) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                for (int j = 0; j < list.size(); j++) {
                    AxleBean bean = list.get(j);
                    String[] sensorIds = bean.getSensorIdsAll();
                    for (int i = 0; i < sensorIds.length; i++) {
                        String sensorId = sensorIds[i];
                        if (sensorId.equals(data.getSensorId())) {
                            switch (i) {
                                case 0:
                                    bean.setPressure1(data.getPressure());
                                    bean.setTemperature1(data.getTemperature());
                                    break;
                                case 1:
                                    bean.setPressure2(data.getPressure());
                                    bean.setTemperature2(data.getTemperature());
                                    break;
                                case 2:
                                    bean.setPressure3(data.getPressure());
                                    bean.setTemperature3(data.getTemperature());
                                    break;
                                case 3:
                                    bean.setPressure4(data.getPressure());
                                    bean.setTemperature4(data.getTemperature());
                                    break;
                            }
                            rAdapter.notifyItemChanged(j);
                            return;
                        }
                    }

                }

            }
        });
    }

    TrailerDialogFragment dialog;

    @Override
    public void hook() {
        if (dialog == null) {
            dialog = new TrailerDialogFragment();
        }

        if (dialog.isAdded()) {
            dialog.dismiss();
        }

        dialog.mListener = this;
        dialog.show(getFragmentManager(), "trailer_dialog");
    }

    @Override
    public void hooked(int trailerId) {
        TrailerBean bean = new TrailerBean();
        bean.setTrailerId(trailerId);
        bean.setHookDate(Utility.getCurrentDateTime());
        bean.setDriverId(Utility.activeUserId);
        bean.setHookedFg(1);
        bean.setLatitude1(Utility.currentLocation.getLatitude() + "");
        bean.setLongitude1(Utility.currentLocation.getLongitude() + "");
        bean.setStartOdometer(CanMessages.OdometerReading);
        TrailerDB.hook(bean);

        MainActivity.postData(CommonTask.Post_TrailerStatus);

        TPMSDataGet();
        for (AxleBean obj : list) {
            if (obj.getVehicleId() == trailerId && obj.getSensorIdsAll() != null) {
                Tpms.addSensorId(obj.getSensorIdsAll());
            }
        }
    }

    @Override
    public void refresh() {
        TPMSDataGet();
    }

    @Override
    public void unhook(int trailerId) {
        for (AxleBean bean : list) {
            if (bean.getVehicleId() == trailerId && bean.getSensorIdsAll() != null) {
                Tpms.removeSensorId(bean.getSensorIdsAll());
            }
        }

        TrailerBean bean = new TrailerBean();
        bean.setTrailerId(trailerId);
        bean.setUnhookDate(Utility.getCurrentDateTime());
        bean.setDriverId(Utility.activeUserId);
        bean.setHookedFg(0);
        bean.setLatitude2(Utility.currentLocation.getLatitude() + "");
        bean.setLongitude2(Utility.currentLocation.getLongitude() + "");
        bean.setEndOdometer(CanMessages.OdometerReading);
        TrailerDB.unhook(bean);
        TPMSDataGet();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name

    }
}
