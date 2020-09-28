package com.hutchsystems.hutchconnect.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.beans.DispatchDetailBean;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.fragments.DispatchDetailFragment.OnFragmentInteractionListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Deepak Sharma on 7/25/2017.
 */

public class DispatchDetailAdapter extends RecyclerView.Adapter<DispatchDetailAdapter.ViewHolder> {

    ArrayList<DispatchDetailBean> data;
    OnFragmentInteractionListener mListener;
    String[] pickDropList;

    public DispatchDetailAdapter(ArrayList<DispatchDetailBean> data, String[] pickDropList, OnFragmentInteractionListener mListener) {
        this.data = data;
        this.mListener = mListener;
        this.pickDropList = pickDropList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dispatch_detail_row_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final DispatchDetailBean bean = data.get(position);

        //holder.tvSerialNo.setText((position + 1) + "");
        if (bean.getContainerNo().isEmpty())
            holder.tvAppointmentNo.setText("Appointment No: " + (bean.getAppointmentNo().isEmpty() ? "N/A" : bean.getAppointmentNo()));
        else
            holder.tvAppointmentNo.setText("Appointment No: " + (bean.getAppointmentNo().isEmpty() ? "N/A" : bean.getAppointmentNo()) + " | Container No: " + bean.getContainerNo());
        holder.tvAppointmentFor.setText(pickDropList[bean.getPickDropId()]);
        holder.tvContainerType.setText("Container Type: " + bean.getContainerType());
        if (bean.getContainerGrade().isEmpty()) {
            holder.tvContainerGrade.setVisibility(View.GONE);
        } else {
            holder.tvContainerGrade.setVisibility(View.VISIBLE);
            holder.tvContainerGrade.setText("Container Grade: " + bean.getContainerGrade());
        }

        if (bean.getContainerSize().isEmpty()) {
            holder.tvContainerSize.setVisibility(View.GONE);
        } else {
            holder.tvContainerSize.setVisibility(View.VISIBLE);
            holder.tvContainerSize.setText("Container Size: " + bean.getContainerSize());
        }
        if (bean.getPickDropId() == 0) {
            bean.setPickDropId(1);
        }
        String[] status = {"Pickup", "Drop", "Return"};
        holder.tvStatus.setText(status[bean.getPickDropId() - 1]);
        if (bean.getPickDropId() == 2) {
            holder.imgAction.setImageResource(R.drawable.drop_big_icon);
        } else if (bean.getPickDropId() == 3) {
            holder.imgAction.setImageResource(R.drawable.empty_big_icon);
        } else
            holder.imgAction.setImageResource(R.drawable.pickup_big_icon);
        if (bean.getAppointmentDate() != null && !bean.getAppointmentDate().isEmpty()) {
            try {
                String format = "hh:mm a"; //12hr
                if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                    format = "HH:mm";
                }
                holder.tvAppointmentTime.setText(new SimpleDateFormat(format).format(Utility.parse(bean.getAppointmentDate())));
            } catch (Exception exe) {

            }
        } else {
            holder.tvAppointmentTime.setText("");
        }
        holder.imgAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onAddContainerDetail(bean.getDispatchDetailId());
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSerialNo, tvAppointmentNo, tvAppointmentFor, tvContainerType, tvContainerGrade, tvContainerSize, tvAppointmentTime, tvStatus;
        ImageView imgAction;

        public ViewHolder(View view) {
            super(view);
            // tvSerialNo = (TextView) view.findViewById(R.id.tvSerialNo);
            tvAppointmentNo = (TextView) view.findViewById(R.id.tvAppointmentNo);
            tvAppointmentFor = (TextView) view.findViewById(R.id.tvAppointmentFor);
            tvContainerType = (TextView) view.findViewById(R.id.tvContainerType);
            tvContainerGrade = (TextView) view.findViewById(R.id.tvContainerGrade);
            tvContainerSize = (TextView) view.findViewById(R.id.tvContainerSize);
            tvAppointmentTime = (TextView) view.findViewById(R.id.tvAppointmentTime);
            tvStatus = (TextView) view.findViewById(R.id.tvStatus);
            imgAction = (ImageView) view.findViewById(R.id.imgAction);
        }
    }
}
