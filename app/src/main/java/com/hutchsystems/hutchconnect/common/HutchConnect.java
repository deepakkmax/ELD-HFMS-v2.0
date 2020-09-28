package com.hutchsystems.hutchconnect.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.ReportBean;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.telit.terminalio.TIOConnection;
import com.telit.terminalio.TIOConnectionCallback;
import com.telit.terminalio.TIOManager;
import com.telit.terminalio.TIOManagerCallback;
import com.telit.terminalio.TIOPeripheral;

public class HutchConnect implements TIOConnectionCallback, TIOManagerCallback {

    private static final int RSSI_INTERVAL = 1670;
    public static ICanMessage mHutchConnectListner;
    public static boolean diconnected = false;
    String TAG = HutchConnect.class.getName();
    boolean isConnected = false;
    boolean isConnecting = false;

    private TIOPeripheral mPeripheral;
    private TIOConnection mConnection;
    private String completeData = "";
    AlertDialog connectingDialog;
    Context context;
    ReportBean objReport;
    public String serialNo, macAddress;

    public HutchConnect(Context context) {
        this.context = context;
        objReport = new ReportBean(this.context);
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return CanMessages.mState;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    public synchronized void setState(int state) {
        CanMessages.mState = state;
    }

    @Override
    public void onConnected(TIOConnection tioConnection) {
        if (mHutchConnectListner != null) {
            mHutchConnectListner.onAlertClear();
        }
        if ((connectingDialog != null) && connectingDialog.isShowing()) {
            connectingDialog.dismiss();
        }

        Utility.HutchConnectStatusFg = true;
        setState(CanMessages.STATE_CONNECTED);

        updateConnectingStatus();

        startRssiListener();


        if (!mPeripheral.shallBeSaved()) {
            // save if connected for the first time
            mPeripheral.setShallBeSaved(true);
            TIOManager.getInstance().savePeripherals();
        }
        mHutchConnectListner.onELD2020Status(CanMessages.STATE_CONNECTED);
        //sendData("AT$REST=2048" + "\n");

    }

    @Override
    public void onPeripheralFound(TIOPeripheral peripheral) {
        Log.d(TAG, "onPeripheralDiscovered " + peripheral.toString());
        if (peripheral.getAddress().equals(Utility.MACAddress)) {
            connect(peripheral);
        }

    }

    public void connect(TIOPeripheral peripheral) {

        try {
            mPeripheral = peripheral;
            //mConnection = mPeripheral.getConnection();
            // register callback
            setState(CanMessages.STATE_CONNECTING);
            mConnection = mPeripheral.connect(this);
            firstFg = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean firstFg = false;

    public void connect(String bluetoothMacAddress) {

        try {
            // retrieve peripheral instance from TIOManager
            if (TIOManager.getInstance() != null) {
                setState(CanMessages.STATE_LISTEN);
                mPeripheral = TIOManager.getInstance().findPeripheral(bluetoothMacAddress);

                if (mPeripheral != null) {
                    //mConnection = mPeripheral.getConnection();
                    // register callback
                    setState(CanMessages.STATE_CONNECTING);
                    CanMessages.deviceName = mPeripheral.getName();
                    mConnection = mPeripheral.connect(this);

                } else {
                    startScan();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            LogDB.writeLogs(HutchConnect.class.getName(), "::connectError:", e.getMessage(), Utility.printStackTrace(e));

        }
    }

    // Alert Dialog For Connecting with bluetooth device
    public void showConnectionMessage() {

        // Create dialog
        connectingDialog = new AlertDialog.Builder(Utility.context)
                .create();
        connectingDialog.setCancelable(true);
        connectingDialog.setCanceledOnTouchOutside(false);
        connectingDialog.setTitle("Hutch Connect Connection");
        connectingDialog.setIcon(R.drawable.ic_launcher);
        connectingDialog.setMessage("Please wait while app is connecting to Bluetooth Device!");

        connectingDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "ok",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        connectingDialog.cancel();
                    }
                });
        connectingDialog.show();
    }

    public void startScan() {
        TIOManager.getInstance().startScan(this);
        setState(CanMessages.STATE_LISTEN);
    }

    public void sendData(String command) {
        try {

            Log.d("onDataReceived:", "Send Data: DateTime: "+Utility.getCurrentDateTime()+", Command: " + command);
            LogFile.ELD2020DataLog("Send Data: DateTime: "+Utility.getCurrentDateTime()+", Command: " + command);
            byte[] data = command.getBytes("CP-1252");
            mConnection.transmit(data);
        } catch (Exception ex) {
            LogFile.ELD2020DataLog("Send Error: " + ex.getMessage());
            Log.e(TAG, ex.toString());
        }
    }

    private void startRssiListener() {
        if (mPeripheral.getConnectionState() == TIOConnection.STATE_CONNECTED) {

            Log.d(TAG, "startRssiListener");

            try {
                mConnection.readRemoteRssi(RSSI_INTERVAL);
            } catch (Exception ex) {

            }
        }
    }

    public void updateConnectingStatus() {
        if (mConnection != null) {
            isConnected = mConnection.getConnectionState() == TIOConnection.STATE_CONNECTED;
            isConnecting = mConnection.getConnectionState() == TIOConnection.STATE_CONNECTING;
        }
    }

    @Override
    public void onConnectFailed(TIOConnection tioConnection, String errorMessage) {
        Log.d(TAG, "onConnectFailed " + errorMessage);
        if ((connectingDialog != null) && connectingDialog.isShowing()) {
            connectingDialog.dismiss();
        }

        setState(CanMessages.STATE_LISTEN);
    }

    @Override
    public void onDisconnected(TIOConnection tioConnection, String errorMessage) {
        Log.d(TAG, "onDisconnected" + errorMessage);
        stopRssiListener();
        setState(CanMessages.STATE_LISTEN);
        diconnected = true;
        Utility.HutchConnectStatusFg = false;
        updateConnectingStatus();

        // show option to re connect bluetooth device
        if (mHutchConnectListner != null) {
            mHutchConnectListner.onAlertWarning();
            mHutchConnectListner.onVehicleMotionChange(false);
            mHutchConnectListner.onELD2020Status(CanMessages.STATE_DISCONNECTED);
        }

    }

    private void stopRssiListener() {
        if (mConnection != null) {

            Log.d(TAG, "stopRssiListener");

            try {
                mConnection.readRemoteRssi(0);
            } catch (Exception ex) {
                LogDB.writeLogs(HutchConnect.class.getName(), "::stopRssiListener : Exception", ex.getMessage(), Utility.printStackTrace(ex));

            }

        }
    }

    @Override
    public void onDataReceived(TIOConnection tioConnection, byte[] data) {
        try {

            CanMessages.diagnosticEngineSynchronizationTime = System.currentTimeMillis();
            diconnected = false;
            completeData += new String(data);
            if (completeData.contains("\r\n")) {

                Log.d("onDataReceived:", completeData);
                LogFile.ELD2020DataLog(completeData);
                // set unit id
                if (settingUnitId && completeData.toUpperCase().startsWith("$OK")) {
                    settingUnitId = false;
                    mHutchConnectListner.onELD2020Status(CanMessages.STATE_SET_UNIT_ID);
                } else {
                    objReport.ReceiveData(completeData);
                }

                completeData = "";
            }


        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    // Created By: Deepak Sharma
    // Created Date: 12 June 2020
    // Purpose: set unit id
    public void setUnitId(String unitId) {

        settingUnitId = true;
        String command = "AT$UNID=" + unitId + "\n";
        sendData(command);
    }

    // Created By: Deepak Sharma
    // Created Date: 12 June 2020
    // Purpose: get unit id
    public static String getUnitId(int vehicleId, int driverId, int coDriverId, int specialStatus) {

        vehicleId = vehicleId + 1000000;
        driverId = driverId + 100000;
        coDriverId = coDriverId + 100000;

        String unitId = vehicleId + "" + driverId + "" + coDriverId + "" + specialStatus;
        return unitId;
    }

    // Created By: Deepak Sharma
    // Created Date: 17 Feb 2020
    // Purpose: set unit id
    public void setUnitId(int vehicleId, int driverId, int coDriverId, int specialStatus) {

        vehicleId = vehicleId + 1000000;
        driverId = driverId + 100000;
        coDriverId = coDriverId + 100000;

        String unitId = vehicleId + "" + driverId + "" + coDriverId + "" + specialStatus;
        String command = "AT$UNID=" + unitId + "\n";
        sendData(command);
    }

    private boolean settingUnitId = false;

    // Created By: Deepak Sharma
    // Created Date: 17 Feb 2020
    // Purpose: set unit id default
    public void setUnitId(int specialStatus) {
        int vehicleId = Utility.vehicleId;
        int driverId = Utility.activeUserId == 0 ? Utility.unIdentifiedDriverId : Utility.activeUserId;

        int coDriverId = 0;
        if (Utility.user2.getAccountId() > 0)
            // set co driverid
            coDriverId = Utility.user1.getAccountId() == driverId ? Utility.user2.getAccountId() : Utility.user1.getAccountId();


        settingUnitId = true;
        setUnitId(vehicleId, driverId, coDriverId, specialStatus);
    }

    @Override
    public void onDataTransmitted(TIOConnection tioConnection, int status, int bytesTransferred) {
        Log.i("AtrackDataTransmitt", "Transmit: Status: " + status + ", TotalBytes: " + bytesTransferred);
    }

    @Override
    public void onReadRemoteRssi(TIOConnection tioConnection, int i, int rssi) {

    }


    // for Disconnecting atrck
    public void disconnect() {
        try {
            mConnection.disconnect();
            setState(CanMessages.STATE_LISTEN);
            Utility.HutchConnectStatusFg = false;
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onPeripheralUpdate(TIOPeripheral tioPeripheral) {

    }

    public interface ICanMessage {
        void onELD2020Status(int status);

        void onAlertClear();

        void onAlertWarning();

        void onReportGet();

        void onVehicleMotionChange(boolean motionFg);

    }
}
