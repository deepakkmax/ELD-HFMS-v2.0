package com.hutchsystems.hutchconnect.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.InspectionCriteriaBean;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InspectionCriteriaAdapter extends ArrayAdapter<InspectionCriteriaBean> {
    private final Context context;
    private List<InspectionCriteriaBean> listInspection;
    public static ArrayList inspectionCheckList = new ArrayList<>();
    private int inspectionResult;
    String inspectionCriteriaList;


    public static class ViewHolder {
        public TextView tvInspectionTitle;
        public TextView tvInspectionSubTitle;
        public RadioGroup rgInspectionResult;
        public RadioButton rbPass;
        public RadioButton rbFail;
        public RadioButton rbNA;

        //public Button butViewInspection;
    }

    public InspectionCriteriaAdapter(Context context, List<InspectionCriteriaBean> values, String inspectionCriteriaList) {
        super(context, -1, values);

        this.context = context;
        this.listInspection = values;
        this.inspectionCriteriaList = inspectionCriteriaList;

        inspectionCheckList = new ArrayList<>();

        if (inspectionCriteriaList.isEmpty()) {
            for (int i = 0; i < listInspection.size(); i++) {
                inspectionCheckList.add(3);
            }
        }

    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        View rowView = convertView;
        try {


            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.inspection_criteria, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();

            viewHolder.tvInspectionTitle = (TextView) rowView.findViewById(R.id.title);
            viewHolder.tvInspectionSubTitle = (TextView) rowView.findViewById(R.id.subtitle);
            viewHolder.rgInspectionResult = (RadioGroup) rowView.findViewById(R.id.rgInspectionResult);
            viewHolder.rbPass = (RadioButton) rowView.findViewById(R.id.rbPass);
            viewHolder.rbFail = (RadioButton) rowView.findViewById(R.id.rbFail);
            viewHolder.rbNA = (RadioButton) rowView.findViewById(R.id.rbNA);

            //handling of reseting radio button when scroll down
            if (inspectionCriteriaList.isEmpty()) {
                int Postion = (int) inspectionCheckList.get(position);
                switch (Postion) {
                    case 0:
                        viewHolder.rbPass.setChecked(true);
                        break;

                    case 1:
                        viewHolder.rbFail.setChecked(true);
                        break;

                    case 2:
                        viewHolder.rbNA.setChecked(true);
                        break;
                }
            }

            // click From Inspection List
            if (!inspectionCriteriaList.isEmpty()) {
                for (View v : viewHolder.rgInspectionResult.getTouchables()) {
                    v.setClickable(false);
                }
                JSONObject inspectionObject = new JSONObject(inspectionCriteriaList);

                ((RadioButton) viewHolder.rgInspectionResult.getChildAt(Integer.valueOf(inspectionObject.get("" + position).toString()))).setChecked(true);
            }

            viewHolder.rgInspectionResult.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    // get selected radio button from radioGroup

                    switch (checkedId) {
                        case R.id.rbPass:
                            inspectionResult = 0;
                            break;

                        case R.id.rbFail:
                            inspectionResult = 1;
                            break;

                        case R.id.rbNA:
                            inspectionResult = 2;
                            break;
                    }
                    inspectionCheckList.set(position, inspectionResult);
                }
            });
            rowView.setTag(viewHolder);


            ViewHolder holder = (ViewHolder) rowView.getTag();

            holder.tvInspectionTitle.setText(listInspection.get(position).getTitle());
            holder.tvInspectionSubTitle.setText(listInspection.get(position).getSubTitle());


        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowView;
    }
}
