package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.MaintenanceAdapter;
import com.hutchsystems.hutchconnect.adapters.MaintenanceCompletedAdapter;
import com.hutchsystems.hutchconnect.beans.ScheduleBean;
import com.hutchsystems.hutchconnect.beans.VehicleMaintenanceBean;
import com.hutchsystems.hutchconnect.common.SwipeHolder;
import com.hutchsystems.hutchconnect.common.SwipeOnItemTouchAdapter;
import com.hutchsystems.hutchconnect.db.ScheduleDB;
import com.hutchsystems.hutchconnect.db.VehicleMaintenanceDB;

import java.util.ArrayList;

public class MaintenanceDetailFragment extends Fragment implements MaintenanceAdapter.IVehicleMaintenance {
    RecyclerView rvMaintenanceDueDetail, rvCompletedMaintenance;
    ImageButton btnNewInspection;
    private OnFragmentInteractionListener mListener;

    public MaintenanceDetailFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MaintenanceDetailFragment newInstance() {
        MaintenanceDetailFragment fragment = new MaintenanceDetailFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_maintenance_detail, container, false);
        initializeTab(view);
        initialize(view);
        return view;
    }


    private void initializeTab(View view) {
        TabHost host = (TabHost) view.findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        View tabview = createTabView(host.getContext(), getString(R.string.upcoming_due_maintenance));
        TabHost.TabSpec spec = host.newTabSpec(getString(R.string.pending)).setIndicator(tabview);
        spec.setContent(R.id.tabPending);
        host.addTab(spec);

        //Tab 2
        tabview = createTabView(host.getContext(), getString(R.string.maintenance_completed));
        spec = host.newTabSpec(getString(R.string.inactive)).setIndicator(tabview);
        spec.setContent(R.id.tabCompleted);
        host.addTab(spec);

    }


    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context).inflate(R.layout.tabdesign, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    private void initialize(View view) {
        MaintenanceAdapter.mListner = this;
        btnNewInspection = (ImageButton) view.findViewById(R.id.btnNewInspection);
        btnNewInspection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedPosition > -1) {
                    ScheduleBean data = ((MaintenanceAdapter) rvMaintenanceDueDetail.getAdapter()).getData().get(selectedPosition);
                    mListener.onNewMaintenance(0, data.getScheduleId(), data.getDueOn());

                } else {
                    mListener.onNewMaintenance(0, 0, 0);
                }
            }
        });
        rvMaintenanceDueDetail = (RecyclerView) view.findViewById(R.id.rvMaintenanceDueDetail);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        rvMaintenanceDueDetail.setLayoutManager(mLayoutManager);
        rvMaintenanceDueDetail.setItemAnimator(new DefaultItemAnimator());

        ArrayList<ScheduleBean> data = ScheduleDB.MaintenanceDueGet();
        MaintenanceAdapter adapter = new MaintenanceAdapter(data,getActivity());
        rvMaintenanceDueDetail.setAdapter(adapter);



        rvMaintenanceDueDetail.addOnItemTouchListener(new SwipeOnItemTouchAdapter(getActivity(), rvMaintenanceDueDetail, mLayoutManager) {
            @Override
            public void onItemHiddenClick(SwipeHolder swipeHolder, int position) {

                //call reset to hide.
                swipeHolder.reset();
            }

            @Override
            public void onItemClick(int position) {

            }
        });




        rvCompletedMaintenance = (RecyclerView) view.findViewById(R.id.rvCompletedMaintenance);

        mLayoutManager = new LinearLayoutManager(getContext());
        rvCompletedMaintenance.setLayoutManager(mLayoutManager);
        rvCompletedMaintenance.setItemAnimator(new DefaultItemAnimator());

        ArrayList<VehicleMaintenanceBean> mdata = VehicleMaintenanceDB.MaintenanceGet();
        MaintenanceCompletedAdapter madapter = new MaintenanceCompletedAdapter(mdata,getActivity());
        rvCompletedMaintenance.setAdapter(madapter);

        rvCompletedMaintenance.addOnItemTouchListener(new SwipeOnItemTouchAdapter(getActivity(), rvCompletedMaintenance, mLayoutManager) {
            @Override
            public void onItemHiddenClick(SwipeHolder swipeHolder, int position) {

                //call reset to hide.
                swipeHolder.reset();
            }

            @Override
            public void onItemClick(int position) {

            }
        });

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

    int selectedPosition = -1;

    @Override
    public void selectItem(int position) {
       /* if (position == -1) {
            btnNewInspection.setVisibility(View.GONE);
        } else
            btnNewInspection.setVisibility(View.VISIBLE);*/
        selectedPosition = position;
        ArrayList<ScheduleBean> list = ((MaintenanceAdapter) rvMaintenanceDueDetail.getAdapter()).getData();
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setChecked(i == position);
        }
        rvMaintenanceDueDetail.getAdapter().notifyDataSetChanged();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onNewMaintenance(int id, int scheduleId, int dueOn);
    }
}
