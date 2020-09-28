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

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.adapters.GeofenceAdapter;
import com.hutchsystems.hutchconnect.beans.GeofenceBean;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.GeofenceDB;
import com.hutchsystems.hutchconnect.services.GeofenceTransitionsIntentService;

import java.util.ArrayList;

public class GeofenceDetailFragment extends Fragment implements GeofenceTransitionsIntentService.IGeofence {
    RecyclerView rvGeofenceDetail;
    private OnFragmentInteractionListener mListener;
    GeofenceAdapter adapter;

    public GeofenceDetailFragment() {
        // Required empty public constructor
    }

    public static GeofenceDetailFragment newInstance() {
        GeofenceDetailFragment fragment = new GeofenceDetailFragment();
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
        View view = inflater.inflate(R.layout.fragment_geofence_detail, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {
        GeofenceTransitionsIntentService.mListner = this;
        rvGeofenceDetail = (RecyclerView) view.findViewById(R.id.rvGeofenceDetail);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        rvGeofenceDetail.setLayoutManager(mLayoutManager);
        rvGeofenceDetail.setItemAnimator(new DefaultItemAnimator());
        bindGeofence();
    }

    private void bindGeofence() {

        ArrayList<GeofenceBean> data = GeofenceDB.GeofenceMonitorGet(Utility.onScreenUserId);
        adapter = new GeofenceAdapter(data);
        rvGeofenceDetail.setAdapter(adapter);
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

    @Override
    public void refresh() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bindGeofence();
                }
            });
        }
    }
}
