package com.hutchsystems.hutchconnect.adapters;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.beans.FuelDetailBean;
import com.hutchsystems.hutchconnect.common.BarCodeGenerator;
import com.hutchsystems.hutchconnect.common.DocumentType;
import com.hutchsystems.hutchconnect.common.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by SAMSUNG on 01-02-2017.
 */

public class FuelDetailAdapter extends RecyclerView.Adapter<FuelDetailAdapter.ViewHolder> {
    ArrayList<FuelDetailBean> data;
    public static IFuel mlistner;
    Activity activity;
    public FuelDetailAdapter(ArrayList<FuelDetailBean> data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fuel_row_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final FuelDetailBean bean = data.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mlistner != null) {
                    mlistner.onClick(bean.get_id());
                }

            }
        });

        holder.tvCardNo.setText(": " + bean.getCardNo());
        holder.tvFuel.setText(": " + bean.getQuantity()+" "  + (bean.getFuelUnit() == 1 ? Utility.context.getString(R.string.litres) : Utility.context.getString(R.string.gallons)));
        if(bean.getPrice()!=null && !bean.getPrice().isEmpty()) {
            holder.tvAmountLabel.setVisibility(View.VISIBLE);
            holder.tvPrice.setVisibility(View.VISIBLE);
            holder.tvPrice.setText(bean.getPrice().isEmpty() ? ": " : ": $" + bean.getPrice());
        }else {
            holder.tvPrice.setVisibility(View.GONE);
            holder.tvAmountLabel.setVisibility(View.GONE);
        }
//        holder.tvDPFFuel.setText(": " + (bean.getDEFFuelQuantity().isEmpty() ? "N/A" : bean.getDEFFuelQuantity() +" " + (bean.getFuelUnit() == 1 ? Utility.context.getString(R.string.litres) : Utility.context.getString(R.string.gallons))));
//        holder.tvDPFPrice.setText(": " + (bean.getDEFFuelPrice().isEmpty() ? "N/A" : "$" + bean.getDEFFuelPrice()));
//        holder.tvCashAdvance.setText(": " + (bean.getCashAdvance().isEmpty() ? "N/A" : "$" + bean.getCashAdvance()));
        holder.tvLocation.setText(bean.getLocation());

        try {
            holder.tvDate.setText(new SimpleDateFormat("MMM dd,yyyy").format(Utility.parse(bean.getFuelDateTime())));
            String format = "hh:mm a"; //12hr
            if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                format = "HH:mm";
            }
            holder.tvTime.setText(new SimpleDateFormat(format).format(Utility.parse(bean.getFuelDateTime())));
        } catch (Exception exe) {
exe.printStackTrace();
        }

        holder.ivQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BarCodeGenerator.generateQrCode(activity,"Fuel", DocumentType.DOCUMENT_MAINTENANCE,
                        String.valueOf(bean.get_id()),bean.getFuelDateTime());
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCardNo, tvFuel, tvPrice, tvTime,   tvDate, tvLocation, tvAmountLabel;
        TextView tvDPFFuel, tvDPFPrice, tvCashAdvance;
        ImageView ivQrCode;
        public ViewHolder(View view) {
            super(view);
            tvCardNo = (TextView) view.findViewById(R.id.tvCardNo);
            tvAmountLabel = (TextView) view.findViewById(R.id.tvAmountLabel);

            tvFuel = (TextView) view.findViewById(R.id.tvFuel);
            tvPrice = (TextView) view.findViewById(R.id.tvPrice);
            tvTime = (TextView) view.findViewById(R.id.tvTime);
//            tvDPFFuel = (TextView) view.findViewById(R.id.tvDPFFuel);
//            tvDPFPrice = (TextView) view.findViewById(R.id.tvDPFPrice);
//
//
//            tvCashAdvance = (TextView) view.findViewById(R.id.tvCashAdvance);
            tvDate = (TextView) view.findViewById(R.id.tvDate);
            tvLocation = (TextView) view.findViewById(R.id.tvLocation);
            ivQrCode = (ImageView) view.findViewById(R.id.ivQrCode);
        }
    }

    public interface IFuel {
        void onClick(int id);
    }
}
