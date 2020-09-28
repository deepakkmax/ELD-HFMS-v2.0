package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.UnidentifiedAdapter;
import com.hutchsystems.hutchconnect.beans.DiagnosticIndicatorBean;
import com.hutchsystems.hutchconnect.beans.EventBean;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.DiagnosticMalfunction;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.EventDB;

import java.util.ArrayList;


public class UnidentifyFragment extends Fragment implements View.OnClickListener, InputInformationDialog.InputInformationDialogInterface, UnidentifiedAdapter.UnidentifiedInterface {
    public int selectedItemIndex = -1;
    ListView lvUnidentified;
    ArrayList<EventBean> eventList;
    UnidentifiedAdapter adapter;
    int driverId = 0;

    //Button btnAssume;
    //Button btnSkip;
    ImageButton fabAssume, fabAll;
    private OnFragmentInteractionListener mListener;
    boolean isAssume;
    //FloatingActionButton fabMenu;

    InputInformationDialog infosDialog;

    public UnidentifyFragment() {
        // Required empty public constructor

    }


    private void initialize(View view) {
        driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        lvUnidentified = (ListView) view.findViewById(R.id.lvUnidentified);
        //btnAssume = (Button) view.findViewById(R.id.btnAssume);
        //btnSkip = (Button) view.findViewById(R.id.btnSkip);
        fabAssume = (ImageButton) view.findViewById(R.id.fabAssume);
        fabAssume.setOnClickListener(this);

        fabAll = (ImageButton) view.findViewById(R.id.fabAll);
        fabAll.setOnClickListener(this);


//        fabMenu = (FloatingActionButton) getActivity().findViewById(R.id.fab);
//        if (fabMenu != null) {
//            fabMenu.setVisibility(View.GONE);
//        }

//        btnAssume.setOnClickListener(this);
//        btnSkip.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                if (fabMenu != null) {
////                    fabMenu.setVisibility(View.VISIBLE);
////                }
//                if (mListener != null) {
//                    mListener.onSkipAssumeRecord();
//                }
//            }
//        });
        EventBind();
    }

    private void EventBind() {
        selectedItemIndex = -1;
        eventList = EventDB.EventUnAssignedGet();
        adapter = new UnidentifiedAdapter(R.layout.unidentified_row_layout, eventList);
        adapter.mListener = this;
        lvUnidentified.setAdapter(adapter);

    }

    public static UnidentifyFragment newInstance() {
        //UnidentifyFragment fragment = new UnidentifyFragment();
        return new UnidentifyFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        isAssume = false;
        View view = inflater.inflate(R.layout.fragment_unidentify, container, false);
        initialize(view);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAssume:
            case R.id.fabAssume:
                if (isAssume) {

                    if (!Utility.HutchConnectStatusFg) {
                        Utility.showAlertMsg("App does not allow to change status while bluetooth is disconnected!");
                        return;
                    }

                    boolean status = false;
                    for (EventBean bean : eventList) {
                        if (bean.getChecked()) {
                            status = true;
                            break;
                        }
                    }
                    if (status) {
                        if (infosDialog == null) {
                            infosDialog = new InputInformationDialog();
                        }
                        if (!infosDialog.isVisible()) {
                            infosDialog.setTitle(getString(R.string.assuming_events_title));
                            infosDialog.mListener = this;
                            infosDialog.setCallWhenAssume(true);
                            infosDialog.show(getFragmentManager(), "infos_dialog");
                        }
                    } else {
                        Utility.showAlertMsg(getString(R.string.please_select_record));
                    }
                } else {
                    if (mListener != null) {
                        mListener.onSkipAssumeRecord();
                    }
                }

                break;
            case R.id.fabAll:
                allFg = !allFg;
                selectAll();
                break;
        }
    }

    boolean allFg = false;

    private void selectAll() {
        for (EventBean bean : eventList) {
            bean.setChecked(allFg);
        }
        if (allFg) {
            fabAll.setImageResource(R.drawable.ic_setup_failed);
        } else {

            fabAll.setVisibility(View.GONE);
            fabAll.setImageResource(R.drawable.ic_fab_check_double_circle);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void selectItem() {
        boolean status = false;
        for (EventBean bean : eventList) {
            if (bean.getChecked()) {
                status = true;
                break;
            }
        }

        if (status) {
            fabAll.setVisibility(View.VISIBLE);
            fabAssume.setImageResource(R.drawable.ic_fab_check_double);
            isAssume = true;
        } else {
            fabAll.setVisibility(View.GONE);
            fabAssume.setImageResource(R.drawable.ic_fab_skip);
            isAssume = false;
        }

        status = false;
        for (EventBean bean : eventList) {
            if (!bean.getChecked()) {
                status = true;
                break;
            }
        }

        if (status) {
            allFg = false;
            fabAll.setImageResource(R.drawable.ic_fab_check_double_circle);

        } else {
            allFg = true;
            fabAll.setImageResource(R.drawable.ic_setup_failed);

        }
    }

    @Override
    public void onInputFinished() {
        Log.i("Input", "fragment onInputFinished");
        infosDialog.mListener = null;
    }

    @Override
    public void onInputSaved(String shipId, String trailerId) {
        boolean status = false;
        for (EventBean bean : eventList) {
            if (bean.getChecked()) {
                int eventId = bean.get_id();
                String eventDate = Utility.dateOnlyStringGet(bean.getEventDateTime());
                int dailyLogId = DailyLogDB.getDailyLog(driverId, eventDate);

                if (dailyLogId == 0) {
                    dailyLogId = DailyLogDB.DailyLogCreateByDate(driverId, eventDate, shipId, trailerId, "");
                }
                // EventDB.EventUpdate(eventId, bean.getEventRecordOrigin(), 2, driverId, dailyLogId, shipId, trailerId);
                EventDB.EventUpdate(eventId, 2, driverId);
                DailyLogDB.DailyLogSyncRevert(driverId, bean.getDailyLogId());

                // Post rule
                MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // update codriverid
                EventDB.EventCopy(eventId, 4, 1, driverId, dailyLogId,0);
                DailyLogDB.DailyLogCertifyRevert(driverId, dailyLogId);

                // Post rule
                MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo);
                status = true;
            }
        }

        if (status) {
            // total minutes of unidentified driving for previous 7 days and current 24 hours
            String date = Utility.getDateTime(Utility.getCurrentDate(), -7);
            int seconds = EventDB.getUnidentifiedTime(date);

            if (seconds <= 15 * 60) {
                //clear Data diagnostic event for unidentified driving time if indicator is on for this event
                if (DiagnosticIndicatorBean.UnidentifiedDrivingDiagnosticFg) {
                    DiagnosticIndicatorBean.UnidentifiedDrivingDiagnosticFg = false;

                    // clear data diagnostic event for unidentified driving time
                    DiagnosticMalfunction.saveDiagnosticIndicatorByCode("5", 4, "UnidentifiedDrivingDiagnosticFg");
                }
            }

            date = Utility.getDateTime(Utility.getCurrentDate(), 0);
            Utility.UnidentifiedDrivingTime = EventDB.getUnidentifiedTime(date);

            EventBind();
            if (mListener != null) {
                mListener.onAssumeRecord();
            }
        } else
            Utility.showAlertMsg(getString(R.string.please_select_record));

        infosDialog.mListener = null;
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

        if (infosDialog != null) {
            infosDialog.mListener = null;
        }
        infosDialog = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void onAssumeRecord();

        void onSkipAssumeRecord();
    }
}
