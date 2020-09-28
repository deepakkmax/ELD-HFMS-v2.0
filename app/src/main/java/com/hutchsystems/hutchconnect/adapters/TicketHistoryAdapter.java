package com.hutchsystems.hutchconnect.adapters;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.TicketBean;

import java.util.ArrayList;


public class TicketHistoryAdapter extends RecyclerView.Adapter<TicketHistoryAdapter.ViewHolder> {
    ArrayList<TicketBean> data;
    String[] status = {"New", "Assigned", "In-Progress", "Pending", "Resolved", "Re-Opened", "Closed"};
    private View itemView;
    ItemClickListener listner;
    public TicketHistoryAdapter(ArrayList<TicketBean> data, Fragment fragment) {
        this.data = data;
        this.listner = (TicketHistoryAdapter.ItemClickListener) fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ticket_row_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final TicketBean bean = data.get(position);

        holder.tvticketDiscription.setText(bean.getTitle());
        holder.tvTicketNO.setText("Ticket No-: " + bean.getTicketNo());
        holder.tvticketStatus.setText(TicketStatusGet(bean.getTicketStatus()));
        //  only non Empty Feedback will be shown

        if(bean.getUserFeedback()!=null&&!bean.getUserFeedback().isEmpty())
        {
            holder.tvticketUserFeedback.setText(bean.getUserFeedback());
            holder.tvticketUserFeedback.setVisibility(View.VISIBLE);
        }else
        {
            holder.tvticketUserFeedback.setVisibility(View.GONE);
        }

        try {
            // rating greater than 0 then we will Display rating bar
            if(bean.getRating() >0 )
            {
                holder.ratingBar.setRating(bean.getRating());
                holder.ratingBar.setVisibility(View.VISIBLE);
                holder.imgfeedback.setVisibility(View.GONE);
            //rating 0 and ticketStatus is Closed or Resolved then we will display rating icon
            }else if(bean.getRating()==0 &&( bean.getTicketStatus()==5 || bean.getTicketStatus()==7))
            {
                holder.imgfeedback.setVisibility(View.VISIBLE);
                holder.ratingBar.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  if(listner!=null)
                  {
                      //rating 0 and ticketStatus is Closed or Resolved then Feedbackpopup show
                      if(bean.getRating()==0 &&( bean.getTicketStatus()==5 || bean.getTicketStatus()==7))
                      {
                          listner.showFeedbackDialog(bean);
                      }
                  }
                }
            });

        } catch (Exception exe) {

        }
    }

    public String TicketStatusGet(int s) {

        return status[s - 1];
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvticketDiscription, tvTicketNO, tvticketStatus,tvticketUserFeedback;
        ImageView imgfeedback;
        RatingBar ratingBar;

        public ViewHolder(View view) {
            super(view);
            tvTicketNO = (TextView) view.findViewById(R.id.tvTicketID);
            tvticketDiscription = (TextView) view.findViewById(R.id.tvticketDiscription);
            tvticketStatus = (TextView) view.findViewById(R.id.tvticketStatus);
            tvticketUserFeedback=(TextView)view.findViewById(R.id.tvticketUserFeedback);
            ratingBar = (RatingBar) view.findViewById(R.id.ratingbar);
            imgfeedback=(ImageView)view.findViewById(R.id.imgfeedback);

        }
    }

    //callback to display selection item
    public interface ItemClickListener {
        void showFeedbackDialog(TicketBean bean);
    }


}
