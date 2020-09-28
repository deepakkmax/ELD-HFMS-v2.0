package com.hutchsystems.hutchconnect.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.Utility;

/**
 * Created by Deepak Sharma on 10/23/2017.
 */

public class SetupCompleteAdapter extends ArrayAdapter<String> {
    String[] data;


    public SetupCompleteAdapter(Context context, int resource, String[] data) {
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
                    R.layout.row_setup_complete, parent,
                    false);
            viewHolder = new ViewHolderItem();
            viewHolder.tvData = (TextView) convertView.findViewById(R.id.tvData);

        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        viewHolder.tvData.setText(data[position]);

        return convertView;
    }

    static class ViewHolderItem {
        TextView tvData;

    }
}

