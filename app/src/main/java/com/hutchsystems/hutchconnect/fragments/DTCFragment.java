package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.DTCAdapter;
import com.hutchsystems.hutchconnect.beans.DTCBean;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.DTCDB;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DTCFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class DTCFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public DTCFragment() {
        // Required empty public constructor
    }

    ListView lvActiveDTCCode, lvInActiveDTCCode;

    public static DTCFragment newInstance() {
        DTCFragment fragment = new DTCFragment();
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
        View view = inflater.inflate(R.layout.fragment_dtc, container, false);
        initialize(view);
        initializeTab(view);
        return view;
    }

    private ArrayList<DTCBean> testdata() {
        ArrayList<DTCBean> list = new ArrayList<>();

        DTCBean dtcBean = new DTCBean();
        dtcBean.setSpn(16);
        dtcBean.setSpnDescription("Engine Fuel Filter Differential Pressure");
        dtcBean.setFmi(8);
        dtcBean.setFmiDescription("Mechanical System Not Responding");
        dtcBean.setDateTime(Utility.getCurrentDateTime());
        dtcBean.setProtocol("J1939");
        dtcBean.setOccurence(1);
        dtcBean.setStatus(1);
        list.add(dtcBean);

        dtcBean = new DTCBean();
        dtcBean.setSpn(18);
        dtcBean.setSpnDescription("Engine Fuel Pressure (Extended Range)");
        dtcBean.setFmi(1);
        dtcBean.setFmiDescription("Above Normal Operational Range - Severe");
        dtcBean.setDateTime(Utility.getCurrentDateTime());
        dtcBean.setProtocol("J1939");
        dtcBean.setOccurence(3);
        dtcBean.setStatus(1);
        list.add(dtcBean);

        return list;
    }

    int activeCount = 0, inactiveCount = 0;

    private void initialize(View view) {
        DTCDB.removeDTCPreviousDay();
        ArrayList<DTCBean> list =DTCDB.getDTCCode(); //testdata();
       /* list = testList();
        DTCDB.Save(list);*/
        ArrayList<DTCBean> activeList = new ArrayList<>();
        ArrayList<DTCBean> inactiveList = new ArrayList<>();
        for (DTCBean bean : list) {
            if (bean.getStatus() == 1) {
                activeList.add(bean);
            } else {
                inactiveList.add(bean);
            }
        }
        activeCount = activeList.size();
        inactiveCount = inactiveList.size();

        lvActiveDTCCode = (ListView) view.findViewById(R.id.lvActiveDTCCode);
        lvActiveDTCCode.setAdapter(new DTCAdapter(getContext(), R.layout.fragment_dtc, activeList));

        lvInActiveDTCCode = (ListView) view.findViewById(R.id.lvInActiveDTCCode);
        lvInActiveDTCCode.setAdapter(new DTCAdapter(getContext(), R.layout.fragment_dtc, inactiveList));
    }

    private void initializeTab(View view) {
        TabHost host = (TabHost) view.findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        View tabview = createTabView(host.getContext(), getString(R.string.active));
        TabHost.TabSpec spec = host.newTabSpec(getString(R.string.active)).setIndicator(tabview);
        spec.setContent(R.id.tabActive);
        host.addTab(spec);

        //Tab 2
        tabview = createTabView(host.getContext(), getString(R.string.inactive));
        spec = host.newTabSpec(getString(R.string.inactive)).setIndicator(tabview);
        spec.setContent(R.id.tabInActive);
        host.addTab(spec);

    }


    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context).inflate(R.layout.tabdesign, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

    }
}
