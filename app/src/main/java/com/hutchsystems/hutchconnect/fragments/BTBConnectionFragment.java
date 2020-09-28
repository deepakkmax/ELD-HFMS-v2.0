package com.hutchsystems.hutchconnect.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.BluetoothDeviceAdapter;
import com.hutchsystems.hutchconnect.adapters.BluetoothPairedDeviceAdapter;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.ConstantFlag;
import com.hutchsystems.hutchconnect.common.HutchConnect;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.tracklocations.GpsBroadCastReceiver;
import com.hutchsystems.hutchconnect.util.AnimationUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.fragment.app.Fragment;


public class BTBConnectionFragment extends Fragment implements View.OnClickListener, BluetoothPairedDeviceAdapter.ButtonClickListener,
        GpsBroadCastReceiver.IBTBStatus {
    final String TAG = BTBConnectionFragment.class.getName();
    private static final int REQUEST_ENABLE_BT = 1;
    private OnFragmentInteractionListener mListener;
    int driverId;
    List<BluetoothDevice> listAvailableDevices;
    BluetoothDeviceAdapter availableDevicesAdapter;

    ListView lvAvailableDevices;
    LinearLayout layoutBluetooth;
    RelativeLayout layoutHeader;
    TextView tvBluetoothEnable, tvBTBHeartbeat, tvEstablishedConnection, tvPairDevice, tvDescription;
    ImageView ivBluetoothEnable;
    ImageView ivBTBHeartbeat;
    ImageView ivPairDevice;
    ImageView ivEstablishedConnection;


    TextView tvSearchingBluetooth, tvAppVersion;
    ImageView ivSearchingBluetooth;
    ImageButton btnRetry;


    private LinearLayout layoutStartVehicle;
    private BluetoothAdapter bTAdapter;

    CanMessages objCan = new CanMessages();

    // initialize atrack class
    HutchConnect objHutchConnect = new HutchConnect(getContext());

    public BTBConnectionFragment() {
    }

    private void resetControlState() {

        layoutStartVehicle.setVisibility(View.GONE);
        layoutBluetooth.setVisibility(View.GONE);
        tvSearchingBluetooth.setVisibility(View.GONE);
        ivSearchingBluetooth.setVisibility(View.GONE);
        tvEstablishedConnection.setVisibility(View.GONE);
        ivEstablishedConnection.setVisibility(View.GONE);
        tvBTBHeartbeat.setVisibility(View.GONE);
        ivBTBHeartbeat.setVisibility(View.GONE);
        tvPairDevice.setVisibility(View.GONE);
        ivPairDevice.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

    }

    boolean afterSetupFg = false;

    private void initialize(View view) {
        try {
            if (getArguments() != null) {
                afterSetupFg = getArguments().getBoolean("afterSetupFg");
            }
            GpsBroadCastReceiver.mListner = this;
            layoutHeader = (RelativeLayout) view.findViewById(R.id.layoutHeader);

            tvDescription = (TextView) view.findViewById(R.id.tvDescription);
            tvAppVersion = (TextView) view.findViewById(R.id.tvAppVersion);
            tvAppVersion.setText(getString(R.string.version) + " " + Utility.ApplicationVersion);
            layoutStartVehicle = (LinearLayout) view.findViewById(R.id.layoutStartVehicle);
            tvBluetoothEnable = (TextView) view.findViewById(R.id.tvCheckingBluetoothEnable);
            ivBluetoothEnable = (ImageView) view.findViewById(R.id.icBluetoothEnable);
            tvBTBHeartbeat = (TextView) view.findViewById(R.id.tvCheckingBTBHeartbeat);
            ivBTBHeartbeat = (ImageView) view.findViewById(R.id.icBTBHearBeat);
            tvEstablishedConnection = (TextView) view.findViewById(R.id.tvEstablishConnection);
            ivEstablishedConnection = (ImageView) view.findViewById(R.id.icEstablishConnection);

            tvSearchingBluetooth = (TextView) view.findViewById(R.id.tvBluetoothSearching);
            tvSearchingBluetooth.setText(getString(R.string.searching_for_btb) + " " + Utility.MACAddress);
            ivSearchingBluetooth = (ImageView) view.findViewById(R.id.icBluetoothSearching);

            tvPairDevice = (TextView) view.findViewById(R.id.tvPairDevice);
            tvPairDevice.setText("Pairing BTB " + Utility.MACAddress);
            ivPairDevice = (ImageView) view.findViewById(R.id.ivPairDevice);

            lvAvailableDevices = (ListView) view.findViewById(R.id.lvAvailableDevices);
            layoutBluetooth = (LinearLayout) view.findViewById(R.id.layoutBluetooth);
            layoutBluetooth.setVisibility(View.GONE);
            btnRetry = (ImageButton) view.findViewById(R.id.btnRetry);
            btnRetry.setOnClickListener(this);

            listAvailableDevices = new ArrayList<>();
            availableDevicesAdapter = new BluetoothDeviceAdapter(getContext(), listAvailableDevices);

            lvAvailableDevices.setAdapter(availableDevicesAdapter);
            if (afterSetupFg) {
                layoutHeader.setVisibility(View.GONE);
                tvDescription.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            LogFile.write(BTBConnectionFragment.class.getName() + "::initialize Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            LogDB.writeLogs(BTBConnectionFragment.class.getName(), "::initialize Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    public static BTBConnectionFragment newInstance() {
        BTBConnectionFragment fragment = new BTBConnectionFragment();
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
        View view = inflater.inflate(R.layout.activity_bluetooth_connectivity_check, container, false);
        try {
            initialize(view);

            //after initialize the layout, check if bluetooth is enable or not
            checkBluetooth();

        } catch (Exception e) {
            LogFile.write(BTBConnectionFragment.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            LogDB.writeLogs(BTBConnectionFragment.class.getName(), "::onCreateView Error:", e.getMessage(), Utility.printStackTrace(e));

        }
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnRetry:
                checkBluetooth();
                break;
        }
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
        try {
            //objCan.stopTransmitRequestHB();
            mListener = null;
            objCan = null;

        } catch (Exception e) {
            LogFile.write(BTBConnectionFragment.class.getName() + "::onDetach Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            LogDB.writeLogs(BTBConnectionFragment.class.getName(), "::onDetach Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onButtonClicked(BluetoothDevice device) {
        unpairDevice(device);
    }

    //use for unpair device
    private void unpairDevice(BluetoothDevice device) {
        try {
            Log.i(TAG, "Start Unpairing...");
            Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
            Method removeBondMethod = btClass.getMethod("removeBond");
            removeBondMethod.invoke(device);

            Log.i(TAG, "Unpairing finished.");
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
            LogFile.write(BTBConnectionFragment.class.getName() + "::unpairDevice Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            LogDB.writeLogs(BTBConnectionFragment.class.getName(), "::unpairDevice Error:", e.getMessage(), Utility.printStackTrace(e));

        }

    }

    public interface OnFragmentInteractionListener {

        void onNextToCanbusRead();
    }

    private void checkBluetooth() {

        resetControlState();
        Thread thBluetooth = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    startAnimation(ivBluetoothEnable, tvBluetoothEnable);
                    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

                    Thread.sleep(100);

                    boolean status = adapter.isEnabled();
                    UpdateViewStatus(ivBluetoothEnable, tvBluetoothEnable, status);

                    if (!status) {
                        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(turnOn, REQUEST_ENABLE_BT);
                    } else {
                        startAnimation(ivSearchingBluetooth, tvSearchingBluetooth);

                        // searching for BTB
                        CanMessages.deviceAddress = null;
                        Set<BluetoothDevice> devices = adapter.getBondedDevices();
                        for (BluetoothDevice device : devices) {

                            if (device.getAddress().equals(Utility.MACAddress)) {
                                CanMessages.deviceAddress = device.getAddress();
                                CanMessages.deviceName = device.getName();
                                Log.d("BTB", "BluethooState-0: Unpairing previous device");
                                if (!ConstantFlag.HutchConnectFg)
                                    unpairDevice(device);
                                break;
                            }
                        }
                        status = CanMessages.deviceAddress != null;
                        if (!status)
                            startDiscovery();
                        else if (ConstantFlag.HutchConnectFg) {

                            UpdateViewStatus(ivSearchingBluetooth, tvSearchingBluetooth, true);
                            startAnimation(ivPairDevice, tvPairDevice);
                            connectDevice();
                        }

                    }

                } catch (Exception exe) {

                }
            }
        });
        thBluetooth.setName("thBluetooth");
        thBluetooth.start();

    }

    private void pairBTB(final String macAddress) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                    adapter.cancelDiscovery();
                    BluetoothDevice device = adapter.getRemoteDevice(macAddress);

                    Class btClass = Class.forName("android.bluetooth.BluetoothDevice");

                    Method createBondMethod = btClass.getMethod("createBond");
                    createBondMethod.invoke(device);

                } catch (Exception exe) {

                }
            }
        }).start();


    }

    private void startDiscovery() {
        listAvailableDevices.clear();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.startDiscovery();
    }


    @Override
    public void discoveryStarted() {
        Log.d("BTB", "BluethooState-1: Started Discovery");
        foundFg = false;
        listAvailableDevices.clear();
        availableDevicesAdapter.notifyDataSetChanged();
    }

    boolean foundFg = false;

    @Override
    public void discoveryFinished() {
        Log.d("BTB", "BluethooState-4: Finished Discovery");

        if (!foundFg) {
            layoutBluetooth.setVisibility(View.VISIBLE);
            availableDevicesAdapter.notifyDataSetChanged();
            UpdateViewStatus(ivSearchingBluetooth, tvSearchingBluetooth, foundFg);
            btnRetry.setVisibility(View.VISIBLE);
        } else {

            layoutBluetooth.setVisibility(View.GONE);
        }
    }

    @Override
    public void deviceFound(BluetoothDevice device) {
        listAvailableDevices.add(device);
        if (device.getAddress().equals(Utility.MACAddress)) {
            Log.d("BTB", "BluethooState-2: found BTB");
            Utility.showMsg("Found BTB " + Utility.MACAddress);
            foundFg = true;
            UpdateViewStatus(ivSearchingBluetooth, tvSearchingBluetooth, true);

            startAnimation(ivPairDevice, tvPairDevice);
            pairBTB(device.getAddress());
        }
    }

    @Override
    public void devicePairing(BluetoothDevice device) {
        if (device.getAddress().equals(Utility.MACAddress)) {

        }
    }

    @Override
    public void devicePaired(BluetoothDevice device) {
        if (device.getAddress().equals(Utility.MACAddress)) {
            Log.d("BTB", "BluethooState-5: paired");
            UpdateViewStatus(ivPairDevice, tvPairDevice, true);
            if (layoutBluetooth != null)
                layoutBluetooth.setVisibility(View.GONE);

            connectDevice();
        }
    }

    @Override
    public void deviceUnpaired(BluetoothDevice device) {
        if (device.getAddress().equals(Utility.MACAddress)) {
            Log.d("BTB", "BluethooState-1: unpaired");
            startDiscovery();
        }
    }

    @Override
    public void deviceConnected(BluetoothDevice device) {
        Log.d("BTB", "BluethooState-7: connected");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        if (requestCode == REQUEST_ENABLE_BT) {
            checkBluetooth();
        }

    }

    private void connectDevice() {

        startAnimation(ivEstablishedConnection, tvEstablishedConnection);
        Log.d("BTB", "BluethooState-6: connecting");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Thread.sleep(1000);
                    // Get the BluetoothDevice object
                    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(Utility.MACAddress);
                    if (afterSetupFg) {
                        CanMessages.mState = CanMessages.STATE_LISTEN;
                        MainActivity.connectRequest = 60;
                    } else {
                        objHutchConnect.connect(CanMessages.deviceAddress);

                    }

                    int i = 0;
                    while (CanMessages.mState != CanMessages.STATE_CONNECTED && i < 30) {
                        Thread.sleep(1000);
                        i++;
                    }
                    final boolean status = CanMessages.mState == CanMessages.STATE_CONNECTED;

                    // Atrack device finish pairing progress
                    if (ConstantFlag.HutchConnectFg) {
                        UpdateViewStatus(ivPairDevice, tvPairDevice, true);
                        if (layoutBluetooth != null)
                            layoutBluetooth.setVisibility(View.GONE);
                    }

                    UpdateViewStatus(ivEstablishedConnection, tvEstablishedConnection, status);

                    // checking Heart beat
                    startAnimation(ivBTBHeartbeat, tvBTBHeartbeat);
                    Thread.sleep(1000);
                    if (ConstantFlag.HutchConnectFg) {
                        objCan.HeartBeat = status;
                    } else {
                        i = 0;
                        while (!objCan.HeartBeat && i < 10) {
                            Thread.sleep(1000);
                            i++;
                        }
                    }
                    UpdateViewStatus(ivBTBHeartbeat, tvBTBHeartbeat, objCan.HeartBeat);

                    if (!afterSetupFg && status) {
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    layoutStartVehicle.setVisibility(View.VISIBLE);
                                }
                            });
                        double rpm = Double.parseDouble(CanMessages.RPM);
                        while (rpm <= 0) {
                            Thread.sleep(1000);
                            rpm = Double.parseDouble(CanMessages.RPM);
                        }
                    }

                    if (getActivity() != null && mListener != null)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status)
                                    mListener.onNextToCanbusRead();
                                else
                                    btnRetry.setVisibility(View.VISIBLE);
                            }
                        });

                } catch (Exception exe) {
                    Log.d("BTB", "BluethooState-9:" + exe.getMessage());

                }
            }
        }).start();
    }

    private void startAnimation(final ImageView imgView, final TextView tvView) {

        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    tvView.setVisibility(View.VISIBLE);
                    imgView.setVisibility(View.VISIBLE);

                    tvView.setTextColor(getResources().getColor(R.color.yellow2));
                    AnimationUtil.startSetupProcessingAnimation(getResources(), imgView);
                }
            });
    }

    private void UpdateViewStatus(final ImageView imgView, final TextView tvView, final boolean status) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imgView.setBackgroundResource(status ? R.drawable.ic_setup_passed : R.drawable.ic_setup_failed);
                    tvView.setTextColor(getResources().getColor(status ? R.color.green2 : R.color.red1));
                }
            });
    }
}