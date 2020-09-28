package com.hutchsystems.hutchconnect.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;

import java.util.List;


public class BluetoothPairedDeviceAdapter extends ArrayAdapter<BluetoothDevice> {
    private ButtonClickListener mListener;

    private final Context context;

    private List<BluetoothDevice> listDevices;

    static class ViewHolder {
        public TextView tvBluetoothDevice;
        public ImageButton ibUnpair;
    }

    public BluetoothPairedDeviceAdapter(Context context, ButtonClickListener listener, List<BluetoothDevice> values) {
        super(context, -1, values);
        this.mListener = listener;
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
                rowView = inflater.inflate(R.layout.bluetooth_paired_item, null);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.tvBluetoothDevice = (TextView) rowView.findViewById(R.id.tvBluetoothDevice);
                viewHolder.ibUnpair = (ImageButton) rowView.findViewById(R.id.btnUnpair);
                viewHolder.ibUnpair.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("BluetoothAdapter", "Unpair clicked!");
                        if (mListener != null) {
                            mListener.onButtonClicked(listDevices.get(position));
                        }
                    }
                });
                rowView.setTag(viewHolder);
            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("BluetoothAdapter", "Item clicked!");
                }
            });


            // fill data
            Resources resources = context.getResources();

            ViewHolder holder = (ViewHolder) rowView.getTag();
            holder.tvBluetoothDevice.setText(listDevices.get(position).getName());
        } catch (Exception e) {
            LogFile.write(BluetoothPairedDeviceAdapter.class.getName() + "::getView Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            LogDB.writeLogs(BluetoothPairedDeviceAdapter.class.getName(),"::getView Error:",e.getMessage(),Utility.printStackTrace(e));

        }
        return rowView;
    }

    public interface ButtonClickListener {
        void onButtonClicked(BluetoothDevice device);
    }
}
