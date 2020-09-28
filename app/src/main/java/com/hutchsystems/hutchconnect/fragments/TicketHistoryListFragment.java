package com.hutchsystems.hutchconnect.fragments;


import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.TicketHistoryAdapter;
import com.hutchsystems.hutchconnect.beans.TicketBean;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.HelpDB;
import com.hutchsystems.hutchconnect.db.LogDB;

import java.util.ArrayList;

public class TicketHistoryListFragment extends Fragment implements TicketHistoryAdapter.ItemClickListener,FeedbackDialogFragment.FeedbackDialogInterface {
    RecyclerView rvTickethistory;
    TicketHistoryAdapter adapter;
    private ArrayList<TicketBean> ticketList = new ArrayList<>();

    public TicketHistoryListFragment() {
        // Required empty public constructor
    }


    public static TicketHistoryListFragment newInstance() {
        TicketHistoryListFragment fragment = new TicketHistoryListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ticket_history_list, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {

        rvTickethistory = (RecyclerView) view.findViewById(R.id.rvTickethistory);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvTickethistory.setLayoutManager(layoutManager);
        rvTickethistory.setItemAnimator(new DefaultItemAnimator());


        // get the ticket history list from the DB
        getTicketHistoryList();

    }

    // Purpose : get Ticket History List from the DB and set in to adapter
    private void getTicketHistoryList() {
        ticketList = HelpDB.GetAllTicket();
        adapter = new TicketHistoryAdapter(ticketList,this);
        rvTickethistory.setAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    // Purpose : On click of the list show Feedback Dialog

    @Override
    public void showFeedbackDialog(TicketBean bean) {
        try {


            FragmentManager manager = getFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();

            Fragment prev = manager.findFragmentByTag("rating_dialog");
            if (prev != null) {
                ft.remove(prev);
                ft.commitNow();
                ft = manager.beginTransaction();
            }

            FeedbackDialogFragment ratingDialog = FeedbackDialogFragment.newInstance();
            ratingDialog.mListener=this;
            ratingDialog.setTicketID(bean.getTicketId());
            ratingDialog.setTicketTitle(bean.getTitle());
            ratingDialog.setTicketNo(bean.getTicketNo());
            ratingDialog.show(ft, "rating_dialog");

        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::showFeedbackDialog Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            e.printStackTrace();
            LogDB.writeLogs(TicketHistoryListFragment.class.getName(),"showFeedbackDialog",e.getMessage(), Utility.printStackTrace(e));

        }
    }

    @Override
    public void onFeedbackDissmisDialog() {
        if(adapter!=null)
        {
            ticketList.clear();
            ticketList.addAll(HelpDB.GetAllTicket());
            adapter.notifyDataSetChanged();
        }
    }
}
