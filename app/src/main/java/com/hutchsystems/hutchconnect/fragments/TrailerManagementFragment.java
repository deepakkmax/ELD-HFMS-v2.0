package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.TrailerManageRecycleAdapter;
import com.hutchsystems.hutchconnect.beans.AxleBean;
import com.hutchsystems.hutchconnect.beans.TrailerBean;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.Tpms;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.TrailerDB;
import com.hutchsystems.hutchconnect.db.VehicleDB;

import java.util.ArrayList;

public class TrailerManagementFragment extends Fragment implements TrailerManageRecycleAdapter.IHookTrailer, TrailerDialogFragment.OnFragmentInteractionListener {
    String TAG = TpmsFragment.class.getName();
    RecyclerView rvTrailer;
    TrailerManageRecycleAdapter rAdapter;
    ArrayList<AxleBean> list;
    private OnFragmentInteractionListener mListener;

    public TrailerManagementFragment() {
        // Required empty public constructor
    }

    public static TrailerManagementFragment newInstance() {
        TrailerManagementFragment fragment = new TrailerManagementFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trailer_management, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {
        TrailerManageRecycleAdapter.mListner = this;
        rvTrailer = (RecyclerView) view.findViewById(R.id.rvTrailer);
        Configuration config = getResources().getConfiguration();
        RecyclerView.LayoutManager mLayoutManager;
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        } else {
            mLayoutManager = new LinearLayoutManager(getContext());
        }

        rvTrailer.setLayoutManager(mLayoutManager);
        rvTrailer.setItemAnimator(new DefaultItemAnimator());
        TrailerDataGet();
    }

    private void TrailerDataGet() {

        ArrayList<String> trailerList = TrailerDB.getHookedTrailer();
        Utility.hookedTrailers = trailerList;
        // including power unit
        list = VehicleDB.AxleInfoGet(trailerList);
        // testData();
        int hooked = trailerList.size() - 1;
        if (hooked < 5) {
            AxleBean bean = new AxleBean();
            bean.setEmptyFg(true);
            bean.setAxlePosition(1);
            list.add(bean);
        }

        rAdapter = new TrailerManageRecycleAdapter(list);
        rvTrailer.setAdapter(rAdapter);
    }

    private void testData() {
        list = new ArrayList<>();
        list.add(createItem(1, 1, false, true, new double[]{90, 90, 90, 90}, new double[]{45, 45, 45, 45}, new double[]{80, 120}, new double[]{40, 60}));

        list.add(createItem(2, 1, true, true, new double[]{90, 90, 90, 90}, new double[]{50, 50, 50, 50}, new double[]{80, 120}, new double[]{40, 60}));
        list.add(createItem(3, 2, true, true, new double[]{90, 90, 90, 90}, new double[]{55, 55, 55, 55}, new double[]{80, 120}, new double[]{40, 60}));
    }

    private AxleBean createItem(int axleNo, int axlePosition, boolean doubleTire, boolean frontFg, double[] temp, double[] pressure, double[] tempRange, double[] pressRange) {
        AxleBean bean = new AxleBean();
        bean.setAxleNo(axleNo);
        bean.setAxlePosition(axlePosition);
        bean.setDoubleTireFg(doubleTire);
        bean.setFrontTireFg(frontFg);

        bean.setLowPressure(pressRange[0]);
        bean.setHighPressure(pressRange[1]);

        bean.setLowTemperature(tempRange[0]);
        bean.setHighTemperature(tempRange[1]);

        bean.setPressure1(pressure[0]);
        bean.setPressure2(pressure[1]);


        bean.setTemperature1(temp[0]);
        bean.setTemperature2(temp[1]);
        if (doubleTire) {
            bean.setTemperature3(temp[2]);
            bean.setTemperature4(temp[3]);

            bean.setPressure3(pressure[2]);
            bean.setPressure4(pressure[3]);
        }

        return bean;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
            View view = inflater.inflate(R.layout.fragment_trailer_management, viewGroup, false);
            viewGroup.removeAllViews();
            viewGroup.addView(view);
            initialize(view);

        } catch (Exception exe) {
        }

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
        TrailerDataGet();
        for (AxleBean obj : list) {
            if (obj.getVehicleId() == trailerId && obj.getSensorIdsAll() != null) {
                Tpms.addSensorId(obj.getSensorIdsAll());
            }
        }
    }

    @Override
    public void refresh() {
        TrailerDataGet();
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
        TrailerDataGet();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
