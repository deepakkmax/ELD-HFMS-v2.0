package com.hutchsystems.hutchconnect.adapters;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.beans.NotificationBean;
import com.hutchsystems.hutchconnect.common.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Pallavi.
 */

public class NotiicationRecycleAdapter extends RecyclerView.Adapter<NotiicationRecycleAdapter.ViewHolder> {

    public static IViewHolder mListner;
    View selectedView = null;
    NotiicationRecycleAdapter.ViewHolder selectedHolder = null;

    Context context;
    private ArrayList<NotificationBean> data;
    private View itemView;

    public NotiicationRecycleAdapter(ArrayList<NotificationBean> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_row_layout, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        NotificationBean bean = data.get(position);
        holder.tvNotificationComment.setText(bean.getComment());
        holder.tvNotificationTitle.setText(bean.getTitle());

        try {
            holder.tvDate.setText(new SimpleDateFormat("MMM dd,yyyy").format(bean.getNotiFicationDate()));
            String format = "hh:mm a"; //12hr
            if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                format = "HH:mm";
            }
            holder.tvTime.setText(new SimpleDateFormat(format).format(bean.getNotiFicationDate()));
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
        }

        float distanceOut = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, -1,
                context.getResources().getDisplayMetrics()
        );
        holder.layoutSlide.animate().translationX(distanceOut).setDuration(1).start();
        holder.layoutSlide.bringToFront();
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.i("Event", "button Edit clicked");
                if (mListner != null) {
                    mListner.onItemClick(v, position);
                }
            }
        });
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                float distanceIn = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 63,
                        context.getResources().getDisplayMetrics()
                );
                float distanceOut = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, -1,
                        context.getResources().getDisplayMetrics()
                );

                if (selectedHolder != null) {
                    //unselect
                    selectedHolder.layoutSlide.animate().translationX(distanceOut).setDuration(500).start();
                }
                selectedHolder = holder;

                if (selectedView != v) {
                    holder.btnDelete.setVisibility(View.VISIBLE);
                    holder.layoutSlide.animate().translationX(distanceIn).setDuration(500).start();
                    selectedView = v;
                } else {
                    holder.layoutSlide.animate().translationX(distanceOut).setDuration(500).start();
                    //holder.butEditEvent.setVisibility(View.INVISIBLE);
                    selectedView = null;
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static interface IViewHolder {
        public void onItemClick(View view, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageButton btnDelete;
        public LinearLayout layoutSlide, layoutInformation;
        TextView tvNotificationComment, tvDate, tvTime, tvNotificationTitle;

        public ViewHolder(View view) {
            super(view);
            tvNotificationComment = (TextView) view.findViewById(R.id.tvNotificationComment);
            tvNotificationTitle = (TextView) view.findViewById(R.id.tvNotificationTitle);
            tvDate = (TextView) view.findViewById(R.id.tvDate);
            btnDelete = (ImageButton) view.findViewById(R.id.btnDelete);
            layoutSlide = (LinearLayout) view.findViewById(R.id.layout_slide);
            layoutInformation = (LinearLayout) view.findViewById(R.id.layoutInformation);
            tvTime = (TextView) view.findViewById(R.id.tvTime);
        }
    }
}
