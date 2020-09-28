package com.hutchsystems.hutchconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.DailyLogBean;
import com.hutchsystems.hutchconnect.common.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DataUnPostedAdapter extends RecyclerView.Adapter<DataUnPostedAdapter.ViewHolder> {

    ArrayList<DailyLogBean> data;

    public DataUnPostedAdapter(ArrayList<DailyLogBean> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.unposted_data_row_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyLogBean bean = data.get(position);
        try {
            holder.swSerialNo.setTextOff((position + 1) + "");
            holder.swSerialNo.setChecked(position == 0);
            holder.tvDate.setText(new SimpleDateFormat("MMM dd,yyyy").format(Utility.parse(bean.getLogDate())));
        } catch (Exception exe) {
            String message = exe.getMessage();
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        ToggleButton swSerialNo;

        public ViewHolder(View itemView) {
            super(itemView);

            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            swSerialNo = (ToggleButton) itemView.findViewById(R.id.swSerialNo);
        }
    }
}
