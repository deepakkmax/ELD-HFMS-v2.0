package com.hutchsystems.hutchconnect.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;

import java.util.List;


public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {


    private final Context context;
    private List<BluetoothDevice> listDevices;

    View selectedView;

    static class ViewHolder {
        public TextView tvBluetoothDevice, tvBTBMacAddress;
        public TextView tvInfos;
    }

    public BluetoothDeviceAdapter(Context context, List<BluetoothDevice> values) {
        super(context, -1, values);
        this.context = context;
        this.listDevices = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        try {
            // reuse views
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.bluetooth_item_, null);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.tvBluetoothDevice = (TextView) rowView.findViewById(R.id.tvBluetoothDevice);
                viewHolder.tvBTBMacAddress = (TextView) rowView.findViewById(R.id.tvBTBMacAddress);

                rowView.setTag(viewHolder);
            }

            final ViewHolder holder = (ViewHolder) rowView.getTag();
            holder.tvBluetoothDevice.setText(listDevices.get(position).getName());
            holder.tvBTBMacAddress.setText(listDevices.get(position).getAddress());
        } catch (Exception e) {
            LogFile.write(BluetoothPairedDeviceAdapter.class.getName() + "::getView Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            LogDB.writeLogs(BluetoothPairedDeviceAdapter.class.getName(),"::getView Error:",e.getMessage(),Utility.printStackTrace(e));

        }

        return rowView;
    }
}
