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
import com.hutchsystems.hutchconnect.beans.TripInspectionBean;
import com.hutchsystems.hutchconnect.common.CustomDateFormat;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;

import java.util.List;


public class InspectionAdapter extends ArrayAdapter<TripInspectionBean> {
    private ItemClickListener mListener;

    private final Context context;

    private List<TripInspectionBean> listInspections;

    View selectedView = null;

    static class ViewHolder {
        public TextView tvDateTime;
        public TextView tvInfor1;
        public TextView tvInfor2;
        public ImageButton btnDelete;

        //public Button butViewInspection;
    }

    public InspectionAdapter(Context context, Fragment fragment, List<TripInspectionBean> values) {
        super(context, -1, values);
        this.mListener = (ItemClickListener) fragment;
        this.context = context;
        this.listInspections = values;
    }

    public void changeItems(List<TripInspectionBean> list) {
        listInspections.clear();
        listInspections.addAll(list);

        notifyDataSetChanged();
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

                //Delete User Selected Record
                viewHolder.btnDelete=(ImageButton) rowView.findViewById(R.id.btnDelete);
                viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null) {
                            mListener.onClickOfDelete(view,position,listInspections.get(position));
                        }
                    }
                });

//                viewHolder.butViewInspection = (Button) rowView.findViewById(R.id.butViewInspection);
//                viewHolder.butViewInspection.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (mListener != null) {
//                            mListener.viewInspection(listInspections.get(position));
//                        }
//                    }
//                });
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
                    type = "Pre";
                    break;
                case 1:
                    type = "Inter";
                    break;
                case 2:
                    type = "Post";
                    break;
            }
            info1 += type;
            info1 += ", By: ";
            info1 += listInspections.get(position).getDriverName();
            holder.tvInfor1.setText(info1);

            String defect = "";
            if (listInspections.get(position).getDefect() == 0) {
                defect = "No defect";
            } else {
                if (listInspections.get(position).getDefectRepaired() == 1) {
                    defect = "Defect Repaired";
                } else {
                    defect = "Defects";
//                    if (listInspections.get(position).getSafeToDrive() == 1) {
//                        defect = "Safe to Drive";
//                    } else {
//                        defect = "Not Safe to Drive";
//                    }
                }
            }

            // Changes for Truck and Trailer DVIR
            String trailerInfo= listInspections.get(position).getTrailerDvirFg() == 1 ?  "Trailer: "+ listInspections.get(position).getTrailerNumber() : "Unit: " +  listInspections.get(position).getTruckNumber();

            String info2 = trailerInfo;
            info2 += ", Defect: ";
            info2 += defect;
            info2 += ", Location: ";
            info2 += listInspections.get(position).getLocation();
            holder.tvInfor2.setText(info2);

            String format = CustomDateFormat.dt5; //12hr
            if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                format = CustomDateFormat.dt6;
            }
            String tripDate = Utility.convertDate(listInspections.get(position).getInspectionDateTime(), format);
            holder.tvDateTime.setText(tripDate);


        } catch (Exception e) {
            LogFile.write(InspectionAdapter.class.getName() + "::getView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(InspectionAdapter.class.getName(),"::getView Error:",e.getMessage(),Utility.printStackTrace(e));

        }

        return rowView;
    }

    //callback to display selection item
    public interface ItemClickListener {
        void viewInspection(TripInspectionBean bean);
        void onClickOfDelete(View v,int postion,TripInspectionBean bean);
    }
}
