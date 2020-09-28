package com.hutchsystems.hutchconnect.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.beans.RuleBean;
import com.hutchsystems.hutchconnect.common.CustomDateFormat;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.fragments.ELogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Deepak Sharma on 2/9/2018.
 */

public class RuleAdapter extends RecyclerView.Adapter<RuleAdapter.ViewHolder> {
    ArrayList<RuleBean> data;
    List<String> listRules;

    private void getRules() {
        try {
            listRules = new ArrayList<>();
            listRules.add(Utility.context.getResources().getString(R.string.canada_rule_1));
            listRules.add(Utility.context.getResources().getString(R.string.canada_rule_2));
            listRules.add(Utility.context.getResources().getString(R.string.us_rule_1));
            listRules.add(Utility.context.getResources().getString(R.string.us_rule_2));
            listRules.add(Utility.context.getResources().getString(R.string.canada_rule_AB));
            listRules.add(Utility.context.getResources().getString(R.string.canada_logging_truck));
            listRules.add(Utility.context.getResources().getString(R.string.canada_oil_well_service));
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::getRules Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ELogFragment.class.getName(), "::getRules Error:", e.getMessage(), Utility.printStackTrace(e));
        }
    }

    public RuleAdapter(ArrayList<RuleBean> data) {
        this.data = data;
        getRules();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rule_row_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RuleBean bean = data.get(position);
        try {
            holder.tvDate.setText(new SimpleDateFormat("MMM dd,yyyy").format(bean.getRuleStartTime()));
            String format = "hh:mm a"; //12hr

            if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                format = "HH:mm";
            }
            holder.tvTime.setText(new SimpleDateFormat(format).format(bean.getRuleStartTime()));

            if (bean.getRuleEndTime() != null) {
                String endDate = Utility.format(bean.getRuleEndTime(), CustomDateFormat.dt5);
                holder.tvEndTime.setText("End Date: " + endDate);
            } else {
                holder.tvEndTime.setText("Current Rule");
            }

            int rowNumber = holder.getAdapterPosition() + 1;
            String rule = listRules.get(bean.getRuleId() - 1);
            holder.tvRule.setText(rule);
            holder.swSerialNo.setTextOff(rowNumber + "");
            holder.swSerialNo.setChecked(false);

        } catch (Exception exe) {

        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime, tvEndTime, tvRule;
        ToggleButton swSerialNo;

        public ViewHolder(View itemView) {
            super(itemView);

            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            tvEndTime = (TextView) itemView.findViewById(R.id.tvEndTime);
            tvRule = (TextView) itemView.findViewById(R.id.tvRule);
            swSerialNo = (ToggleButton) itemView.findViewById(R.id.swSerialNo);

            swSerialNo.setEnabled(false);
        }
    }
}
