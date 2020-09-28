package com.hutchsystems.hutchconnect.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.DrayageDispatchBean;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.fragments.DrayageDispatchFragment.OnFragmentInteractionListener;

import java.util.ArrayList;

/**
 * Created by Deepak Sharma on 7/24/2017.
 */

public class DispatchAdapter extends RecyclerView.Adapter<DispatchAdapter.ViewHolder> {

    ArrayList<DrayageDispatchBean> data;
    OnFragmentInteractionListener mListener;

    public DispatchAdapter(ArrayList<DrayageDispatchBean> data, OnFragmentInteractionListener mListener) {
        this.data = data;
        this.mListener = mListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dispatch_row_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        final DrayageDispatchBean bean = data.get(position);
        holder.tvDispatchNo.setText(bean.getDispatchNo());
        holder.tvBookingNo.setText(bean.getBookingNo());
        holder.tvPickupPoint.setText("Pickup: " + bean.getPickupCompany() + " - " + bean.getPickupAddress());
        holder.tvDropPoint.setText("Drop: " + bean.getDropCompany() + " - " + bean.getDropAddress());
        if (!bean.getEmptyReturnAddress().isEmpty()) {
            holder.tvEmptyReturnPoint.setVisibility(View.VISIBLE);
            holder.tvEmptyReturnPoint.setText("Empty Return: " + bean.getEmptyReturnCompany() + " - " + bean.getEmptyReturnAddress());
        } else {
            holder.tvEmptyReturnPoint.setVisibility(View.GONE);
        }
        if (bean.getNotes().isEmpty()) {
            holder.tvNotes.setVisibility(View.GONE);
        } else {
            holder.tvNotes.setText(bean.getNotes());
            holder.tvNotes.setVisibility(View.VISIBLE);
        }
        String[] status = {"Pending", "In-Progress", "Completed"};
        holder.tvStatus.setText(status[bean.getStatus() - 1]);
        if (bean.getStatus() == 2) {
            holder.imgAction.setImageResource(R.drawable.in_progress_icon);
            holder.imgPlay.setImageResource(R.drawable.stop_dispatch);
        } else if (bean.getStatus() == 3) {
            holder.imgAction.setImageResource(R.drawable.completed);
            holder.imgPlay.setVisibility(View.GONE);
        } else
            holder.imgAction.setImageResource(R.drawable.pending_icon);

        holder.imgAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onDispatchDetail(bean.getDispatchId());
                }
            }
        });

        holder.imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    if (bean.getStatus() == 1) {
                        if (inProgressDispatchFg()) {
                            Utility.showAlertMsg("Please complete In-progress dispatch to start another dispatch!");
                        } else {
                            mListener.startDispatch(bean.getDispatchId());
                            bean.setStatus(2);
                            notifyItemChanged(position);
                        }
                    } else if (bean.getStatus() == 2) {
                        mListener.stopDispatch(bean.getDispatchId());
                        bean.setStatus(3);
                        notifyItemChanged(position);
                    }
                }
            }
        });
    }

    public boolean inProgressDispatchFg() {
        boolean status = false;
        for (DrayageDispatchBean bean : data) {
            if (bean.getStatus() == 2) {
                status = true;
                break;
            }
        }
        return status;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDispatchNo, tvBookingNo, tvPickupPoint, tvDropPoint, tvEmptyReturnPoint, tvNotes, tvStatus;
        ImageView imgAction, imgPlay;

        public ViewHolder(View view) {
            super(view);
            tvDispatchNo = (TextView) view.findViewById(R.id.tvDispatchNo);
            tvBookingNo = (TextView) view.findViewById(R.id.tvBookingNo);
            tvPickupPoint = (TextView) view.findViewById(R.id.tvPickupPoint);
            tvDropPoint = (TextView) view.findViewById(R.id.tvDropPoint);
            tvEmptyReturnPoint = (TextView) view.findViewById(R.id.tvEmptyReturnPoint);
            tvNotes = (TextView) view.findViewById(R.id.tvNotes);
            tvStatus = (TextView) view.findViewById(R.id.tvStatus);
            imgAction = (ImageView) view.findViewById(R.id.imgAction);
            imgPlay = (ImageView) view.findViewById(R.id.imgPlay);
        }
    }
}
