package com.hutchsystems.hutchconnect.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.DevicesAdapter;
import com.hutchsystems.hutchconnect.beans.CarrierInfoBean;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.CarrierInfoDB;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Created By: Sahil Bansal
// Created Date: 12 February 2020
// get all the unit no available
//for change the imei number
public class UnitNoListFragment extends Fragment implements View.OnClickListener {

    RecyclerView recyclerView;

    ProgressBar progressBar;

    DevicesAdapter devicesAdapter;
    Button btnConnect;

    public UnitNoListFragment() {
        // Required empty public constructor
    }

    OnFragmentChangeSerialNoListener mListener;

    // TODO: Rename and change types and number of parameters
    public static UnitNoListFragment newInstance() {


        UnitNoListFragment unitNoListFragment = new UnitNoListFragment();
        return unitNoListFragment;
    }

    public void updateSerialNo() {
        //update the selected imei number
        // get IMEI
        Utility.IMEIGet();

        // get vehicle info of new vehicle connected
        CarrierInfoDB.getCompanyInfo();

        Utility.showMsg("Vehicle is switched successfully!");


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.recycleview, container, false);
        mListener = (OnFragmentChangeSerialNoListener) getActivity();
        initialize(view);
        return view;
    }


    private void initialize(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.rvUnitNo);
        progressBar = (ProgressBar) view.findViewById(R.id.prgsConnection);
        btnConnect = (Button) view.findViewById(R.id.btnConnect);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        List<CarrierInfoBean> list = CarrierInfoDB.getAllUnitNo();
        devicesAdapter = new DevicesAdapter(list, this, getActivity());
        recyclerView.setAdapter(devicesAdapter);
        System.out.println(list);

        btnConnect.setOnClickListener(this);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    public CarrierInfoBean bean;

    public void updateObject(CarrierInfoBean bean) {
        this.bean = bean;

        //enable button
        if (this.bean != null) {
            btnConnect.setEnabled(true);
        } else {
            btnConnect.setEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConnect:
                //update new macAddress
                if (bean != null) {

                    //connect bluetooth with selected macAddress
                    if (mListener != null) {

                        // set IMEI of new selected device
                        Utility.savePreferences("imei_no", bean.getSerailNo());
                        updateSerialNo();
                        mListener.connectMacAddressWithBluetooth(bean.getMACAddress(), bean.getSerailNo());
                    }
                }

                break;
        }
    }

    public interface OnFragmentChangeSerialNoListener {
        void connectMacAddressWithBluetooth(String macAddress, String serialNo);

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem malfunctionItem = menu.findItem(R.id.action_malfunction);
        MenuItem diagnosticItem = menu.findItem(R.id.action_diagnostic);
        MenuItem inspectorModeItem = menu.findItem(R.id.action_inspector_mode);
        MenuItem sendItem = menu.findItem(R.id.action_send);
        MenuItem dashboard = menu.findItem(R.id.action_dashboard);
        malfunctionItem.setVisible(false);
        diagnosticItem.setVisible(false);
        inspectorModeItem.setVisible(false);
        sendItem.setVisible(false);
        dashboard.setVisible(false);

    }

    public void showLoaderAnimation(boolean isShown) {
        if (isShown) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showLoaderAnimation(false);
                            }
                        });
                    }
                }
            }, 30000);
            progressBar.setVisibility(View.VISIBLE);
            if (getActivity() != null) {
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        } else {
            if (getActivity() != null) {
                progressBar.setVisibility(View.GONE);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
    }
}

