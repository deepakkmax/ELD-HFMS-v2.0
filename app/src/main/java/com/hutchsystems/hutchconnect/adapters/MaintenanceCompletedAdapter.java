package com.hutchsystems.hutchconnect.adapters;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.VehicleMaintenanceBean;
import com.hutchsystems.hutchconnect.common.BarCodeGenerator;
import com.hutchsystems.hutchconnect.common.DocumentType;
import com.hutchsystems.hutchconnect.common.Utility;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Deepak Sharma on 4/26/2017.
 */
public class MaintenanceCompletedAdapter extends RecyclerView.Adapter<MaintenanceCompletedAdapter.ViewHolder> {
    ArrayList<VehicleMaintenanceBean> data;
    Activity activity;
    public MaintenanceCompletedAdapter(ArrayList<VehicleMaintenanceBean> data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.completed_maintenance_row_layout, parent, false);

        return new ViewHolder(itemView);
    }

    String[] items = {"Body", "Brake System", "Driveline/Axle", "Engine", "Lighting", "Safety Equipment", "Suspension System", "Tires", "Transmission"};

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final VehicleMaintenanceBean bean = data.get(position);

        Date date = Utility.parse(bean.getMaintenanceDate());
        String day = date.getDay() + " ";
        String month = Utility.format(date, "MMM yyyy");

        holder.tvDate.setText(day + " " + month);
        holder.tvTitle.setText("Unit No: " + bean.getUnitNo() + " | Item: " + items[bean.getItemId()] + " | Schedule: " + bean.getSchedule());
        holder.tvSubTitle.setText("Odometer: " + bean.getOdometerReading() + " | Invoice No: " + bean.getInvoiceNo());

        double totalCost = Double.parseDouble(bean.getPartCost()) + Double.parseDouble(bean.getLabourCost());
        String cost = String.format("Total Cost: $%.2f", totalCost);
        holder.tvCost.setText(cost);
        /*holder.swSerialNo.setTextOff((position + 1) + "");
        holder.swSerialNo.setChecked(bean.isChecked());*/
        holder.ivQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BarCodeGenerator.generateQrCode(activity,"Maintenance Completed", DocumentType.DOCUMENT_MAINTENANCE,
                        String.valueOf(bean.getId()),bean.getMaintenanceDate());
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTitle, tvSubTitle, tvCost;
        ToggleButton swSerialNo;
        ImageView ivQrCode;

        public ViewHolder(View view) {
            super(view);
            tvDate = (TextView) view.findViewById(R.id.tvDate);
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            tvSubTitle = (TextView) view.findViewById(R.id.tvSubTitle);
            tvCost = (TextView) view.findViewById(R.id.tvCost);
            swSerialNo = (ToggleButton) view.findViewById(R.id.swSerialNo);
            ivQrCode = (ImageView) view.findViewById(R.id.ivCompleteMaintenanceQrCode);
        }
    }

    public ArrayList<VehicleMaintenanceBean> getData() {
        return data;
    }

}
