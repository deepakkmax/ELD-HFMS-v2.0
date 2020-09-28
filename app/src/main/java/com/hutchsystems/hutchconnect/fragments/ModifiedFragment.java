package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.hutchsystems.hutchconnect.MainActivity;
import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.UnidentifiedAdapter;
import com.hutchsystems.hutchconnect.beans.EventBean;
import com.hutchsystems.hutchconnect.common.CommonTask;
import com.hutchsystems.hutchconnect.common.LogFile;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DailyLogDB;
import com.hutchsystems.hutchconnect.db.EventDB;
import com.hutchsystems.hutchconnect.db.LogDB;
import com.hutchsystems.hutchconnect.tasks.ModifiedSyncData;

import java.util.ArrayList;


public class ModifiedFragment extends Fragment {

    public int selectedItemIndex = -1;
    ListView lvData;
    ArrayList<EventBean> eventList;
    UnidentifiedAdapter adapter;
    int driverId = 0;
    ImageButton btnConfirm, btnReject;

    ModifiedSyncData.PostTaskListener<Boolean> postTaskListener = new ModifiedSyncData.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            showLoaderAnimation(false);
            if (result) {
                EventBind();
            } else {
                Utility.showErrorMessage(Utility.context);
            }
        }
    };

    public ModifiedFragment() {
        // Required empty public constructor
    }

    RelativeLayout rlLoadingPanel;

    private void showLoaderAnimation(boolean isShown) {
        try {
            if (isShown) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    showLoaderAnimation(false);
                                }
                            });
                        }
                    }
                }, 30000);
                rlLoadingPanel.setVisibility(View.VISIBLE);
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            } else {
                rlLoadingPanel.setVisibility(View.GONE);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::showLoaderAnimation Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            LogDB.writeLogs(ModifiedFragment.class.getName(),"showLoaderAnimation",e.getMessage(), Utility.printStackTrace(e));

        }
    }

    private void initialize(View view) {
        rlLoadingPanel = (RelativeLayout) getActivity().findViewById(R.id.loadingPanel);
        driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        lvData = (ListView) view.findViewById(R.id.lvData);
        btnConfirm = (ImageButton) view.findViewById(R.id.btnConfirm);
        btnReject = (ImageButton) view.findViewById(R.id.btnReject);
        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utility.HutchConnectStatusFg) {
                    Utility.showAlertMsg("App does not allow to change status while bluetooth is disconnected!");
                    return;
                }
                ArrayList<Integer> dailyLogIds = new ArrayList<>();
                for (EventBean bean : eventList) {
                    if (bean.getChecked()) {
                        int eventId = EventDB.getEventId(driverId, bean.getCreatedDate(), 1);
                        EventDB.EventUpdate(eventId, 1, driverId, bean.getDailyLogId());

                        eventId = bean.get_id();
                        EventDB.EventUpdate(eventId, 4, driverId, bean.getDailyLogId());
                        DailyLogDB.DailyLogCertifyRevert(driverId, bean.getDailyLogId());

                        // get dailylog id to post data of respective logs
                        if (!dailyLogIds.contains(bean.getDailyLogId())) {
                            dailyLogIds.add(bean.getDailyLogId());
                        }
                    }
                }

                // post data of each log's event edited
                for (Integer dailyLogId : dailyLogIds) {
                    MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo_DailyLogId, dailyLogId);
                }
                EventBind();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utility.HutchConnectStatusFg) {
                    Utility.showAlertMsg("App does not allow to change status while bluetooth is disconnected!");
                    return;
                }

                ArrayList<Integer> dailyLogIds = new ArrayList<>();
                for (EventBean bean : eventList) {
                    if (bean.getChecked()) {
                        int eventId = EventDB.getEventId(driverId, bean.getCreatedDate(), 1);
                        EventDB.EventUpdate(eventId, 2, driverId, bean.getDailyLogId());

                        eventId = bean.get_id();
                        //  EventDB.EventCopy(eventId, bean.getEventRecordOrigin(), 1, driverId, bean.getDailyLogId());
                        EventDB.EventUpdate(eventId, 1, driverId, bean.getDailyLogId());
                        DailyLogDB.DailyLogCertifyRevert(driverId, bean.getDailyLogId());

                        // get dailylog id to post data of respective logs
                        if (!dailyLogIds.contains(bean.getDailyLogId())) {
                            dailyLogIds.add(bean.getDailyLogId());
                        }
                    }
                }

                // post data of each log's event edited
                for (Integer dailyLogId : dailyLogIds) {
                    MainActivity.postData(CommonTask.Post_DailyLog_DriverInfo_DailyLogId, dailyLogId);
                }
                EventBind();
            }
        });

        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        EventBind();
        showLoaderAnimation(true);
        new ModifiedSyncData(postTaskListener).execute();
    }

    private void EventBind() {
        selectedItemIndex = -1;
        eventList = EventDB.EventEditRequestedGet(driverId);
        adapter = new UnidentifiedAdapter(R.layout.unidentified_row_layout, eventList);
        lvData.setAdapter(adapter);
    }

    public static ModifiedFragment newInstance() {
        return new ModifiedFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_modified, container, false);
        initialize(view);
        return view;
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

}
