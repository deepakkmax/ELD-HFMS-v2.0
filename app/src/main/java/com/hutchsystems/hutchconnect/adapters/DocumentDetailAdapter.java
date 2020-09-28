package com.hutchsystems.hutchconnect.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.beans.DocumentDetailBean;
import com.hutchsystems.hutchconnect.common.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Deepak Sharma on 6/8/2017.
 */

public class DocumentDetailAdapter extends RecyclerView.Adapter<DocumentDetailAdapter.ViewHolder> {
    ArrayList<DocumentDetailBean> data;

    public DocumentDetailAdapter(ArrayList<DocumentDetailBean> data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.document_row_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final DocumentDetailBean bean = data.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mlistner != null) {
                    mlistner.onClick(bean.getDocumentPath());
                }
            }
        });
        holder.tvDocumentType.setText(getDocType(bean.getDocumentType()));
        holder.tvDocumentNo.setText(": " + bean.getDocumentNo());
        try {
            holder.tvDate.setText(new SimpleDateFormat("MMM dd,yyyy").format(Utility.parse(bean.getCreatedDate())));
            String format = "hh:mm a"; //12hr
            if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                format = "HH:mm";
            }
            holder.tvTime.setText(new SimpleDateFormat(format).format(Utility.parse(bean.getCreatedDate())));
        } catch (Exception exe) {

        }
    }

    private String getDocType(int type) {
        String doc = "";
        switch (type) {
            case 0:
                doc = "BOL";
                break;
            case 1:
                doc = "POD";
                break;
            case 2:
                doc = "CustomDoc";
                break;
            case 3:
                doc = "MiscDoc";
                break;

        }
        return doc;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDocumentType, tvDocumentNo, tvDate, tvTime;

        public ViewHolder(View view) {
            super(view);
            tvDocumentType = (TextView) view.findViewById(R.id.tvDocumentType);
            tvDocumentNo = (TextView) view.findViewById(R.id.tvDocumentNo);
            tvDate = (TextView) view.findViewById(R.id.tvDate);
            tvTime = (TextView) view.findViewById(R.id.tvTime);
        }
    }

    public static IDocument mlistner;

    public interface IDocument {
        void onClick(String id);
    }
}
