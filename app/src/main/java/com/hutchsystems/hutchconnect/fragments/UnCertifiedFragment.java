package com.hutchsystems.hutchconnect.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.beans.DailyLogBean;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.EventDB;

import java.util.ArrayList;


public class UnCertifiedFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    public int selectedItemIndex = -1;
    ListView lvData;
    ArrayList<DailyLogBean> logList;
    UnCertifyLogAdapter adapter;
    int driverId = 0;
    //Button btnCertify;
    //Button btnSkip;

    ImageButton fabCertify, fabAll;
    boolean isCertify;

    public UnCertifiedFragment() {
        // Required empty public constructor
    }

    String logIds = "";

    private void initialize(View view) {

        driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        lvData = (ListView) view.findViewById(R.id.lvData);
        fabCertify = (ImageButton) view.findViewById(R.id.fabCertify);
        fabCertify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIds = "";

                // user is now allowed to skip certifying logs
                isCertify = true;
                if (isCertify) {
                    if (!Utility.HutchConnectStatusFg) {
                        Utility.showAlertMsg("App does not allow to change status while bluetooth is disconnected!");
                        return;
                    }

                    for (DailyLogBean bean : logList) {
                        if (bean.getCertifyFG() == 1) {
                            logIds += bean.get_id() + ",";
                        }
                    }
                    if (logIds.isEmpty()) {
                        Utility.showAlertMsg(getString(R.string.please_select_record));
                        return;
                    }
                    logIds = logIds.substring(0, logIds.length() - 1);

                    certifyLogBook();

                } else {
                    if (mListener != null) {
                        mListener.onSkipCertify();
                    }
                }
            }
        });


        fabAll = (ImageButton) view.findViewById(R.id.fabAll);

        fabAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allFg = !allFg;
                selectAll();
            }
        });

        boolean status = LogBind();

        if (!status)
            Utility.showAlertMsg("You have uncertified log(s). You must certify all proceeding.");
    }

    private boolean LogBind() {
        boolean status = false;
        selectedItemIndex = -1;
        logList = DailyLogDB.getUncertifiedDailyLog(driverId);

        if (logList.isEmpty()) {
            status = true;
            fabCertify.setVisibility(View.GONE);
        }

        adapter = new UnCertifyLogAdapter(R.layout.uncertified_row_layout, logList);
        lvData.setAdapter(adapter);
        return status;
    }

    public void certifyLogBook() {

        final AlertDialog ad = new AlertDialog.Builder(Utility.context)
                .create();
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle(getString(R.string.certify_log_title));
        ad.setIcon(R.drawable.ic_launcher);
        ad.setMessage(getString(R.string.certify_log_message));
        ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.certify),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {

                        if (DailyLogDB.DailyLogCertify("", driverId, logIds)) {
                            String[] logs = logIds.split(",");
                            // need to discuss about this should we enter multiple event related to multiple certification
                            for (int i = 0; i < logs.length; i++) {
                                int logId = Integer.parseInt(logs[i]);
                                int n = DailyLogDB.getCertifyCount(logId) + 1;
                                DailyLogDB.CertifyCountUpdate(logId, n);
                                if (n > 9)
                                    n = 9;
                                // to be discuss about event
                                //123 LogFile.write(UnCertifiedFragment.class.getName() + "::certifyLogBook: " + "Driver's " + n + "'th certification of a daily record" + " of driverId:" + driverId, LogFile.USER_INTERACTION, LogFile.DRIVEREVENT_LOG);
                                EventDB.EventCreate(Utility._CurrentDateTime, 4, n, getString(R.string.drivers) + n + getString(R.string.th_certification), 1, 1, logId, driverId, "", MainActivity.currentRule);
                            }
                        }
                        boolean certifyAllFg = LogBind();


                        if (mListener != null && certifyAllFg) {
                            mListener.onLogbookCertified();
                        }
                    }
                });
       /* ad.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.not_ready),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        ad.dismiss();
                    }
                });*/
        ad.show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {

            LogBind();
        }
    }

    // TODO: Rename and change types and number of parameters
    public static UnCertifiedFragment newInstance() {
        //UnCertifiedFragment fragment = new UnCertifiedFragment();
        return new UnCertifiedFragment();


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        isCertify = false;
        View view = inflater.inflate(R.layout.fragment_un_certified, container, false);
        initialize(view);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

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

    public void selectItem() {
        String logId = "";
        boolean status = false;
        for (DailyLogBean bean : logList) {
            if (bean.getCertifyFG() == 1) {
                logId += bean.get_id() + ",";
            } else {
                status = true;
            }
        }
        if (!logId.isEmpty()) {
            fabAll.setVisibility(View.VISIBLE);
            fabCertify.setVisibility(View.VISIBLE);
            //fabCertify.setImageResource(R.drawable.writing);
            isCertify = true;
        } else {
            fabAll.setVisibility(View.GONE);
            fabCertify.setVisibility(View.GONE);
            // fabCertify.setImageResource(R.drawable.ic_fab_skip);
            isCertify = false;
        }

        if (status) {
            allFg = false;
            fabAll.setImageResource(R.drawable.ic_fab_check_double_circle);
        } else {
            allFg = true;
            fabAll.setImageResource(R.drawable.ic_setup_failed);

        }
    }

    boolean allFg = false;

    private void selectAll() {
        for (DailyLogBean bean : logList) {
            bean.setCertifyFG(allFg ? 1 : 0);
        }
        if (allFg) {
            fabAll.setImageResource(R.drawable.ic_setup_failed);
        } else {

            fabAll.setVisibility(View.GONE);
            fabCertify.setVisibility(View.GONE);
            fabAll.setImageResource(R.drawable.ic_fab_check_double_circle);
        }
        adapter.notifyDataSetChanged();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void onLogbookCertified();

        void onSkipCertify();
    }

    /**
     * Created by Deepak.Sharma on 1/19/2016.
     */
    public class UnCertifyLogAdapter extends ArrayAdapter<DailyLogBean> {
        ArrayList<DailyLogBean> data;

        public UnCertifyLogAdapter(int resource,
                                   ArrayList<DailyLogBean> data) {
            super(Utility.context, resource, data);
            this.data = data;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            final ViewHolderItem viewHolder;
            final DailyLogBean bean = data.get(position);
            if (convertView == null || convertView.getTag() == null) {

                LayoutInflater inflater = (LayoutInflater) Utility.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(
                        R.layout.uncertified_row_layout, parent,
                        false);
                viewHolder = new ViewHolderItem();
                viewHolder.lRow = (LinearLayout) convertView.findViewById(R.id.lRow);
                viewHolder.swSerial = (ToggleButton) convertView.findViewById(R.id.swSerialNo);
                // all checkbox will be checked by default as per requirement
              /*  viewHolder.swSerial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            bean.setCertifyFG(1);
                            viewHolder.lRow.setBackground(getResources().getDrawable(R.drawable.list_row_checked));
                        } else {
                            viewHolder.lRow.setBackgroundResource(0);
                            bean.setCertifyFG(0);
                        }
                        selectItem();
                    }
                });*/
                viewHolder.tvLogDate = (TextView) convertView.findViewById(R.id.tvLogDate);
                viewHolder.tvOdometerReading = (TextView) convertView.findViewById(R.id.tvOdometerReading);
                viewHolder.tvLogData = (TextView) convertView.findViewById(R.id.tvLogData);
            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }

            int startOdometer = bean.getStartOdometerReading().isEmpty() ? 0 : Double.valueOf(bean.getStartOdometerReading()).intValue();
            int endOdometer = bean.getEndOdometerReading().isEmpty() ? 0 : Double.valueOf(bean.getEndOdometerReading()).intValue();
            int distance = endOdometer - startOdometer;
            String unit = "Kms";
            if (Utility._appSetting.getUnit() == 2) {
                startOdometer = Math.round(startOdometer * .62137f);
                endOdometer = Math.round(endOdometer * .62137f);
                distance = Math.round(distance * .62137f);
                unit = "Miles";
            }

            bean.setCertifyFG(1);
            viewHolder.swSerial.setTextOff((position + 1) + "");

            viewHolder.swSerial.setChecked(bean.getCertifyFG() == 1);
            viewHolder.tvLogDate.setText(bean.getLogDate());
            viewHolder.tvOdometerReading.setText("Odometer: " + startOdometer + " - " + endOdometer);
            viewHolder.tvLogData.setText(/*"Distance: " + distance +*/ "Trailer Id: " +
                    (bean.getTrailerId().isEmpty() ? "N/A" : bean.getTrailerId()) + ", Shipping Id: "
                    + (bean.getShippingId().isEmpty() ? "N/A" : bean.getShippingId()));
            return convertView;
        }
    }

    static class ViewHolderItem {
        TextView tvLogDate, tvOdometerReading, tvLogData;
        ToggleButton swSerial;
        LinearLayout lRow;
    }
}