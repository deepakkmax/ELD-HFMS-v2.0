package com.hutchsystems.hutchconnect.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.SetupVehicleInfoBean;
import com.hutchsystems.hutchconnect.common.Utility;

import java.util.ArrayList;

/**
 * Created by Deepak Sharma on 10/18/2017.
 */

public class SetupVehicleInfoAdapter extends ArrayAdapter<SetupVehicleInfoBean> {
    ArrayList<SetupVehicleInfoBean> data;


    public SetupVehicleInfoAdapter(Context context, int resource, ArrayList<SetupVehicleInfoBean> data) {
        super(context, resource, data);
        this.data = data;
    }


    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        final ViewHolderItem viewHolder;
        if (convertView == null || convertView.getTag() == null) {

            LayoutInflater inflater = (LayoutInflater) Utility.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(
                    R.layout.setup_vehicle_info_row_layout, parent,
                    false);
            viewHolder = new ViewHolderItem();
            viewHolder.tvData = (TextView) convertView.findViewById(R.id.tvData);
            viewHolder.tvValue = (TextView) convertView.findViewById(R.id.tvValue);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }
        SetupVehicleInfoBean bean = data.get(position);
        viewHolder.tvData.setText(bean.getData().toUpperCase());
        viewHolder.tvValue.setText(bean.getValue());
        return convertView;
    }

    static class ViewHolderItem {
        TextView tvData, tvValue;

    }
}
