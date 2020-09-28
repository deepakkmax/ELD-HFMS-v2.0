package com.hutchsystems.hutchconnect.adapters;

import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.ScheduleBean;
import com.hutchsystems.hutchconnect.common.BarCodeGenerator;
import com.hutchsystems.hutchconnect.common.CustomDateFormat;
import com.hutchsystems.hutchconnect.common.DocumentType;
import com.hutchsystems.hutchconnect.common.Utility;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Deepak Sharma on 2/3/2017.
 */

public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.ViewHolder> {
    ArrayList<ScheduleBean> data;
    Activity activity;
    public MaintenanceAdapter(ArrayList<ScheduleBean> data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.maintenance_row_layout, parent, false);

        return new ViewHolder(itemView);
    }

    ToggleButton prevToggle;

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ScheduleBean bean = data.get(position);
        holder.tvScheduleName.setText(bean.getSchedule());
        holder.tvThreshold.setText(" Every " + bean.getThreshold() + " " + bean.getUnit());
        try {
            int dueOn = bean.getDueOn();
            String dueValue = dueOn + " " + bean.getUnit();
            if (bean.getUnit().equals("Days")) {
                long totalMillisecond = dueOn * 1000l * 60l * 60l * 24l;
                Date dueDate = new Date(totalMillisecond);
                dueValue = Utility.format(dueDate, CustomDateFormat.d10);
            }

            String maintenance = "";
            if (bean.getDueStatus() == 2) {
                maintenance = Utility.context.getString(R.string.maintenance_due) + "- " + dueValue;
            } else {
                maintenance = Utility.context.getString(R.string.upcoming_maintenance) + "- " + dueValue;
            }
            holder.tvMaintenance.setText(maintenance);
            holder.swSerialNo.setTextOff((position + 1) + "");
            holder.swSerialNo.setChecked(bean.isChecked());
            holder.swSerialNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ToggleButton toggle = (ToggleButton) view;
                    if (toggle.isChecked() && mListner != null) {
                        mListner.selectItem(position);
                    }
                    else
                        mListner.selectItem(-1);
                }
            });
        } catch (Exception exe) {

        }
        holder.ivQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BarCodeGenerator.generateQrCode(activity,"Upcoming/Due Maintenance ", DocumentType.DOCUMENT_MAINTENANCE,
                        String.valueOf(bean.get_id()),bean.getEffectiveDate());
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvThreshold, tvScheduleName, tvMaintenance;
        ToggleButton swSerialNo;
        ImageView ivQrCode;
        public ViewHolder(View view) {
            super(view);
            tvMaintenance = (TextView) view.findViewById(R.id.tvMaintenance);
            tvScheduleName = (TextView) view.findViewById(R.id.tvScheduleName);
            tvThreshold = (TextView) view.findViewById(R.id.tvThreshold);
            swSerialNo = (ToggleButton) view.findViewById(R.id.swSerialNo);
            ivQrCode = (ImageView) view.findViewById(R.id.ivMaintenanceQrCode);
        }
    }

    public ArrayList<ScheduleBean> getData() {
        return data;
    }

    public static IVehicleMaintenance mListner;

    public interface IVehicleMaintenance {
        void selectItem(int position);
    }
}
