package com.hutchsystems.hutchconnect.adapters;

import android.content.Context;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.beans.CTPATInspectionBean;
import com.hutchsystems.hutchconnect.common.CustomDateFormat;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;

import java.util.List;


public class CTPATInspectionAdapter extends ArrayAdapter<CTPATInspectionBean> {
    private ItemClickListener mListener;

    private final Context context;

    private List<CTPATInspectionBean> listInspections;

    static class ViewHolder {
        public TextView tvDateTime;
        public TextView tvInfor1;
        public TextView tvInfor2;
        public ImageButton btnDelete;

        //public Button butViewInspection;
    }

    public CTPATInspectionAdapter(Context context, Fragment fragment, List<CTPATInspectionBean> values) {
        super(context, -1, values);
        this.mListener = (ItemClickListener) fragment;
        this.context = context;
        this.listInspections = values;
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        try {
            // reuse views
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.inspection_item, null);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.tvInfor1 = (TextView) rowView.findViewById(R.id.tvInfor1);
                viewHolder.tvInfor2 = (TextView) rowView.findViewById(R.id.tvInfor2);
                viewHolder.tvDateTime = (TextView) rowView.findViewById(R.id.tvTime);
                viewHolder.btnDelete=(ImageButton) rowView.findViewById(R.id.btnDelete);
                viewHolder.btnDelete.setVisibility(View.GONE);

                rowView.setTag(viewHolder);
            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.viewInspection(listInspections.get(position));
                    }
                }
            });

            ViewHolder holder = (ViewHolder) rowView.getTag();

            String info1 = "Type: ";
            String type = "";
            switch (listInspections.get(position).getType()) {
                case 0:
                    type = "Pre-Trip";
                    break;
                case 1:
                    type = "Pre-Arrival-Shipper";
                    break;
                case 2:
                    type = "Pre-Arrival-Border";
                    break;
            }
            info1 += type;
            info1 += ", By: ";
            info1 += listInspections.get(position).getDriverName();
            holder.tvInfor1.setText(info1);


            String trailerInfo= "Trailer: "+ listInspections.get(position).getTrailer()+ ","+ "Unit: " +  listInspections.get(position).getTruckNumber();

            String info2 = trailerInfo;
            holder.tvInfor2.setText(info2);

            String format = CustomDateFormat.dt5; //12hr
            if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                format = CustomDateFormat.dt6;
            }
            String tripDate = Utility.convertDate(listInspections.get(position).getDateTime(), format);
            holder.tvDateTime.setText(tripDate);


        } catch (Exception e) {
            LogFile.write(CTPATInspectionAdapter.class.getName() + "::getView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(CTPATInspectionAdapter.class.getName(),"::getView Error:",e.getMessage(),Utility.printStackTrace(e));

        }

        return rowView;
    }

    //callback to display selection item
    public interface ItemClickListener {
        void viewInspection(CTPATInspectionBean bean);

    }
}
