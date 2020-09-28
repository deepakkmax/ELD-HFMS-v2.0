package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.RecapAdapter;
import com.hutchsystems.hutchconnect.beans.RecapBean;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.EventDB;
import com.hutchsystems.hoursofservice.common.TimeUtility;
import com.hutchsystems.hoursofservice.model.DutyStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecapFragment extends Fragment {
    TextView tvTotalDriving, tvTotalOnDuty;
    RecyclerView rvRecap;
    RecapAdapter adapter;

    public RecapFragment() {
        // Required empty public constructor
    }

    public static RecapFragment newInstance() {
        RecapFragment fragment = new RecapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recap, container, false);
        initialize(view);
        return view;
    }


    private void initialize(View view) {
        tvTotalDriving = (TextView) view.findViewById(R.id.tvTotalDriving);
        tvTotalOnDuty = (TextView) view.findViewById(R.id.tvTotalOnDuty);
        rvRecap = (RecyclerView) view.findViewById(R.id.rvRecap);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        rvRecap.setLayoutManager(mLayoutManager);
        rvRecap.setItemAnimator(new DefaultItemAnimator());


        ArrayList<RecapBean> list = calculateHours();
        adapter = new RecapAdapter(list);
        rvRecap.setAdapter(adapter);
    }

    private ArrayList<RecapBean> calculateHours() {

        // check current rule of on screen user
        int currentRuleOnScreen = DailyLogDB.getCurrentRule(Utility.onScreenUserId);

        String startDate = Utility.getPreviousDateOnly(-6) + " 00:00:00";
        if (currentRuleOnScreen == 2) {
            startDate = Utility.getPreviousDateOnly(-13) + " 00:00:00";
        } else if (currentRuleOnScreen == 3) {
            startDate = Utility.getPreviousDateOnly(-7) + " 00:00:00";
        }


        // current date as per driver time zone
        Date currentDate = Utility.newDateOnly();

        // end time of current date
        Date toDate = toDate = Utility.newDate();

        ArrayList<DutyStatus> list = EventDB.DutyStatusGet(startDate, Utility.onScreenUserId, Utility.context);

        int driving = 0, onDuty = 0;
        Date logDate = currentDate;
        ArrayList<RecapBean> recapList = new ArrayList<>();

        // loop through all of the dutystatus
        for (int i = 0; i < list.size(); i++) {
            // pick duty status at index i
            DutyStatus item = list.get(i);

            // declare variable to hold next duty status
            DutyStatus nextItem = null;

            // if next duty status is same then increase value of i till we get differnt next duty status.
            while (i < list.size() - 1 && list.get(i + 1).getStatus() == item.getStatus()) { //&& list.get(i + 1).getRule() == item.getRule()
                i++;
            }

            // Date of duty status
            Date eventDate = item.getEventDateTime();

            // pick next Duty Status if exists
            nextItem = i == list.size() - 1 ? null : list.get(i + 1);


            // Date of next duty status if it exists else put to Date in next date
            Date nextDate = nextItem == null ? toDate : nextItem.getEventDateTime();

            // to check if next date and event date fall in different day
            int diff = (int) TimeUtility.getDiff(com.hutchsystems.hoursofservice.common.Utility.dateOnly(eventDate), com.hutchsystems.hoursofservice.common.Utility.dateOnly(nextDate), TimeUtility.Unit.DAY);

            // Duration of duty status
            //  double duration = nextDate.Subtract(eventDate).TotalSeconds;
            double duration = TimeUtility.getDiff(eventDate, nextDate, TimeUtility.Unit.SECONDS);


            if (diff == 0) {
                driving += item.getStatus() == 3 ? duration : 0;
                onDuty += item.getStatus() == 4 ? duration : 0;

            } else {
                // in case next event fall in different day
                for (int j = 0; j <= diff; j++) {
                    // get new event date as per new date
                    // for first record event date remain same
                    eventDate = (j == 0 ? eventDate : TimeUtility.addDays(com.hutchsystems.hoursofservice.common.Utility.dateOnly(eventDate), 1));

                    // get new next date
                    // for last record next date remain same
                    Date newNextDate = (j == diff ? nextDate : TimeUtility.addDays(com.hutchsystems.hoursofservice.common.Utility.dateOnly(eventDate), 1));

                    // get new duration
                    //   duration = newNextDate.Subtract(eventDate).TotalSeconds;
                    duration = TimeUtility.getDiff(eventDate, newNextDate, TimeUtility.Unit.SECONDS);

                    DutyStatus newItem = new DutyStatus(item);
                    newItem.setEventDateTime(eventDate);

                    logDate = Utility.dateOnlyGet(eventDate);
                    // if next date then reset hours changed
                    if (j > 0) {
                        driving = 0;
                        onDuty = 0;
                    }

                    driving += item.getStatus() == 3 ? duration : 0;
                    onDuty += item.getStatus() == 4 ? duration : 0;

                    if (j != diff) {
                        RecapBean bean = new RecapBean();
                        bean.setDate(logDate);
                        bean.setDriving(driving);
                        bean.setOnDuty(onDuty);
                        recapList.add(bean);
                    }

                }
            }
        }

        RecapBean bean = new RecapBean();
        bean.setDate(logDate);
        bean.setDriving(driving);
        bean.setOnDuty(onDuty);
        recapList.add(bean);

        int totalDriving = 0, totalOnduty = 0;
        for (RecapBean item : recapList) {
            totalDriving += item.getDriving();
            totalOnduty += item.getOnDuty();
        }

        tvTotalDriving.setText("Total Drivings: " + Utility.getTimeFromSecondsInMin(totalDriving));
        tvTotalOnDuty.setText("Total On Duty: " + Utility.getTimeFromSecondsInMin(totalOnduty));
        Collections.sort(recapList, RecapBean.dateDesc);
        return recapList;
    }

    private RecapBean getHours(ArrayList<DutyStatus> dutyStatusArrayList, String logDate) {
        RecapBean bean = new RecapBean();

        for (int i = 0; i < dutyStatusArrayList.size(); i++) {

        }

        return bean;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private OnFragmentInteractionListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
