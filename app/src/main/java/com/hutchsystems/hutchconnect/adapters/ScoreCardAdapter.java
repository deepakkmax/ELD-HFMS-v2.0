package com.hutchsystems.hutchconnect.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AlertBean;
import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.common.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Deepak on 12/15/2016.
 */

public class ScoreCardAdapter extends ArrayAdapter<AlertBean> {

    ArrayList<AlertBean> data;

    public ScoreCardAdapter(int resource,
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
                    R.layout.score_card_row_layout, parent,
                    false);
            viewHolder = new ViewHolderItem();
            viewHolder.tvAlertName = (TextView) convertView.findViewById(R.id.tvAlertName);
            viewHolder.tvDuration = (TextView) convertView.findViewById(R.id.tvDuration);
            viewHolder.tvScore = (TextView) convertView.findViewById(R.id.tvScore);
            viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        AlertBean bean = data.get(position);
        viewHolder.tvAlertName.setText(bean.getAlertName());
        viewHolder.tvScore.setText(Utility.context.getString(R.string.score_deducted)+": " + bean.getScores());
        if (bean.getDuration() > 0) {
            String duration = Utility.getTimeFromMinute(bean.getDuration());
            viewHolder.tvDuration.setText(Utility.context.getString(R.string.duration)+": " + duration);
            viewHolder.tvDuration.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tvDuration.setVisibility(View.GONE);
        }

        try {
            viewHolder.tvDate.setText(new SimpleDateFormat("MMM dd,yyyy").format(Utility.parse(bean.getAlertDateTime())));
            String format = "hh:mm a"; //12hr
            if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                format = "HH:mm";
            }
            viewHolder.tvTime.setText(new SimpleDateFormat(format).format(Utility.parse(bean.getAlertDateTime())));
        } catch (Exception exe) {

        }
        return convertView;
    }

    static class ViewHolderItem {
        TextView tvAlertName, tvDuration, tvScore, tvDate, tvTime;
    }
}
