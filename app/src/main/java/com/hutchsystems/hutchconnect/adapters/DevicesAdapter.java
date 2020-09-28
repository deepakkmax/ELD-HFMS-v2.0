package com.hutchsystems.hutchconnect.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.CarrierInfoBean;
import com.hutchsystems.hutchconnect.fragments.UnitNoListFragment;

import java.util.List;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.ViewHolder> {
    List<CarrierInfoBean> list;
    UnitNoListFragment unitNoListFragment;
    int pos = -1;
    public DevicesAdapter(List<CarrierInfoBean> data, UnitNoListFragment unitNoListFragment, Context activity) {
        this.list = data;
        this.unitNoListFragment = unitNoListFragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.devices_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if(position !=- 1){
            if(this.pos == position){
                holder.itemView.setBackgroundColor(unitNoListFragment.getResources().getColor(R.color.border));
            }else {
                holder.itemView.setBackgroundColor(unitNoListFragment.getResources().getColor(R.color.white));
            }
        }
        final CarrierInfoBean bean = list.get(position);

        holder.tvUnitNo.setText("Unit No.: "+bean.getUnitNo());
        holder.tvPlateNo.setText("Plate No.: "+bean.getPlateNo());
        holder.tvSerialNo.setText("Serial No.: "+bean.getSerailNo());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                pos = position;
                unitNoListFragment.updateObject(bean);
                notifyDataSetChanged();
            }
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUnitNo, tvPlateNo, tvSerialNo;

        public ViewHolder(View view) {
            super(view);
            tvUnitNo = (TextView) view.findViewById(R.id.tvUniNos);
            tvPlateNo = (TextView) view.findViewById(R.id.tvPlateNos);
            tvSerialNo = (TextView) view.findViewById(R.id.tvSerialNos);

        }
    }

    public static DocumentDetailAdapter.IDocument mlistner;

    public interface IDocument {
        void onClick(String id);
    }
}
