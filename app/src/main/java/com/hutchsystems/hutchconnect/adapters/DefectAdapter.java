package com.hutchsystems.hutchconnect.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;

import java.util.List;


public class DefectAdapter extends ArrayAdapter<String> {
    private ItemClickListener mListener;

    private final Context context;

    private List<String> listItems;

    View selectedView = null;

    static class ViewHolder {
        public TextView tvInfos;
        public CheckBox chItem;
    }

    public DefectAdapter(Context context, ItemClickListener listener, List<String> values) {
        super(context, -1, values);
        this.mListener = listener;
        this.context = context;
        this.listItems = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        try {
            // reuse views
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.defect_item, null);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.tvInfos = (TextView) rowView.findViewById(R.id.tvInformation);
                viewHolder.chItem = (CheckBox) rowView.findViewById(R.id.chItem);
                rowView.setTag(viewHolder);
            }

            // fill data
            Resources resources = context.getResources();

            final ViewHolder holder = (ViewHolder) rowView.getTag();
            holder.tvInfos.setText(listItems.get(position));

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("BluetoothAdapter", "Item clicked!");
                    if (mListener != null) {
                        mListener.onItemClicked(position);
                    }
                    if (holder.chItem.isChecked()) {
                        v.setBackgroundResource(R.color.white);
                    } else {
                        selectedView = v;
                        selectedView.setBackgroundResource(R.color.colorPrimary);
                    }
                }
            });
        } catch (Exception e) {
            LogFile.write(DefectAdapter.class.getName() + "::getView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(DefectAdapter.class.getName(),"::getView Error:",e.getMessage(),Utility.printStackTrace(e));

        }

        return rowView;
    }

    public interface ItemClickListener {
        void onItemClicked(int index);
    }
}
