package com.hutchsystems.hutchconnect.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.IncidentAdapter;
import com.hutchsystems.hutchconnect.beans.IncidentBean;
import com.hutchsystems.hutchconnect.common.DocumentType;
import com.hutchsystems.hutchconnect.common.SwipeHolder;
import com.hutchsystems.hutchconnect.common.SwipeOnItemTouchAdapter;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.EventDB;
import com.hutchsystems.hutchconnect.db.IncidentDB;

import java.util.ArrayList;

public class IncidentListFragment extends Fragment implements IncidentAdapter.IIncident, View.OnClickListener {
    RecyclerView rvIncident;
    ImageButton btnNewIncident, btnNoticeAndOrder, btnCVSAInspection, btnViolationTicket;
    IncidentAdapter adapter;
    LinearLayout layoutIncident;

    private OnFragmentInteractionListener mListener;

    public IncidentListFragment() {
        // Required empty public constructor
    }

    public static IncidentListFragment newInstance() {
        IncidentListFragment fragment = new IncidentListFragment();
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
        View view = inflater.inflate(R.layout.fragment_incident_list, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {
        incidentFg = false;
        IncidentAdapter.mListner = this;
        layoutIncident = (LinearLayout) view.findViewById(R.id.layoutIncident);
        rvIncident = (RecyclerView) view.findViewById(R.id.rvIncident);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        rvIncident.setLayoutManager(mLayoutManager);
        rvIncident.setItemAnimator(new DefaultItemAnimator());

        btnNewIncident = (ImageButton) view.findViewById(R.id.btnNewIncident);
        btnNewIncident.setOnClickListener(this);

        btnNoticeAndOrder = (ImageButton) view.findViewById(R.id.btnNoticeAndOrder);
        btnNoticeAndOrder.setOnClickListener(this);


        btnCVSAInspection = (ImageButton) view.findViewById(R.id.btnCVSAInspection);
        btnCVSAInspection.setOnClickListener(this);


        btnViolationTicket = (ImageButton) view.findViewById(R.id.btnViolationTicket);
        btnViolationTicket.setOnClickListener(this);

        String date = Utility.getPreviousDate(-15);
        ArrayList<IncidentBean> data = IncidentDB.getIncidentDetail(date);
        adapter = new IncidentAdapter(data,getActivity());
        rvIncident.setAdapter(adapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                IncidentDB.deleteDocument();
            }
        }).start();


        rvIncident.addOnItemTouchListener(new SwipeOnItemTouchAdapter(getActivity(), rvIncident, mLayoutManager) {
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

    @Override
    public void onViewIncident(int id, int type, String docType) {
        mListener.onLoadNewIncident(id, type, docType);
    }

    boolean incidentFg = false;

    @Override
    public void onClick(View view) {
        int currentStatus = EventDB.getCurrentDutyStatus(Utility.onScreenUserId);
        String message = getString(R.string.incident_add_alert);
        if (currentStatus == 4 || currentStatus == 5) {

        } else {
            Utility.showAlertMsg(message);
            return;
        }
        switch (view.getId()) {
            case R.id.btnNewIncident:
                if (!incidentFg) {
                    final Animation animationFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
                    layoutIncident.setVisibility(View.VISIBLE);
                    android.animation.ObjectAnimator.ofFloat(view, "rotation", 0, 45).start();
                    layoutIncident.startAnimation(animationFadeIn);
                } else {
                    final Animation animationFadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
                    layoutIncident.setVisibility(View.INVISIBLE);
                    android.animation.ObjectAnimator.ofFloat(view, "rotation", 45, 0).start();
                    layoutIncident.startAnimation(animationFadeOut);

                }
                incidentFg = !incidentFg;
                break;
            case R.id.btnNoticeAndOrder:
                incidentFg = !incidentFg;
                mListener.onLoadNewIncident(0, 0, DocumentType.DOCUMENT_NOTICE_ORDER);
                break;
            case R.id.btnCVSAInspection:
                incidentFg = !incidentFg;
                mListener.onLoadNewIncident(0, 1, DocumentType.DOCUMENT_CVSA_INSPECTION);
                break;
            case R.id.btnViolationTicket:
                incidentFg = !incidentFg;
                mListener.onLoadNewIncident(0, 2, DocumentType.DOCUMENT_VIOLATION_TICKET);
                break;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void onLoadNewIncident(int id, int type, String docType);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup viewGroup = (ViewGroup) getView();
            View view = inflater.inflate(R.layout.fragment_incident_list, viewGroup, false);
            viewGroup.removeAllViews();
            viewGroup.addView(view);
            initialize(view);

        } catch (Exception exe) {
        }

    }
}
