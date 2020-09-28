package com.hutchsystems.hutchconnect.tracklocations;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.common.CanMessages;
import com.hutchsystems.hutchconnect.common.GPSTracker;


public class GpsBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.i("ELog", "Boot completed");

        } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (mListner != null)
                mListner.deviceConnected(device);
            //showMessage(context, device.getName() + " connected");
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            showMessage(context, "Device disconnected: " + device.getName());
        } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
            showMessage(context, "Screen On ");

        } else if (Intent.ACTION_SHUTDOWN.equals(action)) {
            showMessage(context, "Shutdown starts ");

            try {
                ClientSocket obj = new ClientSocket(context, true);
                String signal = GPSTracker.getShutDownEvent();
                obj.execute(signal, "-1");
                Thread.sleep(5000);
            } catch (InterruptedException exe) {

            }
        } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                showMessage(context, " Hutch Connect is paired successfully");
                CanMessages.deviceAddress = device.getAddress();
                CanMessages.deviceName = device.getName();
               MainActivity.connectRequest = 59;
                if (mListner != null)
                    mListner.devicePaired(device);
            } else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                if (mListner != null)
                    mListner.deviceUnpaired(device);
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                if (mListner != null)
                    mListner.devicePairing(device);

            }
        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (mListner != null)
                mListner.deviceFound(device);

        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            if (mListner != null)
                mListner.discoveryStarted();
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            if (mListner != null)
                mListner.discoveryFinished();
        }
    }

    private void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static IBTBStatus mListner;

    public interface IBTBStatus {
        void deviceFound(BluetoothDevice device);

        void devicePaired(BluetoothDevice device);

        void devicePairing(BluetoothDevice device);

        void deviceUnpaired(BluetoothDevice device);

        void deviceConnected(BluetoothDevice device);

        void discoveryFinished();

        void discoveryStarted();
    }
}
