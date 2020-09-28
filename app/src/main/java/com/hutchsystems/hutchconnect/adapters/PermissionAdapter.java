package com.hutchsystems.hutchconnect.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.PermissionBean;
import com.hutchsystems.hutchconnect.common.Utility;

import java.util.ArrayList;

/**
 * Created by Deepak Sharma on 10/10/2017.
 */

public class PermissionAdapter extends ArrayAdapter<PermissionBean> {
    ArrayList<PermissionBean> data;

    public PermissionAdapter(Context context, int resource, ArrayList<PermissionBean> data) {
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
                    R.layout.permission_row_layout, parent,
                    false);
            viewHolder = new ViewHolderItem();
            viewHolder.tvLabel = (TextView) convertView.findViewById(R.id.tvLabel);
            viewHolder.tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }
        PermissionBean bean = data.get(position);
        viewHolder.tvLabel.setText(bean.getLabel().toUpperCase());
        viewHolder.tvDescription.setText(bean.getDescription());
        return convertView;
    }

    static class ViewHolderItem {
        TextView tvLabel, tvDescription;

    }
}
