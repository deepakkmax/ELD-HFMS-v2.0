package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.SetupVehicleInfoAdapter;
import com.hutchsystems.hutchconnect.beans.SetupVehicleInfoBean;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.ConstantFlag;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.common.ZoneList;
import com.hutchsystems.hutchconnect.db.CarrierInfoDB;
import com.hutchsystems.hutchconnect.db.LogDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;


public class CanBusDataFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    public static Map<Integer, String> odometerJ1939 = null;
    public static Map<Integer, String> odometerJ1708 = null;
    final String TAG = CanBusDataFragment.class.getName();
    SetupVehicleInfoAdapter adapter;
    ListView lvData;
    Spinner ddlProtocolSupport, dd2OdometerSource;
    Button btnProceed, btnLock;
    boolean afterSetupFg = false;
    ArrayList<SetupVehicleInfoBean> data = new ArrayList<>();

    private OnFragmentInteractionListener mListener;
    private LinearLayout layoutProtocol;
    ConstraintLayout layoutStartVehicle;
    RelativeLayout layoutHeader;
    private TextView tvProtocol, tvAppVersion,tvCurrentTimeValue;
    private ArrayList<Integer> J1939Keys = new ArrayList<>();

    private ArrayList<Integer> J1708Keys = new ArrayList<>();

    public CanBusDataFragment() {

    }

    public static CanBusDataFragment newInstance() {
        CanBusDataFragment fragment = new CanBusDataFragment();
        return fragment;
    }

    private void initialize(View view) {
        if (getArguments() != null) {
            afterSetupFg = getArguments().getBoolean("afterSetupFg");
        }

        //testing purpose
        //J1939Keys.add(23);
        odometerJ1939 = new HashMap();
        odometerJ1708 = new HashMap();
        tvCurrentTimeValue = (TextView) view.findViewById(R.id.tvCurrentTimeValue);

        layoutProtocol = (LinearLayout) view.findViewById(R.id.layoutProtocol);
        ddlProtocolSupport = (Spinner) view.findViewById(R.id.ddlProtocolSupport);
        ddlProtocolSupport.setOnItemSelectedListener(this);

        dd2OdometerSource = (Spinner) view.findViewById(R.id.dd2OdometerSource);
        dd2OdometerSource.setOnItemSelectedListener(this);

        // user won't be able to set protocol while using Hutch connect
        if (ConstantFlag.HutchConnectFg) {
            layoutProtocol.setVisibility(View.GONE);
            ddlProtocolSupport.setVisibility(View.GONE);
            dd2OdometerSource.setVisibility(View.GONE);
        }

        tvAppVersion = (TextView) view.findViewById(R.id.tvAppVersion);
        tvAppVersion.setText(getString(R.string.version) + " " + Utility.ApplicationVersion);
        lvData = (ListView) view.findViewById(R.id.lvData);
        tvProtocol = (TextView) view.findViewById(R.id.tvProtocol);
        layoutStartVehicle = (ConstraintLayout) view.findViewById(R.id.layoutStartVehicle);
        layoutStartVehicle.setVisibility(View.GONE);
        layoutHeader = (RelativeLayout) view.findViewById(R.id.layoutHeader);
        if (afterSetupFg)
            layoutHeader.setVisibility(View.GONE);
        adapter = new SetupVehicleInfoAdapter(getContext(), R.layout.setup_activity, data);
        lvData.setAdapter(adapter);

        btnProceed = (Button) view.findViewById(R.id.btnProceed);
        btnProceed.setOnClickListener(this);

        btnProceed.setVisibility(View.GONE);


        selectProtocol();


        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (true) {
                    try {

                        if (odometerJ1939 == null || odometerJ1708 == null)
                            break;
                        populateData();

                        if (!afterSetupFg) {
                            lockProtocol();
                        }
                        //lockProtocol();


                        Thread.sleep(1000);


                        if (i < 5)
                            if (validateVehicleInfo()) {
                                i++;
                            } else {
                                i = 0;
                            }

                        if (i >= 5) {
                            if (getActivity() != null)
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (afterSetupFg) {
                                            // btnProceed.setVisibility(View.VISIBLE);
                                        } else
                                            layoutStartVehicle.setVisibility(View.VISIBLE);
                                    }
                                });

                            if (!afterSetupFg) {

                                if (Double.parseDouble(CanMessages.RPM) == 0d) {
                                    if (mListener != null) {
                                        mListener.onNextFromCanBusDataFragment();
                                    }
                                    break;
                                }
                            }
                        }
                    } catch (Exception exe) {
                    }
                }
            }
        }).start();

    }

    private void lockProtocol() {

        String protocol = "";
        int odometerSource = -1;
        if (CanMessages.J1939SupportFg) {
            protocol = "J1939";

            // if atrack device then fix odometersource 23 as per gary sir recommendation
            if (ConstantFlag.HutchConnectFg) {
                odometerSource = 23;
            } else {
                if (J1939Keys.contains(23)) {
                    odometerSource = 23;
                } else if (J1939Keys.contains(49)) {
                    odometerSource = 49;
                } else if (J1939Keys.contains(0)) {
                    Log.d("VehicleINfo", "Source: 0");
                    odometerSource = 0;
                }
            }
        } else if (CanMessages.J1708SupportFg) {
            protocol = "J1708";

            // if atrack device then fix odometersource 128 as per gary sir recommendation
            if (ConstantFlag.HutchConnectFg) {
                odometerSource = 128;
            } else {
                if (J1708Keys.contains(128)) {
                    odometerSource = 128;
                } else if (J1708Keys.contains(140)) {
                    odometerSource = 140;
                }
            }

        } else if (CanMessages.OBD2SupportFg) {
            protocol = "OBD2";
        } else {
            protocol = CanMessages.protocol;
        }

        // if protocol and odometer source are same as locked previously. we will not save it to database
        if (CanMessages.protocol.equals(protocol) && odometerSource == CanMessages.odometerSource)
            return;

        CanMessages.protocol = protocol;
        CanMessages.odometerSource = odometerSource;

        // Update Odometer source and protocol value in carrierinfo db
        CarrierInfoDB.updateOdometerSourceAndProtocol(odometerSource, protocol);
        updateOdometerReading(protocol, odometerSource);

        // if atrack device then protocol setting is not required
        if (!ConstantFlag.HutchConnectFg) {
            if (getActivity() != null)
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        selectProtocol();
                        selectOdometerSource();
                        adapter.notifyDataSetChanged();
                    }
                });
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 17 July 2018
    // Purpose: Load Odometer Source
    private void loadOdometerSource() {
        int selection = ddlProtocolSupport.getSelectedItemPosition();
        String[] odometerSource;

        // From Protocol Dropdown if Selected Protocol is J1939 then below odometer Source List Display in the Odometer Source Dropdown
        if (selection == 2) {
            odometerSource = new String[]{"N/A", "0", "23", "49"};
        }

        // From Protocol Dropdown if Selected Protocol is J1708 then below odometer Source List Display in the Odometer Source Dropdown
        else if (selection == 3) {
            odometerSource = new String[]{"N/A", "128", "140"};
        }

        // Protocol Selected as N/A or OBD2 then "N/A" Display in Odometer Source Dropdown
        else {
            odometerSource = new String[]{"N/A"};
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, odometerSource);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dd2OdometerSource.setAdapter(adapter);
        selectOdometerSource();
    }

    // Created By: Deepak Sharma
    // Created Date: 17 July 2018
    // Purpose: Selection of Protocol.
    private void selectProtocol() {

        //  String protocolValue = Utility.getPreferences("protocol_supported", CanMessages.protocol);
        if (CanMessages.protocol.isEmpty()) {
            ddlProtocolSupport.setSelection(0);
        } else {
            // Select Previously Locked value of Protocol
            ddlProtocolSupport.setSelection(((ArrayAdapter<String>) ddlProtocolSupport.getAdapter()).getPosition(CanMessages.protocol));
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 17 July 2018
    // Purpose: Selection of Odometer Source.
    private void selectOdometerSource() {

        //int odoMeterSource = Utility.getPreferences("odometer_source", CanMessages.odometerSource);
        if (CanMessages.odometerSource == -1) {
            dd2OdometerSource.setSelection(0);
        } else {
            // Select Previously Locked value of Odometer Source
            dd2OdometerSource.setSelection(((ArrayAdapter<String>) dd2OdometerSource.getAdapter()).getPosition(String.valueOf(CanMessages.odometerSource)));
        }
    }

    private boolean validateVehicleInfo() {
        boolean status = Double.parseDouble(CanMessages.RPM) > 0d && Double.parseDouble(CanMessages.OdometerReading) > 0d && Double.parseDouble(CanMessages.EngineHours) > 0d
                && Double.parseDouble(CanMessages.TotalIdleHours) >= 0d && Double.parseDouble(CanMessages.TotalFuelConsumed) >= 0d && Double.parseDouble(CanMessages.TotalIdleFuelConsumed) >= 0d
                && Double.parseDouble(CanMessages.Speed) >= 0d;

        if (afterSetupFg)
            return true;

        return status;
    }

    private void populateData() {
        if (odometerJ1939 == null || odometerJ1708 == null)
            return;
        data.clear();
        data.add(addRow("Protocol Supported", getProtocolSupported()));
        data.add(addRow("RPM", CanMessages.RPM));

        if (ConstantFlag.HutchConnectFg) {
            data.add(addRow("Odometer Reading", CanMessages.OdometerReading));
        } else {
            if (CanMessages.J1939SupportFg) {

                for (Map.Entry pair : odometerJ1939.entrySet()) {
                    int keys = (int) pair.getKey();
                    data.add(addRow("Odometer Reading (Source: " + pair.getKey() + ") ", pair.getValue().toString()));

                    if (!J1939Keys.contains(keys))
                        J1939Keys.add(keys);
                }
            }

            if (CanMessages.J1708SupportFg) {
                //Iterator it = odometerJ1708.entrySet().iterator();
                for (Map.Entry pair : odometerJ1708.entrySet()) {
                    int keys = (int) pair.getKey();
                    data.add(addRow("Odometer Reading (MID: " + pair.getKey() + ") ", pair.getValue().toString()));

                    if (!J1708Keys.contains(keys))
                        J1708Keys.add(keys);
                }
            }

            if (CanMessages.OBD2SupportFg) {
                data.add(addRow("Odometer Reading", CanMessages.OdometerReading));
            }
        }

        data.add(addRow("Engine Hours", CanMessages.EngineHours));
        data.add(addRow("Idle Engine Hour", CanMessages.TotalIdleHours));
        data.add(addRow("Fuel Used", CanMessages.TotalFuelConsumed));
        data.add(addRow("Idle Fuel Used", CanMessages.TotalIdleFuelConsumed));
        data.add(addRow("Speed", CanMessages.Speed));
        // data.add(addRow("VIN", CanMessages.VIN.isEmpty() ? "Not Supported" : CanMessages.VIN));
        data.add(addRow("VIN", Utility.VIN));

        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                    tvCurrentTimeValue.setText(Utility._CurrentDateTime + " - " + ZoneList.getTimezoneName(false));
                }
            });
    }

    private SetupVehicleInfoBean addRow(String data, String value) {
        SetupVehicleInfoBean bean = new SetupVehicleInfoBean();
        bean.setData(data);
        bean.setValue(value);
        return bean;
    }

    private String getProtocolSupported() {
        StringBuilder protocol = new StringBuilder();
        if (CanMessages.J1939SupportFg) {
            protocol.append("J1939").append(", ");
        }

        if (CanMessages.J1708SupportFg) {
            protocol.append("J1708").append(", ");
        }

        if (CanMessages.OBD2SupportFg) {
            protocol.append("OBD2").append(", ");
        }
        if (protocol.length() > 2)
            protocol.setLength(protocol.length() - 2);
        return protocol.toString();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utility.activeUserId==0) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_bluetooth_connectivity_check2, container, false);
        initialize(view);
        return view;
    }

    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (Utility.activeUserId==0) {
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

    }
    // Created By: Pallavi Wattamwar
    // Created Date: 25 July 2018
    // Purpose: Validate Source and Protocol and update value into Database.
    public void saveProtocolAndSource() {

        String protocol = ddlProtocolSupport.getSelectedItem().toString();
        int source = Integer.valueOf(dd2OdometerSource.getSelectedItem().toString().equals("N/A") ? "-1" : dd2OdometerSource.getSelectedItem().toString());

        // if protocol and odometer source are same as locked previously. we will not save it to database
        if (CanMessages.protocol.equals(protocol) && source == CanMessages.odometerSource)
            return;

        if (source != -1 && !J1939Keys.contains(source) && !J1708Keys.contains(source)) {

            Utility.showMsg("Vehicle does not support " + source + " Odometer Source");
            return;
        }
        // if no protocol is selected or if protocol is other than obd2 then odometer source is mendatory
        if (protocol.equals("N/A") || (!protocol.equals("OBD2") && source == -1)) {
            Utility.showMsg("Odometer and Protocol is not locked!");
            return;
        }

        // saved locked protocol into database
        CarrierInfoDB.updateOdometerSourceAndProtocol(source, protocol);

        // update variables for canmessage class
        CanMessages.protocol = protocol;
        CanMessages.odometerSource = source;

        // Reset OdometerReading on protocol locked
        updateOdometerReading(protocol, source);

        Utility.showMsg("Protocol and Odometer Source is successfully locked!");
    }

    // Created By: Deepak Sharma
    // Created Date: 2 Aug 2018
    // Purpose: set odometer reading as per protocol locked
    private void updateOdometerReading(String protocol, int source) {
        try {
            if (protocol.equals("J1939")) {
                CanMessages.OdometerReading = odometerJ1939.get(source);
            } else if (protocol.equals("J1708")) {
                CanMessages.OdometerReading = odometerJ1708.get(source);
            } else {
                CanMessages.OdometerReading = "0";
            }

            // in case hashmap don't have value of selected source
            if (CanMessages.OdometerReading == null) {
                CanMessages.OdometerReading = "0";
            }
        } catch (Exception exe) {

        }
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btnProceed:

                    // save protocol only if it's not atrack device
                    if (!ConstantFlag.HutchConnectFg)
                        saveProtocolAndSource();

                    odometerJ1939 = null;
                    odometerJ1708 = null;

                    if (mListener != null)
                        mListener.onNextFromCanBusDataFragment();
                    break;

            }
        } catch (Exception e) {
            LogFile.write(CanBusDataFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            LogDB.writeLogs(CanBusDataFragment.class.getName(), "::onClick Error:", e.getMessage(), Utility.printStackTrace(e));

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
            mListener = null;
            odometerJ1939 = null;
            odometerJ1708 = null;
        } catch (Exception e) {
            LogFile.write(CanBusDataFragment.class.getName() + "::onDetach Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            LogDB.writeLogs(CanBusDataFragment.class.getName(), "::onDetach Error:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        switch (adapterView.getId()) {

            case R.id.ddlProtocolSupport:

                boolean status = false;
                if (i == 1 && !CanMessages.OBD2SupportFg) {
                    Utility.showMsg("Vehicle does not OBD2 protocol.");
                } else if (i == 2 && !CanMessages.J1939SupportFg) {
                    Utility.showMsg("Vehicle does not support J1939.");
                } else if (i == 3 && !CanMessages.J1708SupportFg) {
                    Utility.showMsg("Vehicle does not support J1708.");

                } else {
                    status = true;
                }

                if (!status) {
                    selectProtocol();
                }
                loadOdometerSource();

                break;
          /*  case R.id.dd2OdometerSource:

                int source = Integer.valueOf(dd2OdometerSource.getSelectedItem().toString().equals("N/A") ? "-1" : dd2OdometerSource.getSelectedItem().toString());

                if (source != -1 && !J1939Keys.contains(source) && !J1708Keys.contains(source)) {
                    selectOdometerSource();
                     Utility.showMsg("Vehicle does not support " + source + " Odometer Source");
                }

                break;*/
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    public interface OnFragmentInteractionListener {
        void onNextFromCanBusDataFragment();
    }
}
