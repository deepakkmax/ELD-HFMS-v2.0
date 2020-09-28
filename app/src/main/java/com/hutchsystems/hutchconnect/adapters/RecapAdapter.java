package com.hutchsystems.hutchconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.RecapBean;
import com.hutchsystems.hutchconnect.common.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class RecapAdapter extends RecyclerView.Adapter<RecapAdapter.ViewHolder> {

    ArrayList<RecapBean> data;

    public RecapAdapter(ArrayList<RecapBean> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recap_row_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecapBean bean = data.get(position);

        holder.tvDate.setText(new SimpleDateFormat("MMM dd,yyyy").format(bean.getDate()));
        holder.tvDriving.setText(Utility.getTimeFromSecondsInMin(bean.getDriving()));
        holder.tvOnDuty.setText(Utility.getTimeFromSecondsInMin(bean.getOnDuty()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDriving, tvOnDuty;
        ToggleButton swSerialNo;

        public ViewHolder(View itemView) {
            super(itemView);

            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            tvDriving = (TextView) itemView.findViewById(R.id.tvDriving);
            tvOnDuty = (TextView) itemView.findViewById(R.id.tvOnDuty);
        }
    }
}
