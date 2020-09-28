package com.hutchsystems.hutchconnect.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.beans.GeofenceBean;
import com.hutchsystems.hutchconnect.common.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Deepak Sharma on 5/12/2017.
 */

public class GeofenceAdapter extends RecyclerView.Adapter<GeofenceAdapter.ViewHolder> {

    ArrayList<GeofenceBean> data;

    public GeofenceAdapter(ArrayList<GeofenceBean> data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.geofence_row_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final GeofenceBean bean = data.get(position);
        holder.tvPortName.setText(bean.getPortRegionName());
        boolean regionInFg = (bean.getRegionInFg() == 1);
        holder.swSerialNo.setChecked(regionInFg);
        Date enterDate = Utility.parse(bean.getEnterDate());
        Date exitDate = Utility.parse(bean.getExitDate());
        if (regionInFg) {
            exitDate = Utility.parse(Utility.getCurrentDateTime());
        }

        int duration = (int) (exitDate.getTime() - enterDate.getTime()) / (1000 * 60);
        String time = "Duration: " + Utility.getTimeFromMinute(duration);
        holder.tvDuration.setText(time);
        try {
            holder.tvDate.setText(new SimpleDateFormat("MMM dd,yyyy").format(enterDate));
            String format = "hh:mm a"; //12hr
            if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                format = "HH:mm";
            }
            holder.tvTime.setText(new SimpleDateFormat(format).format(enterDate));
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPortName, tvDuration, tvTime, tvDate;
        ToggleButton swSerialNo;

        public ViewHolder(View view) {
            super(view);
            tvPortName = (TextView) view.findViewById(R.id.tvPortName);
            tvDuration = (TextView) view.findViewById(R.id.tvDuration);
            tvDate = (TextView) view.findViewById(R.id.tvDate);
            tvTime = (TextView) view.findViewById(R.id.tvTime);
            swSerialNo = (ToggleButton) view.findViewById(R.id.swSerialNo);
        }
    }
}
