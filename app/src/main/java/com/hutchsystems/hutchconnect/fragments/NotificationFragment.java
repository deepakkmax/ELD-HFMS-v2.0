package com.hutchsystems.hutchconnect.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.NotiicationRecycleAdapter;
import com.hutchsystems.hutchconnect.beans.NotificationBean;
import com.hutchsystems.hutchconnect.db.NotificationDB;

import java.util.ArrayList;

public class NotificationFragment extends Fragment implements NotiicationRecycleAdapter.IViewHolder {

    ArrayList<NotificationBean> list;
    NotiicationRecycleAdapter adapter;
    private RecyclerView rvnotification;
    private LinearLayoutManager mLayoutManager;

    public static NotificationFragment newInstance() {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        initialize(view);
        return view;
    }



    private void initialize(View view) {
        rvnotification = (RecyclerView) view.findViewById(R.id.rvnotificationList);
        NotiicationRecycleAdapter.mListner = this;

        mLayoutManager = new LinearLayoutManager(getContext());
        rvnotification.setLayoutManager(mLayoutManager);
        rvnotification.setItemAnimator(new DefaultItemAnimator());

        // get the list of Notification
        list = NotificationDB.getNotification();
        adapter = new NotiicationRecycleAdapter(list,getActivity());
        rvnotification.setAdapter(adapter);

    }


    @Override
    public void onItemClick(final View view,final int position) {

        final AlertDialog ad = new AlertDialog.Builder(getContext())
                .create();
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle(getString(R.string.notification_alert_title));
        ad.setIcon(R.drawable.ic_launcher);
        ad.setMessage(getString(R.string.notification_alert_message));

        ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {


                       // int position = rvnotification.getChildLayoutPosition(view);
                        // get the position of the the list and delete that item which is user selected
                        NotificationBean bean = list.get(position);
                        int notificationID = bean.getNotificationID();
                        NotificationDB.DeleteNotification(notificationID);
                        list.clear();
                        list.addAll(NotificationDB.getNotification());
                        adapter.notifyDataSetChanged();
                        ad.cancel();


                    }
                });
        ad.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        ad.cancel();
                    }
                });

        ad.show();


    }


}
