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
import com.hutchsystems.hutchconnect.beans.IncidentBean;
import com.hutchsystems.hutchconnect.common.BarCodeGenerator;
import com.hutchsystems.hutchconnect.common.DocumentType;
import com.hutchsystems.hutchconnect.common.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by SAMSUNG on 03-02-2017.
 */

public class IncidentAdapter extends RecyclerView.Adapter<IncidentAdapter.ViewHolder> {
    ArrayList<IncidentBean> data;
    public static IIncident mListner;
    Activity activity;
    public IncidentAdapter(ArrayList<IncidentBean> data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.incident_row_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final IncidentBean bean = data.get(position);


        String type = Utility.context.getString(R.string.notice_and_order);
        String level = "";
        String result = "";
        try {
            if (bean.getType() == 0) {

                level = " | " + Utility.context.getString(R.string.box) + (bean.getLevel() + 1);
                String[] resultNo = {Utility.context.getString(R.string.hos), Utility.context.getString(R.string.mechanical), Utility.context.getString(R.string.speeding), Utility.context.getString(R.string.seat_belt), Utility.context.getString(R.string.others)};
                result = resultNo[bean.getResult()];
            } else if (bean.getType() == 1) {

                type = Utility.context.getString(R.string.cvsa_inspection);
                level = " | " + Utility.context.getString(R.string.level) + (bean.getLevel() + 1);
                String[] resultCVSA = {Utility.context.getString(R.string.pass), Utility.context.getString(R.string.fail), Utility.context.getString(R.string.violation_present), Utility.context.getString(R.string.out_of_service), Utility.context.getString(R.string.present)};
                result = resultCVSA[bean.getResult()];
            } else {
                type = Utility.context.getString(R.string.violation_ticket);
                String[] resultCVSA = {Utility.context.getString(R.string.pass), Utility.context.getString(R.string.fail), Utility.context.getString(R.string.violation_present), Utility.context.getString(R.string.out_of_service), Utility.context.getString(R.string.present)};
                result = resultCVSA[bean.getResult()];

            }

        } catch (Exception exe) {

        }
        holder.tvUnitNo.setText(Utility.context.getString(R.string.unit_no) + ": " + Utility.UnitNo);
        holder.tvIncidentType.setText(type);
        holder.tvIncidentLevel.setText(level);
        holder.tvResult.setText(" | " + result);
        holder.tvLocation.setText(bean.getLocation());

        try {
            holder.tvDate.setText(new SimpleDateFormat("MMM dd,yyyy").format(Utility.parse(bean.getIncidentDate())));
            String format = "hh:mm a"; //12hr
            if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                format = "HH:mm";
            }
            holder.tvTime.setText(new SimpleDateFormat(format).format(Utility.parse(bean.getIncidentDate())));
        } catch (Exception exe) {

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListner != null) {
                    String docType = DocumentType.DOCUMENT_NOTICE_ORDER;
                    if (bean.getType() == 1) {
                        docType = DocumentType.DOCUMENT_CVSA_INSPECTION;
                    } else if (bean.getType() == 2) {
                        docType = DocumentType.DOCUMENT_VIOLATION_TICKET;
                    }

                    mListner.onViewIncident(bean.get_id(), bean.getType(), docType);
                }
            }
        });

        holder.ivQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListner != null) {
                    String docType = DocumentType.DOCUMENT_NOTICE_ORDER;


                    if (bean.getType() == 1) {
                        docType = DocumentType.DOCUMENT_CVSA_INSPECTION;
                    } else if (bean.getType() == 2) {
                        docType = DocumentType.DOCUMENT_VIOLATION_TICKET;
                    }

                    BarCodeGenerator.generateQrCode(activity,"Roadside Inspection",docType,
                            String.valueOf(bean.get_id()),bean.getIncidentDate());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIncidentType, tvIncidentState, tvIncidentLevel, tvUnitNo, tvResult, tvLocation, tvTime, tvDate;

        ImageView ivQrCode;
        public ViewHolder(View view) {
            super(view);
            tvIncidentType = (TextView) view.findViewById(R.id.tvIncidentType);

            tvIncidentState = (TextView) view.findViewById(R.id.tvIncidentState);
            tvIncidentLevel = (TextView) view.findViewById(R.id.tvIncidentLevel);
            tvUnitNo = (TextView) view.findViewById(R.id.tvUnitNo);
            tvResult = (TextView) view.findViewById(R.id.tvResult);
            tvLocation = (TextView) view.findViewById(R.id.tvLocation);


            tvDate = (TextView) view.findViewById(R.id.tvDate);
            tvTime = (TextView) view.findViewById(R.id.tvTime);
            tvLocation = (TextView) view.findViewById(R.id.tvLocation);

            ivQrCode =(ImageView) view.findViewById(R.id.ivIncidentQrCode);
        }
    }

    public interface IIncident {
        void onViewIncident(int id, int type, String docType);
    }
}
