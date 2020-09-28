package com.hutchsystems.hutchconnect.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.AppSettings;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hoursofservice.common.TimeUtility;
import com.hutchsystems.hoursofservice.model.ViolationModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class ViolationFragment extends Fragment {
    static ViolationFragment mInstance = null;

    public int selectedItemIndex = -1;
    ListView lvData;
    ArrayList<ViolationModel> violationList;
    ViolationAdapter adapter;
    int driverId = 0;

    public ViolationFragment() {
        // Required empty public constructor
    }

    private void initialize(View view) {
        driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        lvData = (ListView) view.findViewById(R.id.lvData);
        ViolationBind();
    }

    private void ViolationBind() {
        CheckCountry();
        selectedItemIndex = -1;
        violationList = new ArrayList<>();
        /*  Date currentDate = Utility.newDateOnly();
      Date nextDay = Utility.addSeconds(Utility.addDays(currentDate, 1), -1);
        for (int i = 0; i < 15; i++) {
            Date date = Utility.addDays(nextDay, -i);
            HourOfService.ViolationCalculation(date, driverId);
            violationList.addAll(HourOfService.violations);
        }*/
        //violationList = ViolationDB.getViolation(Utility.onScreenUserId);

      /*  int currentStatus = EventDB.getCurrentDutyStatus(Utility.onScreenUserId);
        if (currentStatus == 3) {*/
        for (ViolationModel bean : MainActivity.violationList) {
            // if (bean.getViolationDate().before(Utility.newDate()) && !bean.isVirtualFg()) violationList.add(bean);
            //check if violation exists in list when voilation exists for canada we show last 14 days violation and last 7 day violation for us
            if (bean.getViolationDate().before(Utility.newDate()) && bean.getViolationDate().after(Utility.parse(Utility.getPreviousDate(canadaFg ? -14 : -7))) && !bean.isVirtualFg())
                violationList.add(bean);
        }
        // }

        Collections.sort(violationList, ViolationModel.dateDesc);

        adapter = new ViolationAdapter(R.layout.violation_row_layout_, violationList);
        lvData.setAdapter(adapter);
    }


    public boolean canadaFg;

    private void CheckCountry() {
        String[] addresses = Utility.FullAddress.split(",");
        if (addresses.length >= 4) {
            if (addresses[3].trim().toUpperCase().equals("US")) {
                canadaFg = false;
            } else {
                canadaFg = true;
            }
        }
    }

/*
    private void tempViolation() {
        try {
            HourOfService.ViolationAdd("12(A)", Utility.parse("2016-01-11 09:20:00"), 180, true);
            HourOfService.ViolationAdd("13(B)", Utility.parse("2016-01-12 13:37:00"), 30, true);
            HourOfService.ViolationAdd("14(C)", Utility.parse("2016-01-12 18:43:00"), 75, true);
            HourOfService.ViolationAdd("16(A)", Utility.parse("2016-01-15 23:01:00"), 69, true);
            HourOfService.ViolationAdd("26(B)", Utility.parse("2016-01-19 07:29:00"), 256, true);
            HourOfService.ViolationAdd("25(A)", Utility.parse("2016-01-21 05:15:00"), 300, true);
        } catch (Exception exe) {
        }
    }*/

    public static ViolationFragment newInstance() {
        return new ViolationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_violation, container, false);
        initialize(view);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Created by Deepak.Sharma on 1/15/2016.
     */
    public class ViolationAdapter extends ArrayAdapter<ViolationModel> {

        ArrayList<ViolationModel> data;

        public ViolationAdapter(int resource,
                                ArrayList<ViolationModel> data) {
            super(Utility.context, resource, data);
            this.data = data;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            ViewHolderItem viewHolder;
            if (convertView == null || convertView.getTag() == null) {

                LayoutInflater inflater = (LayoutInflater) Utility.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(
                        R.layout.violation_row_layout_, parent,
                        false);
                viewHolder = new ViewHolderItem();
                viewHolder.tvSerialNo = (TextView) convertView.findViewById(R.id.tvSerialNo);
                viewHolder.tvViolationDateTime = (TextView) convertView.findViewById(R.id.tvViolationDateTime);
                viewHolder.tvViolationDateTime.setVisibility(View.GONE);
                viewHolder.tvViolationDescription = (TextView) convertView.findViewById(R.id.tvViolationDescription);
                //viewHolder.tvViolationDescription.setVisibility(View.GONE);
                viewHolder.tvViolationCode = (TextView) convertView.findViewById(R.id.tvViolationCode);
            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }

            ViolationModel bean = data.get(position);
            viewHolder.tvSerialNo.setText((position + 1) + "");
            viewHolder.tvViolationDateTime.setText(Utility.ConverDateFormat(bean.getViolationDate()));
            viewHolder.tvViolationCode.setText(bean.getRule());
            //viewHolder.tvViolationDescription.setText(getString(R.string.today_hours) + ": " + Utility.getTimeFromMinute(bean.getTotalMinutes()));

            String[] vInfo = Utility.violationInfoGet(bean.getRule());
            final String vTitle = vInfo[0]; //bean.getTitle();
            final String vExplanation = vInfo[1]; //bean.getExplanation();

            try {
                viewHolder.tvViolationDescription.setText(vTitle);
                String format = "hh:mm a"; //12hr
                if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                    format = "HH:mm";
                }

                String datetime = new SimpleDateFormat("MMM dd,yyyy").format(bean.getViolationDate()) + " " + new SimpleDateFormat(format).format(bean.getViolationDate());

                // calculate violation end date by adding violation duration to violation start time
                Date endDate = TimeUtility.addSeconds(bean.getViolationDate(), (int) bean.getViolationDuration()); //new SimpleDateFormat(format).format(bean.getViolationDate()) + "\n" + new SimpleDateFormat("MMM dd,yyyy").format(bean.getViolationDate());


                if (endDate.after(Utility.newDate())) {
                    endDate = Utility.newDate();
                }
                // date time in user readable format
                String toDate = new SimpleDateFormat("MMM dd,yyyy").format(endDate) + " " + new SimpleDateFormat(format).format(endDate);
                viewHolder.tvViolationDateTime.setText(datetime);
                viewHolder.tvViolationCode.setText(bean.getRule() + " at " + datetime + " to " + toDate);
            } catch (Exception exe) {

            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        final AlertDialog alertDialog = new AlertDialog.Builder(Utility.context).create();
                        alertDialog.setCancelable(true);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setTitle(vTitle);
                        alertDialog.setIcon(Utility.DIALOGBOX_ICON);
                        alertDialog.setMessage(vExplanation);
                        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.ok),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alertDialog.cancel();
                                    }
                                });
                        alertDialog.show();
                    } catch (Exception ex) {
                        LogFile.write("onViolationClick Alert Msg: " + ex.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                        LogDB.writeLogs(ViolationAdapter.class.getName(),"onViolationClick",ex.getMessage(), Utility.printStackTrace(ex));

                    }
                }
            });
            return convertView;
        }


    }

    class ViewHolderItem {
        TextView tvViolationDateTime, tvViolationDescription, tvViolationCode, tvSerialNo;
    }
}
