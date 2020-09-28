package com.hutchsystems.hutchconnect.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AlertBean;
import com.hutchsystems.hutchconnect.common.Utility;

import java.util.ArrayList;

/**
 * Created by Deepak on 12/15/2016.
 */

public class ScoreCardThresholdAdapter extends ArrayAdapter<AlertBean> {

    ArrayList<AlertBean> data;

    public ScoreCardThresholdAdapter(int resource,
                                     ArrayList<AlertBean> data) {
        super(Utility.context, resource, data);
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
                    R.layout.score_card_threshold_row_layout, parent,
                    false);
            viewHolder = new ViewHolderItem();
            viewHolder.tvAlertName = (TextView) convertView.findViewById(R.id.tvAlertName);
            viewHolder.tvThreshold = (TextView) convertView.findViewById(R.id.tvThreshold);
            viewHolder.tvCurrentValue = (TextView) convertView.findViewById(R.id.tvCurrentValue);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        AlertBean bean = data.get(position);
        String currentValue = "N/A";
        if (bean.getCurrentValue() != "-99" && bean.getCurrentValue() != "-1") {
            currentValue = bean.getCurrentValue();
        }
        viewHolder.tvAlertName.setText(bean.getAlertName());
        viewHolder.tvCurrentValue.setText(currentValue);
        viewHolder.tvThreshold.setText(Utility.context.getString(R.string.threshold)+": " + bean.getThreshold());

        return convertView;
    }

    static class ViewHolderItem {
        TextView tvAlertName, tvThreshold, tvCurrentValue;
    }
}
